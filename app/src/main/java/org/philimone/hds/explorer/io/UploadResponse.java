package org.philimone.hds.explorer.io;

public class UploadResponse {
    boolean success;
    boolean uploaded;
    String errors;

    public UploadResponse(boolean success, boolean uploaded, String errors) {
        this.success = success;
        this.errors = errors;
    }

    public static UploadResponse createSuccessfullExecution() {
        return new UploadResponse(true, true, "");
    }

    public static UploadResponse createNotSuccessfullExecution(boolean uploaded, String errors) {
        return new UploadResponse(false, uploaded, errors);
    }

    public boolean hasSuccessfullyExecuted() {
        return success;
    }

    public boolean hasErrors() {
        return !success;
    }

    public String[] getErrorsSplitted() {
        return errors.split("\n");
    }

    public String getErrors() {
        return errors;
    }

    public boolean hasUploaded() {
        return uploaded;
    }
}
