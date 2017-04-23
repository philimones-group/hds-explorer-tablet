package net.manhica.dss.explorer.model;

import android.content.ContentValues;

import net.manhica.dss.explorer.database.DatabaseHelper;
import net.manhica.dss.explorer.database.Table;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 5/20/16.
 */
public class  Form implements Serializable, Table {

    private String formId;
    private String formName;
    private String formDescription;
    private String gender; /*M, F, ALL*/
    private int minAge; //0
    private int maxAge; //Default - 120
    private String modules; //if null - is accessed by all
    private boolean isHouseholdForm;
    private boolean isMemberForm;
    private String bindMapText;
    private Map<String, String> bindMap;
    private String redcapApi;
    private String redcapMapText;

    public Form(){
        bindMap = new HashMap<>();
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getFormDescription() {
        return formDescription;
    }

    public void setFormDescription(String formDescription) {
        this.formDescription = formDescription;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public String getModules() {
        return modules;
    }

    public void setModules(String modules) {
        this.modules = modules;
    }

    public boolean isHouseholdForm() {
        return isHouseholdForm;
    }

    public void setHouseholdForm(boolean householdForm) {
        isHouseholdForm = householdForm;
    }

    public boolean isMemberForm() {
        return isMemberForm;
    }

    public void setMemberForm(boolean memberForm) {
        isMemberForm = memberForm;
    }

    public void setBindMap(String bindMapAsText){
        this.bindMapText = bindMapAsText;
        convertBindMapTextToMap();
    }

    public Map<String, String> getBindMap(){
        return this.bindMap;
    }

    private void convertBindMapTextToMap() {
        if (bindMapText != null && !bindMapText.isEmpty()){
            this.bindMap.clear();

            String[] entries = bindMapText.split(";");
            for (String entry : entries){
                String[] keyValue = entry.split(":");
                if (keyValue.length == 2){
                    this.bindMap.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }

    public void setRedcapApi(String redcapApi){
        this.redcapApi = redcapApi;
    }

    public void setRedcapMap(String redcapMapText){
        this.redcapMapText = redcapMapText;
    }

    @Override
    public String getTableName() {
        return DatabaseHelper.Form.TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.Form.COLUMN_FORM_ID, formId);
        cv.put(DatabaseHelper.Form.COLUMN_FORM_NAME, formName);
        cv.put(DatabaseHelper.Form.COLUMN_FORM_DESCRIPTION, formDescription);
        cv.put(DatabaseHelper.Form.COLUMN_GENDER, gender);
        cv.put(DatabaseHelper.Form.COLUMN_MIN_AGE, minAge);
        cv.put(DatabaseHelper.Form.COLUMN_MAX_AGE, maxAge);
        cv.put(DatabaseHelper.Form.COLUMN_MODULES, modules);
        cv.put(DatabaseHelper.Form.COLUMN_IS_HOUSEHOLD, isHouseholdForm ? 1 : 0);
        cv.put(DatabaseHelper.Form.COLUMN_IS_MEMBER, isMemberForm ? 1 : 0);
        cv.put(DatabaseHelper.Form.COLUMN_BIND_MAP, bindMapText);
        cv.put(DatabaseHelper.Form.COLUMN_REDCAP_API, redcapApi);
        cv.put(DatabaseHelper.Form.COLUMN_REDCAP_MAP, redcapMapText);
        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.Form.ALL_COLUMNS;
    }
}
