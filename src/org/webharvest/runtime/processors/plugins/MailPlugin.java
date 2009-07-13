package org.webharvest.runtime.processors.plugins;

import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

/**
 * Sample plugin
 */
public class MailPlugin extends WebHarvestPlugin {

    public String getName() {
        return "mail";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
        Email email = new SimpleEmail();
        email.setHostName( evaluateAttribute("smtp-host", scraper) );
        email.setSmtpPort( evaluateAttributeAsInteger("smtp-port", 25, scraper) );
        try {
            email.setFrom( evaluateAttribute("from", scraper) );
        } catch (EmailException e) {
            e.printStackTrace();
        }
        for ( String to:  CommonUtil.tokenize(evaluateAttribute("to", scraper), ",") ) {
            try {
                email.addTo(to);
            } catch (EmailException e) {
                e.printStackTrace();
            }
        }

        email.setSubject( evaluateAttribute("subject", scraper) );

        String username = evaluateAttribute("username", scraper);
        String password = evaluateAttribute("password", scraper);
        if ( !CommonUtil.isEmptyString(username) ) {
            email.setAuthentication(username, password);
        }

        String security = evaluateAttribute("security", scraper);
        if ("tsl".equals(security)) {
            email.setTLS(true);
        } else if ("ssl".equals(security)) {
            email.setSSL(true);
        }

        try {
            email.setMsg("This is a simple test of commons-email");
        } catch (EmailException e) {
            e.printStackTrace();
        }

        try {
            email.send();
        } catch (EmailException e) {
            e.printStackTrace();
        }

        BaseProcessor[] processors = getSubprocessors(scraper);

        return new NodeVariable(getName());
    }

    public String[] getValidAttributes() {
        return new String[] {"smtp-host", "smtp-port", "from", "reply-to", "to", "cc", "bcc", "subject", "charset", "username", "password", "security"};
    }

    public String[] getRequiredAttributes() {
        return new String[] {"smtp-host", "from", "to"};
    }

    public String[] getValidSubprocessors() {
        return null;
    }

    public String[] getRequiredSubprocessors() {
        return null;
    }

}