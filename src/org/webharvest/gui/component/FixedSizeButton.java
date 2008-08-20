package org.webharvest.gui.component;

import javax.swing.*;
import java.awt.*;

/**
 * Button with fixed dimension
 */
public class FixedSizeButton extends JButton {

    public FixedSizeButton(String text, int width, int height) {
        super(text);
        Dimension dim = new Dimension(width, height);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setMaximumSize(dim);
    }

    public FixedSizeButton(String text, Icon icon, int width, int height) {
        this(text, width, height);
        setIcon(icon);
    }

}