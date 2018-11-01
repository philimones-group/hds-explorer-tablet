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
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.User;

import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.utilities.StringUtil;

public class SurveyMembersActivity extends Activity implements MemberFilterFragment.Listener, MemberActionListener {

    private MemberFilterFragment memberFilterFragment;
    private MemberListFragment memberListFragment;

    private User loggedUser;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_members);

        this.database = new Database(this);

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
    public void onSearch(String name, String permId, String houseNumber, String gender, Integer minAge, Integer maxAge, Boolean isDead, Boolean hasOutmigrated, Boolean liveResident) {
        this.memberListFragment.showProgress(true);

        MemberSearchTask task = new MemberSearchTask(name, permId, houseNumber, gender, minAge, maxAge, isDead, hasOutmigrated, liveResident);
        task.execute();
    }

    @Override
    public void onMemberSelected(Household household, Member member) {
        FormDataLoader[] dataLoaders = getFormLoaders();
        loadFormValues(dataLoaders, household, member);

        Intent intent = new Intent(this, MemberDetailsActivity.class);
        intent.putExtra("user", loggedUser);
        intent.putExtra("member", member);
        intent.putExtra("dataloaders", dataLoaders);

        startActivity(intent);
    }

    @Override
    public void onClosestMembersResult(Member member, MWMPoint[] points, MWMPoint[] originalPoints, ArrayList<Member> members) {
        FormDataLoader[] dataLoaders = getFormLoaders();
        Household household = getHousehold(member);
        loadFormValues(dataLoaders, household, member);

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
        if (member == null || member.getHouseNumber()==null) return null;

        database.open();
        Household household = Queries.getHouseholdBy(database, DatabaseHelper.Household.COLUMN_NAME +"=?", new String[]{ member.getHouseNumber() });
        database.close();

        return household;
    }

    public FormDataLoader[] getFormLoaders(){

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
                list.add(loader);
            }
        }

        FormDataLoader[] aList = new FormDataLoader[list.size()];

        return list.toArray(aList);
    }

    private boolean hasMemberBoundForms(){
        for (FormDataLoader fdls : getFormLoaders()){
            if (fdls.getForm().isMemberForm()){
                return true;
            }
        }
        return false;
    }

    private boolean hasHouseholdBoundForms(){
        for (FormDataLoader fdls : getFormLoaders()){
            if (fdls.getForm().isHouseholdForm()){
                return true;
            }
        }
        return false;
    }

    private void loadFormValues(FormDataLoader loader, Household household, Member member){
        if (household != null){
            loader.loadHouseholdValues(household);
        }
        if (member != null){
            loader.loadMemberValues(member);
        }
        if (loggedUser != null){
            loader.loadUserValues(loggedUser);
        }

        loader.loadConstantValues();
        loader.loadSpecialConstantValues(household, member, loggedUser, null);
    }

    private void loadFormValues(FormDataLoader[] loaders, Household household, Member member){
        for (FormDataLoader loader : loaders){
            loadFormValues(loader, household, member);
        }
    }

    class MemberSearchTask extends AsyncTask<Void, Void, MemberArrayAdapter> {
        private String name;
        private String permId;
        private String gender;
        private String houseNr;
        private Integer minAge;
        private Integer maxAge;
        private Boolean dead;
        private Boolean outmigrated;
        private Boolean resident;

        public MemberSearchTask(String name, String permId, String houseNumber, String gender, Integer minAge, Integer maxAge, Boolean isDead, Boolean hasOutmigrated, Boolean liveResident) {
            this.name = name;
            this.permId = permId;
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
            return memberListFragment.loadMembersByFilters(null, name, permId, houseNr, gender, minAge, maxAge, dead, outmigrated, resident);
        }

        @Override
        protected void onPostExecute(MemberArrayAdapter adapter) {
            memberListFragment.setMemberAdapter(adapter);
            memberListFragment.showProgress(false);
        }
    }
}
