package org.philimone.hds.explorer.io;

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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.DataSet;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.enums.SyncEntity;
import org.philimone.hds.explorer.model.enums.SyncState;
import org.philimone.hds.explorer.model.enums.SyncStatus;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.Database;
import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Module;
import org.philimone.hds.explorer.model.SyncReport;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.followup.TrackingList;

import io.objectbox.Box;
import mz.betainteractive.utilities.StringUtil;

/**
 * AsyncTask responsible for downloading the HDS Explorer "database", that is a
 * subset of the HDS Explorer database records. It does the downloading
 * incrementally, by downloading parts of the data one at a time. For example,
 * it gets all Households and then retrieves all Members.
 */
public class SyncEntitiesTask extends AsyncTask<Void, Integer, SyncEntitiesTask.ExecutionReport> {

	private static final String API_PATH = "/api/export";
	private static final String ZIP_MIME_TYPE = "application/zip;charset=utf-8";
	private static final String XML_MIME_TYPE = "text/xml;charset=utf-8";

	private SyncEntitiesListener listener;

	private HttpURLConnection connection;

	private String baseurl;
	private String username;
	private String password;
	private List<SyncEntity> entities;

	private Map<SyncEntity, Integer> downloadedValues = new LinkedHashMap<>();
	private Map<SyncEntity, Integer> savedValues = new LinkedHashMap<>();
	private Map<SyncEntity, String> errorMessageValues = new LinkedHashMap<>();

	private SyncState state;
	private SyncEntity entity;
	private int entityRecords;

	private Context mContext;
	private Box<ApplicationParam> boxAppParams;

	private boolean canceled;

	public SyncEntitiesTask(Context context, SyncEntitiesListener listener, String url, String username, String password, SyncEntity... entityToDownload) {
		this.baseurl = url;
		this.username = username;
		this.password = password;
		this.mContext = context;
		this.entities = new ArrayList<>();
		this.entities.addAll(Arrays.asList(entityToDownload));
		this.listener = listener;
		this.listener.onSyncCreated();

		initBoxes();
	}

	private void initBoxes() {
		this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
	}

	private Database getDatabase(){
		return new Database(mContext);
	}

	public void setSyncDatabaseListener(SyncEntitiesListener listener){
		this.listener = listener;
	}

	public void setEntitiesToDownload(SyncEntity... entityToDownload){
		this.entities.clear();
		this.entities.addAll(Arrays.asList(entityToDownload));
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		StringBuilder builder = new StringBuilder();

		switch (state) {
		case DOWNLOADING:
			builder.append(mContext.getString(R.string.sync_downloading_lbl));

			/*// This was reporting bad the data - was slow
			if (values.length > 0){
				downloadedValues.put(entity, values[0]);
				Log.d("save", ""+entity+", last-value2="+values[0]);
			}
			*/

			break;
		case SAVING:
			builder.append(mContext.getString(R.string.sync_saving_lbl));

			/* //This was reporting bad the data - was slow
			if (values.length > 0){
				savedValues.put(entity, values[0]);
			}
			*/

			break;
		}

		switch (entity) {
			case PARAMETERS:
				builder.append(" " + mContext.getString(R.string.sync_params_lbl));
				break;
			case MODULES:
				builder.append(" " + mContext.getString(R.string.sync_modules_lbl));
				break;
			case FORMS:
				builder.append(" " + mContext.getString(R.string.sync_forms_lbl));
				break;
			case DATASETS:
				builder.append(" " + mContext.getString(R.string.sync_datasets_lbl));
				break;
			case DATASETS_CSV_FILES:
				builder.append(" " + mContext.getString(R.string.sync_datasets_csv_lbl));
				break;
			case TRACKING_LISTS:
				builder.append(" " + mContext.getString(R.string.sync_tracking_lists_lbl));
				break;
			case USERS:
				builder.append(" " + mContext.getString(R.string.sync_users_lbl));
				break;
			case REGIONS:
				builder.append(" " + mContext.getString(R.string.sync_regions_lbl));
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
			if (state== SyncState.DOWNLOADING){
				msg = ". " + mContext.getString(R.string.sync_saved_lbl) + " "  + getInKB(values[0]) + "KB";
			}

			builder.append(msg);
		}

		listener.onSyncProgressUpdate(values.length > 0 ? values[0] : 0, builder.toString());
	}

	protected ExecutionReport doInBackground(Void... params) {

		// at this point, we don't care to be smart about which data to
		// download, we simply download it all
		//deleteAllTables();

		this.state = SyncState.DOWNLOADING;

		try {

			for (SyncEntity entity : entities){

				//get record count from the server and trigger onSyncStarted
				executeOnSyncStarted(entity, state);

				this.entity = entity;

				switch (entity) {
					case PARAMETERS:
						//deleteAll(ApplicationParam.class);
						processUrl(baseurl + API_PATH + "/params/zip", "params.zip");
						break;
					case MODULES:
						deleteAll(Module.class);
						processUrl(baseurl + API_PATH + "/modules/zip", "modules.zip");
						break;
					case FORMS:
						deleteAll(Form.class);
						processUrl(baseurl + API_PATH + "/forms/zip", "forms.zip");
						break;
					case DATASETS:
						deleteAll(DataSet.class);
						processUrl(baseurl + API_PATH + "/datasets/zip", "datasets.zip");
						break;
					case DATASETS_CSV_FILES:
						downloadExternalDatasetFiles();
						break;
					case TRACKING_LISTS: /*testing*/
						deleteAll(DatabaseHelper.TrackingList.TABLE_NAME);
						deleteAll(DatabaseHelper.TrackingSubjectList.TABLE_NAME);
						processUrl(baseurl + API_PATH + "/trackinglists/zip", "trackinglists.zip");
						break;
					case USERS:
						deleteAll(User.class);
						processUrl(baseurl + API_PATH + "/users/zip", "users.zip");
						break;
					case REGIONS:
						deleteAll(Region.class);
						processUrl(baseurl + API_PATH + "/regions/zip", "regions.zip");
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

			updateSyncReport(entity, new Date(), SyncStatus.STATUS_SYNC_ERROR);
			errorMessageValues.put(entity, e.getMessage());

			return new ExecutionReport(e, mContext.getString(R.string.sync_failure_file_not_found_lbl));

		} catch (Exception e) {
			e.printStackTrace();

			updateSyncReport(entity, new Date(), SyncStatus.STATUS_SYNC_ERROR);
			errorMessageValues.put(entity, e.getMessage());

			return new ExecutionReport(e, mContext.getString(R.string.sync_failure_unknown_lbl));
		}

		return new ExecutionReport(this.mContext.getString(R.string.sync_successfully_lbl));
	}

	private void deleteAllTables() {
		// ordering is somewhat important during delete. a few tables have
		// foreign keys
		Database database = getDatabase();
		database.open();

		database.delete(User.class, null, null);
		database.delete(Form.class, null, null);
		database.delete(Module.class, null, null);
		database.delete(Region.class, null, null);
		database.delete(DataSet.class, null, null);
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

	private void executeOnSyncStarted(SyncEntity entity, SyncState state){
		int size = getSyncRecordToDownload(entity);

		this.entityRecords = size;

		this.listener.onSyncStarted(entity, state, size);
	}

	private int getSyncRecordToDownload(SyncEntity entity){

		String result = processUrl(baseurl + API_PATH + "/sync-report/" + entity.getCode());

		//Log.d("tag-report", "result="+result);

		return Integer.parseInt(result);
	}

	private void downloadExternalDatasetFiles() throws Exception {

		Database database = getDatabase();
		database.open();
		List<DataSet> dataSets = Queries.getAllDataSetBy(database, null, null);
		database.close();

		for (DataSet dataSet : dataSets){
			String url = baseurl + API_PATH + "/dataset/zip/" + dataSet.getDatasetId();
			String name = dataSet.getName()+".zip";
			processUrl(url, name, dataSet);
		}

	}

	/**
	 * Exclusively to Download CSV/ZIP Datasets
	 * @param strUrl
	 * @param exportedFileName
	 * @param dataSet
	 * @throws Exception
	 */
	private void processUrl(String strUrl, String exportedFileName, DataSet dataSet) throws Exception {
		DownloadResponse response = processUrl(strUrl, exportedFileName, false);
		updateDataset(dataSet, response);
	}

	private void processUrl(String strUrl, String exportedFileName) throws Exception {
		processUrl(strUrl, exportedFileName, true);
	}

	private String processUrl(String strUrl) {
		state = SyncState.DOWNLOADING;
		//http request
		try {
			HttpURLConnection urlConnection = null;
			String basicAuth = "Basic " + new String(Base64.encode((this.username+":"+this.password).getBytes(),Base64.NO_WRAP ));

			URL url = new URL(strUrl);

			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setReadTimeout(10000);
			urlConnection.setConnectTimeout(15000);
			urlConnection.setDoInput(true);
			urlConnection.setRequestProperty("Authorization", basicAuth);

			Log.d("login", ""+url);

			urlConnection.connect();

			if (urlConnection.getResponseCode()==200){

				Scanner scan = new Scanner(urlConnection.getInputStream());
				String result = scan.next();
				scan.close();

				//Log.d("result-2", ""+result);

				return result;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private DownloadResponse processUrl(String strUrl, String exportedFileName, boolean processDataContent) throws Exception {
		state = SyncState.DOWNLOADING;
		publishProgress(0);

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

		return processResponse(exportedFileName, processDataContent);
	}

	private DownloadResponse processResponse(String exportedFileName, boolean processData) throws Exception {
		DownloadResponse response = getResponse(exportedFileName);

		Log.d("is", ""+response.getInputStream()+", xml-"+response.isXmlFile()+", zip-"+response.isZipFile());

		//Inform about the download
		this.listener.onSyncStarted(this.entity, this.state, response.fileSize); //remove KB calc
		//save file
		InputStream fileInputStream = saveFileToStorage(response);


		Log.d("tag-d", "processData="+processData+", fis="+fileInputStream);

		if (processData){ //is it necessary to process ZIP/XML Files

			//Inform about the saving process
			this.listener.onSyncStarted(this.entity, SyncState.SAVING, this.entityRecords);

			if (fileInputStream != null){

				if (response.isXmlFile()){
					processXMLDocument(fileInputStream);
				}
				if (response.isZipFile()){
					processZIPDocument(fileInputStream);
				}
			}
		}

		return response;
	}

	private DownloadResponse getResponse(String exportedFileName) throws IOException {
		int response = connection.getResponseCode();
		Log.d("connection", "The response code is: " + response+", type="+connection.getContentType()+", size="+connection.getContentLength());

		InputStream is = connection.getInputStream();

		return new DownloadResponse(is, connection.getContentType(), exportedFileName, connection.getContentLength());//173916816L);
	}

	private void processXMLDocument(InputStream content) throws Exception {
		state = SyncState.SAVING;

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
				if (name.equalsIgnoreCase("applicationParams")) {
					processApplicationParams(parser);
				} else if (name.equalsIgnoreCase("modules")) {
					processModulesParams(parser);
				} else if (name.equalsIgnoreCase("forms")) {
					processFormsParams(parser);
				} else if (name.equalsIgnoreCase("datasets")) {
					processDatasetsParams(parser);
				} else if (name.equalsIgnoreCase("trackinglists")) {
					processTrackingListsParams(parser);
				} else if (name.equalsIgnoreCase("users")) {
					processUsersParams(parser);
				} else if (name.equalsIgnoreCase("regions")) {
					processRegions(parser);
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
		state = SyncState.DOWNLOADING;

		InputStream content = response.getInputStream();

		FileOutputStream fout = new FileOutputStream(Bootstrap.getAppPath() + response.getFileName());
		byte[] buffer = new byte[10*1024];
		int len = 0;
		long total = 0;

		publishProgress(0);

		while ((len = content.read(buffer)) != -1){
			fout.write(buffer, 0, len);
			total += len;
			int perc =  (int) (total); //remove KB Calc /1024

			downloadedValues.put(entity, perc); //publishProgress is a bit slow, ensure this is set here - this code also runs on onProgressUpdate
			//Log.d("save", ""+entity+", last-value="+perc);
			publishProgress(perc);
			//Thread.sleep(200); //REMOVE THIS - IT WAS TOO FAST TO CONTEMPLATE THE PROGRESS
		}

		fout.close();
		content.close();

		FileInputStream fin = new FileInputStream(Bootstrap.getAppPath() + response.getFileName());

		return fin;
	}

	private void processZIPDocument(InputStream inputStream) throws Exception {

		//Log.d("zip", "processing zip file");

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

	private void processApplicationParams(XmlPullParser parser) throws Exception {



		//clear sync_report
		updateSyncReport(SyncEntity.PARAMETERS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<ApplicationParam> values = new ArrayList<>();
		List<String> namesToRemove = new ArrayList<>();
		int count = 0;
		values.clear();

		parser.nextTag(); //<applicationParam>

		while (notEndOfXmlDoc("applicationParams", parser)) {

			count++;
			ApplicationParam table = new ApplicationParam();

			parser.nextTag(); //name
			if (!isEmptyTag("name", parser)) {
				parser.next();
				table.setName(parser.getText());
				parser.nextTag();
			}else{
				table.setName("");
				parser.nextTag();
			}

			parser.nextTag(); //type
			if (!isEmptyTag("type", parser)) {
				parser.next();
				table.setType(parser.getText());
				parser.nextTag();
			}else{
				table.setType("");
				parser.nextTag();
			}

			parser.nextTag(); //value
			if (!isEmptyTag("value", parser)) {
				parser.next();
				table.setValue(parser.getText());
				parser.nextTag();
			}else{
				table.setValue("");
				parser.nextTag();
			}

			parser.nextTag();
			parser.next();

			namesToRemove.add(table.name);
			values.add(table);
			publishProgress(count);

		}

		state = SyncState.SAVING;
		entity = SyncEntity.PARAMETERS;

		if (!values.isEmpty()) {
			count = values.size();

			String[] arrayNames = namesToRemove.toArray(new String[namesToRemove.size()]);
			boxAppParams.query().in(ApplicationParam_.name, arrayNames).build().remove(); //delete all existing names
			boxAppParams.put(values); //insert all of them

			savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
			publishProgress(count);

		}

		updateSyncReport(SyncEntity.PARAMETERS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processModulesParams(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.MODULES, null, SyncStatus.STATUS_NOT_SYNCED);

		List<Table> values = new ArrayList<>();
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

		state = SyncState.SAVING;
		entity = SyncEntity.MODULES;

		Database database = getDatabase();
		database.open();
		if (!values.isEmpty()) {
			count = 0;
			for (Table t : values){
				count++;
				database.insert(t);

				savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
				publishProgress(count);
			}
		}
		database.close();


		updateSyncReport(SyncEntity.MODULES, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processFormsParams(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.FORMS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<Table> values = new ArrayList<>();
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

			parser.nextTag(); //process formDependencies
			if (!isEmptyTag("formDependencies", parser)) {
				parser.next();
				table.setFormDependencies(parser.getText());
				parser.nextTag(); //process </formDependencies>
				//Log.d(count+"-formDependencies", "value="+ parser.getText());
			}else{
				table.setFormDependencies("");
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_REGION_LEVEL
			if (!isEmptyTag(DatabaseHelper.Form.COLUMN_REGION_LEVEL, parser)) {
				parser.next();
				table.setRegionLevel(parser.getText());
				parser.nextTag(); //process </COLUMN_REGION_LEVEL>
				//Log.d(count+"-COLUMN_REGION_LEVEL", "value="+ parser.getText());
			}else{
				table.setRegionLevel("");
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

			parser.nextTag(); //process COLUMN_IS_REGION
			if (!isEmptyTag(DatabaseHelper.Form.COLUMN_IS_REGION, parser)) {
				parser.next();
				table.setRegionForm(Boolean.parseBoolean(parser.getText()));
				parser.nextTag(); //process </COLUMN_IS_REGION>
				//Log.d(count+"-COLUMN_IS_REGION", "value="+ parser.getText());
			}else{
				table.setRegionForm(false);
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

			parser.nextTag(); //process isHouseholdHeadForm
			if (!isEmptyTag("isHouseholdHeadForm", parser)) {
				parser.next();
				table.setHouseholdHeadForm(Boolean.parseBoolean(parser.getText()));
				parser.nextTag(); //process </isHouseholdHeadForm>
				//Log.d(count+"-isHouseholdHeadForm", "value="+ parser.getText());
			}else{
				table.setHouseholdHeadForm(false);
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

			parser.nextTag(); //process multiCollPerSession
			if (!isEmptyTag("multiCollPerSession", parser)) {
				parser.next();
				table.setMultiCollPerSession(Boolean.parseBoolean(parser.getText()));
				parser.nextTag(); //process </multiCollPerSession>
				//Log.d(count+"-multiCollPerSession", "value="+ parser.getText());
			}else{
				table.setMultiCollPerSession(false);
				parser.nextTag();
			}

			parser.nextTag(); //process formMap
			if (!isEmptyTag("formMap", parser)) {
				parser.next();
				table.setFormMap(parser.getText());
				//Log.d(count+"-formMap", "value="+ parser.getText());
				parser.nextTag(); //process </formMap>
			}else{
				table.setFormMap("");
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

			//Log.d("form ", ""+table.getFormId());

			database.insert(table);

			savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
			publishProgress(count);

		}

		state = SyncState.SAVING;
		entity = SyncEntity.FORMS;

		database.setTransactionSuccessful();
		database.endTransaction();
		database.close();

		updateSyncReport(SyncEntity.FORMS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processDatasetsParams(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.DATASETS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<Table> values = new ArrayList<>();
		int count = 0;
		values.clear();

		Database database = getDatabase();
		database.open();
		database.beginTransaction();

		parser.nextTag(); //<dataSet>

		while (notEndOfXmlDoc("datasets", parser)) {
			count++;

			DataSet table = new DataSet();

			parser.nextTag(); //process COLUMN_DATASET_ID
			if (!isEmptyTag(DatabaseHelper.DataSet.COLUMN_DATASET_ID, parser)) {
				parser.next();
				table.setDatasetId(Integer.parseInt(parser.getText()));
				parser.nextTag(); //process </COLUMN_DATASET_ID>
				//Log.d(count+"-COLUMN_DATASET_ID", "value="+ parser.getText());
			}else{
				table.setDatasetId(0);
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_NAME
			if (!isEmptyTag(DatabaseHelper.DataSet.COLUMN_NAME, parser)) {
				parser.next();
				table.setName(parser.getText());
				parser.nextTag(); //process </COLUMN_NAME>
				//Log.d(count+"-COLUMN_NAME", "value="+ parser.getText());
			}else{
				table.setName("");
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_KEYCOLUMN
			if (!isEmptyTag(DatabaseHelper.DataSet.COLUMN_KEYCOLUMN, parser)) {
				parser.next();
				table.setKeyColumn(parser.getText());
				parser.nextTag(); //process </COLUMN_KEYCOLUMN>
				//Log.d(count+"-COLUMN_KEYCOLUMN", "value="+ parser.getText());
			}else{
				table.setKeyColumn("");
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_TABLE_NAME
			if (!isEmptyTag(DatabaseHelper.DataSet.COLUMN_TABLE_NAME, parser)) {
				parser.next();
				table.setTableNameField(parser.getText());
				parser.nextTag(); //process </COLUMN_TABLE_NAME>
				//Log.d(count+"-COLUMN_TABLE_NAME", "value="+ parser.getText());
			}else{
				table.setTableNameField("");
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_TABLE_COLUMN
			if (!isEmptyTag(DatabaseHelper.DataSet.COLUMN_TABLE_COLUMN, parser)) {
				parser.next();
				table.setTableColumn(parser.getText());
				parser.nextTag(); //process </COLUMN_TABLE_COLUMN>
				//Log.d(count+"-COLUMN_TABLE_COLUMN", "value="+ parser.getText());
			}else{
				table.setTableColumn("");
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_CREATED_BY
			if (!isEmptyTag(DatabaseHelper.DataSet.COLUMN_CREATED_BY, parser)) {
				parser.next();
				table.setCreatedBy(parser.getText());
				parser.nextTag(); //process </COLUMN_CREATED_BY>
				//Log.d(count+"-COLUMN_CREATED_BY", "value="+ parser.getText());
			}else{
				table.setCreatedBy("");
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_CREATION_DATE
			if (!isEmptyTag(DatabaseHelper.DataSet.COLUMN_CREATION_DATE, parser)) {
				parser.next();
				table.setCreationDate(parser.getText());
				parser.nextTag(); //process </COLUMN_CREATION_DATE>
				//Log.d(count+"-COLUMN_CREATION_DATE", "value="+ parser.getText());
			}else{
				table.setCreationDate("");
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_UPDATED_BY
			if (!isEmptyTag(DatabaseHelper.DataSet.COLUMN_UPDATED_BY, parser)) {
				parser.next();
				table.setUpdatedBy(parser.getText());
				parser.nextTag(); //process </COLUMN_UPDATED_BY>
				//Log.d(count+"-COLUMN_UPDATED_BY", "value="+ parser.getText());
			}else{
				table.setUpdatedBy("");
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_UPDATED_DATE
			if (!isEmptyTag(DatabaseHelper.DataSet.COLUMN_UPDATED_DATE, parser)) {
				parser.next();
				table.setUpdatedDate(parser.getText());
				parser.nextTag(); //process </COLUMN_UPDATED_DATE>
				//Log.d(count+"-COLUMN_UPDATED_DATE", "value="+ parser.getText());
			}else{
				table.setUpdatedDate("");
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_LABELS
			if (!isEmptyTag(DatabaseHelper.DataSet.COLUMN_LABELS, parser)) {
				parser.next();
				table.setLabels(parser.getText());
				//Log.d(count+"-COLUMN_LABELS", "value="+ parser.getText());
				parser.nextTag(); //process </COLUMN_LABELS>
			}else{
				table.setLabels("");
				parser.nextTag();

			}

			table.setFilename(""); //an empty filename - the variable will be set after downloading data

			parser.nextTag();
			parser.next();


			database.insert(table);

			savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
			publishProgress(count);

		}

		state = SyncState.SAVING;
		entity = SyncEntity.DATASETS;

		database.setTransactionSuccessful();
		database.endTransaction();
		database.close();

		updateSyncReport(SyncEntity.DATASETS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processTrackingListsParams(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.TRACKING_LISTS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<Table> values = new ArrayList<>();
		int tlistCount = 0;
		values.clear();

		parser.nextTag();

		Database database = getDatabase();
		database.open();
		database.beginTransaction();

		while (notEndOfXmlDoc("trackinglists", parser)) {

			if (isEndTag("tracking_list", parser) || isEmptyTag("tracking_list", parser)){
				parser.nextTag();
				continue;
			}

			int trackingId = 0;
			String trlId = null;
			String trlCode = null;
			String trlName = null;
			String trlDetails = null;
			String trlTitle = null;
			String trlModule = null;

			if (isTag("tracking_list", parser)) {
				tlistCount++;

				//read tracking-list
				//Log.d("trl-attr-id", "" + parser.getAttributeValue("", "id"));
				//Log.d("trl-attr-name", "" + parser.getAttributeValue("", "name"));
				//Log.d("trl-attr-title", "" + parser.getAttributeValue("", "title"));
				//Log.d("trl-attr-module", "" + parser.getAttributeValue("", "module"));

				trlId = parser.getAttributeValue("", "id");
				trlCode = parser.getAttributeValue("", "code");
				trlName = parser.getAttributeValue("", "name");
				trlDetails = parser.getAttributeValue("", "details");
				trlTitle = parser.getAttributeValue("", "title");
				trlModule = parser.getAttributeValue("", "module");

				//save to database
				TrackingList trackingList = new TrackingList();
				trackingList.setCode(trlCode);
				trackingList.setName(trlName);
				trackingList.setDetails(trlDetails);
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
					//Log.d("list-attr-id", "" + parser.getAttributeValue("", "id"));
					//Log.d("list-attr-label", "" + parser.getAttributeValue("", "title"));
					//Log.d("list-attr-forms", "" + parser.getAttributeValue("", "forms"));

					listId = parser.getAttributeValue("", "id");
					listTitle = parser.getAttributeValue("", "title");
					listForms = parser.getAttributeValue("", "forms");

				}

				//read members
				parser.nextTag(); //jump to <member end tag> if exists
				while (!isEndTag("list", parser)){
					if (isTag("subject", parser)){
						//Log.d("mem-attr-code", "" + parser.getAttributeValue("", "code"));
						//Log.d("mem-attr-type", "" + parser.getAttributeValue("", "type"));
						//Log.d("mem-attr-forms", "" + parser.getAttributeValue("", "forms"));
						//Log.d("mem-attr-visit", "" + parser.getAttributeValue("", "visit"));
						//Log.d("end","end");

						String mCode = parser.getAttributeValue("", "code");
						String mType = parser.getAttributeValue("", "type");
						String mForms = parser.getAttributeValue("", "forms");
						String mVisit = parser.getAttributeValue("", "visit");

						//save track member list to database
						org.philimone.hds.explorer.model.followup.TrackingSubjectList tml = new org.philimone.hds.explorer.model.followup.TrackingSubjectList();
						tml.setListId(Integer.parseInt(listId));
						tml.setTrackingId(trackingId);
						tml.setTitle(listTitle);
						tml.setForms(listForms==null ? "" : listForms);
						tml.setSubjectCode(mCode);
						tml.setSubjectType(mType);
						tml.setSubjectForms(mForms);
						tml.setCompletionRate(0D);

						if (mVisit != null && !mVisit.isEmpty()){
							tml.setSubjectVisit(Integer.parseInt(mVisit));
						}

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

			savedValues.put(entity, tlistCount); //publish progress is a bit slow - its not reporting well the numbers
			publishProgress(tlistCount);

		}

		database.setTransactionSuccessful();
		database.endTransaction();
		database.close();

		updateSyncReport(SyncEntity.TRACKING_LISTS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processUsersParams(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.USERS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<Table> values = new ArrayList<>();
		int count = 0;
		values.clear();

		parser.nextTag();

		Database database = getDatabase();
		database.open();
		database.beginTransaction();

		while (notEndOfXmlDoc("users", parser)) {
			count++;

			User table = new User();

			parser.nextTag(); //process <code>
			if (!isEmptyTag("code", parser)) {
				parser.next();
				table.setCode(parser.getText());
				parser.nextTag(); //process </code>
				//Log.d(count+"-code", "value="+ table.getCode());
			}else{
				table.setCode("");
				parser.nextTag();
			}

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

			parser.nextTag(); //process <email>
			if (!isEmptyTag("email", parser)){ //its not <email/>
				parser.next();
				table.setEmail(parser.getText());
				parser.nextTag(); // </email>
				//Log.d(count+"-email", "value="+ table.getEmail());
			}else{
				table.setEmail("");
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


			parser.nextTag(); // <user>
			parser.next();

			//values.add(table);
			database.insert(table);

			savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
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

		updateSyncReport(SyncEntity.USERS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processRegions(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.REGIONS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<Table> values = new ArrayList<>();
		int count = 0;
		values.clear();

		parser.nextTag(); //<region>

		while (notEndOfXmlDoc("regions", parser)) {

			count++;
			Region table = new Region();

			parser.nextTag(); //<code>
			if (!isEmptyTag(DatabaseHelper.Region.COLUMN_CODE, parser)) {
				parser.next();
				table.setCode(parser.getText());
				parser.nextTag();
			}else{
				table.setCode("");
				parser.nextTag();
			}

			parser.nextTag(); //name
			if (!isEmptyTag(DatabaseHelper.Region.COLUMN_NAME, parser)) {
				parser.next();
				table.setName(parser.getText());
				parser.nextTag();
			}else{
				table.setName("");
				parser.nextTag();
			}

			parser.nextTag(); //hierarchyLevel
			if (!isEmptyTag(DatabaseHelper.Region.COLUMN_LEVEL, parser)) {
				parser.next();
				table.setLevel(parser.getText());
				parser.nextTag();
			}else{
				table.setLevel("");
				parser.nextTag();
			}

			parser.nextTag(); //parent
			if (!isEmptyTag(DatabaseHelper.Region.COLUMN_PARENT, parser)) {
				parser.next();
				table.setParent(parser.getText());
				parser.nextTag();
			}else{
				table.setParent("");
				parser.nextTag();
			}

			parser.nextTag();
			parser.next();

			values.add(table);
			publishProgress(count);


		}

		state = SyncState.SAVING;
		entity = SyncEntity.REGIONS;

		Database database = getDatabase();
		database.open();
		if (!values.isEmpty()) {
			count = 0;
			for (Table t : values){
				count++;
				database.insert(t);

				savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
				publishProgress(count);
			}
		}
		database.close();


		updateSyncReport(SyncEntity.REGIONS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processHouseholds(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.HOUSEHOLDS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<Table> values = new ArrayList<>();
		int count = 0;
		values.clear();

		parser.nextTag();

		Database database = getDatabase();
		database.open();
		database.beginTransaction();

		while (notEndOfXmlDoc("households", parser)) {
			count++;

			Household table = new Household();


            parser.nextTag(); //process <code>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_CODE, parser)) {
                parser.next();
                table.setCode(parser.getText());
                parser.nextTag(); //process </code>
            }else{
                table.setCode("");
                parser.nextTag();
            }

			parser.nextTag(); //process <region>
			if (!isEmptyTag(DatabaseHelper.Household.COLUMN_REGION, parser)) {
				parser.next();
				table.setRegion(parser.getText());
				parser.nextTag(); //process </region>
			}else{
				table.setRegion("");
				parser.nextTag();
			}

            parser.nextTag(); //process <houseNumber>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_NAME, parser)) {
                parser.next();
                table.setName(parser.getText());
                parser.nextTag(); //process </houseNumber>
            }else{
                table.setName("");
                parser.nextTag();
            }

            parser.nextTag(); //process <headCode>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_HEAD_CODE, parser)) {
                parser.next();
                table.setHeadCode(parser.getText());
                parser.nextTag(); //process </headCode>
            }else{
                table.setHeadCode("");
                parser.nextTag();
            }

			parser.nextTag(); //process <headName>
			if (!isEmptyTag(DatabaseHelper.Household.COLUMN_HEAD_NAME, parser)) {
				parser.next();
				table.setHeadName(parser.getText());
				parser.nextTag(); //process </headName>
			}else{
				table.setHeadName("");
				parser.nextTag();
			}

            parser.nextTag(); //process <subsHeadCode>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_SECHEAD_CODE, parser)) {
                parser.next();
                table.setSecHeadCode(parser.getText());
                parser.nextTag(); //process </subsHeadCode>
            }else{
                table.setSecHeadCode("");
                parser.nextTag();
            }

            parser.nextTag(); //process <hierarchy1>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_HIERARCHY_1, parser)) {
                parser.next();
                table.setHierarchy1(parser.getText());
                parser.nextTag(); //process </hierarchy1>
            }else{
                table.setHierarchy1("");
                parser.nextTag();
            }

            parser.nextTag(); //process <hierarchy2>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_HIERARCHY_2, parser)) {
                parser.next();
                table.setHierarchy2(parser.getText());
                parser.nextTag(); //process </hierarchy2>
            }else{
                table.setHierarchy2("");
                parser.nextTag();
            }

            parser.nextTag(); //process <hierarchy3>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_HIERARCHY_3, parser)) {
                parser.next();
                table.setHierarchy3(parser.getText());
                parser.nextTag(); //process </hierarchy3>
            }else{
                table.setHierarchy3("");
                parser.nextTag();
            }

            parser.nextTag(); //process <hierarchy4>
            if (!isEmptyTag(DatabaseHelper.Household.COLUMN_HIERARCHY_4, parser)) {
                parser.next();
                table.setHierarchy4(parser.getText());
                parser.nextTag(); //process </hierarchy4>
            }else{
                table.setHierarchy4("");
                parser.nextTag();
            }

			parser.nextTag(); //process <hierarchy5>
			if (!isEmptyTag(DatabaseHelper.Household.COLUMN_HIERARCHY_5, parser)) {
				parser.next();
				table.setHierarchy5(parser.getText());
				parser.nextTag(); //process </hierarchy5>
			}else{
				table.setHierarchy5("");
				parser.nextTag();
			}

			parser.nextTag(); //process <hierarchy6>
			if (!isEmptyTag(DatabaseHelper.Household.COLUMN_HIERARCHY_6, parser)) {
				parser.next();
				table.setHierarchy6(parser.getText());
				parser.nextTag(); //process </hierarchy6>
			}else{
				table.setHierarchy6("");
				parser.nextTag();
			}

			parser.nextTag(); //process <hierarchy7>
			if (!isEmptyTag(DatabaseHelper.Household.COLUMN_HIERARCHY_7, parser)) {
				parser.next();
				table.setHierarchy7(parser.getText());
				parser.nextTag(); //process </hierarchy7>
			}else{
				table.setHierarchy7("");
				parser.nextTag();
			}

			parser.nextTag(); //process <hierarchy8>
			if (!isEmptyTag(DatabaseHelper.Household.COLUMN_HIERARCHY_8, parser)) {
				parser.next();
				table.setHierarchy8(parser.getText());
				parser.nextTag(); //process </hierarchy8>
			}else{
				table.setHierarchy8("");
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


			parser.nextTag(); //last process tag
			parser.next();

			//values.add(table);

			database.insert(table);

			if (count % 100 == 0){
				savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
				publishProgress(count);
			}


		}

		publishProgress(count);

		database.setTransactionSuccessful();
		database.endTransaction();
		database.close();

		updateSyncReport(SyncEntity.HOUSEHOLDS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processMembers(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.MEMBERS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<Table> values = new ArrayList<>();
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

            parser.nextTag(); //process <code>
            //Log.d("TAG2", parser.getPositionDescription());
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_CODE, parser)) {
                parser.next();
                table.setCode(parser.getText());
                parser.nextTag(); //process </code>
            }else{
                table.setCode("");
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

			parser.nextTag(); //process <ageAtDeath>
			if (!isEmptyTag(DatabaseHelper.Member.COLUMN_AGE_AT_DEATH, parser)) {
				parser.next();
				table.setAgeAtDeath(Integer.parseInt(parser.getText()));
				parser.nextTag(); //process </ageAtDeath>
			}else{
				table.setAgeAtDeath(-1);
				parser.nextTag();
			}

            parser.nextTag(); //process <motherCode>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_MOTHER_CODE, parser)) {
                parser.next();
                table.setMotherCode(parser.getText());
                parser.nextTag(); //process </motherCode>
            }else{
                table.setMotherCode("");
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

            parser.nextTag(); //process <fatherCode>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_FATHER_CODE, parser)) {
                parser.next();
                table.setFatherCode(parser.getText());
                parser.nextTag(); //process </fatherCode>
            }else{
                table.setFatherCode("");
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

            parser.nextTag(); //process <spouseCode>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_SPOUSE_CODE, parser)) {
                parser.next();
                table.setSpouseCode(parser.getText());
                parser.nextTag(); //process </spouseCode>
            }else{
                table.setSpouseCode("");
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

			parser.nextTag(); //process <spouseType>
			if (!isEmptyTag(DatabaseHelper.Member.COLUMN_MARITAL_STATUS, parser)) {
				parser.next();
				table.setMaritalStatus(parser.getText());
				parser.nextTag(); //process </spouseType>
			}else{
				table.setMaritalStatus("");
				parser.nextTag();
			}

            parser.nextTag(); //process <houseCode>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_HOUSE_CODE, parser)) {
                parser.next();
                table.setHouseholdCode(parser.getText());
                parser.nextTag(); //process </houseCode>
            }else{
                table.setHouseholdCode("");
                parser.nextTag();
            }

            parser.nextTag(); //process <houseNumber>
            if (!isEmptyTag(DatabaseHelper.Member.COLUMN_HOUSE_NAME, parser)) {
                parser.next();
                table.setHouseholdName(parser.getText());
                parser.nextTag(); //process </houseNumber>
            }else{
                table.setHouseholdName("");
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
                //Log.d("e endDate", table.getEndDate());
            }

			parser.nextTag(); //process <entryHousehold>
			if (!isEmptyTag(DatabaseHelper.Member.COLUMN_ENTRY_HOUSEHOLD, parser)) { //endtag is temp
				parser.next();
				//Log.d("note entryHousehold", parser.getText() + ", " +parser.getPositionDescription());
				if (parser.getText()!=null){
					table.setEntryHousehold(parser.getText());
					parser.nextTag(); //process </entryHousehold>
				}
			}else{
				table.setEntryHousehold("");
				parser.nextTag();
				//Log.d("e entryHousehold", table.getEntryHousehold());
			}

			parser.nextTag(); //process <entryType>
			if (!isEmptyTag(DatabaseHelper.Member.COLUMN_ENTRY_TYPE, parser)) { //endtag is temp
				parser.next();
				//Log.d("note entryType", parser.getText() + ", " +parser.getPositionDescription());
				if (parser.getText()!=null){
					table.setEntryType(parser.getText());
					parser.nextTag(); //process </entryType>
				}
			}else{
				table.setEntryType("");
				parser.nextTag();
				//Log.d("e entryType", table.getEntryType());
			}

			parser.nextTag(); //process <entryDate>
			if (!isEmptyTag(DatabaseHelper.Member.COLUMN_ENTRY_DATE, parser)) { //endtag is temp
				parser.next();
				//Log.d("note entryDate", parser.getText() + ", " +parser.getPositionDescription());
				if (parser.getText()!=null){
					table.setEntryDate(parser.getText());
					parser.nextTag(); //process </entryDate>
				}
			}else{
				table.setEntryDate("");
				parser.nextTag();
				//Log.d("e entryDate", table.getEntryDate());
			}

			parser.nextTag(); //process <headRelationshipType>
			if (!isEmptyTag(DatabaseHelper.Member.COLUMN_HEAD_RELATIONSHIP_TYPE, parser)) { //endtag is temp
				parser.next();
				//Log.d("note headRelationshipType", parser.getText() + ", " +parser.getPositionDescription());
				if (parser.getText()!=null){
					table.setHeadRelationshipType(parser.getText());
					parser.nextTag(); //process </headRelationshipType>
				}
			}else{
				table.setHeadRelationshipType("");
				parser.nextTag();
				//Log.d("e entryType", table.getheadRelationshipType());
			}

			parser.nextTag(); //process <isHouseholdHead>
			if (!isEmptyTag(DatabaseHelper.Member.COLUMN_IS_HOUSEHOLD_HEAD, parser)) { //endtag is temp
				parser.next();
				//Log.d("note isHouseholdHead", parser.getText() + ", " +parser.getPositionDescription());
				if (parser.getText()!=null){
					table.setHouseholdHead(Boolean.parseBoolean(parser.getText()));
					parser.nextTag(); //process </entryDate>
				}
			}else{
				table.setHouseholdHead(false);
				parser.nextTag();
				//Log.d("e isHouseholdHead", table.isHouseholdHead());
			}

			parser.nextTag(); //process <isSecHouseholdHead>
			if (!isEmptyTag(DatabaseHelper.Member.COLUMN_IS_SEC_HOUSEHOLD_HEAD, parser)) { //endtag is temp
				parser.next();
				//Log.d("note isSecHouseholdHead", parser.getText() + ", " +parser.getPositionDescription());
				if (parser.getText()!=null){
					table.setSecHouseholdHead(Boolean.parseBoolean(parser.getText()));
					parser.nextTag(); //process </isSecHouseholdHead>
				}
			}else{
				table.setSecHouseholdHead(false);
				parser.nextTag();
				//Log.d("e isSecHouseholdHead", table.isHouseholdHead());
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


			parser.nextTag(); //process last tag
			parser.next();

			//values.add(table);
			database.insert(table);

			if (count % 200 == 0){
				savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
				publishProgress(count);
			}

		}

		publishProgress(count);

		database.setTransactionSuccessful();
		database.endTransaction();
		database.close();

		updateSyncReport(SyncEntity.MEMBERS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	//database.query(SyncReport.class, DatabaseHelper.SyncReport.COLUMN_REPORT_ID+"=?", new String[]{}, null, null, null);
	private void updateSyncReport(SyncEntity reportId, Date date, SyncStatus status){
		Database database = getDatabase();
		database.open();

		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.SyncReport.COLUMN_DATE, date==null ? "" : StringUtil.format(date, "yyyy-MM-dd HH:mm:ss"));
		cv.put(DatabaseHelper.SyncReport.COLUMN_STATUS, status.getCode());
		database.update(SyncReport.class, cv, DatabaseHelper.SyncReport.COLUMN_REPORT_ID+" = ?", new String[]{reportId.getCode()+""} );

		database.close();
	}

	private Map<SyncEntity, SyncStatus> getAllSyncReportStatus(){
		Map<SyncEntity, SyncStatus> statuses = new HashMap<>();

		Database database = getDatabase();
		database.open();

		Queries.getAllSyncReportBy(database, null, null).forEach( syncReport -> {
			statuses.put(syncReport.getReportId(), syncReport.getStatus());
		});

		database.close();

		return statuses;
	}

	private void updateDataset(DataSet dataSet, DownloadResponse response){
		String filename = Bootstrap.getAppPath() + response.getFileName();

		Database database = getDatabase();
		database.open();

		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.DataSet.COLUMN_FILENAME, filename);

		database.update(DataSet.class, cv, DatabaseHelper.DataSet._ID+"=?", new String[]{ dataSet.getId()+"" });

		database.close();
	}

	protected void onPostExecute(ExecutionReport executionReport) {

		Map<SyncEntity, SyncStatus> mapStatuses = getAllSyncReportStatus();

		String result = executionReport.getMessageResult();

		listener.onSyncFinished(result, getDownloadReports(mapStatuses), getPersistedReports(mapStatuses), executionReport.hasErrors(), executionReport.getErrorMessage());

		Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show(); //Maintain This
	}

	private List<SyncEntityReport> getPersistedReports(Map<SyncEntity, SyncStatus> mapStatuses){

		List<SyncEntityReport> reports = new ArrayList<>();

		String downloadedEntity = "";

		for (SyncEntity entity : savedValues.keySet()){
			switch (entity) {
				case PARAMETERS: 	 downloadedEntity = mContext.getString(R.string.sync_params_lbl); break;
				case MODULES: 		 downloadedEntity = mContext.getString(R.string.sync_modules_lbl);   break;
				case FORMS: 		 downloadedEntity = mContext.getString(R.string.sync_forms_lbl);     break;
				case DATASETS: 		 downloadedEntity = mContext.getString(R.string.sync_datasets_lbl); 	break;
				case DATASETS_CSV_FILES: downloadedEntity = mContext.getString(R.string.sync_datasets_csv_lbl); 	break;
				case TRACKING_LISTS: downloadedEntity = mContext.getString(R.string.sync_tracking_lists_lbl);	break;
				case USERS: 		 downloadedEntity = mContext.getString(R.string.sync_users_lbl); 	break;
				case REGIONS: 		 downloadedEntity = mContext.getString(R.string.sync_regions_lbl);	break;
				case HOUSEHOLDS: 	 downloadedEntity = mContext.getString(R.string.sync_households_lbl);	break;
				case MEMBERS: 		 downloadedEntity = mContext.getString(R.string.sync_members_lbl); 	  break;
			}

			boolean error = mapStatuses.get(entity)==SyncStatus.STATUS_SYNC_ERROR;
			String errorMsg = error ? errorMessageValues.get(entity) : "";

			//Log.d("get-error-msg", errorMessageValues+", "+errorMessageValues.get(entity)+", status="+mapStatuses.get(entity));

			String msg = mContext.getString(R.string.sync_synchronized_msg_lbl);
			String size = (savedValues.get(entity) == null ? "" : savedValues.get(entity).toString());
			//msg = msg.replace("#2", savedValues.get(entity).toString());
			msg = msg.replace("#1", downloadedEntity);

			//Log.d("persisted", ""+entity+", size="+size);
			reports.add(new SyncEntityReport(entity, msg, size, errorMsg, !error));
		}

		return reports;
	}

	private List<SyncEntityReport> getDownloadReports(Map<SyncEntity, SyncStatus> mapStatuses){

		List<SyncEntityReport> reports = new ArrayList<>();

		String downloadedEntity = "";

		for (SyncEntity entity : downloadedValues.keySet()){
			switch (entity) {
				case PARAMETERS: 	 downloadedEntity = mContext.getString(R.string.sync_params_lbl); break;
				case MODULES: 		 downloadedEntity = mContext.getString(R.string.sync_modules_lbl);   break;
				case FORMS: 		 downloadedEntity = mContext.getString(R.string.sync_forms_lbl);     break;
				case DATASETS: 		 downloadedEntity = mContext.getString(R.string.sync_datasets_lbl); 	break;
				case TRACKING_LISTS: downloadedEntity = mContext.getString(R.string.sync_tracking_lists_lbl);	break;
				case USERS: 		 downloadedEntity = mContext.getString(R.string.sync_users_lbl); 	break;
				case REGIONS: 		 downloadedEntity = mContext.getString(R.string.sync_regions_lbl);	break;
				case HOUSEHOLDS: 	 downloadedEntity = mContext.getString(R.string.sync_households_lbl);	break;
				case MEMBERS: 		 downloadedEntity = mContext.getString(R.string.sync_members_lbl); 	  break;
			}

			boolean error = mapStatuses.get(entity)==SyncStatus.STATUS_SYNC_ERROR;
			String errorMsg = error ? errorMessageValues.get(entity) : "";

			Integer value = downloadedValues.get(entity);
			String msg = mContext.getString(R.string.sync_downloaded_msg_lbl);
			String size = value == null ? "" : getInKB(value) + " KB";
			msg = msg.replace("#1", downloadedEntity);
			//msg = msg.replace("#2", downloadedValues.get(entity).toString());
			//Log.d("downloaded", ""+entity+", size="+size);
			reports.add(new SyncEntityReport(entity, msg, size, errorMsg, true));
		}

		return reports;

	}

	private String getInKB(Integer value){
		if (value < 1024){
			return String.format("%.2f", (value/1024D));
		} else {
			return (value/1024)+"";
		}
	}

	private String getInMB(Integer value){
		if (value < 1024*1024){
			return String.format("%.2f", (value/1024*1024D));
		} else {
			return (value/1024*1024)+"";
		}
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
			//return fileMimeType.equalsIgnoreCase(XML_MIME_TYPE);
			return XML_MIME_TYPE.startsWith(fileMimeType);
		}

		public boolean isZipFile(){
			//return fileMimeType.equalsIgnoreCase(ZIP_MIME_TYPE);
			return ZIP_MIME_TYPE.startsWith(fileMimeType);
		}
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

}
