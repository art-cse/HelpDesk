package helpdesk;

import java.util.ArrayList;

public class SupportAgent implements Identifiable {
    private final String id;
    private final String fullName;
    private final String email;
    private final ProductType specialization;
    private final ArrayList<Ticket> assignedTickets;

    public SupportAgent(String id, String fullName, String email, ProductType specialization) {
        this.id = requireText(id, "Agent ID");
        this.fullName = requireText(fullName, "Agent name");
        this.email = requireText(email, "Agent email");
        if (specialization == null) {
            throw new IllegalArgumentException("Agent specialization is required.");
        }
        this.specialization = specialization;
        assignedTickets = new ArrayList<Ticket>();
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

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public ProductType getSpecialization() {
        return specialization;
    }

    public ArrayList<Ticket> getAssignedTickets() {
        return new ArrayList<Ticket>(assignedTickets);
    }

    void assignTicket(Ticket ticket) {
        if (ticket != null && !assignedTickets.contains(ticket)) {
            assignedTickets.add(ticket);
        }
    }

    void removeTicket(Ticket ticket) {
        assignedTickets.remove(ticket);
    }

    @Override
    public String toString() {
        return id + " | " + fullName + " | specializes in: " + specialization
                + " | assigned tickets: " + assignedTickets.size();
    }
}
