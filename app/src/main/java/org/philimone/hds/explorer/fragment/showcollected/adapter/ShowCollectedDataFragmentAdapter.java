package org.philimone.hds.explorer.fragment.showcollected.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.philimone.hds.explorer.fragment.showcollected.ShowCoreCollectedDataFragment;
import org.philimone.hds.explorer.fragment.showcollected.ShowVisitCollectedDataFragment;
import org.philimone.hds.explorer.fragment.showcollected.ShowOdkCollectedDataFragment;
import org.philimone.hds.explorer.main.ShowCollectedDataActivity;

import java.util.ArrayList;
import java.util.List;

public class ShowCollectedDataFragmentAdapter extends FragmentStateAdapter {

    private List<String> tabTitles = new ArrayList<>();
    private final List<Fragment> fragments = new ArrayList<>();
    private ShowVisitCollectedDataFragment fragVisitCollectedData;
    private ShowOdkCollectedDataFragment fragOdkCollectedData;
    private ShowCoreCollectedDataFragment fragCoreCollectedData;

    public ShowCollectedDataFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<String> titles, ShowVisitCollectedDataFragment.ActionListener visitFragmentListener, ShowCoreCollectedDataFragment.ActionListener coreFragmentListener, ShowOdkCollectedDataFragment.ActionListener odkFragmentListener) {
        super(fragmentManager, lifecycle);
        this.tabTitles.addAll(titles);

        this.fragVisitCollectedData = ShowVisitCollectedDataFragment.newInstance(visitFragmentListener);
        this.fragOdkCollectedData = ShowOdkCollectedDataFragment.newInstance(odkFragmentListener);
        this.fragCoreCollectedData = ShowCoreCollectedDataFragment.newInstance(coreFragmentListener);

        //this.fragCoreCollectedData.setMainActivity(activity);
        //this.fragOdkCollectedData.setMainActivity(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d("createfrag", ""+position);
        switch (position) {
            case 0: return this.fragVisitCollectedData;
            case 1: return this.fragCoreCollectedData;
            case 2: return this.fragOdkCollectedData;
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


    public ShowOdkCollectedDataFragment getFragmentOdkCollectedData() {
        return fragOdkCollectedData;
    }

    public ShowVisitCollectedDataFragment getFragmentVisitCollectedData() {
        return this.fragVisitCollectedData;
    }

    public ShowCoreCollectedDataFragment getFragmentCoreCollectedData() {
        return this.fragCoreCollectedData;
    }

    public void updateTabTitles(List<String> tabTitles) {
        this.tabTitles.clear();
        this.tabTitles.addAll(tabTitles);
    }
}
