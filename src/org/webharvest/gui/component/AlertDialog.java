package org.webharvest.gui.component;

import org.webharvest.gui.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog which displays alert message
 */
public class AlertDialog extends CommonDialog {

    private Component caller;
    private int result = JOptionPane.CANCEL_OPTION;

    public AlertDialog(String title, String message, Icon icon) {
        this(null, title, message, icon, new int[] {JOptionPane.OK_OPTION}, new String[] {"OK"});
    }

    public AlertDialog(Component caller, String title, String message, Icon icon, int options[], String buttLabels[]) {
        super(title);
        this.caller = caller;

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        JLabel label = new JLabel(message);
        label.setBorder(new EmptyBorder(10, 20, 10, 20));
        label.setIcon(icon);

        contentPane.add(label, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 4));

        if (options == null || options.length == 0 || buttLabels == null || buttLabels.length != options.length) {
            options = new int[] {JOptionPane.OK_OPTION};
            buttLabels = new String[] {"OK"};
        }
        for (int i = 0; i < options.length; i++) {
            final int option = options[i];
            FixedSizeButton butt = new FixedSizeButton(buttLabels[i], 60, 20);
            butt.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    result = option;
                    setVisible(false);
                }
            });
            buttonPanel.add(butt);
        }

        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        pack();
    }

    protected void onOk() {

        setVisible(false);
    }

    public void setVisible(boolean b) {
        if (b) {
            Component relativeTo = caller;
            if (relativeTo == null) {
                relativeTo = GuiUtils.getActiveFrame();
            }
            if (relativeTo != null) {
                GuiUtils.centerRelativeTo(this, relativeTo);
            }
        }
        super.setVisible(b);
    }

    public int display() {
        setVisible(true);
        return result;
    }

}