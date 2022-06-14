package org.philimone.hds.explorer.io.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XmlCreator {

    public static String generateXml(String rootNodeName, Map<String,String> mapVariables) {

        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            Element rootElement = doc.createElement(rootNodeName);
            doc.appendChild(rootElement);

            for (Map.Entry<String, String> entry : mapVariables.entrySet()) {

                Element element = doc.createElement(entry.getKey());

                if (entry.getValue() != null){
                    element.appendChild(doc.createTextNode(entry.getValue()));
                }

                rootElement.appendChild(element);
            }

            // write the content into xml file
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            StreamResult xmlResult = new StreamResult(writer);

            transformer.transform(new DOMSource(doc), xmlResult);

            return writer.toString();

        } catch (TransformerException | ParserConfigurationException exception) {
            exception.printStackTrace();
        }

        return null;

    }
}
