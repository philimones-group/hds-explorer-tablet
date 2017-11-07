package net.manhica.dss.explorer.main;

import android.app.Activity;
import android.content.Intent;
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
import net.manhica.dss.explorer.model.Form;
import net.manhica.dss.explorer.model.Household;
import net.manhica.dss.explorer.model.Member;
import net.manhica.dss.explorer.model.User;
import net.manhica.dss.explorer.model.followup.TrackingList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import mz.betainteractive.utilities.StringUtil;

public class TrackingListDetailsActivity extends Activity {

    private TextView txtTrackListTitleLabel;
    private TextView txtTrackListExtras;
    private ExpandableListView elvTrackingListDetails;

    private TrackingExpandableListAdapter adapter;
    private TrackingList trackingList;
    private User loggedUser;

    private View viewLoadingList;

    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_list_details);

        initialize();
    }

    private void initialize() {
        this.database = new Database(this);
        this.loggedUser = (User) getIntent().getExtras().get("user");
        this.trackingList = (TrackingList) getIntent().getExtras().get("trackinglist");

        ArrayList<TrackingSubListItem> groups = (ArrayList<TrackingSubListItem>) getIntent().getExtras().get("adapter_groups");
        HashMap<TrackingSubListItem, ArrayList<TrackingMemberItem>> map = (HashMap<TrackingSubListItem, ArrayList<TrackingMemberItem>>) getIntent().getExtras().get("adapter_map");
        this.adapter = new TrackingExpandableListAdapter(this, groups, map);

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
        expandAllGroups();
    }

    private void setDataToComponents() {
        txtTrackListTitleLabel.setText(trackingList.getTitle());
        txtTrackListExtras.setText(trackingList.getCode());
        elvTrackingListDetails.setAdapter(adapter);
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
        intent.putExtra("dataloaders", dataLoaders);

        startActivity(intent);
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
}

