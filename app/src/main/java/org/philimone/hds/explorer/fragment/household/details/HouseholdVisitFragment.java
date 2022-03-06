package org.philimone.hds.explorer.fragment.household.details;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.CoreCollectedExpandableAdapter;
import org.philimone.hds.explorer.adapter.MemberArrayAdapter;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.main.hdsforms.FormUtilListener;
import org.philimone.hds.explorer.main.hdsforms.MaritalRelationshipFormUtil;
import org.philimone.hds.explorer.main.hdsforms.MemberEnumerationFormUtil;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.MaritalRelationship;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.Gender;
import org.philimone.hds.explorer.model.enums.SubjectEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HouseholdVisitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HouseholdVisitFragment extends Fragment {

    private ListView lvHouseholdMembers;
    private ExpandableListView elvVisitCollected;
    private Button btClearMember;
    private Button btnVisitMemberEnu;
    private Button btnVisitBirthReg;
    private Button btnVisitPregnancyReg;
    private Button btnVisitExtInmigration;
    private Button btnVisitIntInmigration;
    private Button btnVisitOutmigration;
    private Button btnVisitDeath;
    private Button btnVisitMaritalRelationship;
    private Button btnVisitExtraForm;
    private Button btnVisitMemberIncomplete;
    private Button btnVisitChangeHead;

    private Household household;
    private Visit visit;
    private Member selectedMember;
    private User loggedUser;

    private Box<Member> boxMembers;
    private Box<Form> boxForms;
    private Box<CollectedData> boxCollectedData;
    private Box<CoreCollectedData> boxCoreCollectedData;
    
    private VisitEventsMode currentEventMode = VisitEventsMode.HOUSEHOLD_EVENTS;
    
    private enum VisitEventsMode { HOUSEHOLD_EVENTS, MEMBER_EVENTS}

    public HouseholdVisitFragment() {
        initBoxes();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HouseholdVisitFragment.
     */
    public static HouseholdVisitFragment newInstance(Household household, Visit visit, User user) {
        HouseholdVisitFragment fragment = new HouseholdVisitFragment();
        fragment.household = household;
        fragment.visit = visit;
        fragment.loggedUser = user;
        return fragment;
    }

    public void load(Household household, Visit visit, User user){
        this.household = household;
        this.visit = visit;
        this.loggedUser = user;

        loadDataToListViews();
        setHouseholdMode();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.household_visit, container, false);

        initialize(view);

        return view;
    }

    private void initBoxes() {
        //this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        //this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
    }
    
    private void initialize(View view) {
        this.lvHouseholdMembers = view.findViewById(R.id.lvHouseholdMembers);
        this.elvVisitCollected = view.findViewById(R.id.elvVisitCollected);
        this.btClearMember = view.findViewById(R.id.btClearMember);
        this.btnVisitMemberEnu = view.findViewById(R.id.btnVisitMemberEnu);
        this.btnVisitBirthReg = view.findViewById(R.id.btnVisitBirthReg);
        this.btnVisitPregnancyReg = view.findViewById(R.id.btnVisitPregnancyReg);
        this.btnVisitExtInmigration = view.findViewById(R.id.btnVisitExtInmigration);
        this.btnVisitIntInmigration = view.findViewById(R.id.btnVisitIntInmigration);
        this.btnVisitOutmigration = view.findViewById(R.id.btnVisitOutmigration);
        this.btnVisitDeath = view.findViewById(R.id.btnVisitDeath);
        this.btnVisitMaritalRelationship = view.findViewById(R.id.btnVisitMaritalRelationship);
        this.btnVisitExtraForm = view.findViewById(R.id.btnVisitExtraForm);
        this.btnVisitMemberIncomplete = view.findViewById(R.id.btnVisitMemberIncomplete);
        this.btnVisitChangeHead = view.findViewById(R.id.btnVisitChangeHead);

        this.lvHouseholdMembers.setOnItemClickListener((parent, view1, position, id) -> onMembersClick(position));

        this.lvHouseholdMembers.setOnItemLongClickListener((parent, view12, position, id) -> {
            onMembersLongClick(position);
            return true;
        });

        this.btClearMember.setOnClickListener( v -> {
            onClearMemberClicked();
        });

        this.btnVisitMemberEnu.setOnClickListener(v -> {
            onEnumerationClicked();
        });

        this.btnVisitMaritalRelationship.setOnClickListener( v -> {
            onMaritalClicked();
        });

        this.btnVisitExtInmigration.setOnClickListener(v -> {
            onExtInMigrationClicked();
        });
    }

    private void selectMember(Member member){

        int index = getMembersAdapter().indexOf(member);
        Log.d("new-members", "size="+getMembersAdapter().getCount()+", index="+index);
        onMembersClick(index);
    }

    private void onMembersClick(int position) {
        //select one member and highlight

        MemberArrayAdapter adapter = getMembersAdapter();
        adapter.setSelectedIndex(position);

        this.selectedMember = adapter.getItem(position);
        setMemberMode();

        btClearMember.setVisibility(View.VISIBLE);
    }

    private void onMembersLongClick(int position) {
        //select one and show MemberDetails without Highlight
        MemberArrayAdapter adapter = getMembersAdapter();
        Member member = adapter.getItem(position);

        //TODO DO IT LATER
    }

    private MemberArrayAdapter getMembersAdapter(){
        if (this.lvHouseholdMembers.getAdapter()==null) return null;

        return (MemberArrayAdapter) this.lvHouseholdMembers.getAdapter();
    }

    private void clearMemberSelection(){

        btClearMember.setVisibility(View.GONE);

        MemberArrayAdapter adapter = getMembersAdapter();

        if (adapter != null){
            adapter.setSelectedIndex(-1);
        }
    }

    private void setHouseholdMode() {
        this.currentEventMode = VisitEventsMode.HOUSEHOLD_EVENTS;
        
        clearMemberSelection();

        this.btnVisitMemberEnu.setEnabled(true);
        this.btnVisitBirthReg.setEnabled(false);
        this.btnVisitPregnancyReg.setEnabled(false);
        this.btnVisitExtInmigration.setEnabled(true);
        this.btnVisitIntInmigration.setEnabled(false);
        this.btnVisitOutmigration.setEnabled(false);
        this.btnVisitDeath.setEnabled(false);
        this.btnVisitMaritalRelationship.setEnabled(false);
        this.btnVisitExtraForm.setEnabled(false); //we need to analyse better this

        this.btnVisitChangeHead.setEnabled(true);
        this.btnVisitPregnancyReg.setVisibility(View.GONE);
        this.btnVisitChangeHead.setVisibility(View.VISIBLE);

        this.btnVisitMemberIncomplete.setEnabled(false);
        this.btnVisitMemberEnu.setVisibility(View.VISIBLE);
        this.btnVisitMemberIncomplete.setVisibility(View.GONE);
    }
    
    private void setMemberMode() {
        this.currentEventMode = VisitEventsMode.MEMBER_EVENTS;

        boolean notVisited = countCollectedForms(selectedMember)==0;

        this.btnVisitMemberEnu.setEnabled(false);
        this.btnVisitBirthReg.setEnabled(this.selectedMember!=null && this.selectedMember.gender== Gender.FEMALE);
        this.btnVisitPregnancyReg.setEnabled(this.selectedMember!=null && this.selectedMember.gender== Gender.FEMALE);
        this.btnVisitExtInmigration.setEnabled(false);
        this.btnVisitIntInmigration.setEnabled(true);
        this.btnVisitOutmigration.setEnabled(true);
        this.btnVisitDeath.setEnabled(true);
        this.btnVisitMaritalRelationship.setEnabled(true);
        this.btnVisitExtraForm.setEnabled(true); //we need to analyse better this

        this.btnVisitChangeHead.setEnabled(false);
        this.btnVisitPregnancyReg.setVisibility(View.VISIBLE);
        this.btnVisitChangeHead.setVisibility(View.GONE);

        this.btnVisitMemberIncomplete.setEnabled(notVisited);
        this.btnVisitMemberEnu.setVisibility(View.GONE);
        this.btnVisitMemberIncomplete.setVisibility(View.VISIBLE);
    }

    private void loadMembersToList() {

        btClearMember.setVisibility(View.GONE);

        if (household == null) return;

        List<Member> members = this.boxMembers.query().equal(Member_.householdCode, household.getCode(), QueryBuilder.StringOrder.CASE_SENSITIVE).build().find();

        MemberArrayAdapter adapter = new MemberArrayAdapter(this.getContext(), R.layout.household_visit_member_item, members);
        adapter.setShowHouseholdHead(false);
        this.lvHouseholdMembers.setAdapter(adapter);
    }

    private void loadCollectedEventsToList() {

        if (visit == null) return;

        //Type: Member Enumeration (3) -> CoreCollectedData as subitem
        //layouts: core_form_collected_item,

        List<CoreCollectedData> coreList = this.boxCoreCollectedData.query().equal(CoreCollectedData_.visitId, this.visit.id)
                                                                            .order(CoreCollectedData_.createdDate).build().find();
        LinkedHashMap<CoreFormEntity, List<CoreCollectedData>> mapData = new LinkedHashMap<>();
        
        mapData.put(CoreFormEntity.HOUSEHOLD, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.MEMBER_ENU, new ArrayList<CoreCollectedData>());
        //mapData.put(CoreFormEntity.HEAD_RELATIONSHIP, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.MARITAL_RELATIONSHIP, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.INMIGRATION, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.EXTERNAL_INMIGRATION, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.OUTMIGRATION, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.PREGNANCY_REGISTRATION, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.PREGNANCY_OUTCOME, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.DEATH, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.CHANGE_HOUSEHOLD_HEAD, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.MEMBER_NOT_VISITED, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.VISIT, new ArrayList<CoreCollectedData>());

        //group the coreList in Map
        for(CoreCollectedData collectedData : coreList) {
            mapData.get(collectedData.formEntity).add(collectedData);
        }

        CoreCollectedExpandableAdapter adapter = new CoreCollectedExpandableAdapter(this.getContext(), mapData);
        this.elvVisitCollected.setAdapter(adapter);
    }

    private void loadDataToListViews(){
        loadMembersToList();
        loadCollectedEventsToList();
    }

    private long countCollectedForms(Member member) {
        long count1 = this.boxCoreCollectedData.query().equal(CoreCollectedData_.visitId, visit.id)
                .equal(CoreCollectedData_.formEntityCode, member.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .notEqual(CoreCollectedData_.formEntity, CoreFormEntity.VISIT.code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().count(); //CHECK FOR VISITS (visit id is equal to member.id)

        long count2 = this.boxCollectedData.query().equal(CollectedData_.visitId, visit.id)
                .equal(CollectedData_.recordId, member.id)
                .equal(CollectedData_.recordEntity, SubjectEntity.MEMBER.code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().count();

        //any form collected for this member
        return count1 + count2;
    }

    //region Events Execution

    private void onClearMemberClicked() {
        setHouseholdMode();
    }

    private void onEnumerationClicked() {

        Log.d("on-enum-click-household", ""+this.household);

        MemberEnumerationFormUtil formUtil = new MemberEnumerationFormUtil(getActivity().getSupportFragmentManager(), this.getContext(), this.visit, this.household, new FormUtilListener<Member>() {
            @Override
            public void onNewEntityCreated(Member member) {
                selectedMember = member;
                loadDataToListViews();
                selectMember(member);
            }

            @Override
            public void onEntityEdited(Member member) {

            }

            @Override
            public void onFormCancelled() {

            }
        });

        formUtil.collect();
    }

    private void onMaritalClicked() {
        //get current selected member
        //this.selectedMember

        Log.d("on-enum-click-member", ""+this.selectedMember);

        MaritalRelationshipFormUtil formUtil = new MaritalRelationshipFormUtil(getActivity().getSupportFragmentManager(), this.getContext(), this.visit, this.selectedMember, new FormUtilListener<MaritalRelationship>() {
            @Override
            public void onNewEntityCreated(MaritalRelationship maritalRelationship) {
                loadDataToListViews();
            }

            @Override
            public void onEntityEdited(MaritalRelationship maritalRelationship) {

            }

            @Override
            public void onFormCancelled() {

            }
        });

        formUtil.collect();
    }

    private void onExtInMigrationClicked() {

    }



    public List<Member> getNonVisitedMembers() {
        ///check if all individuals were visited
        List<Member> members = this.boxMembers.query().equal(Member_.householdCode, household.getCode(), QueryBuilder.StringOrder.CASE_SENSITIVE).build().find();
        List<Member> membersNotVisited = new ArrayList<>();

        for (Member member : members) {
            long count = countCollectedForms(member);
            //any form collected for this member
            if (count == 0) {
                membersNotVisited.add(member);
            }
        }


        return membersNotVisited;

    }

    //endregion

}