package org.philimone.hds.explorer.main;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.TrackingExpandableListAdapter;
import org.philimone.hds.explorer.adapter.model.TrackingMemberItem;
import org.philimone.hds.explorer.adapter.model.TrackingSubListItem;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.Database;
import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.DataSet;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.followup.TrackingList;
import org.philimone.hds.explorer.model.followup.TrackingMemberList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import mz.betainteractive.utilities.StringUtil;

public class TrackingListDetailsActivity extends Activity {

    public static final int RC_MEMBER_DETAILS_TRACKINGLIST = 20;

    private TextView txtTrackListTitle;
    private TextView txtTrackListDetails;
    private ExpandableListView elvTrackingLists;

    private TrackingExpandableListAdapter adapter;
    private TrackingList trackingList;
    private User loggedUser;

    private View viewLoadingList;

    private Database database;

    private int lastSelectedGroupPosition;
    private int lastSelectedChildPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_list_details);

        initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //the activity is now visible
        showProgress(true);

        new TrackingMemberListSearchTask(trackingList).execute();
    }

    private void initialize() {
        this.database = new Database(this);
        this.loggedUser = (User) getIntent().getExtras().get("user");
        this.trackingList = (TrackingList) getIntent().getExtras().get("trackinglist");

        this.txtTrackListTitle = (TextView) findViewById(R.id.txtTrackListTitle);
        this.txtTrackListDetails = (TextView) findViewById(R.id.txtTrackListDetails);
        this.elvTrackingLists = (ExpandableListView) findViewById(R.id.elvTrackingLists);
        this.viewLoadingList = findViewById(R.id.viewListProgressBar);

        this.elvTrackingLists.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                onMemberItemClicked(groupPosition, childPosition);
                return true;
            }
        });

        setDataToComponents();
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

    private void onMemberItemClicked(int groupPosition, int childPosition) {
        TrackingMemberItem memberItem = (TrackingMemberItem) adapter.getChild(groupPosition, childPosition);

        FormDataLoader[] dataLoaders = getFormLoaders(memberItem);
        Household household = getHousehold(memberItem.getMember());
        Region region = getRegion(household);

        loadFormValues(dataLoaders, household, memberItem.getMember(), region, memberItem);

        Intent intent = new Intent(this, MemberDetailsActivity.class);
        intent.putExtra("user", loggedUser);
        intent.putExtra("member", memberItem.getMember());
        intent.putExtra("member_studycode", memberItem.getStudyCode());
        intent.putExtra("request_code", RC_MEMBER_DETAILS_TRACKINGLIST);
        intent.putExtra("dataloaders", dataLoaders);

        startActivityForResult(intent, RC_MEMBER_DETAILS_TRACKINGLIST);
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

        Database database = new Database(this);
        database.open();
        Region region = Queries.getRegionBy(database, DatabaseHelper.Region.COLUMN_CODE +"=?", new String[]{ household.getRegion() });
        database.close();

        return region;
    }

    public FormDataLoader[] getFormLoaders(TrackingMemberItem memberItem){

        Database db = new Database(this);

        db.open();
        List<Form> forms = Queries.getAllFormBy(db, null, null); //get all forms
        db.close();

        List<FormDataLoader> list = new ArrayList<>();

        int i=0;
        for (Form form : forms){
            if (memberItem.getForms().contains(form.getFormId())){ //Create formloader for only the forms that the memberItem has to collect
                FormDataLoader loader = new FormDataLoader(form);
                list.add(loader);
            }
        }

        FormDataLoader[] aList = new FormDataLoader[list.size()];

        return list.toArray(aList);
    }

    private void loadFormValues(FormDataLoader loader, Household household, Member member, Region region, TrackingMemberItem memberItem){
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
        loader.loadSpecialConstantValues(household, member, loggedUser, region, memberItem);

        //Load variables on datasets
        for (DataSet dataSet : getDataSets()){
            if (loader.hasMappedDatasetVariable(dataSet)){
                //Log.d("hasMappedVariables", ""+dataSet.getName());
                loader.loadDataSetValues(dataSet, household, member, loggedUser, region);
            }
        }
    }

    private void loadFormValues(FormDataLoader[] loaders, Household household, Member member, Region region, TrackingMemberItem memberItem){
        for (FormDataLoader loader : loaders){
            loadFormValues(loader, household, member, region, memberItem);
        }
    }

    private List<DataSet> getDataSets(){
        List<DataSet> list = null;

        Database db = new Database(this);
        db.open();
        list = Queries.getAllDataSetBy(db, null, null);
        db.close();

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
    private TrackingExpandableListAdapter readTrackingMemberLists(TrackingList trackingList){
        Database db = new Database(this);


        db.open();
        List<TrackingMemberList> listTml = Queries.getAllTrackingMemberListBy(db, DatabaseHelper.TrackingMemberList.COLUMN_TRACKING_ID+"=?", new String[]{ trackingList.getId()+"" });
        List<CollectedData> listCollectedData = Queries.getAllCollectedDataBy(db, DatabaseHelper.CollectedData.COLUMN_FORM_MODULE+"=?", new String[]{ trackingList.getModule()+"" });
        db.close();

        List<String> codes = new ArrayList<>();

        for (TrackingMemberList tm : listTml){
            codes.add(tm.getMemberCode());
            Log.d("code-", ""+tm.getMemberCode());
        }

        List<Member> members = getMembers(db, codes);

        Log.d("listTml", ""+listTml.size());
        Log.d("listCollectedData", ""+listCollectedData.size());
        Log.d("members", ""+members.size());

        ArrayList<TrackingSubListItem> groupItems = new ArrayList<>();
        HashMap<TrackingSubListItem, ArrayList<TrackingMemberItem>> trackingCollection = new HashMap<>();

        //I need member, collectedData
        for (TrackingMemberList item : listTml){
            TrackingSubListItem tsi = getSubListItem(members, listCollectedData, item, trackingList);
            TrackingMemberItem tMember = getMemberItem(members, listCollectedData, item, tsi);
            ArrayList<TrackingMemberItem> listMembers = trackingCollection.get(tsi);

            if (listMembers == null){
                listMembers = new ArrayList<>();

                groupItems.add(tsi);
                listMembers.add(tMember);

                trackingCollection.put(tsi, listMembers);
            }else{
                listMembers.add(tMember);
            }
        }

        //db.close();

        TrackingExpandableListAdapter adapter = new TrackingExpandableListAdapter(this, groupItems, trackingCollection);
        return adapter;
    }

    private List<Member> getMembers(Database db, List<String> codes){
        db.open();
        List<Member> members = Queries.getAllMemberBy(db, DatabaseHelper.Member.COLUMN_CODE +" IN ("+ StringUtil.toInClause(codes) +")", null);
        db.close();

        if (members==null) return new ArrayList<>();

        return members;
    }

    private TrackingMemberItem getMemberItem(List<Member> members, List<CollectedData> collectedDataList, TrackingMemberList item, TrackingSubListItem subListItem) {
        TrackingMemberItem tMember = new TrackingMemberItem();


        Member member = null;
        List<CollectedData> listCollected = new ArrayList<>();
        String[] forms = item.getMemberForms().split(",");
        List<String> formsList = Arrays.asList(forms);

        for (Member mb : members){
            if (mb.getCode().equals(item.getMemberCode())){
                member = mb;
                break;
            }
        }


        Log.d("member", ""+member);

        for (CollectedData cd : collectedDataList){
            if (formsList.contains(cd.getFormId()) && cd.getRecordId()==member.getId()){
                listCollected.add(cd);
            }
        }

        //List<CollectedData> list = Queries.getAllCollectedDataBy(db, DatabaseHelper.CollectedData.COLUMN_RECORD_ID+"=? AND "+DatabaseHelper.CollectedData.COLUMN_FORM_ID+" IN (?)", new String[]{ member.getId()+"", StringUtil.toInClause(forms)});

        tMember.setMember(member);
        tMember.setListItem(subListItem);
        tMember.setStudyCode(item.getMemberStudyCode());
        tMember.setVisitNumber(item.getMemberVisit());
        tMember.addForms(forms);
        tMember.addCollectedData(listCollected);

        return tMember;
    }

    private TrackingSubListItem getSubListItem(List<Member> members, List<CollectedData> collectedDataList, TrackingMemberList trackingMemberList, TrackingList trackingList){
        TrackingSubListItem tsi = new TrackingSubListItem();

        tsi.setId(trackingMemberList.getListId());
        tsi.setTrackingList(trackingList);
        tsi.setTitle(trackingMemberList.getTitle());
        tsi.setForms(trackingMemberList.getForms().split(","));

        return tsi;
    }

    class TrackingMemberListSearchTask extends AsyncTask<Void, Void, TrackingExpandableListAdapter> {

        private TrackingList trackingList;

        public TrackingMemberListSearchTask(TrackingList trackingList){
            this.trackingList = trackingList;
        }

        @Override
        protected TrackingExpandableListAdapter doInBackground(Void... params) {
            return readTrackingMemberLists(trackingList);
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
}

