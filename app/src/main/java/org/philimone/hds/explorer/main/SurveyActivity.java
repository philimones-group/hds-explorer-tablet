package org.philimone.hds.explorer.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.model.User;

import androidx.appcompat.app.AppCompatActivity;

public class SurveyActivity extends AppCompatActivity {

    private User loggedUser;
    private Button btSurveyHouseholds;
    private Button btSurveyMembers;
    private Button btTrackingLists;
    private Button btShowCollectedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey);

        this.loggedUser = Bootstrap.getCurrentUser();

        this.btSurveyHouseholds = (Button) findViewById(R.id.btSurveyHouseholds);
        this.btSurveyMembers = (Button) findViewById(R.id.btSurveyMembers);
        this.btTrackingLists = (Button) findViewById(R.id.btTrackingLists);
        this.btShowCollectedData = (Button) findViewById(R.id.btShowCollectedData);

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

}
