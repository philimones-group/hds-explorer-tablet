package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.StringCollectionConverter;
import org.philimone.hds.explorer.model.converters.SubjectEntityConverter;
import org.philimone.hds.explorer.model.enums.SubjectEntity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;

/**
 * Created by paul on 8/10/16.
 */

@Entity
public class CollectedData implements Serializable {

    @Id
    public long id;
    public String formId;

    public String formUri; /* odk content uri, its different from file path */
    @Unique
    public String formXmlPath;
    public String formInstanceName;
    public Date formLastUpdatedDate;

    public boolean formGroupCollected = false;
    @Index
    public String formGroupId;
    public String formGroupName;
    @Index
    public String formGroupInstanceUuid;

    @Index
    @Convert(converter = StringCollectionConverter.class, dbType = String.class)
    public Set<String> formModules;
    public String formModulesAsText;

    @Index
    public long visitId; /* will be different from zero if the form was collected during a DSS Household Visit*/
    @Index
    public long recordId; /*should be changed to recordCode - to survive synchronizations */

    @Convert(converter = SubjectEntityConverter.class, dbType = String.class)
    public SubjectEntity recordEntity;
    @Index
    public String collectedId;

    public String collectedBy;
    public String updatedBy;
    public String supervisedBy;
    public boolean supervised;

    public CollectedData() {
        this.formModules = new HashSet<>();
    }

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

    public long getRecordId() {
        return recordId;
    }

    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }

    public SubjectEntity getRecordEntity() {
        return recordEntity;
    }

    public void setRecordEntity(SubjectEntity recordEntity) {
        this.recordEntity = recordEntity;
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

    public Set<String> getFormModules() {
        return formModules;
    }

    public void setFormModules(Collection<? extends String> formModules) {
        this.formModules.addAll(formModules);
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
