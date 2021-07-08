package org.philimone.hds.explorer.fragment;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.main.BarcodeScannerActivity;
import org.philimone.hds.explorer.widget.NumberPicker;

/**
 * A simple {@link Fragment} subclass.
 */
public class MemberFilterFragment extends Fragment implements BarcodeScannerActivity.ResultListener {

    private EditText txtMemFilterName;
    private EditText txtMemFilterCode;
    private EditText txtMemFilterCurrHousecode;
    private CheckBox chkMemFilterGFemale;
    private CheckBox chkMemFilterGMale;
    private NumberPicker nbpMemFilterMinAge;
    private NumberPicker nbpMemFilterMaxAge;
    private CheckBox chkMemFilter1dt;
    private CheckBox chkMemFilter2om;
    private CheckBox chkMemFilter3na;
    private Button btMemFilterClear;
    private Button btMemFilterSearch;

    private Listener listener;
    private BarcodeScannerActivity.InvokerClickListener barcodeScannerListener;

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
        this.txtMemFilterCode = (EditText) view.findViewById(R.id.txtMemFilterCode);
        this.txtMemFilterCurrHousecode = (EditText) view.findViewById(R.id.txtMemFilterCurrHousecode);
        this.chkMemFilterGFemale = (CheckBox) view.findViewById(R.id.chkMemFilterGFemale);
        this.chkMemFilterGMale = (CheckBox) view.findViewById(R.id.chkMemFilterGMale);
        this.chkMemFilter1dt = (CheckBox) view.findViewById(R.id.chkMemFilter1);
        this.chkMemFilter2om = (CheckBox) view.findViewById(R.id.chkMemFilter2);
        this.chkMemFilter3na = (CheckBox) view.findViewById(R.id.chkMemFilter3);
        this.nbpMemFilterMinAge = (NumberPicker) view.findViewById(R.id.nbpMemFilterMinAge);
        this.nbpMemFilterMaxAge = (NumberPicker) view.findViewById(R.id.nbpMemFilterMaxAge);
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

        this.txtMemFilterCode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onFilterCodeClicked();
                return true;
            }
        });

        this.txtMemFilterCurrHousecode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onFilterHouseNmClicked();
                return true;
            }
        });

        if (txtMemFilterCode.getText().length()>0){
            onSearch();
        }
    }

    private void onSearch() {
        String name = txtMemFilterName.getText().toString();
        String code = txtMemFilterCode.getText().toString();
        String houseNr = txtMemFilterCurrHousecode.getText().toString();
        String gender = (chkMemFilterGMale.isChecked() && chkMemFilterGFemale.isChecked()) ? "" : chkMemFilterGMale.isChecked() ? "M" : chkMemFilterGFemale.isChecked() ? "F" : "";
        boolean filter1 = chkMemFilter1dt.isChecked();
        boolean filter2 = chkMemFilter2om.isChecked();
        boolean filter3 = chkMemFilter3na.isChecked();
        int minAge = this.nbpMemFilterMinAge.getValue();
        int maxAge = this.nbpMemFilterMaxAge.getValue();

        listener.onSearch(name, code, houseNr, gender, minAge, maxAge, filter1, filter2, filter3);
    }

    private void clear(){
        this.txtMemFilterName.setText("");
        this.txtMemFilterCode.setText("");
        this.txtMemFilterCurrHousecode.setText("");
        this.chkMemFilterGMale.setChecked(false);
        this.chkMemFilterGFemale.setChecked(false);
        this.nbpMemFilterMinAge.setValue(0);
        this.nbpMemFilterMaxAge.setValue(120);
        this.chkMemFilter1dt.setChecked(false);
        this.chkMemFilter2om.setChecked(false);
    }

    public void setBarcodeScannerListener(BarcodeScannerActivity.InvokerClickListener listener){
        this.barcodeScannerListener = listener;
    }

    private void onFilterHouseNmClicked() {
       //1-Load scan dialog (scan id or cancel)
       //2-on scan load scanner and read barcode
       //3-return with readed barcode and put on houseFilterCode EditText

        if (this.barcodeScannerListener != null){
            this.barcodeScannerListener.onBarcodeScannerClicked(R.id.txtMemFilterCurrHousecode, getString(R.string.member_filter_curr_housename_lbl), this);
        }
    }

    private void onFilterCodeClicked() {
        //1-Load scan dialog (scan id or cancel)
        //2-on scan load scanner and read barcode
        //3-return with readed barcode and put on houseFilterCode EditText

        if (this.barcodeScannerListener != null){
            this.barcodeScannerListener.onBarcodeScannerClicked(R.id.txtMemFilterCode, getString(R.string.member_filter_code_lbl), this);
        }
    }

    @Override
    public void onBarcodeScanned(int txtResId, String labelText, String resultContent) {
        //if (textBox != null)
        //    textBox.requestFocus();

        Log.d("we got the barcode", ""+resultContent);

        EditText txtEdit = (EditText) getView().findViewById(txtResId);
        if (txtEdit != null) {
            txtEdit.setText(resultContent);
            txtEdit.requestFocus();
        }
    }

    public interface Listener {
        void onSearch(String name, String code, String houseNumber, String gender, Integer minAge, Integer maxAge, Boolean isDead, Boolean hasOutmigrated, Boolean liveResident);
    }
}
