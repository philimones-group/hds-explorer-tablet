package net.manhica.clip.explorer.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import net.manhica.clip.explorer.R;
import net.manhica.clip.explorer.database.Bootstrap;
import net.manhica.clip.explorer.database.Database;
import net.manhica.clip.explorer.database.DatabaseHelper;
import net.manhica.clip.explorer.database.Table;
import net.manhica.clip.explorer.model.CollectedData;
import net.manhica.clip.explorer.model.Form;
import net.manhica.clip.explorer.model.Household;
import net.manhica.clip.explorer.model.Member;
import net.manhica.clip.explorer.model.Module;
import net.manhica.clip.explorer.model.SyncReport;
import net.manhica.clip.explorer.model.User;

import mz.betainteractive.utilities.StringUtil;

/**
 * AsyncTask responsible for downloading the OpenHDS "database", that is a
 * subset of the OpenHDS database records. It does the downloading
 * incrementally, by downloading parts of the data one at a time. For example,
 * it gets all locations and then retrieves all individuals. Ordering is
 * somewhat important here, because the database has a few foreign key
 * references that must be satisfied (e.g. member references a location
 * location)
 */
public class SyncEntitiesTask extends AsyncTask<Void, Integer, String> {

	private static final String API_PATH = "/api/clip-explorer";
	private static final String ZIP_MIME_TYPE = "application/zip;charset=utf-8";
	private static final String XML_MIME_TYPE = "text/xml;charset=utf-8";

	private SyncDatabaseListener listener;

	private ProgressDialog dialog;
	private HttpURLConnection connection;

	private String baseurl;
	private String username;
	private String password;
	private List<Entity> entities;

	private final List<Table> values = new ArrayList<Table>();

	private State state;
	private Entity entity;

	private Context mContext;

	private enum State {
		DOWNLOADING, SAVING
	}

	public enum Entity {
		SETTINGS, MODULES, FORMS, USERS, MEMBERS, HOUSEHOLDS
	}

	public SyncEntitiesTask(Context context, ProgressDialog dialog, SyncDatabaseListener listener, String url, String username, String password, Entity... entityToDownload) {
		this.baseurl = url;
		this.username = username;
		this.password = password;
		this.dialog = dialog;
		this.listener = listener;
		this.mContext = context;
		this.entities = new ArrayList<>();
		this.entities.addAll(Arrays.asList(entityToDownload));
		initDialog();
	}

	public SyncEntitiesTask(Context context, ProgressDialog dialog, String url, String username, String password, Entity... entityToDownload) {
		this.baseurl = url;
		this.username = username;
		this.password = password;
		this.dialog = dialog;
		this.mContext = context;
		this.entities = new ArrayList<>();
		this.entities.addAll(Arrays.asList(entityToDownload));
		initDialog();
	}

	public SyncEntitiesTask(Context context, ProgressDialog dialog, String url, String username, String password) {
		this.baseurl = url;
		this.username = username;
		this.password = password;
		this.dialog = dialog;
		this.mContext = context;
		this.entities = new ArrayList<>();
		initDialog();
	}

	private void initDialog(){
		dialog.setMessage(mContext.getString(R.string.sync_prepare_download_lbl));
		dialog.show();
	}

	private Database getDatabase(){
		return new Database(mContext);
	}

	public void setSyncDatabaseListener(SyncDatabaseListener listener){
		this.listener = listener;
	}

	public void setEntitiesToDownload(Entity... entityToDownload){
		this.entities.clear();
		this.entities.addAll(Arrays.asList(entityToDownload));
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		StringBuilder builder = new StringBuilder();
		
		switch (state) {
		case DOWNLOADING:
			builder.append(mContext.getString(R.string.sync_downloading_lbl));
			break;
		case SAVING:
			builder.append(mContext.getString(R.string.sync_saving_lbl));
			break;
		}

		switch (entity) {
			case MODULES:
				builder.append(" " + mContext.getString(R.string.sync_modules_lbl));
				break;
			case FORMS:
				builder.append(" " + mContext.getString(R.string.sync_forms_lbl));
				break;
			case USERS:
				builder.append(" " + mContext.getString(R.string.sync_users_lbl));
				break;
			case HOUSEHOLDS:
				builder.append(" " + mContext.getString(R.string.sync_households_lbl));
				break;
			case MEMBERS:
				builder.append(" " + mContext.getString(R.string.sync_members_lbl));
				break;
		}

		if (values.length > 0) {
			String msg = ". " + mContext.getString(R.string.sync_saved_lbl) + " "  + values[0] + " " + mContext.getString(R.string.sync_records_lbl);
			if (state==State.DOWNLOADING){
				msg = ". " + mContext.getString(R.string.sync_saved_lbl) + " "  + values[0] + "KB";
			}

			builder.append(msg);
		}	

		dialog.setMessage(builder.toString());
	}

	protected String doInBackground(Void... params) {

		// at this point, we don't care to be smart about which data to
		// download, we simply download it all
		//deleteAllTables();

		try {

			for (Entity ent : entities){
				entity = ent;
				switch (entity) {
					case MODULES:
						deleteAll(Module.class);
						processUrl(baseurl + API_PATH + "/modules/zip", "modules.zip");
						break;
					case FORMS:
						deleteAll(Form.class);
						processUrl(baseurl + API_PATH + "/forms/zip", "forms.zip");
						break;
					case USERS:
						deleteAll(User.class);
						processUrl(baseurl + API_PATH + "/users/zip", "users.zip");
						break;
					case HOUSEHOLDS:
						deleteAll(Household.class);
						processUrl(baseurl + API_PATH + "/households/zip", "households.zip");
						break;
					case MEMBERS:
						deleteAll(Member.class);
						deleteAll(CollectedData.class, DatabaseHelper.CollectedData.COLUMN_SUPERVISED+"=1", null);
						processUrl(baseurl + API_PATH + "/members/zip", "members.zip");
						break;
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			//Toast.makeText(mContext, mContext.getString(R.string.sync_failure_file_not_found_lbl), Toast.LENGTH_LONG).show();
			return mContext.getString(R.string.sync_failure_file_not_found_lbl);//"Failure";//HttpTask.EndResult.FAILURE;
		} catch (Exception e) {
			e.printStackTrace();
			//Toast.makeText(mContext, mContext.getString(R.string.sync_failure_file_not_found_lbl), Toast.LENGTH_LONG).show();
			return mContext.getString(R.string.sync_failure_file_not_found_lbl);//"Failure";//HttpTask.EndResult.FAILURE;
		}


		return "Success"; //HttpTask.EndResult.SUCCESS;
	}

	private void deleteAllTables() {
		// ordering is somewhat important during delete. a few tables have
		// foreign keys
		Database database = getDatabase();
		database.open();
		
		database.delete(User.class, null, null);
		database.delete(Form.class, null, null);
		database.delete(Module.class, null, null);
		database.delete(Household.class, null, null);
		database.delete(Member.class, null, null);
		database.delete(CollectedData.class, DatabaseHelper.CollectedData.COLUMN_SUPERVISED+"=?", new String[]{ "1"}); //delete all collected data that was supervised

		database.close();
	}

	private void deleteAll(Class<? extends Table> table){
		Database database = getDatabase();
		database.open();
		database.delete(table, null, null);
		database.close();
	}

	private void deleteAll(Class<? extends Table>... tables){
		Database database = getDatabase();
		database.open();
		for (Class<? extends Table> table : tables){
			database.delete(table, null, null);
		}
		database.close();
	}

	private void deleteAll(Class<? extends Table> table, String whereClause, String[] whereClauseArgs){
		Database database = getDatabase();
		database.open();
		database.delete(table, whereClause, whereClauseArgs);
		database.close();
	}

	private void processUrl(String strUrl, String exportedFileName) throws Exception {
		state = State.DOWNLOADING;
		publishProgress();

		String basicAuth = "Basic " + new String(Base64.encode((this.username+":"+this.password).getBytes(),Base64.NO_WRAP ));

		URL url = new URL(strUrl);
		connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setReadTimeout(10000);
		connection.setConnectTimeout(15000);
		connection.setDoInput(true);
		connection.setRequestProperty("Authorization", basicAuth);

		Log.d("processing", ""+url);

		connection.connect();

		processResponse(exportedFileName);
	}

	private void processResponse(String exportedFileName) throws Exception {
		DownloadResponse response = getResponse(exportedFileName);

		Log.d("is", ""+response.getInputStream()+", xml-"+response.isXmlFile()+", zip-"+response.isZipFile());

		//save file
		InputStream fileInputStream = saveFileToStorage(response);

		if (fileInputStream != null){
			if (response.isXmlFile()){
				processXMLDocument(fileInputStream);
			}
			if (response.isZipFile()){
				processZIPDocument(fileInputStream);
			}
		}
	}

	private DownloadResponse getResponse(String exportedFileName) throws IOException {
		int response = connection.getResponseCode();
		Log.d("connection", "The response code is: " + response+", type="+connection.getContentType()+", size="+connection.getContentLength());

		InputStream is = connection.getInputStream();

		return new DownloadResponse(is, connection.getContentType(), exportedFileName, connection.getContentLength());//173916816L);
	}

	private void processXMLDocument(InputStream content) throws Exception {
		state = State.SAVING;

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		//factory.setNamespaceAware(true);
		XmlPullParser parser = factory.newPullParser();

		parser.setInput(content, null);

		int eventType = parser.getEventType();
		
		while (eventType != XmlPullParser.END_DOCUMENT && !isCancelled()) {
			String name = null;

			switch (eventType) {
			case XmlPullParser.START_TAG:
				name = parser.getName();
				if (name.equalsIgnoreCase("modules")) {
					processModulesParams(parser);
				} else if (name.equalsIgnoreCase("forms")) {
					processFormsParams(parser);
				} else if (name.equalsIgnoreCase("users")) {
					processUsersParams(parser);
				} else if (name.equalsIgnoreCase("households")) {
					processHouseholds(parser);
				} else if (name.equalsIgnoreCase("members")) {
					processMembers(parser);
				}
				break;
			}
						
			eventType = parser.next();
		}
	}

	private InputStream saveFileToStorage(DownloadResponse response) throws Exception {

		InputStream content = response.getInputStream();

		FileOutputStream fout = new FileOutputStream(Bootstrap.getAppPath() + response.getFileName());
		byte[] buffer = new byte[10*1024];
		int len = 0;
		long total = 0;

		publishProgress();

		while ((len = content.read(buffer)) != -1){
			fout.write(buffer, 0, len);
			total += len;
			int perc =  (int) ((total/(1024)));
			publishProgress(perc);
		}

		fout.close();
		content.close();

		FileInputStream fin = new FileInputStream(Bootstrap.getAppPath() + response.getFileName());

		return fin;
	}

	private void processZIPDocument(InputStream inputStream) throws Exception {

		Log.d("zip", "processing zip file");


		ZipInputStream zin = new ZipInputStream(inputStream);
		ZipEntry entry = zin.getNextEntry();

		if (entry != null){
			processXMLDocument(zin);
			zin.closeEntry();
		}

		zin.close();
	}

	private boolean notEndOfXmlDoc(String element, XmlPullParser parser) throws XmlPullParserException {
		return !(element.equals(parser.getName()) && parser.getEventType() == XmlPullParser.END_TAG) && !isCancelled();
	}

	private boolean isEndTag(String element, XmlPullParser parser) throws XmlPullParserException {
		return (element.equals(parser.getName()) && parser.getEventType() == XmlPullParser.END_TAG);
	}

	private boolean isEmptyTag(String element, XmlPullParser parser) throws XmlPullParserException {
		return (element.equals(parser.getName()) && parser.isEmptyElementTag());
	}

	private void processModulesParams(XmlPullParser parser) throws XmlPullParserException, IOException {

		//clear sync_report
		updateSyncReport(SyncReport.REPORT_MODULES, null, SyncReport.STATUS_NOT_SYNCED);

		int count = 0;
		values.clear();

		parser.nextTag(); //<module>

		while (notEndOfXmlDoc("modules", parser)) {

			count++;
			Module table = new Module();

			parser.nextTag(); //<code>
			parser.next(); //text
			table.setCode(parser.getText());
			//Log.d("aftere-tag-"+parser.getName(), "value="+parser.getText()+", event="+parser.getEventType());

            //code - startTag
			parser.nextTag(); //</code>
			parser.nextTag(); //<name>
			parser.next();    //text
			table.setName(parser.getText());
			//Log.d("2aftere-tag-"+parser.getName(), "value="+parser.getText()+", event="+parser.getEventType());

			parser.nextTag(); //</name>
			parser.nextTag(); //<description>
			parser.next();    //text
			table.setDescription(parser.getText());
			//Log.d("3aftere-tag-"+parser.getName(), "value="+parser.getText()+", event="+parser.getEventType());

			parser.nextTag(); //</description>
			parser.nextTag(); //</module>
			parser.next();    //</modules> or <module>

			values.add(table);
			publishProgress(count);


		}

		state = State.SAVING;
		entity = Entity.MODULES;

		Database database = getDatabase();
		database.open();
		if (!values.isEmpty()) {
			count = 0;
			for (Table t : values){
				count++;
				database.insert(t);
				publishProgress(count);
			}
		}
		database.close();


		updateSyncReport(SyncReport.REPORT_MODULES, new Date(), SyncReport.STATUS_SYNCED);
	}

	private void processFormsParams(XmlPullParser parser) throws XmlPullParserException, IOException {

		//clear sync_report
		updateSyncReport(SyncReport.REPORT_FORMS, null, SyncReport.STATUS_NOT_SYNCED);

		int count = 0;
		values.clear();

		Database database = getDatabase();
		database.open();
		database.beginTransaction();

		parser.nextTag(); //<form>

		while (notEndOfXmlDoc("forms", parser)) {
			count++;

			Form table = new Form();

			parser.nextTag(); //process formId
			if (!isEmptyTag("formId", parser)) {
				parser.next();
				table.setFormId(parser.getText());
				parser.nextTag(); //process </formId>
				//Log.d(count+"-formId", "value="+ parser.getText());
			}else{
				table.setFormId("");
				parser.nextTag();
			}

			parser.nextTag(); //process formName
			if (!isEmptyTag("formName", parser)) {
				parser.next();
				table.setFormName(parser.getText());
				parser.nextTag(); //process </formName>
				//Log.d(count+"-formName", "value="+ parser.getText());
			}else{
				table.setFormName("");
				parser.nextTag();
			}

			parser.nextTag(); //process formDescription
			if (!isEmptyTag("formDescription", parser)) {
				parser.next();
				table.setFormDescription(parser.getText());
				parser.nextTag(); //process </formDescription>
				//Log.d(count+"-formDescription", "value="+ parser.getText());
			}else{
				table.setFormDescription("");
				parser.nextTag();
			}

			parser.nextTag(); //process gender
			if (!isEmptyTag("gender", parser)) {
				parser.next();
				table.setGender(parser.getText());
				parser.nextTag(); //process </gender>
				//Log.d(count+"-gender", "value="+ parser.getText());
			}else{
				table.setGender("");
				parser.nextTag();
			}

			parser.nextTag(); //process minAge
			if (!isEmptyTag("minAge", parser)) {
				parser.next();
				table.setMinAge(Integer.parseInt(parser.getText()));
				parser.nextTag(); //process </minAge>
				//Log.d(count+"-minAge", "value="+ parser.getText());
			}else{
				//table.setMinAge(0);
				parser.nextTag();
			}

			parser.nextTag(); //process maxAge
			if (!isEmptyTag("maxAge", parser)) {
				parser.next();
				table.setMaxAge(Integer.parseInt(parser.getText()));
				parser.nextTag(); //process </maxAge>
				//Log.d(count+"-maxAge", "value="+ parser.getText());
			}else{
				//table.setMaxAge(0);
				parser.nextTag();
			}

			parser.nextTag(); //process modules
			if (!isEmptyTag("modules", parser)) {
				parser.next();
				table.setModules(parser.getText());
				parser.nextTag(); //process </modules>
				//Log.d(count+"-modules", "value="+ parser.getText());
			}else{
				table.setModules("");
				parser.nextTag();
			}

			parser.nextTag(); //process bindMap
			if (!isEmptyTag("bindMap", parser)) {
				parser.next();
				table.setBindMap(parser.getText());
				//Log.d(count+"-bindMap", "value="+ parser.getText());
				parser.nextTag(); //process </bindMap>
			}else{
				table.setBindMap("");
				parser.nextTag();

			}

			parser.nextTag();
			parser.next();


			database.insert(table);
			publishProgress(count);

		}

		state = State.SAVING;
		entity = Entity.FORMS;

		database.setTransactionSuccessful();
		database.endTransaction();
		database.close();

		updateSyncReport(SyncReport.REPORT_FORMS, new Date(), SyncReport.STATUS_SYNCED);
	}

	private void processUsersParams(XmlPullParser parser) throws XmlPullParserException, IOException {

		//clear sync_report
		updateSyncReport(SyncReport.REPORT_USERS, null, SyncReport.STATUS_NOT_SYNCED);

		int count = 0;
		values.clear();

		parser.nextTag();

		Database database = getDatabase();
		database.open();
		database.beginTransaction();

		while (notEndOfXmlDoc("users", parser)) {
			count++;
						
			User table = new User();

			parser.nextTag(); //process <username>
			if (!isEmptyTag("username", parser)) {
				parser.next();
				table.setUsername(parser.getText());
				parser.nextTag(); //process </username>
				//Log.d(count+"-username", "value="+ table.getUsername());
			}else{
				table.setUsername("");
				parser.nextTag();
			}

			parser.nextTag(); //process <password>
			if (!isEmptyTag("password", parser)) {
				parser.next();
				table.setPassword(parser.getText());
				parser.nextTag(); //process </password>
				//Log.d(count+"-password", "value="+ table.getPassword());
			}else{
				table.setPassword("");
				parser.nextTag();
			}

			parser.nextTag(); //process <firstName>
			if (!isEmptyTag("firstName", parser)) {
				parser.next();
				table.setFirstName(parser.getText());
				parser.nextTag(); //process </firstName>
				//Log.d(count+"-firstName", "value="+ table.getFirstName());
			}else{
				table.setFirstName("");
				parser.nextTag();
			}

			parser.nextTag(); //process <lastName>
			if (!isEmptyTag("lastName", parser)) { //its not <lastName/>
				parser.next();
				table.setLastName(parser.getText());
				parser.nextTag(); //process </lastName>
				//Log.d(count+"-lastName", "value="+ table.getLastName());
			}else{
				table.setLastName("");
				parser.nextTag();
			}

			parser.nextTag(); //process <modules>
			if (!isEmptyTag("modules", parser)) {
				parser.next();
				table.setModules(parser.getText());
				parser.nextTag(); //process </modules>
				//Log.d(count+"-modules", "value="+ table.getModules());
			}else{
				table.setModules("");
				parser.nextTag();
			}

			parser.nextTag(); //process <extras>
			if (!isEmptyTag("extras", parser)){ //its not <extras/>
				parser.next();
				table.setExtras(parser.getText());
				parser.nextTag(); // </extras>
				//Log.d(count+"-extras", "value="+ table.getExtras());
			}else{
				table.setExtras("");
				parser.nextTag();
			}


			parser.nextTag(); // <user>
			parser.next();

			//values.add(table);
			database.insert(table);

			publishProgress(count);
			
		}

		/*
		state = State.SAVING;
		entity = Entity.USERS;

		Database database = getDatabase();
		database.open();
		if (!values.isEmpty()) {
			count = 0;
			for (Table t : values){
				count++;
				database.insert(t);
				publishProgress(count);
			}
		}
		*/

		database.setTransactionSuccessful();
		database.endTransaction();
		database.close();

		updateSyncReport(SyncReport.REPORT_USERS, new Date(), SyncReport.STATUS_SYNCED);
	}

	private void processHouseholds(XmlPullParser parser) throws XmlPullParserException, IOException {

		//clear sync_report
		updateSyncReport(SyncReport.REPORT_HOUSEHOLDS, null, SyncReport.STATUS_NOT_SYNCED);

		int count = 0;
		values.clear();

		parser.nextTag();

		Database database = getDatabase();
		database.open();
		database.beginTransaction();

		while (notEndOfXmlDoc("households", parser)) {
			count++;

			Household table = new Household();

			parser.nextTag(); //process extId
			parser.next();
			table.setExtId(parser.getText());
			//Log.d(count+"-extId", "value="+ parser.getText());

			parser.nextTag(); //process headExtId
			parser.nextTag();
			parser.next();
			table.setHeadExtId(parser.getText());
			//Log.d(count+"-headExtId", "value="+ parser.getText());

			parser.nextTag(); //process houseNumber
			parser.nextTag();
			parser.next();
			table.setHouseNumber(parser.getText());
			//Log.d(count+"-houseNumber", "value="+ parser.getText());

			parser.nextTag(); //process neighborhood
			parser.nextTag();
			parser.next();
			table.setNeighborhood(parser.getText());
			//Log.d(count+"-neighborhood", "value="+ parser.getText());

			parser.nextTag(); //process locality
			parser.nextTag();
			parser.next();
			table.setLocality(parser.getText());
			//Log.d(count+"-locality", "value="+ parser.getText());

			parser.nextTag(); //process adminPost
			parser.nextTag();
			parser.next();
			table.setAdminPost(parser.getText());
			//Log.d(count+"-adminPost", "value="+ parser.getText());

			parser.nextTag(); //process district
			parser.nextTag();
			parser.next();
			table.setDistrict(parser.getText());
			//Log.d(count+"-district", "value="+ parser.getText());

			parser.nextTag(); //process province
			parser.nextTag();
			parser.next();
			table.setProvince(parser.getText());
			//Log.d(count+"-province", "value="+ parser.getText());

			parser.nextTag(); //process head
			parser.nextTag();
			parser.next();
			table.setHead(parser.getText());
			//Log.d(count+"-head", "value="+ parser.getText());

			parser.nextTag(); //process accuracy
			parser.nextTag();
			parser.next();
			table.setAccuracy(parser.getText());
			//Log.d(count+"-accuracy", "value="+ parser.getText());

			parser.nextTag(); //process altitude
			parser.nextTag();
			parser.next();
			table.setAltitude(parser.getText());
			//Log.d(count+"-altitude", "value="+ parser.getText());

			parser.nextTag(); //process latitude
			parser.nextTag();
			parser.next();
			table.setLatitude(parser.getText());
			//Log.d(count+"-latitude", "value="+ parser.getText());

			parser.nextTag(); //process longitude
			parser.nextTag();
			parser.next();
			table.setLongitude(parser.getText());
			//Log.d(count+"-longitude", "value="+ parser.getText());

			parser.nextTag();
			parser.nextTag();
			parser.next();

			//values.add(table);

			database.insert(table);

			if (count % 100 == 0){
				publishProgress(count);
			}


		}

		publishProgress(count);

		database.setTransactionSuccessful();
		database.endTransaction();
		database.close();

		updateSyncReport(SyncReport.REPORT_HOUSEHOLDS, new Date(), SyncReport.STATUS_SYNCED);
	}

	private void processMembers(XmlPullParser parser) throws XmlPullParserException, IOException {

		//clear sync_report
		updateSyncReport(SyncReport.REPORT_MEMBERS, null, SyncReport.STATUS_NOT_SYNCED);

		int count = 0;
		values.clear();

		parser.nextTag();

		Database database = getDatabase();
		database.open();
		database.beginTransaction();

		while (notEndOfXmlDoc("members", parser)) {
			count++;

			Member table = new Member();

			parser.nextTag(); //process extId
			parser.next();
			table.setExtId(parser.getText());
			//Log.d(count+"-extId", "value="+ parser.getText());

			parser.nextTag(); //process permId
			parser.nextTag();
			parser.next();
			table.setPermId(parser.getText());
			//Log.d(count+"-permId", "value="+ parser.getText());

			parser.nextTag(); //process name
			parser.nextTag();
			parser.next();
			table.setName(parser.getText());
			//Log.d(count+"-name", "value="+ parser.getText());

			parser.nextTag(); //process gender
			parser.nextTag();
			parser.next();
			table.setGender(parser.getText());
			//Log.d(count+"-gender", "value="+ parser.getText());

			parser.nextTag(); //process dob
			parser.nextTag();
			parser.next();
			table.setDob(parser.getText());
			//Log.d(count+"-dob", "value="+ parser.getText());

			parser.nextTag(); //process age
			parser.nextTag();
			parser.next();
			table.setAge(Integer.parseInt(parser.getText()));
			//Log.d(count+"-age", "value="+ parser.getText());

			parser.nextTag(); //process motherExtId
			parser.nextTag();
			parser.next();
			table.setMotherExtId(parser.getText());
			//Log.d(count+"-motherExtId", "value="+ parser.getText());

			parser.nextTag(); //process motherName
			parser.nextTag();
			parser.next();
			table.setMotherName(parser.getText());
			//Log.d(count+"-motherName", "value="+ parser.getText());

			parser.nextTag(); //process motherPermId
			parser.nextTag();
			parser.next();
			table.setMotherPermId(parser.getText());
			//Log.d(count+"-motherPermId", "value="+ parser.getText());

			parser.nextTag(); //process fatherExtId
			parser.nextTag();
			parser.next();
			table.setFatherExtId(parser.getText());
			//Log.d(count+"-fatherExtId", "value="+ parser.getText());

			parser.nextTag(); //process fatherName
			parser.nextTag();
			parser.next();
			table.setFatherName(parser.getText());
			//Log.d(count+"-fatherName", "value="+ parser.getText());

			parser.nextTag(); //process fatherPermId
			parser.nextTag();
			parser.next();
			table.setFatherPermId(parser.getText());
			//Log.d(count+"-fatherPermId", "value="+ parser.getText());

			parser.nextTag(); //process hhExtId
			parser.nextTag();
			parser.next();
			table.setHhExtId(parser.getText());
			//Log.d(count+"-hhExtId", "value="+ parser.getText());

			parser.nextTag(); //process hhNumber
			parser.nextTag();
			parser.next();
			table.setHhNumber(parser.getText());
			//Log.d(count+"-hhNumber", "value="+ parser.getText());

			parser.nextTag(); //process hhStartType
			parser.nextTag();
			parser.next();
			table.setHhStartType(parser.getText());
			//Log.d(count+"-hhStartType", "value="+ parser.getText());

			parser.nextTag(); //process hhStartDate
			parser.nextTag();
			parser.next();
			table.setHhStartDate(parser.getText());
			//Log.d(count+"-hhStartDate", "value="+ parser.getText());

			parser.nextTag(); //process hhEndType
			parser.nextTag();
			parser.next();
			table.setHhEndType(parser.getText());
			//Log.d(count+"-hhEndType", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process hhEndDate
			parser.nextTag();
			parser.next();
			table.setHhEndDate(parser.getText());
			//Log.d(count+"-hhEndDate", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process gps-accuracy
			parser.nextTag();
			parser.next();
			table.setHhAccuracy(parser.getText());
			//Log.d(count+"-Accuracy", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process gps-altitude
			parser.nextTag();
			parser.next();
			table.setHhAltitude(parser.getText());
			//Log.d(count+"-Altitude", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process gps-latitude
			parser.nextTag();
			parser.next();
			table.setHhLatitude(parser.getText());
			//Log.d(count+"-Latitude", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process gps-longitude
			parser.nextTag();
			parser.next();
			table.setHhLongitude(parser.getText());
			//Log.d(count+"-Longitude", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process nrPregnancies
			parser.nextTag();
			parser.next();
			table.setNrPregnancies(Integer.parseInt(parser.getText()));
			//Log.d(count+"-nrPregnancies", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process hasDelivered
			parser.nextTag();
			parser.next();
			table.setHasDelivered(Boolean.parseBoolean(parser.getText()));
			//Log.d(count+"-hasDelivered", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process isPregnant
			parser.nextTag();
			parser.next();
			table.setPregnant(Boolean.parseBoolean(parser.getText()));
			//Log.d(count+"-isPregnant", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process clip_id_1
			parser.nextTag();
			parser.next();
			table.setClip_id_1(parser.getText());
			//Log.d(count+"-clip_id_1", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process clip_id_2
			parser.nextTag();
			parser.next();
			table.setClip_id_2(parser.getText());
			//Log.d(count+"-clip_id_2", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process clip_id_3
			parser.nextTag();
			parser.next();
			table.setClip_id_3(parser.getText());
			//Log.d(count+"-clip_id_3", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process clip_id_4
			parser.nextTag();
			parser.next();
			table.setClip_id_4(parser.getText());
			//Log.d(count+"-clip_id_4", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process clip_id_5
			parser.nextTag();
			parser.next();
			table.setClip_id_5(parser.getText());
			//Log.d(count+"-clip_id_5", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process clip_id_6
			parser.nextTag();
			parser.next();
			table.setClip_id_6(parser.getText());
			//Log.d(count+"-clip_id_6", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process clip_id_7
			parser.nextTag();
			parser.next();
			table.setClip_id_7(parser.getText());
			//Log.d(count+"-clip_id_7", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process clip_id_8
			parser.nextTag();
			parser.next();
			table.setClip_id_8(parser.getText());
			//Log.d(count+"-clip_id_8", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process clip_id_9
			parser.nextTag();
			parser.next();
			table.setClip_id_9(parser.getText());
			//Log.d(count+"-clip_id_9", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process onPom
			parser.nextTag();
			parser.next();
			table.setOnPom(Boolean.parseBoolean(parser.getText()));
			//Log.d(count+"-onPom", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process onFacility
			parser.nextTag();
			parser.next();
			table.setOnFacility(Boolean.parseBoolean(parser.getText()));
			//Log.d(count+"-onFacility", "value="+ parser.getText());

			if (parser.getText()!=null)
			parser.nextTag(); //process onSurveillance
			parser.nextTag();
			parser.next();
			table.setOnSurveillance(Boolean.parseBoolean(parser.getText()));
			//Log.d(count+"-onSurveillance", "value="+ parser.getText());


			parser.nextTag();
			parser.nextTag();
			parser.next();

			//values.add(table);
			database.insert(table);

			if (count % 100 == 0){
				publishProgress(count);
			}

		}

		publishProgress(count);

		database.setTransactionSuccessful();
		database.endTransaction();
		database.close();

		updateSyncReport(SyncReport.REPORT_MEMBERS, new Date(), SyncReport.STATUS_SYNCED);
	}

	//database.query(SyncReport.class, DatabaseHelper.SyncReport.COLUMN_REPORT_ID+"=?", new String[]{}, null, null, null);
	private void updateSyncReport(int reportId, Date date, int status){
		Database database = getDatabase();
		database.open();

		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.SyncReport.COLUMN_DATE, date==null ? "" : StringUtil.format(date, "yyyy-MM-dd HH:mm:ss"));
		cv.put(DatabaseHelper.SyncReport.COLUMN_STATUS, status);
		database.update(SyncReport.class, cv, DatabaseHelper.SyncReport.COLUMN_REPORT_ID+" = ?", new String[]{reportId+""} );

		database.close();
	}

	protected void onPostExecute(String result) {
		listener.collectionComplete(result);
		dialog.dismiss();
		Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
	}

	private class DownloadResponse {
		private InputStream inputStream;
		private String fileMimeType;
		private long fileSize;
		private String fileName;

		public DownloadResponse(InputStream is, String fileType, String exportedFileName, long fileSize){
			this.inputStream = is;
			this.fileMimeType = fileType;
			this.fileName = exportedFileName;
			this.fileSize = fileSize;
		}

		public String getFileMimeType() {
			return fileMimeType;
		}

		public InputStream getInputStream() {
			return inputStream;
		}

		public long getFileSize() {
			return fileSize;
		}

		public String getFileName() {
			return fileName;
		}

		public boolean isXmlFile(){
			return fileMimeType.equalsIgnoreCase(XML_MIME_TYPE);
		}

		public boolean isZipFile(){
			return fileMimeType.equalsIgnoreCase(ZIP_MIME_TYPE);
		}
	}
}
