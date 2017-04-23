package net.manhica.dss.explorer.main;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import net.manhica.dss.explorer.R;
import net.manhica.dss.explorer.adapter.MemberArrayAdapter;
import net.manhica.dss.explorer.data.FormDataLoader;
import net.manhica.dss.explorer.database.Database;
import net.manhica.dss.explorer.database.DatabaseHelper;
import net.manhica.dss.explorer.database.Queries;
import net.manhica.dss.explorer.fragment.HouseholdFilterFragment;
import net.manhica.dss.explorer.fragment.MemberListFragment;
import net.manhica.dss.explorer.listeners.MemberActionListener;
import net.manhica.dss.explorer.model.Form;
import net.manhica.dss.explorer.model.Household;
import net.manhica.dss.explorer.model.Member;
import net.manhica.dss.explorer.model.Module;
import net.manhica.dss.explorer.model.User;

import java.util.List;

public class SurveyHouseholdsActivity extends Activity implements HouseholdFilterFragment.Listener, MemberActionListener {

    private HouseholdFilterFragment householdFilterFragment;
    private MemberListFragment memberListFragment;

    private User loggedUser;

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
        MemberSearchTask task = new MemberSearchTask(household, null, null, null, household.getHouseNumber());
        task.execute();
    }

    @Override
    public void onMemberSelected(Household household, Member member) {
        FormDataLoader[] dataLoaders = getFormLoaders();
        loadFormValues(dataLoaders, household, member);

        Intent intent = new Intent(this, MemberDetailsActivity.class);
        intent.putExtra("member", member);
        intent.putExtra("dataloaders", dataLoaders);

        startActivity(intent);
    }

    public FormDataLoader[] getFormLoaders(){

        Database db = new Database(this);

        db.open();
        List<Form> forms = Queries.getAllFormBy(db, DatabaseHelper.Form.COLUMN_MODULES+" like ?", new String[]{ "%" + Module.DSS_SURVEY_MODULE + "%" });
        db.close();

        FormDataLoader[] list = new FormDataLoader[forms.size()];

        int i=0;
        for (Form form : forms){
            FormDataLoader loader = new FormDataLoader(form);
            list[i++] = loader;
        }

        return list;
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
        loader.loadSpecialConstantValues(household, member, loggedUser);
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
        private String houseNumber;
        private Household household;

        public MemberSearchTask(Household household, String name, String permId, String gender, String houseNumber) {
            this.name = name;
            this.permId = permId;
            this.gender = gender;
            this.houseNumber = houseNumber;
            this.household = household;
        }

        @Override
        protected MemberArrayAdapter doInBackground(Void... params) {
            return memberListFragment.loadMembersByFilters(household, name, permId, houseNumber, gender, null, null, null, null, null);
        }

        @Override
        protected void onPostExecute(MemberArrayAdapter adapter) {
            memberListFragment.setMemberAdapter(adapter);
            memberListFragment.showProgress(false);
        }
    }

}
