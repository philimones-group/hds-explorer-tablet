package net.manhica.clip.explorer.main;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import net.manhica.clip.explorer.R;
import net.manhica.clip.explorer.adapter.MemberArrayAdapter;
import net.manhica.clip.explorer.data.FormDataLoader;
import net.manhica.clip.explorer.data.FormDataLoaderList;
import net.manhica.clip.explorer.database.Database;
import net.manhica.clip.explorer.database.DatabaseHelper;
import net.manhica.clip.explorer.database.Queries;
import net.manhica.clip.explorer.fragment.MemberFilterFragment;
import net.manhica.clip.explorer.fragment.MemberListFragment;
import net.manhica.clip.explorer.listeners.ActionListener;
import net.manhica.clip.explorer.listeners.MemberActionListener;
import net.manhica.clip.explorer.model.Form;
import net.manhica.clip.explorer.model.Member;
import net.manhica.clip.explorer.model.User;

import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.model.FilledForm;

public class FacilityActivity extends Activity  implements MemberFilterFragment.Listener, MemberActionListener {

    private MemberFilterFragment memberFilterFragment;
    private MemberListFragment memberListFragment;
    private ActionListener btAddNewPatientListener;

    private User loggedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facility);

        this.loggedUser = (User) getIntent().getExtras().get("user");

        initListeners();

        this.memberFilterFragment = (MemberFilterFragment) (getFragmentManager().findFragmentById(R.id.facMemberFilterFragment));
        this.memberListFragment = (MemberListFragment) getFragmentManager().findFragmentById(R.id.facMemberListFragment);

        this.memberFilterFragment.setListener(this);
        this.memberListFragment.setOnMemberClickedListener(this);

        this.memberListFragment.removeDefaultButtons();
        this.memberListFragment.addButton("Add New Patient", btAddNewPatientListener);
    }

    private void initListeners() {
        btAddNewPatientListener = new ActionListener() {
            @Override
            public void execute() {
                openNewPatientForm();
            }
        };
    }

    @Override
    public void onSearch(String name, String permId, String gender, boolean isPregnant, boolean hasPom, boolean hasFacility) {
        this.memberListFragment.showProgress(true);

        MemberSearchTask task = new MemberSearchTask(name, permId, gender, isPregnant, hasPom, hasFacility);
        task.execute();
    }

    @Override
    public void onMemberSelected(Member member) {
        FormDataLoader[] dataLoaders = getFormLoaders();
        loadFormValues(dataLoaders, member);

        Intent intent = new Intent(this, MemberDetailsActivity.class);
        intent.putExtra("member", member);
        intent.putExtra("dataloaders", dataLoaders);

        startActivity(intent);
    }

    public FormDataLoader[] getFormLoaders(){

        String[] extras = loggedUser.getExtras().split(",");

        Database db = new Database(this);
        db.open();

        List<Form> formFacilities = Queries.getAllFormBy(db, DatabaseHelper.Form.COLUMN_MODULES+"=?", new String[]{ "CLIP-FACILITY" });

        db.close();

        FormDataLoader[] list = new FormDataLoader[formFacilities.size()];

        int i=0;
        for (Form formFacility : formFacilities){
            FormDataLoader loader = new FormDataLoader(formFacility);

            loader.putExtra("facility", extras[0]); //health facility number
            loader.putExtra("fieldWorkerName", loggedUser.getFullname());
            loader.putExtra("fieldWorkerId", loggedUser.getUsername());

            list[i++] = loader;
        }

        return list;
    }

    private void loadFormValues(FormDataLoader loader, Member member){
        loader.putExtra("womanOnSystem", member!=null ? "1" : "2");
        loader.putExtra("belongsToCLIP", member!=null ? "1" : "");
        loader.putExtra("name", member!=null ? member.getName() : "");
        loader.putExtra("hasClipId", member!=null ? (member.getLastClipId().isEmpty() ? "2": "1") : "2");
        loader.putExtra("clip_id", member!=null ? member.getLastClipId() : "");
    }

    private void loadFormValues(FormDataLoader[] loaders, Member member){
        for (FormDataLoader loader : loaders){
            loadFormValues(loader, member);
        }
    }

    private void openNewPatientForm() {
        FormDataLoader[] dataLoaders = getFormLoaders();
        FormDataLoader formDataLoader = dataLoaders[0];
        loadFormValues(dataLoaders, null);

        FormUtilities formUtilities = new FormUtilities(this);

        Form form = formDataLoader.getForm();

        FilledForm filledForm = new FilledForm(form.getFormId());
        filledForm.putAll(formDataLoader.getValues());

        formUtilities.loadForm(filledForm);
    }

    class MemberSearchTask extends AsyncTask<Void, Void, MemberArrayAdapter> {
        private String name;
        private String permId;
        private String gender;
        private boolean isPregnant;
        private boolean hasPom;
        private boolean hasFacility;

        public MemberSearchTask(String name, String permId, String gender, boolean isPregant, boolean hasPom, boolean hasFacility) {
            this.name = name;
            this.permId = permId;
            this.gender = gender;
            this.isPregnant = isPregant;
            this.hasPom = hasPom;
            this.hasFacility = hasFacility;
        }

        @Override
        protected MemberArrayAdapter doInBackground(Void... params) {
            return memberListFragment.loadMembersByFilters(name, permId, gender, isPregnant, hasPom, hasFacility);
        }

        @Override
        protected void onPostExecute(MemberArrayAdapter adapter) {
            memberListFragment.setMemberAdapter(adapter);
            memberListFragment.showProgress(false);
        }
    }
}
