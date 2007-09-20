package org.webharvest.gui;

import org.webharvest.utils.CommonUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class AboutWindow extends JWindow implements HyperlinkListener {

    private static final Dimension WINDOW_DIMENSION = new Dimension(300, 200);

    // Ide instance where this dialog belongs.
    private Ide ide;

    public AboutWindow(Ide ide) throws HeadlessException {
        super(ide);
        this.ide = ide;

        createGUI();
    }

    private void createGUI() {
        Container contentPane = this.getContentPane();
        contentPane.setLayout( new BorderLayout() );

        JEditorPane htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
        htmlPane.setContentType("text/html");
        htmlPane.setEditorKit( new HTMLEditorKit() );
        htmlPane.setBorder(new LineBorder(Color.gray));
        htmlPane.addHyperlinkListener(this);
        htmlPane.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                AboutWindow.this.setVisible(false);
            }
        });
        try {
            htmlPane.setPage( ResourceManager.getAboutUrl() );
        } catch (IOException e) {
            e.printStackTrace();
        }
        contentPane.add(htmlPane, BorderLayout.CENTER);

        this.pack();
    }

    protected JRootPane createRootPane() {
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JRootPane rootPane = new JRootPane();
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            }
        };
        rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        return rootPane;
    }

    public Dimension getPreferredSize() {
        return WINDOW_DIMENSION;
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            String url = e.getDescription().toString();
            ide.openURLInBrowser(url);
        }
    }

    public void open() {
        setLocationRelativeTo(this.ide);
        setVisible(true);
    }

}