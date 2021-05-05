package org.philimone.hds.explorer.main;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.CollectedDataArrayAdapter;
import org.philimone.hds.explorer.adapter.MemberArrayAdapter;
import org.philimone.hds.explorer.adapter.model.CollectedDataItem;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.Database;
import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.DataSet;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.FormSelectorDialog;
import org.philimone.hds.explorer.widget.LoadingDialog;
import org.philimone.hds.explorer.widget.household_details.HouseholdFormDialog;

import java.io.File;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.objectbox.Box;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.listener.OdkFormResultListener;
import mz.betainteractive.odk.model.FilledForm;
import mz.betainteractive.utilities.StringUtil;

public class HouseholdDetailsActivity extends Activity implements OdkFormResultListener {

    private TextView hhDetailsName;
    private TextView hhDetailsCode;
    private TextView hhDetailsHeadName;
    private TextView hhDetailsHeadCode;
    private TextView hhDetailsRegionLabel;
    private TextView hhDetailsRegionValue;
    private ListView lvHouseholdMembers;
    private ListView lvCollectedForms;
    private Button btHouseDetailsCollectData;
    private Button btHouseDetailsBack;
    private ImageView iconView;  
            
    private Household household;
    private Region region;
    private List<FormDataLoader> formDataLoaders = new ArrayList<>();
    private FormDataLoader lastLoadedForm;

    private User loggedUser;

    private LoadingDialog loadingDialog;

    private FormUtilities formUtilities;
    private Database database;
    private Box<ApplicationParam> boxAppParams;
    private Box<CollectedData> boxCollectedData;
    private Box<Form> boxForms;
    private Box<Region> boxRegions;

    private int requestCode;
    private boolean returnFromOdk = false;

    public static final int REQUEST_CODE_NEW_HOUSEHOLD = 1; /* Household Requests will be from 1 to 9 */
    public static final int REQUEST_CODE_EDIT_HOUSEHOLD = 2;

    public enum FormFilter {
        REGION, HOUSEHOLD, HOUSEHOLD_HEAD, MEMBER, FOLLOW_UP
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.household_details);

        this.loggedUser = (User) getIntent().getExtras().get("user");
        this.household = (Household) getIntent().getExtras().get("household");
        this.region = (Region) getIntent().getExtras().get("region");
        this.requestCode = getIntent().getExtras().getInt("request_code");

        initBoxes();

        readFormDataLoader();

        formUtilities = new FormUtilities(this);

        initialize();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (requestCode == REQUEST_CODE_NEW_HOUSEHOLD && returnFromOdk == false){
            if (household == null){
                //Show Create Household dialog - when this activity closes send the new created household back to parent activity
                addNewHousehold(region);
            } else {
                //Reopen Last Created Household
                openAddNewHouseholdForm(household);
            }
        }

        if (requestCode == REQUEST_CODE_EDIT_HOUSEHOLD && returnFromOdk == false){
            openAddNewHouseholdForm(household);
        }
    }

    private boolean isVisibleForm(Form form){
        if (requestCode != TrackingListDetailsActivity.RC_HOUSEHOLD_DETAILS_TRACKINGLIST){ //HouseholdDetails was not opened via Tracking/FollowUp lists
            if (form.isFollowUpOnly()){ //forms flagged with followUpOnly can only be opened using FollowUp Lists, to be able to open via normal surveys remove the flag on the server
                return false;
            }
        }

        return true;
    }

    private void readFormDataLoader(){

        Object[] objs = (Object[]) getIntent().getExtras().get("dataloaders");

        for (int i=0; i < objs.length; i++){
            FormDataLoader formDataLoader = (FormDataLoader) objs[i];
            //Log.d("tag", ""+formDataLoader.getForm().getFormId());
            if (formDataLoader.getForm().isHouseholdForm() && isVisibleForm(formDataLoader.getForm())){
                this.formDataLoaders.add(formDataLoader);
            }
        }
    }

    public void setHousehold(Household household){
        this.household = household;
    }

    private void initialize() {
        hhDetailsName = (TextView) findViewById(R.id.hhDetailsName);
        hhDetailsCode = (TextView) findViewById(R.id.hhDetailsCode);
        hhDetailsHeadName = (TextView) findViewById(R.id.hhDetailsHeadName);
        hhDetailsHeadCode = (TextView) findViewById(R.id.hhDetailsHeadCode);
        hhDetailsRegionLabel = (TextView) findViewById(R.id.hhDetailsRegionLabel);
        hhDetailsRegionValue = (TextView) findViewById(R.id.hhDetailsRegionValue);
        lvHouseholdMembers = (ListView) findViewById(R.id.lvHouseholdMembers);
        lvCollectedForms = (ListView) findViewById(R.id.lvCollectedForms);
        btHouseDetailsCollectData = (Button) findViewById(R.id.btHouseDetailsCollectData);
        btHouseDetailsBack = (Button) findViewById(R.id.btHouseDetailsBack);
        iconView = (ImageView) findViewById(R.id.iconView);

        btHouseDetailsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HouseholdDetailsActivity.this.onBackPressed();
            }
        });

        btHouseDetailsCollectData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCollectDataClicked();
            }
        });

        lvCollectedForms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onCollectedDataItemClicked(position);
            }
        });

        lvHouseholdMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onMemberClicked(position);
            }
        });

        this.loadingDialog = new LoadingDialog(this);

        setHouseholdData();

        enableButtonsByFormLoaders();
        enableButtonsByIntentData();
    }

    private void initBoxes() {
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
    }

    private void enableButtonsByFormLoaders() {
        boolean hasForms = this.formDataLoaders.size()>0;
        this.btHouseDetailsCollectData.setEnabled(hasForms);
    }

    private void enableButtonsByIntentData() {
        Object item = getIntent().getExtras().get("enable-collect-data");

        Boolean enaColData = (item==null) ? null : (boolean)item;

        if (enaColData != null){
            this.btHouseDetailsCollectData.setEnabled(enaColData.booleanValue());
        }
    }

    private void setHouseholdData(){

        if (household == null) return;

        Region region = getRegion(household.getRegion());
        String hierarchyName = getHierarchyName(region);

        hhDetailsName.setText(household.getName());
        hhDetailsCode.setText(household.getCode());
        hhDetailsHeadName.setText(household.getHeadName());
        hhDetailsHeadCode.setText(household.getHeadCode());
        hhDetailsRegionLabel.setText(hierarchyName+":");
        hhDetailsRegionValue.setText(region==null ? "" : region.getName());

        showHouseholdMembers();
        showCollectedData();
    }

    /*Household Census*/

    private void addNewHousehold(Region region){

        new HouseholdFormDialog().newInstance(getFragmentManager(), this.region, this.loggedUser, new HouseholdFormDialog.Listener() {
            @Override
            public void onNewHouseholdCreated(Household household) {
                afterNewHouseholdCreated(household);
            }

            @Override
            public void onCancelClicked() {
                onCancelAddNewHousehold();
            }
        }).show();

    }

    private void onCancelAddNewHousehold(){

        if (requestCode == REQUEST_CODE_NEW_HOUSEHOLD) {

            //Intent intent = new Intent(this, HouseholdDetailsActivity.class);
            //intent.putExtra("user", loggedUser);

            setResult(RESULT_CANCELED); //CANCELED
            finish();
        }
    }

    private void onFinishAddNewHousehold(Household household){
        Intent data = new Intent();
        data.putExtra("household", household);

        setResult(RESULT_OK, data);
        finish();
    }

    private void afterNewHouseholdCreated(Household household) {
        //open ODK Form
        openAddNewHouseholdForm(household, region);
    }

    private void openAddNewHouseholdForm(Household household, Region region){

        this.household = household;

        Form form = new Form();
        form.setFormId("census_household");
        form.setFormName("Household Census Form");
        FormDataLoader loader = new FormDataLoader(form);
        loader.putData("field_worker_id", loggedUser.getUsername());
        loader.putData("region_id", region.getCode());
        loader.putData("region_name", region.getName());
        loader.putData("household_id", household.getCode());
        loader.putData("household_name", household.getName());

        openOdkForm(loader);
    }

    private void openAddNewHouseholdForm(Household household){

        this.household = household;

        Form form = new Form();
        form.setFormId("census_household");
        form.setFormName("Household Census Form");
        FormDataLoader loader = new FormDataLoader(form);

        if (household.getHeadCode()!=null && !household.getHeadCode().isEmpty()){
            Member member = getMemberBy(household.getHeadCode());

            Log.d("member head", member.getCode());
            /*
            loader.putData("head_id", member.getExtId());
            loader.putData("head_perm_id", member.getPermId());
            loader.putData("head_name", member.getName());
            */
        }

        openOdkForm(loader);
    }

    private Member getMemberBy(String code){
        Database database = new Database(this);
        database.open();
        Member member = Queries.getMemberBy(database, DatabaseHelper.Member.COLUMN_CODE+"=?", new String[] { code });
        database.close();

        return member;
    }

    private String generateHouseholdCode(Region region, User fieldWorker){
        Database database = new Database(this);
        database.open();
        String baseId = region.getCode() + fieldWorker.getCode();
        String[] columns = new String[] {DatabaseHelper.Household.COLUMN_CODE};
        String where = DatabaseHelper.Household.COLUMN_CODE + " LIKE ?";
        String[] whereArgs = new String[] { baseId + "%" };
        String orderBy = DatabaseHelper.Household.COLUMN_CODE + " DESC";
        String generatedId = null;

        Cursor cursor = database.query(Household.class, columns, where, whereArgs, null, null, orderBy);

        if (cursor.moveToFirst()) {
            String lastGeneratedId = cursor.getString(0);

            try {
                int increment = Integer.parseInt(lastGeneratedId.substring(6, 9));
                generatedId = baseId + String.format("%03d", increment+1);
            } catch (NumberFormatException e) {
                return baseId + "ERROR_01";
            }

        } else { //no extId based on "baseId"
            generatedId = baseId + "001"; //set the first id of individual household
        }

        cursor.close();
        database.close();

        return generatedId;
    }

    private boolean checkIfHouseCodeExists(String houseCode){
        Database database = new Database(this);
        database.open();
        Household household = Queries.getHouseholdBy(database, DatabaseHelper.Household.COLUMN_CODE+"=?", new String[] { houseCode });
        database.close();

        return household != null;
    }

    /*Ends*/

    private void onCollectedDataItemClicked(int position) {
        CollectedDataArrayAdapter adapter = (CollectedDataArrayAdapter) this.lvCollectedForms.getAdapter();
        CollectedDataItem dataItem = adapter.getItem(position);

        CollectedData collectedData = dataItem.getCollectedData();
        FormDataLoader formDataLoader = getFormDataLoader(collectedData);

        openOdkForm(formDataLoader, collectedData);
    }

    /*
     * Show the data collected for the selected individual - but only shows data that belongs to Forms that the user can view (FormDataLoader)
     * With this if we selected a follow_up list household we will view only the forms of that individual
     */
    private void showCollectedData() {
        //this.showProgress(true);

        List<CollectedData> list = this.boxCollectedData.query().equal(CollectedData_.recordId, household.getId()).and().equal(CollectedData_.tableName, household.getTableName()).build().find();
        List<Form> forms = this.boxForms.getAll();
        List<CollectedDataItem> cdl = new ArrayList<>();

        for (CollectedData cd : list){
            if (hasFormDataLoadersContains(cd.getFormId())){
                Form form = getFormById(forms, cd.getFormId());
                cdl.add(new CollectedDataItem(household, form, cd));
            }
        }

        CollectedDataArrayAdapter adapter = new CollectedDataArrayAdapter(this, cdl);
        this.lvCollectedForms.setAdapter(adapter);
    }

    private void showHouseholdMembers(){
        Database db = new Database(this);
        db.open();

        List<Member> members = Queries.getAllMemberBy(db, DatabaseHelper.Member.COLUMN_HOUSEHOLD_CODE +"=?", new String[]{ household.getCode() } );

        db.close();

        MemberArrayAdapter adapter = new MemberArrayAdapter(this, members);
        this.lvHouseholdMembers.setAdapter(adapter);
    }

    private Household getHousehold(Member member){
        if (member == null || member.getHouseholdCode()==null) return null;

        Database database = new Database(this);
        database.open();
        Household household = Queries.getHouseholdBy(database, DatabaseHelper.Household.COLUMN_CODE +"=?", new String[]{ member.getHouseholdCode() });
        database.close();

        return household;
    }

    private Region getRegion(Household household){
        return getRegion(household.getRegion());
    }

    private Region getRegion(String code){

        Region region = this.boxRegions.query().equal(Region_.code, code).build().findFirst();

        return region;
    }

    private String getHierarchyName(Region region){
        if (region == null) return "";

        ApplicationParam param = Queries.getApplicationParamBy(boxAppParams, region.getLevel());

        if (param != null){
            return param.getValue();
        }

        return "";
    }

    private Form getFormById(List<Form> forms, String formId){
        for (Form f : forms){
            if (f.getFormId().equals(formId)) return f;
        }

        return null;
    }

    private boolean hasFormDataLoadersContains(String formId){
        for (FormDataLoader fdl : formDataLoaders){
            if (fdl.getForm().getFormId().equals(formId)){
                return true;
            }
        }
        return false;
    }

    private void onCollectDataClicked(){

        if (formDataLoaders != null && formDataLoaders.size() > 0){

            if (formDataLoaders.size()==1){
                //open directly the form
                openOdkForm(formDataLoaders.get(0));
            }else {
                //load list dialog and choice the form
                buildFormSelectorDialog(formDataLoaders);
            }
        }
    }

    private FormDataLoader getFormDataLoader(CollectedData collectedData){

        for (FormDataLoader dl : this.formDataLoaders){
            if (dl.getForm().getFormId().equals(collectedData.getFormId())){
                return dl;
            }
        }

        return null;
    }

    private CollectedData getCollectedData(FormDataLoader formDataLoader){

        CollectedData collectedData = this.boxCollectedData.query().equal(CollectedData_.formId, formDataLoader.getForm().getFormId())
                .and().equal(CollectedData_.recordId, household.getId())
                .and().equal(CollectedData_.tableName, household.getTableName()).build().findFirst();

        return collectedData;
    }

    private List<Member> getMemberOnListAdapter(){
        MemberArrayAdapter adapter = (MemberArrayAdapter) this.lvHouseholdMembers.getAdapter();

        if (adapter == null) return new ArrayList<Member>();

        return adapter.getMembers();
    }

    private void openOdkForm(FormDataLoader formDataLoader) {

        CollectedData collectedData = getCollectedData(formDataLoader);

        this.lastLoadedForm = formDataLoader;

        Form form = formDataLoader.getForm();

        //reload timestamp constants
        formDataLoader.reloadTimestampConstants();

        FilledForm filledForm = new FilledForm(form.getFormId());
        filledForm.putAll(formDataLoader.getValues());
        filledForm.setHouseholdMembers(getMemberOnListAdapter());

        if (collectedData == null || form.isMultiCollPerSession()){
            formUtilities.loadForm(filledForm);
        }else{
            formUtilities.loadForm(filledForm, collectedData.getFormUri(), this);
        }

    }

    private void openOdkForm(FormDataLoader formDataLoader, CollectedData collectedData) {

        this.lastLoadedForm = formDataLoader;

        Form form = formDataLoader.getForm();

        //reload timestamp constants
        formDataLoader.reloadTimestampConstants();

        FilledForm filledForm = new FilledForm(form.getFormId());
        filledForm.putAll(formDataLoader.getValues());

        if (collectedData == null){
            formUtilities.loadForm(filledForm);
        }else{
            formUtilities.loadForm(filledForm, collectedData.getFormUri(), this);
        }

    }

    private void buildFormSelectorDialog(List<FormDataLoader> loaders) {

        FormSelectorDialog.createDialog(getFragmentManager(), loaders, new FormSelectorDialog.OnFormSelectedListener() {
            @Override
            public void onFormSelected(FormDataLoader formDataLoader) {
                openOdkForm(formDataLoader);
            }

            @Override
            public void onCancelClicked() {

            }
        }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.returnFromOdk = (requestCode==FormUtilities.SELECTED_ODK_FORM || requestCode == FormUtilities.SELECTED_ODK_REOPEN);
        formUtilities.onActivityResult(requestCode, resultCode, data, this);
    }

    @Override
    public void onFormFinalized(Uri contentUri, File xmlFile, String metaInstanceName, Date lastUpdatedDate) {
        Log.d("form finalized"," "+contentUri+", "+xmlFile);

        //save Collected data
        Database db = new Database(this);
        db.open();
        //update or insert

        //Save household and Update the object household
        if (household.getId()==0){
            int id = (int) db.insert(household);
            household.setId(id);
        }


        //search existing record
        CollectedData collectedData = this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString())
                                                                   .and().equal(CollectedData_.recordId, household.getId())
                                                                   .and().equal(CollectedData_.tableName, household.getTableName()).build().findFirst();

        if (collectedData == null){ //insert
            collectedData = new CollectedData();
            collectedData.setFormId(lastLoadedForm.getForm().getFormId());
            collectedData.setFormUri(contentUri.toString());
            collectedData.setFormXmlPath(xmlFile.toString());
            collectedData.setFormInstanceName(metaInstanceName);
            collectedData.setFormLastUpdatedDate(lastUpdatedDate);

            collectedData.setFormModule(lastLoadedForm.getForm().getModules());
            collectedData.setCollectedBy(loggedUser.getUsername());
            collectedData.setUpdatedBy("");
            collectedData.setSupervisedBy("");

            collectedData.setRecordId(household.getId());
            collectedData.setTableName(household.getTableName());

            this.boxCollectedData.put(collectedData);
            Log.d("inserting", "new collected data");
        }else{ //update
            collectedData.setFormId(lastLoadedForm.getForm().getFormId());
            collectedData.setFormUri(contentUri.toString());
            collectedData.setFormXmlPath(xmlFile.toString());
            collectedData.setFormInstanceName(metaInstanceName);
            collectedData.setFormLastUpdatedDate(lastUpdatedDate);

            //collectedData.setFormModule(lastLoadedForm.getForm().getModules());
            //collectedData.setCollectedBy(loggedUser.getUsername());
            collectedData.setUpdatedBy(loggedUser.getUsername());
            //collectedData.setSupervisedBy("");

            collectedData.setRecordId(household.getId());
            collectedData.setTableName(household.getTableName());

            this.boxCollectedData.put(collectedData);
            Log.d("updating", "new collected data");
        }

        db.close();

        if (requestCode == REQUEST_CODE_NEW_HOUSEHOLD || requestCode == REQUEST_CODE_EDIT_HOUSEHOLD){
            onFinishAddNewHousehold(household);
        } else {
            showCollectedData();
        }
    }

    @Override
    public void onFormUnFinalized(Uri contentUri, File xmlFile, String metaInstanceName, Date lastUpdatedDate) {
        Log.d("form unfinalized"," "+contentUri);

        //save Collected data
        Database db = new Database(this);
        db.open();
        //update or insert

        //Save household and Update the object household
        if (household.getId()==0){
            int id = (int) db.insert(household);
            household.setId(id);
        }

        //search existing record
        CollectedData collectedData = this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString())
                                                           .and().equal(CollectedData_.recordId, household.getId())
                                                           .and().equal(CollectedData_.tableName, household.getTableName()).build().findFirst();

        if (collectedData == null){ //insert
            collectedData = new CollectedData();
            collectedData.setFormId(lastLoadedForm.getForm().getFormId());
            collectedData.setFormUri(contentUri.toString());
            collectedData.setFormXmlPath("");
            collectedData.setFormInstanceName(metaInstanceName);
            collectedData.setFormLastUpdatedDate(lastUpdatedDate);

            collectedData.setFormModule(lastLoadedForm.getForm().getModules());
            collectedData.setCollectedBy(loggedUser.getUsername());
            collectedData.setUpdatedBy("");
            collectedData.setSupervisedBy("");

            collectedData.setRecordId(household.getId());
            collectedData.setTableName(household.getTableName());

            this.boxCollectedData.put(collectedData);
            Log.d("inserting", "new collected data");
        }else{ //update
            collectedData.setFormId(lastLoadedForm.getForm().getFormId());
            collectedData.setFormUri(contentUri.toString());
            collectedData.setFormXmlPath("");
            collectedData.setFormInstanceName(metaInstanceName);
            collectedData.setFormLastUpdatedDate(lastUpdatedDate);

            //collectedData.setFormModule(lastLoadedForm.getForm().getModules());
            //collectedData.setCollectedBy(loggedUser.getUsername());
            collectedData.setUpdatedBy(loggedUser.getUsername());
            //collectedData.setSupervisedBy("");

            collectedData.setRecordId(household.getId());
            collectedData.setTableName(household.getTableName());

            this.boxCollectedData.put(collectedData);
            Log.d("updating", "new collected data");
        }

        db.close();

        if (requestCode == REQUEST_CODE_NEW_HOUSEHOLD || requestCode == REQUEST_CODE_EDIT_HOUSEHOLD){
            onFinishAddNewHousehold(household);
        } else {
            showCollectedData();
        }
    }

    @Override
    public void onDeleteForm(Uri contentUri) {
        Database db = new Database(this);
        db.open();

        this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString()).build().remove(); //delete where formUri=contentUri

        if (requestCode == REQUEST_CODE_NEW_HOUSEHOLD || requestCode == REQUEST_CODE_EDIT_HOUSEHOLD){
            if (household != null && household.getId()>0){
                //delete household
                db.delete(Household.class, DatabaseHelper.Household._ID+"=?", new String[] { household.getId()+"" });
            }

            db.close();

            onCancelAddNewHousehold();

        } else {

            db.close();
            showCollectedData();

        }
    }

    @Override
    public void onFormNotFound(final Uri contenUri) {
        buildDeleteSavedFormDialog(contenUri);
    }

    private void buildDeleteSavedFormDialog(final Uri contenUri){

        DialogFactory.createMessageYN(this, R.string.household_details_dialog_del_saved_form_title_lbl, R.string.household_details_dialog_del_saved_form_msg_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                onDeleteForm(contenUri);

                if (requestCode == REQUEST_CODE_NEW_HOUSEHOLD || requestCode == REQUEST_CODE_EDIT_HOUSEHOLD){
                    onCancelAddNewHousehold();
                }
            }

            @Override
            public void onNoClicked() {

            }
        }).show();
    }

    /* LOAD FORM VALUES */

    private void onMemberClicked(int position) {
        MemberArrayAdapter adapter = (MemberArrayAdapter) this.lvHouseholdMembers.getAdapter();
        Member member = adapter.getItem(position);

        MemberSelectedTask task = new MemberSelectedTask(member);
        task.execute();

        showLoadingDialog(getString(R.string.loading_dialog_member_details_lbl), true);
    }

    public FormDataLoader[] getFormLoaders(FormFilter... filters){

        List<FormFilter> listFilters = Arrays.asList(filters);

        String[] userModules = loggedUser.getModules().split(",");

        List<Form> forms = this.boxForms.getAll(); //get all forms
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
            Log.d("has-mapped-datasets", dataSet.getName()+", "+loader.hasMappedDatasetVariable(dataSet));
            if (loader.hasMappedDatasetVariable(dataSet)){
                Log.d("hasMappedVariables", ""+dataSet.getName());
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

    private void showLoadingDialog(String msg, boolean show){
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.hide();
        }
    }

    class MemberSelectedTask  extends AsyncTask<Void, Void, Void> {
        private Household household;
        private Member member;
        private Region region;
        private FormDataLoader[] dataLoaders;

        public MemberSelectedTask(Member member) {
            //this.household = household;
            this.member = member;
            //this.region = region;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            this.household = getHousehold(member);
            this.region = getRegion(household);

            dataLoaders = getFormLoaders(FormFilter.HOUSEHOLD_HEAD, FormFilter.MEMBER);
            loadFormValues(dataLoaders, household, member, region);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = new Intent(HouseholdDetailsActivity.this, MemberDetailsActivity.class);
            intent.putExtra("user", loggedUser);
            intent.putExtra("member", this.member);
            intent.putExtra("dataloaders", dataLoaders);

            showLoadingDialog(null, false);

            startActivity(intent);
        }
    }
}
