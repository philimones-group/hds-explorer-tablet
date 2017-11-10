package net.manhica.dss.explorer.io;

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

import net.manhica.dss.explorer.R;
import net.manhica.dss.explorer.database.Bootstrap;
import net.manhica.dss.explorer.database.Database;
import net.manhica.dss.explorer.database.DatabaseHelper;
import net.manhica.dss.explorer.database.Table;
import net.manhica.dss.explorer.model.CollectedData;
import net.manhica.dss.explorer.model.Form;
import net.manhica.dss.explorer.model.Household;
import net.manhica.dss.explorer.model.Member;
import net.manhica.dss.explorer.model.Module;
import net.manhica.dss.explorer.model.SyncReport;
import net.manhica.dss.explorer.model.User;
import net.manhica.dss.explorer.model.followup.TrackingList;
import net.manhica.dss.explorer.model.followup.TrackingMemberList;

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

	private static final String API_PATH = "/api/dss-explorer";
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
		SETTINGS, MODULES, FORMS, TRACKING_LISTS, USERS, MEMBERS, HOUSEHOLDS
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
			case TRACKING_LISTS:
				builder.append(" " + mContext.getString(R.string.sync_tracking_lists_lbl));
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
					case TRACKING_LISTS: /*testing*/
						deleteAll(DatabaseHelper.TrackingList.TABLE_NAME);
						deleteAll(DatabaseHelper.TrackingMemberList.TABLE_NAME);
						processUrl(baseurl + API_PATH + "/trackinglists/zip", "trackinglists.zip");
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
						deleteAll(CollectedData.class /*, DatabaseHelper.CollectedData.COLUMN_SUPERVISED+"=1", null*/ ); //remove supervision for now
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


		return this.mContext.getString(R.string.sync_successfully_lbl);
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

	private void deleteAll(String tableName){
		Database database = getDatabase();
		database.open();
		database.delete(tableName, null, null);
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
				} else if (name.equalsIgnoreCase("trackinglists")) {
					processTrackingListsParams(parser);
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

	private boolean isTag(String tagName, XmlPullParser parser) throws XmlPullParserException {
		return (tagName.equals(parser.getName()));
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
			if (!isEmptyTag(DatabaseHelper.Module.COLUMN_CODE, parser)) {
                parser.next();
                table.setCode(parser.getText());
                parser.nextTag();
			}else{
				table.setCode("");
                parser.nextTag();
			}

            parser.nextTag(); //name
            if (!isEmptyTag(DatabaseHelper.Module.COLUMN_NAME, parser)) {
                parser.next();
                table.setName(parser.getText());
                parser.nextTag();
            }else{
                table.setName("");
                parser.nextTag();
            }

            parser.nextTag(); //description
            if (!isEmptyTag(DatabaseHelper.Module.COLUMN_DESCRIPTION, parser)) {
                parser.next();
                table.setDescription(parser.getText());
                parser.nextTag();
            }else{
                table.setDescription("");
                parser.nextTag();
            }

            parser.nextTag();
            parser.next();

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

			parser.nextTag(); //process isHouseholdForm
			if (!isEmptyTag("isHouseholdForm", parser)) {
				parser.next();
				table.setHouseholdForm(Boolean.parseBoolean(parser.getText()));
				parser.nextTag(); //process </isHouseholdForm>
				//Log.d(count+"-isHouseholdForm", "value="+ parser.getText());
			}else{
				table.setHouseholdForm(false);
				parser.nextTag();
			}

			parser.nextTag(); //process isMemberForm
			if (!isEmptyTag("isMemberForm", parser)) {
				parser.next();
				table.setMemberForm(Boolean.parseBoolean(parser.getText()));
				parser.nextTag(); //process </isMemberForm>
				//Log.d(count+"-isMemberForm", "value="+ parser.getText());
			}else{
				table.setMemberForm(false);
				parser.nextTag();
			}

			parser.nextTag(); //process isFollowUpOnly
			if (!isEmptyTag("isFollowUpOnly", parser)) {
				parser.next();
				table.setFollowUpOnly(Boolean.parseBoolean(parser.getText()));
				parser.nextTag(); //process </isFollowUpOnly>
				//Log.d(count+"-isFollowUpOnly", "value="+ parser.getText());
			}else{
				table.setFollowUpOnly(false);
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

			parser.nextTag(); //process redcapApi
			if (!isEmptyTag("redcapApi", parser)) {
				parser.next();
				table.setRedcapApi(parser.getText());
				//Log.d(count+"-redcapApi", "value="+ parser.getText());
				parser.nextTag(); //process </redcapApi>
			}else{
				table.setRedcapApi("");
				parser.nextTag();

			}

			parser.nextTag(); //process redcapMap
			if (!isEmptyTag("redcapMap", parser)) {
				parser.next();
				table.setRedcapMap(parser.getText());
				//Log.d(count+"-redcapMap", "value="+ parser.getText());
				parser.nextTag(); //process </redcapMap>
			}else{
				table.setRedcapMap("");
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

	private void processTrackingListsParams(XmlPullParser parser) throws XmlPullParserException, IOException {

		//clear sync_report
		updateSyncReport(SyncReport.REPORT_TRACKING_LISTS, null, SyncReport.STATUS_NOT_SYNCED);

		int count = 0;
		values.clear();

		parser.nextTag();

		Database database = getDatabase();
		database.open();
		database.beginTransaction();

		while (notEndOfXmlDoc("trackinglists", parser)) {
			count++;

			if (isEndTag("tracking_list", parser) || isEmptyTag("tracking_list", parser)){
				parser.nextTag();
				continue;
			}

			int trackingId = 0;
			String trlId = null;
			String trlLabel = null;
			String trlCode = null;
			String trlTitle = null;
			String trlModule = null;

			if (isTag("tracking_list", parser)) {
				//read tracking-list
				Log.d("trl-attr-id", "" + parser.getAttributeValue("", "id"));
				Log.d("trl-attr-label", "" + parser.getAttributeValue("", "label"));
				Log.d("trl-attr-title", "" + parser.getAttributeValue("", "title"));
				Log.d("trl-attr-module", "" + parser.getAttributeValue("", "module"));

				trlId = parser.getAttributeValue("", "id");
				trlLabel = parser.getAttributeValue("", "label");
				trlCode = parser.getAttributeValue("", "code");
				trlTitle = parser.getAttributeValue("", "title");
				trlModule = parser.getAttributeValue("", "module");

				//save to database
				TrackingList trackingList = new TrackingList();
				trackingList.setLabel(trlLabel);
				trackingList.setCode(trlCode);
				trackingList.setTitle(trlTitle);
				trackingList.setModule(trlModule);
				trackingList.setCompletionRate(0D);

				trackingId = (int) database.insert(trackingList); //insert on db
			}

			//read lists
			parser.nextTag(); //jump to <list> tag
			while(!isEndTag("tracking_list", parser)){
				//read list tag
				String listId = null;
				String listTitle = null;
				String listForms = null;

				if (isEndTag("list", parser) || isEmptyTag("list", parser)){
					parser.nextTag();
					continue;
				}
				if (isTag("list", parser)){
					Log.d("list-attr-id", "" + parser.getAttributeValue("", "id"));
					Log.d("list-attr-label", "" + parser.getAttributeValue("", "title"));
					Log.d("list-attr-forms", "" + parser.getAttributeValue("", "forms"));

					listId = parser.getAttributeValue("", "id");
					listTitle = parser.getAttributeValue("", "title");
					listForms = parser.getAttributeValue("", "forms");

				}

				//read members
				parser.nextTag(); //jump to <member end tag> if exists
				while (!isEndTag("list", parser)){
					if (isTag("member", parser)){
						Log.d("mem-attr-extid", "" + parser.getAttributeValue("", "extid"));
						Log.d("mem-attr-prmid", "" + parser.getAttributeValue("", "permid"));
						Log.d("mem-attr-scode", "" + parser.getAttributeValue("", "studycode"));
						Log.d("mem-attr-forms", "" + parser.getAttributeValue("", "forms"));
						Log.d("end","end");

						String mExtId = parser.getAttributeValue("", "extid");
						String mPrmId = parser.getAttributeValue("", "permid");
						String mScode = parser.getAttributeValue("", "studycode");
						String mForms = parser.getAttributeValue("", "forms");

						//save track member list to database
						TrackingMemberList tml = new TrackingMemberList();
						tml.setListId(Integer.parseInt(listId));
						tml.setTrackingId(trackingId);
						tml.setTitle(listTitle);
						tml.setForms(listForms==null ? "" : listForms);
						tml.setMemberExtId(mExtId);
						tml.setMemberPermId(mPrmId);
						tml.setMemberStudyCode(mScode);
						tml.setMemberForms(mForms);
						tml.setCompletionRate(0D);

						database.insert(tml);

					}
					parser.nextTag();
					parser.nextTag(); //jump to next tag eg. <member> or </list>
				}

			}


           /*

			//values.add(table);
			database.insert(table);
			*/

			publishProgress(count);

		}

		database.setTransactionSuccessful();
		database.endTransaction();
		database.close();

		updateSyncReport(SyncReport.REPORT_TRACKING_LISTS, new Date(), SyncReport.STATUS_SYNCED);
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

			parser.nextTag(); //process <fullName>
			if (!isEmptyTag("fullName", parser)) { //its not <fullName/>
				parser.next();
				table.setFullName(parser.getText());
				parser.nextTag(); //process </fullName>
				//Log.d(count+"-fullName", "value="+ table.getFullName());
			}else{
				table.setFullName("");
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


            parser.nextTag(); //process <extId>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_EXT_ID, parser)) {
                parser.next();
                table.setExtId(parser.getText());
                parser.nextTag(); //process </extId>
            }else{
                table.setExtId("");
                parser.nextTag();
            }

            parser.nextTag(); //process <houseNumber>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_HOUSE_NUMBER, parser)) {
                parser.next();
                table.setHouseNumber(parser.getText());
                parser.nextTag(); //process </houseNumber>
            }else{
                table.setHouseNumber("");
                parser.nextTag();
            }

            parser.nextTag(); //process <headPermId>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_HEAD_PERM_ID, parser)) {
                parser.next();
                table.setHeadPermId(parser.getText());
                parser.nextTag(); //process </headPermId>
            }else{
                table.setHeadPermId("");
                parser.nextTag();
            }

            parser.nextTag(); //process <subsHeadPermId>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_SUBSHEAD_PERM_ID, parser)) {
                parser.next();
                table.setSubsHeadPermId(parser.getText());
                parser.nextTag(); //process </subsHeadPermId>
            }else{
                table.setSubsHeadPermId("");
                parser.nextTag();
            }

            parser.nextTag(); //process <neighborhood>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_NEIGHBORHOOD, parser)) {
                parser.next();
                table.setNeighborhood(parser.getText());
                parser.nextTag(); //process </neighborhood>
            }else{
                table.setNeighborhood("");
                parser.nextTag();
            }

            parser.nextTag(); //process <locality>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_LOCALITY, parser)) {
                parser.next();
                table.setLocality(parser.getText());
                parser.nextTag(); //process </locality>
            }else{
                table.setLocality("");
                parser.nextTag();
            }

            parser.nextTag(); //process <adminPost>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_ADMIN_POST, parser)) {
                parser.next();
                table.setAdminPost(parser.getText());
                parser.nextTag(); //process </adminPost>
            }else{
                table.setAdminPost("");
                parser.nextTag();
            }

            parser.nextTag(); //process <district>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_DISTRICT, parser)) {
                parser.next();
                table.setDistrict(parser.getText());
                parser.nextTag(); //process </district>
            }else{
                table.setDistrict("");
                parser.nextTag();
            }

            parser.nextTag(); //process <province>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_PROVINCE, parser)) {
                parser.next();
                table.setProvince(parser.getText());
                parser.nextTag(); //process </province>
            }else{
                table.setProvince("");
                parser.nextTag();
            }

            parser.nextTag(); //process <gpsAccuracy>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_GPS_ACCURACY, parser)) {
                parser.next();
                table.setGpsAccuracy(Double.parseDouble(parser.getText()));
                parser.nextTag(); //process </gpsAccuracy>
                //Log.d("note gpsacc", table.getGpsAccuracy());
            }else{
                table.setGpsAccuracy(null);
				table.setGpsNull(true);
                parser.nextTag();
                //Log.d("e gpsacc", table.getGpsAccuracy());
            }

            parser.nextTag(); //process <gpsAltitude>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_GPS_ALTITUDE, parser)) {
                parser.next();
                table.setGpsAltitude(Double.parseDouble(parser.getText()));
                parser.nextTag(); //process </gpsAltitude>
            }else{
                table.setGpsAltitude(null);
				table.setGpsNull(true);
                parser.nextTag();
            }

            parser.nextTag(); //process <gpsLatitude>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_GPS_LATITUDE, parser)) {
                parser.next();
                table.setGpsLatitude(Double.parseDouble(parser.getText()));
				table.setCosLatitude(Math.cos(table.getGpsLatitude()*Math.PI / 180.0)); // cos_lat = cos(lat * PI / 180)
				table.setSinLatitude(Math.sin(table.getGpsLatitude()*Math.PI / 180.0)); // sin_lat = sin(lat * PI / 180)
                parser.nextTag(); //process </gpsLatitude>
            }else{
                table.setGpsLatitude(null);
				table.setGpsNull(true);
                parser.nextTag();
            }

            parser.nextTag(); //process <gpsLongitude>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_GPS_LONGITUDE, parser)) {
                parser.next();
                table.setGpsLongitude(Double.parseDouble(parser.getText()));
				table.setCosLongitude(Math.cos(table.getGpsLongitude()*Math.PI / 180.0)); // cos_lng = cos(lng * PI / 180)
				table.setSinLongitude(Math.sin(table.getGpsLongitude()*Math.PI / 180.0)); // sin_lng = sin(lng * PI / 180)
				parser.nextTag(); //process </gpsLongitude>
                //Log.d("note gpslng", table.getGpsLongitude());
            }else{
                table.setGpsLongitude(null);
				table.setGpsNull(true);
                parser.nextTag();
            }

			parser.nextTag(); //process <populationDensity>
			if (!isEmptyTag(DatabaseHelper.Household.COLUMN_POPULATION_DENSITY, parser)) {
				parser.next();
				table.setPopulationDensity(Double.parseDouble(parser.getText()));
				parser.nextTag(); //process </populationDensity>
				//Log.d("note popden", table.getPopulationDensity());
			}else{
				table.setPopulationDensity(0D);
				parser.nextTag();
			}

			parser.nextTag(); //process <densityType>
			if (!isEmptyTag(DatabaseHelper.Household.COLUMN_DENSITY_TYPE, parser)) {
				parser.next();
				table.setDensityType(parser.getText());
				parser.nextTag(); //process </densityType>
				//Log.d("note popden", table.getDensityType());
			}else{
				table.setDensityType("");
				parser.nextTag();
			}

            //Log.d("position now ", ""+parser.getName() + ", " +parser.getPositionDescription() );
            parser.nextTag(); //process <extrasColumns>
            //Log.d("extraColumns "+isEmptyTag(DatabaseHelper.Household.COLUMN_EXTRAS_COLUMNS,parser), ""+parser.getName() + ", " +parser.getPositionDescription() );
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_EXTRAS_COLUMNS, parser)) {
                parser.next();
                table.setExtrasColumns(parser.getText());
                parser.nextTag(); //process </extrasColumns>
                //Log.d("note excol", table.getExtrasColumns());
            }else{
                table.setExtrasColumns("");
                parser.nextTag();
                //Log.d("e excol", table.getExtrasColumns());
            }

            parser.nextTag(); //process <extrasValues>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_EXTRAS_VALUES, parser)) {
                parser.next();
                table.setExtrasValues(parser.getText());
                parser.nextTag(); //process </extrasValues>
            }else{
                table.setExtrasValues("");
                parser.nextTag();
            }


			parser.nextTag(); //last process tag
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

            //Log.d("TAG", parser.getPositionDescription());

            parser.nextTag(); //process <extId>
            //Log.d("TAG2", parser.getPositionDescription());
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_EXT_ID, parser)) {
                parser.next();
                table.setExtId(parser.getText());
                parser.nextTag(); //process </extId>
            }else{
                table.setExtId("");
                parser.nextTag();
            }

            parser.nextTag(); //process <permId>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_PERM_ID, parser)) {
                parser.next();
                table.setPermId(parser.getText());
                parser.nextTag(); //process </permId>
            }else{
                table.setPermId("");
                parser.nextTag();
            }

            parser.nextTag(); //process <name>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_NAME, parser)) {
                parser.next();
                table.setName(parser.getText());
                parser.nextTag(); //process </name>
            }else{
                table.setName("");
                parser.nextTag();
            }

            parser.nextTag(); //process <gender>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_GENDER, parser)) {
                parser.next();
                table.setGender(parser.getText());
                parser.nextTag(); //process </gender>
            }else{
                table.setGender("");
                parser.nextTag();
            }

            parser.nextTag(); //process <dob>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_DOB, parser)) {
                parser.next();
                table.setDob(parser.getText());
                parser.nextTag(); //process </dob>
            }else{
                table.setDob("");
                parser.nextTag();
            }

            parser.nextTag(); //process <age>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_AGE, parser)) {
                parser.next();
                table.setAge(Integer.parseInt(parser.getText()));
                parser.nextTag(); //process </age>
            }else{
                table.setAge(-1);
                parser.nextTag();
            }

            parser.nextTag(); //process <motherExtId>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_MOTHER_EXT_ID, parser)) {
                parser.next();
                table.setMotherExtId(parser.getText());
                parser.nextTag(); //process </motherExtId>
            }else{
                table.setMotherExtId("");
                parser.nextTag();
            }

            parser.nextTag(); //process <motherName>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_MOTHER_NAME, parser)) {
                parser.next();
                table.setMotherName(parser.getText());
                parser.nextTag(); //process </motherName>
            }else{
                table.setMotherName("");
                parser.nextTag();
            }

            parser.nextTag(); //process <motherPermId>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_MOTHER_PERM_ID, parser)) {
                parser.next();
                table.setMotherPermId(parser.getText());
                parser.nextTag(); //process </motherPermId>
            }else{
                table.setMotherPermId("");
                parser.nextTag();
            }

            parser.nextTag(); //process <fatherExtId>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_FATHER_EXT_ID, parser)) {
                parser.next();
                table.setFatherExtId(parser.getText());
                parser.nextTag(); //process </fatherExtId>
            }else{
                table.setFatherExtId("");
                parser.nextTag();
            }

            parser.nextTag(); //process <fatherName>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_FATHER_NAME, parser)) {
                parser.next();
                table.setFatherName(parser.getText());
                parser.nextTag(); //process </fatherName>
            }else{
                table.setFatherName("");
                parser.nextTag();
            }

            parser.nextTag(); //process <fatherPermId>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_FATHER_PERM_ID, parser)) {
                parser.next();
                table.setFatherPermId(parser.getText());
                parser.nextTag(); //process </fatherPermId>
            }else{
                table.setFatherPermId("");
                parser.nextTag();
            }

            parser.nextTag(); //process <spouseExtId>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_SPOUSE_EXT_ID, parser)) {
                parser.next();
                table.setSpouseExtId(parser.getText());
                parser.nextTag(); //process </spouseExtId>
            }else{
                table.setSpouseExtId("");
                parser.nextTag();
            }

            parser.nextTag(); //process <spouseName>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_SPOUSE_NAME, parser)) {
                parser.next();
                table.setSpouseName(parser.getText());
                parser.nextTag(); //process </spouseName>
            }else{
                table.setSpouseName("");
                parser.nextTag();
            }

            parser.nextTag(); //process <spousePermId>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_SPOUSE_PERM_ID, parser)) {
                parser.next();
                table.setSpousePermId(parser.getText());
                parser.nextTag(); //process </spousePermId>
            }else{
                table.setSpousePermId("");
                parser.nextTag();
            }

            parser.nextTag(); //process <houseExtId>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_HOUSE_EXT_ID, parser)) {
                parser.next();
                table.setHouseExtId(parser.getText());
                parser.nextTag(); //process </houseExtId>
            }else{
                table.setHouseExtId("");
                parser.nextTag();
            }

            parser.nextTag(); //process <houseNumber>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_HOUSE_NUMBER, parser)) {
                parser.next();
                table.setHouseNumber(parser.getText());
                parser.nextTag(); //process </houseNumber>
            }else{
                table.setHouseNumber("");
                parser.nextTag();
            }

            parser.nextTag(); //process <startType>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_START_TYPE, parser)) {
                parser.next();
                table.setStartType(parser.getText());
                parser.nextTag(); //process </startType>
            }else{
                table.setStartType("");
                parser.nextTag();
            }

            parser.nextTag(); //process <startDate>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_START_DATE, parser)) {
                parser.next();
                table.setStartDate(parser.getText());
                parser.nextTag(); //process </startDate>
            }else{
                table.setStartDate("");
                parser.nextTag();
            }

            parser.nextTag(); //process <endType>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_END_TYPE, parser)) {
                parser.next();
                table.setEndType(parser.getText());
                parser.nextTag(); //process </endType>
            }else{
                table.setEndType("");
                parser.nextTag();
            }

            parser.nextTag(); //process <endDate>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_END_DATE, parser)) { //endtag is temp
                parser.next();
				//Log.d("note endDate", parser.getText() + ", " +parser.getPositionDescription());
				if (parser.getText()!=null){
					table.setEndDate(parser.getText());
					parser.nextTag(); //process </endDate>
				}
            }else{
                table.setEndDate("");
                parser.nextTag();
                //Log.d("e gpsacc", table.getEndDate());
            }

            //CORRECT THE BUG AROUND HERE
            parser.nextTag(); //process <gpsAccuracy>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_GPS_ACCURACY, parser)) {
                parser.next();
                table.setGpsAccuracy(Double.parseDouble(parser.getText()));
                parser.nextTag(); //process </gpsAccuracy>
                //Log.d("note gpsacc", table.getGpsAccuracy());
            }else{
                table.setGpsAccuracy(null);
				table.setGpsNull(true);
                parser.nextTag();

            }

            parser.nextTag(); //process <gpsAltitude>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_GPS_ALTITUDE, parser)) {
                parser.next();
                table.setGpsAltitude(Double.parseDouble(parser.getText()));
                parser.nextTag(); //process </gpsAltitude>
            }else{
                table.setGpsAltitude(null);
				table.setGpsNull(true);
                parser.nextTag();
            }

            parser.nextTag(); //process <gpsLatitude>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_GPS_LATITUDE, parser)) {
                parser.next();
                table.setGpsLatitude(Double.parseDouble(parser.getText()));
				table.setCosLatitude(Math.cos(table.getGpsLatitude()*Math.PI / 180.0)); // cos_lat = cos(lat * PI / 180)
				table.setSinLatitude(Math.sin(table.getGpsLatitude()*Math.PI / 180.0)); // sin_lat = sin(lat * PI / 180)
                parser.nextTag(); //process </gpsLatitude>
            }else{
                table.setGpsLatitude(null);
				table.setGpsNull(true);
                parser.nextTag();
            }

            parser.nextTag(); //process <gpsLongitude>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_GPS_LONGITUDE, parser)) {
                parser.next();
                table.setGpsLongitude(Double.parseDouble(parser.getText()));
				table.setCosLongitude(Math.cos(table.getGpsLongitude()*Math.PI / 180.0)); // cos_lng = cos(lng * PI / 180)
				table.setSinLongitude(Math.sin(table.getGpsLongitude()*Math.PI / 180.0)); // sin_lng = sin(lng * PI / 180)
                parser.nextTag(); //process </gpsLongitude>
            }else{
                table.setGpsLongitude(null);
				table.setGpsNull(true);
                parser.nextTag();
            }

            parser.nextTag(); //process <extrasColumns>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_EXTRAS_COLUMNS, parser)) {
                parser.next();
                table.setExtrasColumns(parser.getText());
                parser.nextTag(); //process </extrasColumns>
            }else{
                table.setExtrasColumns("");
                parser.nextTag();
            }

            parser.nextTag(); //process <extrasValues>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_EXTRAS_VALUES, parser)) {
                parser.next();
                table.setExtrasValues(parser.getText());
                parser.nextTag(); //process </extrasValues>
            }else{
                table.setExtrasValues("");
                parser.nextTag();
            }


			parser.nextTag(); //process last tag
			parser.next();

			//values.add(table);
			database.insert(table);

			if (count % 200 == 0){
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
