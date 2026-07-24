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
import mz.betainteractive.odk.model.FilledForm;
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
	private Uri formUri; /* odk CONTENT_URI of every instance */
    private Uri instanceUri; /* odk instance file uri */

    private boolean formUnFinished;
    private String instanceXmlPath;
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

    public enum FormStatus {
        FINALIZED, UNFINALIZED, NOT_FOUND
    }
    
    private enum LoadForm {
        OPEN, REOPEN
    }

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
                Log.d("frag request result", ""+granted);
                if (onPermissionReadWriteRequestListener != null) {
                    onPermissionReadWriteRequestListener.requestFinished(granted);
                }
            });
        } else {
            this.requestPermissionRpState = this.activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), permissionResultCallback);
            this.requestPermissionsReadWrite = this.activity.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionResults -> {
                boolean granted = !permissionResults.values().contains(false);
                Log.d("acti request result", ""+granted);
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
        odkStorageType = getOdkStorageType(mContext);
    }

    public static OdkStorageType getOdkStorageType(Context context) {
        final int odk_v_1_26_0 = 3713; //using shared folder /odk
        final int odk_v_1_28_4 = 3963; //last version using shared folder /odk
        final int odk_v_1_30_1 = 4094; //last version with scoped storage without projects
        final int odk_v_2021_2 = 4242; //scoped storage with projects
        final int odk_v_2023_1 = 4616;
        String versionName = "NONE";
        Integer versionCode = null;
        OdkStorageType storageType = OdkStorageType.NO_ODK_INSTALLED;

        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo("org.odk.collect.android", 0);
            versionName = packageInfo.versionName;
            versionCode = packageInfo.versionCode;

            /*
            From ODK Collect up to version v1.28.4 - Support all android versions so far (15) - XML WRITE   - uses SHARED_FOLDER
            From ODK Collect up to version v1.30.1 - Support up to Android 13                 - XML WRITE   - uses ODK_SCOPED_FOLDER_NO_PROJECTS
            From ODK Collect version v2021.2.0     - Supports all android versions            - INTENT CALL - uses ODK_INTENT_LAUNCH
            */

            if (packageInfo.versionCode <= odk_v_1_28_4) {
                //tested up to android 15 - virtually supports all versions
                storageType = OdkStorageType.ODK_SHARED_FOLDER; //odk
            } else if (packageInfo.versionCode <= odk_v_1_30_1) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                    //up to android 13
                    storageType = OdkStorageType.ODK_SCOPED_FOLDER_NO_PROJECTS; //Android/data/ without projects
                } else {
                    storageType = OdkStorageType.ODK_INTENT_LAUNCH;
                }
            } else {
                storageType = OdkStorageType.ODK_INTENT_LAUNCH; //Will use intents on unsupported ODK Collect
            }

        } catch (PackageManager.NameNotFoundException e) {
            storageType = OdkStorageType.NO_ODK_INSTALLED;
        }

        Log.d("odk versionx", storageType+", ODK "+versionName+" - "+versionCode);

        return storageType;
    }

    public OdkStorageType getOdkStorageType() {
        return this.odkStorageType;
    }

    private void requestPermissionsForReadingPhoneState(OnPermissionRequestListener listener){

        if (isPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
            Log.d("permissions", "access to READ_PHONE_STATE");
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
        // if we are using ODK_INTENT_LAUNCH, we dont need any access request

        if (odkStorageType == OdkStorageType.NO_ODK_INSTALLED) {
            DialogFactory.createMessageInfo(mContext, R.string.odk_problem_lbl, R.string.odk_form_load_error_odk_not_installed_lbl).show();
            return;
        }

        if (odkStorageType == OdkStorageType.ODK_INTENT_LAUNCH) {
            //We will use intent call to preload ODK Collect
            callExecuteCurrentLoadTask();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //On Android 11, the basic permissions dont work, we must grant all access
            Log.d("Android 11", "need all access");

            if (odkStorageType == OdkStorageType.ODK_SCOPED_FOLDER_NO_PROJECTS || odkStorageType == OdkStorageType.ODK_SCOPED_FOLDER_PROJECTS) {
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            //less than Android 13
            String[] legacyPermissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            if (isLegacyPermissionGranted(legacyPermissions)) {
                callExecuteCurrentLoadTask();
            } else {
                this.onPermissionReadWriteRequestListener = listener;
                this.requestPermissionsReadWrite.launch(legacyPermissions);
            }
        } else {

            if (isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                callExecuteCurrentLoadTask();
            } else {
                this.onPermissionReadWriteRequestListener = listener;
                this.requestPermissionsReadWrite.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
            }
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

    private boolean isLegacyPermissionGranted(String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void setOdkFormResultListener(OdkFormResultListener listener) {
	    this.formResultListener = listener;
    }

	public void loadForm(final OdkFormLoadData loadData) {
        this.formId = loadData.formId;
        this.formLoadData = loadData;

		this.currentLoadTask = new OdkFormLoadTask(this, loadData, new OdkFormLoadListener() {
            public void onOdkFormLoadSuccess(OdkFormLoadResult result) {
                formUri = result.getFormUri(); //save formUri

                Uri contentUri = result.getInstanceUri(); //instance form - from writing xml

                //save FilledForm to be used by another activity (OdkPreloadActivity) - preloading directly to xml instance if used its already executed on OdkFormLoadTask
                if (odkStorageType == OdkStorageType.ODK_INTENT_LAUNCH) {
                    contentUri = result.getFormUri(); //blank form - intent launch
                    FilledForm.saveInstance(mContext, loadData.preloadedData);
                }

                //Log.d("load-form", "content-uri = "+contentUri+", form-uri="+result.getFormUri());
                Intent intent = new Intent(Intent.ACTION_EDIT, contentUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                odkResultLauncher.launch(intent);
            }

            public void onOdkFormLoadFailure(OdkFormLoadResult result) {
                //Toast.makeText(MainActivity.this, "Cant open ODK Form", 4000);
            	//Log.d("Cant open ODK Form", "odk");
            	createFormLoadResultErrorDialog(result, loadData, LoadForm.OPEN, null);
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

    /* load saved instance */
    public void loadForm(OdkFormLoadData loadData, String formUriStr, String instanceUriStr, String instanceXmlPath, final OdkFormResultListener listener){
        this.formId = loadData.formId;
        this.formLoadData = loadData;
	    this.formUri = Uri.parse(formUriStr);
        this.instanceUri = Uri.parse(instanceUriStr);
        this.instanceXmlPath = instanceXmlPath;
        this.metaInstanceName = "";
        this.lastUpdatedDate = null;

        this.currentLoadTask = new OdkFormLoadTask(this, loadData, this.formUri, this.instanceUri, this.instanceXmlPath, new OdkFormLoadListener() {
            public void onOdkFormLoadSuccess(OdkFormLoadResult result) {
                formUri = result.getFormUri(); //save formUri

                Intent intent = new Intent(Intent.ACTION_EDIT, result.getInstanceUri()); //we need to edit the instance and not the blank form
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                odkResultLauncher.launch(intent);
            }

            public void onOdkFormLoadFailure(OdkFormLoadResult result) {
                //createSavedXFormNotFoundDialog();
                if (listener != null){
                    listener.onFormInstanceNotFound(formLoadData, formUri, instanceUri);
                }else{
                    createFormLoadResultErrorDialog(result, loadData, LoadForm.REOPEN, listener);
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

    private void createFormLoadResultErrorDialog(OdkFormLoadResult result, OdkFormLoadData loadData, LoadForm loadFormType, final OdkFormResultListener listener) {
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


        if (result.getStatus() == OdkFormLoadResult.Status.ERROR_PROVIDER_NA) {
            //add try again button

            DialogFactory.createMessageYN(this.mContext, R.string.warning_lbl, messageId, R.string.bt_ok_lbl, R.string.bt_try_again_lbl, new DialogFactory.OnYesNoClickListener() {
                @Override
                public void onYesClicked() { //NO
                    //xFormNotFound = false;
                    if (formResultListener != null) {
                        formResultListener.onFormLoadError(loadData, result);
                    }
                }

                @Override
                public void onNoClicked() { //Try Again
                    if (loadFormType == LoadForm.OPEN) {
                        loadForm(loadData);
                    } else if (loadFormType == LoadForm.REOPEN) {
                        loadForm(loadData, formUri.toString(), instanceUri.toString(), instanceXmlPath, listener);
                    }
                }
            }).show();

            return;
        }

        DialogFactory.createMessageInfo(this.mContext, R.string.warning_lbl, messageId, clickedButton -> {
            //xFormNotFound = false;
            if (formResultListener != null) {
                formResultListener.onFormLoadError(loadData, result);
            }
        }).show();

    }

    private void onOdkActivityResult(ActivityResult result) {
        this.currentLoadTask = null; //already loaded the task and finished
        handleXformResult(result);
    }

    private void handleXformResult(ActivityResult result) {

        if (result.getResultCode() == Activity.RESULT_OK) {

            // Instance URI returned from ODK, e.g. content://org.odk.collect.android.provider.odk.instances/instances/1
            this.instanceUri = result.getData().getData();


            Log.d("result " + result.getResultCode(), "form-uri = "+this.formUri+", instance-uri = " + instanceUri + ", " + result.toString());
            //The result code its almost meaningless

            new CheckFormStatus(mContext.getContentResolver(), instanceUri).execute();
        }
    }

    private void saveUnfinalizedFile(){

        ContentResolver resolver = mContext.getContentResolver();

        Cursor cursor = resolver.query(
                this.instanceUri,
                new String[] {
                        InstanceProviderAPI.InstanceColumns.STATUS,
                        InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH,
                        InstanceProviderAPI.InstanceColumns.DISPLAY_NAME,
                        InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE
                },
                InstanceProviderAPI.InstanceColumns.STATUS + " IN (?, ?)",
                new String[] {
                        InstanceProviderAPI.STATUS_INCOMPLETE,
                        InstanceProviderAPI.STATUS_VALID
                },
                null
        );

        Log.d("Running check form", "");

        if (cursor.moveToNext()) {
            Log.d("move next", ""+cursor.getString(0));
            instanceXmlPath = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH)); //used to read the xml file

            String sdate = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE));
            java.util.Date ludate = new java.util.Date(Long.parseLong(sdate)) ;

            metaInstanceName = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));
            lastUpdatedDate = ludate;

        } else {
            Log.d("move next", "couldnt find executed form");
        }
    }

    public void editLastOpenedForm(){
        odkResultLauncher.launch(new Intent(Intent.ACTION_EDIT, instanceUri));
    }

    private void createUnfinishedFormDialog() {
        formUnFinished = true;

        DialogFactory dialog = DialogFactory.createMessageYNC(this.mContext, R.string.warning_lbl, R.string.odk_unfinished_dialog_msg, new DialogFactory.OnYesNoCancelClickListener() {
            @Override
            public void onYesClicked() { //delete
                formUnFinished = false;
                mContext.getContentResolver().delete(instanceUri, InstanceProviderAPI.InstanceColumns.STATUS + " in (?, ?)", new String[] { InstanceProviderAPI.STATUS_INCOMPLETE, InstanceProviderAPI.STATUS_VALID });

                if (formResultListener != null) {
                    formResultListener.onDeleteForm(formLoadData, formUri, instanceUri, instanceXmlPath);
                }
            }

            @Override
            public void onNoClicked() { //change
                formUnFinished = false;
                odkResultLauncher.launch(new Intent(Intent.ACTION_EDIT, instanceUri));
            }

            @Override
            public void onCancelClicked() { //save
                Toast.makeText(mContext, mContext.getString(R.string.odk_unfinished_form_saved), Toast.LENGTH_LONG);
                //save form contentUri
                saveUnfinalizedFile();

                if (formResultListener != null) {
                    formResultListener.onFormUnFinalized(formLoadData, formUri, instanceUri, formId, instanceXmlPath, metaInstanceName, lastUpdatedDate);
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

    private Cursor getCursorForInstancesProvider(Uri contentUri) {
        ContentResolver resolver = mContext.getContentResolver();
        Cursor cursor = resolver.query(contentUri, new String[] { InstanceProviderAPI.InstanceColumns.STATUS,
                        InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH,
                        InstanceProviderAPI.InstanceColumns.DISPLAY_NAME,
                        InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE },
                InstanceProviderAPI.InstanceColumns.STATUS + "=?",
                new String[] { InstanceProviderAPI.STATUS_COMPLETE }, null);

        return cursor;
    }

    public FormStatus isFormFinalized(String instanceUriStr) {
        FormStatus formStatus = FormStatus.NOT_FOUND;

        if (instanceUriStr == null) return formStatus;

        Uri contentUri = Uri.parse(instanceUriStr);
        Cursor cursor = null;
        String errorMessage = null;
        int errorRepeatCount = 0;
        String errorLike = "AppDependencyComponent.inject(org.odk.collect.android.external.InstanceProvider)' on a null object reference";

        //Try to get forms instance provider 4 times
        do {

            errorMessage = null;

            try {
                cursor = getCursorForInstancesProvider(contentUri);
            } catch (Exception ex) {
                errorRepeatCount++;
                errorMessage = ex.getMessage();

                Log.d("formutilities", "instanceprovider, error_count="+errorRepeatCount);
                ex.printStackTrace();
            }
        } while (errorMessage != null && errorMessage.contains(errorLike) && errorRepeatCount < 4);

        if (cursor != null) {
            if (cursor.moveToNext()) {
                //Log.d("move next", ""+cursor.getString(0));
                int columnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS);
                String status = cursor.getString(columnIndex);

                //guarantee that is a imcomplete form
                if (InstanceProviderAPI.STATUS_INCOMPLETE.equals(status)) {
                    formStatus = FormStatus.UNFINALIZED;
                } else { //its a completed form
                    formStatus = FormStatus.FINALIZED;
                }

            } else {
                //Log.d("move next", "couldnt find executed form");
                formStatus = FormStatus.NOT_FOUND;
            }

            try {
                cursor.close();
            } catch(Exception e){
                System.err.println("Exception while trying to close cursor !");
                e.printStackTrace();
            }
        }

        return formStatus;
    }

    class CheckFormStatus extends AsyncTask<Void, Void, Boolean> {

        private ContentResolver resolver;
        private Uri instanceUri;

        public CheckFormStatus(ContentResolver resolver, Uri instanceUri) {
            this.resolver = resolver;
            this.instanceUri = instanceUri;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {

            Cursor cursor = null;

            cursor = resolver.query(
                    this.instanceUri,
                    new String[] {
                            InstanceProviderAPI.InstanceColumns.STATUS,
                            InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH,
                            InstanceProviderAPI.InstanceColumns.DISPLAY_NAME,
                            InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE
                    },
                    null, null, null
            );


            boolean resultToReturn = false;
            Log.d("Running check form", "");

            if (cursor != null && cursor.moveToNext()) {

                Log.d("move next", "" + cursor.getString(0));

                /*
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    String columnName = cursor.getColumnName(i);
                    String columnValue = cursor.getString(i);
                    Log.d("column(name,value)x " + i, columnName + " = " + columnValue);
                }*/

                String status = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS));
                Log.d("odk status", status);

                //guarantee that is a imcomplete form
                if (InstanceProviderAPI.STATUS_INCOMPLETE.equals(status) || InstanceProviderAPI.STATUS_VALID.equals(status)) {
                    instanceXmlPath = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH)); //used to read the xml file - its a relative path id is using SAF
                    metaInstanceName = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));
                    resultToReturn = false;
                } else { //its a completed form
                    String statusChangeDate = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE));
                    instanceXmlPath = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH)); //used to read the xml file - its a relative path id is using SAF
                    metaInstanceName = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));
                    lastUpdatedDate = new java.util.Date(Long.parseLong(statusChangeDate));
                    //Log.d("content-x", "" + metaInstanceName );
                    //Log.d("last-date", "" + lastUpdatedDate );

                    resultToReturn = true;
                }

            } else {
                Log.d("move next", "couldnt find executed form");
                resultToReturn = false;
            }

            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    System.err.println("Exception while trying to close cursor !");
                    e.printStackTrace();
                }
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
                    formResultListener.onFormFinalized(formLoadData, formUri, instanceUri, formId, instanceXmlPath, metaInstanceName, lastUpdatedDate);
                }
            } else {

                if (formLoadData.skipUnfinalizedCheck) {
                    //jump to call listener FormUnFinalized
                    if (formResultListener != null) {
                        saveUnfinalizedFile();
                        formResultListener.onFormUnFinalized(formLoadData, formUri, instanceUri, formId, instanceXmlPath, metaInstanceName, lastUpdatedDate);
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