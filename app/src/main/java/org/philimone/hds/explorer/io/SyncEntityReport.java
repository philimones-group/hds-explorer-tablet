package org.philimone.hds.explorer.io;

public class SyncEntityReport {
    private String message;
    private boolean successStatus;

    public SyncEntityReport() {
    }

    public SyncEntityReport(String message, boolean status) {
        this.message = message;
        this.successStatus = status;
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
