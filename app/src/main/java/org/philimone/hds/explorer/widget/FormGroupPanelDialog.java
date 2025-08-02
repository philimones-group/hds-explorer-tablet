package org.philimone.hds.explorer.widget;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.FormGroupChildAdapter;
import org.philimone.hds.explorer.adapter.model.FormGroupChildItem;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.FormGroupInstance;
import org.philimone.hds.explorer.model.FormGroupInstanceChild;
import org.philimone.hds.explorer.model.FormGroupMapping;
import org.philimone.hds.explorer.model.FormSubject;
import org.philimone.hds.explorer.model.enums.FormCollectType;
import org.philimone.hds.explorer.utilities.FormGroupUtilities;
import mz.betainteractive.utilities.StringUtil;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

public class FormGroupPanelDialog extends DialogFragment {

    private FragmentManager fragmentManager;

    private Context mContext;

    private TextView txtFormGroupUuid;
    private TextView txtFormGroupCode;
    private EditText txtFilterName;
    private RecyclerListView lvFormsList;
    private Button btDialogBack;


    private FormGroupChildAdapter adapter;
    private List<FormGroupChildItem> formsList;
    private List<FormDataLoader> formDataLoaders;

    private Box<CollectedData> boxCollectedData;
    private Box<FormGroupInstance> boxFormGroupInstances;

    private FormSubject subject;
    private Form formGroup;
    private FormGroupInstance formGroupInstance;

    private FormGroupUtilities formGroupUtilities;

    private OnFormSelectedListener listener;

    public FormGroupPanelDialog(FormGroupUtilities formGroupUtilities) {
        super();

        this.formGroupUtilities = formGroupUtilities;
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxFormGroupInstances = ObjectBoxDatabase.get().boxFor(FormGroupInstance.class);
    }

    public static FormGroupPanelDialog createDialog(Context context, FragmentManager fm, FormGroupUtilities formGroupUtilities, FormSubject subject, Form form, List<FormDataLoader> formsList, FormGroupInstance formGroupInstance, OnFormSelectedListener listener){
        FormGroupPanelDialog dialog = new FormGroupPanelDialog(formGroupUtilities);

        dialog.mContext = context;
        dialog.fragmentManager = fm;
        dialog.formGroupUtilities = formGroupUtilities;
        dialog.listener = listener;
        dialog.subject = subject;
        dialog.formGroup = form;
        dialog.formDataLoaders = formsList;
        dialog.formGroupInstance = formGroupInstance;
        dialog.prepareAdapterData();

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.form_group_selector, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialize(view);
    }

    private void prepareAdapterData() {
        formsList = new ArrayList<>();

        for (FormDataLoader childFormLoader : this.formDataLoaders) {
            Form childForm = childFormLoader.getForm();
            FormGroupMapping childGroupMapping = null;
            FormGroupInstanceChild childGroupInstance = null;
            CollectedData childCollectedData = getCollectedData(childForm, subject, formGroupInstance);

            for (FormGroupMapping gmapp : formGroup.groupMappings) {
                if (gmapp.formId.equals(childForm.formId)) {
                    childGroupMapping = gmapp;
                    break;
                }
            }

            for (FormGroupInstanceChild instanceChild : formGroupInstance.instanceChilds) {
                if (instanceChild.formId.equals(childForm.formId)) {
                    childGroupInstance = instanceChild;
                    break;
                }
            }

            FormGroupChildItem childItem = new FormGroupChildItem(childFormLoader, childGroupMapping, childGroupInstance, childCollectedData);
            formsList.add(childItem);

            if (formsList.size()>1) {
                FormGroupChildItem previousItem = formsList.get(formsList.size()-2);
                childItem.setPreviousItem(previousItem);
            }

        }
    }

    public void reloadPanelData() {
        formsList.clear();
        this.adapter = null;
        prepareAdapterData();

        this.adapter = new FormGroupChildAdapter(getActivity(), this.formsList);
        this.lvFormsList.setAdapter(this.adapter);

        System.gc();
    }

    private CollectedData getCollectedData(Form form,  FormSubject formSubject, FormGroupInstance groupInstance){

        List<CollectedData> collectedData = this.boxCollectedData.query(CollectedData_.formId.equal(form.formId)
                                                                   .and(CollectedData_.recordId.equal(formSubject.getId())).and(CollectedData_.recordEntity.equal(formSubject.getTableName().code))
                                                                   .and(CollectedData_.formGroupInstanceUuid.equal(groupInstance.instanceUuid)))
                                                                   .orderDesc(CollectedData_.formLastUpdatedDate)
                                                                   .build().find();

        if (collectedData != null && collectedData.size()>0){
            return collectedData.get(0);
        }

        return null;
    }

    private void initialize(View view){
        this.txtFormGroupUuid = view.findViewById(R.id.txtFormGroupUuid);
        this.txtFormGroupCode = view.findViewById(R.id.txtFormGroupCode);
        this.txtFilterName = view.findViewById(R.id.txtFilterName);
        this.lvFormsList = view.findViewById(R.id.lvFormsList);
        this.btDialogBack = (Button) view.findViewById(R.id.btDialogBack);


        if (this.btDialogBack != null)
            this.btDialogBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackCicked();
                }
            });

        if (this.txtFilterName != null)
            this.txtFilterName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    filterFormsByCode(s.toString());
                }
            });


        if (this.lvFormsList != null){
            this.lvFormsList.addOnItemClickListener(new RecyclerListView.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position, long id) {
                    FormGroupChildItem childItem = adapter.getItem(position);
                    onSelectedForm(childItem);
                }

                @Override
                public void onItemLongClick(View view, int position, long id) {

                }
            });
        }

        this.txtFormGroupUuid.setText(formGroupInstance.instanceUuid);
        this.txtFormGroupCode.setText(formGroupInstance.instanceCode);

        this.adapter = new FormGroupChildAdapter(getActivity(), this.formsList);
        this.lvFormsList.setAdapter(this.adapter);

    }

    private void filterFormsByCode(String name) {
        if (name != null){
            FormGroupChildAdapter adapter = (FormGroupChildAdapter) this.lvFormsList.getAdapter();
            adapter.filterForms(name);
        }
    }

    private void onBackCicked(){
        dismiss();
        if (listener != null) listener.onCancelClicked();
    }

    private void onSelectedForm(FormGroupChildItem childItem) {

        this.formGroupInstance = boxFormGroupInstances.get(this.formGroupInstance.id);

        //validateForm();
        FormGroupMapping formGroupMapping = childItem.getFormGroupMapping();

        if (childItem.getCollectedData() == null) {
            Log.d("colectype", ""+formGroupMapping.formCollectType);
            //previous must be finalized
            if (formGroupMapping.formCollectType == FormCollectType.PREVIOUS_FORM_COLLECTED) {
                FormGroupChildItem previousChildItem = childItem.getPreviousItem();

                if (previousChildItem != null) {

                    if (previousChildItem.getCollectedData() == null) {
                        //Previous Form is not collected yet, collect it first
                        String formName = previousChildItem.getForm().getFormName();
                        DialogFactory.createMessageInfo(this.mContext, R.string.form_group_selector_validation_lbl, this.mContext.getString(R.string.form_group_selector_validation_prev_must_be_collected_lbl, formName)).show();
                        return;
                    } else if (!previousChildItem.getCollectedData().isFormFinalized()) {
                        //Form Not Finalized, finalize it first
                        String formName = previousChildItem.getForm().getFormName();
                        DialogFactory.createMessageInfo(this.mContext, R.string.form_group_selector_validation_lbl, this.mContext.getString(R.string.form_group_selector_validation_prev_must_be_finalized_lbl, formName)).show();
                        return;
                    }
                }
            }

            if (formGroupMapping.formCollectType == FormCollectType.CALCULATE_EXPRESSION) {
                //get expression and calculate
                //translate variables (get form previous form values)
                Log.d("calculate", "expression");
                FormGroupUtilities.ExTranslationResult trResult = formGroupUtilities.translateExpression(formGroupMapping.formCollectCondition, this.formGroupInstance);
                String expression = trResult.translatedExpression;
                String label = formGroupMapping.formCollectLabel;

                if (trResult.status == FormGroupUtilities.TranslationStatus.SUCCESS) {

                    String result = formGroupUtilities.evaluateExpression(expression).toString();
                    boolean openForm = StringUtil.isBlank(result) ? true : result.equalsIgnoreCase("true");

                    if (openForm == false) {

                        String message = mContext.getString(R.string.form_group_selector_validation_form_collect_condition_error_lbl, label == null ? "" : label);
                        DialogFactory.createMessageInfo(this.mContext, R.string.form_group_selector_validation_lbl, message).show();

                        return;
                    }
                } else {
                    if (trResult.status == FormGroupUtilities.TranslationStatus.ERROR_DEPENDENCY_NOT_FOUND) {
                        DialogFactory.createMessageInfo(this.mContext, R.string.form_group_selector_validation_lbl, R.string.form_group_selector_validation_form_collect_condition_dependency_not_found_error_lbl).show();
                        return;
                    }

                    if (trResult.status == FormGroupUtilities.TranslationStatus.ERROR_DEPENDENCY_FORM_NOT_FINALIZED) {
                        String formId = trResult.affectedChild!=null ? trResult.affectedChild.formId : "NULL";
                        String formName = getFormName(formId);
                        String message = this.mContext.getString(R.string.form_group_selector_validation_form_collect_condition_dependency_not_finalized_error_lbl, formName);
                        DialogFactory.createMessageInfo(this.mContext, R.string.form_group_selector_validation_lbl, message).show();
                        return;
                    }

                    return;
                }
            }
        }

        //do not dismiss this dialog - it will be refreshed after odk collection
        //dismiss();

        if (listener != null) listener.onFormSelected(childItem);
    }

    private String getFormName(String formId) {
        for (FormGroupChildItem item : formsList) {
            Form form = item.getForm();
            if (form != null && form.formId.equals(formId)) {
                return  form.formName;
            }
        }
        return formId;
    }

    public void show(){
        this.show(fragmentManager, "relatype");
    }

    public interface OnFormSelectedListener {
        void onFormSelected(FormGroupChildItem childItem);

        void onCancelClicked();
    }
}
