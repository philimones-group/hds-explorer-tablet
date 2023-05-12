package org.philimone.hds.explorer.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.CollectedDataFragment;
import org.philimone.hds.explorer.fragment.ExternalDatasetsFragment;
import org.philimone.hds.explorer.fragment.region.details.RegionChildsFragment;
import org.philimone.hds.explorer.fragment.region.details.RegionEditFragment;
import org.philimone.hds.explorer.fragment.region.details.adapter.RegionDetailsFragmentAdapter;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.followup.TrackingSubjectList;
import org.philimone.hds.explorer.settings.RequestCodes;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

public class RegionDetailsActivity extends AppCompatActivity {

    private TabLayout regionDetailsTabLayout;
    private ViewPager2 regionDetailsTabViewPager;
    private RelativeLayout mainPanelTabsLayout;

    private RegionDetailsFragmentAdapter fragmentAdapter;

    private TextView txtRdHieararchyName;
    private TextView txtRdRegionName;
    private TextView txtRdRegionCode;
    private TextView txtRdParent;

    private Button btRegionDetailsCollectData;
    private Button btRegionDetailsBack;

    private Region region;
    private List<FormDataLoader> formDataLoaders = new ArrayList<>();

    private User loggedUser;

    private int activityRequestCode;

    private Box<ApplicationParam> boxAppParams;
    private Box<Region> boxRegions;
    private Box<CollectedData> boxCollectedData;
    private Box<TrackingSubjectList> boxTrackingSubjectList;

    private TrackingSubjectList trackingSubject;
    private CollectedData collectedDataToEdit;
    private boolean callOnCollectData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.region_details);

        this.loggedUser = Bootstrap.getCurrentUser();

        initBoxes();
        readIntentData();

        initialize();
        initFragments();
        enableButtonsByFormLoaders();
    }

    private void readIntentData() {
        long regionId = getIntent().getExtras().getLong("region");
        this.region = boxRegions.get(regionId);
        this.activityRequestCode = getIntent().getExtras().getInt("request_code");

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

    public void setRegion(Region region){
        this.region = region;
    }

    private void initBoxes() {
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxTrackingSubjectList = ObjectBoxDatabase.get().boxFor(TrackingSubjectList.class);
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
    }

    private void initialize() {
        txtRdHieararchyName = (TextView) findViewById(R.id.txtRdHieararchyName);
        txtRdRegionName = (TextView) findViewById(R.id.txtRdRegionName);
        txtRdRegionCode = (TextView) findViewById(R.id.txtRdRegionCode);
        txtRdParent = (TextView) findViewById(R.id.txtRdParent);
        regionDetailsTabLayout = findViewById(R.id.regionDetailsTabLayout);
        regionDetailsTabViewPager = findViewById(R.id.regionDetailsTabViewPager);
        mainPanelTabsLayout = findViewById(R.id.mainPanelTabsLayout);

        btRegionDetailsCollectData = (Button) findViewById(R.id.btRegionDetailsCollectData);
        btRegionDetailsBack = (Button) findViewById(R.id.btRegionDetailsBack);

        btRegionDetailsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegionDetailsActivity.this.onBackPressed();
            }
        });

        btRegionDetailsCollectData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCollectDataClicked();
            }
        });

        setRegionData();

    }

    private void enableButtonsByFormLoaders() {
        boolean hasForms = this.formDataLoaders.size()>0;
        this.btRegionDetailsCollectData.setEnabled(hasForms);
    }

    private void setRegionData(){

        this.region = this.boxRegions.get(region.id);

        String hierarchyName = getHierarchyName(region);
        Region parent = getRegion(region.getParent());

        txtRdHieararchyName.setText(hierarchyName);
        txtRdRegionName.setText(region.getName());
        txtRdRegionCode.setText(region.getCode());
        txtRdParent.setText(parent != null ? parent.getName() : "");
    }

    private Region getRegion(String code){
        Region region = this.boxRegions.query().equal(Region_.code, code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
        return region;
    }

    private String getHierarchyName(Region region){
        if (region == null) return "";

        ApplicationParam param = Queries.getApplicationParamBy(boxAppParams, region.getLevel() );

        if (param != null){
            return param.getValue();
        }

        return "";
    }

    private void initFragments() {

        if (region != null && fragmentAdapter == null) {
            List<String> tabTitles = new ArrayList<>();
            tabTitles.add(getString(R.string.region_details_tab_details_list_lbl));
            tabTitles.add(getString(R.string.region_details_tab_datasets_lbl));
            tabTitles.add(getString(R.string.region_details_tab_collected_forms_lbl));
            tabTitles.add(getString(R.string.region_details_tab_edit_lbl));

            boolean isTracking = activityRequestCode == RequestCodes.REGION_DETAILS_FROM_TRACKING_LIST_DETAILS;

            fragmentAdapter = new RegionDetailsFragmentAdapter(this.getSupportFragmentManager(), this.getLifecycle(), region, loggedUser, this.trackingSubject, tabTitles);
            fragmentAdapter.setCollectedDataToEdit(collectedDataToEdit);
            fragmentAdapter.setFragmentEditListener(new RegionEditFragment.EditListener() {
                @Override
                public void onUpdate() {
                    setRegionData();
                    reloadFragmentsData();
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

                }
            });

            regionDetailsTabViewPager.setAdapter(fragmentAdapter);
            //this will create all fragments
            regionDetailsTabViewPager.setOffscreenPageLimit(4);

            new TabLayoutMediator(regionDetailsTabLayout, regionDetailsTabViewPager, (tab, position) -> {
                tab.setText(fragmentAdapter.getTitle(position));
            }).attach();

            if (collectedDataToEdit != null) {
                this.regionDetailsTabLayout.getTabAt(2).select();
            }
        }


    }

    private void reloadFragmentsData(){
        if (fragmentAdapter != null) {
            RegionChildsFragment childsFragment = this.fragmentAdapter.getFragmentRegionChilds();
            ExternalDatasetsFragment datasetsFragment = this.fragmentAdapter.getFragmentDatasets();
            CollectedDataFragment collectedDataFragment = this.fragmentAdapter.getFragmentCollected();

            if (childsFragment != null) {
                childsFragment.reloadRegions();
            }

            if (datasetsFragment != null) {
                //datasetsFragment.
            }

            if (collectedDataFragment != null) {
                collectedDataFragment.reloadCollectedData();
            }
        }
    }

    private boolean isVisibleForm(Form form){
        if (activityRequestCode != RequestCodes.REGION_DETAILS_FROM_TRACKING_LIST_DETAILS){ //RegionDetails was not opened via Tracking/FollowUp lists
            if (form.isFollowUpForm()){ //forms flagged with followUpOnly can only be opened using FollowUp Lists, to be able to open via normal surveys remove the flag on the server
                return false;
            }
        }

        return true;
    }

    private void onCollectDataClicked(){
        this.regionDetailsTabLayout.getTabAt(2).select();
        //this.householdDetailsTabViewPager.setCurrentItem(2, true);

        //Go to HouseholdFormsFragment and call this action
        CollectedDataFragment collectedDataFragment = this.fragmentAdapter.getFragmentCollected();
        collectedDataFragment.onCollectData();
    }


}
