package org.webharvest.definition;

import org.webharvest.runtime.processors.*;
import org.webharvest.exception.PluginException;

import java.util.*;

/**
 * Definition of all plugin processors.
 */
public class WebHarvestPluginDef extends BaseElementDef {

	private Map attributes;
    private Class pluginClass;
    private WebHarvestPlugin plugin;

    public WebHarvestPluginDef(XmlNode xmlNode) {
        super(xmlNode, false);
        this.attributes = xmlNode.getAttributes();
    }

    void setPluginClass(Class pluginClass) {
        this.pluginClass = pluginClass;
    }

    public Map getAttributes() {
        return attributes;
    }

    public WebHarvestPlugin createPlugin() {
        if (pluginClass != null) {
            try {
                plugin = (WebHarvestPlugin) pluginClass.newInstance();
                plugin.setDef(this);
            } catch (Exception e) {
                throw new PluginException(e);
            }
        }

        throw new PluginException("Cannot create plugin!");
    }

    public String getShortElementName() {
        String name = plugin.getName();
        return name != null ? name.toLowerCase() : "unknown plugin";
    }

    public WebHarvestPlugin getPlugin() {
        return plugin;
    }
    
}