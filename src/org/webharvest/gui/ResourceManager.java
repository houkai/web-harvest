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

/**
 * @author: Vladimir Nikic
 * Date: Apr 19, 2007
 */
public class ResourceManager {

    private static Class clazz = ResourceManager.class;


    public static Icon getIcon(String path) {
        return new ImageIcon(clazz.getResource(path));
    }

    public static Icon getWelcomeLogo() {
        return getIcon("resources/welcomelogo.jpg");
    }

    public static Icon getNoneIcon() {
        return getIcon("resources/icons/none.gif");
    }

    public static Icon getWebHarvestIcon() {
        return getIcon("resources/icons/webharvest.gif");
    }

    public static Icon getNewIcon() {
        return getIcon("resources/icons/new.gif");
    }

    public static Icon getOpenIcon() {
        return getIcon("resources/icons/open.gif");
    }

    public static Icon getCloseIcon() {
        return getIcon("resources/icons/close.gif");
    }

    public static Icon getSaveIcon() {
        return getIcon("resources/icons/save.gif");
    }

    public static Icon getRefreshIcon() {
        return getIcon("resources/icons/refresh.gif");
    }

    public static Icon getRunIcon() {
        return getIcon("resources/icons/run.gif");
    }

    public static Icon getPauseIcon() {
        return getIcon("resources/icons/pause.gif");
    }

    public static Icon getStopIcon() {
        return getIcon("resources/icons/stop.gif");
    }

    public static Icon getCopyIcon() {
        return getIcon("resources/icons/copy.gif");
    }

    public static Icon getCutIcon() {
        return getIcon("resources/icons/cut.gif");
    }

    public static Icon getPasteIcon() {
        return getIcon("resources/icons/paste.gif");
    }

    public static Icon getUndoIcon() {
        return getIcon("resources/icons/undo.gif");
    }

    public static Icon getRedoIcon() {
        return getIcon("resources/icons/redo.gif");
    }

    public static Icon getFindIcon() {
        return getIcon("resources/icons/find.gif");
    }

    public static Icon getSettingsIcon() {
        return getIcon("resources/icons/settings.gif");
    }

    public static Icon getHelpIcon() {
        return getIcon("resources/icons/help.gif");
    }

    public static Icon getHomepageIcon() {
        return getIcon("resources/icons/homepage.gif");
    }

    public static Icon getDownloadIcon() {
        return getIcon("resources/icons/download.gif");
    }

    public static Icon getViewIcon() {
        return getIcon("resources/icons/view.gif");
    }

    public static Icon getValidateIcon() {
        return getIcon("resources/icons/validate.gif");
    }

    public static Icon getPrettyPrintIcon() {
        return getIcon("resources/icons/prettyprint.gif");
    }

    public static Icon getSmallRunIcon() {
        return getIcon("resources/icons/small_run.gif");
    }

    public static Icon getSmallErrorIcon() {
        return getIcon("resources/icons/small_error.gif");
    }

    public static Icon getSmallPausedIcon() {
        return getIcon("resources/icons/small_paused.gif");
    }

    public static Icon getSmallFinishedIcon() {
        return getIcon("resources/icons/small_finished.gif");
    }

}
