package mz.betainteractive.odk.storage.access;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import mz.betainteractive.odk.storage.access.anthonymandra.framework.XDocumentFile;

public class OdkScopedDirUtil {

    public static final String EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents";
    public static final String ODK_SHARED_FOLDER_URI = "primary:odk";
    public static final String ODK_SCOPED_FOLDER_URI = "primary:Android/data/org.odk.collect.android/files";

    public static String PRIMARY_ANDROID_DOC_ID = "primary:Android/data/org.odk.collect.android/files";

    private Context mContext;
    private ContentResolver contentResolver;
    private OdkStorageType odkStorageType;
    private Uri odkDirectoryUri;

    public OdkScopedDirUtil(Context mContext, OdkStorageType odkStorageType) {
        this.mContext = mContext;
        this.contentResolver = mContext.getContentResolver();
        this.odkStorageType = odkStorageType;

        PRIMARY_ANDROID_DOC_ID = odkStorageType==OdkStorageType.ODK_SHARED_FOLDER ? ODK_SHARED_FOLDER_URI : ODK_SCOPED_FOLDER_URI;

        this.odkDirectoryUri = DocumentsContract.buildTreeDocumentUri(EXTERNAL_STORAGE_PROVIDER_AUTHORITY, PRIMARY_ANDROID_DOC_ID);
    }

    public OdkFormObject findBlankForm(String formName) {

        Uri odkFilesUri = odkDirectoryUri; //DocumentsContract.buildChildDocumentsUriUsingTree(androidDirectoryUri, PRIMARY_ANDROID_DOC_ID);//ODK_SCOPED_DIR_DOC_ID);

        XDocumentFile odkFilesDocFile = XDocumentFile.fromUri(mContext, odkFilesUri);

        if (odkStorageType == OdkStorageType.ODK_SHARED_FOLDER || odkStorageType == OdkStorageType.ODK_SCOPED_FOLDER_NO_PROJECTS) {
            Log.d("odk directory", "doesnt contains projects - its shared or scoped folder");
            //get forms folder and file the form
            return findBlankFormInDirectory(odkFilesDocFile, formName);
        }

        if (odkStorageType == OdkStorageType.ODK_SCOPED_FOLDER_PROJECTS) {
            Log.d("odk directory", "scoped folder that contains projects");

            XDocumentFile projectsDocFile = odkFilesDocFile.findFile("projects");

            //get all projects and try to find the blank form - in the future try to get project name setup in settings
            //projects -> project_name -> odk blank form
            for (XDocumentFile projectFile : projectsDocFile.listFiles()) {
                //get forms folder and file the form
                if (projectFile.isDirectory()) {
                    OdkFormObject foundObj = findBlankFormInDirectory(projectFile, formName);
                    if (foundObj != null) return foundObj;
                }
            }
        }

        Log.d("if came here", "the blank form ("+formName+") wasnt found");

        return null;
    }

    private OdkFormObject findBlankFormInDirectory(XDocumentFile odkRootDocFile, String formName) {
        //get forms folder and file the form
        XDocumentFile formsDocFile = odkRootDocFile.findFile("forms");
        XDocumentFile instancesDocFile = odkRootDocFile.findFile("instances");

        if (formsDocFile != null && formsDocFile.isDirectory()) {
            XDocumentFile blankFormDocFile = formsDocFile.findFile(formName);

            if (blankFormDocFile != null) {
                return new OdkFormObject(blankFormDocFile, instancesDocFile);
            }
        }

        return null;
    }

    private OdkFormInstance createNewInstance(OdkFormObject formObject, String directoryName, String fileName) {

        XDocumentFile instancesDirFile = formObject.getInstancesDirectoryFile();
        XDocumentFile newInstanceSubDir = instancesDirFile.createDirectory(directoryName);

        Log.d("new instance dir file", newInstanceSubDir.getUri()+"");

        if (newInstanceSubDir != null) {
            XDocumentFile newInstanceFile = newInstanceSubDir.createFile("text/xml", fileName);

            Log.d("dir file uri", newInstanceFile.getUri()+"");

            return new OdkFormInstance(formObject, newInstanceFile, directoryName + File.separator + fileName);
        }

        return null;
    }

    public class OdkFormObject {
        private XDocumentFile formFile;
        private XDocumentFile instancesDirectoryFile;

        public OdkFormObject(XDocumentFile formUri, XDocumentFile instancesDirectoryUri) {
            this.formFile = formUri;
            this.instancesDirectoryFile = instancesDirectoryUri;
        }

        public XDocumentFile getFormFile() {
            return formFile;
        }

        public XDocumentFile getInstancesDirectoryFile() {
            return instancesDirectoryFile;
        }

        public Uri getFormUri() {
            return formFile.getUri();
        }

        public Uri getInstancesDirectoryUri() {
            return instancesDirectoryFile.getUri();
        }

        public InputStream getFormInputStream() throws FileNotFoundException {
            return contentResolver.openInputStream(getFormUri());
        }

        public OdkFormInstance createNewInstance(String directoryName, String fileName) {

            XDocumentFile instancesDirFile = this.getInstancesDirectoryFile();
            XDocumentFile newInstanceSubDir = instancesDirFile.createDirectory(directoryName);

            Log.d("new instance dir file", newInstanceSubDir.getUri()+"");

            if (newInstanceSubDir != null) {
                XDocumentFile newInstanceFile = newInstanceSubDir.createFile("text/xml", fileName);

                Log.d("dir file uri", newInstanceFile.getUri()+"");

                return new OdkFormInstance(this, newInstanceFile, directoryName + File.separator + fileName);
            }

            return null;
        }

    }

    public class OdkFormInstance {
        private OdkFormObject formObject;
        private XDocumentFile instanceFile;
        private String instanceRelativePath;

        public OdkFormInstance(OdkFormObject formObject, XDocumentFile instanceFile, String instanceRelativePath) {
            this.formObject = formObject;
            this.instanceFile = instanceFile;
            this.instanceRelativePath = instanceRelativePath;
        }

        public OdkFormObject getFormObject() {
            return formObject;
        }

        public XDocumentFile getInstanceFile() {
            return instanceFile;
        }

        public String getInstanceRelativePath() {
            return instanceRelativePath;
        }

        public OutputStream getInstanceOutputStream() throws FileNotFoundException {
            return contentResolver.openOutputStream(instanceFile.getUri());
        }
    }
}
