package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.CoreFormEntityConverter;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;

import java.util.Date;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class CoreCollectedData {

    @Id
    public long id;

    @Convert(converter = CoreFormEntityConverter.class, dbType = String.class)
    public CoreFormEntity formEntity;

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

    public CoreCollectedData() {
        this.createdDate = new Date();
    }
}
