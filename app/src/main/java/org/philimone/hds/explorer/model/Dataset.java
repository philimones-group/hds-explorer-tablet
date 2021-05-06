package org.philimone.hds.explorer.model;

import android.content.ContentValues;

import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;
import org.philimone.hds.explorer.model.converters.LabelMappingConverter;
import org.philimone.hds.explorer.model.converters.SyncStatusConverter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Dataset implements Serializable {

    @Id
    public long id;
    public String datasetId;
    public String name;
    public String keyColumn;
    public String tableNameField;
    public String tableColumn;
    public String filename;

    //public boolean enabled = false;

    public String createdBy;
    public String creationDate;
    public String updatedBy;
    public String updatedDate;
    @Convert(converter = LabelMappingConverter.class, dbType = String.class)
    public Map<String, String> labels;

    public Dataset() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDatasetId() {
        return datasetId;
    }
    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyColumn() {
        return keyColumn;
    }

    public void setKeyColumn(String keyColumn) {
        this.keyColumn = keyColumn;
    }

    public void setTableNameField(String tableName) {
        this.tableNameField = tableName;
    }

    public String getTableNameField() {
        return tableNameField;
    }

    public String getTableColumn() {
        return tableColumn;
    }

    public void setTableColumn(String tableColumn) {
        this.tableColumn = tableColumn;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public void setLabels(Map<String, String> labels){
        this.labels = labels;
    }

    public Map<String, String> getLabels(){
        return this.labels;
    }

    public String getTableName() {
        return "dataset";
    }

}
