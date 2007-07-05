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

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class FindReplaceDialog extends JDialog {

    private static final int OPERATION_FIND = 1;
    private static final int OPERATION_REPLACE = 2; 

    private int operation = OPERATION_FIND;

    // Ide instance where this dialog belongs.
    private Frame parentFrame;

    // settiongs fields
    private JTextField searchField;
    private JLabel replaceLabel;
    private JTextField replaceField;
    private JTextComponent textComponent;

    private JCheckBox caseSensitiveCheckBox;
    private JCheckBox regularExpressionsCheckBox;
    private JRadioButton forwardRadioButton;
    private JRadioButton backwordRadioButton;
    private JRadioButton globalRadioButton;
    private JRadioButton selectedTextRadioButton;
    private JRadioButton fromCursorRadioButton;
    private JRadioButton entireScopeRadioButton;

    public FindReplaceDialog(Frame parentFrame) throws HeadlessException {
        super(parentFrame, "Find Text", true);
        this.parentFrame = parentFrame;
        this.setResizable(false);

        addWindowListener( new WindowAdapter() {
           public void windowActivated( WindowEvent e ){
                searchField.requestFocus();
                searchField.setSelectionStart(0);
             }
        } );

        createGui();
    }

    private void createGui() {
        Container contentPane = this.getContentPane();

        JPanel topPanel = new JPanel(new GridBagLayout());

        contentPane.setLayout( new BorderLayout() );

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2, 5, 2, 5);

        searchField = new JTextField("") {
            public Dimension getPreferredSize() {
                return new Dimension(300, 20);
            }
        };
        replaceLabel = new JLabel("Replace with: ");
        replaceField = new JTextField("") {
            public Dimension getPreferredSize() {
                return new Dimension(300, 20);
            }
        };
        caseSensitiveCheckBox = new JCheckBox("Case sensitive", false);

        constraints.gridx = 0;
        constraints.gridy = 0;
        topPanel.add( new JLabel("Text to find: "), constraints );

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        topPanel.add(searchField, constraints );

        constraints.gridx = 0;
        constraints.gridy = 1;
        topPanel.add( replaceLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 1;
        topPanel.add(replaceField, constraints );

        JPanel optionsPanel = createOptionsPanel();
        JPanel directionsPanel = createDirectionsPanel();
        JPanel scopePanel = createScopePanel();
        JPanel originPanel = createOriginPanel();

        JPanel middlePanel = new JPanel(new GridLayout(2, 2));
        middlePanel.add(optionsPanel);
        middlePanel.add(directionsPanel);
        middlePanel.add(scopePanel);
        middlePanel.add(originPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(new Insets(4, 4, 4, 4)));
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(middlePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel( new FlowLayout(FlowLayout.CENTER) );
        
        JButton okButton = new JButton("  Find  ");
        buttonPanel.add(okButton);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (operation == OPERATION_FIND) {
                    find(false);
                } else {
                    replace(false);
                }
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        contentPane.add(mainPanel, BorderLayout.CENTER);

        this.pack();
    }

    private JPanel createDirectionsPanel() {
        JPanel directionsPanel = new JPanel(new GridLayout(2, 1));
        directionsPanel.setBorder(BorderFactory.createTitledBorder("Direction"));

        ButtonGroup group = new ButtonGroup();

        this.forwardRadioButton = new JRadioButton("Forward");
        this.forwardRadioButton.setSelected(true);
        this.backwordRadioButton = new JRadioButton("Backword");

        group.add(this.forwardRadioButton);
        group.add(this.backwordRadioButton);
        
        directionsPanel.add(this.forwardRadioButton);
        directionsPanel.add(this.backwordRadioButton);
        
        return directionsPanel;
    }

    private JPanel createScopePanel() {
        JPanel scopePanel = new JPanel(new GridLayout(2, 1));
        scopePanel.setBorder(BorderFactory.createTitledBorder("Scope"));

        ButtonGroup group = new ButtonGroup();

        this.globalRadioButton = new JRadioButton("Global");
        this.globalRadioButton.setSelected(true);
        this.selectedTextRadioButton = new JRadioButton("Selected text");

        group.add(this.globalRadioButton);
        group.add(this.selectedTextRadioButton);

        scopePanel.add(this.globalRadioButton);
        scopePanel.add(this.selectedTextRadioButton);

        return scopePanel;
    }

    private JPanel createOriginPanel() {
        JPanel originPanel = new JPanel(new GridLayout(2, 1));
        originPanel.setBorder(BorderFactory.createTitledBorder("Origin"));

        ButtonGroup group = new ButtonGroup();

        this.fromCursorRadioButton = new JRadioButton("From cursor");
        this.fromCursorRadioButton.setSelected(true);
        this.entireScopeRadioButton = new JRadioButton("Entire scope");

        group.add(this.fromCursorRadioButton);
        group.add(this.entireScopeRadioButton);

        originPanel.add(this.fromCursorRadioButton);
        originPanel.add(this.entireScopeRadioButton);

        return originPanel;
    }

    private JPanel createOptionsPanel() {
        JPanel optionsPanel = new JPanel(new GridLayout(2, 1));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
        this.caseSensitiveCheckBox = new JCheckBox("Case sensitive");
        optionsPanel.add(this.caseSensitiveCheckBox);
        this.regularExpressionsCheckBox = new JCheckBox("Regular expressions");
        optionsPanel.add(this.regularExpressionsCheckBox);
        return optionsPanel;
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

        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (operation == OPERATION_FIND) {
                    find(false);
                } else {
                    replace(false);
                }
            }
        };
        rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);


        return rootPane;
    }

    public void open(JTextComponent textComponent, boolean isReplace) {
        setTextComponent(textComponent);
        this.operation = isReplace ? OPERATION_REPLACE : OPERATION_FIND;
        this.replaceLabel.setVisible(isReplace);
        this.replaceField.setVisible(isReplace);
        this.setTitle(isReplace ? "Replace Text" : "Find Text");
        this.pack();

        setLocationRelativeTo(parentFrame);
        setVisible(true);
    }

    public void find(boolean backward) {
        if (backward) {
            backward = this.forwardRadioButton.isSelected(); 
        } else {
            backward = this.backwordRadioButton.isSelected();
        }
        
        this.setVisible(false);
        if (this.textComponent != null) {
            String content = this.textComponent.getText();
            String searchText = this.searchField.getText();
            if ( !"".equals(searchText) ) {
                int startPosition = 0;
                if ( this.fromCursorRadioButton.isSelected() ) {
                    startPosition = textComponent.getCaretPosition();
                } else if (backward) {
                    startPosition = content.length() - 1;
                } else {
                    startPosition = 0;
                }

                int index = -1;
                if (backward) {
                    content = content.substring( 0, Math.max(startPosition - searchText.length(), 1) );
                    index = content.lastIndexOf(searchText);
                } else {
                    index = content.indexOf(searchText, startPosition);
                }
                
                if (index >= 0) {
                    textComponent.grabFocus();
                    textComponent.select(index, index + searchText.length());
                } else {
                    showTooltipMessage("Next occurrence of \"" + searchText + "\" not found.");
                }
            }
        }
    }

    /**
     * Displays specified message in the tooltip at the caret position.
     * @param text
     */
    private void showTooltipMessage(String text) {
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        this.textComponent.setToolTipText(text);

        int previousInitialDelay = toolTipManager.getInitialDelay();
        toolTipManager.setInitialDelay(0);

        Point caretPosition = this.textComponent.getCaret().getMagicCaretPosition();
        toolTipManager.registerComponent(this.textComponent);
        toolTipManager.mouseMoved( new MouseEvent(this.textComponent, 0, 0, 0, (int)caretPosition.getX(), (int)caretPosition.getY(), 0, false) );

        toolTipManager.setInitialDelay(previousInitialDelay);

        toolTipManager.unregisterComponent(this.textComponent);
    }

    public void replace(boolean backward) {
        
    }

    public String getSearchText() {
        return this.searchField.getText();
    }

    public void setTextComponent(JTextComponent textComponent) {
        this.textComponent = textComponent;
    }

    public void findNext(JTextComponent textComponent) {
        setTextComponent(textComponent);
        find(false);
    }

    public void findPrev(JTextComponent textComponent) {
        this.textComponent = textComponent;
        find(true);
    }
}