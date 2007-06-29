/*
 * $Id: ScrollableEditorPanel.java,v 1.1 2005/03/28 13:35:41 edankert Exp $
 *
 * Copyright (c) 2002 - 2005, Edwin Dankert
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright 
 *   notice, this list of conditions and the following disclaimer in the 
 *   documentation and/or other materials provided with the distribution. 
 * * Neither the name of 'Edwin Dankert' nor the names of its contributors 
 *   may  be used to endorse or promote products derived from this software 
 *   without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.bounce.text;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.View;
import javax.swing.text.WrappedPlainView;
import javax.swing.text.BadLocationException;

/**
 * Wrapper panel to force the editor pane to resize when a
 * Wrapped View has been installed.
 * <p/>
 * Takes Block Increment and Unit Increment info from editor.
 *
 * @author Edwin Dankert <edankert@gmail.com>
 * @version $Revision: 1.1 $, $Date: 2005/03/28 13:35:41 $
 */
public class ScrollableEditorPanel extends JPanel implements Scrollable {
    public static final int DEFAULT_MARKER_TYPE = 0;
    public static final int RUNNING_MARKER_TYPE = 1;
    public static final int ERROR_MARKER_TYPE = 2;

    /**
     * Panel used for optionally displying line numbers.
     */
    private class LineNumberPanel extends JPanel {
        private final Color BORDER_COLOR = new Color(128, 128, 128);
        private final Color NUMBER_COLOR = new Color(128, 128, 128);
        private final Color DEFAULT_MARKER_COLOR = new Color(128, 128, 128);
        private final Color RUNNING_MARKER_COLOR = new Color(0, 128, 0);
        private final Color ERROR_MARKER_COLOR = new Color(255, 0, 0);

        private final Font font = editor.getFont();

        public Dimension getPreferredSize() {
            int editorHeight = editor.getHeight();
            FontMetrics fm = getFontMetrics(font);
            int lineHeight = fm.getHeight();
            int maxHeight = calculateTextHeight();

            int numOfLines = lineHeight > 0 ? maxHeight / lineHeight : 0;

            String lastValue = String.valueOf(numOfLines);
            Rectangle2D rect = fm.getStringBounds(lastValue, getGraphics());

            return new Dimension(20 + (int)rect.getWidth(), editorHeight);
        }

        public void paint(Graphics g) {
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
            g.setColor(NUMBER_COLOR);

            g.setFont(font);

            int x = 5;
            int lineHeight = getFontMetrics(font).getHeight();
            int y = lineHeight;

            int maxHeight = calculateTextHeight();

            int lineNum = 1;
            while (y < maxHeight) {
                g.drawString("" + lineNum, x, y);
                lineNum++;
                y += lineHeight;
            }

            int right = getWidth() - 1;
            g.setColor(BORDER_COLOR);
            g.drawLine(right, 0, right, getHeight() - 1);

            for (int i = 0; i < markers.size(); i++) {
                LineMarker lineMarker = (LineMarker) markers.get(i);
                switch(lineMarker.markerType) {
                    case DEFAULT_MARKER_TYPE:
                        g.setColor(DEFAULT_MARKER_COLOR);
                        g.drawString(String.valueOf((char)0x25BA), this.getWidth() - 12, lineHeight * lineMarker.line);
                        break;
                    case RUNNING_MARKER_TYPE:
                        g.setColor(RUNNING_MARKER_COLOR);
                        g.drawString(String.valueOf((char)0x25BA), this.getWidth() - 12, lineHeight * lineMarker.line);
                        break;
                    case ERROR_MARKER_TYPE:
                        g.setColor(ERROR_MARKER_COLOR);
                        g.drawString(String.valueOf((char)0x25BA), this.getWidth() - 12, lineHeight * lineMarker.line);
                        break;
                }
            }
        }

        private int calculateTextHeight() {
            int maxHeight = 0;
            int lastOffset = editor.getDocument().getEndPosition().getOffset();
            try {
                maxHeight = (int) (editor.modelToView(lastOffset).getMaxY());
            } catch (Exception e) {
                maxHeight = 0;
            }
            return maxHeight;
        }
    }

    private class LineMarker {
        private int markerType = DEFAULT_MARKER_TYPE;
        private int line = -1;

        public LineMarker(int markerType, int line) {
            this.markerType = markerType;
            this.line = line;
        }
    }

    private JEditorPane editor = null;

    private ScrollableEditorPanel.LineNumberPanel lineNumberPanel;

    private boolean showLineNumbers = true;

    // list of all markers
    private java.util.List markers = new ArrayList();

    /**
     * Constructs the panel, with the editor in the Center
     * of the BorderLayout.
     *
     * @param editor the parent editor.
     */
    public ScrollableEditorPanel(JEditorPane editor, boolean showLineNumbers) {
        super(new BorderLayout());

        this.editor = editor;
        this.showLineNumbers = showLineNumbers;

        this.lineNumberPanel = new LineNumberPanel();
        this.lineNumberPanel.setVisible(showLineNumbers);

        add(this.lineNumberPanel, BorderLayout.WEST);
        add(editor, BorderLayout.CENTER);
    }

    /**
     * @see Scrollable#getPreferredScrollableViewportSize()
     */
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    /**
     * Returns the information directly from the editor component.
     *
     * @see Scrollable#getScrollableUnitIncrement(java.awt.Rectangle,int,int)
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return editor.getScrollableUnitIncrement(visibleRect, orientation, direction);
    }

    /**
     * Returns the information directly from the editor component.
     *
     * @see Scrollable#getScrollableBlockIncrement(java.awt.Rectangle,int,int)
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return editor.getScrollableBlockIncrement(visibleRect, orientation, direction);
    }

    /**
     * Return true when a Wrapped View is used.
     *
     * @see Scrollable#getScrollableTracksViewportWidth()
     */
    public boolean getScrollableTracksViewportWidth() {
        View view = editor.getUI().getRootView(editor).getView(0);

        if (view instanceof WrappedPlainView) {
            return true;
        } else if (getParent() instanceof JViewport) {
            return (((JViewport) getParent()).getWidth() > getPreferredSize().width);
        }

        return false;
    }

    /**
     * @see Scrollable#getScrollableTracksViewportHeight()
     */
    public boolean getScrollableTracksViewportHeight() {
        if (getParent() instanceof JViewport) {
            return (((JViewport) getParent()).getHeight() > getPreferredSize().height);
        }
        return false;
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
        this.showLineNumbers = showLineNumbers;
        this.lineNumberPanel.setVisible(showLineNumbers);
        repaintLineNumbers();
    }

    public void toggleShowLineNumbers() {
        setShowLineNumbers(!showLineNumbers);
    }
 
    public boolean isShowLineNumbers() {
        return showLineNumbers;
    }

    /**
     * Initiates repaint of line numbers area.
     */
    private void repaintLineNumbers() {
        if (showLineNumbers) {
            this.lineNumberPanel.repaint();
        }
    }

    /**
     * Adds new editor marker specified by type and line number
     * @param markerType
     * @param line
     */
    public void addMarker(int markerType, int line) {
        this.markers.add( new LineMarker(markerType, line) );
        if (showLineNumbers) {
            this.lineNumberPanel.repaint();
        }
    }

    /**
     * Clears list of all editor markers.
     */
    public void clearAllMarkers() {
        this.markers.clear();
        if (this.showLineNumbers) {
            this.lineNumberPanel.repaint();
        }
    }

    /**
     * Deletes all markers of specified type.
     */
    public void clearMarkers(int markerType) {
        Iterator iterator = markers.iterator();
        while (iterator.hasNext()) {
            LineMarker lineMarker = (LineMarker) iterator.next();
            if (lineMarker.markerType == markerType) {
                iterator.remove();
            }
        }

        if (this.showLineNumbers) {
            this.lineNumberPanel.repaint();
        }
    }

    public void onDocChanged() {
        clearAllMarkers();
        repaintLineNumbers();
    }

}