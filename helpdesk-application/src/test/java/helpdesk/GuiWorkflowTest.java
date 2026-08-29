package helpdesk;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class GuiWorkflowTest {
    private int passedChecks;

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        final GuiWorkflowTest test = new GuiWorkflowTest();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    test.runTests();
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }
        });
        System.out.println("All " + test.passedChecks + " Swing admin GUI checks passed.");
    }

    private void runTests() throws Exception {
        HelpDesk helpDesk = DemoData.createHelpDeskWithSampleData();
        HelpDeskFrame frame = new HelpDeskFrame(helpDesk);

        check("FiberNet HelpDesk".equals(frame.getTitle()), "Admin window title");
        JMenuBar menuBar = frame.getJMenuBar();
        check(menuBar != null && menuBar.getMenuCount() == 5, "Traditional five-menu bar");
        JTabbedPane tabs = frame.getTabs();
        check(tabs.getTabCount() == 5, "Five full admin tabs");

        DashboardPanel dashboard = (DashboardPanel) tabs.getComponentAt(0);
        CustomersPanel customers = frame.getCustomersPanel();
        TicketsPanel tickets = frame.getTicketsPanel();
        ProductsPanel products = (ProductsPanel) tabs.getComponentAt(3);
        SupportAgentsPanel agents = (SupportAgentsPanel) tabs.getComponentAt(4);

        check(dashboard.getQueueRowCount() == 3, "Overview active-ticket queue");
        check(customers.getVisibleRowCount() == 4, "Customer table demo rows");
        check(tickets.getVisibleRowCount() == 4, "Ticket table demo rows");
        check(products.getVisibleRowCount() == 6, "Product table demo rows");
        check(agents.getVisibleRowCount() == 3, "Agent table demo rows");

        customers.setSearchText("INST-4402");
        customers.applyFilters();
        check(customers.getVisibleRowCount() == 1, "Customer identifying-information search");
        customers.setSearchText("");
        customers.applyFilters();

        tickets.setSearchText("secure portal");
        tickets.applyFilters();
        check(tickets.getVisibleRowCount() == 1, "Ticket text search");
        tickets.setSearchText("");
        tickets.setAgentFilterIndex(1);
        tickets.applyFilters();
        check(tickets.getVisibleRowCount() == 1, "Unassigned ticket filter");
        tickets.setAgentFilterIndex(3);
        tickets.applyFilters();
        check(tickets.getVisibleRowCount() == 1, "Assigned-agent filter");
        tickets.setAgentFilterIndex(0);

        Customer newCustomer = new ResidentialCustomer("C-GUI-001", "GUI Test Customer",
                "gui.test@example.com", "+383 44 111 222", "Test Address", "GUI-9001");
        helpDesk.registerCustomer(newCustomer);
        helpDesk.assignProductToCustomer("C-GUI-001", "P-101");
        helpDesk.createTicket("T-GUI-001", "C-GUI-001", "P-101",
                TicketType.TECHNICAL_PROBLEM, "GUI workflow ticket",
                "Created while testing table refresh behavior.");
        frame.refreshAll();
        check(customers.getVisibleRowCount() == 5, "Add Customer refreshes table");
        check(tickets.getVisibleRowCount() == 5, "Unassigned New Ticket refreshes table");
        check(helpDesk.getTicket("T-GUI-001").getResponsibleAgent() == null,
                "New admin ticket is initially unassigned");

        helpDesk.assignAgentToTicket("T-GUI-001", "A-01");
        helpDesk.assignAgentToTicket("T-GUI-001", "A-03");
        check(helpDesk.getSupportAgent("A-03").getAssignedTickets()
                .contains(helpDesk.getTicket("T-GUI-001")), "Agent reassignment adds new relation");
        check(!helpDesk.getSupportAgent("A-01").getAssignedTickets()
                .contains(helpDesk.getTicket("T-GUI-001")), "Agent reassignment removes old relation");
        helpDesk.updateTicketStatus("T-GUI-001", TicketStatus.IN_PROGRESS,
                "GUI status update test.");
        check(helpDesk.getTicket("T-GUI-001").getStatus() == TicketStatus.IN_PROGRESS,
                "Status update and history workflow");

        helpDesk.updateCustomer("C-GUI-001", "Updated GUI Customer", "updated@example.com",
                "+383 44 333 444", "Updated Address", "GUI-9002", "");
        check("Updated GUI Customer".equals(helpDesk.getCustomer("C-GUI-001").getName()),
                "Customer editing workflow");

        CustomerDialog addDialog = new CustomerDialog(frame, helpDesk, null);
        ArrayList<JTextField> addFields = findComponents(addDialog, JTextField.class);
        check(addFields.size() >= 7, "Add Customer form fields exist");
        addFields.get(0).setText("C-DIALOG-001");
        addFields.get(1).setText("Dialog Business LLC");
        addFields.get(2).setText("dialog.business@example.com");
        addFields.get(3).setText("+383 38 555 100");
        addFields.get(4).setText("Dialog Test Address");
        addFields.get(5).setText("REG-DIALOG-1");
        addFields.get(6).setText("Test Contact");
        findButton(addDialog, "Save").doClick();
        check(addDialog.isSaved() && helpDesk.getCustomerCount() == 6,
                "Add Customer Save button registers data");

        Customer dialogCustomer = helpDesk.getCustomer("C-DIALOG-001");
        CustomerDialog editDialog = new CustomerDialog(frame, helpDesk, dialogCustomer);
        ArrayList<JTextField> editFields = findComponents(editDialog, JTextField.class);
        editFields.get(1).setText("Edited Dialog Business LLC");
        findButton(editDialog, "Save").doClick();
        check("Edited Dialog Business LLC".equals(dialogCustomer.getName()),
                "Edit Customer Save button updates data");

        AssignProductDialog assignDialog = new AssignProductDialog(frame, helpDesk, dialogCustomer);
        findButton(assignDialog, "Assign").doClick();
        check(assignDialog.isSaved() && !dialogCustomer.getProducts().isEmpty(),
                "Assign Product button updates customer products");

        TicketDialog createTicketDialog = new TicketDialog(frame, helpDesk);
        ArrayList<JComboBox> ticketBoxes = findComponents(createTicketDialog, JComboBox.class);
        JComboBox customerBox = findComboBoxWithItemCount(ticketBoxes, helpDesk.getCustomerCount());
        check(customerBox != null, "New Ticket customer dropdown exists");
        customerBox.setSelectedIndex(helpDesk.getCustomerCount() - 1);
        ArrayList<JTextField> ticketFields = findComponents(createTicketDialog, JTextField.class);
        ArrayList<JTextArea> ticketAreas = findComponents(createTicketDialog, JTextArea.class);
        ticketFields.get(0).setText("T-DIALOG-001");
        ticketFields.get(1).setText("Dialog-created ticket");
        ticketAreas.get(0).setText("Created through the Swing dialog Save action.");
        findButton(createTicketDialog, "Create Ticket").doClick();
        check(createTicketDialog.isSaved() && helpDesk.getTicket("T-DIALOG-001") != null,
                "New Ticket button registers ticket");
        check(helpDesk.getTicket("T-DIALOG-001").getResponsibleAgent() == null,
                "New Ticket dialog does not assign an agent");

        CustomerDialog customerCancelDialog = new CustomerDialog(frame, helpDesk, null);
        JButton customerCancel = findButton(customerCancelDialog, "Cancel");
        customerCancel.doClick();
        check(!customerCancelDialog.isDisplayable(), "Customer dialog Cancel closes safely");

        TicketDialog ticketCancelDialog = new TicketDialog(frame, helpDesk);
        JButton ticketCancel = findButton(ticketCancelDialog, "Cancel");
        ticketCancel.doClick();
        check(!ticketCancelDialog.isDisplayable(), "Ticket dialog Cancel closes safely");

        AssignAgentDialog agentCancelDialog = new AssignAgentDialog(frame, helpDesk,
                helpDesk.getTicket("T-DIALOG-001"));
        findButton(agentCancelDialog, "Cancel").doClick();
        check(!agentCancelDialog.isDisplayable(), "Agent-assignment Cancel closes safely");

        frame.refreshAll();
        check(customers.getVisibleRowCount() == helpDesk.getCustomerCount(),
                "Tables refresh after dialog operations");
        frame.dispose();
    }

    private JComboBox findComboBoxWithItemCount(ArrayList<JComboBox> boxes, int itemCount) {
        for (JComboBox box : boxes) {
            if (box.getItemCount() == itemCount) {
                return box;
            }
        }
        return null;
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
            throw new AssertionError("GUI check failed: " + description);
        }
        passedChecks++;
        System.out.println("PASS: " + description);
    }
}
