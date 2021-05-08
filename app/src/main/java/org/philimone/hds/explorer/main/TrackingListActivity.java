package org.philimone.hds.explorer.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.philimone.hds.explorer.R;

import org.philimone.hds.explorer.adapter.TrackingListArrayAdapter;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.followup.TrackingList;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import mz.betainteractive.utilities.StringUtil;

public class TrackingListActivity extends Activity {

    public static final int RC_TRACKING_LIST_DETAILS = 10;

    private User loggedUser;
    private TextView txtTrackListModule;
    private EditText txtTrackListFilter;
    private ListView lvTrackingList;
    private Button btTrackListUpdate;
    private Button btTrackListBack;

    private View viewLoadingList;

    private Box<TrackingList> boxTrackingLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_list);

        initBoxes();
        initialize();

        if (savedInstanceState == null){
            return;
        }

        String track_list_filter = savedInstanceState.getString("track_list_filter");
        this.txtTrackListFilter.setText(track_list_filter);
    }

    private void initBoxes() {
        this.boxTrackingLists = ObjectBoxDatabase.get().boxFor(TrackingList.class);
    }

    private void initialize() {
        this.loggedUser = (User) getIntent().getExtras().get("user");

        this.txtTrackListModule = (TextView) findViewById(R.id.txtTrackListModule);
        this.txtTrackListFilter = (EditText) findViewById(R.id.txtTrackListFilter);
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

        this.txtTrackListModule.setText(this.loggedUser.getModules());

        showTrackingLists();
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

    private void onTrackingListClicked(int position) {

        TrackingListArrayAdapter adapter = (TrackingListArrayAdapter) this.lvTrackingList.getAdapter();
        TrackingList trackingList = adapter.getItem(position);

        Intent intent = new Intent(this, TrackingListDetailsActivity.class);
        intent.putExtra("user", loggedUser);
        intent.putExtra("trackinglist", trackingList);

        startActivityForResult(intent, RC_TRACKING_LIST_DETAILS);
    }

    private void filterSubjectsByCode(String code){
        if (code != null){

            TrackingListArrayAdapter adapter = (TrackingListArrayAdapter) this.lvTrackingList.getAdapter();
            adapter.filterSubjects(code);
            adapter.notifyDataSetChanged();
            //this.elvTrackingLists.invalidateViews();
        }
    }

    private void showTrackingLists() {
        ArrayList<TrackingList> trackingLists = getTrackingLists();
        //Log.d("tlistsssx", ""+trackingLists.size());
        TrackingListArrayAdapter adapter = new TrackingListArrayAdapter(this, trackingLists);

        this.lvTrackingList.setAdapter(adapter);

        //filter data
        filterSubjectsByCode(this.txtTrackListFilter.getText().toString());
    }

    /**
     * Get Tracking Lists of User's modules
     * @return
     */
    public ArrayList<TrackingList> getTrackingLists(){

        String[] userModules = loggedUser.getModules().split(",");

        List<TrackingList> tlists = this.boxTrackingLists.getAll(); //get all forms

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
