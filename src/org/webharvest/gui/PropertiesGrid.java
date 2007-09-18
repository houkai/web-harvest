/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

    Redistribution and use of this software in source and binary forms,
    with or without modification, are permitted provided that the following
    conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.

    * The name of Web-Harvest may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.gui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author: Vladimir Nikic
 * Date: May 7, 2007
 */
public class PropertiesGrid extends JTable {
    private static final Color PROPERTY_NAME_FOREGROUND = new Color(180, 180, 180);

    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setRolloverEnabled(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setIcon(ResourceManager.SMALL_VIEW_ICON);
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private boolean isPushed;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            button.setText("...");
            isPushed = true;

            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                PropertiesGridModel model = getPropertiesGridModel();
                String propertyName = (String) model.getValueAt(this.row, 0); 
                Object value = model.getValueAt(this.row, 1);
                final ViewerFrame viewerFrame = new ViewerFrame( propertyName, value, model.getNodeInfo(), 0 );
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        viewerFrame.setVisible(true);
                        viewerFrame.toFront();
                    }
                });
            }
            isPushed = false;
            return null;
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    public PropertiesGrid() {
        super( new PropertiesGridModel() );

        this.getTableHeader().setReorderingAllowed(false);

        TableColumnModel columnModel = this.getColumnModel();
        columnModel.getColumn(2).setCellRenderer( new ButtonRenderer() );
        columnModel.getColumn(2).setCellEditor( new ButtonEditor(new JCheckBox()) );
        columnModel.getColumn(2).setMaxWidth(14);
    }

    public Component prepareRenderer(TableCellRenderer cellRenderer, int i, int j) {
        Component component = super.prepareRenderer(cellRenderer, i, j);
        component.setForeground( j == 0 ? PROPERTY_NAME_FOREGROUND : getForeground() );

        return component;
    }

    public PropertiesGridModel getPropertiesGridModel() {
        TableModel tableModel = getModel();
        return tableModel instanceof PropertiesGridModel ? (PropertiesGridModel) tableModel : null;
    }

}