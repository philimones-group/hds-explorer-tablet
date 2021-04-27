package org.philimone.hds.explorer.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.io.SyncEntityResult;

public class SyncResultDialog extends Dialog {
    private Context mContext;
    private LinearLayout syncResultMainItems;
    private TextView syncTextResult;
    private Button btProgressOk;

    private boolean okButtonEnabled;

    private SyncEntityResult syncResult;

    public SyncResultDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sync_result_dialog);

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

    public void setSyncResult(SyncEntityResult syncResult) {
        this.syncResult = syncResult;
    }

    public void clean(){
        this.syncResult = null;
        cleanLayouts();
    }

    public void doLayout() {

        setButtonEnabled(true);
        setCancelable(false);

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (syncResult != null){
            syncResult.reportsMap.forEach( (entity, syncEntityReports) -> {

                //create a item main layout
                View mainItemView = inflater.inflate(R.layout.sync_result_item_main, null);
                TextView syncItemMainHeader = mainItemView.findViewById(R.id.syncItemMainHeader);
                LinearLayout syncItemChildsLayout = mainItemView.findViewById(R.id.syncItemChildsLayout);

                //clean default layouts
                syncItemChildsLayout.removeAllViews();

                //create the child layouts
                syncEntityReports.forEach( syncEntityReport -> {

                    View childItemView = inflater.inflate(R.layout.sync_result_item_child, null);
                    View iconOnView = childItemView.findViewById(R.id.iconOn);
                    View iconOffView = childItemView.findViewById(R.id.iconOff);
                    TextView syncTxtMessage = childItemView.findViewById(R.id.syncTxtMessage);
                    TextView syncTxtSize = childItemView.findViewById(R.id.syncTxtSize);

                    //set child values
                    String errorMessage = syncEntityReport.isSuccessStatus() ? "" : "\n"+syncEntityReport.getErrorMessage();

                    iconOnView.setVisibility(syncEntityReport.isSuccessStatus() ? View.VISIBLE : View.GONE);
                    iconOffView.setVisibility(syncEntityReport.isSuccessStatus() ? View.GONE : View.VISIBLE);
                    syncTxtMessage.setText(syncEntityReport.getMessage()+errorMessage);
                    syncTxtSize.setText(syncEntityReport.getSize());

                    //add to item main layout
                    syncItemChildsLayout.addView(childItemView);
                });

                //set header value
                Log.d("sync-entity", ""+entity);

                syncItemMainHeader.setText(this.mContext.getString(entity.getNameId()));


                //add to main items
                this.syncResultMainItems.addView(mainItemView);
            });

            //set result text message
            syncTextResult.setText(syncResult.result);
        }


    }
}
