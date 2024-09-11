package org.philimone.hds.explorer.main;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.fragment.showcollected.ShowCoreCollectedDataFragment;
import org.philimone.hds.explorer.fragment.showcollected.ShowVisitCollectedDataFragment;
import org.philimone.hds.explorer.fragment.showcollected.ShowOdkCollectedDataFragment;
import org.philimone.hds.explorer.fragment.showcollected.adapter.ShowCollectedDataFragmentAdapter;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.widget.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import mz.betainteractive.utilities.StringUtil;

public class ShowCollectedDataActivity extends AppCompatActivity implements ShowOdkCollectedDataFragment.ActionListener, ShowCoreCollectedDataFragment.ActionListener, ShowVisitCollectedDataFragment.ActionListener {

    private TextView txtShowCollectedDataModules;
    private TextView txtShowCollectedCoreForms;
    private TextView txtShowCollectedOdkForms;
    private TabLayout collectedDataTabLayout;
    private ViewPager2 collectedDataTabViewPager;
    private Button btShowCollectedBack;
    private Button btShowCollectedUpdate;

    private User user;

    private Box<CoreCollectedData> boxCoreCollectedData;
    private Box<CollectedData> boxCollectedData;

    private ShowCollectedDataFragmentAdapter fragmentAdapter;

    private LoadingDialog loadingDialog;
    private int loadedData = 0; //1+2+3 = 6 means full loaded
    private int fragmentsCreated = 0;

    public ShowCollectedDataActivity() {
        this.initBoxes();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_collected_data);

        this.user = Bootstrap.getCurrentUser();

        initialize();
    }

    private void initBoxes() {
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
    }

    private void initialize() {

        this.txtShowCollectedDataModules = findViewById(R.id.txtShowCollectedDataModules);
        this.txtShowCollectedCoreForms = findViewById(R.id.txtShowCollectedCoreForms);
        this.txtShowCollectedOdkForms = findViewById(R.id.txtShowCollectedOdkForms);
        this.collectedDataTabLayout = findViewById(R.id.collectedDataTabLayout);
        this.collectedDataTabViewPager = findViewById(R.id.collectedDataTabViewPager);
        this.btShowCollectedBack = findViewById(R.id.btShowCollectedBack);
        this.btShowCollectedUpdate = findViewById(R.id.btShowCollectedUpdate);
        this.loadingDialog = new LoadingDialog(this); //findViewById(R.id.loadingProgressBar);

        initFragments();

        this.btShowCollectedBack.setOnClickListener(v -> {
            ShowCollectedDataActivity.this.onBackPressed();
        });

        this.btShowCollectedUpdate.setOnClickListener(v -> {
            loadCollectedDataLists();
        });

        showLoadingDialog(R.string.loading_dialog_load_data_lbl, true);
    }

    private void initFragments() {

        if (fragmentAdapter == null) {

            long count_visit = this.boxCoreCollectedData.query(CoreCollectedData_.formEntity.equal(CoreFormEntity.VISIT.code)).build().count();
            long count_core = this.boxCoreCollectedData.count();
            long count_odk = this.boxCollectedData.query().filter((c) -> StringUtil.containsAny(c.formModules, user.getSelectedModules())).build().find().size();

            List<String> tabTitles = new ArrayList<>();
            tabTitles.add(getString(R.string.show_collected_data_tab_visit_forms_lbl) + (count_visit == 0 ? "" : " ("+ count_visit +")"));
            tabTitles.add(getString(R.string.show_collected_data_tab_core_forms_lbl) + (count_core == 0 ? "" : " ("+ count_core +")"));
            tabTitles.add(getString(R.string.show_collected_data_tab_odk_forms_lbl) + (count_odk == 0 ? "" : " ("+ count_odk +")"));

            fragmentAdapter = new ShowCollectedDataFragmentAdapter(this.getSupportFragmentManager(), this.getLifecycle(), tabTitles, this, this, this);
            collectedDataTabViewPager.setAdapter(fragmentAdapter);

            //this will create all fragments
            collectedDataTabViewPager.setOffscreenPageLimit(3);

            new TabLayoutMediator(collectedDataTabLayout, collectedDataTabViewPager, (tab, position) -> {
                tab.setText(fragmentAdapter.getTitle(position));
            }).attach();
        }

    }

    public void showResumeDetails() {
        refreshTabTitles();
        loadCollectedDataLists();
    }

    public void refreshTabTitles(){
        long count_visit = this.boxCoreCollectedData.query(CoreCollectedData_.formEntity.equal(CoreFormEntity.VISIT.code)).build().count();
        long count_core = this.boxCoreCollectedData.count();
        long count_odk = this.boxCollectedData.query().filter((c) -> StringUtil.containsAny(c.formModules, user.getSelectedModules())).build().find().size();

        String visit_label = getString(R.string.show_collected_data_tab_visit_forms_lbl) + (count_visit == 0 ? "" : " ("+ count_visit +")");
        String core_label = getString(R.string.show_collected_data_tab_core_forms_lbl) + (count_core == 0 ? "" : " ("+ count_core +")");
        String odk_label = getString(R.string.show_collected_data_tab_odk_forms_lbl) + (count_odk == 0 ? "" : " ("+ count_odk +")");

        collectedDataTabLayout.getTabAt(0).setText(visit_label);
        collectedDataTabLayout.getTabAt(1).setText(core_label);
        collectedDataTabLayout.getTabAt(2).setText(odk_label);

        this.txtShowCollectedDataModules.setText(user.getSelectedModulesCodes());
    }

    private void loadCollectedDataLists() {
        loadVisitCollectedDataList();
        loadCoreCollectedDataList();
        loadOdkCollectedDataList();
    }

    private void loadVisitCollectedDataList() {
        if (this.fragmentAdapter != null) {
            ShowVisitCollectedDataFragment fragment = this.fragmentAdapter.getFragmentVisitCollectedData();

            if (fragment != null) {
                fragment.reloadCollectedData();
            }
        }
    }

    private void loadCoreCollectedDataList() {
        if (this.fragmentAdapter != null) {
            ShowCoreCollectedDataFragment fragment = this.fragmentAdapter.getFragmentCoreCollectedData();
            if (fragment != null) {
                fragment.reloadCollectedData();
            }
        }
    }

    private void showLoadingDialog(@StringRes int msgId, boolean show) {
        showLoadingDialog(getString(msgId), show);
    }

    private void showLoadingDialog(String msg, boolean show){
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.dismiss();
        }
    }

    private void loadOdkCollectedDataList() {
        if (this.fragmentAdapter != null) {
            ShowOdkCollectedDataFragment fragment = this.fragmentAdapter.getFragmentOdkCollectedData();
            if (fragment != null) {
                fragment.reloadCollectedData();
            }
        }
    }

    @Override
    public void onVisitEdited() {
        showResumeDetails();
    }

    @Override
    public void onDeletedOdkForms() {
        refreshTabTitles();
        loadOdkCollectedDataList();
    }

    @Override
    public void onOdkFormEdited() {
        //nothing to do its already refreshed
        refreshTabTitles();
        loadOdkCollectedDataList();
    }

    @Override
    public void onDeletedCoreForms() {
        showResumeDetails();
    }

    @Override
    public void onCoreFormEdited() {
        showResumeDetails();
    }

    @Override
    public void onCoreFormsLoaded() {
        loadedData += 1;
        onDataLoaded();
    }

    @Override
    public void onVisitsLoaded() {
        loadedData += 2;
        onDataLoaded();
    }

    @Override
    public void onOdkFormsLoaded() {
        loadedData += 3;
        onDataLoaded();
    }

    private void onDataLoaded() {
        Log.d("loading", ""+loadedData);
        if (loadedData >= 6) {
            loadedData = 0;
            showLoadingDialog("", false);
        }
    }

    @Override
    public void onCoreCollectedViewsCreated(ShowCoreCollectedDataFragment fragment) {
        fragment.reloadCollectedData();
    }

    @Override
    public void onOdkCollectedViewsCreated(ShowOdkCollectedDataFragment fragment) {
        fragment.reloadCollectedData();
    }

    @Override
    public void onVisitCollectedViewsCreated(ShowVisitCollectedDataFragment fragment) {
        fragment.reloadCollectedData();
    }

    private void onFragmentsCreated() {
        Log.d("creating-frags", ""+loadedData);
        if (fragmentsCreated >= 6) {
            fragmentsCreated = 0;
            Log.d("fragments", "all created");

            loadCollectedDataLists();
        }
    }
}