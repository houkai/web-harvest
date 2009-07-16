package org.webharvest.runtime.processors.plugins;

import org.webharvest.runtime.*;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.*;

/**
 * Sample plugin
 */
public class MailAttachPlugin extends WebHarvestPlugin {

    public String getName() {
        return "mail-attach";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
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