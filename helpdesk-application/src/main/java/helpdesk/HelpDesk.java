package helpdesk;

import java.util.ArrayList;

public class HelpDesk {
    public static final String UNASSIGNED_AGENT_FILTER = "UNASSIGNED";

    private final Registry<Customer> customers;
    private final Registry<Product> products;
    private final Registry<SupportAgent> supportAgents;
    private final Registry<Ticket> tickets;
    private int nextCustomerNumber;
    private int nextTicketNumber;

    public HelpDesk() {
        customers = new Registry<Customer>();
        products = new Registry<Product>();
        supportAgents = new Registry<SupportAgent>();
        tickets = new Registry<Ticket>();
        nextCustomerNumber = 1001;
        nextTicketNumber = 1001;
    }

    public void registerCustomer(Customer customer) throws HelpDeskException {
        customers.add(customer);
    }

    public Customer createCustomer(CustomerCategory category, String name, String email,
            String phone, String address, String firstCategoryValue,
            String secondCategoryValue) throws HelpDeskException {
        String customerId = generateCustomerId();
        Customer customer;

        if (category == CustomerCategory.BUSINESS) {
            customer = new BusinessCustomer(customerId, name, email, phone, address,
                    firstCategoryValue, secondCategoryValue);
        } else if (category == CustomerCategory.OFFICIAL) {
            customer = new OfficialCustomer(customerId, name, email, phone, address,
                    firstCategoryValue, secondCategoryValue);
        } else if (category == CustomerCategory.RESIDENTIAL) {
            customer = new ResidentialCustomer(customerId, name, email, phone, address,
                    firstCategoryValue);
        } else {
            throw new IllegalArgumentException("Customer category is required.");
        }

        registerCustomer(customer);
        return customer;
    }

    private String generateCustomerId() {
        String customerId;
        do {
            customerId = String.format("C-%04d", nextCustomerNumber);
            nextCustomerNumber++;
        } while (customers.findById(customerId) != null);
        return customerId;
    }

    public void updateCustomer(String customerId, String name, String email, String phone,
            String address, String categoryValue1, String categoryValue2)
            throws HelpDeskException {
        Customer customer = getCustomer(customerId);
        customer.updateDetails(name, email, phone, address);
        customer.updateCategoryInformation(categoryValue1, categoryValue2);
    }

    public void registerProduct(Product product) throws HelpDeskException {
        products.add(product);
    }

    public void registerSupportAgent(SupportAgent agent) throws HelpDeskException {
        supportAgents.add(agent);
    }

    public void assignProductToCustomer(String customerId, String productId)
            throws HelpDeskException {
        Customer customer = getCustomer(customerId);
        Product product = getProduct(productId);

        if (!product.isAvailableTo(customer.getCategory())) {
            throw new HelpDeskException(product.getName() + " is not offered to "
                    + customer.getCategory() + " customers.");
        }
        customer.addProduct(product);
    }

    public Ticket createTicket(String customerId, String productId, TicketType type,
            String title, String description) throws HelpDeskException {
        Customer customer = getCustomer(customerId);
        Product product = getProduct(productId);

        if (!customer.hasProduct(productId)) {
            throw new HelpDeskException(
                    "The selected product is not associated with this customer.");
        }

        String ticketId = generateTicketId();
        TreatmentPriority priority = customer.getTreatmentPriority();
        Ticket ticket = new Ticket(ticketId, customer, product, type, title,
                description, priority);
        tickets.add(ticket);
        customer.addTicketToHistory(ticket);
        return ticket;
    }

    private String generateTicketId() {
        String ticketId;
        do {
            ticketId = String.format("T-%04d", nextTicketNumber);
            nextTicketNumber++;
        } while (tickets.findById(ticketId) != null);
        return ticketId;
    }

    public void assignAgentToTicket(String ticketId, String agentId) throws HelpDeskException {
        Ticket ticket = getTicket(ticketId);
        SupportAgent newAgent = getSupportAgent(agentId);
        SupportAgent previousAgent = ticket.getResponsibleAgent();
        if (previousAgent == newAgent) {
            throw new HelpDeskException("This agent is already responsible for the ticket.");
        }
        if (previousAgent != null) {
            previousAgent.removeTicket(ticket);
        }
        ticket.reassignAgent(newAgent);
        newAgent.assignTicket(ticket);
    }

    public void updateTicketStatus(String ticketId, TicketStatus newStatus, String note)
            throws HelpDeskException {
        Ticket ticket = getTicket(ticketId);
        ticket.updateStatus(newStatus, note);
    }

    public Customer getCustomer(String id) throws HelpDeskException {
        return customers.getRequired(id, "Customer");
    }

    public Product getProduct(String id) throws HelpDeskException {
        return products.getRequired(id, "Product");
    }

    public SupportAgent getSupportAgent(String id) throws HelpDeskException {
        return supportAgents.getRequired(id, "Support agent");
    }

    public Ticket getTicket(String id) throws HelpDeskException {
        return tickets.getRequired(id, "Ticket");
    }

    public ArrayList<Customer> getCustomers() {
        return customers.getAll();
    }

    public ArrayList<Product> getProducts() {
        return products.getAll();
    }

    public ArrayList<SupportAgent> getSupportAgents() {
        return supportAgents.getAll();
    }

    public ArrayList<Ticket> getTickets() {
        return tickets.getAll();
    }

    public ArrayList<Customer> searchCustomers(String keyword) {
        ArrayList<Customer> results = new ArrayList<Customer>();
        for (Customer customer : customers.getAll()) {
            if (customer.matchesKeyword(keyword)) {
                results.add(customer);
            }
        }
        return results;
    }

    public ArrayList<Customer> filterCustomersByCategory(CustomerCategory category) {
        ArrayList<Customer> results = new ArrayList<Customer>();
        for (Customer customer : customers.getAll()) {
            if (customer.getCategory() == category) {
                results.add(customer);
            }
        }
        return results;
    }

    public ArrayList<Customer> filterCustomersByProduct(String productId)
            throws HelpDeskException {
        getProduct(productId);
        ArrayList<Customer> results = new ArrayList<Customer>();
        for (Customer customer : customers.getAll()) {
            if (customer.hasProduct(productId)) {
                results.add(customer);
            }
        }
        return results;
    }

    public ArrayList<Product> getProductsForCategory(CustomerCategory category) {
        ArrayList<Product> results = new ArrayList<Product>();
        for (Product product : products.getAll()) {
            if (product.isAvailableTo(category)) {
                results.add(product);
            }
        }
        return results;
    }

    public ArrayList<Ticket> getTicketsForAgent(String agentId) throws HelpDeskException {
        SupportAgent agent = getSupportAgent(agentId);
        return agent.getAssignedTickets();
    }

    public ArrayList<Ticket> filterTickets(String keyword, TicketStatus status, String agentId) {
        ArrayList<Ticket> results = new ArrayList<Ticket>();
        String searchText = keyword == null ? "" : keyword.trim().toLowerCase();

        for (Ticket ticket : getTicketsInPriorityOrder()) {
            boolean matchesText = searchText.isEmpty()
                    || ticket.getId().toLowerCase().contains(searchText)
                    || ticket.getTitle().toLowerCase().contains(searchText)
                    || ticket.getDescription().toLowerCase().contains(searchText)
                    || ticket.getCustomer().getName().toLowerCase().contains(searchText)
                    || ticket.getProduct().getName().toLowerCase().contains(searchText);
            boolean matchesStatus = status == null || ticket.getStatus() == status;
            boolean matchesAgent = matchesAgentFilter(ticket, agentId);

            if (matchesText && matchesStatus && matchesAgent) {
                results.add(ticket);
            }
        }
        return results;
    }

    private boolean matchesAgentFilter(Ticket ticket, String agentId) {
        if (agentId == null || agentId.isEmpty()) {
            return true;
        }
        if (UNASSIGNED_AGENT_FILTER.equals(agentId)) {
            return ticket.getResponsibleAgent() == null;
        }
        return ticket.getResponsibleAgent() != null
                && ticket.getResponsibleAgent().getId().equalsIgnoreCase(agentId);
    }

    public ArrayList<Ticket> getTicketsInPriorityOrder() {
        ArrayList<Ticket> orderedTickets = new ArrayList<Ticket>();
        ArrayList<Ticket> allTickets = tickets.getAll();

        for (TreatmentPriority priority : TreatmentPriority.values()) {
            for (Ticket ticket : allTickets) {
                if (ticket.getPriority() == priority) {
                    orderedTickets.add(ticket);
                }
            }
        }
        return orderedTickets;
    }

    public int getCustomerCount() {
        return customers.size();
    }

    public int getProductCount() {
        return products.size();
    }

    public int getSupportAgentCount() {
        return supportAgents.size();
    }

    public int getTicketCount() {
        return tickets.size();
    }

    public int getActiveTicketCount() {
        int count = 0;
        for (Ticket ticket : tickets.getAll()) {
            if (ticket.getStatus() != TicketStatus.CLOSED) {
                count++;
            }
        }
        return count;
    }
}
