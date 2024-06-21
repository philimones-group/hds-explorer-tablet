package org.philimone.hds.explorer.fragment;


import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.listeners.BarcodeContextMenuClickedListener;
import org.philimone.hds.explorer.main.BarcodeScannerActivity;
import org.philimone.hds.explorer.widget.NumberPicker;

import java.lang.reflect.Field;

/**
 * A simple {@link Fragment} subclass.
 */
public class MemberFilterFragment extends Fragment implements BarcodeScannerActivity.ResultListener, BarcodeContextMenuClickedListener {

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


        /*
        this.txtMemFilterCode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onFilterCodeBarcodeScanClicked();
                return true;
            }
        });*/

        /*
        this.txtMemFilterCurrHousecode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onFilterHouseNmBarcodeScanClicked();
                return true;
            }
        });*/

        if (txtMemFilterCode.getText().length()>0){
            onSearch();
        }

        this.registerForContextMenu(txtMemFilterName);
        this.registerForContextMenu(txtMemFilterCode);
        this.registerForContextMenu(txtMemFilterCurrHousecode);
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        Log.d("menutag", "creating context menu = "+v.getId()+", info="+menuInfo);

        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_barcode, menu);

        for(int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);

            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    onBarcodeContextMenuItemClicked(v, item);
                    return true;
                }
            });

            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            spanString.setSpan(new ForegroundColorSpan(Color.BLACK), 0,     spanString.length(), 0); //fix the color to white
            item.setTitle(spanString);
            item.setIcon(R.drawable.nui_paste_icon);
        }
    }

    @Override
    public void onBarcodeContextMenuItemClicked(View view, MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuBarcodePaste:
                String paste = getClipboardPasteText();
                if (view.getId()==txtMemFilterName.getId()) {
                    txtMemFilterName.setText(paste);
                } else if (view.getId()==txtMemFilterCode.getId()) {
                    txtMemFilterCode.setText(paste);
                } else if (view.getId()==txtMemFilterCurrHousecode.getId()) {
                    txtMemFilterCurrHousecode.setText(paste);
                }
                break;
            case R.id.menuBarcodeScan:
                if (view.getId()==txtMemFilterName.getId()) {
                    onFilterNameBarcodeScanClicked();
                } else if (view.getId()==txtMemFilterCode.getId()) {
                    onFilterCodeBarcodeScanClicked();
                } else if (view.getId()==txtMemFilterCurrHousecode.getId()) {
                    onFilterHouseNmBarcodeScanClicked();
                }
                break;
        }

        Log.d("barcode menu", view.toString() + ", "+item+", paste="+getClipboardPasteText());
    }

    private String getClipboardPasteText() {
        ClipboardManager clipboard = (ClipboardManager) this.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        try {
            CharSequence textToPaste = clipboard.getPrimaryClip().getItemAt(0).getText();
            return textToPaste.toString();
        } catch (Exception e) {
            return null;
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
        this.chkMemFilter3na.setChecked(false);
    }

    public void setBarcodeScannerListener(BarcodeScannerActivity.InvokerClickListener listener){
        this.barcodeScannerListener = listener;
    }

    private void onFilterNameBarcodeScanClicked() {
        //1-Load scan dialog (scan id or cancel)
        //2-on scan load scanner and read barcode
        //3-return with readed barcode and put on houseFilterCode EditText

        if (this.barcodeScannerListener != null){
            this.barcodeScannerListener.onBarcodeScannerClicked(R.id.txtMemFilterName, getString(R.string.member_filter_name_lbl), this);
        }
    }

    private void onFilterCodeBarcodeScanClicked() {
        //1-Load scan dialog (scan id or cancel)
        //2-on scan load scanner and read barcode
        //3-return with readed barcode and put on houseFilterCode EditText

        if (this.barcodeScannerListener != null){
            this.barcodeScannerListener.onBarcodeScannerClicked(R.id.txtMemFilterCode, getString(R.string.member_filter_code_lbl), this);
        }
    }

    private void onFilterHouseNmBarcodeScanClicked() {
        //1-Load scan dialog (scan id or cancel)
        //2-on scan load scanner and read barcode
        //3-return with readed barcode and put on houseFilterCode EditText

        if (this.barcodeScannerListener != null){
            this.barcodeScannerListener.onBarcodeScannerClicked(R.id.txtMemFilterCurrHousecode, getString(R.string.member_filter_curr_housename_lbl), this);
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

