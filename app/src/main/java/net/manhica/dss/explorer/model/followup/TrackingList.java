package net.manhica.dss.explorer.model.followup;

import android.content.ContentValues;

import net.manhica.dss.explorer.database.DatabaseHelper;
import net.manhica.dss.explorer.database.Table;

import java.io.Serializable;

public class TrackingList implements Serializable, Table {

    private int id;
    private String label;
    private String title;
    private String module;
    private Double completionRate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    @Override
    public String getTableName() {
        return DatabaseHelper.TrackingList.TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();

        cv.put(DatabaseHelper.TrackingList.COLUMN_LABEL, label);
        cv.put(DatabaseHelper.TrackingList.COLUMN_TITLE, title);
        cv.put(DatabaseHelper.TrackingList.COLUMN_MODULE, module);
        cv.put(DatabaseHelper.TrackingList.COLUMN_COMPLETION_RATE, completionRate);

        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.TrackingList.ALL_COLUMNS;
    }
}