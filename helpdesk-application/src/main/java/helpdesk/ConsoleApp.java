package helpdesk;

import java.util.ArrayList;
import java.util.Scanner;

public class ConsoleApp {
    private final HelpDesk helpDesk;
    private final Scanner scanner;

    public ConsoleApp(HelpDesk helpDesk) {
        this.helpDesk = helpDesk;
        scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("FiberNet HelpDesk - optional console fallback");
        boolean running = true;
        while (running) {
            printMenu();
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 0) {
                    running = false;
                } else {
                    handleChoice(choice);
                }
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid menu number.");
            } catch (HelpDeskException | IllegalArgumentException exception) {
                System.out.println("Operation failed: " + exception.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Overview");
        System.out.println("2. List customers");
        System.out.println("3. List products / services");
        System.out.println("4. List tickets");
        System.out.println("5. Register unassigned ticket");
        System.out.println("6. Assign support agent");
        System.out.println("7. Update ticket status");
        System.out.println("8. List support agents");
        System.out.println("9. View customer ticket history");
        System.out.println("0. Exit");
        System.out.print("Choice: ");
    }

    private void handleChoice(int choice) throws HelpDeskException {
        switch (choice) {
            case 1:
                showOverview();
                break;
            case 2:
                printCustomers();
                break;
            case 3:
                printProducts();
                break;
            case 4:
                printTickets(helpDesk.getTicketsInPriorityOrder());
                break;
            case 5:
                registerTicket();
                break;
            case 6:
                assignAgent();
                break;
            case 7:
                updateStatus();
                break;
            case 8:
                printAgents();
                break;
            case 9:
                showCustomerHistory();
                break;
            default:
                System.out.println("Unknown menu option.");
        }
    }

    private void showOverview() {
        System.out.println("Customers: " + helpDesk.getCustomerCount());
        System.out.println("Products/services: " + helpDesk.getProductCount());
        System.out.println("Support agents: " + helpDesk.getSupportAgentCount());
        System.out.println("Registered tickets: " + helpDesk.getTicketCount());
        System.out.println("Active tickets: " + helpDesk.getActiveTicketCount());
    }

    private void printCustomers() {
        for (Customer customer : helpDesk.getCustomers()) {
            System.out.println(customer);
        }
    }

    private void printProducts() {
        for (Product product : helpDesk.getProducts()) {
            System.out.println(product);
        }
    }

    private void printAgents() {
        for (SupportAgent agent : helpDesk.getSupportAgents()) {
            System.out.println(agent);
        }
    }

    private void printTickets(ArrayList<Ticket> tickets) {
        if (tickets.isEmpty()) {
            System.out.println("No tickets found.");
            return;
        }
        for (Ticket ticket : tickets) {
            System.out.println(ticket.getSummary());
        }
    }

    private void registerTicket() throws HelpDeskException {
        printCustomers();
        String customerId = readRequired("Customer ID: ");
        Customer customer = helpDesk.getCustomer(customerId);
        for (Product product : customer.getProducts()) {
            System.out.println(product);
        }
        String productId = readRequired("Affected product ID: ");
        String ticketId = readRequired("Ticket ID: ");
        TicketType type = chooseTicketType();
        String title = readRequired("Short title: ");
        String description = readRequired("Description: ");
        Ticket ticket = helpDesk.createTicket(ticketId, customerId, productId,
                type, title, description);
        System.out.println("Registered: " + ticket.getSummary());
    }

    private void assignAgent() throws HelpDeskException {
        printTickets(helpDesk.getTicketsInPriorityOrder());
        String ticketId = readRequired("Ticket ID: ");
        printAgents();
        String agentId = readRequired("Support agent ID: ");
        helpDesk.assignAgentToTicket(ticketId, agentId);
        System.out.println("Agent assigned.");
    }

    private void updateStatus() throws HelpDeskException {
        printTickets(helpDesk.getTicketsInPriorityOrder());
        String ticketId = readRequired("Ticket ID: ");
        TicketStatus[] statuses = TicketStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + ". " + statuses[i]);
        }
        int selection = Integer.parseInt(readRequired("New status number: "));
        if (selection < 1 || selection > statuses.length) {
            throw new IllegalArgumentException("Invalid status selection.");
        }
        String note = readRequired("Progress note: ");
        helpDesk.updateTicketStatus(ticketId, statuses[selection - 1], note);
        System.out.println("Status updated.");
    }

    private void showCustomerHistory() throws HelpDeskException {
        String customerId = readRequired("Customer ID: ");
        Customer customer = helpDesk.getCustomer(customerId);
        printTickets(customer.getTicketHistory());
    }

    private TicketType chooseTicketType() {
        TicketType[] types = TicketType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.println((i + 1) + ". " + types[i]);
        }
        int selection = Integer.parseInt(readRequired("Ticket type number: "));
        if (selection < 1 || selection > types.length) {
            throw new IllegalArgumentException("Invalid ticket type selection.");
        }
        return types[selection - 1];
    }

    private String readRequired(String prompt) {
        System.out.print(prompt);
        String value = scanner.nextLine().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("A value is required.");
        }
        return value;
    }
}
