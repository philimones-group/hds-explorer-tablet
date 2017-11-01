package net.manhica.dss.explorer.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.mapswithme.maps.api.MWMPoint;
import com.mapswithme.maps.api.MapsWithMeApi;

import net.manhica.dss.explorer.R;
import net.manhica.dss.explorer.adapter.HouseholdArrayAdapter;
import net.manhica.dss.explorer.adapter.MemberArrayAdapter;
import net.manhica.dss.explorer.data.FormDataLoader;
import net.manhica.dss.explorer.database.Database;
import net.manhica.dss.explorer.database.DatabaseHelper;
import net.manhica.dss.explorer.database.Queries;
import net.manhica.dss.explorer.model.Household;
import net.manhica.dss.explorer.model.Member;

import mz.betainteractive.utilities.math.GpsDistanceCalculator;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class GpsSearchedListActivity extends Activity {


    private Button btGpsListShowMap;
    private Button btGpsOrigListShowMap;
    private Button btGpsListBack;
    private ListView lvGpsSearchedList;

    private MWMPoint[] points;
    private MWMPoint[] points_bak;
    private ArrayList<Member> members;
    private ArrayList<Household> households;
    private Member mainMember;
    private Household mainHousehold;
    private boolean isMemberMap;
    private boolean showOriginalMap;

    private FormDataLoader[] formDataLoaders;

    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gps_searched_list);

        initialize();
    }

    private void initialize() {

        readFormDataLoaders();

        Object[] obj_points = (Object[]) getIntent().getExtras().get("points");
        Object[] obj_points_bak = (Object[]) getIntent().getExtras().get("points_original");
        Object obj_members = getIntent().getExtras().get("members");
        Object obj_households = getIntent().getExtras().get("households");
        Object obj_main_member = getIntent().getExtras().get("main_member");
        Object obj_main_household = getIntent().getExtras().get("main_household");

        this.isMemberMap = obj_main_member != null;

        this.points = convertToMWMPoint(obj_points);
        this.points_bak = convertToMWMPoint(obj_points_bak);


        if (isMemberMap){
            this.members = (ArrayList<Member>) obj_members;
            this.mainMember = (Member) obj_main_member;
        }else{
            this.households = (ArrayList<Household>) obj_households;
            this.mainHousehold = (Household) obj_main_household;
        }

        //load components
        this.btGpsListShowMap = (Button) findViewById(R.id.btGpsListShowMap);
        this.btGpsOrigListShowMap = (Button) findViewById(R.id.btGpsOrigListShowMap);
        this.btGpsListBack = (Button) findViewById(R.id.btGpsListBack);
        this.lvGpsSearchedList = (ListView) findViewById(R.id.lvGpsSearchedList);

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

        this.database = new Database(this);

        loadData();
    }

    private void readFormDataLoaders(){

        Object[] objs = (Object[]) getIntent().getExtras().get("dataloaders");
        this.formDataLoaders = new FormDataLoader[objs.length];

        for (int i=0; i < objs.length; i++){
            FormDataLoader formDataLoader = (FormDataLoader) objs[i];
            this.formDataLoaders[i] = formDataLoader;
        }
    }

    private void onMemberClicked(int position) {
        MemberArrayAdapter adapter = (MemberArrayAdapter) this.lvGpsSearchedList.getAdapter();
        Member member = adapter.getItem(position);
        Household household = getHousehold(member);

        //adapter.setSelectedIndex(-1);

        Intent intent = new Intent(this, MemberDetailsActivity.class);
        intent.putExtra("member", member);
        intent.putExtra("dataloaders", formDataLoaders);

        startActivity(intent);
    }

    private Household getHousehold(Member member){
        if (member == null || member.getHouseNumber()==null) return null;

        database.open();
        Household household = Queries.getHouseholdBy(database, DatabaseHelper.Household.COLUMN_HOUSE_NUMBER+"=?", new String[]{ member.getHouseNumber() });
        database.close();

        return household;
    }

    private void loadData() {
        if (isMemberMap){
            loadMembers();
        }else {
            loadHouseholds();
        }
    }

    private MWMPoint[] convertToMWMPoint(Object[] o_points){
        MWMPoint[] ps = new MWMPoint[o_points.length];
        for (int i=0; i<ps.length; i++){
            ps[i] = (MWMPoint) o_points[i];
        }
        return ps;
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

        MemberArrayAdapter adapter = new MemberArrayAdapter(this, this.members, extras);
        adapter.setShowHouseholdAndPermId(true);
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

        HouseholdArrayAdapter adapter = new HouseholdArrayAdapter(this, this.households, extras);
        setAdapter(adapter);
    }

    public void setAdapter(ArrayAdapter adapter) {
        this.lvGpsSearchedList.setAdapter(adapter);
        //if is empty
        boolean value =  (adapter == null || adapter.isEmpty());

        //disable buttons
        this.btGpsListShowMap.setEnabled(!value);
    }

    private void showMap() {
        if (isMemberMap){
            if (showOriginalMap){
                MapsWithMeApi.showPointsOnMap(this, getString(R.string.map_closest_members_from_lbl) + " " + mainMember.getPermId(), points_bak);
            }else {
                MapsWithMeApi.showPointsOnMap(this, getString(R.string.map_closest_members_from_lbl) + " " + mainMember.getPermId(), points);
            }
        }else{
            MapsWithMeApi.showPointsOnMap(this, getString(R.string.map_closest_houses_from_lbl) + " " + mainHousehold.getHouseNumber(), points);
        }
    }
}