package helpdesk;

public enum ProductType {
    INTERNET("Internet service"),
    NETWORKING("Networking"),
    EQUIPMENT("Equipment / router support"),
    BUSINESS_CONNECTIVITY("Business connectivity"),
    SECURITY("Security service");

    private final String displayName;

    ProductType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
