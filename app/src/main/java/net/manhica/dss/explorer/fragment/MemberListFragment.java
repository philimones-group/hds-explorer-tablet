package net.manhica.dss.explorer.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.mapswithme.maps.api.MWMPoint;
import com.mapswithme.maps.api.MapsWithMeApi;

import net.manhica.dss.explorer.R;
import net.manhica.dss.explorer.adapter.MemberArrayAdapter;
import net.manhica.dss.explorer.database.Converter;
import net.manhica.dss.explorer.database.Database;
import net.manhica.dss.explorer.database.DatabaseHelper;
import net.manhica.dss.explorer.database.Queries;
import net.manhica.dss.explorer.listeners.ActionListener;
import net.manhica.dss.explorer.listeners.MemberActionListener;
import net.manhica.dss.explorer.model.Household;
import net.manhica.dss.explorer.model.Member;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class MemberListFragment extends Fragment {

    private ListView lvMembersList;
    private LinearLayout listButtons;
    private List<Button> buttons = new ArrayList<>();
    /*Default buttons*/
    private Button btMemListShowMmbMap;
    private Button btMemListShowClosestMembers;
    private Button btMemListShowClosestHouses;
    private Button btMemListNewMemberCollect;

    private View mProgressView;

    private Database database;

    private ArrayList<String> lastSearch;

    private MemberActionListener memberActionListener;
    private Household currentHousehold;

    private AlertDialog dialogNewMember;

    public enum Buttons {
        MEMBERS_MAP, CLOSEST_MEMBERS, CLOSEST_HOUSES, NEW_MEMBER_COLLECT
    }

    public MemberListFragment() {
        // Required empty public constructor
        lastSearch = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.member_list, container, false);

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

    public void setButtonVisibilityGone(Buttons... buttons){

        for (Buttons button : buttons){
            if (button==Buttons.MEMBERS_MAP){
                btMemListShowMmbMap.setVisibility(View.GONE);
            }
            if (button==Buttons.CLOSEST_HOUSES){
                btMemListShowClosestHouses.setVisibility(View.GONE);
            }
            if (button==Buttons.CLOSEST_MEMBERS){
                btMemListShowClosestMembers.setVisibility(View.GONE);
            }
            if (button==Buttons.NEW_MEMBER_COLLECT){
                btMemListNewMemberCollect.setVisibility(View.GONE);
            }
        }
    }

    public void setButtonEnabled(boolean enabled, Buttons... buttons){

        for (Buttons button : buttons){
            if (button==Buttons.MEMBERS_MAP){
                btMemListShowMmbMap.setEnabled(enabled);
            }
            if (button==Buttons.CLOSEST_HOUSES){
                btMemListShowClosestHouses.setEnabled(enabled);
            }
            if (button==Buttons.CLOSEST_MEMBERS){
                btMemListShowClosestMembers.setEnabled(enabled);
            }
            if (button==Buttons.NEW_MEMBER_COLLECT){
                btMemListNewMemberCollect.setEnabled(enabled);
            }
        }
    }

    public Button addButton(String buttonName, final ActionListener action){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.height = 80;

        Button button = new Button(this.getActivity());
        button.setText(buttonName);
        button.setLayoutParams(params);
        button.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_normal));

        //buttons.add(button);
        listButtons.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.execute();
            }
        });

        return button;
    }

    private void restoreLastSearch(Bundle savedInstanceState) {
        ArrayList<String> list = savedInstanceState.getStringArrayList("adapter");
        if (list != null && list.size()>0){
            Household household = getHousehold(list.get(0));
            String name = list.get(1);
            String peid = list.get(2);
            String hsnr = list.get(3);
            String gndr = list.get(4);
            Integer min = Integer.getInteger(list.get(5));
            Integer max = Integer.getInteger(list.get(6));
            Boolean filter1 = list.get(7).equals("true"); //dth
            Boolean filter2 = list.get(8).equals("true"); //ext
            Boolean filter3 = list.get(9).equals("true"); //na

            /*
            this.lastSearch.add(household);
            this.lastSearch.add(name);
            this.lastSearch.add(permId);
            this.lastSearch.add(houseNumber);
            this.lastSearch.add(gender);
            this.lastSearch.add(minAge==null ? "" : minAge.toString());
            this.lastSearch.add(maxAge==null ? "" : maxAge.toString());
            this.lastSearch.add(isDead==null ? "" : isDead+"");
            this.lastSearch.add(hasOutmigrated==null ? "" : hasOutmigrated+"");
            this.lastSearch.add(liveResident==null ? "" : liveResident+"");
             */

            Log.d("restoring",""+name);
            MemberArrayAdapter ma = loadMembersByFilters(household, name, peid, hsnr, gndr, min, max, filter1, filter2, filter3);
            setMemberAdapter(ma);
        }
    }

    private void initialize(View view) {
        if (getActivity() instanceof MemberActionListener){
            this.memberActionListener = (MemberActionListener) getActivity();
        }

        this.lvMembersList = (ListView) view.findViewById(R.id.lvMembersList);
        this.btMemListShowMmbMap = (Button) view.findViewById(R.id.btMemListShowMmbMap);
        this.btMemListShowClosestHouses = (Button) view.findViewById(R.id.btMemListShowClosestHouses);
        this.btMemListShowClosestMembers = (Button) view.findViewById(R.id.btMemListShowClosestMembers);
        this.btMemListNewMemberCollect = (Button) view.findViewById(R.id.btMemListNewMemberCollect);
        this.listButtons = (LinearLayout) view.findViewById(R.id.viewListButtons);
        this.mProgressView = view.findViewById(R.id.viewListProgressBar);

        this.btMemListShowMmbMap.setEnabled(false);
        this.btMemListShowClosestHouses.setEnabled(false);
        this.btMemListShowClosestMembers.setEnabled(false);
        this.btMemListNewMemberCollect.setEnabled(true);

        this.btMemListShowMmbMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGpsMap();
            }
        });

        this.btMemListShowClosestHouses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildHouseDistanceSelectorDialog();
            }
        });

        this.btMemListShowClosestMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildMemberDistanceSelectorDialog();
            }
        });

        this.btMemListNewMemberCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildNewMemberDialog();
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

        this.database = new Database(getActivity());
    }

    private void showClosestHouses(Household household, double distance, String distanceDescription) {

        if (currentHousehold == null || currentHousehold.isGpsNull()){
            buildOkDialog(getString(R.string.map_gps_not_available_title_lbl), getString(R.string.member_list_gps_not_available_lbl));
            return;
        }

        double cur_cos_lat = household.getCosLatitude();
        double cur_sin_lat = household.getSinLatitude();
        double cur_cos_lng = household.getCosLongitude();
        double cur_sin_lng = household.getSinLongitude();
        double cur_allowed_distance = Math.cos(distance / 6371); //# This is  200meters

        //SELECT * FROM position WHERE CUR_sin_lat * sin_lat + CUR_cos_lat * cos_lat * (cos_lng* CUR_cos_lng + sin_lng * CUR_sin_lng) > cos_allowed_distance;

        String sql = "SELECT * FROM " + DatabaseHelper.Household.TABLE_NAME + " ";
        String where = "WHERE ((" + cur_sin_lat + " * sinLatitude) + (" + cur_cos_lat + " * cosLatitude) * (cosLongitude * " + cur_cos_lng + " + sinLongitude*" + cur_sin_lng + ")) > " + cur_allowed_distance;


        List<Household> households = new ArrayList<>();

        database.open();
        Cursor cursor = database.rawQuery(sql + where, new String[]{});
        while (cursor.moveToNext()) {
            households.add(Converter.cursorToHousehold(cursor));
        }
        database.close();

        if (households.size() == 0){
            buildOkDialog(getString(R.string.map_no_closest_houses_found_lbl).replace("###", distanceDescription));
            return;
        }

        final MWMPoint[] points = new MWMPoint[households.size()];
        boolean hasAnyCoords = false;
        int i = 0;

        for (Household h : households) {
            if (!h.isGpsNull()) {
                points[i++] = new MWMPoint(h.getGpsLatitude(), h.getGpsLongitude(), h.getHouseNumber());
                hasAnyCoords = true;
            }
        }

        if (!hasAnyCoords){
            buildOkDialog(getString(R.string.map_gps_not_available_title_lbl), getString(R.string.household_filter_gps_not_available_lbl));
            return;
        }

        MapsWithMeApi.showPointsOnMap(this.getActivity(), getString(R.string.map_closest_houses_from_lbl) + " " + household.getHouseNumber(), points);

    }

    private void showClosestMembers(Member member, double distance, String distanceDescription) {

        if (member == null || member.isGpsNull()){
            buildOkDialog(getString(R.string.map_gps_not_available_title_lbl), getString(R.string.member_list_member_gps_not_available_lbl));
            return;
        }

        double cur_cos_lat = member.getCosLatitude();
        double cur_sin_lat = member.getSinLatitude();
        double cur_cos_lng = member.getCosLongitude();
        double cur_sin_lng = member.getSinLongitude();
        double cur_allowed_distance = Math.cos(distance / 6371); //# This is  200meters

        //SELECT * FROM position WHERE CUR_sin_lat * sin_lat + CUR_cos_lat * cos_lat * (cos_lng* CUR_cos_lng + sin_lng * CUR_sin_lng) > cos_allowed_distance;

        String sql = "SELECT * FROM " + DatabaseHelper.Member.TABLE_NAME + " ";
        String where = "WHERE ((" + cur_sin_lat + " * sinLatitude) + (" + cur_cos_lat + " * cosLatitude) * (cosLongitude * " + cur_cos_lng + " + sinLongitude*" + cur_sin_lng + ")) >= " + cur_allowed_distance;


        List<Member> members = new ArrayList<>();

        database.open();
        Cursor cursor = database.rawQuery(sql + where, new String[]{});
        while (cursor.moveToNext()) {
            members.add(Converter.cursorToMember(cursor));
        }
        database.close();

        if (members.size() == 0){
            buildOkDialog(getString(R.string.map_no_closest_members_found_lbl).replace("###", distanceDescription));
            return;
        }

        members.add(member); //add the selected member to the map

        final MWMPoint[] points = new MWMPoint[members.size()+1];
        Map<String, List<MWMPoint>> gpsMapHouseMembers = new HashMap<>();
        boolean hasAnyCoords = false;
        int i = 0;

        for (Member m : members) {
            if (!m.isGpsNull()) {
                points[i] = new MWMPoint(m.getGpsLatitude(), m.getGpsLongitude(), m.getName());

                //put point on a java map organized by houseNumber
                if (gpsMapHouseMembers.containsKey(m.getHouseNumber())){
                    List<MWMPoint> list = gpsMapHouseMembers.get(m.getHouseNumber());
                    list.add(points[i]);
                }else{
                    List<MWMPoint> list = new ArrayList<MWMPoint>();
                    list.add(points[i]);
                    gpsMapHouseMembers.put(m.getHouseNumber(), list);
                }

                hasAnyCoords = true;
                i++;
            }
        }

        if (!hasAnyCoords){
            buildOkDialog(getString(R.string.map_gps_not_available_title_lbl), getString(R.string.household_filter_gps_not_available_lbl));
            return;
        }

        //organizeHouseMembersCoordinates(gpsMapHouseMembers);

        MapsWithMeApi.showPointsOnMap(this.getActivity(), getString(R.string.map_closest_members_from_lbl) + " " + member.getPermId(), points);

    }

    private void buildHouseDistanceSelectorDialog() {

        if (currentHousehold == null && currentHousehold.isGpsNull()){
            buildOkDialog(getString(R.string.map_gps_not_available_title_lbl), getString(R.string.member_list_gps_not_available_lbl));
            return;
        }

        final String[] items = new String[]{ "100m", "200m", "500m", "1 Km", "2 Km", "5 Km"};
        final double[] values = new double[]{ 0.1,    0.2,    0.5,    1.0,    2.0,    5.0};
        final ArrayAdapter adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, items);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle(getString(R.string.member_list_select_distance_radius_lbl));
        builder.setCancelable(true);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showClosestHouses(currentHousehold, values[which], items[which]);
            }
        });

        builder.show();
    }

    private void buildMemberDistanceSelectorDialog() {
        final Member member = getMemberAdapter().getSelectedMember();

        if (member == null && member.isGpsNull()){
            buildOkDialog(getString(R.string.member_list_gps_not_available_lbl));
            return;
        }

        final String[] items = new String[]{ "100m", "200m", "500m", "1 Km", "2 Km", "5 Km"};
        final double[] values = new double[]{ 0.1,    0.2,    0.5,    1.0,    2.0,    5.0};
        final ArrayAdapter adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, items);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle(getString(R.string.member_list_select_distance_radius_lbl));
        builder.setCancelable(true);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showClosestMembers(member, values[which], items[which]);
            }
        });

        builder.show();
    }

    private void buildNewMemberDialog(){

        if (dialogNewMember == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            builder.setTitle(getString(R.string.member_list_newmem_title_lbl));
            builder.setView(inflater.inflate(R.layout.new_member, null));
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.member_list_newmem_collect_lbl, null);
            builder.setNegativeButton(R.string.bt_cancel_lbl, null);
            dialogNewMember = builder.create();

            dialogNewMember.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button b = dialogNewMember.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onAddNewMemberCollect();
                        }
                    });
                }
            });


        }

        //dialogNewMember.setCancelable(false);
        dialogNewMember.show();
    }

    private void onAddNewMemberCollect() {
        if (dialogNewMember == null) return;

        EditText txtNmHouseNumber = (EditText) dialogNewMember.findViewById(R.id.txtNmHouseNumber);
        EditText txtNmPermId = (EditText) dialogNewMember.findViewById(R.id.txtNmPermId);
        EditText txtNmName = (EditText) dialogNewMember.findViewById(R.id.txtNmName);
        DatePicker dtpNmDob = (DatePicker) dialogNewMember.findViewById(R.id.dtpNmDob);

        Member member = Member.getEmptyMember();
        member.setHouseExtId(txtNmHouseNumber.getText().toString());
        member.setHouseNumber(txtNmHouseNumber.getText().toString());
        member.setExtId(txtNmPermId.getText().toString());
        member.setPermId(txtNmPermId.getText().toString());
        member.setName(txtNmName.getText().toString());
        member.setDob(StringUtil.format(GeneralUtil.getDate(dtpNmDob), "yyyy-MM-dd" ));
        member.setAge(GeneralUtil.getAge(GeneralUtil.getDate(dtpNmDob)));

        //
        if (!member.getHouseNumber().matches("[0-9]{4}-[0-9]{3}")){
            buildOkDialog(getString(R.string.member_list_newmem_houseno_err_lbl));
            dialogNewMember.show();
            return;
        }
        if (!member.getPermId().matches("[0-9]{4}-[0-9]{3}-[0-9]{2}")){
            buildOkDialog(getString(R.string.member_list_newmem_permid_err_lbl));
            dialogNewMember.show();
            return;
        }
        if (member.getName().trim().isEmpty()){
            buildOkDialog(getString(R.string.member_list_newmem_name_err_lbl));
            dialogNewMember.show();
            return;
        }
        if (member.getDobDate().after(new Date())){
            buildOkDialog(getString(R.string.member_list_newmem_dob_err_lbl));
            dialogNewMember.show();
            return;
        }

        buildOkDialog("data: "+ GeneralUtil.getDate(dtpNmDob));
        memberActionListener.onMemberSelected(null, member);
    }

    private void buildOkDialog(String message){
        buildOkDialog(null, message);
    }

    private void buildOkDialog(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        title = (title==null || title.isEmpty()) ? getString(R.string.info_lbl) : title;

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", null);
        builder.show();
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
            points[0] = new MWMPoint(household.getGpsLatitude(), household.getGpsLongitude(), household.getHouseNumber());

            MapsWithMeApi.showPointsOnMap(this.getActivity(), getString(R.string.map_households), points);
        }else{
            buildOkDialog(getString(R.string.map_gps_not_available_title_lbl), getString(R.string.member_list_gps_not_available_lbl));
        }
    }

    private void showMembersMap() {
        MemberArrayAdapter adapter = (MemberArrayAdapter) this.lvMembersList.getAdapter();

        if (adapter==null || adapter.isEmpty()){
            buildOkDialog(getString(R.string.map_gps_not_available_title_lbl), getString(R.string.member_list_no_members_lbl));
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
                if (gpsMapHouseMembers.containsKey(m.getHouseNumber())){
                    List<MWMPoint> list = gpsMapHouseMembers.get(m.getHouseNumber());
                    list.add(points[i]);
                }else{
                    List<MWMPoint> list = new ArrayList<MWMPoint>();
                    list.add(points[i]);
                    gpsMapHouseMembers.put(m.getHouseNumber(), list);
                }

                hasAnyCoords = true;
            }
        }

        if (!hasAnyCoords){
            buildOkDialog(getString(R.string.map_gps_not_available_title_lbl), getString(R.string.household_filter_gps_not_available_lbl));
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

    private void onMemberLongClicked(int position) {
        MemberArrayAdapter adapter = (MemberArrayAdapter) this.lvMembersList.getAdapter();

        adapter.setSelectedIndex(position);
        this.btMemListShowClosestMembers.setEnabled(true);
        this.btMemListNewMemberCollect.setEnabled(false);
    }

    private void onMemberClicked(int position) {
        MemberArrayAdapter adapter = (MemberArrayAdapter) this.lvMembersList.getAdapter();
        Member member = adapter.getItem(position);
        Household household = getHousehold(member);

        if (memberActionListener != null){
            adapter.setSelectedIndex(-1);
            this.btMemListShowClosestMembers.setEnabled(false);

            memberActionListener.onMemberSelected(household, member);
        }
    }

    private Household getHousehold(Member member){
        if (member == null || member.getHouseNumber()==null) return null;

        database.open();
        Household household = Queries.getHouseholdBy(database, DatabaseHelper.Household.COLUMN_HOUSE_NUMBER+"=?", new String[]{ member.getHouseNumber() });
        database.close();

        return household;
    }

    private Household getHousehold(int id){
        return getHousehold(id+"");
    }

    private Household getHousehold(String id){
        if (Integer.getInteger(id) == null) return null;

        database.open();
        Household household = Queries.getHouseholdBy(database, DatabaseHelper.Household._ID+"=?", new String[]{ id });
        database.close();

        return household;
    }

    public MemberArrayAdapter loadMembersByFilters(Household household, String name, String permId, String houseNumber, String gender, Integer minAge, Integer maxAge, Boolean isDead, Boolean hasOutmigrated, Boolean liveResident) {
        //open loader
        this.currentHousehold = household;

        String endType = "";

        if (name == null) name = "";
        if (permId == null) permId = "";
        if (houseNumber == null) houseNumber = "";
        if (gender == null) gender = "";
        if (isDead != null && isDead) endType = "DTH";
        if (hasOutmigrated != null && hasOutmigrated) endType = "EXT";
        if (liveResident != null && liveResident) endType = "NA";

        //save last search
        this.lastSearch = new ArrayList();
        this.lastSearch.add(household!=null ? household.getId()+"" : "");
        this.lastSearch.add(name);
        this.lastSearch.add(permId);
        this.lastSearch.add(houseNumber);
        this.lastSearch.add(gender);
        this.lastSearch.add(minAge==null ? "" : minAge.toString());
        this.lastSearch.add(maxAge==null ? "" : maxAge.toString());
        this.lastSearch.add(isDead==null ? "" : isDead+"");
        this.lastSearch.add(hasOutmigrated==null ? "" : hasOutmigrated+"");
        this.lastSearch.add(liveResident==null ? "" : liveResident+"");


        //search on database
        List<Member> members = new ArrayList<>();
        List<String> whereValues = new ArrayList<>();
        String whereClause = "";

        if (!name.isEmpty()) {
            whereClause = DatabaseHelper.Member.COLUMN_NAME + " like ?";
            whereValues.add(name+"%");
        }
        if (!permId.isEmpty()){
            whereClause += (whereClause.isEmpty()? "" : " AND ");
            whereClause += DatabaseHelper.Member.COLUMN_PERM_ID + " like ?";
            whereValues.add(permId+"%");
        }
        if (!houseNumber.isEmpty()){
            whereClause += (whereClause.isEmpty()? "" : " AND ");
            whereClause += DatabaseHelper.Member.COLUMN_HOUSE_NUMBER + " like ?";
            whereValues.add(houseNumber+"%");
        }
        if (!gender.isEmpty()){
            whereClause += (whereClause.isEmpty()? "" : " AND ");
            whereClause += DatabaseHelper.Member.COLUMN_GENDER + " = ?";
            whereValues.add(gender);
        }
        if (minAge != null){
            whereClause += (whereClause.isEmpty()? "" : " AND ");
            whereClause += DatabaseHelper.Member.COLUMN_AGE + " >= ?";
            whereValues.add(minAge.toString());
        }
        if (maxAge != null){
            whereClause += (whereClause.isEmpty()? "" : " AND ");
            whereClause += DatabaseHelper.Member.COLUMN_AGE + " <= ?";
            whereValues.add(maxAge.toString());
        }
        if (!endType.isEmpty()){
            whereClause += (whereClause.isEmpty()? "" : " AND ");
            whereClause += DatabaseHelper.Member.COLUMN_END_TYPE + " = ?";
            whereValues.add(endType);
        }


        database.open();

        String[] ar = new String[whereValues.size()];
        Cursor cursor = database.query(Member.class, DatabaseHelper.Member.ALL_COLUMNS, whereClause, whereValues.toArray(ar), null, null, DatabaseHelper.Member.COLUMN_PERM_ID);

        while (cursor.moveToNext()){
            Member member = Converter.cursorToMember(cursor);
            members.add(member);
            //Log.d("household", ""+household);
            //Log.d("head", ""+(household!=null ? household.getHeadPermId():"null"));
            if (household != null && household.getHeadPermId().equals(member.getPermId())){
                member.setHouseholdHead(true);
            }

            if (household != null && household.getSubsHeadPermId().equals(member.getPermId())){
                member.setSubsHouseholdHead(true);
            }
        }

        database.close();

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
        this.btMemListShowClosestHouses.setEnabled(!value);
        this.btMemListShowClosestMembers.setEnabled(false);

    }

    public MemberArrayAdapter getMemberAdapter(){
        if (lvMembersList.getAdapter() instanceof MemberArrayAdapter){
            return (MemberArrayAdapter) lvMembersList.getAdapter();
        }

        return null;
    }
}
