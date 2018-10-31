package org.philimone.hds.explorer.main;

import android.app.Activity;
import android.os.Bundle;

import org.philimone.hds.explorer.R;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        this.setTitle(getString(R.string.settings_lbl));
    }

}