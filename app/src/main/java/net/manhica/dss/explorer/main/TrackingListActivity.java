package net.manhica.dss.explorer.main;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import net.manhica.dss.explorer.R;
import net.manhica.dss.explorer.adapter.TrackingExpandableListAdapter;
import net.manhica.dss.explorer.adapter.TrackingListArrayAdapter;
import net.manhica.dss.explorer.adapter.model.TrackingMemberItem;
import net.manhica.dss.explorer.adapter.model.TrackingSubListItem;
import net.manhica.dss.explorer.database.Database;
import net.manhica.dss.explorer.database.DatabaseHelper;
import net.manhica.dss.explorer.database.Queries;
import net.manhica.dss.explorer.model.CollectedData;
import net.manhica.dss.explorer.model.Member;
import net.manhica.dss.explorer.model.User;
import net.manhica.dss.explorer.model.followup.TrackingList;
import net.manhica.dss.explorer.model.followup.TrackingMemberList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import mz.betainteractive.utilities.StringUtil;

public class TrackingListActivity extends Activity {

    private User loggedUser;
    private TextView txtTrackListModule;
    private ListView lvTrackingList;
    private Button btTrackListUpdate;
    private Button btTrackListBack;

    private View viewLoadingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_list);

        initialize();
    }

    private void initialize() {
        this.loggedUser = (User) getIntent().getExtras().get("user");

        this.txtTrackListModule = (TextView) findViewById(R.id.txtTrackListModule);
        this.lvTrackingList = (ListView) findViewById(R.id.lvTrackingList);
        this.btTrackListUpdate = (Button) findViewById(R.id.btTrackListUpdate);
        this.btTrackListBack = (Button) findViewById(R.id.btTrackListBack);
        this.viewLoadingList = findViewById(R.id.viewListProgressBar);

        this.btTrackListUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTrackingLists();
                showTrackingLists();
            }
        });

        this.btTrackListBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrackingListActivity.this.onBackPressed();
            }
        });

        this.lvTrackingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onTrackingListClicked(position);
            }
        });


        this.txtTrackListModule.setText(this.loggedUser.getModules());

        showTrackingLists();
    }

    private void onTrackingListClicked(int position) {

        TrackingListArrayAdapter adapter = (TrackingListArrayAdapter) this.lvTrackingList.getAdapter();
        TrackingList trackingList = adapter.getItem(position);

        showProgress(true);

        new TrackingMemberListSearchTask(trackingList).execute();
    }

    private void openTrackingListDetails(TrackingList trackingList, TrackingExpandableListAdapter adapter) {
        showProgress(false);

        Intent intent = new Intent(this, TrackingListDetailsActivity.class);
        intent.putExtra("user", loggedUser);
        intent.putExtra("trackinglist", trackingList);
        intent.putExtra("adapter_groups", adapter.getGroupItems());
        intent.putExtra("adapter_map", adapter.getTrackingCollection());
        startActivity(intent);
    }

    private void showTrackingLists() {
        ArrayList<TrackingList> trackingLists = getTrackingLists();

        TrackingListArrayAdapter adapter = new TrackingListArrayAdapter(this, trackingLists);

        this.lvTrackingList.setAdapter(adapter);
    }

    /**
     * Get Tracking Lists of User's modules
     * @return
     */
    public ArrayList<TrackingList> getTrackingLists(){

        String[] userModules = loggedUser.getModules().split(",");

        Database db = new Database(this);

        db.open();
        List<TrackingList> tlists = Queries.getAllTrackingListBy(db, null, null); //get all forms
        db.close();

        ArrayList<TrackingList> list = new ArrayList<>();

        int i=0;
        for (TrackingList tl : tlists){
            String[] modules = tl.getModule().split(",");

            if (StringUtil.containsAny(userModules, modules)){ //if the user has access to module specified on Form
                list.add(tl);
            }
        }

        return list;
    }

    private void updateTrackingLists() {
        showProgress(true);

        //TODO - get all tracking member list and their respective collectedData to calculate if we collected the forms or not

        for (TrackingList trackingList : getTrackingLists()){
            TrackingExpandableListAdapter adapter = readTrackingMemberLists(trackingList);
            int c = adapter.getCompletionOfTrackingList();

            trackingList.setCompletionRate(c/100D);

            Database db = new Database(this);
            db.open();
            db.update(TrackingList.class, trackingList.getContentValues(), DatabaseHelper.TrackingList._ID+"=?", new String[]{ trackingList.getId()+"" });
            db.close();
        }

        showProgress(false);
    }

    public void showProgress(final boolean show) {
        viewLoadingList.setVisibility(show ? View.VISIBLE : View.GONE);
        lvTrackingList.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private TrackingExpandableListAdapter readTrackingMemberLists(TrackingList trackingList){
        Database db = new Database(this);
        db.open();

        List<TrackingMemberList> list = Queries.getAllTrackingMemberListBy(db, DatabaseHelper.TrackingMemberList.COLUMN_TRACKING_ID+"=?", new String[]{ trackingList.getId()+"" });

        ArrayList<TrackingSubListItem> groupItems = new ArrayList<>();
        HashMap<TrackingSubListItem, ArrayList<TrackingMemberItem>> trackingCollection = new HashMap<>();

        //I need member, collectedData
        for (TrackingMemberList item : list){
            TrackingSubListItem tsi = getSubListItem(item, trackingList);
            TrackingMemberItem tMember = getMemberItem(db, item, tsi);
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

        db.close();

        TrackingExpandableListAdapter adapter = new TrackingExpandableListAdapter(this, groupItems, trackingCollection);
        return adapter;
    }

    private TrackingMemberItem getMemberItem(Database db, TrackingMemberList item, TrackingSubListItem subListItem) {
        TrackingMemberItem tMember = new TrackingMemberItem();


        String[] forms = item.getMemberForms().split(",");

        Member member = Queries.getMemberBy(db, DatabaseHelper.Member.COLUMN_EXT_ID+"=? AND "+DatabaseHelper.Member.COLUMN_PERM_ID+"=?", new String[]{ item.getMemberExtId(), item.getMemberPermId() });
        Log.d("member", ""+member);
        List<CollectedData> list = Queries.getAllCollectedDataBy(db, DatabaseHelper.CollectedData.COLUMN_RECORD_ID+"=? AND "+DatabaseHelper.CollectedData.COLUMN_FORM_ID+" IN (?)", new String[]{ member.getId()+"", StringUtil.toInClause(forms)});

        tMember.setMember(member);
        tMember.setListItem(subListItem);
        tMember.setStudyCode(item.getMemberStudyCode());
        tMember.addForms(forms);
        tMember.addCollectedData(list);

        return tMember;
    }

    private TrackingSubListItem getSubListItem(TrackingMemberList trackingMemberList, TrackingList trackingList){
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
        protected void onPostExecute(TrackingExpandableListAdapter adapter) {
            openTrackingListDetails(trackingList, adapter);
        }
    }
}
