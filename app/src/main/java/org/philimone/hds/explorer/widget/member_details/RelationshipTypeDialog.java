package org.philimone.hds.explorer.widget.member_details;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.philimone.hds.explorer.R;

public class RelationshipTypeDialog extends DialogFragment {

    private FragmentManager fragmentManager;

    private Spinner spnRelationshipType;
    private Button btDialogSelect;
    private Button btDialogCancel;

    private OnClickListener listener;

    public RelationshipTypeDialog() {
        super();
    }

    public static RelationshipTypeDialog createDialog(FragmentManager fm, OnClickListener listener){
        RelationshipTypeDialog dialog = new RelationshipTypeDialog();

        dialog.fragmentManager = fm;
        dialog.listener = listener;

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.relationship_type_dialog, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        initialize(view);
    }

    private void initialize(View view){
        this.spnRelationshipType = (Spinner) view.findViewById(R.id.spnRelationshipType);
        this.btDialogSelect = (Button) view.findViewById(R.id.btDialogSelect);
        this.btDialogCancel = (Button) view.findViewById(R.id.btDialogCancel);

        if (this.btDialogSelect != null)
            this.btDialogSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSelectClicked();
                }
            });

        if (this.btDialogCancel != null)
            this.btDialogCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCancelCicked();
                }
            });

        //initialize spinner
        initializeSpinners();
    }

    private void initializeSpinners(){
        String[] types = getContext().getResources().getStringArray(R.array.relationship_types_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), R.layout.spinner_list_item, types);
        spnRelationshipType.setAdapter(adapter);
    }

    private void onCancelCicked(){
        dismiss();
        if (listener != null) listener.onCancelClicked();
    }

    private void onSelectClicked() {
        dismiss();

        int selected = spnRelationshipType.getSelectedItemPosition();

        if (listener != null) listener.onTypeSelected(selected);
    }

    public void show(){
        this.show(fragmentManager, "relatype");
    }

    public interface OnClickListener {
        void onTypeSelected(int type);

        void onCancelClicked();
    }
}
