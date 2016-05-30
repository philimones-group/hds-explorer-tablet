package mz.betainteractive.odk;


import mz.betainteractive.odk.listener.OdkFormLoadListener;
import mz.betainteractive.odk.model.FilledForm;
import mz.betainteractive.odk.task.OdkGeneratedFormLoadTask;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import net.manhica.clip.explorer.R;

public class FormUtilities {
	private Context mContext;
	private String jrFormId;
	private Uri contentUri;
	
	public FormUtilities(Context context) {
		this.mContext = context;
	}
	
	public void loadForm(final FilledForm filledForm) {
		
		new OdkGeneratedFormLoadTask(mContext, filledForm, new OdkFormLoadListener() {
            public void onOdkFormLoadSuccess(Uri contentUri) {
            	//Log.d("contenturi", contentUri+"");
            	
            	Cursor cursor = getCursorForFormsProvider(filledForm.getFormName());
                if (cursor.moveToFirst()) {
                    jrFormId = cursor.getString(0);
                }
                
                FormUtilities.this.contentUri = contentUri;
                
                mContext.startActivity(new Intent(Intent.ACTION_EDIT, contentUri));
            }

            public void onOdkFormLoadFailure() {
                //Toast.makeText(MainActivity.this, "Cant open ODK Form", 4000);
            	//Log.d("Cant open ODK Form", "odk");
            	createXFormNotFoundDialog();
            }
        }).execute();
    }
	
    private void createXFormNotFoundDialog() {
        //xFormNotFound = true;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
          alertDialogBuilder.setTitle(mContext.getString(R.string.warning_lbl));
          alertDialogBuilder.setMessage(mContext.getString(R.string.couldnt_open_xform_lbl));
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
                FormsProviderAPI.FormsColumns.JR_FORM_ID, FormsProviderAPI.FormsColumns.FORM_FILE_PATH },
                FormsProviderAPI.FormsColumns.JR_FORM_ID + " like ?", new String[] { name + "%" }, null);
    }
}
