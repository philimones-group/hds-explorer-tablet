package org.philimone.hds.explorer.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.philimone.hds.explorer.R;

import org.philimone.hds.explorer.adapter.TrackingListArrayAdapter;
import org.philimone.hds.explorer.database.Database;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.followup.TrackingList;

import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.utilities.StringUtil;

public class TrackingListActivity extends Activity {

    public static final int RC_TRACKING_LIST_DETAILS = 10;

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

        Intent intent = new Intent(this, TrackingListDetailsActivity.class);
        intent.putExtra("user", loggedUser);
        intent.putExtra("trackinglist", trackingList);

        startActivityForResult(intent, RC_TRACKING_LIST_DETAILS);
    }

    private void showTrackingLists() {
        ArrayList<TrackingList> trackingLists = getTrackingLists();
        //Log.d("tlistsssx", ""+trackingLists.size());
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
            //Log.d("tl", ""+tl.getCode()+", "+StringUtil.containsAny(userModules, modules)+", u:"+userModules[0]+", m:"+modules[0]);
            if (StringUtil.containsAny(userModules, modules)){ //if the user has access to module specified on Form
                list.add(tl);
            }
        }

        return list;
    }

    public void showProgress(final boolean show) {
        viewLoadingList.setVisibility(show ? View.VISIBLE : View.GONE);
        lvTrackingList.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        showTrackingLists();
    }
}
