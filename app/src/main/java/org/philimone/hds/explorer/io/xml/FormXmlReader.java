package org.philimone.hds.explorer.io.xml;

import android.util.Log;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 8/12/16.
 */
public class FormXmlReader {
    private File xmlFile;
    private Map<String, String> contentValues;

    public FormXmlReader(){
        this.contentValues = new LinkedHashMap<>();
    }

    public static Map<String, String> getXmlData(String xmlFilePath) {
        FormXmlReader reader = new FormXmlReader();
        return reader.getXmlContentValues(xmlFilePath);
    }

    public static Map<String, String> getXmlData(InputStream xmlInputStream) {
        FormXmlReader reader = new FormXmlReader();
        return reader.getXmlContentValues(xmlInputStream);
    }

    private void readFile(String xmlFilePath){
        try {

            SAXBuilder builder = new SAXBuilder();
            xmlFile = new File(xmlFilePath);

            org.jdom2.Document doc = (org.jdom2.Document) builder.build(xmlFile);
            org.jdom2.Element element = (org.jdom2.Element) doc.getRootElement();

            //Log.d("xmlFile", xmlFilePath);
            //Log.d("element", element.getName() +", "+element);

            readXmlNode(element);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        }
    }

    private void readFile(InputStream xmlInputStream){
        try {

            SAXBuilder builder = new SAXBuilder();

            org.jdom2.Document doc = (org.jdom2.Document) builder.build(xmlInputStream);
            org.jdom2.Element element = (org.jdom2.Element) doc.getRootElement();

            //Log.d("xmlFile", xmlFilePath);
            //Log.d("element", element.getName() +", "+element);

            readXmlNode(element);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getXmlContentValues(String xmlFilePath){
        readFile(xmlFilePath);
        return getContentValues();
    }

    public Map<String, String> getXmlContentValues(InputStream xmlInputStream){
        readFile(xmlInputStream);
        return getContentValues();
    }

    public Map<String, String> getContentValues(){
        return this.contentValues;
    }

    private void readXmlNode(org.jdom2.Element node) {
        List<Element> childElements = node.getChildren();

        for (org.jdom2.Element n : childElements) {

            if (n.getChildren().size()==0) {

                String name = n.getName();
                String value = n.getValue();
                //Log.d("node", "name:"+n.getName()+", isRoot="+(n.getChildren().size()>0)+", "+n.getValue());

                this.contentValues.put(name, value);

            }else if (n.getChildren().size()>0){
                readXmlNode(n);
            }
        }
    }

}
