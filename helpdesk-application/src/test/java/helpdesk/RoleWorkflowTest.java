package helpdesk;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class RoleWorkflowTest {
    private int passedChecks;

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        final RoleWorkflowTest test = new RoleWorkflowTest();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    test.runWorkflow();
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }
        });
        System.out.println("All " + test.passedChecks + " role workflow checks passed.");
    }

    private void runWorkflow() throws Exception {
        HelpDesk helpDesk = DemoData.createHelpDeskWithSampleData();
        AuthenticationService authentication = DemoData.createAuthenticationService();

        LoginFrame login = new LoginFrame(helpDesk, authentication);
        check("FiberNet HelpDesk - Login".equals(login.getTitle()),
                "Application starts with a Login window type");
        check(findButton(login, "Login") != null && findButton(login, "Exit") != null,
                "Login and Exit buttons exist");

        boolean invalidRejected = false;
        try {
            authentication.authenticate("customer1", "wrong");
        } catch (HelpDeskException exception) {
            invalidRejected = true;
        }
        check(invalidRejected, "Invalid login is rejected");

        login.setVisible(true);
        JFrame adminLoginResult = login.openForCredentials("admin", "admin123");
        check(adminLoginResult instanceof HelpDeskFrame, "Admin login opens full admin view");
        check(!login.isVisible(), "Login hides after successful login");
        HelpDeskFrame loggedInAdmin = (HelpDeskFrame) adminLoginResult;
        check(loggedInAdmin.getTabs().getTabCount() == 5, "Admin retains all five tabs");
        loggedInAdmin.performLogout();
        check(login.isVisible(), "Admin logout returns to Login");

        JFrame agentLoginResult = login.openForCredentials("agent1", "agent123");
        check(agentLoginResult instanceof AgentFrame, "Agent login opens My Tickets view");
        ((AgentFrame) agentLoginResult).performLogout();
        check(login.isVisible(), "Agent logout returns to Login");

        JFrame customerLoginResult = login.openForCredentials("customer1", "customer123");
        check(customerLoginResult instanceof CustomerFrame,
                "Customer login opens customer-only view");
        ((CustomerFrame) customerLoginResult).performLogout();
        check(login.isVisible(), "Customer logout returns to Login");
        login.dispose();

        Customer customer = helpDesk.getCustomer("C-RES-001");
        CustomerFrame customerFrame = new CustomerFrame(helpDesk, customer, null);
        check(customerFrame.getCustomer() == customer,
                "Customer view is linked to the logged-in customer only");
        check(customerFrame.getProductRowCount() == customer.getProducts().size(),
                "Customer sees only their own products");
        check(customerFrame.getTicketRowCount() == customer.getTicketHistory().size(),
                "Customer sees only their own tickets");
        check(findButton(customerFrame, "Assign Agent") == null,
                "Customer cannot assign support agents");
        check(findButton(customerFrame, "Update Status") == null,
                "Customer cannot update ticket status");

        TicketDialog customerTicketDialog = new TicketDialog(customerFrame, helpDesk, customer);
        ArrayList<JComboBox> customerTicketBoxes =
                findComponents(customerTicketDialog, JComboBox.class);
        boolean customerSelectionLocked = false;
        for (JComboBox box : customerTicketBoxes) {
            if (!box.isEnabled() && box.getItemCount() == 1) {
                customerSelectionLocked = true;
            }
        }
        check(customerSelectionLocked, "Customer ticket form fixes the logged-in customer");
        ArrayList<JTextField> ticketFields =
                findComponents(customerTicketDialog, JTextField.class);
        ArrayList<JTextArea> ticketAreas =
                findComponents(customerTicketDialog, JTextArea.class);
        check(ticketFields.size() == 1,
                "Customer ticket form has no editable Ticket ID field");
        ticketFields.get(0).setText("Customer internet offline");
        ticketAreas.get(0).setText("The fiber connection stopped working this morning.");
        findButton(customerTicketDialog, "Submit Ticket").doClick();
        check(customerTicketDialog.isSaved(), "Customer Submit Ticket button works");

        Ticket ticket = customerTicketDialog.getCreatedTicket();
        check(ticket != null && "T-1005".equals(ticket.getId()),
                "Customer-created ticket receives the next system ID");
        check(ticket.getCustomer() == customer, "Customer-created ticket belongs to that customer");
        check(ticket.getStatus() == TicketStatus.OPEN, "Customer-created ticket starts Open");
        check(ticket.getResponsibleAgent() == null,
                "Customer-created ticket starts Unassigned");
        check(ticket.getPriority() == customer.getTreatmentPriority(),
                "Customer-created ticket keeps polymorphic priority");
        check("Unassigned".equals(ticket.getResponsibleAgentName())
                && ticket.getDetails().contains("Unassigned"),
                "Unassigned ticket summary and details are null-safe");

        customerFrame.refreshData();
        int customerRow = findRow(customerFrame.getTicketTable(), ticket.getId());
        check(customerRow >= 0, "New ticket appears in customer My Tickets table");
        check("Unassigned".equals(customerFrame.getTicketTable().getModel()
                .getValueAt(customerRow, 4)), "Customer table displays Unassigned");

        HelpDeskFrame adminFrame = new HelpDeskFrame(helpDesk);
        adminFrame.refreshAll();
        JTable adminTicketTable = adminFrame.getTicketsPanel().getTicketTable();
        int adminRow = findRow(adminTicketTable, ticket.getId());
        check(adminRow >= 0, "Unassigned ticket appears in admin ticket table");
        check("Unassigned".equals(adminTicketTable.getModel().getValueAt(adminRow, 4)),
                "Admin ticket table displays Unassigned");

        AssignAgentDialog assignDialog = new AssignAgentDialog(adminFrame, helpDesk, ticket);
        assignDialog.setSelectedAgentId("A-01");
        findButton(assignDialog, "Assign").doClick();
        check(assignDialog.isSaved() && ticket.getResponsibleAgent()
                == helpDesk.getSupportAgent("A-01"), "Admin assigns an unassigned ticket");

        SupportAgent firstAgent = helpDesk.getSupportAgent("A-01");
        AgentFrame firstAgentFrame = new AgentFrame(helpDesk, firstAgent, null);
        check(findRow(firstAgentFrame.getTicketTable(), ticket.getId()) >= 0,
                "Assigned ticket appears in that agent's My Tickets view");
        check(firstAgentFrame.getVisibleRowCount() == firstAgent.getAssignedTickets().size(),
                "Agent view contains only that agent's assigned tickets");
        check(findButton(firstAgentFrame, "New Ticket") == null,
                "Agent cannot create customer tickets");
        check(findButton(firstAgentFrame, "Update Status") != null,
                "Agent can update assigned ticket status");

        TicketStatusDialog progressDialog =
                new TicketStatusDialog(firstAgentFrame, helpDesk, ticket);
        progressDialog.setSelectedStatus(TicketStatus.IN_PROGRESS);
        progressDialog.setNote("Agent started line diagnostics.");
        findButton(progressDialog, "Update").doClick();
        check(progressDialog.isSaved() && ticket.getStatus() == TicketStatus.IN_PROGRESS,
                "Agent updates ticket from Open to In progress");

        customerFrame.refreshData();
        customerRow = findRow(customerFrame.getTicketTable(), ticket.getId());
        check(TicketStatus.IN_PROGRESS.equals(customerFrame.getTicketTable().getModel()
                .getValueAt(customerRow, 5)), "Customer sees the agent's updated status");

        TicketStatusDialog resolveDialog =
                new TicketStatusDialog(firstAgentFrame, helpDesk, ticket);
        resolveDialog.setSelectedStatus(TicketStatus.RESOLVED);
        resolveDialog.setNote("Fiber signal restored.");
        findButton(resolveDialog, "Update").doClick();
        check(resolveDialog.isSaved() && ticket.getStatus() == TicketStatus.RESOLVED,
                "Agent resolves the ticket");
        check(ticket.getStatusHistory().size() == 3
                && GuiUtil.formatTicketHistory(ticket).contains("Fiber signal restored"),
                "Customer-visible ticket history retains both agent updates");

        AssignAgentDialog reassignDialog = new AssignAgentDialog(adminFrame, helpDesk, ticket);
        reassignDialog.setSelectedAgentId("A-02");
        findButton(reassignDialog, "Assign").doClick();
        SupportAgent secondAgent = helpDesk.getSupportAgent("A-02");
        check(reassignDialog.isSaved() && ticket.getResponsibleAgent() == secondAgent,
                "Admin reassigns Agent A ticket to Agent B");
        check(!firstAgent.getAssignedTickets().contains(ticket),
                "Reassignment removes the ticket from Agent A");
        check(secondAgent.getAssignedTickets().contains(ticket),
                "Reassignment adds the ticket to Agent B");
        firstAgentFrame.refreshData();
        AgentFrame secondAgentFrame = new AgentFrame(helpDesk, secondAgent, null);
        check(findRow(firstAgentFrame.getTicketTable(), ticket.getId()) < 0
                && findRow(secondAgentFrame.getTicketTable(), ticket.getId()) >= 0,
                "Reassignment is reflected in both agent views");

        final boolean[] logoutCalled = new boolean[] { false };
        AgentFrame logoutAgentFrame = new AgentFrame(helpDesk, firstAgent, new Runnable() {
            @Override
            public void run() {
                logoutCalled[0] = true;
            }
        });
        findButton(logoutAgentFrame, "Logout").doClick();
        check(logoutCalled[0], "Agent Logout button works");

        final boolean[] customerLogoutCalled = new boolean[] { false };
        CustomerFrame logoutCustomerFrame = new CustomerFrame(helpDesk, customer, new Runnable() {
            @Override
            public void run() {
                customerLogoutCalled[0] = true;
            }
        });
        findButton(logoutCustomerFrame, "Logout").doClick();
        check(customerLogoutCalled[0], "Customer Logout button works");

        customerFrame.dispose();
        adminFrame.dispose();
        firstAgentFrame.dispose();
        secondAgentFrame.dispose();
    }

    private int findRow(JTable table, String id) {
        for (int row = 0; row < table.getModel().getRowCount(); row++) {
            if (id.equals(table.getModel().getValueAt(row, 0))) {
                return row;
            }
        }
        return -1;
    }

    private JButton findButton(Container container, String text) {
        ArrayList<JButton> buttons = findComponents(container, JButton.class);
        for (JButton button : buttons) {
            if (text.equals(button.getText())) {
                return button;
            }
        }
        return null;
    }

    private <T extends Component> ArrayList<T> findComponents(Container container,
            Class<T> componentType) {
        ArrayList<T> results = new ArrayList<T>();
        collectComponents(container, componentType, results);
        return results;
    }

    private <T extends Component> void collectComponents(Container container,
            Class<T> componentType, ArrayList<T> results) {
        for (Component component : container.getComponents()) {
            if (componentType.isInstance(component)) {
                results.add(componentType.cast(component));
            }
            if (component instanceof Container) {
                collectComponents((Container) component, componentType, results);
            }
        }
    }

    private void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError("Role workflow check failed: " + description);
        }
        passedChecks++;
        System.out.println("PASS: " + description);
    }
}
