package org.philimone.hds.explorer.fragment.household.details;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.model.Household;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HouseholdVisitButtonsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HouseholdVisitButtonsFragment extends Fragment {

    private Household household;

    public HouseholdVisitButtonsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HouseholdVisitFragment.
     */
    public static HouseholdVisitButtonsFragment newInstance(Household household) {
        HouseholdVisitButtonsFragment fragment = new HouseholdVisitButtonsFragment();
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
        return inflater.inflate(R.layout.household_visit_buttons, container, false);
    }
}