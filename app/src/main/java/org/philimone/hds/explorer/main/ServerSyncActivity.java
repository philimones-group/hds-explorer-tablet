package org.philimone.hds.explorer.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Database;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.io.SyncEntitiesListener;
import org.philimone.hds.explorer.io.SyncEntitiesTask;
import org.philimone.hds.explorer.io.SyncEntity;
import org.philimone.hds.explorer.io.SyncEntityReport;
import org.philimone.hds.explorer.model.SyncReport;
import org.philimone.hds.explorer.widget.SyncProgressDialog;

import java.util.List;

import mz.betainteractive.utilities.StringUtil;

public class ServerSyncActivity extends Activity implements SyncEntitiesListener {

    private Button btSyncModules;
    private Button btSyncForms;
    private Button btSyncTrackingLists;
    private Button btSyncUsers;
    private Button btSyncHouseholds;
    private Button btSyncMembers;

    private TextView txtSyncModulesStatus;
    private TextView txtSyncFormsStatus;
    private TextView txtSyncTrackingListsStatus;
    private TextView txtSyncUsersStatus;
    private TextView txtSyncHouseholdsStatus;
    private TextView txtSyncMembersStatus;

    private SyncProgressDialog progressDialog;


    private String username;
    private String password;

    private String serverUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_sync);

        initialize();
        showStatus();
    }

    private void showStatus() {
        Database db = new Database(this);

        db.open();

        List<SyncReport> reports = Queries.getAllSyncReportBy(db, null, null);

        for (SyncReport report : reports){
            String status = getString(R.string.server_sync_status_notsynced_lbl);

            if (report.getStatus()==SyncReport.STATUS_SYNCED){
                status = getString(R.string.server_sync_status_synced_lbl) + " " + StringUtil.format(report.getDate(), "yyyy-MM-dd HH:mm:ss");
            }

            if (report.getStatus()==SyncReport.STATUS_SYNC_ERROR){
                status = getString(R.string.server_sync_status_sync_error_lbl) + " " + StringUtil.format(report.getDate(), "yyyy-MM-dd HH:mm:ss");
            }

            switch (report.getReportId()){
                case SyncReport.REPORT_MODULES: txtSyncModulesStatus.setText(status+"");  break;
                case SyncReport.REPORT_FORMS: txtSyncFormsStatus.setText(status+"");  break;
                case SyncReport.REPORT_TRACKING_LISTS: txtSyncTrackingListsStatus.setText(status+"");  break;
                case SyncReport.REPORT_USERS: txtSyncUsersStatus.setText(status+"");  break;
                case SyncReport.REPORT_HOUSEHOLDS: txtSyncHouseholdsStatus.setText(status+"");  break;
                case SyncReport.REPORT_MEMBERS: txtSyncMembersStatus.setText(status+"");  break;
            }
        }

        db.close();
    }

    private void initialize() {
        this.btSyncModules = (Button) findViewById(R.id.btSyncSettings);
        this.btSyncForms = (Button) findViewById(R.id.btSyncDatasets);
        this.btSyncTrackingLists = (Button) findViewById(R.id.btSyncTrackingLists);
        this.btSyncUsers = (Button) findViewById(R.id.btSyncUsers);
        this.btSyncHouseholds = (Button) findViewById(R.id.btSyncHouseholds);
        this.btSyncMembers = (Button) findViewById(R.id.btSyncMembers);

        this.txtSyncModulesStatus = (TextView) findViewById(R.id.txtSyncModulesStatus);
        this.txtSyncFormsStatus = (TextView) findViewById(R.id.txtSyncFormsStatus);
        this.txtSyncTrackingListsStatus = (TextView) findViewById(R.id.txtSyncTrackingListsStatus);
        this.txtSyncUsersStatus = (TextView) findViewById(R.id.txtSyncUsersStatus);
        this.txtSyncHouseholdsStatus = (TextView) findViewById(R.id.txtSyncHouseholdsStatus);
        this.txtSyncMembersStatus = (TextView) findViewById(R.id.txtSyncMembersStatus);

        this.progressDialog = new SyncProgressDialog(this);

        this.btSyncModules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncSettings();
            }
        });

        this.btSyncForms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncDatasets();
            }
        });

        this.btSyncTrackingLists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncTrackingLists();
            }
        });

        this.btSyncUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncUsers();
            }
        });

        this.btSyncHouseholds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncHouseholds();
            }
        });

        this.btSyncMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncMembers();
            }
        });

        this.username = (String) getIntent().getExtras().get("username");
        this.password = (String) getIntent().getExtras().get("password");
        this.serverUrl = (String) getIntent().getExtras().get("server-url");

        this.progressDialog.setCancelable(false);

        /*
        if (BuildConfig.DEBUG){ //this is working perfectly - if is a release version wont use my personal computer
            this.serverUrl = "http://172.16.234.123:8080/manhica-dbsync"; //getString(R.string.server_url);//"getString("http://172.16.234.123:8080/manhica-dbsync");
        }else{
            this.serverUrl = getString(R.string.server_url);
        }
        */

    }

    private void syncSettings() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this, this, serverUrl, username, password, SyncEntity.MODULES, SyncEntity.PARAMETERS, SyncEntity.FORMS);
        syncEntitiesTask.execute();
    }

    private void syncDatasets() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this, this, serverUrl, username, password, SyncEntity.DATASETS, SyncEntity.DATASETS_CSV_FILES);
        syncEntitiesTask.execute();
    }

    private void syncTrackingLists() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this, this, serverUrl, username, password, SyncEntity.TRACKING_LISTS);
        syncEntitiesTask.execute();
    }

    private void syncUsers() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this, this, serverUrl, username, password, SyncEntity.USERS);
        syncEntitiesTask.execute();
    }

    private void syncHouseholds() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this, this, serverUrl, username, password, SyncEntity.REGIONS, SyncEntity.HOUSEHOLDS);
        syncEntitiesTask.execute();
    }

    private void syncMembers() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this, this, serverUrl, username, password, SyncEntity.MEMBERS);
        syncEntitiesTask.execute();
    }

    /* Dialog Progress Updates*/

    private void initDialog(){
        progressDialog.syncInitialize();;
        progressDialog.setTitle(this.getString(R.string.sync_title_lbl));
        progressDialog.setMessage(this.getString(R.string.sync_prepare_download_lbl));
        progressDialog.setButtonEnabled(false);
        progressDialog.show();
    }

    @Override
    public void onSyncStart() {
        initDialog();
    }

    @Override
    public void onSyncProgressUpdate(Integer progress, String progressText) {
        progressDialog.setMessage(progressText);
    }

    @Override
    public void onSyncFinished(String result, List<SyncEntityReport> downloadReports, List<SyncEntityReport> persistedReports) {

        for (SyncEntityReport report : downloadReports){
            progressDialog.addSynchronizedMessage(report.getMessage(), report.isSuccessStatus());
        }

        for (SyncEntityReport report : persistedReports){
            progressDialog.addSynchronizedMessage(report.getMessage(), report.isSuccessStatus());
        }


        progressDialog.setMessage(result);
        progressDialog.setButtonEnabled(true);
        progressDialog.syncFinalize();


        showStatus();
    }
}
