package org.philimone.hds.explorer.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
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

    /*
    @Override
    protected void onPostResume() {
        super.onPostResume();

        showResumeDetails();
    }
    */

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

        initFragments();

        this.btShowCollectedBack.setOnClickListener(v -> {
            ShowCollectedDataActivity.this.onBackPressed();
        });

        this.btShowCollectedUpdate.setOnClickListener(v -> {
            loadCollectedDataLists();
        });
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

        /*
        List<String> tabTitles = new ArrayList<>();
        tabTitles.add(visit_label);
        tabTitles.add(core_label);
        tabTitles.add(odk_label);

        if (this.fragmentAdapter != null) {
            this.fragmentAdapter.updateTabTitles(tabTitles);
            this.fragmentAdapter.notifyDataSetChanged();
        }
         */

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
}