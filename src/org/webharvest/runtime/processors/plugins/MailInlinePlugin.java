package org.webharvest.runtime.processors.plugins;

import org.webharvest.runtime.*;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.*;

/**
 * Sample plugin
 */
public class MailInlinePlugin extends WebHarvestPlugin {

    public String getName() {
        return "mail-inline";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
        BaseProcessor mailProcessor = scraper.getRunningProcessorOfType(MailPlugin.class);
        if (mailProcessor != null) {

        } else {

        }
        return new NodeVariable(getName());
    }

    public String[] getValidAttributes() {
        return new String[] {};
    }

    public String[] getRequiredAttributes() {
        return new String[] {};
    }

    public String[] getValidSubprocessors() {
        return null;
    }

    public String[] getRequiredSubprocessors() {
        return null;
    }

}