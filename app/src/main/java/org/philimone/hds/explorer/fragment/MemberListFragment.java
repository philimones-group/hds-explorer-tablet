package org.philimone.hds.explorer.fragment;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.MemberAdapter;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.listeners.MemberActionListener;
import org.philimone.hds.explorer.main.GpsSearchedListActivity;
import org.philimone.hds.explorer.main.maps.MapMarker;
import org.philimone.hds.explorer.main.maps.MapViewActivity;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.server.settings.generator.CodeGeneratorService;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.RecyclerListView;
import org.philimone.hds.explorer.widget.member_details.Distance;
import org.philimone.hds.explorer.widget.member_details.GpsNearBySelectorDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.StringUtil;
import mz.betainteractive.utilities.TextFilters;


/**
 * A simple {@link Fragment} subclass.
 */
public class MemberListFragment extends Fragment {

    private RecyclerListView lvMembersList;
    private LinearLayout listButtons;
    private LinearLayout memberListHouseHeader;
    private TextView mbHouseDetailsNumber;
    /*Default buttons*/
    private Button btMemListShowHousehold;
    private Button btMemListShowMap;
    private Button btMemListSearchNearby;

    private View viewListProgressBar;

    private ArrayList<String> lastSearch;

    private MemberActionListener memberActionListener;
    private Household currentHousehold;

    private boolean censusMode;

    private Box<CollectedData> boxCollectedData;
    private Box<Form> boxForms;
    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;

    private User currentUser = Bootstrap.getCurrentUser();

    private CodeGeneratorService codeGeneratorService;

    public enum Buttons {
        SHOW_HOUSEHOLD, MEMBERS_MAP, COLLECTED_DATA
    }

    public MemberListFragment() {
        // Required empty public constructor
        lastSearch = new ArrayList<>();
        codeGeneratorService = new CodeGeneratorService();
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
                btMemListShowMap.setVisibility(View.GONE);
            }
        }
    }

    public void setButtonEnabled(boolean enabled, Buttons... buttons){

        for (Buttons button : buttons){
            if (button==Buttons.SHOW_HOUSEHOLD){
                btMemListShowHousehold.setEnabled(!currentHousehold.preRegistered && enabled);
            }
            if (button==Buttons.MEMBERS_MAP){
                btMemListShowMap.setEnabled(!currentHousehold.preRegistered && enabled);
            }
        }
    }

    public void setHouseholdHeaderVisibility(boolean visibility) {
        this.memberListHouseHeader.setVisibility(visibility ? View.VISIBLE : View.GONE);
        this.viewListProgressBar.setBackground(this.getContext().getDrawable(visibility ? R.drawable.nui_members_list_panel : R.drawable.nui_list_rborder_panel));
        this.lvMembersList.setBackground(this.getContext().getDrawable(visibility ? R.drawable.nui_members_list_panel : R.drawable.nui_list_rborder_panel));
    }

    private void initialize(View view) {
        if (getActivity() instanceof MemberActionListener){
            this.memberActionListener = (MemberActionListener) getActivity();
        }

        this.lvMembersList = view.findViewById(R.id.lvMembersList);
        this.btMemListShowHousehold = (Button) view.findViewById(R.id.btMemListShowHousehold);
        this.btMemListShowMap = (Button) view.findViewById(R.id.btMemListShowMap);
        this.btMemListSearchNearby = view.findViewById(R.id.btMemListSearchNearby);
        this.listButtons = (LinearLayout) view.findViewById(R.id.viewListButtons);
        this.memberListHouseHeader = (LinearLayout) view.findViewById(R.id.memberListHouseHeader);
        this.mbHouseDetailsNumber = (TextView)  view.findViewById(R.id.mbHouseDetailsNumber);
        this.viewListProgressBar = view.findViewById(R.id.viewListProgressBar);

        this.memberListHouseHeader.setVisibility(View.GONE);
        this.viewListProgressBar.setBackground(this.getContext().getDrawable(R.drawable.nui_list_rborder_panel));
        this.lvMembersList.setBackground(this.getContext().getDrawable(R.drawable.nui_list_rborder_panel));
        this.btMemListShowHousehold.setEnabled(false);
        this.btMemListShowMap.setEnabled(false);
        this.btMemListSearchNearby.setEnabled(false);
        this.mbHouseDetailsNumber.setText("");

        this.btMemListShowHousehold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShowHouseholdClicked();
            }
        });

        this.btMemListShowMap.setOnClickListener(v -> onShowGpsMapClicked());

        this.btMemListSearchNearby.setOnClickListener(v -> {
            onSearchNearbyClicked();
        });

        this.lvMembersList.addOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long id) {
                onMemberClicked(position);
            }

            @Override
            public void onItemLongClick(View view, int position, long id) {
                onMemberLongClicked(position);
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

    private void searchNearbyHouseholds(Household household, Distance gdistance) {

        if (currentHousehold == null || currentHousehold.isGpsNull()){
            DialogFactory.createMessageInfo(this.getActivity(), R.string.map_gps_not_available_title_lbl, R.string.member_list_gps_not_available_lbl).show();
            return;
        }

        //call the main activity to open GPSList Activity
        Intent intent = new Intent(this.getContext(), GpsSearchedListActivity.class);

        intent.putExtra("main_household", household);
        intent.putExtra("distance", gdistance);

        startActivity(intent);
    }

    private void searchNearbyMembers(Member member, Distance gdistance) {

        if (member == null || member.isGpsNull()){
            DialogFactory.createMessageInfo(this.getActivity(), R.string.map_gps_not_available_title_lbl, R.string.member_list_member_gps_not_available_lbl).show();
            return;
        }

        //call the main activity to open GPSList Activity
        Household household = getHousehold(member);

        Intent intent = new Intent(this.getContext(), GpsSearchedListActivity.class);
        intent.putExtra("main_member", member);
        intent.putExtra("main_household", household);
        intent.putExtra("distance", gdistance);

        startActivity(intent);
    }

    private void buildHouseDistanceSelectorDialog() {

        if (currentHousehold == null && currentHousehold.isGpsNull()){
            DialogFactory.createMessageInfo(this.getActivity(), R.string.map_gps_not_available_title_lbl, R.string.member_list_gps_not_available_lbl).show();
            return;
        }

        GpsNearBySelectorDialog.createDialog(getParentFragmentManager(), new GpsNearBySelectorDialog.OnClickListener() {
            @Override
            public void onSelectedDistance(Distance distance) {
                searchNearbyHouseholds(currentHousehold, distance);
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
                searchNearbyMembers(member, distance);
            }

            @Override
            public void onCancelClicked() {

            }
        }).show();
    }

    public void showMemberNotFoundMessage(){
        Toast toast = Toast.makeText(getActivity(), getString(R.string.member_list_member_not_found_lbl), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }

    private void onShowGpsMapClicked() {
        if (currentHousehold != null){
            showHouseholdInMap(currentHousehold);
        }else {
            MemberAdapter adapter = (MemberAdapter) this.lvMembersList.getAdapter();
            Member selectedMember = adapter.getSelectedMember();

            if (selectedMember != null) {
                showMemberInMap(selectedMember);
            } else {
                showMembersMap();
            }
        }
    }

    private void onSearchNearbyClicked() {
        MemberAdapter adapter = getMemberAdapter();

        if (adapter != null) {
            Member selectedMember = adapter.getSelectedMember();

            if (this.currentHousehold != null && selectedMember == null) { //member not selected - its just household
                buildHouseDistanceSelectorDialog();
            } else if (selectedMember != null) {
                buildMemberDistanceSelectorDialog();
            }
        }

    }

    private void visualizeMapBox(String pageTitle, ArrayList<MapMarker> points) {
        Intent intent = new Intent(this.getContext(), MapViewActivity.class);
        intent.putExtra("pageTitle", pageTitle);
        intent.putParcelableArrayListExtra("markersList", points);
        startActivity(intent);
    }

    private void showHouseholdInMap(Household household){

        if (!household.isGpsNull()){

            ArrayList<MapMarker> points = new ArrayList<>();
            points.add(new MapMarker(household.gpsLatitude, household.gpsLongitude, household.name, household.code));

            visualizeMapBox(getString(R.string.map_households), points);
        }else{
            DialogFactory.createMessageInfo(this.getActivity(), R.string.map_gps_not_available_title_lbl, R.string.member_list_gps_not_available_lbl).show();
        }
    }

    private void showMemberInMap(Member member){

        if (!member.isGpsNull()){

            ArrayList<MapMarker> points = new ArrayList<>();
            points.add(new MapMarker(member.gpsLatitude, member.gpsLongitude, member.name, member.code));

            visualizeMapBox(getString(R.string.map_members_coordinates), points);
        }else{
            DialogFactory.createMessageInfo(this.getActivity(), R.string.map_gps_not_available_title_lbl, R.string.member_list_member_gps_not_available_lbl).show();
        }
    }

    private void showMembersMap() {
        MemberAdapter adapter = (MemberAdapter) this.lvMembersList.getAdapter();

        if (adapter==null || adapter.isEmpty()){
            DialogFactory.createMessageInfo(this.getActivity(), R.string.map_gps_not_available_title_lbl, R.string.member_list_no_members_lbl).show();
            return;
        }

        ArrayList<MapMarker> points = new ArrayList<>();
        //organize by households and calculate new coordinates
        Map<String, List<MapMarker>> gpsMapHouseMembers = new HashMap<>();
        boolean hasAnyCoords = false;
        int totalMembers = adapter.getMembers().size();
        
        for (int i=0; i < totalMembers; i++){
            Member m = adapter.getMembers().get(i);
            String name = m.getName();
            
            if (!m.isGpsNull()) {
                double lat = m.getGpsLatitude();
                double lon = m.getGpsLongitude();
                MapMarker marker = new MapMarker(lat, lon, m.getName(), m.getCode());
                points.add(marker);

                //put point on a java map organized by houseNumber
                if (gpsMapHouseMembers.containsKey(m.getHouseholdName())){
                    List<MapMarker> list = gpsMapHouseMembers.get(m.getHouseholdName());
                    list.add(marker);
                }else{
                    List<MapMarker> list = new ArrayList<MapMarker>();
                    list.add(marker);
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

        visualizeMapBox(getString(R.string.map_members_coordinates), points);
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

    private void onMemberLongClicked(int position) {
        MemberAdapter adapter = (MemberAdapter) this.lvMembersList.getAdapter();

        if (adapter != null) {
            adapter.setSelectedIndex(position);
        }

        this.btMemListShowHousehold.setEnabled(true);
        this.btMemListSearchNearby.setEnabled(true);
    }

    private void onMemberClicked(int position) {
        this.btMemListShowHousehold.setEnabled(this.currentHousehold!=null || false);
        this.btMemListSearchNearby.setEnabled(this.currentHousehold!=null || false);

        MemberAdapter adapter = (MemberAdapter) this.lvMembersList.getAdapter();
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

        Region region = this.boxRegions.query().equal(Region_.code, household.getRegion(), QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

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
        MemberAdapter adapter = (MemberAdapter) this.lvMembersList.getAdapter();
        Member member = adapter.getSelectedMember();
        return member;
    }

    public MemberAdapter loadMembersByFilters(Household household, String name, String code, String householdCode, String gender, Integer minAge, Integer maxAge, Boolean isDead, Boolean hasOutmigrated, Boolean liveResident) {
        //open loader
        this.currentHousehold = household;

        List<String> endTypes = new ArrayList<>();

        if (name == null) name = "";
        if (code == null) code = "";
        if (householdCode == null) householdCode = "";
        if (gender == null) gender = "";

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
                    builder.startsWith(Member_.name, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case ENDSWITH:
                    builder.endsWith(Member_.name, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case CONTAINS:
                    builder.contains(Member_.name, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case MULTIPLE_CONTAINS:
                    for (String t : filter.getFilterTexts()) {
                        builder.contains(Member_.name, t, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    }
                    break;
                case NONE:
                    builder.equal(Member_.name, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case EMPTY:
                    break;
            }
            //whereClause = DatabaseHelper.Member.COLUMN_NAME + " like ?";
        }
        if (!code.isEmpty()){

            TextFilters filter = new TextFilters(code);
            String text = filter.getFilterText();

            if (codeGeneratorService.isMemberCodeValid(text)) {
                filter.setFilterType(TextFilters.Filter.NONE);
            }

            switch (filter.getFilterType()) {
                case STARTSWITH:
                    builder.startsWith(Member_.code, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case ENDSWITH:
                    builder.endsWith(Member_.code, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case CONTAINS:
                    builder.contains(Member_.code, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case MULTIPLE_CONTAINS:
                    for (String t : filter.getFilterTexts()) {
                        builder.contains(Member_.code, t, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    }
                    break;
                case NONE:
                    builder.equal(Member_.code, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case EMPTY:
                    break;
            }
            //whereClause += DatabaseHelper.Member.COLUMN_CODE + " like ?";
        }
        if (!householdCode.isEmpty()){
            TextFilters filter = new TextFilters(householdCode);
            String text = filter.getFilterText();
            boolean isCodeExclusive = false;

            if (codeGeneratorService.isHouseholdCodeValid(text)) {
                filter.setFilterType(TextFilters.Filter.NONE);
                isCodeExclusive = true;
            }

            switch (filter.getFilterType()) {
                case STARTSWITH:
                    builder.startsWith(Member_.householdCode, text, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                           .or().startsWith(Member_.householdName, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case ENDSWITH:
                    builder.endsWith(Member_.householdCode, text, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                           .or().endsWith(Member_.householdName, text, QueryBuilder.StringOrder.CASE_INSENSITIVE)  ;
                    break;
                case CONTAINS:
                    builder.contains(Member_.householdCode, text, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                           .or().contains(Member_.householdName, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    break;
                case MULTIPLE_CONTAINS:
                    for (String t : filter.getFilterTexts()) {
                        builder.contains(Member_.householdCode, t, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                               .or().contains(Member_.householdName, t, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    }
                    break;
                case NONE:

                    if (isCodeExclusive) {
                        builder.equal(Member_.householdCode, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    } else {
                        builder.equal(Member_.householdCode, text, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                                .or().equal(Member_.householdName, text, QueryBuilder.StringOrder.CASE_INSENSITIVE);
                    }
                    break;
                case EMPTY:
                    break;
            }
            //whereClause += DatabaseHelper.Member.COLUMN_HOUSEHOLD_CODE + " like ?";

        } else if (household != null) {
            builder.equal(Member_.householdCode, household.code, QueryBuilder.StringOrder.CASE_SENSITIVE);
        }
        if (!gender.isEmpty()){
            builder.equal(Member_.gender, gender, QueryBuilder.StringOrder.CASE_SENSITIVE);
            //whereClause += DatabaseHelper.Member.COLUMN_GENDER + " = ?";
        }

        //filter age
        if (minAge != null) builder.greaterOrEqual(Member_.age, minAge);
        if (maxAge != null) builder.lessOrEqual(Member_.age, maxAge);

        //At at death
        //if (minAge != null) builder.greaterOrEqual(Member_.ageAtDeath, minAge);
        //if (maxAge != null) builder.lessOrEqual(Member_.ageAtDeath, maxAge);

        if (isDead != null && isDead) endTypes.add(ResidencyEndType.DEATH.code);
        if (hasOutmigrated != null && hasOutmigrated) endTypes.add(ResidencyEndType.EXTERNAL_OUTMIGRATION.code);
        if (liveResident != null && liveResident) endTypes.add(ResidencyEndType.NOT_APPLICABLE.code);

        if (endTypes.size() > 0){
            builder.in(Member_.endType, endTypes.toArray(new String[0]), QueryBuilder.StringOrder.CASE_SENSITIVE);
        }

        //only one module is selected on login
        String module = currentUser.getSelectedModules().stream().findFirst().get();
        if (module != null) {
            builder.contains(Member_.modules, module, QueryBuilder.StringOrder.CASE_SENSITIVE);
        }

        //Limit for now to 20000 - in the next version implement live data - implement the filter at that time
        List<Member> members = builder.build().find(0, 20000);

        MemberAdapter currentAdapter = new MemberAdapter(this.getActivity(), members);
        currentAdapter.setShowHouseholdHeadIcon(true);
        
        return currentAdapter;

    }

    public void showProgress(final boolean show) {
        viewListProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        lvMembersList.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public void setMemberAdapter(MemberAdapter memberAdapter) {
        this.lvMembersList.setAdapter(memberAdapter);
        //if is empty
        boolean value =  (memberAdapter == null || memberAdapter.isEmpty());

        //disable buttons
        this.btMemListShowMap.setEnabled(!value);
        this.btMemListSearchNearby.setEnabled(false);
        this.btMemListShowHousehold.setEnabled(false);

        if (currentHousehold != null){
            this.mbHouseDetailsNumber.setText(currentHousehold.getCode());
            this.btMemListShowMap.setEnabled(true);
            this.btMemListShowHousehold.setEnabled(true);
            this.btMemListSearchNearby.setEnabled(true);
        }

    }

    public MemberAdapter getMemberAdapter(){
        if (lvMembersList.getAdapter() instanceof MemberAdapter){
            return (MemberAdapter) lvMembersList.getAdapter();
        }

        return null;
    }
}
