package helpdesk;

public class DemoData {
    private DemoData() {
    }

    public static HelpDesk createHelpDeskWithSampleData() throws HelpDeskException {
        HelpDesk helpDesk = new HelpDesk();
        addProducts(helpDesk);
        addSupportAgents(helpDesk);
        addCustomers(helpDesk);
        assignProducts(helpDesk);
        addTickets(helpDesk);
        return helpDesk;
    }

    public static AuthenticationService createAuthenticationService()
            throws HelpDeskException {
        AuthenticationService authentication = new AuthenticationService();
        authentication.addAccount(new UserAccount("admin", "admin123",
                UserRole.ADMIN, null));
        authentication.addAccount(new UserAccount("agent1", "agent123",
                UserRole.AGENT, "A-01"));
        authentication.addAccount(new UserAccount("agent2", "agent123",
                UserRole.AGENT, "A-02"));
        authentication.addAccount(new UserAccount("customer1", "customer123",
                UserRole.CUSTOMER, "C-RES-001"));
        authentication.addAccount(new UserAccount("business1", "business123",
                UserRole.CUSTOMER, "C-BIZ-001"));
        return authentication;
    }

    private static void addProducts(HelpDesk helpDesk) throws HelpDeskException {
        helpDesk.registerProduct(new Product("P-101", "Home Fiber 300",
                "300 Mbps residential fiber internet", ProductType.INTERNET, 24.99,
                new CustomerCategory[] { CustomerCategory.RESIDENTIAL }));
        helpDesk.registerProduct(new Product("P-102", "Business Fiber Pro",
                "Dedicated business fiber with service monitoring",
                ProductType.BUSINESS_CONNECTIVITY, 149.99,
                new CustomerCategory[] { CustomerCategory.BUSINESS }));
        helpDesk.registerProduct(new Product("P-103", "Institution Secure Network",
                "Protected connectivity for public institutions", ProductType.SECURITY, 249.99,
                new CustomerCategory[] { CustomerCategory.OFFICIAL }));
        helpDesk.registerProduct(new Product("P-104", "Managed Router Support",
                "Router installation, replacement, and configuration", ProductType.EQUIPMENT, 12.50,
                new CustomerCategory[] { CustomerCategory.BUSINESS, CustomerCategory.OFFICIAL,
                        CustomerCategory.RESIDENTIAL }));
        helpDesk.registerProduct(new Product("P-105", "Cyber Shield",
                "Managed firewall and threat monitoring", ProductType.SECURITY, 89.00,
                new CustomerCategory[] { CustomerCategory.BUSINESS, CustomerCategory.OFFICIAL }));
        helpDesk.registerProduct(new Product("P-106", "Wi-Fi Mesh Care",
                "Managed wireless coverage and access-point support", ProductType.NETWORKING, 18.00,
                new CustomerCategory[] { CustomerCategory.BUSINESS,
                        CustomerCategory.RESIDENTIAL }));
    }

    private static void addSupportAgents(HelpDesk helpDesk) throws HelpDeskException {
        helpDesk.registerSupportAgent(new SupportAgent("A-01", "Arta Krasniqi",
                "arta@fibernet.example", ProductType.INTERNET));
        helpDesk.registerSupportAgent(new SupportAgent("A-02", "Blerim Hoxha",
                "blerim@fibernet.example", ProductType.SECURITY));
        helpDesk.registerSupportAgent(new SupportAgent("A-03", "Dona Berisha",
                "dona@fibernet.example", ProductType.EQUIPMENT));
    }

    private static void addCustomers(HelpDesk helpDesk) throws HelpDeskException {
        helpDesk.registerCustomer(new BusinessCustomer("C-BIZ-001", "Alba Logistics LLC",
                "it@albalogistics.example", "+383 38 700 101", "Prishtina Industrial Zone",
                "810245671", "Luan Gashi"));
        helpDesk.registerCustomer(new OfficialCustomer("C-OFF-001", "Municipal Records Office",
                "network@municipality.example", "+383 38 700 202", "Central Civic Building",
                "INST-4402", "Information Technology Department"));
        helpDesk.registerCustomer(new ResidentialCustomer("C-RES-001", "Era Dervishi",
                "era@example.com", "+383 44 700 303", "Dardania, Prishtina",
                "1203990123456"));
        helpDesk.registerCustomer(new ResidentialCustomer("C-RES-002", "Aron Kelmendi",
                "aron@example.com", "+383 49 700 404", "Peja City Center",
                "0506980123456"));
    }

    private static void assignProducts(HelpDesk helpDesk) throws HelpDeskException {
        helpDesk.assignProductToCustomer("C-BIZ-001", "P-102");
        helpDesk.assignProductToCustomer("C-BIZ-001", "P-104");
        helpDesk.assignProductToCustomer("C-BIZ-001", "P-105");
        helpDesk.assignProductToCustomer("C-OFF-001", "P-103");
        helpDesk.assignProductToCustomer("C-OFF-001", "P-104");
        helpDesk.assignProductToCustomer("C-RES-001", "P-101");
        helpDesk.assignProductToCustomer("C-RES-001", "P-104");
        helpDesk.assignProductToCustomer("C-RES-002", "P-101");
        helpDesk.assignProductToCustomer("C-RES-002", "P-106");
    }

    private static void addTickets(HelpDesk helpDesk) throws HelpDeskException {
        helpDesk.createTicket("T-1001", "C-BIZ-001", "P-102",
                TicketType.TECHNICAL_PROBLEM, "Warehouse connection is unstable",
                "Packet loss is interrupting the warehouse inventory terminals.");
        helpDesk.assignAgentToTicket("T-1001", "A-01");
        helpDesk.updateTicketStatus("T-1001", TicketStatus.IN_PROGRESS,
                "Fiber line diagnostics started.");

        helpDesk.createTicket("T-1002", "C-OFF-001", "P-103",
                TicketType.TECHNICAL_PROBLEM, "Secure portal cannot be reached",
                "Employees cannot reach the protected municipal records portal.");
        helpDesk.assignAgentToTicket("T-1002", "A-02");
        helpDesk.updateTicketStatus("T-1002", TicketStatus.IN_PROGRESS,
                "Security gateway logs are being checked.");
        helpDesk.updateTicketStatus("T-1002", TicketStatus.WAITING_FOR_CUSTOMER,
                "Waiting for the institution to confirm a test account.");

        helpDesk.createTicket("T-1003", "C-RES-001", "P-104",
                TicketType.TECHNICAL_PROBLEM, "Router restarts every evening",
                "The home router loses power and restarts at approximately 20:00.");
        helpDesk.assignAgentToTicket("T-1003", "A-03");
        helpDesk.updateTicketStatus("T-1003", TicketStatus.IN_PROGRESS,
                "A replacement power adapter was delivered.");
        helpDesk.updateTicketStatus("T-1003", TicketStatus.RESOLVED,
                "The new adapter stopped the router restarts.");
        helpDesk.updateTicketStatus("T-1003", TicketStatus.CLOSED,
                "Customer confirmed stable service for two days.");

        helpDesk.createTicket("T-1004", "C-BIZ-001", "P-105",
                TicketType.SERVICE_REQUEST, "Add a firewall rule",
                "Allow the new accounting server to reach the tax reporting service.");
    }
}
