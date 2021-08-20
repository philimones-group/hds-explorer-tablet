package org.philimone.hds.explorer.fragment;


import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapswithme.maps.api.MWMPoint;
import com.mapswithme.maps.api.MapsWithMeApi;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.MemberArrayAdapter;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.listeners.ActionListener;
import org.philimone.hds.explorer.listeners.MemberActionListener;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.member_details.Distance;
import org.philimone.hds.explorer.widget.member_details.GpsNearBySelectorDialog;
import org.philimone.hds.explorer.widget.member_details.MemberFormDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.TextFilters;


/**
 * A simple {@link Fragment} subclass.
 */
public class MemberListFragment extends Fragment {

    private ListView lvMembersList;
    private LinearLayout listButtons;
    private LinearLayout memberListHouseHeader;
    private TextView mbHouseDetailsNumber;
    /*Default buttons*/
    private Button btMemListShowHousehold;
    private Button btMemListShowMmbMap;
    private Button btMemListNewTempMember;
    private Button btMemListShowCollectedData;

    private View mProgressView;

    private ArrayList<String> lastSearch;

    private MemberActionListener memberActionListener;
    private Household currentHousehold;

    private boolean censusMode;

    private Box<CollectedData> boxCollectedData;
    private Box<Form> boxForms;
    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;

    public enum Buttons {
        SHOW_HOUSEHOLD, MEMBERS_MAP, NEW_MEMBER_COLLECT, COLLECTED_DATA
    }

    public MemberListFragment() {
        // Required empty public constructor
        lastSearch = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.member_list, container, false);

        initBoxes();
        initialize(view);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList("adapter", lastSearch);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        //if (savedInstanceState != null)
        //restoreLastSearch(savedInstanceState);
    }

    private void initBoxes() {
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
    }

    public void setButtonVisibilityGone(Buttons... buttons){

        for (Buttons button : buttons){
            if (button==Buttons.SHOW_HOUSEHOLD){
                btMemListShowHousehold.setVisibility(View.GONE);
            }
            if (button==Buttons.MEMBERS_MAP){
                btMemListShowMmbMap.setVisibility(View.GONE);
            }
            if (button==Buttons.NEW_MEMBER_COLLECT){
                btMemListNewTempMember.setVisibility(View.GONE);
            }
            if (button==Buttons.COLLECTED_DATA){
                btMemListShowCollectedData.setVisibility(View.GONE);
            }
        }
    }

    public void setButtonEnabled(boolean enabled, Buttons... buttons){

        for (Buttons button : buttons){
            if (button==Buttons.SHOW_HOUSEHOLD){
                btMemListShowHousehold.setEnabled(enabled);
            }
            if (button==Buttons.MEMBERS_MAP){
                btMemListShowMmbMap.setEnabled(enabled);
            }
            if (button==Buttons.NEW_MEMBER_COLLECT){
                btMemListNewTempMember.setEnabled(enabled);
            }
            if (button==Buttons.COLLECTED_DATA){
                btMemListShowCollectedData.setEnabled(enabled);
            }
        }
    }

    public void setHouseholdHeaderVisibility(boolean visibility) {
        this.memberListHouseHeader.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    private void initialize(View view) {
        if (getActivity() instanceof MemberActionListener){
            this.memberActionListener = (MemberActionListener) getActivity();
        }

        this.lvMembersList = (ListView) view.findViewById(R.id.lvMembersList);
        this.btMemListShowHousehold = (Button) view.findViewById(R.id.btMemListShowHousehold);
        this.btMemListShowMmbMap = (Button) view.findViewById(R.id.btMemListShowMmbMap);
        this.btMemListNewTempMember = (Button) view.findViewById(R.id.btMemListNewTempMember);
        this.btMemListShowCollectedData = (Button) view.findViewById(R.id.btMemListShowCollectedData);
        this.listButtons = (LinearLayout) view.findViewById(R.id.viewListButtons);
        this.memberListHouseHeader = (LinearLayout) view.findViewById(R.id.memberListHouseHeader);
        this.mbHouseDetailsNumber = (TextView)  view.findViewById(R.id.mbHouseDetailsNumber);
        this.mProgressView = view.findViewById(R.id.viewListProgressBar);

        this.memberListHouseHeader.setVisibility(View.GONE);
        this.btMemListShowHousehold.setEnabled(false);
        this.btMemListShowMmbMap.setEnabled(false);
        this.btMemListNewTempMember.setEnabled(true);
        this.mbHouseDetailsNumber.setText("");

        this.btMemListShowHousehold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShowHouseholdClicked();
            }
        });

        this.btMemListShowMmbMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGpsMap();
            }
        });

        this.btMemListNewTempMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildNewTempMemberDialog();
            }
        });

        this.btMemListShowCollectedData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShowCollectedData();
            }
        });

        this.lvMembersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onMemberClicked(position);
            }
        });

        this.lvMembersList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                onMemberLongClicked(position);
                return true;
            }
        });

    }

    public boolean isCensusMode() {
        return censusMode;
    }

    public void setCensusMode(boolean censusMode) {
        this.censusMode = censusMode;
    }

    public void setCurrentHouseld(Household household) {
        this.currentHousehold = household;
    }

    private void showClosestHouses(Household household, Distance gdistance) {

        if (currentHousehold == null || currentHousehold.isGpsNull()){
            DialogFactory.createMessageInfo(this.getActivity(), R.string.map_gps_not_available_title_lbl, R.string.member_list_gps_not_available_lbl).show();
            return;
        }
/*
        double distance = gdistance.getValue();
        String distanceDescription = gdistance.getLabel();

        double cur_cos_lat = household.getCosLatitude();
        double cur_sin_lat = household.getSinLatitude();
        double cur_cos_lng = household.getCosLongitude();
        double cur_sin_lng = household.getSinLongitude();
        double cur_allowed_distance = Math.cos(distance / 6371); //# This is  200meters

        //SELECT * FROM position WHERE CUR_sin_lat * sin_lat + CUR_cos_lat * cos_lat * (cos_lng* CUR_cos_lng + sin_lng * CUR_sin_lng) > cos_allowed_distance;

        String sql = "SELECT * FROM " + DatabaseHelper.Household.TABLE_NAME + " ";
        String where = "WHERE ((" + cur_sin_lat + " * sinLatitude) + (" + cur_cos_lat + " * cosLatitude) * (cosLongitude * " + cur_cos_lng + " + sinLongitude*" + cur_sin_lng + ")) > " + cur_allowed_distance;

        ArrayList<Household> households = new ArrayList<>();

        Database database = new Database(this.getActivity());
        database.open();
        Cursor cursor = database.rawQuery(sql + where, new String[]{});
        while (cursor.moveToNext()) {
            households.add(Converter.cursorToHousehold(cursor));
        }
        database.close();

        if (households.size() == 0){
            DialogFactory.createMessageInfo(this.getActivity(), getString(R.string.info_lbl), getString(R.string.map_no_closest_houses_found_lbl, distanceDescription)).show();
            return;
        }

        final MWMPoint[] points = new MWMPoint[households.size()];
        boolean hasAnyCoords = false;
        int i = 0;

        for (Household h : households) {
            if (!h.isGpsNull()) {
                points[i++] = new MWMPoint(h.getGpsLatitude(), h.getGpsLongitude(), h.getName());
                hasAnyCoords = true;
            }
        }

        if (!hasAnyCoords){
            DialogFactory.createMessageInfo(this.getActivity(), R.string.map_gps_not_available_title_lbl, R.string.household_filter_gps_not_available_lbl).show();
            return;
        }

        //call the main activity to open GPSList Activity
        this.memberActionListener.onClosestHouseholdsResult(household, gdistance, points, households);*/

        Log.d("GPS", "I NEED TO FIND A WAY TO CALCULATE DISTANCE FROM GPS");

        assert 1==0;
    }

    private void showClosestMembers(Member member, Distance gdistance) {
/*
        if (member == null || member.isGpsNull()){
            DialogFactory.createMessageInfo(this.getActivity(), R.string.map_gps_not_available_title_lbl, R.string.member_list_member_gps_not_available_lbl).show();
            return;
        }

        double distance = gdistance.getValue();
        String distanceDescription = gdistance.getLabel();

        Household household = getHousehold(member);

        double cur_cos_lat = member.getCosLatitude();
        double cur_sin_lat = member.getSinLatitude();
        double cur_cos_lng = member.getCosLongitude();
        double cur_sin_lng = member.getSinLongitude();
        double cur_allowed_distance = Math.cos(distance / 6371); //# This is  200meters

        //SELECT * FROM position WHERE CUR_sin_lat * sin_lat + CUR_cos_lat * cos_lat * (cos_lng* CUR_cos_lng + sin_lng * CUR_sin_lng) > cos_allowed_distance;

        String sql = "SELECT * FROM " + DatabaseHelper.Member.TABLE_NAME + " ";
        String where = "WHERE ((" + cur_sin_lat + " * sinLatitude) + (" + cur_cos_lat + " * cosLatitude) * (cosLongitude * " + cur_cos_lng + " + sinLongitude*" + cur_sin_lng + ")) >= " + cur_allowed_distance +
                       " ORDER BY "+DatabaseHelper.Member.COLUMN_HOUSEHOLD_NAME;


        ArrayList<Member> members = new ArrayList<>();

        Database database = new Database(this.getActivity());
        database.open();
        Cursor cursor = database.rawQuery(sql + where, new String[]{});
        while (cursor.moveToNext()) {
            members.add(Converter.cursorToMember(cursor));
        }
        database.close();

        if (members.size() == 0){
            DialogFactory.createMessageInfo(this.getActivity(), getString(R.string.info_lbl), getString(R.string.map_no_closest_members_found_lbl, distanceDescription)).show();
            return;
        }

        //members.add(member); //add the selected member to the map

        final MWMPoint[] points = new MWMPoint[members.size()+1];
        final MWMPoint[] points_bak = new MWMPoint[members.size()+1];
        Map<String, List<MWMPoint>> gpsMapHouseMembers = new HashMap<>();
        boolean hasAnyCoords = false;
        int i = 0;

        putMainHouseholdOnBegining(member, members);

        for (Member m : members) {
            if (!m.isGpsNull()) {
                points[i] = new MWMPoint(m.getGpsLatitude(), m.getGpsLongitude(), m.getName());
                points_bak[i] = new MWMPoint(m.getGpsLatitude(), m.getGpsLongitude(), m.getName());

                //put point on a java map organized by houseNumber
                if (gpsMapHouseMembers.containsKey(m.getHouseholdName())){
                    List<MWMPoint> list = gpsMapHouseMembers.get(m.getHouseholdName());
                    list.add(points[i]);
                }else{
                    List<MWMPoint> list = new ArrayList<MWMPoint>();
                    list.add(points[i]);
                    gpsMapHouseMembers.put(m.getHouseholdName(), list);
                }

                hasAnyCoords = true;
                i++;
            }
        }

        if (!hasAnyCoords){
            DialogFactory.createMessageInfo(this.getActivity(), R.string.map_gps_not_available_title_lbl, R.string.household_filter_gps_not_available_lbl).show();
            return;
        }


        organizeHouseMembersCoordinates(gpsMapHouseMembers);

        //call the main activity to open GPSList Activity
        this.memberActionListener.onClosestMembersResult(member, gdistance, points, points_bak, members);
*/
    }

    private void buildHouseDistanceSelectorDialog() {

        if (currentHousehold == null && currentHousehold.isGpsNull()){
            DialogFactory.createMessageInfo(this.getActivity(), R.string.map_gps_not_available_title_lbl, R.string.member_list_gps_not_available_lbl).show();
            return;
        }

        GpsNearBySelectorDialog.createDialog(getParentFragmentManager(), new GpsNearBySelectorDialog.OnClickListener() {
            @Override
            public void onSelectedDistance(Distance distance) {
                showClosestHouses(currentHousehold, distance);
            }

            @Override
            public void onCancelClicked() {

            }
        }).show();
    }

    private void buildMemberDistanceSelectorDialog() {
        final Member member = getMemberAdapter().getSelectedMember();

        if (member == null && member.isGpsNull()){
            DialogFactory.createMessageInfo(this.getActivity(), R.string.info_lbl, R.string.member_list_gps_not_available_lbl).show();
            return;
        }

        GpsNearBySelectorDialog.createDialog(getParentFragmentManager(), new GpsNearBySelectorDialog.OnClickListener() {
            @Override
            public void onSelectedDistance(Distance distance) {
                showClosestMembers(member, distance);
            }

            @Override
            public void onCancelClicked() {

            }
        }).show();
    }

    private void buildNewTempMemberDialog(){

        MemberFormDialog.createTemporaryMemberDialog(getParentFragmentManager(), this.currentHousehold, new MemberFormDialog.Listener() {
            @Override
            public void onNewMemberCreated(Member member) {
                afterTemporaryMemberCreated(member);
            }

            @Override
            public void onCancelClicked() {

            }
        }).show();
    }

    private void afterTemporaryMemberCreated(Member member) {

        memberActionListener.onMemberSelected(null, member, null);
    }

    public void showMemberNotFoundMessage(){
        Toast toast = Toast.makeText(getActivity(), getString(R.string.member_list_member_not_found_lbl), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }

    private void showGpsMap() {
        if (currentHousehold != null){
            showHouseholdInMap(currentHousehold);
        }else {
            showMembersMap();
        }
    }

    private void showHouseholdInMap(Household household){
        final MWMPoint[] points = new MWMPoint[1];

        if (!household.isGpsNull()){
            points[0] = new MWMPoint(household.getGpsLatitude(), household.getGpsLongitude(), household.getName());

            MapsWithMeApi.showPointsOnMap(this.getActivity(), getString(R.string.map_households), points);
        }else{
            DialogFactory.createMessageInfo(this.getActivity(), R.string.map_gps_not_available_title_lbl, R.string.member_list_gps_not_available_lbl).show();
        }
    }

    private void showMembersMap() {
        MemberArrayAdapter adapter = (MemberArrayAdapter) this.lvMembersList.getAdapter();

        if (adapter==null || adapter.isEmpty()){
            DialogFactory.createMessageInfo(this.getActivity(), R.string.map_gps_not_available_title_lbl, R.string.member_list_no_members_lbl).show();
            return;
        }

        final MWMPoint[] points = new MWMPoint[adapter.getMembers().size()];
        //organize by households and calculate new coordinates
        Map<String, List<MWMPoint>> gpsMapHouseMembers = new HashMap<>();
        boolean hasAnyCoords = false;

        for (int i=0; i < points.length; i++){
            Member m = adapter.getMembers().get(i);
            String name = m.getName();
            if (!m.isGpsNull()) {
                double lat = m.getGpsLatitude();
                double lon = m.getGpsLongitude();
                points[i] = new MWMPoint(lat, lon, name);

                //put point on a java map organized by houseNumber
                if (gpsMapHouseMembers.containsKey(m.getHouseholdName())){
                    List<MWMPoint> list = gpsMapHouseMembers.get(m.getHouseholdName());
                    list.add(points[i]);
                }else{
                    List<MWMPoint> list = new ArrayList<MWMPoint>();
                    list.add(points[i]);
                    gpsMapHouseMembers.put(m.getHouseholdName(), list);
                }

                hasAnyCoords = true;
            }
        }

        if (!hasAnyCoords){
            DialogFactory.createMessageInfo(this.getActivity(), R.string.map_gps_not_available_title_lbl, R.string.household_filter_gps_not_available_lbl).show();
            return;
        }

        organizeHouseMembersCoordinates(gpsMapHouseMembers);

        MapsWithMeApi.showPointsOnMap(this.getActivity(), getString(R.string.map_members_coordinates), points);
    }

    private void organizeHouseMembersCoordinates(Map<String, List<MWMPoint>> gpsMapHouseMembers){
        final double pointRadius = 0.00008; //grads equivalent to 2 meters - 0.0001242

        for (String house : gpsMapHouseMembers.keySet()){
            List<MWMPoint> list = gpsMapHouseMembers.get(house);
            int n = list.size();
            int max_col = (int)(Math.ceil(Math.sqrt(n))) + 1;
            int max_row = (int) Math.ceil(n/(max_col*1.0));

            int r=0,c=0;
            for (MWMPoint p : list){
                p.setLat( p.getLat() + (c * pointRadius) );
                p.setLon( p.getLon() + (r * pointRadius) );

                c++;
                if (c == max_col){
                    c = 0;
                    r++;
                }
            }
        }
    }

    private void onShowCollectedData(){
        showProgress(true);
        Map<Member, Integer> mapMembers = new HashMap<>();

        List<Member> members = new ArrayList<>();
        ArrayList<String> extras = new ArrayList<>();

        //load collected data
        List<CollectedData> list = this.boxCollectedData.query().equal(CollectedData_.tableName, Member.getEmptyMember().getTableName()).build().find(); //only collected data from members
        List<Form> forms = boxForms.getAll();

        for (CollectedData cd : list){
            Member member = Queries.getMemberById(this.boxMembers, cd.getRecordId());
            if (member != null){
                Integer value = mapMembers.get(member);
                if (value==null){
                    mapMembers.put(member, 1);
                }else{
                    mapMembers.put(member, ++value);
                }
            }
        }

        String extraText = getString(R.string.member_list_item_extra_collected_lbl);

        for (Member m : mapMembers.keySet()){
            members.add(m);
            extras.add(extraText.replace("#", ""+mapMembers.get(m)));
        }

        mapMembers.clear();

        //put on list
        MemberArrayAdapter adapter = new MemberArrayAdapter(this.getActivity(), members, extras);
        adapter.setShowHouseholdAndCode(true);
        adapter.setIgnoreHeadOfHousehold(true);
        adapter.setMemberIcon(MemberArrayAdapter.MemberIcon.NORMAL_HEAD_ICON);
        this.lvMembersList.setAdapter(adapter);


        showProgress(false);
    }

    private void onMemberLongClicked(int position) {
        MemberArrayAdapter adapter = (MemberArrayAdapter) this.lvMembersList.getAdapter();

        adapter.setSelectedIndex(position);
        this.btMemListShowHousehold.setEnabled(true);
        this.btMemListNewTempMember.setEnabled(false);
    }

    private void onMemberClicked(int position) {
        MemberArrayAdapter adapter = (MemberArrayAdapter) this.lvMembersList.getAdapter();
        Member member = adapter.getItem(position);
        Household household = getHousehold(member);
        Region region = getRegion(household);

        if (memberActionListener != null){
            adapter.setSelectedIndex(-1);

            memberActionListener.onMemberSelected(household, member, region);
        }
    }

    private void onShowHouseholdClicked(){

        Member member = null;
        Household household = null;

        if (currentHousehold != null){ //is HouseholdMembers Activity based
            household = currentHousehold;
        }else {
            member = getMemberAdapter().getSelectedMember();
            household = getHousehold(member);
        }

        if (household == null){
            DialogFactory.createMessageInfo(this.getActivity(), R.string.info_lbl, R.string.member_list_household_not_found_lbl).show();
            return;
        }

        Member head = getHouseholdHead(household);
        Region region = getRegion(household);

        memberActionListener.onShowHouseholdClicked(household, head, region);
    }

    private Region getRegion(Household household){
        if (household == null || household.getRegion()==null) return null;

        Region region = this.boxRegions.query().equal(Region_.code, household.getRegion()).build().findFirst();

        return region;
    }

    private Household getHousehold(Member member){
        if (member == null || member.getHouseholdCode()==null) return null;

        Household household = Queries.getHouseholdByCode(boxHouseholds, member.getHouseholdCode());

        return household;
    }

    private Member getHouseholdHead(Household household){
        if (household == null || household.getHeadCode()==null) return null;

        Member member = Queries.getMemberByCode(this.boxMembers, household.getHeadCode());

        return member;
    }

    private Member getSelectedMember(){
        MemberArrayAdapter adapter = (MemberArrayAdapter) this.lvMembersList.getAdapter();
        Member member = adapter.getSelectedMember();
        return member;
    }

    public MemberArrayAdapter loadMembersByFilters(Household household, String name, String code, String householdCode, String gender, Integer minAge, Integer maxAge, Boolean isDead, Boolean hasOutmigrated, Boolean liveResident) {
        //open loader
        this.currentHousehold = household;

        ResidencyEndType endType = null;

        if (name == null) name = "";
        if (code == null) code = "";
        if (householdCode == null) householdCode = "";
        if (gender == null) gender = "";
        if (isDead != null && isDead) endType = ResidencyEndType.DEATH;
        if (hasOutmigrated != null && hasOutmigrated) endType = ResidencyEndType.EXTERNAL_OUTMIGRATION;
        if (liveResident != null && liveResident) endType = ResidencyEndType.NOT_APPLICABLE;

        //save last search
        this.lastSearch = new ArrayList();
        this.lastSearch.add(household!=null ? household.getId()+"" : "");
        this.lastSearch.add(name);
        this.lastSearch.add(code);
        this.lastSearch.add(householdCode);
        this.lastSearch.add(gender);
        this.lastSearch.add(minAge==null ? "" : minAge.toString());
        this.lastSearch.add(maxAge==null ? "" : maxAge.toString());
        this.lastSearch.add(isDead==null ? "" : isDead+"");
        this.lastSearch.add(hasOutmigrated==null ? "" : hasOutmigrated+"");
        this.lastSearch.add(liveResident==null ? "" : liveResident+"");


        //search on database
        QueryBuilder<Member> builder = this.boxMembers.query();

        if (!name.isEmpty()) {

            TextFilters filter = new TextFilters(name);
            String text = filter.getFilterText();
            switch (filter.getFilterType()) {
                case STARTSWITH:
                    builder.startsWith(Member_.name, text);
                    break;
                case ENDSWITH:
                    builder.endsWith(Member_.name, text);
                    break;
                case CONTAINS:
                    builder.contains(Member_.name, text);
                    break;
                case MULTIPLE_CONTAINS:
                    for (String t : filter.getFilterTexts()) {
                        builder.contains(Member_.name, t);
                    }
                    break;
                case NONE:
                    builder.equal(Member_.name, text);
                    break;
                case EMPTY:
                    break;
            }
            //whereClause = DatabaseHelper.Member.COLUMN_NAME + " like ?";
        }
        if (!code.isEmpty()){

            TextFilters filter = new TextFilters(code);
            String text = filter.getFilterText();
            switch (filter.getFilterType()) {
                case STARTSWITH:
                    builder.startsWith(Member_.code, text);
                    break;
                case ENDSWITH:
                    builder.endsWith(Member_.code, text);
                    break;
                case CONTAINS:
                    builder.contains(Member_.code, text);
                    break;
                case MULTIPLE_CONTAINS:
                    for (String t : filter.getFilterTexts()) {
                        builder.contains(Member_.code, t);
                    }
                    break;
                case NONE:
                    builder.equal(Member_.code, text);
                    break;
                case EMPTY:
                    break;
            }
            //whereClause += DatabaseHelper.Member.COLUMN_CODE + " like ?";
        }
        if (!householdCode.isEmpty()){
            TextFilters filter = new TextFilters(householdCode);
            String text = filter.getFilterText();
            switch (filter.getFilterType()) {
                case STARTSWITH:
                    builder.startsWith(Member_.householdCode, text);
                    break;
                case ENDSWITH:
                    builder.endsWith(Member_.householdCode, text);
                    break;
                case CONTAINS:
                    builder.contains(Member_.householdCode, text);
                    break;
                case MULTIPLE_CONTAINS:
                    for (String t : filter.getFilterTexts()) {
                        builder.contains(Member_.householdCode, t);
                    }
                    break;
                case NONE:
                    builder.equal(Member_.householdCode, text);
                    break;
                case EMPTY:
                    break;
            }
            //whereClause += DatabaseHelper.Member.COLUMN_HOUSEHOLD_CODE + " like ?";

        }
        if (!gender.isEmpty()){
            builder.equal(Member_.gender, gender);
            //whereClause += DatabaseHelper.Member.COLUMN_GENDER + " = ?";
        }

        if (endType != null && endType==ResidencyEndType.DEATH){
            if (minAge != null){
                builder.greaterOrEqual(Member_.ageAtDeath, minAge);
                //whereClause += DatabaseHelper.Member.COLUMN_AGE_AT_DEATH + " >= ?";
            }
            if (maxAge != null){
                builder.lessOrEqual(Member_.ageAtDeath, maxAge);
                //whereClause += DatabaseHelper.Member.COLUMN_AGE_AT_DEATH + " <= ?";
            }
        }else {
            if (minAge != null){
                builder.greaterOrEqual(Member_.age, minAge);
                //whereClause += DatabaseHelper.Member.COLUMN_AGE + " >= ?";
            }
            if (maxAge != null){
                builder.lessOrEqual(Member_.age, maxAge);
                //whereClause += DatabaseHelper.Member.COLUMN_AGE + " <= ?";
            }
        }

        if (endType != null){
            builder.equal(Member_.endType, endType.getId());
            //whereClause += DatabaseHelper.Member.COLUMN_END_TYPE + " = ?";
        }

        List<Member> members = builder.build().find();

        MemberArrayAdapter currentAdapter = new MemberArrayAdapter(this.getActivity(), members);

        return currentAdapter;

    }

    public void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        lvMembersList.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public void setMemberAdapter(MemberArrayAdapter memberAdapter) {
        this.lvMembersList.setAdapter(memberAdapter);
        //if is empty
        boolean value =  (memberAdapter == null || memberAdapter.isEmpty());

        //disable buttons
        this.btMemListShowMmbMap.setEnabled(!value);
        this.btMemListNewTempMember.setEnabled(true);

        if (currentHousehold != null){
            this.mbHouseDetailsNumber.setText(currentHousehold.getCode());
        }

    }

    public MemberArrayAdapter getMemberAdapter(){
        if (lvMembersList.getAdapter() instanceof MemberArrayAdapter){
            return (MemberArrayAdapter) lvMembersList.getAdapter();
        }

        return null;
    }
}
