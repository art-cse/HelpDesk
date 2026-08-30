package helpdesk;

import java.util.ArrayList;

public class HelpDeskTest {
    private int passedTests;

    public static void main(String[] args) throws Exception {
        HelpDeskTest test = new HelpDeskTest();
        test.runTests();
        System.out.println("All " + test.passedTests + " HelpDesk workflow tests passed.");
    }

    private void runTests() throws Exception {
        testDemoDataPriorityAndUnassignedTicket();
        testCustomerPolymorphismSearchAndFilters();
        testSeparateTicketCreationAndAssignment();
        testStatusHistoryAndInvalidTransitions();
        testAuthenticationAccounts();
        testDefensiveCollectionCopiesAndDuplicateIds();
    }

    private void testDemoDataPriorityAndUnassignedTicket() throws Exception {
        HelpDesk helpDesk = DemoData.createHelpDeskWithSampleData();
        check(helpDesk.getCustomerCount() == 4, "Demo data should contain four customers.");
        check(helpDesk.getProductCount() == 6, "Demo data should contain six products.");
        check(helpDesk.getSupportAgentCount() == 3, "Demo data should contain three agents.");
        check(helpDesk.getTicketCount() == 4, "Demo data should contain four tickets.");

        ArrayList<Ticket> ordered = helpDesk.getTicketsInPriorityOrder();
        check(ordered.get(0).getPriority() == TreatmentPriority.URGENT,
                "Official customer ticket should be first in the priority queue.");
        check(ordered.get(ordered.size() - 1).getPriority() == TreatmentPriority.STANDARD,
                "Residential tickets should follow urgent and high tickets.");
        check(helpDesk.getTicket("T-1003").getStatusHistory().size() == 4,
                "Closed demo ticket should retain its complete status history.");
        Ticket unassigned = helpDesk.getTicket("T-1004");
        check(unassigned.getResponsibleAgent() == null,
                "Demo data should include an unassigned ticket.");
        check("Unassigned".equals(unassigned.getResponsibleAgentName()),
                "Unassigned tickets should have safe display text.");
        check(helpDesk.filterTickets("", null, HelpDesk.UNASSIGNED_AGENT_FILTER).size() == 1,
                "Unassigned filter should return the demo unassigned ticket.");

        pass("demo data, priority queue, and unassigned ticket");
    }

    private void testCustomerPolymorphismSearchAndFilters() throws Exception {
        HelpDesk helpDesk = new HelpDesk();
        Customer business = new BusinessCustomer("B-1", "North Trade", "b@example.com",
                "100", "Business Street", "REG-900", "Mira Basha");
        Customer official = new OfficialCustomer("O-1", "Public Archive", "o@example.com",
                "200", "Official Street", "INST-77", "Records Department");
        Customer residential = new ResidentialCustomer("R-1", "Rina Kola", "r@example.com",
                "300", "Home Street", "PERSON-55");

        helpDesk.registerCustomer(business);
        helpDesk.registerCustomer(official);
        helpDesk.registerCustomer(residential);

        check(business.getTreatmentPriority() == TreatmentPriority.HIGH,
                "Business override should return high priority.");
        check(official.getTreatmentPriority() == TreatmentPriority.URGENT,
                "Official override should return urgent priority.");
        check(residential.getTreatmentPriority() == TreatmentPriority.STANDARD,
                "Residential override should return standard priority.");
        check(helpDesk.searchCustomers("INST-77").get(0) == official,
                "Search should include category-specific identifying information.");
        check(helpDesk.searchCustomers("rina").get(0) == residential,
                "Search should be case-insensitive and include customer names.");
        check(helpDesk.filterCustomersByCategory(CustomerCategory.BUSINESS).size() == 1,
                "Category filter should return the business customer only.");

        pass("customer polymorphism, search, and category filtering");
    }

    private void testSeparateTicketCreationAndAssignment() throws Exception {
        HelpDesk helpDesk = new HelpDesk();
        Customer customer = new ResidentialCustomer("R-2", "Dren Meta", "d@example.com",
                "400", "Home Road", "PERSON-88");
        Product internet = new Product("P-HOME", "Home Internet", "Home connection",
                ProductType.INTERNET, 20.0,
                new CustomerCategory[] { CustomerCategory.RESIDENTIAL });
        SupportAgent firstAgent = new SupportAgent("A-1", "Agent One", "agent1@example.com",
                ProductType.INTERNET);
        SupportAgent secondAgent = new SupportAgent("A-2", "Agent Two", "agent2@example.com",
                ProductType.EQUIPMENT);
        helpDesk.registerCustomer(customer);
        helpDesk.registerProduct(internet);
        helpDesk.registerSupportAgent(firstAgent);
        helpDesk.registerSupportAgent(secondAgent);
        helpDesk.assignProductToCustomer(customer.getId(), internet.getId());

        Ticket ticket = helpDesk.createTicket(customer.getId(), internet.getId(),
                TicketType.TECHNICAL_PROBLEM, "No connection", "The modem has no signal.");
        check("T-1001".equals(ticket.getId()),
                "HelpDesk should assign the first ticket ID.");
        check(ticket.getCustomer() == customer, "Ticket should associate with its customer.");
        check(ticket.getProduct() == internet, "Ticket should associate with its product.");
        check(ticket.getResponsibleAgent() == null,
                "New tickets must initially be unassigned.");
        check(firstAgent.getAssignedTickets().isEmpty(),
                "Ticket creation must not add a ticket to an agent.");
        check(ticket.getPriority() == TreatmentPriority.STANDARD,
                "Ticket priority should still come from the customer override.");

        helpDesk.assignAgentToTicket(ticket.getId(), firstAgent.getId());
        check(ticket.getResponsibleAgent() == firstAgent,
                "Separate assignment should set the responsible agent.");
        check(firstAgent.getAssignedTickets().contains(ticket),
                "Separate assignment should update agent aggregation.");

        boolean sameAgentRejected = false;
        try {
            helpDesk.assignAgentToTicket(ticket.getId(), firstAgent.getId());
        } catch (HelpDeskException exception) {
            sameAgentRejected = true;
        }
        check(sameAgentRejected, "Assigning the same agent twice should be rejected.");

        helpDesk.assignAgentToTicket(ticket.getId(), secondAgent.getId());
        check(ticket.getResponsibleAgent() == secondAgent,
                "Reassignment should update the ticket agent.");
        check(!firstAgent.getAssignedTickets().contains(ticket),
                "Reassignment should remove the previous agent relation.");
        check(secondAgent.getAssignedTickets().contains(ticket),
                "Reassignment should add the new agent relation.");
        check(customer.getTicketHistory().contains(ticket),
                "Customer history should contain the registered ticket.");

        Ticket nextTicket = helpDesk.createTicket(customer.getId(), internet.getId(),
                TicketType.SERVICE_REQUEST, "Router check", "Please check the home router.");
        check("T-1002".equals(nextTicket.getId()),
                "HelpDesk should assign sequential unique ticket IDs.");

        pass("automatic ticket IDs, creation, assignment, and reassignment");
    }

    private void testStatusHistoryAndInvalidTransitions() throws Exception {
        HelpDesk helpDesk = createSmallHelpDesk();
        Ticket ticket = helpDesk.createTicket("R-3", "P-ROUTER",
                TicketType.COMPLAINT, "Router complaint", "The replacement is delayed.");
        helpDesk.assignAgentToTicket(ticket.getId(), "A-3");

        boolean skippedStepRejected = false;
        try {
            helpDesk.updateTicketStatus(ticket.getId(), TicketStatus.RESOLVED,
                    "Skipping work step");
        } catch (HelpDeskException exception) {
            skippedStepRejected = true;
        }
        check(skippedStepRejected, "Open tickets must not jump directly to resolved.");
        check(ticket.getStatus() == TicketStatus.OPEN,
                "A rejected transition must not change ticket state.");

        helpDesk.updateTicketStatus(ticket.getId(), TicketStatus.IN_PROGRESS,
                "Agent started work.");
        helpDesk.updateTicketStatus(ticket.getId(), TicketStatus.RESOLVED,
                "Replacement delivered.");
        helpDesk.updateTicketStatus(ticket.getId(), TicketStatus.CLOSED,
                "Customer confirmed resolution.");
        check(ticket.getStatus() == TicketStatus.CLOSED, "Valid status flow should close ticket.");
        check(ticket.getStatusHistory().size() == 4,
                "History should include creation and all three valid updates.");

        boolean closedTicketRejected = false;
        try {
            helpDesk.updateTicketStatus(ticket.getId(), TicketStatus.IN_PROGRESS,
                    "Invalid reopening");
        } catch (HelpDeskException exception) {
            closedTicketRejected = true;
        }
        check(closedTicketRejected, "A closed ticket must reject further updates.");

        pass("status workflow, history composition, and custom exceptions");
    }

    private void testAuthenticationAccounts() throws Exception {
        AuthenticationService authentication = DemoData.createAuthenticationService();
        check(authentication.authenticate("admin", "admin123").getRole() == UserRole.ADMIN,
                "Administrator demo login should work.");
        check(authentication.authenticate("agent1", "agent123").getRole() == UserRole.AGENT,
                "Agent demo login should work.");
        check(authentication.authenticate("customer1", "customer123").getRole()
                == UserRole.CUSTOMER, "Customer demo login should work.");

        boolean invalidRejected = false;
        try {
            authentication.authenticate("admin", "wrong-password");
        } catch (HelpDeskException exception) {
            invalidRejected = true;
        }
        check(invalidRejected, "Invalid login should be rejected.");

        pass("simple in-memory authentication accounts");
    }

    private void testDefensiveCollectionCopiesAndDuplicateIds() throws Exception {
        HelpDesk helpDesk = createSmallHelpDesk();
        Customer customer = helpDesk.getCustomer("R-3");

        ArrayList<Customer> customerCopy = helpDesk.getCustomers();
        customerCopy.clear();
        check(helpDesk.getCustomerCount() == 1,
                "Clearing a returned list must not clear the HelpDesk registry.");

        ArrayList<Product> productCopy = customer.getProducts();
        productCopy.clear();
        check(customer.getProducts().size() == 1,
                "Clearing a returned list must not clear the customer's products.");

        boolean duplicateRejected = false;
        try {
            helpDesk.registerCustomer(new ResidentialCustomer("R-3", "Duplicate", "x@example.com",
                    "999", "Other Road", "OTHER"));
        } catch (HelpDeskException exception) {
            duplicateRejected = true;
        }
        check(duplicateRejected, "The generic registry should reject duplicate IDs.");

        pass("encapsulation, duplicate validation, and defensive copies");
    }

    private HelpDesk createSmallHelpDesk() throws Exception {
        HelpDesk helpDesk = new HelpDesk();
        helpDesk.registerCustomer(new ResidentialCustomer("R-3", "Lira Dema", "l@example.com",
                "600", "Residential Road", "PERSON-99"));
        helpDesk.registerProduct(new Product("P-ROUTER", "Router Care", "Router support",
                ProductType.EQUIPMENT, 10.0,
                new CustomerCategory[] { CustomerCategory.RESIDENTIAL }));
        helpDesk.registerSupportAgent(new SupportAgent("A-3", "Agent Three",
                "agent3@example.com", ProductType.EQUIPMENT));
        helpDesk.assignProductToCustomer("R-3", "P-ROUTER");
        return helpDesk;
    }

    private void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private void pass(String testName) {
        passedTests++;
        System.out.println("PASS: " + testName);
    }
}
