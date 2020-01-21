package org.philimone.hds.explorer.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mapswithme.maps.api.MWMPoint;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.MemberArrayAdapter;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.Database;
import org.philimone.hds.explorer.database.DatabaseHelper;
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
import org.philimone.hds.explorer.widget.LoadingDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mz.betainteractive.utilities.StringUtil;

public class SurveyHouseholdsActivity extends Activity implements HouseholdFilterFragment.Listener, MemberActionListener {

    private HouseholdFilterFragment householdFilterFragment;
    private MemberListFragment memberListFragment;

    private User loggedUser;
    private boolean censusMode = true;

    private LoadingDialog loadingDialog;

    private final int MEMBER_DETAILS_REQUEST_CODE = 31;

    public enum FormFilter {
        REGION, HOUSEHOLD, HOUSEHOLD_HEAD, MEMBER, FOLLOW_UP
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_households);

        this.loggedUser = (User) getIntent().getExtras().get("user");
        //this.censusMode = getIntent().getExtras().getBoolean("censusMode");

        this.householdFilterFragment = (HouseholdFilterFragment) (getFragmentManager().findFragmentById(R.id.householdFilterFragment));
        this.memberListFragment = (MemberListFragment) getFragmentManager().findFragmentById(R.id.memberListFragment);

        initialize();
    }

    private void initialize() {
        this.memberListFragment.setButtonVisibilityGone(MemberListFragment.Buttons.CLOSEST_MEMBERS);
        this.memberListFragment.setButtonEnabled(hasMemberBoundForms(), MemberListFragment.Buttons.NEW_MEMBER_COLLECT);

        //this.memberListFragment.setHouseholdHeaderVisibility(true);
        //this.memberListFragment.setCensusMode(censusMode);
        this.householdFilterFragment.setCensusMode(censusMode);

        if (censusMode){
            //this.memberListFragment.setButtonVisibilityGone(MEMBERS_MAP, CLOSEST_MEMBERS, NEW_MEMBER_COLLECT);
            //this.memberListFragment.setButtonEnabled(false, ADD_NEW_MEMBER, EDIT_MEMBER, CLOSEST_HOUSES);

        } else{
            //this.memberListFragment.setButtonVisibilityGone(ADD_NEW_MEMBER, EDIT_MEMBER, MEMBERS_MAP, CLOSEST_MEMBERS);
        }

        this.householdFilterFragment.setLoggedUser(loggedUser);

        this.loadingDialog = new LoadingDialog(this);
    }

    @Override
    public void onAddNewHousehold(Region region) {
        FormDataLoader[] dataLoaders = getFormLoaders();

        Intent intent = new Intent(this, HouseholdDetailsActivity.class);
        intent.putExtra("user", loggedUser);
        intent.putExtra("region", region);
        intent.putExtra("dataloaders", dataLoaders);
        intent.putExtra("request_code", HouseholdDetailsActivity.REQUEST_CODE_NEW_HOUSEHOLD);

        startActivityForResult(intent, HouseholdDetailsActivity.REQUEST_CODE_NEW_HOUSEHOLD);
    }

    @Override
    public void onEditHousehold(Household household) {
        FormDataLoader[] dataLoaders = getFormLoaders();

        Intent intent = new Intent(this, HouseholdDetailsActivity.class);
        intent.putExtra("user", loggedUser);
        intent.putExtra("dataloaders", dataLoaders);
        intent.putExtra("household", household);
        intent.putExtra("request_code", HouseholdDetailsActivity.REQUEST_CODE_NEW_HOUSEHOLD);

        startActivityForResult(intent, HouseholdDetailsActivity.REQUEST_CODE_NEW_HOUSEHOLD);
    }

    @Override
    public void onHouseholdClick(Household household) {
        MemberSearchTask task = new MemberSearchTask(household, null, null, null, household.getCode());
        task.execute();
    }

    @Override
    public void onSelectedRegion(Region region) {
        FormDataLoader[] dataLoaders = getFormLoaders(FormFilter.REGION);
        this.householdFilterFragment.checkSupportForRegionForms(dataLoaders);
    }

    @Override
    public void onRegionCollectDataClicked(Region region) {
        ShowRegionTask task = new ShowRegionTask(region);
        task.execute();

        showLoadingDialog(getString(R.string.loading_dialog_region_details_lbl), true);
    }

    @Override
    public void onMemberSelected(Household household, Member member, Region region) {
        OnMemberSelectedTask task = new OnMemberSelectedTask(household, member, region);
        task.execute();

        showLoadingDialog(getString(R.string.loading_dialog_member_details_lbl), true);
    }

    @Override
    public void onShowHouseholdClicked(Household household, Member member, Region region) {
        ShowHouseholdTask task = new ShowHouseholdTask(household, member, region);
        task.execute();

        showLoadingDialog(getString(R.string.loading_dialog_household_details_lbl), true);
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
            //Log.d("has-mapped-datasets", dataSet.getName()+", "+loader.hasMappedDatasetVariable(dataSet));
            if (loader.hasMappedDatasetVariable(dataSet)){
                //Log.d("hasMappedVariables", ""+dataSet.getName());
                loader.loadDataSetValues(dataSet, household, member, loggedUser, region);
            }
        }
    }

    private Set<DataSet> getLoadableDatasets(FormDataLoader[] loaders){
        Set<DataSet> list = new HashSet<>();
        List<DataSet> dataSets = getDataSets();

        for (FormDataLoader loader : loaders)
        for (DataSet dataSet : dataSets){
            if (loader.hasMappedDatasetVariable(dataSet)){
                list.add(dataSet);
            }
        }
        return list;
    }

    private void loadFormValues(FormDataLoader[] loaders, Household household, Member member, Region region){

        //get loadable datasets - to prevent reading unnecessary data
        //Set<DataSet> dataSets = getLoadableDatasets(loaders);

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

    private Household getHousehold(String code){
        if (code == null) return null;

        Database database = new Database(this);
        database.open();
        Household household = Queries.getHouseholdBy(database, DatabaseHelper.Household.COLUMN_CODE+"=?", new String[]{ code });
        database.close();

        return household;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MEMBER_DETAILS_REQUEST_CODE){ //if it is a return from MemberDetails - check if it was a new individual and thn update the list members

            if (data != null && data.getExtras() != null){
                String houseCode = data.getExtras().getString("household_code");
                Boolean isNewMember = data.getExtras().getBoolean("is_new_member");

                if (isNewMember != null && isNewMember==true){
                    Household household = getHousehold(houseCode);
                    MemberSearchTask task = new MemberSearchTask(household, null, null, null, houseCode);
                    task.execute();
                }
                //Log.d("request code data1", ""+data.getExtras().getString("household_code"));
                //Log.d("request code data2", ""+data.getExtras().getBoolean("is_new_member"));
            }

        }

        if (requestCode == HouseholdDetailsActivity.REQUEST_CODE_NEW_HOUSEHOLD && resultCode==RESULT_OK){ //If result is CANCELED just do nothing
            //Household added

            Household household = (Household) data.getExtras().get("household");

            if (household != null){
                householdFilterFragment.searchHouses(household.getCode());
                onHouseholdClick(household);
            }
        }
    }

    private void showLoadingDialog(String msg, boolean show){
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.hide();
        }
    }

    class MemberSearchTask extends AsyncTask<Void, Void, MemberArrayAdapter> {
        private String name;
        private String code;
        private String gender;
        private String houseCode;
        private Household household;

        public MemberSearchTask(Household household, String name, String code, String gender, String houseCode) {
            this.name = name;
            this.code = code;
            this.gender = gender;
            this.houseCode = houseCode;
            this.household = household;
        }

        @Override
        protected MemberArrayAdapter doInBackground(Void... params) {
            return memberListFragment.loadMembersByFilters(household, name, code, houseCode, gender, null, null, null, null, null);
        }

        @Override
        protected void onPostExecute(MemberArrayAdapter adapter) {
            memberListFragment.setMemberAdapter(adapter);
            memberListFragment.setCurrentHouseld(household);
            memberListFragment.showProgress(false);
            memberListFragment.setButtonEnabled(true, MemberListFragment.Buttons.SHOW_HOUSEHOLD);
        }
    }

    class OnMemberSelectedTask  extends AsyncTask<Void, Void, Void> {
        private Household household;
        private Member member;
        private Region region;
        private FormDataLoader[] dataLoaders;

        public OnMemberSelectedTask(Household household, Member member, Region region) {
            this.household = household;
            this.member = member;
            this.region = region;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            dataLoaders = getFormLoaders(FormFilter.HOUSEHOLD_HEAD, FormFilter.MEMBER);
            loadFormValues(dataLoaders, household, member, region);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = new Intent(SurveyHouseholdsActivity.this, MemberDetailsActivity.class);
            intent.putExtra("user", loggedUser);
            intent.putExtra("member", this.member);
            intent.putExtra("dataloaders", dataLoaders);

            showLoadingDialog(null, false);

            startActivityForResult(intent, MEMBER_DETAILS_REQUEST_CODE);
        }
    }

    class ShowHouseholdTask extends AsyncTask<Void, Void, Void> {
        private Household household;
        private Member member;
        private Region region;
        private FormDataLoader[] dataLoaders;

        public ShowHouseholdTask(Household household, Member member, Region region) {
            this.household = household;
            this.member = member;
            this.region = region;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            this.dataLoaders = getFormLoaders(FormFilter.HOUSEHOLD);
            loadFormValues(dataLoaders, household, member, region);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = new Intent(SurveyHouseholdsActivity.this, HouseholdDetailsActivity.class);
            intent.putExtra("user", loggedUser);
            intent.putExtra("household", household);
            intent.putExtra("dataloaders", dataLoaders);

            showLoadingDialog(null, false);

            startActivity(intent);
        }
    }

    class ShowRegionTask extends AsyncTask<Void, Void, Void> {
        private Region region;
        private FormDataLoader[] dataLoaders;

        public ShowRegionTask(Region region) {
            this.region = region;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            this.dataLoaders = getFormLoaders(FormFilter.REGION);
            loadFormValues(dataLoaders, null, null, region);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            showLoadingDialog(null, false);

            Intent intent = new Intent(SurveyHouseholdsActivity.this, RegionDetailsActivity.class);
            intent.putExtra("user", loggedUser);
            intent.putExtra("region", region);
            intent.putExtra("dataloaders", dataLoaders);

            startActivity(intent);
        }
    }

}
