package helpdesk;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class RoleWorkflowTest {
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        final RoleWorkflowTest test = new RoleWorkflowTest();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    test.testRoleLogins();
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }
        });
        System.out.println("All role access tests passed.");
    }

    private void testRoleLogins() throws Exception {
        HelpDesk helpDesk = DemoData.createHelpDeskWithSampleData();
        AuthenticationService authentication = DemoData.createAuthenticationService();

        UserAccount adminAccount = authentication.authenticate("admin", "admin123");
        UserAccount agentAccount = authentication.authenticate("agent1", "agent123");
        UserAccount customerAccount = authentication.authenticate("customer1", "customer123");

        check(adminAccount.getRole() == UserRole.ADMIN, "Admin account should have admin access.");
        check(agentAccount.getRole() == UserRole.AGENT,
                "Agent account should have support-agent access.");
        check(customerAccount.getRole() == UserRole.CUSTOMER,
                "Customer account should have customer access.");
        check(helpDesk.getSupportAgent(agentAccount.getLinkedEntityId()) != null,
                "Agent account should link to one support agent.");
        check(helpDesk.getCustomer(customerAccount.getLinkedEntityId()) != null,
                "Customer account should link to one customer.");

        boolean invalidLoginRejected = false;
        try {
            authentication.authenticate("admin", "wrong-password");
        } catch (HelpDeskException exception) {
            invalidLoginRejected = true;
        }
        check(invalidLoginRejected, "Invalid login details should be rejected.");

        LoginFrame login = new LoginFrame(helpDesk, authentication);
        JFrame adminWindow = login.openForCredentials("admin", "admin123");
        check(adminWindow instanceof HelpDeskFrame, "Admin login should open the admin window.");
        adminWindow.dispose();

        JFrame agentWindow = login.openForCredentials("agent1", "agent123");
        check(agentWindow instanceof AgentFrame, "Agent login should open the agent window.");
        agentWindow.dispose();

        JFrame customerWindow = login.openForCredentials("customer1", "customer123");
        check(customerWindow instanceof CustomerFrame,
                "Customer login should open the customer window.");
        customerWindow.dispose();
        login.dispose();

        System.out.println("PASS: admin, agent, and customer role access");
    }

    private void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
