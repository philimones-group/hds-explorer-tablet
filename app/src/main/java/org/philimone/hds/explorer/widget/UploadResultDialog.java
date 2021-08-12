package org.philimone.hds.explorer.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.io.SyncEntityResult;
import org.philimone.hds.explorer.io.UploadEntityResult;
import org.philimone.hds.explorer.model.CoreCollectedData;

import androidx.annotation.NonNull;

public class UploadResultDialog extends Dialog {
    private Context mContext;
    private LinearLayout syncResultMainItems;
    private TextView syncTextResult;
    private Button btProgressOk;

    private boolean okButtonEnabled;

    private UploadEntityResult uploadResult;

    public UploadResultDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sync_upload_result_dialog);

        initialize();
    }

    private void initialize(){
        this.syncResultMainItems = (LinearLayout) findViewById(R.id.syncResultMainItems);
        this.syncTextResult = (TextView) findViewById(R.id.syncTextResult);
        this.btProgressOk = (Button) findViewById(R.id.btProgressOk);

        this.btProgressOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOkButtonClicked();
            }
        });

        if (this.btProgressOk != null){
            this.btProgressOk.setEnabled(this.okButtonEnabled);
        }
    }

    private void onOkButtonClicked() {
        dismiss();
    }

    public void cleanLayouts(){
        if (this.syncResultMainItems != null){
            this.syncResultMainItems.removeAllViews();
        }
        if (this.syncTextResult != null){
            this.syncTextResult.setText("");
        }
    }

    public void setButtonEnabled(boolean enable){
        this.okButtonEnabled = enable;

        if (this.btProgressOk != null){
            this.btProgressOk.setEnabled(enable);
        }
    }

    public void setUploadResult(UploadEntityResult uploadResult) {
        this.uploadResult = uploadResult;
    }

    public void clean(){
        this.uploadResult = null;
        cleanLayouts();
    }

    public void doLayout() {

        setButtonEnabled(true);
        setCancelable(false);

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (uploadResult != null){

            uploadResult.uploadReportsMap.forEach( (entity, uploadEntityReports) -> {

                //create a item main layout
                View mainItemView = inflater.inflate(R.layout.sync_upload_result_item_main, null);
                TextView syncItemMainHeader = mainItemView.findViewById(R.id.syncItemMainHeader);
                TextView txtInstanceId = mainItemView.findViewById(R.id.txtInstanceId);
                TextView txtInstanceName = mainItemView.findViewById(R.id.txtInstanceName);
                LinearLayout syncItemChildsLayout = mainItemView.findViewById(R.id.syncItemChildsLayout);
                CoreCollectedData collectedData = uploadResult.collectedData;

                //clean default layouts
                syncItemChildsLayout.removeAllViews();

                //create the child layouts
                uploadEntityReports.forEach( uploadEntityReport -> {

                    View childItemView = inflater.inflate(R.layout.sync_result_item_child, null);
                    View iconOnView = childItemView.findViewById(R.id.iconOn);
                    View iconOffView = childItemView.findViewById(R.id.iconOff);
                    TextView syncTxtMessage = childItemView.findViewById(R.id.syncTxtMessage);
                    TextView syncTxtSize = childItemView.findViewById(R.id.syncTxtSize);

                    //set child values
                    iconOnView.setVisibility(uploadEntityReport.isSuccessStatus() ? View.VISIBLE : View.GONE);
                    iconOffView.setVisibility(uploadEntityReport.isSuccessStatus() ? View.GONE : View.VISIBLE);
                    syncTxtSize.setVisibility(View.GONE);

                    //String errorMessage = uploadEntityReport.isSuccessStatus() ? "" : "\n"+uploadEntityReport.getErrorMessage();

                    syncTxtMessage.setText(uploadEntityReport.getMessage());

                    //add to item main layout
                    syncItemChildsLayout.addView(childItemView);
                });

                //set header value
                Log.d("sync-entity", ""+entity);

                syncItemMainHeader.setText(this.mContext.getString(entity.name));
                txtInstanceId.setText(collectedData.formUuid);
                txtInstanceName.setText(collectedData.formEntityCode);

                //add to main items
                this.syncResultMainItems.addView(mainItemView);
            });




            //set result text message
            syncTextResult.setText(uploadResult.result);
        }


    }
}
