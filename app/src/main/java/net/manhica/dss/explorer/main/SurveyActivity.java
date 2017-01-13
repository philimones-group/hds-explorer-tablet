package net.manhica.dss.explorer.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import net.manhica.dss.explorer.R;
import net.manhica.dss.explorer.model.User;

public class SurveyActivity extends Activity {

    private User loggedUser;
    private Button btSurveyHouseholds;
    private Button btSurveyMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey);

        this.loggedUser = (User) getIntent().getExtras().get("user");

        this.btSurveyHouseholds = (Button) findViewById(R.id.btSurveyHouseholds);
        this.btSurveyMembers = (Button) findViewById(R.id.btSurveyMembers);

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


}
