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
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.EtchedBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SettingsDialog extends JDialog implements ChangeListener {

    // Ide instance where this dialog belongs.
    private Ide ide;

    // settiongs fields
    private JTextField workingPathField;
    private JTextField proxyServerField;
    private JTextField proxyPortField;
    private JTextField proxyUsernameField;
    private JTextField proxyPasswordField;
    private JCheckBox proxyEnabledCheckBox;
    private JCheckBox proxyAuthEnabledCheckBox;
    
    private JLabel proxyUsernameLabel;
    private JLabel proxyPasswordLabel;
    private JLabel proxyPortLabel;
    private JLabel proxyServerLabel;

    public SettingsDialog(Ide ide) throws HeadlessException {
        super(ide, "Settings", true);
        this.ide = ide;
        this.setResizable(false);

        createGui();
    }

    private void createGui() {
        Settings settings = ide.getSettings();

        Container contentPane = this.getContentPane();

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder( new EtchedBorder() );

        contentPane.setLayout( new BorderLayout() );

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2, 5, 2, 5);

        workingPathField = new JTextField( settings.getWorkingPath() );

        proxyServerField = new JTextField( settings.getProxyServer() );
        proxyPortField = new JTextField( settings.getProxyPort() > 0 ? "" + settings.getProxyPort() : "" );
        proxyUsernameField = new JTextField( settings.getProxyUserename() );
        proxyPasswordField = new JTextField( settings.getProxyPassword() );

        proxyEnabledCheckBox = new JCheckBox("Proxy server enabled", settings.isProxyEnabled());
        proxyEnabledCheckBox.addChangeListener(this);
        proxyAuthEnabledCheckBox = new JCheckBox("Proxy authentication enabled", settings.isProxyAuthEnabled());
        proxyAuthEnabledCheckBox.addChangeListener(this);

        constraints.ipadx = 150;
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add( new JLabel("Output path"), constraints );

        constraints.ipadx = 150;
        constraints.gridx = 1;
        constraints.gridy = 0;
        panel.add(workingPathField, constraints );

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0.0;
        constraints.gridwidth = 2;
        panel.add(proxyEnabledCheckBox , constraints );

        constraints.ipadx = 150;
        constraints.ipadx = 200;
        constraints.gridx = 0;
        constraints.gridy = 2;
        proxyServerLabel = new JLabel("Proxy server");
        panel.add(proxyServerLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 2;
        panel.add(proxyServerField, constraints );

        constraints.gridx = 0;
        constraints.gridy = 3;
        proxyPortLabel = new JLabel("Proxy port (blank is default)");
        panel.add(proxyPortLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 3;
        panel.add(proxyPortField, constraints );

        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.weightx = 0.0;
        constraints.gridwidth = 2;
        panel.add(proxyAuthEnabledCheckBox , constraints );

        constraints.gridx = 0;
        constraints.gridy = 5;
        proxyUsernameLabel = new JLabel("Proxy username");
        panel.add(proxyUsernameLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 5;
        panel.add(proxyUsernameField, constraints );

        constraints.gridx = 0;
        constraints.gridy = 6;
        proxyPasswordLabel = new JLabel("Proxy password");
        panel.add(proxyPasswordLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 6;
        panel.add(proxyPasswordField, constraints );

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(new Insets(4, 4, 4, 4)));
        mainPanel.add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel( new FlowLayout(FlowLayout.CENTER) );

        JButton okButton = new JButton("  Ok  ");
        buttonPanel.add(okButton);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                define();
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

        updateControls();

        this.pack();
    }

    private void define() {
        Settings settings = this.ide.getSettings();

        settings.setWorkingPath( this.workingPathField.getText() );
        settings.setProxyServer( this.proxyServerField.getText() );

        int port = -1;
        try {
            Integer.parseInt( this.proxyPortField.getText() );
        } catch (NumberFormatException e) {
        }
        settings.setProxyPort(port);

        settings.setProxyUserename( this.proxyUsernameField.getText() );
        settings.setProxyPassword( this.proxyPasswordField.getText() );

        settings.setProxyEnabled( this.proxyEnabledCheckBox.isSelected() );
        settings.setProxyAuthEnabled( this.proxyAuthEnabledCheckBox.isSelected() );

        updateControls();

        setVisible(false);
    }

    /**
     * Enable/disable controls depending on setting values.
     */
    private void updateControls() {
        boolean isProxyEnabled = this.proxyEnabledCheckBox.isSelected();
        boolean isProxyAuthEnabled = this.proxyAuthEnabledCheckBox.isSelected();

        this.proxyServerLabel.setEnabled(isProxyEnabled);
        this.proxyServerField.setEnabled(isProxyEnabled);
        this.proxyPortLabel.setEnabled(isProxyEnabled);
        this.proxyPortField.setEnabled(isProxyEnabled);

        this.proxyAuthEnabledCheckBox.setEnabled(isProxyEnabled);

        this.proxyUsernameLabel.setEnabled( isProxyEnabled && isProxyAuthEnabled );
        this.proxyUsernameField.setEnabled( isProxyEnabled && isProxyAuthEnabled );
        this.proxyPasswordLabel.setEnabled( isProxyEnabled && isProxyAuthEnabled );
        this.proxyPasswordField.setEnabled( isProxyEnabled && isProxyAuthEnabled );
    }

    public void stateChanged(ChangeEvent e) {
        updateControls();
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
                define();
            }
        };
        rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);


        return rootPane;
    }
    
}