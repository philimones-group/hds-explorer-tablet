package org.philimone.hds.explorer.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.philimone.hds.explorer.R;

public class SettingsActivity extends Activity {

    private Button btTrackListBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        initialize();

        this.setTitle(getString(R.string.settings_lbl));
    }

    private void initialize() {
        this.btTrackListBack = (Button) findViewById(R.id.btTrackListBack);

        this.btTrackListBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.this.onBackPressed();
            }
        });
    }

}