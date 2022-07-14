package mz.betainteractive.odk.task;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.io.File;

public class OdkFormLoadResult {

    private Status status;
    private OpenMode odkOpenMode;
    private File instanceFile;
    private @StringRes Integer messageText;

    public OdkFormLoadResult(Status status, OpenMode openMode, File instanceFile, @Nullable Integer messageText) {
        this.status = status;
        this.odkOpenMode = openMode;
        this.instanceFile = instanceFile;
        this.messageText = messageText;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public OpenMode getOdkOpenMode() {
        return odkOpenMode;
    }

    public void setOdkOpenMode(OpenMode odkOpenMode) {
        this.odkOpenMode = odkOpenMode;
    }

    public File getInstanceFile() {
        return instanceFile;
    }

    public void setInstanceFile(File instanceFile) {
        this.instanceFile = instanceFile;
    }

    public Integer getMessageText() {
        return messageText;
    }

    public void setMessageText(Integer messageText) {
        this.messageText = messageText;
    }

    public enum Status {
        SUCCESS,
        ERROR_PROVIDER_NA,
        ERROR_FORM_NOT_FOUND,
        ERROR_ODK_FOLDER_PERMISSION_DENIED,
        ERROR_ODK_CREATE_SAVED_INSTANCE_FILE,
        ERROR_ODK_INSERT_SAVED_INSTANCE
    }

    public enum OpenMode {
        NEW_ODK_INSTANCE,
        EDIT_ODK_INSTANCE
    }
}
