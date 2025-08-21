package org.philimone.hds.explorer.main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.CollectedDataFragment;
import org.philimone.hds.explorer.fragment.household.details.HouseholdEditFragment;
import org.philimone.hds.explorer.fragment.household.details.HouseholdMembersFragment;
import org.philimone.hds.explorer.fragment.household.details.HouseholdVisitFragment;
import org.philimone.hds.explorer.fragment.household.details.adapter.HouseholdDetailsFragmentAdapter;
import org.philimone.hds.explorer.listeners.HouseholdDetailsListener;
import org.philimone.hds.explorer.main.hdsforms.FormUtilListener;
import org.philimone.hds.explorer.main.hdsforms.HouseholdFormUtil;
import org.philimone.hds.explorer.main.hdsforms.VisitFormUtil;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.Round;
import org.philimone.hds.explorer.model.Round_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.Visit_;
import org.philimone.hds.explorer.model.enums.HouseholdType;
import org.philimone.hds.explorer.model.followup.TrackingSubjectList;
import org.philimone.hds.explorer.settings.RequestCodes;
import org.philimone.hds.explorer.widget.DialogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.StringUtil;

public class HouseholdDetailsActivity extends AppCompatActivity implements HouseholdDetailsListener {

    private enum HouseholdDetailsMode {
        NORMAL_MODE,        /* Default, when Details is normally - can create/open visit*/
        VISIT_MODE,         /* Activated when Create Visit clicked - can finish visit */
        NEW_HOUSEHOLD_MODE, /* Activated on onCreated - comes from other activity - automacally creates visits */
        TRACKING_MODE       /* Activated on onCreated - comes from TrackingList - cant create/open visit */
    };

    private TextView hhDetailsName;
    private TextView hhDetailsCode;
    private TextView hhDetailsHeadName;
    private TextView hhDetailsHeadCode;
    private TextView hhDetailsRegionLabel;
    private TextView hhDetailsRegionValue;
    private TextView hhDetailsVisitDateValue;
    private Button btHouseDetailsCollectData;
    private Button btHouseDetailsCreateVisit;
    private Button btHouseDetailsFinishVisit;
    private Button btHouseDetailsOpenVisit;
    private Button btHouseDetailsBack;
    private ImageView iconView;

    private TabLayout householdDetailsTabLayout;
    private ViewPager2 householdDetailsTabViewPager;

    private RelativeLayout mainPanelTabsLayout;
    private LinearLayout mainPanelVisitLayout;

    private HouseholdVisitFragment householdVisitFragment;

    private HouseholdDetailsFragmentAdapter fragmentAdapter;

    private Region region;
    private Household household;
    private Visit visit;
    private List<FormDataLoader> formDataLoaders = new ArrayList<>();

    private User loggedUser;

    private Box<ApplicationParam> boxAppParams;
    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Round> boxRounds;
    private Box<Visit> boxVisits;
    private Box<CollectedData> boxCollectedData;
    private Box<CoreCollectedData> boxCoreCollectedData;
    private Box<TrackingSubjectList> boxTrackingSubjectList;

    private TrackingSubjectList trackingSubject;
    private Round currentRound;

    private Integer requestCode;

    private HouseholdDetailsMode hdetailsMode;
    private boolean loadNewHousehold = false;
    private Map<String,Object> visitExtraData = new HashMap<>();

    private FormUtilities odkFormUtilities;

    private CollectedData collectedDataToEdit;
    private boolean callOnCollectData;

    private DateUtil dateUtil = Bootstrap.getDateUtil();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.household_details);

        initBoxes();

        readIntentData();

        initialize();
        initModes();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        boolean roundExists = currentRound != null;

        if (!roundExists && this.loadNewHousehold) {
            DialogFactory.createMessageInfo(this, R.string.error_lbl, R.string.round_does_not_exists_lbl).show();
            return;
        }

        //if its the first time - load HForm Household
        if (this.loadNewHousehold==true){
            createNewHousehold();
        }

        this.loadNewHousehold = false; //on other resumes will not load new household

        if (!roundExists){
            DialogFactory.createMessageInfo(this, R.string.info_lbl, R.string.round_household_details_does_not_exists_lbl).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (visit != null) {
            //close visit first
            DialogFactory.createMessageInfo(this, getString(R.string.household_details_visit_panel_lbl), getString(R.string.household_details_visit_not_closed_lbl)).show();
        } else {
            super.onBackPressed();
        }
    }

    private void readIntentData() {

        this.loggedUser = Bootstrap.getCurrentUser();

        try {
            long regionId = getIntent().getExtras().getLong("region");
            this.region = boxRegions.get(regionId);
        } catch (Exception ex){
            Log.d("read-intent-error", "regionId -> " + ex.getMessage());
        }

        try{
            long householdId = getIntent().getExtras().getLong("household");
            this.household = boxHouseholds.get(householdId);
        }catch (Exception ex){
            Log.d("read-intent-error", "householdId -> " + ex.getMessage());
        }

        try{
            this.requestCode = getIntent().getExtras().getInt("request_code");
        }catch (Exception ex){
            Log.d("read-intent-error", "requestCode -> " + ex.getMessage());
        }

        if (getIntent().getExtras().containsKey("tracking_subject_id")) {
            this.trackingSubject = boxTrackingSubjectList.get(getIntent().getExtras().getLong("tracking_subject_id"));
        }

        if (getIntent().getExtras().containsKey("odk-form-edit")) {
            long collectedDataId = getIntent().getExtras().getLong("odk-form-edit");
            this.collectedDataToEdit = boxCollectedData.get(collectedDataId);
        }

        if (getIntent().getExtras().containsKey("odk-form-collect")) {
            this.callOnCollectData = true;
        }
    }

    private void initModes() {
        if (requestCode == null || requestCode == 0) {
            setHouseholdMode();
            return;
        }

        if (requestCode == RequestCodes.HOUSEHOLD_DETAILS_FROM_HFILTER_NEW_HOUSEHOLD) {
            setNewHouseholdMode();
        }

        if (requestCode == RequestCodes.HOUSEHOLD_DETAILS_FROM_TRACKING_LIST_DETAILS) {
            setTrackingMode();
        }
    }

    private void initBoxes() {
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxVisits = ObjectBoxDatabase.get().boxFor(Visit.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxRounds = ObjectBoxDatabase.get().boxFor(Round.class);
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
        this.boxTrackingSubjectList = ObjectBoxDatabase.get().boxFor(TrackingSubjectList.class);
    }

    private void initialize() {

        this.odkFormUtilities = new FormUtilities(this, null);

        currentRound = this.boxRounds.query().order(Round_.roundNumber, QueryBuilder.DESCENDING).build().findFirst();

        hhDetailsName = (TextView) findViewById(R.id.hhDetailsName);
        hhDetailsCode = (TextView) findViewById(R.id.hhDetailsCode);
        hhDetailsHeadName = (TextView) findViewById(R.id.hhDetailsHeadName);
        hhDetailsHeadCode = (TextView) findViewById(R.id.hhDetailsHeadCode);
        hhDetailsRegionLabel = (TextView) findViewById(R.id.hhDetailsRegionLabel);
        hhDetailsRegionValue = (TextView) findViewById(R.id.hhDetailsRegionValue);
        hhDetailsVisitDateValue = findViewById(R.id.hhDetailsVisitDateValue);
        btHouseDetailsCollectData = (Button) findViewById(R.id.btHouseDetailsCollectData);
        btHouseDetailsCreateVisit = findViewById(R.id.btHouseDetailsCreateVisit);
        btHouseDetailsFinishVisit = findViewById(R.id.btHouseDetailsFinishVisit);
        btHouseDetailsOpenVisit = findViewById(R.id.btHouseDetailsOpenVisit);
        btHouseDetailsBack = (Button) findViewById(R.id.btHouseDetailsBack);
        iconView = (ImageView) findViewById(R.id.iconView);
        householdDetailsTabLayout = findViewById(R.id.householdDetailsTabLayout);
        householdDetailsTabViewPager = findViewById(R.id.householdDetailsTabViewPager);
        mainPanelTabsLayout = findViewById(R.id.mainPanelTabsLayout);
        mainPanelVisitLayout = findViewById(R.id.mainPanelVisitLayout);
        //visitTabItem = findViewById(R.id.visitTabItem);

        this.householdVisitFragment = (HouseholdVisitFragment) (getSupportFragmentManager().findFragmentById(R.id.householdVisitFragment));
        this.householdVisitFragment.setHouseholdDetailsListener(this);

        btHouseDetailsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HouseholdDetailsActivity.this.onBackPressed();
            }
        });

        btHouseDetailsCollectData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCollectDataClicked();
            }
        });

        btHouseDetailsCreateVisit.setOnClickListener(v -> {
            onCreateVisitClicked();
        });

        btHouseDetailsFinishVisit.setOnClickListener(v -> {
            onFinishVisitClicked();
        });

        btHouseDetailsOpenVisit.setOnClickListener(v -> {
            onOpenVisitClicked();
        });

    }

    private void initFragments() {
        //this.householdMembersFragment = HouseholdMembersFragment.newInstance(this.household, this.loggedUser);
        //this.collectedDataFragment = CollectedDataFragment.newInstance(this.household, this.loggedUser, this.formDataLoaders);
        //this.householdDatasetsFragment = ExternalDatasetsFragment.newInstance(this.household);

        //List<Fragment> list = new ArrayList<>();
        //list.add(householdMembersFragment);
        //list.add(householdDatasetsFragment);
        //list.add(collectedDataFragment);

        if (household != null && fragmentAdapter == null) {
            List<String> tabTitles = new ArrayList<>();
            tabTitles.add(getString(R.string.household_details_tab_members_list_lbl));
            tabTitles.add(getString(R.string.household_details_tab_datasets_lbl));
            tabTitles.add(getString(R.string.household_details_tab_collected_forms_lbl));
            tabTitles.add(getString(R.string.household_details_tab_edit_lbl));

            boolean isTracking = requestCode == RequestCodes.HOUSEHOLD_DETAILS_FROM_TRACKING_LIST_DETAILS;

            fragmentAdapter = new HouseholdDetailsFragmentAdapter(this.getSupportFragmentManager(), this.getLifecycle(), household, loggedUser, this.trackingSubject, tabTitles);
            fragmentAdapter.setCollectedDataToEdit(collectedDataToEdit);
            fragmentAdapter.setFragmentEditListener(new HouseholdEditFragment.EditListener() {
                @Override
                public void onUpdate() {
                    displayHouseholdDetails();
                }
            });

            fragmentAdapter.setFragmentCollectListener(new CollectedDataFragment.CollectedDataFragmentListener() {
                @Override
                public void afterExternalCallOnCollectDataFinished() {
                    callOnCollectData = false;
                    onBackPressed();
                }

                @Override
                public void afterExternalCallCollectedDataToEditFinished() {
                    collectedDataToEdit = null;
                    onBackPressed();
                }

                @Override
                public void afterInternalCollectDataFinished() {
                    if (hdetailsMode == HouseholdDetailsMode.VISIT_MODE) {
                        householdVisitFragment.loadDataToListViews();
                    }
                }

            });


            householdDetailsTabViewPager.setAdapter(fragmentAdapter);
            //this will create all fragments
            householdDetailsTabViewPager.setOffscreenPageLimit(4);

            new TabLayoutMediator(householdDetailsTabLayout, householdDetailsTabViewPager, (tab, position) -> {
                tab.setText(fragmentAdapter.getTitle(position));
            }).attach();

            if (collectedDataToEdit != null) {
                this.householdDetailsTabLayout.getTabAt(2).select();
            }
        }


    }

    private void reloadFragmentsData(){
        if (fragmentAdapter != null) {
            HouseholdMembersFragment membersFragment = this.fragmentAdapter.getFragmentMembers();
            CollectedDataFragment collectedDataFragment = this.fragmentAdapter.getFragmentCollected();

            if (membersFragment != null) {
                membersFragment.reloadMembers();
            }
            if (collectedDataFragment != null) {
                collectedDataFragment.reloadCollectedData();
            }
        }
    }

    private void initializeButtons() {
        if (this.fragmentAdapter != null) {
            if (this.fragmentAdapter != null && this.fragmentAdapter.getFragmentCollected() != null) {
                boolean hasForms = this.fragmentAdapter.getFragmentCollected().getFormDataLoaders().size() > 0;
                this.btHouseDetailsCollectData.setEnabled(hasForms);
            }
        }
    }

    private void displayHouseholdDetails(){

        hhDetailsName.setText("");
        hhDetailsCode.setText("");
        hhDetailsHeadName.setText("");
        hhDetailsHeadCode.setText("");
        hhDetailsRegionLabel.setText("");
        hhDetailsRegionValue.setText("");
        hhDetailsVisitDateValue.setText("");

        if (household == null) return;

        //reload
        household = this.boxHouseholds.get(household.id);

        Region region = this.boxRegions.query().equal(Region_.code, household.region, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
        Visit lastVisit = this.boxVisits.query().equal(Visit_.householdCode, household.code, QueryBuilder.StringOrder.CASE_SENSITIVE).order(Visit_.visitDate, QueryBuilder.DESCENDING).build().findFirst();
        String hierarchyName = getHierarchyName(region);

        hhDetailsName.setText(household.getName());
        hhDetailsCode.setText(household.getCode());
        hhDetailsHeadName.setText(household.getHeadName());
        hhDetailsHeadCode.setText(household.getHeadCode());
        hhDetailsRegionLabel.setText(hierarchyName+":");
        hhDetailsRegionValue.setText(region==null ? "" : region.getName());
        hhDetailsVisitDateValue.setText(lastVisit==null ? "None" : dateUtil.formatYMD(lastVisit.visitDate));

        if (household.type == HouseholdType.INSTITUTIONAL) {
            iconView.setImageResource(R.mipmap.nui_household_inst_filled_icon);
        } else {
            iconView.setImageResource(R.mipmap.nui_household_filled_icon);
        }

        reloadFragmentsData();
    }

    private String getHierarchyName(Region region){
        if (region == null) return "";

        ApplicationParam param = Queries.getApplicationParamBy(boxAppParams, region.getLevel());

        if (param != null){
            return param.getValue();
        }

        return "";
    }

    private boolean isVisibleForm(Form form){

        boolean isInTrackingListMode = requestCode == RequestCodes.HOUSEHOLD_DETAILS_FROM_TRACKING_LIST_DETAILS;

        if (form.isHouseholdForm()){
            if (form.isFollowUpForm() && !isInTrackingListMode){ //forms flagged with followUpOnly can only be opened using FollowUp Lists, to be able to open via normal surveys remove the flag on the server
                return false; //follow up only will not be visible in other modes
            }

            return true;
        }

        return false;
    }

    private void onCreateVisitClicked() {
        if (!hasRecentlyCreatedVisit() && this.household.recentlyCreated) { //no visit and its a recently created household
            loadNewVisitForm(true);
        } else {
            loadNewVisitForm(false);
        }

    }

    private void onFinishVisitClicked() {
        Log.d("finishingx","visit");

        finishVisit();
    }

    private void onOpenVisitClicked() {
        openPreviousVisit();
    }

    private void onCollectDataClicked(){

        this.householdDetailsTabLayout.getTabAt(2).select();
        //this.householdDetailsTabViewPager.setCurrentItem(2, true);

        //Go to HouseholdFormsFragment and call this action
        if (this.fragmentAdapter != null) {
            CollectedDataFragment collectedDataFragment = this.fragmentAdapter.getFragmentCollected();
            collectedDataFragment.onCollectData();
        }
    }

    private boolean hasRecentlyCreatedVisit() {
        Visit lastVisit = null;

        if (household != null) {
            lastVisit = this.boxVisits.query().equal(Visit_.householdCode, household.code, QueryBuilder.StringOrder.CASE_SENSITIVE).order(Visit_.visitDate, QueryBuilder.DESCENDING).build().findFirst();
        }

        return lastVisit != null && lastVisit.recentlyCreated;
    }

    private void setHouseholdMode(){
        this.hdetailsMode = HouseholdDetailsMode.NORMAL_MODE;
        boolean recentlyCreatedVisit = hasRecentlyCreatedVisit();
        boolean roundExists = currentRound != null;

        btHouseDetailsCreateVisit.setEnabled(roundExists && true);
        btHouseDetailsFinishVisit.setEnabled(false);
        btHouseDetailsOpenVisit.setEnabled(roundExists && recentlyCreatedVisit);

        btHouseDetailsCreateVisit.setVisibility(View.VISIBLE);
        btHouseDetailsFinishVisit.setVisibility(View.GONE);
        btHouseDetailsOpenVisit.setVisibility(recentlyCreatedVisit ? View.VISIBLE : View.GONE);
        btHouseDetailsCollectData.setVisibility(View.VISIBLE);

        mainPanelTabsLayout.setVisibility(View.VISIBLE);
        mainPanelVisitLayout.setVisibility(View.GONE);

        displayHouseholdDetails();
        initFragments();
        initializeButtons();
    }

    private void setVisitMode(){
        this.hdetailsMode = HouseholdDetailsMode.VISIT_MODE;
        boolean roundExists = currentRound != null;

        btHouseDetailsCreateVisit.setEnabled(false);
        btHouseDetailsFinishVisit.setEnabled(roundExists && true);
        btHouseDetailsOpenVisit.setEnabled(false);

        btHouseDetailsCreateVisit.setVisibility(View.GONE);
        btHouseDetailsFinishVisit.setVisibility(View.VISIBLE);
        btHouseDetailsOpenVisit.setVisibility(View.GONE);
        btHouseDetailsCollectData.setVisibility(View.GONE);

        mainPanelTabsLayout.setVisibility(View.GONE);
        mainPanelVisitLayout.setVisibility(View.VISIBLE);

        displayHouseholdDetails();
        initializeButtons();

        //load visit fragment
        Log.d("setvisitmode", "visitFragment="+householdVisitFragment+", household="+household);
        if (this.householdVisitFragment != null) {
            this.householdVisitFragment.load(household, visit, loggedUser, visitExtraData);
        }



        Log.d("visit code", ""+visit.code);
    }

    private void setNewHouseholdMode(){
        this.hdetailsMode = HouseholdDetailsMode.NEW_HOUSEHOLD_MODE;
        this.loadNewHousehold = true;
        boolean roundExists = currentRound != null;

        btHouseDetailsCreateVisit.setEnabled(false);
        btHouseDetailsFinishVisit.setEnabled(false);
        btHouseDetailsOpenVisit.setEnabled(false);

        btHouseDetailsCreateVisit.setVisibility(View.GONE);
        btHouseDetailsFinishVisit.setVisibility(View.GONE);
        btHouseDetailsOpenVisit.setVisibility(View.GONE);
        btHouseDetailsCollectData.setVisibility(View.VISIBLE);

        mainPanelTabsLayout.setVisibility(View.VISIBLE);
        mainPanelVisitLayout.setVisibility(View.GONE);

        initializeButtons();
        displayHouseholdDetails();
    }

    private void setTrackingMode(){
        this.hdetailsMode = HouseholdDetailsMode.NORMAL_MODE;

        btHouseDetailsCreateVisit.setEnabled(false);
        btHouseDetailsFinishVisit.setEnabled(false);
        btHouseDetailsOpenVisit.setEnabled(false);

        btHouseDetailsCreateVisit.setVisibility(View.GONE);
        btHouseDetailsFinishVisit.setVisibility(View.GONE);
        btHouseDetailsOpenVisit.setVisibility(View.GONE);
        btHouseDetailsCollectData.setVisibility(View.VISIBLE);

        mainPanelTabsLayout.setVisibility(View.VISIBLE);
        mainPanelVisitLayout.setVisibility(View.GONE);

        displayHouseholdDetails();
        initFragments();
        initializeButtons();
    }

    private void createNewHousehold(){
        //load new Household HDS-Form and save it
        if (this.region != null) {
            onCreateNewHousehold();
        } else {
            DialogFactory.createMessageInfo(this, "Household Enumeration", "Cant create new Household because there is no region selected");
        }
    }

    private void finishVisit() {

        //finsih the visit in visit fragment
        if (householdVisitFragment != null)  {

            //Check if there is non collected extension forms
            ExtensionCollectedValidationResult validationResult1 = validateExtensionCollected();
            if (validationResult1.foundExtensionNotCollected) {

                DialogFactory dialog = DialogFactory.createMessageInfo(this, R.string.info_lbl,validationResult1.messageText);
                dialog.setDialogMessageAsHtml(true);
                dialog.show();

                //dont finalize visit
                return;
            }

            //if is a institutional household - just close the visit - no need to check unvisited members
            if (this.household.type == HouseholdType.INSTITUTIONAL) {
                closeVisit();
                return;
            }

            //Check if there is non visited members
            NoVisitedValidationResult validationResult2 = validateNonVisitedMembers();
            if (validationResult2.foundNotVisited) {

                DialogFactory.createMessageYN(this, getString(R.string.household_visit_not_finished_title_lbl), getString(R.string.household_visit_not_finished_msg_ask_lbl, validationResult2.nonVisitedAsText), new DialogFactory.OnYesNoClickListener() {
                    @Override
                    public void onYesClicked() {
                        markAllAsNonVisited(visit, validationResult2.nonVisitedCodesList, validationResult2.nonVisitedAsText);
                        closeVisit();
                    }

                    @Override
                    public void onNoClicked() { /* just do nothing - this will not close the visit */ }
                }).show();

            } else {
                //closing the visit - if all members were visited
                closeVisit();
            }
        }
    }

    private void closeVisit(){
        //close visit - update endtimestamp
        VisitFormUtil.updateEndTimestamp(this, this.visit.getRecentlyCreatedUri());

        //finish visit mode
        this.visit = null;
        if (this.fragmentAdapter != null && this.fragmentAdapter.getFragmentCollected() != null) {
            this.fragmentAdapter.getFragmentCollected().setVisit(null);
        }
        setHouseholdMode();
    }

    private ExtensionCollectedValidationResult validateExtensionCollected(){

        //check if there is unfinalized form extensions
        List<CoreCollectedData> unfinalizedList = householdVisitFragment.getUnfinalizedExtensionForms();
        if (unfinalizedList.size() > 0) {
            String unlist = "";
            for (CoreCollectedData cd : unfinalizedList) {
                unlist += (unlist.isEmpty() ? "" : "\n") + "<li>&nbsp;&nbsp;<b>" + getString(cd.formEntity.name) + " ("+ cd.formEntityCode +")</b></li>";
            }

            String msg = getString(R.string.household_visit_unfinalized_msg_info_lbl, unlist);

            return new ExtensionCollectedValidationResult(true, msg);
        }

        return new ExtensionCollectedValidationResult(false, null);
    }

    private NoVisitedValidationResult validateNonVisitedMembers() {
        List<Member> notVisited = householdVisitFragment.getNonVisitedMembers();
        List<Member> toremove = new ArrayList<>();
        String nonVisitedAsText = "";
        List<String> nonVisitedCodesList = new ArrayList<>();

        for (Member member : notVisited) {
            nonVisitedAsText += nonVisitedAsText.isEmpty() ? member.code : ", "+member.code;
            nonVisitedCodesList.add(member.code);

            if (visit != null && visit.nonVisitedMembers != null) {
                if (visit.nonVisitedMembers.contains(member.code)) {
                    toremove.add(member);
                }
            }
        }

        //remove all marked as non visited
        notVisited.removeAll(toremove);

        //Not all of the individuals were visited
        if (notVisited.size()>0) {
            return new NoVisitedValidationResult(true, nonVisitedCodesList, nonVisitedAsText);
        }

        return new NoVisitedValidationResult(false, null, null);
    }

    private void markAllAsNonVisited(Visit visit, List<String> nonVisitedMembersCodeList, String nonVisitedMembersAsText) {
        if (visit != null) {
            visit.nonVisitedMembers.addAll(nonVisitedMembersCodeList);
            this.boxVisits.put(visit);

            VisitFormUtil.markAllAsNonVisited(this, this.visit.getRecentlyCreatedUri(), nonVisitedMembersAsText);
        }
    }

    private void openPreviousVisit() {
        //get the recently created visit
        Visit lastVisit = this.boxVisits.query().equal(Visit_.householdCode, household.code, QueryBuilder.StringOrder.CASE_SENSITIVE).orderDesc(Visit_.visitDate).orderDesc(Visit_.code).build().findFirst();

        if (lastVisit != null && lastVisit.recentlyCreated) {

            VisitFormUtil visitFormUtil = new VisitFormUtil(this, this, this.household, lastVisit, this.odkFormUtilities, new FormUtilListener<Visit>() {
                @Override
                public void onNewEntityCreated(Visit entity, Map<String, Object> data) { }

                @Override
                public void onEntityEdited(Visit entity, Map<String, Object> data) {
                    HouseholdDetailsActivity.this.visit = entity;
                    if (entity.visitPossible) {
                        setVisitMode();
                    } else {
                        setHouseholdMode();
                        HouseholdDetailsActivity.this.visit = null;
                    }
                }

                @Override
                public void onFormCancelled() {
                    HouseholdDetailsActivity.this.visit = null;
                }
            });

            visitFormUtil.collect();
        }
    }

    /* Household Form */
    private void onCreateNewHousehold(){

        //check if this household is pre-registered
        if (this.household != null && this.household.preRegistered == true) {

            //Try to complete Household Registration
            DialogFactory.createMessageYN(this, R.string.household_details_complete_reg_title_lbl, R.string.household_details_complete_reg_msg_lbl, new DialogFactory.OnYesNoClickListener() {
                @Override
                public void onYesClicked() {
                    completeHouseholdRegistration();
                }

                @Override
                public void onNoClicked() {
                    finish(); //close this activity
                }
            }).show();

        } else {
            loadNewHouseholdForm();
        }

    }

    private void loadNewHouseholdForm() {
        HouseholdFormUtil householdForm = new HouseholdFormUtil(this, this, this.region, this.odkFormUtilities, new FormUtilListener<Household>() {
            @Override
            public void onNewEntityCreated(Household household, Map<String, Object> data) {
                HouseholdDetailsActivity.this.household = household;
                displayHouseholdDetails();
                loadNewVisitForm(true);
            }

            @Override
            public void onEntityEdited(Household household, Map<String, Object> data) {

            }

            @Override
            public void onFormCancelled() {
                HouseholdDetailsActivity.this.finish();
            }
        });
        householdForm.collect();
    }

    private void completeHouseholdRegistration() {
        HouseholdFormUtil householdFormUtil = HouseholdFormUtil.completeRegistration(this, this, this.region, this.household, this.odkFormUtilities, new FormUtilListener<Household>() {
            @Override
            public void onNewEntityCreated(Household entity, Map<String, Object> data) {
                HouseholdDetailsActivity.this.household = household;
                loadNewVisitForm(true);
            }

            @Override
            public void onEntityEdited(Household entity, Map<String, Object> data) {

            }

            @Override
            public void onFormCancelled() {
                finish();
            }
        });
        householdFormUtil.collect();
    }

    /* Visit Form */
    private void loadNewVisitForm(boolean newHouseholdCreated){

        VisitFormUtil visitFormUtil = new VisitFormUtil(this, this, this.household, newHouseholdCreated, this.odkFormUtilities, new FormUtilListener<Visit>() {
            @Override
            public void onNewEntityCreated(Visit entity, Map<String, Object> data) {
                HouseholdDetailsActivity.this.visit = entity;
                visitExtraData.putAll(data);

                if (entity.visitPossible) {
                    setVisitMode();
                } else {
                    setHouseholdMode();
                    HouseholdDetailsActivity.this.visit = null;
                }
            }

            @Override
            public void onEntityEdited(Visit entity, Map<String, Object> data) {

            }

            @Override
            public void onFormCancelled() {
                HouseholdDetailsActivity.this.visit = null;

                if (newHouseholdCreated) {
                    //delete the Household
                    setHouseholdMode();
                }
            }
        });

        visitFormUtil.collect();
    }

    @Override
    public void updateHouseholdDetails() {
        displayHouseholdDetails();
    }

    @Override
    public void onVisitCollectData(Visit visit) {
        if (fragmentAdapter != null) {
            CollectedDataFragment collectedDataFragment = this.fragmentAdapter.getFragmentCollected();
            collectedDataFragment.setVisit(visit);

            collectedDataFragment.onCollectData();
        }
    }

    @Override
    public void onVisitEditData(Visit visit, CollectedData collectedData) {
        if (fragmentAdapter != null) {
            CollectedDataFragment collectedDataFragment = this.fragmentAdapter.getFragmentCollected();
            collectedDataFragment.setVisit(visit);
            collectedDataFragment.setInternalCollectedDataToEdit(collectedData);

            collectedDataFragment.onEditCollectedData(collectedData);
        }
    }

    //usefull classes

    class ExtensionCollectedValidationResult {
        public boolean foundExtensionNotCollected;
        public String messageText;

        public ExtensionCollectedValidationResult(boolean foundExtensionNotCollected, String messageText) {
            this.foundExtensionNotCollected = foundExtensionNotCollected;
            this.messageText = messageText;
        }
    }

    class NoVisitedValidationResult {
        public boolean foundNotVisited;
        public List<String> nonVisitedCodesList;
        public String nonVisitedAsText = "";

        public NoVisitedValidationResult(boolean foundNotVisited, List<String> nonVisitedCodesList, String nonVisitedAsText) {
            this.foundNotVisited = foundNotVisited;
            this.nonVisitedCodesList = nonVisitedCodesList;
            this.nonVisitedAsText = nonVisitedAsText;
        }
    }
}
