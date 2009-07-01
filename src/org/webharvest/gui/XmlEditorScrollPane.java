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

import org.bounce.text.ScrollableEditorPanel;
import org.webharvest.gui.component.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Scroll pane that contains XML xmlTextPane and line numbers at left border
 */
public class XmlEditorScrollPane extends WHScrollPane {

    public static final int DEFAULT_MARKER_TYPE = 0;
    public static final int RUNNING_MARKER_TYPE = 1;
    public static final int ERROR_MARKER_TYPE = 2;

    private class LineMarker {
        private int markerType = DEFAULT_MARKER_TYPE;
        private int line = -1;

        public LineMarker(int markerType, int line) {
            this.markerType = markerType;
            this.line = line;
        }
    }
    
    /**
     * Panel used for optionally displying line numbers.
     */
    private class LineNumberPanel extends JPanel {
        private final Color BORDER_COLOR = new Color(128, 128, 128);
        private final Color NUMBER_COLOR = new Color(128, 128, 128);
        private final Color DEFAULT_MARKER_COLOR = new Color(164, 164, 164);
        private final Color RUNNING_MARKER_COLOR = new Color(0, 128, 0);
        private final Color ERROR_MARKER_COLOR = new Color(255, 0, 0);

        private final Font font = xmlTextPane.getFont();

        private LineNumberPanel() {
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int y = e.getPoint().y;
                    int lineHeight = getFontMetrics(font).getHeight();
                    BreakpointCollection breakpoints = xmlTextPane.getBreakpoints();
                    int lineNumber = y / lineHeight + 1;
                    if ( breakpoints.isThereBreakpoint(lineNumber) ) {
                        breakpoints.removeBreakpoint(lineNumber);
                    } else {
                        breakpoints.addBreakpoint(new BreakpointInfo(lineNumber));
                    }
                    repaintLineNumbers();
                }
            });
        }

        public Dimension getPreferredSize() {
            int editorHeight = xmlTextPane.getHeight();
            FontMetrics fm = getFontMetrics(font);
            int lineHeight = fm.getHeight();
            int maxHeight = calculateTextHeight();

            int numOfLines = lineHeight > 0 ? maxHeight / lineHeight : 0;

            String lastValue = String.valueOf(numOfLines);
            Rectangle2D rect = fm.getStringBounds(lastValue, getGraphics());

            int width = 22 + (int) rect.getWidth();
            
            return new Dimension(width, editorHeight);
        }

        public void paint(Graphics g) {
            int width = getWidth();

            g.setColor(Color.white);
            g.fillRect(0, 0, width - 1, getHeight() - 1);
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

            BreakpointCollection breakpoints = xmlTextPane.getBreakpoints();
            for (int i = 0; i < breakpoints.size(); i++) {
                BreakpointInfo breakpoint = (BreakpointInfo) breakpoints.get(i);
                y = breakpoint.getLineNumber() * lineHeight;
                if (y < maxHeight) {
                    g.drawImage(ResourceManager.BREAKPOINT_IMAGE, width - 14, y - 10, this);
                }
            }

            int right = width - 1;
            g.setColor(BORDER_COLOR);
            g.drawLine(right, 0, right, getHeight() - 1);

            for (int i = 0; i < markers.size(); i++) {
                LineMarker lineMarker = (LineMarker) markers.get(i);
                int xpos = width - 12;
                int ypos = lineHeight * lineMarker.line + 1;

                switch(lineMarker.markerType) {
                    case DEFAULT_MARKER_TYPE:
                        g.setColor(DEFAULT_MARKER_COLOR);
                        g.drawString(String.valueOf((char)0x25BA), xpos, ypos);
                        break;
                    case RUNNING_MARKER_TYPE:
                        g.setColor(RUNNING_MARKER_COLOR);
                        g.drawString(String.valueOf((char)0x25BA), xpos, ypos);
                        break;
                    case ERROR_MARKER_TYPE:
                        g.setColor(ERROR_MARKER_COLOR);
                        g.drawString(String.valueOf((char)0x25BA), xpos, ypos);
                        break;
                }
            }
        }

        private int calculateTextHeight() {
            int maxHeight = 0;
            int lastOffset = xmlTextPane.getDocument().getEndPosition().getOffset();
            try {
                maxHeight = (int) (xmlTextPane.modelToView(lastOffset).getMaxY());
            } catch (Exception e) {
                maxHeight = 0;
            }
            return maxHeight;
        }
    }

    private LineNumberPanel lineNumberPanel;
    private XmlTextPane xmlTextPane = null;
    private boolean showLineNumbers;

    // list of all markers
    private java.util.List markers = new ArrayList();

    /**
     * Constructor.
     * @param showLineNumbers
     * @param editor
     */
    public XmlEditorScrollPane(XmlTextPane editor, boolean showLineNumbers) {
        super( new ScrollableEditorPanel(editor) );
        this.xmlTextPane = editor;
        this.lineNumberPanel = new LineNumberPanel();
        this.showLineNumbers = showLineNumbers;
        this.setRowHeaderView(showLineNumbers ? this.lineNumberPanel : null);
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
        this.showLineNumbers = showLineNumbers;
        this.setRowHeaderView(showLineNumbers ? this.lineNumberPanel : null);
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
     * Adds new xmlTextPane marker specified by type and line number
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
     * Clears list of all xmlTextPane markers.
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