package org.philimone.hds.explorer.model;

import java.io.Serializable;
import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

/**
 * Created by paul on 8/10/16.
 */

@Entity
public class CollectedData implements Serializable {

    @Id
    public long id;
    public String formId;

    @Unique
    public String formUri;
    public String formXmlPath;
    public String formInstanceName;
    public Date formLastUpdatedDate;
    public String formModule;
    public int recordId;
    public String tableName;

    public String collectedBy;
    public String updatedBy;
    public String supervisedBy;
    public boolean supervised;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public Date getFormLastUpdatedDate() {
        return formLastUpdatedDate;
    }

    public void setFormLastUpdatedDate(Date formLastUpdatedDate) {
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
}
