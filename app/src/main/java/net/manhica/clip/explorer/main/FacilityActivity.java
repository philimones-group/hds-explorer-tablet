package net.manhica.clip.explorer.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import net.manhica.clip.explorer.R;
import net.manhica.clip.explorer.adapter.MemberArrayAdapter;
import net.manhica.clip.explorer.data.FormDataLoader;
import net.manhica.clip.explorer.database.Converter;
import net.manhica.clip.explorer.database.Database;
import net.manhica.clip.explorer.database.DatabaseHelper;
import net.manhica.clip.explorer.database.Queries;
import net.manhica.clip.explorer.fragment.MemberFilterFragment;
import net.manhica.clip.explorer.fragment.MemberListFragment;
import net.manhica.clip.explorer.io.xml.FormXmlReader;
import net.manhica.clip.explorer.listeners.ActionListener;
import net.manhica.clip.explorer.listeners.MemberActionListener;
import net.manhica.clip.explorer.model.CollectedData;
import net.manhica.clip.explorer.model.Form;
import net.manhica.clip.explorer.model.Household;
import net.manhica.clip.explorer.model.Member;
import net.manhica.clip.explorer.model.Module;
import net.manhica.clip.explorer.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.listener.OdkFormResultListener;
import mz.betainteractive.odk.model.FilledForm;

public class FacilityActivity extends Activity  implements MemberFilterFragment.Listener, MemberActionListener, OdkFormResultListener {

    private MemberFilterFragment memberFilterFragment;
    private MemberListFragment memberListFragment;
    private ActionListener btAddNewPatientListener;
    private ActionListener btShowPatientsListener;
    private ActionListener btMarkAsSupervisedListener;
    private FormUtilities formUtilities;
    private Member selectedNewMember;
    private FormDataLoader lastLoadedForm;
    private boolean visualizingCollectedData;
    private Button btMarkAsSupervised;

    private User loggedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facility);

        this.loggedUser = (User) getIntent().getExtras().get("user");

        initListeners();

        this.memberFilterFragment = (MemberFilterFragment) (getFragmentManager().findFragmentById(R.id.facMemberFilterFragment));
        this.memberListFragment = (MemberListFragment) getFragmentManager().findFragmentById(R.id.facMemberListFragment);

        this.memberListFragment.removeDefaultButtons();

        if (!isSupervisor(loggedUser)){
            this.memberListFragment.addButton(getString(R.string.member_list_bt_add_patient_lbl), btAddNewPatientListener);
            this.memberListFragment.addButton(getString(R.string.member_list_bt_show_patients_lbl), btShowPatientsListener);
        }else{
            this.memberListFragment.addButton(getString(R.string.member_list_bt_show_patients_lbl), btShowPatientsListener);
            this.btMarkAsSupervised = this.memberListFragment.addButton(getString(R.string.member_list_bt_mark_as_supervised_lbl), btMarkAsSupervisedListener);

            this.btMarkAsSupervised.setEnabled(false);
        }


        formUtilities = new FormUtilities(this);
    }

    private void initListeners() {
        btAddNewPatientListener = new ActionListener() {
            @Override
            public void execute() {
                openNewPatientForm();
            }
        };

        btShowPatientsListener = new ActionListener() {
            @Override
            public void execute() {
                showCollectedMember();
            }
        };

        btMarkAsSupervisedListener = new ActionListener() {
            @Override
            public void execute() {
                markAllAsSupervised();
            }
        };
    }

    @Override
    public void onSearch(String name, String permId, String gender, boolean isPregnant, boolean hasPom, boolean hasFacility) {
        this.memberListFragment.showProgress(true);

        this.visualizingCollectedData = false;

        if (btMarkAsSupervised != null){
            btMarkAsSupervised.setEnabled(false);
        }

        MemberSearchTask task = new MemberSearchTask(name, permId, gender, isPregnant, hasPom, hasFacility);
        task.execute();
    }

    @Override
    public void onMemberSelected(Household household, Member member) {

        boolean isSupervisor = isSupervisor(loggedUser);

        if (member.getAge()==0 || (visualizingCollectedData && isSupervisor)){ //new added patient
            this.selectedNewMember = member;
            CollectedData collectedData = getCollectedData("Facility", member);
            FormDataLoader[] dataLoaders = getFormLoaders();

            this.lastLoadedForm = dataLoaders[0];

            if (collectedData != null){
                formUtilities.loadForm(collectedData.getFormUri());
            }

            return;
        }

        FormDataLoader[] dataLoaders = getFormLoaders();
        loadFormValues(dataLoaders, member);

        Intent intent = new Intent(this, MemberDetailsActivity.class);
        intent.putExtra("member", member);
        intent.putExtra("dataloaders", dataLoaders);

        if (isSupervisor){
            intent.putExtra("enable-collect-data", false);
        }else{
            intent.putExtra("enable-collect-data", true);
        }

        startActivity(intent);
    }

    private boolean isSupervisor(User user){
        String[] modules = user.getModules().split(",");

        for(String s : modules ){
            if (s.equals(Module.CLIP_SUPERVISOR)){
                return true;
            }
        }

        return false;
    }

    public FormDataLoader[] getFormLoaders(){

        String[] extras = loggedUser.getExtras().split(",");

        Database db = new Database(this);
        db.open();

        List<Form> formFacilities = Queries.getAllFormBy(db, DatabaseHelper.Form.COLUMN_MODULES+" like ?", new String[]{ "%"+ Module.CLIP_FACILITY_MODULE +"%" });

        db.close();

        FormDataLoader[] list = new FormDataLoader[formFacilities.size()];

        int i=0;
        for (Form formFacility : formFacilities){
            FormDataLoader loader = new FormDataLoader(formFacility);

            //loader.putExtra("facility", extras[0]); //health facility number
            loader.putExtra("fieldWorkerName", loggedUser.getFullname());
            //loader.putExtra("fieldWorkerId", loggedUser.getUsername());

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

    private void openNewPatientForm() {
        selectedNewMember = null;

        FormDataLoader[] dataLoaders = getFormLoaders();
        FormDataLoader formDataLoader = dataLoaders[0];
        loadFormValues(dataLoaders, null);

        Form form = formDataLoader.getForm();

        FilledForm filledForm = new FilledForm(form.getFormId());
        filledForm.putAll(formDataLoader.getValues());

        this.lastLoadedForm = formDataLoader;

        formUtilities.loadForm(filledForm);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        formUtilities.onActivityResult(requestCode, resultCode, data, this);
    }

    private void showCollectedMember() {
        this.memberListFragment.showProgress(true);

        this.visualizingCollectedData = true;

        CollectedMemberSearchTask task = new CollectedMemberSearchTask(isSupervisor(loggedUser));
        task.execute();
    }

    private void markAllAsSupervised(){
        //readcollectedData and mark as supervised='true'

        MemberArrayAdapter adapter = memberListFragment.getMemberAdapter();

        if (visualizingCollectedData==false || adapter == null){
            //without collected data to supervise

            return;
        }

        String ids = "";
        for (Member mb : adapter.getMembers()){
            if (ids.length() > 0){
                ids += ", "+mb.getId();
            }else{
                ids += ""+mb.getId();
            }
        }

        Database db = new Database(FacilityActivity.this);
        db.open();

        String whereClause = DatabaseHelper.CollectedData.COLUMN_FORM_ID + "=? AND "+DatabaseHelper.CollectedData.COLUMN_RECORD_ID + " IN ("+ids+")";
        String[] whereArgs = new String[]{ "Facility" };

        List<CollectedData> collectedDatas = Queries.getAllCollectedDataBy(db, whereClause, whereArgs);

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.CollectedData.COLUMN_SUPERVISED, 1);

        for (CollectedData cd : collectedDatas){
            db.update(CollectedData.class, cv, DatabaseHelper.CollectedData._ID+"=?", new String[]{ cd.getId()+"" });
        }
        db.close();

        buildMarkAsSupervisedFinishedDialog();
    }

    private void buildMarkAsSupervisedFinishedDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.facility_supervise_title_lbl));
        builder.setMessage(getString(R.string.facility_supervise_mark_all_done_lbl));
        builder.setPositiveButton(getString(R.string.bt_ok_lbl), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showCollectedMember();
            }
        });

        builder.show();
    }

    @Override
    public void onFormFinalized(Uri contentUri, File xmlFile) {
        Log.d("form finalized"," "+contentUri+", "+xmlFile);

        Member member = null;

        if (selectedNewMember == null){ //new member

            member = readMemberByOdk(xmlFile);
            int next = nextNewMember();

            member.setPermId("XX-0000-000-"+ String.format("%02d", next));
            member.setNrPregnancies(next);

            Database db = new Database(this);
            db.open();
            db.insert(member); //35-0000-000-99
            db.close();

            member = getMemberByExtId(member.getExtId());
        }else {
            member = selectedNewMember;
        }



        //save Collected data
        Database db = new Database(this);
        db.open();
        //update or insert

        //search existing record
        String whereClause = DatabaseHelper.CollectedData.COLUMN_RECORD_ID + "=? AND "+DatabaseHelper.CollectedData.COLUMN_FORM_URI + "=?";
        String[] whereArgs = new String[]{ ""+member.getId(), contentUri.toString() };

        CollectedData collectedData = Queries.getCollectedDataBy(db, whereClause, whereArgs);

        if (collectedData == null){ //insert
            collectedData = new CollectedData();
            collectedData.setFormId(lastLoadedForm.getForm().getFormId());
            collectedData.setFormUri(contentUri.toString());
            collectedData.setFormXmlPath(xmlFile.toString());
            collectedData.setRecordId(member.getId());
            collectedData.setTableName(member.getTableName());

            db.insert(collectedData);
            Log.d("fin-inserting", "new collected data");
        }else{ //update
            collectedData.setFormId(lastLoadedForm.getForm().getFormId());
            collectedData.setFormUri(contentUri.toString());
            collectedData.setFormXmlPath(xmlFile.toString());
            collectedData.setRecordId(member.getId());
            collectedData.setTableName(member.getTableName());

            db.update(CollectedData.class, collectedData.getContentValues(), whereClause, whereArgs);
            Log.d("fin-updating", "new collected data");

            //update name of member too
            if (member.getAge()==0){
                Member m = readMemberByOdk(xmlFile);
                member.setName(m.getName());

                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.Member.COLUMN_NAME, member.getName());

                db.update(Member.class, cv, DatabaseHelper.Member._ID+"=?", new String[]{ member.getId()+"" });
            }
        }

        db.close();


        if (isSupervisor(loggedUser)){
            buildMarkAsSupervisedDialog(collectedData);
        }

    }

    @Override
    public void onFormUnFinalized(Uri contentUri, File xmlFile) {
        Log.d("form unfinalized"," "+contentUri);

        Member member = null;

        if (selectedNewMember == null){ //new member

            member = readMemberByOdk(xmlFile);
            int next = nextNewMember();

            member.setPermId("XX-0000-000-"+ String.format("%02d", next));
            member.setNrPregnancies(next);

            Database db = new Database(this);
            db.open();
            db.insert(member); //35-0000-000-99
            db.close();

            member = getMemberByExtId(member.getExtId());
        }else {
            member = selectedNewMember;
        }



        //save Collected data
        Database db = new Database(this);
        db.open();

        //read xml file from odk

        //search existing record
        String whereClause = DatabaseHelper.CollectedData.COLUMN_RECORD_ID + "=? AND "+DatabaseHelper.CollectedData.COLUMN_FORM_URI + "=?";
        String[] whereArgs = new String[]{ ""+member.getId(), contentUri.toString() };

        CollectedData collectedData = Queries.getCollectedDataBy(db, whereClause, whereArgs);

        if (collectedData == null){ //insert
            collectedData = new CollectedData();
            collectedData.setFormId(lastLoadedForm.getForm().getFormId());
            collectedData.setFormUri(contentUri.toString());
            collectedData.setFormXmlPath("");
            collectedData.setRecordId(member.getId());
            collectedData.setTableName(member.getTableName());

            db.insert(collectedData);
            Log.d("inserting", "new collected data");
        }else{ //update
            collectedData.setFormId(lastLoadedForm.getForm().getFormId());
            collectedData.setFormUri(contentUri.toString());
            collectedData.setFormXmlPath("");
            collectedData.setRecordId(member.getId());
            collectedData.setTableName(member.getTableName());

            db.update(CollectedData.class, collectedData.getContentValues(), whereClause, whereArgs);
            Log.d("updating", "new collected data");

            //update name of member too
            if (member.getAge()==0){
                Member m = readMemberByOdk(xmlFile);
                member.setName(m.getName());

                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.Member.COLUMN_NAME, member.getName());

                db.update(Member.class, cv, DatabaseHelper.Member._ID+"=?", new String[]{ member.getId()+"" });
            }
        }

        db.close();
    }

    private void buildMarkAsSupervisedDialog(final CollectedData collectedData) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.facility_supervise_title_lbl));
        builder.setMessage(getString(R.string.facility_supervise_collected_form_lbl));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.bt_yes_lbl), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.CollectedData.COLUMN_SUPERVISED, 1);

                Database db = new Database(FacilityActivity.this);
                db.open();
                db.update(CollectedData.class, cv, DatabaseHelper.CollectedData._ID+"=?", new String[]{ collectedData.getId()+"" });
                db.close();

                Log.d("supervised", ""+collectedData.getFormId());
                showCollectedMember();
            }
        });
        builder.setNegativeButton(getString(R.string.bt_no_lbl), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
            }
        });


        builder.show();
    }

    private CollectedData getCollectedData(String formId, Member member){
        Database db = new Database(this);
        db.open();

        String whereClause = DatabaseHelper.CollectedData.COLUMN_FORM_ID + "=? AND " + DatabaseHelper.CollectedData.COLUMN_RECORD_ID + "=? AND "+DatabaseHelper.CollectedData.COLUMN_TABLE_NAME + "=?";
        String[] whereArgs = new String[]{ formId,  ""+member.getId(), member.getTableName() };

        CollectedData collectedData = Queries.getCollectedDataBy(db, whereClause, whereArgs);

        db.close();

        return collectedData;
    }

    private Member getMemberByExtId(String extId){
        Member member = null;

        Database db = new Database(this);
        db.open();

        Cursor cursor = db.query(Member.class, DatabaseHelper.Member.ALL_COLUMNS, DatabaseHelper.Member.COLUMN_EXT_ID+"=?", new String[]{extId}, null, null, null);
        if (cursor.moveToNext()){
            member = Converter.cursorToMember(cursor);
        }
        cursor.close();
        db.close();

        return member;
    }

    private Member readMemberByOdk(File xmlFile) {
        FormXmlReader reader = new FormXmlReader();

        Member member = null;

        try {
            member = reader.readMemberFromFacility(new FileInputStream(xmlFile), "Facility");
            if (member.getName()==null || member.getName().isEmpty()){
                member.setName(getString(R.string.member_details_name_unknown_lbl));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return member;
    }

    private int nextNewMember(){
        String[] extras = loggedUser.getExtras().split(",");
        String facNumber = extras[0];

        int next = 1;

        Database db = new Database(this);
        db.open();
        String[] columns = new String[]{DatabaseHelper.Member.COLUMN_NR_PREGNANCIES};
        Cursor cursor = db.query(Member.class, columns, DatabaseHelper.Member.COLUMN_AGE+"=0", null, null, null, DatabaseHelper.Member.COLUMN_NR_PREGNANCIES+" DESC");

        if (cursor.moveToNext()){
            next = cursor.getInt(0)+1;
        }

        cursor.close();
        db.close();

        return next;
    }

    @Override
    public void onDeleteForm(Uri contentUri) {
        //delete collected data by uri
        Database db = new Database(this);
        db.open();
        db.delete(CollectedData.class, DatabaseHelper.CollectedData.COLUMN_FORM_URI+"=?", new String[]{ contentUri.toString() } );
        db.close();
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
            return memberListFragment.loadMembersByFilters(null, name, permId, gender, null, isPregnant, hasPom, hasFacility);
        }

        @Override
        protected void onPostExecute(MemberArrayAdapter adapter) {
            memberListFragment.setMemberAdapter(adapter);
            memberListFragment.showProgress(false);
        }
    }

    class CollectedMemberSearchTask extends AsyncTask<Void, Void, MemberArrayAdapter> {
        private boolean withSupervision;

        public CollectedMemberSearchTask(){

        }

        public CollectedMemberSearchTask(boolean withSupervision){
            this.withSupervision = withSupervision;
        }

        @Override
        protected MemberArrayAdapter doInBackground(Void... params) {
            Database db = new Database(FacilityActivity.this);
            db.open();

            List<CollectedData> list = Queries.getAllCollectedDataBy(db, DatabaseHelper.CollectedData.COLUMN_FORM_ID+"=?", new String[]{"Facility"});
            List<Member> members = new ArrayList<>();
            List<Boolean> checks = new ArrayList<>();
            List<Boolean> supervList = new ArrayList<>();

            String ids = "";
            for (CollectedData cd : list){
                if (ids.length() > 0){
                    ids += ", "+cd.getRecordId();
                }else{
                    ids += ""+cd.getRecordId();
                }
            }

            Cursor cursor = db.query(Member.class, DatabaseHelper.Member.ALL_COLUMNS, DatabaseHelper.Member._ID + " IN ("+ids+")", null, null, null, DatabaseHelper.Member.COLUMN_PERM_ID);

            while (cursor.moveToNext()){
                Member member = Converter.cursorToMember(cursor);
                members.add(member);
                CollectedData cd = getCollectedData(list, member);
                checks.add(cd.getFormXmlPath()!=null && !cd.getFormXmlPath().isEmpty());
                supervList.add(cd.isSupervised());
            }

            cursor.close();

            db.close();

            MemberArrayAdapter adapter = null;

            if (withSupervision){
                adapter = new MemberArrayAdapter(FacilityActivity.this, members, checks, supervList);
            }else{
                adapter = new MemberArrayAdapter(FacilityActivity.this, members, checks);
            }

            return adapter;
        }

        public CollectedData getCollectedData(List<CollectedData> listCollecteds, Member member){
            for (CollectedData cd : listCollecteds){
                if (cd.getRecordId()==member.getId()) return cd;
            }

            return null;
        }

        @Override
        protected void onPostExecute(MemberArrayAdapter adapter) {
            memberListFragment.setMemberAdapter(adapter);
            memberListFragment.showProgress(false);

            if (withSupervision){
                btMarkAsSupervised.setEnabled(true);
            }else{
                if (btMarkAsSupervised != null){
                    btMarkAsSupervised.setEnabled(false);
                }
            }
        }
    }
}
