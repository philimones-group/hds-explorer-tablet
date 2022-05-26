package org.philimone.hds.explorer.fragment.member.details.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.fragment.CollectedDataFragment;
import org.philimone.hds.explorer.fragment.ExternalDatasetsFragment;
import org.philimone.hds.explorer.fragment.household.details.HouseholdMembersFragment;
import org.philimone.hds.explorer.fragment.member.details.MemberDetailsFragment;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.User;

import java.util.ArrayList;
import java.util.List;

public class MemberDetailsFragmentAdapter extends FragmentStateAdapter {

    private List<String> tabTitles = new ArrayList<>();
    private final List<Fragment> fragments = new ArrayList<>();
    private MemberDetailsFragment fragMemberDetails;
    private CollectedDataFragment fragCollected;
    private ExternalDatasetsFragment fragDatasets;
    private Member member;
    private User user;
    private List<FormDataLoader> formDataLoaders;

    public MemberDetailsFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, Member member, User user, List<FormDataLoader> formDataLoaders, List<String> titles) {
        super(fragmentManager, lifecycle);
        this.member = member;
        this.user = user;
        this.formDataLoaders = formDataLoaders;
        this.tabTitles.addAll(titles);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0: this.fragMemberDetails = MemberDetailsFragment.newInstance(this.member, this.user); return this.fragMemberDetails;
            case 1: this.fragDatasets = ExternalDatasetsFragment.newInstance(this.member); return this.fragDatasets;
            case 2: this.fragCollected = CollectedDataFragment.newInstance(this.member, this.user, this.formDataLoaders); return this.fragCollected;
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

    public MemberDetailsFragment getFragmentMemberDetails() {
        return fragMemberDetails;
    }

    public CollectedDataFragment getFragmentCollected() {
        return fragCollected;
    }

    public ExternalDatasetsFragment getFragmentDatasets() {
        return fragDatasets;
    }
}
