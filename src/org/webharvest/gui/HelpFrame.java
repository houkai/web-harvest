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

import org.webharvest.definition.XmlNode;
import org.webharvest.definition.XmlParser;
import org.webharvest.gui.component.ProportionalSplitPane;
import org.webharvest.utils.CommonUtil;
import org.xml.sax.InputSource;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

/**
 * Frame that contains Web-Harvest help.
 * @author: Vladimir Nikic
 * Date: May 8, 2007
 */
public class HelpFrame extends JFrame implements TreeSelectionListener {

    private static final Dimension HELP_FRAME_DIMENSION = new Dimension(640, 480);

    private class TopicInfo {
        private String id;
        private String title;
        private int subtopicCount;

        public TopicInfo(String id, String title, int subtopicCount) {
            this.id = id;
            this.title = title;
            this.subtopicCount = subtopicCount;
        }

        public String toString() {
            return title;
        }
    }

    private JEditorPane htmlPane;
    private JTree tree;
    private DefaultMutableTreeNode topNode;
    private DefaultTreeModel treeModel;

    /**
     * Constructor - creates layout.
     */
    public HelpFrame() {
        setTitle("Web-Harvest Help");
        setIconImage( ((ImageIcon) ResourceManager.HELP_ICON).getImage() );

        this.topNode = new DefaultMutableTreeNode();
        this.treeModel = new DefaultTreeModel(this.topNode);
        try {
            String helpContent = CommonUtil.readStringFromUrl( ResourceManager.getHelpContentUrl() );
            XmlNode xmlNode = XmlParser.parse( new InputSource(new StringReader(helpContent)) );
            createNodes(topNode, xmlNode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        tree = new JTree(topNode);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setBorder( new EmptyBorder(5, 5, 5, 5) );
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) value;
                    Object userObject =  defaultMutableTreeNode.getUserObject();
                    if (userObject instanceof TopicInfo) {
                        TopicInfo topicInfo = (TopicInfo) userObject;
                        renderer.setIcon( topicInfo.subtopicCount == 0 ? ResourceManager.HELPTOPIC_ICON : ResourceManager.HELPDIR_ICON );
                    }
                }
                return renderer;
            }
        });
        tree.addTreeSelectionListener(this);

        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
        htmlPane.setContentType("text/html");
        htmlPane.setEditorKit( new HTMLEditorKit() );
        htmlPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        JSplitPane splitPane = new ProportionalSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.0d);
        splitPane.setBorder(null);

        JScrollPane treeScrollPane = new JScrollPane(tree);
        treeScrollPane.getViewport().setBackground(Color.white);
        treeScrollPane.setBackground(Color.white);
        splitPane.setLeftComponent(treeScrollPane);
        splitPane.setRightComponent(new JScrollPane(htmlPane));
        splitPane.setDividerLocation(0.3d);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(splitPane, BorderLayout.CENTER);


        pack();
    }

    private void createNodes(DefaultMutableTreeNode root, XmlNode xmlNode) {
        if (xmlNode != null) {
            Object topicsObject = xmlNode.getElement("topic");
            if (topicsObject instanceof java.util.List) {
                java.util.List subtopics = (java.util.List) topicsObject;
                for (int i = 0; i < subtopics.size(); i++) {
                    XmlNode xmlSubNode = (XmlNode) subtopics.get(i);

                    String id = xmlSubNode.getAttribute("id");
                    String title = xmlSubNode.getAttribute("title");
                    Object subs = xmlSubNode.getElement("topic");
                    int subtopicCount = subs instanceof java.util.List ? ((java.util.List)subs).size() : 0;

                    TopicInfo topicInfo = new TopicInfo(id, title, subtopicCount);
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(topicInfo);
                    this.treeModel.insertNodeInto(node, root, root.getChildCount());
                    createNodes(node, xmlSubNode);
                }
            }
        }
    }


    public Dimension getPreferredSize() {
        return HELP_FRAME_DIMENSION;
    }

    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        Object userObject =  node.getUserObject();
        if (userObject instanceof TopicInfo) {

            TopicInfo topicInfo = (TopicInfo) userObject;
            URL helpFileUrl = ResourceManager.getHelpFileUrl(topicInfo.id);
            try {
                this.htmlPane.setPage(helpFileUrl);
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(this, "Cannot read help for \"" + topicInfo.title + "\"!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}