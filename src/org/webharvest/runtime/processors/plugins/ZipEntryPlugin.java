package org.webharvest.runtime.processors.plugins;

import org.webharvest.runtime.*;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.*;
import org.webharvest.utils.*;

import java.io.*;
import java.util.zip.*;

/**
 * Zip entry plugin - can be used only inside zip plugin.
 */
public class ZipEntryPlugin extends WebHarvestPlugin {

    public String getName() {
        return "zip-entry";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
        BaseProcessor processor = scraper.getRunningProcessorOfType(ZipPlugin.class);
        if (processor != null) {
            String name = evaluateAttribute("name", scraper);
            if (CommonUtil.isEmptyString(name)) {
                throw new ZipPluginException("Name of zip entry cannot be empty!");
            }
            String charset = evaluateAttribute("charset", scraper);
            if (CommonUtil.isEmptyString(charset)) {
                charset = scraper.getConfiguration().getCharset();
            }

            ZipPlugin zipPlugin = (ZipPlugin) processor;
            ZipOutputStream zipOutStream = zipPlugin.getZipOutStream();
            Variable bodyResult = executeBody(scraper, context);
            try {
                zipOutStream.putNextEntry(new ZipEntry(name));
                zipOutStream.write(bodyResult.toBinary(charset));
                zipOutStream.closeEntry();
            } catch (IOException e) {
                throw new ZipPluginException(e);
            }
        } else {
            throw new ZipPluginException("Cannot use zip entry plugin out of zip plugin context!");
        }
        return new EmptyVariable();
    }

    public String[] getValidAttributes() {
        return new String[] {"name", "charset"};
    }

    public String[] getRequiredAttributes() {
        return new String[] {"name"};
    }

}