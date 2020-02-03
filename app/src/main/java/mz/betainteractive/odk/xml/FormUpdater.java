package mz.betainteractive.odk.xml;

import android.content.ContentValues;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FormUpdater {
    private File xmlFile;
    private ContentValues values;

    public FormUpdater(){
        values = new ContentValues();
    }

    public FormUpdater(File xmlFile){
        this();
        this.xmlFile = xmlFile;
    }

    public FormUpdater(String xmlFile){
        this(new File(xmlFile));
    }

    public FormUpdater(File xmlFile, ContentValues contentValues){
        this(xmlFile);
        this.values.putAll(contentValues);
    }

    public FormUpdater(String xmlFile, ContentValues contentValues){
        this(new File(xmlFile), contentValues);
    }

    public void setContentValues(ContentValues contentValues){
        this.values.putAll(contentValues);
    }

    public void setContentValues(Map<String, Object> mapValues){
        for (String key : mapValues.keySet()) {

            this.values.put(key, mapValues.get(key).toString());
        }
    }

    public void update(){
        openAndUpdateXML(this.xmlFile, this.values);
    }

    private void openAndUpdateXML(File formXmlFile, ContentValues cvs) {

        //Log.d("filledForm",""+filledForm.getValues());
        try {

            SAXBuilder builder = new SAXBuilder();

            org.jdom2.Document doc = (org.jdom2.Document) builder.build(formXmlFile);
            Element element = (Element) doc.getRootElement();

            //Log.d("element", element.getName() +", "+element);

            updateElementNodes(element, cvs);

            //saving xml file
            org.jdom2.output.XMLOutputter xmlOutput = new org.jdom2.output.XMLOutputter();
            xmlOutput.setFormat(org.jdom2.output.Format.getPrettyFormat());
            xmlOutput.output(doc, new FileWriter(formXmlFile));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        }
    }

    private void updateElementNodes(Element node, ContentValues cvs) {
        List<Element> childElements = node.getChildren();

        List<String> params = new ArrayList<>(cvs.keySet());
        //Log.d("executing-pnc",""+childElements.toString());

        for (int i = 0; i < childElements.size(); i++) {
            Element n = childElements.get(i);

            if (n.getChildren().size()==0) {

                String name = n.getName();
                //Log.d("node", "name:"+n.getName()+", isRoot="+(n.getChildren().size()>0)+", "+n.getValue());

                if (params.contains(name)) {
                    Object value = cvs.getAsString(name);
                    n.setText(value.toString());
                    //Log.d("filling", n+", "+n.getValue()+", valueToS="+value.toString()+", ");
                }

            }else if (n.getChildren().size()>0){
                updateElementNodes(n, cvs);
            }
        }
    }

}
