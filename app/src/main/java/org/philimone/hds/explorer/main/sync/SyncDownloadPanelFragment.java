package org.philimone.hds.explorer.main.sync;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import io.objectbox.Box;
import mz.betainteractive.utilities.StringUtil;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.io.SyncEntitiesTask;
import org.philimone.hds.explorer.io.SyncEntityResult;
import org.philimone.hds.explorer.model.SyncReport;
import org.philimone.hds.explorer.model.enums.SyncEntity;
import org.philimone.hds.explorer.model.enums.SyncStatus;
import org.philimone.hds.explorer.widget.DialogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static org.philimone.hds.explorer.model.enums.SyncEntity.DATASETS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.DATASETS_CSV_FILES;
import static org.philimone.hds.explorer.model.enums.SyncEntity.FORMS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.HOUSEHOLDS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.MEMBERS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.MODULES;
import static org.philimone.hds.explorer.model.enums.SyncEntity.PARAMETERS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.REGIONS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.SETTINGS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.TRACKING_LISTS;
import static org.philimone.hds.explorer.model.enums.SyncEntity.USERS;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SyncDownloadPanelFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SyncDownloadPanelFragment extends Fragment implements SyncPanelItemFragment.SyncPanelItemListener {

    private String username;
    private String password;
    private String serverUrl;

    //fragments
    private SyncPanelItemFragment settingsSyncFragment;
    private SyncPanelItemFragment datasetsSyncFragment;
    private SyncPanelItemFragment trackingListsSyncFragment;
    private SyncPanelItemFragment usersSyncFragment;
    private SyncPanelItemFragment householdsSyncFragment;
    private SyncPanelItemFragment membersSyncFragment;

    private SyncPanelItemFragment clickedSyncFragment;

    private Button btSyncAllData;

    private List<Synchronizer> synchronizerAllList = new ArrayList<>();

    private Box<SyncReport> boxSyncReports;

    private ActivityResultLauncher<String[]> requestPermissions;

    public SyncDownloadPanelFragment() {
        // Required empty public constructor
        initBoxes();
    }

    public static SyncDownloadPanelFragment newInstance(String username, String password, String serverUrl) {
        SyncDownloadPanelFragment fragment = new SyncDownloadPanelFragment();
        fragment.username = username;
        fragment.password = password;
        fragment.serverUrl = serverUrl;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.settingsSyncFragment = SyncPanelItemFragment.newInstance(getString(R.string.server_sync_bt_settings_lbl));
        this.datasetsSyncFragment = SyncPanelItemFragment.newInstance(getString(R.string.server_sync_bt_datasets_lbl));
        this.trackingListsSyncFragment = SyncPanelItemFragment.newInstance(getString(R.string.server_sync_bt_tracking_lists_lbl));
        this.usersSyncFragment = SyncPanelItemFragment.newInstance(getString(R.string.server_sync_bt_users_lbl));
        this.householdsSyncFragment = SyncPanelItemFragment.newInstance(getString(R.string.server_sync_bt_households_lbl));
        this.membersSyncFragment = SyncPanelItemFragment.newInstance(getString(R.string.server_sync_bt_members_lbl));

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

        getChildFragmentManager().beginTransaction().replace(R.id.usersSyncFragment, this.usersSyncFragment).commit();

        getChildFragmentManager().beginTransaction().replace(R.id.householdsSyncFragment, this.householdsSyncFragment).commit();

        getChildFragmentManager().beginTransaction().replace(R.id.membersSyncFragment, this.membersSyncFragment).commit();

        initPermissions();
        initialize(view);

        return view;
    }

    private void initBoxes(){
        this.boxSyncReports = ObjectBoxDatabase.get().boxFor(SyncReport.class);
    }

    private void initPermissions() {
        this.requestPermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionResults -> {
            boolean granted = !permissionResults.values().contains(false);

            if (granted) {
                if (this.clickedSyncFragment == null) {//sync all
                    syncAllData();
                } else {
                    executeSyncStartButton(this.clickedSyncFragment);
                }
            } else {
                //message info
                DialogFactory.createMessageInfo(this.getContext(), R.string.permissions_sync_storage_title_lbl, R.string.permissions_sync_storage_denied_lbl).show();
            }
        });
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
            statusMessage = getString(R.string.server_sync_status_synced_lbl) + " " + StringUtil.format(report.getDate(), "yyyy-MM-dd HH:mm:ss");
        }

        if (report.getStatus()==SyncStatus.STATUS_SYNC_ERROR){
            statusMessage = getString(R.string.server_sync_status_sync_error_lbl) + " " + StringUtil.format(report.getDate(), "yyyy-MM-dd HH:mm:ss");
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
        SyncReport params = Queries.getSyncReportBy(boxSyncReports, PARAMETERS);
        SyncReport datasetCsv = Queries.getSyncReportBy(boxSyncReports, DATASETS_CSV_FILES);
        SyncReport datasets = Queries.getSyncReportBy(boxSyncReports, DATASETS);
        SyncReport trackLists = Queries.getSyncReportBy(boxSyncReports, TRACKING_LISTS);
        SyncReport users = Queries.getSyncReportBy(boxSyncReports, USERS);
        SyncReport regions = Queries.getSyncReportBy(boxSyncReports, REGIONS);
        SyncReport households = Queries.getSyncReportBy(boxSyncReports, HOUSEHOLDS);
        SyncReport members = Queries.getSyncReportBy(boxSyncReports, MEMBERS);

        //setting general status - if one block is bad general is bad

        //settings
        if (modules != null || forms != null || params != null){
            SyncReport report = getBestReport(modules, forms, params);
            settingsSyncFragment.setSyncedDate(getStatusMessage(report), report.getStatus());
        }

        //datasets
        if (datasetCsv != null || datasets != null){
            SyncReport report = getBestReport(datasetCsv, datasets);
            datasetsSyncFragment.setSyncedDate(getStatusMessage(report), report.getStatus());
        }

        if (trackLists != null) {
            trackingListsSyncFragment.setSyncedDate(getStatusMessage(trackLists), trackLists.getStatus());
        }

        //households
        if (regions != null || households != null){
            SyncReport report = getBestReport(regions, households);
            householdsSyncFragment.setSyncedDate(getStatusMessage(report), report.getStatus());
        }

        if (users != null) {
            usersSyncFragment.setSyncedDate(getStatusMessage(users), users.getStatus());
        }

        if (members != null) {
            membersSyncFragment.setSyncedDate(getStatusMessage(members), members.getStatus());
        }

    }

    private void onSyncAllData(){
        //Synchronize One by One

        this.synchronizerAllList.clear();
        this.clickedSyncFragment = null;

        if (isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            syncAllData();
        }
    }

    private void syncAllData() {
        Synchronizer settings = () -> syncSettings();
        Synchronizer datasets = () -> syncDatasets();
        Synchronizer tracklists = () -> syncTrackingLists();
        Synchronizer users = () -> syncUsers();
        Synchronizer households = () -> syncHouseholds();
        Synchronizer members = () -> syncMembers();

        this.synchronizerAllList.add(settings);
        this.synchronizerAllList.add(datasets);
        this.synchronizerAllList.add(tracklists);
        this.synchronizerAllList.add(users);
        this.synchronizerAllList.add(households);
        this.synchronizerAllList.add(members);

        settings.executeSync();
    }

    private void syncSettings() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this.getContext(), this.settingsSyncFragment, serverUrl, username, password, MODULES, PARAMETERS, FORMS);
        syncEntitiesTask.execute();
    }

    private void syncDatasets() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this.getContext(), this.datasetsSyncFragment, serverUrl, username, password, DATASETS, DATASETS_CSV_FILES);
        syncEntitiesTask.execute();
    }

    private void syncTrackingLists() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this.getContext(), this.trackingListsSyncFragment, serverUrl, username, password, TRACKING_LISTS);
        syncEntitiesTask.execute();
    }

    private void syncUsers() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this.getContext(), this.usersSyncFragment, serverUrl, username, password, USERS);
        syncEntitiesTask.execute();
    }

    private void syncHouseholds() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this.getContext(), this.householdsSyncFragment, serverUrl, username, password, REGIONS, HOUSEHOLDS);
        syncEntitiesTask.execute();
    }

    private void syncMembers() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this.getContext(), this.membersSyncFragment, serverUrl, username, password, MEMBERS);
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
        SyncEntityResult usersResult = getEntityResult(prefs, USERS);
        SyncEntityResult householdsResult = getEntityResult(prefs, HOUSEHOLDS);
        SyncEntityResult membersResult = getEntityResult(prefs, MEMBERS);

        if (settingsResult != null) {
            settingsSyncFragment.setSyncResult(settingsResult);
        }
        if (datasetsResult != null) {
            datasetsSyncFragment.setSyncResult(datasetsResult);
        }
        if (trackListResult != null) {
            trackingListsSyncFragment.setSyncResult(trackListResult);
        }
        if (usersResult != null) {
            usersSyncFragment.setSyncResult(usersResult);
        }
        if (householdsResult != null) {
            householdsSyncFragment.setSyncResult(householdsResult);
        }
        if (membersResult != null) {
            membersSyncFragment.setSyncResult(membersResult);
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

    private boolean isPermissionGranted(final String... permissions) {

        boolean denied = Arrays.stream(permissions).anyMatch(permission -> ContextCompat.checkSelfPermission(this.getContext(), permission) == PackageManager.PERMISSION_DENIED);

        if (denied) { //without access
            //request permissions
            this.requestPermissions.launch(permissions);
        }

        return !denied;
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

        if (syncPanelItem.equals(this.usersSyncFragment)){
            syncUsers();
        }

        if (syncPanelItem.equals(this.householdsSyncFragment)){
            syncHouseholds();
        }

        if (syncPanelItem.equals(this.membersSyncFragment)){
            syncMembers();
        }

        this.clickedSyncFragment = null;
    }

    @Override
    public void onSyncStartButtonClicked(SyncPanelItemFragment syncPanelItem) {
        Log.d("sync", "sync start");

        this.clickedSyncFragment = syncPanelItem;

        if (isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            executeSyncStartButton(syncPanelItem);
        }
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
        savePreferences(syncEntityResult);
    }

    interface Synchronizer {
        void executeSync();
    }
}