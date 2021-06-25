package mz.betainteractive.odk;


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
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import mz.betainteractive.odk.listener.OdkFormLoadListener;
import mz.betainteractive.odk.listener.OdkFormResultListener;
import mz.betainteractive.odk.model.FilledForm;
import mz.betainteractive.odk.task.OdkGeneratedFormLoadTask;
import mz.betainteractive.utilities.StringUtil;

public class FormUtilities {
    public static final int SELECTED_ODK_FORM = 51;
    public static final int SELECTED_ODK_REOPEN = 52;

    private Context mContext;
    private Fragment fragment;
    private AppCompatActivity activity;
	private String jrFormId;
	private Uri contentUri;
    private boolean formUnFinished;
    private String xmlFilePath;
    private OdkFormResultListener formResultListener;

    private String metaInstanceName;
    private Date lastUpdatedDate;

    private String deviceId;
    private ActivityResultLauncher<String> requestPermission;
    private OdkGeneratedFormLoadTask currentLoadTask;
    private OnPermissionRequestListener onPermissionRequestListener;

	public FormUtilities(Fragment fragment) {
	    this.fragment = fragment;
		this.mContext = fragment.getContext();
		this.initPermissions();
	}

    public FormUtilities(AppCompatActivity activity) {
        this.activity = activity;
        this.mContext = activity;
        this.initPermissions();
    }

    public Context getContext() {
        return mContext;
    }

    private void initPermissions(){

        ActivityResultCallback<Boolean> permissionResultCallback = new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean granted) {
                if (granted) {
                    deviceId = getDeviceId();
                    Log.d("deviceidx", ""+deviceId);

                    if (onPermissionRequestListener != null) {
                        onPermissionRequestListener.requestFinished(granted);
                    }

                } else {
                    //Log.d("deviceid", "no permission to read it");
                    DialogFactory.createMessageInfo(mContext, org.philimone.hds.forms.R.string.device_id_title_lbl, org.philimone.hds.forms.R.string.device_id_permissions_error, new DialogFactory.OnClickListener() {
                        @Override
                        public void onClicked(DialogFactory.Buttons clickedButton) {
                            if (onPermissionRequestListener != null) {
                                onPermissionRequestListener.requestFinished(granted);
                            }
                        }
                    }).show();
                }
            }
        };

        if (this.fragment != null) {
            this.requestPermission = this.fragment.registerForActivityResult(new ActivityResultContracts.RequestPermission(), permissionResultCallback);
        } else if (this.activity != null) {
            this.requestPermission = this.activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), permissionResultCallback);
        }
    }

    private void requestPermissionsForReadingPhoneState(OnPermissionRequestListener listener){
	    this.onPermissionRequestListener = listener;
        this.requestPermission.launch(Manifest.permission.READ_PHONE_STATE);
    }

    public void setOdkFormResultListener(OdkFormResultListener listener) {
	    this.formResultListener = listener;
    }

    /*
     * call startActivityForResult using either Fragment or Activity
     */
    private void callStartActivityForResult(Intent intent, int requestCode) {
	    if (fragment != null) {
	        fragment.startActivityForResult(intent, requestCode);
        }else if (activity != null) {
	        activity.startActivityForResult(intent, requestCode);
        }
    }

	public void loadForm(final FilledForm filledForm) {

		this.currentLoadTask = new OdkGeneratedFormLoadTask(this, filledForm, new OdkFormLoadListener() {
            public void onOdkFormLoadSuccess(Uri contentUri) {
            	//Log.d("contenturi", contentUri+"");
            	
            	Cursor cursor = getCursorForFormsProvider(filledForm.getFormName());
                if (cursor.moveToFirst()) {
                    jrFormId = cursor.getString(0);
                    //Log.d("form",""+jrFormId+", v = "+cursor.getString(2));
                }
                cursor.close();
                
                FormUtilities.this.contentUri = contentUri;

                callStartActivityForResult(new Intent(Intent.ACTION_EDIT, contentUri), SELECTED_ODK_FORM);
            }

            public void onOdkFormLoadFailure() {
                //Toast.makeText(MainActivity.this, "Cant open ODK Form", 4000);
            	//Log.d("Cant open ODK Form", "odk");
            	createXFormNotFoundDialog();
            }
        });

		//request permission for reading deviceId - after this execute the load task
		requestPermissionsForReadingPhoneState(granted -> {
            currentLoadTask.execute(); //load odk form - regardless of the permission state
        });
    }

    public void loadForm(FilledForm filledForm, String contentUriAsString, final OdkFormResultListener listener){
        this.contentUri = Uri.parse(contentUriAsString);
        this.metaInstanceName = "";
        this.lastUpdatedDate = null;

        this.currentLoadTask = new OdkGeneratedFormLoadTask(this, filledForm, contentUri, new OdkFormLoadListener() {
            public void onOdkFormLoadSuccess(Uri contentUri) {
                FormUtilities.this.contentUri = contentUri;
                callStartActivityForResult(new Intent(Intent.ACTION_EDIT, contentUri), SELECTED_ODK_FORM);
            }

            public void onOdkFormLoadFailure() {
                //createSavedXFormNotFoundDialog();

                if (listener != null){
                    listener.onFormNotFound(contentUri);
                }else{
                    createSavedXFormNotFoundDialog();
                }
            }
        });

        //request permission for reading deviceId - after this execute the load task
        requestPermissionsForReadingPhoneState(granted -> {
            currentLoadTask.execute(); //load odk form - regardless of the permission state
        });
    }

    private void createXFormNotFoundDialog() {
        //xFormNotFound = true;

        DialogFactory.createMessageInfo(this.mContext, R.string.warning_lbl, R.string.odk_couldnt_open_xform_lbl, new DialogFactory.OnClickListener() {
            @Override
            public void onClicked(DialogFactory.Buttons clickedButton) {
                //xFormNotFound = false;
            }
        }).show();

    }

    private void createSavedXFormNotFoundDialog() {
        //xFormNotFound = true;

        DialogFactory.createMessageInfo(this.mContext, R.string.warning_lbl, R.string.odk_couldnt_reopen_xform_lbl, new DialogFactory.OnClickListener() {
            @Override
            public void onClicked(DialogFactory.Buttons clickedButton) {
                //xFormNotFound = false;
            }
        }).show();

    }

	private Cursor getCursorForFormsProvider(String name) {
    	ContentResolver resolver = mContext.getContentResolver();
        return resolver.query(FormsProviderAPI.FormsColumns.CONTENT_URI, new String[] {
                FormsProviderAPI.FormsColumns.JR_FORM_ID, FormsProviderAPI.FormsColumns.FORM_FILE_PATH, FormsProviderAPI.FormsColumns.JR_VERSION },
                FormsProviderAPI.FormsColumns.JR_FORM_ID + " like ?", new String[] { name + "%" }, null);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data, OdkFormResultListener listener) {

        Log.d("activityResult", "res-"+requestCode);

        this.currentLoadTask = null; //already loaded the task and finished
        this.formResultListener = listener;

        switch (requestCode) {
            case SELECTED_ODK_FORM:
                handleXformResult(requestCode, resultCode, data);
                break;
            case SELECTED_ODK_REOPEN:
                handleXformResult(requestCode, resultCode, data);
                break;
        }
    }

    private void handleXformResult(int requestCode, int resultCode, Intent data) {
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
                callStartActivityForResult(new Intent(Intent.ACTION_EDIT, contentUri), SELECTED_ODK_REOPEN);
            }

            @Override
            public void onCancelClicked() { //save
                Toast.makeText(mContext, mContext.getString(R.string.odk_unfinished_form_saved), Toast.LENGTH_LONG);
                //save form contentUri
                saveUnfinalizedFile();

                if (formResultListener != null) {
                    formResultListener.onFormUnFinalized(contentUri, new File(xmlFilePath), metaInstanceName, lastUpdatedDate);
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
        TelephonyManager mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this.mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        String deviceId = mTelephonyManager.getImei();
        String orDeviceId;

        if (deviceId != null ) {
            if ((deviceId.contains("*") || deviceId.contains("000000000000000"))) {
                deviceId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                orDeviceId = Settings.Secure.ANDROID_ID + ":" + deviceId;
            } else {
                orDeviceId = "imei:" + deviceId;
            }
        }
        if ( deviceId == null ) {
            // no SIM -- WiFi only
            // Retrieve WiFiManager
            WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

            // Get WiFi status
            WifiInfo info = wifi.getConnectionInfo();

            if ( info != null ) {
                deviceId = info.getMacAddress();
                orDeviceId = "mac:" + deviceId;
            }
        }
        // if it is still null, use ANDROID_ID
        if ( deviceId == null ) {
            deviceId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
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
                    formResultListener.onFormFinalized(contentUri, new File(xmlFilePath), metaInstanceName, lastUpdatedDate);
                }
            } else {
                createUnfinishedFormDialog();
            }

        }
    }

    interface OnPermissionRequestListener {
        void requestFinished(boolean granted);
    }
}
