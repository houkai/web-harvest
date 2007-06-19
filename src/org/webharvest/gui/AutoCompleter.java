package org.webharvest.gui;

import org.webharvest.definition.DefinitionResolver;
import org.webharvest.definition.ElementInfo;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Instance of the class is responsible for auto completion of defined tags and
 * attributes in the Web-Harvest XML configuration. It wraps instance of XML editor
 * pane and popup menu that offers context specific set of tags/attributes in the
 * editor. 
 *
 * @author: Vladimir Nikic
 * Date: May 24, 2007
 */
public class AutoCompleter implements ActionListener {

    // editor context which decides about auto completion type  
    private static final int TAG_CONTEXT = 0;
    private static final int ATTRIBUTE_CONTEXT = 1;

    // special XML constructs
    private static final String CDATA_NAME = "<![CDATA[ ... ]]>";
    private static final String XML_COMMENT_NAME = "<!-- ... -->";

    // popup window look & feel
    private static final Color BG_COLOR = new Color(235, 244, 254);
    private static final Font POPUP_FONT = new Font( "Courier", Font.PLAIN, 12);

    // instance of popup menu used as auto completion popup window
    private JPopupMenu popupMenu = new JPopupMenu();

    // xml pane instance which this auto completer is bound to
    private XmlTextPane xmlPane;

    // current context for auto cempletion
    private transient int context = TAG_CONTEXT;

    // length of prefix that user already has typed
    private int prefixLength;

    // allowed elements
    private Map elementInfos;

    /**
     * Constructor.
     * @param xmlPane
     */
    public AutoCompleter(final XmlTextPane xmlPane) {
        this.popupMenu.addMenuKeyListener( new MenuKeyListener() {
            public void menuKeyPressed(MenuKeyEvent e) {
                char ch = e.getKeyChar();
                int code = e.getKeyCode();
                if ( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '-' || code == MenuKeyEvent.VK_BACK_SPACE ) {
                    Document document = xmlPane.getDocument();
                    int pos = xmlPane.getCaretPosition();
                    try {
                        // deleting or inserting new character
                        if (code == MenuKeyEvent.VK_BACK_SPACE) {
                            if ( pos > 0 && document.getLength() > 0 ) {
                                document.remove(pos - 1, 1);
                            }
                        } else {
                            document.insertString(pos, String.valueOf(ch), null);
                        }
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                    popupMenu.setVisible(false);
                    autoComplete();
                }
            }
            public void menuKeyReleased(MenuKeyEvent e) {
            }
            public void menuKeyTyped(MenuKeyEvent e) {
            }
        });

        this.xmlPane = xmlPane;
        this.popupMenu.setBorder( new TitledBorder("") );
        this.elementInfos = DefinitionResolver.getElementInfos();
    }

    private void addItem(final String name) {
        JMenuItem menuItem = new JMenuItem(name);

        menuItem.setBackground(BG_COLOR);
        menuItem.setFont(POPUP_FONT);
        menuItem.addActionListener(this);

        this.popupMenu.add(menuItem);
    }

    private void defineTagsMenu(String prefix) {
        if (prefix != null) {
            prefix = prefix.toLowerCase();
        }
        
        this.popupMenu.removeAll();

        Iterator iterator = this.elementInfos.entrySet().iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            if ( prefix == null || key.toLowerCase().startsWith(prefix) ) {
                ElementInfo elementInfo = (ElementInfo) entry.getValue();
                addItem( elementInfo.getName() );
                count++;
            }
        }

        boolean addCData = CDATA_NAME.toLowerCase().startsWith("<" + prefix);
        boolean addXmlComment = XML_COMMENT_NAME.toLowerCase().startsWith("<" + prefix);

        if (addCData || addXmlComment) {
            if (count > 0) {
                this.popupMenu.addSeparator();
            }
            if (addCData) {
                addItem(CDATA_NAME);
            }
            if (addXmlComment) {
                addItem(XML_COMMENT_NAME);
            }
        }
    }

    private void defineAttributesMenu(String elementName, String prefix) {
        elementName = elementName.toLowerCase();
        prefix = prefix.toLowerCase();

        this.popupMenu.removeAll();

        ElementInfo elementInfo = DefinitionResolver.getElementInfo(elementName);
        if (elementInfo != null) {
            Set allAtts = elementInfo.getAttsSet();

            Iterator iterator = allAtts.iterator();
            while (iterator.hasNext()) {
                String att = (String) iterator.next();
                if ( att != null && att.toLowerCase().startsWith(prefix) ) {
                    addItem(att);
                }
            }
        }
    }

    /**
     * Performs auto completion.
     */
    public void autoComplete() {
        try {
            Document document = this.xmlPane.getDocument();
            int offset = this.xmlPane.getCaretPosition();
            String text = document.getText(0, offset);

            int openindex = text.lastIndexOf('<');
            int closeindex = text.lastIndexOf('>');

            if (openindex > closeindex) {                   // inside tag definition
                text = text.substring(openindex);
                String identifier = getIdentifierFromEnd(text);
                if ( containWhitespaces(text) ) {           // attributes context
                    this.context = ATTRIBUTE_CONTEXT;
                    String elementName = getIdentifierFromStart(text);
                    defineAttributesMenu(elementName, identifier);
                    this.prefixLength = identifier.length();
                } else {
                    this.context = TAG_CONTEXT;         // tag name context
                    defineTagsMenu(identifier);
                    this.prefixLength = identifier.length() + 1;
                }
            } else {                                        // ouside tag definition
                this.context = TAG_CONTEXT;
                defineTagsMenu("");
                this.prefixLength = 0;
            }

            Rectangle position = this.xmlPane.modelToView(offset);
            if (this.popupMenu.getComponentCount() > 0) {
                this.popupMenu.show( this.xmlPane, (int)position.getX(), (int)(position.getY() + position.getHeight()) );
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param text
     * @return True if specified string contains any whitespace characters, false otherwise.
     */
    private boolean containWhitespaces(String text) {
        int len = text.length();
        for (int i = 0; i < len; i++) {
            if ( Character.isWhitespace(text.charAt(i)) ) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param text
     * @return Maximal peace at the start of specified string which is valid tag or attribute name. 
     */
    private String getIdentifierFromStart(String text) {
        if ( text.startsWith("<") ) {
            text = text.substring(1);
        }
        
        StringBuffer result = new StringBuffer();
        int len = text.length();
        for (int i = 0; i < len; i++) {
            char ch = text.charAt(i);
            if ( Character.isLetter(ch) || ch == '-' || ch == '_' || ch == '!' ) {
                result.append(ch);
            } else {
                break;
            }
        }

        return result.toString(); 
    }

    /**
     * @param text
     * @return Maximal peace at the end of specified string which is valid tag or attribute name.
     */
    private String getIdentifierFromEnd(String text) {
        StringBuffer result = new StringBuffer();
        for (int i = text.length() - 1; i >= 0; i--) {
            char ch = text.charAt(i);
            if ( Character.isLetter(ch) || ch == '-' || ch == '_' || ch == '!' ) {
                result.insert(0, ch);
            } else {
                break;
            }
        }

        return result.toString();
    }

    /**
     * Action for auto complete items
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
        JMenuItem menuItem = (JMenuItem) e.getSource();
        String name = menuItem.getText();

        try {
            if (this.context == TAG_CONTEXT) {
                completeTag(name);
            } else {
                completeAttribute(name);
            }
        } catch(BadLocationException e1) {
            e1.printStackTrace();
        }
    }

    private void completeAttribute(String name) throws BadLocationException {
        Document document = xmlPane.getDocument();
        int pos = xmlPane.getCaretPosition();
        String template = (name + "=\"\" ").substring(this.prefixLength);

        document.insertString(pos, template, null);
    }

    private void completeTag(String name) throws BadLocationException {
        Document document = xmlPane.getDocument();
        int pos = xmlPane.getCaretPosition();

        if ( CDATA_NAME.equals(name) ) {
            document.insertString(pos, "<![CDATA[  ]]>".substring(this.prefixLength), null);
            xmlPane.setCaretPosition( xmlPane.getCaretPosition() - 4 );
        } else if ( XML_COMMENT_NAME.equals(name) ) {
            document.insertString(pos, "<!--  -->".substring(this.prefixLength), null);
            xmlPane.setCaretPosition( xmlPane.getCaretPosition() - 4 );
        } else {
            ElementInfo info = DefinitionResolver.getElementInfo(name);
            if (info != null) {
                String template = info.getTemplate(true).substring(this.prefixLength);
                document.insertString(pos, template, null);
                int closingIndex = template.lastIndexOf("</");
                if (closingIndex >= 0) {
                    xmlPane.setCaretPosition( xmlPane.getCaretPosition() - template.length() + closingIndex );
                }
            }
        }
    }

}