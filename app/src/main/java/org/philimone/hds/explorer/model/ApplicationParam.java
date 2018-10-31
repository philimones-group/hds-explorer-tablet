package org.philimone.hds.explorer.model;

import android.content.ContentValues;

import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;


/**
 * Represents an application parameter or setting to be saved on the database
 */
public class ApplicationParam implements Table {

    public static final String APP_URL = "app-url";
    public static final String ODK_URL = "odk-url";
    public static final String REDCAP_URL = "redcap-url";

    private int id;
    private String name;
    private String type;
    private String value;

    public ApplicationParam(){

    }

    public ApplicationParam(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getTableName() {
        return DatabaseHelper.ApplicationParam.TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.ApplicationParam.COLUMN_NAME, name);
        cv.put(DatabaseHelper.ApplicationParam.COLUMN_TYPE, type);
        cv.put(DatabaseHelper.ApplicationParam.COLUMN_VALUE, value);
        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.ApplicationParam.ALL_COLUMNS;
    }
}
