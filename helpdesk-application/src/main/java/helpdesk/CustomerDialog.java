package helpdesk;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CustomerDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final HelpDesk helpDesk;
    private final Customer existingCustomer;
    private final JTextField idField;
    private final JTextField nameField;
    private final JTextField emailField;
    private final JTextField phoneField;
    private final JTextField addressField;
    private final JComboBox<CustomerCategory> categoryBox;
    private final JLabel categoryLabel1;
    private final JLabel categoryLabel2;
    private final JTextField categoryField1;
    private final JTextField categoryField2;
    private boolean saved;

    public CustomerDialog(HelpDeskFrame owner, HelpDesk helpDesk, Customer existingCustomer) {
        super(owner, existingCustomer == null ? "Register Customer" : "Edit Customer", true);
        this.helpDesk = helpDesk;
        this.existingCustomer = existingCustomer;
        saved = false;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 12, 4, 12));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        idField = new JTextField(24);
        nameField = new JTextField(24);
        emailField = new JTextField(24);
        phoneField = new JTextField(24);
        addressField = new JTextField(24);
        categoryBox = new JComboBox<CustomerCategory>(CustomerCategory.values());
        categoryField1 = new JTextField(24);
        categoryField2 = new JTextField(24);
        categoryLabel1 = new JLabel();
        categoryLabel2 = new JLabel();

        int row = 0;
        addField(form, constraints, row++, "Customer ID:", idField);
        addField(form, constraints, row++, "Name:", nameField);
        addField(form, constraints, row++, "Email:", emailField);
        addField(form, constraints, row++, "Phone:", phoneField);
        addField(form, constraints, row++, "Address:", addressField);
        addField(form, constraints, row++, "Customer Type:", categoryBox);
        addField(form, constraints, row++, categoryLabel1, categoryField1);
        addField(form, constraints, row, categoryLabel2, categoryField2);
        add(form, BorderLayout.CENTER);

        categoryBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updateCategoryFields();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        JButton saveButton = new JButton("Save");
        getRootPane().setDefaultButton(saveButton);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                saveCustomer();
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

        if (existingCustomer != null) {
            loadCustomer(existingCustomer);
        } else {
            updateCategoryFields();
        }

        pack();
        setMinimumSize(new Dimension(480, getHeight()));
        setLocationRelativeTo(owner);
    }

    private void addField(JPanel panel, GridBagConstraints constraints, int row,
            String labelText, java.awt.Component component) {
        addField(panel, constraints, row, new JLabel(labelText), component);
    }

    private void addField(JPanel panel, GridBagConstraints constraints, int row,
            JLabel label, java.awt.Component component) {
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.weightx = 0;
        panel.add(label, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(component, constraints);
    }

    private void loadCustomer(Customer customer) {
        idField.setText(customer.getId());
        idField.setEnabled(false);
        nameField.setText(customer.getName());
        emailField.setText(customer.getEmail());
        phoneField.setText(customer.getPhone());
        addressField.setText(customer.getAddress());
        categoryBox.setSelectedItem(customer.getCategory());
        categoryBox.setEnabled(false);
        updateCategoryFields();
        categoryField1.setText(customer.getCategorySpecificInformation());
        categoryField1.setEnabled(false);
        categoryField2.setText("Category and identifying data cannot be changed here.");
        categoryField2.setEnabled(false);
    }

    private void updateCategoryFields() {
        CustomerCategory category = (CustomerCategory) categoryBox.getSelectedItem();
        if (category == CustomerCategory.BUSINESS) {
            categoryLabel1.setText("Registration Number:");
            categoryLabel2.setText("Contact Person:");
            categoryField2.setEnabled(existingCustomer == null);
        } else if (category == CustomerCategory.OFFICIAL) {
            categoryLabel1.setText("Institution Code:");
            categoryLabel2.setText("Department:");
            categoryField2.setEnabled(existingCustomer == null);
        } else {
            categoryLabel1.setText("Personal Number:");
            categoryLabel2.setText("Additional Information:");
            categoryField2.setText("");
            categoryField2.setEnabled(false);
        }
    }

    private void saveCustomer() {
        try {
            if (existingCustomer == null) {
                Customer customer = createCustomer();
                helpDesk.registerCustomer(customer);
            } else {
                existingCustomer.updateDetails(nameField.getText(), emailField.getText(),
                        phoneField.getText(), addressField.getText());
            }
            saved = true;
            dispose();
        } catch (HelpDeskException | IllegalArgumentException exception) {
            GuiUtil.showError(this, exception);
        }
    }

    private Customer createCustomer() {
        CustomerCategory category = (CustomerCategory) categoryBox.getSelectedItem();
        if (category == CustomerCategory.BUSINESS) {
            return new BusinessCustomer(idField.getText(), nameField.getText(), emailField.getText(),
                    phoneField.getText(), addressField.getText(), categoryField1.getText(),
                    categoryField2.getText());
        }
        if (category == CustomerCategory.OFFICIAL) {
            return new OfficialCustomer(idField.getText(), nameField.getText(), emailField.getText(),
                    phoneField.getText(), addressField.getText(), categoryField1.getText(),
                    categoryField2.getText());
        }
        return new ResidentialCustomer(idField.getText(), nameField.getText(), emailField.getText(),
                phoneField.getText(), addressField.getText(), categoryField1.getText());
    }

    public boolean isSaved() {
        return saved;
    }
}
