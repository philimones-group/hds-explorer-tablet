package org.philimone.hds.explorer.fragment.household.details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.model.Household;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HouseholdDatasetsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HouseholdDatasetsFragment extends Fragment {

    private Household household;

    public HouseholdDatasetsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HouseholdVisitFragment.
     */
    public static HouseholdDatasetsFragment newInstance(Household household) {
        HouseholdDatasetsFragment fragment = new HouseholdDatasetsFragment();
        fragment.household = household;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.household_datasets, container, false);
    }
}