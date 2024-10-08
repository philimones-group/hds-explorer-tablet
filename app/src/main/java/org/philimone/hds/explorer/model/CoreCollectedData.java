package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.CoreFormEntityConverter;
import org.philimone.hds.explorer.model.converters.CoreFormRecordTypeConverter;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.CoreFormRecordType;

import java.util.Date;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToOne;

@Entity
public class CoreCollectedData {

    @Id
    public long id;

    @Convert(converter = CoreFormEntityConverter.class, dbType = String.class)
    public CoreFormEntity formEntity;

    @Convert(converter = CoreFormRecordTypeConverter.class, dbType = String.class)
    public CoreFormRecordType recordType = CoreFormRecordType.NEW_RECORD;

    public long visitId;

    public long formEntityId;

    public String formEntityCode;

    public String formEntityCodes; /* other affected objects */

    public String formEntityName;

    @Unique
    public String formUuid;

    public String formFilename;

    public Date createdDate;

    public Date updatedDate;

    public boolean uploaded;

    public Date    uploadedDate;

    public boolean uploadedWithError;

    public String  uploadedError;

    @Index
    public String collectedId;

    public ToOne<CoreFormExtension> extension;

    public boolean extensionCollected;

    public String extensionCollectedUri;

    public String extensionCollectedFilepath;

    public CoreCollectedData() {
        this.createdDate = new Date();
    }

    public CoreCollectedData(CoreFormEntity formEntity) {
        this();
        this.formEntity = formEntity;
    }

    public CoreFormEntity getFormEntity() {
        return formEntity;
    }
}
