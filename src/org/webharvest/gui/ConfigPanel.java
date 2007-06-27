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
import org.webharvest.definition.BaseElementDef;
import org.webharvest.definition.ConstantDef;
import org.webharvest.definition.IElementDef;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperRuntimeListener;
import org.webharvest.runtime.processors.BaseProcessor;
import org.webharvest.runtime.web.HttpClientManager;
import org.webharvest.utils.Constants;
import org.xml.sax.InputSource;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Single panel containing XML configuration.
 * It is part of multiple-document interface where several such instances may exist at
 * the same time.
 */
public class ConfigPanel extends JPanel implements ScraperRuntimeListener, TreeSelectionListener {

    // basic skeletion for new opened configuration
    private static final String BASIC_CONFIG_SKELETION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n<config>\n\t\n</config>";

    // size of splitter pane dividers
    private static final int DIVIDER_SIZE = 3;
    private ScrollableEditorPanel xmlEditorPanel;

    // loger for this configuration panel
    private Logger logger;

    private class ViewerActionListener implements ActionListener {
        private int viewType = ViewerFrame.TEXT_VIEW;

        public ViewerActionListener(int viewType) {
            this.viewType = viewType;
        }

        public void actionPerformed(ActionEvent e) {
            DefaultMutableTreeNode treeNode;

            TreePath path = tree.getSelectionPath();
            if (path != null) {
                treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (treeNode != null) {
                    Object userObject = treeNode.getUserObject();
                    if (userObject instanceof TreeNodeInfo) {
                        TreeNodeInfo treeNodeInfo = (TreeNodeInfo) userObject;
                        Map properties = treeNodeInfo.getProperties();
                        Object value = properties == null ? null : properties.get(Constants.VALUE_PROPERTY_NAME);
                        final ViewerFrame viewerFrame = new ViewerFrame( Constants.VALUE_PROPERTY_NAME, value, treeNodeInfo, this.viewType );
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                viewerFrame.setVisible(true);
                                viewerFrame.toFront();
                            }
                        });
                    }
                }
            }
        }
    }

    private ConfigDocument configDocument;

    private Ide ide;

    private ScraperConfiguration scraperConfiguration;

    private DefaultMutableTreeNode topNode;
    private DefaultTreeModel treeModel;
    private TreeNodeInfo selectedNodeInfo;
    private JTextArea logTextArea;
    private Map nodeInfos = new Hashtable();
    private NodeRenderer nodeRenderer = new NodeRenderer();

    private JSplitPane bottomSplitter;
    private JSplitPane leftSplitter;
    private JSplitPane leftView;
    private JScrollPane bottomView;
    private int leftDividerLocation = 0;
    private int bottomDividerLocation = 0;
    
    private XmlTextPane xmlPane;
    private JTree tree;
    private Scraper scraper;
    private PropertiesGrid propertiesGrid;

    // tree popup menu items
    private JMenuItem textViewMenuItem;
    private JMenuItem xmlViewMenuItem;
    private JMenuItem htmlViewMenuItem;
    private JMenuItem imageViewMenuItem;
    private JMenuItem listViewMenuItem;

    /**
     * Constructor of the panel - initializes parent Ide instance and name of the document.
     * @param ide
     * @param name
     */
    public ConfigPanel(Ide ide, String name) {
        super(new BorderLayout());

        this.ide = ide;

        this.topNode = new DefaultMutableTreeNode();
        this.treeModel = new DefaultTreeModel(this.topNode);

        this.tree = new JTree(this.treeModel);
        this.tree.setRootVisible(false);
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        ToolTipManager.sharedInstance().registerComponent(this.tree);
        this.tree.setCellRenderer(this.nodeRenderer);
        tree.setShowsRootHandles(true);
        this.tree.addTreeSelectionListener(this);

        // defines pop menu for the tree
        final JPopupMenu treePopupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Locate in source");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                xmlEditorPanel.clearMarkers(ScrollableEditorPanel.DEFAULT_MARKER_TYPE);
                TreePath path = tree.getSelectionPath();
                if (path != null) {
                    int line = locateInSource( (DefaultMutableTreeNode) path.getLastPathComponent(), false );
                    xmlEditorPanel.addMarker( ScrollableEditorPanel.DEFAULT_MARKER_TYPE, line );
                }
            }
        });
        treePopupMenu.add(menuItem);

        treePopupMenu.addSeparator();

        textViewMenuItem = new JMenuItem("View result as text");
        textViewMenuItem.addActionListener( new ViewerActionListener(ViewerFrame.TEXT_VIEW) );
        treePopupMenu.add(textViewMenuItem);

        xmlViewMenuItem = new JMenuItem("View result as XML");
        xmlViewMenuItem.addActionListener( new ViewerActionListener(ViewerFrame.XML_VIEW) );
        treePopupMenu.add(xmlViewMenuItem);

        htmlViewMenuItem = new JMenuItem("View result as HTML");
        htmlViewMenuItem.addActionListener( new ViewerActionListener(ViewerFrame.HTML_VIEW) );
        treePopupMenu.add(htmlViewMenuItem);

        imageViewMenuItem = new JMenuItem("View result as image");
        imageViewMenuItem.addActionListener( new ViewerActionListener(ViewerFrame.IMAGE_VIEW) );
        treePopupMenu.add(imageViewMenuItem);

        listViewMenuItem = new JMenuItem("View result as list");
        listViewMenuItem.addActionListener( new ViewerActionListener(ViewerFrame.LIST_VIEW) );
        treePopupMenu.add(listViewMenuItem);

        treePopupMenu.setOpaque(true);
        treePopupMenu.setLightWeightPopupEnabled(true);

        this.tree.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if ( e.isPopupTrigger()) {
                    TreePath path = tree.getClosestPathForLocation( e.getX(), e.getY() );
                    if (path != null) {
                        tree.setSelectionPath(path);
                    }
                    treePopupMenu.show( (JComponent)e.getSource(), e.getX(), e.getY() );
                }
            }
        });

        JScrollPane treeView = new JScrollPane(this.tree);

        //Create the XML editor pane.
        this.xmlPane = new XmlTextPane();
        final AutoCompleter xmlPanePopupMenu = new AutoCompleter(this.xmlPane);
        this.xmlPane.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if ( e.getKeyCode() == KeyEvent.VK_SPACE ) {
                    if ( (e.getModifiers() & KeyEvent.CTRL_MASK) != 0 ) {
                        xmlPanePopupMenu.autoComplete();
                    }
                }
            }
        });

        // creates document for this configuration panel
        this.configDocument = new ConfigDocument(this, name);

        // initialize document content
        try {
            this.configDocument.load(BASIC_CONFIG_SKELETION);
        } catch (IOException e) {
            DialogHelper.showErrorMessage( e.getMessage() );
        }

        this.xmlEditorPanel = new ScrollableEditorPanel(this.xmlPane);
        JScrollPane xmlView = new JScrollPane(this.xmlEditorPanel);

        this.propertiesGrid = new PropertiesGrid();
        JScrollPane propertiesView = new JScrollPane(propertiesGrid);
        this.leftView = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.leftView.setTopComponent(treeView);
        this.leftView.setBottomComponent(propertiesView);
        this.leftView.setBorder(null);
        this.leftView.setDividerLocation(320);
        this.leftView.setDividerSize(DIVIDER_SIZE);

        //Add the scroll panes to a split pane.
        leftSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        leftSplitter.setBorder(null);
        leftSplitter.setLeftComponent(leftView);
        leftSplitter.setRightComponent(xmlView);
        leftSplitter.setDividerSize(DIVIDER_SIZE);

        leftSplitter.setDividerLocation(250);

//        JPanel bottomPanel = new JPanel( new BorderLayout() );
        logTextArea = new JTextArea();
        logTextArea.setFont( new Font("Courier New", Font.PLAIN, 11) );
        logTextArea.setEditable(false);

        this.logger = Logger.getLogger(this.toString() + System.currentTimeMillis());
        this.logger.addAppender( new TextAreaAppender(this.logTextArea) );

//        bottomPanel.add(logTextArea , BorderLayout.CENTER );

        bottomSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        bottomSplitter.setBorder(null);
        bottomSplitter.setTopComponent(leftSplitter);
        this.bottomView = new JScrollPane(logTextArea);
        bottomSplitter.setBottomComponent(this.bottomView);
        bottomSplitter.setDividerLocation(400);
        bottomSplitter.setDividerSize(DIVIDER_SIZE);

        this.add(bottomSplitter, BorderLayout.CENTER);

        updateControls();
    }

    private void updateControls() {
        boolean viewAllowed = false;

        if (this.scraper != null) {
            viewAllowed = this.scraper.getStatus() != Scraper.STATUS_READY;
        }

        this.textViewMenuItem.setEnabled(viewAllowed);
        this.xmlViewMenuItem.setEnabled(viewAllowed);
        this.htmlViewMenuItem.setEnabled(viewAllowed);
        this.imageViewMenuItem.setEnabled(viewAllowed);
        this.listViewMenuItem.setEnabled(viewAllowed);
    }

    /** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        Object userObject =  node.getUserObject();
        if (userObject instanceof TreeNodeInfo) {
            this.selectedNodeInfo = (TreeNodeInfo) userObject;
            PropertiesGridModel model =  this.propertiesGrid.getPropertiesGridModel();
            if (model != null) {
                model.setProperties( this.selectedNodeInfo.getProperties(), this.selectedNodeInfo );
            }
        }
    }

    /**
     * Recursively traverses the configuration and creates visual tree representation.
     * @param root
     * @param defs
     */
    private void createNodes(DefaultMutableTreeNode root, IElementDef[] defs) {
        if (defs != null) {
            for (int i = 0; i < defs.length; i++) {
                IElementDef elementDef = defs[i];
                // constant text is not interesting to be in the visual tree
                if ( !(elementDef instanceof ConstantDef) ) {
                    TreeNodeInfo treeNodeInfo = new TreeNodeInfo(elementDef);
                    this.nodeInfos.put( treeNodeInfo.getElementDef(), treeNodeInfo );
                    DefaultMutableTreeNode node = treeNodeInfo.getNode();
                    this.treeModel.insertNodeInto(node, root, root.getChildCount());
                    createNodes( node, elementDef.getOperationDefs() );
                }
            }
        }
    }

    /**
     * Loads configuration from the file.
     * @param file
     */
    public void loadConfig(File file) {
        try {
            this.configDocument.load(file);
        } catch (IOException e) {
            DialogHelper.showErrorMessage(e.getMessage());
        }

        ScraperConfiguration scraperConfiguration = null;
        try {
            scraperConfiguration = new ScraperConfiguration(file);
            setScraperConfiguration(scraperConfiguration);
        } catch (Exception e) {
            DialogHelper.showErrorMessage(e.getMessage());
        }
    }

    /**
     * Loads configuration from the file.
     * @param url
     */
    public void loadConfig(URL url) {
        try {
            this.configDocument.load(url);
        } catch (IOException e) {
            DialogHelper.showErrorMessage(e.getMessage());
        }

        ScraperConfiguration scraperConfiguration = null;
        try {
            scraperConfiguration = new ScraperConfiguration(url);
            setScraperConfiguration(scraperConfiguration);
        } catch (IOException e) {
            DialogHelper.showErrorMessage(e.getMessage());
        }
    }

    /**
     * Refreshes tree view.
     * @return
     */
    public boolean refreshTree() {
        this.scraper = null;
        updateControls();

        String xmlContent = this.xmlPane.getText();
        InputSource in = new InputSource( new StringReader(xmlContent) );
        try {
            ScraperConfiguration scraperConfiguration = new ScraperConfiguration(in);
            scraperConfiguration.setSourceFile( this.configDocument.getFile() );
            scraperConfiguration.setUrl( this.configDocument.getUrl() );

            setScraperConfiguration(scraperConfiguration);

            ide.setTabIcon(this, null);
        } catch(Exception e) {
            e.printStackTrace();

            String errorMessage = e.getMessage();

            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            this.logger.error(errorMessage + "\n" + writer.getBuffer().toString());
            
            ide.setTabIcon(this, ResourceManager.getSmallErrorIcon());
            JOptionPane.showMessageDialog(this, errorMessage, "Parser exception", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void setScraperConfiguration(ScraperConfiguration scraperConfiguration) {
        this.scraperConfiguration = scraperConfiguration;

        java.util.List operationDefs = scraperConfiguration.getOperations();
        IElementDef[] defs = new IElementDef[operationDefs.size()];
        Iterator it = operationDefs.iterator();
        int index = 0;
        while (it.hasNext()) {
            defs[index++] = (IElementDef) it.next();
        }

        this.topNode.removeAllChildren();
        this.nodeInfos.clear();
        createNodes(this.topNode, defs);
        this.treeModel.reload();
        expandTree();
    }

    /**
     * Expands whole tree.
     */
    private void expandTree() {
        for (int row = 0; row < tree.getRowCount(); row++) {
            tree.expandRow(row);
        }
    }

    public void onNewProcessorExecution(Scraper scraper, BaseProcessor processor) {
        BaseElementDef elementDef = processor.getElementDef();
        if (elementDef != null) {
            TreeNodeInfo nodeInfo = (TreeNodeInfo) this.nodeInfos.get(elementDef);
            if (nodeInfo != null) {
                nodeInfo.setProcessor(processor);
                nodeInfo.increaseExecutionCount();
                setExecutingNode(nodeInfo);
                if ( ide.getSettings().isDynamicConfigLocate() ) {
                    int lineNumber = locateInSource( nodeInfo.getNode(), true );
                    xmlEditorPanel.clearMarkers(ScrollableEditorPanel.RUNNING_MARKER_TYPE);
                    xmlEditorPanel.addMarker(ScrollableEditorPanel.RUNNING_MARKER_TYPE, lineNumber);
                }
            }
        }
    }

    public void onExecutionStart(Scraper scraper) {
        if ( ide.getSettings().isDynamicConfigLocate() ) {
            this.xmlPane.setEditable(false);
        }
        this.xmlEditorPanel.clearAllMarkers();
        updateControls();
        this.ide.updateGUI();
    }

    public void onExecutionContinued(Scraper scraper) {
        this.ide.updateGUI();
    }

    public void onExecutionPaused(Scraper scraper) {
        this.ide.updateGUI();
    }

    public void onExecutionEnd(Scraper scraper) {
        if ( ide.getSettings().isDynamicConfigLocate() ) {
            this.xmlPane.setEditable(true);
        }

        int status = scraper.getStatus();
        if (status == Scraper.STATUS_FINISHED) {
            DialogHelper.showInfoMessage("Configuration \"" + configDocument.getName() + "\" finished execution.");
            ide.setTabIcon(this, ResourceManager.getSmallFinishedIcon());
        } else if (status == Scraper.STATUS_STOPPED) {
            DialogHelper.showWarningMessage("Configuration \"" + configDocument.getName() + "\" aborted by user!");
            ide.setTabIcon(this, ResourceManager.getSmallFinishedIcon());
        }

        // refresh last executing node
        TreeNodeInfo previousNodeInfo = this.nodeRenderer.getExecutingNodeInfo();
        setExecutingNode(null);
        if (previousNodeInfo != null) {
            this.treeModel.nodeChanged( previousNodeInfo.getNode() );
        }

        // update GUI controls
        this.ide.updateGUI();
    }

    public void onProcessorExecutionFinished(Scraper scraper, BaseProcessor processor, Map properties) {
        BaseElementDef elementDef = processor.getElementDef();
        if (elementDef != null) {
            TreeNodeInfo nodeInfo = (TreeNodeInfo) this.nodeInfos.get(elementDef);
            if (nodeInfo != null) {
                nodeInfo.setProperties(properties);
                if ( nodeInfo == this.selectedNodeInfo ) {
                    PropertiesGridModel model =  this.propertiesGrid.getPropertiesGridModel();
                    if (model != null) {
                        model.setProperties( nodeInfo.getProperties(), nodeInfo );
                    }
                }

                java.util.List syncViews = nodeInfo.getSynchronizedViews();
                if (syncViews != null) {
                    for (int i = 0; i < syncViews.size(); i++) {
                        ViewerFrame viewerFrame = (ViewerFrame) syncViews.get(i);
                        viewerFrame.setValue(properties);
                    }
                }
            }
        }
    }

    public void onExecutionError(Scraper scraper, Exception e) {
        if ( ide.getSettings().isDynamicConfigLocate() ) {
            this.xmlPane.setEditable(true);
        }

        markException(e);
        String errorMessage = e.getMessage();

        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        this.scraper.getLogger().error(errorMessage + "\n" + writer.getBuffer().toString());

        DialogHelper.showErrorMessage(errorMessage);

        this.ide.setTabIcon(this, ResourceManager.getSmallErrorIcon());
        this.ide.updateGUI();
    }

    private void setExecutingNode(TreeNodeInfo nodeInfo) {
        TreeNodeInfo previousNodeInfo = this.nodeRenderer.getExecutingNodeInfo();
        if (previousNodeInfo != null) {
            this.treeModel.nodeChanged( previousNodeInfo.getNode() );
        }
        this.nodeRenderer.setExecutingNodeInfo(nodeInfo);
        if (nodeInfo != null) {
            this.treeModel.nodeChanged( nodeInfo.getNode() );
        }
    }

    public void markException(Exception e) {
        xmlEditorPanel.clearMarkers(ScrollableEditorPanel.ERROR_MARKER_TYPE);
        xmlEditorPanel.clearMarkers(ScrollableEditorPanel.RUNNING_MARKER_TYPE);

        this.nodeRenderer.markException(e);
        TreeNodeInfo treeNodeInfo = this.nodeRenderer.getExecutingNodeInfo();
        if (treeNodeInfo != null) {
            this.treeModel.nodeChanged( treeNodeInfo.getNode() );
            int line = locateInSource( (DefaultMutableTreeNode) treeNodeInfo.getNode(), true );
            xmlEditorPanel.addMarker( ScrollableEditorPanel.ERROR_MARKER_TYPE, line );
        }
    }

    public void runConfiguration() {
        if ( this.scraper != null && this.scraper.getStatus() == Scraper.STATUS_PAUSED ) {
            synchronized (this.scraper) {
                this.scraper.notifyAll();
            }

            ide.setTabIcon(this, ResourceManager.getSmallRunIcon());
        } else if ( this.scraper == null || this.scraper.getStatus() != Scraper.STATUS_RUNNING ) {
            boolean ok = refreshTree();
            if (ok) {
                Settings settings = ide.getSettings();
                this.scraper = new Scraper(this.scraperConfiguration, settings.getWorkingPath());
                if ( settings.isProxyEnabled() ) {
                    HttpClientManager httpClientManager = scraper.getHttpClientManager();

                    int proxyPort = settings.getProxyPort();
                    String proxyServer = settings.getProxyServer();
                    if (proxyPort > 0) {
                        httpClientManager.setHttpProxy(proxyServer, proxyPort);
                    } else {
                		httpClientManager.setHttpProxy(proxyServer);
                	}

                    if ( settings.isProxyAuthEnabled() ) {
                        httpClientManager.setHttpProxyCredentials( settings.getProxyUserename(), settings.getProxyPassword() );
                    }
                }

                this.scraper.setDebug(true);
                this.logTextArea.setText(null);
                this.scraper.getLogger().addAppender( new TextAreaAppender(this.logTextArea) );
                this.scraper.addRuntimeListener(this);

                ide.setTabIcon(this, ResourceManager.getSmallRunIcon());

                // starts scrapping in separate thread
                new ScraperExecutionThread(this, this.scraper).start();
            }
        }
    }

    public synchronized int getScraperStatus() {
        if (this.scraper != null) {
            return this.scraper.getStatus();
        }

        return -1;
    }

    public Ide getIde() {
        return ide;
    }

    public synchronized void stopScraperExecution() {
        if (this.scraper != null) {
            this.scraper.stopExecution();
        }
    }

    public synchronized void pauseScraperExecution() {
        if (this.scraper != null) {
            this.scraper.pauseExecution();
            ide.setTabIcon(this, ResourceManager.getSmallPausedIcon());
        }
    }

    private int locateInSource(DefaultMutableTreeNode treeNode, boolean locateAtLineBeginning) {
        if (treeNode != null) {
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof TreeNodeInfo) {
                TreeNodeInfo treeNodeInfo = (TreeNodeInfo) userObject;
                BaseElementDef elementDef = (BaseElementDef) treeNodeInfo.getElementDef();
                int lineNumber = elementDef.getLineNumber();
                int columnNumber = elementDef.getColumnNumber();

                String content = null;
                try {
                    content = this.xmlPane.getDocument().getText( 0, this.xmlPane.getDocument().getLength() );
                    String[] lines = content.split("\n");
                    int offset = 0;
                    int lineCount = 1;
                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i];
                        if(lineCount == lineNumber) {
                            offset += locateAtLineBeginning ? 1 : columnNumber;
                            break;
                        }
                        lineCount++;
                        if(lineCount > 2) {
                            offset++;
                        }
                        offset += line.length();
                    }

                    this.xmlPane.grabFocus();
                    this.xmlPane.setCaretPosition(offset);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }

                return lineNumber;
            }
        }

        return -1;
    }

    public void undo() {
        this.xmlPane.undo();
    }

    public void redo() {
        this.xmlPane.redo();
    }

    public String getXml() {
        return this.xmlPane.getText();
    }

    public XmlTextPane getXmlPane() {
        return xmlPane;
    }

    public ScrollableEditorPanel getXmlEditorPanel() {
        return xmlEditorPanel;
    }

    public void setConfigFile(File file) {
        if (this.scraperConfiguration != null) {
            this.scraperConfiguration.setSourceFile(file);
        }
    }

    public ConfigDocument getConfigDocument() {
        return configDocument;
    }

    public void showHierarchy() {
        boolean isVisible = this.leftView.isVisible();
        if (isVisible) {
            this.leftDividerLocation = this.leftSplitter.getDividerLocation();
        }
        this.leftView.setVisible(!isVisible);
        if (!isVisible) {
            this.leftSplitter.setDividerLocation(this.leftDividerLocation);
        }
    }

    public void showLog() {
        boolean isVisible = this.bottomView.isVisible();
        if (isVisible) {
            this.bottomDividerLocation = this.bottomSplitter.getDividerLocation();
        }
        this.bottomView.setVisible(!isVisible);
        if (!isVisible) {
            this.bottomSplitter.setDividerLocation(this.bottomDividerLocation);
        }
    }

    public boolean isHierarchyVisible() {
        return this.leftView.isVisible();
    }

    public boolean isLogVisible() {
        return this.bottomView.isVisible();
    }

}