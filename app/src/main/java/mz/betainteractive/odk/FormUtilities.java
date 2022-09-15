package mz.betainteractive.odk;

import static android.content.Context.STORAGE_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.widget.DialogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import mz.betainteractive.odk.listener.OdkFormLoadListener;
import mz.betainteractive.odk.listener.OdkFormResultListener;
import mz.betainteractive.odk.model.OdkFormLoadData;
import mz.betainteractive.odk.storage.access.OdkScopedDirUtil;
import mz.betainteractive.odk.storage.access.OdkStorageType;
import mz.betainteractive.odk.task.OdkFormLoadResult;
import mz.betainteractive.odk.task.OdkFormLoadTask;

public class FormUtilities {
    public static final int SELECTED_ODK_FORM = 51;
    public static final int SELECTED_ODK_REOPEN = 52;

    private Context mContext;
    private Fragment fragment;
    private AppCompatActivity activity;
    private OdkFormLoadData formLoadData;
	private Uri contentUri; /* odk CONTENT_URI of every instance */
    private String instanceUri; /* odk instance file uri */
    private boolean formUnFinished;
    private String xmlFilePath;
    private OdkFormResultListener formResultListener;

    private String metaInstanceName;
    private Date lastUpdatedDate;

    private String formId;
    private String deviceId;

    private OdkStorageType odkStorageType;
    private OdkScopedDirUtil odkScopedDirUtil;

    private ActivityResultLauncher<String> requestPermissionRpState;
    private ActivityResultLauncher<String[]> requestPermissionsReadWrite;
    private ActivityResultLauncher<Intent> odkResultLauncher;
    private ActivityResultLauncher<Intent> requestManageAllLauncher;
    private ActivityResultLauncher<Intent> requestAccessAndroidDirLauncher;
    private OdkFormLoadTask currentLoadTask;
    private OnPermissionRequestListener onPermissionRpStateRequestListener;
    private OnPermissionRequestListener onPermissionReadWriteRequestListener;

	public FormUtilities(Fragment fragment, OdkFormResultListener listener) {
	    this.fragment = fragment;
		this.mContext = fragment.getContext();
        this.formResultListener = listener;

        initialize();
	}

    public FormUtilities(AppCompatActivity activity, OdkFormResultListener listener) {
        this.activity = activity;
        this.mContext = activity;
        this.formResultListener = listener;

        this.initialize();
    }

    public Context getContext() {
        return mContext;
    }

    public OdkScopedDirUtil getOdkScopedDirUtil(){
        return this.odkScopedDirUtil;
    }

    private void initialize() {
        this.initResultCallbacks();

        this.retrieveOdkStorageType();
    }

    private void initResultCallbacks(){

        ActivityResultCallback<Boolean> permissionResultCallback = granted -> {
            if (granted) {
                deviceId = getDeviceId();
                Log.d("deviceidx", ""+deviceId);
            }

            if (onPermissionRpStateRequestListener != null) {
                onPermissionRpStateRequestListener.requestFinished(granted);
            }
        };

        //for starting request permissions
        if (this.fragment != null) {
            this.requestPermissionRpState = this.fragment.registerForActivityResult(new ActivityResultContracts.RequestPermission(), permissionResultCallback);
            this.requestPermissionsReadWrite = this.fragment.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionResults -> {
                boolean granted = !permissionResults.values().contains(false);
                Log.d("request result", ""+granted);
                if (onPermissionReadWriteRequestListener != null) {
                    onPermissionReadWriteRequestListener.requestFinished(granted);
                }
            });
        } else {
            this.requestPermissionRpState = this.activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), permissionResultCallback);
            this.requestPermissionsReadWrite = this.activity.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionResults -> {
                boolean granted = !permissionResults.values().contains(false);
                if (onPermissionReadWriteRequestListener != null) {
                    onPermissionReadWriteRequestListener.requestFinished(granted);
                }
            });
        }

        //for starting odk activity
        if (this.fragment != null) {
            this.odkResultLauncher = this.fragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> onOdkActivityResult(result));
            this.requestManageAllLauncher = this.fragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> onRequestManageAllActivityResult(result.getResultCode()));
            this.requestAccessAndroidDirLauncher =  this.fragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> onRequestAccessAndroidDirResult(result));
        } else {
            this.odkResultLauncher = this.activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> onOdkActivityResult(result));
            this.requestManageAllLauncher = this.activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> onRequestManageAllActivityResult(result.getResultCode()));
            this.requestAccessAndroidDirLauncher =  this.activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> onRequestAccessAndroidDirResult(result));
        }

    }

    private void retrieveOdkStorageType() {
        final int odk_v_1_26_0 = 3713; //scoped storage without projects
        final int odk_v_1_30_1 = 4094; //last scoped storage with projects
        final int odk_v_2021_2 = 4242; //scoped storage with projects
        String versionName = "NONE";

        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo("org.odk.collect.android", 0);
            versionName = packageInfo.versionName;

            if (packageInfo.versionCode < odk_v_1_26_0) {
                odkStorageType = OdkStorageType.ODK_SHARED_FOLDER; //Android/data/odk
            } else if (packageInfo.versionCode < odk_v_2021_2) {
                odkStorageType = OdkStorageType.ODK_SCOPED_FOLDER_NO_PROJECTS;
            } else {
                odkStorageType = OdkStorageType.ODK_SCOPED_FOLDER_PROJECTS;
            }

        } catch (PackageManager.NameNotFoundException e) {
            odkStorageType = OdkStorageType.NO_ODK_INSTALLED;
        }

        Log.d("odk version", odkStorageType+", ODK v"+versionName);
    }

    public OdkStorageType getOdkStorageType() {
        return this.odkStorageType;
    }

    private void requestPermissionsForReadingPhoneState(OnPermissionRequestListener listener){

        if (isPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
            //GO TO CHECK ODK FOLDER ACCESS
            requestPermissionsForOdkFolders();
        } else {
            //request the permission
            this.onPermissionRpStateRequestListener = listener;
            this.requestPermissionRpState.launch(Manifest.permission.READ_PHONE_STATE);
        }
    }

    private void requestPermissionsForOdkFolders(){
        //READ_PHONE_STATE ALREADY GRANTED ACCESS

        //Check all cases of storage permissions
        //- < Android 11
        //-   Android 11+

        if (odkStorageType == OdkStorageType.NO_ODK_INSTALLED) {
            DialogFactory.createMessageInfo(mContext, R.string.odk_problem_lbl, R.string.odk_form_load_error_odk_not_installed_lbl).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //On Android 11, the basic permissions dont work, we must grant all access
            Log.d("Android 11", "need all access");

            if (odkStorageType == OdkStorageType.ODK_SCOPED_FOLDER_NO_PROJECTS || odkStorageType == odkStorageType.ODK_SCOPED_FOLDER_PROJECTS) {
                //needs to request access to Android/data/org.odk.collect.android/files
                OdkScopedDirUtil.PRIMARY_ANDROID_DOC_ID = OdkScopedDirUtil.ODK_SCOPED_FOLDER_URI;

            } else if (odkStorageType == OdkStorageType.ODK_SHARED_FOLDER){
                //needs to request access to /odk
                OdkScopedDirUtil.PRIMARY_ANDROID_DOC_ID = OdkScopedDirUtil.ODK_SHARED_FOLDER_URI;
            }

            //We will use the Storage Access Framework Only - MANAGE ALL FILES WILL NOT BE USED
            requestAccessAndroidDir();

            return;
        } else {
            //<= Android 10
            OnPermissionRequestListener readWriteGrantListener = granted -> {
                if (granted) {
                    callExecuteCurrentLoadTask();
                } else {
                    DialogFactory.createMessageInfo(this.getContext(), R.string.permissions_sync_storage_title_lbl, R.string.odk_form_load_permission_request_readwrite_denied_lbl).show();
                }
            };

            requestPermissionsForReadAndWriteFiles(readWriteGrantListener);
        }

    }

    private void requestPermissionsForReadAndWriteFiles(OnPermissionRequestListener listener){

        if (isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            callExecuteCurrentLoadTask();
        } else {
            this.onPermissionReadWriteRequestListener = listener;
            this.requestPermissionsReadWrite.launch(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }

    }

    private void requestPermissionsForManageAllFiles(){
        //Log.d("external", ""+Environment.isExternalStorageManager());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            callExecuteCurrentLoadTask();
            return;
        } else {

            //Message - to grant access to ODK Form Files on Android 11 and greater, you must enable
            DialogFactory.createMessageInfo(this.getContext(), R.string.odk_form_load_permission_request_manage_all_title_lbl, R.string.odk_form_load_permission_request_manage_all_info_lbl, clickedButton -> {
                executeRequestPermissionsForManageAllFiles();
            }).show();
        }

    }

    private void executeRequestPermissionsForManageAllFiles() {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(Uri.parse(String.format("package:%s", getContext().getPackageName())));

            this.requestManageAllLauncher.launch(intent);
        } catch (Exception e) {
            e.printStackTrace();

            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            this.requestManageAllLauncher.launch(intent);
        }
    }

    private void onRequestManageAllActivityResult(int resultCode) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            Log.d("manage-all", "result_code="+resultCode+", "+Environment.isExternalStorageManager());

            callExecuteCurrentLoadTask();
        } else {
            //CANT ACCESS ODK FILES TO PRELOAD DATA TO ODK FORMS
            DialogFactory.createMessageInfo(this.getContext(), R.string.storage_access_title_lbl, R.string.odk_form_load_permission_storage_denied_lbl).show();
        }
    }

    private boolean hasSAFPermissionToAndroidDir() {
        Uri treeUri = DocumentsContract.buildTreeDocumentUri(OdkScopedDirUtil.EXTERNAL_STORAGE_PROVIDER_AUTHORITY, OdkScopedDirUtil.PRIMARY_ANDROID_DOC_ID);

        Log.d("permission asked", ""+treeUri);

        boolean permissionGranted = false;

        for (UriPermission uriPermission : mContext.getContentResolver().getPersistedUriPermissions()) {

            Log.d("permission granted", ""+uriPermission.getUri());

            if (uriPermission.getUri().equals(treeUri) && uriPermission.isReadPermission()) {
                permissionGranted = true;
            }
        }

        return permissionGranted;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestAccessAndroidDir(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hasSAFPermissionToAndroidDir()) {
            //Create Scoped Dir Utility class and call currentLoadTask
            this.odkScopedDirUtil = new OdkScopedDirUtil(mContext, odkStorageType);
            callExecuteCurrentLoadTask();

        } else {
            //Message - to grant access to ODK Form Files on Android 11 and greater, you must enable
            DialogFactory.createMessageInfo(this.getContext(), R.string.odk_form_load_permission_request_manage_all_title_lbl, R.string.odk_form_load_permission_request_android_dir_info_lbl, clickedButton -> {
                executeRequestAccessAndroidDir();
            }).show();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void executeRequestAccessAndroidDir() {
        Uri uri = DocumentsContract.buildDocumentUri(OdkScopedDirUtil.EXTERNAL_STORAGE_PROVIDER_AUTHORITY, OdkScopedDirUtil.PRIMARY_ANDROID_DOC_ID);
        Intent intent = getPrimaryVolume().createOpenDocumentTreeIntent();
        intent.putExtra("android.provider.extra.INITIAL_URI", uri);

        this.requestAccessAndroidDirLauncher.launch(intent);
    }

    private void onRequestAccessAndroidDirResult(ActivityResult result) {

        if (result.getResultCode() == Activity.RESULT_OK) {
            Uri directoryUri = result.getData().getData();

            Log.d("granted uri", directoryUri+"");

            mContext.getContentResolver().takePersistableUriPermission(directoryUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            if (hasSAFPermissionToAndroidDir()) {
                //Create Scoped Dir Utility class and call currentLoadTask
                this.odkScopedDirUtil = new OdkScopedDirUtil(mContext, odkStorageType);
                callExecuteCurrentLoadTask();
            } else {
                //CANT ACCESS ODK FILES TO PRELOAD DATA TO ODK FORMS
                DialogFactory.createMessageInfo(this.getContext(), R.string.storage_access_title_lbl, R.string.odk_form_load_permission_storage_denied_lbl).show();
            }

        } else {
            //CANT ACCESS ODK FILES TO PRELOAD DATA TO ODK FORMS
            DialogFactory.createMessageInfo(this.getContext(), R.string.storage_access_title_lbl, R.string.odk_form_load_permission_storage_denied_lbl).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private StorageVolume getPrimaryVolume() {
        StorageManager sm = (StorageManager)  mContext.getSystemService(STORAGE_SERVICE);
        return sm.getPrimaryStorageVolume();
    }

    private boolean isPermissionGranted(final String... permissions) {
        boolean denied = Arrays.stream(permissions).anyMatch(permission -> ContextCompat.checkSelfPermission(this.getContext(), permission) == PackageManager.PERMISSION_DENIED);
        return !denied;
    }

    public void setOdkFormResultListener(OdkFormResultListener listener) {
	    this.formResultListener = listener;
    }

	public void loadForm(final OdkFormLoadData loadData) {
        this.formId = loadData.formId;
        this.formLoadData = loadData;

		this.currentLoadTask = new OdkFormLoadTask(this, loadData, new OdkFormLoadListener() {
            public void onOdkFormLoadSuccess(OdkFormLoadResult result) {
                FormUtilities.this.contentUri = result.getContentUri();
                FormUtilities.this.instanceUri = result.getInstanceUri();

                odkResultLauncher.launch(new Intent(Intent.ACTION_EDIT, result.getContentUri()));
            }

            public void onOdkFormLoadFailure(OdkFormLoadResult result) {
                //Toast.makeText(MainActivity.this, "Cant open ODK Form", 4000);
            	//Log.d("Cant open ODK Form", "odk");
            	createFormLoadResultErrorDialog(result, loadData);
            }
        });

        //we have
        //- permission phone state
        //- permission odk access
        //   - vintage read and write permission
        //   - manage all files permission
        //   - manage android directory permission

        OnPermissionRequestListener readPhoneStateGrantListener = granted -> {
            if (granted) {
                requestPermissionsForOdkFolders();
            } else {
                DialogFactory.createMessageInfo(this.getContext(), R.string.permissions_sync_storage_title_lbl, R.string.odk_form_load_permission_request_readphonestate_denied_lbl).show();
            }
        };

        //check all permissions
        requestPermissionsForReadingPhoneState(readPhoneStateGrantListener);
    }

    public void loadForm(OdkFormLoadData loadData, String contentUriAsString, String instanceXmlUri, final OdkFormResultListener listener){
        this.formId = loadData.formId;
        this.formLoadData = loadData;
	    this.contentUri = Uri.parse(contentUriAsString);
        this.metaInstanceName = "";
        this.lastUpdatedDate = null;
        loadData.formInstanceUri = instanceXmlUri;

        this.currentLoadTask = new OdkFormLoadTask(this, loadData, this.contentUri, new OdkFormLoadListener() {
            public void onOdkFormLoadSuccess(OdkFormLoadResult result) {
                FormUtilities.this.contentUri = result.getContentUri();
                FormUtilities.this.instanceUri = result.getInstanceUri();

                odkResultLauncher.launch(new Intent(Intent.ACTION_EDIT, result.getContentUri()));
            }

            public void onOdkFormLoadFailure(OdkFormLoadResult result) {
                //createSavedXFormNotFoundDialog();
                if (listener != null){
                    listener.onFormInstanceNotFound(formLoadData, contentUri);
                }else{
                    createFormLoadResultErrorDialog(result, loadData);
                }
            }
        });

        //we have
        //- permission phone state
        //- permission odk access
        //   - vintage read and write permission
        //   - manage all files permission
        //   - manage android directory permission

        OnPermissionRequestListener readPhoneStateGrantListener = granted -> {
            if (granted) {
                callExecuteCurrentLoadTask();
            } else {
                DialogFactory.createMessageInfo(this.getContext(), R.string.permissions_sync_storage_title_lbl, R.string.odk_form_load_permission_request_readphonestate_denied_lbl).show();
            }
        };

        //check all permissions
        requestPermissionsForReadingPhoneState(readPhoneStateGrantListener);
    }

    private void callExecuteCurrentLoadTask() {
        if (this.currentLoadTask != null) {
            this.currentLoadTask.execute();
        }
    }

    private void createFormLoadResultErrorDialog(OdkFormLoadResult result, OdkFormLoadData loadData) {
        //xFormNotFound = true;

        @StringRes int messageId = R.string.odk_form_load_error_odk_not_installed_lbl;

        if (result.getStatus() == OdkFormLoadResult.Status.ERROR_NO_ODK_INSTALLED) {
            messageId = R.string.odk_form_load_error_odk_not_installed_lbl;
        } else if (result.getStatus() == OdkFormLoadResult.Status.ERROR_PROVIDER_NA) {
            messageId = R.string.odk_form_load_error_provider_lbl;
        } else if (result.getStatus() == OdkFormLoadResult.Status.ERROR_FORM_NOT_FOUND) {
            messageId = R.string.odk_form_load_error_form_not_found_lbl;
        } else if (result.getStatus() == OdkFormLoadResult.Status.ERROR_ODK_FOLDER_PERMISSION_DENIED) {
            messageId = R.string.odk_form_load_error_folder_permissions_lbl;
        } else if (result.getStatus() == OdkFormLoadResult.Status.ERROR_ODK_CREATE_SAVED_INSTANCE_FILE) {
            messageId = R.string.odk_form_load_error_saving_prefilled_xml_lbl;
        } else if (result.getStatus() == OdkFormLoadResult.Status.ERROR_ODK_INSERT_SAVED_INSTANCE) {
            messageId = R.string.odk_form_load_error_saving_instance_form_lbl;
        }

        DialogFactory.createMessageInfo(this.mContext, R.string.warning_lbl, messageId, new DialogFactory.OnClickListener() {
            @Override
            public void onClicked(DialogFactory.Buttons clickedButton) {
                //xFormNotFound = false;
                if (formResultListener != null) {
                    formResultListener.onFormLoadError(loadData, result);
                }
            }
        }).show();

    }

    private void onOdkActivityResult(ActivityResult result) {
        this.currentLoadTask = null; //already loaded the task and finished
        handleXformResult(result);
    }

    private void handleXformResult(ActivityResult result) {

        Log.d("result "+result.getResultCode(), ""+result.toString());
        //The result code its almost meaningless

        new CheckFormStatus(mContext.getContentResolver(), contentUri).execute();

        /*
        if (result.getResultCode() == Activity.RESULT_OK) {
            new CheckFormStatus(mContext.getContentResolver(), contentUri).execute();
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.odk_problem_lbl), Toast.LENGTH_LONG).show();
        }*/
    }

    private void saveUnfinalizedFile(){
        ContentResolver resolver = mContext.getContentResolver();

        Cursor cursor = resolver.query(contentUri, new String[] { InstanceProviderAPI.InstanceColumns.STATUS,
                        InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE },
                InstanceProviderAPI.InstanceColumns.STATUS + "=?",
                new String[] { InstanceProviderAPI.STATUS_INCOMPLETE }, null);

        Log.d("Running check form", "");

        if (cursor.moveToNext()) {
            Log.d("move next", ""+cursor.getString(0));
            xmlFilePath = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH)); //used to read the xml file

            String sdate = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE));
            java.util.Date ludate = new java.util.Date(Long.parseLong(sdate)) ;

            metaInstanceName = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));
            lastUpdatedDate = ludate;

        } else {
            Log.d("move next", "couldnt find executed form");
        }
    }

    private void createUnfinishedFormDialog() {
        formUnFinished = true;

        DialogFactory dialog = DialogFactory.createMessageYNC(this.mContext, R.string.warning_lbl, R.string.odk_unfinished_dialog_msg, new DialogFactory.OnYesNoCancelClickListener() {
            @Override
            public void onYesClicked() { //delete
                formUnFinished = false;
                mContext.getContentResolver().delete(contentUri, InstanceProviderAPI.InstanceColumns.STATUS + "=?", new String[] { InstanceProviderAPI.STATUS_INCOMPLETE });

                if (formResultListener != null) {
                    formResultListener.onDeleteForm(formLoadData, contentUri, instanceUri);
                }
            }

            @Override
            public void onNoClicked() { //change
                formUnFinished = false;
                odkResultLauncher.launch(new Intent(Intent.ACTION_EDIT, contentUri));
            }

            @Override
            public void onCancelClicked() { //save
                Toast.makeText(mContext, mContext.getString(R.string.odk_unfinished_form_saved), Toast.LENGTH_LONG);
                //save form contentUri
                saveUnfinalizedFile();

                if (formResultListener != null) {
                    formResultListener.onFormUnFinalized(formLoadData, contentUri, formId, instanceUri, metaInstanceName, lastUpdatedDate);
                }
            }
        });
        dialog.setYesText(R.string.odk_unfinished_button_delete);
        dialog.setNoText(R.string.odk_unfinished_button_change);
        dialog.setCancelText(R.string.odk_unfinished_button_save);
        dialog.show();

    }

    /* Get special values for forms */
    public String getStartTimestamp() {
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        long gmt = TimeUnit.HOURS.convert(tz.getRawOffset(), TimeUnit.MILLISECONDS);

        sdf.setCalendar(cal);
        cal.setTime(new Date());


        //Log.d("timezone", "GMT "+gmt);
        //Log.d("realtime", StringUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));
        //Log.d("original-date", ""+sdf.format(cal.getTime()));

        cal.add(Calendar.HOUR_OF_DAY, (int) (-1 * gmt)); //Fixing ODK Error on this variable (ODK is adding GMT Hours number to the datetime of "start" variable)

        String dateString = sdf.format(cal.getTime());
        //Log.d("fixed-datetime", ""+dateString);


        return dateString;
    }

    public String getDeviceId() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }

        TelephonyManager mTelephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);

        String deviceId = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            deviceId = mTelephonyManager.getImei();
        }
        String orDeviceId = "";

        if (deviceId != null ) {
            if ((deviceId.contains("*") || deviceId.contains("000000000000000"))) {
                deviceId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                orDeviceId = Settings.Secure.ANDROID_ID + ":" + deviceId;
            } else {
                orDeviceId = "imei:" + deviceId;
            }
        } else {
            // no SIM -- WiFi only
            // Retrieve WiFiManager
            WifiManager wifi = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            // Get WiFi status
            WifiInfo info = wifi.getConnectionInfo();

            if (info != null) {
                deviceId = info.getMacAddress();
                orDeviceId = "mac:" + deviceId;
            }
        }
        // if it is still null, use ANDROID_ID
        if (deviceId == null) {
            deviceId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            orDeviceId = Settings.Secure.ANDROID_ID + ":" + deviceId;

            //sbuilder.append("<deviceId>"+ orDeviceId +"</deviceId>" + "\r\n");

            return  orDeviceId;
        }

        //sbuilder.append("<deviceId>"+ deviceId +"</deviceId>" + "\r\n");

        return deviceId;
    }

    public boolean deleteInstanceFile(String instanceFileUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //SAF

            try {
                Log.d("saf delete file0", instanceFileUri);

                /*
                //instanceFileUri = instanceFileUri.replace("content://com.android.externalstorage.documents/tree/primary%3Aodk/document/",""); //remove authority from id
                //instanceFileUri = instanceFileUri.replace("%3A", ":");
                //instanceFileUri = instanceFileUri.replace("%2F", "/");
                //instanceFileUri = instanceFileUri.replace("%20", " ");


                Log.d("saf delete file1", instanceFileUri);

                Uri uri = Uri.parse(instanceFileUri);

                //Uri uri = DocumentsContract.buildTreeDocumentUri(OdkScopedDirUtil.EXTERNAL_STORAGE_PROVIDER_AUTHORITY, instanceFileUri);
                Log.d("saf delete file2", uri.toString());
                //int result = mContext.getContentResolver().delete(uri, null);

                XDocumentFile xdocFile = XDocumentFile.fromUri(mContext, uri);
                boolean delete = xdocFile.delete(); //result > 0;
                */

                boolean delete = DocumentsContract.deleteDocument(mContext.getContentResolver(), Uri.parse(instanceFileUri));

                Log.d("saf delete file", instanceFileUri+" - "+delete);
                return delete;

            }catch (Exception ex) {
                Log.d("saf delete file uri", "couldnt delete - "+ex.getMessage());
                ex.printStackTrace();

                return false;
            }

        } else {
            //File API
            try {
                boolean delete = new File(instanceFileUri).delete();
                Log.d("delete file", instanceFileUri+" - "+delete);
                return delete;
            } catch (Exception ex) {
                Log.d("delete file uri", "couldnt delete - "+ex.getMessage());
                ex.printStackTrace();

                return false;
            }
        }

    }

    public InputStream openInstanceInputStream(String instanceFileUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //SAF
            try {
                Uri uri = Uri.parse(instanceFileUri);
                return mContext.getContentResolver().openInputStream(uri);
            }catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            //File API
            try {
                return new FileInputStream(instanceFileUri);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    class CheckFormStatus extends AsyncTask<Void, Void, Boolean> {

        private ContentResolver resolver;
        private Uri contentUri;

        public CheckFormStatus(ContentResolver resolver, Uri contentUri) {
            this.resolver = resolver;
            this.contentUri = contentUri;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {

            Cursor cursorx = resolver.query(contentUri, null, null, null,null);
            while (cursorx.moveToNext()) {
                Log.d("started", "record");
                for (int i = 0; i < cursorx.getColumnCount(); i++) {
                    String columnName = cursorx.getColumnName(i);
                    String columnValue = cursorx.getString(i);
                    Log.d("column(name,value) "+i, columnName+" = "+columnValue);
                }

                Log.d("finished", "record");
            }


            Cursor cursor = resolver.query(contentUri, new String[] { InstanceProviderAPI.InstanceColumns.STATUS,
                            InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE },
                    InstanceProviderAPI.InstanceColumns.STATUS + "=?",
                    new String[] { InstanceProviderAPI.STATUS_COMPLETE }, null);

            boolean resultToReturn = false;
            Log.d("Running check form", "");

            if (cursor.moveToNext()) {
                Log.d("move next", ""+cursor.getString(0));

                String status = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS));

                //guarantee that is a imcomplete form
                if (InstanceProviderAPI.STATUS_INCOMPLETE.equals(status)) {
                    resultToReturn = false;
                } else { //its a completed form
                    String statusChangeDate = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE));
                    xmlFilePath = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH)); //used to read the xml file - its a relative path id is using SAF
                    metaInstanceName = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));
                    lastUpdatedDate = new java.util.Date(Long.parseLong(statusChangeDate)) ;
                    //Log.d("content-x", "" + metaInstanceName );
                    //Log.d("last-date", "" + lastUpdatedDate );

                    resultToReturn = true;
                }

            } else {
                Log.d("move next", "couldnt find executed form");
                resultToReturn = false;
            }

            try{
                cursor.close();
            }catch(Exception e){
                System.err.println("Exception while trying to close cursor !");
                e.printStackTrace();
            }

            return resultToReturn;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            //hideProgressFragment();

            if (result) {
                //When everything is OK - save current form location

                //pass contentUri and filepath to a listener - onFormFinalized, onFormUnfinalized
                if (formResultListener != null) {
                    formResultListener.onFormFinalized(formLoadData, contentUri, formId, instanceUri, metaInstanceName, lastUpdatedDate);
                }
            } else {

                if (formLoadData.skipUnfinalizedCheck) {
                    //jump to call listener FormUnFinalized
                    if (formResultListener != null) {
                        saveUnfinalizedFile();
                        formResultListener.onFormUnFinalized(formLoadData, contentUri, formId, instanceUri, metaInstanceName, lastUpdatedDate);
                    }
                } else {
                    createUnfinishedFormDialog();
                }
            }

        }
    }


}

interface OnPermissionRequestListener {
    void requestFinished(boolean granted);
}