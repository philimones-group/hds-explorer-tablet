package org.philimone.hds.explorer.fragment.showcollected.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.philimone.hds.explorer.fragment.showcollected.ShowCoreCollectedDataFragment;
import org.philimone.hds.explorer.fragment.showcollected.ShowOdkCollectedDataFragment;

import java.util.ArrayList;
import java.util.List;

public class ShowCollectedDataFragmentAdapter extends FragmentStateAdapter {

    private List<String> tabTitles = new ArrayList<>();
    private final List<Fragment> fragments = new ArrayList<>();
    private ShowCoreCollectedDataFragment fragCoreCollectedData;
    private ShowOdkCollectedDataFragment fragOdkCollectedData;

    public ShowCollectedDataFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<String> titles) {
        super(fragmentManager, lifecycle);
        this.tabTitles.addAll(titles);

        this.fragCoreCollectedData = ShowCoreCollectedDataFragment.newInstance();
        this.fragOdkCollectedData = ShowOdkCollectedDataFragment.newInstance();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d("createfrag", ""+position);
        switch (position) {
            case 0: return this.fragCoreCollectedData;
            case 1: return this.fragOdkCollectedData;
            default: return null;
        }

    }

    public String getTitle(int position) {
        return this.tabTitles.get(position);
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean containsItem(long itemId) {
        return itemId >= 0 && itemId <= 1;
    }


    public ShowOdkCollectedDataFragment getFragmentOdkCollectedData() {
        return fragOdkCollectedData;
    }

    public ShowCoreCollectedDataFragment getFragmentCoreCollectedData() {
        return this.fragCoreCollectedData;
    }
}
