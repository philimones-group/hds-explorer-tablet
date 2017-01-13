package net.manhica.dss.explorer.model;

import android.content.ContentValues;

import net.manhica.dss.explorer.database.DatabaseHelper;
import net.manhica.dss.explorer.database.Table;

import java.io.Serializable;

/**
 * Created by paul on 5/20/16.
 */
public class Module implements Serializable, Table {

    public final static String DSS_SURVEY_MODULE = "DSS-SURVEY";
    public final static String DSS_SUPERVISOR = "DSS-SUPERVISOR";
    public final static String DSS_OTHERS = "DSS-ANY";

    private String code;
    private String name;
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getTableName() {
        return DatabaseHelper.Module.TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.Module.COLUMN_CODE, code);
        cv.put(DatabaseHelper.Module.COLUMN_NAME, name);
        cv.put(DatabaseHelper.Module.COLUMN_DESCRIPTION, description);
        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.Module.ALL_COLUMNS;
    }
}
