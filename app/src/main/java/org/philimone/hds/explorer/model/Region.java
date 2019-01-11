package org.philimone.hds.explorer.model;

import android.content.ContentValues;

import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;
import java.io.Serializable;

import mz.betainteractive.utilities.ReflectionUtils;

public class Region implements Table, Serializable {

    public static String HIERARCHY_1 = "hierarchy1";
    public static String HIERARCHY_2 = "hierarchy2";
    public static String HIERARCHY_3 = "hierarchy3";
    public static String HIERARCHY_4 = "hierarchy4";
    public static String HIERARCHY_5 = "hierarchy5";
    public static String HIERARCHY_6 = "hierarchy6";
    public static String HIERARCHY_7 = "hierarchy7";
    public static String HIERARCHY_8 = "hierarchy8";

    private int id;
    private String code;
    private String name;
    private String level;
    private String parent;

    private boolean selected; /*USED ON EXPANDED SELECTION LIST*/

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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getValueByName(String variableName){
        return ReflectionUtils.getValueByName(this, variableName);
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
