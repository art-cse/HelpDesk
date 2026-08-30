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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TicketStatusDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final HelpDesk helpDesk;
    private final Ticket ticket;
    private final JComboBox<TicketStatus> statusBox;
    private final JTextArea noteArea;
    private boolean saved;

    public TicketStatusDialog(Frame owner, HelpDesk helpDesk, Ticket ticket) {
        super(owner, "Update Ticket Status - " + ticket.getId(), true);
        this.helpDesk = helpDesk;
        this.ticket = ticket;
        saved = false;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 12, 4, 12));
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
        statusBox = new JComboBox<TicketStatus>(TicketStatus.values());
        statusBox.setSelectedItem(getSuggestedNextStatus(ticket.getStatus()));
        form.add(statusBox, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        form.add(new JLabel("Progress note:"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        noteArea = new JTextArea(4, 30);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        form.add(new JScrollPane(noteArea), constraints);
        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        JButton updateButton = new JButton("Update");
        getRootPane().setDefaultButton(updateButton);
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updateStatus();
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
        buttons.add(updateButton);
        buttons.add(cancelButton);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(500, getHeight()));
        setLocationRelativeTo(owner);
    }

    private TicketStatus getSuggestedNextStatus(TicketStatus current) {
        switch (current) {
            case OPEN:
                return TicketStatus.IN_PROGRESS;
            case IN_PROGRESS:
            case WAITING_FOR_CUSTOMER:
                return TicketStatus.RESOLVED;
            case RESOLVED:
                return TicketStatus.CLOSED;
            case CLOSED:
            default:
                return current;
        }
    }

    private void updateStatus() {
        try {
            helpDesk.updateTicketStatus(ticket.getId(),
                    (TicketStatus) statusBox.getSelectedItem(), noteArea.getText());
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
