package helpdesk;

public enum TicketStatus {
    OPEN("Open"),
    IN_PROGRESS("In progress"),
    WAITING_FOR_CUSTOMER("Waiting for customer"),
    RESOLVED("Resolved"),
    CLOSED("Closed");

    private final String displayName;

    TicketStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
