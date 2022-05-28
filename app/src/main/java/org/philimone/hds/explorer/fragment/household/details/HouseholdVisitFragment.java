package org.philimone.hds.explorer.fragment.household.details;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.CoreCollectedExpandableAdapter;
import org.philimone.hds.explorer.adapter.MemberAdapter;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.listeners.HouseholdDetailsListener;
import org.philimone.hds.explorer.main.hdsforms.ChangeHeadFormUtil;
import org.philimone.hds.explorer.main.hdsforms.DeathFormUtil;
import org.philimone.hds.explorer.main.hdsforms.ExternalInMigrationFormUtil;
import org.philimone.hds.explorer.main.hdsforms.FormUtilListener;
import org.philimone.hds.explorer.main.hdsforms.InternalInMigrationFormUtil;
import org.philimone.hds.explorer.main.hdsforms.MaritalRelationshipFormUtil;
import org.philimone.hds.explorer.main.hdsforms.MemberEnumerationFormUtil;
import org.philimone.hds.explorer.main.hdsforms.IncompleteVisitFormUtil;
import org.philimone.hds.explorer.main.hdsforms.OutmigrationFormUtil;
import org.philimone.hds.explorer.main.hdsforms.PregnancyOutcomeFormUtil;
import org.philimone.hds.explorer.main.hdsforms.PregnancyRegistrationFormUtil;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.Death;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.IncompleteVisit;
import org.philimone.hds.explorer.model.Inmigration;
import org.philimone.hds.explorer.model.MaritalRelationship;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Outmigration;
import org.philimone.hds.explorer.model.PregnancyOutcome;
import org.philimone.hds.explorer.model.PregnancyRegistration;
import org.philimone.hds.explorer.model.PregnancyRegistration_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.Gender;
import org.philimone.hds.explorer.model.enums.PregnancyStatus;
import org.philimone.hds.explorer.model.enums.SubjectEntity;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.RecyclerListView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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

    private Household household;
    private Visit visit;
    private Member selectedMember;
    private User loggedUser;

    private Box<Member> boxMembers;
    private Box<Form> boxForms;
    private Box<CollectedData> boxCollectedData;
    private Box<CoreCollectedData> boxCoreCollectedData;
    private Box<PregnancyRegistration> boxPregnancyRegistrations;

    private HouseholdDetailsListener householdDetailsListener;

    private FormUtilities odkFormUtilities;
    
    private VisitEventsMode currentEventMode = VisitEventsMode.HOUSEHOLD_EVENTS;
    
    private enum VisitEventsMode { HOUSEHOLD_EVENTS, MEMBER_EVENTS}

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
    public static HouseholdVisitFragment newInstance(Household household, Visit visit, User user, HouseholdDetailsListener listener) {
        HouseholdVisitFragment fragment = new HouseholdVisitFragment(listener);
        fragment.household = household;
        fragment.visit = visit;
        fragment.loggedUser = user;
        fragment.householdDetailsListener = listener;
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
        //this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        //this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxPregnancyRegistrations = ObjectBoxDatabase.get().boxFor(PregnancyRegistration.class);
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

        this.btClearMember.setOnClickListener( v -> {
            onClearMemberClicked();
        });

        this.btnVisitMemberIncomplete.setOnClickListener(v -> {
            onIncompleteVisitClicked();
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

        this.btnVisitIntInmigration.setOnClickListener(v -> {
            onIntInmigrationClicked();
        });

        this.btnVisitOutmigration.setOnClickListener(v -> {
            onOutmigrationClicked();
        });

        this.btnVisitPregnancyReg.setOnClickListener(v -> {
            onPregnancyRegistrationClicked();
        });

        this.btnVisitBirthReg.setOnClickListener(v -> {
            onPregnancyOutcomeClicked();
        });

        this.btnVisitDeath.setOnClickListener(v -> {
            onDeathClicked();
        });

        this.btnVisitChangeHead.setOnClickListener(v -> {
            onChangeHeadClicked();
        });

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

            //TODO DO IT LATER
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
        
        clearMemberSelection();

        this.btnVisitMemberEnu.setEnabled(true);
        this.btnVisitBirthReg.setEnabled(false);
        this.btnVisitPregnancyReg.setEnabled(false);
        this.btnVisitExtInmigration.setEnabled(true);
        this.btnVisitIntInmigration.setEnabled(true);
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
        this.btnVisitIntInmigration.setEnabled(false);
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

        List<Member> members = this.boxMembers.query().equal(Member_.householdCode, household.getCode(), QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                      .equal(Member_.endType, ResidencyEndType.NOT_APPLICABLE.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                      .build().find();

        MemberAdapter adapter = new MemberAdapter(this.getContext(), R.layout.household_visit_member_item, members);
        adapter.setShowHouseholdHead(false);
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

    //region Events Execution

    private void onClearMemberClicked() {
        setHouseholdMode();
    }

    private void onIncompleteVisitClicked() {
        //get current selected member
        //this.selectedMember
        Log.d("on-incomplete-clicked", ""+this.selectedMember);

        IncompleteVisitFormUtil formUtil = new IncompleteVisitFormUtil(this, this.getContext(), this.visit, this.selectedMember, this.odkFormUtilities, new FormUtilListener<IncompleteVisit>() {
            @Override
            public void onNewEntityCreated(IncompleteVisit entity) {
                loadDataToListViews();
            }

            @Override
            public void onEntityEdited(IncompleteVisit entity) {
                loadDataToListViews();
            }

            @Override
            public void onFormCancelled() {

            }
        });

        formUtil.collect();
    }

    private void onEnumerationClicked() {

        Log.d("on-enum-click-household", ""+this.household);

        MemberEnumerationFormUtil formUtil = new MemberEnumerationFormUtil(this, this.getContext(), this.visit, this.household, this.odkFormUtilities, new FormUtilListener<Member>() {
            @Override
            public void onNewEntityCreated(Member member) {
                selectedMember = member;
                loadDataToListViews();
                //selectMember(member);
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

        Log.d("on-marital-clicked", ""+this.selectedMember);

        MaritalRelationshipFormUtil formUtil = new MaritalRelationshipFormUtil(this, this.getContext(), this.visit, this.selectedMember, this.odkFormUtilities, new FormUtilListener<MaritalRelationship>() {
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
        Log.d("on-extinmigration", ""+this.household);

        ExternalInMigrationFormUtil formUtil = new ExternalInMigrationFormUtil(this, this.getContext(), this.visit, this.household, this.odkFormUtilities, new FormUtilListener<Member>() {
            @Override
            public void onNewEntityCreated(Member member) {
                selectedMember = member;
                loadDataToListViews();
                //selectMember(member);
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

    private void onIntInmigrationClicked() {
        Log.d("on-int-tinmigration", ""+this.household);

        InternalInMigrationFormUtil formUtil = new InternalInMigrationFormUtil(this, this.getContext(), this.visit, this.household, this.odkFormUtilities, new FormUtilListener<Inmigration>() {
            @Override
            public void onNewEntityCreated(Inmigration inmigration) {
                selectedMember = boxMembers.query().equal(Member_.code, inmigration.memberCode, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
                loadDataToListViews();
                //selectMember(selectedMember);
            }

            @Override
            public void onEntityEdited(Inmigration inmigration) {

            }

            @Override
            public void onFormCancelled() {

            }
        });

        formUtil.collect();
    }

    private void onOutmigrationClicked() {
        Log.d("on-outmigration", ""+this.selectedMember);

        OutmigrationFormUtil formUtil = new OutmigrationFormUtil(this, this.getContext(), this.visit, this.household, this.selectedMember, this.odkFormUtilities, new FormUtilListener<Outmigration>() {
            @Override
            public void onNewEntityCreated(Outmigration outmigration) {
                selectedMember = null;
                loadDataToListViews();
            }

            @Override
            public void onEntityEdited(Outmigration outmigration) {

            }

            @Override
            public void onFormCancelled() {

            }
        });

        formUtil.collect();
    }

    private void onPregnancyRegistrationClicked() {
        Log.d("on-pregregistration", ""+this.selectedMember);

        PregnancyRegistrationFormUtil formUtil = new PregnancyRegistrationFormUtil(this, this.getContext(), this.visit, this.household, this.selectedMember, this.odkFormUtilities, new FormUtilListener<PregnancyRegistration>() {
            @Override
            public void onNewEntityCreated(PregnancyRegistration pregnancyRegistration) {
                loadDataToListViews();
            }

            @Override
            public void onEntityEdited(PregnancyRegistration pregnancyRegistration) {

            }

            @Override
            public void onFormCancelled() {

            }
        });

        formUtil.collect();
    }

    private void onPregnancyOutcomeClicked() {
        Log.d("on-pregoutcome", ""+this.selectedMember);

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
    }

    private void createPregnancyRegistrationForOutcome() {
        Log.d("started ", "pregnancy outcome - preg registration");
        new PregnancyRegistrationFormUtil(this, this.getContext(), this.visit, this.household, this.selectedMember, PregnancyStatus.DELIVERED, this.odkFormUtilities, new FormUtilListener<PregnancyRegistration>() {
            @Override
            public void onNewEntityCreated(PregnancyRegistration entity) {
                openPregnancyOutcomeForm(entity, true);
            }

            @Override
            public void onEntityEdited(PregnancyRegistration entity) {

            }

            @Override
            public void onFormCancelled() {

            }
        }).collect();
    }

    private void openPregnancyOutcomeForm(PregnancyRegistration pregnancyRegistration, boolean createdForOutcome){
        new PregnancyOutcomeFormUtil(this, this.getContext(), this.visit, this.household, this.selectedMember, pregnancyRegistration, createdForOutcome, this.odkFormUtilities, new FormUtilListener<PregnancyOutcome>() {
            @Override
            public void onNewEntityCreated(PregnancyOutcome pregnancyOutcome) {
                loadDataToListViews();
            }

            @Override
            public void onEntityEdited(PregnancyOutcome pregnancyOutcome) {

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

    private void onDeathClicked() {
        Log.d("on-death", ""+this.selectedMember);

        DeathFormUtil formUtil = new DeathFormUtil(this, this.getContext(), this.visit, this.household, this.selectedMember, this.odkFormUtilities, new FormUtilListener<Death>() {
            @Override
            public void onNewEntityCreated(Death death) {
                selectedMember = null;
                loadDataToListViews();
            }

            @Override
            public void onEntityEdited(Death death) {

            }

            @Override
            public void onFormCancelled() {

            }
        });

        formUtil.collect();
    }

    private void onChangeHeadClicked() {
        Log.d("on-changehead", ""+this.household.code);

        ChangeHeadFormUtil formUtil = new ChangeHeadFormUtil(this, this.getContext(), this.visit, this.household, this.odkFormUtilities, new FormUtilListener<Member>() {
            @Override
            public void onNewEntityCreated(Member newHeadMember) {
                loadDataToListViews();

                if (householdDetailsListener != null) {
                    householdDetailsListener.updateHouseholdDetails();
                }
            }

            @Override
            public void onEntityEdited(Member newHeadMember) {

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

}