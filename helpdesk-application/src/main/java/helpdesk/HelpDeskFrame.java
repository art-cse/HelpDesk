package helpdesk;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

public class HelpDeskFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final HelpDesk helpDesk;
    private final JTabbedPane tabs;
    private final DashboardPanel dashboardPanel;
    private final CustomersPanel customersPanel;
    private final TicketsPanel ticketsPanel;
    private final ProductsPanel productsPanel;
    private final SupportAgentsPanel supportAgentsPanel;

    public HelpDeskFrame(HelpDesk helpDesk) {
        super("FiberNet HelpDesk");
        if (helpDesk == null) {
            throw new IllegalArgumentException("HelpDesk is required.");
        }
        this.helpDesk = helpDesk;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 580));
        setSize(1120, 680);
        setLocationRelativeTo(null);

        Runnable refreshAction = new Runnable() {
            @Override
            public void run() {
                refreshAll();
            }
        };

        dashboardPanel = new DashboardPanel(helpDesk);
        customersPanel = new CustomersPanel(this, helpDesk, refreshAction);
        ticketsPanel = new TicketsPanel(this, helpDesk, refreshAction);
        productsPanel = new ProductsPanel(this, helpDesk, refreshAction);
        supportAgentsPanel = new SupportAgentsPanel(this, helpDesk);

        tabs = new JTabbedPane();
        tabs.addTab("Overview", dashboardPanel);
        tabs.addTab("Customers", customersPanel);
        tabs.addTab("Tickets", ticketsPanel);
        tabs.addTab("Products / Services", productsPanel);
        tabs.addTab("Support Agents", supportAgentsPanel);
        add(tabs);

        setJMenuBar(createMenuBar());
        refreshAll();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem refreshItem = new JMenuItem("Refresh All");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        refreshItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                refreshAll();
            }
        });
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu customerMenu = new JMenu("Customers");
        customerMenu.setMnemonic(KeyEvent.VK_C);
        JMenuItem addCustomerItem = new JMenuItem("Add Customer...");
        addCustomerItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                tabs.setSelectedComponent(customersPanel);
                customersPanel.openAddCustomerDialog();
            }
        });
        JMenuItem editCustomerItem = new JMenuItem("Edit Selected Customer...");
        editCustomerItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                tabs.setSelectedComponent(customersPanel);
                customersPanel.openEditCustomerDialog();
            }
        });
        JMenuItem assignProductItem = new JMenuItem("Assign Product...");
        assignProductItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                tabs.setSelectedComponent(customersPanel);
                customersPanel.openAssignProductDialog();
            }
        });
        customerMenu.add(addCustomerItem);
        customerMenu.add(editCustomerItem);
        customerMenu.add(assignProductItem);

        JMenu ticketMenu = new JMenu("Tickets");
        ticketMenu.setMnemonic(KeyEvent.VK_T);
        JMenuItem newTicketItem = new JMenuItem("New Ticket...");
        newTicketItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                tabs.setSelectedComponent(ticketsPanel);
                ticketsPanel.openNewTicketDialog();
            }
        });
        JMenuItem assignAgentItem = new JMenuItem("Assign Selected Ticket...");
        assignAgentItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                tabs.setSelectedComponent(ticketsPanel);
                ticketsPanel.assignSelectedAgent();
            }
        });
        JMenuItem updateStatusItem = new JMenuItem("Update Selected Status...");
        updateStatusItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                tabs.setSelectedComponent(ticketsPanel);
                ticketsPanel.updateSelectedStatus();
            }
        });
        ticketMenu.add(newTicketItem);
        ticketMenu.add(assignAgentItem);
        ticketMenu.add(updateStatusItem);

        JMenu productMenu = new JMenu("Products");
        productMenu.setMnemonic(KeyEvent.VK_P);
        JMenuItem showProductsItem = new JMenuItem("Show Products / Services");
        showProductsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                tabs.setSelectedComponent(productsPanel);
            }
        });
        JMenuItem productAssignmentItem = new JMenuItem("Assign Product to Customer...");
        productAssignmentItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                tabs.setSelectedComponent(productsPanel);
                productsPanel.openAssignProductDialog();
            }
        });
        productMenu.add(showProductsItem);
        productMenu.add(productAssignmentItem);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        JMenuItem aboutItem = new JMenuItem("About FiberNet HelpDesk");
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JOptionPane.showMessageDialog(HelpDeskFrame.this,
                        "FiberNet HelpDesk\nJava Swing OOP university project",
                        "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(customerMenu);
        menuBar.add(ticketMenu);
        menuBar.add(productMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    public void refreshAll() {
        dashboardPanel.refreshData();
        customersPanel.refreshData();
        ticketsPanel.refreshData();
        productsPanel.refreshData();
        supportAgentsPanel.refreshData();
    }

    HelpDesk getHelpDesk() {
        return helpDesk;
    }

    JTabbedPane getTabs() {
        return tabs;
    }

    CustomersPanel getCustomersPanel() {
        return customersPanel;
    }

    TicketsPanel getTicketsPanel() {
        return ticketsPanel;
    }
}
