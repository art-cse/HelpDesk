package helpdesk;

public class HelpDeskTest {
    public static void main(String[] args) throws Exception {
        HelpDeskTest test = new HelpDeskTest();
        test.testAutomaticCustomerIdsAndPolymorphism();
        test.testTicketCreationAndAssignment();
        test.testStatusHistoryAndValidation();
        System.out.println("All HelpDesk domain tests passed.");
    }

    private void testAutomaticCustomerIdsAndPolymorphism() throws Exception {
        HelpDesk helpDesk = new HelpDesk();

        Customer business = helpDesk.createCustomer(CustomerCategory.BUSINESS,
                "North Trade", "business@example.com", "100", "Business Street",
                "REG-900", "Mira Basha");
        Customer official = helpDesk.createCustomer(CustomerCategory.OFFICIAL,
                "Public Archive", "archive@example.com", "200", "Official Street",
                "INST-77", "Records Department");
        Customer residential = helpDesk.createCustomer(CustomerCategory.RESIDENTIAL,
                "Rina Kola", "rina@example.com", "300", "Home Street",
                "PERSON-55", "");

        check("C-1001".equals(business.getId()), "First customer ID should be automatic.");
        check("C-1002".equals(official.getId()), "Customer IDs should be sequential.");
        check("C-1003".equals(residential.getId()), "Customer IDs should be unique.");
        check(business instanceof BusinessCustomer
                && business.getTreatmentPriority() == TreatmentPriority.HIGH,
                "Business customers should use their overridden priority.");
        check(official instanceof OfficialCustomer
                && official.getTreatmentPriority() == TreatmentPriority.URGENT,
                "Official customers should use their overridden priority.");
        check(residential instanceof ResidentialCustomer
                && residential.getTreatmentPriority() == TreatmentPriority.STANDARD,
                "Residential customers should use their overridden priority.");
        check(helpDesk.searchCustomers("INST-77").contains(official),
                "Search should include category-specific identification.");
        check(helpDesk.filterCustomersByCategory(CustomerCategory.BUSINESS).contains(business),
                "Customer category filtering should work.");

        System.out.println("PASS: automatic customer IDs and polymorphism");
    }

    private void testTicketCreationAndAssignment() throws Exception {
        HelpDesk helpDesk = createSmallHelpDesk();
        Customer customer = helpDesk.getCustomer("C-1001");

        Ticket firstTicket = helpDesk.createTicket(customer.getId(), "P-ROUTER",
                TicketType.TECHNICAL_PROBLEM, "No connection",
                "The router has no internet signal.");
        Ticket secondTicket = helpDesk.createTicket(customer.getId(), "P-ROUTER",
                TicketType.SERVICE_REQUEST, "Router check",
                "Please check the router configuration.");

        check("T-1001".equals(firstTicket.getId()), "First ticket ID should be automatic.");
        check("T-1002".equals(secondTicket.getId()), "Ticket IDs should be sequential.");
        check(firstTicket.getResponsibleAgent() == null,
                "A new ticket should start unassigned.");
        check(firstTicket.getPriority() == customer.getTreatmentPriority(),
                "Ticket priority should come from the customer subtype.");

        helpDesk.assignAgentToTicket(firstTicket.getId(), "A-1");
        SupportAgent agent = helpDesk.getSupportAgent("A-1");
        check(firstTicket.getResponsibleAgent() == agent,
                "Assignment should update the ticket.");
        check(agent.getAssignedTickets().contains(firstTicket),
                "Assignment should update the support agent.");
        check(customer.getTicketHistory().contains(firstTicket),
                "The customer should retain the ticket in their history.");

        System.out.println("PASS: ticket creation and agent assignment");
    }

    private void testStatusHistoryAndValidation() throws Exception {
        HelpDesk helpDesk = createSmallHelpDesk();
        Ticket ticket = helpDesk.createTicket("C-1001", "P-ROUTER",
                TicketType.COMPLAINT, "Router complaint", "Replacement is delayed.");
        helpDesk.assignAgentToTicket(ticket.getId(), "A-1");

        boolean invalidTransitionRejected = false;
        try {
            helpDesk.updateTicketStatus(ticket.getId(), TicketStatus.RESOLVED,
                    "Skipping the work step.");
        } catch (HelpDeskException exception) {
            invalidTransitionRejected = true;
        }
        check(invalidTransitionRejected, "Invalid status changes should be rejected.");

        helpDesk.updateTicketStatus(ticket.getId(), TicketStatus.IN_PROGRESS,
                "Agent started diagnostics.");
        helpDesk.updateTicketStatus(ticket.getId(), TicketStatus.RESOLVED,
                "Connection restored.");
        helpDesk.updateTicketStatus(ticket.getId(), TicketStatus.CLOSED,
                "Customer confirmed the fix.");

        check(ticket.getStatus() == TicketStatus.CLOSED,
                "A valid status sequence should close the ticket.");
        check(ticket.getStatusHistory().size() == 4,
                "Ticket history should contain creation and each status change.");

        System.out.println("PASS: status validation and ticket history");
    }

    private HelpDesk createSmallHelpDesk() throws Exception {
        HelpDesk helpDesk = new HelpDesk();
        Customer customer = helpDesk.createCustomer(CustomerCategory.RESIDENTIAL,
                "Lira Dema", "lira@example.com", "600", "Residential Road",
                "PERSON-99", "");
        helpDesk.registerProduct(new Product("P-ROUTER", "Router Care", "Router support",
                ProductType.EQUIPMENT, 10.0,
                new CustomerCategory[] { CustomerCategory.RESIDENTIAL }));
        helpDesk.registerSupportAgent(new SupportAgent("A-1", "Agent One",
                "agent@example.com", ProductType.EQUIPMENT));
        helpDesk.assignProductToCustomer(customer.getId(), "P-ROUTER");
        return helpDesk;
    }

    private void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
