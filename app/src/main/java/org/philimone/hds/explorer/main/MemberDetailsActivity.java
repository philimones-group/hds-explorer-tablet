package org.philimone.hds.explorer.main;

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
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.settings.RequestCodes;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.FormSelectorDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.listener.OdkFormResultListener;
import mz.betainteractive.odk.model.FilledForm;
import mz.betainteractive.utilities.StringUtil;

public class MemberDetailsActivity extends AppCompatActivity implements OdkFormResultListener {

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

    private User loggedUser;

    private FormUtilities formUtilities;

    private int requestCode;

    private Box<CollectedData> boxCollectedData;
    private Box<Form> boxForms;
    private Box<Member> boxMembers;

    //public static final int REQUEST_CODE_ADD_NEW_MEMBER = 10; /* Member Requests will be from 10 to 19 */
    //public static final int REQUEST_CODE_EDIT_NEW_MEMBER = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_details);

        this.loggedUser = Bootstrap.getCurrentUser();
        this.household = (Household) getIntent().getExtras().get("household");
        this.member = (Member) getIntent().getExtras().get("member");
        this.studyCodeValue = getIntent().getExtras().getString("member_studycode");
        this.requestCode = getIntent().getExtras().getInt("request_code");

        readFormDataLoader();

        formUtilities = new FormUtilities(this, this);

        initBoxes();
        initialize();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
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
        if (requestCode != RequestCodes.MEMBER_DETAILS_FROM_TRACKING_LIST_DETAILS){ //MemberDetails was not opened via Tracking/FollowUp lists
            if (form.isFollowUpForm()){ //forms flagged with followUpOnly can only be opened using FollowUp Lists, to be able to open via normal surveys remove the flag on the server
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
        setMemberData();

        enableButtonsByFormLoaders();
        enableButtonsByIntentData();

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
        setResult(AppCompatActivity.RESULT_OK, resultIntent);
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
        mbDetailsDob.setText(StringUtil.formatYMD(member.dob));
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
        List<Member> members = this.boxMembers.query().equal(Member_.householdCode, this.member.getHouseholdCode(), QueryBuilder.StringOrder.CASE_SENSITIVE).build().find();

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

        List<CollectedData> list = this.boxCollectedData.query().equal(CollectedData_.recordId, member.getId()).and().equal(CollectedData_.recordEntity, member.getTableName().code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().find();
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
        if (member.getEndType() == ResidencyEndType.NOT_APPLICABLE) return getString(R.string.member_details_endtype_na_lbl);
        if (member.getEndType() == ResidencyEndType.EXTERNAL_OUTMIGRATION) return getString(R.string.member_details_endtype_ext_lbl);
        if (member.getEndType() == ResidencyEndType.DEATH) return getString(R.string.member_details_endtype_dth_lbl);

        return member.getEndType().getId();
    }

    private String getEndDateMsg(Member member){
        Date date = member.getEndDate();
        if (member.getEndType() == ResidencyEndType.NOT_APPLICABLE) {
            date = member.getStartDate();
        }

        return StringUtil.formatYMD(date);
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

        CollectedData collectedData = this.boxCollectedData.query().equal(CollectedData_.formId, formDataLoader.getForm().getFormId(), QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                           .and().equal(CollectedData_.recordId, member.getId())
                                                           .and().equal(CollectedData_.recordEntity, member.getTableName().code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

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

        FormSelectorDialog.createDialog(getSupportFragmentManager(), loaders, new FormSelectorDialog.OnFormSelectedListener() {
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
        CollectedData collectedData = this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString(), QueryBuilder.StringOrder.CASE_SENSITIVE)
                .and().equal(CollectedData_.recordId, member.getId())
                .and().equal(CollectedData_.recordEntity, member.getTableName().code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();


        if (collectedData == null){ //insert
            collectedData = new CollectedData();
            collectedData.setFormId(lastLoadedForm.getForm().getFormId());
            collectedData.setFormUri(contentUri.toString());
            collectedData.setFormXmlPath(xmlFile.toString());
            collectedData.setFormInstanceName(metaInstanceName);
            collectedData.setFormLastUpdatedDate(lastUpdatedDate);

            collectedData.setFormModules(lastLoadedForm.getForm().getModules());
            collectedData.setCollectedBy(loggedUser.getUsername());
            collectedData.setUpdatedBy("");
            collectedData.setSupervisedBy("");

            collectedData.setRecordId(member.getId());
            collectedData.setRecordEntity(member.getTableName());

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
            collectedData.setRecordEntity(member.getTableName());

            this.boxCollectedData.put(collectedData);
            Log.d("updating", "new collected data");
        }

        showCollectedData();
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
        CollectedData collectedData = this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString(), QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                           .and().equal(CollectedData_.recordId, member.getId())
                                                           .and().equal(CollectedData_.recordEntity, member.getTableName().code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (collectedData == null){ //insert
            collectedData = new CollectedData();
            collectedData.setFormId(lastLoadedForm.getForm().getFormId());
            collectedData.setFormUri(contentUri.toString());
            collectedData.setFormXmlPath("");
            collectedData.setFormInstanceName(metaInstanceName);
            collectedData.setFormLastUpdatedDate(lastUpdatedDate);

            collectedData.setFormModules(lastLoadedForm.getForm().getModules());
            collectedData.setCollectedBy(loggedUser.getUsername());
            collectedData.setUpdatedBy("");
            collectedData.setSupervisedBy("");

            collectedData.setRecordId(member.getId());
            collectedData.setRecordEntity(member.getTableName());

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
            collectedData.setRecordEntity(member.getTableName());

            this.boxCollectedData.put(collectedData);
            Log.d("updating", "new collected data");
        }

        showCollectedData();

    }

    @Override
    public void onDeleteForm(Uri contentUri) {

        this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString(), QueryBuilder.StringOrder.CASE_SENSITIVE).build().remove(); //delete where formUri=contentUri

        showCollectedData();
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

}
