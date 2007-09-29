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

import org.apache.commons.httpclient.NameValuePair;
import org.webharvest.definition.HttpDef;
import org.webharvest.exception.HttpException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.web.HttpClientManager;
import org.webharvest.runtime.web.HttpResponseWrapper;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Http processor.
 */
public class HttpProcessor extends BaseProcessor {

    private HttpDef httpDef;
    
    List httpParams = new ArrayList();
    Map httpHeaderMap = new HashMap();

    public HttpProcessor(HttpDef httpDef) {
        super(httpDef);
        this.httpDef = httpDef;
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
    	scraper.setRunningHttpProcessor(this);

        ScriptEngine scriptEngine = scraper.getScriptEngine();
        String url = BaseTemplater.execute( httpDef.getUrl(), scriptEngine);
        String method = BaseTemplater.execute( httpDef.getMethod(), scriptEngine);
        String charset = BaseTemplater.execute( httpDef.getCharset(), scriptEngine);
        String username = BaseTemplater.execute( httpDef.getUsername(), scriptEngine);
        String password = BaseTemplater.execute( httpDef.getPassword(), scriptEngine);
        String cookiePolicy = BaseTemplater.execute( httpDef.getCookiePolicy(), scriptEngine);

        if (charset == null) {
            charset = scraper.getConfiguration().getCharset();
        }
        
        // executes body of HTTP processor
        new BodyProcessor(httpDef).execute(scraper, context);

        HttpClientManager manager = scraper.getHttpClientManager();
        manager.setCookiePolicy(cookiePolicy);

        HttpResponseWrapper res = manager.execute(method, url, charset, username, password, httpParams, httpHeaderMap);

        scraper.removeRunningHttpProcessor();

        String mimeType = res.getMimeType();

        long contentLength = res.getContentLength();
        if ( scraper.getLogger().isInfoEnabled() ) {
            scraper.getLogger().info("Downloaded: " + url + ", mime type = " + mimeType + ", length = " + contentLength + "B.");
        }

        Variable result;
        
        if (mimeType == null || mimeType.toLowerCase().indexOf("text") == 0) {
            String text;
            try {
                text = new String(res.getBody(), charset);
            } catch (UnsupportedEncodingException e) {
                throw new HttpException("Charset " + charset + " is not supported!", e);
            }
            
            result =  new NodeVariable(text);
        } else {
            result = new NodeVariable( res.getBody() );
        }

        this.setProperty("URL", url);
        this.setProperty("Method", method);
        this.setProperty("Charset", charset);
        this.setProperty("Content length", String.valueOf(contentLength));
        this.setProperty("Status code", new Integer(res.getStatusCode()));
        this.setProperty("Status text", res.getStatusText());

        Map responseHeaders = res.getHeaders();
        if (responseHeaders != null) {
            Iterator iterator = responseHeaders.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                this.setProperty("HTTP header: " + entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
    
    protected void addHttpParam(String name, String value) {
    	httpParams.add( new NameValuePair(name, value) );
    }
    
    protected void addHttpHeader(String name, String value) {
    	httpHeaderMap.put(name, value);
    }

}