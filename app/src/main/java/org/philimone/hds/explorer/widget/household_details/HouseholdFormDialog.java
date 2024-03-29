package org.philimone.hds.explorer.widget.household_details;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.widget.DialogFactory;

import androidx.annotation.Nullable;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

/**
 *
 */
public class HouseholdFormDialog extends DialogFragment {

    private FragmentManager fragmentManager;

    private TextView txtRegionCode;
    private TextView txtRegionName;
    private TextView txtHouseCode;
    private EditText txtHouseName;
    private Button btNewHhCancel;
    private Button btNewHhCollect;
/*
    TextView txtRegionCode = null;
    TextView txtRegionName = null;
    TextView txtHouseCode = null;
    EditText txtHouseName = null;*/
    private Box<Household> boxHouseholds;

    private Listener listener;

    private Region region;
    private User user;

    public HouseholdFormDialog(){
        super();
    }


    public static HouseholdFormDialog newInstance(FragmentManager fm, Region region, User fieldWorker, Listener listener){
        HouseholdFormDialog dialog = new HouseholdFormDialog();

        dialog.fragmentManager = fm;
        dialog.region = region;
        dialog.user = fieldWorker;
        dialog.listener = listener;
        dialog.setCancelable(false);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.new_household, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initBoxes();
        initialize(view);
    }

    private void initBoxes(){
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
    }

    private void initialize(View view) {
        this.txtRegionCode = (TextView) view.findViewById(R.id.txtRegionCode);
        this.txtRegionName = (TextView) view.findViewById(R.id.txtRegionName);
        this.txtHouseCode = (TextView) view.findViewById(R.id.txtHouseCode);
        this.txtHouseName = (EditText) view.findViewById(R.id.txtHouseName);
        this.btNewHhCancel = (Button) view.findViewById(R.id.btNewHhCancel);
        this.btNewHhCollect = (Button) view.findViewById(R.id.btNewHhCollect);

        this.btNewHhCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelClicked();
            }
        });

        this.btNewHhCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCollectClicked();
            }
        });

        //Set values to textviews
        String code = generateHouseholdCode(region, user);

        txtRegionCode.setText(region.getCode());
        txtRegionName.setText(region.getName());
        txtHouseCode.setText(code);
    }

    private void onCancelClicked(){
        dismiss();

        if (this.listener != null){
            this.listener.onCancelClicked();
        }
    }

    private void fireOnNewHouseholdCreated(Household household){
        dismiss();

        if (this.listener != null){
            this.listener.onNewHouseholdCreated(household);
        }
    }

    private void onCollectClicked(){

        //try to create new Member
        Household household = validateAndCreateHousehold();

        if (household != null){
            fireOnNewHouseholdCreated(household);
        }
    }

    private Household validateAndCreateHousehold() {

        Household household = Household.getEmptyHousehold();

        household.setCode(txtHouseCode.getText().toString());
        household.setName(txtHouseName.getText().toString());
        household.setRegion(region.getCode());

        household.setRecentlyCreated(true);

        //checks

        if (!household.getCode().matches("[A-Z0-9]{6}[0-9]{3}")){
            DialogFactory.createMessageInfo(getActivity(), R.string.info_lbl, R.string.new_household_code_err_lbl).show();
            return null;
        }

        if (household.getName().isEmpty()){
            DialogFactory.createMessageInfo(getActivity(), R.string.info_lbl, R.string.new_household_code_empty_lbl).show();
            return null;
        }

        if (!household.getCode().startsWith(household.getRegion())){
            DialogFactory.createMessageInfo(getActivity(), R.string.info_lbl, R.string.new_household_code_region_err_lbl).show();
            return null;
        }

        //check if houseNumber exists
        if (checkIfHouseCodeExists(household.getCode())){
            DialogFactory.createMessageInfo(getActivity(), R.string.info_lbl, R.string.new_household_code_exists_lbl).show();
            return null;
        }

        return household;

    }


    /* Database Usefull Methods */
    private String generateHouseholdCode(Region region, User fieldWorker){

        String baseId = region.getCode() + fieldWorker.getCode();
        /*
        String[] columns = new String[] {DatabaseHelper.Household.COLUMN_CODE};
        String where = DatabaseHelper.Household.COLUMN_CODE + " LIKE ?";
        String[] whereArgs = new String[] { baseId + "%" };
        String orderBy = DatabaseHelper.Household.COLUMN_CODE + " DESC";
        */

        String generatedId = null;

        Household lastHousehold = this.boxHouseholds.query().startsWith(Household_.code, baseId, QueryBuilder.StringOrder.CASE_SENSITIVE).orderDesc(Household_.code).build().findFirst();

        if (lastHousehold != null) {
            String lastGeneratedId = lastHousehold.code;

            try {
                int increment = Integer.parseInt(lastGeneratedId.substring(6, 9));
                generatedId = baseId + String.format("%03d", increment+1);
            } catch (NumberFormatException e) {
                return baseId + "ERROR_01";
            }

        } else { //no extId based on "baseId"
            generatedId = baseId + "001"; //set the first id of individual household
        }

        return generatedId;
    }

    private boolean checkIfHouseCodeExists(String houseCode){
        Household household = Queries.getHouseholdByCode(boxHouseholds, houseCode);
        return household != null;
    }

    public void show(){
        this.show(fragmentManager, "hhform");
    }


    public interface Listener {

        void onNewHouseholdCreated(Household household);

        void onCancelClicked();

    }

}
