package net.manhica.dss.explorer.fragment;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import net.manhica.clip.explorer.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MemberFilterFragment extends Fragment {

    private EditText txtMemFilterName;
    private EditText txtMemFilterPermId;
    private CheckBox chkMemFilterGFemale;
    private CheckBox chkMemFilterGMale;
    private CheckBox chkMemFilter1cp;
    private CheckBox chkMemFilter2hd;
    private CheckBox chkMemFilter3hf;
    private Button btMemFilterClear;
    private Button btMemFilterSearch;

    private Listener listener;

    public MemberFilterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.member_filter, container, false);

        initialize(view);
        return view;
    }

    private void initialize(View view) {
        if (getActivity() instanceof Listener){
            this.listener = (Listener) getActivity();
        }

        this.txtMemFilterName = (EditText) view.findViewById(R.id.txtMemFilterName);
        this.txtMemFilterPermId = (EditText) view.findViewById(R.id.txtMemFilterPermId);
        this.chkMemFilterGFemale = (CheckBox) view.findViewById(R.id.chkMemFilterGFemale);
        this.chkMemFilterGMale = (CheckBox) view.findViewById(R.id.chkMemFilterGMale);
        this.chkMemFilter1cp = (CheckBox) view.findViewById(R.id.chkMemFilter1);
        this.chkMemFilter2hd = (CheckBox) view.findViewById(R.id.chkMemFilter2);
        this.chkMemFilter3hf = (CheckBox) view.findViewById(R.id.chkMemFilter3);
        this.btMemFilterClear = (Button) view.findViewById(R.id.btMemFilterClear);
        this.btMemFilterSearch = (Button) view.findViewById(R.id.btMemFilterSearch);

        this.btMemFilterClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
            }
        });

        this.btMemFilterSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearch();
            }
        });

        if (txtMemFilterPermId.getText().length()>0){
            onSearch();
        }
    }

    private void onSearch() {
        String name = txtMemFilterName.getText().toString();
        String permid = txtMemFilterPermId.getText().toString();
        String gender = (chkMemFilterGMale.isChecked() && chkMemFilterGFemale.isChecked()) ? "" : chkMemFilterGMale.isChecked() ? "M" : chkMemFilterGFemale.isChecked() ? "F" : "";
        boolean filter1 = chkMemFilter1cp.isChecked();
        boolean filter2 = chkMemFilter2hd.isChecked();
        boolean filter3 = chkMemFilter3hf.isChecked();

        listener.onSearch(name, permid, gender, filter1, filter2, filter3);
    }

    private void clear(){
        this.txtMemFilterName.setText("");
        this.txtMemFilterPermId.setText("");
        this.chkMemFilterGMale.setChecked(false);
        this.chkMemFilterGFemale.setChecked(true);
        this.chkMemFilter1cp.setChecked(false);
        this.chkMemFilter2hd.setChecked(false);
        this.chkMemFilter3hf.setChecked(false);
    }

    public interface Listener {
        void onSearch(String name, String permId, String gender, boolean isPregnant, boolean hasDelivered, boolean hasFacility);
    }
}
