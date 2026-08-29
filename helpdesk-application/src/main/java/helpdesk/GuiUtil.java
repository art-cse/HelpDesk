package helpdesk;

import java.awt.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

final class GuiUtil {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private GuiUtil() {
    }

    static JTable createTable(ReadOnlyTableModel model) {
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        return table;
    }

    static String getSelectedId(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        return table.getModel().getValueAt(modelRow, 0).toString();
    }

    static String formatProducts(ArrayList<Product> products) {
        String result = "";
        for (int i = 0; i < products.size(); i++) {
            if (i > 0) {
                result += ", ";
            }
            result += products.get(i).getName();
        }
        return result.isEmpty() ? "None" : result;
    }

    static String formatCategories(ArrayList<CustomerCategory> categories) {
        String result = "";
        for (int i = 0; i < categories.size(); i++) {
            if (i > 0) {
                result += ", ";
            }
            result += categories.get(i).toString();
        }
        return result;
    }

    static String formatDate(LocalDateTime date) {
        return date.format(DATE_FORMATTER);
    }

    static JTextArea createTextArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        area.setCaretPosition(0);
        return area;
    }

    static void showError(Component parent, Exception exception) {
        JOptionPane.showMessageDialog(parent, exception.getMessage(),
                "Operation could not be completed", JOptionPane.ERROR_MESSAGE);
    }

    static void showSelectionRequired(Component parent, String itemName) {
        JOptionPane.showMessageDialog(parent, "Select a " + itemName + " first.",
                "Selection required", JOptionPane.INFORMATION_MESSAGE);
    }
}
