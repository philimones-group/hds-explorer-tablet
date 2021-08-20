package org.philimone.hds.explorer.io;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Dataset;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.MaritalRelationship;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Module;
import org.philimone.hds.explorer.model.PregnancyRegistration;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Residency;
import org.philimone.hds.explorer.model.Round;
import org.philimone.hds.explorer.model.SyncReport;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.converters.FormMappingConverter;
import org.philimone.hds.explorer.model.converters.LabelMappingConverter;
import org.philimone.hds.explorer.model.enums.EstimatedDateOfDeliveryType;
import org.philimone.hds.explorer.model.enums.Gender;
import org.philimone.hds.explorer.model.enums.HeadRelationshipType;
import org.philimone.hds.explorer.model.enums.MaritalEndStatus;
import org.philimone.hds.explorer.model.enums.MaritalStartStatus;
import org.philimone.hds.explorer.model.enums.MaritalStatus;
import org.philimone.hds.explorer.model.enums.PregnancyStatus;
import org.philimone.hds.explorer.model.enums.SyncEntity;
import org.philimone.hds.explorer.model.enums.SyncState;
import org.philimone.hds.explorer.model.enums.SyncStatus;
import org.philimone.hds.explorer.model.enums.VisitLocationItem;
import org.philimone.hds.explorer.model.enums.VisitReason;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipEndType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipStartType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyStartType;
import org.philimone.hds.explorer.model.followup.TrackingList;
import org.philimone.hds.explorer.model.followup.TrackingSubjectList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

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
	private Box<SyncReport> boxSyncReports;
	private Box<Module> boxModules;
	private Box<CollectedData> boxCollectedData;
	private Box<CoreCollectedData> boxCoreCollectedData;
	private Box<Form> boxForms;
	private Box<User> boxUsers;
	private Box<Round> boxRounds;
	private Box<Region> boxRegions;
	private Box<Dataset> boxDatasets;
	private Box<TrackingList> boxTrackingLists;
	private Box<TrackingSubjectList> boxTrackingSubjects;
	private Box<Household> boxHouseholds;
	private Box<Member> boxMembers;
	private Box<Residency> boxResidencies;
	private Box<Visit> boxVisits;
	private Box<HeadRelationship> boxHeadRelationships;
	private Box<MaritalRelationship> boxMaritalRelationships;
	private Box<PregnancyRegistration> boxPregnancyRegistrations;

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
		this.boxSyncReports = ObjectBoxDatabase.get().boxFor(SyncReport.class);
		this.boxModules = ObjectBoxDatabase.get().boxFor(Module.class);
		this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
		this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
		this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
		this.boxUsers = ObjectBoxDatabase.get().boxFor(User.class);
		this.boxDatasets = ObjectBoxDatabase.get().boxFor(Dataset.class);
		this.boxTrackingLists = ObjectBoxDatabase.get().boxFor(TrackingList.class);
		this.boxTrackingSubjects = ObjectBoxDatabase.get().boxFor(TrackingSubjectList.class);
		this.boxRounds = ObjectBoxDatabase.get().boxFor(Round.class);
		this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
		this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
		this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
		this.boxResidencies = ObjectBoxDatabase.get().boxFor(Residency.class);
		this.boxVisits = ObjectBoxDatabase.get().boxFor(Visit.class);
		this.boxHeadRelationships = ObjectBoxDatabase.get().boxFor(HeadRelationship.class);
		this.boxMaritalRelationships = ObjectBoxDatabase.get().boxFor(MaritalRelationship.class);
		this.boxPregnancyRegistrations = ObjectBoxDatabase.get().boxFor(PregnancyRegistration.class);
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
			case ROUNDS:
				builder.append(" " + mContext.getString(R.string.sync_rounds_lbl));
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
			case RESIDENCIES:
				builder.append(" " + mContext.getString(R.string.sync_residencies_lbl));
				break;
			case VISITS:
				builder.append(" " + mContext.getString(R.string.sync_visits_lbl));
				break;
			case HEAD_RELATIONSHIPS:
				builder.append(" " + mContext.getString(R.string.sync_head_relationships_lbl));
				break;
			case MARITAL_RELATIONSHIPS:
				builder.append(" " + mContext.getString(R.string.sync_marital_relationships_lbl));
				break;
			case PREGNANCY_REGISTRATIONS:
				builder.append(" " + mContext.getString(R.string.sync_pregnancies_lbl));
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
						boxModules.removeAll();
						processUrl(baseurl + API_PATH + "/modules/zip", "modules.zip");
						break;
					case FORMS:
						boxForms.removeAll();
						processUrl(baseurl + API_PATH + "/forms/zip", "forms.zip");
						break;
					case DATASETS:
						this.boxDatasets.removeAll();
						processUrl(baseurl + API_PATH + "/datasets/zip", "datasets.zip");
						break;
					case DATASETS_CSV_FILES:
						downloadExternalDatasetFiles();
						break;
					case TRACKING_LISTS: /*testing*/
						this.boxTrackingLists.removeAll();
						this.boxTrackingSubjects.removeAll();
						processUrl(baseurl + API_PATH + "/trackinglists/zip", "trackinglists.zip");
						break;
					case USERS:
						this.boxUsers.removeAll();
						processUrl(baseurl + API_PATH + "/users/zip", "users.zip");
						break;
					case ROUNDS:
						this.boxRounds.removeAll();
						processUrl(baseurl + API_PATH + "/rounds/zip", "rounds.zip");
						break;
					case REGIONS:
						this.boxRegions.removeAll();
						processUrl(baseurl + API_PATH + "/regions/zip", "regions.zip");
						break;
					case HOUSEHOLDS:
						this.boxHouseholds.removeAll();
						this.boxCoreCollectedData.removeAll();
						processUrl(baseurl + API_PATH + "/households/zip", "households.zip");
						break;
					case MEMBERS:
						this.boxMembers.removeAll();
						this.boxCollectedData.removeAll();
						this.boxCoreCollectedData.removeAll();
						processUrl(baseurl + API_PATH + "/members/zip", "members.zip");
						break;
					case RESIDENCIES:
						this.boxResidencies.removeAll();
						processUrl(baseurl + API_PATH + "/residencies/zip", "residencies.zip");
						break;
					case VISITS:
						this.boxVisits.removeAll();
						processUrl(baseurl + API_PATH + "/visits/zip", "visits.zip");
						break;
					case HEAD_RELATIONSHIPS:
						this.boxHeadRelationships.removeAll();
						processUrl(baseurl + API_PATH + "/hrelationships/zip", "hrelationships.zip");
						break;
					case MARITAL_RELATIONSHIPS:
						this.boxMaritalRelationships.removeAll();
						processUrl(baseurl + API_PATH + "/mrelationships/zip", "mrelationships.zip");
						break;
					case PREGNANCY_REGISTRATIONS:
						this.boxPregnancyRegistrations.removeAll();
						processUrl(baseurl + API_PATH + "/pregnancies/zip", "pregnancies.zip");
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

	private void executeOnSyncStarted(SyncEntity entity, SyncState state){
		int size = getSyncRecordToDownload(entity);

		this.entityRecords = size;

		this.listener.onSyncStarted(entity, state, size);
	}

	private int getSyncRecordToDownload(SyncEntity entity){

		String result = processUrl(baseurl + API_PATH + "/sync-report/?id=" + entity.name());

		//Log.d("tag-report", "result="+result);
		//CHANGE THIS - TO HANDLE ERROS
		return Integer.parseInt(result);
	}

	private void downloadExternalDatasetFiles() throws Exception {

		List<Dataset> datasets = this.boxDatasets.getAll();

		for (Dataset dataSet : datasets){
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
	private void processUrl(String strUrl, String exportedFileName, Dataset dataSet) throws Exception {
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
				} else if (name.equalsIgnoreCase("residencies")) {
					processResidencies(parser);
				} else if (name.equalsIgnoreCase("rounds")) {
					processRounds(parser);
				} else if (name.equalsIgnoreCase("visits")) {
					processVisits(parser);
				} else if (name.equalsIgnoreCase("headrelationships")) {
					processHeadRelationships(parser);
				} else if (name.equalsIgnoreCase("maritalrelationships")) {
					processMaritalRelationships(parser);
				} else if (name.equalsIgnoreCase("pregnancyregistrations")) {
					processPregnancyRegistrations(parser);
				}
				break;
			}

			eventType = parser.next();
		}
	}

	private InputStream saveFileToStorage(DownloadResponse response) throws Exception {
		state = SyncState.DOWNLOADING;

		InputStream content = response.getInputStream();

		FileOutputStream fout = new FileOutputStream(Bootstrap.getBasePath() + response.getFileName());
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

		FileInputStream fin = new FileInputStream(Bootstrap.getBasePath() + response.getFileName());

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
		}

		savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
		publishProgress(count);

		updateSyncReport(SyncEntity.PARAMETERS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processModulesParams(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.MODULES, null, SyncStatus.STATUS_NOT_SYNCED);

		List<Module> values = new ArrayList<>();
		int count = 0;
		values.clear();

		parser.nextTag(); //<module>

		while (notEndOfXmlDoc("modules", parser)) {

			count++;
			Module table = new Module();

			parser.nextTag(); //<code>
			if (!isEmptyTag("code", parser)) {
                parser.next();
                table.setCode(parser.getText());
                parser.nextTag();
			}else{
				table.setCode("");
                parser.nextTag();
			}

            parser.nextTag(); //name
            if (!isEmptyTag("name", parser)) {
                parser.next();
                table.setName(parser.getText());
                parser.nextTag();
            }else{
                table.setName("");
                parser.nextTag();
            }

            parser.nextTag(); //description
            if (!isEmptyTag("description", parser)) {
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


		if (!values.isEmpty()) {
			boxModules.put(values);
		}

		savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
		publishProgress(count);

		updateSyncReport(SyncEntity.MODULES, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processFormsParams(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.FORMS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<Form> values = new ArrayList<>();
		int count = 0;
		values.clear();

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
			if (!isEmptyTag("regionLevel", parser)) {
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
			if (!isEmptyTag("isRegionForm", parser)) {
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
			if (!isEmptyTag("isFollowUpForm", parser)) {
				parser.next();
				table.setFollowUpForm(Boolean.parseBoolean(parser.getText()));
				parser.nextTag(); //process </isFollowUpOnly>
				//Log.d(count+"-isFollowUpOnly", "value="+ parser.getText());
			}else{
				table.setFollowUpForm(false);
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
				Map<String, String> map = new FormMappingConverter().convertToEntityProperty(parser.getText());
				table.setFormMap(map);
				//Log.d(count+"-formMap", "value="+ parser.getText());
				parser.nextTag(); //process </formMap>
			}else{
				table.setFormMap(new LinkedHashMap<>());
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

			values.add(table);
		}

		boxForms.put(values);
		savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
		publishProgress(count);

		state = SyncState.SAVING;
		entity = SyncEntity.FORMS;

		updateSyncReport(SyncEntity.FORMS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processDatasetsParams(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.DATASETS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<Dataset> values = new ArrayList<>();
		int count = 0;
		values.clear();

		parser.nextTag(); //<dataSet>

		while (notEndOfXmlDoc("datasets", parser)) {
			count++;

			Dataset table = new Dataset();

			parser.nextTag(); //process COLUMN_DATASET_ID
			if (!isEmptyTag("datasetId", parser)) {
				parser.next();
				table.setDatasetId(parser.getText());
				parser.nextTag(); //process </COLUMN_DATASET_ID>
				//Log.d(count+"-COLUMN_DATASET_ID", "value="+ parser.getText());
			}else{
				table.setDatasetId("");
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_NAME
			if (!isEmptyTag("name", parser)) {
				parser.next();
				table.setName(parser.getText());
				parser.nextTag(); //process </COLUMN_NAME>
				//Log.d(count+"-COLUMN_NAME", "value="+ parser.getText());
			}else{
				table.setName("");
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_LABEL
			if (!isEmptyTag("label", parser)) {
				parser.next();
				table.label = parser.getText();
				parser.nextTag(); //process </COLUMN_LABEL>
				//Log.d(count+"-COLUMN_LABEL", "value="+ parser.getText());
			}else{
				table.label = "";
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_KEYCOLUMN
			if (!isEmptyTag("keyColumn", parser)) {
				parser.next();
				table.setKeyColumn(parser.getText());
				parser.nextTag(); //process </COLUMN_KEYCOLUMN>
				//Log.d(count+"-COLUMN_KEYCOLUMN", "value="+ parser.getText());
			}else{
				table.setKeyColumn("");
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_TABLE_NAME
			if (!isEmptyTag("tableName", parser)) {
				parser.next();
				table.setTableName(parser.getText());
				parser.nextTag(); //process </COLUMN_TABLE_NAME>
				//Log.d(count+"-COLUMN_TABLE_NAME", "value="+ parser.getText());
			}else{
				table.setTableName("");
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_TABLE_COLUMN
			if (!isEmptyTag("tableColumn", parser)) {
				parser.next();
				table.setTableColumn(parser.getText());
				parser.nextTag(); //process </COLUMN_TABLE_COLUMN>
				//Log.d(count+"-COLUMN_TABLE_COLUMN", "value="+ parser.getText());
			}else{
				table.setTableColumn("");
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_LABELS
			if (!isEmptyTag("tableColumnLabels", parser)) {
				parser.next();
				table.tableColumnLabels = parser.getText();
				//Log.d(count+"-COLUMN_LABELS", "value="+ parser.getText());
				parser.nextTag(); //process </COLUMN_LABELS>
			}else{
				table.tableColumnLabels = "";
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_CREATED_BY
			if (!isEmptyTag("createdBy", parser)) {
				parser.next();
				table.setCreatedBy(parser.getText());
				parser.nextTag(); //process </COLUMN_CREATED_BY>
				//Log.d(count+"-COLUMN_CREATED_BY", "value="+ parser.getText());
			}else{
				table.setCreatedBy("");
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_CREATION_DATE
			if (!isEmptyTag("creationDate", parser)) {
				parser.next();
				table.setCreationDate(parser.getText());
				parser.nextTag(); //process </COLUMN_CREATION_DATE>
				//Log.d(count+"-COLUMN_CREATION_DATE", "value="+ parser.getText());
			}else{
				table.setCreationDate("");
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_UPDATED_BY
			if (!isEmptyTag("updatedBy", parser)) {
				parser.next();
				table.setUpdatedBy(parser.getText());
				parser.nextTag(); //process </COLUMN_UPDATED_BY>
				//Log.d(count+"-COLUMN_UPDATED_BY", "value="+ parser.getText());
			}else{
				table.setUpdatedBy("");
				parser.nextTag();
			}

			parser.nextTag(); //process COLUMN_UPDATED_DATE
			if (!isEmptyTag("updatedDate", parser)) {
				parser.next();
				table.setUpdatedDate(parser.getText());
				parser.nextTag(); //process </COLUMN_UPDATED_DATE>
				//Log.d(count+"-COLUMN_UPDATED_DATE", "value="+ parser.getText());
			}else{
				table.setUpdatedDate("");
				parser.nextTag();
			}

			table.setFilename(""); //an empty filename - the variable will be set after downloading data

			parser.nextTag();
			parser.next();



			values.add(table);

		}

		state = SyncState.SAVING;
		entity = SyncEntity.DATASETS;

		this.boxDatasets.put(values);
		savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
		publishProgress(count);

		updateSyncReport(SyncEntity.DATASETS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processTrackingListsParams(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.TRACKING_LISTS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<TrackingList> values = new ArrayList<>();
		List<TrackingSubjectList> valuesTs = new ArrayList<>();
		int tlistCount = 0;
		values.clear();

		parser.nextTag();

		while (notEndOfXmlDoc("trackinglists", parser)) {

			if (isEndTag("tracking_list", parser) || isEmptyTag("tracking_list", parser)){
				parser.nextTag();
				continue;
			}

			long trackingId = 0;
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

				trackingId = this.boxTrackingLists.put(trackingList); //insert on db
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

						//this.boxTrackingSubjects.put(tml);
						valuesTs.add(tml);

					}
					parser.nextTag();
					parser.nextTag(); //jump to next tag eg. <member> or </list>
				}

				if (!valuesTs.isEmpty()) {
					this.boxTrackingSubjects.put(valuesTs);
					valuesTs.clear();
				}

			}

			savedValues.put(entity, tlistCount); //publish progress is a bit slow - its not reporting well the numbers
			publishProgress(tlistCount);

		}

		savedValues.put(entity, tlistCount);
		publishProgress(tlistCount);

		updateSyncReport(SyncEntity.TRACKING_LISTS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processUsersParams(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.USERS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<User> values = new ArrayList<>();
		int count = 0;
		values.clear();

		parser.nextTag();

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
			this.boxUsers.put(table);
			savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
			publishProgress(count);

			
		}

		//this.boxUsers.put(values);
		savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
		publishProgress(count);

		updateSyncReport(SyncEntity.USERS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processRegions(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.REGIONS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<Region> values = new ArrayList<>();
		int count = 0;
		values.clear();

		parser.nextTag(); //<region>

		while (notEndOfXmlDoc("regions", parser)) {

			count++;
			Region table = new Region();

			parser.nextTag(); //<code>
			if (!isEmptyTag("code", parser)) {
				parser.next();
				table.setCode(parser.getText());
				parser.nextTag();
			}else{
				table.setCode("");
				parser.nextTag();
			}

			parser.nextTag(); //name
			if (!isEmptyTag("name", parser)) {
				parser.next();
				table.setName(parser.getText());
				parser.nextTag();
			}else{
				table.setName("");
				parser.nextTag();
			}

			parser.nextTag(); //hierarchyLevel
			if (!isEmptyTag("hierarchyLevel", parser)) {
				parser.next();
				table.setLevel(parser.getText());
				parser.nextTag();
			}else{
				table.setLevel("");
				parser.nextTag();
			}

			parser.nextTag(); //parent
			if (!isEmptyTag("parent", parser)) {
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

		if (!values.isEmpty()) {
			this.boxRegions.put(values);
		}

		savedValues.put(entity, count);
		publishProgress(count);

		updateSyncReport(SyncEntity.REGIONS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processHouseholds(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.HOUSEHOLDS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<Household> values = new ArrayList<>();
		int count = 0;
		values.clear();

		parser.nextTag();

		while (notEndOfXmlDoc("households", parser)) {
			count++;

			Household table = new Household();

            parser.nextTag(); //process <code>
            if (!isEmptyTag("code", parser)) {
                parser.next();
                table.setCode(parser.getText());
                parser.nextTag(); //process </code>
            }else{
                table.setCode("");
                parser.nextTag();
            }

			parser.nextTag(); //process <region>
			if (!isEmptyTag("region", parser)) {
				parser.next();
				table.setRegion(parser.getText());
				parser.nextTag(); //process </region>
			}else{
				table.setRegion("");
				parser.nextTag();
			}

            parser.nextTag(); //process <houseNumber>
            if (!isEmptyTag("name", parser)) {
                parser.next();
                table.setName(parser.getText());
                parser.nextTag(); //process </houseNumber>
            }else{
                table.setName("");
                parser.nextTag();
            }

            parser.nextTag(); //process <headCode>
            if (!isEmptyTag("headCode", parser)) {
                parser.next();
                table.setHeadCode(parser.getText());
                parser.nextTag(); //process </headCode>
            }else{
                table.setHeadCode("");
                parser.nextTag();
            }

			parser.nextTag(); //process <headName>
			if (!isEmptyTag("headName", parser)) {
				parser.next();
				table.setHeadName(parser.getText());
				parser.nextTag(); //process </headName>
			}else{
				table.setHeadName("");
				parser.nextTag();
			}

            parser.nextTag(); //process <secHeadCode>
            if (!isEmptyTag("secHeadCode", parser)) {
                parser.next();
                table.setSecHeadCode(parser.getText());
                parser.nextTag(); //process </subsHeadCode>
            }else{
                table.setSecHeadCode("");
                parser.nextTag();
            }

            parser.nextTag(); //process <hierarchy1>
            if (!isEmptyTag("hierarchy1", parser)) {
                parser.next();
                table.setHierarchy1(parser.getText());
                parser.nextTag(); //process </hierarchy1>
            }else{
                table.setHierarchy1("");
                parser.nextTag();
            }

            parser.nextTag(); //process <hierarchy2>
            if (!isEmptyTag("hierarchy2", parser)) {
                parser.next();
                table.setHierarchy2(parser.getText());
                parser.nextTag(); //process </hierarchy2>
            }else{
                table.setHierarchy2("");
                parser.nextTag();
            }

            parser.nextTag(); //process <hierarchy3>
            if (!isEmptyTag("hierarchy3", parser)) {
                parser.next();
                table.setHierarchy3(parser.getText());
                parser.nextTag(); //process </hierarchy3>
            }else{
                table.setHierarchy3("");
                parser.nextTag();
            }

            parser.nextTag(); //process <hierarchy4>
            if (!isEmptyTag("hierarchy4", parser)) {
                parser.next();
                table.setHierarchy4(parser.getText());
                parser.nextTag(); //process </hierarchy4>
            }else{
                table.setHierarchy4("");
                parser.nextTag();
            }

			parser.nextTag(); //process <hierarchy5>
			if (!isEmptyTag("hierarchy5", parser)) {
				parser.next();
				table.setHierarchy5(parser.getText());
				parser.nextTag(); //process </hierarchy5>
			}else{
				table.setHierarchy5("");
				parser.nextTag();
			}

			parser.nextTag(); //process <hierarchy6>
			if (!isEmptyTag("hierarchy6", parser)) {
				parser.next();
				table.setHierarchy6(parser.getText());
				parser.nextTag(); //process </hierarchy6>
			}else{
				table.setHierarchy6("");
				parser.nextTag();
			}

			parser.nextTag(); //process <hierarchy7>
			if (!isEmptyTag("hierarchy7", parser)) {
				parser.next();
				table.setHierarchy7(parser.getText());
				parser.nextTag(); //process </hierarchy7>
			}else{
				table.setHierarchy7("");
				parser.nextTag();
			}

			parser.nextTag(); //process <hierarchy8>
			if (!isEmptyTag("hierarchy8", parser)) {
				parser.next();
				table.setHierarchy8(parser.getText());
				parser.nextTag(); //process </hierarchy8>
			}else{
				table.setHierarchy8("");
				parser.nextTag();
			}

            parser.nextTag(); //process <gpsAccuracy>
            if (!isEmptyTag("gpsAccuracy", parser)) {
                parser.next();
                table.setGpsAccuracy(StringUtil.toDouble(parser.getText()));
                parser.nextTag(); //process </gpsAccuracy>
                //Log.d("note gpsacc", table.getGpsAccuracy());
            }else{
                table.setGpsAccuracy(null);
				table.setGpsNull(true);
                parser.nextTag();
                //Log.d("e gpsacc", table.getGpsAccuracy());
            }

            parser.nextTag(); //process <gpsAltitude>
            if (!isEmptyTag("gpsAltitude", parser)) {
                parser.next();
                table.setGpsAltitude(StringUtil.toDouble(parser.getText()));
                parser.nextTag(); //process </gpsAltitude>
            }else{
                table.setGpsAltitude(null);
				table.setGpsNull(true);
                parser.nextTag();
            }

            parser.nextTag(); //process <gpsLatitude>
            if (!isEmptyTag("gpsLatitude", parser)) {
                parser.next();
                table.setGpsLatitude(StringUtil.toDouble(parser.getText()));
				table.setCosLatitude(Math.cos(table.getGpsLatitude()*Math.PI / 180.0)); // cos_lat = cos(lat * PI / 180)
				table.setSinLatitude(Math.sin(table.getGpsLatitude()*Math.PI / 180.0)); // sin_lat = sin(lat * PI / 180)
                parser.nextTag(); //process </gpsLatitude>
            }else{
                table.setGpsLatitude(null);
				table.setGpsNull(true);
                parser.nextTag();
            }

            parser.nextTag(); //process <gpsLongitude>
            if (!isEmptyTag("gpsLongitude", parser)) {
                parser.next();
                table.setGpsLongitude(StringUtil.toDouble(parser.getText()));
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

			values.add(table);

			//database.insert(table);

			if (count % 500 == 0){
				this.boxHouseholds.put(values); //try with runTx
				values.clear();
				savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
				publishProgress(count);
			}


		}

		if (!values.isEmpty()) {
			this.boxHouseholds.put(values);
		}

		savedValues.put(entity, count);
		publishProgress(count);

		updateSyncReport(SyncEntity.HOUSEHOLDS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processMembers(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.MEMBERS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<Member> values = new ArrayList<>();
		int count = 0;
		values.clear();

		parser.nextTag();

		while (notEndOfXmlDoc("members", parser)) {
			count++;

			Member table = new Member();

            parser.nextTag(); //process <code>
            //Log.d("TAG2", parser.getPositionDescription());
            if (!isEmptyTag("code", parser)) {
                parser.next();
                table.setCode(parser.getText());
                parser.nextTag(); //process </code>
            }else{
                table.setCode("");
                parser.nextTag();
            }

            parser.nextTag(); //process <name>
            if (!isEmptyTag("name", parser)) {
                parser.next();
                table.setName(parser.getText());
                parser.nextTag(); //process </name>
            }else{
                table.setName("");
                parser.nextTag();
            }

            parser.nextTag(); //process <gender>
            if (!isEmptyTag("gender", parser)) {
                parser.next();
                table.setGender(Gender.getFrom(parser.getText()));
                parser.nextTag(); //process </gender>
            }else{
                table.setGender(Gender.INVALID_ENUM);
                parser.nextTag();
            }

            parser.nextTag(); //process <dob>
            if (!isEmptyTag("dob", parser)) {
                parser.next();
                table.setDob(parser.getText());
                parser.nextTag(); //process </dob>
            }else{
                table.setDob("");
                parser.nextTag();
            }

            parser.nextTag(); //process <age>
            if (!isEmptyTag("age", parser)) {
                parser.next();
                table.setAge(Integer.parseInt(parser.getText()));
                parser.nextTag(); //process </age>
            }else{
                table.setAge(-1);
                parser.nextTag();
            }

			parser.nextTag(); //process <ageAtDeath>
			if (!isEmptyTag("ageAtDeath", parser)) {
				parser.next();
				table.setAgeAtDeath(Integer.parseInt(parser.getText()));
				parser.nextTag(); //process </ageAtDeath>
			}else{
				table.setAgeAtDeath(-1);
				parser.nextTag();
			}

            parser.nextTag(); //process <motherCode>
            if (!isEmptyTag("motherCode", parser)) {
                parser.next();
                table.setMotherCode(parser.getText());
                parser.nextTag(); //process </motherCode>
            }else{
                table.setMotherCode("");
                parser.nextTag();
            }

            parser.nextTag(); //process <motherName>
            if (!isEmptyTag("motherName", parser)) {
                parser.next();
                table.setMotherName(parser.getText());
                parser.nextTag(); //process </motherName>
            }else{
                table.setMotherName("");
                parser.nextTag();
            }

            parser.nextTag(); //process <fatherCode>
            if (!isEmptyTag("fatherCode", parser)) {
                parser.next();
                table.setFatherCode(parser.getText());
                parser.nextTag(); //process </fatherCode>
            }else{
                table.setFatherCode("");
                parser.nextTag();
            }

            parser.nextTag(); //process <fatherName>
            if (!isEmptyTag("fatherName", parser)) {
                parser.next();
                table.setFatherName(parser.getText());
                parser.nextTag(); //process </fatherName>
            }else{
                table.setFatherName("");
                parser.nextTag();
            }

			parser.nextTag(); //process <maritalStatus>
			if (!isEmptyTag("maritalStatus", parser)) {
				parser.next();
				table.setMaritalStatus(MaritalStatus.getFrom(parser.getText()));
				parser.nextTag(); //process </maritalStatus>
			}else{
				table.setMaritalStatus(null);
				parser.nextTag();
			}

            parser.nextTag(); //process <spouseCode>
            if (!isEmptyTag("spouseCode", parser)) {
                parser.next();
                table.setSpouseCode(parser.getText());
                parser.nextTag(); //process </spouseCode>
            }else{
                table.setSpouseCode("");
                parser.nextTag();
            }

            parser.nextTag(); //process <spouseName>
            if (!isEmptyTag("spouseName", parser)) {
                parser.next();
                table.setSpouseName(parser.getText());
                parser.nextTag(); //process </spouseName>
            }else{
                table.setSpouseName("");
                parser.nextTag();
            }

            parser.nextTag(); //process <householdCode>
            if (!isEmptyTag("householdCode", parser)) {
                parser.next();
                table.setHouseholdCode(parser.getText());
                parser.nextTag(); //process </householdCode>
            }else{
                table.setHouseholdCode("");
                parser.nextTag();
            }

            parser.nextTag(); //process <householdName>
            if (!isEmptyTag("householdName", parser)) {
                parser.next();
                table.setHouseholdName(parser.getText());
                parser.nextTag(); //process </houseNumber>
            }else{
                table.setHouseholdName("");
                parser.nextTag();
            }

            parser.nextTag(); //process <startType>
            if (!isEmptyTag("startType", parser)) {
                parser.next();
                table.setStartType(ResidencyStartType.getFrom(parser.getText()));
                parser.nextTag(); //process </startType>
            }else{
                table.setStartType(null);
                parser.nextTag();
            }

            parser.nextTag(); //process <startDate>
            if (!isEmptyTag("startDate", parser)) {
                parser.next();
                table.setStartDate(parser.getText());
                parser.nextTag(); //process </startDate>
            }else{
                table.setStartDate("");
                parser.nextTag();
            }

            parser.nextTag(); //process <endType>
            if (!isEmptyTag("endType", parser)) {
                parser.next();
                table.setEndType(ResidencyEndType.getFrom(parser.getText()));
                parser.nextTag(); //process </endType>
            }else{
                table.setEndType(null);
                parser.nextTag();
            }

            parser.nextTag(); //process <endDate>
            if (!isEmptyTag("endDate", parser)) { //endtag is temp
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
			if (!isEmptyTag("entryHousehold", parser)) { //endtag is temp
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
			if (!isEmptyTag("entryType", parser)) { //endtag is temp
				parser.next();
				//Log.d("note entryType", parser.getText() + ", " +parser.getPositionDescription());
				if (parser.getText()!=null){
					table.setEntryType(ResidencyStartType.getFrom(parser.getText()));
					parser.nextTag(); //process </entryType>
				}
			}else{
				table.setEntryType(null);
				parser.nextTag();
				//Log.d("e entryType", table.getEntryType());
			}

			parser.nextTag(); //process <entryDate>
			if (!isEmptyTag("entryDate", parser)) { //endtag is temp
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
			if (!isEmptyTag("headRelationshipType", parser)) { //endtag is temp
				parser.next();
				//Log.d("note headRelationshipType", parser.getText() + ", " +parser.getPositionDescription());
				if (parser.getText()!=null){
					table.setHeadRelationshipType(HeadRelationshipType.getFrom(parser.getText()));
					parser.nextTag(); //process </headRelationshipType>
				}
			}else{
				table.setHeadRelationshipType(null);
				parser.nextTag();
				//Log.d("e entryType", table.getheadRelationshipType());
			}

			parser.nextTag(); //process <isHouseholdHead>
			if (!isEmptyTag("isHouseholdHead", parser)) { //endtag is temp
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

			/*
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
			}*/

            //CORRECT THE BUG AROUND HERE
            parser.nextTag(); //process <gpsAccuracy>
            if (!isEmptyTag("gpsAccuracy", parser)) {
                parser.next();
                table.setGpsAccuracy(StringUtil.toDouble(parser.getText()));
                parser.nextTag(); //process </gpsAccuracy>
                //Log.d("note gpsacc", table.getGpsAccuracy());
            }else{
                table.setGpsAccuracy(null);
				table.setGpsNull(true);
                parser.nextTag();

            }

            parser.nextTag(); //process <gpsAltitude>
            if (!isEmptyTag("gpsAltitude", parser)) {
                parser.next();
                table.setGpsAltitude(StringUtil.toDouble(parser.getText()));
                parser.nextTag(); //process </gpsAltitude>
            }else{
                table.setGpsAltitude(null);
				table.setGpsNull(true);
                parser.nextTag();
            }

            parser.nextTag(); //process <gpsLatitude>
            if (!isEmptyTag("gpsLatitude", parser)) {
                parser.next();
                table.setGpsLatitude(StringUtil.toDouble(parser.getText()));
				table.setCosLatitude(Math.cos(table.getGpsLatitude()*Math.PI / 180.0)); // cos_lat = cos(lat * PI / 180)
				table.setSinLatitude(Math.sin(table.getGpsLatitude()*Math.PI / 180.0)); // sin_lat = sin(lat * PI / 180)
                parser.nextTag(); //process </gpsLatitude>
            }else{
                table.setGpsLatitude(null);
				table.setGpsNull(true);
                parser.nextTag();
            }

            parser.nextTag(); //process <gpsLongitude>
            if (!isEmptyTag("gpsLongitude", parser)) {
                parser.next();
                table.setGpsLongitude(StringUtil.toDouble(parser.getText()));
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

			values.add(table);

			if (count % 500 == 0){
				this.boxMembers.put(values); //try with runTx
				values.clear();
				savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
				publishProgress(count);
			}


		}

		if (!values.isEmpty()) {
			this.boxMembers.put(values);
		}

		savedValues.put(entity, count);
		publishProgress(count);

		updateSyncReport(SyncEntity.MEMBERS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processRounds(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.ROUNDS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<Round> values = new ArrayList<>();
		int count = 0;
		values.clear();

		parser.nextTag();

		while (notEndOfXmlDoc("rounds", parser)) {
			count++;

			Round table = new Round();

			parser.nextTag(); //roundNumber
			if (!isEmptyTag("roundNumber", parser)) {
				parser.next();
				table.roundNumber = Integer.parseInt(parser.getText());
				parser.nextTag();
			}else{
				//table.roundNumber = 0;
				parser.nextTag();
			}

			parser.nextTag(); //startDate
			if (!isEmptyTag("startDate", parser)) {
				parser.next();
				table.startDate = StringUtil.toDate(parser.getText(), "yyyy-MM-dd");
				parser.nextTag();
			}else{
				//table.startDate = false;
				parser.nextTag();
			}

			parser.nextTag(); //endDate
			if (!isEmptyTag("endDate", parser)) {
				parser.next();
				table.endDate = StringUtil.toDate(parser.getText(), "yyyy-MM-dd");
				parser.nextTag();
			}else{
				//table.endDate = false;
				parser.nextTag();
			}

			parser.nextTag(); //description
			if (!isEmptyTag("description", parser)) {
				parser.next();
				table.description = parser.getText();
				parser.nextTag();
			}else{
				table.description = "";
				parser.nextTag();
			}

			parser.nextTag(); //last process tag
			parser.next();

			//values.add(table);
			//database.insert(table);

			this.boxRounds.put(table); //try with runTx
			savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
			publishProgress(count);


		}

		if (!values.isEmpty()) {
			this.boxRounds.put(values);
		}

		savedValues.put(entity, count);
		publishProgress(count);

		updateSyncReport(SyncEntity.ROUNDS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processResidencies(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.RESIDENCIES, null, SyncStatus.STATUS_NOT_SYNCED);

		List<Residency> values = new ArrayList<>();
		int count = 0;
		values.clear();

		parser.nextTag();

		while (notEndOfXmlDoc("residencies", parser)) {
			count++;

			Residency table = new Residency();

			parser.nextTag(); //householdCode
			if (!isEmptyTag("householdCode", parser)) {
				parser.next();
				table.householdCode = parser.getText();
				parser.nextTag();
			}else{
				table.householdCode = "";
				parser.nextTag();
			}

			parser.nextTag(); //memberCode
			if (!isEmptyTag("memberCode", parser)) {
				parser.next();
				table.memberCode = parser.getText();
				parser.nextTag();
			}else{
				table.memberCode = "";
				parser.nextTag();
			}

			parser.nextTag(); //startType
			if (!isEmptyTag("startType", parser)) {
				parser.next();
				table.startType = ResidencyStartType.getFrom(parser.getText());
				parser.nextTag();
			}else{
				table.startType = null;
				parser.nextTag();
			}

			parser.nextTag(); //startDate
			if (!isEmptyTag("startDate", parser)) {
				parser.next();
				table.startDate = StringUtil.toDate(parser.getText(), "yyyy-MM-dd");
				parser.nextTag();
			}else{
				//table.startDate = false;
				parser.nextTag();
			}

			parser.nextTag(); //endType
			if (!isEmptyTag("endType", parser)) {
				parser.next();
				table.endType = ResidencyEndType.getFrom(parser.getText());
				parser.nextTag();
			}else{
				table.endType = null;
				parser.nextTag();
			}

			parser.nextTag(); //endDate
			if (!isEmptyTag("endDate", parser)) {
				parser.next();
				table.endDate = StringUtil.toDate(parser.getText(), "yyyy-MM-dd");
				parser.nextTag();
			}else{
				//table.endDate = false;
				parser.nextTag();
			}

			parser.nextTag(); //last process tag
			parser.next();

			values.add(table);

			//database.insert(table);

			if (count % 500 == 0){
				this.boxResidencies.put(values); //try with runTx
				values.clear();
				savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
				publishProgress(count);
			}

		}

		if (!values.isEmpty()) {
			this.boxResidencies.put(values);
		}

		savedValues.put(entity, count);
		publishProgress(count);

		updateSyncReport(SyncEntity.RESIDENCIES, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processVisits(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.VISITS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<Visit> values = new ArrayList<>();
		int count = 0;
		values.clear();

		parser.nextTag();

		while (notEndOfXmlDoc("visits", parser)) {
			count++;

			Visit table = new Visit();

			parser.nextTag(); //code
			if (!isEmptyTag("code", parser)) {
				parser.next();
				table.code = parser.getText();
				parser.nextTag();
			}else{
				table.code = "";
				parser.nextTag();
			}

			parser.nextTag(); //householdCode
			if (!isEmptyTag("householdCode", parser)) {
				parser.next();
				table.householdCode = parser.getText();
				parser.nextTag();
			}else{
				table.householdCode = "";
				parser.nextTag();
			}

			parser.nextTag(); //visitDate
			if (!isEmptyTag("visitDate", parser)) {
				parser.next();
				table.visitDate = StringUtil.toDate(parser.getText(), "yyyy-MM-dd");
				parser.nextTag();
			}else{
				//table.visitDate = false;
				parser.nextTag();
			}

			parser.nextTag(); //visitReason
			if (!isEmptyTag("visitReason", parser)) {
				parser.next();
				table.visitReason = VisitReason.getFrom(parser.getText());
				parser.nextTag();
			}else{
				table.visitReason = null;
				parser.nextTag();
			}

			parser.nextTag(); //visitLocation
			if (!isEmptyTag("visitLocation", parser)) {
				parser.next();
				table.visitLocation = VisitLocationItem.getFrom(parser.getText());
				parser.nextTag();
			}else{
				table.visitLocation = null;
				parser.nextTag();
			}

			parser.nextTag(); //visitLocationOther
			if (!isEmptyTag("visitLocationOther", parser)) {
				parser.next();
				table.visitLocationOther = parser.getText();
				parser.nextTag();
			}else{
				table.visitLocationOther = "";
				parser.nextTag();
			}

			parser.nextTag(); //roundNumber
			if (!isEmptyTag("roundNumber", parser)) {
				parser.next();
				table.roundNumber = Integer.parseInt(parser.getText());
				parser.nextTag();
			}else{
				//table.roundNumber = 0;
				parser.nextTag();
			}

			parser.nextTag(); //respondentCode
			if (!isEmptyTag("respondentCode", parser)) {
				parser.next();
				table.respondentCode = parser.getText();
				parser.nextTag();
			}else{
				table.respondentCode = "";
				parser.nextTag();
			}

			parser.nextTag(); //hasInterpreter
			if (!isEmptyTag("hasInterpreter", parser)) {
				parser.next();
				table.hasInterpreter = Boolean.parseBoolean(parser.getText());
				parser.nextTag();
			}else{
				//table.hasInterpreter = false;
				parser.nextTag();
			}

			parser.nextTag(); //interpreterName
			if (!isEmptyTag("interpreterName", parser)) {
				parser.next();
				table.interpreterName = parser.getText();
				parser.nextTag();
			}else{
				table.interpreterName = "";
				parser.nextTag();
			}

			parser.nextTag(); //gpsAccuracy
			if (!isEmptyTag("gpsAccuracy", parser)) {
				parser.next();
				table.gpsAccuracy = Double.parseDouble(parser.getText());
				parser.nextTag();
			}else{
				//table.gpsAccuracy = 0D;
				parser.nextTag();
			}

			parser.nextTag(); //gpsAltitude
			if (!isEmptyTag("gpsAltitude", parser)) {
				parser.next();
				table.gpsAltitude = Double.parseDouble(parser.getText());
				parser.nextTag();
			}else{
				//table.gpsAltitude = 0D;
				parser.nextTag();
			}

			parser.nextTag(); //gpsLatitude
			if (!isEmptyTag("gpsLatitude", parser)) {
				parser.next();
				table.gpsLatitude = Double.parseDouble(parser.getText());
				parser.nextTag();
			}else{
				//table.gpsLatitude = 0D;
				parser.nextTag();
			}

			parser.nextTag(); //gpsLongitude
			if (!isEmptyTag("gpsLongitude", parser)) {
				parser.next();
				table.gpsLongitude = Double.parseDouble(parser.getText());
				parser.nextTag();
			}else{
				//table.gpsLongitude = 0D;
				parser.nextTag();
			}

			parser.nextTag(); //last process tag
			parser.next();

			values.add(table);

			//database.insert(table);

			if (count % 500 == 0){
				this.boxVisits.put(values); //try with runTx
				values.clear();
				savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
				publishProgress(count);
			}

		}

		if (!values.isEmpty()) {
			this.boxVisits.put(values);
		}

		savedValues.put(entity, count);
		publishProgress(count);

		updateSyncReport(SyncEntity.VISITS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processHeadRelationships(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.HEAD_RELATIONSHIPS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<HeadRelationship> values = new ArrayList<>();
		int count = 0;
		values.clear();

		parser.nextTag();

		while (notEndOfXmlDoc("headrelationships", parser)) {
			count++;

			HeadRelationship table = new HeadRelationship();

			parser.nextTag(); //householdCode
			if (!isEmptyTag("householdCode", parser)) {
				parser.next();
				table.householdCode = parser.getText();
				parser.nextTag();
			}else{
				table.householdCode = "";
				parser.nextTag();
			}

			parser.nextTag(); //memberCode
			if (!isEmptyTag("memberCode", parser)) {
				parser.next();
				table.memberCode = parser.getText();
				parser.nextTag();
			}else{
				table.memberCode = "";
				parser.nextTag();
			}

			parser.nextTag(); //headCode
			if (!isEmptyTag("headCode", parser)) {
				parser.next();
				table.headCode = parser.getText();
				parser.nextTag();
			}else{
				table.headCode = "";
				parser.nextTag();
			}

			parser.nextTag(); //relationshipType
			if (!isEmptyTag("relationshipType", parser)) {
				parser.next();
				table.relationshipType = HeadRelationshipType.getFrom(parser.getText());
				parser.nextTag();
			}else{
				table.relationshipType = null;
				parser.nextTag();
			}

			parser.nextTag(); //startType
			if (!isEmptyTag("startType", parser)) {
				parser.next();
				table.startType = HeadRelationshipStartType.getFrom(parser.getText());
				parser.nextTag();
			}else{
				table.startType = null;
				parser.nextTag();
			}

			parser.nextTag(); //startDate
			if (!isEmptyTag("startDate", parser)) {
				parser.next();
				table.startDate = StringUtil.toDate(parser.getText(), "yyyy-MM-dd");
				parser.nextTag();
			}else{
				//table.startDate = false;
				parser.nextTag();
			}

			parser.nextTag(); //endType
			if (!isEmptyTag("endType", parser)) {
				parser.next();
				table.endType = HeadRelationshipEndType.getFrom(parser.getText());
				parser.nextTag();
			}else{
				table.endType = null;
				parser.nextTag();
			}

			parser.nextTag(); //endDate
			if (!isEmptyTag("endDate", parser)) {
				parser.next();
				table.endDate = StringUtil.toDate(parser.getText(), "yyyy-MM-dd");
				parser.nextTag();
			}else{
				//table.endDate = false;
				parser.nextTag();
			}

			parser.nextTag(); //last process tag
			parser.next();

			values.add(table);

			//database.insert(table);

			if (count % 500 == 0){
				this.boxHeadRelationships.put(values); //try with runTx
				values.clear();
				savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
				publishProgress(count);
			}

		}

		if (!values.isEmpty()) {
			this.boxHeadRelationships.put(values);
		}

		savedValues.put(entity, count);
		publishProgress(count);

		updateSyncReport(SyncEntity.HEAD_RELATIONSHIPS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processMaritalRelationships(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.MARITAL_RELATIONSHIPS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<MaritalRelationship> values = new ArrayList<>();
		int count = 0;
		values.clear();

		parser.nextTag();

		while (notEndOfXmlDoc("maritalrelationships", parser)) {
			count++;

			MaritalRelationship table = new MaritalRelationship();

			parser.nextTag(); //memberA_code
			if (!isEmptyTag("memberA_code", parser)) {
				parser.next();
				table.memberA_code = parser.getText();
				parser.nextTag();
			}else{
				table.memberA_code = "";
				parser.nextTag();
			}

			parser.nextTag(); //memberB_code
			if (!isEmptyTag("memberB_code", parser)) {
				parser.next();
				table.memberB_code = parser.getText();
				parser.nextTag();
			}else{
				table.memberB_code = "";
				parser.nextTag();
			}

			parser.nextTag(); //startStatus
			if (!isEmptyTag("startStatus", parser)) {
				parser.next();
				table.startStatus = MaritalStartStatus.getFrom(parser.getText());
				parser.nextTag();
			}else{
				table.startStatus = null;
				parser.nextTag();
			}

			parser.nextTag(); //startDate
			if (!isEmptyTag("startDate", parser)) {
				parser.next();
				table.startDate = StringUtil.toDate(parser.getText(), "yyyy-MM-dd");
				parser.nextTag();
			}else{
				//table.startDate = false;
				parser.nextTag();
			}

			parser.nextTag(); //endStatus
			if (!isEmptyTag("endStatus", parser)) {
				parser.next();
				table.endStatus = MaritalEndStatus.getFrom(parser.getText());
				parser.nextTag();
			}else{
				table.endStatus = null;
				parser.nextTag();
			}

			parser.nextTag(); //endDate
			if (!isEmptyTag("endDate", parser)) {
				parser.next();
				table.endDate = StringUtil.toDate(parser.getText(), "yyyy-MM-dd");
				parser.nextTag();
			}else{
				//table.endDate = false;
				parser.nextTag();
			}

			parser.nextTag(); //last process tag
			parser.next();

			values.add(table);

			//database.insert(table);

			if (count % 500 == 0){
				this.boxMaritalRelationships.put(values); //try with runTx
				values.clear();
				savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
				publishProgress(count);
			}

		}

		if (!values.isEmpty()) {
			this.boxMaritalRelationships.put(values);
		}

		savedValues.put(entity, count);
		publishProgress(count);

		updateSyncReport(SyncEntity.MARITAL_RELATIONSHIPS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	private void processPregnancyRegistrations(XmlPullParser parser) throws Exception {

		//clear sync_report
		updateSyncReport(SyncEntity.PREGNANCY_REGISTRATIONS, null, SyncStatus.STATUS_NOT_SYNCED);

		List<PregnancyRegistration> values = new ArrayList<>();
		int count = 0;
		values.clear();

		parser.nextTag();

		while (notEndOfXmlDoc("pregnancyregistrations", parser)) {
			count++;

			PregnancyRegistration table = new PregnancyRegistration();

			parser.nextTag(); //code
			if (!isEmptyTag("code", parser)) {
				parser.next();
				table.code = parser.getText();
				parser.nextTag();
			}else{
				table.code = "";
				parser.nextTag();
			}

			parser.nextTag(); //motherCode
			if (!isEmptyTag("motherCode", parser)) {
				parser.next();
				table.motherCode = parser.getText();
				parser.nextTag();
			}else{
				table.motherCode = "";
				parser.nextTag();
			}

			parser.nextTag(); //recordedDate
			if (!isEmptyTag("recordedDate", parser)) {
				parser.next();
				table.recordedDate = StringUtil.toDate(parser.getText(), "yyyy-MM-dd");
				parser.nextTag();
			}else{
				//table.recordedDate = false;
				parser.nextTag();
			}

			parser.nextTag(); //pregMonths
			if (!isEmptyTag("pregMonths", parser)) {
				parser.next();
				table.pregMonths = Integer.parseInt(parser.getText());
				parser.nextTag();
			}else{
				//table.pregMonths = 0;
				parser.nextTag();
			}

			parser.nextTag(); //eddKnown
			if (!isEmptyTag("eddKnown", parser)) {
				parser.next();
				table.eddKnown = Boolean.parseBoolean(parser.getText());
				parser.nextTag();
			}else{
				//table.eddKnown = false;
				parser.nextTag();
			}

			parser.nextTag(); //hasPrenatalRecord
			if (!isEmptyTag("hasPrenatalRecord", parser)) {
				parser.next();
				table.hasPrenatalRecord = Boolean.parseBoolean(parser.getText());
				parser.nextTag();
			}else{
				//table.hasPrenatalRecord = false;
				parser.nextTag();
			}

			parser.nextTag(); //eddDate
			if (!isEmptyTag("eddDate", parser)) {
				parser.next();
				table.eddDate = StringUtil.toDate(parser.getText(), "yyyy-MM-dd");
				parser.nextTag();
			}else{
				//table.eddDate = false;
				parser.nextTag();
			}

			parser.nextTag(); //eddType
			if (!isEmptyTag("eddType", parser)) {
				parser.next();
				table.eddType = EstimatedDateOfDeliveryType.getFrom(parser.getText());
				parser.nextTag();
			}else{
				table.eddType = null;
				parser.nextTag();
			}

			parser.nextTag(); //lmpKnown
			if (!isEmptyTag("lmpKnown", parser)) {
				parser.next();
				table.lmpKnown = Boolean.parseBoolean(parser.getText());
				parser.nextTag();
			}else{
				//table.lmpKnown = false;
				parser.nextTag();
			}

			parser.nextTag(); //lmpDate
			if (!isEmptyTag("lmpDate", parser)) {
				parser.next();
				table.lmpDate = StringUtil.toDate(parser.getText(), "yyyy-MM-dd");
				parser.nextTag();
			}else{
				//table.lmpDate = false;
				parser.nextTag();
			}

			parser.nextTag(); //expectedDeliveryDate
			if (!isEmptyTag("expectedDeliveryDate", parser)) {
				parser.next();
				table.expectedDeliveryDate = StringUtil.toDate(parser.getText(), "yyyy-MM-dd");
				parser.nextTag();
			}else{
				//table.expectedDeliveryDate = false;
				parser.nextTag();
			}

			parser.nextTag(); //status
			if (!isEmptyTag("status", parser)) {
				parser.next();
				table.status = PregnancyStatus.getFrom(parser.getText());
				parser.nextTag();
			}else{
				table.status = null;
				parser.nextTag();
			}

			parser.nextTag(); //visitCode
			if (!isEmptyTag("visitCode", parser)) {
				parser.next();
				table.visitCode = parser.getText();
				parser.nextTag();
			}else{
				table.visitCode = "";
				parser.nextTag();
			}

			parser.nextTag(); //last process tag
			parser.next();

			values.add(table);

			//database.insert(table);

			if (count % 500 == 0){
				this.boxPregnancyRegistrations.put(values); //try with runTx
				values.clear();
				savedValues.put(entity, count); //publish progress is a bit slow - its not reporting well the numbers
				publishProgress(count);
			}

		}

		if (!values.isEmpty()) {
			this.boxPregnancyRegistrations.put(values);
		}

		savedValues.put(entity, count);
		publishProgress(count);

		updateSyncReport(SyncEntity.PREGNANCY_REGISTRATIONS, new Date(), SyncStatus.STATUS_SYNCED);
	}

	//database.query(SyncReport.class, DatabaseHelper.SyncReport.COLUMN_REPORT_ID+"=?", new String[]{}, null, null, null);
	private void updateSyncReport(SyncEntity syncEntity, Date date, SyncStatus status){

		SyncReport report = Queries.getSyncReportBy(boxSyncReports, syncEntity);

		if (report != null) {
			report.date	= date;
			report.status = status;
			this.boxSyncReports.put(report);

		}
	}

	private Map<SyncEntity, SyncStatus> getAllSyncReportStatus(){
		Map<SyncEntity, SyncStatus> statuses = new HashMap<>();

		this.boxSyncReports.getAll().forEach( syncReport -> {
			statuses.put(syncReport.getReportId(), syncReport.getStatus());
		});

		return statuses;
	}

	private void updateDataset(Dataset dataSet, DownloadResponse response){
		String filename = Bootstrap.getBasePath() + response.getFileName();

		dataSet.setFilename(filename);
		this.boxDatasets.put(dataSet);
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
				case ROUNDS: 		 downloadedEntity = mContext.getString(R.string.sync_rounds_lbl); 	break;
				case USERS: 		 downloadedEntity = mContext.getString(R.string.sync_users_lbl); 	break;
				case REGIONS: 		 downloadedEntity = mContext.getString(R.string.sync_regions_lbl);	break;
				case HOUSEHOLDS: 	 downloadedEntity = mContext.getString(R.string.sync_households_lbl);	break;
				case MEMBERS: 		 downloadedEntity = mContext.getString(R.string.sync_members_lbl); 	  break;
				case VISITS: 		 downloadedEntity = mContext.getString(R.string.sync_visits_lbl); 	  break;
				case HEAD_RELATIONSHIPS: 	 downloadedEntity = mContext.getString(R.string.sync_head_relationships_lbl); 	  break;
				case MARITAL_RELATIONSHIPS:  downloadedEntity = mContext.getString(R.string.sync_marital_relationships_lbl); 	  break;
				case PREGNANCY_REGISTRATIONS:downloadedEntity = mContext.getString(R.string.sync_pregnancies_lbl); 	  break;
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
				case ROUNDS: 		 downloadedEntity = mContext.getString(R.string.sync_rounds_lbl); 	break;
				case REGIONS: 		 downloadedEntity = mContext.getString(R.string.sync_regions_lbl);	break;
				case HOUSEHOLDS: 	 downloadedEntity = mContext.getString(R.string.sync_households_lbl);	break;
				case MEMBERS: 		 downloadedEntity = mContext.getString(R.string.sync_members_lbl); 	  break;
				case VISITS: 		 downloadedEntity = mContext.getString(R.string.sync_visits_lbl); 	  break;
				case HEAD_RELATIONSHIPS: 	 downloadedEntity = mContext.getString(R.string.sync_head_relationships_lbl); 	  break;
				case MARITAL_RELATIONSHIPS:  downloadedEntity = mContext.getString(R.string.sync_marital_relationships_lbl); 	  break;
				case PREGNANCY_REGISTRATIONS:downloadedEntity = mContext.getString(R.string.sync_pregnancies_lbl); 	  break;
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
