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
import org.webharvest.gui.component.DropDownButton;
import org.webharvest.gui.component.DropDownButtonListener;
import org.webharvest.runtime.variables.IVariable;
import org.webharvest.utils.XmlUtil;
import org.webharvest.utils.XmlValidator;
import org.xml.sax.InputSource;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.StringReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: Vladimir Nikic
 * Date: May 8, 2007
 */
public class ViewerFrame extends JFrame implements DropDownButtonListener, ActionListener {

    public static final int TEXT_VIEW = 0;
    public static final int XML_VIEW = 1;
    public static final int HTML_VIEW = 2;
    public static final int IMAGE_VIEW = 3;
    public static final int LIST_VIEW = 4;

    // array of flags indicating if specific view is refreshed with new value
    private boolean refreshed[] = new boolean[5]; 

    // name of the property being viewed
    private String propertyName;
    
    // value that should be displayed in this viewer
    private Object value;

    // node info for which this view is connected
    private TreeNodeInfo nodeInfo;

    // index of currently open view
    private int currentView = TEXT_VIEW;

    // display components
    private JPanel cardPanel;
    private JTextArea textArea;
    private JEditorPane htmlPane;
    private XmlTextPane xmlPane;
    private JPanel imagePanel;
    private JLabel imageLabel;
    private JEditorPane listPane;
    private JCheckBox keepSyncCheckBox;
    private JButton xmlValidateButton;
    private JButton xmlPrettyPrintButton;

    /**
     * Constructor.
     * @param propertyName
     * @param value
     * @param nodeInfo
     */
    public ViewerFrame(final String propertyName, final Object value, final TreeNodeInfo nodeInfo, final int viewIndex) {
        String elementName = nodeInfo.getElementDef().getShortElementName();
        setTitle("[processor: " + elementName + ", property: " + propertyName + "]");
        this.setIconImage( ((ImageIcon) ResourceManager.getViewIcon()).getImage() );

        this.propertyName = propertyName;
        this.value = value;
        this.nodeInfo = nodeInfo;

        this.addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                // remove this viewer from the list of synchronized views
                if (nodeInfo != null) {
                    nodeInfo.removeSynchronizedView(ViewerFrame.this);
                }
                super.windowClosing(e);
            }
        });

        Container contentPane = getContentPane();
        contentPane.setLayout( new BorderLayout() );
        
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        this.keepSyncCheckBox = new JCheckBox("Keep synchronized");
        this.keepSyncCheckBox.addActionListener(this);
        toolBar.add(this.keepSyncCheckBox);

        toolBar.addSeparator(new Dimension(10, 0));

        DropDownButton viewTypeButton = new DropDownButton();
        viewTypeButton.addMenuItem( new JMenuItem("Text  ") );
        viewTypeButton.addMenuItem( new JMenuItem("XML  ") );
        viewTypeButton.addMenuItem( new JMenuItem("HTML  ") );
        viewTypeButton.addMenuItem( new JMenuItem("Image  ") );
        viewTypeButton.addMenuItem( new JMenuItem("List  ") );
        viewTypeButton.changeSelectedTo(viewIndex);
        viewTypeButton.addListener(this);
        toolBar.add( new JLabel(" View as: ") );
        toolBar.add(viewTypeButton);

        this.xmlValidateButton = new JButton("Check well-formedness", ResourceManager.getValidateIcon());
        this.xmlValidateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                validateXml(true);
            }
        });

        this.xmlPrettyPrintButton = new JButton("Pretty-print", ResourceManager.getPrettyPrintIcon());
        this.xmlPrettyPrintButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                prettyPrintXml();
            }
        });

        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(this.xmlValidateButton);
        toolBar.add(this.xmlPrettyPrintButton);

        contentPane.add(toolBar, BorderLayout.NORTH);

        this.cardPanel = new JPanel( new CardLayout() );

        // text view
        this.textArea = new JTextArea();
        this.textArea.setFont( new Font("Courier New", Font.PLAIN, 11) );
        this.cardPanel.add( new JScrollPane(this.textArea), String.valueOf(TEXT_VIEW) );

        // XML view
        this.xmlPane = new XmlTextPane();
        this.cardPanel.add( new JScrollPane(new ScrollableEditorPanel(xmlPane)), String.valueOf(XML_VIEW) );

        // HTML view
        this.htmlPane = new JEditorPane();
        this.htmlPane.setEditable(false);
        this.htmlPane.setContentType("text/html");
        this.htmlPane.setEditorKit( new HTMLEditorKit() );
        this.cardPanel.add( new JScrollPane(this.htmlPane), String.valueOf(HTML_VIEW) );

        // image view
        this.imagePanel = new JPanel(new BorderLayout());
        this.imageLabel = new JLabel("", JLabel.CENTER);
        this.imagePanel.add(imageLabel, BorderLayout.CENTER);
        this.cardPanel.add( new JScrollPane(this.imagePanel), String.valueOf(IMAGE_VIEW) );

        // HTML view
        this.listPane = new JEditorPane();
        this.listPane.setEditable(false);
        this.listPane.setContentType("text/html");
        this.listPane.setEditorKit( new HTMLEditorKit() );
        this.cardPanel.add( new JScrollPane(this.listPane), String.valueOf(LIST_VIEW) );

        openView(viewIndex);

        contentPane.add(cardPanel, BorderLayout.CENTER);

        this.pack();
    }

    private void prettyPrintXml() {
        boolean valid = validateXml(false);
        if (valid) {
            String xmlText = this.xmlPane.getText();
            try {
                String prettyXml = XmlUtil.prettyPrintXml(xmlText);
                this.xmlPane.setText(prettyXml);
                this.xmlPane.setCaretPosition(0);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "parsing error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validateXml(boolean showOkMessage) {
        XmlValidator validator = new XmlValidator();
        String s = value == null ? "" : value.toString();
        boolean valid = validator.parse( new InputSource(new StringReader(s)) );
        if (valid) {
            if (showOkMessage) {
                JOptionPane.showMessageDialog(this, "XML is well-formed.", "XML validation", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            String msg = "XML is not well-formed: " + validator.getException().getMessage() +
                         " [line: " + validator.getLineNumber() + ", col: " + validator.getColumnNumber() + "].";
            JOptionPane.showMessageDialog(this, msg, "XML validation", JOptionPane.ERROR_MESSAGE);
        }

        return valid;
    }

    private void refresh(int viewIndex) {
        switch(viewIndex) {
            case TEXT_VIEW:
                if (!this.refreshed[TEXT_VIEW]) {
                    this.textArea.setText( value == null ? null : value.toString() );
                    this.textArea.setCaretPosition(0);
                }
                break;
            case XML_VIEW:
                if (!this.refreshed[XML_VIEW]) {
                    this.xmlPane.setText( value == null ? null : value.toString() );

                }this.xmlPane.setCaretPosition(0);
                break;
            case HTML_VIEW:
                if (!this.refreshed[HTML_VIEW]) {
                    if (value != null) {
                        // Workaround for BUG 4695909: parse the HTML String and remove everything in the HEAD section before calling setText(String).
                        Pattern pattern = Pattern.compile("<head(.)*</head>", Pattern.DOTALL|Pattern.UNICODE_CASE);
                        Matcher matcher = pattern.matcher(value.toString());
                        String htmlText = matcher.replaceFirst("");

                        this.htmlPane.setText(htmlText);
                    } else {
                        this.htmlPane.setText(null);
                    }
                }
                break;
            case IMAGE_VIEW:
                if (!this.refreshed[IMAGE_VIEW]) {
                    if (value instanceof IVariable) {
                        IVariable var = (IVariable) value;
                        this.imageLabel.setIcon( new ImageIcon(var.toBinary()) );
                    } else {
                        this.imageLabel.setIcon(null);
                    }
                }
                break;
            case LIST_VIEW:
                if (!this.refreshed[LIST_VIEW]) {
                    if (value instanceof IVariable) {
                        IVariable var = (IVariable) value;
                        java.util.List list = var.toList();
                        String html = "<table width=\"100%\">";
                        for (int i = 0; i < list.size(); i++) {
                            Object curr = list.get(i);
                            String stringValue = curr == null ? "" : curr.toString();
                            stringValue = stringValue.replaceAll("<", "&lt;");
                            stringValue = stringValue.replaceAll(">", "&gt;");
                            html += i % 2 == 0 ? "<tr>" : "<tr bgcolor=\"#FFFFCC\">";
                            html += "<td><code>" + (i + 1) + ".</code></td>";
                            html += "<td width=\"100%\"><code>" + stringValue + "</code></td>";
                            html += "</tr>";
                        }
                        html += "</table>";
                        this.listPane.setText(html);
                    } else {
                        this.listPane.setText(value == null ? "" : value.toString());
                    }
                }
                break;
        }

        if (viewIndex >= 0 && viewIndex < this.refreshed.length) {
            this.refreshed[viewIndex] = true;
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(640, 480);
    }

    private void updateControls() {
        this.xmlValidateButton.setVisible( this.currentView == XML_VIEW );
        this.xmlPrettyPrintButton.setVisible( this.currentView == XML_VIEW );
    }

    private void openView(int viewIndex) {
        CardLayout cardLayout = (CardLayout)(this.cardPanel.getLayout());
        this.currentView = viewIndex;
        refresh(viewIndex);
        cardLayout.show( this.cardPanel, String.valueOf(viewIndex) );
        updateControls();
    }

    /**
     * When button changes
     * @param dropDownButton
     */
    public void onChange(DropDownButton dropDownButton) {
        openView( dropDownButton.getSelectedItem() );
    }

    public void setValue(Map properties) {
        if (properties != null) {
            Object newValue = properties.get(this.propertyName);
            this.value = newValue;

            // invalidate views
            for (int i = 0; i < refreshed.length; i++) {
                refreshed[i] = false;

            }

            refresh(this.currentView);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.keepSyncCheckBox) {
            boolean isSynchronized = this.keepSyncCheckBox.isSelected();
            if (isSynchronized) {
                this.nodeInfo.addSynchronizedView(this);
            } else {
                this.nodeInfo.removeSynchronizedView(this);
            }
        }
    }

}