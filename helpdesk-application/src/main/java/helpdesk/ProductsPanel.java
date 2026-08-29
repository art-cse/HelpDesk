package helpdesk;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class ProductsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final HelpDeskFrame owner;
    private final HelpDesk helpDesk;
    private final Runnable refreshAction;
    private final ReadOnlyTableModel tableModel;

    public ProductsPanel(HelpDeskFrame owner, HelpDesk helpDesk, Runnable refreshAction) {
        this.owner = owner;
        this.helpDesk = helpDesk;
        this.refreshAction = refreshAction;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tableModel = new ReadOnlyTableModel(new String[] {
            "ID", "Name", "Type", "Monthly Price", "Description", "Eligible Customer Types"
        });
        JTable productTable = GuiUtil.createTable(tableModel);
        productTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(260);
        productTable.getColumnModel().getColumn(5).setPreferredWidth(210);
        add(new JScrollPane(productTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        JButton assignButton = new JButton("Assign to Customer");
        assignButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                openAssignProductDialog();
            }
        });
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                refreshData();
            }
        });
        buttonPanel.add(assignButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        for (Product product : helpDesk.getProducts()) {
            tableModel.addRow(new Object[] {
                product.getId(), product.getName(), product.getType(),
                String.format("EUR %.2f", product.getMonthlyPrice()), product.getDescription(),
                GuiUtil.formatCategories(product.getEligibleCategories())
            });
        }
    }

    void openAssignProductDialog() {
        AssignProductDialog dialog = new AssignProductDialog(owner, helpDesk, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshAction.run();
        }
    }

    int getVisibleRowCount() {
        return tableModel.getRowCount();
    }
}
