package helpdesk;

import java.util.ArrayList;

public class HelpDeskTest {
    private int passedTests;

    public static void main(String[] args) throws Exception {
        HelpDeskTest testSuite = new HelpDeskTest();
        testSuite.runAllTests();
    }

    private void runAllTests() throws Exception {
        testDemoDataAndPriorityQueue();
        testCustomerPolymorphismSearchAndFilters();
        testProductAndTicketRelationships();
        testStatusHistoryAndInvalidTransitions();
        testDefensiveCollectionCopiesAndDuplicateIds();
        System.out.println("All " + passedTests + " HelpDesk workflow tests passed.");
    }

    private void testDemoDataAndPriorityQueue() throws Exception {
        HelpDesk helpDesk = DemoData.createHelpDeskWithSampleData();

        check(helpDesk.getCustomerCount() == 4, "Demo data should have four customers.");
        check(helpDesk.getProductCount() == 6, "Demo data should have six products.");
        check(helpDesk.getSupportAgentCount() == 3, "Demo data should have three agents.");
        check(helpDesk.getTicketCount() == 4, "Demo data should have four tickets.");

        ArrayList<Ticket> ordered = helpDesk.getTicketsInPriorityOrder();
        check(ordered.get(0).getPriority() == TreatmentPriority.URGENT,
                "Official customer ticket should be first in the priority queue.");
        check(ordered.get(ordered.size() - 1).getPriority() == TreatmentPriority.STANDARD,
                "Residential customer ticket should follow urgent and high tickets.");
        check(helpDesk.getTicket("T-1003").getStatusHistory().size() == 4,
                "Resolved demo ticket should retain its complete status history.");

        pass("demo data and priority queue");
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

    private void testProductAndTicketRelationships() throws Exception {
        HelpDesk helpDesk = new HelpDesk();
        Customer residential = new ResidentialCustomer("R-2", "Dren Meta", "d@example.com",
                "400", "Home Road", "PERSON-88");
        Customer business = new BusinessCustomer("B-2", "Delta LLC", "delta@example.com",
                "500", "Company Road", "REG-88", "Drita Meta");
        Product homeInternet = new Product("P-HOME", "Home Internet", "Home connection",
                ProductType.INTERNET, 20.0,
                new CustomerCategory[] { CustomerCategory.RESIDENTIAL });
        SupportAgent agent = new SupportAgent("A-1", "Agent One", "agent@example.com",
                ProductType.INTERNET);

        helpDesk.registerCustomer(residential);
        helpDesk.registerCustomer(business);
        helpDesk.registerProduct(homeInternet);
        helpDesk.registerSupportAgent(agent);
        helpDesk.assignProductToCustomer("R-2", "P-HOME");

        boolean unavailableOfferRejected = false;
        try {
            helpDesk.assignProductToCustomer("B-2", "P-HOME");
        } catch (HelpDeskException exception) {
            unavailableOfferRejected = true;
        }
        check(unavailableOfferRejected,
                "A category-specific product must be rejected for an ineligible customer.");

        Ticket ticket = helpDesk.createTicket("T-1", "R-2", "P-HOME", "A-1",
                TicketType.TECHNICAL_PROBLEM, "No connection", "The modem has no signal.");
        check(ticket.getCustomer() == residential, "Ticket should associate with its customer.");
        check(ticket.getProduct() == homeInternet, "Ticket should associate with its product.");
        check(ticket.getResponsibleAgent() == agent, "Ticket should associate with its agent.");
        check(agent.getAssignedTickets().contains(ticket),
                "Agent aggregation should contain the assigned ticket.");
        check(residential.getTicketHistory().contains(ticket),
                "Customer history should contain the registered ticket.");
        check(helpDesk.filterCustomersByProduct("P-HOME").contains(residential),
                "Product filter should return customers using that product.");

        pass("product eligibility and ticket object relationships");
    }

    private void testStatusHistoryAndInvalidTransitions() throws Exception {
        HelpDesk helpDesk = createSmallHelpDesk();
        Ticket ticket = helpDesk.createTicket("T-2", "R-3", "P-ROUTER", "A-2",
                TicketType.COMPLAINT, "Router complaint", "The replacement is delayed.");

        boolean skippedStepRejected = false;
        try {
            helpDesk.updateTicketStatus("T-2", TicketStatus.RESOLVED, "Skipping work step");
        } catch (HelpDeskException exception) {
            skippedStepRejected = true;
        }
        check(skippedStepRejected, "Open tickets must not jump directly to resolved.");
        check(ticket.getStatus() == TicketStatus.OPEN,
                "A rejected transition must not change ticket state.");

        helpDesk.updateTicketStatus("T-2", TicketStatus.IN_PROGRESS, "Agent started work.");
        helpDesk.updateTicketStatus("T-2", TicketStatus.RESOLVED, "Replacement delivered.");
        helpDesk.updateTicketStatus("T-2", TicketStatus.CLOSED, "Customer confirmed resolution.");
        check(ticket.getStatus() == TicketStatus.CLOSED, "Valid status flow should close the ticket.");
        check(ticket.getStatusHistory().size() == 4,
                "Status history should include creation and all three valid updates.");

        boolean closedTicketRejected = false;
        try {
            helpDesk.updateTicketStatus("T-2", TicketStatus.IN_PROGRESS, "Invalid reopening");
        } catch (HelpDeskException exception) {
            closedTicketRejected = true;
        }
        check(closedTicketRejected, "A closed ticket must reject further updates.");

        pass("status workflow, history composition, and custom exceptions");
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

        boolean missingRejected = false;
        try {
            helpDesk.getTicket("NOT-THERE");
        } catch (HelpDeskException exception) {
            missingRejected = true;
        }
        check(missingRejected, "Missing objects should produce a meaningful custom exception.");

        pass("encapsulation, duplicate validation, and missing-data handling");
    }

    private HelpDesk createSmallHelpDesk() throws Exception {
        HelpDesk helpDesk = new HelpDesk();
        helpDesk.registerCustomer(new ResidentialCustomer("R-3", "Lira Dema", "l@example.com",
                "600", "Residential Road", "PERSON-99"));
        helpDesk.registerProduct(new Product("P-ROUTER", "Router Care", "Router support",
                ProductType.EQUIPMENT, 10.0,
                new CustomerCategory[] { CustomerCategory.RESIDENTIAL }));
        helpDesk.registerSupportAgent(new SupportAgent("A-2", "Agent Two", "agent2@example.com",
                ProductType.EQUIPMENT));
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
