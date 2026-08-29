package helpdesk;

public enum TreatmentPriority {
    URGENT("Urgent"),
    HIGH("High"),
    STANDARD("Standard");

    private final String displayName;

    TreatmentPriority(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
