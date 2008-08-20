/*  Copyright (c) 2006-2008, Vladimir Nikic
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
package org.webharvest.runtime.processors;

import org.webharvest.runtime.*;
import org.webharvest.runtime.variables.*;
import org.webharvest.utils.CommonUtil;
import org.webharvest.definition.WebHarvestPluginDef;

import java.util.*;

/**
 * Base for all user-defined plugins. 
 */
abstract public class WebHarvestPlugin extends BaseProcessor {

    private Map attributes;

    public WebHarvestPlugin() {
        super();
    }

    /**
     * Defines name of the processor. Should be valid identifier.
     * Processor's tag will use this name. For example, if this name is
     * "myprocessor" in config file it will be used as &lt;myprocessor...&gt;...&lt;/myprocessor&gt; 
     * @return Name of the processor
     */
    public abstract String getName();

    /**
     * This method should return all possible attribute names for the plugin processor.
     * @return Array of attribute names (case insensitive)
     */
    public String[] getValidAttributes() {
        return new String[] {};
    }

    /**
     * This method should return all mandatory attribute names for the plugin processor.
     * @return Array of attribute names (case insensitive)
     */
    public String[] getRequiredAttributes() {
        return new String[] {};
    }

    /**
     * This method should return all names of all allowed processors inside the body of
     * this processor plugin. If null is returned, then all subprocessors are allowed.
     * @return Array of allowed subprocessor names (case insensitive)
     */
    public String[] getValidSubprocessors() {
        return null;
    }

    /**
     * This method should return all mandatory subprocessor names, or in other words all
     * mandatory subtags that must be present in the body of this processor plugin.
     * @return Array of mandatory subprocessor names (case insensitive)
     */
    public String[] getRequiredSubprocessors() {
        return new String[] {};
    }

    /**
     * Mathod that actually executes processor. Since one instance of this class may
     * be used for multiple executions, creator of plugin is responsible for initiating
     * local variables at the beginning of this method. 
     * @param scraper
     * @param context
     * @return Instance of variable as result of xecution.
     */
    public abstract Variable execute(Scraper scraper, ScraperContext context);

    public String getTagDesc() {
        String[] validSubprocessors = getValidSubprocessors();
        if (validSubprocessors == null) {
            return null;
        }

        String requiredTags[] = getRequiredSubprocessors();

        StringBuffer result = new StringBuffer();
        for (int i = 0; i < validSubprocessors.length; i++) {
            if (result.length() != 0) {
                result.append(',');
            }
            String subProcessor = validSubprocessors[i];
            if (CommonUtil.existsInStringArray(requiredTags, subProcessor, true)) {
                result.append('!');
            }
            result.append(subProcessor);
        }
        return result.toString();
    }

    public String getAttributeDesc() {
        String[] validAtts = getValidAttributes();
        if (validAtts == null) {
            return "id";
        }

        String requiredAtts[] = getRequiredAttributes();

        StringBuffer result = new StringBuffer("id,");
        for (int i = 0; i < validAtts.length; i++) {
            String att = validAtts[i];
            if (CommonUtil.existsInStringArray(requiredAtts, att, true)) {
                result.append('!');
            }
            result.append(att);
            result.append(",");
        }
        return result.toString();
    }

    public Map getAttributes() {
        return attributes;
    }

    public void setDef(WebHarvestPluginDef def) {
        this.elementDef = def;
        this.attributes = def.getAttributes();
    }
    
}