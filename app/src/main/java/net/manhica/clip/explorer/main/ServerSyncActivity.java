package net.manhica.clip.explorer.main;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.manhica.clip.explorer.R;
import net.manhica.clip.explorer.database.Database;
import net.manhica.clip.explorer.database.Queries;
import net.manhica.clip.explorer.io.SyncDatabaseListener;
import net.manhica.clip.explorer.io.SyncEntitiesTask;
import net.manhica.clip.explorer.model.SyncReport;

import java.util.List;

import mz.betainteractive.utilities.StringUtil;

public class ServerSyncActivity extends AppCompatActivity implements SyncDatabaseListener {

    private Button btSyncModules;
    private Button btSyncForms;
    private Button btSyncUsers;
    private Button btSyncHouseholds;
    private Button btSyncMembers;

    private TextView txtSyncModulesStatus;
    private TextView txtSyncFormsStatus;
    private TextView txtSyncUsersStatus;
    private TextView txtSyncHouseholdsStatus;
    private TextView txtSyncMembersStatus;

    private ProgressDialog progressDialog;


    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_sync);

        initialize();
        showStatus();
    }

    @Override
    public void collectionComplete(String result) {
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
                case SyncReport.REPORT_USERS: txtSyncUsersStatus.setText(status+"");  break;
                case SyncReport.REPORT_HOUSEHOLDS: txtSyncHouseholdsStatus.setText(status+"");  break;
                case SyncReport.REPORT_MEMBERS: txtSyncMembersStatus.setText(status+"");  break;
            }
        }

        db.close();
    }

    private void initialize() {
        this.btSyncModules = (Button) findViewById(R.id.btSyncModules);
        this.btSyncForms = (Button) findViewById(R.id.btSyncForms);
        this.btSyncUsers = (Button) findViewById(R.id.btSyncUsers);
        this.btSyncHouseholds = (Button) findViewById(R.id.btSyncHouseholds);
        this.btSyncMembers = (Button) findViewById(R.id.btSyncMembers);

        this.txtSyncModulesStatus = (TextView) findViewById(R.id.txtSyncModulesStatus);
        this.txtSyncFormsStatus = (TextView) findViewById(R.id.txtSyncFormsStatus);
        this.txtSyncUsersStatus = (TextView) findViewById(R.id.txtSyncUsersStatus);
        this.txtSyncHouseholdsStatus = (TextView) findViewById(R.id.txtSyncHouseholdsStatus);
        this.txtSyncMembersStatus = (TextView) findViewById(R.id.txtSyncMembersStatus);

        this.progressDialog = new ProgressDialog(this);

        this.btSyncModules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncModules();
            }
        });

        this.btSyncForms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncForms();
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


    }

    private void syncModules() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this, progressDialog, this, getString(R.string.server_url), username, password, SyncEntitiesTask.Entity.MODULES);
        syncEntitiesTask.execute();
    }

    private void syncForms() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this, progressDialog, this, getString(R.string.server_url), username, password, SyncEntitiesTask.Entity.FORMS);
        syncEntitiesTask.execute();
    }

    private void syncUsers() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this, progressDialog, this, getString(R.string.server_url), username, password, SyncEntitiesTask.Entity.USERS);
        syncEntitiesTask.execute();
    }

    private void syncHouseholds() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this, progressDialog, this, getString(R.string.server_url), username, password, SyncEntitiesTask.Entity.HOUSEHOLDS);
        syncEntitiesTask.execute();
    }

    private void syncMembers() {
        SyncEntitiesTask syncEntitiesTask = new SyncEntitiesTask(this, progressDialog, this, getString(R.string.server_url), username, password, SyncEntitiesTask.Entity.MEMBERS);
        syncEntitiesTask.execute();
    }
}
