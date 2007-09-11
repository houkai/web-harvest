package org.webharvest.gui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Iterator;

public class RunParamsDialog extends JDialog {

    private class MyTableModel extends AbstractTableModel {
        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return params.size();
        }

        public Object getValueAt(int row, int col) {
            String[] pair = (String[]) params.get(row);
            return pair[col];
        }

        public String getColumnName(int column) {
            return column == 0 ? "Name" : "Value";
        }

        public void removeRow(int rowIndex) {
            int size = params.size();
            if (rowIndex >= 0 && rowIndex < size) {
                params.remove(rowIndex);
                this.fireTableDataChanged();
            }
        }

        public void addEmptyRow() {
            params.add( new String[] {"", ""} );
            this.fireTableDataChanged();
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            String rowArray[] = (String[]) params.get(rowIndex);
            rowArray[columnIndex] = (String) value;
        }
    }

    // list of name-value pairs used as model for the table
    private java.util.List params = new ArrayList();

    // Ide instance where this dialog belongs.
    private Ide ide;

    public RunParamsDialog(Ide ide) throws HeadlessException {
        super(ide, "Initial Configuration Parameters", true);
        this.ide = ide;
        this.setResizable(false);

        createGUI();
    }

    private void createGUI() {
        Container contentPane = this.getContentPane();
        contentPane.setLayout( new BorderLayout() );

        final MyTableModel dataModel = new MyTableModel();

        final JTable table = new JTable(dataModel) {
            public void editingStopped(ChangeEvent event) {
                TableCellEditor editor = (TableCellEditor) event.getSource();
                int row = getSelectedRow();
                int column = getSelectedColumn();
                if (row < 0) {
                    row = 0;
                }
                if (column < 0) {
                    column = 0;
                }
                String value = (String) editor.getCellEditorValue();
                dataModel.setValueAt(value, row, column);
                super.editingStopped(event);
            }
        };
        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 10, 5));
        buttonPanel.setBorder(new EmptyBorder(4, 2, 4, 4));

        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dataModel.addEmptyRow();
                int lastRow = dataModel.getRowCount() - 1;
                table.grabFocus();
                table.getSelectionModel().setSelectionInterval(lastRow, lastRow);
                table.editCellAt(lastRow, 0);
            }
        });

        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dataModel.removeRow(table.getSelectedRow());
            }
        });

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                defineParams();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(new JLabel(" "));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        JPanel leftPane = new JPanel(new BorderLayout());
        leftPane.setBorder(new EmptyBorder(4, 4, 4, 4));
        JScrollPane tableScrollPane = new JScrollPane(table);
        leftPane.add(tableScrollPane, BorderLayout.CENTER);
        contentPane.add(leftPane, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.EAST);

        this.pack();
    }

    protected JRootPane createRootPane() {
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JRootPane rootPane = new JRootPane();
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            }
        };
        rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        return rootPane;
    }

    private void defineParams() {
        ConfigPanel configPanel = ide.getActiveConfigPanel();
        if (configPanel != null) {
            Map paramMap = new LinkedHashMap();
            Iterator iterator = params.iterator();
            while (iterator.hasNext()) {
                String[] pair = (String[]) iterator.next();
                paramMap.put(pair[0], pair[1]);
            }

            configPanel.setInitParams(paramMap);
        }

        setVisible(false);
    }

    public Dimension getPreferredSize() {
        return new Dimension(400, 200);
    }

    public void setVisible(boolean b) {
        if (b) {
            ConfigPanel configPanel = ide.getActiveConfigPanel();
            if (configPanel != null) {
                params.clear();
                Map paramsMap = configPanel.getInitParams();
                if (paramsMap != null) {
                    Iterator iterator = paramsMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry entry = (Map.Entry) iterator.next();
                        params.add(new String[] {(String) entry.getKey(), (String) entry.getValue()});
                    }
                }
            }
        }
        
        super.setVisible(b);
    }
    
}