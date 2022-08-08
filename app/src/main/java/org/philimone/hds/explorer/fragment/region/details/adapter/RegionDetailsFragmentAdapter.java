package org.philimone.hds.explorer.fragment.region.details.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.fragment.CollectedDataFragment;
import org.philimone.hds.explorer.fragment.ExternalDatasetsFragment;
import org.philimone.hds.explorer.fragment.household.details.HouseholdEditFragment;
import org.philimone.hds.explorer.fragment.region.details.RegionChildsFragment;
import org.philimone.hds.explorer.fragment.region.details.RegionEditFragment;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.followup.TrackingSubjectList;

import java.util.ArrayList;
import java.util.List;

public class RegionDetailsFragmentAdapter extends FragmentStateAdapter {
    private List<String> tabTitles = new ArrayList<>();
    private final List<Fragment> fragments = new ArrayList<>();
    private RegionChildsFragment fragRegionChilds;
    private CollectedDataFragment fragCollected;
    private ExternalDatasetsFragment fragDatasets;
    private RegionEditFragment fragEdit;
    private Region region;
    private User user;

    public RegionDetailsFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, Region region, User user, TrackingSubjectList trackingSubjectItem, List<String> titles) {
        super(fragmentManager, lifecycle);
        this.region = region;
        this.user = user;
        this.tabTitles.addAll(titles);

        this.fragRegionChilds = RegionChildsFragment.newInstance(this.region, this.user);
        this.fragDatasets = ExternalDatasetsFragment.newInstance(this.region);
        this.fragCollected = CollectedDataFragment.newInstance(this.region, this.user, trackingSubjectItem);
        this.fragEdit = RegionEditFragment.newInstance(this.region, this.user);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0: return this.fragRegionChilds;
            case 1: return this.fragDatasets;
            case 2: return this.fragCollected;
            case 3: return this.fragEdit;
            default: return null;
        }

    }

    public void setAutoHighlightCollectedData(CollectedData autoHighlightCollectedData) {
        this.fragCollected.setAutoHighlightCollectedData(autoHighlightCollectedData);
    }

    public void setFragmentEditListener(RegionEditFragment.EditListener listener) {
        this.fragEdit.setEditListener(listener);
    }

    public String getTitle(int position) {
        return this.tabTitles.get(position);
    }

    @Override
    public int getItemCount() {
        return 4;
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
