package helpdesk;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            System.out.println("The system look and feel could not be loaded.");
        }

        try {
            final HelpDesk helpDesk = DemoData.createHelpDeskWithSampleData();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    HelpDeskFrame window = new HelpDeskFrame(helpDesk);
                    window.setVisible(true);
                }
            });
        } catch (HelpDeskException | IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(null,
                    "The application could not start: " + exception.getMessage(),
                    "FiberNet HelpDesk", JOptionPane.ERROR_MESSAGE);
        }
    }
}
