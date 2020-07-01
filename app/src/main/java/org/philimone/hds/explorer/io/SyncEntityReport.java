package org.philimone.hds.explorer.io;

public class SyncEntityReport {
    private SyncEntity entity;
    private String message;
    private String size;
    private boolean successStatus;

    public SyncEntityReport() {
    }

    public SyncEntityReport(String message, boolean status) {
        this.message = message;
        this.successStatus = status;
    }

    public SyncEntityReport(SyncEntity entity, String message, String size, boolean successStatus) {
        this.entity = entity;
        this.message = message;
        this.size = size;
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
