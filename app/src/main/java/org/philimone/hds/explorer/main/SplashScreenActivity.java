package org.philimone.hds.explorer.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;

import org.philimone.hds.explorer.R;
import org.philimone.hds.forms.listeners.FormCollectionListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

/*
        FormFragment.newInstance(getResources().openRawResource(R.raw.hform_model), "paul", new FormCollectionListener() {
            @Override
            public ValidationResult onFormValidate(HForm form, List<ColumnValue> collectedValues) {
                return new ValidationResult(false, collectedValues);
            }

            @Override
            public void onFormFinished(HForm form, List<ColumnValue> collectedValues, XmlFormResult result) {

            }
        }).show(this.getSupportFragmentManager(), "tagmaster");*/

        Handler handler=new Handler();
        handler.postDelayed(() -> {
            Intent intent=new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, 2000);

    }
}