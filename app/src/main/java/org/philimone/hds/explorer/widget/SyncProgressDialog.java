package org.philimone.hds.explorer.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.philimone.hds.explorer.R;

import java.util.ArrayList;
import java.util.List;

public class SyncProgressDialog extends Dialog {
    private Context mContext;
    private LinearLayout layoutStatus;
    private ProgressBar progressBar;
    private TextView progressMessage;
    private Button btProgressOk;

    private boolean okButtonEnabled;

    private List<String> synchronizedMessages;

    public SyncProgressDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
        this.synchronizedMessages = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sync_progress);


        layoutStatus = (LinearLayout) findViewById(R.id.layoutStatus);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressMessage = (TextView) findViewById(R.id.progressMessage);
        btProgressOk = (Button) findViewById(R.id.btProgressOk);

        btProgressOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOkClick();
            }
        });

        if (btProgressOk != null){
            btProgressOk.setEnabled(okButtonEnabled);
        }

    }

    public void syncInitialize(){
        clearSynchronizedMessages();

        if (this.progressBar != null){
            this.progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void syncFinalize(){
        hideProgressBar();
    }

    public void hideProgressBar(){
        if (this.progressBar != null){
            this.progressBar.setVisibility(View.GONE);
        }
    }

    public void setButtonEnabled(boolean enable){
        this.okButtonEnabled = enable;

        if (this.btProgressOk != null){
            this.btProgressOk.setEnabled(enable);
        }
    }

    private void onOkClick() {
        dismiss();
    }

    /**
     * Current synchronization Message
     * @param msg
     */
    public void setMessage(String msg){
        if (progressMessage != null){
            progressMessage.setText(msg);
        }
    }

    public void clearSynchronizedMessages(){
        this.synchronizedMessages.clear();

        if (this.layoutStatus != null){
            this.layoutStatus.removeAllViews();
        }
    }

    public void addSynchronizedMessage(String msg, boolean successfull){

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView = inflater.inflate(R.layout.sync_progress_item, null);

        View iconOnView = itemView.findViewById(R.id.iconOn);
        View iconOffView = itemView.findViewById(R.id.iconOff);
        TextView txtMessage = (TextView) itemView.findViewById(R.id.txtMessage);

        iconOnView.setVisibility(successfull ? View.VISIBLE : View.GONE);
        iconOffView.setVisibility(successfull ? View.GONE : View.VISIBLE);

        txtMessage.setText(msg);

        if (layoutStatus != null){
            layoutStatus.addView(itemView);
        }
    }

}
