package org.philimone.hds.explorer.fragment.household.details;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.CoreCollectedExpandableAdapter;
import org.philimone.hds.explorer.adapter.MemberAdapter;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.listeners.HouseholdDetailsListener;
import org.philimone.hds.explorer.main.MemberDetailsActivity;
import org.philimone.hds.explorer.main.hdsforms.ChangeHeadFormUtil;
import org.philimone.hds.explorer.main.hdsforms.DeathFormUtil;
import org.philimone.hds.explorer.main.hdsforms.EditCoreExtensionFormUtil;
import org.philimone.hds.explorer.main.hdsforms.ExternalInMigrationFormUtil;
import org.philimone.hds.explorer.main.hdsforms.FormUtil;
import org.philimone.hds.explorer.main.hdsforms.FormUtilListener;
import org.philimone.hds.explorer.main.hdsforms.HouseholdFormUtil;
import org.philimone.hds.explorer.main.hdsforms.InternalInMigrationFormUtil;
import org.philimone.hds.explorer.main.hdsforms.MaritalRelationshipFormUtil;
import org.philimone.hds.explorer.main.hdsforms.MemberEnumerationFormUtil;
import org.philimone.hds.explorer.main.hdsforms.IncompleteVisitFormUtil;
import org.philimone.hds.explorer.main.hdsforms.OutmigrationFormUtil;
import org.philimone.hds.explorer.main.hdsforms.PregnancyOutcomeFormUtil;
import org.philimone.hds.explorer.main.hdsforms.PregnancyRegistrationFormUtil;
import org.philimone.hds.explorer.main.hdsforms.VisitFormUtil;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.Death;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.IncompleteVisit;
import org.philimone.hds.explorer.model.Inmigration;
import org.philimone.hds.explorer.model.MaritalRelationship;
import org.philimone.hds.explorer.model.MaritalRelationship_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Outmigration;
import org.philimone.hds.explorer.model.PregnancyOutcome;
import org.philimone.hds.explorer.model.PregnancyRegistration;
import org.philimone.hds.explorer.model.PregnancyRegistration_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.Gender;
import org.philimone.hds.explorer.model.enums.PregnancyStatus;
import org.philimone.hds.explorer.model.enums.SubjectEntity;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.LoadingDialog;
import org.philimone.hds.explorer.widget.RecyclerListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HouseholdVisitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HouseholdVisitFragment extends Fragment {

    private RecyclerListView lvHouseholdMembers;
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

    private LoadingDialog loadingDialog;

    private Household household;
    private Visit visit;
    private Member selectedMember;
    private User loggedUser;
    private Map<String, Object> visitExtraData = new HashMap<>();

    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Visit> boxVisits;
    private Box<Member> boxMembers;
    private Box<MaritalRelationship> boxMaritalRelationships;
    private Box<Inmigration> boxInmigrations;
    private Box<Outmigration> boxOutmigrations;
    private Box<PregnancyRegistration> boxPregnancyRegistrations;
    private Box<PregnancyOutcome> boxPregnancyOutcomes;
    private Box<Death> boxDeaths;
    private Box<HeadRelationship> boxHeadRelationships;
    private Box<IncompleteVisit> boxIncompleteVisits;
    private Box<CollectedData> boxCollectedData;
    private Box<CoreCollectedData> boxCoreCollectedData;

    private HouseholdDetailsListener householdDetailsListener;

    private FormUtilities odkFormUtilities;
    
    private VisitEventsMode currentEventMode = VisitEventsMode.HOUSEHOLD_EVENTS;

    private boolean respondentNotRegistered = false;
    
    private enum VisitEventsMode { HOUSEHOLD_EVENTS, MEMBER_EVENTS, RESPONDENT_NOT_REG_EVENTS}

    public HouseholdVisitFragment(){
        initBoxes();
    }

    public HouseholdVisitFragment(HouseholdDetailsListener listener) {
        this.householdDetailsListener = listener;
        initBoxes();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HouseholdVisitFragment.
     */
    public static HouseholdVisitFragment newInstance(Household household, Visit visit, User user, HouseholdDetailsListener listener, Map<String, Object> visitData) {
        HouseholdVisitFragment fragment = new HouseholdVisitFragment(listener);
        fragment.household = household;
        fragment.visit = visit;
        fragment.loggedUser = user;
        fragment.householdDetailsListener = listener;
        return fragment;
    }

    public void load(Household household, Visit visit, User user, Map<String, Object> data){
        this.household = household;
        this.visit = visit;
        this.loggedUser = user;

        if (data != null) {
            this.visitExtraData.putAll(data);
        }

        loadDataToListViews();


        if (data.get("respondentNotRegistered") != null) {
            Boolean notRegistered = (Boolean) data.get("respondentNotRegistered");

            if (notRegistered){
                this.respondentNotRegistered = true;
                setRespondentNotRegVisitMode();
                return;
            }
        }

        setHouseholdMode();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.odkFormUtilities = new FormUtilities(this, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.household_visit, container, false);

        initialize(view);

        return view;
    }

    public HouseholdDetailsListener getHouseholdDetailsListener() {
        return householdDetailsListener;
    }

    public void setHouseholdDetailsListener(HouseholdDetailsListener householdDetailsListener) {
        this.householdDetailsListener = householdDetailsListener;
    }

    private void initBoxes() {
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxVisits = ObjectBoxDatabase.get().boxFor(Visit.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxMaritalRelationships = ObjectBoxDatabase.get().boxFor(MaritalRelationship.class);
        this.boxInmigrations = ObjectBoxDatabase.get().boxFor(Inmigration.class);
        this.boxInmigrations = ObjectBoxDatabase.get().boxFor(Inmigration.class);
        this.boxOutmigrations = ObjectBoxDatabase.get().boxFor(Outmigration.class);
        this.boxPregnancyRegistrations = ObjectBoxDatabase.get().boxFor(PregnancyRegistration.class);
        this.boxPregnancyOutcomes = ObjectBoxDatabase.get().boxFor(PregnancyOutcome.class);
        this.boxDeaths = ObjectBoxDatabase.get().boxFor(Death.class);
        this.boxHeadRelationships = ObjectBoxDatabase.get().boxFor(HeadRelationship.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxIncompleteVisits = ObjectBoxDatabase.get().boxFor(IncompleteVisit.class);
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

        this.loadingDialog = new LoadingDialog(this.getContext());

        this.lvHouseholdMembers.addOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long id) {
                onMembersClick(position);
            }

            @Override
            public void onItemLongClick(View view, int position, long id) {
                onMembersLongClick(position);
            }
        });

        this.elvVisitCollected.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                CoreCollectedExpandableAdapter adapter = (CoreCollectedExpandableAdapter) parent.getExpandableListAdapter();
                CoreCollectedData coreCollectedData = (CoreCollectedData) adapter.getChild(groupPosition, childPosition);

                onVisitCollectedChildClicked(coreCollectedData);

                return true;
            }
        });

        this.elvVisitCollected.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);
                    Log.d("long clicked", "group="+groupPosition+", child="+childPosition+", position="+position);
                    CoreCollectedExpandableAdapter adapter = (CoreCollectedExpandableAdapter) elvVisitCollected.getExpandableListAdapter();
                    CoreCollectedData coreCollectedData = (CoreCollectedData) adapter.getChild(groupPosition, childPosition);

                    onVisitCollectedChildLongClicked(coreCollectedData);

                    return true;
                }

                return false;
            }
        });

        this.btClearMember.setOnClickListener( v -> {
            onClearMemberClicked();
        });

        this.btnVisitMemberIncomplete.setOnClickListener(v -> {
            onIncompleteVisitClicked(null);
        });

        this.btnVisitMemberEnu.setOnClickListener(v -> {
            onEnumerationClicked(null);
        });

        this.btnVisitMaritalRelationship.setOnClickListener( v -> {
            onMaritalClicked(null);
        });

        this.btnVisitExtInmigration.setOnClickListener(v -> {
            onExtInMigrationClicked(null);
        });

        this.btnVisitIntInmigration.setOnClickListener(v -> {
            onIntInmigrationClicked(null);
        });

        this.btnVisitOutmigration.setOnClickListener(v -> {
            onOutmigrationClicked(null);
        });

        this.btnVisitPregnancyReg.setOnClickListener(v -> {
            onPregnancyRegistrationClicked(null);
        });

        this.btnVisitBirthReg.setOnClickListener(v -> {
            onPregnancyOutcomeClicked(null);
        });

        this.btnVisitDeath.setOnClickListener(v -> {
            onDeathClicked(null);
        });

        this.btnVisitChangeHead.setOnClickListener(v -> {
            onChangeHeadClicked(null);
        });

        this.btnVisitExtraForm.setOnClickListener(v -> {
            onCollectExtraFormClicked();
        });

    }

    private void onCollectExtraFormClicked() {
        if (selectedMember != null) {

            MemberSelectedTask task = new MemberSelectedTask(selectedMember, household, true);
            task.execute();

            showLoadingDialog(getString(R.string.loading_dialog_member_details_lbl), true);
        }
    }

    private void selectMember(Member member){

        int index = getMembersAdapter().indexOf(member);
        Log.d("new-members", "size="+getMembersAdapter().getItemCount()+", index="+index);
        onMembersClick(index);
    }

    private void onMembersClick(int position) {
        //select one member and highlight

        MemberAdapter adapter = getMembersAdapter();
        adapter.setSelectedIndex(position);

        this.selectedMember = adapter.getItem(position);
        setMemberMode();

        btClearMember.setVisibility(View.VISIBLE);
    }

    private void onMembersLongClick(int position) {
        //select one and show MemberDetails without Highlight
        MemberAdapter adapter = getMembersAdapter();
        if (adapter != null) {
            Member member = adapter.getItem(position);

            MemberSelectedTask task = new MemberSelectedTask(member, household, false);
            task.execute();

            showLoadingDialog(getString(R.string.loading_dialog_member_details_lbl), true);
        }
    }

    private MemberAdapter getMembersAdapter(){
        if (this.lvHouseholdMembers.getAdapter()==null) return null;

        return (MemberAdapter) this.lvHouseholdMembers.getAdapter();
    }

    private void clearMemberSelection(){

        btClearMember.setVisibility(View.GONE);

        MemberAdapter adapter = getMembersAdapter();

        if (adapter != null){
            adapter.setSelectedIndex(-1);
        }

        this.selectedMember = null;
    }

    private void setHouseholdMode() {
        this.currentEventMode = VisitEventsMode.HOUSEHOLD_EVENTS;
        boolean isCensusHousehold = this.household.recentlyCreated;
        clearMemberSelection();

        this.btnVisitMemberEnu.setEnabled(true);
        this.btnVisitBirthReg.setEnabled(false);
        this.btnVisitPregnancyReg.setEnabled(false);

        this.btnVisitExtInmigration.setEnabled(true && !isCensusHousehold);
        this.btnVisitIntInmigration.setEnabled(true && !isCensusHousehold);
        this.btnVisitChangeHead.setEnabled(true && !isCensusHousehold);

        this.btnVisitOutmigration.setEnabled(false);
        this.btnVisitDeath.setEnabled(false);
        this.btnVisitMaritalRelationship.setEnabled(false);
        this.btnVisitExtraForm.setEnabled(false); //we need to analyse better this

        this.btnVisitPregnancyReg.setVisibility(View.GONE);
        this.btnVisitChangeHead.setVisibility(View.VISIBLE);

        this.btnVisitMemberIncomplete.setEnabled(false);
        this.btnVisitMemberEnu.setVisibility(View.VISIBLE);
        this.btnVisitMemberIncomplete.setVisibility(View.GONE);

        //disable buttons if already collected and ready to edit
        //ChangeHead - must be collected one per visit
        CoreCollectedData ccdataChangeHead = this.boxCoreCollectedData.query(CoreCollectedData_.visitId.equal(visit.id).and(CoreCollectedData_.formEntity.equal(CoreFormEntity.CHANGE_HOUSEHOLD_HEAD.code))).build().findFirst();
        btnVisitChangeHead.setEnabled(ccdataChangeHead == null);

        Log.d("household-visit"+visit.id, "changehead="+ccdataChangeHead);

        setMainListsSelectable(true);
    }

    private void setRespondentNotRegVisitMode(){
        //is household mode

        this.currentEventMode = VisitEventsMode.RESPONDENT_NOT_REG_EVENTS;
        boolean isCensusHousehold = this.household.recentlyCreated;

        clearMemberSelection();

        this.btnVisitMemberEnu.setEnabled(true);
        this.btnVisitBirthReg.setEnabled(false);
        this.btnVisitPregnancyReg.setEnabled(false);
        this.btnVisitExtInmigration.setEnabled(true && !isCensusHousehold);
        this.btnVisitIntInmigration.setEnabled(true && !isCensusHousehold);
        this.btnVisitOutmigration.setEnabled(false);
        this.btnVisitDeath.setEnabled(false);
        this.btnVisitMaritalRelationship.setEnabled(false);
        this.btnVisitExtraForm.setEnabled(false); //we need to analyse better this

        this.btnVisitChangeHead.setEnabled(false);
        this.btnVisitPregnancyReg.setVisibility(View.GONE);
        this.btnVisitChangeHead.setVisibility(View.VISIBLE);

        this.btnVisitMemberIncomplete.setEnabled(false);
        this.btnVisitMemberEnu.setVisibility(View.VISIBLE);
        this.btnVisitMemberIncomplete.setVisibility(View.GONE);

        //disable item selection
        setMainListsSelectable(false);
    }
    
    private void setMemberMode() {
        this.currentEventMode = VisitEventsMode.MEMBER_EVENTS;
        boolean isCensusHousehold = this.household.recentlyCreated;
        boolean notVisited = countCollectedForms(selectedMember)==0;

        this.btnVisitMemberEnu.setEnabled(false);
        this.btnVisitBirthReg.setEnabled(this.selectedMember!=null && this.selectedMember.gender== Gender.FEMALE);
        this.btnVisitPregnancyReg.setEnabled(this.selectedMember!=null && this.selectedMember.gender== Gender.FEMALE);
        this.btnVisitExtInmigration.setEnabled(false);
        this.btnVisitIntInmigration.setEnabled(false);
        this.btnVisitOutmigration.setEnabled(true && !isCensusHousehold);
        this.btnVisitDeath.setEnabled(true && !isCensusHousehold);
        this.btnVisitMaritalRelationship.setEnabled(true);
        this.btnVisitExtraForm.setEnabled(true); //we need to analyse better this

        this.btnVisitChangeHead.setEnabled(false);
        this.btnVisitPregnancyReg.setVisibility(View.VISIBLE);
        this.btnVisitChangeHead.setVisibility(View.GONE);

        this.btnVisitMemberIncomplete.setEnabled(notVisited);
        this.btnVisitMemberEnu.setVisibility(View.GONE);
        this.btnVisitMemberIncomplete.setVisibility(View.VISIBLE);

        //disable buttons if already collected and ready to edit
        //Incomplete, Marital, Pregnancy Reg and Pregnancy Outcome - must be collected one per visit and member
        CoreCollectedData ccdataIncomplete = this.boxCoreCollectedData.query(CoreCollectedData_.visitId.equal(visit.id).and(CoreCollectedData_.formEntity.equal(CoreFormEntity.INCOMPLETE_VISIT.code)).and(CoreCollectedData_.formEntityCode.equal(selectedMember.code))).build().findFirst();
        CoreCollectedData ccdataPregnancy = this.boxCoreCollectedData.query(CoreCollectedData_.visitId.equal(visit.id).and(CoreCollectedData_.formEntity.equal(CoreFormEntity.PREGNANCY_REGISTRATION.code)).and(CoreCollectedData_.formEntityCode.equal(selectedMember.code))).build().findFirst();
        CoreCollectedData ccdataPOutcome = this.boxCoreCollectedData.query(CoreCollectedData_.visitId.equal(visit.id).and(CoreCollectedData_.formEntity.equal(CoreFormEntity.PREGNANCY_OUTCOME.code)).and(CoreCollectedData_.formEntityCode.equal(selectedMember.code))).build().findFirst();
        List<CoreCollectedData> ccdataMaritals = this.boxCoreCollectedData.query(CoreCollectedData_.visitId.equal(visit.id).and(CoreCollectedData_.formEntity.equal(CoreFormEntity.MARITAL_RELATIONSHIP.code))).build().find();
        boolean hasMaritalRelationship = false;
        for (CoreCollectedData collectedData : ccdataMaritals) { //all marital relationships registered in this visit
            long countm = this.boxMaritalRelationships.query(MaritalRelationship_.id.equal(collectedData.formEntityId).and(MaritalRelationship_.memberA_code.equal(selectedMember.code).or(MaritalRelationship_.memberB_code.equal(selectedMember.code)))).build().count();
            if (countm>0) {
                hasMaritalRelationship = true;
                break;
            }
        }

        btnVisitMemberIncomplete.setEnabled(btnVisitMemberIncomplete.isEnabled() && ccdataIncomplete == null);
        btnVisitMaritalRelationship.setEnabled(btnVisitMaritalRelationship.isEnabled() && hasMaritalRelationship == false);
        btnVisitPregnancyReg.setEnabled(btnVisitPregnancyReg.isEnabled() && ccdataPregnancy == null);
        btnVisitBirthReg.setEnabled(btnVisitBirthReg.isEnabled() && ccdataPOutcome == null);

        setMainListsSelectable(true);
    }

    private void loadMembersToList() {

        btClearMember.setVisibility(View.GONE);

        if (household == null) return;

        List<Member> members = this.boxMembers.query().equal(Member_.householdCode, household.getCode(), QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                      .equal(Member_.endType, ResidencyEndType.NOT_APPLICABLE.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                      .build().find();

        MemberAdapter adapter = new MemberAdapter(this.getContext(), R.layout.household_visit_member_item, members);
        //adapter.setShowExtraDetails(true);
        adapter.setShowGender(true);
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
        mapData.put(CoreFormEntity.INCOMPLETE_VISIT, new ArrayList<CoreCollectedData>());
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

        if (this.selectedMember != null) { //was selected
            selectMember(selectedMember);
        }
    }

    private void setMainListsSelectable(boolean itemselectable) {
        lvHouseholdMembers.setItemsSelectable(itemselectable);
        elvVisitCollected.setClickable(itemselectable);
        elvVisitCollected.setFocusable(itemselectable);
    }

    private long countCollectedForms(Member member) {
        /*long count1 = this.boxCoreCollectedData.query()
                .equal(CoreCollectedData_.visitId, visit.id)
                .equal(CoreCollectedData_.formEntityCode, member.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .contains(CoreCollectedData_.formEntityCodes, member.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
   .notEqual(CoreCollectedData_.formEntity, CoreFormEntity.VISIT.code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().count(); //CHECK FOR VISITS (visit id is equal to member.id)
        */

        long count1 = this.boxCoreCollectedData.query(
                CoreCollectedData_.visitId.equal(visit.id)
           .and(CoreCollectedData_.formEntityCode.equal(member.code).or(CoreCollectedData_.formEntityCodes.contains(member.code)))
           .and(CoreCollectedData_.formEntity.notEqual(CoreFormEntity.VISIT.code))).build().count();

        long count2 = this.boxCollectedData.query().equal(CollectedData_.visitId, visit.id)
                .equal(CollectedData_.recordId, member.id)
                .equal(CollectedData_.recordEntity, SubjectEntity.MEMBER.code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().count();

        //any form collected for this member
        return count1 + count2;
    }

    private void updateRespondentAfterNewMember(Member member) {
        if (respondentNotRegistered) {
            Log.d("notreg","true");
            respondentNotRegistered = false;

            visit.respondentCode = member.code;
            this.boxVisits.put(visit);

            VisitFormUtil.updateRespondent(this.getContext(), this.visit.getRecentlyCreatedUri(), member);
        }
    }

    //region Events Execution
    private void updateHouseholdDetails(){
        if (householdDetailsListener != null) {
            householdDetailsListener.updateHouseholdDetails();
        }
    }

    private void onVisitCollectedChildClicked(CoreCollectedData coreCollectedData) {
        switch (coreCollectedData.formEntity) {
            case HOUSEHOLD:
                Household household = this.boxHouseholds.get(coreCollectedData.formEntityId);
                onEditHousehold(household);
                break;
            case VISIT:
                Visit visit = this.boxVisits.get(coreCollectedData.formEntityId);
                onEditVisit(visit);
                break;
            case MEMBER_ENU:
                Member member = this.boxMembers.get(coreCollectedData.formEntityId);
                onEnumerationClicked(member);
                break;
            case MARITAL_RELATIONSHIP:
                MaritalRelationship mrelationship = this.boxMaritalRelationships.get(coreCollectedData.formEntityId);
                onMaritalClicked(mrelationship);
                break;
            case INMIGRATION:
                Inmigration inmigration = this.boxInmigrations.get(coreCollectedData.formEntityId);
                onIntInmigrationClicked(inmigration);
                break;
            case EXTERNAL_INMIGRATION:
                Inmigration extinmigration = this.boxInmigrations.get(coreCollectedData.formEntityId);
                onExtInMigrationClicked(extinmigration);
                break;
            case OUTMIGRATION:
                Outmigration outmigration = this.boxOutmigrations.get(coreCollectedData.formEntityId);
                onOutmigrationClicked(outmigration);
                break;
            case PREGNANCY_REGISTRATION:
                PregnancyRegistration pregnancy_registration = this.boxPregnancyRegistrations.get(coreCollectedData.formEntityId);
                onPregnancyRegistrationClicked(pregnancy_registration);
                break;
            case PREGNANCY_OUTCOME:
                PregnancyOutcome pregnancy_outcome = this.boxPregnancyOutcomes.get(coreCollectedData.formEntityId);
                onPregnancyOutcomeClicked(pregnancy_outcome);
                break;
            case DEATH:
                Death death = this.boxDeaths.get(coreCollectedData.formEntityId);
                onDeathClicked(death);
                break;
            case CHANGE_HOUSEHOLD_HEAD:
                HeadRelationship cghead = this.boxHeadRelationships.get(coreCollectedData.formEntityId);
                onChangeHeadClicked(cghead);
                break;
            case INCOMPLETE_VISIT:
                IncompleteVisit incvisit = this.boxIncompleteVisits.get(coreCollectedData.formEntityId);
                onIncompleteVisitClicked(incvisit);
                break;
        }
    }

    private void onVisitCollectedChildLongClicked(CoreCollectedData coreCollectedData) {
        //edit the odk form
        if (coreCollectedData != null && coreCollectedData.extensionCollected) {
            ///edit
            Log.d("edot core odk", coreCollectedData.extensionCollectedUri);

            CollectedData odkCollectedData = boxCollectedData.query(CollectedData_.collectedId.equal(coreCollectedData.collectedId).and(CollectedData_.formUri.equal(coreCollectedData.extensionCollectedUri))).build().findFirst();

            EditCoreExtensionFormUtil formUtil = new EditCoreExtensionFormUtil(this, this.getContext(), null, coreCollectedData, this.household, this.odkFormUtilities, new EditCoreExtensionFormUtil.Listener() {
                @Override
                public void onFinishedCollecting() {
                    loadDataToListViews();
                    updateHouseholdDetails();
                }
            });

            formUtil.editExtensionForm(odkCollectedData);

        }
    }

    private void onClearMemberClicked() {
        setHouseholdMode();
    }

    private void onEditHousehold(Household household) {
        HouseholdFormUtil.newInstance(FormUtil.Mode.EDIT, this, this.getContext(), null, household, this.odkFormUtilities, new FormUtilListener<Household>() {
            @Override
            public void onNewEntityCreated(Household entity, Map<String, Object> data) { }

            @Override
            public void onEntityEdited(Household entity, Map<String, Object> data) {
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onFormCancelled() {

            }
        }).collect();
    }

    private void onEditVisit(Visit visit) {
        VisitFormUtil formUtil = new VisitFormUtil(this, this.getContext(), this.household, visit, this.odkFormUtilities, new FormUtilListener<Visit>() {
            @Override
            public void onNewEntityCreated(Visit entity, Map<String, Object> data) { }

            @Override
            public void onEntityEdited(Visit entity, Map<String, Object> data) {
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onFormCancelled() { }
        });

        formUtil.collect();
    }

    private void onIncompleteVisitClicked(IncompleteVisit incompleteVisit) {
        //get current selected member
        //this.selectedMember
        Log.d("on-incomplete-clicked", ""+this.selectedMember);

        FormUtil.Mode mode = incompleteVisit == null ? FormUtil.Mode.CREATE : FormUtil.Mode.EDIT;

        IncompleteVisitFormUtil formUtil = IncompleteVisitFormUtil.newInstance(mode,this, this.getContext(), this.visit, this.selectedMember, incompleteVisit, this.odkFormUtilities, new FormUtilListener<IncompleteVisit>() {
            @Override
            public void onNewEntityCreated(IncompleteVisit entity, Map<String, Object> data) {
                loadDataToListViews();
            }

            @Override
            public void onEntityEdited(IncompleteVisit entity, Map<String, Object> data) {
                loadDataToListViews();
            }

            @Override
            public void onFormCancelled() {

            }
        });

        formUtil.collect();
    }

    private void onEnumerationClicked(Member member) {

        Log.d("on-enum-click-household", ""+this.household);

        FormUtil.Mode mode = member == null ? FormUtil.Mode.CREATE : FormUtil.Mode.EDIT;

        MemberEnumerationFormUtil formUtil = MemberEnumerationFormUtil.newInstance(mode, this, this.getContext(), this.visit, this.household, member, this.odkFormUtilities, new FormUtilListener<Member>() {
            @Override
            public void onNewEntityCreated(Member member, Map<String, Object> data) {
                selectedMember = member;
                loadDataToListViews();
                //selectMember(member);
                updateRespondentAfterNewMember(member);
                updateHouseholdDetails();
            }

            @Override
            public void onEntityEdited(Member member, Map<String, Object> data) {
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onFormCancelled() {

            }
        });

        formUtil.collect();
    }

    private void onMaritalClicked(MaritalRelationship maritalRelationship) {        
        Log.d("on-marital-clicked", ""+maritalRelationship);

        FormUtil.Mode mode = maritalRelationship == null ? FormUtil.Mode.CREATE : FormUtil.Mode.EDIT;

        MaritalRelationshipFormUtil formUtil = MaritalRelationshipFormUtil.newInstance(mode, this, this.getContext(), this.visit, this.selectedMember, maritalRelationship, this.odkFormUtilities, new FormUtilListener<MaritalRelationship>() {
            @Override
            public void onNewEntityCreated(MaritalRelationship maritalRelationship, Map<String, Object> data) {
                loadDataToListViews();
            }

            @Override
            public void onEntityEdited(MaritalRelationship maritalRelationship, Map<String, Object> data) {
                loadDataToListViews();
            }

            @Override
            public void onFormCancelled() {

            }
        });

        formUtil.collect();
    }

    private void onExtInMigrationClicked(Inmigration inmigration) {
        Log.d("on-extinmigration", ""+this.household);

        FormUtil.Mode mode = inmigration == null ? FormUtil.Mode.CREATE : FormUtil.Mode.EDIT;

        ExternalInMigrationFormUtil formUtil = ExternalInMigrationFormUtil.newInstance(mode, this, this.getContext(), this.visit, this.household, inmigration, this.odkFormUtilities, new FormUtilListener<Inmigration>() {
            @Override
            public void onNewEntityCreated(Inmigration inmigration, Map<String, Object> data) {
                Member member = boxMembers.query(Member_.code.equal(inmigration.memberCode)).build().findFirst();
                selectedMember = member;

                updateRespondentAfterNewMember(member);
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onEntityEdited(Inmigration inmigration, Map<String, Object> data) {
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onFormCancelled() {

            }
        });

        formUtil.collect();
    }

    private void onIntInmigrationClicked(Inmigration inmigration) {
        Log.d("on-int-tinmigration", ""+this.household);

        FormUtil.Mode mode = inmigration == null ? FormUtil.Mode.CREATE : FormUtil.Mode.EDIT;
        
        InternalInMigrationFormUtil formUtil = InternalInMigrationFormUtil.newInstance(mode, this, this.getContext(), this.visit, this.household, inmigration, this.odkFormUtilities, new FormUtilListener<Inmigration>() {
            @Override
            public void onNewEntityCreated(Inmigration inmigration, Map<String, Object> data) {
                selectedMember = boxMembers.query().equal(Member_.code, inmigration.memberCode, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

                updateRespondentAfterNewMember(selectedMember);
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onEntityEdited(Inmigration inmigration, Map<String, Object> data) {
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onFormCancelled() {

            }
        });

        formUtil.collect();
    }

    private void onOutmigrationClicked(Outmigration outmigration) {
        Log.d("on-outmigration", ""+this.selectedMember);

        FormUtil.Mode mode = outmigration == null ? FormUtil.Mode.CREATE : FormUtil.Mode.EDIT;

        OutmigrationFormUtil formUtil = OutmigrationFormUtil.newInstance(mode, this, this.getContext(), this.visit, this.household, this.selectedMember, outmigration, this.odkFormUtilities, new FormUtilListener<Outmigration>() {
            @Override
            public void onNewEntityCreated(Outmigration outmigration, Map<String, Object> data) {
                selectedMember = null;
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onEntityEdited(Outmigration outmigration, Map<String, Object> data) {
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onFormCancelled() {

            }
        });

        formUtil.collect();
    }

    private void onPregnancyRegistrationClicked(PregnancyRegistration pregnancyRegistration) {
        Log.d("on-pregregistration", ""+this.selectedMember);

        FormUtil.Mode mode = pregnancyRegistration == null ? FormUtil.Mode.CREATE : FormUtil.Mode.EDIT;

        PregnancyRegistrationFormUtil formUtil = PregnancyRegistrationFormUtil.newInstance(mode, this, this.getContext(), this.visit, this.household, this.selectedMember, pregnancyRegistration, this.odkFormUtilities, new FormUtilListener<PregnancyRegistration>() {
            @Override
            public void onNewEntityCreated(PregnancyRegistration pregnancyRegistration, Map<String, Object> data) {
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onEntityEdited(PregnancyRegistration pregnancyRegistration, Map<String, Object> data) {
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onFormCancelled() {

            }
        });

        formUtil.collect();
    }

    private void onPregnancyOutcomeClicked(PregnancyOutcome pregnancyOutcome) {
        Log.d("on-pregoutcome", ""+this.selectedMember);

        FormUtil.Mode mode = null;
        
        if (pregnancyOutcome == null) {
            mode = FormUtil.Mode.CREATE;

            PregnancyRegistration pregnancyRegistration = getLastPregnancyRegistration(this.selectedMember);

            if (pregnancyRegistration == null || (pregnancyRegistration != null && pregnancyRegistration.status != PregnancyStatus.PREGNANT)){
                //create new pregnancy with status delivered in resume mode
                //there is no previous collected pregnancy registration for this outcome,\n a new pregnancy registration will be created with status as DELIVERED, THEN YOU WILL CONTINUE THE REGISTRATION

                DialogFactory.createMessageInfo(this.getContext(), R.string.pregnancy_outcome_nroutcomes_title_lbl, R.string.pregnancy_outcome_create_pregreg_info_lbl, new DialogFactory.OnClickListener() {
                    @Override
                    public void onClicked(DialogFactory.Buttons clickedButton) {
                        createPregnancyRegistrationForOutcome();
                    }
                }).show();
            } else {
                openPregnancyOutcomeForm(pregnancyRegistration, false);
            }
            
        } else {
            mode = FormUtil.Mode.EDIT;
            openPregnancyOutcomeForEdit(pregnancyOutcome);
        }
        
        
    }

    private void createPregnancyRegistrationForOutcome() {
        Log.d("started ", "pregnancy outcome - preg registration");
        new PregnancyRegistrationFormUtil(this, this.getContext(), this.visit, this.household, this.selectedMember, PregnancyStatus.DELIVERED, this.odkFormUtilities, new FormUtilListener<PregnancyRegistration>() {
            @Override
            public void onNewEntityCreated(PregnancyRegistration entity, Map<String, Object> data) {
                openPregnancyOutcomeForm(entity, true);
            }

            @Override
            public void onEntityEdited(PregnancyRegistration entity, Map<String, Object> data) {
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onFormCancelled() {

            }
        }).collect();
    }

    private void openPregnancyOutcomeForm(PregnancyRegistration pregnancyRegistration, boolean createdForOutcome){
                
        new PregnancyOutcomeFormUtil(this, this.getContext(), this.visit, this.household, this.selectedMember, pregnancyRegistration, createdForOutcome, this.odkFormUtilities, new FormUtilListener<PregnancyOutcome>() {
            @Override
            public void onNewEntityCreated(PregnancyOutcome pregnancyOutcome, Map<String, Object> data) {
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onEntityEdited(PregnancyOutcome pregnancyOutcome, Map<String, Object> data) {
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onFormCancelled() {

            }
        }).collect();
    }

    private void openPregnancyOutcomeForEdit(PregnancyOutcome pregnancyOutcome){

        //FOR EDITING
        new PregnancyOutcomeFormUtil(this, this.getContext(), this.visit, this.household, pregnancyOutcome, this.odkFormUtilities, new FormUtilListener<PregnancyOutcome>() {
            @Override
            public void onNewEntityCreated(PregnancyOutcome pregnancyOutcome, Map<String, Object> data) { }

            @Override
            public void onEntityEdited(PregnancyOutcome pregnancyOutcome, Map<String, Object> data) {
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onFormCancelled() {

            }
        }).collect();
    }
    
    private PregnancyRegistration getLastPregnancyRegistration(Member motherMember){
        //def pregnancies = PregnancyRegistration.executeQuery("select p from PregnancyRegistration p where p.mother.code=? order by p.recordedDate desc", [motherCode], [offset:0, max:1])
        PregnancyRegistration pregnancyRegistration = this.boxPregnancyRegistrations.query(PregnancyRegistration_.motherCode.equal(motherMember.code))
                .orderDesc(PregnancyRegistration_.code).build().findFirst();

        return pregnancyRegistration;
    }

    private void onDeathClicked(Death death) {
        Log.d("on-death", ""+this.selectedMember);

        FormUtil.Mode mode = death == null ? FormUtil.Mode.CREATE : FormUtil.Mode.EDIT;

        DeathFormUtil formUtil = DeathFormUtil.newInstance(mode, this, this.getContext(), this.visit, this.household, this.selectedMember, death, this.odkFormUtilities, new FormUtilListener<Death>() {
            @Override
            public void onNewEntityCreated(Death death, Map<String, Object> data) {
                selectedMember = null;
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onEntityEdited(Death death, Map<String, Object> data) {
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onFormCancelled() {

            }
        });

        formUtil.collect();
    }

    private void onChangeHeadClicked(HeadRelationship headRelationship) {
        Log.d("on-changehead", ""+headRelationship);

        FormUtil.Mode mode = headRelationship == null ? FormUtil.Mode.CREATE : FormUtil.Mode.EDIT;
        
        ChangeHeadFormUtil formUtil = ChangeHeadFormUtil.newInstance(mode, this, this.getContext(), this.visit, this.household, headRelationship, this.odkFormUtilities, new FormUtilListener<HeadRelationship>() {
            @Override
            public void onNewEntityCreated(HeadRelationship headRelationship, Map<String, Object> data) {
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onEntityEdited(HeadRelationship headRelationship, Map<String, Object> data) {
                loadDataToListViews();
                updateHouseholdDetails();
            }

            @Override
            public void onFormCancelled() {

            }
        });

        formUtil.collect();
    }

    public List<Member> getNonVisitedMembers() {
        ///check if all individuals were visited
        List<Member> members = this.boxMembers.query().equal(Member_.householdCode, household.getCode(), QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                      .and().equal(Member_.endType, ResidencyEndType.NOT_APPLICABLE.code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().find();
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

    private void showLoadingDialog(String msg, boolean show){
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.dismiss();
        }
    }

    class MemberSelectedTask  extends AsyncTask<Void, Void, Void> {
        private Household household;
        private Member member;
        private boolean clickCollectData;

        public MemberSelectedTask(Member member, Household household, boolean clickCollectData) {
            this.household = household;
            this.member = member;
            this.clickCollectData = clickCollectData;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = new Intent(getActivity(), MemberDetailsActivity.class);
            intent.putExtra("household", this.household.id);
            intent.putExtra("member", this.member.id);

            if (clickCollectData) {
                intent.putExtra("odk-form-collect", "true");
            }

            showLoadingDialog(null, false);

            startActivity(intent);
        }
    }

}