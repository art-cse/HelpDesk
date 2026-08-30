package helpdesk;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class TicketDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final HelpDesk helpDesk;
    private final ArrayList<Customer> customers;
    private final ArrayList<Product> products;
    private final JComboBox<String> customerBox;
    private final JComboBox<String> productBox;
    private final JComboBox<TicketType> typeBox;
    private final JTextField subjectField;
    private final JTextArea descriptionArea;
    private final JLabel priorityValue;
    private boolean saved;

    public TicketDialog(HelpDeskFrame owner, HelpDesk helpDesk) {
        this((Frame) owner, helpDesk, null);
    }

    public TicketDialog(Frame owner, HelpDesk helpDesk, Customer fixedCustomer) {
        super(owner, fixedCustomer == null ? "Register New Ticket" : "Submit Support Ticket",
                true);
        this.helpDesk = helpDesk;
        customers = new ArrayList<Customer>();
        if (fixedCustomer == null) {
            customers.addAll(helpDesk.getCustomers());
        } else {
            customers.add(fixedCustomer);
        }
        products = new ArrayList<Product>();
        saved = false;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(8, 8));

        customerBox = new JComboBox<String>();
        for (Customer customer : customers) {
            customerBox.addItem(customer.getId() + " - " + customer.getName());
        }
        if (fixedCustomer != null) {
            customerBox.setEnabled(false);
        }
        productBox = new JComboBox<String>();
        productBox.setPrototypeDisplayValue("P-000 - Institution Secure Network");
        typeBox = new JComboBox<TicketType>(TicketType.values());
        subjectField = new JTextField(25);
        descriptionArea = new JTextArea(5, 25);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        priorityValue = new JLabel();

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 12, 4, 12));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;

        int row = 0;
        addField(form, constraints, row++, "Ticket ID:", new JLabel("Assigned automatically"));
        addField(form, constraints, row++, "Customer:", customerBox);
        addField(form, constraints, row++, "Product / Service:", productBox);
        addField(form, constraints, row++, "Ticket Type:", typeBox);
        addField(form, constraints, row++, "Subject:", subjectField);
        addField(form, constraints, row++, "Calculated Priority:", priorityValue);
        addField(form, constraints, row++, "Initial Status:", new JLabel("Open"));
        addField(form, constraints, row++, "Assigned Agent:", new JLabel("Unassigned"));

        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.weightx = 0;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Description:"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setPreferredSize(new Dimension(340, 100));
        form.add(descriptionScroll, constraints);
        add(form, BorderLayout.CENTER);

        customerBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                refreshCustomerProducts();
            }
        });
        refreshCustomerProducts();

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        JButton saveButton = new JButton(fixedCustomer == null ? "Create Ticket" : "Submit Ticket");
        getRootPane().setDefaultButton(saveButton);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                saveTicket();
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(540, getHeight()));
        setLocationRelativeTo(owner);
    }

    private void addField(JPanel panel, GridBagConstraints constraints, int row,
            String label, java.awt.Component component) {
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.weightx = 0;
        panel.add(new JLabel(label), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(component, constraints);
    }

    private void refreshCustomerProducts() {
        products.clear();
        productBox.removeAllItems();
        if (customerBox.getSelectedIndex() < 0) {
            priorityValue.setText("Not available");
            return;
        }
        Customer customer = customers.get(customerBox.getSelectedIndex());
        for (Product product : customer.getProducts()) {
            products.add(product);
            productBox.addItem(product.getId() + " - " + product.getName());
        }
        priorityValue.setText(customer.getTreatmentPriority().toString()
                + " (from customer category)");
    }

    private void saveTicket() {
        if (customerBox.getSelectedIndex() < 0) {
            GuiUtil.showSelectionRequired(this, "customer");
            return;
        }
        if (productBox.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this,
                    "The selected customer has no assigned product. Assign a product first.",
                    "Product required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Customer customer = customers.get(customerBox.getSelectedIndex());
            Product product = products.get(productBox.getSelectedIndex());
            helpDesk.createTicket(customer.getId(), product.getId(),
                    (TicketType) typeBox.getSelectedItem(), subjectField.getText(),
                    descriptionArea.getText());
            saved = true;
            dispose();
        } catch (HelpDeskException | IllegalArgumentException exception) {
            GuiUtil.showError(this, exception);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
