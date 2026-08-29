package helpdesk;

public enum TicketType {
    TECHNICAL_PROBLEM("Technical problem"),
    SERVICE_REQUEST("Service request"),
    COMPLAINT("Complaint");

    private final String displayName;

    TicketType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
