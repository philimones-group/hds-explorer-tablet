package org.philimone.hds.explorer.widget;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDialog;

import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.philimone.hds.explorer.R;

public class LoadingDialog extends AppCompatDialog {

    private Context mContext;
    private ProgressBar progressBarLoading;
    private TextView txtLoadingMessage;
    private Button btCancel;
    private String message = "";

    private Listener listener;

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
        this.btCancel = findViewById(R.id.btCancel);

        if (txtLoadingMessage != null){
            txtLoadingMessage.setText(message);
        }

        if (this.btCancel != null) {
            this.btCancel.setOnClickListener(v -> onCancelClicked());
        }

        if (btCancel != null) {
            this.btCancel.setVisibility(View.GONE);
        }
        setCancelable(false);
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(@StringRes int messageId) {
        if (txtLoadingMessage != null) {
            txtLoadingMessage.setText(messageId);
        }
    }

    public void setMessage(String message){
        this.message = message;

        if (txtLoadingMessage != null){
            txtLoadingMessage.setText(message);
        }
    }

    @Override
    public void show() {
        super.show();

        if (btCancel != null) {
            btCancel.setVisibility(View.GONE);
        }
    }

    private void onCancelClicked() {
        if (this.listener != null) {
            dismiss();
            this.listener.onButtonCancelClicked();
        }
    }

    public void showCancelButton(){        
        if (btCancel != null) {
            this.btCancel.setVisibility(View.VISIBLE);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onButtonCancelClicked();
    }

}
