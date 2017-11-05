package net.manhica.dss.explorer.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import net.manhica.dss.explorer.R;
import net.manhica.dss.explorer.adapter.TrackingExpandableListAdapter;
import net.manhica.dss.explorer.adapter.model.TrackingMemberItem;
import net.manhica.dss.explorer.adapter.model.TrackingSubListItem;
import net.manhica.dss.explorer.model.User;
import net.manhica.dss.explorer.model.followup.TrackingList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class TrackingListDetailsActivity extends Activity {

    private TextView txtTrackListTitleLabel;
    private TextView txtTrackListExtras;
    private ExpandableListView elvTrackingListDetails;

    private TrackingExpandableListAdapter adapter;
    private TrackingList trackingList;
    private User loggedUser;

    private View viewLoadingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_list_details);

        initialize();
    }

    private void initialize() {
        this.loggedUser = (User) getIntent().getExtras().get("user");
        this.trackingList = (TrackingList) getIntent().getExtras().get("trackinglist");

        ArrayList<TrackingSubListItem> groups = (ArrayList<TrackingSubListItem>) getIntent().getExtras().get("adapter_groups");
        HashMap<TrackingSubListItem, ArrayList<TrackingMemberItem>> map = (HashMap<TrackingSubListItem, ArrayList<TrackingMemberItem>>) getIntent().getExtras().get("adapter_map");
        this.adapter = new TrackingExpandableListAdapter(this, groups, map);

        this.txtTrackListExtras = (TextView) findViewById(R.id.txtTrackListExtras);
        this.txtTrackListTitleLabel = (TextView) findViewById(R.id.txtTrackListTitleLabel);
        this.elvTrackingListDetails = (ExpandableListView) findViewById(R.id.elvTrackingListDetails);
        this.viewLoadingList = findViewById(R.id.viewListProgressBar);

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
}

