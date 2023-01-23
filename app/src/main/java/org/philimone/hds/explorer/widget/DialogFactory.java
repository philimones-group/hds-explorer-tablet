package org.philimone.hds.explorer.widget;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDialog;

import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.philimone.hds.explorer.R;

import mz.betainteractive.utilities.StringUtil;

public class DialogFactory extends AppCompatDialog {

    private Context mContext;
    private TextView txtDialogTitle;
    private TextView txtDialogMessage;
    private EditText txtDialogInput;
    private Button btDialogOk;
    private Button btDialogYes;
    private Button btDialogNo;
    private Button btDialogCancel;

    private boolean hasInput;
    private boolean hasOkButton = true;
    private boolean hasYesButton = false;
    private boolean hasNoButton = false;
    private boolean hasCancelButton = false;

    private boolean cancelable = false;

    private String dialogTitle;
    private String dialogMessage;
    private String dialogYesText;
    private String dialogNoText;
    private String dialogCancelText;
    private String dialogOkText;

    private DialogInputType dialogInputType;

    public enum Buttons { OK, YES, NO, CANCEL };

    public enum DialogInputType { NUMBER, TEXT, LONGTEXT}

    private OnClickListener okClickListener;
    private OnYesNoClickListener yesNoClickListener;
    private OnYesNoCancelClickListener yesNoCancelClickListener;
    private OnInputTextListener inputTextListener;

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

    public static DialogFactory newInstance(Context context, @StringRes int titleResId, String message, boolean okButton, boolean yesButton, boolean noButton, boolean cancelButton){
        DialogFactory dialog = new DialogFactory(context);

        dialog.dialogTitle = context.getString(titleResId);
        dialog.dialogMessage = message;

        dialog.hasOkButton = okButton;
        dialog.hasCancelButton = cancelButton;
        dialog.hasYesButton = yesButton;
        dialog.hasNoButton = noButton;

        return dialog;
    }

    public static DialogFactory newInstance(Context context, String title, @StringRes  int messageResId, boolean okButton, boolean yesButton, boolean noButton, boolean cancelButton){
        DialogFactory dialog = new DialogFactory(context);

        dialog.dialogTitle = title;
        dialog.dialogMessage = context.getString(messageResId);

        dialog.hasOkButton = okButton;
        dialog.hasCancelButton = cancelButton;
        dialog.hasYesButton = yesButton;
        dialog.hasNoButton = noButton;

        return dialog;
    }

    public static DialogFactory newInstance(Context context, @StringRes int titleResId, @StringRes  int messageResId, boolean cancelButton){
        DialogFactory dialog = new DialogFactory(context);

        dialog.dialogTitle = context.getString(titleResId);
        dialog.dialogMessage = context.getString(messageResId);

        dialog.hasInput = true;
        dialog.hasOkButton = true;
        dialog.hasCancelButton = cancelButton;
        dialog.hasYesButton = false;
        dialog.hasNoButton = false;

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

    public static DialogFactory createMessageInfo(Context context, @StringRes int titleResId, String message){
        DialogFactory dialog = newInstance(context, titleResId, message, true, false, false, false);

        return dialog;
    }

    public static DialogFactory createMessageInfo(Context context, String title, @StringRes int messageResId){
        DialogFactory dialog = newInstance(context, title, messageResId, true, false, false, false);

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

    public static DialogFactory createNumberInput(Context context, @StringRes int titleResId, @StringRes int messageResId, OnInputTextListener listener){
        DialogFactory dialog = newInstance(context, titleResId, messageResId, true);
        dialog.inputTextListener = listener;
        dialog.dialogInputType = DialogInputType.NUMBER;
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
        this.txtDialogInput = findViewById(R.id.txtDialogInput);
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

        if (this.txtDialogInput != null) {
            //this.txtDialogInput.sett
        }

        doLayout();
    }

    private void onCancelCicked(){
        dismiss();

        if (inputTextListener != null){
            inputTextListener.onCancel();
            return;
        }

        if (yesNoCancelClickListener != null) yesNoCancelClickListener.onCancelClicked();
    }

    private void onOkClicked() {
        dismiss();

        if (hasInput){
            if (inputTextListener != null){
                String value = txtDialogInput.getText().toString();

                if (StringUtil.isBlank(value)){
                    return;
                }

                if (dialogInputType==DialogInputType.NUMBER) {
                    inputTextListener.onNumberTyped(Integer.parseInt(value));
                } else {
                    inputTextListener.onTextTyped(value);
                }
            }
            return;
        }

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

        if (txtDialogInput != null){
            txtDialogInput.setText("");
        }

        if (this.btDialogYes != null && this.dialogYesText != null) {
            this.btDialogYes.setText(dialogYesText);
            //this.btDialogYes.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, this.btDialogYes.getLayoutParams().height));
        }

        if (this.btDialogNo != null && this.dialogNoText != null) {
            this.btDialogNo.setText(dialogNoText);
            //this.btDialogNo.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, this.btDialogNo.getLayoutParams().height));
        }

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

    @Override
    public void setCancelable(boolean flag) {
        super.setCancelable(flag);
        this.cancelable = flag;
    }

    public void doLayout() {

        super.setCancelable(cancelable);

        setTexts();

        this.btDialogOk.setVisibility(hasOkButton ? View.VISIBLE : View.GONE);
        this.btDialogYes.setVisibility(hasYesButton ? View.VISIBLE : View.GONE);
        this.btDialogNo.setVisibility(hasNoButton ? View.VISIBLE : View.GONE);
        this.btDialogCancel.setVisibility(hasCancelButton ? View.VISIBLE : View.GONE);
        this.txtDialogInput.setVisibility(hasInput ? View.VISIBLE : View.GONE);

        if (dialogInputType == DialogInputType.NUMBER){
            txtDialogInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        if (dialogInputType == DialogInputType.TEXT){
            txtDialogInput.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        if (dialogInputType == DialogInputType.LONGTEXT){
            txtDialogInput.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        }

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

    public interface OnInputTextListener {
        void onNumberTyped(Integer value);

        void onTextTyped(String value);

        void onCancel();
    }
}
