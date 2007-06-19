package org.webharvest.utils;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.*;

/**
 * XML utils - contains common logic for XML handling
 */
public class XmlUtil {

    public static void prettyPrintXml(Document doc, Writer writer) throws IOException {
        try {
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            serializer.setOutputProperty(OutputKeys.METHOD, "xml");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.transform(domSource, streamResult);
        } catch (TransformerException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String prettyPrintXml(String xmlAsString) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse( new InputSource(new StringReader(xmlAsString)) );
        StringWriter writer = new StringWriter();

        prettyPrintXml(doc, writer);

        String result = writer.toString();

        writer.close();
        return result;
    }

}
