package helpdesk;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StatusChange {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TicketStatus previousStatus;
    private final TicketStatus newStatus;
    private final LocalDateTime changedAt;
    private final String note;

    StatusChange(TicketStatus previousStatus, TicketStatus newStatus, String note) {
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        changedAt = LocalDateTime.now();
        if (note == null || note.trim().isEmpty()) {
            this.note = "No note supplied";
        } else {
            this.note = note.trim();
        }
    }

    public TicketStatus getPreviousStatus() {
        return previousStatus;
    }

    public TicketStatus getNewStatus() {
        return newStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public String getNote() {
        return note;
    }

    @Override
    public String toString() {
        String from = previousStatus == null ? "Created" : previousStatus.toString();
        return changedAt.format(FORMATTER) + " | " + from + " -> " + newStatus + " | " + note;
    }
}
