package org.philimone.hds.explorer.main;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.CollectedDataArrayAdapter;
import org.philimone.hds.explorer.adapter.model.CollectedDataItem;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.CollectedDataFragment;
import org.philimone.hds.explorer.fragment.ExternalDatasetsFragment;
import org.philimone.hds.explorer.fragment.region.details.RegionChildsFragment;
import org.philimone.hds.explorer.fragment.region.details.adapter.RegionDetailsFragmentAdapter;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.settings.RequestCodes;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.FormSelectorDialog;

import java.io.File;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.listener.OdkFormResultListener;
import mz.betainteractive.odk.model.FilledForm;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.region_details);

        this.loggedUser = Bootstrap.getCurrentUser();
        this.region = (Region) getIntent().getExtras().get("region");
        this.activityRequestCode = getIntent().getExtras().getInt("request_code");

        initBoxes();

        readFormDataLoader();

        initialize();
        initFragments();
    }

    public void setRegion(Region region){
        this.region = region;
    }

    private void initBoxes() {
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
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

        regionDetailsTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                regionDetailsTabViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        setRegionData();

        enableButtonsByFormLoaders();
        enableButtonsByIntentData();
    }

    private void enableButtonsByFormLoaders() {
        boolean hasForms = this.formDataLoaders.size()>0;
        this.btRegionDetailsCollectData.setEnabled(hasForms);
    }

    private void enableButtonsByIntentData() {
        Object item = getIntent().getExtras().get("enable-collect-data");

        Boolean enaColData = (item==null) ? null : (boolean)item;

        if (enaColData != null){
            this.btRegionDetailsCollectData.setEnabled(enaColData.booleanValue());
        }
    }

    private void setRegionData(){
        String hierarchyName = getHierarchyName(region);
        Region parent = getRegion(region.getParent());

        txtRdHieararchyName.setText(hierarchyName);
        txtRdRegionName.setText(region.getName());
        txtRdRegionCode.setText(region.getCode());
        txtRdParent.setText(parent.getName());
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
            fragmentAdapter = new RegionDetailsFragmentAdapter(this.getSupportFragmentManager(), this.getLifecycle(), region, loggedUser, formDataLoaders);
            regionDetailsTabViewPager.setAdapter(fragmentAdapter);
            //this will create all fragments
            regionDetailsTabViewPager.setOffscreenPageLimit(3);
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

    private void readFormDataLoader(){

        Object[] objs = (Object[]) getIntent().getExtras().get("dataloaders");

        for (int i=0; i < objs.length; i++){
            FormDataLoader formDataLoader = (FormDataLoader) objs[i];
            //Log.d("tag", ""+formDataLoader.getForm().getFormId());
            if (formDataLoader.getForm().isRegionForm() && formDataLoader.getForm().getRegionLevel().equals(region.getLevel()) && isVisibleForm(formDataLoader.getForm())){
                this.formDataLoaders.add(formDataLoader);
            }
        }
    }

    private void onCollectDataClicked(){
        this.regionDetailsTabLayout.getTabAt(2).select();
        //this.householdDetailsTabViewPager.setCurrentItem(2, true);

        //Go to HouseholdFormsFragment and call this action
        CollectedDataFragment collectedDataFragment = this.fragmentAdapter.getFragmentCollected();
        collectedDataFragment.onCollectData();
    }


}
