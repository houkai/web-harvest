package org.webharvest.definition;

import org.webharvest.runtime.processors.*;

import java.util.*;

/**
 * Definition of all plugin processors.
 */
public class WebHarvestPluginDef extends BaseElementDef {

	private Map attributes;
    private WebHarvestPlugin plugin;

    public WebHarvestPluginDef(XmlNode xmlNode) {
        super(xmlNode, false);
        this.attributes = xmlNode.getAttributes();
    }

    void setPluginProcessor(WebHarvestPlugin plugin) {
        this.plugin = plugin;
    }

    public Map getAttributes() {
        return attributes;
    }

    public WebHarvestPlugin getPlugin() {
        return plugin;
    }

    public String getShortElementName() {
        String name = plugin.getName();
        return name != null ? name.toLowerCase() : "unknown plugin";
    }

}