package net.manhica.dss.explorer.model.followup;

import android.content.ContentValues;

import net.manhica.dss.explorer.database.DatabaseHelper;
import net.manhica.dss.explorer.database.Table;

import java.io.Serializable;

public class TrackingMemberList implements Serializable, Table {

    private int id;
    private int trackingId;
    private String title;
    private String forms;

    private String memberExtId;
    private String memberPermId;
    private String memberStudyCode;
    private String memberForms;
    private Double completionRate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(int trackingId) {
        this.trackingId = trackingId;
    }

    public String getForms() {
        return forms;
    }

    public void setForms(String forms) {
        this.forms = forms;
    }

    public String getMemberPermId() {
        return memberPermId;
    }

    public void setMemberPermId(String memberPermId) {
        this.memberPermId = memberPermId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMemberExtId() {
        return memberExtId;
    }

    public void setMemberExtId(String memberExtId) {
        this.memberExtId = memberExtId;
    }

    public String getMemberStudyCode() {
        return memberStudyCode;
    }

    public void setMemberStudyCode(String memberStudyCode) {
        this.memberStudyCode = memberStudyCode;
    }

    public String getMemberForms() {
        return memberForms;
    }

    public void setMemberForms(String memberForms) {
        this.memberForms = memberForms;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    @Override
    public String getTableName() {
        return DatabaseHelper.TrackingMemberList.TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();

        cv.put(DatabaseHelper.TrackingMemberList.COLUMN_TRACKING_ID, trackingId);
        cv.put(DatabaseHelper.TrackingMemberList.COLUMN_TITLE, title);
        cv.put(DatabaseHelper.TrackingMemberList.COLUMN_FORMS, forms);

        cv.put(DatabaseHelper.TrackingMemberList.COLUMN_MEMBER_EXT_ID, memberExtId);
        cv.put(DatabaseHelper.TrackingMemberList.COLUMN_MEMBER_PERM_ID, memberPermId);
        cv.put(DatabaseHelper.TrackingMemberList.COLUMN_MEMBER_STUDY_CODE, memberStudyCode);
        cv.put(DatabaseHelper.TrackingMemberList.COLUMN_MEMBER_FORMS, memberForms);

        cv.put(DatabaseHelper.TrackingMemberList.COLUMN_COMPLETION_RATE, completionRate);


        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.TrackingList.ALL_COLUMNS;
    }
}