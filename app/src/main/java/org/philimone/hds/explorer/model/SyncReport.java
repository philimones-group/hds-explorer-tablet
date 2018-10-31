package org.philimone.hds.explorer.model;

import android.content.ContentValues;

import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;

import java.io.Serializable;
import java.util.Date;

import mz.betainteractive.utilities.StringUtil;

/**
 * Created by paul on 5/21/16.
 */
public class SyncReport implements Serializable, Table {
    public final static int REPORT_MODULES = 1;
    public final static int REPORT_FORMS = 2;
    public final static int REPORT_USERS = 3;
    public final static int REPORT_HOUSEHOLDS = 4;
    public final static int REPORT_MEMBERS = 5;
    public final static int REPORT_TRACKING_LISTS = 6;

    public final static int STATUS_NOT_SYNCED = 0;
    public final static int STATUS_SYNCED = 1;
    public final static int STATUS_SYNC_ERROR = 2;

    private int id;
    private int reportId;
    private Date date;
    private int status;
    private String description;

    public SyncReport(){

    }

    public SyncReport(int reportId, Date date, int status, String description) {
        this.reportId = reportId;
        this.date = date;
        this.status = status;
        this.description = description;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDate(String date) {
        if (date != null && !date.isEmpty()){
            this.date = StringUtil.toDate(date, "yyyy-MM-dd HH:mm:ss");
            return;
        }

        this.date = null;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String getTableName() {
        return DatabaseHelper.SyncReport.TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.SyncReport.COLUMN_REPORT_ID, reportId);
        cv.put(DatabaseHelper.SyncReport.COLUMN_DATE, date==null ? "" : StringUtil.format(date, "yyyy-MM-dd HH:mm:ss"));
        cv.put(DatabaseHelper.SyncReport.COLUMN_STATUS, status);
        cv.put(DatabaseHelper.SyncReport.COLUMN_DESCRIPTION, description);
        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.SyncReport.ALL_COLUMNS;
    }
}
