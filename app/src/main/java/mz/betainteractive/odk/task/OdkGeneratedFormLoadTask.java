package mz.betainteractive.odk.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mz.betainteractive.odk.FormsProviderAPI;
import mz.betainteractive.odk.InstanceProviderAPI;
import mz.betainteractive.odk.listener.OdkFormLoadListener;
import mz.betainteractive.odk.model.FilledForm;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;


public class OdkGeneratedFormLoadTask extends AsyncTask<Void, Void, Boolean> {

	private static String FORMS_PATH = "mz.betainteractive.openingodk";
	
    private OdkFormLoadListener listener;
    private ContentResolver resolver;
    private Uri odkUri;
    private FilledForm filledForm;
    private Context mContext;
    
    
    public OdkGeneratedFormLoadTask(Context context, FilledForm filledForm, OdkFormLoadListener listener) {
        this.listener = listener;
        this.resolver = context.getContentResolver();
        this.filledForm = filledForm;
        this.mContext = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        Cursor cursor = getCursorForFormsProvider(filledForm.getFormName());
        
        if (cursor.moveToFirst()) {
            String jrFormId = cursor.getString(0);
            String formFilePath = cursor.getString(1);
            
            String xml = processXml(jrFormId, formFilePath);            
            File targetFile = saveFile(xml,jrFormId);
                                    
            if (targetFile != null) {
            	
            	boolean writeFile = writeContent(targetFile, filledForm.getFormName(), jrFormId);            	           	
            	
                return writeFile;
            }
        }
        cursor.close();

        return false;
    }

    private Cursor getCursorForFormsProvider(String name) {
        return resolver.query(FormsProviderAPI.FormsColumns.CONTENT_URI, new String[] {
                FormsProviderAPI.FormsColumns.JR_FORM_ID, FormsProviderAPI.FormsColumns.FORM_FILE_PATH },
                FormsProviderAPI.FormsColumns.JR_FORM_ID + " like ?", new String[] { name + "%" }, null);
    }

    private String processXml(String jrFormId, String formFilePath) {

        StringBuilder sbuilder = new StringBuilder();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new FileInputStream(formFilePath));

            Node node = doc.getElementsByTagName("data").item(0);
            
            if (node==null){
            	node = doc.getElementsByTagName(jrFormId).item(0);
                sbuilder.append("<"+jrFormId+" id=\"" + jrFormId + "\">" + "\r\n");
            } else {
            	sbuilder.append("<data id=\"" + jrFormId + "\">" + "\r\n");
            }

            processNodeChildren(node, sbuilder);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return sbuilder.toString();
    }

    private void processNodeChildren(Node node, StringBuilder sbuilder) {
        NodeList childElements = node.getChildNodes();

        List<String> params = filledForm.getVariables();
        
        for (int i = 0; i < childElements.getLength(); i++) {
            Node n = childElements.item(i);
            
            if (n.getNodeType() == Node.ELEMENT_NODE) {
            	
                String name = n.getNodeName();

                if (params.contains(name)) {
                	
                	Object value = filledForm.get(name);                	
                	sbuilder.append(value==null ? "<"+name+" />" + "\r\n" : "<"+name+">" + value + "</"+name+">" + "\r\n");
                    
                } else {
                    if (!n.hasChildNodes())
                        sbuilder.append("<" + name + " />" + "\r\n");
                    else {
                        sbuilder.append("<" + name + ">" + "\r\n");
                        processNodeChildren(n, sbuilder);
                    }
                }
            }
        }
        
        sbuilder.append("</" + node.getNodeName() + ">" + "\r\n");
    }

    private File saveFile(String xml, String jrFormId) {
    	DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
        df.setTimeZone(TimeZone.getDefault());
        String date = df.format(new Date());

        File root = Environment.getExternalStorageDirectory();
        String destinationPath = root.getAbsolutePath() + File.separator + "Android" + File.separator + "data"
                + File.separator + FORMS_PATH + File.separator + "files"+ File.separator + jrFormId + date;

        File baseDir = new File(destinationPath);
                
        if (!baseDir.exists()) {
            boolean created = baseDir.mkdirs();
            if (!created) {
                return null;
            }
        }
       
        destinationPath += File.separator + date + ".xml";
        File targetFile = new File(destinationPath);
        if (!targetFile.exists()) {
            try {
                FileWriter writer = new FileWriter(targetFile);
                writer.write(xml);
                writer.close();
            } catch (IOException e) {
                return null;
            }
        }
        return targetFile;
    }

    private boolean writeContent(File targetFile, String displayName, String formId) {

        ContentValues values = new ContentValues();
        values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, targetFile.getAbsolutePath());
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, displayName);
        values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, formId);
        odkUri = resolver.insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values);
        if (odkUri == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        if (result)
            listener.onOdkFormLoadSuccess(odkUri);
        else
            listener.onOdkFormLoadFailure();
    }
}
