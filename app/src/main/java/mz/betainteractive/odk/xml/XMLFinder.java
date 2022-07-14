package mz.betainteractive.odk.xml;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XMLFinder {

    public static XFormDef getODKForm(File file) {

        if (file.exists()) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            try {

                // optional, but recommended
                // process XML securely, avoid attacks like XML External Entities (XXE)
                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(file);

                // optional, but recommended
                // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
                doc.getDocumentElement().normalize();

                String formId = null;
                String formName = null;
                String formVersion = null;

                // get nodes
                NodeList titleNodes = doc.getElementsByTagName("h:title");
                NodeList instanceNodes = doc.getElementsByTagName("instance");


                Node titLeNode = titleNodes.item(0);
                formName = titLeNode.getTextContent();


                //get sub nodes
                Node nodeInstance = instanceNodes.item(0);
                NodeList instanceChilds = nodeInstance.getChildNodes();
                Node mainFormNode = instanceChilds.item(0);

                Log.d("form_node", "name="+mainFormNode.getNodeName());

                if (mainFormNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element elementMainNode = (Element) mainFormNode;
                    formId = elementMainNode.getAttribute("id");
                    formVersion = elementMainNode.getAttribute("version");
                }

                if (formId != null) {
                    XFormDef formDef = new XFormDef(formId, formName, formVersion, file.getAbsolutePath());

                    return formDef;
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}

