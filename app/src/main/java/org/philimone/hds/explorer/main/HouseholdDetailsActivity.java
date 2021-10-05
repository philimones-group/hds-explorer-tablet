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

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.ExternalDatasetsFragment;
import org.philimone.hds.explorer.fragment.CollectedDataFragment;
import org.philimone.hds.explorer.fragment.household.details.HouseholdMembersFragment;
import org.philimone.hds.explorer.fragment.household.details.HouseholdVisitFragment;
import org.philimone.hds.explorer.fragment.household.details.adapter.HouseholdDetailsFragmentAdapter;
import org.philimone.hds.explorer.main.hdsforms.HouseholdFormUtil;
import org.philimone.hds.explorer.main.hdsforms.VisitFormUtil;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.Visit_;
import org.philimone.hds.explorer.settings.RequestCodes;
import org.philimone.hds.explorer.widget.DialogFactory;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.StringUtil;

public class HouseholdDetailsActivity extends AppCompatActivity {

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
    private Box<Visit> boxVisits;
    private Box<CoreCollectedData> boxCoreCollectedData;

    private Integer requestCode;

    private HouseholdDetailsMode hdetailsMode;
    private boolean loadNewHousehold = false;

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

        //if its the first time - load HForm Household
        if (this.loadNewHousehold==true){
            createNewHousehold();
        }

        this.loadNewHousehold = false; //on other resumes will not load new household
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
            ex.printStackTrace();
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
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
    }

    private void readFormDataLoader(){

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

        householdDetailsTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                householdDetailsTabViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        initializeButtons();
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
            fragmentAdapter = new HouseholdDetailsFragmentAdapter(this.getSupportFragmentManager(), this.getLifecycle(), household, loggedUser, formDataLoaders);
            householdDetailsTabViewPager.setAdapter(fragmentAdapter);
            //this will create all fragments
            householdDetailsTabViewPager.setOffscreenPageLimit(3);
        }


    }

    private void initializeButtons() {
        Object item = getIntent().getExtras().get("enable-collect-data");

        Boolean enaColData = (item==null) ? null : (boolean)item;

        if (enaColData != null){
            this.btHouseDetailsCollectData.setEnabled(enaColData.booleanValue());
        }

        boolean hasForms = this.formDataLoaders.size()>0;
        this.btHouseDetailsCollectData.setEnabled(hasForms);
    }

    private void displayHouseholdDetails(){

        if (household == null) return;

        Region region = this.boxRegions.query().equal(Region_.code, household.region).build().findFirst();
        Visit lastVisit = this.boxVisits.query().equal(Visit_.householdCode, household.code).order(Visit_.visitDate, QueryBuilder.DESCENDING).build().findFirst();
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
        Log.d("finishing","visit");
        DialogFactory.createMessageInfo(this, "", "Finishing current Visit, comeback later", clickedButton -> {
            closeCurrentVisit();
        }).show();
    }

    private void onOpenVisitClicked() {
        openPreviousVisit();
    }

    private void onCollectDataClicked(){

        this.householdDetailsTabViewPager.setCurrentItem(1, true);

        //Go to HouseholdFormsFragment and call this action
        //this.collectedDataFragment.onCollectData();

    }

    private boolean hasRecentlyCreatedVisit() {
        Visit lastVisit = null;

        if (household != null) {
            lastVisit = this.boxVisits.query().equal(Visit_.householdCode, household.code).order(Visit_.visitDate, QueryBuilder.DESCENDING).build().findFirst();
        }

        return lastVisit != null && lastVisit.recentlyCreated;
    }

    private void setHouseholdMode(){
        this.hdetailsMode = HouseholdDetailsMode.NORMAL_MODE;
        boolean recentlyCreatedVisit = hasRecentlyCreatedVisit();

        btHouseDetailsCreateVisit.setEnabled(true);
        btHouseDetailsFinishVisit.setEnabled(false);
        btHouseDetailsOpenVisit.setEnabled(recentlyCreatedVisit);

        btHouseDetailsCreateVisit.setVisibility(View.VISIBLE);
        btHouseDetailsFinishVisit.setVisibility(View.GONE);
        btHouseDetailsOpenVisit.setVisibility(recentlyCreatedVisit ? View.VISIBLE : View.GONE);

        mainPanelTabsLayout.setVisibility(View.VISIBLE);
        mainPanelVisitLayout.setVisibility(View.GONE);

        displayHouseholdDetails();
        initFragments();
    }

    private void setVisitMode(){
        this.hdetailsMode = HouseholdDetailsMode.VISIT_MODE;

        btHouseDetailsCreateVisit.setEnabled(false);
        btHouseDetailsFinishVisit.setEnabled(true);
        btHouseDetailsOpenVisit.setEnabled(false);

        btHouseDetailsCreateVisit.setVisibility(View.GONE);
        btHouseDetailsFinishVisit.setVisibility(View.VISIBLE);
        btHouseDetailsOpenVisit.setVisibility(View.GONE);

        mainPanelTabsLayout.setVisibility(View.GONE);
        mainPanelVisitLayout.setVisibility(View.VISIBLE);

        displayHouseholdDetails();

        //load visit fragment
        if (this.householdVisitFragment != null) {
            this.householdVisitFragment.load(household, visit, loggedUser);
        }

    }

    private void setNewHouseholdMode(){
        this.hdetailsMode = HouseholdDetailsMode.NEW_HOUSEHOLD_MODE;
        this.loadNewHousehold = true;

        btHouseDetailsCreateVisit.setEnabled(false);
        btHouseDetailsFinishVisit.setEnabled(true);
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
    }

    private void createNewHousehold(){
        //load new Household HDS-Form and save it
        if (this.region != null) {
            loadNewHouseholdForm();
        } else {
            DialogFactory.createMessageInfo(this, "Household Enumeration", "Cant create new Household because there is no region selected");
        }
    }

    private void closeCurrentVisit() {
        //close visit methods
        this.visit = null;

        setHouseholdMode();
    }

    private void openPreviousVisit() {
        setVisitMode();
    }

    /* Household Form */
    private void loadNewHouseholdForm(){

        HouseholdFormUtil householdForm = new HouseholdFormUtil(this.getSupportFragmentManager(), this, this.region, new HouseholdFormUtil.Listener() {
            @Override
            public void onNewHouseholdCreated(Household household) {
                HouseholdDetailsActivity.this.household = household;
                loadNewVisitForm(true);
            }

            @Override
            public void onHouseholdEdited(Household household) {

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

        VisitFormUtil visitFormUtil = new VisitFormUtil(this.getSupportFragmentManager(), this, this.household, newHouseholdCreated, new VisitFormUtil.Listener() {
            @Override
            public void onNewVisitCreated(Visit visit) {
                HouseholdDetailsActivity.this.visit = visit;
                setVisitMode();
            }

            @Override
            public void onVisitEdited(Visit visit) {

            }

            @Override
            public void onFormCancelled() {
                HouseholdDetailsActivity.this.visit = null;
            }
        });

        visitFormUtil.collect();
    }

}
