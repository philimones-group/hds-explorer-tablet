package org.philimone.hds.explorer.fragment.member.details.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.fragment.CollectedDataFragment;
import org.philimone.hds.explorer.fragment.ExternalDatasetsFragment;
import org.philimone.hds.explorer.fragment.household.details.HouseholdEditFragment;
import org.philimone.hds.explorer.fragment.member.details.MemberDetailsFragment;
import org.philimone.hds.explorer.fragment.member.details.MemberEditFragment;
import org.philimone.hds.explorer.model.Household;
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
    private MemberEditFragment fragEdit;
    private Household household;
    private Member member;
    private User user;
    private List<FormDataLoader> formDataLoaders;

    public MemberDetailsFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, Household household, Member member, User user, List<FormDataLoader> formDataLoaders, List<String> titles) {
        super(fragmentManager, lifecycle);
        this.household = household;
        this.member = member;
        this.user = user;
        this.formDataLoaders = formDataLoaders;
        this.tabTitles.addAll(titles);

        this.fragMemberDetails = MemberDetailsFragment.newInstance(this.member, this.user);
        this.fragDatasets = ExternalDatasetsFragment.newInstance(this.member);
        this.fragCollected = CollectedDataFragment.newInstance(this.member, this.user, this.formDataLoaders);
        this.fragEdit = MemberEditFragment.newInstance(fragmentManager, this.household, this.member, this.user);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d("createfrag", ""+position);
        switch (position) {
            case 0: return this.fragMemberDetails;
            case 1: return this.fragDatasets;
            case 2: return this.fragCollected;
            case 3: return this.fragEdit;
            default: return null;
        }

    }

    public void setFragmentEditListener(MemberEditFragment.EditListener listener) {
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
