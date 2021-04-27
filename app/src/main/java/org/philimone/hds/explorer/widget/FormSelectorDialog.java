package org.philimone.hds.explorer.widget;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.FormLoaderAdapter;
import org.philimone.hds.explorer.data.FormDataLoader;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class FormSelectorDialog extends DialogFragment {

    private FragmentManager fragmentManager;

    private ListView lvFormsList;
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.form_selector, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        initialize(view);
    }

    private void initialize(View view){
        this.lvFormsList = (ListView) view.findViewById(R.id.lvFormsList);
        this.btDialogBack = (Button) view.findViewById(R.id.btDialogBack);


        if (this.btDialogBack != null)
            this.btDialogBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackCicked();
                }
            });



        if (this.lvFormsList != null){
            this.lvFormsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    FormDataLoader formDataLoader = adapter.getItem(position);
                    onSelectedForm(formDataLoader);
                }
            });
        }

        this.adapter = new FormLoaderAdapter(getActivity(), this.formsList);
        this.lvFormsList.setAdapter(this.adapter);

    }

    private void onBackCicked(){
        dismiss();
        if (listener != null) listener.onCancelClicked();
    }

    private void onSelectedForm(FormDataLoader formDataLoader) {
        dismiss();

        if (listener != null) listener.onFormSelected(formDataLoader);
    }

    public void show(){
        this.show(fragmentManager, "relatype");
    }

    public interface OnFormSelectedListener {
        void onFormSelected(FormDataLoader formDataLoader);

        void onCancelClicked();
    }
}
