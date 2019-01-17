package org.philimone.hds.explorer.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.philimone.hds.explorer.R;

public class LoadingDialog extends Dialog {

    private Context mContext;
    private ProgressBar progressBarLoading;
    private TextView txtLoadingMessage;


    public LoadingDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loading_dialog);

        progressBarLoading = (ProgressBar) findViewById(R.id.progressBarLoading);
        txtLoadingMessage = (TextView) findViewById(R.id.txtLoadingMessage);


        setCancelable(false);
    }

    public String getMessage(){
        return (txtLoadingMessage != null) ? txtLoadingMessage.getText().toString() : null;
    }

    public void setMessage(String message){
        if (txtLoadingMessage != null){
            txtLoadingMessage.setText(message);
        }
    }

}
