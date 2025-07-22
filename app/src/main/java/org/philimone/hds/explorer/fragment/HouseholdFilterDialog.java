package org.philimone.hds.explorer.fragment;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.HouseholdAdapter;
import org.philimone.hds.explorer.adapter.MemberAdapter;
import org.philimone.hds.explorer.adapter.RegionExpandableListAdapter;
import org.philimone.hds.explorer.adapter.model.HierarchyItem;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.listeners.BarcodeContextMenuClickedListener;
import org.philimone.hds.explorer.main.BarcodeScannerActivity;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.Round;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.server.settings.generator.CodeGeneratorService;
import org.philimone.hds.explorer.widget.RecyclerListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.StringUtil;
import mz.betainteractive.utilities.TextFilters;

/**
 *
 */
public class HouseholdFilterDialog extends DialogFragment implements RegionExpandableListAdapter.Listener, BarcodeScannerActivity.ResultListener, BarcodeContextMenuClickedListener {

    private FragmentManager fragmentManager;
    private Context mContext;

    private TextView txtDialogTitle;
    private Button btDialogClose;
    private EditText txtHouseFilterCode;
    private RecyclerListView hfHousesList;
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

    //Member List variables
    private TextView mbHouseDetailsNumber;
    private RecyclerListView lvMembersList;
    private Button hflSelectHousehold;
    private View progressBarLayout;

    private Listener listener;
    private BarcodeScannerActivity.InvokerClickListener barcodeScannerListener;

    private Box<ApplicationParam> boxAppParams;
    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<Round> boxRounds;

    private RegionExpandableListAdapter regionAdapter;
    private Region currentRegion;
    private Household currentHousehold;
    private String lastRegionLevel = "";
    private boolean startSearchOnShow;

    private String title;

    private CodeGeneratorService codeGeneratorService;

    private User loggedUser = Bootstrap.getCurrentUser();

    public HouseholdFilterDialog(){
        super();
        this.codeGeneratorService = new CodeGeneratorService();
        initBoxes();
    }

    public static HouseholdFilterDialog newInstance(FragmentManager fm, String title, Listener householdFilterListener){
        return newInstance(fm, title, false, householdFilterListener);
    }

    public static HouseholdFilterDialog newInstance(FragmentManager fm, String title, boolean cancelable, Listener householdFilterListener){
        HouseholdFilterDialog filterDialog = new HouseholdFilterDialog();

        filterDialog.fragmentManager = fm;
        filterDialog.listener = householdFilterListener;
        filterDialog.title = title;
        filterDialog.setCancelable(cancelable);

        return filterDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.household_filter_dialog, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().setTitle(title==null ? "" : title);

        initialize(view);

        loadHieararchyItems();
        loadRegionsList();
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            Window window = dialog.getWindow();
            if (window != null)
                window.setLayout((int)(metrics.widthPixels * 0.95), (int)(metrics.heightPixels * 0.95)  // 95% width
            );
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (startSearchOnShow) {
            //onSearch();
        }
    }

    public void onHouseholdClicked(Household household) {
        showMemberListProgress(true);
        MemberSearchTask task = new MemberSearchTask(household, null, null, null, null /*household.getCode()*/);
        task.execute();
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
        }
    }

    @Override
    public void onBarcodeContextMenuItemClicked(View view, MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuBarcodePaste:
                String paste = getClipboardPasteText();
                if (view.getId()==txtHouseFilterCode.getId()) {
                    txtHouseFilterCode.setText(paste);
                }
                break;
            case R.id.menuBarcodeScan:
                if (view.getId()==txtHouseFilterCode.getId()) {
                    onHouseFilterCodeBarcodeScanClicked();
                }
                break;
        }

        Log.d("barcode menu", view.toString() + ", "+item+", paste="+getClipboardPasteText());
    }

    @Override
    public void onBarcodeScanned(int txtResId, String labelText, String resultContent) {
        Log.d("we got the barcode", ""+resultContent);

        txtHouseFilterCode.setText(resultContent);
        txtHouseFilterCode.requestFocus();
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

    private void onHouseFilterCodeBarcodeScanClicked() {
        //1-Load scan dialog (scan id or cancel)
        //2-on scan load scanner and read barcode
        //3-return with readed barcode and put on houseFilterCode EditText

        if (this.barcodeScannerListener != null){
            this.barcodeScannerListener.onBarcodeScannerClicked(R.id.txtHouseFilterCode, getString(R.string.household_filter_code_lbl), this);
        }
    }

    private void initBoxes() {
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxRounds = ObjectBoxDatabase.get().boxFor(Round.class);
    }

    private void initialize(View view) {
        this.mContext = getContext();

        this.txtDialogTitle = (TextView) view.findViewById(R.id.txtDialogTitle);
        this.btDialogClose = (Button) view.findViewById(R.id.btDialogClose);

        //Filter
        this.txtHouseFilterCode = (EditText) view.findViewById(R.id.txtHouseFilterCode);
        this.hfHousesList = view.findViewById(R.id.hfHousesList);
        this.hfViewProgressBar = (RelativeLayout) view.findViewById(R.id.hfViewProgressBar);
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
        this.btHouseFilterSearch = (Button) view.findViewById(R.id.btHouseFilterSearch);

        //List
        this.mbHouseDetailsNumber = view.findViewById(R.id.mbHouseDetailsNumber);
        this.progressBarLayout = view.findViewById(R.id.progressBarLayout);
        this.lvMembersList = view.findViewById(R.id.lvMembersList);
        this.hflSelectHousehold = view.findViewById(R.id.hflSelectHousehold);

        this.btDialogClose.setOnClickListener(v -> {
            if (isCancelable()) {
                closeDialog();
            }
        });

        this.txtHouseFilterCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>2 || s.toString().startsWith("#")){
                    searchHouses(s.toString(), true, false, false);
                }
            }
        });

        if (btHouseFilterSearch != null) {
            this.btHouseFilterSearch.setOnClickListener(v -> {
                String regionCode = currentRegion.getCode();  //get the last selected region
                String code = txtHouseFilterCode.getText().toString();
                String search = (code==null || code.isEmpty()) ? regionCode : code;

                if (StringUtil.isBlank(code)) {
                    searchHouses(regionCode, false, true, false); //search households on that region
                } else {
                    searchHouses(code, true, false, false); //search using typed text
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

        this.expListRegions.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {

            onRegionClicked(groupPosition, childPosition);

            return true;
        });

        this.hflSelectHousehold.setOnClickListener( v -> {
            onHouseholdSelected();
        });

        this.registerForContextMenu(txtHouseFilterCode);

        this.hflSelectHousehold.setEnabled(false);

        if (title != null){
            this.txtDialogTitle.setText(title);
        }
    }

    private void onHouseholdSelected() {
        dismiss();

        if (this.listener != null) {
            this.listener.onSelectedHousehold(currentHousehold);
        }
    }

    private void loadHieararchyItems(){

        List<ApplicationParam> params = boxAppParams.query().startsWith(ApplicationParam_.name, "hierarchy", QueryBuilder.StringOrder.CASE_SENSITIVE).build().find(); //COLUMN_NAME+" like 'hierarchy%'"

        for (ApplicationParam param : params){

            if (param.getName().endsWith(".head")) continue;

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

    private void loadRegionsList(){

        List<String> smodules = new ArrayList<>(loggedUser.getSelectedModules());

        List<ApplicationParam> params = boxAppParams.query().startsWith(ApplicationParam_.name, "hierarchy", QueryBuilder.StringOrder.CASE_SENSITIVE).build().find(); //COLUMN_NAME+" like 'hierarchy%'"
        List<Region> regions = this.boxRegions.query().order(Region_.code).build().find();  //query().filter((r) -> StringUtil.containsAny(r.modules, smodules)).build().find(); //filter by modules


        ArrayList<HierarchyItem> hierarchies = new ArrayList<>();
        HashMap<HierarchyItem, ArrayList<Region>> regionCollection = new HashMap<>();

        for (ApplicationParam param : params){

            if (param.getValue().isEmpty() || param.getName().endsWith(".head")) continue;

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

            //Add fake region to be SearchBox
            Region region = new Region();
            region.id = 0;
            region.code  = "SEARCH";
            region.parent = list.get(0).getParent();

            list.add(0, region);

            hierarchies.add(item);
            regionCollection.put(item, list);
        }

        this.regionAdapter = new RegionExpandableListAdapter(this.getContext(), this.expListRegions, hierarchies, regionCollection);
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

    private void updateCurrentRegion(Region region){
        this.currentRegion = region;

        Log.d("region", ""+currentRegion.getLevel()+", "+currentRegion.getName());

        updateRegionTextViews(region);
        updateButtonViews(region);
    }

    private void updateButtonViews(Region region){
        boolean lastLevel = region.getLevel().equals(lastRegionLevel);
        btHouseFilterSearch.setEnabled(lastLevel);
        //ApplicationParam param = boxAppParams.query(ApplicationParam_.name.equal(region.level)).build().findFirst();

        if (lastLevel) {
            searchHouses(region.code, false, true, false); //search households on that region
        }
    }

    private void updateRegionTextViews(Region region){

        Region reg = region;
        do {
            String regionLevel = reg.getLevel();

            if (regionLevel.equalsIgnoreCase(Region.HIERARCHY_1)){
                txtHierarchy1_value.setText(region.getName());
                txtHierarchy2_value.setText("");
                txtHierarchy3_value.setText("");
                txtHierarchy4_value.setText("");
                txtHierarchy5_value.setText("");
                txtHierarchy6_value.setText("");
                txtHierarchy7_value.setText("");
                txtHierarchy8_value.setText("");
                return;
            } else if (regionLevel.equalsIgnoreCase(Region.HIERARCHY_2)){
                txtHierarchy2_value.setText(region.getName());
                txtHierarchy3_value.setText("");
                txtHierarchy4_value.setText("");
                txtHierarchy5_value.setText("");
                txtHierarchy6_value.setText("");
                txtHierarchy7_value.setText("");
                txtHierarchy8_value.setText("");
                return;
            } else if (regionLevel.equalsIgnoreCase(Region.HIERARCHY_3)){
                txtHierarchy3_value.setText(region.getName());
                txtHierarchy4_value.setText("");
                txtHierarchy5_value.setText("");
                txtHierarchy6_value.setText("");
                txtHierarchy7_value.setText("");
                txtHierarchy8_value.setText("");
                return;
            } else if (regionLevel.equalsIgnoreCase(Region.HIERARCHY_4)){
                txtHierarchy4_value.setText(region.getName());
                txtHierarchy5_value.setText("");
                txtHierarchy6_value.setText("");
                txtHierarchy7_value.setText("");
                txtHierarchy8_value.setText("");
                return;
            } else if (regionLevel.equalsIgnoreCase(Region.HIERARCHY_5)){
                txtHierarchy5_value.setText(region.getName());
                txtHierarchy6_value.setText("");
                txtHierarchy7_value.setText("");
                txtHierarchy8_value.setText("");
                return;
            } else if (regionLevel.equalsIgnoreCase(Region.HIERARCHY_6)){
                txtHierarchy6_value.setText(region.getName());
                txtHierarchy7_value.setText("");
                txtHierarchy8_value.setText("");
                return;
            } else if (regionLevel.equalsIgnoreCase(Region.HIERARCHY_7)){
                txtHierarchy7_value.setText(region.getName());
                txtHierarchy8_value.setText("");
                return;
            } else if (regionLevel.equalsIgnoreCase(Region.HIERARCHY_8)){
                txtHierarchy8_value.setText(region.getName());
                return;
            }

            reg = boxRegions.query(Region_.code.equal(reg.parent)).build().findFirst();

        } while (reg != null);


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

        updateCurrentRegion(selectedRegion);

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
            this.expListRegions.smoothScrollToPositionFromTop(groupPosition, 0, 200);
        }
    }

    private boolean isLastRegionGroup(int groupPosition){
        return groupPosition==this.regionAdapter.getGroupCount()-1;
    }

    public void searchHouses(String householdCode, boolean searchBothNameCode, boolean searchByRegionCode, boolean searchFullHouseholdCode){
        //showProgress(true);
        HouseholdSearchTask task = new HouseholdSearchTask(householdCode, searchBothNameCode, searchByRegionCode, searchFullHouseholdCode);
        task.execute();
    }

    public HouseholdAdapter loadHouseholdsByFilters(String houseCode, boolean searchNameAndCode, boolean searchByRegionCode, boolean searchFullHouseholdCode) {
        //open loader
        //search
        List<String> smodules = new ArrayList<>(loggedUser.getSelectedModules());
        List<Household> households = new ArrayList<>();

        if (houseCode.equalsIgnoreCase("#") || houseCode.equalsIgnoreCase("#new")) {
            households = this.boxHouseholds.query(Household_.recentlyCreated.equal(true)).build().find();
        } else {
            if (searchFullHouseholdCode) {
                households = this.boxHouseholds.query(Household_.code.equal(houseCode))
                        .orderDesc(Household_.code) //query by modules
                        .filter((h) -> StringUtil.containsAny(h.modules, smodules))
                        .build().find();
            } else if (searchNameAndCode) {
                households = this.boxHouseholds.query(Household_.code.startsWith(houseCode).or(Household_.name.contains(houseCode)))
                        .orderDesc(Household_.code) //query by modules
                        .filter((h) -> StringUtil.containsAny(h.modules, smodules))
                        .build().find();
            } if (searchByRegionCode) {
                households = this.boxHouseholds.query(Household_.region.equal(houseCode))
                        .orderDesc(Household_.code) //query by modules
                        .filter((h) -> StringUtil.containsAny(h.modules, smodules))
                        .build().find();
            } else {
                households = this.boxHouseholds.query(Household_.code.startsWith(houseCode))
                        .orderDesc(Household_.code) //query by modules
                        .filter((h) -> StringUtil.containsAny(h.modules, smodules))
                        .build().find();
            }
        }


        HouseholdAdapter currentAdapter = new HouseholdAdapter(this.getActivity(), households);

        return currentAdapter;
    }

    public void showHouseholdNotFoundMessage(String code){
        Toast toast = Toast.makeText(getActivity(), getString(R.string.household_filter_household_not_found_lbl, code), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }

    private void clear(){

    }

    public MemberAdapter loadMembersByFilters(Household household, String name, String code, String householdCode, String gender, Integer minAge, Integer maxAge, Boolean isDead, Boolean hasOutmigrated, Boolean liveResident) {
        //open loader
        this.currentHousehold = household;

        List<String> endTypes = new ArrayList<>();

        if (name == null) name = "";
        if (code == null) code = "";
        if (householdCode == null) householdCode = "";
        if (gender == null) gender = "";

        //search on database
        QueryBuilder<Member> builder = this.boxMembers.query();

        if (!name.isEmpty()) {

            TextFilters filter = new TextFilters(name);
            String text = filter.getFilterText();
            switch (filter.getFilterType()) {
                case STARTSWITH:
                    builder.startsWith(Member_.name, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case ENDSWITH:
                    builder.endsWith(Member_.name, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case CONTAINS:
                    builder.contains(Member_.name, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case MULTIPLE_CONTAINS:
                    for (String t : filter.getFilterTexts()) {
                        builder.contains(Member_.name, t, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    }
                    break;
                case NONE:
                    builder.equal(Member_.name, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case EMPTY:
                    break;
            }
            //whereClause = DatabaseHelper.Member.COLUMN_NAME + " like ?";
        }
        if (!code.isEmpty()){

            TextFilters filter = new TextFilters(code);
            String text = filter.getFilterText();

            if (codeGeneratorService.isMemberCodeValid(text)) {
                filter.setFilterType(TextFilters.Filter.NONE);
            }

            switch (filter.getFilterType()) {
                case STARTSWITH:
                    builder.startsWith(Member_.code, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case ENDSWITH:
                    builder.endsWith(Member_.code, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case CONTAINS:
                    builder.contains(Member_.code, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case MULTIPLE_CONTAINS:
                    for (String t : filter.getFilterTexts()) {
                        builder.contains(Member_.code, t, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    }
                    break;
                case NONE:
                    builder.equal(Member_.code, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case EMPTY:
                    break;
            }
            //whereClause += DatabaseHelper.Member.COLUMN_CODE + " like ?";
        }
        if (!householdCode.isEmpty()){
            TextFilters filter = new TextFilters(householdCode);
            String text = filter.getFilterText();
            boolean isCodeExclusive = false;

            if (codeGeneratorService.isHouseholdCodeValid(text)) {
                filter.setFilterType(TextFilters.Filter.NONE);
                isCodeExclusive = true;
            }

            switch (filter.getFilterType()) {
                case STARTSWITH:
                    builder.startsWith(Member_.householdCode, text, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                            .or().startsWith(Member_.householdName, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case ENDSWITH:
                    builder.endsWith(Member_.householdCode, text, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                            .or().endsWith(Member_.householdName, text, QueryBuilder.StringOrder.CASE_INSENSITIVE)  ;
                    break;
                case CONTAINS:
                    builder.contains(Member_.householdCode, text, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                            .or().contains(Member_.householdName, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case MULTIPLE_CONTAINS:
                    for (String t : filter.getFilterTexts()) {
                        builder.contains(Member_.householdCode, t, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                                .or().contains(Member_.householdName, t, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    }
                    break;
                case NONE:

                    if (isCodeExclusive) {
                        builder.equal(Member_.householdCode, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    } else {
                        builder.equal(Member_.householdCode, text, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                                .or().equal(Member_.householdName, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    }
                    break;
                case EMPTY:
                    break;
            }
            //whereClause += DatabaseHelper.Member.COLUMN_HOUSEHOLD_CODE + " like ?";

        } else if (household != null) {
            builder.equal(Member_.householdCode, household.code, QueryBuilder.StringOrder.CASE_SENSITIVE);
        }
        if (!gender.isEmpty()){
            builder.equal(Member_.gender, gender, QueryBuilder.StringOrder.CASE_SENSITIVE);
            //whereClause += DatabaseHelper.Member.COLUMN_GENDER + " = ?";
        }

        //filter age
        if (minAge != null) builder.greaterOrEqual(Member_.age, minAge);
        if (maxAge != null) builder.lessOrEqual(Member_.age, maxAge);

        //At at death
        //if (minAge != null) builder.greaterOrEqual(Member_.ageAtDeath, minAge);
        //if (maxAge != null) builder.lessOrEqual(Member_.ageAtDeath, maxAge);

        if (isDead != null && isDead) endTypes.add(ResidencyEndType.DEATH.code);
        if (hasOutmigrated != null && hasOutmigrated) endTypes.add(ResidencyEndType.EXTERNAL_OUTMIGRATION.code);
        if (liveResident != null && liveResident) endTypes.add(ResidencyEndType.NOT_APPLICABLE.code);

        if (endTypes.size() > 0){
            builder.in(Member_.endType, endTypes.toArray(new String[0]), QueryBuilder.StringOrder.CASE_SENSITIVE);
        }

        //only one module is selected on login
        String module = loggedUser.getSelectedModules().stream().findFirst().get();
        if (module != null) {
            builder.contains(Member_.modules, module, QueryBuilder.StringOrder.CASE_SENSITIVE);
        }

        //Limit for now to 20000 - in the next version implement live data - implement the filter at that time
        List<Member> members = builder.build().find(0, 20000);

        MemberAdapter currentAdapter = new MemberAdapter(this.getActivity(), members);
        currentAdapter.setShowHouseholdHeadIcon(true);

        return currentAdapter;

    }

    public void setMemberAdapter(MemberAdapter memberAdapter) {
        this.lvMembersList.setAdapter(memberAdapter);
        //if is empty
        boolean value =  (memberAdapter == null || memberAdapter.isEmpty());

        //disable buttons
        this.hflSelectHousehold.setEnabled(!value);

        if (currentHousehold != null){
            this.mbHouseDetailsNumber.setText(currentHousehold.getCode());
        }

    }

    public MemberAdapter getMemberAdapter(){
        if (lvMembersList.getAdapter() instanceof MemberAdapter){
            return (MemberAdapter) lvMembersList.getAdapter();
        }

        return null;
    }

    public void setStartSearchOnShow(boolean searchOnShow) {
        this.startSearchOnShow = searchOnShow;
    }

    private void showMemberListProgress(final boolean show) {
        //lvMembersList.setAdapter(null);
        lvMembersList.setVisibility(show ? View.GONE : View.VISIBLE);
        progressBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void show(){
        this.show(fragmentManager, "relatype");
    }

    private void closeDialog(){
        dismiss();

        if (this.listener != null){
            this.listener.onCanceled();
        }
    }

    class HouseholdSearchTask extends AsyncTask<Void, Void, HouseholdAdapter> {
        private String code;
        private boolean searchNameAndCode;
        private boolean searchByRegionCode;
        private boolean searchFullHouseholdCode;

        public HouseholdSearchTask(String code, boolean searchNameAndCode, boolean searchByRegionCode, boolean searchFullHouseholdCode) {
            this.code = code;
            this.searchNameAndCode = searchNameAndCode;
            this.searchByRegionCode = searchByRegionCode;
            this.searchFullHouseholdCode = searchFullHouseholdCode;
        }

        @Override
        protected HouseholdAdapter doInBackground(Void... params) {
            return loadHouseholdsByFilters(code, searchNameAndCode, searchByRegionCode, searchFullHouseholdCode);
        }

        @Override
        protected void onPostExecute(HouseholdAdapter adapter) {
            //HouseholdFilterDialog.this.currentHousehold = household;
            hfHousesList.setAdapter(adapter);
            //showProgress(false);

            if (adapter.getItemCount()==0){
                showHouseholdNotFoundMessage(code);
            }
        }
    }

    class MemberSearchTask extends AsyncTask<Void, Void, MemberAdapter> {
        private String name;
        private String code;
        private String gender;
        private String houseCode;
        private Household household;

        public MemberSearchTask(Household household, String name, String code, String gender, String houseCode) {
            this.name = name;
            this.code = code;
            this.gender = gender;
            this.houseCode = houseCode;
            this.household = household;
        }

        @Override
        protected MemberAdapter doInBackground(Void... params) {
            return loadMembersByFilters(household, name, code, houseCode, gender, null, null, null, null, true);
        }

        @Override
        protected void onPostExecute(MemberAdapter adapter) {

            adapter.setShowExtraDetails(true);
            adapter.setShowHouseholdAndCode(false);
            adapter.setShowMemberDetails(true);

            currentHousehold = household;
            setMemberAdapter(adapter);
            showMemberListProgress(false);
        }
    }

    public interface Listener {
        void onSelectedHousehold(Household member);

        void onCanceled();
    }
}
