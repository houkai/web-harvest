package org.webharvest.gui.component;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * @author: Vladimir Nikic
 * Date: Sep 18, 2007
 */
public class GCPanel extends JPanel {

    private PercentLabel percentLabel;

    private class PercentLabel extends JLabel {
        public PercentLabel() {
            this.setBackground(Color.white);
            this.setText(getUsageString());
            this.setBorder(new LineBorder(Color.gray));
            startThread();
        }

        private void startThread() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    while (true) {
                        repaint();
                        try {
                            wait(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        public int getHeight() {
            return 20;
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
        add(this.percentLabel);
    }

    private long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    private long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    private double getPercentOfUsedMemory() {
        long total = getTotalMemory();
        long used = total - getFreeMemory();
        return total > 0 ? (double)used/total : 1d;
    }

    private String getUsageString() {
        long total = getTotalMemory();
        long used = total - getFreeMemory();

        long usedInMegabytes = used / (1024*1024);
        long totalInMegabytes = total / (1024*1024);

        return usedInMegabytes + "M of " + totalInMegabytes + "M"; 
    }

}
