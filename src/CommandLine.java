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

import org.apache.log4j.PropertyConfigurator;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.Scraper;

import java.io.FileNotFoundException;
import java.util.Properties;

public class CommandLine {
	
	private static String getArgValue(String[] args, String name) {
		for (int i = 0; i < args.length; i++) {
			String curr = args[i];
			int eqIndex = curr.indexOf('=');
			if (eqIndex >= 0) {
				String argName = curr.substring(0, eqIndex).trim(); 
				String argValue = curr.substring(eqIndex+1).trim();
				
				if (argName.toLowerCase().startsWith(name.toLowerCase())) {
					return argValue;
				}
			}
		}
		
		return ""; 
	}
	
    public static void main(String[] args) throws FileNotFoundException {
        String configFilePath = getArgValue(args, "config");
        if ("".equals(configFilePath)) {
        	System.err.println("You must specify configuration file path using config=<path> argument!");
        	System.exit(1);
        }
        
        String workingDir = getArgValue(args, "workdir");
        if ("".equals(workingDir)) {
        	System.err.println("You must specify working directory path using workdir=<path> argument!");
        	System.exit(1);
        }
        
        String isDebug = getArgValue(args, "debug");
        
    	Properties props = new Properties();
    	props.setProperty("log4j.rootLogger", "INFO, stdout");
    	props.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
    	props.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
    	props.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%-5p (%20F:%-3L) - %m\n");

    	props.setProperty("log4j.appender.file", "org.apache.log4j.DailyRollingFileAppender");
    	props.setProperty("log4j.appender.file.File", workingDir + "/out.log");
    	props.setProperty("log4j.appender.file.DatePattern", "yyyy-MM-dd");
    	props.setProperty("log4j.appender.file.layout", "org.apache.log4j.PatternLayout");
    	props.setProperty("log4j.appender.file.layout.ConversionPattern", "%-5p (%20F:%-3L) - %m\n");
      
        PropertyConfigurator.configure(props);

        ScraperConfiguration config = new ScraperConfiguration(configFilePath);
        Scraper scraper = new Scraper(config, workingDir);
        
        if ("yes".equalsIgnoreCase(isDebug)) {
        	scraper.setDebug(true);
        }
        
        String proxyHost = getArgValue(args, "proxyHost");
        if (!"".equals(proxyHost)) {
        	String proxyPort = getArgValue(args, "proxyPort");
        	if (!"".equals(proxyPort)) {
        		int port = Integer.parseInt(proxyPort);
        		scraper.getHttpClientManager().setHttpProxy(proxyHost, port);
        	} else {
        		scraper.getHttpClientManager().setHttpProxy(proxyHost);
        	}
        }

        String proxyUser = getArgValue(args, "proxyUser");
        if (!"".equals(proxyUser)) {
            String proxyPassword = getArgValue(args, "proxyPassword");
            scraper.getHttpClientManager().setHttpProxyCredentials(proxyUser, proxyPassword);
        }

        scraper.execute();
    }

}