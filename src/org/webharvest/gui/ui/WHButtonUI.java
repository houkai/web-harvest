package org.webharvest.gui.ui;

import org.webharvest.gui.component.*;

import javax.swing.plaf.basic.*;
import javax.swing.plaf.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class WHButtonUI extends BasicButtonUI implements MouseListener {

    protected static final Color DISABLED_COLOR = new Color(125, 121, 111);
    protected static final Color DEFAULT_BG_FROM = new Color(238, 237, 234);
    protected static final Color DEFAULT_BG_TO = new Color(192, 189, 182);

    private CommonBorder buttonBorder = new CommonBorder();
    private Component comp = null;

    private boolean showDownArrow = false;
    private Color bgFrom = DEFAULT_BG_FROM;
    private Color bgTo = DEFAULT_BG_TO;
    private boolean isDefaultColor = true;

    public static ComponentUI createUI(JComponent c) {
        return new WHButtonUI();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        if (comp.isEnabled()) {
            buttonBorder.setMouseOverStatus();
            comp.repaint();
        }
    }

    public void mouseExited(MouseEvent e) {
        if (comp.isEnabled()) {
            buttonBorder.setNormalStatus();
            comp.repaint();
        }
    }

    public void installUI(JComponent c) {
        this.comp = c;
        super.installUI(c);
        c.addMouseListener(this);
        c.setBorder(buttonBorder);
        c.setOpaque(false);
    }

    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        c.removeMouseListener(this);
    }

    public void paint(Graphics g, JComponent c) {
        int w = c.getWidth();
        int h = c.getHeight();
        Graphics2D g2 = (Graphics2D) g;
        boolean isEnabled = comp.isEnabled();
        ButtonModel model = ((JButton) c).getModel();
        boolean isPressed = model.isArmed() && model.isPressed();
        Color gradFrom = isEnabled ? (isPressed ? bgTo : bgFrom) : DEFAULT_BG_FROM;
        Color gradTo = isEnabled ? (isPressed ? bgFrom : bgTo) : DEFAULT_BG_TO;
        GradientPaint gp = new GradientPaint(0, 0, gradFrom, 0, h - 1, gradTo, false);
        g2.setPaint(gp);
        g2.fillRect(1, 1, w - 2, h - 2);
        comp.setForeground(isDefaultColor ? Color.black : Color.white);
        super.paint(g, c);
        if (comp.hasFocus()) {
            g2.setColor(Color.black);
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{1}, 1));
            g2.drawRect(2, 2, w - 5, h - 5);
        }
    }

    protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        FontMetrics fm = b.getFontMetrics(b.getFont());
        int mnemonicIndex = b.getDisplayedMnemonicIndex();
        int textShiftOffset = getTextShiftOffset();

        if (showDownArrow) {
            boolean textAdapted = false;
            do {
                int textWidth = SwingUtilities.computeStringWidth(fm, text);
                if (textWidth >= comp.getWidth() - 7 - textRect.x - textShiftOffset) {
                    if (text != null && text.endsWith("...")) {
                        text = text.substring(0, text.length() - 3);
                    }
                    if (text.length() > 0) {
                        text = text.substring(0, text.length() - 1);
                    } else {
                        text = "...";
                        textAdapted = true;
                    }
                    text += "...";
                } else {
                    textAdapted = true;
                }
            } while (!textAdapted);
        }

        // Draw the Text
        g.setColor(model.isEnabled() ? b.getForeground() : DISABLED_COLOR);
        g.drawString(text, textRect.x + textShiftOffset, textRect.y + fm.getAscent() + textShiftOffset);

        int w = c.getWidth();
        int h = c.getHeight();
        if (showDownArrow) {
            g.setColor(comp.isEnabled() ? Color.black : DISABLED_COLOR);
            int ha = h / 2;
            g.drawLine(w - 8, ha - 1, w - 4, ha - 1);
            g.drawLine(w - 7, ha, w - 5, ha);
            g.drawLine(w - 6, ha + 1, w - 6, ha + 1);
        }

    }

    public boolean isShowDownArrow() {
        return showDownArrow;
    }

    public void setShowDownArrow(boolean showDownArrow) {
        this.showDownArrow = showDownArrow;
    }

    public boolean isDefaultColor() {
        return isDefaultColor;
    }

    public void setDefaultColor(boolean defaultColor) {
        isDefaultColor = defaultColor;
    }

    public void setColors(Color c1, Color c2) {
        bgFrom = c1;
        bgTo = c2;
        isDefaultColor = false;
        comp.repaint();
    }

    public void resetColors() {
        bgFrom = DEFAULT_BG_FROM;
        bgTo = DEFAULT_BG_TO;
        isDefaultColor = true;
        comp.repaint();
    }

}