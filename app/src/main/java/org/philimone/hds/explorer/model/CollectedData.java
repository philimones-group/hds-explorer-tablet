package org.philimone.hds.explorer.model;

import android.content.ContentValues;

import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;

import java.io.Serializable;

/**
 * Created by paul on 8/10/16.
 */
public class CollectedData implements Serializable, Table {

    private int id;
    private String formId;
    private String formUri;
    private String formXmlPath;
    private String formInstanceName;
    private String formLastUpdatedDate;
    private String formModule;
    private int recordId;
    private String tableName;

    private String collectedBy;
    private String updatedBy;
    private String supervisedBy;
    private boolean supervised;




    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getFormUri() {
        return formUri;
    }

    public void setFormUri(String formUri) {
        this.formUri = formUri;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getFormXmlPath() {
        return formXmlPath;
    }

    public void setFormXmlPath(String formXmlPath) {
        this.formXmlPath = formXmlPath;
    }

    public String getFormInstanceName() {
        return formInstanceName;
    }

    public void setFormInstanceName(String formInstanceName) {
        this.formInstanceName = formInstanceName;
    }

    public String getFormLastUpdatedDate() {
        return formLastUpdatedDate;
    }

    public void setFormLastUpdatedDate(String formLastUpdatedDate) {
        this.formLastUpdatedDate = formLastUpdatedDate;
    }

    public String getFormModule() {
        return formModule;
    }

    public void setFormModule(String formModule) {
        this.formModule = formModule;
    }

    public String getCollectedBy() {
        return collectedBy;
    }

    public void setCollectedBy(String collectedBy) {
        this.collectedBy = collectedBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getSupervisedBy() {
        return supervisedBy;
    }

    public void setSupervisedBy(String supervisedBy) {
        this.supervisedBy = supervisedBy;
    }

    public boolean isFormFinalized(){
        return this.formXmlPath!=null && !this.formXmlPath.isEmpty();
    }

    public boolean isSupervised() {
        return supervised;
    }

    public void setSupervised(boolean supervised) {
        this.supervised = supervised;
    }

    @Override
    public String getTableName() {
        return DatabaseHelper.CollectedData.TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.CollectedData.COLUMN_FORM_ID, formId);
        cv.put(DatabaseHelper.CollectedData.COLUMN_FORM_URI, formUri);
        cv.put(DatabaseHelper.CollectedData.COLUMN_FORM_XML_PATH, formXmlPath);
        cv.put(DatabaseHelper.CollectedData.COLUMN_FORM_INSTANCE_NAME, formInstanceName);
        cv.put(DatabaseHelper.CollectedData.COLUMN_FORM_LAST_UPDATED_DATE, formLastUpdatedDate);
        cv.put(DatabaseHelper.CollectedData.COLUMN_FORM_MODULE, formModule);
        cv.put(DatabaseHelper.CollectedData.COLUMN_COLLECTED_BY, collectedBy);
        cv.put(DatabaseHelper.CollectedData.COLUMN_UPDATED_BY, updatedBy);
        cv.put(DatabaseHelper.CollectedData.COLUMN_SUPERVISED_BY, supervisedBy);
        cv.put(DatabaseHelper.CollectedData.COLUMN_RECORD_ID, recordId);
        cv.put(DatabaseHelper.CollectedData.COLUMN_TABLE_NAME, tableName);
        cv.put(DatabaseHelper.CollectedData.COLUMN_SUPERVISED, supervised ? 1 : 0);
        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.CollectedData.ALL_COLUMNS;
    }
}
