package helpdesk;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class DashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final HelpDesk helpDesk;
    private final JLabel customerCountLabel;
    private final JLabel productCountLabel;
    private final JLabel agentCountLabel;
    private final JLabel ticketCountLabel;
    private final JLabel activeCountLabel;
    private final ReadOnlyTableModel queueModel;

    public DashboardPanel(HelpDesk helpDesk) {
        this.helpDesk = helpDesk;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JLabel heading = new JLabel("System Overview");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 16.0f));
        headingPanel.add(heading);
        add(headingPanel, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(8, 12));
        JPanel summary = new JPanel(new GridBagLayout());
        summary.setBorder(BorderFactory.createTitledBorder("Current totals"));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(3, 8, 3, 20);
        constraints.gridx = 0;
        constraints.gridy = 0;
        summary.add(new JLabel("Registered customers:"), constraints);
        constraints.gridx = 1;
        customerCountLabel = new JLabel();
        summary.add(customerCountLabel, constraints);
        constraints.gridx = 2;
        summary.add(new JLabel("Products / services:"), constraints);
        constraints.gridx = 3;
        productCountLabel = new JLabel();
        summary.add(productCountLabel, constraints);

        constraints.gridy = 1;
        constraints.gridx = 0;
        summary.add(new JLabel("Support agents:"), constraints);
        constraints.gridx = 1;
        agentCountLabel = new JLabel();
        summary.add(agentCountLabel, constraints);
        constraints.gridx = 2;
        summary.add(new JLabel("Registered tickets:"), constraints);
        constraints.gridx = 3;
        ticketCountLabel = new JLabel();
        summary.add(ticketCountLabel, constraints);

        constraints.gridy = 2;
        constraints.gridx = 0;
        summary.add(new JLabel("Active tickets:"), constraints);
        constraints.gridx = 1;
        activeCountLabel = new JLabel();
        summary.add(activeCountLabel, constraints);
        content.add(summary, BorderLayout.NORTH);

        queueModel = new ReadOnlyTableModel(new String[] {
            "Ticket ID", "Subject", "Priority", "Status", "Assigned Agent"
        });
        JTable queueTable = GuiUtil.createTable(queueModel);
        JScrollPane queueScroll = new JScrollPane(queueTable);
        queueScroll.setBorder(BorderFactory.createTitledBorder("Priority queue"));
        content.add(queueScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                refreshData();
            }
        });
        buttonPanel.add(refreshButton);
        content.add(buttonPanel, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);
    }

    public void refreshData() {
        customerCountLabel.setText(String.valueOf(helpDesk.getCustomerCount()));
        productCountLabel.setText(String.valueOf(helpDesk.getProductCount()));
        agentCountLabel.setText(String.valueOf(helpDesk.getSupportAgentCount()));
        ticketCountLabel.setText(String.valueOf(helpDesk.getTicketCount()));
        activeCountLabel.setText(String.valueOf(helpDesk.getActiveTicketCount()));

        queueModel.setRowCount(0);
        ArrayList<Ticket> tickets = helpDesk.getTicketsInPriorityOrder();
        for (Ticket ticket : tickets) {
            if (ticket.getStatus() != TicketStatus.CLOSED) {
                queueModel.addRow(new Object[] {
                    ticket.getId(), ticket.getTitle(), ticket.getPriority(), ticket.getStatus(),
                    ticket.getResponsibleAgent().getFullName()
                });
            }
        }
    }

    int getQueueRowCount() {
        return queueModel.getRowCount();
    }
}
