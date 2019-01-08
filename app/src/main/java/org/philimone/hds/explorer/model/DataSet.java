package org.philimone.hds.explorer.model;

import android.content.ContentValues;

import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataSet  implements Table, Serializable {

    private int id;
    private int datasetId;
    private String name;
    private String keyColumn;
    private String tableNameField;
    private String tableColumn;
    private String filename;

    //private boolean enabled = false;

    private String createdBy;
    private String creationDate;
    private String updatedBy;
    private String updatedDate;

    private String labelsText;
    private Map<String, String> labels;

    public DataSet() {
        labels = new LinkedHashMap<String, String>();
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(int datasetId) {
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

    public void setLabels(String labelsText){
        this.labelsText = labelsText;
        convertLabelsTextToMap();
    }

    public Map<String, String> getLabels(){
        return this.labels;
    }

    private void convertLabelsTextToMap() {
        if (labelsText != null && !labelsText.isEmpty()){
            this.labels.clear();

            String[] entries = labelsText.split(";");
            for (String entry : entries){
                String[] keyValue = entry.split(":");
                if (keyValue.length == 2){
                    this.labels.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }

    @Override
    public String getTableName() {
        return DatabaseHelper.DataSet.TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        
        cv.put(DatabaseHelper.DataSet.COLUMN_DATASET_ID, datasetId);
        cv.put(DatabaseHelper.DataSet.COLUMN_NAME, name);
        cv.put(DatabaseHelper.DataSet.COLUMN_KEYCOLUMN, keyColumn);
        cv.put(DatabaseHelper.DataSet.COLUMN_TABLE_NAME, tableNameField);
        cv.put(DatabaseHelper.DataSet.COLUMN_TABLE_COLUMN, tableColumn);
        cv.put(DatabaseHelper.DataSet.COLUMN_FILENAME, filename);
        cv.put(DatabaseHelper.DataSet.COLUMN_CREATED_BY, createdBy);
        cv.put(DatabaseHelper.DataSet.COLUMN_CREATION_DATE, creationDate);
        cv.put(DatabaseHelper.DataSet.COLUMN_UPDATED_BY, updatedBy);
        cv.put(DatabaseHelper.DataSet.COLUMN_UPDATED_DATE, updatedDate);
        cv.put(DatabaseHelper.DataSet.COLUMN_LABELS, labelsText);
        
        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.DataSet.ALL_COLUMNS;
    }
}
