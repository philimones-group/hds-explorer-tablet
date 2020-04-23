package mz.betainteractive.odk.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mz.betainteractive.odk.FormsProviderAPI;
import mz.betainteractive.odk.InstanceProviderAPI;
import mz.betainteractive.odk.listener.OdkFormLoadListener;
import mz.betainteractive.odk.model.FilledForm;

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

        /* finding odk form on odk database */
        String jrFormId = null;
        String formFilePath = null;
        String formVersion = null;
        String savedFormFilePath = openingSavedUri ? getXmlFilePath(odkUri) : null;
        Cursor cursor = null;

        try {
            cursor = getCursorForFormsProvider(filledForm.getFormName());
            if (cursor.moveToNext()){
                jrFormId = cursor.getString(0);
                formFilePath = cursor.getString(1);
                formVersion = cursor.getString(2);
            }
            cursor.close();
        }catch (Exception ex){

            ex.printStackTrace();

            if (cursor != null && !cursor.isClosed()){
                cursor.close();
            }
            //file not found
            return false;
        }
        /* ends here - finding odk form on odk database */

        Log.d("loading forms", "form_id="+jrFormId+",ver="+formVersion+", path="+formFilePath);


        if (openingSavedUri) {

            //**** ReOpen a saved ODK Form ****//

            Log.d("loading uri", "" + odkUri);
            Log.d("loading path", "" + savedFormFilePath);

            if (new File(savedFormFilePath).exists()) {

                //WE WILL JUST REOPEN THE SAVED ODK XML

                //processExistingXml(jrFormId, formVersion, formFilePath, savedFormFilePath);
                //File targetFile = saveOpenedFile(xml,jrFormId);
            } else {
                //file doesnt exists
                return false;
            }


            return odkUri != null;

        } else {

            //**** Open a New ODK Form ****//

            String xml = processNewXml(jrFormId, formVersion, formFilePath);
            File targetFile = saveFile(xml, jrFormId);
            boolean writeFile = false;
            //Log.d("xml", xml);
            if (targetFile != null) {
                writeFile = writeContent(targetFile, filledForm.getFormName(), jrFormId, formVersion);
            }

            //Log.d("finished", "creating file");

            return writeFile;
        }

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

    public String getXmlFilePath(){
        return getXmlFilePath(this.odkUri);
    }

    private Cursor getCursorForFormsProvider(String name) {
        return resolver.query(FormsProviderAPI.FormsColumns.CONTENT_URI, new String[] {
                FormsProviderAPI.FormsColumns.JR_FORM_ID, FormsProviderAPI.FormsColumns.FORM_FILE_PATH, FormsProviderAPI.FormsColumns.JR_VERSION },
                FormsProviderAPI.FormsColumns.JR_FORM_ID + " like ?", new String[] { name + "%" }, null);
    }

    private String processNewXml(String jrFormId, String formVersion, String formFilePath) {

        StringBuilder sbuilder = new StringBuilder();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new FileInputStream(formFilePath));

            Node node = doc.getElementsByTagName("data").item(0);
            formVersion = formVersion==null ? "" : " version=\""+ formVersion +"\"";
            
            if (node==null){
            	node = doc.getElementsByTagName(jrFormId).item(0);
                //Log.d("node", ""+node.getNodeName());
                sbuilder.append("<"+jrFormId+" id=\"" + jrFormId + "\""+ formVersion +">" + "\r\n"); // version="161103141"
            } else {
            	sbuilder.append("<data id=\"" + jrFormId + "\""+ formVersion +">" + "\r\n");
            }

            processNewNodeChildren(node, sbuilder);
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

    private void processNewNodeChildren(Node node, StringBuilder sbuilder) {
        NodeList childElements = node.getChildNodes();

        List<String> params = filledForm.getVariables();
        //Log.d("executing-pnc",""+params);
        for (int i = 0; i < childElements.getLength(); i++) {
            Node n = childElements.item(i);
            //Log.d("odk-xml-param", ""+n.getNodeName()+", "+n.getNodeValue()+", repeat="+isRepeatGroup(n));
            if (n.getNodeType() == Node.ELEMENT_NODE) {
            	
                String name = n.getNodeName();

                //Log.d("odk-xml-param", ""+name+", "+n.getNodeValue()+", "+n.getNodeType());

                if (params.contains(name)) {
                    NodeList nodeRepeatChilds = null;

                    if (filledForm.isMemberRepeatGroup(name)){ //is a repeat group with auto-filled members
                        nodeRepeatChilds = n.getChildNodes();

                        /*
                        int count = filledForm.getMembersCount(name);
                        //Log.d("sdd", "count="+count);
                        if (count > 0){
                            //Map the default variable "repeatGroupName[Count]"
                            sbuilder.append("<"+name+"_count>" + count + "</"+name+"_count>" + "\r\n");
                        }
                         */
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
                        //its a normal variable (not a repeat group)
                        Object value = filledForm.get(name);
                        sbuilder.append(value==null ? "<"+name+" />" + "\r\n" : "<"+name+">" + value + "</"+name+">" + "\r\n");
                    }
                    
                } else if(name.equalsIgnoreCase("start")){
                    sbuilder.append("<"+name+">" + getStartTimestamp() + "</"+name+">" + "\r\n");
                } else if(name.equalsIgnoreCase("deviceId")){
                    sbuilder.append("<"+name+">" + getDeviceId() + "</"+name+">" + "\r\n");
                } else if (isRepeatCountVar(name)) {
                    Log.d("repeat_count", name);

                    //The repeat count variable is auto filled by ODK in the end of the form, leave with the default value - 1.
                    //The Form is opening normally
                    /*
                    int count = getRepeatCountValue(name);
                    sbuilder.append(count==0 ? "<"+name+" />" + "\r\n" : "<"+name+">" + count + "</"+name+">" + "\r\n");
                    */
                } else {
                    if (!n.hasChildNodes())
                        sbuilder.append("<" + name + " />" + "\r\n");
                    else {
                        if (!isRepeatGroup(n)) { //dont group repeat groups without mapping
                            sbuilder.append("<" + name + ">" + "\r\n");
                            processNewNodeChildren(n, sbuilder);
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
                //Log.d("inner-node"+i, ""+name);
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
            //Log.d("inner-node2-FW"+i, ""+name);
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
        if (node != null && node.endsWith("_count")){
            return filledForm.isMemberRepeatGroup(node.replace("_count", ""));
        }

        return false;
    }

    private int getRepeatCountValue(String nodeRepeatCountVar){
        if (nodeRepeatCountVar != null && nodeRepeatCountVar.endsWith("_count")){
            String repeat = nodeRepeatCountVar.replace("_count", "");

            if (filledForm.isMemberRepeatGroup(repeat)){
                return filledForm.getMembersCount(repeat);
            }
        }

        return 0;
    }

    /*
     * Updates a pre-existent XML File, only updates some mapped variables - OLD
     */

    /*
    private void processPreExXml(String formId, String formVersion, String odkFormFilePath, String savedOdkFormXml) {

        //Log.d("filledForm",""+filledForm.getValues());
        try {

            SAXBuilder builder = new SAXBuilder();
            File xmlFile = new File(odkFormFilePath);

            org.jdom2.Document doc = (org.jdom2.Document) builder.build(xmlFile);
            org.jdom2.Element element = (org.jdom2.Element) doc.getRootElement();

            //Log.d("element", element.getName() +", "+element);

            processPreExNodeChildren(element);

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

    private void processPreExNodeChildren(org.jdom2.Element node) {
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
                processPreExNodeChildren(n);
            }
        }
    }
    */

    /*
     * Updates a pre-existent XML File, only updates some mapped variables - NEW with RepeatGroup Support
     */
    private void processExistingXml(String formId, String formVersion, String odkFormFilePath, String savedOdkFormXml){

        //1. read the content of a previous filled odk xml file
        //2. read the odk blank form - to create a new xml file
        //3. iterate the new xml file and update with values
        //4. overwrite the previous filled odk xml file with the new xml content

        //1. read the content of a previous filled odk xml file
        //-----------------------------------------------------
        Map<String, String> savedXmlValues = readExistingXml(savedOdkFormXml);

        //2. read the odk blank form - to create a new xml file
        //-----------------------------------------------------
        StringBuilder sbuilder = new StringBuilder();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new FileInputStream(odkFormFilePath));

            Node node = doc.getElementsByTagName("data").item(0);
            formVersion = formVersion==null ? "" : " version=\""+ formVersion +"\"";

            if (node==null){
                node = doc.getElementsByTagName(formId).item(0);
                //Log.d("node", ""+node.getNodeName());
                sbuilder.append("<"+formId+" id=\"" + formId + "\""+ formVersion +">" + "\r\n"); // version="161103141"
            } else {
                sbuilder.append("<data id=\"" + formId + "\""+ formVersion +">" + "\r\n");
            }

            //3. iterate the new xml file and update with values
            //--------------------------------------------------
            processExistingXmlNodeChildren(node, savedXmlValues, sbuilder);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        String newXml = sbuilder.toString();

        //4. overwrite the previous filled odk xml file with the new xml content
        //----------------------------------------------------------------------
        try {
            FileWriter writer = new FileWriter(savedOdkFormXml);
            writer.write(newXml);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("processExistingXml","finished!");
    }

    private void processExistingXmlNodeChildren(Node node, Map<String, String> savedXmlValues, StringBuilder sbuilder) {
        NodeList childElements = node.getChildNodes();

        List<String> params = filledForm.getVariables(); //content to fill the form
        //Log.d("executing-pnc",""+params);
        for (int i = 0; i < childElements.getLength(); i++) {
            Node n = childElements.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {

                String name = n.getNodeName();
                String oldValue = savedXmlValues.get(name);
                Object newValue = filledForm.get(name);

                if (newValue != null) {

                    sbuilder.append(newValue.toString().isEmpty() ? "<"+name+" />" + "\r\n" : "<"+name+">" + newValue + "</"+name+">" + "\r\n");

                } else if (oldValue != null) {

                    sbuilder.append(oldValue.isEmpty() ? "<"+name+" />" + "\r\n" : "<"+name+">" + oldValue + "</"+name+">" + "\r\n");

                } else {
                    if (!n.hasChildNodes())
                        sbuilder.append("<" + name + " />" + "\r\n");
                    else {
                        sbuilder.append("<" + name + ">" + "\r\n");
                        processExistingXmlNodeChildren(n, savedXmlValues, sbuilder);
                    }
                }
            }
        }
        //Log.d("finished", sbuilder.toString());

        sbuilder.append("</" + node.getNodeName() + ">" + "\r\n");
    }

    private Map<String, String> readExistingXml(String savedOdkFormXml){
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File(savedOdkFormXml);

        org.jdom2.Document doc = null;
        try {
            doc = builder.build(xmlFile);
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        org.jdom2.Element rootNode = doc.getRootElement();

        readExistingXmlChildNodes(map, rootNode);

        return map;
    }

    private void readExistingXmlChildNodes(Map<String, String> map, org.jdom2.Element node){
        List<org.jdom2.Element> childElements = node.getChildren();

        for (int i = 0; i < childElements.size(); i++) {
            org.jdom2.Element n = childElements.get(i);

            if (n.getChildren().size()==0) {

                String name = n.getName();
                String value = n.getValue();
                //n.setText(value.toString());

                map.put(name, value); //save the xml node content to the map

                //Log.d("node", "name:" + name + ", value:" + value +", isRoot=" + (n.getChildren().size()>0));

            }else if (n.getChildren().size()>0){
                readExistingXmlChildNodes(map, n);
            }
        }
    }
    /**/
    

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
