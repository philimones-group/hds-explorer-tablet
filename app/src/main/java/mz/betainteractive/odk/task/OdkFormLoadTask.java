package mz.betainteractive.odk.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.FormsProviderAPI;
import mz.betainteractive.odk.InstanceProviderAPI;
import mz.betainteractive.odk.listener.OdkFormLoadListener;
import mz.betainteractive.odk.model.FilledForm;
import mz.betainteractive.odk.model.OdkFormLoadData;
import mz.betainteractive.odk.storage.access.OdkScopedDirUtil;
import mz.betainteractive.odk.storage.access.OdkStorageType;
import mz.betainteractive.odk.xml.OdkColumnsPreloader;
import mz.betainteractive.odk.xml.XFormDef;
import mz.betainteractive.odk.xml.XMLFinder;

import org.philimone.hds.explorer.database.Bootstrap;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
//import android.support.v4.app.ActivityCompat;
import android.os.Build;
import android.util.Log;


public class OdkFormLoadTask extends AsyncTask<Void, Void, OdkFormLoadResult> {

    private FormUtilities formUtilities;
    private OdkFormLoadListener listener;
    private ContentResolver resolver;
    private Uri odkContentUri;
    private String savedInstanceUri;
    private OdkFormLoadData odkFormLoadData;
    private FilledForm filledForm;
    private boolean openingSavedUri;
    private Context mContext;

    OdkScopedDirUtil.OdkFormObject foundFormObject;

    private OdkFormLoadTask(FormUtilities formUtilities) {
        this.formUtilities = formUtilities;
        this.mContext = formUtilities.getContext();
    }

    public OdkFormLoadTask(FormUtilities formUtilities, OdkFormLoadData loadData, OdkFormLoadListener listener) {
        this(formUtilities);
        this.listener = listener;
        this.resolver = mContext.getContentResolver();
        this.odkFormLoadData = loadData;
        this.filledForm = loadData.preloadedData;
    }

    public OdkFormLoadTask(FormUtilities formUtilities, OdkFormLoadData loadData, Uri uri, OdkFormLoadListener listener) { //used to open pre-existing collected forms and filling out auto-filled columns
        this(formUtilities);
        this.listener = listener;
        this.resolver = mContext.getContentResolver();
        this.odkContentUri = uri;
        this.savedInstanceUri = loadData.formInstanceUri;
        this.odkFormLoadData = loadData;
        this.filledForm = loadData.preloadedData;
        this.openingSavedUri = true;
    }

    @Override
    protected OdkFormLoadResult doInBackground(Void... params) {

        Log.d("loading-odk", "starting");

        /* finding odk form on odk database */
        String jrFormId = null;
        String jrFormName = null;
        String formFilePath = null;
        String formAbsoluteFilePath = null;
        String formVersion = null;
        OdkFormLoadResult.OpenMode openMode = openingSavedUri ? OdkFormLoadResult.OpenMode.EDIT_ODK_INSTANCE : OdkFormLoadResult.OpenMode.NEW_ODK_INSTANCE;
        Cursor cursor = null;

        if (openingSavedUri) {
            Log.d("openingsaveduri", "true");
            return OdkFormLoadResult.newEditSuccessResult(this.odkFormLoadData, this.odkContentUri, this.savedInstanceUri);
        }

        Log.d("get cursor for forms", "");

        int errorCount = 0;
        String messageLike = "AppDependencyComponent.inject(org.odk.collect.android.external.FormsProvider)' on a null object reference";
        String errorMessage = null;

        do {
            errorMessage = null;
            try {
                cursor = getCursorForFormsProvider(filledForm.getFormName());
            } catch (Exception e) {
                errorCount++;
                errorMessage = e.getMessage();
                Log.d("special error", "errorCount="+ errorCount + ", message: " + e.getMessage());

            }
        } while (errorMessage != null && errorMessage.contains(messageLike) && errorCount < 3);

        if (errorMessage != null) {
            return OdkFormLoadResult.newErrorResult(this.odkFormLoadData, OdkFormLoadResult.Status.ERROR_PROVIDER_NA, openMode);
        }

        //After trying to get the cursor
        if (cursor != null) {
            if (cursor.moveToNext()) {
                int formIdIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID);
                int formNameIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME);
                int formPathIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_FILE_PATH);
                int formVersionIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_VERSION);

                jrFormId = cursor.getString(formIdIndex);
                jrFormName = cursor.getString(formNameIndex);
                formFilePath = cursor.getString(formPathIndex);
                formVersion = cursor.getString(formVersionIndex);
            }

            cursor.close();
        } else {
            //no content provider
            Log.d("odk content provider", "not available");
            return OdkFormLoadResult.newErrorResult(this.odkFormLoadData, OdkFormLoadResult.Status.ERROR_PROVIDER_NA, openMode);
        }


        if (jrFormId == null || formFilePath == null) {
            return OdkFormLoadResult.newErrorResult(this.odkFormLoadData, OdkFormLoadResult.Status.ERROR_FORM_NOT_FOUND, openMode);
        }

        //ANDROID 11+ - ITS NOT POSSIBLE TO GET PRIVATE DIRECTORY

        Log.d("odk storage type", formUtilities.getOdkStorageType()+", ffpath: "+formFilePath+", exists="+new File(formFilePath).exists());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //ANDROID 11+ - Accessing Scoped and Shared Folder using SAF

            OdkScopedDirUtil odkScopedDirUtil = formUtilities.getOdkScopedDirUtil();

            if (formUtilities.getOdkStorageType() == OdkStorageType.ODK_SHARED_FOLDER) {
                //In Android 11 we must access the file using SAF - direct file path through File API doesnt work without MANAGE ALL FILES PERMISSION
                //To be able to find the file just get the filename
                formAbsoluteFilePath = formFilePath; //save the full file path
                formFilePath = new File(formFilePath).getName();
            }

            if (odkScopedDirUtil != null) {
                foundFormObject = odkScopedDirUtil.findBlankForm(formFilePath);

                if (foundFormObject == null) {
                    return OdkFormLoadResult.newErrorResult(this.odkFormLoadData, OdkFormLoadResult.Status.ERROR_FORM_NOT_FOUND, openMode);
                }
                //the blank form was found
            } else {
                return OdkFormLoadResult.newErrorResult(this.odkFormLoadData, OdkFormLoadResult.Status.ERROR_ODK_FOLDER_PERMISSION_DENIED, openMode);
            }
        } else {
            //ANDROID 10- - Uses File.API

            if (formUtilities.getOdkStorageType() != OdkStorageType.ODK_SHARED_FOLDER) {
                //WHEN USING A SCOPED FOLDER

                //the absolute file path is not found -> try to find using ODK Collect App base Path
                SearchFormResult searchResult = findOdkFormOnScopedDir(formFilePath);

                if (searchResult.status == SearchStatus.NOT_FOUND) {
                    return OdkFormLoadResult.newErrorResult(this.odkFormLoadData, OdkFormLoadResult.Status.ERROR_FORM_NOT_FOUND, openMode);
                }

                if (searchResult.status == SearchStatus.PERMISSION_DENIED) {
                    return OdkFormLoadResult.newErrorResult(this.odkFormLoadData, OdkFormLoadResult.Status.ERROR_ODK_FOLDER_PERMISSION_DENIED, openMode);
                }

                //The file was found
                File formFile = searchResult.formFile;
                Log.d("correct-odk-file", "for=" + formFile.getAbsolutePath() + ", found=" + (formFile));

                if (formFile != null) {
                    formFilePath = formFile.getAbsolutePath();
                } else {
                    return OdkFormLoadResult.newErrorResult(this.odkFormLoadData, OdkFormLoadResult.Status.ERROR_ODK_FOLDER_PERMISSION_DENIED, openMode);
                }

                if (!new File(formFilePath).canRead()) {
                    return OdkFormLoadResult.newErrorResult(this.odkFormLoadData, OdkFormLoadResult.Status.ERROR_ODK_FOLDER_PERMISSION_DENIED, openMode);
                }
            } else {
                //formFilePath - already have the file location
            }
        }

        /* ends here - finding odk form on odk database */
        Log.d("loading forms", "form_id=" + jrFormId + ",ver=" + formVersion + ", path=" + formFilePath);

        if (openMode == OdkFormLoadResult.OpenMode.NEW_ODK_INSTANCE) {

            //**** Open a New ODK Form ****//
            OdkColumnsPreloader preloader = new OdkColumnsPreloader(this.formUtilities, filledForm);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                //We are using SAF to access folder

                String xml = preloader.generatePreloadedXml(jrFormId, formVersion, foundFormObject);
                //Log.d("created xml", xml);
                OdkScopedDirUtil.OdkFormInstance targetFile = createNewOdkFormInstanceFile(xml, jrFormName, foundFormObject);
                boolean writeFile = false;

                if (targetFile != null) {
                    writeFile = insertNewOdkFormInstance(targetFile, formAbsoluteFilePath, filledForm.getFormName(), jrFormId, formVersion);
                    if (!writeFile) {
                        return OdkFormLoadResult.newScopedDirErrorResult(this.odkFormLoadData, OdkFormLoadResult.Status.ERROR_ODK_INSERT_SAVED_INSTANCE, openMode, targetFile.getInstanceFile(), odkContentUri);
                    }
                } else {
                    return OdkFormLoadResult.newScopedDirErrorResult(this.odkFormLoadData, OdkFormLoadResult.Status.ERROR_ODK_CREATE_SAVED_INSTANCE_FILE, openMode, targetFile.getInstanceFile(), odkContentUri);
                }

                Log.d("finished", "creating file, write file: "+writeFile);
                return OdkFormLoadResult.newScopedDirSuccessResult(this.odkFormLoadData, openMode, targetFile.getInstanceFile(), odkContentUri);

            } else {

                //Android 10 and below we are using FILE API

                String xml = preloader.generatePreloadedXml(jrFormId, formVersion, formFilePath);
                File instanceFile = createNewOdkFormInstanceFile(xml, jrFormName, new File(formFilePath));
                boolean writeFile = false;

                if (instanceFile != null) {
                    writeFile = insertNewOdkFormInstance(instanceFile, filledForm.getFormName(), jrFormId, formVersion);
                    if (!writeFile) {
                        return OdkFormLoadResult.newFileApiErrorResult(this.odkFormLoadData, OdkFormLoadResult.Status.ERROR_ODK_INSERT_SAVED_INSTANCE, openMode, instanceFile, odkContentUri);
                    }
                } else {
                    return OdkFormLoadResult.newFileApiErrorResult(this.odkFormLoadData, OdkFormLoadResult.Status.ERROR_ODK_CREATE_SAVED_INSTANCE_FILE, openMode, null, odkContentUri);
                }

                Log.d("finished", "creating file");
                return OdkFormLoadResult.newSuccessResult(this.odkFormLoadData, OdkFormLoadResult.Status.SUCCESS, openMode, instanceFile, this.odkContentUri);
            }

        }

        return OdkFormLoadResult.newEditSuccessResult(this.odkFormLoadData, this.odkContentUri, savedInstanceUri);
    }

    @Override
    protected void onPostExecute(final OdkFormLoadResult result) {
        if (result.getStatus() == OdkFormLoadResult.Status.SUCCESS) {
            listener.onOdkFormLoadSuccess(result);
        } else {
            listener.onOdkFormLoadFailure(result);
        }
    }

    private Cursor getCursorForFormsProvider(String name) {
        return resolver.query(FormsProviderAPI.FormsColumns.CONTENT_URI, new String[]{
                        FormsProviderAPI.FormsColumns.JR_FORM_ID, FormsProviderAPI.FormsColumns.DISPLAY_NAME, FormsProviderAPI.FormsColumns.FORM_FILE_PATH, FormsProviderAPI.FormsColumns.JR_VERSION},
                FormsProviderAPI.FormsColumns.JR_FORM_ID + " like ?", new String[]{name + "%"}, null);
    }

    private SearchFormResult findOdkFormOnScopedDir(String formFilePath) {
        File formsDir = getOdkScopedStorageFormsDir();

        if (formsDir.exists() && formsDir.isDirectory() && formsDir.canRead()) {
            for (File ffile : formsDir.listFiles()) {
                if (ffile.isFile() && ffile.getName().equalsIgnoreCase(formFilePath)) {
                    return new SearchFormResult(ffile, SearchStatus.FOUND);
                }
            }

            return new SearchFormResult(null, SearchStatus.NOT_FOUND);

        } else {
            //the folder exists but couldnt read it
            return new SearchFormResult(null, SearchStatus.PERMISSION_DENIED);
        }

    }

    private File getOdkScopedStorageFormsDir() {
        String odkBasePath = Bootstrap.getOdkScopedStoragePath(mContext);
        File odkDir = new File(odkBasePath);
        File formFile = null;

        Log.d("odkDir", ""+odkBasePath+", exists="+odkDir.exists());
        if (odkDir.exists()) {

            if (formUtilities.getOdkStorageType() == OdkStorageType.ODK_SCOPED_FOLDER_PROJECTS) {
                File projectsDir = new File(odkBasePath + "projects");

                for (File projectSubDir : projectsDir.listFiles()) {
                    if (projectSubDir.isDirectory()) { //files/projects/ssad11-ss-1${ODK_PROJECT}plda5da-dadasd
                        File formsDir = new File(projectSubDir.getAbsolutePath() + File.separator + "forms");

                        Log.d("found odk-pform-sub-dir", ""+formsDir.toString());

                        if (formsDir.exists() && formsDir.isDirectory()) {
                            return formsDir;
                        }
                    }
                }

                return null;
            } else if (formUtilities.getOdkStorageType() == OdkStorageType.ODK_SCOPED_FOLDER_NO_PROJECTS) {
                File formsDir = new File(odkBasePath + "forms");;

                Log.d("found odk-forms-sub-dir", ""+formsDir.toString());

                if (formsDir.exists() && formsDir.isDirectory()) {
                    return formsDir;
                }
            }

        }

        return null;
    }

    private XFormDef findOdkForm(File formsDir, String formId){        
        //get xml files and read them
        for (File formFile : formsDir.listFiles()) {
            if (formFile.getName().toLowerCase().endsWith(".xml")) {
                //read odk xml
                XFormDef formDef = XMLFinder.getODKForm(formFile);

                if (formDef != null) {
                    Log.d("form found", formDef.formFilePath);

                    if (formDef.formId.equals(formId)) {                                              

                        Log.d("form really found", formId);

                        return formDef;
                    }
                }
            }
        }
        
        return null;
    }

    private String getXmlFilePath(Uri contentUri) {
        Cursor cursor = resolver.query(contentUri, new String[]{InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH}, null, null, null);

        String xmlFilePath = "";
        Log.d("Running check form", "");

        if (cursor.moveToNext()) {
            //Log.d("move next", ""+cursor.getString(0));
            xmlFilePath = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH)); //used to read the xml file
        } else {
            Log.d("move next", "couldnt find executed form");
        }

        try {
            cursor.close();
        } catch (Exception e) {
            System.err.println("Exception while trying to close cursor !");
            e.printStackTrace();
        }

        return xmlFilePath;
    }

    private File createNewOdkFormInstanceFile(String xml, String jrFormId, File blankFormFilePath) {
    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
        df.setTimeZone(TimeZone.getDefault());
        String date = df.format(new Date());

        String dirName = jrFormId + "_" + date;
        String fileName =  jrFormId + "_" + date + ".xml";

        File root = null;

        //get instance path from xml file
        File formsDir = blankFormFilePath.getParentFile();
        File odkDir = formsDir.getParentFile();
        String destinationPath = odkDir.getAbsolutePath() + File.separator + "instances" + File.separator + dirName + File.separator;

        File baseDir = new File(destinationPath);
        File targetFile = new File(destinationPath + fileName);

        if (!baseDir.exists()) {
            boolean created = baseDir.mkdirs();
            if (!created) {
                return null;
            }
        }

        if (!targetFile.exists()) {
            try {
                FileWriter writer = new FileWriter(targetFile);
                writer.write(xml);
                writer.close();
                return targetFile;
            } catch (IOException e) {
                return null;
            }
        }

        return null;
    }

    private OdkScopedDirUtil.OdkFormInstance createNewOdkFormInstanceFile(String xml, String jrFormId, OdkScopedDirUtil.OdkFormObject formObject) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
        df.setTimeZone(TimeZone.getDefault());
        String date = df.format(new Date());

        String dirName = jrFormId + "_" + date;
        String fileName =  jrFormId + "_" + date + ".xml";

        OdkScopedDirUtil.OdkFormInstance formInstance = formObject.createNewInstance(dirName, fileName);

        try {

            OutputStream outputStream = formInstance.getInstanceOutputStream();

            PrintStream output = new PrintStream(outputStream);
            output.print(xml);
            output.close();

            return formInstance;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    private boolean insertNewOdkFormInstance(File targetFile, String displayName, String formId, String formVersion) {

        ContentValues values = new ContentValues();
        values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, targetFile.getAbsolutePath());
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, displayName);
        values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, formId);

        if (formVersion != null){
            values.put(InstanceProviderAPI.InstanceColumns.JR_VERSION, formVersion);
        }

        odkContentUri = resolver.insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values);

        if (odkContentUri == null) {
            return false;
        }

        return true;
    }

    private boolean insertNewOdkFormInstance(OdkScopedDirUtil.OdkFormInstance targetFile, String blankFormFilePath, String displayName, String formId, String formVersion) {

        //formAbsoluteFilePath - is used with we are using a SHARED FOLDER, we must save the odk full path

        String instanceFilePath = targetFile.getInstanceRelativePath();
        if (formUtilities.getOdkStorageType() == OdkStorageType.ODK_SHARED_FOLDER) {
            File formsDir = new File(blankFormFilePath).getParentFile();
            File odkDir = formsDir.getParentFile();
            instanceFilePath = odkDir.getAbsolutePath() + File.separator + "instances" + File.separator + targetFile.getInstanceRelativePath();
        }


        ContentValues values = new ContentValues();
        values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, instanceFilePath);
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, displayName);
        values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, formId);

        if (formVersion != null){
            values.put(InstanceProviderAPI.InstanceColumns.JR_VERSION, formVersion);
        }

        odkContentUri = resolver.insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values);

        Log.d("saving new instance", ""+ odkContentUri);

        if (odkContentUri == null) {
            return false;
        }

        return true;
    }

    class SearchFormResult {
        public File formFile;
        public SearchStatus status;

        public SearchFormResult(File formFile, SearchStatus status) {
            this.formFile = formFile;
            this.status = status;
        }
    }

    enum SearchStatus {
        FOUND, NOT_FOUND, PERMISSION_DENIED
    }

}
