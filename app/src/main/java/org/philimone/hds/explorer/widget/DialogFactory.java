package org.philimone.hds.explorer.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.io.SyncEntityResult;

public class DialogFactory extends Dialog {

    private Context mContext;
    private TextView txtDialogTitle;
    private TextView txtDialogMessage;
    private Button btDialogOk;
    private Button btDialogYes;
    private Button btDialogNo;
    private Button btDialogCancel;

    private boolean hasOkButton = true;
    private boolean hasYesButton = false;
    private boolean hasNoButton = false;
    private boolean hasCancelButton = false;

    private String dialogTitle;
    private String dialogMessage;
    private String dialogYesText;
    private String dialogNoText;
    private String dialogCancelText;
    private String dialogOkText;

    public enum Buttons { OK, YES, NO, CANCEL };

    private OnClickListener okClickListener;
    private OnYesNoClickListener yesNoClickListener;
    private OnYesNoCancelClickListener yesNoCancelClickListener;

    public DialogFactory(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public static DialogFactory newInstance(Context context, String title, String message, boolean okButton, boolean yesButton, boolean noButton, boolean cancelButton){
        DialogFactory dialog = new DialogFactory(context);

        dialog.dialogTitle = title;
        dialog.dialogMessage = message;

        dialog.hasOkButton = okButton;
        dialog.hasCancelButton = cancelButton;
        dialog.hasYesButton = yesButton;
        dialog.hasNoButton = noButton;

        return dialog;
    }

    public static DialogFactory newInstance(Context context, @StringRes int titleResId, @StringRes  int messageResId, boolean okButton, boolean yesButton, boolean noButton, boolean cancelButton){
        DialogFactory dialog = new DialogFactory(context);

        dialog.dialogTitle = context.getString(titleResId);
        dialog.dialogMessage = context.getString(messageResId);

        dialog.hasOkButton = okButton;
        dialog.hasCancelButton = cancelButton;
        dialog.hasYesButton = yesButton;
        dialog.hasNoButton = noButton;

        return dialog;
    }

    public static DialogFactory createMessageInfo(Context context, String title, String message){
        DialogFactory dialog = newInstance(context, title, message, true, false, false, false);

        return dialog;
    }

    public static DialogFactory createMessageInfo(Context context, @StringRes int titleResId, @StringRes  int messageResId){
        DialogFactory dialog = newInstance(context, titleResId, messageResId, true, false, false, false);

        return dialog;
    }

    public static DialogFactory createMessageInfo(Context context, String title, String message, OnClickListener okListener){
        DialogFactory dialog = newInstance(context, title, message, true, false, false, false);
        dialog.okClickListener = okListener;
        return dialog;
    }

    public static DialogFactory createMessageInfo(Context context, @StringRes int titleResId, @StringRes int messageResId, OnClickListener okListener){
        DialogFactory dialog = newInstance(context, titleResId, messageResId, true, false, false, false);
        dialog.okClickListener = okListener;
        return dialog;
    }

    public static DialogFactory createMessageYN(Context context, String title, String message, OnYesNoClickListener clickListener){
        DialogFactory dialog = newInstance(context, title, message, false, true, true, false);
        dialog.yesNoClickListener = clickListener;
        return dialog;
    }

    public static DialogFactory createMessageYN(Context context, @StringRes int titleResId, @StringRes int messageResId, OnYesNoClickListener clickListener){
        DialogFactory dialog = newInstance(context, titleResId, messageResId, false, true, true, false);
        dialog.yesNoClickListener = clickListener;
        return dialog;
    }

    public static DialogFactory createMessageYNC(Context context, String title, String message, OnYesNoCancelClickListener clickListener){
        DialogFactory dialog = newInstance(context, title, message, false, true, true, true);
        dialog.yesNoCancelClickListener = clickListener;
        return dialog;
    }

    public static DialogFactory createMessageYNC(Context context, @StringRes int titleResId, @StringRes int messageResId, OnYesNoCancelClickListener clickListener){
        DialogFactory dialog = newInstance(context, titleResId, messageResId, false, true, true, true);
        dialog.yesNoCancelClickListener = clickListener;
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.message_dialog);

        initialize();
    }

    private void initialize(){
        this.txtDialogTitle = (TextView) findViewById(R.id.txtDialogTitle);
        this.txtDialogMessage = (TextView) findViewById(R.id.txtDialogMessage);
        this.btDialogOk = (Button) findViewById(R.id.btDialogOk);
        this.btDialogYes = (Button) findViewById(R.id.btDialogYes);
        this.btDialogNo = (Button) findViewById(R.id.btDialogNo);
        this.btDialogCancel = (Button) findViewById(R.id.btDialogCancel);

        if (this.btDialogOk != null)
            this.btDialogOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onOkClicked();
                }
            });

        if (this.btDialogYes != null)
            this.btDialogYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onYesClicked();
                }
            });

        if (this.btDialogNo != null)
            this.btDialogNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNoClicked();
                }
            });

        if (this.btDialogCancel != null)
            this.btDialogCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCancelCicked();
                }
            });

        doLayout();
    }

    private void onCancelCicked(){
        dismiss();
        if (yesNoCancelClickListener != null) yesNoCancelClickListener.onCancelClicked();
    }

    private void onOkClicked() {
        dismiss();
        if (okClickListener != null) okClickListener.onClicked(Buttons.OK);
    }

    private void onNoClicked() {
        dismiss();
        if (yesNoClickListener != null) yesNoClickListener.onNoClicked();
        if (yesNoCancelClickListener != null) yesNoCancelClickListener.onNoClicked();
    }

    private void onYesClicked() {
        dismiss();
        if (yesNoClickListener != null) yesNoClickListener.onYesClicked();
        if (yesNoCancelClickListener != null) yesNoCancelClickListener.onYesClicked();

    }

    public void setTexts(){

        if (this.txtDialogTitle != null){
            this.txtDialogTitle.setText(this.dialogTitle);
            this.txtDialogMessage.setText(this.dialogMessage);
        }

        if (this.btDialogYes != null && this.dialogYesText != null)
            this.btDialogYes.setText(dialogYesText);

        if (this.btDialogNo != null && this.dialogNoText != null)
            this.btDialogNo.setText(dialogNoText);

        if (this.btDialogCancel != null && this.dialogCancelText != null)
            this.btDialogCancel.setText(dialogCancelText);

        if (this.btDialogOk != null && this.dialogOkText != null)
            this.btDialogOk.setText(dialogOkText);
    }

    public void setDialogTitle(String title){
        this.dialogTitle = title;
        setTexts();
    }

    public void setDialogMessage(String message){
        this.dialogMessage = message;
        setTexts();
    }

    public void setYesText(String text){
        this.dialogYesText = text;
    }

    public void setYesText(@StringRes int textResId){
        this.dialogYesText = getContext().getString(textResId);
    }

    public void setNoText(String text){
        this.dialogNoText = text;
    }

    public void setNoText(@StringRes int textResId){
        this.dialogNoText = getContext().getString(textResId);
    }

    public void setCancelText(String text){
        this.dialogCancelText = text;
    }

    public void setCancelText(@StringRes int textResId){
        this.dialogCancelText = getContext().getString(textResId);
    }

    public void setOkText(String text){
        this.dialogOkText = text;
    }

    public void setOkText(@StringRes int textResId){
        this.dialogOkText = getContext().getString(textResId);
    }

    public void doLayout() {

        setCancelable(false);

        setTexts();

        this.btDialogOk.setVisibility(hasOkButton ? View.VISIBLE : View.GONE);
        this.btDialogYes.setVisibility(hasYesButton ? View.VISIBLE : View.GONE);
        this.btDialogNo.setVisibility(hasNoButton ? View.VISIBLE : View.GONE);
        this.btDialogCancel.setVisibility(hasCancelButton ? View.VISIBLE : View.GONE);

    }

    public interface OnClickListener {
        void onClicked(Buttons clickedButton);
    }

    public interface OnYesNoClickListener {
        void onYesClicked();

        void onNoClicked();
    }

    public interface OnYesNoCancelClickListener {
        void onYesClicked();

        void onNoClicked();

        void onCancelClicked();
    }

    public interface OnAllClickListener {
        void onOkClicked();

        void onYesClicked();

        void onNoClicked();

        void onCancelClicked();
    }
}
