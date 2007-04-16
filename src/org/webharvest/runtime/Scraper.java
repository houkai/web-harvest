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
package org.webharvest.runtime;

import org.apache.log4j.Logger;
import org.webharvest.definition.IElementDef;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.processors.BaseProcessor;
import org.webharvest.runtime.processors.CallProcessor;
import org.webharvest.runtime.processors.HttpProcessor;
import org.webharvest.runtime.processors.ProcessorResolver;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.variables.IVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.web.HttpClientManager;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.Stack;

import java.util.Iterator;
import java.util.List;

/**
 * Basic runtime class.
 */
public class Scraper {

    protected static Logger log = Logger.getLogger(Scraper.class);

    private ScraperConfiguration configuration;
    private String workingDir;
    private ScraperContext context;

    private transient boolean isDebugMode = false;

    private HttpClientManager httpClientManager;

    // stack of running functions
    private transient Stack runningFunctions = new Stack();
    
    // stack of running http processors
    private transient Stack runningHttpProcessors = new Stack();

    // shows depth of running processors during execution 
    private transient int runningLevel = 1;

    // default script engine used throughout the configuration execution
    ScriptEngine scriptEngine = null;

    /**
     * Constructor.
     * @param configuration
     * @param workingDir
     */
    public Scraper(ScraperConfiguration configuration, String workingDir) {
        this.configuration = configuration;
        this.workingDir = CommonUtil.adaptFilename(workingDir);

        this.httpClientManager = new HttpClientManager();

        this.context = new ScraperContext();
        this.scriptEngine = new ScriptEngine(this.context);
    }

    /**
     * Adds parameter with specified name and value to the context.
     * This way some predefined variables can be put in runtime context
     * before execution starts.
     * @param name
     * @param value
     */
    public void addVariableToContext(String name, Object value) {
        this.context.put(name, new NodeVariable(value));
    }

    public IVariable execute(List ops) {
        Iterator it = ops.iterator();
        while (it.hasNext()) {
            IElementDef elementDef = (IElementDef) it.next();
            BaseProcessor processor = ProcessorResolver.createProcessor(elementDef);

            if (processor != null) {
                processor.run(this, context);
            }
        }

        return new NodeVariable("");
    }

    public void execute() {
    	long startTime = System.currentTimeMillis();
        execute( configuration.getOperations() );
        log.info("Configuration executed in " + (System.currentTimeMillis() - startTime) + "ms.");
    }
    
    public ScraperContext getContext() {
		return context;
	}

	public ScraperConfiguration getConfiguration() {
        return configuration;
    }

    public String getWorkingDir() {
        return this.workingDir;
    }

    public HttpClientManager getHttpClientManager() {
        return httpClientManager;
    }

    public void addRunningFunction(CallProcessor callProcessor) {
        runningFunctions.push(callProcessor);
    }

    public CallProcessor getRunningFunction() {
        return (CallProcessor) runningFunctions.peek();
    }

    public void removeRunningFunction() {
        if (runningFunctions.size() > 0) {
            runningFunctions.pop();
        }
    }
    
    public HttpProcessor getRunningHttpProcessor() {
    	return (HttpProcessor) runningHttpProcessors.peek();
    }
    
    public void setRunningHttpProcessor(HttpProcessor httpProcessor) {
    	runningHttpProcessors.push(httpProcessor);
    }

    public void removeRunningHttpProcessor() {
        if (runningHttpProcessors.size() > 0) {
            runningHttpProcessors.pop();
        }
    }
    public void increaseRunningLevel() {
        this.runningLevel++;
    }

    public void decreaseRunningLevel() {
        this.runningLevel--;
    }

    public int getRunningLevel() {
        return runningLevel;
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public void setDebug(boolean debug) {
        this.isDebugMode = debug;
    }

    public ScriptEngine getScriptEngine() {
        return runningFunctions.size() > 0 ? getRunningFunction().getScriptEngine() : this.scriptEngine;
    }

}