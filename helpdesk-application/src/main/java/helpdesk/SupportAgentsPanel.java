package helpdesk;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class SupportAgentsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final HelpDeskFrame owner;
    private final HelpDesk helpDesk;
    private final ReadOnlyTableModel tableModel;
    private final JTable agentTable;

    public SupportAgentsPanel(HelpDeskFrame owner, HelpDesk helpDesk) {
        this.owner = owner;
        this.helpDesk = helpDesk;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tableModel = new ReadOnlyTableModel(new String[] {
            "ID", "Name", "Email", "Specialization", "Assigned Tickets"
        });
        agentTable = GuiUtil.createTable(tableModel);
        agentTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        agentTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        agentTable.getColumnModel().getColumn(2).setPreferredWidth(210);
        agentTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        agentTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        add(new JScrollPane(agentTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        JButton assignedButton = new JButton("View Assigned Tickets");
        assignedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showAssignedTickets();
            }
        });
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                refreshData();
            }
        });
        buttonPanel.add(assignedButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        for (SupportAgent agent : helpDesk.getSupportAgents()) {
            tableModel.addRow(new Object[] {
                agent.getId(), agent.getFullName(), agent.getEmail(),
                agent.getSpecialization(), agent.getAssignedTickets().size()
            });
        }
    }

    private void showAssignedTickets() {
        String agentId = GuiUtil.getSelectedId(agentTable);
        if (agentId == null) {
            GuiUtil.showSelectionRequired(this, "support agent");
            return;
        }
        try {
            SupportAgent agent = helpDesk.getSupportAgent(agentId);
            StringBuilder text = new StringBuilder();
            text.append(agent.getFullName()).append("\n");
            text.append(agent.getEmail()).append("\n");
            text.append("Specialization: ").append(agent.getSpecialization()).append("\n\n");
            if (agent.getAssignedTickets().isEmpty()) {
                text.append("No tickets are assigned to this agent.");
            } else {
                for (Ticket ticket : agent.getAssignedTickets()) {
                    text.append(ticket.getId()).append(" | ").append(ticket.getStatus())
                            .append(" | ").append(ticket.getTitle())
                            .append(" | ").append(ticket.getCustomer().getName()).append("\n");
                }
            }
            TextDialog.showText(owner, "Assigned Tickets - " + agent.getFullName(),
                    text.toString());
        } catch (HelpDeskException exception) {
            GuiUtil.showError(this, exception);
        }
    }

}
