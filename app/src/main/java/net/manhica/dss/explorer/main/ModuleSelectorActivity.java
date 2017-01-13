package net.manhica.dss.explorer.main;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import net.manhica.dss.explorer.R;
import net.manhica.dss.explorer.model.User;

public class ModuleSelectorActivity extends Activity {

    private Button btPomModule;
    private Button btFacilityModule;
    private Button btSurveyModule;
    private Button btModuleSelectorExit;
    private User loggedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.module_selector);

        initialize();
    }

    private void initialize() {
        this.btPomModule = (Button) findViewById(R.id.btPomModule);
        this.btFacilityModule = (Button) findViewById(R.id.btFacilityModule);
        this.btSurveyModule = (Button) findViewById(R.id.btSurveyModule);
        this.btModuleSelectorExit = (Button) findViewById(R.id.btModuleSelectorExit);

        this.loggedUser = (User) getIntent().getExtras().get("user");

        this.btSurveyModule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchModule(SurveyActivity.class);
            }
        });

        this.btModuleSelectorExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });
    }

    private void launchModule(Class<? extends Activity> intentClass){
        Intent intent = new Intent(this, intentClass);
        intent.putExtra("user", loggedUser);
        startActivity(intent);
    }

    private void exit() {
        finish();
    }

}
