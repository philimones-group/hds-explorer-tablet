package org.philimone.hds.explorer.main;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.ApplicationParam;

import androidx.appcompat.app.AppCompatActivity;

import io.objectbox.Box;

public class SettingsActivity extends AppCompatActivity {

    private Button btTrackListBack;
    private Box<ApplicationParam> boxAppParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        initBoxes();
        initialize();

        this.setTitle(getString(R.string.settings_lbl));
    }

    private void initBoxes() {
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
    }

    private void initialize() {
        this.btTrackListBack = (Button) findViewById(R.id.btTrackListBack);

        this.btTrackListBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.this.onBackPressed();
            }
        });

        this.btTrackListBack.setOnLongClickListener(v -> {
            downloadApk();
            return true;
        });
    }

    private void downloadApk(){
        String app_url = Queries.getApplicationParamValue(this.boxAppParams, ApplicationParam.APP_URL);

        if (app_url != null) {
            app_url += "/download/apk";

            //download
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(app_url));

            // Start the activity
            startActivity(browserIntent);
        }
    }

}