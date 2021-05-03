package org.philimone.hds.explorer.fragment;


import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapswithme.maps.api.MWMPoint;
import com.mapswithme.maps.api.MapsWithMeApi;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.HouseholdArrayAdapter;
import org.philimone.hds.explorer.adapter.RegionExpandableListAdapter;
import org.philimone.hds.explorer.adapter.model.HierarchyItem;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.Converter;
import org.philimone.hds.explorer.database.Database;
import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.main.BarcodeScannerActivity;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.widget.DialogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.objectbox.Box;


/**
 * A simple {@link Fragment} subclass.
 */
public class HouseholdFilterFragment extends Fragment implements RegionExpandableListAdapter.Listener, BarcodeScannerActivity.ResultListener {

    private Context mContext;
    private EditText txtHouseFilterCode;
    private ListView hfHousesList;
    private Button btHouseFilterClear;
    private Button btHouseFilterShowRegion;
    private Button btHouseFilterSearch;
    private Button btHouseFilterGpsMap;
    private Button btHouseFilterEditHousehold;
    private Button btHouseFilterAddHousehold;
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

    private Database database;
    private Box<ApplicationParam> boxAppParams;

    private RegionExpandableListAdapter regionAdapter;
    private Region currentRegion;
    private String lastRegionLevel = "";
    private User loggedUser;
    private Household household;

    private boolean censusMode;


    public enum Buttons {
        COLLECT_DATA, SEARCH_HOUSEHOLDS, GPS_MAP, CLEAR_FILTER
    }

    public HouseholdFilterFragment() {
        initBoxes();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.household_filter, container, false);
        initialize(view);

        loadRegionsList();
        loadHieararchyItems();

        return view;
    }

    private void initBoxes() {
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
    }

    private void initialize(View view) {
        this.database = new Database(getActivity());

        this.mContext = getActivity();

        if (getActivity() instanceof Listener){
            this.listener = (Listener) getActivity();
        }

        this.txtHouseFilterCode = (EditText) view.findViewById(R.id.txtHouseFilterCode);
        this.hfHousesList = (ListView) view.findViewById(R.id.hfHousesList);
        this.btHouseFilterClear = (Button) view.findViewById(R.id.btHouseFilterClear);
        this.btHouseFilterSearch = (Button) view.findViewById(R.id.btHouseFilterSearch);
        this.btHouseFilterGpsMap = (Button) view.findViewById(R.id.btHouseFilterGpsMap);
        this.btHouseFilterEditHousehold = (Button) view.findViewById(R.id.btHouseFilterEditHousehold);
        this.btHouseFilterAddHousehold = (Button) view.findViewById(R.id.btHouseFilterAddHousehold);
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


        if (btHouseFilterClear != null) {
            this.btHouseFilterClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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

        if (btHouseFilterGpsMap != null) {
            this.btHouseFilterGpsMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHouseholdsMap();
                }
            });
        }

        this.hfHousesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HouseholdArrayAdapter adapter = (HouseholdArrayAdapter) hfHousesList.getAdapter();
                Household household = adapter.getItem(position);
                adapter.setSelectedIndex(position);

                if (listener != null && household != null){
                    onHouseholdClicked(household);
                }
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

        if (btHouseFilterEditHousehold != null){
            btHouseFilterEditHousehold.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onEditHouseholdClicked();
                }
            });
        }

        if (btHouseFilterAddHousehold != null){
            btHouseFilterAddHousehold.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAddHouseholdClicked();
                }
            });
        }
    }

    public void setBarcodeScannerListener(BarcodeScannerActivity.InvokerClickListener listener){
        this.barcodeScannerListener = listener;
    }

    private void onHouseFilterCodeClicked() {
        //1-Load scan dialog (scan id or cancel)
        //2-on scan load scanner and read barcode
        //3-return with readed barcode and put on houseFilterCode EditText

        if (this.barcodeScannerListener != null){
            this.barcodeScannerListener.onBarcodeScannerClicked(R.id.txtHouseFilterCode, getString(R.string.household_filter_code_lbl), this);
        }
    }

    @Override
    public void onBarcodeScanned(int txtResId, String labelText, String resultContent) {
//if (textBox != null)
        //            textBox.requestFocus();

        Log.d("we got the barcode", ""+resultContent);

        txtHouseFilterCode.setText(resultContent);
        txtHouseFilterCode.requestFocus();
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

        this.btHouseFilterAddHousehold.setVisibility(censusMode ? View.VISIBLE : View.GONE);
        this.btHouseFilterEditHousehold.setVisibility(censusMode ? View.VISIBLE : View.GONE);
        this.btHouseFilterAddHousehold.setEnabled(false);
        this.btHouseFilterEditHousehold.setEnabled(false);
    }

    private void onEditHouseholdClicked() {
        listener.onEditHousehold(household);
    }

    private void onAddHouseholdClicked() {

        //1-Generate ExtId
        //2-Show a dialog (with extId and name), and buttons cancel or collect new household

        if (isLastRegionLevel()){
            listener.onAddNewHousehold(currentRegion);
        }

    }

    private void onShowRegionClicked() {
        listener.onShowRegionDetailsClicked(currentRegion);
    }

    public void setButtonEnabled(boolean enabled, Buttons... buttons){

        for (Buttons button : buttons){
            if (button==Buttons.COLLECT_DATA){
                btHouseFilterShowRegion.setEnabled(enabled);
            }
            if (button==Buttons.SEARCH_HOUSEHOLDS){
                btHouseFilterSearch.setEnabled(enabled);
            }
            if (button==Buttons.GPS_MAP){
                btHouseFilterGpsMap.setEnabled(enabled);
            }
            if (button==Buttons.CLEAR_FILTER){
                btHouseFilterClear.setEnabled(enabled);
            }
        }
    }

    private void onRegionClicked(int groupPosition, int childPosition) {
        regionAdapter.selectChild(groupPosition, childPosition);
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
        Database db = new Database(this.getActivity());
        db.open();
        List<ApplicationParam> params = boxAppParams.query().startsWith(ApplicationParam_.name, "hierarchy").build().find(); //COLUMN_NAME+" like 'hierarchy%'"
        List<Region> regions = Queries.getAllRegionBy(db, null, null);
        db.close();

        ArrayList<HierarchyItem> hierarchies = new ArrayList<>();
        HashMap<HierarchyItem, ArrayList<Region>> regionCollection = new HashMap<>();

        for (ApplicationParam param : params){

            if (param.getValue().isEmpty()) continue;

            HierarchyItem item = new HierarchyItem(param);
            ArrayList<Region> list = new ArrayList<>();

            for (Region region : regions){
                if (region.getLevel().equals(item.getLevel())){
                    list.add(region);
                }
            }

            hierarchies.add(item);
            regionCollection.put(item, list);
        }

        this.regionAdapter = new RegionExpandableListAdapter(this.mContext, hierarchies, regionCollection);
        this.regionAdapter.setListener(this);
        this.expListRegions.setAdapter(regionAdapter);
    }

    private void loadHieararchyItems(){

        List<ApplicationParam> params = boxAppParams.query().startsWith(ApplicationParam_.name, "hierarchy").build().find(); //COLUMN_NAME+" like 'hierarchy%'"

        for (ApplicationParam param : params){

            if (!param.getValue().isEmpty() && param.getName().compareTo(lastRegionLevel) > 0){
                lastRegionLevel = param.getName();
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

    private void showHouseholdsMap() {
        HouseholdArrayAdapter adapter = (HouseholdArrayAdapter) this.hfHousesList.getAdapter();

        if (adapter==null || adapter.isEmpty()){
            DialogFactory.createMessageInfo(this.getActivity(), R.string.household_filter_gps_not_available_title_lbl, R.string.household_filter_no_houses_lbl).show();
            return;
        }

        final MWMPoint[] points = new MWMPoint[adapter.getHouseholds().size()];
        boolean hasAnyCoords = false;

        for (int i=0; i < points.length; i++){
            Household h = adapter.getHouseholds().get(i);
            String name = h.getName();

            if (!h.isGpsNull()) {
                double lat = h.getGpsLatitude();
                double lon = h.getGpsLongitude();
                points[i] = new MWMPoint(lat, lon, name);
                Log.d("corrds-test", name+" - "+h.getGpsLatitude()+", "+h.getGpsLongitude());
                hasAnyCoords = true;
            }
        }

        if (!hasAnyCoords){
            DialogFactory.createMessageInfo(this.getActivity(), R.string.map_gps_not_available_title_lbl, R.string.household_filter_gps_not_available_lbl).show();
            return;
        }

        MapsWithMeApi.showPointsOnMap(this.getActivity(), getString(R.string.map_households), points);
    }

    public void searchHouses(String householdCode){
        //showProgress(true);
        HouseholdSearchTask task = new HouseholdSearchTask(householdCode, null);
        task.execute();
    }

    private void onHouseholdClicked(Household household){
        //paint item as selected
        this.household = household;

        this.btHouseFilterEditHousehold.setEnabled(isCensusMode() && household.isRecentlyCreated());

        listener.onHouseholdClick(household);
    }

    private void onSelectedRegion(Region region){

        boolean lastLevel = region.getLevel().equals(lastRegionLevel);

        btHouseFilterSearch.setEnabled(lastLevel);
        btHouseFilterAddHousehold.setEnabled(lastLevel);

        listener.onSelectedRegion(region);
    }

    private boolean isLastRegionLevel(){
        if (currentRegion==null) return false;

        return currentRegion.getLevel().equals(lastRegionLevel);
    }

    public void checkSupportForRegionForms(FormDataLoader[] formDataLoaders) {
        this.btHouseFilterShowRegion.setEnabled(hasAssociatedRegionForms(formDataLoaders));
    }

    private boolean hasAssociatedRegionForms(FormDataLoader[] formDataLoaders){
        for (FormDataLoader fdl : formDataLoaders){
            Form form = fdl.getForm();

            if (form.getRegionLevel().equals(currentRegion.getLevel())){
                return true;
            }
        }
        return false;
    }

    public HouseholdArrayAdapter loadHouseholdsByFilters(String houseCode) {
        //open loader

        //search on database
        List<Household> households = new ArrayList<>();
        List<String> whereValues = new ArrayList<>();
        String[] arrayWhereValues;

        String whereClause = DatabaseHelper.Household.COLUMN_CODE + " like ?";
        whereValues.add(houseCode+"%");

        arrayWhereValues = new String[whereValues.size()];

        //search
        database.open();

        Cursor cursor = database.query(Household.class, DatabaseHelper.Household.ALL_COLUMNS, whereClause, whereValues.toArray(arrayWhereValues), null, null, DatabaseHelper.Household.COLUMN_CODE);

        while (cursor.moveToNext()){
            households.add(Converter.cursorToHousehold(cursor));
        }

        database.close();

        HouseholdArrayAdapter currentAdapter = new HouseholdArrayAdapter(this.getActivity(), households);

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

    class HouseholdSearchTask extends AsyncTask<Void, Void, HouseholdArrayAdapter> {
        private String code;
        private String houseName;

        public HouseholdSearchTask(String code, String houseName) {
            this.code = code;
            this.houseName = houseName;
        }

        @Override
        protected HouseholdArrayAdapter doInBackground(Void... params) {
            return loadHouseholdsByFilters(code);
        }

        @Override
        protected void onPostExecute(HouseholdArrayAdapter adapter) {
            HouseholdFilterFragment.this.household = household;
            hfHousesList.setAdapter(adapter);
            //showProgress(false);

            if (adapter.isEmpty()){
                showHouseholdNotFoundMessage(code);
            }
        }
    }

    public interface Listener {
        void onHouseholdClick(Household household);

        void onSelectedRegion(Region region);

        void onShowRegionDetailsClicked(Region region);

        void onAddNewHousehold(Region region);

        void onEditHousehold(Household household);
    }
}
