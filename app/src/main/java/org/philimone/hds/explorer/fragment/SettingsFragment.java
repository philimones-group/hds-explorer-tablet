package org.philimone.hds.explorer.fragment;


import android.content.ContentValues;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Database;
import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.ApplicationParam;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment {


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);

        initialize();
    }

    private void initialize() {

        EditTextPreference prefAppUrl = (EditTextPreference) findPreference(ApplicationParam.APP_URL);
        EditTextPreference prefOdkUrl = (EditTextPreference) findPreference(ApplicationParam.ODK_URL);
        EditTextPreference prefRedcapUrl = (EditTextPreference) findPreference(ApplicationParam.REDCAP_URL);

        String app_url = Queries.getApplicationParamValue(ApplicationParam.APP_URL, this.getActivity());
        String odk_url = Queries.getApplicationParamValue(ApplicationParam.ODK_URL, this.getActivity());
        String redcap_url = Queries.getApplicationParamValue(ApplicationParam.REDCAP_URL, this.getActivity());

        if (app_url != null){
            setValueInPreference(prefAppUrl, app_url);
        }
        if (odk_url != null){
            setValueInPreference(prefOdkUrl, odk_url);
        }
        if (redcap_url != null){
            setValueInPreference(prefRedcapUrl, redcap_url);
        }

        prefAppUrl.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //Log.d("value-changed-to", ""+newValue);
                setValueInDatabase((EditTextPreference) preference, newValue.toString());
                return true;
            }
        });

        prefOdkUrl.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //Log.d("value-changed-to", ""+newValue);
                setValueInDatabase((EditTextPreference) preference, newValue.toString());
                return true;
            }
        });

        prefRedcapUrl.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //Log.d("value-changed-to", ""+newValue);
                setValueInDatabase((EditTextPreference) preference, newValue.toString());
                return true;
            }
        });
    }

    private void setValueInPreference(EditTextPreference pref, String value){
        pref.setText(value);
        pref.setSummary(value);
    }

    private void setValueInDatabase(EditTextPreference pref, String newValue){
        pref.setText(newValue);
        pref.setSummary(newValue);

        updateApplicationParam(pref.getKey(), newValue);
    }

    private boolean updateApplicationParam(String name, String value){

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.ApplicationParam.COLUMN_VALUE, value);

        Database db = new Database(this.getActivity());
        db.open();

        int i = db.update(ApplicationParam.class, cv, DatabaseHelper.ApplicationParam.COLUMN_NAME+"=?", new String[]{ name });

        db.close();

        return i>0;
    }
}
