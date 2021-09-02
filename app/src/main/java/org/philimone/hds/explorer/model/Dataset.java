package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.LabelMappingConverter;
import org.philimone.hds.explorer.model.converters.StringCollectionConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import mz.betainteractive.utilities.StringUtil;

@Entity
public class Dataset implements Serializable {

    @Id
    public long id;
    public String datasetId;
    public String name;
    public String label;
    public String keyColumn;
    public String tableName;
    public String tableColumn;
    public String tableColumnLabels;
    public String filename;

    //public boolean enabled = false;

    public String createdBy;
    public String creationDate;
    public String updatedBy;
    public String updatedDate;

    @Index
    @Convert(converter = StringCollectionConverter.class, dbType = String.class)
    public Set<String> modules;

    public Dataset() {
        this.modules = new HashSet<>();
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

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
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

    public List<String> getLabels(){
        String[] spt = this.tableColumnLabels.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        List<String> list = new ArrayList<>();

        for (String label : spt) {
            list.add(StringUtil.removeQuotes(label));
        }

        return list;
    }

    public void setModules(Collection<? extends String> modules) {
        this.modules.addAll(modules);
    }

    @Override
    public String toString() {
        return this.label;
    }
}
