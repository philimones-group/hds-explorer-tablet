package org.philimone.hds.explorer.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.household.details.HouseholdDatasetsFragment;
import org.philimone.hds.explorer.fragment.household.details.HouseholdFormsFragment;
import org.philimone.hds.explorer.fragment.household.details.HouseholdMembersFragment;
import org.philimone.hds.explorer.fragment.household.details.HouseholdVisitFragment;
import org.philimone.hds.explorer.fragment.household.details.adapter.HouseholdDetailsFragmentAdapter;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.Visit_;
import org.philimone.hds.explorer.widget.DialogFactory;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.StringUtil;

public class HouseholdDetailsActivity extends AppCompatActivity {

    private enum HouseholdDetailsMode { VISIT_MODE, NORMAL_MODE };

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

    private Household household;
    private List<FormDataLoader> formDataLoaders = new ArrayList<>();

    private User loggedUser;

    private Box<ApplicationParam> boxAppParams;
    private Box<Region> boxRegions;
    private Box<Visit> boxVisits;

    private int requestCode;

    private HouseholdDetailsMode hdetailsMode = HouseholdDetailsMode.NORMAL_MODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.household_details);

        this.loggedUser = (User) getIntent().getExtras().get("user");
        this.household = (Household) getIntent().getExtras().get("household");
        //this.region = (Region) getIntent().getExtras().get("region");
        this.requestCode = getIntent().getExtras().getInt("request_code");

        initBoxes();

        readFormDataLoader();

        initialize();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    private void readFormDataLoader(){

        Object[] objs = (Object[]) getIntent().getExtras().get("dataloaders");

        for (int i=0; i < objs.length; i++){
            FormDataLoader formDataLoader = (FormDataLoader) objs[i];
            //Log.d("tag", ""+formDataLoader.getForm().getFormId());
            if (formDataLoader.getForm().isHouseholdForm() && isVisibleForm(formDataLoader.getForm())){
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
        enableLayoutsByVisitMode();
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

    private void initBoxes() {
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxVisits = ObjectBoxDatabase.get().boxFor(Visit.class);
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

    private void enableLayoutsByVisitMode() {
        boolean isVisitMode = hdetailsMode == HouseholdDetailsMode.VISIT_MODE;
        Visit lastVisit = this.boxVisits.query().equal(Visit_.householdCode, household.code).order(Visit_.visitDate, QueryBuilder.DESCENDING).build().findFirst();
        boolean hasRecentVisit = lastVisit != null && lastVisit.recentlyCreated;


        btHouseDetailsCreateVisit.setEnabled(!isVisitMode);
        btHouseDetailsFinishVisit.setEnabled(isVisitMode);
        btHouseDetailsOpenVisit.setEnabled(!isVisitMode && hasRecentVisit);

        btHouseDetailsCreateVisit.setVisibility(isVisitMode ? View.GONE : View.VISIBLE);
        btHouseDetailsFinishVisit.setVisibility(isVisitMode ? View.VISIBLE : View.GONE);
        btHouseDetailsOpenVisit.setVisibility(!isVisitMode && hasRecentVisit ? View.VISIBLE : View.GONE);

        this.mainPanelTabsLayout.setVisibility(isVisitMode ? View.GONE : View.VISIBLE);
        this.mainPanelVisitLayout.setVisibility(isVisitMode ? View.VISIBLE : View.GONE);
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
        if (requestCode != TrackingListDetailsActivity.RC_HOUSEHOLD_DETAILS_TRACKINGLIST){ //HouseholdDetails was not opened via Tracking/FollowUp lists
            if (form.isFollowUpOnly()){ //forms flagged with followUpOnly can only be opened using FollowUp Lists, to be able to open via normal surveys remove the flag on the server
                return false;
            }
        }

        return true;
    }

    private void createNewVisit(){
        hdetailsMode = HouseholdDetailsMode.VISIT_MODE;

        showHouseholdData();
        enableLayoutsByVisitMode();
    }

    private void closeCurrentVisit() {
        hdetailsMode =  HouseholdDetailsMode.NORMAL_MODE;

        showHouseholdData();
        enableLayoutsByVisitMode();
    }

    private void openPreviousVisit() {
        hdetailsMode = HouseholdDetailsMode.VISIT_MODE;

        showHouseholdData();
        enableLayoutsByVisitMode();
    }


}
