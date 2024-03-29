package org.philimone.hds.explorer.main;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.model.TrackingSubListItem;
import org.philimone.hds.explorer.adapter.model.TrackingSubjectItem;
import org.philimone.hds.explorer.adapter.trackinglist.TrackingGroupItem;
import org.philimone.hds.explorer.adapter.trackinglist.TrackingListExpandableAdapter;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.Dataset;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.followup.TrackingList;
import org.philimone.hds.explorer.model.followup.TrackingSubjectList;
import org.philimone.hds.explorer.model.followup.TrackingSubjectList_;
import org.philimone.hds.explorer.settings.RequestCodes;
import org.philimone.hds.explorer.widget.LoadingDialog;
import org.philimone.hds.explorer.widget.RecyclerListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

public class TrackingListDetailsActivity extends AppCompatActivity implements BarcodeScannerActivity.ResultListener, BarcodeScannerActivity.InvokerClickListener {

    private TextView txtTrackListTitle;
    private TextView txtTrackListDetails;
    private EditText txtTrackListFilter;
    private RecyclerListView elvTrackingLists;
    private Button btTrackListBack;

    private TrackingListExpandableAdapter adapter;
    private TrackingList trackingList;
    private User loggedUser;

    private View viewLoadingList;

    private Box<CollectedData> boxCollectedData;
    private Box<Form> boxForms;
    private Box<Region> boxRegions;
    private Box<Dataset> boxDatasets;
    private Box<TrackingList> boxTrackingLists;
    private Box<TrackingSubjectList> boxTrackingSubjects;
    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;

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
        this.boxTrackingLists = ObjectBoxDatabase.get().boxFor(TrackingList.class);
        this.boxTrackingSubjects = ObjectBoxDatabase.get().boxFor(TrackingSubjectList.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
    }

    private void initialize() {
        this.loggedUser = Bootstrap.getCurrentUser();
        long trackingListId = getIntent().getExtras().getLong("trackinglist");
        this.trackingList = boxTrackingLists.get(trackingListId);

        this.txtTrackListTitle = (TextView) findViewById(R.id.txtTrackListTitle);
        this.txtTrackListDetails = (TextView) findViewById(R.id.txtTrackListDetails);
        this.txtTrackListFilter = (EditText) findViewById(R.id.txtTrackListFilter);
        this.elvTrackingLists = findViewById(R.id.elvTrackingLists);
        this.btTrackListBack = (Button) findViewById(R.id.btTrackListBack);
        this.viewLoadingList = findViewById(R.id.viewListProgressBar);

        this.elvTrackingLists.addOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long id) {
                onSubjectItemClicked(position);
            }

            @Override
            public void onItemLongClick(View view, int position, long id) {

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

    private void setTrackingListAdapter(TrackingListExpandableAdapter mAdapter){
        this.adapter = mAdapter;
        elvTrackingLists.setAdapter(this.adapter);

        showProgress(false);
        expandAllGroups();

        //run filter data
        filterSubjectsByCode(this.txtTrackListFilter.getText().toString());
    }

    private void expandAllGroups(){
        if (adapter != null) {
            adapter.expandAll();
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
            this.loadingDialog.dismiss();
        }
    }

    private void onSubjectItemClicked(int itemPosition) {

        TrackingGroupItem listItem = adapter.getItem(itemPosition);

        if (listItem.isChildItem()) {
            TrackingSubjectItem subjectItem = listItem.getChildItem();

            OnSubjectSelectedTask task = new OnSubjectSelectedTask(subjectItem);
            task.execute();

            showLoadingDialog(getString(R.string.loading_dialog_member_details_lbl), true);
        }

    }

    private Household getHousehold(Member member){
        if (member == null || member.getHouseholdCode()==null) return null;

        Household household = Queries.getHouseholdByCode(boxHouseholds, member.getHouseholdCode());

        return household;
    }

    private Region getRegion(Household household){
        if (household == null || household.getRegion()==null) return null;

        Region region = this.boxRegions.query().equal(Region_.code, household.getRegion(), QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        return region;
    }

    public FormDataLoader[] getFormLoaders(TrackingSubjectItem subjectItem){

        List<Form> forms = this.boxForms.getAll(); //get all forms
        List<FormDataLoader> list = new ArrayList<>();

        int i=0;
        for (Form form : forms){
            if (subjectItem.getForms().contains(form.getFormId())){ //Create formloader for only the forms that the subjectItem has to collect
                //FormDataLoader loader = new FormDataLoader(form, subjectItem);
                //list.add(loader);
            }
        }

        FormDataLoader[] aList = new FormDataLoader[list.size()];

        return list.toArray(aList);
    }

    //reading tracking list
    private TrackingListExpandableAdapter readTrackingSubjectLists(TrackingList trackingList){

        List<TrackingSubjectList> listTml = this.boxTrackingSubjects.query().equal(TrackingSubjectList_.trackingId, trackingList.getId()).build().find();
        List<CollectedData> listCollectedData = Queries.getCollectedDataBy(this.boxCollectedData, trackingList.modules);

        List<String> codesRegions = new ArrayList<>();
        List<String> codesHouseholds = new ArrayList<>();
        List<String> codesMembers = new ArrayList<>();


        for (TrackingSubjectList tm : listTml){

            if (tm.isRegionSubject()){
                codesRegions.add(tm.getSubjectCode());
                //Log.d("r-code-", ""+tm.getSubjectCode());
            }
            if (tm.isHouseholdSubject()){
                codesHouseholds.add(tm.getSubjectCode());
                //Log.d("h-code-", ""+tm.getSubjectCode());
            }
            if (tm.isMemberSubject()){
                codesMembers.add(tm.getSubjectCode());
                //Log.d("code-", ""+tm.getSubjectCode());
            }

        }

        List<Region> regions = getRegions(codesRegions);
        List<Household> households = getHouseholds(codesHouseholds);
        List<Member> members = getMembers(codesMembers);

        //Log.d("listTml", ""+listTml.size());
        //Log.d("listCollectedData", ""+listCollectedData.size());
        //Log.d("regions", ""+regions.size());
        //Log.d("households", ""+households.size());
        //Log.d("members", ""+members.size());

        ArrayList<TrackingSubListItem> groupItems = new ArrayList<>();
        HashMap<TrackingSubListItem, List<TrackingSubjectItem>> trackingCollection = new LinkedHashMap<>();

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


            List<TrackingSubjectItem> listSubjects = trackingCollection.get(tsi);

            if (listSubjects == null){
                listSubjects = new ArrayList<>();

                groupItems.add(tsi);

                if (!subjectItem.isSubjectNull()){
                    listSubjects.add(subjectItem);
                }

                trackingCollection.put(tsi, listSubjects);
            }else{
                if (!subjectItem.isSubjectNull()){
                    listSubjects.add(subjectItem);
                }
            }
        }

        //db.close();

        TrackingListExpandableAdapter adapter = new TrackingListExpandableAdapter(this.elvTrackingLists, trackingCollection);
        return adapter;
    }

    private List<Region> getRegions(List<String> codes){
        String[] arrayCodes = codes.toArray(new String[codes.size()]);

        List<Region> regions = this.boxRegions.query().in(Region_.code, arrayCodes, QueryBuilder.StringOrder.CASE_SENSITIVE).build().find();
        //Queries.getAllRegionBy(db, DatabaseHelper.Region.COLUMN_CODE +" IN ("+ StringUtil.toInClause(codes) +")", null);

        if (regions==null) return new ArrayList<>();

        return regions;
    }

    private List<Household> getHouseholds(List<String> codes){

        String[] codesArray = codes.toArray(new String[codes.size()]);

        List<Household> households = this.boxHouseholds.query().in(Household_.code, codesArray, QueryBuilder.StringOrder.CASE_SENSITIVE).build().find();

        if (households==null) return new ArrayList<>();

        return households;
    }

    private List<Member> getMembers(List<String> codes){
        String[] codesArray = codes.toArray(new String[codes.size()]);
        List<Member> members = this.boxMembers.query().in(Member_.code, codesArray, QueryBuilder.StringOrder.CASE_SENSITIVE).build().find();

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

        //Log.d("region", ""+region);
        //Log.d("household", ""+household);
        //Log.d("member", ""+member);

        long tableId = (region!=null) ? region.getId() : ( (household!=null) ? household.getId() : ((member!=null) ? member.getId() : 0) );

        for (CollectedData cd : collectedDataList){
            if (formsList.contains(cd.getFormId()) && cd.getRecordId()==tableId){ //get the selected subject Collected Data
                listCollected.add(cd);
            }
        }

        //List<CollectedData> list = Queries.getAllCollectedDataBy(db, DatabaseHelper.CollectedData.COLUMN_RECORD_ID+"=? AND "+DatabaseHelper.CollectedData.COLUMN_FORM_ID+" IN (?)", new String[]{ member.getId()+"", StringUtil.toInClause(forms)});

        tSubject.setEntityId(item.id);
        tSubject.setRegion(region);
        tSubject.setHousehold(household);
        tSubject.setMember(member);
        tSubject.setListItem(subListItem);
        tSubject.setSubjectType(item.getSubjectType());
        tSubject.setVisitCode(item.subjectVisitCode);
        tSubject.setVisitUuid(item.subjectVisitUuid);
        tSubject.addForms(forms);
        tSubject.addCollectedData(listCollected);

        return tSubject;
    }

    private TrackingSubListItem getSubListItem(org.philimone.hds.explorer.model.followup.TrackingSubjectList trackingSubjectList, TrackingList trackingList){
        TrackingSubListItem tsi = new TrackingSubListItem();

        tsi.setId(trackingSubjectList.getListId());
        tsi.setTrackingList(trackingList);
        tsi.setTitle(trackingSubjectList.getTitle());

        return tsi;
    }

    private TrackingSubListItem getSubListItem(List<Member> members, List<CollectedData> collectedDataList, org.philimone.hds.explorer.model.followup.TrackingSubjectList trackingMemberList, TrackingList trackingList){
        TrackingSubListItem tsi = new TrackingSubListItem();

        tsi.setId(trackingMemberList.getListId());
        tsi.setTrackingList(trackingList);
        tsi.setTitle(trackingMemberList.getTitle());

        return tsi;
    }

    private void filterSubjectsByCode(String code){
        if (code != null && adapter != null){
            adapter.filterSubjects(code);
            //this.elvTrackingLists.invalidate();
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


        startActivityForResult(intent, RequestCodes.SCAN_BARCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.SCAN_BARCODE && resultCode == RESULT_OK){
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

    class TrackingSubjectListSearchTask extends AsyncTask<Void, Void, TrackingListExpandableAdapter> {

        private TrackingList trackingList;

        public TrackingSubjectListSearchTask(TrackingList trackingList){
            this.trackingList = trackingList;
        }

        @Override
        protected TrackingListExpandableAdapter doInBackground(Void... params) {
            return readTrackingSubjectLists(trackingList);
        }

        @Override
        protected void onPostExecute(TrackingListExpandableAdapter mAdapter) {
            saveCompletionOfList(mAdapter);
            setTrackingListAdapter(mAdapter);
        }

        private void saveCompletionOfList(TrackingListExpandableAdapter adapter) {
            int c = adapter.getCompletionOfTrackingList();

            trackingList.setCompletionRate(c/100D);
            boxTrackingLists.put(trackingList);
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

            //this.dataLoaders = getFormLoaders(subjectItem);

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

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = null;

            if (isRegion) {
                intent = new Intent(TrackingListDetailsActivity.this, RegionDetailsActivity.class);
                intent.putExtra("region", region.id);
                intent.putExtra("tracking_subject_id", subjectItem.getEntityId());
                intent.putExtra("request_code", RequestCodes.REGION_DETAILS_FROM_TRACKING_LIST_DETAILS);
            }

            if (isHousehold) {
                intent = new Intent(TrackingListDetailsActivity.this, HouseholdDetailsActivity.class);
                intent.putExtra("household", household.id);
                intent.putExtra("tracking_subject_id", subjectItem.getEntityId());
                intent.putExtra("request_code", RequestCodes.HOUSEHOLD_DETAILS_FROM_TRACKING_LIST_DETAILS);
            }

            if (isMember) {
                intent = new Intent(TrackingListDetailsActivity.this, MemberDetailsActivity.class);
                intent.putExtra("member", member.id);
                intent.putExtra("request_code", RequestCodes.MEMBER_DETAILS_FROM_TRACKING_LIST_DETAILS);
                intent.putExtra("tracking_subject_id", subjectItem.getEntityId());
            }


            showLoadingDialog(null, false);

            startActivityForResult(intent, RequestCodes.MEMBER_DETAILS_FROM_TRACKING_LIST_DETAILS);
        }
    }

}

