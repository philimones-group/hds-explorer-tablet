package mz.betainteractive.odk.task;

import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.io.File;

import mz.betainteractive.odk.model.OdkFormLoadData;
import mz.betainteractive.odk.storage.access.anthonymandra.framework.XDocumentFile;

public class OdkFormLoadResult {

    private Status status;
    private OpenMode odkOpenMode;
    private OdkFormLoadData odkFormLoadData;
    private Uri contentUri;
    private String instanceUri;
    private File instanceFile;
    private XDocumentFile instanceDocumentFile;
    private @StringRes Integer messageText;

    public OdkFormLoadResult(){

    }

    public OdkFormLoadResult(OdkFormLoadData odkFormLoadData, Status status, OpenMode openMode, File instanceFile, @Nullable Integer messageText) {
        this.odkFormLoadData = odkFormLoadData;
        this.status = status;
        this.odkOpenMode = openMode;
        this.instanceFile = instanceFile;
        this.messageText = messageText;
    }

    public static OdkFormLoadResult newInstance(OdkFormLoadData odkFormLoadData, Status status, OpenMode openMode, XDocumentFile instanceFile, Uri instanceContentUri, @Nullable Integer messageText) {
        OdkFormLoadResult result = new OdkFormLoadResult();
        result.odkFormLoadData = odkFormLoadData;
        result.status = status;
        result.odkOpenMode = openMode;
        result.instanceDocumentFile = instanceFile;
        result.contentUri = instanceContentUri;
        result.messageText = messageText;

        return result;
    }

    public OdkFormLoadResult(OdkFormLoadData odkFormLoadData, Status status, OpenMode openMode, Uri instanceContentUri) {
        this.odkFormLoadData = odkFormLoadData;
        this.status = status;
        this.odkOpenMode = openMode;
        this.contentUri = instanceContentUri;        ;
    }

    public OdkFormLoadResult(OdkFormLoadData odkFormLoadData, Status status, OpenMode openMode, Uri instanceContentUri, String instanceUri, String hahahaha) {
        this.odkFormLoadData = odkFormLoadData;
        this.status = status;
        this.odkOpenMode = openMode;
        this.contentUri = instanceContentUri;
        this.instanceUri = instanceUri;
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

    public Uri getContentUri() {
        return contentUri;
    }

    public File getInstanceFile() {
        return instanceFile;
    }

    public void setInstanceFile(File instanceFile) {
        this.instanceFile = instanceFile;
    }

    public XDocumentFile getInstanceDocumentFile() {
        return instanceDocumentFile;
    }

    public void setInstanceDocumentFile(XDocumentFile instanceDocumentFile) {
        this.instanceDocumentFile = instanceDocumentFile;
    }

    public Integer getMessageText() {
        return messageText;
    }

    public void setMessageText(Integer messageText) {
        this.messageText = messageText;
    }

    public String getInstanceUri() {
        if (instanceUri != null) {
            return instanceUri;
        } else if (instanceDocumentFile != null) {
            return instanceDocumentFile.getUri().toString();
        } else if (instanceFile != null) {
            return instanceFile.getAbsolutePath();
        }

        return null;
    }

    public enum Status {
        SUCCESS,
        ERROR_NO_ODK_INSTALLED,
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
