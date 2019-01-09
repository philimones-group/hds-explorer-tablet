package org.philimone.hds.explorer.model;

import android.content.ContentValues;

import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 5/20/16.
 */
public class  Form implements Serializable, Table {

    private int id;
    private String formId;
    private String formName;
    private String formDescription;
    private String formDependencies;
    private String regionLevel;
    private String gender; /*M, F, ALL*/
    private int minAge; //0
    private int maxAge; //Default - 120
    private String modules; //if null - is accessed by all
    private boolean isRegionForm;
    private boolean isHouseholdForm;
    private boolean isHouseholdHeadForm;
    private boolean isMemberForm;
    private boolean isFollowUpOnly;
    private String formMapText;
    private Map<String, String> formMap;
    private String redcapApi;
    private String redcapMapText;

    public Form(){
        formMap = new HashMap<>();
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getFormDependencies() {
        return formDependencies;
    }

    public void setFormDependencies(String formDependencies) {
        this.formDependencies = formDependencies;
    }

    public String getRegionLevel() {
        return regionLevel;
    }

    public void setRegionLevel(String regionLevel) {
        this.regionLevel = regionLevel;
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

    public boolean isRegionForm() {
        return isRegionForm;
    }

    public void setRegionForm(boolean regionForm) {
        isRegionForm = regionForm;
    }

    public boolean isHouseholdForm() {
        return isHouseholdForm;
    }

    public void setHouseholdForm(boolean householdForm) {
        isHouseholdForm = householdForm;
    }

    public boolean isHouseholdHeadForm() {
        return isHouseholdHeadForm;
    }

    public void setHouseholdHeadForm(boolean householdHeadForm) {
        isHouseholdHeadForm = householdHeadForm;
    }

    public boolean isMemberForm() {
        return isMemberForm;
    }

    public void setMemberForm(boolean memberForm) {
        isMemberForm = memberForm;
    }

    public boolean isFollowUpOnly() {
        return isFollowUpOnly;
    }

    public void setFollowUpOnly(boolean followUpOnly) {
        isFollowUpOnly = followUpOnly;
    }

    public void setFormMap(String bindMapAsText){
        this.formMapText = bindMapAsText;
        convertFormMapTextToMap();
    }

    public Map<String, String> getFormMap(){
        return this.formMap;
    }

    private void convertFormMapTextToMap() {
        if (formMapText != null && !formMapText.isEmpty()){
            this.formMap.clear();

            String[] entries = formMapText.split(";");
            for (String entry : entries){
                String[] keyValue = entry.split(":");
                if (keyValue.length == 2){
                    this.formMap.put(keyValue[1], keyValue[0]); //mapping unique items (odk variables) as Key, the values a the domain column names (TableName.columnName)
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
        cv.put(DatabaseHelper.Form.COLUMN_FORM_DEPENDENCIES, formDependencies);
        cv.put(DatabaseHelper.Form.COLUMN_REGION_LEVEL, regionLevel);
        cv.put(DatabaseHelper.Form.COLUMN_GENDER, gender);
        cv.put(DatabaseHelper.Form.COLUMN_MIN_AGE, minAge);
        cv.put(DatabaseHelper.Form.COLUMN_MAX_AGE, maxAge);
        cv.put(DatabaseHelper.Form.COLUMN_MODULES, modules);
        cv.put(DatabaseHelper.Form.COLUMN_IS_REGION, isRegionForm ? 1 : 0);
        cv.put(DatabaseHelper.Form.COLUMN_IS_HOUSEHOLD, isHouseholdForm ? 1 : 0);
        cv.put(DatabaseHelper.Form.COLUMN_IS_HOUSEHOLD_HEAD, isHouseholdHeadForm ? 1 : 0);
        cv.put(DatabaseHelper.Form.COLUMN_IS_MEMBER, isMemberForm ? 1 : 0);
        cv.put(DatabaseHelper.Form.COLUMN_IS_FOLLOW_UP_ONLY, isFollowUpOnly ? 1 : 0);
        cv.put(DatabaseHelper.Form.COLUMN_FORM_MAP, formMapText);
        cv.put(DatabaseHelper.Form.COLUMN_REDCAP_API, redcapApi);
        cv.put(DatabaseHelper.Form.COLUMN_REDCAP_MAP, redcapMapText);
        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.Form.ALL_COLUMNS;
    }
}
