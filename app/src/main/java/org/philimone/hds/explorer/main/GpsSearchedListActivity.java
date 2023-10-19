package org.philimone.hds.explorer.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.HouseholdAdapter;
import org.philimone.hds.explorer.adapter.MemberAdapter;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.main.maps.MapMarker;
import org.philimone.hds.explorer.main.maps.MapViewActivity;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.widget.RecyclerListView;
import org.philimone.hds.explorer.widget.member_details.Distance;

import java.text.DecimalFormat;
import java.util.ArrayList;

import io.objectbox.Box;
import mz.betainteractive.utilities.math.GpsDistanceCalculator;

public class GpsSearchedListActivity extends AppCompatActivity {

    private Button btGpsListShowMap;
    private Button btGpsOrigListShowMap;
    private Button btGpsListBack;
    private RecyclerListView lvGpsSearchedList;
    private TextView txtHouseholdName;
    private TextView txtDistanceName;
    private TextView txtResults;

    private ArrayList<MapMarker> points;
    private ArrayList<MapMarker> points_bak;
    private ArrayList<Member> members;
    private ArrayList<Household> households;
    private Member mainMember;
    private Household mainHousehold;
    private Distance distance;
    private boolean isMemberMap;
    private boolean showOriginalMap;

    private Box<Household> boxHouseholds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gps_searched_list);

        initBoxes();
        initialize();
    }

    private void initBoxes() {
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
    }

    private void initialize() {

        ArrayList<MapMarker> obj_points = getIntent().getParcelableArrayListExtra("points");
        ArrayList<MapMarker> obj_points_bak = getIntent().getParcelableArrayListExtra("points_original");
        Object obj_members = getIntent().getExtras().get("members");
        Object obj_households = getIntent().getExtras().get("households");
        Object obj_main_member = getIntent().getExtras().get("main_member");
        Object obj_main_household = getIntent().getExtras().get("main_household");
        Object obj_distance = getIntent().getExtras().get("distance");

        this.points = new ArrayList<>();
        this.points_bak = new ArrayList<>();

        this.isMemberMap = obj_main_member != null;

        if (obj_points != null) {
            this.points.addAll(obj_points);
        }
        if (obj_points_bak != null) {
            this.points_bak.addAll(obj_points_bak);
        }

        this.distance = (Distance) obj_distance;
        if (isMemberMap){
            this.members = (ArrayList<Member>) obj_members;
            this.mainMember = (Member) obj_main_member;
        }else{
            this.households = (ArrayList<Household>) obj_households;
            this.mainHousehold = (Household) obj_main_household;
        }

        //load components
        this.txtHouseholdName = (TextView) findViewById(R.id.txtHouseholdName);
        this.txtDistanceName = (TextView) findViewById(R.id.txtDistanceName);
        this.txtResults = (TextView) findViewById(R.id.txtResults);
        this.btGpsListShowMap = (Button) findViewById(R.id.btGpsListShowMap);
        this.btGpsOrigListShowMap = (Button) findViewById(R.id.btGpsOrigListShowMap);
        this.btGpsListBack = (Button) findViewById(R.id.btGpsListBack);
        this.lvGpsSearchedList = findViewById(R.id.lvGpsSearchedList);

        this.btGpsListBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GpsSearchedListActivity.this.onBackPressed();
            }
        });

        this.btGpsListShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOriginalMap = false;
                showMap();
            }
        });

        this.btGpsOrigListShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOriginalMap = true;
                showMap();
            }
        });

        btGpsListShowMap.setEnabled(false);
        btGpsOrigListShowMap.setEnabled(false);

        loadData();
    }

    private void onMemberClicked(int position) {
        MemberAdapter adapter = (MemberAdapter) this.lvGpsSearchedList.getAdapter();
        Member member = adapter.getItem(position);
        Household household = getHousehold(member);

        //adapter.setSelectedIndex(-1);

        Intent intent = new Intent(this, MemberDetailsActivity.class);
        intent.putExtra("member", member.id);

        startActivity(intent);
    }

    private Household getHousehold(Member member){
        if (member == null || member.getHouseholdCode()==null) return null;

        Household household = Queries.getHouseholdByCode(boxHouseholds, member.getHouseholdCode());

        return household;
    }

    private Integer getNumberOfResults(){
        return isMemberMap ? this.members.size() : this.households.size();
    }

    private void loadData() {
        if (isMemberMap){
            loadMembers();

            this.btGpsListShowMap.setEnabled(true);
            this.btGpsOrigListShowMap.setEnabled(true);
        }else {
            loadHouseholds();

            this.btGpsListShowMap.setEnabled(true);
        }

        this.txtHouseholdName.setText(mainHousehold.getCode()+" - "+mainHousehold.getName());
        this.txtDistanceName.setText(distance.getLabel());
        txtResults.setText(getString(R.string.gps_searched_list_results_value, getNumberOfResults()));
    }

    private void loadMembers() {
        //create adapter and calculate distances
        ArrayList<String> extras = new ArrayList<>(this.members.size());

        DecimalFormat df = new DecimalFormat("#0.00");

        double m1_lat = mainMember.getGpsLatitude();
        double m1_lng = mainMember.getGpsLongitude();

        for (int i=0; i < this.members.size(); i++){

            Member m = this.members.get(i);
            double m2_lat = m.getGpsLatitude();
            double m2_lng = m.getGpsLongitude();

            double distance = GpsDistanceCalculator.distance(m1_lat, m1_lng, m2_lat, m2_lng, GpsDistanceCalculator.UNIT.KM);

            String xtra = (distance<1) ? ((distance==0) ? "0.00m" : df.format(distance*100)+"m") : (df.format(distance))+"km";

            extras.add(xtra);
        }

        MemberAdapter adapter = new MemberAdapter(this, this.members, extras);
        adapter.setShowHouseholdAndCode(true);
        adapter.setSelectedIndex(0);
        setAdapter(adapter);
    }

    private void loadHouseholds() {
        //create adapter and calculate distances
        ArrayList<String> extras = new ArrayList<>(this.households.size());

        DecimalFormat df = new DecimalFormat("#0.00");

        double h1_lat = mainHousehold.getGpsLatitude();
        double h1_lng = mainHousehold.getGpsLongitude();

        for (int i=0; i < this.households.size(); i++){

            Household h = this.households.get(i);
            double h2_lat = h.getGpsLatitude();
            double h2_lng = h.getGpsLongitude();

            double distance = GpsDistanceCalculator.distance(h1_lat, h1_lng, h2_lat, h2_lng, GpsDistanceCalculator.UNIT.KM);

            String xtra = (distance<1) ?  df.format(distance*100)+"m" : (df.format(distance))+"km";

            extras.add(xtra);
        }

        HouseholdAdapter adapter = new HouseholdAdapter(this, this.households, extras);
        setAdapter(adapter);
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        this.lvGpsSearchedList.setAdapter(adapter);
        //if is empty
        boolean value =  (adapter == null || adapter.getItemCount()==0);

        //disable buttons
        this.btGpsListShowMap.setEnabled(!value);
    }

    private void showMap() {
        if (isMemberMap){
            if (showOriginalMap){
                visualizeMapBox(getString(R.string.map_closest_members_from_lbl) + " " + mainMember.getCode(), points_bak);
            }else {
                visualizeMapBox(getString(R.string.map_closest_members_from_lbl) + " " + mainMember.getCode(), points);
            }
        }else{
            visualizeMapBox(getString(R.string.map_closest_houses_from_lbl) + " " + mainHousehold.getName(), points);
        }
    }

    private void visualizeMapBox(String pageTitle, ArrayList<MapMarker> points) {
        Intent intent = new Intent(this, MapViewActivity.class);
        intent.putExtra("pageTitle", pageTitle);
        intent.putParcelableArrayListExtra("markersList", points);
        startActivity(intent);
    }
}
