package helpdesk;

import java.util.ArrayList;
import java.util.Scanner;

public class ConsoleApp {
    private final HelpDesk helpDesk;
    private final Scanner scanner;
    private boolean running;

    public ConsoleApp(HelpDesk helpDesk) {
        if (helpDesk == null) {
            throw new IllegalArgumentException("HelpDesk is required.");
        }
        this.helpDesk = helpDesk;
        scanner = new Scanner(System.in);
    }

    public void run() {
        running = true;
        System.out.println("====================================================");
        System.out.println("       FiberNet IT / Internet Service HelpDesk");
        System.out.println("====================================================");
        System.out.println("Realistic demonstration data is loaded for this session.");

        while (running) {
            printMenu();
            int choice = readNumber("Choose an option: ");
            if (!running) {
                break;
            }

            try {
                handleChoice(choice);
            } catch (HelpDeskException exception) {
                System.out.println("Operation failed: " + exception.getMessage());
            } catch (IllegalArgumentException exception) {
                System.out.println("Invalid data: " + exception.getMessage());
            }
        }

        System.out.println("HelpDesk application closed.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("-------------------- MAIN MENU --------------------");
        System.out.println(" 1. Show system overview");
        System.out.println(" 2. List product and service offers");
        System.out.println(" 3. Register a customer");
        System.out.println(" 4. Add a product to a customer");
        System.out.println(" 5. List all customers");
        System.out.println(" 6. Search customers");
        System.out.println(" 7. Filter customers by category");
        System.out.println(" 8. Filter customers by product");
        System.out.println(" 9. Register a support ticket");
        System.out.println("10. List all tickets and statuses");
        System.out.println("11. List tickets for a support agent");
        System.out.println("12. Update a ticket status");
        System.out.println("13. View a customer's ticket history");
        System.out.println("14. List support agents");
        System.out.println(" 0. Exit");
    }

    private void handleChoice(int choice) throws HelpDeskException {
        switch (choice) {
            case 1:
                showOverview();
                break;
            case 2:
                listProducts();
                break;
            case 3:
                registerCustomer();
                break;
            case 4:
                addProductToCustomer();
                break;
            case 5:
                printCustomers(helpDesk.getCustomers());
                break;
            case 6:
                searchCustomers();
                break;
            case 7:
                filterCustomersByCategory();
                break;
            case 8:
                filterCustomersByProduct();
                break;
            case 9:
                registerTicket();
                break;
            case 10:
                printTickets(helpDesk.getTicketsInPriorityOrder());
                break;
            case 11:
                listTicketsForAgent();
                break;
            case 12:
                updateTicketStatus();
                break;
            case 13:
                showCustomerHistory();
                break;
            case 14:
                listSupportAgents();
                break;
            case 0:
                running = false;
                break;
            default:
                System.out.println("Please choose a number from the menu.");
        }
    }

    private void showOverview() {
        System.out.println();
        System.out.println("SYSTEM OVERVIEW");
        System.out.println("Customers: " + helpDesk.getCustomerCount());
        System.out.println("Products/services: " + helpDesk.getProductCount());
        System.out.println("Support agents: " + helpDesk.getSupportAgentCount());
        System.out.println("Registered tickets: " + helpDesk.getTicketCount());
        System.out.println("Active tickets: " + helpDesk.getActiveTicketCount());
        System.out.println("Queue order: Urgent -> High -> Standard");
    }

    private void listProducts() {
        printProducts(helpDesk.getProducts());
    }

    private void printProducts(ArrayList<Product> products) {
        System.out.println();
        System.out.println("PRODUCT / SERVICE OFFERS");
        if (products.isEmpty()) {
            System.out.println("No products match the selection.");
            return;
        }

        for (Product product : products) {
            System.out.println(product);
            System.out.println("   " + product.getDescription());
            System.out.println("   Available to: " + formatCategories(product.getEligibleCategories()));
        }
    }

    private String formatCategories(ArrayList<CustomerCategory> categories) {
        String result = "";
        for (int i = 0; i < categories.size(); i++) {
            if (i > 0) {
                result += ", ";
            }
            result += categories.get(i);
        }
        return result;
    }

    private void registerCustomer() throws HelpDeskException {
        System.out.println();
        System.out.println("REGISTER CUSTOMER");
        CustomerCategory category = chooseCustomerCategory();
        String id = readRequired("Customer ID: ");
        String name = readRequired("Customer or organization name: ");
        String email = readRequired("Email: ");
        String phone = readRequired("Phone: ");
        String address = readRequired("Address: ");

        Customer customer;
        if (category == CustomerCategory.BUSINESS) {
            String registrationNumber = readRequired("Company registration number: ");
            String contactPerson = readRequired("Contact person: ");
            customer = new BusinessCustomer(id, name, email, phone, address,
                    registrationNumber, contactPerson);
        } else if (category == CustomerCategory.OFFICIAL) {
            String institutionCode = readRequired("Institution code: ");
            String department = readRequired("Department: ");
            customer = new OfficialCustomer(id, name, email, phone, address,
                    institutionCode, department);
        } else {
            String personalNumber = readRequired("Personal number: ");
            customer = new ResidentialCustomer(id, name, email, phone, address, personalNumber);
        }

        helpDesk.registerCustomer(customer);
        System.out.println("Customer registered: " + customer);
        System.out.println("Support policy: " + customer.getSupportPolicy());
        System.out.println("Products offered to this category:");
        printProducts(helpDesk.getProductsForCategory(category));
    }

    private void addProductToCustomer() throws HelpDeskException {
        System.out.println();
        System.out.println("ADD PRODUCT TO CUSTOMER");
        printCustomers(helpDesk.getCustomers());
        String customerId = readRequired("Customer ID: ");
        Customer customer = helpDesk.getCustomer(customerId);
        System.out.println("Available products for " + customer.getCategory() + ":");
        printProducts(helpDesk.getProductsForCategory(customer.getCategory()));
        String productId = readRequired("Product ID to add: ");
        helpDesk.assignProductToCustomer(customerId, productId);
        System.out.println("Product added to " + customer.getName() + ".");
    }

    private void printCustomers(ArrayList<Customer> customers) {
        System.out.println();
        System.out.println("CUSTOMERS");
        if (customers.isEmpty()) {
            System.out.println("No customers match the selection.");
            return;
        }
        for (Customer customer : customers) {
            printCustomer(customer);
        }
    }

    private void printCustomer(Customer customer) {
        System.out.println(customer);
        System.out.println("   Email: " + customer.getEmail() + ", phone: " + customer.getPhone());
        System.out.println("   Address: " + customer.getAddress());
        System.out.println("   " + customer.getCategorySpecificInformation());
        System.out.println("   Policy: " + customer.getSupportPolicy());
        if (customer.getProducts().isEmpty()) {
            System.out.println("   Products: none");
        } else {
            System.out.println("   Products:");
            for (Product product : customer.getProducts()) {
                System.out.println("      - " + product.getId() + " | " + product.getName());
            }
        }
    }

    private void searchCustomers() {
        System.out.println();
        String keyword = readRequired("Search by ID, name, email, phone, address, or category ID data: ");
        printCustomers(helpDesk.searchCustomers(keyword));
    }

    private void filterCustomersByCategory() {
        System.out.println();
        CustomerCategory category = chooseCustomerCategory();
        printCustomers(helpDesk.filterCustomersByCategory(category));
    }

    private void filterCustomersByProduct() throws HelpDeskException {
        printProducts(helpDesk.getProducts());
        String productId = readRequired("Product ID: ");
        printCustomers(helpDesk.filterCustomersByProduct(productId));
    }

    private void registerTicket() throws HelpDeskException {
        System.out.println();
        System.out.println("REGISTER SUPPORT TICKET");
        printCustomers(helpDesk.getCustomers());
        String customerId = readRequired("Customer ID: ");
        Customer customer = helpDesk.getCustomer(customerId);
        if (customer.getProducts().isEmpty()) {
            throw new HelpDeskException("Add a product to this customer before creating a ticket.");
        }

        System.out.println("Customer products:");
        for (Product product : customer.getProducts()) {
            System.out.println("   " + product);
        }
        String productId = readRequired("Affected product ID: ");

        listSupportAgents();
        String agentId = readRequired("Responsible support agent ID: ");
        TicketType type = chooseTicketType();
        String ticketId = readRequired("Ticket ID: ");
        String title = readRequired("Short title: ");
        String description = readRequired("Problem/request description: ");

        Ticket ticket = helpDesk.createTicket(ticketId, customerId, productId, agentId,
                type, title, description);
        System.out.println("Ticket registered with " + ticket.getPriority() + " priority:");
        System.out.println(ticket.getDetails());
    }

    private void printTickets(ArrayList<Ticket> tickets) {
        System.out.println();
        System.out.println("TICKETS (CATEGORY PRIORITY ORDER)");
        if (tickets.isEmpty()) {
            System.out.println("No tickets match the selection.");
            return;
        }
        for (Ticket ticket : tickets) {
            System.out.println(ticket.getDetails());
        }
    }

    private void listTicketsForAgent() throws HelpDeskException {
        listSupportAgents();
        String agentId = readRequired("Support agent ID: ");
        printTickets(helpDesk.getTicketsForAgent(agentId));
    }

    private void updateTicketStatus() throws HelpDeskException {
        printTickets(helpDesk.getTicketsInPriorityOrder());
        String ticketId = readRequired("Ticket ID to update: ");
        TicketStatus status = chooseTicketStatus();
        String note = readRequired("Status change note: ");
        helpDesk.updateTicketStatus(ticketId, status, note);
        System.out.println("Ticket status updated.");
        System.out.println(helpDesk.getTicket(ticketId).getDetails());
    }

    private void showCustomerHistory() throws HelpDeskException {
        printCustomers(helpDesk.getCustomers());
        String customerId = readRequired("Customer ID: ");
        Customer customer = helpDesk.getCustomer(customerId);
        System.out.println();
        System.out.println("TICKET / COMPLAINT / REQUEST HISTORY FOR " + customer.getName());
        if (customer.getTicketHistory().isEmpty()) {
            System.out.println("No history is registered for this customer.");
            return;
        }

        for (Ticket ticket : customer.getTicketHistory()) {
            System.out.println(ticket.getDetails());
            System.out.println("   Status history:");
            for (StatusChange change : ticket.getStatusHistory()) {
                System.out.println("      " + change);
            }
        }
    }

    private void listSupportAgents() {
        System.out.println();
        System.out.println("SUPPORT AGENTS");
        for (SupportAgent agent : helpDesk.getSupportAgents()) {
            System.out.println(agent);
        }
    }

    private CustomerCategory chooseCustomerCategory() {
        System.out.println("1. Business");
        System.out.println("2. Official / institutional");
        System.out.println("3. Residential");
        while (running) {
            int choice = readNumber("Customer category: ");
            if (choice == 1) {
                return CustomerCategory.BUSINESS;
            }
            if (choice == 2) {
                return CustomerCategory.OFFICIAL;
            }
            if (choice == 3) {
                return CustomerCategory.RESIDENTIAL;
            }
            System.out.println("Choose category 1, 2, or 3.");
        }
        return CustomerCategory.RESIDENTIAL;
    }

    private TicketType chooseTicketType() {
        System.out.println("1. Technical problem");
        System.out.println("2. Service request");
        System.out.println("3. Complaint");
        while (running) {
            int choice = readNumber("Ticket type: ");
            if (choice == 1) {
                return TicketType.TECHNICAL_PROBLEM;
            }
            if (choice == 2) {
                return TicketType.SERVICE_REQUEST;
            }
            if (choice == 3) {
                return TicketType.COMPLAINT;
            }
            System.out.println("Choose type 1, 2, or 3.");
        }
        return TicketType.TECHNICAL_PROBLEM;
    }

    private TicketStatus chooseTicketStatus() {
        TicketStatus[] statuses = TicketStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + ". " + statuses[i]);
        }
        while (running) {
            int choice = readNumber("New status: ");
            if (choice >= 1 && choice <= statuses.length) {
                return statuses[choice - 1];
            }
            System.out.println("Choose a status from the list.");
        }
        return TicketStatus.OPEN;
    }

    private int readNumber(String prompt) {
        while (running) {
            String input = readLine(prompt);
            if (!running) {
                return 0;
            }
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a whole number.");
            }
        }
        return 0;
    }

    private String readRequired(String prompt) {
        while (running) {
            String input = readLine(prompt).trim();
            if (!running || !input.isEmpty()) {
                return input;
            }
            System.out.println("This value is required.");
        }
        return "";
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            running = false;
            return "";
        }
        return scanner.nextLine();
    }
}
