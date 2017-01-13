package net.manhica.dss.explorer.fragment;


import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private View mProgressView;

    private Database database;

    private ArrayList<String> lastSearch;

    private MemberActionListener memberActionListener;

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

    public void removeDefaultButtons(){
        if (btMemListShowMmbMap != null){
            btMemListShowMmbMap.setVisibility(View.GONE);
        }
        if (btMemListShowClosestMembers != null){
            btMemListShowClosestMembers.setVisibility(View.GONE);
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
        this.btMemListShowClosestMembers = (Button) view.findViewById(R.id.btMemListShowClosestMembers);
        this.listButtons = (LinearLayout) view.findViewById(R.id.viewListButtons);
        this.mProgressView = view.findViewById(R.id.viewListProgressBar);

        this.btMemListShowMmbMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("test-show-mmbmap", "not yet implemented");
                showMembersMap();
            }
        });

        this.btMemListShowClosestMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("test-show-closest", "not yet implemented");
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

    private void showMembersMap() {
        MemberArrayAdapter adapter = (MemberArrayAdapter) this.lvMembersList.getAdapter();

        final MWMPoint[] points = new MWMPoint[adapter.getMembers().size()];

        //organize by households and calculate new coordinates
        Map<String, List<MWMPoint>> gpsMapHouseMembers = new HashMap<>();

        for (int i=0; i < points.length; i++){
            Member m = adapter.getMembers().get(i);
            String name = m.getName();
            if (!m.hasNullCoordinates()) {
                double lat = Double.parseDouble(m.getGpsLatitude());
                double lon = Double.parseDouble(m.getGpsLongitude());
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
            }
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
        Member member = adapter.getItem(position);
        Household household = getHousehold(member);

        adapter.setSelectedIndex(position);
    }

    private void onMemberClicked(int position) {
        MemberArrayAdapter adapter = (MemberArrayAdapter) this.lvMembersList.getAdapter();
        Member member = adapter.getItem(position);
        Household household = getHousehold(member);

        if (memberActionListener != null){
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
            Log.d("household", ""+household);
            Log.d("head", ""+(household!=null ? household.getHeadPermId():"null"));
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
    }

    public MemberArrayAdapter getMemberAdapter(){
        if (lvMembersList.getAdapter() instanceof MemberArrayAdapter){
            return (MemberArrayAdapter) lvMembersList.getAdapter();
        }

        return null;
    }
}
