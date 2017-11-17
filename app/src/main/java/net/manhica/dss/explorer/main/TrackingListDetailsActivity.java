package net.manhica.dss.explorer.main;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import net.manhica.dss.explorer.R;
import net.manhica.dss.explorer.adapter.TrackingExpandableListAdapter;
import net.manhica.dss.explorer.adapter.model.TrackingMemberItem;
import net.manhica.dss.explorer.adapter.model.TrackingSubListItem;
import net.manhica.dss.explorer.data.FormDataLoader;
import net.manhica.dss.explorer.database.Database;
import net.manhica.dss.explorer.database.DatabaseHelper;
import net.manhica.dss.explorer.database.Queries;
import net.manhica.dss.explorer.model.CollectedData;
import net.manhica.dss.explorer.model.Form;
import net.manhica.dss.explorer.model.Household;
import net.manhica.dss.explorer.model.Member;
import net.manhica.dss.explorer.model.User;
import net.manhica.dss.explorer.model.followup.TrackingList;
import net.manhica.dss.explorer.model.followup.TrackingMemberList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import mz.betainteractive.utilities.StringUtil;

public class TrackingListDetailsActivity extends Activity {

    public static final int RC_MEMBER_DETAILS_TRACKINGLIST = 20;

    private TextView txtTrackListTitleLabel;
    private TextView txtTrackListExtras;
    private ExpandableListView elvTrackingListDetails;

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

        this.txtTrackListExtras = (TextView) findViewById(R.id.txtTrackListExtras);
        this.txtTrackListTitleLabel = (TextView) findViewById(R.id.txtTrackListTitleLabel);
        this.elvTrackingListDetails = (ExpandableListView) findViewById(R.id.elvTrackingListDetails);
        this.viewLoadingList = findViewById(R.id.viewListProgressBar);

        this.elvTrackingListDetails.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                onMemberItemClicked(groupPosition, childPosition);
                return true;
            }
        });

        setDataToComponents();
    }

    private void setDataToComponents() {
        txtTrackListTitleLabel.setText(trackingList.getTitle());
        txtTrackListExtras.setText(trackingList.getCode());
    }

    private void setTrackingListAdapter(TrackingExpandableListAdapter mAdapter){
        this.adapter = mAdapter;
        elvTrackingListDetails.setAdapter(this.adapter);

        showProgress(false);
        expandAllGroups();
    }

    private void expandAllGroups(){
        if (adapter != null)
        for ( int i = 0; i < adapter.getGroupCount(); i++ ){
            elvTrackingListDetails.expandGroup(i);
        }
    }

    public void showProgress(final boolean show) {
        viewLoadingList.setVisibility(show ? View.VISIBLE : View.GONE);
        elvTrackingListDetails.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void onMemberItemClicked(int groupPosition, int childPosition) {
        TrackingMemberItem memberItem = (TrackingMemberItem) adapter.getChild(groupPosition, childPosition);

        FormDataLoader[] dataLoaders = getFormLoaders(memberItem);
        Household household = getHousehold(memberItem.getMember());
        loadFormValues(dataLoaders, household, memberItem.getMember(), memberItem);

        Intent intent = new Intent(this, MemberDetailsActivity.class);
        intent.putExtra("user", loggedUser);
        intent.putExtra("member", memberItem.getMember());
        intent.putExtra("member_studycode", memberItem.getStudyCode());
        intent.putExtra("request_code", RC_MEMBER_DETAILS_TRACKINGLIST);
        intent.putExtra("dataloaders", dataLoaders);

        startActivityForResult(intent, RC_MEMBER_DETAILS_TRACKINGLIST);
    }

    private Household getHousehold(Member member){
        if (member == null || member.getHouseNumber()==null) return null;


        database.open();
        Household household = Queries.getHouseholdBy(database, DatabaseHelper.Household.COLUMN_HOUSE_NUMBER+"=?", new String[]{ member.getHouseNumber() });
        database.close();

        return household;
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

    private void loadFormValues(FormDataLoader loader, Household household, Member member, TrackingMemberItem memberItem){
        if (household != null){
            loader.loadHouseholdValues(household);
        }
        if (member != null){
            loader.loadMemberValues(member);
        }
        if (loggedUser != null){
            loader.loadUserValues(loggedUser);
        }

        loader.loadConstantValues();
        loader.loadSpecialConstantValues(household, member, loggedUser, memberItem);
    }

    private void loadFormValues(FormDataLoader[] loaders, Household household, Member member, TrackingMemberItem memberItem){
        for (FormDataLoader loader : loaders){
            loadFormValues(loader, household, member, memberItem);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode== RC_MEMBER_DETAILS_TRACKINGLIST){
            refreshCurrentList();
        }
    }

    private void refreshCurrentList() {
        TrackingSubListItem groupItem = (TrackingSubListItem) adapter.getGroup(lastSelectedGroupPosition);
        TrackingMemberItem memberItem = (TrackingMemberItem) adapter.getChild(lastSelectedGroupPosition, lastSelectedChildPosition);
        Member member = memberItem.getMember();

        database.open();

        List<CollectedData> list = Queries.getAllCollectedDataBy(database, DatabaseHelper.CollectedData.COLUMN_RECORD_ID + "=? AND "+DatabaseHelper.CollectedData.COLUMN_FORM_ID+" IN (?)",
                                                                           new String[]{ member.getId()+"", StringUtil.toInClause(memberItem.getForms()) } );

        database.close();

        memberItem.getCollectedForms().clear();
        memberItem.addCollectedData(list);

        elvTrackingListDetails.setAdapter(adapter);
    }

    //reading tracking list
    private TrackingExpandableListAdapter readTrackingMemberLists(TrackingList trackingList){
        Database db = new Database(this);


        db.open();
        List<TrackingMemberList> listTml = Queries.getAllTrackingMemberListBy(db, DatabaseHelper.TrackingMemberList.COLUMN_TRACKING_ID+"=?", new String[]{ trackingList.getId()+"" });
        List<CollectedData> listCollectedData = Queries.getAllCollectedDataBy(db, DatabaseHelper.CollectedData.COLUMN_FORM_MODULE+"=?", new String[]{ trackingList.getModule()+"" });
        db.close();

        List<String> extIds = new ArrayList<>();

        for (TrackingMemberList tm : listTml){
            extIds.add(tm.getMemberExtId());
            Log.d("extId-", ""+tm.getMemberExtId());
        }

        List<Member> members = getMembers(db, extIds);

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

    private List<Member> getMembers(Database db, List<String> extIds){
        db.open();
        List<Member> members = Queries.getAllMemberBy(db, DatabaseHelper.Member.COLUMN_EXT_ID+" IN ("+ StringUtil.toInClause(extIds) +")", null);
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
            if (mb.getExtId().equals(item.getMemberExtId())){
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
            setTrackingListAdapter(mAdapter);
        }
    }
}

