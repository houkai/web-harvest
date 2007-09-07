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

/**
 * @author: Vladimir Nikic
 * Date: Apr 17, 2007
 */

import org.webharvest.runtime.Scraper;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Ide extends JFrame implements ActionListener, ChangeListener {

    private static final String COMMAND_NEW = "new";
    private static final String COMMAND_OPEN = "open";
    private static final String COMMAND_SAVE = "save";
    private static final String COMMAND_SAVEAS = "saveas";
    private static final String COMMAND_CLOSE = "close";
    private static final String COMMAND_CLOSE_ALL = "closeall";
    private static final String COMMAND_REFRESH = "refresh";
    private static final String COMMAND_UNDO = "undo";
    private static final String COMMAND_REDO = "redo";
    private static final String COMMAND_CUT = "cut";
    private static final String COMMAND_COPY = "copy";
    private static final String COMMAND_PASTE = "paste";
    private static final String COMMAND_NEXTTAB = "nexttab";
    private static final String COMMAND_PREVTAB = "prevtab";
    private static final String COMMAND_FIND = "find";
    private static final String COMMAND_REPLACE = "replace";
    private static final String COMMAND_FINDNEXT = "findnext";
    private static final String COMMAND_FINDPREV = "findprev";
    private static final String COMMAND_VIEW_HIERARCHY = "viewhierarchy";
    private static final String COMMAND_VIEW_LOG = "viewlog";
    private static final String COMMAND_VIEW_LINENUMBERS = "viewlinenumbers";
    private static final String COMMAND_RUN = "run";
    private static final String COMMAND_PAUSE = "pause";
    private static final String COMMAND_STOP = "stop";
    private static final String COMMAND_EXIT = "exit";
    private static final String COMMAND_SETTINGS = "settings";
    private static final String COMMAND_ABOUT = "about";
    private static final String COMMAND_HOMEPAGE = "homepage";
    private static final String COMMAND_UNDERDEVELOPMENT = "underdevelopment";

    {
        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Couldn't use system look and feel.");
        }
    }


    // map of sets, each containing common GUI components connected with the same command
    private Map commandSets = new HashMap();

    private JTabbedPane tabbedPane;
    private StatusBar statusBar;
    private int configCounter = 0;

    // settings dialog box
    private SettingsDialog settingsDialog;

    // find/replace dialog box
    private FindReplaceDialog findReplaceDialog;

    // working settings
    Settings settings = new Settings();

    // popup menu for XML editor panes
    private JPopupMenu editorPopupMenu;

    /**
     * Constructor.
     */
    public Ide() {
        super("Web-Harvest");

        DialogHelper.init(this);

        this.addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });

        this.settingsDialog = new SettingsDialog(this);
        this.findReplaceDialog = new FindReplaceDialog(this);
    }

    /**
     * Offers a user to save modified documents.
     * @return True if unless user canceled operation.
     */
    private void exitApplication() {
        int count = this.tabbedPane.getTabCount();
        for (int i = 0; i < count; i++) {
            Component component = this.tabbedPane.getComponentAt(i);
            if (component instanceof ConfigPanel) {
                ConfigPanel currenConfigPanel = (ConfigPanel) component;

                ConfigDocument configDocument = currenConfigPanel.getConfigDocument();
                if (configDocument != null) {
                    boolean canceled = false;

                    int status = currenConfigPanel.getScraperStatus();
                    if (status == Scraper.STATUS_RUNNING) {
                        canceled = !DialogHelper.showYesNoConfirmWarning("Configuration \"" + configDocument.getName() + "\" is still running!\nAre you sure you want to exit Web-Harvest?");
                    }

                    if (!canceled) {
                        canceled = !configDocument.offerToSaveIfChanged();
                    }

                    if (canceled) {
                        return;
                    }
                }
            }
        }

        this.dispose();
        System.exit(0);
    }

    /**
     * Closes specified tab.
     * @param tabIndex
     * @return
     */
    private boolean closeTab(int tabIndex) {
        Component component = this.tabbedPane.getComponentAt(tabIndex);
        if (component instanceof ConfigPanel) {
            ConfigPanel currenConfigPanel = (ConfigPanel) component;
            ConfigDocument configDocument = currenConfigPanel.getConfigDocument();
            if (configDocument != null) {
                boolean canceled = false;
                
                int status = currenConfigPanel.getScraperStatus();
                if (status == Scraper.STATUS_RUNNING) {
                    canceled = !DialogHelper.showYesNoConfirmWarning("Configuration \"" + configDocument.getName() + "\" is still running!\nAre you sure you want to exit Web-Harvest?");
                    if (!canceled) {
                        currenConfigPanel.stopScraperExecution();
                    }
                }

                if (!canceled) {
                    canceled = !configDocument.offerToSaveIfChanged();
                }

                if (canceled) {
                    return false;
                }
            }
        }

        this.tabbedPane.remove(tabIndex);
        
        if ( this.tabbedPane.getTabCount() == 0 ) {
            openWelcomeScreen();
        }

        return true;
    }

    /**
     * Closes specified tab.
     * @return
     */
    private void closeAllTabs() {
        while ( tabbedPane.getComponentAt(tabbedPane.getTabCount() - 1) instanceof ConfigPanel ) {
            boolean canceled = !closeTab( tabbedPane.getTabCount() - 1 );
            if (canceled) {
                return;
            }
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(800, 600);
    }

    private void defineToolbarButton(String text, String command, Icon icon, JToolBar toolBar) {
        JButton button = new JButton(icon);
        button.setActionCommand(command);
        button.addActionListener(this);
        button.setToolTipText(text);
        toolBar.add(button);

        addComponentForCommand(button, command);
    }

    /**
     * Adds specified GUI component to the set of all components executing the same command.
     * @param component
     * @param command
     */
    private void addComponentForCommand(Component component, String command) {
        if (component == null || command == null) {
            return;
        }

        Set set = (Set) this.commandSets.get(command);
        if (set == null) {
            set = new HashSet();
            this.commandSets.put(command, set);
        }

        set.add(component);
    }

    /**
     * Enables/disables all GUI components that execute the same command
     * @param command
     * @param enable
     */
    private void setCommandEnabled(String command, boolean enable) {
        Set set = (Set) this.commandSets.get(command);
        if (set != null) {
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                Component component = (Component) iterator.next();
                component.setEnabled(enable);
            }
        }
    }

    /**
     * Selects/unselects all GUI components that execute the same command and are selectable
     * @param command
     * @param select
     */
    private void setCommandSelected(String command, boolean select) {
        Set set = (Set) this.commandSets.get(command);
        if (set != null) {
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                Component component = (Component) iterator.next();
                if (component instanceof JCheckBoxMenuItem) {
                    ((JCheckBoxMenuItem)component).setSelected(select);
                }
            }
        }
    }

    public void createAndShowGUI() {
        this.setJMenuBar( defineMenuBar() );
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setIconImage( ((ImageIcon) ResourceManager.getWebHarvestIcon()).getImage() );

        JPanel mainPanel = new JPanel( new BorderLayout() );
        mainPanel.setOpaque(true);

        // define toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorderPainted(true);

        defineToolbarButton("New configuration file", COMMAND_NEW, ResourceManager.getNewIcon(), toolBar);
        defineToolbarButton("Open configuration file", COMMAND_OPEN, ResourceManager.getOpenIcon(), toolBar);
        defineToolbarButton("Save configuration file", COMMAND_SAVE, ResourceManager.getSaveIcon(), toolBar);
        defineToolbarButton("Synchronize tree view with XML editor", COMMAND_REFRESH, ResourceManager.getRefreshIcon(), toolBar);
        toolBar.addSeparator(new Dimension(10, 0));
        defineToolbarButton("Run", COMMAND_RUN, ResourceManager.getRunIcon(), toolBar);
        defineToolbarButton("Pause execution", COMMAND_PAUSE, ResourceManager.getPauseIcon(), toolBar);
        defineToolbarButton("Stop execution", COMMAND_STOP, ResourceManager.getStopIcon(), toolBar);
        toolBar.addSeparator(new Dimension(10, 0));
        defineToolbarButton("Open Settings Dialog", COMMAND_SETTINGS, ResourceManager.getSettingsIcon(), toolBar);

        mainPanel.add(toolBar, BorderLayout.NORTH);

        this.tabbedPane = new JTabbedPane();
        this.tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
//        this.tabbedPane.setComponentPopupMenu( defineTabContextMenu() );
        this.tabbedPane.addChangeListener(this);
        final JPopupMenu tabContextMenu = defineTabContextMenu();
        this.tabbedPane.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                super.mouseClicked(e);
                if ( e.isPopupTrigger() ) {
                    tabContextMenu.show( e.getComponent(), e.getX(), e.getY() );
                } else if ( e.getButton() == MouseEvent.BUTTON2 ) {
                    closeTab( tabbedPane.getSelectedIndex() );
                }
            }

            public void mouseReleased(MouseEvent e) {
                if ( e.isPopupTrigger() ) {
                    tabContextMenu.show( e.getComponent(), e.getX(), e.getY() );
                }
            }
        });

        // by default, open welcome screen at startup
        openWelcomeScreen();

        //Add the split pane to this panel.
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        this.setContentPane(mainPanel);

        this.pack();
        updateGUI();
        this.setVisible(true);

//        this.statusBar = new StatusBar();
//        this.statusBar.setBorder( new BevelBorder(BevelBorder.LOWERED) );
//        add(this.statusBar, BorderLayout.SOUTH);
    }

    private void openWelcomeScreen() {
        WelcomePanel welcomePanel = new WelcomePanel(this);
        tabbedPane.addTab( "Welcome", new JScrollPane(welcomePanel) );
        tabbedPane.setSelectedIndex( tabbedPane.getTabCount() - 1 );
    }

    public void addTab() {
        configCounter++;
        String tabName = "Config " + configCounter;
        ConfigPanel configPanel = new ConfigPanel(this, tabName);
        tabbedPane.addTab(tabName, configPanel);
        tabbedPane.setSelectedIndex( tabbedPane.getTabCount() - 1 );
    }

    public void addTab(final Object source) {
        final ConfigPanel configPanel = new ConfigPanel(this, "");
        tabbedPane.addTab("Loading...", configPanel);
        tabbedPane.setSelectedIndex( tabbedPane.getTabCount() - 1 );
        new Thread(new Runnable() {
            public void run() {
                configPanel.loadConfig(source);
            }
        }).start();
    }

    public void openConfigFromFile() {
        JFileChooser fileChooser = DialogHelper.getFileChooser();
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File files[] = fileChooser.getSelectedFiles();
            for (int i = 0; i < files.length; i++) {
                addTab(files[i]);
            }
        }
    }

    public void openConfigFromUrl(String url) {
        try {
            addTab(new URL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void defineSettings() {
        this.settingsDialog.setLocationRelativeTo(this);
        this.settingsDialog.setVisible(true);
    }

    /**
     * Defines single menu item for the specified menu with specified parameters.
     * @param menu
     * @param text
     * @param icon
     * @param mnemonic
     * @param command
     * @param keyStroke
     */
    private void defineMenuItem(JMenu menu, String text, Icon icon, int mnemonic, String command, KeyStroke keyStroke) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.setIcon(icon == null ? ResourceManager.getNoneIcon() : icon);
        menuItem.setMnemonic(mnemonic);
        menuItem.setAccelerator(keyStroke);
        menuItem.setActionCommand(command);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        addComponentForCommand(menuItem, command);
    }

    /**
     * Defines single checkbox menu item for the specified menu with specified parameters.
     * @param menu
     * @param text
     * @param icon
     * @param mnemonic
     * @param command
     * @param keyStroke
     */
    private void defineCheckboxMenuItem(JMenu menu, String text, Icon icon, int mnemonic, String command, KeyStroke keyStroke, boolean isChecked) {
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(text);
        menuItem.setSelected(isChecked);
        menuItem.setIcon(icon == null ? ResourceManager.getNoneIcon() : icon);
        menuItem.setMnemonic(mnemonic);
        menuItem.setAccelerator(keyStroke);
        menuItem.setActionCommand(command);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        addComponentForCommand(menuItem, command);
    }

    /**
     * Defines single menu item for the specified popup menu with specified parameters.
     * @param menu
     * @param text
     * @param icon
     * @param mnemonic
     * @param command
     * @param keyStroke
     */
    private void definePopupMenuItem(JPopupMenu menu, String text, Icon icon, int mnemonic, String command, KeyStroke keyStroke) {
        JMenuItem menuItem = new JMenuItem(text);
        if (icon != null) {
            menuItem.setIcon(icon);
        }
        menuItem.setMnemonic(mnemonic);
        menuItem.setAccelerator(keyStroke);
        menuItem.setActionCommand(command);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        addComponentForCommand(menuItem, command);
    }

    /**
     * Defines menu bar. 
     * @return JMEnuBar instance.
     */
    private JMenuBar defineMenuBar() {
        JMenuBar menuBar;
        JMenu menu;

        // Create the menu bar.
        menuBar = new JMenuBar();

        // Build the CONFIGURATION menu.
        menu = new JMenu("Config");
        defineMenuItem(menu, "New", ResourceManager.getNewIcon(), KeyEvent.VK_N, COMMAND_NEW, KeyStroke.getKeyStroke( KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        defineMenuItem(menu, "Open", ResourceManager.getOpenIcon(), KeyEvent.VK_O, COMMAND_OPEN, KeyStroke.getKeyStroke( KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        defineMenuItem(menu, "Save", ResourceManager.getSaveIcon(),  KeyEvent.VK_S, COMMAND_SAVE, KeyStroke.getKeyStroke( KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        defineMenuItem(menu, "Save As", null, KeyEvent.VK_V, COMMAND_SAVEAS, null);
        menu.addSeparator();
        defineMenuItem(menu, "Close", ResourceManager.getCloseIcon(), KeyEvent.VK_C, COMMAND_CLOSE, KeyStroke.getKeyStroke( KeyEvent.VK_F4, ActionEvent.CTRL_MASK));
        defineMenuItem(menu, "Close All", null, KeyEvent.VK_A, COMMAND_CLOSE_ALL, null);
        menu.addSeparator();
        defineMenuItem(menu, "Exit", null, KeyEvent.VK_X, COMMAND_EXIT, null);
        menuBar.add(menu);

        // Build the EDIT menu.
        menu = new JMenu("Edit");
        defineMenuItem(menu, "Undo", ResourceManager.getUndoIcon(), KeyEvent.VK_U, COMMAND_UNDO, KeyStroke.getKeyStroke( KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        defineMenuItem(menu, "Redo", ResourceManager.getRedoIcon(), KeyEvent.VK_R, COMMAND_REDO, KeyStroke.getKeyStroke( KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        menu.addSeparator();
        defineMenuItem(menu, "Find", ResourceManager.getFindIcon(), KeyEvent.VK_F, COMMAND_FIND, KeyStroke.getKeyStroke( KeyEvent.VK_F, ActionEvent.CTRL_MASK));
        defineMenuItem(menu, "Replace", null, KeyEvent.VK_L, COMMAND_REPLACE, KeyStroke.getKeyStroke( KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        defineMenuItem(menu, "Find Next", null, KeyEvent.VK_N, COMMAND_FINDNEXT, KeyStroke.getKeyStroke( KeyEvent.VK_F3, 0));
        defineMenuItem(menu, "Find Previous", null, KeyEvent.VK_V, COMMAND_FINDPREV, KeyStroke.getKeyStroke( KeyEvent.VK_F3, ActionEvent.SHIFT_MASK));
        menu.addSeparator();
        defineMenuItem(menu, "Cut", ResourceManager.getCutIcon(), KeyEvent.VK_U, COMMAND_CUT, KeyStroke.getKeyStroke( KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        defineMenuItem(menu, "Copy", ResourceManager.getCopyIcon(), KeyEvent.VK_C, COMMAND_COPY, KeyStroke.getKeyStroke( KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        defineMenuItem(menu, "Paste", ResourceManager.getPasteIcon(), KeyEvent.VK_P, COMMAND_PASTE, KeyStroke.getKeyStroke( KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        menu.addSeparator();
        defineMenuItem(menu, "Next Tab", null, KeyEvent.VK_E, COMMAND_NEXTTAB, KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK | ActionEvent.CTRL_MASK));
        defineMenuItem(menu, "Previous Tab", null, KeyEvent.VK_P, COMMAND_PREVTAB, KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, ActionEvent.ALT_MASK | ActionEvent.CTRL_MASK));
        menuBar.add(menu);

        // Build the editor popup menu.
        editorPopupMenu = new JPopupMenu();
        definePopupMenuItem(editorPopupMenu, "Undo", ResourceManager.getUndoIcon(), KeyEvent.VK_U, COMMAND_UNDO, KeyStroke.getKeyStroke( KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        definePopupMenuItem(editorPopupMenu, "Redo", ResourceManager.getRedoIcon(), KeyEvent.VK_R, COMMAND_REDO, KeyStroke.getKeyStroke( KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        editorPopupMenu.addSeparator();
        definePopupMenuItem(editorPopupMenu, "Find", ResourceManager.getFindIcon(), KeyEvent.VK_F, COMMAND_FIND, KeyStroke.getKeyStroke( KeyEvent.VK_F, ActionEvent.CTRL_MASK));
        definePopupMenuItem(editorPopupMenu, "Replace", null, KeyEvent.VK_L, COMMAND_REPLACE, KeyStroke.getKeyStroke( KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        definePopupMenuItem(editorPopupMenu, "Find Next", null, KeyEvent.VK_N, COMMAND_FINDNEXT, KeyStroke.getKeyStroke( KeyEvent.VK_F3, 0));
        definePopupMenuItem(editorPopupMenu, "Find Previous", null, KeyEvent.VK_V, COMMAND_FINDPREV, KeyStroke.getKeyStroke( KeyEvent.VK_F3, ActionEvent.SHIFT_MASK));
        editorPopupMenu.addSeparator();
        definePopupMenuItem(editorPopupMenu, "Cut", ResourceManager.getCutIcon(), KeyEvent.VK_U, COMMAND_CUT, KeyStroke.getKeyStroke( KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        definePopupMenuItem(editorPopupMenu, "Copy", ResourceManager.getCopyIcon(), KeyEvent.VK_C, COMMAND_COPY, KeyStroke.getKeyStroke( KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        definePopupMenuItem(editorPopupMenu, "Paste", ResourceManager.getPasteIcon(), KeyEvent.VK_P, COMMAND_PASTE, KeyStroke.getKeyStroke( KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        menuBar.add(editorPopupMenu);

        // Build the VIEW menu.
        menu = new JMenu("View");
        defineMenuItem(menu, "Synchronize tree", ResourceManager.getRefreshIcon(), KeyEvent.VK_R, COMMAND_REFRESH, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        menu.addSeparator();
        defineCheckboxMenuItem(menu, "Hierarchy", null, KeyEvent.VK_H, COMMAND_VIEW_HIERARCHY, null, false);
        defineCheckboxMenuItem(menu, "Log", null, KeyEvent.VK_L, COMMAND_VIEW_LOG, null, false);
        menu.addSeparator();
        defineCheckboxMenuItem(menu, "Line numbers", null, KeyEvent.VK_N, COMMAND_VIEW_LINENUMBERS, null, false);
        menuBar.add(menu);

        // Build the EXECUTION menu.
        menu = new JMenu("Execution");
        defineMenuItem(menu, "Run", ResourceManager.getRunIcon(), KeyEvent.VK_R, COMMAND_RUN, KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        defineMenuItem(menu, "Pause", ResourceManager.getPauseIcon(), KeyEvent.VK_R, COMMAND_PAUSE, null);
        defineMenuItem(menu, "Stop", ResourceManager.getStopIcon(), KeyEvent.VK_S, COMMAND_STOP, null);
        menu.addSeparator();
        defineMenuItem(menu, "Settings...", ResourceManager.getSettingsIcon(), KeyEvent.VK_T, COMMAND_SETTINGS, null);
        menuBar.add(menu);

        // Build the HELP menu.
        menu = new JMenu("Help");
        defineMenuItem(menu, "Help", ResourceManager.getHelpIcon(), KeyEvent.VK_H, COMMAND_UNDERDEVELOPMENT, null);
        menu.addSeparator();
        defineMenuItem(menu, "Program Homepage", ResourceManager.getHomepageIcon(), KeyEvent.VK_H, COMMAND_HOMEPAGE, null);
        menu.addSeparator();
        defineMenuItem(menu, "About Web-Harvest", null, KeyEvent.VK_A, COMMAND_ABOUT, null);
        menuBar.add(menu);

        return menuBar;
    }

    /**
     * Defines context menu for tabbed pane.
     */
    private JPopupMenu defineTabContextMenu() {
        JPopupMenu menu = new JPopupMenu();

        definePopupMenuItem(menu, "New", null, KeyEvent.VK_N, COMMAND_NEW, null);
        menu.addSeparator();
        definePopupMenuItem(menu, "Close", null, KeyEvent.VK_C, COMMAND_CLOSE, null);
        definePopupMenuItem(menu, "Close All", null, KeyEvent.VK_A, COMMAND_CLOSE_ALL, null);

        return menu;
    }

    public void updateGUI() {
        ConfigPanel configPanel = getActiveConfigPanel();

        setCommandEnabled(COMMAND_SAVE, configPanel != null); 
        setCommandEnabled(COMMAND_SAVEAS, configPanel != null); 
        setCommandEnabled(COMMAND_REFRESH, configPanel != null &&
                                           configPanel.getScraperStatus() != Scraper.STATUS_RUNNING &&
                                           configPanel.getScraperStatus() != Scraper.STATUS_PAUSED);
        setCommandEnabled(COMMAND_RUN, configPanel != null && configPanel.getScraperStatus() != Scraper.STATUS_RUNNING);
        setCommandEnabled(COMMAND_PAUSE, configPanel != null && configPanel.getScraperStatus() == Scraper.STATUS_RUNNING);
        setCommandEnabled(COMMAND_STOP, configPanel != null && configPanel.getScraperStatus() == Scraper.STATUS_RUNNING);

        setCommandEnabled(COMMAND_UNDO, configPanel != null);
        setCommandEnabled(COMMAND_REDO, configPanel != null); 

        boolean hasSelection = configPanel != null && configPanel.getXmlPane().hasSelection();

        setCommandEnabled(COMMAND_CUT, hasSelection);
        setCommandEnabled(COMMAND_COPY, hasSelection);
        setCommandEnabled(COMMAND_PASTE, configPanel != null);

        int tabCount = tabbedPane.getTabCount();
        setCommandEnabled(COMMAND_NEXTTAB, tabCount > 1);
        setCommandEnabled(COMMAND_PREVTAB, tabbedPane.getTabCount() > 1); 

        String textToFind = findReplaceDialog.getSearchText();
        setCommandEnabled(COMMAND_FIND, configPanel != null);
        setCommandEnabled(COMMAND_REPLACE, configPanel != null);
        setCommandEnabled(COMMAND_FINDNEXT, configPanel != null && textToFind != null && !"".equals(textToFind));
        setCommandEnabled(COMMAND_FINDPREV, configPanel != null && textToFind != null && !"".equals(textToFind));

        setCommandEnabled(COMMAND_VIEW_HIERARCHY, configPanel != null);
        setCommandSelected(COMMAND_VIEW_HIERARCHY, configPanel != null && configPanel.isHierarchyVisible());
        setCommandEnabled(COMMAND_VIEW_LOG, configPanel != null);
        setCommandSelected(COMMAND_VIEW_LOG, configPanel != null && configPanel.isLogVisible());
        setCommandEnabled(COMMAND_VIEW_LOG, configPanel != null);
        setCommandSelected(COMMAND_VIEW_LINENUMBERS, configPanel != null && configPanel.getXmlEditorPanel().isShowLineNumbers());
        setCommandEnabled(COMMAND_VIEW_LINENUMBERS, configPanel != null);
    }

    private ConfigPanel getActiveConfigPanel() {
        Component component = this.tabbedPane.getSelectedComponent();
        return component instanceof ConfigPanel ? (ConfigPanel)component : null;
    }

    public JPopupMenu getEditorPopupMenu() {
        return editorPopupMenu;
    }

    private ConfigDocument getActiveConfigDocument() {
        ConfigPanel activeConfigPanel = getActiveConfigPanel();
        return activeConfigPanel != null ? activeConfigPanel.getConfigDocument() : null;
    }

    int findTabIndex(ConfigPanel configPanel) {
        int tabCount = this.tabbedPane.getTabCount();
        for (int i = 0; i < tabCount; i++) {
            if ( configPanel == this.tabbedPane.getComponentAt(i) ) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Sets specified icon for the specified configuration panel.
     * @param configPanel
     * @param icon
     */
    public void setTabIcon(ConfigPanel configPanel, Icon icon) {
        int index = findTabIndex(configPanel);
        if ( index >= 0 && index < this.tabbedPane.getTabCount() ) {
            this.tabbedPane.setIconAt(index, icon);
        }
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if ( COMMAND_NEW.equals(cmd) ) {
            addTab();
            grabFocusToActiveEditor();
        } if ( COMMAND_OPEN.equals(cmd) ) {
            openConfigFromFile();
            grabFocusToActiveEditor();
        } if ( COMMAND_SAVE.equals(cmd) ) {
            ConfigDocument configDocument = getActiveConfigDocument();
            if (configDocument != null) {
                configDocument.saveConfigToFile(false);
            }
        } if ( COMMAND_SAVEAS.equals(cmd) ) {
            ConfigDocument configDocument = getActiveConfigDocument();
            if (configDocument != null) {
                configDocument.saveConfigToFile(true);
            }
        } if ( COMMAND_CLOSE.equals(cmd) ) {
            closeTab( this.tabbedPane.getSelectedIndex() );
        } if ( COMMAND_CLOSE_ALL.equals(cmd) ) {
            closeAllTabs();
        } else if ( COMMAND_REFRESH.equals(cmd) ) {
            ConfigPanel activeConfigPanel = getActiveConfigPanel();
            if (activeConfigPanel != null) {
                activeConfigPanel.refreshTree();
            }
        } else if ( COMMAND_RUN.equals(cmd) ) {
            ConfigPanel activeConfigPanel = getActiveConfigPanel();
            if (activeConfigPanel != null) {
                activeConfigPanel.runConfiguration();
            }
        } else if ( COMMAND_PAUSE.equals(cmd) ) {
            ConfigPanel activeConfigPanel = getActiveConfigPanel();
            if (activeConfigPanel != null && activeConfigPanel.getScraperStatus() == Scraper.STATUS_RUNNING) {
                activeConfigPanel.pauseScraperExecution();
            }
        } else if ( COMMAND_STOP.equals(cmd) ) {
            ConfigPanel activeConfigPanel = getActiveConfigPanel();
            if (activeConfigPanel != null) {
                activeConfigPanel.stopScraperExecution();
            }
        } else if ( COMMAND_SETTINGS.equals(cmd) ) {
            defineSettings();
        } else if ( COMMAND_ABOUT.equals(cmd) ) {
            JOptionPane.showMessageDialog(this, "Web-Harvest GUI, version 1.0 alpha, build 2", "Status", JOptionPane.INFORMATION_MESSAGE);
        } else if ( COMMAND_HOMEPAGE.equals(cmd) ) {
            openURLInBrowser("http://web-harvest.sourceforge.net");
        } else if ( COMMAND_EXIT.equals(cmd) ) {
            exitApplication();
        } else if ( COMMAND_UNDERDEVELOPMENT.equals(cmd) ) {
            JOptionPane.showMessageDialog(this, "Under development!", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else if ( COMMAND_UNDO.equals(cmd) ) {
            ConfigPanel activeConfigPanel = getActiveConfigPanel();
            if (activeConfigPanel != null) {
                activeConfigPanel.undo();
            }
        } else if ( COMMAND_REDO.equals(cmd) ) {
            ConfigPanel activeConfigPanel = getActiveConfigPanel();
            if (activeConfigPanel != null) {
                activeConfigPanel.redo();
            }
        } else if ( COMMAND_FIND.equals(cmd) ) {
            ConfigPanel activeConfigPanel = getActiveConfigPanel();
            if (activeConfigPanel != null) {
                this.findReplaceDialog.open( activeConfigPanel.getXmlPane(), false );
            }
        } else if ( COMMAND_REPLACE.equals(cmd) ) {
            ConfigPanel activeConfigPanel = getActiveConfigPanel();
            if (activeConfigPanel != null) {
                this.findReplaceDialog.open( activeConfigPanel.getXmlPane(), true );
            }
        } else if ( COMMAND_FINDNEXT.equals(cmd) ) {
            ConfigPanel activeConfigPanel = getActiveConfigPanel();
            if (activeConfigPanel != null) {
                this.findReplaceDialog.findNext( activeConfigPanel.getXmlPane() );
            }
        } else if ( COMMAND_FINDPREV.equals(cmd) ) {
            ConfigPanel activeConfigPanel = getActiveConfigPanel();
            if (activeConfigPanel != null) {
                this.findReplaceDialog.findPrev( activeConfigPanel.getXmlPane() );
            }
        } else if ( COMMAND_CUT.equals(cmd) ) {
            ConfigPanel activeConfigPanel = getActiveConfigPanel();
            if (activeConfigPanel != null) {
                activeConfigPanel.getXmlPane().cut();
            }
        } else if ( COMMAND_COPY.equals(cmd) ) {
            ConfigPanel activeConfigPanel = getActiveConfigPanel();
            if (activeConfigPanel != null) {
                activeConfigPanel.getXmlPane().copy();
            }
        } else if ( COMMAND_PASTE.equals(cmd) ) {
            ConfigPanel activeConfigPanel = getActiveConfigPanel();
            if (activeConfigPanel != null) {
                activeConfigPanel.getXmlPane().paste();
            }
        } else if ( COMMAND_NEXTTAB.equals(cmd) ) {
            int tabCount = tabbedPane.getTabCount();
            int selectedTab = tabbedPane.getSelectedIndex();
            tabbedPane.setSelectedIndex(selectedTab >= tabCount - 1 ? 0 : selectedTab + 1);
            grabFocusToActiveEditor();
        } else if ( COMMAND_PREVTAB.equals(cmd) ) {
            int tabCount = tabbedPane.getTabCount();
            int selectedTab = tabbedPane.getSelectedIndex();
            tabbedPane.setSelectedIndex(selectedTab > 0 ? selectedTab - 1 : tabCount - 1);
            grabFocusToActiveEditor();
        } else if ( COMMAND_VIEW_HIERARCHY.equals(cmd) ) {
            ConfigPanel activeConfigPanel = getActiveConfigPanel();
            if (activeConfigPanel != null) {
                activeConfigPanel.showHierarchy();
            }
        } else if ( COMMAND_VIEW_LOG.equals(cmd) ) {
            ConfigPanel activeConfigPanel = getActiveConfigPanel();
            if (activeConfigPanel != null) {
                activeConfigPanel.showLog();
            }
        } else if ( COMMAND_VIEW_LINENUMBERS.equals(cmd) ) {
            ConfigPanel activeConfigPanel = getActiveConfigPanel();
            if (activeConfigPanel != null) {
                activeConfigPanel.getXmlEditorPanel().toggleShowLineNumbers();
            }
        }
        
        this.updateGUI();
    }

    private void grabFocusToActiveEditor() {
        ConfigPanel activeConfigPanel = getActiveConfigPanel();
        if (activeConfigPanel != null) {
            activeConfigPanel.getXmlPane().grabFocus();
        }
    }

    /**
     * Implementation of state changed listener's method.
     * @param event
     */
    public void stateChanged(ChangeEvent event) {
        Object source = event.getSource();
        if (source == this.tabbedPane) {
            updateGUI(); 
        }
    }

    /**
     * Gets settings for this IDE.
     * @return Settings instance.
     */
    public Settings getSettings() {
        return settings;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    /**
     * Opens specified URL in default system's browser.
     * @param url
     */
    private void openURLInBrowser(String url) {
        String osName = System.getProperty("os.name");
        try {
            if (osName.startsWith("Mac OS")) {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
                openURL.invoke(null, new Object[]{url});
            } else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else { //assume Unix or Linux
                String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++) {
                    if ( Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0 ) {
                        browser = browsers[count];
                    }
                }
                if (browser == null) {
                    throw new Exception("Could not find web browser");
                } else {
                    Runtime.getRuntime().exec(new String[]{browser, url});
                }
            }
        } catch (Exception e) {
            DialogHelper.showErrorMessage( "Error attempting to launch web browser" + ":\n" + e.getLocalizedMessage() );
        }
    }

}