package org.webharvest.runtime.processors.plugins;

import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;

/**
 * Support for database operations.
 */
public class DatabasePlugin extends WebHarvestPlugin {

    public String getName() {
        return "database";
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        Variable body = executeBody(scraper, context);
        return new NodeVariable(evaluateAttribute("connection", scraper));
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