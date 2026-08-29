package helpdesk;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class GuiVisualReview {
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        File reviewDirectory = new File("visual-review");
        if (!reviewDirectory.exists() && !reviewDirectory.mkdirs()) {
            throw new IllegalStateException("Could not create visual-review directory.");
        }

        final HelpDesk helpDesk = DemoData.createHelpDeskWithSampleData();
        final AuthenticationService authentication = DemoData.createAuthenticationService();

        LoginFrame login = createLogin(helpDesk, authentication);
        capture(login, new File(reviewDirectory, "01-login.png"));
        login.dispose();

        HelpDeskFrame admin = createAdmin(helpDesk);
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                admin.getTabs().setSelectedIndex(2);
            }
        });
        capture(admin, new File(reviewDirectory, "02-admin-tickets.png"));

        TicketDialog adminTicket = createAdminTicketDialog(admin, helpDesk);
        capture(adminTicket, new File(reviewDirectory, "03-admin-new-ticket.png"));
        adminTicket.dispose();

        AssignAgentDialog assignment = createAssignmentDialog(admin, helpDesk,
                helpDesk.getTicket("T-1004"));
        capture(assignment, new File(reviewDirectory, "04-assign-agent.png"));
        assignment.dispose();

        AgentFrame agent = createAgent(helpDesk, helpDesk.getSupportAgent("A-01"));
        capture(agent, new File(reviewDirectory, "05-agent-my-tickets.png"));

        TicketStatusDialog status = createStatusDialog(agent, helpDesk,
                helpDesk.getTicket("T-1001"));
        capture(status, new File(reviewDirectory, "06-agent-update-status.png"));
        status.dispose();
        agent.dispose();

        Customer customer = helpDesk.getCustomer("C-RES-001");
        CustomerFrame customerFrame = createCustomer(helpDesk, customer);
        capture(customerFrame, new File(reviewDirectory, "07-customer-account.png"));

        JTabbedPane customerTabs = findComponent(customerFrame, JTabbedPane.class);
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                customerTabs.setSelectedIndex(2);
            }
        });
        capture(customerFrame, new File(reviewDirectory, "08-customer-tickets.png"));

        TicketDialog customerTicket = createCustomerTicketDialog(customerFrame, helpDesk,
                customer);
        capture(customerTicket, new File(reviewDirectory, "09-customer-new-ticket.png"));
        customerTicket.dispose();

        TextDialog history = createHistoryDialog(customerFrame, helpDesk.getTicket("T-1003"));
        capture(history, new File(reviewDirectory, "10-customer-ticket-history.png"));
        history.dispose();

        customerFrame.dispose();
        admin.dispose();
        System.out.println("Captured 10 role-aware Swing screens in "
                + reviewDirectory.getAbsolutePath());
    }

    private static LoginFrame createLogin(final HelpDesk helpDesk,
            final AuthenticationService authentication) throws Exception {
        final LoginFrame[] holder = new LoginFrame[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                holder[0] = new LoginFrame(helpDesk, authentication);
                holder[0].setVisible(true);
            }
        });
        return holder[0];
    }

    private static HelpDeskFrame createAdmin(final HelpDesk helpDesk) throws Exception {
        final HelpDeskFrame[] holder = new HelpDeskFrame[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                holder[0] = new HelpDeskFrame(helpDesk);
                holder[0].setVisible(true);
            }
        });
        return holder[0];
    }

    private static TicketDialog createAdminTicketDialog(final HelpDeskFrame owner,
            final HelpDesk helpDesk) throws Exception {
        final TicketDialog[] holder = new TicketDialog[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                holder[0] = new TicketDialog(owner, helpDesk);
                holder[0].setModal(false);
                holder[0].setVisible(true);
            }
        });
        return holder[0];
    }

    private static AssignAgentDialog createAssignmentDialog(final HelpDeskFrame owner,
            final HelpDesk helpDesk, final Ticket ticket) throws Exception {
        final AssignAgentDialog[] holder = new AssignAgentDialog[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                holder[0] = new AssignAgentDialog(owner, helpDesk, ticket);
                holder[0].setModal(false);
                holder[0].setVisible(true);
            }
        });
        return holder[0];
    }

    private static AgentFrame createAgent(final HelpDesk helpDesk, final SupportAgent agent)
            throws Exception {
        final AgentFrame[] holder = new AgentFrame[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                holder[0] = new AgentFrame(helpDesk, agent, null);
                holder[0].setVisible(true);
            }
        });
        return holder[0];
    }

    private static TicketStatusDialog createStatusDialog(final AgentFrame owner,
            final HelpDesk helpDesk, final Ticket ticket) throws Exception {
        final TicketStatusDialog[] holder = new TicketStatusDialog[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                holder[0] = new TicketStatusDialog(owner, helpDesk, ticket);
                holder[0].setModal(false);
                holder[0].setVisible(true);
            }
        });
        return holder[0];
    }

    private static CustomerFrame createCustomer(final HelpDesk helpDesk,
            final Customer customer) throws Exception {
        final CustomerFrame[] holder = new CustomerFrame[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                holder[0] = new CustomerFrame(helpDesk, customer, null);
                holder[0].setVisible(true);
            }
        });
        return holder[0];
    }

    private static TicketDialog createCustomerTicketDialog(final CustomerFrame owner,
            final HelpDesk helpDesk, final Customer customer) throws Exception {
        final TicketDialog[] holder = new TicketDialog[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                holder[0] = new TicketDialog(owner, helpDesk, customer);
                holder[0].setModal(false);
                holder[0].setVisible(true);
            }
        });
        return holder[0];
    }

    private static TextDialog createHistoryDialog(final CustomerFrame owner,
            final Ticket ticket) throws Exception {
        final TextDialog[] holder = new TextDialog[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                holder[0] = new TextDialog(owner, "Ticket History",
                        GuiUtil.formatTicketHistory(ticket));
                holder[0].setModal(false);
                holder[0].setVisible(true);
            }
        });
        return holder[0];
    }

    private static <T extends Component> T findComponent(Container container,
            Class<T> componentType) {
        for (Component component : container.getComponents()) {
            if (componentType.isInstance(component)) {
                return componentType.cast(component);
            }
            if (component instanceof Container) {
                T result = findComponent((Container) component, componentType);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static void capture(Window window, File destination) throws Exception {
        window.toFront();
        Thread.sleep(250);
        Point location = window.getLocationOnScreen();
        Rectangle bounds = new Rectangle(location.x, location.y,
                window.getWidth(), window.getHeight());
        BufferedImage image = new Robot().createScreenCapture(bounds);
        ImageIO.write(image, "png", destination);
    }
}
