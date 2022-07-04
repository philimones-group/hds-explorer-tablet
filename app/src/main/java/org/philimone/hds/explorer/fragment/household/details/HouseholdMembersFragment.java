package org.philimone.hds.explorer.fragment.household.details;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HouseholdMembersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HouseholdMembersFragment extends Fragment {

    private RecyclerListView lvHouseholdMembers;
    private LoadingDialog loadingDialog;

    private Household household;
    private User loggedUser;

    private Box<Region> boxRegions;
    private Box<Member> boxMembers;
    private Box<Form> boxForms;
    private Box<Dataset> boxDatasets;

    public HouseholdMembersFragment() {
        // Required empty public constructor
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.household_members, container, false);

        initBoxes();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialize(view);
    }

    private void initialize(View view) {
        lvHouseholdMembers = view.findViewById(R.id.lvHouseholdMembers);

        lvHouseholdMembers.addOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long id) {
                onMemberClicked(position);
            }

            @Override
            public void onItemLongClick(View view, int position, long id) {

            }
        });

        this.loadingDialog = new LoadingDialog(this.getContext());

        this.showHouseholdMembers();
    }

    private void initBoxes() {
//        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
//        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        this.boxDatasets = ObjectBoxDatabase.get().boxFor(Dataset.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
//        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
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

    private void showHouseholdMembers(){
        List<Member> members = this.boxMembers.query().equal(Member_.householdCode, household.getCode(), QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                      .equal(Member_.endType, ResidencyEndType.NOT_APPLICABLE.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                      .build().find();

        MemberAdapter adapter = new MemberAdapter(this.getContext(), members);
        adapter.setShowHouseholdHeadIcon(true);
        //adapter.setShowExtraDetails(true);
        this.lvHouseholdMembers.setAdapter(adapter);
    }

    private void showLoadingDialog(String msg, boolean show){
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.hide();
        }
    }

    public void reloadMembers() {
        this.showHouseholdMembers();
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
            intent.putExtra("household", this.household);
            intent.putExtra("member", this.member);

            showLoadingDialog(null, false);

            startActivity(intent);
        }
    }
}