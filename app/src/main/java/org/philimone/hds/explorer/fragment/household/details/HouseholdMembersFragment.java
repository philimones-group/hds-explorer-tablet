package org.philimone.hds.explorer.fragment.household.details;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.MemberAdapter;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.main.MemberDetailsActivity;
import org.philimone.hds.explorer.model.Dataset;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.widget.LoadingDialog;
import org.philimone.hds.explorer.widget.RecyclerListView;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HouseholdMembersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HouseholdMembersFragment extends Fragment {

    private Spinner membersListSpinner;
    private RecyclerListView lvHouseholdMembers;
    private LoadingDialog loadingDialog;

    private Household household;
    private User loggedUser;

    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<Form> boxForms;
    private Box<Dataset> boxDatasets;



    public HouseholdMembersFragment() {
        // Required empty public constructor
        initBoxes();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HouseholdMembersFragment.
     */
    public static HouseholdMembersFragment newInstance(Household household, User user) {
        HouseholdMembersFragment fragment = new HouseholdMembersFragment();
        fragment.household = household;
        fragment.loggedUser = user;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey("household_id")) {
            this.household = this.boxHouseholds.get(savedInstanceState.getLong("household_id"));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (outState != null && this.household != null) {
            outState.putLong("household_id", this.household.id);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.household_members, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialize(view);
    }

    private void initialize(View view) {
        membersListSpinner = view.findViewById(R.id.membersListSpinner);
        lvHouseholdMembers = view.findViewById(R.id.lvHouseholdMembers);

        this.loadingDialog = new LoadingDialog(this.getContext());

        lvHouseholdMembers.addOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long id) {
                onMemberClicked(position);
            }

            @Override
            public void onItemLongClick(View view, int position, long id) {

            }
        });

        membersListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    MembersListSpinnerItem item = (MembersListSpinnerItem) parent.getAdapter().getItem(position);
                    onMembersListSelected(item.type);
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        loadSpinners();

        this.showHouseholdResidents();
    }

    private void initBoxes() {
//        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
//        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        this.boxDatasets = ObjectBoxDatabase.get().boxFor(Dataset.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
    }

    private void loadSpinners() {
        List<MembersListSpinnerItem> list = new ArrayList<>();
        list.add(new MembersListSpinnerItem(MembersListType.RESIDENTS));
        list.add(new MembersListSpinnerItem(MembersListType.EXITED_AND_RESIDENTS));

        ArrayAdapter<MembersListSpinnerItem> adapter = new ArrayAdapter<>(this.getContext(), R.layout.household_members_spinner_item, R.id.txtMembersListItem, list);
        membersListSpinner.setAdapter(adapter);
    }

    private void onMemberClicked(int position) {
        MemberAdapter adapter = (MemberAdapter) this.lvHouseholdMembers.getAdapter();
        Member member = adapter.getItem(position);

        MemberSelectedTask task = new MemberSelectedTask(member, household);
        task.execute();

        showLoadingDialog(getString(R.string.loading_dialog_member_details_lbl), true);
    }

    public Household getHousehold() {
        return household;
    }

    public void setHousehold(Household household) {
        this.household = household;
    }

    private Region getRegion(String code){

        Region region = this.boxRegions.query().equal(Region_.code, code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        return region;
    }

    private void onMembersListSelected(MembersListType type) {
        switch (type){
            case RESIDENTS: showHouseholdResidents(); break;
            case EXITED_AND_RESIDENTS: showHouseholdResidentsAndExited();
        }
    }

    private void showHouseholdResidents(){
        List<Member> members = this.boxMembers.query().equal(Member_.householdCode, household.getCode(), QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                      .equal(Member_.endType, ResidencyEndType.NOT_APPLICABLE.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                      .build().find();

        MemberAdapter adapter = new MemberAdapter(this.getContext(), members);
        adapter.setShowHouseholdHeadIcon(true);
        adapter.setShowExtraDetails(true);
        adapter.setShowMemberDetails(true);
        this.lvHouseholdMembers.setAdapter(adapter);
    }

    private void showHouseholdResidentsAndExited(){
        List<Member> members = this.boxMembers.query().equal(Member_.householdCode, household.getCode(), QueryBuilder.StringOrder.CASE_SENSITIVE)
                .build().find();

        MemberAdapter adapter = new MemberAdapter(this.getContext(), members);
        adapter.setShowHouseholdHeadIcon(true);
        adapter.setShowExtraDetails(true);
        adapter.setShowMemberDetails(true);
        adapter.setShowResidencyStatus(true);
        this.lvHouseholdMembers.setAdapter(adapter);
    }

    private void showLoadingDialog(String msg, boolean show){
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.dismiss();
        }
    }

    public void reloadMembers() {
        this.showHouseholdResidents();
    }

    class MemberSelectedTask  extends AsyncTask<Void, Void, Void> {
        private Household household;
        private Member member;
        private Region region;

        public MemberSelectedTask(Member member, Household household) {
            this.household = household;
            this.member = member;
            //this.region = region;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            this.region = getRegion(household.region);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = new Intent(getActivity(), MemberDetailsActivity.class);
            intent.putExtra("household", this.household.id);
            intent.putExtra("member", this.member.id);

            showLoadingDialog(null, false);

            startActivity(intent);
        }
    }

    private enum MembersListType {
        RESIDENTS (R.string.household_details_members_list_type_residents_lbl),
        EXITED_AND_RESIDENTS (R.string.household_details_members_list_type_exited_n_residents_lbl);

        @StringRes int name;

        MembersListType(@StringRes int name){
            this.name = name;
        }
    }

    class MembersListSpinnerItem {
        public MembersListType type;

        public MembersListSpinnerItem(MembersListType type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return getString(type.name);
        }
    }
}