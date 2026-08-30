package helpdesk;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JLabel;

public class TicketsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final HelpDeskFrame owner;
    private final HelpDesk helpDesk;
    private final Runnable refreshAction;
    private final ReadOnlyTableModel tableModel;
    private final JTable ticketTable;
    private final JTextField searchField;
    private final JComboBox<String> statusBox;
    private final JComboBox<String> agentBox;
    private final ArrayList<SupportAgent> filterAgents;

    public TicketsPanel(HelpDeskFrame owner, HelpDesk helpDesk, Runnable refreshAction) {
        this.owner = owner;
        this.helpDesk = helpDesk;
        this.refreshAction = refreshAction;
        filterAgents = new ArrayList<SupportAgent>();

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        filterPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        filterPanel.add(searchField);
        filterPanel.add(new JLabel("Status:"));
        statusBox = new JComboBox<String>();
        statusBox.addItem("All");
        for (TicketStatus status : TicketStatus.values()) {
            statusBox.addItem(status.toString());
        }
        filterPanel.add(statusBox);
        filterPanel.add(new JLabel("Agent:"));
        agentBox = new JComboBox<String>();
        agentBox.setPrototypeDisplayValue("A-000 - Long Support Agent Name");
        filterPanel.add(agentBox);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                applyFilters();
            }
        });
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                searchField.setText("");
                statusBox.setSelectedIndex(0);
                agentBox.setSelectedIndex(0);
                applyFilters();
            }
        });
        filterPanel.add(searchButton);
        filterPanel.add(clearButton);
        add(filterPanel, BorderLayout.NORTH);

        tableModel = new ReadOnlyTableModel(new String[] {
            "Ticket ID", "Customer", "Problem / Subject", "Priority",
            "Assigned Agent", "Status", "Created Date"
        });
        ticketTable = GuiUtil.createTable(tableModel);
        ticketTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        ticketTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        ticketTable.getColumnModel().getColumn(2).setPreferredWidth(240);
        ticketTable.getColumnModel().getColumn(4).setPreferredWidth(140);
        ticketTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        add(new JScrollPane(ticketTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        JButton newButton = new JButton("New Ticket");
        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                openNewTicketDialog();
            }
        });
        JButton assignButton = new JButton("Assign Agent");
        assignButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                assignSelectedAgent();
            }
        });
        JButton statusButton = new JButton("Update Status");
        statusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updateSelectedStatus();
            }
        });
        JButton detailsButton = new JButton("View Details");
        detailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showSelectedDetails();
            }
        });
        JButton historyButton = new JButton("View History");
        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showSelectedHistory();
            }
        });
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                refreshData();
            }
        });
        buttonPanel.add(newButton);
        buttonPanel.add(assignButton);
        buttonPanel.add(statusButton);
        buttonPanel.add(detailsButton);
        buttonPanel.add(historyButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshData() {
        int selectedAgentIndex = agentBox.getSelectedIndex();
        filterAgents.clear();
        agentBox.removeAllItems();
        agentBox.addItem("All");
        agentBox.addItem("Unassigned");
        for (SupportAgent agent : helpDesk.getSupportAgents()) {
            filterAgents.add(agent);
            agentBox.addItem(agent.getId() + " - " + agent.getFullName());
        }
        if (selectedAgentIndex >= 0 && selectedAgentIndex < agentBox.getItemCount()) {
            agentBox.setSelectedIndex(selectedAgentIndex);
        }
        applyFilters();
    }

    void applyFilters() {
        TicketStatus status = statusBox.getSelectedIndex() == 0
                ? null : TicketStatus.values()[statusBox.getSelectedIndex() - 1];
        String agentId = null;
        if (agentBox.getSelectedIndex() == 1) {
            agentId = HelpDesk.UNASSIGNED_AGENT_FILTER;
        } else if (agentBox.getSelectedIndex() >= 2) {
            agentId = filterAgents.get(agentBox.getSelectedIndex() - 2).getId();
        }
        ArrayList<Ticket> tickets = helpDesk.filterTickets(searchField.getText(), status, agentId);

        tableModel.setRowCount(0);
        for (Ticket ticket : tickets) {
            tableModel.addRow(new Object[] {
                ticket.getId(), ticket.getCustomer().getName(), ticket.getTitle(),
                ticket.getPriority(), ticket.getResponsibleAgentName(),
                ticket.getStatus(), GuiUtil.formatDate(ticket.getCreatedAt())
            });
        }
    }

    void openNewTicketDialog() {
        TicketDialog dialog = new TicketDialog(owner, helpDesk);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshAction.run();
        }
    }

    void assignSelectedAgent() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) {
            return;
        }
        AssignAgentDialog dialog = new AssignAgentDialog(owner, helpDesk, ticket);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshAction.run();
        }
    }

    void updateSelectedStatus() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) {
            return;
        }
        TicketStatusDialog dialog = new TicketStatusDialog(owner, helpDesk, ticket);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshAction.run();
        }
    }

    private Ticket getSelectedTicket() {
        String ticketId = GuiUtil.getSelectedId(ticketTable);
        if (ticketId == null) {
            GuiUtil.showSelectionRequired(this, "ticket");
            return null;
        }
        try {
            return helpDesk.getTicket(ticketId);
        } catch (HelpDeskException exception) {
            GuiUtil.showError(this, exception);
            return null;
        }
    }

    private void showSelectedDetails() {
        Ticket ticket = getSelectedTicket();
        if (ticket != null) {
            TextDialog.showText(owner, "Ticket Details", ticket.getDetails());
        }
    }

    private void showSelectedHistory() {
        Ticket ticket = getSelectedTicket();
        if (ticket != null) {
            TextDialog.showText(owner, "Ticket History",
                    GuiUtil.formatTicketHistory(ticket));
        }
    }




}
