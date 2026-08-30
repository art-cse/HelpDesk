package helpdesk;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final HelpDesk helpDesk;
    private final AuthenticationService authentication;
    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public LoginFrame(HelpDesk helpDesk, AuthenticationService authentication) {
        super("FiberNet HelpDesk - Login");
        this.helpDesk = helpDesk;
        this.authentication = authentication;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(8, 8));

        JLabel heading = new JLabel("FiberNet HelpDesk", JLabel.CENTER);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 17.0f));
        heading.setBorder(BorderFactory.createEmptyBorder(14, 10, 4, 10));
        add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.gridx = 0;
        constraints.gridy = 0;
        form.add(new JLabel("Username:"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        usernameField = new JTextField(18);
        form.add(usernameField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0;
        form.add(new JLabel("Password:"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        passwordField = new JPasswordField(18);
        form.add(passwordField, constraints);
        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        JButton loginButton = new JButton("Login");
        getRootPane().setDefaultButton(loginButton);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                handleLogin();
            }
        });
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
        buttons.add(loginButton);
        buttons.add(exitButton);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(370, getHeight()));
        setLocationRelativeTo(null);
    }

    private void handleLogin() {
        try {
            openForCredentials(usernameField.getText(),
                    new String(passwordField.getPassword()));
        } catch (HelpDeskException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Login failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }

    JFrame openForCredentials(String username, String password) throws HelpDeskException {
        UserAccount account = authentication.authenticate(username, password);
        Runnable logoutAction = new Runnable() {
            @Override
            public void run() {
                usernameField.setText("");
                passwordField.setText("");
                setVisible(true);
                toFront();
            }
        };

        JFrame roleFrame;
        if (account.getRole() == UserRole.ADMIN) {
            roleFrame = new HelpDeskFrame(helpDesk, logoutAction);
        } else if (account.getRole() == UserRole.AGENT) {
            SupportAgent agent = helpDesk.getSupportAgent(account.getLinkedEntityId());
            roleFrame = new AgentFrame(helpDesk, agent, logoutAction);
        } else {
            Customer customer = helpDesk.getCustomer(account.getLinkedEntityId());
            roleFrame = new CustomerFrame(helpDesk, customer, logoutAction);
        }

        setVisible(false);
        roleFrame.setVisible(true);
        return roleFrame;
    }


}
