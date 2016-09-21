package net.manhica.clip.explorer.fragment;


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

import net.manhica.clip.explorer.R;
import net.manhica.clip.explorer.adapter.MemberArrayAdapter;
import net.manhica.clip.explorer.database.Converter;
import net.manhica.clip.explorer.database.Database;
import net.manhica.clip.explorer.database.DatabaseHelper;
import net.manhica.clip.explorer.database.Queries;
import net.manhica.clip.explorer.listeners.ActionListener;
import net.manhica.clip.explorer.listeners.MemberActionListener;
import net.manhica.clip.explorer.model.Household;
import net.manhica.clip.explorer.model.Member;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MemberListFragment extends Fragment {

    private ListView lvMembersList;
    private LinearLayout listButtons;
    private List<Button> buttons = new ArrayList<>();
    /*Default buttons*/
    private Button btMemListShowHHMap;
    private Button btMemListShowMmbMap;
    private Button btMemListShowClosestPregnt;

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
        if (btMemListShowHHMap != null){
            btMemListShowHHMap.setVisibility(View.GONE);
        }
        if (btMemListShowMmbMap != null){
            btMemListShowMmbMap.setVisibility(View.GONE);
        }
        if (btMemListShowClosestPregnt != null){
            btMemListShowClosestPregnt.setVisibility(View.GONE);
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
            String name = list.get(0);
            String peid = list.get(1);
            String gndr = list.get(2);
            boolean filter1 = list.get(3).equals("true");
            boolean filter2 = list.get(4).equals("true");
            boolean filter3 = list.get(5).equals("true");

            Log.d("restoring",""+name);
            MemberArrayAdapter ma = loadMembersByFilters(null, name, peid, gndr, null, filter1, filter2, filter3);
            setMemberAdapter(ma);
        }
    }

    private void initialize(View view) {
        if (getActivity() instanceof MemberActionListener){
            this.memberActionListener = (MemberActionListener) getActivity();
        }

        this.lvMembersList = (ListView) view.findViewById(R.id.lvMembersList);
        this.btMemListShowHHMap = (Button) view.findViewById(R.id.btMemListShowHHMap);
        this.btMemListShowMmbMap = (Button) view.findViewById(R.id.btMemListShowMmbMap);
        this.btMemListShowClosestPregnt = (Button) view.findViewById(R.id.btMemListShowClosestPregnt);
        this.listButtons = (LinearLayout) view.findViewById(R.id.viewListButtons);
        this.mProgressView = view.findViewById(R.id.viewListProgressBar);

        this.btMemListShowHHMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("test-show-hhmap", "not yet implemented");
            }
        });

        this.btMemListShowMmbMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("test-show-mmbmap", "not yet implemented");
                showMembersMap();
            }
        });

        this.btMemListShowClosestPregnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("test-show-closest-wmmap", "not yet implemented");
            }
        });
        
        this.lvMembersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onMemberClicked(position);
            }
        });

        this.database = new Database(getActivity());
    }

    private void showMembersMap() {
        MemberArrayAdapter adpter = (MemberArrayAdapter) this.lvMembersList.getAdapter();

        final MWMPoint[] points = new MWMPoint[adpter.getMembers().size()];


        for (int i=0; i < points.length; i++){
            Member m = adpter.getMembers().get(i);
            String name = m.getName();
            double lat = Double.parseDouble(m.getHhLatitude());
            double lon = Double.parseDouble(m.getHhLongitude());
            points[i] = new MWMPoint(lat, lon, name);
        }

        MapsWithMeApi.showPointsOnMap(this.getActivity(), "Localization of Women on map", points);

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
        if (member == null || member.getHhNumber()==null) return null;

        database.open();
        Household household = Queries.getHouseholdBy(database, DatabaseHelper.Household.COLUMN_HOUSE_NUMBER+"=?", new String[]{ member.getHhNumber() });
        database.close();

        return household;
    }

    public MemberArrayAdapter loadMembersByFilters(Household household, String name, String permId, String gender, String houseNumber, Boolean isPregnant, Boolean hasPom, Boolean hasFacility) {
        //open loader

        if (name == null) name = "";
        if (permId == null) permId = "";
        if (houseNumber == null) houseNumber = "";
        if (gender == null) gender = "";

        //save last search
        this.lastSearch = new ArrayList();
        this.lastSearch.add(name);
        this.lastSearch.add(permId);
        this.lastSearch.add(gender);
        this.lastSearch.add(houseNumber);
        this.lastSearch.add(isPregnant+"");
        this.lastSearch.add(hasPom+"");
        this.lastSearch.add(hasFacility+"");

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
        if (!gender.isEmpty()){
            whereClause += (whereClause.isEmpty()? "" : " AND ");
            whereClause += DatabaseHelper.Member.COLUMN_GENDER + " = ?";
            whereValues.add(gender);
        }
        if (!houseNumber.isEmpty()){
            whereClause += (whereClause.isEmpty()? "" : " AND ");
            whereClause += DatabaseHelper.Member.COLUMN_HH_NUMBER + " = ?";
            whereValues.add(houseNumber);
        }
        if (isPregnant != null && isPregnant){
            whereClause += (whereClause.isEmpty()? "" : " AND ");
            whereClause += DatabaseHelper.Member.COLUMN_IS_PREGNANT + " = ?";
            whereValues.add("1");
        }
        if (hasPom != null && hasPom){
            whereClause += (whereClause.isEmpty()? "" : " AND ");
            whereClause += DatabaseHelper.Member.COLUMN_ON_POM + " = ?";
            whereValues.add("1");
        }
        if (hasFacility != null && hasFacility){
            whereClause += (whereClause.isEmpty()? "" : " AND ");
            whereClause += DatabaseHelper.Member.COLUMN_ON_FACILITY + " = ?";
            whereValues.add("1");
        }

        database.open();

        String[] ar = new String[whereValues.size()];
        Cursor cursor = database.query(Member.class, DatabaseHelper.Member.ALL_COLUMNS, whereClause, whereValues.toArray(ar), null, null, DatabaseHelper.Member.COLUMN_PERM_ID);

        while (cursor.moveToNext()){
            Member member = Converter.cursorToMember(cursor);
            members.add(member);
            Log.d("household", ""+household);
            Log.d("head", ""+(household!=null ? household.getHead():"null"));
            if (household != null && household.getHead().equals(member.getPermId())){
                member.setHouseholdHead(true);
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
