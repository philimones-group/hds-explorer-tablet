package org.philimone.hds.explorer.fragment;


import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;

import io.objectbox.Box;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment {

    private Box<ApplicationParam> boxAppParams;

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

    private void initBoxes() {
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
    }

    private void initQueries(){

    }

    private void initialize() {

        initBoxes();

        EditTextPreference prefAppUrl = (EditTextPreference) findPreference(ApplicationParam.APP_URL);
        EditTextPreference prefOdkUrl = (EditTextPreference) findPreference(ApplicationParam.ODK_URL);
        EditTextPreference prefRedcapUrl = (EditTextPreference) findPreference(ApplicationParam.REDCAP_URL);

        String app_url = Queries.getApplicationParamValue(this.boxAppParams, ApplicationParam.APP_URL);
        String odk_url = Queries.getApplicationParamValue(this.boxAppParams, ApplicationParam.ODK_URL);
        String redcap_url = Queries.getApplicationParamValue(this.boxAppParams, ApplicationParam.REDCAP_URL);

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

        ApplicationParam param = boxAppParams.query().equal(ApplicationParam_.name, name).build().findFirst();

        if (param != null) {
            param.value = value;
            long result = boxAppParams.put(param);

            return result>0;
        }

        return false;
    }
}
