package org.webharvest.gui.component;

import org.webharvest.gui.ResourceManager;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author: Vladimir Nikic
 * Date: Sep 18, 2007
 */
public class GCPanel extends JPanel {

    private static final Dimension PERCENT_LABEL_DIMENSION = new Dimension(80, 20);
    private static final Dimension GC_BUTTON_DIMENSION = new Dimension(20, 20);
    
    private PercentLabel percentLabel;

    private class MemoryCheckThread extends Thread {
        public void run() {
            while (true) {
                refresh();
            }
        }

        private synchronized void refresh() {
            percentLabel.setText( getUsageString() );
            percentLabel.repaint();
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class PercentLabel extends JLabel {
        public PercentLabel() {
            this.setBackground(Color.white);
            this.setText(getUsageString());
            this.setBorder(new LineBorder(Color.gray));
            new MemoryCheckThread().start();
        }


        public Dimension getPreferredSize() {
            return PERCENT_LABEL_DIMENSION;
        }

        public void paint(Graphics g) {
            Color color = g.getColor();
            g.setColor(new Color(160, 176, 228));
            int width = (int) (getWidth() * getPercentOfUsedMemory());
            g.fillRect(0, 0, width, getHeight());
            g.setColor(color);
            super.paint(g);
        }
    }

    public GCPanel(LayoutManager layout) {
        super(layout);
        this.percentLabel = new PercentLabel();
        this.percentLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(this.percentLabel);
        JButton gcButton = new JButton(ResourceManager.SMALL_TRASHCAN_ICON) {
            public Dimension getPreferredSize() {
                return GC_BUTTON_DIMENSION;
            }
        };
        gcButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.gc();
            }
        });
        gcButton.setFocusable(false);
        add(gcButton);
    }

    private long getFreeMemory() {
        return Runtime.getRuntime().freeMemory() / (1024*1024);
    }

    private long getTotalMemory() {
        return Runtime.getRuntime().totalMemory() / (1024*1024);
    }

    private double getPercentOfUsedMemory() {
        long total = getTotalMemory();
        long used = total - getFreeMemory();
        return total > 0 ? ((double)used)/total : 1d;
    }

    private String getUsageString() {
        long total = getTotalMemory();
        long used = total - getFreeMemory();

        return used + "M of " + total + "M"; 
    }

}
