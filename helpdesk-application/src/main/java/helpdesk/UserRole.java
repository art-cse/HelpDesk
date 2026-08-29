package helpdesk;

public enum UserRole {
    ADMIN("Administrator"),
    AGENT("Support Agent"),
    CUSTOMER("Customer");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
