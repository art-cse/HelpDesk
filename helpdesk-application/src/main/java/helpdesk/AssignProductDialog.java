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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class AssignProductDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final HelpDesk helpDesk;
    private final ArrayList<Customer> customers;
    private final ArrayList<Product> products;
    private final JComboBox<String> customerBox;
    private final JComboBox<String> productBox;
    private boolean saved;

    public AssignProductDialog(HelpDeskFrame owner, HelpDesk helpDesk, Customer selectedCustomer) {
        super(owner, "Assign Product / Service", true);
        this.helpDesk = helpDesk;
        customers = helpDesk.getCustomers();
        products = new ArrayList<Product>();
        saved = false;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 4, 5, 4);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        customerBox = new JComboBox<String>();
        for (Customer customer : customers) {
            customerBox.addItem(customer.getId() + " - " + customer.getName());
        }
        productBox = new JComboBox<String>();
        productBox.setPrototypeDisplayValue("P-000 - Institution Secure Network");

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        form.add(new JLabel("Customer:"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        form.add(customerBox, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0;
        form.add(new JLabel("Product / Service:"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        form.add(productBox, constraints);
        add(form, BorderLayout.CENTER);

        customerBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                refreshProducts();
            }
        });

        if (selectedCustomer != null) {
            for (int i = 0; i < customers.size(); i++) {
                if (customers.get(i) == selectedCustomer) {
                    customerBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        refreshProducts();

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        JButton assignButton = new JButton("Assign");
        getRootPane().setDefaultButton(assignButton);
        assignButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                assignProduct();
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
        buttonPanel.add(assignButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(500, getHeight()));
        setLocationRelativeTo(owner);
    }

    private void refreshProducts() {
        products.clear();
        productBox.removeAllItems();
        if (customerBox.getSelectedIndex() < 0) {
            return;
        }
        Customer customer = customers.get(customerBox.getSelectedIndex());
        for (Product product : helpDesk.getProductsForCategory(customer.getCategory())) {
            if (!customer.hasProduct(product.getId())) {
                products.add(product);
                productBox.addItem(product.getId() + " - " + product.getName());
            }
        }
    }

    private void assignProduct() {
        if (customerBox.getSelectedIndex() < 0) {
            GuiUtil.showSelectionRequired(this, "customer");
            return;
        }
        if (productBox.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this,
                    "This customer already has every available product for the category.",
                    "No product available", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            Customer customer = customers.get(customerBox.getSelectedIndex());
            Product product = products.get(productBox.getSelectedIndex());
            helpDesk.assignProductToCustomer(customer.getId(), product.getId());
            saved = true;
            dispose();
        } catch (HelpDeskException exception) {
            GuiUtil.showError(this, exception);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
