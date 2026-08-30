package helpdesk;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class AgentFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final HelpDesk helpDesk;
    private final SupportAgent agent;
    private final Runnable logoutAction;
    private final ReadOnlyTableModel tableModel;
    private final JTable ticketTable;

    public AgentFrame(HelpDesk helpDesk, SupportAgent agent, Runnable logoutAction) {
        super("FiberNet HelpDesk - My Tickets");
        this.helpDesk = helpDesk;
        this.agent = agent;
        this.logoutAction = logoutAction;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(780, 460));
        setSize(980, 580);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        JLabel title = new JLabel("My Tickets - " + agent.getFullName());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16.0f));
        heading.add(title, BorderLayout.WEST);
        heading.add(new JLabel("Specialization: " + agent.getSpecialization()),
                BorderLayout.EAST);
        add(heading, BorderLayout.NORTH);

        tableModel = new ReadOnlyTableModel(new String[] {
            "Ticket ID", "Customer", "Problem / Subject", "Product / Service",
            "Priority", "Status", "Created Date"
        });
        ticketTable = GuiUtil.createTable(tableModel);
        ticketTable.getColumnModel().getColumn(2).setPreferredWidth(230);
        ticketTable.getColumnModel().getColumn(3).setPreferredWidth(170);
        JScrollPane scroll = new JScrollPane(ticketTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Assigned tickets"));
        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
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
        JButton updateButton = new JButton("Update Status");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updateSelectedStatus();
            }
        });
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                refreshData();
            }
        });
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                performLogout();
            }
        });
        buttons.add(detailsButton);
        buttons.add(historyButton);
        buttons.add(updateButton);
        buttons.add(refreshButton);
        buttons.add(logoutButton);
        add(buttons, BorderLayout.SOUTH);

        refreshData();
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        for (Ticket ticket : agent.getAssignedTickets()) {
            tableModel.addRow(new Object[] {
                ticket.getId(), ticket.getCustomer().getName(), ticket.getTitle(),
                ticket.getProduct().getName(), ticket.getPriority(), ticket.getStatus(),
                GuiUtil.formatDate(ticket.getCreatedAt())
            });
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
            TextDialog.showText(this, "Ticket Details", ticket.getDetails());
        }
    }

    private void showSelectedHistory() {
        Ticket ticket = getSelectedTicket();
        if (ticket != null) {
            TextDialog.showText(this, "Ticket History",
                    GuiUtil.formatTicketHistory(ticket));
        }
    }

    private void updateSelectedStatus() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) {
            return;
        }
        TicketStatusDialog dialog = new TicketStatusDialog(this, helpDesk, ticket);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    void performLogout() {
        dispose();
        if (logoutAction != null) {
            logoutAction.run();
        }
    }



}
