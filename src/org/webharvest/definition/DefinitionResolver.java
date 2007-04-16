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
package org.webharvest.definition;

import org.webharvest.exception.ConfigurationException;
import org.webharvest.exception.ErrMsg;

import java.util.*;

public class DefinitionResolver {
	
    public static IElementDef createElementDefinition(XmlNode node) {
        IElementDef result;
    	
    	String nodeName = node.getName();
        
        if (nodeName.equalsIgnoreCase("empty")) {
        	validate(node, null, "id");
            result = new EmptyDef(node);
        } else if (nodeName.equalsIgnoreCase("text")) {
        	validate(node, null, "id");
        	result = new TextDef(node);
        } else if (nodeName.equalsIgnoreCase("file")) {
        	validate(node, null, "id,!path,action,type,charset");
            result = new FileDef(node);
        } else if (nodeName.equalsIgnoreCase("var-def")) {
        	validate(node, null, "id,!name");
        	result = new VarDefDef(node);
        } else if (nodeName.equalsIgnoreCase("var")) {
        	validate(node, "", "id,!name");
        	result = new VarDef(node);
        } else if (nodeName.equalsIgnoreCase("http")) {
        	validate(node, null, "id,!url,method,charset,username,password");
            result = new HttpDef(node);
        } else if (nodeName.equalsIgnoreCase("http-param")) {
        	validate(node, null, "id,!name");
        	result = new HttpParamDef(node);
        } else if (nodeName.equalsIgnoreCase("http-header")) {
        	validate(node, null, "id,!name");
        	result = new HttpHeaderDef(node);
        } else if (nodeName.equalsIgnoreCase("html-to-xml")) {
        	validate(node, null, "id");
            result = new HtmlToXmlDef(node);
        } else if (nodeName.equalsIgnoreCase("regexp")) {
        	validate(node, "!regexp-pattern,!regexp-source,regexp-result", "id,replace,max");
            result = new RegexpDef(node);
        } else if (nodeName.equalsIgnoreCase("xpath")) {
        	validate(node, null, "id,!expression");
            result = new XPathDef(node);
        } else if (nodeName.equalsIgnoreCase("xquery")) {
        	validate(node, "xq-param,!xq-expression", "id");
            result = new XQueryDef(node);
        } else if (nodeName.equalsIgnoreCase("xslt")) {
        	validate(node, "!xml,!stylesheet", "id");
            result = new XsltDef(node);
        } else if (nodeName.equalsIgnoreCase("template")) {
        	validate(node, null, "id");
            result = new TemplateDef(node);
        } else if (nodeName.equalsIgnoreCase("case")) {
        	validate(node, "!if,else", "id");
            result = new CaseDef(node);
        } else if (nodeName.equalsIgnoreCase("loop")) {
        	validate(node, "!list,!body", "id,item,index,maxloops,filter");
            result = new LoopDef(node);
        } else if (nodeName.equalsIgnoreCase("while")) {
        	validate(node, null, "id,!condition,index,maxloops");
        	result = new WhileDef(node);
        } else if (nodeName.equalsIgnoreCase("function")) {
        	validate(node, null, "id,!name");
        	result = new FunctionDef(node);
        } else if (nodeName.equalsIgnoreCase("return")) {
        	validate(node, null, "id");
        	result = new ReturnDef(node);
        } else if (nodeName.equalsIgnoreCase("call")) {
        	validate(node, null, "id,!name");
        	result = new CallDef(node);
        } else if (nodeName.equalsIgnoreCase("call-param")) {
        	validate(node, null, "id,!name");
        	result = new CallParamDef(node);
        } else if (nodeName.equalsIgnoreCase("include")) {
        	validate(node, "", "id,!path");
        	result = new IncludeDef(node);
        } else if (nodeName.equalsIgnoreCase("try")) {
        	validate(node, "!body,!catch", "id");
        	result = new TryDef(node);
        } else if (nodeName.equalsIgnoreCase("script")) {
        	validate(node, null, "id");
        	result = new ScriptDef(node);
        } else {
        	throw new ConfigurationException("Unexpected configuration element: " + nodeName + "!");
        }

        return result;
    }
    
    public static void validate(XmlNode node, String validTags, String validAtts) {
        if (node == null) {
            return;
        }

        // check element validity
    	if (validTags != null) {
    		Set validTagsSet = new HashSet();
    		
    		// collects valid elements from given validTags comma-separated list
    		StringTokenizer tokenizer = new StringTokenizer(validTags, ",");
    		while (tokenizer.hasMoreTokens()) {
    			String token = tokenizer.nextToken().toLowerCase();
    			if (token.startsWith("!")) {
    				String tagName = token.substring(1);
    				if ( node.getElement(tagName) == null ) {
    					throw new ConfigurationException( ErrMsg.missingTag(node.getName(), tagName) );
    				}
    				validTagsSet.add(tagName);
    			} else {
    				validTagsSet.add(token);
    			}
    		}
    		
    		// iterates through all tags and check if they are valid
    		Iterator it = node.keySet().iterator();
    		while (it.hasNext()) {
    			String tagName = ((String) it.next()).toLowerCase();
    			if (!validTagsSet.contains(tagName)) {
    				throw new ConfigurationException( ErrMsg.invalidTag(node.getName(), tagName) );
    			}
    		}
    	}
    	
    	// check attributes validity
    	if (validAtts != null) {
    		Set validAttsSet = new HashSet();
    		
    		// collects valid attributes from given validAtts comma-separated list
    		StringTokenizer tokenizer = new StringTokenizer(validAtts, ",");
    		while (tokenizer.hasMoreTokens()) {
    			String token = tokenizer.nextToken().toLowerCase();
    			if (token.startsWith("!")) {
    				String attName = token.substring(1);
    				if ( node.getAttribute(attName) == null ) {
    					throw new ConfigurationException( ErrMsg.missingAttribute(node.getName(), attName) );
    				}
    				validAttsSet.add(attName);
    			} else {
    				validAttsSet.add(token);
    			}
    		}
    		
    		// iterates through all attributes and check if they are valid
            Map attributes = node.getAttributes();
            if (attributes != null) {
                Iterator it = attributes.keySet().iterator();
                while (it.hasNext()) {
                    String attName = ((String) it.next()).toLowerCase();
                    if (!validAttsSet.contains(attName)) {
                        throw new ConfigurationException( ErrMsg.invalidAttribute(node.getName(), attName) );
                    }
                }
            }
        }
    }

}