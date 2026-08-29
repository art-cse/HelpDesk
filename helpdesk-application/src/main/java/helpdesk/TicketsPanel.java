package helpdesk;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

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
        String agentId = agentBox.getSelectedIndex() <= 0
                ? null : filterAgents.get(agentBox.getSelectedIndex() - 1).getId();
        ArrayList<Ticket> tickets = helpDesk.filterTickets(searchField.getText(), status, agentId);

        tableModel.setRowCount(0);
        for (Ticket ticket : tickets) {
            tableModel.addRow(new Object[] {
                ticket.getId(), ticket.getCustomer().getName(), ticket.getTitle(),
                ticket.getPriority(), ticket.getResponsibleAgent().getFullName(),
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
        ArrayList<SupportAgent> agents = helpDesk.getSupportAgents();
        JComboBox<String> selection = new JComboBox<String>();
        int currentIndex = 0;
        for (int i = 0; i < agents.size(); i++) {
            SupportAgent agent = agents.get(i);
            selection.addItem(agent.getId() + " - " + agent.getFullName());
            if (agent == ticket.getResponsibleAgent()) {
                currentIndex = i;
            }
        }
        selection.setSelectedIndex(currentIndex);

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JLabel("Responsible support agent:"), BorderLayout.NORTH);
        panel.add(selection, BorderLayout.CENTER);
        int result = JOptionPane.showConfirmDialog(owner, panel,
                "Assign Agent - " + ticket.getId(), JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            helpDesk.assignAgentToTicket(ticket.getId(),
                    agents.get(selection.getSelectedIndex()).getId());
            refreshAction.run();
        } catch (HelpDeskException exception) {
            GuiUtil.showError(this, exception);
        }
    }

    void updateSelectedStatus() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) {
            return;
        }

        JComboBox<TicketStatus> statusSelection =
                new JComboBox<TicketStatus>(TicketStatus.values());
        statusSelection.setSelectedItem(getSuggestedNextStatus(ticket.getStatus()));
        JTextArea noteArea = new JTextArea(4, 30);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        form.add(new JLabel("Current status:"), constraints);
        constraints.gridx = 1;
        form.add(new JLabel(ticket.getStatus().toString()), constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        form.add(new JLabel("New status:"), constraints);
        constraints.gridx = 1;
        form.add(statusSelection, constraints);
        constraints.gridx = 0;
        constraints.gridy = 2;
        form.add(new JLabel("Note:"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        JScrollPane noteScroll = new JScrollPane(noteArea);
        noteScroll.setPreferredSize(new Dimension(340, 90));
        form.add(noteScroll, constraints);

        int result = JOptionPane.showConfirmDialog(owner, form,
                "Update Ticket Status - " + ticket.getId(), JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        if (noteArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(owner, "Enter a note for the status change.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            helpDesk.updateTicketStatus(ticket.getId(),
                    (TicketStatus) statusSelection.getSelectedItem(), noteArea.getText());
            refreshAction.run();
        } catch (HelpDeskException exception) {
            GuiUtil.showError(this, exception);
        }
    }

    private TicketStatus getSuggestedNextStatus(TicketStatus current) {
        if (current == TicketStatus.OPEN) {
            return TicketStatus.IN_PROGRESS;
        }
        if (current == TicketStatus.IN_PROGRESS) {
            return TicketStatus.RESOLVED;
        }
        if (current == TicketStatus.WAITING_FOR_CUSTOMER) {
            return TicketStatus.IN_PROGRESS;
        }
        if (current == TicketStatus.RESOLVED) {
            return TicketStatus.CLOSED;
        }
        return TicketStatus.CLOSED;
    }

    private void showSelectedDetails() {
        Ticket ticket = getSelectedTicket();
        if (ticket != null) {
            TextDialog.showText(owner, "Ticket Details - " + ticket.getId(), ticket.getDetails());
        }
    }

    private void showSelectedHistory() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) {
            return;
        }
        StringBuilder text = new StringBuilder();
        text.append(ticket.getId()).append(" - ").append(ticket.getTitle()).append("\n");
        text.append("Customer: ").append(ticket.getCustomer().getName()).append("\n");
        text.append("Current agent: ").append(ticket.getResponsibleAgent().getFullName()).append("\n");
        text.append("Current status: ").append(ticket.getStatus()).append("\n\n");
        text.append("Status history\n");
        text.append("--------------\n");
        for (StatusChange change : ticket.getStatusHistory()) {
            text.append(change).append("\n");
        }
        TextDialog.showText(owner, "Ticket History - " + ticket.getId(), text.toString());
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

    int getVisibleRowCount() {
        return tableModel.getRowCount();
    }

    JTable getTicketTable() {
        return ticketTable;
    }

    void setSearchText(String text) {
        searchField.setText(text);
    }

    void setAgentFilterIndex(int index) {
        agentBox.setSelectedIndex(index);
    }
}
