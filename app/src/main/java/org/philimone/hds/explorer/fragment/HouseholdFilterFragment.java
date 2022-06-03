package org.philimone.hds.explorer.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.HouseholdAdapter;
import org.philimone.hds.explorer.adapter.RegionExpandableListAdapter;
import org.philimone.hds.explorer.adapter.model.HierarchyItem;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.main.BarcodeScannerActivity;
import org.philimone.hds.explorer.main.HouseholdDetailsActivity;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.Round;
import org.philimone.hds.explorer.model.Round_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.settings.RequestCodes;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.RecyclerListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.StringUtil;


/**
 * A simple {@link Fragment} subclass.
 */
public class HouseholdFilterFragment extends Fragment implements RegionExpandableListAdapter.Listener, BarcodeScannerActivity.ResultListener {

    private Context mContext;
    private EditText txtHouseFilterCode;
    private RecyclerListView hfHousesList;
    private Button btHouseFilterAddNewHousehold;
    private Button btHouseFilterShowRegion;
    private Button btHouseFilterSearch;
    private RelativeLayout hfViewProgressBar;

    private ExpandableListView expListRegions;

    private LinearLayout layoutHierarchy1;
    private LinearLayout layoutHierarchy2;
    private LinearLayout layoutHierarchy3;
    private LinearLayout layoutHierarchy4;
    private LinearLayout layoutHierarchy5;
    private LinearLayout layoutHierarchy6;
    private LinearLayout layoutHierarchy7;
    private LinearLayout layoutHierarchy8;

    private TextView txtHierarchy1_name;
    private TextView txtHierarchy2_name;
    private TextView txtHierarchy3_name;
    private TextView txtHierarchy4_name;
    private TextView txtHierarchy5_name;
    private TextView txtHierarchy6_name;
    private TextView txtHierarchy7_name;
    private TextView txtHierarchy8_name;

    private TextView txtHierarchy1_value;
    private TextView txtHierarchy2_value;
    private TextView txtHierarchy3_value;
    private TextView txtHierarchy4_value;
    private TextView txtHierarchy5_value;
    private TextView txtHierarchy6_value;
    private TextView txtHierarchy7_value;
    private TextView txtHierarchy8_value;

    private Listener listener;
    private BarcodeScannerActivity.InvokerClickListener barcodeScannerListener;

    private Box<ApplicationParam> boxAppParams;
    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Round> boxRounds;
    private Box<Form> boxForms;

    private RegionExpandableListAdapter regionAdapter;
    private Region currentRegion;
    private String lastRegionLevel = "";
    private User loggedUser = Bootstrap.getCurrentUser();
    private Household household;

    private boolean censusMode;

    public enum Buttons {
        COLLECT_DATA, SEARCH_HOUSEHOLDS, GPS_MAP, CLEAR_FILTER
    }

    private ActivityResultLauncher<Intent> addNewHouseholdLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        if (result.getResultCode()== Activity.RESULT_OK) {
            Intent data = result.getData();

            String code = data.getStringExtra("new_household_code");

            if (code != null) { //returned the new household code - search it
                searchHouses(code);
            }
        }
    });

    public HouseholdFilterFragment() {
        initBoxes();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.household_filter, container, false);
        initialize(view);

        loadHieararchyItems();
        loadRegionsList();

        return view;
    }

    private void initBoxes() {
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxRounds = ObjectBoxDatabase.get().boxFor(Round.class);
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
    }

    private void initialize(View view) {
        this.mContext = getActivity();

        if (getActivity() instanceof Listener){
            this.listener = (Listener) getActivity();
        }

        this.txtHouseFilterCode = (EditText) view.findViewById(R.id.txtHouseFilterCode);
        this.hfHousesList = view.findViewById(R.id.hfHousesList);
        this.btHouseFilterAddNewHousehold = (Button) view.findViewById(R.id.btHouseFilterAddNewHousehold);
        this.btHouseFilterSearch = (Button) view.findViewById(R.id.btHouseFilterSearch);
        this.hfViewProgressBar = (RelativeLayout) view.findViewById(R.id.hfViewProgressBar);

        btHouseFilterShowRegion = (Button) view.findViewById(R.id.btHouseFilterShowRegion);
        expListRegions = (ExpandableListView) view.findViewById(R.id.expListRegions);

        /* region items */
        layoutHierarchy1 = (LinearLayout) view.findViewById(R.id.layoutHierarchy1);
        layoutHierarchy2 = (LinearLayout) view.findViewById(R.id.layoutHierarchy2);
        layoutHierarchy3 = (LinearLayout) view.findViewById(R.id.layoutHierarchy3);
        layoutHierarchy4 = (LinearLayout) view.findViewById(R.id.layoutHierarchy4);
        layoutHierarchy5 = (LinearLayout) view.findViewById(R.id.layoutHierarchy5);
        layoutHierarchy6 = (LinearLayout) view.findViewById(R.id.layoutHierarchy6);
        layoutHierarchy7 = (LinearLayout) view.findViewById(R.id.layoutHierarchy7);
        layoutHierarchy8 = (LinearLayout) view.findViewById(R.id.layoutHierarchy8);

        txtHierarchy1_name = (TextView) view.findViewById(R.id.txtHierarchy1_name);
        txtHierarchy2_name = (TextView) view.findViewById(R.id.txtHierarchy2_name);
        txtHierarchy3_name = (TextView) view.findViewById(R.id.txtHierarchy3_name);
        txtHierarchy4_name = (TextView) view.findViewById(R.id.txtHierarchy4_name);
        txtHierarchy5_name = (TextView) view.findViewById(R.id.txtHierarchy5_name);
        txtHierarchy6_name = (TextView) view.findViewById(R.id.txtHierarchy6_name);
        txtHierarchy7_name = (TextView) view.findViewById(R.id.txtHierarchy7_name);
        txtHierarchy8_name = (TextView) view.findViewById(R.id.txtHierarchy8_name);

        txtHierarchy1_value = (TextView) view.findViewById(R.id.txtHierarchy1_value);
        txtHierarchy2_value = (TextView) view.findViewById(R.id.txtHierarchy2_value);
        txtHierarchy3_value = (TextView) view.findViewById(R.id.txtHierarchy3_value);
        txtHierarchy4_value = (TextView) view.findViewById(R.id.txtHierarchy4_value);
        txtHierarchy5_value = (TextView) view.findViewById(R.id.txtHierarchy5_value);
        txtHierarchy6_value = (TextView) view.findViewById(R.id.txtHierarchy6_value);
        txtHierarchy7_value = (TextView) view.findViewById(R.id.txtHierarchy7_value);
        txtHierarchy8_value = (TextView) view.findViewById(R.id.txtHierarchy8_value);

        this.txtHouseFilterCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>2){
                    searchHouses(s.toString());
                }
            }
        });

        this.txtHouseFilterCode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onHouseFilterCodeClicked();
                return true;
            }
        });


        if (btHouseFilterAddNewHousehold != null) {
            this.btHouseFilterAddNewHousehold.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAddNewHouseholdClicked();
                }
            });
        }

        if (btHouseFilterSearch != null) {
            this.btHouseFilterSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String regionCode = currentRegion.getCode();  //get the last selected region
                    String code = txtHouseFilterCode.getText().toString();
                    String search = (code==null || code.isEmpty()) ? regionCode : code;
                    searchHouses(search); //search households on that region

                }
            });
        }

        this.hfHousesList.addOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long id) {
                HouseholdAdapter adapter = (HouseholdAdapter) hfHousesList.getAdapter();
                Household household = adapter.getItem(position);
                adapter.setSelectedIndex(position);

                if (listener != null && household != null){
                    onHouseholdClicked(household);
                }
            }

            @Override
            public void onItemLongClick(View view, int position, long id) {

            }
        });

        this.expListRegions.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                onRegionClicked(groupPosition, childPosition);

                return true;
            }
        });

        if (btHouseFilterShowRegion != null){
            btHouseFilterShowRegion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onShowRegionClicked();
                }
            });
        }

        this.btHouseFilterAddNewHousehold.setEnabled(false);
    }

    private void onHouseFilterCodeClicked() {
        //1-Load scan dialog (scan id or cancel)
        //2-on scan load scanner and read barcode
        //3-return with readed barcode and put on houseFilterCode EditText

        if (this.barcodeScannerListener != null){
            this.barcodeScannerListener.onBarcodeScannerClicked(R.id.txtHouseFilterCode, getString(R.string.household_filter_code_lbl), this);
        }
    }

    private void onHouseholdClicked(Household household){
        //paint item as selected
        this.household = household;

        listener.onHouseholdClick(household);
    }

    private void onAddNewHouseholdClicked() {

        boolean roundsExists = this.boxRounds.query().order(Round_.roundNumber, QueryBuilder.DESCENDING).build().count()>0;

        if (!roundsExists) {
            DialogFactory.createMessageInfo(this.mContext, R.string.error_lbl, R.string.round_does_not_exists_lbl).show();
            return;
        }

        //Call HouseholdDetailsActivity in mode NEW_HOUSEHOLD
        //Receive the recent created Household, put the code on search after it

        Intent intent = new Intent(this.getContext(), HouseholdDetailsActivity.class);
        intent.putExtra("region", currentRegion);
        intent.putExtra("request_code", RequestCodes.HOUSEHOLD_DETAILS_FROM_HFILTER_NEW_HOUSEHOLD);
        //intent.putExtra("dataloaders", dataLoaders);

        addNewHouseholdLauncher.launch(intent);
    }

    private void onShowRegionClicked() {
        listener.onShowRegionDetailsClicked(currentRegion);
    }

    private void onRegionClicked(int groupPosition, int childPosition) {
        regionAdapter.selectChild(groupPosition, childPosition);
    }

    @Override
    public void onBarcodeScanned(int txtResId, String labelText, String resultContent) {
//if (textBox != null)
        //            textBox.requestFocus();

        Log.d("we got the barcode", ""+resultContent);

        txtHouseFilterCode.setText(resultContent);
        txtHouseFilterCode.requestFocus();
    }

    private void onSelectedRegion(Region region){

        boolean lastLevel = region.getLevel().equals(lastRegionLevel);

        this.btHouseFilterShowRegion.setEnabled(true);
        btHouseFilterSearch.setEnabled(lastLevel);
        btHouseFilterAddNewHousehold.setEnabled(lastLevel);

        //listener.onSelectedRegion(region);
    }

    public void setBarcodeScannerListener(BarcodeScannerActivity.InvokerClickListener listener){
        this.barcodeScannerListener = listener;
    }

    public User getLoggedUser() {
        return loggedUser;
    }

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }

    public boolean isCensusMode() {
        return censusMode;
    }

    public void setCensusMode(boolean censusMode) {
        this.censusMode = censusMode;
    }

    @Override
    public void onChildSelected(Region selectedRegion, int groupPosition, int childPosition, int nextGroupPosition) {

        if (!isLastRegionGroup(groupPosition)){ //Dont Collapse the last group
            collapseRegionGroup(groupPosition);
        }

        expandRegionGroup(nextGroupPosition);

        setSelectedRegion(selectedRegion);

        Log.d("last level", ""+lastRegionLevel);
        //Log.d("gp", "group="+groupPosition+", count="+regionAdapter.getGroupCount()+", isLast="+isLastRegionGroup(groupPosition));
    }

    private void collapseRegionGroup(int groupPosition){

        if (groupPosition != -1){
            this.expListRegions.collapseGroup(groupPosition);
        }
    }

    private void expandRegionGroup(int groupPosition){
        if (groupPosition != -1){
            this.expListRegions.expandGroup(groupPosition);
        }
    }

    private boolean isLastRegionGroup(int groupPosition){
        return groupPosition==this.regionAdapter.getGroupCount()-1;
    }

    private void loadRegionsList(){

        List<String> smodules = new ArrayList<>(loggedUser.getSelectedModules());

        List<ApplicationParam> params = boxAppParams.query().startsWith(ApplicationParam_.name, "hierarchy", QueryBuilder.StringOrder.CASE_SENSITIVE).build().find(); //COLUMN_NAME+" like 'hierarchy%'"
        List<Region> regions = this.boxRegions.query().order(Region_.name).build().find();  //query().filter((r) -> StringUtil.containsAny(r.modules, smodules)).build().find(); //filter by modules


        ArrayList<HierarchyItem> hierarchies = new ArrayList<>();
        HashMap<HierarchyItem, ArrayList<Region>> regionCollection = new HashMap<>();

        for (ApplicationParam param : params){

            if (param.getValue().isEmpty()) continue;

            HierarchyItem item = new HierarchyItem(param);
            ArrayList<Region> list = new ArrayList<>();

            for (Region region : regions){
                if (region.getLevel().equals(item.getLevel())){ //Filter Here

                    if (region.getLevel().equals(lastRegionLevel)) {

                        //filter according to the selected user modules
                        //Log.d("test-"+lastRegionLevel, region.name+", contains "+StringUtil.containsAny(region.modules, smodules)+", rmodules="+region.modules+", smodules="+smodules);
                        if (StringUtil.containsAny(region.modules, smodules)) {
                            list.add(region);
                        }

                    } else {
                        list.add(region); //not at last level, just add
                    }

                }
            }

            hierarchies.add(item);
            regionCollection.put(item, list);
        }

        this.regionAdapter = new RegionExpandableListAdapter(this.mContext, hierarchies, regionCollection);
        this.regionAdapter.setListener(this);
        this.expListRegions.setAdapter(regionAdapter);
    }

    private int getHierarchyNumber(String hierarchyName) {
        if (StringUtil.isBlank(hierarchyName)) return 0;

        try {
            return Integer.parseInt(hierarchyName.replace("hierarchy", ""));
        }catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    private void loadHieararchyItems(){

        List<ApplicationParam> params = boxAppParams.query().startsWith(ApplicationParam_.name, "hierarchy", QueryBuilder.StringOrder.CASE_SENSITIVE).build().find(); //COLUMN_NAME+" like 'hierarchy%'"

        for (ApplicationParam param : params){

            if (!param.getValue().isEmpty()) {
                if (getHierarchyNumber(param.getName()) > getHierarchyNumber(lastRegionLevel)) {
                    lastRegionLevel = param.getName();
                }
            }

            if (param.getName().equalsIgnoreCase(Region.HIERARCHY_1)){
                txtHierarchy1_name.setText(param.getValue()+":");
                txtHierarchy1_value.setText("");
                layoutHierarchy1.setVisibility(param.getValue().isEmpty() ? View.GONE :  View.VISIBLE);
                continue;
            }
            if (param.getName().equalsIgnoreCase(Region.HIERARCHY_2)){
                txtHierarchy2_name.setText(param.getValue()+":");
                txtHierarchy2_value.setText("");
                layoutHierarchy2.setVisibility(param.getValue().isEmpty() ? View.GONE :  View.VISIBLE);
                continue;
            }
            if (param.getName().equalsIgnoreCase(Region.HIERARCHY_3)){
                txtHierarchy3_name.setText(param.getValue()+":");
                txtHierarchy3_value.setText("");
                layoutHierarchy3.setVisibility(param.getValue().isEmpty() ? View.GONE :  View.VISIBLE);
                continue;
            }
            if (param.getName().equalsIgnoreCase(Region.HIERARCHY_4)){
                txtHierarchy4_name.setText(param.getValue()+":");
                txtHierarchy4_value.setText("");
                layoutHierarchy4.setVisibility(param.getValue().isEmpty() ? View.GONE :  View.VISIBLE);
                continue;
            }
            if (param.getName().equalsIgnoreCase(Region.HIERARCHY_5)){
                txtHierarchy5_name.setText(param.getValue()+":");
                txtHierarchy5_value.setText("");
                layoutHierarchy5.setVisibility(param.getValue().isEmpty() ? View.GONE :  View.VISIBLE);
                continue;
            }
            if (param.getName().equalsIgnoreCase(Region.HIERARCHY_6)){
                txtHierarchy6_name.setText(param.getValue()+":");
                txtHierarchy6_value.setText("");
                layoutHierarchy6.setVisibility(param.getValue().isEmpty() ? View.GONE :  View.VISIBLE);
                continue;
            }
            if (param.getName().equalsIgnoreCase(Region.HIERARCHY_7)){
                txtHierarchy7_name.setText(param.getValue()+":");
                txtHierarchy7_value.setText("");
                layoutHierarchy7.setVisibility(param.getValue().isEmpty() ? View.GONE :  View.VISIBLE);
                continue;
            }
            if (param.getName().equalsIgnoreCase(Region.HIERARCHY_8)){
                txtHierarchy8_name.setText(param.getValue()+":");
                txtHierarchy8_value.setText("");
                layoutHierarchy8.setVisibility(param.getValue().isEmpty() ? View.GONE :  View.VISIBLE);
                continue;
            }

        }
    }
    
    private void setSelectedRegion(Region region){
        this.currentRegion = region;
        updateRegionTextViews(region);

        Log.d("region", ""+currentRegion.getLevel()+", "+currentRegion.getName());

        onSelectedRegion(region);
    }

    private void updateRegionTextViews(Region region){
        if (region.getLevel().equalsIgnoreCase(Region.HIERARCHY_1)){
            txtHierarchy1_value.setText(region.getName());
            txtHierarchy2_value.setText("");
            txtHierarchy3_value.setText("");
            txtHierarchy4_value.setText("");
            txtHierarchy5_value.setText("");
            txtHierarchy6_value.setText("");
            txtHierarchy7_value.setText("");
            txtHierarchy8_value.setText("");
            return;
        }
        if (region.getLevel().equalsIgnoreCase(Region.HIERARCHY_2)){
            txtHierarchy2_value.setText(region.getName());
            txtHierarchy3_value.setText("");
            txtHierarchy4_value.setText("");
            txtHierarchy5_value.setText("");
            txtHierarchy6_value.setText("");
            txtHierarchy7_value.setText("");
            txtHierarchy8_value.setText("");
            return;
        }
        if (region.getLevel().equalsIgnoreCase(Region.HIERARCHY_3)){
            txtHierarchy3_value.setText(region.getName());
            txtHierarchy4_value.setText("");
            txtHierarchy5_value.setText("");
            txtHierarchy6_value.setText("");
            txtHierarchy7_value.setText("");
            txtHierarchy8_value.setText("");
            return;
        }
        if (region.getLevel().equalsIgnoreCase(Region.HIERARCHY_4)){
            txtHierarchy4_value.setText(region.getName());
            txtHierarchy5_value.setText("");
            txtHierarchy6_value.setText("");
            txtHierarchy7_value.setText("");
            txtHierarchy8_value.setText("");
            return;
        }
        if (region.getLevel().equalsIgnoreCase(Region.HIERARCHY_5)){
            txtHierarchy5_value.setText(region.getName());
            txtHierarchy6_value.setText("");
            txtHierarchy7_value.setText("");
            txtHierarchy8_value.setText("");
            return;
        }
        if (region.getLevel().equalsIgnoreCase(Region.HIERARCHY_6)){
            txtHierarchy6_value.setText(region.getName());
            txtHierarchy7_value.setText("");
            txtHierarchy8_value.setText("");
            return;
        }
        if (region.getLevel().equalsIgnoreCase(Region.HIERARCHY_7)){
            txtHierarchy7_value.setText(region.getName());
            txtHierarchy8_value.setText("");
            return;
        }
        if (region.getLevel().equalsIgnoreCase(Region.HIERARCHY_8)){
            txtHierarchy8_value.setText(region.getName());
            return;
        }

    }

    public void searchHouses(String householdCode){
        //showProgress(true);
        HouseholdSearchTask task = new HouseholdSearchTask(householdCode, null);
        task.execute();
    }

    public HouseholdAdapter loadHouseholdsByFilters(String houseCode) {
        //open loader
        //search
        List<String> smodules = new ArrayList<>(loggedUser.getSelectedModules());

        List<Household> households = this.boxHouseholds.query().startsWith(Household_.code, houseCode, QueryBuilder.StringOrder.CASE_SENSITIVE).orderDesc(Household_.code) //query by modules
                                                               .filter((h)->StringUtil.containsAny(h.modules, smodules))
                                                               .build().find();

        HouseholdAdapter currentAdapter = new HouseholdAdapter(this.getActivity(), households);

        return currentAdapter;
    }

    public void showHouseholdNotFoundMessage(String code){
        Toast toast = Toast.makeText(getActivity(), getString(R.string.household_filter_household_not_found_lbl, code), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }

    public void showProgress(final boolean show) {
        hfViewProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        hfHousesList.setVisibility(show ? View.GONE : View.VISIBLE);
    }    

    class HouseholdSearchTask extends AsyncTask<Void, Void, HouseholdAdapter> {
        private String code;
        private String houseName;

        public HouseholdSearchTask(String code, String houseName) {
            this.code = code;
            this.houseName = houseName;
        }

        @Override
        protected HouseholdAdapter doInBackground(Void... params) {
            return loadHouseholdsByFilters(code);
        }

        @Override
        protected void onPostExecute(HouseholdAdapter adapter) {
            HouseholdFilterFragment.this.household = household;
            hfHousesList.setAdapter(adapter);
            //showProgress(false);

            if (adapter.getItemCount()==0){
                showHouseholdNotFoundMessage(code);
            }
        }
    }

    public interface Listener {
        void onHouseholdClick(Household household);

        void onSelectedRegion(Region region);

        void onShowRegionDetailsClicked(Region region);
    }
}
