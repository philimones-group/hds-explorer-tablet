package org.philimone.hds.explorer.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.main.sync.SyncDataSharingActivity;
import org.philimone.hds.explorer.model.User;

import androidx.appcompat.app.AppCompatActivity;

public class SurveyActivity extends AppCompatActivity {

    private User loggedUser;
    private Button btSurveyHouseholds;
    private Button btSurveyMembers;
    private Button btTrackingLists;
    private Button btShowCollectedData;
    private Button btSearchByDatasets;
    private Button btSyncDataSharing;

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

        this.btSurveyHouseholds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSurveyHouseholds();
            }
        });

        this.btSurveyMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSurveyMembers();
            }
        });

        this.btTrackingLists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTrackingLists();
            }
        });

        this.btShowCollectedData.setOnClickListener(v -> {
            openShowCollectedData();
        });

        this.btSearchByDatasets.setOnClickListener( v -> {
            openSearchByDatasets();
        });

        this.btSyncDataSharing.setOnClickListener(v -> {
            openSyncDataSharing();
        });
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

}
