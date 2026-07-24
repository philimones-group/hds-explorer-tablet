package org.philimone.hds.explorer.main.utilities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import mz.betainteractive.odk.model.FilledForm;
import mz.betainteractive.odk.storage.access.OdkStorageType;

public class OdkPreloadActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load FilledForm that was saved to a temporary file
        FilledForm preloadedData = FilledForm.loadInstance(this);

        //load the data to ODK field-list - so far we dont support repeat groups
        Intent intent = new Intent();

        for (Map.Entry<String, Object> entry : preloadedData.getValues().entrySet()) {
            intent.putExtra(entry.getKey(), entry.getValue()+"");
        }

        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
