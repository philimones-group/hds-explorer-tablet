package org.philimone.hds.explorer.main;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.main.sync.SyncDataSharingActivity;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.widget.LoadingDialog;

import androidx.appcompat.app.AppCompatActivity;

public class SurveyActivity extends AppCompatActivity {

    private User loggedUser;
    private Button btSurveyHouseholds;
    private Button btSurveyMembers;
    private Button btTrackingLists;
    private Button btShowCollectedData;
    private Button btSearchByDatasets;
    private Button btSyncDataSharing;

    private LoadingDialog loadingDialog;

    private enum MenuItem { SURVEY_HOUSEHOLDS, SURVEY_MEMBERS, TRACKING_LISTS, SHOW_COLLECTED, SEARCH_BY_DATASETS, SYNC_DATASHARING }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey2);

        this.loggedUser = Bootstrap.getCurrentUser();

        this.btSurveyHouseholds = (Button) findViewById(R.id.btSurveyHouseholds);
        this.btSurveyMembers = (Button) findViewById(R.id.btSurveyMembers);
        this.btTrackingLists = (Button) findViewById(R.id.btTrackingLists);
        this.btShowCollectedData = (Button) findViewById(R.id.btShowCollectedData);
        this.btSearchByDatasets = (Button) findViewById(R.id.btSearchByDatasets);
        this.btSyncDataSharing = (Button) findViewById(R.id.btSyncDataSharing);

        this.loadingDialog = new LoadingDialog(this);

        this.btSurveyHouseholds.setOnClickListener(v -> onMenuItemSelected(MenuItem.SURVEY_HOUSEHOLDS));

        this.btSurveyMembers.setOnClickListener(v -> onMenuItemSelected(MenuItem.SURVEY_MEMBERS));

        this.btTrackingLists.setOnClickListener(v -> onMenuItemSelected(MenuItem.TRACKING_LISTS));

        this.btShowCollectedData.setOnClickListener(v -> onMenuItemSelected(MenuItem.SHOW_COLLECTED));

        this.btSearchByDatasets.setOnClickListener( v -> onMenuItemSelected(MenuItem.SEARCH_BY_DATASETS));

        this.btSyncDataSharing.setOnClickListener(v -> onMenuItemSelected(MenuItem.SYNC_DATASHARING));
    }

    private void openSurveyMembers() {
        Intent intent = new Intent(this, SurveyMembersActivity.class);
        startActivity(intent);
    }

    private void openSurveyHouseholds() {
        Intent intent = new Intent(this, SurveyHouseholdsActivity.class);
        startActivity(intent);
    }

    private void openTrackingLists() {
        Intent intent = new Intent(this, TrackingListActivity.class);
        startActivity(intent);
    }

    private void openShowCollectedData() {
        Intent intent = new Intent(this, ShowCollectedDataActivity.class);
        startActivity(intent);
    }

    private void openSyncDataSharing() {
        Intent intent = new Intent(this, SyncDataSharingActivity.class);
        startActivity(intent);
    }

    private void openSearchByDatasets() {
        //TODO To be implemented
    }

    private void onMenuItemSelected(MenuItem menuItem) {
        //showLoadingDialog("", true);
        //new OpenActivityTask(menuItem).execute();
        openActivity(menuItem);
    }
    private void openActivity(MenuItem menuItem) {
        switch (menuItem) {
            case SURVEY_HOUSEHOLDS: openSurveyHouseholds(); break;
            case SURVEY_MEMBERS: openSurveyMembers(); break;
            case TRACKING_LISTS: openTrackingLists(); break;
            case SHOW_COLLECTED: openShowCollectedData(); break;
            case SEARCH_BY_DATASETS: openSearchByDatasets(); break;
            case SYNC_DATASHARING: openSyncDataSharing(); break;
        }
    }

    private void showLoadingDialog(String msg, boolean show){
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.dismiss();
        }
    }

    class OpenActivityTask extends AsyncTask<Void, Void, Boolean> {
        MenuItem menuItem;

        public OpenActivityTask(MenuItem menuItem) {
            this.menuItem = menuItem;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            openActivity(menuItem);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            showLoadingDialog("", false);
        }
    }

}
