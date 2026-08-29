package helpdesk;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TextDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    public TextDialog(HelpDeskFrame owner, String title, String text) {
        super(owner, title, true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JTextArea area = GuiUtil.createTextArea(text);
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
        buttons.add(closeButton);
        add(buttons, BorderLayout.SOUTH);

        setSize(700, 460);
        setLocationRelativeTo(owner);
    }

    public static void showText(HelpDeskFrame owner, String title, String text) {
        TextDialog dialog = new TextDialog(owner, title, text);
        dialog.setVisible(true);
    }
}
