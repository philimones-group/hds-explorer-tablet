package org.philimone.hds.explorer.model.followup;

import android.content.ContentValues;

import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;

import java.io.Serializable;

public class TrackingSubjectList implements Serializable, Table {

    public static String TYPE_REGION = "Region";
    public static String TYPE_HOUSEHOLD = "Household";
    public static String TYPE_MEMBER = "Member";
    public static String TYPE_USER = "User";

    private int id;
    private int listId;     /* Id of the MemberList */
    private int trackingId; /* Id of the TrackingList/Follow-up List */
    private String title;   /* Title of the List */
    private String forms;   /* List of the Forms that all Members will have to collect */

    private String subjectCode;
    private String subjectType;
    private String subjectForms;
    private int subjectVisit;
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

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public boolean isRegionSubject(){
        return subjectType.equalsIgnoreCase(TYPE_REGION);
    }

    public boolean isHouseholdSubject(){
        return subjectType.equalsIgnoreCase(TYPE_HOUSEHOLD);
    }

    public boolean isMemberSubject(){
        return subjectType.equalsIgnoreCase(TYPE_MEMBER);
    }

    public int getSubjectVisit() {
        return subjectVisit;
    }

    public void setSubjectVisit(int subjectVisit) {
        this.subjectVisit = subjectVisit;
    }

    public String getSubjectForms() {
        return subjectForms;
    }

    public void setSubjectForms(String subjectForms) {
        this.subjectForms = subjectForms;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    @Override
    public String getTableName() {
        return DatabaseHelper.TrackingSubjectList.TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();

        cv.put(DatabaseHelper.TrackingSubjectList.COLUMN_LIST_ID, listId);
        cv.put(DatabaseHelper.TrackingSubjectList.COLUMN_TRACKING_ID, trackingId);
        cv.put(DatabaseHelper.TrackingSubjectList.COLUMN_TITLE, title);
        cv.put(DatabaseHelper.TrackingSubjectList.COLUMN_FORMS, forms);

        cv.put(DatabaseHelper.TrackingSubjectList.COLUMN_SUBJECT_CODE, subjectCode);
        cv.put(DatabaseHelper.TrackingSubjectList.COLUMN_SUBJECT_TYPE, subjectType);
        cv.put(DatabaseHelper.TrackingSubjectList.COLUMN_SUBJECT_VISIT, subjectVisit);
        cv.put(DatabaseHelper.TrackingSubjectList.COLUMN_SUBJECT_FORMS, subjectForms);

        cv.put(DatabaseHelper.TrackingSubjectList.COLUMN_COMPLETION_RATE, completionRate);


        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.TrackingSubjectList.ALL_COLUMNS;
    }
}