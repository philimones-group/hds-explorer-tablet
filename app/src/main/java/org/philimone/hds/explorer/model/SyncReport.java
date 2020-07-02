package org.philimone.hds.explorer.model;

import android.content.ContentValues;

import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;
import org.philimone.hds.explorer.io.SyncEntity;
import org.philimone.hds.explorer.io.SyncStatus;

import java.io.Serializable;
import java.util.Date;

import mz.betainteractive.utilities.StringUtil;

/**
 * Created by paul on 5/21/16.
 */
public class SyncReport implements Serializable, Table {

    private int id;
    private SyncEntity reportId;
    private Date date;
    private SyncStatus status;
    private String description;

    public SyncReport(){

    }

    public SyncReport(SyncEntity reportId, Date date, SyncStatus status, String description) {
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

    public SyncEntity getReportId() {
        return reportId;
    }

    public void setReportId(SyncEntity reportId) {
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

    public SyncStatus getStatus() {
        return status;
    }

    public void setStatus(SyncStatus status) {
        this.status = status;
    }

    @Override
    public String getTableName() {
        return DatabaseHelper.SyncReport.TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.SyncReport.COLUMN_REPORT_ID, reportId.getCode());
        cv.put(DatabaseHelper.SyncReport.COLUMN_DATE, date==null ? "" : StringUtil.format(date, "yyyy-MM-dd HH:mm:ss"));
        cv.put(DatabaseHelper.SyncReport.COLUMN_STATUS, status.getCode());
        cv.put(DatabaseHelper.SyncReport.COLUMN_DESCRIPTION, description);
        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.SyncReport.ALL_COLUMNS;
    }
}
