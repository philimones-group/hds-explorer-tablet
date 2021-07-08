package org.philimone.hds.explorer.fragment.household.details;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.MemberArrayAdapter;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.data.FormFilter;
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
import org.philimone.hds.explorer.widget.LoadingDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.objectbox.Box;
import mz.betainteractive.utilities.StringUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HouseholdMembersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HouseholdMembersFragment extends Fragment {

    private ListView lvHouseholdMembers;
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

        lvHouseholdMembers.setOnItemClickListener((parent, view1, position, id) -> onMemberClicked(position));

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
        MemberArrayAdapter adapter = (MemberArrayAdapter) this.lvHouseholdMembers.getAdapter();
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

        Region region = this.boxRegions.query().equal(Region_.code, code).build().findFirst();

        return region;
    }

    private void showHouseholdMembers(){
        List<Member> members = this.boxMembers.query().equal(Member_.householdCode, household.getCode()).build().find();

        MemberArrayAdapter adapter = new MemberArrayAdapter(this.getContext(), members);
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

    public void updateHouseholdMembers() {
        this.showHouseholdMembers();
    }

    /*
    * Loaders
    */
    FormDataLoader[] getFormLoaders(org.philimone.hds.explorer.data.FormFilter... filters) {
        return FormDataLoader.getFormLoaders(boxForms, loggedUser, filters);
    }

    private void loadFormValues(FormDataLoader[] loaders, Household household, Member member, Region region){
        for (FormDataLoader loader : loaders){
            loadFormValues(loader, household, member, region);
        }
    }

    private void loadFormValues(FormDataLoader loader, Household household, Member member, Region region){
        if (household != null){
            loader.loadHouseholdValues(household);
        }
        if (member != null){
            loader.loadMemberValues(member);
        }
        if (loggedUser != null){
            loader.loadUserValues(loggedUser);
        }
        if (region != null){
            loader.loadRegionValues(region);
        }

        loader.loadConstantValues();
        loader.loadSpecialConstantValues(household, member, loggedUser, region, null);

        //Load variables on datasets
        for (Dataset dataSet : getDataSets()){
            Log.d("has-mapped-datasets", dataSet.getName()+", "+loader.hasMappedDatasetVariable(dataSet));
            if (loader.hasMappedDatasetVariable(dataSet)){
                Log.d("hasMappedVariables", ""+dataSet.getName());
                loader.loadDataSetValues(dataSet, household, member, loggedUser, region);
            }
        }
    }

    private List<Dataset> getDataSets(){
        List<Dataset> list = this.boxDatasets.getAll();
        return list;
    }

    class MemberSelectedTask  extends AsyncTask<Void, Void, Void> {
        private Household household;
        private Member member;
        private Region region;
        private FormDataLoader[] dataLoaders;

        public MemberSelectedTask(Member member, Household household) {
            this.household = household;
            this.member = member;
            //this.region = region;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            this.region = getRegion(household.getCode());

            dataLoaders = getFormLoaders(FormFilter.HOUSEHOLD_HEAD, FormFilter.MEMBER);
            loadFormValues(dataLoaders, household, member, region);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = new Intent(getActivity(), MemberDetailsActivity.class);
            intent.putExtra("user", loggedUser);
            intent.putExtra("member", this.member);
            intent.putExtra("dataloaders", dataLoaders);

            showLoadingDialog(null, false);

            startActivity(intent);
        }
    }
}