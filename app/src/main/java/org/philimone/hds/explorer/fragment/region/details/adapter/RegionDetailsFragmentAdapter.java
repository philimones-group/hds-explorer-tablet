package org.philimone.hds.explorer.fragment.region.details.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.fragment.CollectedDataFragment;
import org.philimone.hds.explorer.fragment.ExternalDatasetsFragment;
import org.philimone.hds.explorer.fragment.member.details.MemberDetailsFragment;
import org.philimone.hds.explorer.fragment.region.details.RegionChildsFragment;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.User;

import java.util.ArrayList;
import java.util.List;

public class RegionDetailsFragmentAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragments = new ArrayList<>();
    private RegionChildsFragment fragRegionChilds;
    private CollectedDataFragment fragCollected;
    private ExternalDatasetsFragment fragDatasets;
    private Region region;
    private User user;
    private List<FormDataLoader> formDataLoaders;

    public RegionDetailsFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, Region region, User user, List<FormDataLoader> formDataLoaders) {
        super(fragmentManager, lifecycle);
        this.region = region;
        this.user = user;
        this.formDataLoaders = formDataLoaders;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0: this.fragRegionChilds = RegionChildsFragment.newInstance(this.region, this.user); return this.fragRegionChilds;
            case 1: this.fragDatasets = ExternalDatasetsFragment.newInstance(this.region); return this.fragDatasets;
            case 2: this.fragCollected = CollectedDataFragment.newInstance(this.region, this.user, this.formDataLoaders); return this.fragCollected;
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

    public RegionChildsFragment getFragmentRegionChilds() {
        return fragRegionChilds;
    }

    public CollectedDataFragment getFragmentCollected() {
        return fragCollected;
    }

    public ExternalDatasetsFragment getFragmentDatasets() {
        return fragDatasets;
    }
}
