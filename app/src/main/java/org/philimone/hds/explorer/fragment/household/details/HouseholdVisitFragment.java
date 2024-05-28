package org.philimone.hds.explorer.fragment.household.details;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.utilities.GeneralUtil;

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
import org.philimone.hds.explorer.adapter.model.VisitCollectedDataItem;
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
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.CoreEntity;
import org.philimone.hds.explorer.model.CoreFormExtension;
import org.philimone.hds.explorer.model.Death;
import org.philimone.hds.explorer.model.Death_;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Form_;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.HeadRelationship_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.IncompleteVisit;
import org.philimone.hds.explorer.model.IncompleteVisit_;
import org.philimone.hds.explorer.model.Inmigration;
import org.philimone.hds.explorer.model.Inmigration_;
import org.philimone.hds.explorer.model.MaritalRelationship;
import org.philimone.hds.explorer.model.MaritalRelationship_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Outmigration;
import org.philimone.hds.explorer.model.Outmigration_;
import org.philimone.hds.explorer.model.PregnancyOutcome;
import org.philimone.hds.explorer.model.PregnancyOutcome_;
import org.philimone.hds.explorer.model.PregnancyRegistration;
import org.philimone.hds.explorer.model.PregnancyRegistration_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.Visit_;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.Gender;
import org.philimone.hds.explorer.model.enums.PregnancyStatus;
import org.philimone.hds.explorer.model.enums.SubjectEntity;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.LoadingDialog;
import org.philimone.hds.explorer.widget.RecyclerListView;
import org.philimone.hds.forms.model.HForm;

import java.util.ArrayList;
import java.util.Date;
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

    private Box<User> boxUsers;
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
    private Box<ApplicationParam> boxAppParams;
    private Box<Form> boxForms;

    private HouseholdDetailsListener householdDetailsListener;

    private FormUtilities odkFormUtilities;
    
    private VisitEventsMode currentEventMode = VisitEventsMode.HOUSEHOLD_EVENTS;

    private boolean respondentNotRegistered = false;
    private int minimunMotherAge;
    private int minimunSpouseAge;

    private enum VisitEventsMode { HOUSEHOLD_EVENTS, MEMBER_EVENTS, RESPONDENT_NOT_REG_EVENTS}

    private ActivityResultLauncher<Intent> onMemberDetailsExtraFormCollectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        loadDataToListViews();
    });

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

        //check if there is unfinalized form extensions
        List<CoreCollectedData> unfinalizedList = getUnfinalizedExtensionForms();
        if (unfinalizedList.size() > 0) {
            String unlist = "";
            for (CoreCollectedData cd : unfinalizedList) {
                unlist += (unlist.isEmpty() ? "" : "\n") + "<li>&nbsp;&nbsp;<b>" + getString(cd.formEntity.name) + " ("+ cd.formEntityCode +")</b></li>";
            }

            String msg = getString(R.string.household_visit_unfinalized_msg_info_lbl, unlist);

            DialogFactory dialog = DialogFactory.createMessageInfo(getContext(), R.string.info_lbl, msg);
            dialog.setDialogMessageAsHtml(true);
            dialog.show();
        }
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
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        this.boxUsers = ObjectBoxDatabase.get().boxFor(User.class);
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
                VisitCollectedDataItem dataItem = (VisitCollectedDataItem) adapter.getChild(groupPosition, childPosition);

                if (dataItem.isCoreCollectedData()) {
                    onVisitCoreCollectedChildClicked(dataItem.getCoreCollectedData());
                } else if (dataItem.isOdkCollectedData()) {
                    onVisitOdkCollectedChildClicked(dataItem.getOdkCollectedData());
                }

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
                    VisitCollectedDataItem dataItem = (VisitCollectedDataItem) adapter.getChild(groupPosition, childPosition);

                    if (dataItem.isCoreCollectedData()) {
                        onVisitCollectedChildLongClicked(dataItem);
                    } else {
                        //do nothing
                    }

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

        this.minimunMotherAge = retrieveMinimumMotherAge();
        this.minimunSpouseAge = retrieveMinimumSpouseAge();

    }

    private int retrieveMinimumMotherAge() {
        ApplicationParam param = this.boxAppParams.query().equal(ApplicationParam_.name, ApplicationParam.PARAMS_MIN_AGE_OF_MOTHER, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (param != null) {
            try {
                return Integer.parseInt(param.value);
            } catch (Exception ex) {

            }
        }

        return 12;
    }

    private int retrieveMinimumSpouseAge() {
        ApplicationParam param = this.boxAppParams.query().equal(ApplicationParam_.name, ApplicationParam.PARAMS_MIN_AGE_OF_SPOUSE, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (param != null) {
            try {
                return Integer.parseInt(param.value);
            } catch (Exception ex) {

            }
        }

        return 12;
    }

    private List<CoreCollectedData> getUnfinalizedExtensionForms() {
        List<CoreCollectedData> list = new ArrayList<>();

        CoreCollectedExpandableAdapter adapter = (CoreCollectedExpandableAdapter) this.elvVisitCollected.getExpandableListAdapter();

        for (List<VisitCollectedDataItem> items : adapter.getChildItems()) {
            for (VisitCollectedDataItem dataItem : items) {
                if (dataItem != null) {
                    CoreFormExtension extension = dataItem.getExtension();
                    if (extension != null && extension.enabled && dataItem.odkFormStatus != FormUtilities.FormStatus.FINALIZED) { //if is not finalized or not found
                        list.add(dataItem.getCoreCollectedData());
                    }
                }
            }
        }

        return list;
    }

    private void onCollectExtraFormClicked() {

        OnExtraFormCollectTask task = new OnExtraFormCollectTask(selectedMember, household, visit);
        task.execute();

        showLoadingDialog(getString(R.string.loading_dialog_extra_forms_load_lbl), true);
    }

    private void selectMember(Member member){

        int index = getMembersAdapter().indexOf(member);
        Log.d("new-members", "size="+getMembersAdapter().getItemCount()+", index="+index);
        onMembersClick(index);
    }

    private void onMembersClick(int position) {
        //select one member and highlight

        MemberAdapter adapter = getMembersAdapter();


        this.selectedMember = adapter.getItem(position);

        adapter.setSelectedIndex(position);
        this.lvHouseholdMembers.scrollToPosition(position);

        setMemberMode();

        btClearMember.setVisibility(View.VISIBLE);
    }

    private void onMembersLongClick(int position) {
        //select one and show MemberDetails without Highlight
        MemberAdapter adapter = getMembersAdapter();
        if (adapter != null) {
            Member member = adapter.getItem(position);

            MemberSelectedTask task = new MemberSelectedTask(member, household, this.visit, false);
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

        this.btnVisitExtInmigration.setEnabled(true); // && !isCensusHousehold
        this.btnVisitIntInmigration.setEnabled(true); // && !isCensusHousehold
        this.btnVisitChangeHead.setEnabled(true && !isCensusHousehold);

        this.btnVisitOutmigration.setEnabled(false);
        this.btnVisitDeath.setEnabled(false);
        this.btnVisitMaritalRelationship.setEnabled(false);
        this.btnVisitExtraForm.setEnabled(true);

        this.btnVisitPregnancyReg.setVisibility(View.GONE);
        this.btnVisitChangeHead.setVisibility(View.VISIBLE);

        this.btnVisitMemberIncomplete.setEnabled(false);
        this.btnVisitMemberEnu.setVisibility(View.VISIBLE);
        this.btnVisitMemberIncomplete.setVisibility(View.GONE);

        //disable buttons if already collected and ready to edit
        //ChangeHead - must be collected one per visit
        CoreCollectedData ccdataChangeHead = this.boxCoreCollectedData.query(CoreCollectedData_.visitId.equal(visit.id).and(CoreCollectedData_.formEntity.equal(CoreFormEntity.CHANGE_HOUSEHOLD_HEAD.code))).build().findFirst();
        btnVisitChangeHead.setEnabled(ccdataChangeHead == null && !isCensusHousehold);

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
        this.btnVisitExtInmigration.setEnabled(true); // && !isCensusHousehold
        this.btnVisitIntInmigration.setEnabled(true); // && !isCensusHousehold
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
        int age = GeneralUtil.getAge(this.selectedMember.dob, new Date());
        boolean isAtMotherAge = age >= this.minimunMotherAge;
        boolean isAtSpouseAge = age >= this.minimunSpouseAge;

        this.btnVisitMemberEnu.setEnabled(false);
        this.btnVisitBirthReg.setEnabled(this.selectedMember!=null && this.selectedMember.gender== Gender.FEMALE && isAtMotherAge);
        this.btnVisitPregnancyReg.setEnabled(this.selectedMember!=null && this.selectedMember.gender== Gender.FEMALE && isAtMotherAge);
        this.btnVisitExtInmigration.setEnabled(false);
        this.btnVisitIntInmigration.setEnabled(false);
        this.btnVisitOutmigration.setEnabled(true); // && !isCensusHousehold
        this.btnVisitDeath.setEnabled(true); // && !isCensusHousehold
        this.btnVisitMaritalRelationship.setEnabled(true && isAtSpouseAge);
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
        boolean isPregnant = false;

        if (selectedMember.gender == Gender.FEMALE && isAtMotherAge) {
            isPregnant = this.boxPregnancyRegistrations.query(PregnancyRegistration_.motherCode.equal(selectedMember.code).and(PregnancyRegistration_.status.equal(PregnancyStatus.PREGNANT.code))).build().count()>0;
           Log.d("pregnat", ""+isPregnant);
        }

        btnVisitMemberIncomplete.setEnabled(btnVisitMemberIncomplete.isEnabled() && ccdataIncomplete == null);
        btnVisitMaritalRelationship.setEnabled(btnVisitMaritalRelationship.isEnabled()); // && hasMaritalRelationship == false);
        btnVisitPregnancyReg.setEnabled(btnVisitPregnancyReg.isEnabled() && ccdataPregnancy == null && !isPregnant);
        btnVisitBirthReg.setEnabled((btnVisitBirthReg.isEnabled() && ccdataPOutcome == null));

        if (isPregnant){
            btnVisitPregnancyReg.setEnabled(false);
        }

        setMainListsSelectable(true);
    }

    private void loadMembersToList() {

        btClearMember.setVisibility(View.GONE);

        if (household == null) return;

        List<Member> members = this.boxMembers.query().equal(Member_.householdCode, household.getCode(), QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                      .equal(Member_.endType, ResidencyEndType.NOT_APPLICABLE.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                      .build().find();

        MemberAdapter adapter = new MemberAdapter(this.getContext(), R.layout.household_visit_member_item, members);
        adapter.setShowExtraDetails(true);
        adapter.setShowMemberDetails(true);
        this.lvHouseholdMembers.setAdapter(adapter);
    }

    private void loadCollectedEventsToList() {

        if (visit == null) return;

        //Type: Member Enumeration (3) -> CoreCollectedData as subitem
        //layouts: core_form_collected_item,

        List<CoreCollectedData> coreList = this.boxCoreCollectedData.query().equal(CoreCollectedData_.visitId, this.visit.id).order(CoreCollectedData_.createdDate).build().find();
        List<CollectedData> extraList = this.boxCollectedData.query().equal(CollectedData_.visitId, this.visit.id).order(CollectedData_.formLastUpdatedDate).build().find();

        Log.d("extraList", ""+extraList.size());

        LinkedHashMap<CoreFormEntity, List<VisitCollectedDataItem>> mapData = new LinkedHashMap<>();
        
        mapData.put(CoreFormEntity.HOUSEHOLD, new ArrayList<VisitCollectedDataItem>());
        mapData.put(CoreFormEntity.MEMBER_ENU, new ArrayList<VisitCollectedDataItem>());
        //mapData.put(CoreFormEntity.HEAD_RELATIONSHIP, new ArrayList<VisitCollectedDataItem>());
        mapData.put(CoreFormEntity.MARITAL_RELATIONSHIP, new ArrayList<VisitCollectedDataItem>());
        mapData.put(CoreFormEntity.INMIGRATION, new ArrayList<VisitCollectedDataItem>());
        mapData.put(CoreFormEntity.EXTERNAL_INMIGRATION, new ArrayList<VisitCollectedDataItem>());
        mapData.put(CoreFormEntity.OUTMIGRATION, new ArrayList<VisitCollectedDataItem>());
        mapData.put(CoreFormEntity.PREGNANCY_REGISTRATION, new ArrayList<VisitCollectedDataItem>());
        mapData.put(CoreFormEntity.PREGNANCY_OUTCOME, new ArrayList<VisitCollectedDataItem>());
        mapData.put(CoreFormEntity.DEATH, new ArrayList<VisitCollectedDataItem>());
        mapData.put(CoreFormEntity.CHANGE_HOUSEHOLD_HEAD, new ArrayList<VisitCollectedDataItem>());
        mapData.put(CoreFormEntity.INCOMPLETE_VISIT, new ArrayList<VisitCollectedDataItem>());
        mapData.put(CoreFormEntity.VISIT, new ArrayList<VisitCollectedDataItem>());

        mapData.put(CoreFormEntity.EXTRA_FORM, new ArrayList<VisitCollectedDataItem>());

        //group the coreList in Map
        for(CoreCollectedData collectedData : coreList) {
            FormUtilities.FormStatus collectedDataStatus = odkFormUtilities.isFormFinalized(collectedData.extensionCollectedUri);
            mapData.get(collectedData.formEntity).add(new VisitCollectedDataItem(collectedData, collectedDataStatus));
        }
        //group the odkList in Map - EXTRA FORMS
        for(CollectedData collectedData : extraList) {
            Form form = boxForms.query().equal(Form_.formId, collectedData.formId, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
            mapData.get(CoreFormEntity.EXTRA_FORM).add(new VisitCollectedDataItem(collectedData, form));
        }

        CoreCollectedExpandableAdapter adapter = new CoreCollectedExpandableAdapter(this.getContext(), mapData);
        this.elvVisitCollected.setAdapter(adapter);
    }

    public void loadDataToListViews(){
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

    private void onVisitCoreCollectedChildClicked(CoreCollectedData coreCollectedData) {
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

    private void onVisitOdkCollectedChildClicked(CollectedData collectedData) {
        //reopen odk form
        OnExtraFormEditTask task = new OnExtraFormEditTask(collectedData);
        task.execute();

        showLoadingDialog(getString(R.string.loading_dialog_extra_forms_load_lbl), true);
    }

    private void onVisitCollectedChildLongClicked(VisitCollectedDataItem dataItem) {
        CoreCollectedData coreCollectedData = dataItem.getCoreCollectedData();
        CoreFormExtension extension = coreCollectedData != null ? coreCollectedData.extension.getTarget() : null;
        //1. if extension required && not_collected || not_found -> create new extension
        //2. if extension required && exists

        if (coreCollectedData != null) {
            if (dataItem.isExtensionCollected() && dataItem.odkFormStatus != FormUtilities.FormStatus.NOT_FOUND) {
                //The form exists - so lets edit it
                Log.d("edit core odk", coreCollectedData.formEntity.code+"");
                CollectedData odkCollectedData = boxCollectedData.query(CollectedData_.collectedId.equal(coreCollectedData.collectedId).and(CollectedData_.formUri.equal(coreCollectedData.extensionCollectedUri))).build().findFirst();
                HForm hform = FormUtil.getHFormBy(getContext(), coreCollectedData.formEntity);
                CoreEntity existingEntity = getRecordEntity(coreCollectedData);

                EditCoreExtensionFormUtil formUtil = new EditCoreExtensionFormUtil(this, this.getContext(), hform, coreCollectedData, existingEntity, this.odkFormUtilities, () -> {
                    loadDataToListViews();
                    updateHouseholdDetails();
                });

                formUtil.editExtensionForm(odkCollectedData);

            } else if (extension.required){
                //The form doesnt exists but its required - lets create new form

                HForm hform = FormUtil.getHFormBy(getContext(), coreCollectedData.formEntity);
                CoreEntity existingEntity = getRecordEntity(coreCollectedData);

                Log.d("create new extension", coreCollectedData.formEntity+", extension=" + extension.extFormId +", entity="+existingEntity);

                EditCoreExtensionFormUtil formUtil = new EditCoreExtensionFormUtil(this, this.getContext(), hform, coreCollectedData, existingEntity, this.odkFormUtilities, () -> {
                    loadDataToListViews();
                    updateHouseholdDetails();
                });

                formUtil.reCollectExtensionForm();

            }
        }


        //edit the odk form
        if (coreCollectedData != null && coreCollectedData.extensionCollected) {
            ///edit


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
                setHouseholdMode();
            }

            @Override
            public void onEntityEdited(Outmigration outmigration, Map<String, Object> data) {
                loadDataToListViews();
                updateHouseholdDetails();
                setHouseholdMode();
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
                setHouseholdMode();
            }

            @Override
            public void onEntityEdited(Death death, Map<String, Object> data) {
                loadDataToListViews();
                updateHouseholdDetails();
                setHouseholdMode();
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

    CoreEntity getRecordEntity(CoreCollectedData coreCollectedData) {
        CoreEntity entity = null;

        switch (coreCollectedData.formEntity) {
            case EDITED_REGION:
            case REGION: entity = boxRegions.query(Region_.id.equal(coreCollectedData.formEntityId)).build().findFirst(); break;
            case PRE_HOUSEHOLD:
            case EDITED_HOUSEHOLD:
            case HOUSEHOLD: entity = boxHouseholds.query(Household_.id.equal(coreCollectedData.formEntityId)).build().findFirst(); break;
            case EDITED_MEMBER:
            case MEMBER_ENU: entity = boxMembers.query(Member_.id.equal(coreCollectedData.formEntityId)).build().findFirst(); break;
            case HEAD_RELATIONSHIP:
            case CHANGE_HOUSEHOLD_HEAD: entity = boxHeadRelationships.query(HeadRelationship_.id.equal(coreCollectedData.formEntityId)).build().findFirst(); break;
            case MARITAL_RELATIONSHIP: entity = boxMaritalRelationships.query(MaritalRelationship_.id.equal(coreCollectedData.formEntityId)).build().findFirst(); break;
            case INMIGRATION:
            case EXTERNAL_INMIGRATION: entity = boxInmigrations.query(Inmigration_.id.equal(coreCollectedData.formEntityId)).build().findFirst(); break;
            case OUTMIGRATION: entity = boxOutmigrations.query(Outmigration_.id.equal(coreCollectedData.formEntityId)).build().findFirst(); break;
            case PREGNANCY_REGISTRATION: entity = boxPregnancyRegistrations.query(PregnancyRegistration_.id.equal(coreCollectedData.formEntityId)).build().findFirst(); break;
            case PREGNANCY_OUTCOME: entity = boxPregnancyOutcomes.query(PregnancyOutcome_.id.equal(coreCollectedData.formEntityId)).build().findFirst(); break;
            case DEATH: entity = boxDeaths.query(Death_.id.equal(coreCollectedData.formEntityId)).build().findFirst(); break;
            case INCOMPLETE_VISIT: entity = boxIncompleteVisits.query(IncompleteVisit_.id.equal(coreCollectedData.formEntityId)).build().findFirst(); break;
            case VISIT: entity = boxVisits.query(Visit_.id.equal(coreCollectedData.formEntityId)).build().findFirst(); break;
            case EXTRA_FORM: break;
            case INVALID_ENUM: break;
        }

        return entity;
    }

    private void showLoadingDialog(String msg, boolean show){
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.dismiss();
        }
    }

    class MemberSelectedTask extends AsyncTask<Void, Void, Void> {
        private Household household;
        private Member member;
        private Visit visit;
        private boolean clickCollectData;

        public MemberSelectedTask(Member member, Household household, Visit visit, boolean clickCollectData) {
            this.household = household;
            this.member = member;
            this.visit = visit;
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
            intent.putExtra("visit", this.visit.id);

            if (clickCollectData) {
                intent.putExtra("odk-form-collect", "true");
            }

            showLoadingDialog(null, false);

            startActivity(intent);
        }
    }

    class OnExtraFormCollectTask extends AsyncTask<Void, Void, Void> {
        private Household household;
        private Member member;
        private Visit visit;

        public OnExtraFormCollectTask(Member member, Household household, Visit visit) {
            this.household = household;
            this.member = member;
            this.visit = visit;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            showLoadingDialog(null, false);

            if (this.member != null) {
                //execute member based forms
                Intent intent = new Intent(getActivity(), MemberDetailsActivity.class);
                intent.putExtra("household", this.household.id);
                intent.putExtra("member", this.member.id);
                intent.putExtra("visit", this.visit.id);
                intent.putExtra("odk-form-collect", "true");

                onMemberDetailsExtraFormCollectLauncher.launch(intent);
            } else {
                //execute household based forms
                householdDetailsListener.onVisitCollectData(this.visit);
            }

        }
    }

    class OnExtraFormEditTask extends AsyncTask<Void, Void, Void> {
        private Household household;
        private Member member;
        private Visit visit;
        private CollectedData collectedData;

        public OnExtraFormEditTask(CollectedData collectedDataToEdit) {
            this.collectedData = collectedDataToEdit;
            this.household = HouseholdVisitFragment.this.household;
            this.visit = HouseholdVisitFragment.this.visit;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (collectedData.recordEntity == SubjectEntity.MEMBER) {
                this.member = boxMembers.get(collectedData.recordId);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            showLoadingDialog(null, false);

            if (this.member != null) {
                //execute member based forms
                Intent intent = new Intent(getActivity(), MemberDetailsActivity.class);
                intent.putExtra("household", this.household.id);
                intent.putExtra("member", this.member.id);
                intent.putExtra("visit", this.visit.id);
                intent.putExtra("odk-form-edit", this.collectedData.id);

                onMemberDetailsExtraFormCollectLauncher.launch(intent);
            } else {
                //execute household based forms
                householdDetailsListener.onVisitEditData(this.visit, collectedData);
            }

        }
    }

}