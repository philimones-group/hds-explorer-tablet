package net.manhica.dss.explorer.main;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import net.manhica.dss.explorer.R;
import net.manhica.dss.explorer.adapter.MemberArrayAdapter;
import net.manhica.dss.explorer.data.FormDataLoader;
import net.manhica.dss.explorer.database.Database;
import net.manhica.dss.explorer.database.DatabaseHelper;
import net.manhica.dss.explorer.database.Queries;
import net.manhica.dss.explorer.fragment.MemberFilterFragment;
import net.manhica.dss.explorer.fragment.MemberListFragment;
import net.manhica.dss.explorer.listeners.MemberActionListener;
import net.manhica.dss.explorer.model.Form;
import net.manhica.dss.explorer.model.Household;
import net.manhica.dss.explorer.model.Member;
import net.manhica.dss.explorer.model.Module;
import net.manhica.dss.explorer.model.User;

import java.util.List;

public class SurveyMembersActivity extends Activity implements MemberFilterFragment.Listener, MemberActionListener {

    private MemberFilterFragment memberFilterFragment;
    private MemberListFragment memberListFragment;

    private User loggedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_members);

        this.loggedUser = (User) getIntent().getExtras().get("user");

        this.memberFilterFragment = (MemberFilterFragment) (getFragmentManager().findFragmentById(R.id.memberFilterFragment));
        this.memberListFragment = (MemberListFragment) getFragmentManager().findFragmentById(R.id.memberListFragment);
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
