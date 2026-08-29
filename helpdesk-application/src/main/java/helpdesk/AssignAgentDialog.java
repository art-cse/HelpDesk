package helpdesk;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class AssignAgentDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final HelpDesk helpDesk;
    private final Ticket ticket;
    private final ArrayList<SupportAgent> agents;
    private final JComboBox<String> agentBox;
    private boolean saved;

    public AssignAgentDialog(Frame owner, HelpDesk helpDesk, Ticket ticket) {
        super(owner, "Assign Support Agent - " + ticket.getId(), true);
        this.helpDesk = helpDesk;
        this.ticket = ticket;
        agents = helpDesk.getSupportAgents();
        saved = false;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new BorderLayout(8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        form.add(new JLabel("Responsible support agent:"), BorderLayout.NORTH);
        agentBox = new JComboBox<String>();
        for (SupportAgent agent : agents) {
            agentBox.addItem(agent.getId() + " - " + agent.getFullName());
        }
        selectCurrentAgent();
        form.add(agentBox, BorderLayout.CENTER);
        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        JButton assignButton = new JButton("Assign");
        getRootPane().setDefaultButton(assignButton);
        assignButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                assignAgent();
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
        buttons.add(assignButton);
        buttons.add(cancelButton);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(460, getHeight()));
        setLocationRelativeTo(owner);
    }

    private void selectCurrentAgent() {
        SupportAgent current = ticket.getResponsibleAgent();
        if (current == null) {
            return;
        }
        for (int i = 0; i < agents.size(); i++) {
            if (agents.get(i) == current) {
                agentBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void assignAgent() {
        if (agentBox.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "No support agent is available.",
                    "Agent required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            SupportAgent selectedAgent = agents.get(agentBox.getSelectedIndex());
            helpDesk.assignAgentToTicket(ticket.getId(), selectedAgent.getId());
            saved = true;
            dispose();
        } catch (HelpDeskException exception) {
            GuiUtil.showError(this, exception);
        }
    }

    void setSelectedAgentId(String agentId) {
        for (int i = 0; i < agents.size(); i++) {
            if (agents.get(i).getId().equalsIgnoreCase(agentId)) {
                agentBox.setSelectedIndex(i);
                return;
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
