package mz.betainteractive.odk.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.FormsProviderAPI;
import mz.betainteractive.odk.InstanceProviderAPI;
import mz.betainteractive.odk.listener.OdkFormLoadListener;
import mz.betainteractive.odk.model.FilledForm;
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
import android.util.Log;


public class OdkFormLoadTask extends AsyncTask<Void, Void, OdkFormLoadResult> {

    private static String FORMS_PATH = "org.philimone.hds.explorer";

    private FormUtilities formUtilities;
    private OdkFormLoadListener listener;
    private ContentResolver resolver;
    private Uri odkUri;
    private FilledForm filledForm;
    private boolean openingSavedUri;
    private Context mContext;

    private OdkFormLoadTask(FormUtilities formUtilities) {
        this.formUtilities = formUtilities;
        this.mContext = formUtilities.getContext();
    }

    public OdkFormLoadTask(FormUtilities formUtilities, FilledForm filledForm, OdkFormLoadListener listener) {
        this(formUtilities);
        this.listener = listener;
        this.resolver = mContext.getContentResolver();
        this.filledForm = filledForm;

    }

    public OdkFormLoadTask(FormUtilities formUtilities, FilledForm filledForm, Uri uri, OdkFormLoadListener listener) { //used to open pre-existing collected forms and filling out auto-filled columns
        this(formUtilities);
        this.listener = listener;
        this.resolver = mContext.getContentResolver();
        this.odkUri = uri;
        this.filledForm = filledForm;
        this.openingSavedUri = true;
    }

    @Override
    protected OdkFormLoadResult doInBackground(Void... params) {

        Log.d("loading-odk", "starting");

        /* finding odk form on odk database */
        String jrFormId = null;
        String jrFormName = null;
        String formFilePath = null;
        String formVersion = null;
        OdkFormLoadResult.OpenMode openMode = openingSavedUri ? OdkFormLoadResult.OpenMode.EDIT_ODK_INSTANCE : OdkFormLoadResult.OpenMode.NEW_ODK_INSTANCE;
        String savedFormFilePath = openingSavedUri ? getXmlFilePath(odkUri) : null;

        Cursor cursor = null;

        cursor = getCursorForFormsProvider(filledForm.getFormName());

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
            return new OdkFormLoadResult(OdkFormLoadResult.Status.ERROR_PROVIDER_NA, openMode, null, null);
        }

        if (jrFormId == null || formFilePath == null) {
            return new OdkFormLoadResult(OdkFormLoadResult.Status.ERROR_FORM_NOT_FOUND, openMode, null, null);
        }

        //ANDROID 11+ - ITS NOT POSSIBLE TO GET PRIVATE DIRECTORY

        boolean hasPrivateOdkStorage = !new File(formFilePath).exists() && !formFilePath.startsWith("/");
        Log.d("odk with private folder", hasPrivateOdkStorage+", ffpath: "+formFilePath+", exists="+new File(formFilePath).exists());

        if (hasPrivateOdkStorage) {
            //the absolute file path is not found -> try to find using ODK Collect App base Path
            SearchFormResult searchResult = findOdkFormOnScopedDir(formFilePath);

            if (searchResult.status == SearchStatus.NOT_FOUND) {
                return new OdkFormLoadResult(OdkFormLoadResult.Status.ERROR_FORM_NOT_FOUND, openMode, null, null);
            }

            if (searchResult.status == SearchStatus.PERMISSION_DENIED) {
                return new OdkFormLoadResult(OdkFormLoadResult.Status.ERROR_ODK_FOLDER_PERMISSION_DENIED, openMode, null, null);
            }


            //The file was found

            File formFile = searchResult.formFile;
            Log.d("correct-odk-file", "for=" + formFile.getAbsolutePath() + ", found="+(formFile));

            if (formFile != null) {
                formFilePath = formFile.getAbsolutePath();
            } else {
                return new OdkFormLoadResult(OdkFormLoadResult.Status.ERROR_ODK_FOLDER_PERMISSION_DENIED, openMode, null, null);
            }
        }

        if (!new File(formFilePath).canRead()) {
            return new OdkFormLoadResult(OdkFormLoadResult.Status.ERROR_ODK_FOLDER_PERMISSION_DENIED, openMode, null, null);
        }

        /* ends here - finding odk form on odk database */
        Log.d("loading forms", "form_id=" + jrFormId + ",ver=" + formVersion + ", path=" + formFilePath);

        //request permission for reading device id

        if (openMode == OdkFormLoadResult.OpenMode.NEW_ODK_INSTANCE) {

            //**** Open a New ODK Form ****//
            OdkColumnsPreloader preloader = new OdkColumnsPreloader(this.formUtilities, filledForm);
            String xml = preloader.generatePreloadedXml(jrFormId, formVersion, formFilePath);
            File targetFile = createNewOdkFormInstanceFile(xml, jrFormName, new File(formFilePath));
            boolean writeFile = false;
            Log.d("xml", xml);
            if (targetFile != null) {
                writeFile = insertNewOdkFormInstance(targetFile, filledForm.getFormName(), jrFormId, formVersion);

                if (!writeFile) {
                    return new OdkFormLoadResult(OdkFormLoadResult.Status.ERROR_ODK_INSERT_SAVED_INSTANCE, openMode, targetFile, null);
                }


            } else {
                return new OdkFormLoadResult(OdkFormLoadResult.Status.ERROR_ODK_CREATE_SAVED_INSTANCE_FILE, openMode, targetFile, null);
            }

            Log.d("finished", "creating file");

            return new OdkFormLoadResult(OdkFormLoadResult.Status.SUCCESS, openMode, targetFile, null);
        }

        return new OdkFormLoadResult(OdkFormLoadResult.Status.SUCCESS, openMode, null, null);
    }

    @Override
    protected void onPostExecute(final OdkFormLoadResult result) {
        if (result.getStatus() == OdkFormLoadResult.Status.SUCCESS) {
            listener.onOdkFormLoadSuccess(odkUri);
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
            for (File projectSubDir : odkDir.listFiles()) {
                if (projectSubDir.isDirectory()) { //files/projects/ssad11-ss-1${ODK_PROJECT}plda5da-dadasd
                    File formsDir = new File(projectSubDir.getAbsolutePath() + File.separator + "forms");
                    File instancesDir = new File(projectSubDir.getAbsolutePath() + File.separator + "instances");

                    Log.d("found odk-sub-dir", ""+formsDir.toString());

                    if (formsDir.exists() && formsDir.isDirectory()) {

                        return formsDir;
                    }
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

    private boolean insertNewOdkFormInstance(File targetFile, String displayName, String formId, String formVersion) {

        ContentValues values = new ContentValues();
        values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, targetFile.getAbsolutePath());
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, displayName);
        values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, formId);

        if (formVersion != null){
            values.put(InstanceProviderAPI.InstanceColumns.JR_VERSION, formVersion);
        }

        odkUri = resolver.insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values);

        if (odkUri == null) {
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
