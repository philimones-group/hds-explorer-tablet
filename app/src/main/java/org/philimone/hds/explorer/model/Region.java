package org.philimone.hds.explorer.model;

import android.content.ContentValues;

import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;
import java.io.Serializable;

public class Region implements Table, Serializable {

    private int id;
    private String code;
    private String name;
    private String level;
    private String parent;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @Override
    public String getTableName() {
        return DatabaseHelper.Region.TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.Region.COLUMN_CODE, code);
        cv.put(DatabaseHelper.Region.COLUMN_NAME, name);
        cv.put(DatabaseHelper.Region.COLUMN_LEVEL, level);
        cv.put(DatabaseHelper.Region.COLUMN_PARENT, parent);
        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.Region.ALL_COLUMNS;
    }


}
