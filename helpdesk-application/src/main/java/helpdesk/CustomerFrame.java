package helpdesk;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

public class CustomerFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final HelpDesk helpDesk;
    private final Customer customer;
    private final Runnable logoutAction;
    private final ReadOnlyTableModel productModel;
    private final ReadOnlyTableModel ticketModel;
    private final JTable ticketTable;

    public CustomerFrame(HelpDesk helpDesk, Customer customer, Runnable logoutAction) {
        super("FiberNet HelpDesk - Customer");
        this.helpDesk = helpDesk;
        this.customer = customer;
        this.logoutAction = logoutAction;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 480));
        setSize(980, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        JLabel title = new JLabel("Customer Account - " + customer.getName());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16.0f));
        heading.add(title, BorderLayout.WEST);
        add(heading, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My Account", createAccountPanel());

        productModel = new ReadOnlyTableModel(new String[] {
            "Product ID", "Name", "Type", "Monthly Price", "Description"
        });
        JTable productTable = GuiUtil.createTable(productModel);
        tabs.addTab("My Products / Services", new JScrollPane(productTable));

        ticketModel = new ReadOnlyTableModel(new String[] {
            "Ticket ID", "Problem / Subject", "Product / Service", "Priority",
            "Assigned Agent", "Status", "Created Date"
        });
        ticketTable = GuiUtil.createTable(ticketModel);
        ticketTable.getColumnModel().getColumn(1).setPreferredWidth(230);
        ticketTable.getColumnModel().getColumn(2).setPreferredWidth(170);
        JPanel ticketsPanel = new JPanel(new BorderLayout(6, 6));
        ticketsPanel.add(new JScrollPane(ticketTable), BorderLayout.CENTER);
        JPanel ticketButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        JButton newTicketButton = new JButton("New Ticket");
        newTicketButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                openNewTicketDialog();
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
        ticketButtons.add(newTicketButton);
        ticketButtons.add(detailsButton);
        ticketButtons.add(historyButton);
        ticketButtons.add(refreshButton);
        ticketsPanel.add(ticketButtons, BorderLayout.SOUTH);
        tabs.addTab("My Tickets", ticketsPanel);

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        center.add(tabs, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                performLogout();
            }
        });
        bottom.add(logoutButton);
        add(bottom, BorderLayout.SOUTH);

        refreshData();
    }

    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.add(new JLabel("Customer ID:"));
        panel.add(new JLabel(customer.getId()));
        panel.add(new JLabel("Name:"));
        panel.add(new JLabel(customer.getName()));
        panel.add(new JLabel("Category:"));
        panel.add(new JLabel(customer.getCategory().toString()));
        panel.add(new JLabel("Email:"));
        panel.add(new JLabel(customer.getEmail()));
        panel.add(new JLabel("Phone:"));
        panel.add(new JLabel(customer.getPhone()));
        panel.add(new JLabel("Address:"));
        panel.add(new JLabel(customer.getAddress()));
        panel.add(new JLabel("Support policy:"));
        panel.add(new JLabel(customer.getSupportPolicy()));
        return panel;
    }

    public void refreshData() {
        productModel.setRowCount(0);
        for (Product product : customer.getProducts()) {
            productModel.addRow(new Object[] {
                product.getId(), product.getName(), product.getType(),
                String.format("%.2f", product.getMonthlyPrice()), product.getDescription()
            });
        }

        ticketModel.setRowCount(0);
        for (Ticket ticket : customer.getTicketHistory()) {
            ticketModel.addRow(new Object[] {
                ticket.getId(), ticket.getTitle(), ticket.getProduct().getName(),
                ticket.getPriority(), ticket.getResponsibleAgentName(), ticket.getStatus(),
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
            Ticket ticket = helpDesk.getTicket(ticketId);
            if (ticket.getCustomer() != customer) {
                throw new HelpDeskException("This ticket does not belong to the logged-in customer.");
            }
            return ticket;
        } catch (HelpDeskException exception) {
            GuiUtil.showError(this, exception);
            return null;
        }
    }

    private void openNewTicketDialog() {
        TicketDialog dialog = new TicketDialog(this, helpDesk, customer);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
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

    void performLogout() {
        dispose();
        if (logoutAction != null) {
            logoutAction.run();
        }
    }




}
