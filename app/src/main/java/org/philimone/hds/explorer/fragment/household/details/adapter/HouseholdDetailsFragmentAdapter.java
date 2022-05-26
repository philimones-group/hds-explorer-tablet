package org.philimone.hds.explorer.fragment.household.details.adapter;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.fragment.CollectedDataFragment;
import org.philimone.hds.explorer.fragment.ExternalDatasetsFragment;
import org.philimone.hds.explorer.fragment.household.details.HouseholdMembersFragment;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.User;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HouseholdDetailsFragmentAdapter extends FragmentStateAdapter {

    private List<String> tabTitles = new ArrayList<>();
    private final List<Fragment> fragments = new ArrayList<>();
    private HouseholdMembersFragment fragMembers;
    private CollectedDataFragment fragCollected;
    private ExternalDatasetsFragment fragDatasets;
    private Household household;
    private User user;
    private List<FormDataLoader> formDataLoaders;

    public HouseholdDetailsFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, Household household, User user, List<FormDataLoader> formDataLoaders, List<String> titles) {
        super(fragmentManager, lifecycle);
        this.household = household;
        this.user = user;
        this.formDataLoaders = formDataLoaders;
        this.tabTitles.addAll(titles);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0: this.fragMembers = HouseholdMembersFragment.newInstance(this.household, this.user); return this.fragMembers;
            case 1: this.fragDatasets = ExternalDatasetsFragment.newInstance(this.household); return this.fragDatasets;
            case 2: this.fragCollected = CollectedDataFragment.newInstance(this.household, this.user, this.formDataLoaders); return this.fragCollected;
            default: return null;
        }

    }

    public String getTitle(int position) {
        return this.tabTitles.get(position);
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean containsItem(long itemId) {
        return itemId >= 0 && itemId <= 2;
    }

    public HouseholdMembersFragment getFragmentMembers() {
        return fragMembers;
    }

    public CollectedDataFragment getFragmentCollected() {
        return fragCollected;
    }

    public ExternalDatasetsFragment getFragmentDatasets() {
        return fragDatasets;
    }
}
