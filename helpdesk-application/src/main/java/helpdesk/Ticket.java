package helpdesk;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Ticket implements Identifiable {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String id;
    private final Customer customer;
    private final Product product;
    private SupportAgent responsibleAgent;
    private final TicketType type;
    private final String title;
    private final String description;
    private final TreatmentPriority priority;
    private final LocalDateTime createdAt;
    private TicketStatus status;
    private final ArrayList<StatusChange> statusHistory;

    public Ticket(String id, Customer customer, Product product, SupportAgent responsibleAgent,
            TicketType type, String title, String description, TreatmentPriority priority) {
        this.id = requireText(id, "Ticket ID");
        if (customer == null || product == null || responsibleAgent == null) {
            throw new IllegalArgumentException("Customer, product, and responsible agent are required.");
        }
        if (type == null || priority == null) {
            throw new IllegalArgumentException("Ticket type and priority are required.");
        }
        this.customer = customer;
        this.product = product;
        this.responsibleAgent = responsibleAgent;
        this.type = type;
        this.title = requireText(title, "Ticket title");
        this.description = requireText(description, "Ticket description");
        this.priority = priority;
        createdAt = LocalDateTime.now();
        status = TicketStatus.OPEN;
        statusHistory = new ArrayList<StatusChange>();
        statusHistory.add(new StatusChange(null, TicketStatus.OPEN,
                "Ticket created and assigned to " + responsibleAgent.getFullName()));
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    @Override
    public String getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Product getProduct() {
        return product;
    }

    public SupportAgent getResponsibleAgent() {
        return responsibleAgent;
    }

    void reassignAgent(SupportAgent newAgent) {
        if (newAgent == null) {
            throw new IllegalArgumentException("Responsible agent is required.");
        }
        responsibleAgent = newAgent;
    }

    public TicketType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TreatmentPriority getPriority() {
        return priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public ArrayList<StatusChange> getStatusHistory() {
        return new ArrayList<StatusChange>(statusHistory);
    }

    public void updateStatus(TicketStatus newStatus, String note) throws HelpDeskException {
        if (newStatus == null) {
            throw new HelpDeskException("A new ticket status is required.");
        }
        if (newStatus == status) {
            throw new HelpDeskException("The ticket already has status " + status + ".");
        }
        if (!isAllowedTransition(status, newStatus)) {
            throw new HelpDeskException("Status cannot change from " + status + " to " + newStatus + ".");
        }

        TicketStatus previousStatus = status;
        status = newStatus;
        statusHistory.add(new StatusChange(previousStatus, newStatus, note));
    }

    private boolean isAllowedTransition(TicketStatus current, TicketStatus next) {
        switch (current) {
            case OPEN:
                return next == TicketStatus.IN_PROGRESS;
            case IN_PROGRESS:
                return next == TicketStatus.WAITING_FOR_CUSTOMER
                        || next == TicketStatus.RESOLVED;
            case WAITING_FOR_CUSTOMER:
                return next == TicketStatus.IN_PROGRESS
                        || next == TicketStatus.RESOLVED;
            case RESOLVED:
                return next == TicketStatus.IN_PROGRESS || next == TicketStatus.CLOSED;
            case CLOSED:
                return false;
            default:
                return false;
        }
    }

    public String getSummary() {
        return id + " | " + priority + " | " + status + " | " + title
                + " | customer: " + customer.getName()
                + " | agent: " + responsibleAgent.getFullName();
    }

    public String getDetails() {
        return getSummary()
                + System.lineSeparator() + "Type: " + type
                + System.lineSeparator() + "Product: " + product.getName()
                + System.lineSeparator() + "Created: " + createdAt.format(FORMATTER)
                + System.lineSeparator() + System.lineSeparator() + description;
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
