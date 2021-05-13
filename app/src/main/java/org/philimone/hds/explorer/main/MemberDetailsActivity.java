package org.philimone.hds.explorer.main;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.CollectedDataArrayAdapter;
import org.philimone.hds.explorer.adapter.model.CollectedDataItem;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.fragment.MemberFilterDialog;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.enums.Gender;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.FormSelectorDialog;
import org.philimone.hds.explorer.widget.member_details.MemberFormDialog;
import org.philimone.hds.explorer.widget.member_details.RelationshipTypeDialog;

import java.io.File;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.listener.OdkFormResultListener;
import mz.betainteractive.odk.model.FilledForm;

public class MemberDetailsActivity extends Activity implements OdkFormResultListener {

    private TextView mbDetailsName;
    private TextView mbDetailsCode;
    private TextView mbDetailsGender;
    private TextView mbDetailsAge;
    private TextView mbDetailsDob;
    private TextView mbDetailsHouseNo;
    private TextView mbDetailsEndType;
    private TextView mbDetailsEndDate;
    private TextView mbDetailsFather;
    private TextView mbDetailsMother;
    private TextView mbDetailsSpouse;
    private ListView lvCollectedForms;
    private Button btMemDetailsCollectData;
    private Button btMemDetailsBack;
    private ImageView iconView;

    private LinearLayout mbDetailsLayoutSc;
    private TextView mbDetailsStudyCodeLabel;
    private TextView mbDetailsStudyCodeValue;
    private String studyCodeValue;

    private Household household;
    private Member member;
    private boolean isNewTempMember;
    private List<FormDataLoader> formDataLoaders = new ArrayList<>();
    private FormDataLoader lastLoadedForm;

    private List<Member> allHouseholdMembers = new ArrayList<>();

    /* Add New Member Variables */
    private Member selectedFather = null;
    private Member selectedMother = null;
    private Member selectedSpouse = null;
    private boolean isHeadOfHousehold = false;
    private Integer maritalStatus = null;
    private boolean spouseChanged = false;
    /* Ends - Add New Member Variables */

    private User loggedUser;

    private FormUtilities formUtilities;

    private int requestCode;
    private boolean returnFromOdk;
    private boolean editingMember;

    private Box<CollectedData> boxCollectedData;
    private Box<Form> boxForms;
    private Box<Member> boxMembers;

    public static final int REQUEST_CODE_ADD_NEW_MEMBER = 10; /* Member Requests will be from 10 to 19 */
    public static final int REQUEST_CODE_EDIT_NEW_MEMBER = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_details);

        this.loggedUser = (User) getIntent().getExtras().get("user");
        this.household = (Household) getIntent().getExtras().get("household");
        this.member = (Member) getIntent().getExtras().get("member");
        this.studyCodeValue = getIntent().getExtras().getString("member_studycode");
        this.requestCode = getIntent().getExtras().getInt("request_code");

        if (requestCode != REQUEST_CODE_ADD_NEW_MEMBER && requestCode != REQUEST_CODE_EDIT_NEW_MEMBER){
            readFormDataLoader();
        }

        this.returnFromOdk = false;

        formUtilities = new FormUtilities(this);

        initBoxes();
        initialize();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (requestCode == REQUEST_CODE_ADD_NEW_MEMBER && returnFromOdk == false){

            if (member == null){
                //Show Create Household dialog - when this activity closes send the new created household back to parent activity
                startAddNewMember();
            } else {
                //Reopen Last Created Household
                this.editingMember = true;
                buildChangeMotherDialog();
            }
        }

        if (requestCode == REQUEST_CODE_EDIT_NEW_MEMBER && returnFromOdk == false){
            this.editingMember = true;
            buildChangeMotherDialog();
        }
    }

    private void readFormDataLoader(){

        Object[] objs = (Object[]) getIntent().getExtras().get("dataloaders");

        for (int i=0; i < objs.length; i++){
            FormDataLoader formDataLoader = (FormDataLoader) objs[i];
            Log.d("tag", ""+formDataLoader.getForm().getFormId());
            if (isMemberVisualizableForm(formDataLoader.getForm())){
                this.formDataLoaders.add(formDataLoader);
            }
        }
    }

    private boolean isMemberVisualizableForm(Form form) {
        if (requestCode != TrackingListDetailsActivity.RC_MEMBER_DETAILS_TRACKINGLIST){ //MemberDetails was not opened via Tracking/FollowUp lists
            if (form.isFollowUpOnly()){ //forms flagged with followUpOnly can only be opened using FollowUp Lists, to be able to open via normal surveys remove the flag on the server
                return false;
            }
        }

        if (form.isHouseholdForm()){ //Dont Show Household Form
            return false;
        }

        if (form.isHouseholdHeadForm() && (!member.isHouseholdHead() || !member.isSecHouseholdHead() )){ //Dont show HouseholdHead Form for non-head members
            return false;
        }

        return  (member.getAge() >= form.getMinAge() && member.getAge() <= form.getMaxAge()) && (member.getGender().equals(form.getGender()) || form.getGender().equals("ALL")) && (form.isMemberForm());
    }

    public void setMember(Member member){
        this.member = member;
    }

    private void initBoxes() {
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
    }

    private void initialize() {
        mbDetailsName = (TextView) findViewById(R.id.mbDetailsName);
        mbDetailsCode = (TextView) findViewById(R.id.mbDetailsCode);
        mbDetailsGender = (TextView) findViewById(R.id.mbDetailsGender);
        mbDetailsAge = (TextView) findViewById(R.id.mbDetailsAge);
        mbDetailsDob = (TextView) findViewById(R.id.mbDetailsDob);
        mbDetailsHouseNo = (TextView) findViewById(R.id.mbDetailsHouseName);
        mbDetailsEndType = (TextView) findViewById(R.id.mbDetailsEndType);
        mbDetailsEndDate = (TextView) findViewById(R.id.mbDetailsEndDate);
        mbDetailsFather = (TextView) findViewById(R.id.mbDetailsFather);
        mbDetailsMother = (TextView) findViewById(R.id.mbDetailsMother);
        mbDetailsSpouse = (TextView) findViewById(R.id.mbDetailsSpouse);
        lvCollectedForms = (ListView) findViewById(R.id.lvCollectedForms);
        btMemDetailsCollectData = (Button) findViewById(R.id.btMemDetailsCollectData);
        btMemDetailsBack = (Button) findViewById(R.id.btMemDetailsBack);
        iconView = (ImageView) findViewById(R.id.iconView);

        mbDetailsLayoutSc = (LinearLayout) findViewById(R.id.mbDetailsLayoutSc);
        mbDetailsStudyCodeLabel = (TextView) findViewById(R.id.mbDetailsStudyCodeLabel);
        mbDetailsStudyCodeValue = (TextView) findViewById(R.id.mbDetailsStudyCodeValue);


        btMemDetailsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemberDetailsActivity.this.onBackPressed();
            }
        });

        btMemDetailsCollectData.setOnClickListener(new View.OnClickListener() {
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

        isNewTempMember = member!=null && member.getId()==0;

        clearMemberData();

        if (requestCode != REQUEST_CODE_ADD_NEW_MEMBER){
            setMemberData();

            enableButtonsByFormLoaders();
            enableButtonsByIntentData();
        }

    }

    @Override
    public void onBackPressed() {
        onBackClicked();
        //super.onBackPressed();
    }

    private void onBackClicked() {
        Intent resultIntent = new Intent();

        resultIntent.putExtra("household_code", member.getHouseholdCode());
        resultIntent.putExtra("is_new_temp_member", isNewTempMember);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void enableButtonsByFormLoaders() {
        boolean hasForms = this.formDataLoaders.size()>0;
        this.btMemDetailsCollectData.setEnabled(hasForms);
    }

    private void enableButtonsByIntentData() {
        Object item = getIntent().getExtras().get("enable-collect-data");

        Boolean enaColData = (item==null) ? null : (boolean)item;

        if (enaColData != null){
            this.btMemDetailsCollectData.setEnabled(enaColData.booleanValue());
        }
    }

    private void clearMemberData(){
        mbDetailsName.setText("");
        mbDetailsCode.setText("");
        mbDetailsGender.setText("");
        mbDetailsAge.setText("");
        mbDetailsDob.setText("");
        mbDetailsHouseNo.setText("");
        mbDetailsEndType.setText("");
        mbDetailsEndDate.setText("");
        mbDetailsFather.setText("");
        mbDetailsMother.setText("");
        mbDetailsSpouse.setText("");

        if (studyCodeValue != null){
            mbDetailsLayoutSc.setVisibility(View.VISIBLE);

            String studyCodeLabel = getString(R.string.member_details_studycode_lbl); //.replace("#", loggedUser.getModules());
            mbDetailsStudyCodeLabel.setText(studyCodeLabel);
            mbDetailsStudyCodeValue.setText(studyCodeValue);
        }else{
            mbDetailsLayoutSc.setVisibility(View.GONE);
        }
    }

    private void setMemberData(){
        mbDetailsName.setText(member.getName());
        mbDetailsCode.setText(member.getCode());
        mbDetailsGender.setText(member.getGender().getId());
        mbDetailsAge.setText(member.getAge()+"");
        mbDetailsDob.setText(member.getDob());
        mbDetailsHouseNo.setText(member.getHouseholdName());
        mbDetailsEndType.setText(getEndTypeMsg(member));
        mbDetailsEndDate.setText(getEndDateMsg(member));
        mbDetailsFather.setText(getParentName(member.getFatherName()));
        mbDetailsMother.setText(getParentName(member.getMotherName()));
        mbDetailsSpouse.setText(getSpouseName(member.getSpouseName()));

        if (member.isHouseholdHead()){
            iconView.setImageResource(R.mipmap.nui_member_red_filled_icon);
        }

        if (member.isSecHouseholdHead()){
            iconView.setImageResource(R.mipmap.nui_member_red_filled_two_icon);
        }

        if (studyCodeValue != null){
            mbDetailsLayoutSc.setVisibility(View.VISIBLE);

            String studyCodeLabel = getString(R.string.member_details_studycode_lbl); //.replace("#", loggedUser.getModules());
            mbDetailsStudyCodeLabel.setText(studyCodeLabel);
            mbDetailsStudyCodeValue.setText(studyCodeValue);
        }else{
            mbDetailsLayoutSc.setVisibility(View.GONE);
        }

        showCollectedData();
        retrieveAllHouseholdMembers();

    }

    private void retrieveAllHouseholdMembers(){
        List<Member> members = this.boxMembers.query().equal(Member_.householdCode, this.member.getHouseholdCode()).build().find();

        this.allHouseholdMembers.clear();
        this.allHouseholdMembers.addAll(members);
    }

    private void onCollectedDataItemClicked(int position) {
        CollectedDataArrayAdapter adapter = (CollectedDataArrayAdapter) this.lvCollectedForms.getAdapter();
        CollectedDataItem dataItem = adapter.getItem(position);

        CollectedData collectedData = dataItem.getCollectedData();
        FormDataLoader formDataLoader = getFormDataLoader(collectedData);

        openOdkForm(formDataLoader, collectedData);
    }

    /*
     * Show the data collected for the selected individual - but only shows data that belongs to Forms that the user can view (FormDataLoader)
     * With this if we selected a follow_up list member we will view only the forms of that individual
     */
    private void showCollectedData() {
        //this.showProgress(true);

        List<CollectedData> list = this.boxCollectedData.query().equal(CollectedData_.recordId, member.getId()).and().equal(CollectedData_.tableName, member.getTableName()).build().find();
        List<Form> forms = this.boxForms.getAll();
        List<CollectedDataItem> cdl = new ArrayList<>();

        for (CollectedData cd : list){
            if (hasFormDataLoadersContains(cd.getFormId())){
                Form form = getFormById(forms, cd.getFormId());
                cdl.add(new CollectedDataItem(member, form, cd));
            }
        }

        CollectedDataArrayAdapter adapter = new CollectedDataArrayAdapter(this, cdl);
        this.lvCollectedForms.setAdapter(adapter);
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

    private String getEndTypeMsg(Member member){
        if (member.getEndType().equals("NA")) return getString(R.string.member_details_endtype_na_lbl);
        if (member.getEndType().equals("EXT")) return getString(R.string.member_details_endtype_ext_lbl);
        if (member.getEndType().equals("DTH")) return getString(R.string.member_details_endtype_dth_lbl);

        return member.getEndType();
    }

    private String getEndDateMsg(Member member){
        String date = member.getEndDate();
        if (member.getEndType().equals("NA")) return date = member.getStartDate();

        return member.getEndDate();
    }

    private String getParentName(String name){
        if (name.equals("Unknown") || name.equals("member.unknown.label")){
            return getString(R.string.member_details_unknown_lbl);
        }else {
            return name;
        }
    }

    private String getSpouseName(String name){
        if (name == null || name.isEmpty()){
            return "";
        }
        if (name.equals("Unknown") || name.equals("member.unknown.label")){
            return getString(R.string.member_details_unknown_lbl);
        }else {
            return name;
        }
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
                                                           .and().equal(CollectedData_.recordId, member.getId())
                                                           .and().equal(CollectedData_.tableName, member.getTableName()).build().findFirst();

        return collectedData;
    }

    private void openOdkForm(FormDataLoader formDataLoader) {

        CollectedData collectedData = getCollectedData(formDataLoader);

        this.lastLoadedForm = formDataLoader;

        Form form = formDataLoader.getForm();

        //reload timestamp constants
        formDataLoader.reloadTimestampConstants();

        FilledForm filledForm = new FilledForm(form.getFormId());
        filledForm.putAll(formDataLoader.getValues());
        filledForm.setHouseholdMembers(allHouseholdMembers);

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
        filledForm.setHouseholdMembers(allHouseholdMembers);

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
        //update or insert
        //Save Member and Update the object member
        if (member.getId()==0){
            long id = this.boxMembers.put(member);
            member.setId(id);
        }

        //search existing record
        CollectedData collectedData = this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString())
                .and().equal(CollectedData_.recordId, member.getId())
                .and().equal(CollectedData_.tableName, member.getTableName()).build().findFirst();


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

            collectedData.setRecordId(member.getId());
            collectedData.setTableName(member.getTableName());

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

            collectedData.setRecordId(member.getId());
            collectedData.setTableName(member.getTableName());

            this.boxCollectedData.put(collectedData);
            Log.d("updating", "new collected data");
        }

        if (requestCode == REQUEST_CODE_ADD_NEW_MEMBER || requestCode == REQUEST_CODE_EDIT_NEW_MEMBER){
            onFinishAddNewMember(this.member);
        } else {
            showCollectedData();
        }
    }

    @Override
    public void onFormUnFinalized(Uri contentUri, File xmlFile, String metaInstanceName, Date lastUpdatedDate) {
        Log.d("form unfinalized"," "+contentUri);

        //save Collected data
        //update or insert
        //Save Member and Update the object member
        if (member.getId()==0){
            long id = this.boxMembers.put(member);
            member.setId(id);
        }

        //search existing record
        CollectedData collectedData = this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString())
                                                           .and().equal(CollectedData_.recordId, member.getId())
                                                           .and().equal(CollectedData_.tableName, member.getTableName()).build().findFirst();

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

            collectedData.setRecordId(member.getId());
            collectedData.setTableName(member.getTableName());

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

            collectedData.setRecordId(member.getId());
            collectedData.setTableName(member.getTableName());

            this.boxCollectedData.put(collectedData);
            Log.d("updating", "new collected data");
        }

        if (requestCode == REQUEST_CODE_ADD_NEW_MEMBER || requestCode == REQUEST_CODE_EDIT_NEW_MEMBER){
            onFinishAddNewMember(this.member);
        } else {
            showCollectedData();
        }
    }

    @Override
    public void onDeleteForm(Uri contentUri) {

        this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString()).build().remove(); //delete where formUri=contentUri

        if (requestCode == REQUEST_CODE_ADD_NEW_MEMBER || requestCode == REQUEST_CODE_EDIT_NEW_MEMBER){
            onCancelAddNewMember();
        } else {
            showCollectedData();
        }
    }

    @Override
    public void onFormNotFound(final Uri contenUri) {
        buildDeleteSavedFormDialog(contenUri);
    }

    private void buildDeleteSavedFormDialog(final Uri contenUri){

        DialogFactory.createMessageYN(this, R.string.member_details_dialog_del_saved_form_title_lbl, R.string.member_details_dialog_del_saved_form_msg_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                onDeleteForm(contenUri);
            }

            @Override
            public void onNoClicked() {

            }
        }).show();

    }

    /* ADD NEW MEMBER METHODS */
    private void startAddNewMember(){
        //is census mode
        //get current household
        //check if is an empty household and prompt the head of household registration
        //select mother and father


        Log.d("head", ""+household.getHeadCode());

        if (household.getHeadCode() == null || household.getHeadCode().trim().isEmpty()){

            DialogFactory.createMessageInfo(this, getString(R.string.info_lbl), getString(R.string.new_member_dialog_household_head_warning_lbl), new DialogFactory.OnClickListener(){
                @Override
                public void onClicked(DialogFactory.Buttons clickedButton) {
                    buildNewMemberDialog();
                }
            }).show();

            isHeadOfHousehold = true;
        } else {
            buildNewMemberDialog();
        }


    }

    private void buildNewMemberDialog(){

        MemberFormDialog dialog = MemberFormDialog.createMemberDialog(getFragmentManager(), this.household, new MemberFormDialog.Listener() {
            @Override
            public void onNewMemberCreated(Member member) {
                afterNewMemberCreated(member);
            }

            @Override
            public void onCancelClicked() {
                onCancelAddNewMember();
            }
        });
        dialog.show();

    }

    private void onCancelAddNewMember(){

        if (requestCode == REQUEST_CODE_ADD_NEW_MEMBER) {

            Intent intent = new Intent();
            intent.putExtra("household", this.household);

            setResult(RESULT_CANCELED, intent); //CANCELED
            finish();
        }
    }

    private void onFinishAddNewMember(Member member){
        Intent data = new Intent();
        data.putExtra("household", this.household);
        data.putExtra("member", member);

        setResult(RESULT_OK, data);
        finish();
    }

    private void afterNewMemberCreated(Member member){
        this.member = member;

        buildSelectMotherDialog();
    }

    private void openOdkForNewMember(){
        //after filter mother, father and spouse

        boolean hasSpouse = selectedSpouse != null && (maritalStatus != null && maritalStatus>1 );
        boolean isHead = isHeadOfHousehold;

        this.member.setFatherCode(selectedFather.getCode());        
        this.member.setFatherName(selectedFather.getName());

        this.member.setMotherCode(selectedMother.getCode());        
        this.member.setMotherName(selectedMother.getName());


        if (hasSpouse){
            this.member.setSpouseCode(selectedSpouse.getCode());            
            this.member.setSpouseName(selectedSpouse.getName());
        } else {
            this.maritalStatus = 1;
        }


        Form form = new Form();
        form.setFormId("census_member");
        form.setFormName("Member Census Form");

        FormDataLoader loader = new FormDataLoader(form);
        loader.putData("field_worker_id", loggedUser.getUsername());
        loader.putData("household_id", member.getHouseholdCode());
        loader.putData("household_no", member.getHouseholdName());
        loader.putData("code", member.getCode());
        loader.putData("name", member.getName());
        loader.putData("gender", member.getGender().getId());
        loader.putData("dob", member.getDob());
        loader.putData("father_id", member.getFatherCode());
        loader.putData("father_name", member.getFatherName());
        loader.putData("mother_id", member.getMotherCode());
        loader.putData("mother_name", member.getMotherName());
        loader.putData("spouse_type", maritalStatus+"");
        loader.putData("spouse_id", member.getSpouseCode());
        loader.putData("spouse_name", member.getSpouseName());

        loader.putData("is_household_head", isHead ? "1" : "2");
        loader.putData("relationship_with_head", member.getCode());

        Log.d("editing-spouse", "selected-"+member.getSpouseCode()+", ms="+maritalStatus+", HEAD="+loader.getValues().get("is_household_head"));

        openOdkForm(loader);
    }

    private void openOdkForNewMember(Household household, Member member){

        this.household = household;
        this.member = member;

        Form form = new Form();
        form.setFormId("census_member");
        form.setFormName("Member Census Form");
        FormDataLoader loader = new FormDataLoader(form);

        if (editingMember){

            boolean fatherChanged = (selectedFather != null);
            boolean motherChanged = (selectedMother != null);

            //spouse changed when: maritalStatus not null = he

            if (fatherChanged) {
                this.member.setFatherCode(selectedFather.getCode());
                this.member.setFatherName(selectedFather.getName());
            }

            if (motherChanged) {
                this.member.setMotherCode(selectedMother.getCode());
                this.member.setMotherName(selectedMother.getName());
            }

            if (spouseChanged){
                this.maritalStatus = selectedSpouse==null || maritalStatus==null ? 1 : maritalStatus;
                this.member.setSpouseCode(selectedSpouse != null ? selectedSpouse.getCode() : "");
                this.member.setSpouseName(selectedSpouse != null ? selectedSpouse.getName() : "");
            }

            loader.putData("father_id", member.getFatherCode());
            loader.putData("father_name", member.getFatherName());
            loader.putData("mother_id", member.getMotherCode());
            loader.putData("mother_name", member.getMotherName());

            if (spouseChanged) {
                loader.putData("spouse_type", maritalStatus + "");
                loader.putData("spouse_id", member.getSpouseCode());
                loader.putData("spouse_name", member.getSpouseName());
                Log.d("editing-spouse", "selected-"+member.getSpouseCode()+", ms="+maritalStatus);
            }

            //save Member by updating changed fields
            this.boxMembers.put(this.member);

        }

        openOdkForm(loader);
    }

    private void filterMother(){
        FragmentManager fm = getFragmentManager();

        MemberFilterDialog.Listener listener = new MemberFilterDialog.Listener() {
            @Override
            public void onSelectedMember(Member member) {
                Log.d("selected-mother", ""+member.getCode());
                selectedMother = member;

                if (editingMember){
                    buildChangeFatherDialog();
                } else {
                    buildSelectFatherDialog();
                }
            }
        };

        MemberFilterDialog dialog = MemberFilterDialog.newInstance(listener, getString(R.string.new_member_dialog_mother_select_lbl), false);
        dialog.setGenderFemaleOnly();
        dialog.setFilterMinAge(12, true);
        dialog.setFilterHouseCode(household.getCode());

        dialog.show(fm, "fragment_edit_name");
    }

    private void filterFather(){
        FragmentManager fm = getFragmentManager();

        MemberFilterDialog.Listener listener = new MemberFilterDialog.Listener() {
            @Override
            public void onSelectedMember(Member member) {
                Log.d("selected-father", ""+member.getCode());
                selectedFather = member;

                if (editingMember) {
                    buildChangeMaritalStatusDialog();
                } else {
                    buildSelectMaritalStatusDialog();
                }
            }
        };

        MemberFilterDialog dialog = MemberFilterDialog.newInstance(listener, getString(R.string.new_member_dialog_father_select_lbl), false);
        dialog.setGenderMaleOnly();
        dialog.setFilterMinAge(12, true);
        dialog.setFilterHouseCode(household.getCode());

        dialog.show(fm, "fragment_edit_name");
    }

    private void filterSpouse(Member otherSpouse){
        FragmentManager fm = getFragmentManager();

        MemberFilterDialog.Listener listener = new MemberFilterDialog.Listener() {
            @Override
            public void onSelectedMember(Member filteredSpouse) {
                Log.d("selected-spouse", ""+filteredSpouse.getCode());
                selectedSpouse = filteredSpouse;

                if (editingMember) {
                    openOdkForNewMember(household, member);
                } else {
                    openOdkForNewMember();
                }
            }
        };

        MemberFilterDialog dialog = MemberFilterDialog.newInstance(listener, getString(R.string.new_member_dialog_spouse_select_lbl), false);

        if (otherSpouse.getGender() == Gender.MALE){
            dialog.setGenderFemaleOnly();
        }else {
            dialog.setGenderMaleOnly();
        }

        dialog.setFilterMinAge(12, true);
        dialog.setFilterHouseCode(household.getCode());

        dialog.show(fm, "fragment_edit_name");
    }

    private void buildSelectMotherDialog(){

        DialogFactory.createMessageYN(this, R.string.new_member_dialog_mother_select_lbl, R.string.new_member_dialog_mother_exists_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                //open mother filter dialog
                filterMother();
            }

            @Override
            public void onNoClicked() {
                //set unknown and jump to father dialog
                selectedMother = Member.getUnknownIndividual();

                if (editingMember){
                    buildChangeFatherDialog();
                } else {
                    buildSelectFatherDialog();
                }
            }
        }).show();

    }

    private void buildSelectFatherDialog(){

        DialogFactory.createMessageYN(this, R.string.new_member_dialog_father_select_lbl, R.string.new_member_dialog_father_exists_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                //open mother filter dialog
                filterFather();
            }

            @Override
            public void onNoClicked() {
                //set unknown and jump to father dialog
                selectedFather = Member.getUnknownIndividual();

                if (editingMember){
                    buildChangeMaritalStatusDialog();
                } else {
                    buildSelectMaritalStatusDialog();
                }
            }
        }).show();

    }

    private void buildSelectMaritalStatusDialog(){

        if (member.getAge()<15) { //MINIMAL AGE TO GET MARRIED, THIS SHOULD BE A VARIABLE
            selectedSpouse = null;
            spouseChanged = false;
            maritalStatus = 1;

            if (editingMember) {
                openOdkForNewMember(household, member);
            } else {
                openOdkForNewMember();
            }

            return; //jump if age lower than 15 - he cannot have a spouse
        }

        Log.d("Running", "nui spinner 2");
        RelationshipTypeDialog.createDialog(getFragmentManager(), new RelationshipTypeDialog.OnClickListener() {
            @Override
            public void onTypeSelected(int type) {
                onSelectedRelationshipType(type);
            }

            @Override
            public void onCancelClicked() {
                //THIS BUTTON IS DISABLED
            }
        }).show();
    }

    private void onSelectedRelationshipType(int selectedMaritalStatus){
        maritalStatus = selectedMaritalStatus+1;

        if (maritalStatus == 1) { //Solteiro
            selectedSpouse = null;
            spouseChanged = !member.getSpouseCode().isEmpty(); //if has previous spouse

            if (editingMember) {
                openOdkForNewMember(household, member);
            } else {
                openOdkForNewMember();
            }
        } else { //Selecionar conjugue
            buildSelectSpouseDialog();
        }
    }

    private void buildSelectSpouseDialog(){

        if (member.getAge()<15) return; //jump if age lower than 15 - he cannot have a spouse

        DialogFactory.createMessageYN(this, R.string.new_member_dialog_spouse_select_lbl, R.string.new_member_dialog_spouse_exists_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                //open spouse filter dialog
                spouseChanged = true;
                filterSpouse(member);
            }

            @Override
            public void onNoClicked() {
                //set null - dont have a spouse
                selectedSpouse = null;
                spouseChanged = !member.getSpouseCode().isEmpty(); //If he/she had a spouse before now they havent

                if (editingMember) {
                    openOdkForNewMember(household, member);
                } else {
                    openOdkForNewMember();
                }
            }
        }).show();

    }

    private void buildChangeMotherDialog(){

        DialogFactory.createMessageYN(this, R.string.new_member_dialog_change_title_lbl, R.string.new_member_dialog_mother_change_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                buildSelectMotherDialog();
            }

            @Override
            public void onNoClicked() {
                buildChangeFatherDialog();
            }
        }).show();

    }

    private void buildChangeFatherDialog(){

        DialogFactory.createMessageYN(this, R.string.new_member_dialog_change_title_lbl, R.string.new_member_dialog_father_change_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                buildSelectFatherDialog();
            }

            @Override
            public void onNoClicked() {
                buildChangeMaritalStatusDialog();
            }
        }).show();

    }

    private void buildChangeSpouseDialog(){

        if (member.getAge()<15) return; //jump if age lower than 15 - he cannot have a spouse

        DialogFactory.createMessageYN(this, R.string.new_member_dialog_change_title_lbl, R.string.new_member_dialog_spouse_change_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                buildSelectSpouseDialog();
            }

            @Override
            public void onNoClicked() {
                openOdkForNewMember(household, member);
            }
        }).show();

    }

    private void buildChangeMaritalStatusDialog(){

        if (member.getAge()<15) {
            spouseChanged = false;

            openOdkForNewMember(household, member);

            return; //jump if age lower than 15 - he cannot have a spouse
        }

        DialogFactory.createMessageYN(this, R.string.new_member_dialog_change_title_lbl, R.string.new_member_dialog_spouse_type_change_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                buildSelectMaritalStatusDialog();
            }

            @Override
            public void onNoClicked() {
                spouseChanged = false;

                openOdkForNewMember(household, member);
            }
        }).show();

    }

}
