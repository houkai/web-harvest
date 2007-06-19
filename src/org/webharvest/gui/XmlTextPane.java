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
 * Date: Apr 20, 2007
 */

import org.bounce.text.xml.XMLEditorKit;
import org.bounce.text.xml.XMLDocument;
import org.bounce.text.xml.XMLStyleConstants;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URL;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import javax.swing.text.*;

public class XmlTextPane extends JEditorPane {

    private UndoManager undoManager = new UndoManager();

    public XmlTextPane() {
        XMLEditorKit kit = new XMLEditorKit(true);

        kit.setLineWrappingEnabled(false);

        kit.setStyle( XMLStyleConstants.ELEMENT_NAME, new Color(128, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.ELEMENT_VALUE, new Color(0, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.ELEMENT_PREFIX, new Color(128, 0, 0), Font.PLAIN);

        kit.setStyle( XMLStyleConstants.ATTRIBUTE_NAME, new Color(255, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.ATTRIBUTE_VALUE, new Color(0, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.ATTRIBUTE_PREFIX, new Color(128, 0, 0), Font.PLAIN);

        kit.setStyle( XMLStyleConstants.NAMESPACE_NAME, new Color(102, 102, 102), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.NAMESPACE_VALUE, new Color(0, 51, 51), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.NAMESPACE_PREFIX, new Color(102, 102, 102), Font.PLAIN);

        kit.setStyle( XMLStyleConstants.ENTITY, new Color(0, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.COMMENT, new Color(153, 153, 153), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.CDATA, new Color(0, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.SPECIAL, new Color(0, 0, 0), Font.PLAIN);

        this.setEditorKit(kit);

        this.setFont( new Font( "Courier", Font.PLAIN, 12));
    }

    public boolean getScrollableTracksViewportWidth() {
        //should not allow text to be wrapped
        return false;
    }

    public void undo() {
        if ( this.undoManager.canUndo() ) {
            this.undoManager.undo();
        }
    }

    public void redo() {
        if ( this.undoManager.canRedo() ) {
            this.undoManager.redo();
        }
    }

    public UndoableEditListener getUndoManager() {
        return undoManager;
    }
    
}