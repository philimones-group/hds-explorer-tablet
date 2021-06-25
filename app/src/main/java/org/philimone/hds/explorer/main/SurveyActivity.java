package org.philimone.hds.explorer.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.model.User;

import androidx.appcompat.app.AppCompatActivity;

public class SurveyActivity extends AppCompatActivity {

    private User loggedUser;
    private Button btSurveyHouseholds;
    private Button btSurveyMembers;
    private Button btTrackingLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey);

        this.loggedUser = (User) getIntent().getExtras().get("user");

        this.btSurveyHouseholds = (Button) findViewById(R.id.btSurveyHouseholds);
        this.btSurveyMembers = (Button) findViewById(R.id.btSurveyMembers);
        this.btTrackingLists = (Button) findViewById(R.id.btTrackingLists);

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
    }

    private void openSurveyMembers() {
        Intent intent = new Intent(this, SurveyMembersActivity.class);
        intent.putExtra("user", loggedUser);
        startActivity(intent);
    }

    private void openSurveyHouseholds() {
        Intent intent = new Intent(this, SurveyHouseholdsActivity.class);
        intent.putExtra("user", loggedUser);
        startActivity(intent);
    }

    private void openTrackingLists() {
        Intent intent = new Intent(this, TrackingListActivity.class);
        intent.putExtra("user", loggedUser);
        startActivity(intent);
    }


}
