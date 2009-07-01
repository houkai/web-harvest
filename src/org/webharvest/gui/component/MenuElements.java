package org.webharvest.gui.component;

import org.webharvest.gui.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.*;
import java.awt.*;

/**
 * Elements used for menu creation.
 */
public class MenuElements {

    public static final Color MENU_SEP_SHADOW = new Color(160, 158, 153);
    public static final Color MENU_SEP_HIGHLIGHT = new Color(235, 234, 230);
    public static final Color MENUBAR_BACKGROUND = new Color(215, 211, 203);
    public static final Color MENUBAR_FOREGROUND = new Color(0, 0, 0);
    public static final Color MENUBAR_SELECTION_BACKGROUND = new Color(0, 57, 114);
    public static final Color MENUBAR_SELECTION_FOREGROUND = new Color(255, 255, 255);
    public static final Color MENU_ITEM_FG = new Color(0, 0, 0);
    public static final Color MENU_DIS_ITEM_FG = new Color(130, 130, 130);
    public static final Color MENU_BORDER_COLOR = new Color(85, 83, 79);
    public static final Color MENU_SEL_ITEM_BG_1 = new Color(53, 89, 131);
    public static final Color MENU_SEL_ITEM_BG_2 = new Color(33, 69, 111);
    public static final Color MENU_BG_1 = new Color(225, 222, 216);
    public static final Color MENU_BG_2 = new Color(235, 232, 226);

    public static class MenuSeparatorUI extends BasicSeparatorUI {
        public void paint(Graphics g, JComponent c) {
            int hh = c.getHeight() / 2;
            g.setColor(MENU_SEP_SHADOW);
            g.drawLine(2, hh - 1, c.getWidth() - 4, hh - 1);
            g.setColor(MENU_SEP_HIGHLIGHT);
            g.drawLine(3, hh, c.getWidth() - 3, hh);
        }
    }

    public static class Menu extends JMenu {
        public Menu(String s) {
            super(s);

            setOpaque(true);

            setUI(new BasicMenuUI() {
                protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
                    bgColor = menuItem.isSelected() ? MENUBAR_SELECTION_BACKGROUND : MENUBAR_BACKGROUND;
                    super.paintBackground(g, menuItem, bgColor);
                }
                protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect, String text) {
                    Color fgColor = menuItem.isSelected() ? MENUBAR_SELECTION_FOREGROUND : MENUBAR_FOREGROUND;
                    g.setColor(fgColor);
                    super.paintText(g, menuItem, textRect, text);
                }
            });

            setPopupMenuLook(getPopupMenu());
        }

        public void addSeparator() {
            JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
            separator.setUI(new MenuSeparatorUI());
            getPopupMenu().add(separator);
        }
    }


    public static class InnerMenu extends JMenu {
        public InnerMenu(String s) {
            super(s);

            setOpaque(false);
            setPopupMenuLook(getPopupMenu());
            setForeground(MENU_ITEM_FG);
            setUI(new BasicMenuUI() {
                protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
                    if (menuItem.isEnabled() && menuItem.isSelected()) {
                        paintItemBackground(g, menuItem);
                    }
                }
                protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect, String text) {
                    g.setColor(MENU_ITEM_FG);
                    if (!menuItem.isEnabled()) {
                        UIManager.put("MenuItem.disabledForeground", MENU_DIS_ITEM_FG);
                    }
                    String jreVersion = System.getProperty ("java.version");
                    if ("1.6".compareTo (jreVersion) > 0)
                        textRect.x += 4;
                    super.paintText(g, menuItem, textRect, text);
                }
            });
        }

        public void addSeparator() {
            JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
            separator.setUI(new MenuSeparatorUI());
            getPopupMenu().add(separator);
        }
    }

    public static class MenuItem extends JMenuItem {
        public MenuItem(String s) {
            super(s);
            defineLook();
        }
        public MenuItem(String text, int mnemonic) {
            super(text, mnemonic);
            defineLook();
        }
        public MenuItem(String text, Icon icon) {
            super(text, icon);
            defineLook();
        }
        private void defineLook() {
            setOpaque(false);
            setUI(new BasicMenuItemUI() {
                protected void paintMenuItem(Graphics g, JComponent c, Icon checkIcon, Icon arrowIcon, Color background, Color foreground, int defaultTextIconGap) {
                    acceleratorForeground = MENU_ITEM_FG;
                    checkIcon = ResourceManager.RADIO_BUTTON_EMPTY_ICON;
                    super.paintMenuItem(g, c, checkIcon, arrowIcon, background, foreground, defaultTextIconGap);
                }
                protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect, String text) {
                    g.setColor(MENU_ITEM_FG);
                    if (!menuItem.isEnabled()) {
                        UIManager.put("MenuItem.disabledForeground", MENU_DIS_ITEM_FG);
                    }
                    super.paintText(g, menuItem, textRect, text);
                }
                protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
                    if (menuItem.getModel().isArmed() && menuItem.isEnabled()) {
                        paintItemBackground(g, menuItem);
                    }
                }
            });
        }
    }

    public static class RadioMenuItem extends JRadioButtonMenuItem {
        public RadioMenuItem(String text) {
            super(text);
            setOpaque(false);
            setUI(new BasicRadioButtonMenuItemUI() {
                protected void paintMenuItem(Graphics g, JComponent c, Icon checkIcon, Icon arrowIcon, Color background, Color foreground, int defaultTextIconGap) {
                    acceleratorForeground = MENU_ITEM_FG;
                    checkIcon = RadioMenuItem.this.isSelected() ? ResourceManager.RADIO_BUTTON_SELECTED_ICON : ResourceManager.RADIO_BUTTON_EMPTY_ICON;
                    super.paintMenuItem(g, c, checkIcon, arrowIcon, background, foreground, defaultTextIconGap);
                }
                protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect, String text) {
                    g.setColor(MENU_ITEM_FG);
                    if (!menuItem.isEnabled()) {
                        UIManager.put("MenuItem.disabledForeground", MENU_DIS_ITEM_FG);
                    }
                    super.paintText(g, menuItem, textRect, text);
                }
                protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
                    if (menuItem.getModel().isArmed() && menuItem.isEnabled()) {
                        paintItemBackground(g, menuItem);
                    }
                }
            });
        }
    }

    public static class CheckboxMenuItem extends JCheckBoxMenuItem {
        public CheckboxMenuItem(String text, boolean isChecked) {
            super(text, isChecked);
            setOpaque(false);
            setUI(new BasicCheckBoxMenuItemUI() {
                protected void paintMenuItem(Graphics g, JComponent c, Icon checkIcon, Icon arrowIcon, Color background, Color foreground, int defaultTextIconGap) {
                    acceleratorForeground = MENU_ITEM_FG;
                    checkIcon = CheckboxMenuItem.this.isSelected() ? ResourceManager.CHECKBOX_SELECTED_ICON : ResourceManager.CHECKBOX_ICON;
                    super.paintMenuItem(g, c, checkIcon, arrowIcon, background, foreground, defaultTextIconGap);    //To change body of overridden methods use File | Settings | File Templates.
                }
                protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect, String text) {
                    g.setColor(MENU_ITEM_FG);
                    if (!menuItem.isEnabled()) {
                        UIManager.put("MenuItem.disabledForeground", MENU_DIS_ITEM_FG);
                    }
                    super.paintText(g, menuItem, textRect, text);
                }
                protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
                    if (menuItem.getModel().isArmed() && menuItem.isEnabled()) {
                        paintItemBackground(g, menuItem);
                    }
                }
            });
        }
    }

    public static void setPopupMenuLook(final JPopupMenu popup) {
        popup.setLightWeightPopupEnabled(false);
        popup.setBorder(new Border() {
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                g.setColor(MENU_BORDER_COLOR);
                g.drawRect(0, 0, width - 1, height - 1);
            }
            public Insets getBorderInsets(Component c) {
                return new Insets(1, 1, 1, 1);
            }
            public boolean isBorderOpaque() {
                return false;
            }
        });
        popup.setUI(new BasicPopupMenuUI() {
            public void paint(Graphics g, JComponent c) {
                int w = c.getWidth();
                int h = c.getHeight();
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, MENU_BG_1, w - 1, 0, MENU_BG_2, false);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }
        });
    }

    public static void paintItemBackground(Graphics g, JMenuItem menuItem) {
        int w = menuItem.getWidth();
        int h = menuItem.getHeight();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, MENU_SEL_ITEM_BG_1, 0, h - 1, MENU_SEL_ITEM_BG_2, false);
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

}