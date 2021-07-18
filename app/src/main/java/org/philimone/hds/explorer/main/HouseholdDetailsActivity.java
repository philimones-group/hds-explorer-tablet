package org.philimone.hds.explorer.main;

import android.os.Bundle;
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
import org.philimone.hds.explorer.fragment.household.details.HouseholdDatasetsFragment;
import org.philimone.hds.explorer.fragment.household.details.HouseholdFormsFragment;
import org.philimone.hds.explorer.fragment.household.details.HouseholdMembersFragment;
import org.philimone.hds.explorer.fragment.household.details.HouseholdVisitFragment;
import org.philimone.hds.explorer.fragment.household.details.adapter.HouseholdDetailsFragmentAdapter;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.Visit_;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.settings.RequestCodes;
import org.philimone.hds.explorer.settings.generator.CodeGeneratorService;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.forms.listeners.FormCollectionListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
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

    private HouseholdMembersFragment householdMembersFragment;
    private HouseholdVisitFragment householdVisitFragment;
    private HouseholdFormsFragment householdFormsFragment;
    private HouseholdDatasetsFragment householdDatasetsFragment;

    private Region region;
    private Household household;
    private List<FormDataLoader> formDataLoaders = new ArrayList<>();

    private User loggedUser;

    private Box<ApplicationParam> boxAppParams;
    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Visit> boxVisits;
    private Box<CoreCollectedData> boxCoreCollectedData;

    private Integer requestCode;

    private HouseholdDetailsMode hdetailsMode = HouseholdDetailsMode.NORMAL_MODE;
    private boolean loadNewHousehold = false;
    private boolean postExecution = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.household_details);

        readIntentData();

        initModes();
        initBoxes();
        initialize();

        this.loadNewHousehold = hdetailsMode==HouseholdDetailsMode.NEW_HOUSEHOLD_MODE; //Will be true only once
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

    private void readIntentData() {

        try {
            this.loggedUser = (User) getIntent().getExtras().get("user");
        }catch (Exception ex){
            ex.printStackTrace();
        }

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
        if (requestCode == null) {
            this.hdetailsMode = HouseholdDetailsMode.NORMAL_MODE;
            return;
        }

        if (requestCode == RequestCodes.HOUSEHOLD_DETAILS_FROM_HFILTER_NEW_HOUSEHOLD) {
            this.hdetailsMode = HouseholdDetailsMode.NEW_HOUSEHOLD_MODE;
        }

        if (requestCode == RequestCodes.HOUSEHOLD_DETAILS_FROM_TRACKING_LIST_DETAILS) {
            this.hdetailsMode = HouseholdDetailsMode.TRACKING_MODE;
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

        postExecution = Queries.getApplicationParamValue(this.boxAppParams, ApplicationParam.HFORM_POST_EXECUTION).equals("true");

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

        //householdDetailsTabViewPager.setOnPaddLayoutChangeListener((View.OnLayoutChangeListener) new TabLayout.TabLayoutOnPageChangeListener(householdDetailsTabLayout));
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

        initFragments();

        showHouseholdData();

        enableButtonsByFormLoaders();
        enableButtonsByIntentData();
        enableLayoutsByHouseholdMode();
    }

    private void onCreateVisitClicked() {

        //New Visit
        DialogFactory.createMessageInfo(this, "", "A Visit Form will be opened now", clickedButton -> {
            createNewVisit();
        }).show();
    }

    private void onFinishVisitClicked() {
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
        this.householdFormsFragment.onCollectData();
    }

    private void initFragments() {
        this.householdMembersFragment = HouseholdMembersFragment.newInstance(this.household, this.loggedUser);
        this.householdFormsFragment = HouseholdFormsFragment.newInstance(this.household, this.loggedUser, this.formDataLoaders);
        this.householdDatasetsFragment = HouseholdDatasetsFragment.newInstance(this.household);

        List<Fragment> list = new ArrayList<>();
        list.add(householdMembersFragment);
        list.add(householdDatasetsFragment);
        list.add(householdFormsFragment);

        HouseholdDetailsFragmentAdapter adapter = new HouseholdDetailsFragmentAdapter(this.getSupportFragmentManager(),  this.getLifecycle(), list);
        householdDetailsTabViewPager.setAdapter(adapter);

        //this will create all fragments
        householdDetailsTabViewPager.setOffscreenPageLimit(list.size());
    }

    private void enableButtonsByFormLoaders() {
        boolean hasForms = this.formDataLoaders.size()>0;
        this.btHouseDetailsCollectData.setEnabled(hasForms);
    }

    private void enableButtonsByIntentData() {
        Object item = getIntent().getExtras().get("enable-collect-data");

        Boolean enaColData = (item==null) ? null : (boolean)item;

        if (enaColData != null){
            this.btHouseDetailsCollectData.setEnabled(enaColData.booleanValue());
        }
    }

    private boolean hasRecentlyCreatedVisit() {
        Visit lastVisit = null;

        if (household != null) {
            lastVisit = this.boxVisits.query().equal(Visit_.householdCode, household.code).order(Visit_.visitDate, QueryBuilder.DESCENDING).build().findFirst();
        }

        return lastVisit != null && lastVisit.recentlyCreated;
    }

    private void enableLayoutsByHouseholdMode() {

        boolean recentlyCreatedVisit = hasRecentlyCreatedVisit();

        if (hdetailsMode == HouseholdDetailsMode.NORMAL_MODE) {
            btHouseDetailsCreateVisit.setEnabled(true);
            btHouseDetailsFinishVisit.setEnabled(false);
            btHouseDetailsOpenVisit.setEnabled(recentlyCreatedVisit);

            btHouseDetailsCreateVisit.setVisibility(View.VISIBLE);
            btHouseDetailsFinishVisit.setVisibility(View.GONE);
            btHouseDetailsOpenVisit.setVisibility(recentlyCreatedVisit ? View.VISIBLE : View.GONE);

            mainPanelTabsLayout.setVisibility(View.VISIBLE);
            mainPanelVisitLayout.setVisibility(View.GONE);
        }

        if (hdetailsMode == HouseholdDetailsMode.VISIT_MODE) {
            btHouseDetailsCreateVisit.setEnabled(false);
            btHouseDetailsFinishVisit.setEnabled(true);
            btHouseDetailsOpenVisit.setEnabled(false);

            btHouseDetailsCreateVisit.setVisibility(View.GONE);
            btHouseDetailsFinishVisit.setVisibility(View.VISIBLE);
            btHouseDetailsOpenVisit.setVisibility(View.GONE);

            mainPanelTabsLayout.setVisibility(View.GONE);
            mainPanelVisitLayout.setVisibility(View.VISIBLE);
        }

        if (hdetailsMode == HouseholdDetailsMode.NEW_HOUSEHOLD_MODE) {
            btHouseDetailsCreateVisit.setEnabled(false);
            btHouseDetailsFinishVisit.setEnabled(true);
            btHouseDetailsOpenVisit.setEnabled(false);

            btHouseDetailsCreateVisit.setVisibility(View.GONE);
            btHouseDetailsFinishVisit.setVisibility(View.VISIBLE);
            btHouseDetailsOpenVisit.setVisibility(View.GONE);

            mainPanelTabsLayout.setVisibility(View.GONE);
            mainPanelVisitLayout.setVisibility(View.VISIBLE);
        }

        if (hdetailsMode == HouseholdDetailsMode.TRACKING_MODE) {
            btHouseDetailsCreateVisit.setEnabled(false);
            btHouseDetailsFinishVisit.setEnabled(false);
            btHouseDetailsOpenVisit.setEnabled(false);

            btHouseDetailsCreateVisit.setVisibility(View.GONE);
            btHouseDetailsFinishVisit.setVisibility(View.GONE);
            btHouseDetailsOpenVisit.setVisibility(View.GONE);

            mainPanelTabsLayout.setVisibility(View.VISIBLE);
            mainPanelVisitLayout.setVisibility(View.GONE);
        }


    }

    private void showHouseholdData(){

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
            if (form.isFollowUpOnly() && !isInTrackingListMode){ //forms flagged with followUpOnly can only be opened using FollowUp Lists, to be able to open via normal surveys remove the flag on the server
                return false; //follow up only will not be visible in other modes
            }

            return true;
        }

        return false;
    }

    private void createNewHousehold(){
        //load new Household HDS-Form and save it
        if (this.region != null) {
            loadNewHouseholdForm();
        } else {
            DialogFactory.createMessageInfo(this, "Household Enumeration", "Cant create new Household because there is no region selected");
        }
    }

    private void createNewVisit(){
        hdetailsMode = HouseholdDetailsMode.VISIT_MODE;

        showHouseholdData();
        enableLayoutsByHouseholdMode();
    }

    private void closeCurrentVisit() {

        //close visit methods

        if (hdetailsMode == HouseholdDetailsMode.NEW_HOUSEHOLD_MODE) {

        }

        hdetailsMode =  HouseholdDetailsMode.NORMAL_MODE;

        showHouseholdData();
        enableLayoutsByHouseholdMode();
    }

    private void openPreviousVisit() {
        hdetailsMode = HouseholdDetailsMode.VISIT_MODE;

        showHouseholdData();
        enableLayoutsByHouseholdMode();
    }

    /* Household forn */
    void loadNewHouseholdForm(){
        InputStream hformFile = getResources().openRawResource(R.raw.household_form);

        CodeGeneratorService codeGenerator = new CodeGeneratorService();

        Map<String, String> preloadedMap = new LinkedHashMap<>();
        preloadedMap.put("regionCode", region.code);
        preloadedMap.put("regionName", region.name);
        preloadedMap.put("householdCode", codeGenerator.generateHouseholdCode(region, loggedUser));
        //preloadedMap.put("household_name", );

        FormCollectionListener newHouseholdFormListener = new FormCollectionListener() {
            @Override
            public ValidationResult onFormValidate(HForm form, Map<String, ColumnValue> collectedValues) {

                ColumnValue columnHouseholdCode = collectedValues.get("householdCode");
                ColumnValue columnHouseholdName = collectedValues.get("householdName");

                String household_code = columnHouseholdCode.getValue();
                String household_name = columnHouseholdName.getValue();
                //String household_gps = collectedValues.get("gps").getValue();

                if (!codeGenerator.isHouseholdCodeValid(household_code)){
                    String message = getString(R.string.new_household_code_err_lbl);
                    //DialogFactory.createMessageInfo(HouseholdDetailsActivity.this, R.string.info_lbl, R.string.new_household_code_err_lbl).show();
                    return new ValidationResult(columnHouseholdCode, message);
                }

                if (!household_code.startsWith(region.code)){
                    String message = getString(R.string.new_household_code_region_err_lbl);
                    //DialogFactory.createMessageInfo(HouseholdDetailsActivity.this, R.string.info_lbl, R.string.new_household_code_region_err_lbl).show();
                    return new ValidationResult(columnHouseholdCode, message);
                }

                //check if household with code exists
                if (boxHouseholds.query().equal(Household_.code, household_code).build().findFirst() != null){
                    String message = getString(R.string.new_household_code_exists_lbl);
                    //DialogFactory.createMessageInfo(HouseholdDetailsActivity.this, R.string.info_lbl, R.string.new_household_code_exists_lbl).show();
                    return new ValidationResult(columnHouseholdCode, message);
                }

                if (household_name.isEmpty()){
                    String message = getString(R.string.new_household_code_empty_lbl);
                    //DialogFactory.createMessageInfo(HouseholdDetailsActivity.this, R.string.info_lbl, R.string.new_household_code_empty_lbl).show();
                    return new ValidationResult(columnHouseholdName, message);
                }

                return ValidationResult.noErrors();
            }

            @Override
            public void onFormFinished(HForm form, Map<String, ColumnValue> collectedValues, XmlFormResult result) {
                //saveNewHousehold();
                ColumnValue colRegionCode = collectedValues.get("regionCode");
                ColumnValue colRegionName = collectedValues.get("regionName");
                ColumnValue colHouseholdCode = collectedValues.get("householdCode");
                ColumnValue colHouseholdName = collectedValues.get("householdName");
                ColumnValue colHeadCode = collectedValues.get("headCode");
                ColumnValue colHeadName = collectedValues.get("headName");
                ColumnValue colCollBy = collectedValues.get("collectedBy");
                ColumnValue colCollDate = collectedValues.get("collectedDate");
                ColumnValue colGps = collectedValues.get("gps");
                Map<String, Double> gpsValues = colGps.getGpsValues();
                Double gpsLat = gpsValues.get("gps_lat");
                Double gpsLon = gpsValues.get("gps_lon");
                Double gpsAlt = gpsValues.get("gps_alt");
                Double gpsAcc = gpsValues.get("gps_acc");


                Household household = new Household();
                household.region = colRegionCode.getValue();
                household.code = colHouseholdCode.getValue();
                household.name = colHouseholdName.getValue();
                household.headCode = colHeadCode.getValue();
                household.headName = colHeadName.getValue();
                household.gpsLatitude = gpsLat;
                household.gpsLongitude = gpsLon;
                household.gpsAltitude = gpsAlt;
                household.gpsAccuracy = gpsAcc;
                household.recentlyCreated = true;
                household.recentlyCreatedUri = result.getFilename();

                long entityId = boxHouseholds.put(household);

                CoreCollectedData collectedData = new CoreCollectedData();
                collectedData.formEntity = CoreFormEntity.HOUSEHOLD;
                collectedData.formEntityId = entityId;
                collectedData.formEntityCode = household.code;
                collectedData.formEntityName = household.name;
                collectedData.formUuid = result.getFormUuid();
                collectedData.formFilename = result.getFilename();
                collectedData.createdDate = new Date();

                boxCoreCollectedData.put(collectedData);


                HouseholdDetailsActivity.this.household = household;

                createNewVisit();
            }

        };

        FormFragment form = FormFragment.newInstance(this.getSupportFragmentManager(), hformFile, Bootstrap.getInstancesPath(), loggedUser.username, preloadedMap, postExecution , newHouseholdFormListener);
        form.startCollecting();
    }


}
