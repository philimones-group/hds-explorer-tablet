package mz.betainteractive.odk.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mz.betainteractive.odk.FormsProviderAPI;
import mz.betainteractive.odk.InstanceProviderAPI;
import mz.betainteractive.odk.listener.OdkFormLoadListener;
import mz.betainteractive.odk.model.FilledForm;
import mz.betainteractive.utilities.StringUtil;

import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.philimone.hds.explorer.model.Member;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;


public class OdkGeneratedFormLoadTask extends AsyncTask<Void, Void, Boolean> {

	private static String FORMS_PATH = "org.philimone.hds.explorer";

    private final String householdPrefix = "Household.";
    private final String memberPrefix = "Member.";
    private final String userPrefix = "User.";
    private final String regionPrefix = "Region.";
    private final String constPrefix = "#.";
    private final String specialConstPrefix = "$.";
    private final String repeatGroupAttribute = "jr:template";

    private OdkFormLoadListener listener;
    private ContentResolver resolver;
    private Uri odkUri;
    private FilledForm filledForm;
    private boolean openingSavedUri;
    private Context mContext;
    
    
    public OdkGeneratedFormLoadTask(Context context, FilledForm filledForm, OdkFormLoadListener listener) {
        this.listener = listener;
        this.resolver = context.getContentResolver();
        this.filledForm = filledForm;
        this.mContext = context;
    }

    public OdkGeneratedFormLoadTask(Context context, Uri uri, OdkFormLoadListener listener) { //used to open pre-existing collected forms
        this.listener = listener;
        this.resolver = context.getContentResolver();
        this.mContext = context;
        this.odkUri = uri;
        this.openingSavedUri = true;
    }

    public OdkGeneratedFormLoadTask(Context context, FilledForm filledForm, Uri uri, OdkFormLoadListener listener) { //used to open pre-existing collected forms and filling out auto-filled columns
        this.listener = listener;
        this.resolver = context.getContentResolver();
        this.mContext = context;
        this.odkUri = uri;
        this.filledForm = filledForm;
        this.openingSavedUri = true;
    }

    @Override
    protected Boolean doInBackground(Void... params) {


        if (openingSavedUri){
            if (filledForm != null){
                Log.d("saving file", ""+odkUri);
                //fill up the variables automatically
                String jrFormId = filledForm.getFormName();
                String formFilePath = getXmlFilePath(odkUri);

                Log.d("file", formFilePath);


                if (new File(formFilePath).exists()){
                    processXmlDirectly(jrFormId, formFilePath);
                    //File targetFile = saveOpenedFile(xml,jrFormId);
                }else{
                    //file doesnt exists
                    return false;
                }


            }

            return odkUri != null;
        }

        Cursor cursor = getCursorForFormsProvider(filledForm.getFormName());

        //creating a new xml auto filled file and saving it
        if (cursor.moveToFirst()) {
            String jrFormId = cursor.getString(0);
            String formFilePath = cursor.getString(1);
            String formVersion = cursor.getString(2);

            Log.d("file", formFilePath);
            Log.d("loading forms", ""+cursor.toString()+", "+cursor.getColumnName(0)+", formVersion="+formVersion);

            /*
            try {
                Scanner scanner = new Scanner(new File(formFilePath));
                int n=0;
                while (scanner.hasNextLine()){
                    Log.d("file"+(++n), scanner.nextLine());
                }
                scanner.close();
            }catch (Exception ex){
                ex.printStackTrace();
            }
            */

            String xml = processXml(jrFormId, formFilePath);            
            File targetFile = saveFile(xml,jrFormId);
            boolean writeFile = false;
            Log.d("xml", xml);
            if (targetFile != null) {
            	writeFile = writeContent(targetFile, filledForm.getFormName(), jrFormId, formVersion);
            }

            //Log.d("finished", "creating file");

            cursor.close();
            return writeFile;
        }

        cursor.close();
        return false;
    }

    private File saveOpenedFile(String xml, String xmlFilePath){
        File targetFile = new File(xmlFilePath);
        if (targetFile.exists()) {
            try {
                FileWriter writer = new FileWriter(targetFile);
                writer.write(xml);
                writer.close();
            } catch (IOException e) {
                return null;
            }
        }

        return  targetFile;
    }

    private String getXmlFilePath(Uri contentUri){
        Cursor cursor = resolver.query(contentUri, new String[] { InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH }, null, null, null);

        String xmlFilePath = "";
        Log.d("Running check form", "");

        if (cursor.moveToNext()) {
            //Log.d("move next", ""+cursor.getString(0));
            xmlFilePath = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH)); //used to read the xml file
        } else {
            Log.d("move next", "couldnt find executed form");
        }

        try{
            cursor.close();
        }catch(Exception e){
            System.err.println("Exception while trying to close cursor !");
            e.printStackTrace();
        }

        return xmlFilePath;
    }

    private Cursor getCursorForFormsProvider(String name) {
        return resolver.query(FormsProviderAPI.FormsColumns.CONTENT_URI, new String[] {
                FormsProviderAPI.FormsColumns.JR_FORM_ID, FormsProviderAPI.FormsColumns.FORM_FILE_PATH, FormsProviderAPI.FormsColumns.JR_VERSION },
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
                //Log.d("node", ""+node.getNodeName());
                sbuilder.append("<"+jrFormId+" id=\"" + jrFormId + "\">" + "\r\n"); // version="161103141"
            } else {
            	sbuilder.append("<data id=\"" + jrFormId + "\">" + "\r\n");
            }

            processNodeChildren(node, sbuilder);
            Log.d("processXml","finished!");

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
        //Log.d("executing-pnc",""+params);
        for (int i = 0; i < childElements.getLength(); i++) {
            Node n = childElements.item(i);
            Log.d("odk-xml-param", ""+n.getNodeName()+", "+n.getNodeValue()+", repeat="+isRepeatGroup(n));
            if (n.getNodeType() == Node.ELEMENT_NODE) {
            	
                String name = n.getNodeName();

                //Log.d("odk-xml-param", ""+name+", "+n.getNodeValue()+", "+n.getNodeType());

                if (params.contains(name)) {
                    NodeList nodeRepeatChilds = null;

                    if (filledForm.isMemberRepeatGroup(name)){ //is a repeat group with auto-filled members
                        nodeRepeatChilds = n.getChildNodes();
                        int count = filledForm.getMembersCount(name);

                        if (count > 0){
                            //Map the default variable "repeatGroupName[Count]"
                            sbuilder.append("<"+name+"Count>" + count + "</"+name+"Count>" + "\r\n");
                        }
                    }

                    //checking special repeat groups
                    if (filledForm.isAllMembersRepeatGroup(name)){
                        //map all members using <name></name>
                        createMembers(sbuilder, name, nodeRepeatChilds, filledForm.getRepeatGroupMapping(name), filledForm.getHouseholdMembers());
                    } else if (filledForm.isResidentMemberRepeatGroup(name)){
                        createMembers(sbuilder, name, nodeRepeatChilds, filledForm.getRepeatGroupMapping(name), filledForm.getResidentMembers());
                    } else if (filledForm.isDeadMembersRepeatGroup(name)){
                        createMembers(sbuilder, name, nodeRepeatChilds, filledForm.getRepeatGroupMapping(name), filledForm.getDeadMembers());
                    } else if (filledForm.isOutMigMembersRepeatGroup(name)){
                        createMembers(sbuilder, name, nodeRepeatChilds, filledForm.getRepeatGroupMapping(name), filledForm.getOutmigMembers());
                    } else {
                        Object value = filledForm.get(name);
                        sbuilder.append(value==null ? "<"+name+" />" + "\r\n" : "<"+name+">" + value + "</"+name+">" + "\r\n");
                    }
                    
                } else if(name.equalsIgnoreCase("start")){
                    sbuilder.append("<"+name+">" + getStartTimestamp() + "</"+name+">" + "\r\n");
                } else if(name.equalsIgnoreCase("deviceId")){
                    sbuilder.append("<"+name+">" + getDeviceId() + "</"+name+">" + "\r\n");
                } else if (isRepeatCountVar(name)) {
                    Log.d("repeat_count", name);
                } else {
                    if (!n.hasChildNodes())
                        sbuilder.append("<" + name + " />" + "\r\n");
                    else {
                        if (!isRepeatGroup(n)) { //dont group repeat groups without mapping
                            sbuilder.append("<" + name + ">" + "\r\n");
                            processNodeChildren(n, sbuilder);
                        }
                    }
                }
            }
        }
        //Log.d("finished", sbuilder.toString());
        
        sbuilder.append("</" + node.getNodeName() + ">" + "\r\n");
    }

    /**
     *
     * @param sbuilder
     * @param repeatGroupName
     * @param nodeRepeatChilds
     * @param repeatGroupMapping contains mapping to Member columns only - variableName:Member.columnName
     * @param members
     */
    private void createMembers(StringBuilder sbuilder, String repeatGroupName, NodeList nodeRepeatChilds, Map<String, String> repeatGroupMapping, List<Member> members) {

        if (nodeRepeatChilds == null) return;

        for (Member m : members){
            sbuilder.append("<" + repeatGroupName + ">" + "\r\n");

            //This is not including GROUPS
            for (int i = 0; i < nodeRepeatChilds.getLength(); i++) {
                Node n = nodeRepeatChilds.item(i);
                String name = n.getNodeName();
                Log.d("inner-node"+i, ""+name);
                if (n.getNodeType() == Node.ELEMENT_NODE){
                    if (repeatGroupMapping.containsKey(name)) {
                        String value = repeatGroupMapping.get(name);
                        String var = value.replace(memberPrefix, "");

                        sbuilder.append("<"+name+">" + m.getValueByName(var) + "</"+name+">" + "\r\n"); //map value for mapped variables
                    }else {
                        if (!n.hasChildNodes())
                            sbuilder.append("<" + name + " />" + "\r\n"); //without mapping defined
                            //n.getNodeValue(); //not doing anything, didnt want to break the  if
                        else {
                            sbuilder.append("<" + name + ">" + "\r\n"); //Its a group within
                            createMemberProcessChilds(n, sbuilder, repeatGroupMapping, m);
                            sbuilder.append("</" + name + ">" + "\r\n"); //Closing the group
                        }
                    }
                }
            }

            sbuilder.append("</" + repeatGroupName + ">" + "\r\n");
        }
    }

    private void createMemberProcessChilds(Node node, StringBuilder sbuilder, Map<String, String> repeatGroupMapping, Member member){
        NodeList childElements = node.getChildNodes();

        List<String> params = filledForm.getVariables();
        //Log.d("executing-pnc",""+params);
        for (int i = 0; i < childElements.getLength(); i++) {
            Node n = childElements.item(i);
            String name = n.getNodeName();
            Log.d("inner-node2-FW"+i, ""+name);
            if (n.getNodeType() == Node.ELEMENT_NODE){
                if (repeatGroupMapping.containsKey(name)) {
                    String value = repeatGroupMapping.get(name);
                    String var = value.replace(memberPrefix, "");

                    sbuilder.append("<"+name+">" + member.getValueByName(var) + "</"+name+">" + "\r\n"); //map value for mapped variables
                }else {
                    if (!n.hasChildNodes())
                        sbuilder.append("<" + name + " />" + "\r\n"); //without mapping defined
                        //n.getNodeValue(); //not doing anything, didnt want to break the  if
                    else {
                        sbuilder.append("<" + name + ">" + "\r\n"); //Its a group within
                        createMemberProcessChilds(n, sbuilder, repeatGroupMapping, member);
                        sbuilder.append("</" + name + ">" + "\r\n"); //Closing the group
                    }
                }
            }
        }
    }

    private String getAttributes(Node node){
        String attr = "";
        NamedNodeMap map = node.getAttributes();
        for (int i = 0; i < map.getLength(); i++) {
            Node n = map.item(i);
            attr += n.getNodeName()+":"+n.getNodeValue()+":"+n.getTextContent()+",  ";
        }

        return attr;
    }

    private boolean isRepeatGroup(Node node) {
        NamedNodeMap map = node.getAttributes();

        Node n = (map!=null) ? map.getNamedItem(repeatGroupAttribute) : null;

        return n != null;
    }

    private boolean isRepeatCountVar(String node){
        if (node != null && node.endsWith("Count")){
            return filledForm.isMemberRepeatGroup(node.replace("Count", ""));
        }

        return false;
    }

    /*
     * Updates a pre-existent XML File, only updates some mapped variables
     */
    private void processXmlDirectly(String jrFormId, String formFilePath) {

        //Log.d("filledForm",""+filledForm.getValues());
        try {

            SAXBuilder builder = new SAXBuilder();
            File xmlFile = new File(formFilePath);

            org.jdom2.Document doc = (org.jdom2.Document) builder.build(xmlFile);
            org.jdom2.Element element = (org.jdom2.Element) doc.getRootElement();

            //Log.d("element", element.getName() +", "+element);

            processNodeChildrenDirectly(element);

            //saving xml file
            org.jdom2.output.XMLOutputter xmlOutput = new org.jdom2.output.XMLOutputter();
            xmlOutput.setFormat(org.jdom2.output.Format.getPrettyFormat());
            xmlOutput.output(doc, new FileWriter(xmlFile));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        }
    }

    private void processNodeChildrenDirectly(org.jdom2.Element node) {
        List<org.jdom2.Element> childElements = node.getChildren();

        List<String> params = filledForm.getVariables();
        //Log.d("executing-pnc",""+childElements.toString());

        for (int i = 0; i < childElements.size(); i++) {
            org.jdom2.Element n = childElements.get(i);

            if (n.getChildren().size()==0) {

                String name = n.getName();
                //Log.d("node", "name:"+n.getName()+", isRoot="+(n.getChildren().size()>0)+", "+n.getValue());

                if (params.contains(name)) {
                    Object value = filledForm.get(name);
                    n.setText(value.toString());
                    //Log.d("filling", n+", "+n.getValue()+", valueToS="+value.toString()+", ");
                }

            }else if (n.getChildren().size()>0){
                processNodeChildrenDirectly(n);
            }
        }
    }

    private String getStartTimestamp(){
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        long gmt = TimeUnit.HOURS.convert(tz.getRawOffset(), TimeUnit.MILLISECONDS);

        sdf.setCalendar(cal);
        cal.setTime(new Date());


        //Log.d("timezone", "GMT "+gmt);
        //Log.d("realtime", StringUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));
        //Log.d("original-date", ""+sdf.format(cal.getTime()));

        cal.add(Calendar.HOUR_OF_DAY, (int)(-1*gmt)); //Fixing ODK Error on this variable (ODK is adding GMT Hours number to the datetime of "start" variable)

        String dateString = sdf.format(cal.getTime());
        //Log.d("fixed-datetime", ""+dateString);


        return dateString;
    }

    private String getDeviceId(){
        TelephonyManager mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

        String deviceId = mTelephonyManager.getDeviceId();
        String orDeviceId;

        if (deviceId != null ) {
            if ((deviceId.contains("*") || deviceId.contains("000000000000000"))) {
                deviceId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                orDeviceId = Settings.Secure.ANDROID_ID + ":" + deviceId;
            } else {
                orDeviceId = "imei:" + deviceId;
            }
        }
        if ( deviceId == null ) {
            // no SIM -- WiFi only
            // Retrieve WiFiManager
            WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

            // Get WiFi status
            WifiInfo info = wifi.getConnectionInfo();

            if ( info != null ) {
                deviceId = info.getMacAddress();
                orDeviceId = "mac:" + deviceId;
            }
        }
        // if it is still null, use ANDROID_ID
        if ( deviceId == null ) {
            deviceId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            orDeviceId = Settings.Secure.ANDROID_ID + ":" + deviceId;

            //sbuilder.append("<deviceId>"+ orDeviceId +"</deviceId>" + "\r\n");

            return  orDeviceId;
        }

        //sbuilder.append("<deviceId>"+ deviceId +"</deviceId>" + "\r\n");

        return deviceId;
    }

    private File saveFile(String xml, String jrFormId) {
    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
        df.setTimeZone(TimeZone.getDefault());
        String date = df.format(new Date());

        File root = Environment.getExternalStorageDirectory();
        String destinationPath = root.getAbsolutePath() + File.separator + "odk" + File.separator + "instances"+ File.separator + jrFormId + "_" + date;
        /*String destinationPath = root.getAbsolutePath() + File.separator + "Android" + File.separator + "data"
                + File.separator + FORMS_PATH + File.separator + "files"+ File.separator + jrFormId + date;*/


        File baseDir = new File(destinationPath);
                
        if (!baseDir.exists()) {
            boolean created = baseDir.mkdirs();
            if (!created) {
                return null;
            }
        }
       
        destinationPath += File.separator + jrFormId + "_" + date + ".xml";

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

    private boolean writeContent(File targetFile, String displayName, String formId, String formVersion) {

        ContentValues values = new ContentValues();
        values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, targetFile.getAbsolutePath());
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, displayName);
        values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, formId);
        if (formVersion != null){
            values.put(InstanceProviderAPI.InstanceColumns.JR_VERSION, formVersion);
        }
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
