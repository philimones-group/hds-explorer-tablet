package mz.betainteractive.odk;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;


import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.widget.DialogFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import mz.betainteractive.odk.listener.OdkFormLoadListener;
import mz.betainteractive.odk.listener.OdkFormResultListener;
import mz.betainteractive.odk.model.FilledForm;
import mz.betainteractive.odk.task.OdkFormLoadResult;
import mz.betainteractive.odk.task.OdkFormLoadTask;

public class FormUtilities {
    public static final int SELECTED_ODK_FORM = 51;
    public static final int SELECTED_ODK_REOPEN = 52;

    private Context mContext;
    private Fragment fragment;
    private AppCompatActivity activity;
	private Uri contentUri;
    private boolean formUnFinished;
    private String xmlFilePath;
    private OdkFormResultListener formResultListener;

    private String metaInstanceName;
    private Date lastUpdatedDate;

    private String formId;

    private String deviceId;
    private ActivityResultLauncher<String> requestPermissionRpState;
    private ActivityResultLauncher<String[]> requestPermissionsReadWrite;
    private ActivityResultLauncher<Intent> odkResultLauncher;
    private OdkFormLoadTask currentLoadTask;
    private OnPermissionRequestListener onPermissionRpStateRequestListener;
    private OnPermissionRequestListener onPermissionReadWriteRequestListener;

	public FormUtilities(Fragment fragment, OdkFormResultListener listener) {
	    this.fragment = fragment;
		this.mContext = fragment.getContext();
        this.formResultListener = listener;

		this.initResultCallbacks();
	}

    public FormUtilities(AppCompatActivity activity, OdkFormResultListener listener) {
        this.activity = activity;
        this.mContext = activity;
        this.formResultListener = listener;

        this.initResultCallbacks();
    }

    public Context getContext() {
        return mContext;
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
            this.odkResultLauncher = this.fragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> onOdkActivityResult(result.getResultCode()));
        } else {
            this.odkResultLauncher = this.activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> onOdkActivityResult(result.getResultCode()));
        }

    }

    private void requestPermissionsForReadingPhoneState(OnPermissionRequestListener listener){
	    this.onPermissionRpStateRequestListener = listener;
        this.requestPermissionRpState.launch(Manifest.permission.READ_PHONE_STATE);
    }

    private void requestPermissionsForReadAndWriteFiles(OnPermissionRequestListener listener){
        this.onPermissionReadWriteRequestListener = listener;
        this.requestPermissionsReadWrite.launch(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
    }

    private boolean isPermissionGranted(final String... permissions) {
        boolean denied = Arrays.stream(permissions).anyMatch(permission -> ContextCompat.checkSelfPermission(this.getContext(), permission) == PackageManager.PERMISSION_DENIED);
        return !denied;
    }

    public void setOdkFormResultListener(OdkFormResultListener listener) {
	    this.formResultListener = listener;
    }

	public void loadForm(final FilledForm filledForm) {
        this.formId = filledForm.getFormName();

		this.currentLoadTask = new OdkFormLoadTask(this, filledForm, new OdkFormLoadListener() {
            public void onOdkFormLoadSuccess(Uri contentUri) {
                FormUtilities.this.contentUri = contentUri;

                odkResultLauncher.launch(new Intent(Intent.ACTION_EDIT, contentUri));
            }

            public void onOdkFormLoadFailure(OdkFormLoadResult result) {
                //Toast.makeText(MainActivity.this, "Cant open ODK Form", 4000);
            	//Log.d("Cant open ODK Form", "odk");
            	createFormLoadResultErrorDialog(result);
            }
        });

        OnPermissionRequestListener readPhoneStateGrantListener = granted -> {
            if (granted) {
                callExecuteCurrentLoadTask();
            } else {
                DialogFactory.createMessageInfo(this.getContext(), R.string.permissions_sync_storage_title_lbl, R.string.odk_form_load_permission_request_readphonestate_denied_lbl).show();
            }
        };

        OnPermissionRequestListener readWriteGrantListener = granted -> {
            if (granted) {
                if (isPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
                    callExecuteCurrentLoadTask();
                } else {
                    requestPermissionsForReadingPhoneState(readPhoneStateGrantListener);
                }
            } else {
                DialogFactory.createMessageInfo(this.getContext(), R.string.permissions_sync_storage_title_lbl, R.string.odk_form_load_permission_request_readwrite_denied_lbl).show();
            }
        };

        if (isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (isPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
                callExecuteCurrentLoadTask();
            } else {
                requestPermissionsForReadingPhoneState(readPhoneStateGrantListener);
            }
        } else {
            requestPermissionsForReadAndWriteFiles(readWriteGrantListener);
        }

    }

    public void loadForm(FilledForm filledForm, String contentUriAsString, final OdkFormResultListener listener){
        this.formId = filledForm.getFormName();
	    this.contentUri = Uri.parse(contentUriAsString);
        this.metaInstanceName = "";
        this.lastUpdatedDate = null;

        this.currentLoadTask = new OdkFormLoadTask(this, filledForm, contentUri, new OdkFormLoadListener() {
            public void onOdkFormLoadSuccess(Uri contentUri) {
                FormUtilities.this.contentUri = contentUri;

                odkResultLauncher.launch(new Intent(Intent.ACTION_EDIT, contentUri));
            }

            public void onOdkFormLoadFailure(OdkFormLoadResult result) {
                //createSavedXFormNotFoundDialog();
                if (listener != null){
                    listener.onFormNotFound(contentUri);
                }else{
                    createFormLoadResultErrorDialog(result);
                }
            }
        });

        OnPermissionRequestListener readPhoneStateGrantListener = granted -> {
            if (granted) {
                callExecuteCurrentLoadTask();
            } else {
                DialogFactory.createMessageInfo(this.getContext(), R.string.permissions_sync_storage_title_lbl, R.string.odk_form_load_permission_request_readphonestate_denied_lbl).show();
            }

        };

        OnPermissionRequestListener readWriteGrantListener = granted -> {
            if (granted) {
                if (isPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
                    callExecuteCurrentLoadTask();
                } else {
                    requestPermissionsForReadingPhoneState(readPhoneStateGrantListener);
                }
            } else {
                DialogFactory.createMessageInfo(this.getContext(), R.string.permissions_sync_storage_title_lbl, R.string.odk_form_load_permission_request_readwrite_denied_lbl).show();
            }
        };

        if (isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (isPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
                callExecuteCurrentLoadTask();
            } else {
                requestPermissionsForReadingPhoneState(readPhoneStateGrantListener);
            }
        } else {
            requestPermissionsForReadAndWriteFiles(readWriteGrantListener);
        }
    }

    private void callExecuteCurrentLoadTask() {
        if (this.currentLoadTask != null) {
            this.currentLoadTask.execute();
        }
    }

    private void createFormLoadResultErrorDialog(OdkFormLoadResult result) {
        //xFormNotFound = true;

        @StringRes int messageId = R.string.odk_form_load_error_provider_lbl;

        if (result.getStatus() == OdkFormLoadResult.Status.ERROR_PROVIDER_NA) {
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
            }
        }).show();

    }

    private void onOdkActivityResult(int resultCode) {
        this.currentLoadTask = null; //already loaded the task and finished
        handleXformResult(resultCode);
    }

    private void handleXformResult(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            new CheckFormStatus(mContext.getContentResolver(), contentUri).execute();
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.odk_problem_lbl), Toast.LENGTH_LONG).show();
        }
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
                    formResultListener.onDeleteForm(contentUri);
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
                    formResultListener.onFormUnFinalized(contentUri, formId, new File(xmlFilePath), metaInstanceName, lastUpdatedDate);
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

    class CheckFormStatus extends AsyncTask<Void, Void, Boolean> {

        private ContentResolver resolver;
        private Uri contentUri;

        public CheckFormStatus(ContentResolver resolver, Uri contentUri) {
            this.resolver = resolver;
            this.contentUri = contentUri;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Cursor cursor = resolver.query(contentUri, new String[] { InstanceProviderAPI.InstanceColumns.STATUS,
                            InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE },
                    InstanceProviderAPI.InstanceColumns.STATUS + "=?",
                    new String[] { InstanceProviderAPI.STATUS_COMPLETE }, null);

            boolean resultToReturn = false;
            Log.d("Running check form", "");

            if (cursor.moveToNext()) {
                Log.d("move next", ""+cursor.getString(0));
                xmlFilePath = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH)); //used to read the xml file

                String sdate = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE));
                java.util.Date ludate = new java.util.Date(Long.parseLong(sdate)) ;

                metaInstanceName = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));
                lastUpdatedDate = ludate;

                //Log.d("content-x", "" + metaInstanceName );
                //Log.d("last-date", "" + lastUpdatedDate );

                resultToReturn = true;
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
                    formResultListener.onFormFinalized(contentUri, formId, new File(xmlFilePath), metaInstanceName, lastUpdatedDate);
                }
            } else {
                createUnfinishedFormDialog();
            }

        }
    }


}

interface OnPermissionRequestListener {
    void requestFinished(boolean granted);
}