package org.webharvest.runtime.processors.plugins;

import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;

/**
 * Sample plugin
 */
public class SamplePlugin2 extends WebHarvestPlugin {

    public String getName() {
        return "sample2";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
        return new NodeVariable(getName());
    }

    public String[] getValidAttributes() {
        return new String[] {"type", "connection"};
    }

    public String[] getRequiredAttributes() {
        return new String[] {"connection"};
    }

    public String[] getValidSubprocessors() {
        return null;
    }

    public String[] getRequiredSubprocessors() {
        return null;
    }

}