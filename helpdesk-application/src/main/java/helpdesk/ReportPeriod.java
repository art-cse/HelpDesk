package helpdesk;

import java.time.LocalDate;
import java.time.LocalDateTime;

public enum ReportPeriod {
    ALL_TIME("All time"),
    LAST_30_DAYS("Last 30 days"),
    LAST_60_DAYS("Last 60 days"),
    CURRENT_QUARTER("Current quarter"),
    CURRENT_YEAR("Current year");

    private final String displayName;

    ReportPeriod(String displayName) {
        this.displayName = displayName;
    }

    public LocalDate getStartDate(LocalDate today) {
        switch (this) {
            case LAST_30_DAYS:
                return today.minusDays(29);
            case LAST_60_DAYS:
                return today.minusDays(59);
            case CURRENT_QUARTER:
                int firstMonth = ((today.getMonthValue() - 1) / 3) * 3 + 1;
                return LocalDate.of(today.getYear(), firstMonth, 1);
            case CURRENT_YEAR:
                return LocalDate.of(today.getYear(), 1, 1);
            default:
                return null;
        }
    }

    public boolean includes(LocalDateTime createdAt, LocalDate today) {
        LocalDate createdDate = createdAt.toLocalDate();
        LocalDate startDate = getStartDate(today);
        return !createdDate.isAfter(today)
                && (startDate == null || !createdDate.isBefore(startDate));
    }

    @Override
    public String toString() {
        return displayName;
    }
}
