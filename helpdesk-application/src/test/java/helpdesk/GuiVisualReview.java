package helpdesk;

import java.awt.Dialog;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
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
        final HelpDeskFrame[] frameHolder = new HelpDeskFrame[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                frameHolder[0] = new HelpDeskFrame(helpDesk);
                frameHolder[0].setVisible(true);
            }
        });
        HelpDeskFrame frame = frameHolder[0];

        String[] tabNames = { "01-overview", "02-customers", "03-tickets",
                "04-products", "05-agents" };
        for (int i = 0; i < tabNames.length; i++) {
            final int tabIndex = i;
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    frame.getTabs().setSelectedIndex(tabIndex);
                    frame.toFront();
                }
            });
            Thread.sleep(300);
            capture(frame, new File(reviewDirectory, tabNames[i] + ".png"));
        }

        final CustomerDialog[] customerDialog = new CustomerDialog[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                customerDialog[0] = new CustomerDialog(frame, helpDesk, null);
                customerDialog[0].setModal(false);
                customerDialog[0].setVisible(true);
            }
        });
        Thread.sleep(250);
        capture(customerDialog[0], new File(reviewDirectory, "06-register-customer.png"));
        customerDialog[0].dispose();

        final TicketDialog[] ticketDialog = new TicketDialog[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                ticketDialog[0] = new TicketDialog(frame, helpDesk);
                ticketDialog[0].setModal(false);
                ticketDialog[0].setVisible(true);
            }
        });
        Thread.sleep(250);
        capture(ticketDialog[0], new File(reviewDirectory, "07-register-ticket.png"));
        ticketDialog[0].dispose();

        final AssignProductDialog[] productDialog = new AssignProductDialog[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                productDialog[0] = new AssignProductDialog(frame, helpDesk,
                        helpDesk.getCustomers().get(0));
                productDialog[0].setModal(false);
                productDialog[0].setVisible(true);
            }
        });
        Thread.sleep(250);
        capture(productDialog[0], new File(reviewDirectory, "08-assign-product.png"));
        productDialog[0].dispose();

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                frame.getTabs().setSelectedIndex(2);
                frame.getTicketsPanel().getTicketTable().setRowSelectionInterval(0, 0);
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.getTicketsPanel().updateSelectedStatus();
            }
        });
        JDialog statusDialog = waitForDialog("Update Ticket Status");
        capture(statusDialog, new File(reviewDirectory, "09-update-status.png"));
        statusDialog.dispose();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.getTicketsPanel().assignSelectedAgent();
            }
        });
        JDialog agentDialog = waitForDialog("Assign Agent");
        capture(agentDialog, new File(reviewDirectory, "10-assign-agent.png"));
        agentDialog.dispose();

        final TextDialog[] historyDialog = new TextDialog[1];
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                String history = "T-1003 - Router restarts every evening\n\n"
                        + helpDesk.getCustomers().get(2).getTicketHistory().get(0)
                                .getStatusHistory().get(0)
                        + "\n\nHistory entries continue in the scrollable area.";
                historyDialog[0] = new TextDialog(frame, "Ticket History - T-1003", history);
                historyDialog[0].setModal(false);
                historyDialog[0].setVisible(true);
            }
        });
        Thread.sleep(250);
        capture(historyDialog[0], new File(reviewDirectory, "11-ticket-history.png"));
        historyDialog[0].dispose();

        frame.dispose();
        System.out.println("Captured 11 Swing screens in " + reviewDirectory.getAbsolutePath());
    }

    private static JDialog waitForDialog(String titlePrefix) throws Exception {
        for (int attempt = 0; attempt < 50; attempt++) {
            for (Window window : Window.getWindows()) {
                if (window instanceof JDialog && window.isVisible()) {
                    JDialog dialog = (JDialog) window;
                    if (dialog.getTitle().startsWith(titlePrefix)) {
                        Thread.sleep(250);
                        return dialog;
                    }
                }
            }
            Thread.sleep(100);
        }
        throw new IllegalStateException("Dialog did not open: " + titlePrefix);
    }

    private static void capture(Window window, File destination) throws Exception {
        window.toFront();
        Thread.sleep(150);
        Point location = window.getLocationOnScreen();
        Rectangle bounds = new Rectangle(location.x, location.y,
                window.getWidth(), window.getHeight());
        BufferedImage image = new Robot().createScreenCapture(bounds);
        ImageIO.write(image, "png", destination);
    }
}
