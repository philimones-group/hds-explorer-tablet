package org.philimone.hds.explorer.main;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.TrackingExpandableListAdapter;
import org.philimone.hds.explorer.adapter.model.TrackingSubjectItem;
import org.philimone.hds.explorer.adapter.model.TrackingSubListItem;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.Database;
import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.Dataset;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.followup.TrackingList;
import org.philimone.hds.explorer.widget.LoadingDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import io.objectbox.Box;
import mz.betainteractive.utilities.StringUtil;

public class TrackingListDetailsActivity extends Activity implements BarcodeScannerActivity.ResultListener, BarcodeScannerActivity.InvokerClickListener {

    public static final int RC_REGION_DETAILS_TRACKINGLIST = 20;
    public static final int RC_HOUSEHOLD_DETAILS_TRACKINGLIST = 21;
    public static final int RC_MEMBER_DETAILS_TRACKINGLIST = 22;


    private TextView txtTrackListTitle;
    private TextView txtTrackListDetails;
    private EditText txtTrackListFilter;
    private ExpandableListView elvTrackingLists;
    private Button btTrackListBack;

    private TrackingExpandableListAdapter adapter;
    private TrackingList trackingList;
    private User loggedUser;

    private View viewLoadingList;

    private Database database;
    private Box<CollectedData> boxCollectedData;
    private Box<Form> boxForms;
    private Box<Region> boxRegions;
    private Box<Dataset> boxDatasets;

    private LoadingDialog loadingDialog;

    private int lastSelectedGroupPosition;
    private int lastSelectedChildPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_list_details);

        initBoxes();
        initialize();

        if (savedInstanceState == null){
            return;
        }

        String track_list_filter = savedInstanceState.getString("track_list_filter");
        this.txtTrackListFilter.setText(track_list_filter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //the activity is now visible
        showProgress(true);

        new TrackingSubjectListSearchTask(trackingList).execute();
    }

    private void initBoxes() {
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxDatasets = ObjectBoxDatabase.get().boxFor(Dataset.class);
    }

    private void initialize() {
        this.database = new Database(this);
        this.loggedUser = (User) getIntent().getExtras().get("user");
        this.trackingList = (TrackingList) getIntent().getExtras().get("trackinglist");

        this.txtTrackListTitle = (TextView) findViewById(R.id.txtTrackListTitle);
        this.txtTrackListDetails = (TextView) findViewById(R.id.txtTrackListDetails);
        this.txtTrackListFilter = (EditText) findViewById(R.id.txtTrackListFilter);
        this.elvTrackingLists = (ExpandableListView) findViewById(R.id.elvTrackingLists);
        this.btTrackListBack = (Button) findViewById(R.id.btTrackListBack);
        this.viewLoadingList = findViewById(R.id.viewListProgressBar);

        this.elvTrackingLists.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                onSubjectItemClicked(groupPosition, childPosition);
                return true;
            }
        });

        this.txtTrackListFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterSubjectsByCode(s.toString());
            }
        });

        this.txtTrackListFilter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onTrackListFilterCodeClicked();
                return true;
            }
        });

        this.btTrackListBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrackingListDetailsActivity.this.onBackPressed();
            }
        });

        setDataToComponents();

        this.loadingDialog = new LoadingDialog(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putString("track_list_filter", this.txtTrackListFilter.getText().toString());

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState == null){
            return;
        }

        String track_list_filter = savedInstanceState.getString("track_list_filter");
        this.txtTrackListFilter.setText(track_list_filter);
    }

    private void setDataToComponents() {
        txtTrackListTitle.setText(trackingList.getTitle());
        txtTrackListDetails.setText(trackingList.getCode());
    }

    private void setTrackingListAdapter(TrackingExpandableListAdapter mAdapter){
        this.adapter = mAdapter;
        elvTrackingLists.setAdapter(this.adapter);

        showProgress(false);
        expandAllGroups();

        //run filter data
        filterSubjectsByCode(this.txtTrackListFilter.getText().toString());
    }

    private void expandAllGroups(){
        if (adapter != null)
        for ( int i = 0; i < adapter.getGroupCount(); i++ ){
            elvTrackingLists.expandGroup(i);
        }
    }

    public void showProgress(final boolean show) {
        viewLoadingList.setVisibility(show ? View.VISIBLE : View.GONE);
        elvTrackingLists.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showLoadingDialog(String msg, boolean show){
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.hide();
        }
    }

    private void onSubjectItemClicked(int groupPosition, int childPosition) {
        TrackingSubjectItem subjectItem = (TrackingSubjectItem) adapter.getChild(groupPosition, childPosition);

        OnSubjectSelectedTask task = new OnSubjectSelectedTask(subjectItem);
        task.execute();

        showLoadingDialog(getString(R.string.loading_dialog_member_details_lbl), true);

    }

    private Household getHousehold(Member member){
        if (member == null || member.getHouseholdCode()==null) return null;


        database.open();
        Household household = Queries.getHouseholdBy(database, DatabaseHelper.Household.COLUMN_CODE +"=?", new String[]{ member.getHouseholdCode() });
        database.close();

        return household;
    }

    private Region getRegion(Household household){
        if (household == null || household.getRegion()==null) return null;

        Region region = this.boxRegions.query().equal(Region_.code, household.getRegion()).build().findFirst();

        return region;
    }

    public FormDataLoader[] getFormLoaders(TrackingSubjectItem subjectItem){

        List<Form> forms = this.boxForms.getAll(); //get all forms
        List<FormDataLoader> list = new ArrayList<>();

        int i=0;
        for (Form form : forms){
            if (subjectItem.getForms().contains(form.getFormId())){ //Create formloader for only the forms that the subjectItem has to collect
                FormDataLoader loader = new FormDataLoader(form);
                list.add(loader);
            }
        }

        FormDataLoader[] aList = new FormDataLoader[list.size()];

        return list.toArray(aList);
    }

    private void loadFormValues(FormDataLoader loader, Household household, Member member, Region region, TrackingSubjectItem subjectItem){
        if (household != null){
            loader.loadHouseholdValues(household);
        }
        if (member != null){
            loader.loadMemberValues(member);
        }
        if (loggedUser != null){
            loader.loadUserValues(loggedUser);
        }
        if (region != null){
            loader.loadRegionValues(region);
        }

        loader.loadConstantValues();
        loader.loadSpecialConstantValues(household, member, loggedUser, region, subjectItem);

        //Load variables on datasets
        for (Dataset dataSet : getDataSets()){
            if (loader.hasMappedDatasetVariable(dataSet)){
                //Log.d("hasMappedVariables", ""+dataSet.getName());
                loader.loadDataSetValues(dataSet, household, member, loggedUser, region);
            }
        }
    }

    private void loadFormValues(FormDataLoader[] loaders, Household household, Member member, Region region, TrackingSubjectItem subjectItem){
        for (FormDataLoader loader : loaders){
            loadFormValues(loader, household, member, region, subjectItem);
        }
    }

    private List<Dataset> getDataSets(){
        List<Dataset> list = this.boxDatasets.getAll();

        return list;
    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode== RC_MEMBER_DETAILS_TRACKINGLIST){

        }
    }
    */

    //reading tracking list
    private TrackingExpandableListAdapter readTrackingSubjectLists(TrackingList trackingList){
        Database db = new Database(this);


        db.open();
        List<org.philimone.hds.explorer.model.followup.TrackingSubjectList> listTml = Queries.getAllTrackingSubjectListBy(db, DatabaseHelper.TrackingSubjectList.COLUMN_TRACKING_ID+"=?", new String[]{ trackingList.getId()+"" });
        List<CollectedData> listCollectedData = this.boxCollectedData.query().equal(CollectedData_.formModule, trackingList.getModule()).build().find();
        db.close();

        List<String> codesRegions = new ArrayList<>();
        List<String> codesHouseholds = new ArrayList<>();
        List<String> codesMembers = new ArrayList<>();


        for (org.philimone.hds.explorer.model.followup.TrackingSubjectList tm : listTml){

            if (tm.isRegionSubject()){
                codesRegions.add(tm.getSubjectCode());
                Log.d("r-code-", ""+tm.getSubjectCode());
            }
            if (tm.isHouseholdSubject()){
                codesHouseholds.add(tm.getSubjectCode());
                Log.d("h-code-", ""+tm.getSubjectCode());
            }
            if (tm.isMemberSubject()){
                codesMembers.add(tm.getSubjectCode());
                Log.d("code-", ""+tm.getSubjectCode());
            }

        }

        List<Region> regions = getRegions(db, codesRegions);
        List<Household> households = getHouseholds(db, codesHouseholds);
        List<Member> members = getMembers(db, codesMembers);

        Log.d("listTml", ""+listTml.size());
        Log.d("listCollectedData", ""+listCollectedData.size());
        Log.d("regions", ""+regions.size());
        Log.d("households", ""+households.size());
        Log.d("members", ""+members.size());

        ArrayList<TrackingSubListItem> groupItems = new ArrayList<>();
        HashMap<TrackingSubListItem, ArrayList<TrackingSubjectItem>> trackingCollection = new LinkedHashMap<>();

        //I need member/region/household, collectedData
        for (org.philimone.hds.explorer.model.followup.TrackingSubjectList item : listTml){

            TrackingSubListItem tsi = getSubListItem(item, trackingList);

            TrackingSubjectItem subjectItem = null;
            if (item.isRegionSubject()){
                subjectItem = getSubjectItem(regions, null, null, listCollectedData, item, tsi);
            }
            if (item.isHouseholdSubject()){
                subjectItem = getSubjectItem(null, households, null, listCollectedData, item, tsi);
            }
            if (item.isMemberSubject()){
                subjectItem = getSubjectItem(null, null, members, listCollectedData, item, tsi);
            }


            ArrayList<TrackingSubjectItem> listSubjects = trackingCollection.get(tsi);

            if (listSubjects == null){
                listSubjects = new ArrayList<>();

                groupItems.add(tsi);
                listSubjects.add(subjectItem);

                trackingCollection.put(tsi, listSubjects);
            }else{
                listSubjects.add(subjectItem);
            }
        }

        //db.close();

        TrackingExpandableListAdapter adapter = new TrackingExpandableListAdapter(this, groupItems, trackingCollection);
        return adapter;
    }

    private List<Region> getRegions(Database db, List<String> codes){
        String[] arrayCodes = codes.toArray(new String[codes.size()]);

        List<Region> regions = this.boxRegions.query().in(Region_.code, arrayCodes).build().find();
        //Queries.getAllRegionBy(db, DatabaseHelper.Region.COLUMN_CODE +" IN ("+ StringUtil.toInClause(codes) +")", null);

        if (regions==null) return new ArrayList<>();

        return regions;
    }

    private List<Household> getHouseholds(Database db, List<String> codes){
        db.open();
        List<Household> households = Queries.getAllHouseholdBy(db, DatabaseHelper.Household.COLUMN_CODE +" IN ("+ StringUtil.toInClause(codes) +")", null);
        db.close();

        if (households==null) return new ArrayList<>();

        return households;
    }

    private List<Member> getMembers(Database db, List<String> codes){
        db.open();
        List<Member> members = Queries.getAllMemberBy(db, DatabaseHelper.Member.COLUMN_CODE +" IN ("+ StringUtil.toInClause(codes) +")", null);
        db.close();

        if (members==null) return new ArrayList<>();

        return members;
    }

    private TrackingSubjectItem getSubjectItem(List<Region> regions, List<Household> households, List<Member> members, List<CollectedData> collectedDataList, org.philimone.hds.explorer.model.followup.TrackingSubjectList item, TrackingSubListItem subListItem) {
        TrackingSubjectItem tSubject = new TrackingSubjectItem();


        Region region = null;
        Household household = null;
        Member member = null;

        List<CollectedData> listCollected = new ArrayList<>();
        String[] forms = item.getSubjectForms().split(",");
        List<String> formsList = Arrays.asList(forms);

        if (regions != null){
            for (Region rg : regions){
                if (rg.getCode().equals(item.getSubjectCode())){
                    region = rg;
                    break;
                }
            }
        } else if (households != null){
            for (Household hh : households){
                if (hh.getCode().equals(item.getSubjectCode())){
                    household = hh;
                    break;
                }
            }
        } else if (members != null){
            for (Member mb : members){
                if (mb.getCode().equals(item.getSubjectCode())){
                    member = mb;
                    break;
                }
            }
        }

        Log.d("region", ""+region);
        Log.d("household", ""+household);
        Log.d("member", ""+member);

        long tableId = (region!=null) ? region.getId() : ( (household!=null) ? household.getId() : ((member!=null) ? member.getId() : 0) );

        for (CollectedData cd : collectedDataList){
            if (formsList.contains(cd.getFormId()) && cd.getRecordId()==tableId){ //get the selected subject Collected Data
                listCollected.add(cd);
            }
        }

        //List<CollectedData> list = Queries.getAllCollectedDataBy(db, DatabaseHelper.CollectedData.COLUMN_RECORD_ID+"=? AND "+DatabaseHelper.CollectedData.COLUMN_FORM_ID+" IN (?)", new String[]{ member.getId()+"", StringUtil.toInClause(forms)});

        tSubject.setRegion(region);
        tSubject.setHousehold(household);
        tSubject.setMember(member);
        tSubject.setListItem(subListItem);
        tSubject.setSubjectType(item.getSubjectType());
        tSubject.setVisitNumber(item.getSubjectVisit());
        tSubject.addForms(forms);
        tSubject.addCollectedData(listCollected);

        return tSubject;
    }

    private TrackingSubListItem getSubListItem(org.philimone.hds.explorer.model.followup.TrackingSubjectList trackingSubjectList, TrackingList trackingList){
        TrackingSubListItem tsi = new TrackingSubListItem();

        tsi.setId(trackingSubjectList.getListId());
        tsi.setTrackingList(trackingList);
        tsi.setTitle(trackingSubjectList.getTitle());
        tsi.setForms(trackingSubjectList.getForms().split(","));

        return tsi;
    }

    private TrackingSubListItem getSubListItem(List<Member> members, List<CollectedData> collectedDataList, org.philimone.hds.explorer.model.followup.TrackingSubjectList trackingMemberList, TrackingList trackingList){
        TrackingSubListItem tsi = new TrackingSubListItem();

        tsi.setId(trackingMemberList.getListId());
        tsi.setTrackingList(trackingList);
        tsi.setTitle(trackingMemberList.getTitle());
        tsi.setForms(trackingMemberList.getForms().split(","));

        return tsi;
    }

    private void filterSubjectsByCode(String code){
        if (code != null){

            adapter.filterSubjects(code);
            adapter.notifyDataSetChanged();
            this.elvTrackingLists.invalidateViews();
        }
    }

    private void onTrackListFilterCodeClicked() {
        //1-Load scan dialog (scan id or cancel)
        //2-on scan load scanner and read barcode
        //3-return with readed barcode and put on houseFilterCode EditText

        this.onBarcodeScannerClicked(R.id.txtTrackListFilter, getString(R.string.trackinglist_filter_code_hint_lbl), this);
    }

    @Override
    public void onBarcodeScanned(int txtResId, String labelText, String resultContent) {
        //if (textBox != null)
        //    textBox.requestFocus();

        Log.d("we got the barcode", ""+resultContent);

        this.txtTrackListFilter.setText(resultContent);
        this.txtTrackListFilter.requestFocus();

    }

    @Override
    public void onBarcodeScannerClicked(int txtResId, String labelText, BarcodeScannerActivity.ResultListener resultListener) {
        Intent intent = new Intent(this, BarcodeScannerActivity.class);


        String resultHashCode = resultListener.hashCode()+"";
        intent.putExtra("text_box_res_id", txtResId);
        intent.putExtra("text_box_label", labelText);
        intent.putExtra("result_listener_code", resultHashCode);

        Log.d("res listener", ""+resultListener);

        //barcodeResultListeners.put(resultHashCode, resultListener);

        //Log.d("res listener size", ""+barcodeResultListeners.size());


        startActivityForResult(intent, BarcodeScannerActivity.SCAN_BARCODE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BarcodeScannerActivity.SCAN_BARCODE_REQUEST_CODE && resultCode == RESULT_OK){
            //send result back to the invoker listener

            int txtResId = data.getExtras().getInt("text_box_res_id");
            String txtLabel = data.getExtras().getString("text_box_label");
            String barcode = data.getExtras().getString("scanned_barcode");
            String resultListenerCode = data.getExtras().getString("result_listener_code");

            Log.d("returning with barcode", ""+barcode+", listener="+resultListenerCode);
            //Log.d("contains listener", ""+barcodeResultListeners.containsKey(resultListenerCode));
            //Log.d("listeners", ""+barcodeResultListeners);


            this.onBarcodeScanned(txtResId, txtLabel, barcode);

        }
    }

    class TrackingSubjectListSearchTask extends AsyncTask<Void, Void, TrackingExpandableListAdapter> {

        private TrackingList trackingList;

        public TrackingSubjectListSearchTask(TrackingList trackingList){
            this.trackingList = trackingList;
        }

        @Override
        protected TrackingExpandableListAdapter doInBackground(Void... params) {
            return readTrackingSubjectLists(trackingList);
        }

        @Override
        protected void onPostExecute(TrackingExpandableListAdapter mAdapter) {
            saveCompletionOfList(mAdapter);
            setTrackingListAdapter(mAdapter);
        }

        private void saveCompletionOfList(TrackingExpandableListAdapter adapter) {
            int c = adapter.getCompletionOfTrackingList();

            trackingList.setCompletionRate(c/100D);

            Database db = new Database(TrackingListDetailsActivity.this);
            db.open();
            db.update(TrackingList.class, trackingList.getContentValues(), DatabaseHelper.TrackingList._ID+"=?", new String[]{ trackingList.getId()+"" });
            db.close();
        }
    }

    class OnSubjectSelectedTask extends AsyncTask<Void, Void, Void> {
        private Household household;
        private Member member;
        private Region region;
        private TrackingSubjectItem subjectItem;
        private FormDataLoader[] dataLoaders;
        private boolean isRegion;
        private boolean isHousehold;
        private boolean isMember;

        public OnSubjectSelectedTask(TrackingSubjectItem subjectItem) {
            this.subjectItem = subjectItem;
            this.isRegion = subjectItem.isRegionSubject();
            this.isHousehold = subjectItem.isHouseholdSubject();
            this.isMember = subjectItem.isMemberSubject();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            this.dataLoaders = getFormLoaders(subjectItem);

            if (isRegion){
                this.region = subjectItem.getRegion();

            } else if (isHousehold){
                this.household = subjectItem.getHousehold();
                this.region = getRegion(household);

            } else if (isMember){
                this.member = subjectItem.getMember();
                this.household = getHousehold(subjectItem.getMember());
                this.region = getRegion(household);
            }

            loadFormValues(dataLoaders, household, member, region, subjectItem);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = null;

            if (isRegion) {
                intent = new Intent(TrackingListDetailsActivity.this, RegionDetailsActivity.class);
                intent.putExtra("user", loggedUser);
                intent.putExtra("region", region);
                intent.putExtra("dataloaders", dataLoaders);
                intent.putExtra("request_code", RC_REGION_DETAILS_TRACKINGLIST);
            }

            if (isHousehold) {
                intent = new Intent(TrackingListDetailsActivity.this, HouseholdDetailsActivity.class);
                intent.putExtra("user", loggedUser);
                intent.putExtra("household", household);
                intent.putExtra("dataloaders", dataLoaders);
                intent.putExtra("request_code", RC_HOUSEHOLD_DETAILS_TRACKINGLIST);
            }

            if (isMember) {
                intent = new Intent(TrackingListDetailsActivity.this, MemberDetailsActivity.class);
                intent.putExtra("user", loggedUser);
                intent.putExtra("member", subjectItem.getMember());
                intent.putExtra("member_studycode", subjectItem.getSubjectType());
                intent.putExtra("request_code", RC_MEMBER_DETAILS_TRACKINGLIST);
                intent.putExtra("dataloaders", dataLoaders);
            }


            showLoadingDialog(null, false);

            startActivityForResult(intent, RC_MEMBER_DETAILS_TRACKINGLIST);
        }
    }

}

