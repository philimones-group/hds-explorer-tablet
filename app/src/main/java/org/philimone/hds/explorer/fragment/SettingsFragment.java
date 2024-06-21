package org.philimone.hds.explorer.fragment;


import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

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
        CheckBoxPreference prefPostExec = (CheckBoxPreference) findPreference(ApplicationParam.HFORM_POST_EXECUTION);

        String app_url = Queries.getApplicationParamValue(this.boxAppParams, ApplicationParam.APP_URL);
        String odk_url = Queries.getApplicationParamValue(this.boxAppParams, ApplicationParam.ODK_URL);
        String redcap_url = Queries.getApplicationParamValue(this.boxAppParams, ApplicationParam.REDCAP_URL);
        String spost_exec = Queries.getApplicationParamValue(this.boxAppParams, ApplicationParam.HFORM_POST_EXECUTION);
        Boolean post_exec = spost_exec!=null && spost_exec.equalsIgnoreCase("true");

        if (app_url != null){
            setValueInPreference(prefAppUrl, app_url);
        }
        if (odk_url != null){
            setValueInPreference(prefOdkUrl, odk_url);
        }
        if (redcap_url != null){
            setValueInPreference(prefRedcapUrl, redcap_url);
        }
        if (post_exec != null){
            setValueInPreference(prefPostExec, post_exec);
        }

        if (prefAppUrl != null)
        prefAppUrl.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //Log.d("value-changed-to", ""+newValue);
                setValueInDatabase((EditTextPreference) preference, newValue.toString());
                setValueInPreference((EditTextPreference) preference, newValue.toString());
                return true;
            }
        });

        if (prefOdkUrl != null)
        prefOdkUrl.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //Log.d("value-changed-to", ""+newValue);
                setValueInDatabase((EditTextPreference) preference, newValue.toString());
                setValueInPreference((EditTextPreference) preference, newValue.toString());
                return true;
            }
        });

        if (prefRedcapUrl != null)
        prefRedcapUrl.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //Log.d("value-changed-to", ""+newValue);
                setValueInDatabase((EditTextPreference) preference, newValue.toString());
                setValueInPreference((EditTextPreference) preference, newValue.toString());
                return true;
            }
        });

        if (prefPostExec != null)
        prefPostExec.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //Log.d("value-changed-to", ""+newValue);
                setValueInDatabase(preference, newValue.toString());
                setValueInPreference((CheckBoxPreference) preference, (Boolean) newValue);
                return true;
            }
        });
    }

    private void setValueInPreference(EditTextPreference pref, String value){
        if (pref == null) return;
        pref.setText(value);
        pref.setSummary(value);
    }

    private void setValueInPreference(CheckBoxPreference pref, boolean value){
        if (pref == null) return;
        pref.setChecked(value);
    }

    private void setValueInDatabase(Preference pref, String newValue){
        if (pref == null) return;

        //pref.setText(newValue);
        //pref.setSummary(newValue);

        updateApplicationParam(pref.getKey(), newValue);
    }

    private boolean updateApplicationParam(String name, String value){

        ApplicationParam param = boxAppParams.query().equal(ApplicationParam_.name, name, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (param != null) {
            param.value = value;
        }else {
            param = new ApplicationParam(name, "string", value);
            boxAppParams.put(param);
        }

        long result = boxAppParams.put(param);
        return result>0;
    }
}
