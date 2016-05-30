package net.manhica.clip.explorer.model;

import android.content.ContentValues;

import net.manhica.clip.explorer.database.DatabaseHelper;
import net.manhica.clip.explorer.database.Table;

import java.io.Serializable;

/**
 * Created by paul on 5/20/16.
 */
public class Form implements Serializable, Table {

    private String formId;
    private String formName;
    private String formDescription;
    private String gender; /*M, F, ALL*/
    private int minAge; //0
    private int maxAge; //Default - 120
    private String modules; //if null - is accessed by all

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
        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.Form.ALL_COLUMNS;
    }
}
