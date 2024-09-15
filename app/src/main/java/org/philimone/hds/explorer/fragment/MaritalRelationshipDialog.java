package org.philimone.hds.explorer.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.MaritalRelationshipAdapter;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.MaritalRelationship;
import org.philimone.hds.explorer.model.MaritalRelationship_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.enums.Gender;
import org.philimone.hds.explorer.model.enums.MaritalEndStatus;
import org.philimone.hds.explorer.widget.RecyclerListView;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

/**
 *
 */
public class MaritalRelationshipDialog extends DialogFragment implements MaritalRelationshipAdapter.Listener {

    private FragmentManager fragmentManager;

    private LinearLayout layoutSelectedSpouse;
    private LinearLayout layoutHusband;
    private TextView txtDialogTitle;
    private Button btDialogClose;
    private TextView txtMarSelectedSpouse;
    private TextView txtMarHusbandSpouse;
    private RecyclerListView lvRelationships;
    private Button btMarNewPolygamic;
    private Button btMarNewRelationship;

    private View progressBarLayout;
    private Box<Member> boxMembers;
    private Box<MaritalRelationship> boxMaritalRelationships;
    private Member spouseA;
    private Member spouseB;
    private Member selectedSpouse;
    private MaritalRelationship mainMaritalRelationship;
    private SelectedSpouseMode spouseMode;

    private boolean genderChecking;
    private int filterMinimunSpouseAge;
    private String filterHouseholdCode;
    private Household fastFilterHousehold;

    private MrDialogListener listener;

    private enum SelectedSpouseMode { MALE_MARRIED, FEMALE_MARRIED, FEMALE_NOT_MARRIED}

    public MaritalRelationshipDialog(){
        super();
        initBoxes();
    }

    public static MaritalRelationshipDialog newInstance(FragmentManager fm, Member selectedSpouse, boolean selectedSpouseMarried, Member spouseA, Member spouseB, MrDialogListener memberFilterListener){
        return newInstance(fm, selectedSpouse, selectedSpouseMarried, spouseA, spouseB, true, memberFilterListener);
    }

    public static MaritalRelationshipDialog newInstance(FragmentManager fm, Member selectedSpouse, boolean selectedSpouseMarried, Member spouseA, Member spouseB, boolean cancelable, MrDialogListener memberFilterListener){
        MaritalRelationshipDialog filterDialog = new MaritalRelationshipDialog();

        filterDialog.fragmentManager = fm;
        filterDialog.listener = memberFilterListener;
        filterDialog.setCancelable(cancelable);
        filterDialog.selectedSpouse = selectedSpouse;
        filterDialog.spouseA = spouseA;
        filterDialog.spouseB = spouseB;

        if (spouseA.gender == Gender.FEMALE) {
            filterDialog.spouseA = spouseB;
            filterDialog.spouseB = spouseA;
        }

        filterDialog.onInstanceCreation(selectedSpouseMarried);

        return filterDialog;
    }

    private void onInstanceCreation(boolean selectedSpouseMarried){
        if (this.selectedSpouse.gender == Gender.FEMALE) {
            spouseMode = selectedSpouseMarried ? SelectedSpouseMode.FEMALE_MARRIED : SelectedSpouseMode.FEMALE_NOT_MARRIED;
        } else {
            spouseMode = SelectedSpouseMode.MALE_MARRIED;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.marital_relationship_dialog, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialize(view);
    }

    @Override
    public void onResume() {
        super.onResume();

        Window window = getDialog().getWindow();
        Point size = new Point();

        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);

        int width = size.x;
        int height = size.y;

        window.setLayout((int) (width * 0.97), (int) (height * 0.80));
        window.setGravity(Gravity.CENTER);
    }

    private void initBoxes() {
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxMaritalRelationships = ObjectBoxDatabase.get().boxFor(MaritalRelationship.class);
    }

    private void initialize(View view) {

        this.layoutSelectedSpouse = view.findViewById(R.id.layoutSelectedSpouse);
        this.layoutHusband = view.findViewById(R.id.layoutHusband);
        this.txtDialogTitle = view.findViewById(R.id.txtDialogTitle);
        this.btDialogClose = view.findViewById(R.id.btDialogClose);
        this.txtMarSelectedSpouse = view.findViewById(R.id.txtMarSelectedSpouse);
        this.txtMarHusbandSpouse = view.findViewById(R.id.txtMarHusbandSpouse);
        this.progressBarLayout = view.findViewById(R.id.progressBarLayout);
        this.lvRelationships = view.findViewById(R.id.lvMembersList);
        this.btMarNewPolygamic = view.findViewById(R.id.btMarNewPolygamic);
        this.btMarNewRelationship = view.findViewById(R.id.btMarNewRelationship);

        this.btMarNewPolygamic.setOnClickListener(v -> onAddNewWifeButtonClicked());

        this.btMarNewRelationship.setOnClickListener(v -> onNewRelationshipButtonClicked());

        this.btDialogClose.setOnClickListener(v -> {
            if (isCancelable()) {
                closeDialog();
            }
        });

        displayData();

        if (spouseMode == SelectedSpouseMode.FEMALE_MARRIED) {
            this.layoutSelectedSpouse.setVisibility(View.VISIBLE);
            this.layoutHusband.setVisibility(View.VISIBLE);
            this.btMarNewRelationship.setEnabled(false);
            this.btMarNewPolygamic.setEnabled(false);
        } else if (spouseMode == SelectedSpouseMode.FEMALE_NOT_MARRIED) {
            this.layoutSelectedSpouse.setVisibility(View.VISIBLE);
            this.layoutHusband.setVisibility(View.VISIBLE);
            this.btMarNewRelationship.setEnabled(true);
            this.btMarNewPolygamic.setEnabled(false);
        } else if (spouseMode == SelectedSpouseMode.MALE_MARRIED){
            this.layoutSelectedSpouse.setVisibility(View.GONE);
            this.layoutHusband.setVisibility(View.VISIBLE);
            this.btMarNewRelationship.setEnabled(false);
            this.btMarNewPolygamic.setEnabled(true);
        }
    }

    private void displayData() {
        txtMarSelectedSpouse.setText(this.spouseB.name);
        txtMarHusbandSpouse.setText(this.spouseA.name);

        loadHusbandRelationships();
    }

    public void loadHusbandRelationships() {
        showProgress(true);

        //get all current marital relationships of spouseA
        List<MaritalRelationship> list = boxMaritalRelationships.query(MaritalRelationship_.endStatus.equal(MaritalEndStatus.NOT_APPLICABLE.code)
                                                                       .and(MaritalRelationship_.memberA_code.equal(spouseA.code).or(MaritalRelationship_.memberB_code.equal(spouseA.code))) )
                                                                       .order(MaritalRelationship_.startDate).build().find();
        List<MaritalRelationshipAdapter.MaritalRelationshipItem> items = new ArrayList<>();

        for (MaritalRelationship mr : list) {
            Member spouseA = boxMembers.query(Member_.code.equal(mr.memberA_code)).build().findFirst();
            Member spouseB = boxMembers.query(Member_.code.equal(mr.memberB_code)).build().findFirst();

            if (spouseB.gender==Gender.MALE) {
                Member bak = spouseA;
                spouseA = spouseB;
                spouseB = bak;
            }

            boolean enable = true;
            boolean isSelectedSpouse = spouseB.id == selectedSpouse.id;

            if (spouseMode == SelectedSpouseMode.FEMALE_MARRIED) enable = enable && isSelectedSpouse;
            if (spouseMode == SelectedSpouseMode.FEMALE_NOT_MARRIED) enable = false;

            //dont allow to end relationship if is a recentlyCreated MaritalRelationship
            if (mr.recentlyCreated) enable = false;

            items.add(new MaritalRelationshipAdapter.MaritalRelationshipItem(mr, spouseA, spouseB, enable));
        }

        if (list.size() > 0) {
            this.mainMaritalRelationship = list.get(0); //First Wife
            MaritalRelationshipAdapter adapter = new MaritalRelationshipAdapter(this.getContext(), items, this);
            this.lvRelationships.setAdapter(adapter);
        } else {
            this.lvRelationships.setAdapter(null);
        }

        showProgress(false);
    }

    private void onNewRelationshipButtonClicked() {
        this.listener.onAddNewPolygamicRelationship(this.mainMaritalRelationship, spouseA, spouseB);
    }

    private void onAddNewWifeButtonClicked() {
        //add new wife - select spouse
        MemberFilterDialog dialog = MemberFilterDialog.newInstance(this.fragmentManager, this.getContext().getString(R.string.maritalrelationship_spouse_select_lbl), true, new MemberFilterDialog.Listener() {
            @Override
            public void onSelectedMember(Member member) {
                onNewSpouseSelected(member);
            }

            @Override
            public void onCanceled() { }
        });

        if (genderChecking) {
            if (spouseA.gender == Gender.MALE){
                dialog.setGenderFemaleOnly();
                dialog.addFilterExcludeMarried();
            } else {
                dialog.setGenderMaleOnly();
            }
        }

        dialog.setFilterMinAge(this.filterMinimunSpouseAge, true);
        dialog.setFastFilterHousehold(fastFilterHousehold);
        dialog.setFilterHouseCode(filterHouseholdCode);
        dialog.addFilterExcludeMember(this.spouseA);
        dialog.addFilterExcludeMember(this.spouseB);
        dialog.setStartSearchOnShow(true);
        dialog.show();
    }

    private void onNewSpouseSelected(Member member) {
        this.spouseB = member;
        this.listener.onAddNewPolygamicRelationship(this.mainMaritalRelationship, spouseA, spouseB);
    }

    @Override
    public void onEndingRelationship(MaritalRelationship maritalRelationship) {
        //closing relationship
        listener.onEndingRelationship(maritalRelationship);
    }

    private void showProgress(final boolean show) {
        //lvMembersList.setAdapter(null);
        lvRelationships.setVisibility(show ? View.GONE : View.VISIBLE);
        progressBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void show(){
        this.show(fragmentManager, "relatype");
    }

    private void closeDialog(){
        dismiss();

        if (this.listener != null){
            this.listener.onCanceled();
        }
    }

    public boolean isGenderChecking() {
        return genderChecking;
    }

    public void setGenderChecking(boolean genderChecking) {
        this.genderChecking = genderChecking;
    }

    public int getFilterMinimunSpouseAge() {
        return filterMinimunSpouseAge;
    }

    public void setFilterMinimunSpouseAge(int filterMinimunSpouseAge) {
        this.filterMinimunSpouseAge = filterMinimunSpouseAge;
    }

    public void setFastFilterHousehold(Household household) {
        this.fastFilterHousehold = household;
    }

    public String getFilterHouseholdCode() {
        return filterHouseholdCode;
    }

    public void setFilterHouseholdCode(String filterHouseholdCode) {
        this.filterHouseholdCode = filterHouseholdCode;
    }

    public interface MrDialogListener {
        void onEndingRelationship(MaritalRelationship maritalRelationship);

        void onAddNewPolygamicRelationship(MaritalRelationship mainMaritalRelationship, Member spouseA, Member spouseB);

        void onCanceled();
    }
}
