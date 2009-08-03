package org.webharvest.runtime.processors.plugins;

import org.apache.commons.net.ftp.*;
import org.webharvest.runtime.*;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.*;
import org.webharvest.utils.*;

import java.io.*;
import java.util.*;

/**
 * Ftp List plugin - can be used only inside ftp plugin for listing file in working remote directory.
 */
public class FtpListPlugin extends WebHarvestPlugin {

    public String getName() {
        return "ftp-list";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
        FtpPlugin ftpPlugin = (FtpPlugin) scraper.getRunningProcessorOfType(FtpPlugin.class);
        if (ftpPlugin != null) {
            FTPClient ftpClient = ftpPlugin.getFtpClient();

            String path = CommonUtil.nvl( evaluateAttribute("path", scraper), "" );
            boolean listFiles = evaluateAttributeAsBoolean("listfiles", true, scraper);
            boolean listDirs = evaluateAttributeAsBoolean("listdirs", true, scraper);
            boolean listLinks = evaluateAttributeAsBoolean("listlinks", true, scraper);

            setProperty("Path", path);
            setProperty("List Files", listFiles);
            setProperty("List Directories", listDirs);
            setProperty("List Symbolic Links", listLinks);

            try {
                FTPFile[] files = ftpClient.listFiles(path);
                if (files != null) {
                    List<String> filenameList = new ArrayList<String>();
                    for (FTPFile ftpFile: files) {
                        if ( (listFiles && ftpFile.isFile()) || (listDirs && ftpFile.isDirectory()) || (listLinks && ftpFile.isSymbolicLink()) ) {
                            filenameList.add(ftpFile.getName());
                        }
                    }

                    return new ListVariable(filenameList);
                }
            } catch (IOException e) {
                throw new FtpPluginException(e);
            }
        } else {
            throw new FtpPluginException("Cannot use ftp list plugin out of ftp plugin context!");
        }
        
        return new EmptyVariable();
    }

    public String[] getValidAttributes() {
        return new String[] {"path", "listfiles", "listdirs", "listlinks"};
    }

    public String[] getRequiredAttributes() {
        return new String[] {};
    }

}