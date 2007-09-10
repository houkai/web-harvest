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
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author: Vladimir Nikic
 * Date: Apr 25, 2007
 */
public class WelcomePanel extends JPanel {

    private static final Color LINK_COLOR = new Color(47, 67, 96);

    private static final Color BG_COLOR = new Color(210, 213, 226);

    private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);

    /**
     * Simple Command interface that is used intenally for links actions.
     */

    private interface WelcomeCommand {

        public void execute();
    }

    /**
     * Command for loading configuraion from URL.
     */
    private class OpenConfigFromUrlCommand implements WelcomeCommand {
        private String url;

        public OpenConfigFromUrlCommand(String url) {
            this.url = url;
        }

        public void execute() {
            ide.openConfigFromUrl(this.url);
        }
    }

    // parent IDE
    private Ide ide;

    /**
     * Constructor.
     * @param ide
     */
    public WelcomePanel(final Ide ide) {
        this.ide = ide;
        this.setLayout( new BoxLayout(this, BoxLayout.PAGE_AXIS) );

        this.setBorder( new EmptyBorder(10, 10, 10, 10) );
        this.setOpaque(true);
        
        this.setBackground( BG_COLOR );

        JLabel titleLabel = new JLabel("Welcome to the Web-Harvest", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Verdana", Font.PLAIN, 24));
        this.add(titleLabel);

        JLabel subtitleLabel = new JLabel("version 1.0", SwingConstants.LEFT);
        subtitleLabel.setFont(new Font("Verdana", Font.PLAIN, 12));
        this.add(subtitleLabel);

        this.add(Box.createRigidArea( new Dimension(1, 30)) );

        JLabel quickStartLabel = new JLabel("QUICK START", SwingConstants.LEFT);
        quickStartLabel.setFont(new Font("Verdana", Font.BOLD, 14));
        quickStartLabel.setForeground(LINK_COLOR);
        this.add(quickStartLabel);

        this.add(Box.createRigidArea( new Dimension(1, 15)) );

        WelcomeCommand newCommand = new WelcomeCommand() {
            public void execute() {
                ide.addTab();
            }
        };

        WelcomeCommand openCommand = new WelcomeCommand() {
            public void execute() {
                ide.openConfigFromFile();
            }
        };

        WelcomeCommand settingsCommand = new WelcomeCommand() {
            public void execute() {
                ide.defineSettings();
            }
        };

        this.add( getLinkLabel("Create new configuration file", ResourceManager.getNewIcon(), newCommand) );
        this.add(Box.createRigidArea( new Dimension(1, 5)) );
        this.add( getLinkLabel("Open configuration file", ResourceManager.getOpenIcon(), openCommand) );
        this.add(Box.createRigidArea( new Dimension(1, 5)) );
        this.add( getLinkLabel("Modify execution settings", ResourceManager.getSettingsIcon(), settingsCommand) );

        this.add(Box.createRigidArea( new Dimension(1, 15)) );

        this.add( getExampleLabel("Download and open example #1 - Bookmaker odds at expekt.com", new OpenConfigFromUrlCommand("http://web-harvest.sourceforge.net/examples/expekt.xml")) );
        this.add(Box.createRigidArea( new Dimension(1, 5)) );
        this.add( getExampleLabel("Download and open example #2 - Canon products at Yahoo Shopping", new OpenConfigFromUrlCommand("http://web-harvest.sourceforge.net/examples/canon.xml")) );
        this.add(Box.createRigidArea( new Dimension(1, 5)) );
        this.add( getExampleLabel("Download and open example #3 - Google images", new OpenConfigFromUrlCommand("http://web-harvest.sourceforge.net/examples/google_images.xml")) );
        this.add(Box.createRigidArea( new Dimension(1, 5)) );
        this.add( getExampleLabel("Download and open example #4 - The New York Times newspaper articles", new OpenConfigFromUrlCommand("http://web-harvest.sourceforge.net/examples/nytimes.xml")) );
        this.add(Box.createRigidArea( new Dimension(1, 5)) );
        this.add( getExampleLabel("Download and open example #5 - XQuery use in the Web-Harvest", new OpenConfigFromUrlCommand("http://web-harvest.sourceforge.net/examples/xquery.xml")) );
        this.add(Box.createRigidArea( new Dimension(1, 5)) );
        this.add( getExampleLabel("Download and open example #6 - Simple web site crawler", new OpenConfigFromUrlCommand("http://web-harvest.sourceforge.net/examples/crawler.xml")) );
    }

    private JLabel getLinkLabel(final String text, final Icon icon, final WelcomeCommand command) {
        JLabel label = new JLabel(text);

        label.setIcon(icon);
        label.setForeground(LINK_COLOR);
        label.setFont(new Font("Verdana", Font.BOLD, 11));
        label.setCursor(HAND_CURSOR);

        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (command != null) {
                    command.execute();
                }
            }
        });
        
        return label;
    }

    private JLabel getExampleLabel(final String text, WelcomeCommand command) {
        JLabel label = getLinkLabel(text, ResourceManager.getDownloadIcon(), command);
        return label;
    }

}
