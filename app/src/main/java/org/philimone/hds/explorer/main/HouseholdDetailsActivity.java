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
import org.philimone.hds.explorer.adapter.model.TrackingSubjectItem;
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
    private Box<CoreCollectedData> boxCoreCollectedData;

    private Round currentRound;

    private Integer requestCode;

    private HouseholdDetailsMode hdetailsMode;
    private boolean loadNewHousehold = false;
    private Map<String,Object> visitExtraData = new HashMap<>();

    private FormUtilities odkFormUtilities;

    private CollectedData autoHighlightCollectedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.household_details);

        readIntentData();

        initBoxes();
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
            this.region = (Region) getIntent().getExtras().get("region");
        } catch (Exception ex){
            ex.printStackTrace();
        }

        try{
            this.household = (Household) getIntent().getExtras().get("household");
        }catch (Exception ex){
            ex.printStackTrace();
        }

        try{
            this.requestCode = getIntent().getExtras().getInt("request_code");
        }catch (Exception ex){
            ex.printStackTrace();
        }

        try {
            readFormDataLoader();
        } catch (Exception ex){
            Log.d("dataloaders", "failed to read them - "+ex.getMessage());
            //ex.printStackTrace();
        }

        if (getIntent().getExtras().containsKey("odk-form-select")) {
            this.autoHighlightCollectedData = (CollectedData) getIntent().getExtras().get("odk-form-select");
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
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
    }

    private void readFormDataLoader(){

        if (!getIntent().getExtras().containsKey("dataloaders")){
            return;
        }

        Object[] objs = (Object[]) getIntent().getExtras().get("dataloaders");

        for (int i=0; i < objs.length; i++){
            FormDataLoader formDataLoader = (FormDataLoader) objs[i];
            //Log.d("tag", ""+formDataLoader.getForm().getFormId());
            if (isVisibleForm(formDataLoader.getForm())){
                this.formDataLoaders.add(formDataLoader);
            }
        }
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

            fragmentAdapter = new HouseholdDetailsFragmentAdapter(this.getSupportFragmentManager(), this.getLifecycle(), household, loggedUser, isTracking ? formDataLoaders : null, tabTitles);
            fragmentAdapter.setAutoHighlightCollectedData(autoHighlightCollectedData);
            fragmentAdapter.setFragmentEditListener(new HouseholdEditFragment.EditListener() {
                @Override
                public void onUpdate() {
                    displayHouseholdDetails();
                }
            });


            householdDetailsTabViewPager.setAdapter(fragmentAdapter);
            //this will create all fragments
            householdDetailsTabViewPager.setOffscreenPageLimit(4);

            new TabLayoutMediator(householdDetailsTabLayout, householdDetailsTabViewPager, (tab, position) -> {
                tab.setText(fragmentAdapter.getTitle(position));
            }).attach();

            if (autoHighlightCollectedData != null) {
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
        boolean hasForms = this.fragmentAdapter.getFragmentCollected().getFormDataLoaders().size()>0;;
        this.btHouseDetailsCollectData.setEnabled(hasForms);
    }

    private void displayHouseholdDetails(){

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
        hhDetailsVisitDateValue.setText(lastVisit==null ? "None" : StringUtil.formatYMD(lastVisit.visitDate));
        //if (this.householdMembersFragment == null) {
        //    this.householdMembersFragment.updateHouseholdMembers();
        //}

        //if (this.householdFormsFragment == null) {
        //    this.householdFormsFragment.showCollectedData();
        //}

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
        loadNewVisitForm(false);
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
        CollectedDataFragment collectedDataFragment = this.fragmentAdapter.getFragmentCollected();
        collectedDataFragment.onCollectData();
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

        mainPanelTabsLayout.setVisibility(View.GONE);
        mainPanelVisitLayout.setVisibility(View.VISIBLE);

        displayHouseholdDetails();

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
        btHouseDetailsFinishVisit.setEnabled(roundExists && true);
        btHouseDetailsOpenVisit.setEnabled(false);

        btHouseDetailsCreateVisit.setVisibility(View.GONE);
        btHouseDetailsFinishVisit.setVisibility(View.VISIBLE);
        btHouseDetailsOpenVisit.setVisibility(View.GONE);

        mainPanelTabsLayout.setVisibility(View.GONE);
        mainPanelVisitLayout.setVisibility(View.VISIBLE);
    }

    private void setTrackingMode(){
        this.hdetailsMode = HouseholdDetailsMode.NORMAL_MODE;

        btHouseDetailsCreateVisit.setEnabled(false);
        btHouseDetailsFinishVisit.setEnabled(false);
        btHouseDetailsOpenVisit.setEnabled(false);

        btHouseDetailsCreateVisit.setVisibility(View.GONE);
        btHouseDetailsFinishVisit.setVisibility(View.GONE);
        btHouseDetailsOpenVisit.setVisibility(View.GONE);

        mainPanelTabsLayout.setVisibility(View.VISIBLE);
        mainPanelVisitLayout.setVisibility(View.GONE);

        displayHouseholdDetails();
        initFragments();
        initializeButtons();
    }

    private void createNewHousehold(){
        //load new Household HDS-Form and save it
        if (this.region != null) {
            loadNewHouseholdForm();
        } else {
            DialogFactory.createMessageInfo(this, "Household Enumeration", "Cant create new Household because there is no region selected");
        }
    }

    private void finishVisit() {
        //close visit methods

        //finsih the visit in visit fragment
        if (householdVisitFragment != null)  {
            List<Member> notVisited = householdVisitFragment.getNonVisitedMembers();
            List<Member> toremove = new ArrayList<>();
            String nonVisitedAsText = "";
            List<String> nonVisitedCodesList = new ArrayList<>();

            for (Member member : notVisited) {
                nonVisitedAsText += nonVisitedAsText.isEmpty() ? member.code : ", "+member.code;
                nonVisitedCodesList.add(member.code);

                if (visit.nonVisitedMembers.contains(member.code)){
                    toremove.add(member);
                }
            }

            //remove all marked as non visited
            notVisited.removeAll(toremove);

            //Not all of the individuals were visited
            if (notVisited.size()>0) {

                final String finalList = nonVisitedAsText;

                DialogFactory.createMessageYN(this, getString(R.string.household_visit_not_finished_title_lbl), getString(R.string.household_visit_not_finished_msg_ask_lbl, nonVisitedAsText), new DialogFactory.OnYesNoClickListener() {
                    @Override
                    public void onYesClicked() {
                        markAllAsNonVisited(visit, nonVisitedCodesList, finalList);
                    }

                    @Override
                    public void onNoClicked() {
                        //just dont do nothing
                    }
                }).show();

                return;
            }
            //Log.d("ending visit", "me");

            //close visit - update endtimestamp
            VisitFormUtil.updateEndTimestamp(this, this.visit.getRecentlyCreatedUri());

            //finish visit mode
            this.visit = null;
            setHouseholdMode();
        }


    }

    private void markAllAsNonVisited(Visit visit, List<String> nonVisitedMembersCodeList, String nonVisitedMembersAsText) {

        visit.nonVisitedMembers.addAll(nonVisitedMembersCodeList);
        this.boxVisits.put(visit);

        VisitFormUtil.markAllAsNonVisited(this, this.visit.getRecentlyCreatedUri(), nonVisitedMembersAsText);
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
                    setVisitMode();
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
    private void loadNewHouseholdForm(){

        HouseholdFormUtil householdForm = new HouseholdFormUtil(this, this, this.region, this.odkFormUtilities, new FormUtilListener<Household>() {
            @Override
            public void onNewEntityCreated(Household household, Map<String, Object> data) {
                HouseholdDetailsActivity.this.household = household;
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

    /* Visit Form */
    private void loadNewVisitForm(boolean newHouseholdCreated){

        VisitFormUtil visitFormUtil = new VisitFormUtil(this, this, this.household, newHouseholdCreated, this.odkFormUtilities, new FormUtilListener<Visit>() {
            @Override
            public void onNewEntityCreated(Visit entity, Map<String, Object> data) {
                HouseholdDetailsActivity.this.visit = entity;
                visitExtraData.putAll(data);
                setVisitMode();
            }

            @Override
            public void onEntityEdited(Visit entity, Map<String, Object> data) {

            }

            @Override
            public void onFormCancelled() {
                HouseholdDetailsActivity.this.visit = null;
            }
        });

        visitFormUtil.collect();
    }

    @Override
    public void updateHouseholdDetails() {
        displayHouseholdDetails();
    }
}
