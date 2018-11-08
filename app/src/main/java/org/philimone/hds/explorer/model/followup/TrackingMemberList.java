package org.philimone.hds.explorer.model.followup;

import android.content.ContentValues;

import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;

import java.io.Serializable;

public class TrackingMemberList implements Serializable, Table {

    private int id;
    private int listId;     /* Id of the MemberList */
    private int trackingId; /* Id of the TrackingList/Follow-up List */
    private String title;   /* Title of the List */
    private String forms;   /* List of the Forms that all Members will have to collect */

    private String memberCode;
    private String memberStudyCode;
    private String memberForms;
    private int    memberVisit;
    private Double completionRate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public String getMemberStudyCode() {
        return memberStudyCode;
    }

    public void setMemberStudyCode(String memberStudyCode) {
        this.memberStudyCode = memberStudyCode;
    }

    public int getMemberVisit() {
        return memberVisit;
    }

    public void setMemberVisit(int memberVisit) {
        this.memberVisit = memberVisit;
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

        cv.put(DatabaseHelper.TrackingMemberList.COLUMN_LIST_ID, listId);
        cv.put(DatabaseHelper.TrackingMemberList.COLUMN_TRACKING_ID, trackingId);
        cv.put(DatabaseHelper.TrackingMemberList.COLUMN_TITLE, title);
        cv.put(DatabaseHelper.TrackingMemberList.COLUMN_FORMS, forms);

        cv.put(DatabaseHelper.TrackingMemberList.COLUMN_MEMBER_CODE, memberCode);
        cv.put(DatabaseHelper.TrackingMemberList.COLUMN_MEMBER_STUDY_CODE, memberStudyCode);
        cv.put(DatabaseHelper.TrackingMemberList.COLUMN_MEMBER_VISIT, memberVisit);
        cv.put(DatabaseHelper.TrackingMemberList.COLUMN_MEMBER_FORMS, memberForms);

        cv.put(DatabaseHelper.TrackingMemberList.COLUMN_COMPLETION_RATE, completionRate);


        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.TrackingMemberList.ALL_COLUMNS;
    }
}