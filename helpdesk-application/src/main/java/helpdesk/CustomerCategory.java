package helpdesk;

public enum CustomerCategory {
    BUSINESS("Business"),
    OFFICIAL("Official / institutional"),
    RESIDENTIAL("Residential");

    private final String displayName;

    CustomerCategory(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
