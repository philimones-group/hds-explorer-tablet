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
import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.MemberFilterFragment;
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

public class SurveyMembersActivity extends Activity implements MemberFilterFragment.Listener, MemberActionListener {

    private MemberFilterFragment memberFilterFragment;
    private MemberListFragment memberListFragment;

    private User loggedUser;

    public enum FormFilter {
        REGION, HOUSEHOLD, HOUSEHOLD_HEAD, MEMBER, FOLLOW_UP
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_members);

        this.loggedUser = (User) getIntent().getExtras().get("user");

        this.memberFilterFragment = (MemberFilterFragment) (getFragmentManager().findFragmentById(R.id.memberFilterFragment));
        this.memberListFragment = (MemberListFragment) getFragmentManager().findFragmentById(R.id.memberListFragment);

        initialize();
    }

    private void initialize() {
        this.memberListFragment.setButtonVisibilityGone(MemberListFragment.Buttons.CLOSEST_HOUSES);
        this.memberListFragment.setButtonEnabled(hasMemberBoundForms(), MemberListFragment.Buttons.NEW_MEMBER_COLLECT);
    }

    @Override
    public void onSearch(String name, String code, String houseNumber, String gender, Integer minAge, Integer maxAge, Boolean isDead, Boolean hasOutmigrated, Boolean liveResident) {
        this.memberListFragment.showProgress(true);

        MemberSearchTask task = new MemberSearchTask(name, code, houseNumber, gender, minAge, maxAge, isDead, hasOutmigrated, liveResident);
        task.execute();
    }

    @Override
    public void onMemberSelected(Household household, Member member, Region region) {
        FormDataLoader[] dataLoaders = getFormLoaders(FormFilter.HOUSEHOLD_HEAD, FormFilter.MEMBER); //I only need MemberForm and HouseholdHead
        loadFormValues(dataLoaders, household, member, region);

        Intent intent = new Intent(this, MemberDetailsActivity.class);
        intent.putExtra("user", loggedUser);
        intent.putExtra("member", member);
        intent.putExtra("dataloaders", dataLoaders);

        startActivity(intent);
    }

    @Override
    public void onShowHouseholdClicked(Household household, Member member, Region region) {
        FormDataLoader[] dataLoaders = getFormLoaders(FormFilter.HOUSEHOLD); //Only need Household Forms
        loadFormValues(dataLoaders, household, member, region);

        Intent intent = new Intent(this, HouseholdDetailsActivity.class);
        intent.putExtra("user", loggedUser);
        intent.putExtra("household", household);
        intent.putExtra("dataloaders", dataLoaders);

        startActivity(intent);
    }

    @Override
    public void onClosestMembersResult(Member member, MWMPoint[] points, MWMPoint[] originalPoints, ArrayList<Member> members) {
        FormDataLoader[] dataLoaders = getFormLoaders(FormFilter.HOUSEHOLD_HEAD, FormFilter.MEMBER); //only members
        Household household = getHousehold(member);
        loadFormValues(dataLoaders, household, member, null);

        Intent intent = new Intent(this, GpsSearchedListActivity.class);
        intent.putExtra("main_member", member);
        intent.putExtra("main_household", household);
        intent.putExtra("members", members);
        intent.putExtra("points", points);
        intent.putExtra("points_original", originalPoints);

        intent.putExtra("dataloaders", dataLoaders);

        startActivity(intent);
    }

    @Override
    public void onClosestHouseholdsResult(Household household, MWMPoint[] points, ArrayList<Household> households) {

    }

    private Household getHousehold(Member member){
        if (member == null || member.getHouseholdCode()==null) return null;

        Database db = new Database(this);
        db.open();
        Household household = Queries.getHouseholdBy(db, DatabaseHelper.Household.COLUMN_CODE +"=?", new String[]{ member.getHouseholdCode() });
        db.close();

        return household;
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
            Log.d("forms", ""+loggedUser.getModules() +" - " + form.getModules() );
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

    private List<FormFilter> toList(FormFilter... filters){
        List<FormFilter> list = new ArrayList<>();
        for (FormFilter f : filters){
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
        private String houseNr;
        private Integer minAge;
        private Integer maxAge;
        private Boolean dead;
        private Boolean outmigrated;
        private Boolean resident;

        public MemberSearchTask(String name, String code, String houseNumber, String gender, Integer minAge, Integer maxAge, Boolean isDead, Boolean hasOutmigrated, Boolean liveResident) {
            this.name = name;
            this.code = code;
            this.houseNr = houseNumber;
            this.gender = gender;
            this.minAge = minAge;
            this.maxAge = maxAge;
            this.dead = isDead;
            this.outmigrated = hasOutmigrated;
            this.resident = liveResident;
        }

        @Override
        protected MemberArrayAdapter doInBackground(Void... params) {
            return memberListFragment.loadMembersByFilters(null, name, code, houseNr, gender, minAge, maxAge, dead, outmigrated, resident);
        }

        @Override
        protected void onPostExecute(MemberArrayAdapter adapter) {
            memberListFragment.setMemberAdapter(adapter);
            memberListFragment.showProgress(false);

            if (adapter.isEmpty()){
                memberListFragment.showMemberNotFoundMessage();
            }
        }
    }
}
