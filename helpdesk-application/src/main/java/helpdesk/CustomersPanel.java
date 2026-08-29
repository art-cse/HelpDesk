package helpdesk;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

public class CustomersPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final HelpDeskFrame owner;
    private final HelpDesk helpDesk;
    private final Runnable refreshAction;
    private final ReadOnlyTableModel tableModel;
    private final JTable customerTable;
    private final JTextField searchField;
    private final JComboBox<String> categoryBox;
    private final JComboBox<String> productBox;
    private final ArrayList<Product> filterProducts;

    public CustomersPanel(HelpDeskFrame owner, HelpDesk helpDesk, Runnable refreshAction) {
        this.owner = owner;
        this.helpDesk = helpDesk;
        this.refreshAction = refreshAction;
        filterProducts = new ArrayList<Product>();

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        filterPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        filterPanel.add(searchField);
        filterPanel.add(new JLabel("Category:"));
        categoryBox = new JComboBox<String>();
        categoryBox.addItem("All");
        for (CustomerCategory category : CustomerCategory.values()) {
            categoryBox.addItem(category.toString());
        }
        filterPanel.add(categoryBox);
        filterPanel.add(new JLabel("Product:"));
        productBox = new JComboBox<String>();
        productBox.setPrototypeDisplayValue("P-000 - Managed Router Support");
        filterPanel.add(productBox);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                applyFilters();
            }
        });
        filterPanel.add(searchButton);
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                searchField.setText("");
                categoryBox.setSelectedIndex(0);
                productBox.setSelectedIndex(0);
                applyFilters();
            }
        });
        filterPanel.add(clearButton);
        add(filterPanel, BorderLayout.NORTH);

        tableModel = new ReadOnlyTableModel(new String[] {
            "ID", "Name", "Customer Type", "Email", "Phone", "Products", "Priority"
        });
        customerTable = GuiUtil.createTable(tableModel);
        customerTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        customerTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        customerTable.getColumnModel().getColumn(5).setPreferredWidth(240);
        add(new JScrollPane(customerTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        JButton addButton = new JButton("Add Customer");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                openAddCustomerDialog();
            }
        });
        JButton editButton = new JButton("Edit Customer");
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                openEditCustomerDialog();
            }
        });
        JButton assignButton = new JButton("Assign Product");
        assignButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                openAssignProductDialog();
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
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(assignButton);
        buttonPanel.add(historyButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshData() {
        int selectedProductIndex = productBox.getSelectedIndex();
        filterProducts.clear();
        productBox.removeAllItems();
        productBox.addItem("All");
        for (Product product : helpDesk.getProducts()) {
            filterProducts.add(product);
            productBox.addItem(product.getId() + " - " + product.getName());
        }
        if (selectedProductIndex >= 0 && selectedProductIndex < productBox.getItemCount()) {
            productBox.setSelectedIndex(selectedProductIndex);
        }
        applyFilters();
    }

    void applyFilters() {
        tableModel.setRowCount(0);
        String searchText = searchField.getText().trim();
        CustomerCategory category = categoryBox.getSelectedIndex() == 0
                ? null : CustomerCategory.values()[categoryBox.getSelectedIndex() - 1];
        Product product = productBox.getSelectedIndex() <= 0
                ? null : filterProducts.get(productBox.getSelectedIndex() - 1);

        for (Customer customer : helpDesk.getCustomers()) {
            boolean matchesSearch = searchText.isEmpty() || customer.matchesKeyword(searchText);
            boolean matchesCategory = category == null || customer.getCategory() == category;
            boolean matchesProduct = product == null || customer.hasProduct(product.getId());
            if (matchesSearch && matchesCategory && matchesProduct) {
                tableModel.addRow(new Object[] {
                    customer.getId(), customer.getName(), customer.getCategory(),
                    customer.getEmail(), customer.getPhone(),
                    GuiUtil.formatProducts(customer.getProducts()), customer.getTreatmentPriority()
                });
            }
        }
    }

    void openAddCustomerDialog() {
        CustomerDialog dialog = new CustomerDialog(owner, helpDesk, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshAction.run();
        }
    }

    void openEditCustomerDialog() {
        String customerId = GuiUtil.getSelectedId(customerTable);
        if (customerId == null) {
            GuiUtil.showSelectionRequired(this, "customer");
            return;
        }
        try {
            Customer customer = helpDesk.getCustomer(customerId);
            CustomerDialog dialog = new CustomerDialog(owner, helpDesk, customer);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshAction.run();
            }
        } catch (HelpDeskException exception) {
            GuiUtil.showError(this, exception);
        }
    }

    void openAssignProductDialog() {
        Customer selectedCustomer = null;
        String customerId = GuiUtil.getSelectedId(customerTable);
        if (customerId != null) {
            try {
                selectedCustomer = helpDesk.getCustomer(customerId);
            } catch (HelpDeskException exception) {
                GuiUtil.showError(this, exception);
                return;
            }
        }
        AssignProductDialog dialog = new AssignProductDialog(owner, helpDesk, selectedCustomer);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshAction.run();
        }
    }

    private void showSelectedHistory() {
        String customerId = GuiUtil.getSelectedId(customerTable);
        if (customerId == null) {
            GuiUtil.showSelectionRequired(this, "customer");
            return;
        }
        try {
            Customer customer = helpDesk.getCustomer(customerId);
            StringBuilder text = new StringBuilder();
            text.append("Customer: ").append(customer.getName()).append("\n");
            text.append("Category: ").append(customer.getCategory()).append("\n");
            text.append("Products: ").append(GuiUtil.formatProducts(customer.getProducts()))
                    .append("\n\n");
            if (customer.getTicketHistory().isEmpty()) {
                text.append("No tickets have been registered for this customer.");
            } else {
                for (Ticket ticket : customer.getTicketHistory()) {
                    text.append(ticket.getId()).append(" - ").append(ticket.getTitle())
                            .append(" [").append(ticket.getStatus()).append("]\n");
                    for (StatusChange change : ticket.getStatusHistory()) {
                        text.append("    ").append(change).append("\n");
                    }
                    text.append("\n");
                }
            }
            TextDialog.showText(owner, "Customer History", text.toString());
        } catch (HelpDeskException exception) {
            GuiUtil.showError(this, exception);
        }
    }

    int getVisibleRowCount() {
        return tableModel.getRowCount();
    }

    JTable getCustomerTable() {
        return customerTable;
    }

    void setSearchText(String text) {
        searchField.setText(text);
    }
}
