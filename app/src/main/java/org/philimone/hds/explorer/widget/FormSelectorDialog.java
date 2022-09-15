package org.philimone.hds.explorer.widget;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.FormLoaderAdapter;
import org.philimone.hds.explorer.adapter.trackinglist.TrackingListAdapter;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.enums.FormType;

import java.util.ArrayList;
import java.util.List;

public class FormSelectorDialog extends DialogFragment {

    private FragmentManager fragmentManager;

    private TextView txtDialogTitle;
    private EditText txtFilterName;
    private RecyclerListView lvFormsList;
    private Button btDialogBack;


    private FormLoaderAdapter adapter;
    private List<FormDataLoader> formsList;

    private OnFormSelectedListener listener;

    public FormSelectorDialog() {
        super();
    }

    public static FormSelectorDialog createDialog(FragmentManager fm, List<FormDataLoader> formsList, OnFormSelectedListener listener){
        FormSelectorDialog dialog = new FormSelectorDialog();

        dialog.fragmentManager = fm;
        dialog.listener = listener;
        dialog.formsList = new ArrayList<>(formsList);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.form_selector, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialize(view);
    }

    private void initialize(View view){
        this.txtDialogTitle = view.findViewById(R.id.txtDialogTitle);
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
                    FormDataLoader formDataLoader = adapter.getItem(position);
                    onSelectedForm(formDataLoader);
                }

                @Override
                public void onItemLongClick(View view, int position, long id) {

                }
            });
        }

        //choose title
        if (formsList != null && formsList.size()>0) {
            FormDataLoader dataLoader = formsList.get(0);

            if (dataLoader != null && dataLoader.getForm() != null) {
                Form form = dataLoader.getForm();

                @StringRes int titleEntityId = form.isMemberForm() ? R.string.form_subject_member_lbl : form.isHouseholdForm ? R.string.form_subject_household_lbl : form.isRegionForm() ? R.string.form_subject_region_lbl : R.string.invalid_enum_value;

                if (titleEntityId != R.string.invalid_enum_value) {
                    String title = getString(R.string.member_details_forms_selector_lbl) + " ("+ getString(titleEntityId) +")";
                    this.txtDialogTitle.setText(title);
                }
            }
        }

        this.adapter = new FormLoaderAdapter(getActivity(), this.formsList);
        this.lvFormsList.setAdapter(this.adapter);

    }

    private void filterFormsByCode(String name) {
        if (name != null){
            FormLoaderAdapter adapter = (FormLoaderAdapter) this.lvFormsList.getAdapter();
            adapter.filterForms(name);
        }
    }

    private void onBackCicked(){
        dismiss();
        if (listener != null) listener.onCancelClicked();
    }

    private void onSelectedForm(FormDataLoader formDataLoader) {
        dismiss();

        if (listener != null) {

            if (formDataLoader.getForm().formType == FormType.REGULAR) {
                listener.onFormSelected(formDataLoader);
            } else {
                listener.onFormGroupSelected(formDataLoader);
            }
        }
    }

    public void show(){
        this.show(fragmentManager, "relatype");
    }

    public interface OnFormSelectedListener {
        void onFormSelected(FormDataLoader formDataLoader);

        void onFormGroupSelected(FormDataLoader formDataLoader);

        void onCancelClicked();
    }
}
