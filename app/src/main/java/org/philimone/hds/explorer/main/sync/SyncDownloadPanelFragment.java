package org.philimone.hds.explorer.main.sync;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import io.objectbox.Box;
import mz.betainteractive.utilities.DateUtil;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.io.SyncEntitiesTask;
import org.philimone.hds.explorer.io.SyncEntityResult;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.SyncReport;
import org.philimone.hds.explorer.model.enums.SyncEntity;
import org.philimone.hds.explorer.model.enums.SyncStatus;
import org.philimone.hds.explorer.widget.DialogFactory;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static org.philimone.hds.explorer.model.enums.SyncEntity.CORE_FORMS_OPTIONS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.DATASETS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.DATASETS_CSV_FILES;
import static org.philimone.hds.explorer.model.enums.SyncEntity.DEATHS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.DEMOGRAPHICS_EVENTS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.FORMS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.CORE_FORMS_EXT;
import static org.philimone.hds.explorer.model.enums.SyncEntity.HEAD_RELATIONSHIPS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.HOUSEHOLDS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.HOUSEHOLDS_DATASETS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.MARITAL_RELATIONSHIPS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.MEMBERS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.MODULES;
import static org.philimone.hds.explorer.model.enums.SyncEntity.PARAMETERS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.PREGNANCY_OUTCOMES;
import static org.philimone.hds.explorer.model.enums.SyncEntity.PREGNANCY_REGISTRATIONS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.PREGNANCY_VISITS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.HOUSEHOLD_PROXY_HEADS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.REGIONS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.REGION_HEADS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.RESIDENCIES;
import static org.philimone.hds.explorer.model.enums.SyncEntity.ROUNDS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.SETTINGS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.TRACKING_LISTS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.USERS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.VISITS;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SyncDownloadPanelFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SyncDownloadPanelFragment extends Fragment implements SyncPanelItemFragment.SyncPanelItemListener {

    private String username;
    private String password;
    private String serverUrl;
    private boolean connectedToServer;

    //fragments
    private SyncPanelItemFragment settingsSyncFragment;
    private SyncPanelItemFragment datasetsSyncFragment;
    private SyncPanelItemFragment trackingListsSyncFragment;
    private SyncPanelItemFragment householdsDatasetsSyncFragment;
    private SyncPanelItemFragment demographicsEventsSyncFragment;

    private SyncPanelItemFragment clickedSyncFragment;

    private Button btSyncAllData;

    private List<Synchronizer> synchronizerAllList = new ArrayList<>();
    private List<SyncPanelItemFragment> syncPanelItemFragments;

    private Box<SyncReport> boxSyncReports;
    private Box<CoreCollectedData> boxCoreCollectedData;

    private DateUtil dateUtil = Bootstrap.getDateUtil();

    private SyncPanelActivity.SyncFragmentListener syncFragmentListener;

    public SyncDownloadPanelFragment() {
        // Required empty public constructor
        initBoxes();

        this.syncPanelItemFragments = new ArrayList<>();
    }

    public static SyncDownloadPanelFragment newInstance(String username, String password, String serverUrl, boolean connectedToServer, SyncPanelActivity.SyncFragmentListener fragmentListener) {
        SyncDownloadPanelFragment fragment = new SyncDownloadPanelFragment();
        fragment.username = username;
        fragment.password = password;
        fragment.serverUrl = serverUrl;
        fragment.connectedToServer = connectedToServer;
        fragment.syncFragmentListener = fragmentListener;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean hasDataToUpload = hasDataToUpload();

        this.settingsSyncFragment = SyncPanelItemFragment.newInstance(getString(R.string.server_sync_bt_settings_lbl), connectedToServer, hasDataToUpload);
        this.datasetsSyncFragment = SyncPanelItemFragment.newInstance(getString(R.string.server_sync_bt_datasets_lbl), connectedToServer, hasDataToUpload);
        this.trackingListsSyncFragment = SyncPanelItemFragment.newInstance(getString(R.string.server_sync_bt_tracking_lists_lbl), connectedToServer, hasDataToUpload);
        this.householdsDatasetsSyncFragment = SyncPanelItemFragment.newInstance(getString(R.string.server_sync_bt_households_datasets_lbl), connectedToServer, hasDataToUpload);
        this.demographicsEventsSyncFragment = SyncPanelItemFragment.newInstance(getString(R.string.server_sync_bt_dssevents_lbl), connectedToServer, hasDataToUpload);

        this.settingsSyncFragment.setListener(this);
        this.datasetsSyncFragment.setListener(this);
        this.trackingListsSyncFragment.setListener(this);
        this.householdsDatasetsSyncFragment.setListener(this);
        this.demographicsEventsSyncFragment.setListener(this);

        syncPanelItemFragments.add(this.settingsSyncFragment);
        syncPanelItemFragments.add(this.datasetsSyncFragment);
        syncPanelItemFragments.add(this.trackingListsSyncFragment);
        syncPanelItemFragments.add(this.householdsDatasetsSyncFragment);
        syncPanelItemFragments.add(this.demographicsEventsSyncFragment);

    }

    @Override
    public void onResume() {
        super.onResume();

        showStatus();
        readPreferences();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.sync_download_panel, container, false);

        getChildFragmentManager().beginTransaction().replace(R.id.settingsSyncFragment, this.settingsSyncFragment).commit();

        getChildFragmentManager().beginTransaction().replace(R.id.datasetsSyncFragment, this.datasetsSyncFragment).commit();

        getChildFragmentManager().beginTransaction().replace(R.id.trackingListsSyncFragment, this.trackingListsSyncFragment).commit();

        getChildFragmentManager().beginTransaction().replace(R.id.householdsDatasetsSyncFragment, this.householdsDatasetsSyncFragment).commit();

        getChildFragmentManager().beginTransaction().replace(R.id.demographicsEventsSyncFragment, this.demographicsEventsSyncFragment).commit();

        initialize(view);

        return view;
    }

    private void initBoxes(){
        this.boxSyncReports = ObjectBoxDatabase.get().boxFor(SyncReport.class);
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
    }

    private void initialize(View view) {

        //this.username = (String) getIntent().getExtras().get("username");
        //this.password = (String) getIntent().getExtras().get("password");
        //this.serverUrl = (String) getIntent().getExtras().get("server-url");

        btSyncAllData = view.findViewById(R.id.btSyncAllData);

        btSyncAllData.setOnClickListener(v -> onSyncAllData());

        this.clickedSyncFragment = null;

        Log.d("init", "finishes "+this.datasetsSyncFragment);
    }

    private String getStatusMessage(SyncReport report){
        String statusMessage = getString(R.string.server_sync_status_notsynced_lbl);

        if (report.getStatus()== SyncStatus.STATUS_SYNCED){
            statusMessage = getString(R.string.server_sync_status_synced_lbl) + " " + dateUtil.formatYMDHMS(report.getDate()); //, "yyyy-MM-dd HH:mm:ss");
        }

        if (report.getStatus()==SyncStatus.STATUS_SYNC_ERROR){
            statusMessage = getString(R.string.server_sync_status_sync_error_lbl) + " " + dateUtil.formatYMDHMS(report.getDate()); //, "yyyy-MM-dd HH:mm:ss");
        }

        return statusMessage;
    }

    private SyncReport getBestReport(SyncReport... reports) {
        SyncReport report = reports[0];

        for (SyncReport r : reports){
            if (r.getStatus()==SyncStatus.STATUS_SYNC_ERROR){
                report = r;
                continue;
            }
            if (report.getStatus() != SyncStatus.STATUS_SYNC_ERROR){
                report = r;
            }
        }

        return report;
    }

    public void showStatus() {

        SyncReport modules = Queries.getSyncReportBy(boxSyncReports, MODULES);
        SyncReport forms = Queries.getSyncReportBy(boxSyncReports, FORMS);
        SyncReport coreforms = Queries.getSyncReportBy(boxSyncReports, CORE_FORMS_EXT);
        SyncReport coreformsopts = Queries.getSyncReportBy(boxSyncReports, CORE_FORMS_OPTIONS);
        SyncReport params = Queries.getSyncReportBy(boxSyncReports, PARAMETERS);
        SyncReport datasetCsv = Queries.getSyncReportBy(boxSyncReports, DATASETS_CSV_FILES);
        SyncReport datasets = Queries.getSyncReportBy(boxSyncReports, DATASETS);
        SyncReport trackLists = Queries.getSyncReportBy(boxSyncReports, TRACKING_LISTS);
        SyncReport users = Queries.getSyncReportBy(boxSyncReports, USERS);
        SyncReport regions = Queries.getSyncReportBy(boxSyncReports, REGIONS);
        SyncReport households = Queries.getSyncReportBy(boxSyncReports, HOUSEHOLDS);
        SyncReport members = Queries.getSyncReportBy(boxSyncReports, MEMBERS);
        SyncReport residencies = Queries.getSyncReportBy(boxSyncReports, RESIDENCIES);
        SyncReport rounds = Queries.getSyncReportBy(boxSyncReports, ROUNDS);
        SyncReport visits = Queries.getSyncReportBy(boxSyncReports, VISITS);
        SyncReport hrelationships = Queries.getSyncReportBy(boxSyncReports, HEAD_RELATIONSHIPS);
        SyncReport mrelationships = Queries.getSyncReportBy(boxSyncReports, MARITAL_RELATIONSHIPS);
        SyncReport pregnancies = Queries.getSyncReportBy(boxSyncReports, PREGNANCY_REGISTRATIONS);
        SyncReport pregnancyoutcomes = Queries.getSyncReportBy(boxSyncReports, PREGNANCY_OUTCOMES);
        SyncReport pregnancyvisits = Queries.getSyncReportBy(boxSyncReports, PREGNANCY_VISITS);
        SyncReport deaths = Queries.getSyncReportBy(boxSyncReports, DEATHS);
        SyncReport regionheads = Queries.getSyncReportBy(boxSyncReports, REGION_HEADS);
        SyncReport proxyheads = Queries.getSyncReportBy(boxSyncReports, HOUSEHOLD_PROXY_HEADS);
        //setting general status - if one block is bad general is bad

        //settings
        if (modules != null || forms != null || coreforms != null || coreformsopts != null || params != null || users != null){
            SyncReport report = getBestReport(modules, forms, params, users);
            settingsSyncFragment.setSyncedDate(getStatusMessage(report), report.getStatus());
        }

        //datasets
        if (datasetCsv != null || datasets != null){
            SyncReport report = getBestReport(datasetCsv, datasets);
            datasetsSyncFragment.setSyncedDate(getStatusMessage(report), report.getStatus());
        }

        //tracklists
        if (trackLists != null) {
            trackingListsSyncFragment.setSyncedDate(getStatusMessage(trackLists), trackLists.getStatus());
        }

        //households datasets
        if (rounds != null || regions != null || households != null || members != null || residencies != null || regionheads != null || proxyheads != null){
            SyncReport report = getBestReport(rounds, regions, households, members, residencies, regionheads, proxyheads);
            householdsDatasetsSyncFragment.setSyncedDate(getStatusMessage(report), report.getStatus());
        }

        //demographics events
        if (visits != null || hrelationships != null || mrelationships != null || pregnancies != null || pregnancyoutcomes != null || pregnancyvisits != null || deaths != null) {
            SyncReport report = getBestReport(visits, hrelationships, mrelationships, pregnancies, pregnancyoutcomes, pregnancyvisits);
            demographicsEventsSyncFragment.setSyncedDate(getStatusMessage(report), report.getStatus());
        }

    }

    public void updateAllPanelItems() {
        boolean hasDataToUpload = hasDataToUpload();

        for (SyncPanelItemFragment frag : this.syncPanelItemFragments) {
            frag.refreshSyncButton();
            frag.setHasDataToUpload(hasDataToUpload);
        }
    }

    private void onSyncAllData(){
        //Synchronize One by One

        if (!connectedToServer) { //logged in locally only
            DialogFactory.createMessageInfo(this.getContext(), R.string.server_sync_offline_mode_title_lbl, R.string.server_sync_offline_mode_msg_lbl).show();
            return;
        }

        //check if there is data to upload? - ask: do you want to proceed
        if (hasDataToUpload()){
            DialogFactory.createMessageInfo(this.getContext(), R.string.server_sync_warning_upload_required_lbl, R.string.server_sync_warning_upload_required_msg_lbl, new DialogFactory.OnClickListener() {
                @Override
                public void onClicked(DialogFactory.Buttons clickedButton) {

                }
            }).show();
        } else {
            executeSyncAllData();
        }
    }

    private void executeSyncAllData(){
        this.synchronizerAllList.clear();
        this.clickedSyncFragment = null;

        syncAllData();
    }

    private boolean hasDataToUpload(){
        return this.boxCoreCollectedData.query(CoreCollectedData_.uploaded.equal(false)).build().count() > 0;
    }

    private void syncAllData() {
        Synchronizer settings = () -> syncSettings();
        Synchronizer datasets = () -> syncDatasets();
        Synchronizer tracklists = () -> syncTrackingLists();
        Synchronizer households = () -> syncHouseholdDatasets();
        Synchronizer dssevents = () -> syncDemographicsEvents();

        this.synchronizerAllList.add(settings);
        this.synchronizerAllList.add(datasets);
        this.synchronizerAllList.add(tracklists);
        this.synchronizerAllList.add(households);
        this.synchronizerAllList.add(dssevents);

        settings.executeSync();
    }

    private void syncSettings() {
        settingsSyncFragment.cleanProgress();
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this.getContext(), this.settingsSyncFragment, serverUrl, username, password, PARAMETERS, MODULES, FORMS, CORE_FORMS_EXT, CORE_FORMS_OPTIONS, USERS);
        syncEntitiesTask.execute();
    }

    private void syncDatasets() {
        datasetsSyncFragment.cleanProgress();
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this.getContext(), this.datasetsSyncFragment, serverUrl, username, password, DATASETS, DATASETS_CSV_FILES);
        syncEntitiesTask.execute();
    }

    private void syncTrackingLists() {
        trackingListsSyncFragment.cleanProgress();
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this.getContext(), this.trackingListsSyncFragment, serverUrl, username, password, TRACKING_LISTS);
        syncEntitiesTask.execute();
    }

    private void syncHouseholdDatasets() {
        householdsDatasetsSyncFragment.cleanProgress();
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this.getContext(), this.householdsDatasetsSyncFragment, serverUrl, username, password, ROUNDS, REGIONS, HOUSEHOLDS, MEMBERS, RESIDENCIES, REGION_HEADS, HOUSEHOLD_PROXY_HEADS);
        syncEntitiesTask.execute();
    }

    private void syncDemographicsEvents() {
        demographicsEventsSyncFragment.cleanProgress();
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this.getContext(), this.demographicsEventsSyncFragment, serverUrl, username, password, VISITS, HEAD_RELATIONSHIPS, MARITAL_RELATIONSHIPS, PREGNANCY_REGISTRATIONS, PREGNANCY_OUTCOMES, PREGNANCY_VISITS, DEATHS);
        syncEntitiesTask.execute();
    }

    private void savePreferences(SyncEntityResult syncEntityResult) {
        SyncEntity entity = syncEntityResult.getMainEntity();
        String jsonEntityResult = new Gson().toJson(syncEntityResult);

        SharedPreferences prefs = getActivity().getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        //save json to preferences
        editor.putString(entity.name(), jsonEntityResult);
        editor.commit();
    }

    public void readPreferences(){

        SharedPreferences prefs = getActivity().getPreferences(MODE_PRIVATE);

        SyncEntityResult settingsResult = getEntityResult(prefs, SETTINGS);
        SyncEntityResult datasetsResult = getEntityResult(prefs, DATASETS);
        SyncEntityResult trackListResult = getEntityResult(prefs, TRACKING_LISTS);
        SyncEntityResult householdsResult = getEntityResult(prefs, HOUSEHOLDS_DATASETS);
        SyncEntityResult dssEventsResult = getEntityResult(prefs, DEMOGRAPHICS_EVENTS);

        if (settingsResult != null) {
            settingsSyncFragment.setSyncResult(settingsResult);
        }
        if (datasetsResult != null) {
            datasetsSyncFragment.setSyncResult(datasetsResult);
        }
        if (trackListResult != null) {
            trackingListsSyncFragment.setSyncResult(trackListResult);
        }
        if (householdsResult != null) {
            householdsDatasetsSyncFragment.setSyncResult(householdsResult);
        }
        if (dssEventsResult != null) {
            demographicsEventsSyncFragment.setSyncResult(dssEventsResult);
        }
    }

    private SyncEntityResult getEntityResult(SharedPreferences prefs, SyncEntity entity){
        SyncEntityResult result = null;

        try {
            String json = prefs.getString(entity.name(), "");
            result = new Gson().fromJson(json, SyncEntityResult.class);
        } catch (Exception  ex){
            ex.printStackTrace();
        }


        return result;
    }

    private void executeSyncStartButton(SyncPanelItemFragment syncPanelItem){
        if (syncPanelItem.equals(this.settingsSyncFragment)){
            syncSettings();
        }

        if (syncPanelItem.equals(this.datasetsSyncFragment)){
            syncDatasets();
        }

        if (syncPanelItem.equals(this.trackingListsSyncFragment)){
            syncTrackingLists();
        }

        if (syncPanelItem.equals(this.householdsDatasetsSyncFragment)){
            if (hasDataToUpload()){
                DialogFactory.createMessageInfo(this.getContext(), R.string.server_sync_warning_upload_required_lbl, R.string.server_sync_warning_upload_required_msg_lbl, clickedButton -> { }).show();
            } else {
                syncHouseholdDatasets();
            }
        }

        if (syncPanelItem.equals(this.demographicsEventsSyncFragment)){
            if (hasDataToUpload()){
                DialogFactory.createMessageInfo(this.getContext(), R.string.server_sync_warning_upload_required_lbl, R.string.server_sync_warning_upload_required_msg_lbl, clickedButton -> { }).show();
            } else {
                syncDemographicsEvents();
            }
        }

        this.clickedSyncFragment = null;
    }

    @Override
    public void onSyncStartButtonClicked(SyncPanelItemFragment syncPanelItem) {
        Log.d("sync", "sync start");

        this.clickedSyncFragment = syncPanelItem;

        if (!connectedToServer) { //logged in locally only
            DialogFactory.createMessageInfo(this.getContext(), R.string.server_sync_offline_mode_title_lbl, R.string.server_sync_offline_mode_msg_lbl, clickedButton -> syncPanelItem.resetSyncButton()).show();
            return;
        }

        executeSyncStartButton(syncPanelItem);

    }

    @Override
    public void onSyncStopButtonClicked(SyncPanelItemFragment syncPanelItem) {
        //Stopping

        synchronizerAllList.clear();
    }

    @Override
    public void onSyncFinished(SyncPanelItemFragment syncPanelItem, SyncEntityResult syncEntityResult) {

        //Sync All - Continue
        if (synchronizerAllList.size() > 0){ //As Long as our list as a next synchronizer we execute next
            synchronizerAllList.remove(0); //remove first

            if (synchronizerAllList.size() > 0){
                synchronizerAllList.get(0).executeSync(); //execute the next item
            }
        }

        showStatus();
        updateAllPanelItems();
        savePreferences(syncEntityResult);
    }

    interface Synchronizer {
        void executeSync();
    }
}