package org.philimone.hds.explorer.io;

import org.philimone.hds.explorer.model.enums.CoreFormEntity;

import java.io.Serializable;

public class UploadEntityReport implements Serializable {
    private CoreFormEntity entity;
    private String message;
    private boolean successStatus;

    public UploadEntityReport(CoreFormEntity entity, String message, boolean successStatus) {
        this.entity = entity;
        this.message = message;
        this.successStatus = successStatus;
    }


    public CoreFormEntity getEntity() {
        return entity;
    }

    public void setEntity(CoreFormEntity entity) {
        this.entity = entity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccessStatus() {
        return successStatus;
    }

    public void setSuccessStatus(boolean successStatus) {
        this.successStatus = successStatus;
    }
}
