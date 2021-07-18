package org.philimone.hds.explorer.main.sync;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.fragment.household.details.HouseholdDatasetsFragment;
import org.philimone.hds.explorer.fragment.household.details.HouseholdFormsFragment;
import org.philimone.hds.explorer.fragment.household.details.HouseholdMembersFragment;
import org.philimone.hds.explorer.fragment.household.details.adapter.HouseholdDetailsFragmentAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class SyncPanelActivity extends AppCompatActivity {

    private String username;
    private String password;
    private String serverUrl;

    private ViewPager2 syncViewPager;
    private TabLayout syncTabLayout;

    private SyncDownloadPanelFragment syncDownloadPanel;
    private SyncUploadPanelFragment syncUploadPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.synchronization_panel);

        syncViewPager = findViewById(R.id.syncViewPager);
        syncTabLayout = findViewById(R.id.syncTabLayout);

        this.username = (String) getIntent().getExtras().get("username");
        this.password = (String) getIntent().getExtras().get("password");
        this.serverUrl = (String) getIntent().getExtras().get("server-url");

        initFragments();

    }

    @Override
    protected void onResume() {
        super.onResume();

        //updateFragments();
        //showStatus();
        //readPreferences();
        //set syncentityresult saved on preferences
    }

    private void initFragments(){
        this.syncDownloadPanel = SyncDownloadPanelFragment.newInstance(this.username, this.password, this.serverUrl);
        this.syncUploadPanel = SyncUploadPanelFragment.newInstance(this.username, this.password, this.serverUrl);

        List<Fragment> list = new ArrayList<>();
        list.add(this.syncDownloadPanel);
        list.add(this.syncUploadPanel);

        SyncFragmentsAdapter adapter = new SyncFragmentsAdapter(this.getSupportFragmentManager(),  this.getLifecycle(), list);
        syncViewPager.setAdapter(adapter);

        //create on change tab listener
        this.syncTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                syncViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //this will create all fragments
        syncViewPager.setOffscreenPageLimit(list.size());
    }

    private void updateFragments(){
        if (this.syncDownloadPanel != null) {
            this.syncDownloadPanel.showStatus();
            this.syncDownloadPanel.readPreferences();
        }
    }

    class SyncFragmentsAdapter extends FragmentStateAdapter {

        private final List<Fragment> fragments = new ArrayList<>();

        public SyncFragmentsAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<Fragment> fragments) {
            super(fragmentManager, lifecycle);
            this.fragments.addAll(fragments);

        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return this.fragments.size();
        }
    }

}