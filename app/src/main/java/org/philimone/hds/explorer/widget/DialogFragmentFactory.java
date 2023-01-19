package org.philimone.hds.explorer.widget;


import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.philimone.hds.explorer.R;

import mz.betainteractive.utilities.StringUtil;

public class DialogFragmentFactory extends DialogFragment {

    private FragmentManager fragmentManager;

    private Context mContext;
    private TextView txtDialogTitle;
    private TextView txtDialogMessage;
    private EditText txtDialogInput;
    private Button btDialogOk;
    private Button btDialogYes;
    private Button btDialogNo;
    private Button btDialogCancel;

    private @LayoutRes int dialogLayoutResId;

    private boolean hasInput;
    private boolean hasOkButton = true;
    private boolean hasYesButton = false;
    private boolean hasNoButton = false;
    private boolean hasCancelButton = false;
    private boolean cancelable = false;

    private @StringRes int dialogTitleResId;
    private @StringRes int dialogMessageResId;
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

    public DialogFragmentFactory(FragmentManager fragmentManager) {
        super();
        this.fragmentManager = fragmentManager;
    }

    public static DialogFragmentFactory newInstance(FragmentManager fragManager, @LayoutRes int dialogLayoutResId, OnYesNoClickListener listener) {
        DialogFragmentFactory dialog = new DialogFragmentFactory(fragManager);
        dialog.dialogLayoutResId = dialogLayoutResId;
        dialog.yesNoClickListener = listener;
        dialog.hasOkButton = false;
        dialog.hasCancelButton = false;
        dialog.hasYesButton = true;
        dialog.hasNoButton = true;
        return dialog;
    }

    public static DialogFragmentFactory newInstance(FragmentManager fragManager, String title, String message, boolean okButton, boolean yesButton, boolean noButton, boolean cancelButton){
        DialogFragmentFactory dialog = new DialogFragmentFactory(fragManager);

        dialog.dialogTitle = title;
        dialog.dialogMessage = message;

        dialog.hasOkButton = okButton;
        dialog.hasCancelButton = cancelButton;
        dialog.hasYesButton = yesButton;
        dialog.hasNoButton = noButton;

        return dialog;
    }

    public static DialogFragmentFactory newInstance(FragmentManager fragManager, @StringRes int titleResId, @StringRes  int messageResId, boolean okButton, boolean yesButton, boolean noButton, boolean cancelButton){
        DialogFragmentFactory dialog = new DialogFragmentFactory(fragManager);

        dialog.dialogTitleResId = titleResId;
        dialog.dialogMessageResId = messageResId;

        dialog.hasOkButton = okButton;
        dialog.hasCancelButton = cancelButton;
        dialog.hasYesButton = yesButton;
        dialog.hasNoButton = noButton;

        return dialog;
    }

    public static DialogFragmentFactory newInstance(FragmentManager fragManager, @StringRes int titleResId, String message, boolean okButton, boolean yesButton, boolean noButton, boolean cancelButton){
        DialogFragmentFactory dialog = new DialogFragmentFactory(fragManager);

        dialog.dialogTitleResId = titleResId;
        dialog.dialogMessage = message;

        dialog.hasOkButton = okButton;
        dialog.hasCancelButton = cancelButton;
        dialog.hasYesButton = yesButton;
        dialog.hasNoButton = noButton;

        return dialog;
    }

    public static DialogFragmentFactory newInstance(FragmentManager fragManager, String title, @StringRes  int messageResId, boolean okButton, boolean yesButton, boolean noButton, boolean cancelButton){
        DialogFragmentFactory dialog = new DialogFragmentFactory(fragManager);

        dialog.dialogTitle = title;
        dialog.dialogMessageResId = messageResId;

        dialog.hasOkButton = okButton;
        dialog.hasCancelButton = cancelButton;
        dialog.hasYesButton = yesButton;
        dialog.hasNoButton = noButton;

        return dialog;
    }

    public static DialogFragmentFactory newInstance(FragmentManager fragManager, @StringRes int titleResId, @StringRes  int messageResId, boolean cancelButton){
        DialogFragmentFactory dialog = new DialogFragmentFactory(fragManager);

        dialog.dialogTitleResId = titleResId;
        dialog.dialogMessageResId = messageResId;

        dialog.hasInput = true;
        dialog.hasOkButton = true;
        dialog.hasCancelButton = cancelButton;
        dialog.hasYesButton = false;
        dialog.hasNoButton = false;

        return dialog;
    }

    public static DialogFragmentFactory createMessageInfo(FragmentManager fragManager, String title, String message){
        DialogFragmentFactory dialog = newInstance(fragManager, title, message, true, false, false, false);

        return dialog;
    }

    public static DialogFragmentFactory createMessageInfo(FragmentManager fragManager, @StringRes int titleResId, @StringRes  int messageResId){
        DialogFragmentFactory dialog = newInstance(fragManager, titleResId, messageResId, true, false, false, false);

        return dialog;
    }

    public static DialogFragmentFactory createMessageInfo(FragmentManager fragManager, @StringRes int titleResId, String message){
        DialogFragmentFactory dialog = newInstance(fragManager, titleResId, message, true, false, false, false);

        return dialog;
    }

    public static DialogFragmentFactory createMessageInfo(FragmentManager fragManager, String title, @StringRes int messageResId){
        DialogFragmentFactory dialog = newInstance(fragManager, title, messageResId, true, false, false, false);

        return dialog;
    }

    public static DialogFragmentFactory createMessageInfo(FragmentManager fragManager, String title, String message, OnClickListener okListener){
        DialogFragmentFactory dialog = newInstance(fragManager, title, message, true, false, false, false);
        dialog.okClickListener = okListener;
        return dialog;
    }

    public static DialogFragmentFactory createMessageInfo(FragmentManager fragManager, @StringRes int titleResId, @StringRes int messageResId, OnClickListener okListener){
        DialogFragmentFactory dialog = newInstance(fragManager, titleResId, messageResId, true, false, false, false);
        dialog.okClickListener = okListener;
        return dialog;
    }

    public static DialogFragmentFactory createMessageYN(FragmentManager fragManager, String title, String message, OnYesNoClickListener clickListener){
        DialogFragmentFactory dialog = newInstance(fragManager, title, message, false, true, true, false);
        dialog.yesNoClickListener = clickListener;
        return dialog;
    }

    public static DialogFragmentFactory createMessageYN(FragmentManager fragManager, @StringRes int titleResId, @StringRes int messageResId, OnYesNoClickListener clickListener){
        DialogFragmentFactory dialog = newInstance(fragManager, titleResId, messageResId, false, true, true, false);
        dialog.yesNoClickListener = clickListener;
        return dialog;
    }

    public static DialogFragmentFactory createMessageYNC(FragmentManager fragManager, String title, String message, OnYesNoCancelClickListener clickListener){
        DialogFragmentFactory dialog = newInstance(fragManager, title, message, false, true, true, true);
        dialog.yesNoCancelClickListener = clickListener;
        return dialog;
    }

    public static DialogFragmentFactory createMessageYNC(FragmentManager fragManager, @StringRes int titleResId, @StringRes int messageResId, OnYesNoCancelClickListener clickListener){
        DialogFragmentFactory dialog = newInstance(fragManager, titleResId, messageResId, false, true, true, true);
        dialog.yesNoCancelClickListener = clickListener;
        return dialog;
    }

    public static DialogFragmentFactory createNumberInput(FragmentManager fragManager, @StringRes int titleResId, @StringRes int messageResId, OnInputTextListener listener){
        DialogFragmentFactory dialog = newInstance(fragManager, titleResId, messageResId, true);
        dialog.inputTextListener = listener;
        dialog.dialogInputType = DialogInputType.NUMBER;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setStyle(DialogFragment.STYLE_NORMAL, org.philimone.hds.forms.R.style.AppTheme);

        this.setCancelable(cancelable);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        @LayoutRes int resourceId = (dialogLayoutResId == 0) ? R.layout.message_dialog : dialogLayoutResId;

        View view = inflater.inflate(resourceId, container, false);

        Window window = getDialog().getWindow();

        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER); // Optional
        window.setBackgroundDrawableResource(R.drawable.nui_dialog_border_shadow);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.mContext = this.getContext();



        initialize(view);
    }
/*
    public void onResume()
    {
        super.onResume();
        Window window = getDialog().getWindow();

        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER); // Optional
    }*/

    private void initialize(View view){
        this.txtDialogTitle = (TextView) view.findViewById(R.id.txtDialogTitle);
        this.txtDialogMessage = (TextView) view.findViewById(R.id.txtDialogMessage);
        this.txtDialogInput = view.findViewById(R.id.txtDialogInput);
        this.btDialogOk = (Button) view.findViewById(R.id.btDialogOk);
        this.btDialogYes = (Button) view.findViewById(R.id.btDialogYes);
        this.btDialogNo = (Button) view.findViewById(R.id.btDialogNo);
        this.btDialogCancel = (Button) view.findViewById(R.id.btDialogCancel);

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

    @Override
    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        super.setCancelable(cancelable);
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

                if (dialogInputType== DialogInputType.NUMBER) {
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

        if (dialogLayoutResId != 0) {
            return;
        }

        if (this.dialogTitleResId != 0) {
            this.dialogTitle = mContext.getString(this.dialogTitleResId);
        }

        if (this.dialogMessageResId != 0) {
            this.dialogMessage = mContext.getString(this.dialogMessageResId);
        }

        if (this.txtDialogTitle != null){
            this.txtDialogTitle.setText(this.dialogTitle);
            this.txtDialogMessage.setText(this.dialogMessage);
        }

        if (txtDialogInput != null){
            txtDialogInput.setText("");
        }

        if (this.btDialogYes != null && this.dialogYesText != null) {
            this.btDialogYes.setText(dialogYesText);
            this.btDialogYes.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, this.btDialogYes.getLayoutParams().height));
        }

        if (this.btDialogNo != null && this.dialogNoText != null) {
            this.btDialogNo.setText(dialogNoText);
            this.btDialogNo.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, this.btDialogNo.getLayoutParams().height));
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

    public void show() {
        show(fragmentManager, "dfragfactory");
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
