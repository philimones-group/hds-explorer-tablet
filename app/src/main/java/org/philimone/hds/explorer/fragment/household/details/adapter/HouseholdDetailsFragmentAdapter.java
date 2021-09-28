package org.philimone.hds.explorer.fragment.household.details.adapter;

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

    private final List<Fragment> fragments = new ArrayList<>();
    private Household household;
    private User user;
    private List<FormDataLoader> formDataLoaders;

    public HouseholdDetailsFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, Household household, User user, List<FormDataLoader> formDataLoaders) {
        super(fragmentManager, lifecycle);
        this.household = household;
        this.user = user;
        this.formDataLoaders = formDataLoaders;

    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0: return HouseholdMembersFragment.newInstance(this.household, this.user);
            case 1: return CollectedDataFragment.newInstance(this.household, this.user, this.formDataLoaders);
            case 2: return ExternalDatasetsFragment.newInstance(this.household);
            default: return null;
        }

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
}
