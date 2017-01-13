package net.manhica.dss.explorer.io.xml;

import android.util.Log;

import net.manhica.dss.explorer.model.Member;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by paul on 8/12/16.
 */
public class FormXmlReader {
    private XPath xpath = XPathFactory.newInstance().newXPath();
    private DocumentBuilder builder;

    private Document buildDocument(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        if (builder == null) {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }

        Document doc = builder.parse(is);
        return doc;
    }

}
