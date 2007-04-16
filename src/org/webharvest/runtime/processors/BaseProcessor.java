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
package org.webharvest.runtime.processors;

import org.apache.log4j.Logger;
import org.webharvest.definition.BaseElementDef;
import org.webharvest.definition.IElementDef;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.Appender;
import org.webharvest.runtime.variables.IVariable;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.utils.CommonUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Base processor that contains common processor logic.
 * All other processors extend this class.
 */
abstract public class BaseProcessor {

    protected static Logger log = Logger.getLogger(BaseProcessor.class);

    abstract public IVariable execute(Scraper scraper, ScraperContext context);

    protected BaseElementDef elementDef;

    protected BaseProcessor() {
    }

    /**
     * Base constructor - assigns element definition to the processor.
     * @param elementDef
     */
    protected BaseProcessor(BaseElementDef elementDef) {
        this.elementDef = elementDef;
    }

    /**
     * Wrapper for the execute method. Adds controling and logging logic.
     */
    public IVariable run(Scraper scraper, ScraperContext context) {
        long startTime = System.currentTimeMillis();

        int runningLevel = scraper.getRunningLevel();

        String id = (this.elementDef != null) ? BaseTemplater.execute( this.elementDef.getId(), scraper.getScriptEngine() ) : null;
        String idDesc = id != null ? "[ID=" + id + "] " : "";
        String indent = CommonUtil.replicate("    ", runningLevel-1);

        log.info(indent + CommonUtil.getClassName(this) + " starts processing..." + idDesc);

        scraper.increaseRunningLevel();
        IVariable result = execute(scraper, context);
        scraper.decreaseRunningLevel();

        // if debug mode is true and processor ID is not null then write debugging file
        if (scraper.isDebugMode() && id != null) {
            writeDebugFile(result, id, scraper);
        }

        log.info(indent + CommonUtil.getClassName(this) +
                  " processor executed  in " + (System.currentTimeMillis() - startTime) + "ms." + idDesc);

        return result;
    }

    protected IVariable[] executeBody(BaseElementDef elementDef, Scraper scraper, ScraperContext context) {
        IElementDef[] defs = elementDef.getOperationDefs();
        IVariable[] result = new IVariable[ Math.max(defs.length, 1) ];     // at least one element

        if (defs.length > 0) {
            for (int i = 0; i < defs.length; i++) {
                BaseProcessor processor = ProcessorResolver.createProcessor( defs[i] );
                result[i] = processor.run(scraper, context);
            }
        } else {
            result[0] = new NodeVariable( elementDef.getBodyText() );
        }

        return result;
    }

    protected void debug(BaseElementDef elementDef, Scraper scraper, IVariable variable) {
        String id = (elementDef != null) ? BaseTemplater.execute( elementDef.getId(), scraper.getScriptEngine() ) : null;

        if (scraper.isDebugMode() && id != null) {
            if (variable != null) {
                writeDebugFile(variable, id, scraper);
            }
        }
    }

    protected IVariable getBodyTextContent(BaseElementDef elementDef, Scraper scraper, ScraperContext context) {
        if (elementDef == null) {
            return null;
        } else if (elementDef.hasOperations()) {
            IVariable[] vars = executeBody(elementDef, scraper, context);
            return Appender.appendText(vars);
        } else {
            return new NodeVariable(elementDef.getBodyText());
        }
    }

    protected IVariable getBodyBinaryContent(BaseElementDef elementDef, Scraper scraper, ScraperContext context) {
        if (elementDef == null) {
            return null;
        } else if (elementDef.hasOperations()) {
            IVariable[] vars = executeBody(elementDef, scraper, context);
            return Appender.appendBinary(vars);
        } else {
            return new NodeVariable(elementDef.getBodyText().getBytes());
        }
    }

    protected IVariable getBodyListContent(BaseElementDef elementDef, Scraper scraper, ScraperContext context) {
        IVariable[] vars = executeBody(elementDef, scraper, context);

        ListVariable listVariable = new ListVariable();
        for (int i = 0; i < vars.length; i++) {
            if (!vars[i].isEmpty()) {
                listVariable.addVariable(vars[i]);
            }
        }

        return listVariable;
    }

    public BaseElementDef getElementDef() {
        return elementDef;
    }

    private void writeDebugFile(IVariable var, String processorId, Scraper scraper) {
        byte[] data = var == null ? new byte[] {} : var.toString().getBytes();

        String workingDir = scraper.getWorkingDir();
        String dir = CommonUtil.getAbsoluteFilename(workingDir, "_debug");
        
        int index = 1;
        String fullPath = dir + "/" + processorId + "_" + index + ".debug";
        while ( new File(fullPath).exists() ) {
            index++;
            fullPath = dir + "/" + processorId + "_" + index + ".debug";
        }

        FileOutputStream out;
        try {
            new File(dir).mkdirs();
            out = new FileOutputStream(fullPath, false);
            out.write(data);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}