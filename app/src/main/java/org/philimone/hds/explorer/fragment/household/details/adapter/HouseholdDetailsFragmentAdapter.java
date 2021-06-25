package org.philimone.hds.explorer.fragment.household.details.adapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HouseholdDetailsFragmentAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragments = new ArrayList<>();

    public HouseholdDetailsFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<Fragment> fragments) {
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
