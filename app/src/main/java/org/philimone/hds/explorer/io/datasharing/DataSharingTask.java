package org.philimone.hds.explorer.io.datasharing;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.philimone.hds.explorer.BuildConfig;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.converters.StringCollectionConverter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

/**
 *
 * DataSharingTask responsible for data transfer between HDS-Explorer mobile devices
 * Data is transferred using XML and an embedded JSON parsed object that will be converted to equivalent classes
 *
 * The XML is represented like:
 * <data app="hds-explorer" protocol="data-sharing" command="get-device-info"> *
 * </data>
 *
 * Commands used to transfer datasets
 * 1. get-data-info    - its a request for information about the device
 * 2. post-data-info   - its the response of the request "get-data-info", it comes with a content
 * 3. get-shared-data  - its a request for shared data information
 * 4. post-shared-data - its the responseof the request "get-shared-data", it comes with a content
 *
 * The content that will be interchanged
 * 1. <deviceinfo>
 *      <uuid></uuid>
 *      <name></name>
 *      <user></user>
 *      <appVersion></appVersion>
 *    </deviceinfo>
 *
 * 2. <shareddata>
 *       <regions>
 *         <region>
 *            JSON Content of a Region Object
 *         </region>
 *       </regions>
 *       <households>
 *       </households>
 *       <members>
 *       </members>
 *    </shareddata>
 */

public class DataSharingTask extends AsyncTask<Void, DataSharingTask.PublishingReport, DataSharingTask.ExecutionReport> {

    private static final String APP_NAME = "hds-explorer";
    private static final String PROTOCOL = "data-sharing";
    private static final String TAG_DATA = "data";
    private static final String TAG_DEVICE_INFO = "deviceinfo";
    private static final String TAG_DEVICE_INFO_UUID = "uuid";
    private static final String TAG_DEVICE_INFO_NAME = "name";
    private static final String TAG_DEVICE_INFO_USER = "user";
    private static final String TAG_DEVICE_INFO_APPVERSION = "appVersion";
    private static final String TAG_SHARED_DATA = "shareddata";
    private static final String TAG_SHARED_DATA_REGIONS = "regions";
    private static final String TAG_SHARED_DATA_HOUSEHOLDS = "households";
    private static final String TAG_SHARED_DATA_MEMBERS = "members";
    private static final String XML_INITIAL_TAG = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";

    public enum Command {

        GET_DATA_INFO ("get-data-info"),
        POST_DATA_INFO ("post-data-info"),
        GET_SHARED_DATA ("get-shared-data"),
        POST_SHARED_DATA ("post-shared-data");

        String code;

        Command(String code) { this.code = code; }

        public String getId(){ return code; }

        private static final Map<String, Command> MAP = new HashMap<>();

        static {
            for (Command e: values()) MAP.put(e.code, e);
        }

        public static Command getFrom(String code) {
            return code==null ? null : MAP.get(code);
        }
    }

    private enum TaskState {
        TASK_INITIATED, POST_DATA_INFO, POST_SHARED_DATA
    }

    private Context mContext;
    private SharingDevice remoteDevice;
    private User currentUser;

    private PrintStream output;
    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;

    private Listener listener;



    public DataSharingTask(Context mContext, SharingDevice remoteDevice, Listener listener) {
        this.mContext = mContext;
        this.remoteDevice = remoteDevice;
        this.listener = listener;
        this.currentUser = Bootstrap.getCurrentUser();

        this.remoteDevice.setSharingTask(this);

        initBoxes();

        Log.d("creating datasharing", "for server = "+remoteDevice.isServer());
    }

    private void initBoxes() {
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
    }

    private void initializeStreams() {
        Log.d("initialize", "stream start");
        this.output = new PrintStream(remoteDevice.getOutputStream());
        Log.d("initialize", "stream finished");
    }

    @Override
    protected ExecutionReport doInBackground(Void... voids) {

        try {
            initializeStreams();

            //listen to input stream
            //state = SyncState.SAVING;

            Log.d("task", "call init");
            publishProgress(new PublishingReport(TaskState.TASK_INITIATED, null, null));

            //read buffer
            InputStreamReader isr = new InputStreamReader(this.remoteDevice.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            String xmlLine = "";

            while (xmlLine != null) {
                xmlLine = reader.readLine();

                //after reading the xml <data></data> separated by newline - deal with

                int eventType = -1;
                XmlPullParser parser = null;
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                parser = factory.newPullParser();
                parser.setInput(new StringReader(xmlLine));
                eventType = parser.getEventType();

                //Liste to the other device ouput
                while (eventType != XmlPullParser.END_DOCUMENT && !isCancelled()) {
                    String name = null;

                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            name = parser.getName();
                            if (name.equalsIgnoreCase(TAG_DATA)) {
                                processDataTag(parser);
                            } else if (name.equalsIgnoreCase(TAG_DEVICE_INFO)) {
                                processDeviceInfoTag(parser);
                            } else if (name.equalsIgnoreCase(TAG_SHARED_DATA)) {
                                processSharedDataTag(parser);
                            }
                            break;
                    }

                    Log.d("xmlreader", "before parser.next ("+parser.getName()+")="+parser.getEventType()+", text="+parser.getText());

                    eventType = parser.next();
                    Log.d("xmlreader2", "parser.next ("+parser.getName()+")="+parser.getEventType()+", text="+parser.getText());
                }

            }

            return new ExecutionReport(null, "success");

        } catch (Exception ex) {
            ex.printStackTrace();

            return new ExecutionReport(ex, "An error occurred");
        }

    }

    @Override
    protected void onProgressUpdate(PublishingReport... values) {
        if (values != null && values.length > 0) {
            PublishingReport report = values[0];
            if (report.state == TaskState.TASK_INITIATED) {
                listener.onDataSharingTaskStarted();
            } else if (report.state == TaskState.POST_DATA_INFO) {
                listener.onPostDataInfo(remoteDevice, report.dataInfo);
            } else if (report.state == TaskState.POST_SHARED_DATA) {
                listener.onPostSharedData(remoteDevice, report.sharedData);
            }
        }
    }

    private void handleCommands(Command command) throws IOException {
        switch (command) {
            case GET_DATA_INFO:   sendCommand(Command.POST_DATA_INFO); break;
            case GET_SHARED_DATA: sendCommand(Command.POST_SHARED_DATA); break;
            /* POST commands are handled directly in the XML reader */
        }
    }

    public boolean sendCommand(Command command) throws IOException {

        Log.d("executing command", ""+command);

        if (output != null) {
            Log.d("executing command", ""+command+", real exec");

            String code = command.code;
            String content = "";
            TaskState taskState = null;
            Map<String, String> dataInfo = new HashMap<>();
            SharedData sharedData = null;

            switch (command) {
                case GET_DATA_INFO: content = ""; break;
                case GET_SHARED_DATA: content = ""; break;
                case POST_DATA_INFO:
                    dataInfo = createDataInfo();
                    content = getDataInfoXML();
                    taskState=TaskState.POST_DATA_INFO;
                    break;
                case POST_SHARED_DATA:
                    sharedData = createSharedData();
                    content = getSharedDataXML(sharedData);
                    taskState=TaskState.POST_SHARED_DATA;
                    break;
            }

            String xml = XML_INITIAL_TAG +
                    "<data app=\"" + APP_NAME + "\" protocol=\"" + PROTOCOL + "\" command=\"" + code + "\">" +
                    "" + content +
                    "</data>";

            Log.d("xml write", xml);

            output.println(xml);

            publishProgress(new PublishingReport(taskState, dataInfo, sharedData));
        }

        return false;
    }

    private void processDataTag(XmlPullParser parser) throws Exception {
        /*
         * <data app="hds-explorer" protocol="data-sharing" command="get-device-info"> *
         * </data>
         */

        Log.d("xml processing", "DATA TAG");

        final int DATA_ATTR_APP_INDEX = 0;
        final int DATA_ATTR_PROTOCOL_INDEX = 1;
        final int DATA_ATTR_COMMAND_INDEX = 2;

        //<data>
        String appName = parser.getAttributeValue(DATA_ATTR_APP_INDEX);
        String protocol = parser.getAttributeValue(DATA_ATTR_PROTOCOL_INDEX);
        String scommand = parser.getAttributeValue(DATA_ATTR_COMMAND_INDEX);

        Command command = Command.getFrom(scommand);

        handleCommands(command);

        //if its not an empty tag will call parser.next()
        if (isTag(TAG_DATA, parser) && !isEmptyTag(TAG_DATA, parser)) {
            //parser.nextTag(); //goto next tag (</data> or <deviceinfo> or <shareddata>)
        }

    }

    private void processDeviceInfoTag(XmlPullParser parser) throws Exception {

        Log.d("xml processing", "DEVICE_INFO");

        String uuid = null;
        String name = null;
        String user = null;
        String appVersion = null;

        //<deviceinfo>

        if (notEndOfTag(TAG_DEVICE_INFO, parser)) {

            parser.nextTag(); //uuid
            if (isTag("uuid", parser) && !parser.isEmptyElementTag()) {
                parser.next();
                uuid = parser.getText();
                parser.nextTag(); //</uuid>
            } else {
                parser.nextTag(); //close tag
            }

            parser.nextTag(); //name
            if (isTag("name", parser) && !parser.isEmptyElementTag()) {
                parser.next();
                name = parser.getText();
                parser.nextTag();
            } else {
                parser.nextTag(); //close tag
            }

            parser.nextTag(); //user
            if (isTag("user", parser) && !parser.isEmptyElementTag()) {
                parser.next();
                user = parser.getText();
                parser.nextTag();
            } else {
                parser.nextTag(); //close tag
            }

            parser.nextTag(); //appVersion
            if (isTag("appVersion", parser) && !parser.isEmptyElementTag()) {
                parser.next();
                appVersion = parser.getText();
                parser.nextTag();
            } else {
                parser.nextTag(); //close tag
            }

            parser.nextTag(); //</deviceinfo>
        }

        parser.next(); //data

        //update parameters
        this.remoteDevice.setUsername(user);
        this.remoteDevice.setAppVersion(appVersion);

        Map<String,String> map = new LinkedHashMap<>();
        map.put("uuid", uuid);
        map.put("name", name);
        map.put("user", user);
        map.put("appVersion", appVersion);

        publishProgress(new PublishingReport(TaskState.POST_DATA_INFO, map, null));

    }

    private void processSharedDataTag(XmlPullParser parser) throws Exception {
        //<shareddata>

        List<Region> regionsToSaveList = new ArrayList<>();
        List<Household> householdsToSaveList = new ArrayList<>();
        List<Member> membersToSaveList = new ArrayList<>();

        if (isTag(TAG_SHARED_DATA, parser) && notEndOfTag(TAG_SHARED_DATA, parser)) {

            /* Regions Block */
            parser.nextTag(); //<regions>

            while (notEndOfTag(TAG_SHARED_DATA_REGIONS, parser)) {
                parser.nextTag(); //</regions> or <region>

                if (isTag("region", parser) && !parser.isEmptyElementTag()) {
                    Region region = parseRegion(parser);
                    region.recentlyCreated = true;
                    region.shareable = false;

                    //if this region doesn't exist insert it
                    if (this.boxRegions.query(Region_.code.equal(region.code)).build().count() == 0) {
                        regionsToSaveList.add(region);
                    }
                } else if (isTag("region", parser)){ //is going to close </region>
                    parser.nextTag();
                }
            }

            /* Households Block */
            parser.nextTag(); //<households>
            while (notEndOfTag(TAG_SHARED_DATA_HOUSEHOLDS, parser)) {
                parser.nextTag(); //</households> or <household>

                if (isTag("household", parser) && !parser.isEmptyElementTag()) {
                    Household household = parseHousehold(parser);
                    household.recentlyCreated = true;
                    household.shareable = false;

                    //if this region doesn't exist insert it
                    if (this.boxHouseholds.query(Household_.code.equal(household.code)).build().count() == 0) {
                        householdsToSaveList.add(household);
                    }
                } else if (isTag("household", parser)) {
                    parser.nextTag();
                }
            }

            /* Members Block - not being used now */
            parser.nextTag(); //<members>
            parser.nextTag(); //</members>
            parser.nextTag(); //</shareddata>
        }

        parser.next(); //</data>

        this.boxRegions.put(regionsToSaveList);
        this.boxHouseholds.put(householdsToSaveList);
        this.boxMembers.put(membersToSaveList);

        publishProgress(new PublishingReport(TaskState.POST_SHARED_DATA, null, new SharedData(regionsToSaveList, householdsToSaveList, membersToSaveList)));
    }

    private Region parseRegion(XmlPullParser parser) throws Exception {
        Region table = new Region();

        parser.nextTag(); //<code>
        if (isTag("code", parser) && !parser.isEmptyElementTag()) {
            parser.next();
            table.code = parser.getText();
            parser.nextTag(); //</code>
        } else{
            parser.nextTag(); //close tag
        }

        parser.nextTag(); //<name>
        if (isTag("name", parser) && !parser.isEmptyElementTag()) {
            parser.next();
            table.name = parser.getText();
            parser.nextTag(); //</name>
        } else{
            parser.nextTag(); //close tag
        }

        parser.nextTag(); //<level>
        if (isTag("level", parser) && !parser.isEmptyElementTag()) {
            parser.next();
            table.level = parser.getText();
            parser.nextTag(); //</level>
        } else{
            parser.nextTag(); //close tag
        }

        parser.nextTag(); //<parent>
        if (isTag("parent", parser) && !parser.isEmptyElementTag()) {
            parser.next();
            table.parent = parser.getText();
            parser.nextTag(); //</parent>
        } else{
            parser.nextTag(); //close tag
        }

        parser.nextTag(); //<collectedId>
        if (isTag("collectedId", parser) && !parser.isEmptyElementTag()) {
            parser.next();
            table.collectedId = parser.getText();
            parser.nextTag(); //</collectedId>
        } else{
            parser.nextTag(); //close tag
        }

        parser.nextTag(); //<modules>
        if (isTag("modules", parser) && !parser.isEmptyElementTag()) {
            parser.next();
            table.modules = StringCollectionConverter.getCollectionFrom(parser.getText());
            parser.nextTag(); //</modules>
        } else{
            parser.nextTag(); //close tag
        }

        Log.d("region1", "code="+table.code);
        Log.d("region2", "name="+table.name);
        Log.d("region3", "level="+table.level);
        Log.d("region4", "colid="+table.collectedId);
        Log.d("region5", "modul="+table.modules);

        parser.nextTag(); //</region>


        return table;
    }

    private Household parseHousehold(XmlPullParser parser) throws Exception {
        Household table = new Household();

        parser.nextTag(); //<code>
        if (isTag("code", parser) && !parser.isEmptyElementTag()) {
            parser.next();
            table.code = parser.getText();
            parser.nextTag(); //</code>
        } else{
            parser.nextTag(); //close tag
        }

        parser.nextTag(); //<region>
        if (isTag("region", parser) && !parser.isEmptyElementTag()) {
            parser.next();
            table.region = parser.getText();
            parser.nextTag(); //</region>
        } else{
            parser.nextTag(); //close tag
        }

        parser.nextTag(); //<name>
        if (isTag("name", parser) && !parser.isEmptyElementTag()) {
            parser.next();
            table.name = parser.getText();
            parser.nextTag(); //</name>
        } else{
            parser.nextTag(); //close tag
        }

        parser.nextTag(); //<preRegistered>
        if (isTag("preRegistered", parser) && !parser.isEmptyElementTag()) {
            parser.next();
            table.preRegistered = Boolean.parseBoolean(parser.getText());
            parser.nextTag(); //</preRegistered>
        } else{
            parser.nextTag(); //close tag
        }

        parser.nextTag(); //<collectedId>
        if (isTag("collectedId", parser) && !parser.isEmptyElementTag()) {
            parser.next();
            table.collectedId = parser.getText();
            parser.nextTag(); //</collectedId>
        } else{
            parser.nextTag(); //close tag
        }

        parser.nextTag(); //<modules>
        if (isTag("modules", parser) && !parser.isEmptyElementTag()) {
            parser.next();
            table.modules = StringCollectionConverter.getCollectionFrom(parser.getText());
            parser.nextTag(); //</modules>
        } else{
            parser.nextTag(); //close tag
        }

        parser.nextTag(); //</household>

        return table;
    }

    private String getDataInfoXML() {

        //get this device information and send as a XML
        /*
         * 1. <deviceinfo>
         *      <uuid></uuid>
         *      <name></name>
         *      <user></user>
         *      <appVersion></appVersion>
         *    </deviceinfo>
         */

        return "<" + TAG_DEVICE_INFO + ">" +
                "<" + TAG_DEVICE_INFO_UUID + " />" + //this information is already in the remote device
                "<" + TAG_DEVICE_INFO_NAME + " />" + //this information is already in the remote device
                "<" + TAG_DEVICE_INFO_USER + ">" + currentUser.fullName + "</" + TAG_DEVICE_INFO_USER + ">" +
                "<" + TAG_DEVICE_INFO_APPVERSION + ">" + BuildConfig.VERSION_NAME + "</" + TAG_DEVICE_INFO_APPVERSION + ">" +
               "</"+ TAG_DEVICE_INFO + ">" ;
    }

    private Map<String, String> createDataInfo() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(TAG_DEVICE_INFO_UUID, ""); //this information is already in the remote device
        map.put(TAG_DEVICE_INFO_NAME, ""); //this information is already in the remote device
        map.put(TAG_DEVICE_INFO_USER, currentUser.fullName);
        map.put(TAG_DEVICE_INFO_APPVERSION, BuildConfig.VERSION_NAME);
        return map;
    }

    private SharedData createSharedData() {
        List<Region> regions = boxRegions.query(Region_.shareable.equal(true)).build().find();
        List<Household> households = boxHouseholds.query(Household_.shareable.equal(true)).build().find();
        List<Member> members = new ArrayList<>();

        return new SharedData(regions, households, members);
    }

    private String getSharedDataXML(SharedData sharedData) {

        /* So far we are sharing recentlyCreated Regions and recentlyCreated preRegistered Households
         * Shared content should not be editable */

        /**
         * <regions>
         *     <region>
         *       variables
         *     </region>
         * </regions>
         * <households>
         * </households>
         * <members>
         * </members>
         */

        List<Region> regions = sharedData.getRegions();
        List<Household> households = sharedData.getHouseholds();

        String contentRegions = getRegionsXML(regions);
        String contentHouseholds = getHouseholdsXML(households);
        String contentMembers = "";

        return "<" + TAG_SHARED_DATA + ">" +
               "<"  + TAG_SHARED_DATA_REGIONS + ">" +
               ""    + contentRegions +
               "</" + TAG_SHARED_DATA_REGIONS + ">" +
               "<"  + TAG_SHARED_DATA_HOUSEHOLDS + ">" +
               ""    + contentHouseholds +
               "</" + TAG_SHARED_DATA_HOUSEHOLDS + ">" +
               "<"  + TAG_SHARED_DATA_MEMBERS + ">" +
               ""    + contentMembers +
               "</" + TAG_SHARED_DATA_MEMBERS + ">" +
               "</"+ TAG_SHARED_DATA + ">" ;
    }

    private String getRegionsXML(List<Region> regions) {
        String xml = "";
        for (Region region : regions) {
            xml += "<region>" +
                   "<code>" + region.code + "</code>" +
                   "<name>" + region.name + "</name>" +
                   "<level>" + region.level + "</level>" +
                   "<parent>" + region.parent + "</parent>" +
                   "<collectedId>" + region.collectedId + "</collectedId>" +
                   "<modules>" + StringCollectionConverter.getStringFrom(region.modules) + "</modules>" +
                   "</region>";
        }

        return xml;
    }

    private String getHouseholdsXML(List<Household> households) {
        String xml = "";
        for (Household h : households) {
            xml += "<household>" +
                    "<code>" + h.code + "</code>" +
                    "<region>" + h.region + "</region>" +
                    "<name>" + h.name + "</name>" +
                    /*"<headCode>" + h.headCode + "</headCode>" +
                    "<headName>" + h.headName + "</headName>" +
                    "<secHeadCode>" + h.secHeadCode + "</secHeadCode>" +
                    "<hierarchy1>" + h.hierarchy1 + "</hierarchy1>" +
                    "<hierarchy2>" + h.hierarchy2 + "</hierarchy2>" +
                    "<hierarchy3>" + h.hierarchy3 + "</hierarchy3>" +
                    "<hierarchy4>" + h.hierarchy4 + "</hierarchy4>" +
                    "<hierarchy5>" + h.hierarchy5 + "</hierarchy5>" +
                    "<hierarchy6>" + h.hierarchy6 + "</hierarchy6>" +
                    "<hierarchy7>" + h.hierarchy7 + "</hierarchy7>" +
                    "<hierarchy8>" + h.hierarchy8 + "</hierarchy8>" +
                    "<gpsAccuracy>" + h.gpsAccuracy + "</gpsAccuracy>" +
                    "<gpsAltitude>" + h.gpsAltitude + "</gpsAltitude>" +
                    "<gpsLatitude>" + h.gpsLatitude + "</gpsLatitude>" +
                    "<gpsLongitude>" + h.gpsLongitude + "</gpsLongitude>" +*/
                    "<preRegistered>" + h.preRegistered + "</preRegistered>" +
                    "<collectedId>" + h.collectedId + "</collectedId>" +
                    "<modules>" + StringCollectionConverter.getStringFrom(h.modules) + "</modules>"+
                    "</household>";
        }

        return xml;
    }

    private String getMembersXML() {
        return "";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Log.d("data sharing", "on pre execute");
    }

    @Override
    protected void onPostExecute(ExecutionReport executionReport) {
        super.onPostExecute(executionReport);

        Log.d("data sharing", "on post execute - finishing");
    }

    private boolean notEndOfTag(String element, XmlPullParser parser) throws XmlPullParserException {
        return !(element.equals(parser.getName()) && parser.getEventType() == XmlPullParser.END_TAG) && !isCancelled();
    }

    private boolean isEndTag(String element, XmlPullParser parser) throws XmlPullParserException {
        return (element.equals(parser.getName()) && parser.getEventType() == XmlPullParser.END_TAG);
    }

    private boolean isEmptyTag(String element, XmlPullParser parser) throws XmlPullParserException {
        return (element.equals(parser.getName()) && parser.isEmptyElementTag());
    }

    private boolean isTag(String tagName, XmlPullParser parser) throws XmlPullParserException {
        return (tagName.equals(parser.getName()));
    }

    class ExecutionReport {
        private boolean errors;
        private Exception exception;
        private String messageResult;

        public ExecutionReport(String messageResult) {
            this.messageResult = messageResult;
        }

        public ExecutionReport(Exception exception, String messageResult) {
            this.errors = exception != null;
            this.exception = exception;
            this.messageResult = messageResult;
        }

        public boolean hasErrors() {
            return errors;
        }

        public Exception getException() {
            return exception;
        }

        public void setException(Exception exception) {
            this.exception = exception;
        }

        public String getErrorMessage(){
            return (exception!=null) ? exception.getMessage() : null;
        }

        public String getMessageResult() {
            return messageResult;
        }

        public void setMessageResult(String messageResult) {
            this.messageResult = messageResult;
        }
    }

    class PublishingReport {
        public TaskState state;
        public Map<String,String> dataInfo;
        public SharedData sharedData;

        public PublishingReport(TaskState state, Map<String, String> dataInfo, SharedData sharedData) {
            this.state = state;
            this.dataInfo = dataInfo;
            this.sharedData = sharedData;
        }
    }

    public interface Listener {

        void onDataSharingTaskStarted();

        void onPostDataInfo(SharingDevice device, Map<String,String> contentInfo);

        void onPostSharedData(SharingDevice device, SharedData data);
    }
}
