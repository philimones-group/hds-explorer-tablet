package org.philimone.hds.explorer.main.sync;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Database;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.io.SyncEntitiesTask;
import org.philimone.hds.explorer.io.SyncEntity;
import org.philimone.hds.explorer.model.SyncReport;
import org.philimone.hds.explorer.widget.SyncProgressDialog;

import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.utilities.StringUtil;

public class SyncPanelActivity extends AppCompatActivity implements SyncPanelItemFragment.SyncPanelItemListener {

    private SyncProgressDialog progressDialog;

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

    private Button btSyncAllData;

    private List<Synchronizer> synchronizerAllList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.synchronization_panel);

        this.settingsSyncFragment = SyncPanelItemFragment.newInstance(getString(R.string.server_sync_bt_settings_lbl));
        this.datasetsSyncFragment = SyncPanelItemFragment.newInstance(getString(R.string.server_sync_bt_datasets_lbl));
        this.trackingListsSyncFragment = SyncPanelItemFragment.newInstance(getString(R.string.server_sync_bt_tracking_lists_lbl));
        this.usersSyncFragment = SyncPanelItemFragment.newInstance(getString(R.string.server_sync_bt_users_lbl));
        this.householdsSyncFragment = SyncPanelItemFragment.newInstance(getString(R.string.server_sync_bt_households_lbl));
        this.membersSyncFragment = SyncPanelItemFragment.newInstance(getString(R.string.server_sync_bt_members_lbl));


        getSupportFragmentManager().beginTransaction().replace(R.id.settingsSyncFragment, this.settingsSyncFragment).commit();

        getSupportFragmentManager().beginTransaction().replace(R.id.datasetsSyncFragment, this.datasetsSyncFragment).commit();

        getSupportFragmentManager().beginTransaction().replace(R.id.trackingListsSyncFragment, this.trackingListsSyncFragment).commit();

        getSupportFragmentManager().beginTransaction().replace(R.id.usersSyncFragment, this.usersSyncFragment).commit();

        getSupportFragmentManager().beginTransaction().replace(R.id.householdsSyncFragment, this.householdsSyncFragment).commit();

        getSupportFragmentManager().beginTransaction().replace(R.id.membersSyncFragment, this.membersSyncFragment).commit();

        initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();

        showStatus();
    }

    private void initialize() {
        this.username = (String) getIntent().getExtras().get("username");
        this.password = (String) getIntent().getExtras().get("password");
        this.serverUrl = (String) getIntent().getExtras().get("server-url");

        btSyncAllData = (Button) this.findViewById(R.id.btSyncAllData);

        btSyncAllData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncAllData();
            }
        });

        Log.d("init", "finishes "+this.datasetsSyncFragment);
    }

    private void showStatus() {
        Database db = new Database(this);

        db.open();
        List<SyncReport> reports = Queries.getAllSyncReportBy(db, null, null);
        db.close();

        for (SyncReport report : reports){
            String status = getString(R.string.server_sync_status_notsynced_lbl);

            if (report.getStatus()==SyncReport.STATUS_SYNCED){
                status = getString(R.string.server_sync_status_synced_lbl) + " " + StringUtil.format(report.getDate(), "yyyy-MM-dd HH:mm:ss");
            }

            if (report.getStatus()==SyncReport.STATUS_SYNC_ERROR){
                status = getString(R.string.server_sync_status_sync_error_lbl) + " " + StringUtil.format(report.getDate(), "yyyy-MM-dd HH:mm:ss");
            }

            switch (report.getReportId()){
                case SyncReport.REPORT_MODULES:
                case SyncReport.REPORT_FORMS:
                case SyncReport.REPORT_PARAMETERS: settingsSyncFragment.setSyncedDate(status); break;
                case SyncReport.REPORT_DATASETS: datasetsSyncFragment.setSyncedDate(status); break;
                case SyncReport.REPORT_TRACKING_LISTS: trackingListsSyncFragment.setSyncedDate(status);  break;
                case SyncReport.REPORT_USERS: usersSyncFragment.setSyncedDate(status);  break;
                case SyncReport.REPORT_HOUSEHOLDS: householdsSyncFragment.setSyncedDate(status);  break;
                case SyncReport.REPORT_MEMBERS: membersSyncFragment.setSyncedDate(status);  break;
            }
        }


    }

    private void syncAllData(){
        //Synchronize One by One

        this.synchronizerAllList.clear();

        Synchronizer settings = new Synchronizer(){ public void executeSync() {  syncSettings();  } };
        Synchronizer datasets = new Synchronizer(){ public void executeSync() {  syncDatasets();  } };
        Synchronizer tracklists = new Synchronizer(){ public void executeSync() {  syncTrackingLists();  } };
        Synchronizer users = new Synchronizer(){ public void executeSync() {  syncUsers();  } };
        Synchronizer households = new Synchronizer(){ public void executeSync() {  syncHouseholds();  } };
        Synchronizer members = new Synchronizer(){ public void executeSync() {  syncMembers();  } };

        this.synchronizerAllList.add(settings);
        this.synchronizerAllList.add(datasets);
        this.synchronizerAllList.add(tracklists);
        this.synchronizerAllList.add(users);
        this.synchronizerAllList.add(households);
        this.synchronizerAllList.add(members);

        settings.executeSync();
    }

    private void syncSettings() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this, this.settingsSyncFragment, serverUrl, username, password, SyncEntity.MODULES, SyncEntity.PARAMETERS, SyncEntity.FORMS);
        syncEntitiesTask.execute();
    }

    private void syncDatasets() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this, this.datasetsSyncFragment, serverUrl, username, password, SyncEntity.DATASETS, SyncEntity.DATASETS_CSV_FILES);
        syncEntitiesTask.execute();
    }

    private void syncTrackingLists() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this, this.trackingListsSyncFragment, serverUrl, username, password, SyncEntity.TRACKING_LISTS);
        syncEntitiesTask.execute();
    }

    private void syncUsers() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this, this.usersSyncFragment, serverUrl, username, password, SyncEntity.USERS);
        syncEntitiesTask.execute();
    }

    private void syncHouseholds() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this, this.householdsSyncFragment, serverUrl, username, password, SyncEntity.REGIONS, SyncEntity.HOUSEHOLDS);
        syncEntitiesTask.execute();
    }

    private void syncMembers() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this, this.membersSyncFragment, serverUrl, username, password, SyncEntity.MEMBERS);
        syncEntitiesTask.execute();
    }

    @Override
    public void onSyncStartButtonClicked(SyncPanelItemFragment syncPanelItem) {
        Log.d("sync", "sync start");


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

    }

    @Override
    public void onSyncStopButtonClicked(SyncPanelItemFragment syncPanelItem) {
        //Stopping

        synchronizerAllList.clear();
    }

    @Override
    public void onSyncFinished(SyncPanelItemFragment syncPanelItem) {

        //Sync All - Continue
        if (synchronizerAllList.size() > 0){ //As Long as our list as a next synchronizer we execute next
            synchronizerAllList.remove(0); //remove first

            if (synchronizerAllList.size() > 0){
                synchronizerAllList.get(0).executeSync(); //execute the next item
            }
        }

        showStatus();
    }

    interface Synchronizer {
        public void executeSync();
    }

}