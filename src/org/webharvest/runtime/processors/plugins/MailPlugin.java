package org.webharvest.runtime.processors.plugins;

import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.*;
import org.webharvest.utils.CommonUtil;
import org.apache.commons.mail.*;

import javax.activation.*;
import java.io.*;

/**
 * Sample plugin
 */
public class MailPlugin extends WebHarvestPlugin {

    public static DataSource createDataSourceOfVariable(Variable variable, String charset, String mimeType) throws IOException {
        if (variable != null) {
            byte[] bytes = variable.toBinary(charset);
            return new ByteArrayDataSource(bytes, mimeType);
        }
        return null;
    }

    Email email = null;

    public String getName() {
        return "mail";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
        email = null;
        
        boolean isHtml = "html".equalsIgnoreCase(evaluateAttribute("type", scraper));
        if (isHtml) {
            email = new HtmlEmail();
        } else {
            email = new SimpleEmail();
        }

        email.setHostName( evaluateAttribute("smtp-host", scraper) );
        email.setSmtpPort( evaluateAttributeAsInteger("smtp-port", 25, scraper) );

        try {
            email.setFrom( evaluateAttribute("from", scraper) );
        } catch (EmailException e) {
            e.printStackTrace();
        }

        for ( String replyTo:  CommonUtil.tokenize(evaluateAttribute("reply-to", scraper), ",") ) {
            try {
                email.addReplyTo(replyTo);
            } catch (EmailException e) {
                e.printStackTrace();
            }
        }
        for ( String to:  CommonUtil.tokenize(evaluateAttribute("to", scraper), ",") ) {
            try {
                email.addTo(to);
            } catch (EmailException e) {
                e.printStackTrace();
            }
        }
        for ( String cc:  CommonUtil.tokenize(evaluateAttribute("cc", scraper), ",") ) {
            try {
                email.addCc(cc);
            } catch (EmailException e) {
                e.printStackTrace();
            }
        }
        for ( String bcc:  CommonUtil.tokenize(evaluateAttribute("bcc", scraper), ",") ) {
            try {
                email.addBcc(bcc);
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

        String charset = evaluateAttribute("charset", scraper);
        if (CommonUtil.isEmptyString(charset)) {
            charset = scraper.getConfiguration().getCharset();
        }
        email.setCharset(charset);

        if (isHtml) {
            HtmlEmail htmlEmail = (HtmlEmail) email;
            String htmlContent = executeBody(scraper, context).toString();
            try {
                htmlEmail.setHtmlMsg(htmlContent);
            } catch (EmailException e) {
                e.printStackTrace();
            }
        } else {
            try {
                email.setMsg(executeBody(scraper, context).toString());
            } catch (EmailException e) {
                e.printStackTrace();
            }
        }

        try {
            email.send();
        } catch (EmailException e) {
            e.printStackTrace();
        }

        email = null;
        
        return new EmptyVariable();
    }

    public String[] getValidAttributes() {
        return new String[] {
                "smtp-host",
                "smtp-port",
                "type",
                "from",
                "reply-to",
                "to",
                "cc",
                "bcc",
                "subject",
                "charset",
                "username", 
                "password",
                "security"
        };
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

    public Class<WebHarvestPlugin>[] getDependantProcessors() {
        return new Class[] {
            MailAttachPlugin.class,
        };
    }

    public Email getEmail() {
        return email;
    }

}