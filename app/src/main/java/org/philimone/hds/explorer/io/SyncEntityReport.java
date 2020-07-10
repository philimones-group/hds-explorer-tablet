package org.philimone.hds.explorer.io;

import java.io.Serializable;

public class SyncEntityReport implements Serializable {
    private SyncEntity entity;
    private String message;
    private String size;
    private boolean successStatus;
    private String errorMessage;

    public SyncEntityReport() {
    }

    public SyncEntityReport(String message, boolean status) {
        this.message = message;
        this.successStatus = status;
    }

    public SyncEntityReport(SyncEntity entity, String message, String size, String errorMessage, boolean successStatus) {
        this.entity = entity;
        this.message = message;
        this.size = size;
        this.errorMessage = errorMessage;
        this.successStatus = successStatus;
    }


    public SyncEntity getEntity() {
        return entity;
    }

    public void setEntity(SyncEntity entity) {
        this.entity = entity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSize() {
        return size;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public boolean isSuccessStatus() {
        return successStatus;
    }

    public void setSuccessStatus(boolean successStatus) {
        this.successStatus = successStatus;
    }
}
