package org.philimone.hds.explorer.main;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.RecyclerListView;
import org.philimone.hds.explorer.widget.member_details.Distance;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import mz.betainteractive.utilities.math.GpsDistanceCalculator;

public class GpsSearchedListActivity extends AppCompatActivity {

    private Button btGpsListShowMap;
    private Button btGpsOrigListShowMap;
    private Button btGpsListBack;
    private View viewListProgressBar;
    private RecyclerListView lvGpsSearchedList;
    private TextView txtHouseholdName;
    private TextView txtDistanceName;
    private TextView txtResults;

    private ArrayList<MapMarker> points = new ArrayList<>();
    private ArrayList<MapMarker> points_bak = new ArrayList<>();
    private List<Member> members;
    private List<Household> households;
    private Member mainMember;
    private Household mainHousehold;
    private Distance distance;
    private boolean isMemberMap;
    private boolean showOriginalMap;

    private boolean initiated = false;

    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gps_searched_list);

        initBoxes();
        initialize();
    }

    private void initBoxes() {
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
    }

    private void initialize() {
        readIntent();

        //load components
        this.txtHouseholdName = (TextView) findViewById(R.id.txtHouseholdName);
        this.txtDistanceName = (TextView) findViewById(R.id.txtDistanceName);
        this.txtResults = (TextView) findViewById(R.id.txtResults);
        this.btGpsListShowMap = (Button) findViewById(R.id.btGpsListShowMap);
        this.btGpsOrigListShowMap = (Button) findViewById(R.id.btGpsOrigListShowMap);
        this.btGpsListBack = (Button) findViewById(R.id.btGpsListBack);
        this.viewListProgressBar = findViewById(R.id.viewListProgressBar);
        this.lvGpsSearchedList = findViewById(R.id.lvGpsSearchedList);

        this.btGpsListBack.setOnClickListener(v -> GpsSearchedListActivity.this.onBackPressed());

        this.btGpsListShowMap.setOnClickListener(v -> {
            showOriginalMap = false;
            showMap();
        });

        this.btGpsOrigListShowMap.setOnClickListener(v -> {
            showOriginalMap = true;
            showMap();
        });

        btGpsListShowMap.setEnabled(false);
        btGpsOrigListShowMap.setEnabled(false);

        this.txtHouseholdName.setText(mainHousehold.getCode()+" - "+mainHousehold.getName());
        this.txtDistanceName.setText(distance.getLabel());
        txtResults.setText("");
    }

    private void readIntent() {
        Object obj_main_member = getIntent().getExtras().get("main_member");
        Object obj_main_household = getIntent().getExtras().get("main_household");
        Object obj_distance = getIntent().getExtras().get("distance");

        if (obj_main_household != null) {
            this.mainHousehold = (Household) obj_main_household;
        }
        if (obj_main_member != null) {
            this.mainMember = (Member) obj_main_member;
            this.isMemberMap = true;
        }

        this.distance = (Distance) obj_distance;
    }

    @Override
    protected void onResume() {
        super.onResume();


        if (this.initiated == false) {
            initSearching();
        }

        this.initiated = true;
    }

    private void initSearching() {
        new SearchingLocationsTask(mainHousehold, mainMember, distance).execute();
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

    private void loadSearchedData() {
        if (isMemberMap){
            loadMembers();

            this.btGpsListShowMap.setEnabled(true);
            this.btGpsOrigListShowMap.setEnabled(true);
        }else {
            loadHouseholds();

            this.btGpsListShowMap.setEnabled(true);
        }

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
        adapter.setShowMemberCodeWithHousehold(true);
        adapter.setShowExtraDetails(true);
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

    private void searchNearbyHouseholds(Household household, Distance gdistance) {

        if (household == null || household.isGpsNull()){
            DialogFactory.createMessageInfo(this, R.string.map_gps_not_available_title_lbl, R.string.member_list_gps_not_available_lbl).show();
            return;
        }

        final double distance = gdistance.getValue();
        String distanceDescription = gdistance.getLabel();

        final double cur_cos_lat = household.getCosLatitude();
        final double cur_sin_lat = household.getSinLatitude();
        final double cur_cos_lng = household.getCosLongitude();
        final double cur_sin_lng = household.getSinLongitude();
        final double cur_allowed_distance = Math.cos(distance / 6371); //# This is  200meters

        this.households = this.boxHouseholds.query(Household_.gpsLatitude.notNull().and(Household_.gpsLongitude.notNull()))
                .filter((h) -> !h.isGpsNull() && ((cur_sin_lat*h.sinLatitude + cur_cos_lat*h.cosLatitude * (h.cosLongitude*cur_cos_lng + h.sinLongitude*cur_sin_lng)) > cur_allowed_distance)  )
                .build().find();

        if (this.households.size() == 0){
            DialogFactory.createMessageInfo(this, getString(R.string.info_lbl), getString(R.string.map_no_closest_houses_found_lbl, distanceDescription)).show();
            return;
        }

        this.points = new ArrayList<>();
        boolean hasAnyCoords = false;
        int i = 0;

        for (Household h : this.households) {
            if (!h.isGpsNull()) {
                points.add(new MapMarker(h.gpsLatitude, h.gpsLongitude, h.name, h.code));
                hasAnyCoords = true;
            }
        }

        if (!hasAnyCoords){
            DialogFactory.createMessageInfo(this, R.string.map_gps_not_available_title_lbl, R.string.household_filter_gps_not_available_lbl).show();
            return;
        }
    }

    private void searchNearbyMembers(Member member, Distance gdistance) {

        if (member == null || member.isGpsNull()){
            DialogFactory.createMessageInfo(this, R.string.map_gps_not_available_title_lbl, R.string.member_list_member_gps_not_available_lbl).show();
            return;
        }

        double distance = gdistance.getValue();
        String distanceDescription = gdistance.getLabel();

        final double cur_cos_lat = member.getCosLatitude();
        final double cur_sin_lat = member.getSinLatitude();
        final double cur_cos_lng = member.getCosLongitude();
        final double cur_sin_lng = member.getSinLongitude();
        final double cur_allowed_distance = Math.cos(distance / 6371); //# This is  200meters

        this.members = this.boxMembers.query(Member_.gpsLatitude.notNull().and(Member_.gpsLongitude.notNull()))
                .filter((m) -> !m.isGpsNull() && ((cur_sin_lat*m.sinLatitude + cur_cos_lat*m.cosLatitude * (m.cosLongitude*cur_cos_lng + m.sinLongitude*cur_sin_lng)) > cur_allowed_distance)  )
                .build().find();

        if (this.members.size() == 0){
            DialogFactory.createMessageInfo(this, getString(R.string.info_lbl), getString(R.string.map_no_closest_members_found_lbl, distanceDescription)).show();
            return;
        }

        this.points = new ArrayList<>();
        this.points_bak = new ArrayList<>();

        Map<String, List<MapMarker>> gpsMapHouseMembers = new HashMap<>();
        boolean hasAnyCoords = false;
        int i = 0;

        putMainHouseholdOnBegining(member, this.members);

        for (Member m : this.members) {
            if (!m.isGpsNull()) {
                MapMarker marker = new MapMarker(m.gpsLatitude, m.gpsLongitude, m.name, m.code);
                MapMarker marker_bak = new MapMarker(m.gpsLatitude, m.gpsLongitude, m.name, m.code);
                points.add(marker);
                points_bak.add(marker_bak);

                //put point on a java map organized by houseNumber
                if (gpsMapHouseMembers.containsKey(m.getHouseholdName())){
                    List<MapMarker> list = gpsMapHouseMembers.get(m.getHouseholdName());
                    list.add(marker);
                }else{
                    List<MapMarker> list = new ArrayList<>();
                    list.add(marker);
                    gpsMapHouseMembers.put(m.getHouseholdName(), list);
                }

                hasAnyCoords = true;
                i++;
            }
        }

        if (!hasAnyCoords){
            DialogFactory.createMessageInfo(this, R.string.map_gps_not_available_title_lbl, R.string.household_filter_gps_not_available_lbl).show();
            return;
        }

        organizeHouseMembersCoordinates(gpsMapHouseMembers);
    }

    private void organizeHouseMembersCoordinates(Map<String, List<MapMarker>> gpsMapHouseMembers){
        final double pointRadius = 0.00008; //grads equivalent to 2 meters - 0.0001242

        for (String house : gpsMapHouseMembers.keySet()){
            List<MapMarker> list = gpsMapHouseMembers.get(house);
            int n = list.size();
            int max_col = (int)(Math.ceil(Math.sqrt(n))) + 1;
            int max_row = (int) Math.ceil(n/(max_col*1.0));

            int r=0,c=0;
            for (MapMarker p : list){
                p.setGpsLatitude( p.getGpsLatitude() + (c * pointRadius) );
                p.setGpsLongitude( p.getGpsLongitude() + (r * pointRadius) );

                c++;
                if (c == max_col){
                    c = 0;
                    r++;
                }
            }
        }
    }

    private void putMainHouseholdOnBegining(Member mainMember, List<Member> members) {
        List<Member> houseMembers = new ArrayList<>();

        for (Member m : members)
            if (m.getHouseholdName().equals(mainMember.getHouseholdName()))
                houseMembers.add(m);


        houseMembers.remove(mainMember);
        houseMembers.add(0, mainMember);

        members.removeAll(houseMembers);
        members.addAll(0, houseMembers);
    }

    class SearchingLocationsTask extends AsyncTask<Void, Void, Boolean> {
        private Household household;
        private Member member;
        private Distance distance;
        public SearchingLocationsTask(Household mainHousehold, Member mainMember, Distance distance) {
            this.household = mainHousehold;
            this.member = mainMember;
            this.distance = distance;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            //this must end up with loading households, members, points and points_bak variables
            if (this.member != null) {
                searchNearbyMembers(this.member, this.distance);
            } else {
                searchNearbyHouseholds(this.household, this.distance);
            }

            return Boolean.TRUE;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            viewListProgressBar.setVisibility(View.VISIBLE);
            lvGpsSearchedList.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            viewListProgressBar.setVisibility(View.GONE);
            lvGpsSearchedList.setVisibility(View.VISIBLE);

            loadSearchedData();
        }
    }
}
