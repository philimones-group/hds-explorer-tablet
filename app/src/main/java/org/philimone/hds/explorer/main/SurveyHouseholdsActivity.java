package org.philimone.hds.explorer.main;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.mapswithme.maps.api.MWMPoint;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.MemberArrayAdapter;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.Database;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.HouseholdFilterFragment;
import org.philimone.hds.explorer.fragment.MemberListFragment;
import org.philimone.hds.explorer.listeners.MemberActionListener;
import org.philimone.hds.explorer.model.DataSet;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mz.betainteractive.utilities.StringUtil;

public class SurveyHouseholdsActivity extends Activity implements HouseholdFilterFragment.Listener, MemberActionListener {

    private HouseholdFilterFragment householdFilterFragment;
    private MemberListFragment memberListFragment;

    private User loggedUser;

    public enum FormFilter {
        REGION, HOUSEHOLD, HOUSEHOLD_HEAD, MEMBER, FOLLOW_UP
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_households);

        this.loggedUser = (User) getIntent().getExtras().get("user");

        this.householdFilterFragment = (HouseholdFilterFragment) (getFragmentManager().findFragmentById(R.id.householdFilterFragment));
        this.memberListFragment = (MemberListFragment) getFragmentManager().findFragmentById(R.id.memberListFragment);

        initialize();
    }

    private void initialize() {
        this.memberListFragment.setButtonVisibilityGone(MemberListFragment.Buttons.CLOSEST_MEMBERS);
        this.memberListFragment.setButtonEnabled(hasMemberBoundForms(), MemberListFragment.Buttons.NEW_MEMBER_COLLECT);
    }

    @Override
    public void onHouseholdClick(Household household) {
        Log.d("survey-household", ""+household);
        MemberSearchTask task = new MemberSearchTask(household, null, null, null, household.getName());
        task.execute();
    }

    @Override
    public void onSelectedRegion(Region region) {
        FormDataLoader[] dataLoaders = getFormLoaders(FormFilter.REGION);
        //loadFormValues(dataLoaders, null, null, region);

        this.householdFilterFragment.checkSupportForRegionForms(dataLoaders);
    }

    @Override
    public void onRegionCollectDataClicked(Region region) {
        FormDataLoader[] dataLoaders = getFormLoaders(FormFilter.REGION);
        loadFormValues(dataLoaders, null, null, region);

        Intent intent = new Intent(this, RegionDetailsActivity.class);
        intent.putExtra("user", loggedUser);
        intent.putExtra("region", region);
        intent.putExtra("dataloaders", dataLoaders);

        startActivity(intent);
    }

    @Override
    public void onMemberSelected(Household household, Member member, Region region) {
        FormDataLoader[] dataLoaders = getFormLoaders(FormFilter.HOUSEHOLD_HEAD, FormFilter.MEMBER);
        loadFormValues(dataLoaders, household, member, region);

        Intent intent = new Intent(this, MemberDetailsActivity.class);
        intent.putExtra("user", loggedUser);
        intent.putExtra("member", member);
        intent.putExtra("dataloaders", dataLoaders);

        startActivity(intent);
    }

    @Override
    public void onMemberHouseholdSelected(Household household, Member member, Region region) {
        FormDataLoader[] dataLoaders = getFormLoaders(FormFilter.HOUSEHOLD);
        loadFormValues(dataLoaders, household, member, region);

        Intent intent = new Intent(this, HouseholdDetailsActivity.class);
        intent.putExtra("user", loggedUser);
        intent.putExtra("household", household);
        intent.putExtra("dataloaders", dataLoaders);

        startActivity(intent);
    }

    @Override
    public void onClosestMembersResult(Member member, MWMPoint[] points, MWMPoint[] originalPoints, ArrayList<Member> members) {

    }

    @Override
    public void onClosestHouseholdsResult(Household household, MWMPoint[] points, ArrayList<Household> households) {
        FormDataLoader[] dataLoaders = getFormLoaders(FormFilter.HOUSEHOLD);
        loadFormValues(dataLoaders, household, null, null);

        Intent intent = new Intent(this, GpsSearchedListActivity.class);

        intent.putExtra("main_household", household);
        intent.putExtra("households", households);
        intent.putExtra("points", points);

        intent.putExtra("dataloaders", dataLoaders);

        startActivity(intent);
    }

    public FormDataLoader[] getFormLoaders(FormFilter... filters){

        List<FormFilter> listFilters = Arrays.asList(filters);

        String[] userModules = loggedUser.getModules().split(",");

        Database db = new Database(this);

        db.open();
        List<Form> forms = Queries.getAllFormBy(db, null, null); //get all forms
        db.close();

        List<FormDataLoader> list = new ArrayList<>();

        int i=0;
        for (Form form : forms){
            String[] formModules = form.getModules().split(",");

            if (StringUtil.containsAny(userModules, formModules)){ //if the user has access to module specified on Form
                FormDataLoader loader = new FormDataLoader(form);

                if (form.isFollowUpOnly() && listFilters.contains(FormFilter.FOLLOW_UP)){
                    list.add(loader);
                    continue;
                }
                if (form.isRegionForm() && listFilters.contains(FormFilter.REGION)){
                    list.add(loader);
                    continue;
                }
                if (form.isHouseholdForm() && listFilters.contains(FormFilter.HOUSEHOLD)){
                    list.add(loader);
                    continue;
                }
                if (form.isHouseholdHeadForm() && listFilters.contains(FormFilter.HOUSEHOLD_HEAD)){
                    list.add(loader);
                    continue;
                }
                if (form.isMemberForm() && listFilters.contains(FormFilter.MEMBER)){
                    list.add(loader);
                    continue;
                }
            }
        }

        FormDataLoader[] aList = new FormDataLoader[list.size()];

        return list.toArray(aList);
    }

    private List<SurveyMembersActivity.FormFilter> toList(SurveyMembersActivity.FormFilter... filters){
        List<SurveyMembersActivity.FormFilter> list = new ArrayList<>();
        for (SurveyMembersActivity.FormFilter f : filters){
            list.add(f);
        }
        return list;
    }


    private boolean hasMemberBoundForms(){
        for (FormDataLoader fdls : getFormLoaders(FormFilter.HOUSEHOLD_HEAD, FormFilter.MEMBER)){
            if (fdls.getForm().isMemberForm()){
                return true;
            }
        }
        return false;
    }

    private boolean hasHouseholdBoundForms(){
        for (FormDataLoader fdls : getFormLoaders(FormFilter.HOUSEHOLD)){
            if (fdls.getForm().isHouseholdForm()){
                return true;
            }
        }
        return false;
    }

    private boolean hasRegionBoundForms(){
        for (FormDataLoader fdls : getFormLoaders(FormFilter.REGION)){
            if (fdls.getForm().isRegionForm()){
                return true;
            }
        }
        return false;
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
        for (DataSet dataSet : getDataSets()){
            if (loader.hasMappedDatasetVariable(dataSet)){
                //Log.d("hasMappedVariables", ""+dataSet.getName());
                loader.loadDataSetValues(dataSet, household, member, loggedUser, region);
            }
        }
    }

    private void loadFormValues(FormDataLoader[] loaders, Household household, Member member, Region region){
        for (FormDataLoader loader : loaders){
            loadFormValues(loader, household, member, region);
        }
    }

    private List<DataSet> getDataSets(){
        List<DataSet> list = null;

        Database db = new Database(this);
        db.open();
        list = Queries.getAllDataSetBy(db, null, null);
        db.close();

        return list;
    }

    class MemberSearchTask extends AsyncTask<Void, Void, MemberArrayAdapter> {
        private String name;
        private String code;
        private String gender;
        private String houseNumber;
        private Household household;

        public MemberSearchTask(Household household, String name, String code, String gender, String houseNumber) {
            this.name = name;
            this.code = code;
            this.gender = gender;
            this.houseNumber = houseNumber;
            this.household = household;
        }

        @Override
        protected MemberArrayAdapter doInBackground(Void... params) {
            return memberListFragment.loadMembersByFilters(household, name, code, houseNumber, gender, null, null, null, null, null);
        }

        @Override
        protected void onPostExecute(MemberArrayAdapter adapter) {
            memberListFragment.setMemberAdapter(adapter);
            memberListFragment.showProgress(false);
            memberListFragment.setButtonEnabled(true, MemberListFragment.Buttons.SHOW_HOUSEHOLD);
        }
    }

}
