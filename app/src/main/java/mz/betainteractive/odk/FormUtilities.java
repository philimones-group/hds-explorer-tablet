package mz.betainteractive.odk;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import net.manhica.dss.explorer.R;

import java.io.File;
import java.sql.Date;

import mz.betainteractive.odk.listener.OdkFormLoadListener;
import mz.betainteractive.odk.listener.OdkFormResultListener;
import mz.betainteractive.odk.model.FilledForm;
import mz.betainteractive.odk.task.OdkGeneratedFormLoadTask;
import mz.betainteractive.utilities.StringUtil;

public class FormUtilities {
    public static final int SELECTED_ODK_FORM = 51;
    public static final int SELECTED_ODK_REOPEN = 52;

    private Activity mContext;
	private String jrFormId;
	private Uri contentUri;
    private AlertDialog xformUnfinishedDialog;
    private boolean formUnFinished;
    private String xmlFilePath;
    private OdkFormResultListener formResultListener;

    private String metaInstanceName;
    private String lastUpdatedDate;

	public FormUtilities(Activity context) {
		this.mContext = context;
	}
	
	public void loadForm(final FilledForm filledForm) {
		
		new OdkGeneratedFormLoadTask(mContext, filledForm, new OdkFormLoadListener() {
            public void onOdkFormLoadSuccess(Uri contentUri) {
            	//Log.d("contenturi", contentUri+"");
            	
            	Cursor cursor = getCursorForFormsProvider(filledForm.getFormName());
                if (cursor.moveToFirst()) {
                    jrFormId = cursor.getString(0);
                    //Log.d("form",""+jrFormId+", v = "+cursor.getString(2));
                }
                cursor.close();
                
                FormUtilities.this.contentUri = contentUri;
                
                mContext.startActivityForResult(new Intent(Intent.ACTION_EDIT, contentUri), SELECTED_ODK_FORM);
            }

            public void onOdkFormLoadFailure() {
                //Toast.makeText(MainActivity.this, "Cant open ODK Form", 4000);
            	//Log.d("Cant open ODK Form", "odk");
            	createXFormNotFoundDialog();
            }
        }).execute();
    }

    public void loadForm(String contentUriAsString){
        contentUri = Uri.parse(contentUriAsString);
        loadForm(contentUri);
    }

    public void loadForm(FilledForm filledForm, String contentUriAsString){
        this.contentUri = Uri.parse(contentUriAsString);
        this.metaInstanceName = "";
        this.lastUpdatedDate = "";

        new OdkGeneratedFormLoadTask(mContext, filledForm, contentUri, new OdkFormLoadListener() {
            public void onOdkFormLoadSuccess(Uri contentUri) {
                FormUtilities.this.contentUri = contentUri;
                mContext.startActivityForResult(new Intent(Intent.ACTION_EDIT, contentUri), SELECTED_ODK_FORM);
            }

            public void onOdkFormLoadFailure() {
                createXFormNotFoundDialog();
            }
        }).execute();
    }

    public void loadForm(Uri content_uri){

        new OdkGeneratedFormLoadTask(mContext, content_uri, new OdkFormLoadListener() {
            public void onOdkFormLoadSuccess(Uri contentUri) {
                FormUtilities.this.contentUri = contentUri;
                mContext.startActivityForResult(new Intent(Intent.ACTION_EDIT, contentUri), SELECTED_ODK_FORM);
            }

            public void onOdkFormLoadFailure() {
                createXFormNotFoundDialog();
            }
        }).execute();

    }

    private void createXFormNotFoundDialog() {
        //xFormNotFound = true;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
          alertDialogBuilder.setTitle(mContext.getString(R.string.warning_lbl));
          alertDialogBuilder.setMessage(mContext.getString(R.string.odk_couldnt_open_xform_lbl));
          alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //xFormNotFound = false;
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
	
	private Cursor getCursorForFormsProvider(String name) {
    	ContentResolver resolver = mContext.getContentResolver();
        return resolver.query(FormsProviderAPI.FormsColumns.CONTENT_URI, new String[] {
                FormsProviderAPI.FormsColumns.JR_FORM_ID, FormsProviderAPI.FormsColumns.FORM_FILE_PATH, FormsProviderAPI.FormsColumns.JR_VERSION },
                FormsProviderAPI.FormsColumns.JR_FORM_ID + " like ?", new String[] { name + "%" }, null);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data, OdkFormResultListener listener) {

        Log.d("activityResult", "res-"+requestCode);

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
                lastUpdatedDate = StringUtil.format(ludate, "yyyy-MM-dd HH:mm:ss");

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
                formResultListener.onFormFinalized(contentUri, new File(xmlFilePath), metaInstanceName, lastUpdatedDate);
            } else {
                createUnfinishedFormDialog();
            }

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
        } else {
            Log.d("move next", "couldnt find executed form");
        }
    }

    private void createUnfinishedFormDialog() {
        formUnFinished = true;
        xformUnfinishedDialog = null;

        if (xformUnfinishedDialog == null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
            alertDialogBuilder.setTitle(mContext.getString(R.string.warning_lbl));
            alertDialogBuilder.setMessage(mContext.getString(R.string.odk_unfinished_dialog_msg));
            alertDialogBuilder.setCancelable(true);

            alertDialogBuilder.setPositiveButton(mContext.getString(R.string.odk_unfinished_button_delete), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    formUnFinished = false;
                    xformUnfinishedDialog.hide();
                    mContext.getContentResolver().delete(contentUri, InstanceProviderAPI.InstanceColumns.STATUS + "=?", new String[] { InstanceProviderAPI.STATUS_INCOMPLETE });
                    formResultListener.onDeleteForm(contentUri);
                }
            });

            alertDialogBuilder.setNeutralButton(mContext.getString(R.string.odk_unfinished_button_save), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(mContext, mContext.getString(R.string.odk_unfinished_form_saved), Toast.LENGTH_LONG);
                    //save form contentUri
                    saveUnfinalizedFile();

                    formResultListener.onFormUnFinalized(contentUri, new File(xmlFilePath), metaInstanceName, lastUpdatedDate);
                }
            });

            alertDialogBuilder.setNegativeButton(mContext.getString(R.string.odk_unfinished_button_change), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    formUnFinished = false;
                    xformUnfinishedDialog.hide();
                    mContext.startActivityForResult(new Intent(Intent.ACTION_EDIT, contentUri), SELECTED_ODK_REOPEN);
                }
            });


            xformUnfinishedDialog = alertDialogBuilder.create();
        }

        xformUnfinishedDialog.show();
    }
}
