package net.manhica.dss.explorer.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.manhica.dss.explorer.R;
import net.manhica.dss.explorer.adapter.CollectedDataArrayAdapter;
import net.manhica.dss.explorer.adapter.CollectedDataItem;
import net.manhica.dss.explorer.adapter.FormLoaderAdapter;
import net.manhica.dss.explorer.data.FormDataLoader;
import net.manhica.dss.explorer.database.Database;
import net.manhica.dss.explorer.database.DatabaseHelper;
import net.manhica.dss.explorer.database.Queries;
import net.manhica.dss.explorer.model.CollectedData;
import net.manhica.dss.explorer.model.Form;
import net.manhica.dss.explorer.model.Member;
import net.manhica.dss.explorer.model.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.listener.OdkFormResultListener;
import mz.betainteractive.odk.model.FilledForm;

public class MemberDetailsActivity extends Activity implements OdkFormResultListener {

    private TextView mbDetailsName;
    private TextView mbDetailsPermId;
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
            
    private Member member;
    private List<FormDataLoader> formDataLoaders = new ArrayList<>();
    private FormDataLoader lastLoadedForm;

    private User loggedUser;

    private FormUtilities formUtilities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_details);

        this.loggedUser = (User) getIntent().getExtras().get("user");
        this.member = (Member) getIntent().getExtras().get("member");
        readFormDataLoader();

        formUtilities = new FormUtilities(this);

        initialize();
    }

    private void readFormDataLoader(){

        Object[] objs = (Object[]) getIntent().getExtras().get("dataloaders");

        for (int i=0; i < objs.length; i++){
            FormDataLoader formDataLoader = (FormDataLoader) objs[i];

            if (isVisualizableForm(formDataLoader.getForm())){
                this.formDataLoaders.add(formDataLoader);
            }
        }
    }

    private boolean isVisualizableForm(Form form) {
        return  (member.getAge() >= form.getMinAge() && member.getAge() <= form.getMaxAge()) && (member.getGender().equals(form.getGender()) || form.getGender().equals("ALL"));
    }

    public void setMember(Member member){
        this.member = member;
    }

    private void initialize() {
        mbDetailsName = (TextView) findViewById(R.id.mbDetailsName);
        mbDetailsPermId = (TextView) findViewById(R.id.mbDetailsPermId);
        mbDetailsGender = (TextView) findViewById(R.id.mbDetailsGender);
        mbDetailsAge = (TextView) findViewById(R.id.mbDetailsAge);
        mbDetailsDob = (TextView) findViewById(R.id.mbDetailsDob);
        mbDetailsHouseNo = (TextView) findViewById(R.id.mbDetailsHouseNo);
        mbDetailsEndType = (TextView) findViewById(R.id.mbDetailsEndType);
        mbDetailsEndDate = (TextView) findViewById(R.id.mbDetailsEndDate);
        mbDetailsFather = (TextView) findViewById(R.id.mbDetailsFather);
        mbDetailsMother = (TextView) findViewById(R.id.mbDetailsMother);
        mbDetailsSpouse = (TextView) findViewById(R.id.mbDetailsSpouse);
        lvCollectedForms = (ListView) findViewById(R.id.lvCollectedForms);
        btMemDetailsCollectData = (Button) findViewById(R.id.btMemDetailsCollectData);
        btMemDetailsBack = (Button) findViewById(R.id.btMemDetailsBack);
        iconView = (ImageView) findViewById(R.id.iconView);

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

        setMemberData();

        enableButtonsByFormLoaders();
        enableButtonsByIntentData();
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

    private void setMemberData(){
        mbDetailsName.setText(member.getName());
        mbDetailsPermId.setText(member.getPermId());
        mbDetailsGender.setText(member.getGender());
        mbDetailsAge.setText(member.getAge()+"");
        mbDetailsDob.setText(member.getDob());
        mbDetailsHouseNo.setText(member.getHouseNumber());
        mbDetailsEndType.setText(getEndTypeMsg(member));
        mbDetailsEndDate.setText(getEndDateMsg(member));
        mbDetailsFather.setText(getParentName(member.getFatherName()));
        mbDetailsMother.setText(getParentName(member.getMotherName()));
        mbDetailsSpouse.setText(getSpouseName(member.getSpouseName()));

        if (member.isHouseholdHead()){
            iconView.setImageResource(R.mipmap.member_big_head_icon);
        }

        if (member.isSubsHouseholdHead()){
            iconView.setImageResource(R.mipmap.member_big_subs_icon);
        }

        showCollectedData();
    }

    private void onCollectedDataItemClicked(int position) {
        CollectedDataArrayAdapter adapter = (CollectedDataArrayAdapter) this.lvCollectedForms.getAdapter();
        CollectedDataItem dataItem = adapter.getItem(position);

        CollectedData collectedData = dataItem.getCollectedData();
        Member member = dataItem.getMember();
        FormDataLoader formDataLoader = getFormDataLoader(collectedData);

        openOdkForm(formDataLoader, collectedData);
    }

    private void showCollectedData() {
        //this.showProgress(true);

        Database db = new Database(this);
        db.open();

        List<CollectedData> list = Queries.getAllCollectedDataBy(db, DatabaseHelper.CollectedData.COLUMN_RECORD_ID + "=?", new String[]{ member.getId()+"" } );
        List<Form> forms = Queries.getAllFormBy(db, null, null);
        List<CollectedDataItem> cdl = new ArrayList<>();

        for (CollectedData cd : list){
            Form form = getFormById(forms, cd.getFormId());
            cdl.add(new CollectedDataItem(member, form, cd));
        }

        db.close();

        CollectedDataArrayAdapter adapter = new CollectedDataArrayAdapter(this, cdl);
        this.lvCollectedForms.setAdapter(adapter);
    }

    private Form getFormById(List<Form> forms, String formId){
        for (Form f : forms){
            if (f.getFormId().equals(formId)) return f;
        }

        return null;
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
        if (name.equals("Unknown")){
            return getString(R.string.member_details_unknown_lbl);
        }else {
            return name;
        }
    }

    private String getSpouseName(String name){
        if (name == null || name.isEmpty()){
            return "";
        }
        if (name.equals("Unknown")){
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
        Database db = new Database(this);
        db.open();

        String whereClause = DatabaseHelper.CollectedData.COLUMN_FORM_ID + "=? AND " + DatabaseHelper.CollectedData.COLUMN_RECORD_ID + "=? AND "+DatabaseHelper.CollectedData.COLUMN_TABLE_NAME + "=?";
        String[] whereArgs = new String[]{ formDataLoader.getForm().getFormId(),  ""+member.getId(), member.getTableName() };

        CollectedData collectedData = Queries.getCollectedDataBy(db, whereClause, whereArgs);

        db.close();

        return collectedData;
    }

    private void openOdkForm(FormDataLoader formDataLoader) {

        CollectedData collectedData = getCollectedData(formDataLoader);

        this.lastLoadedForm = formDataLoader;

        Form form = formDataLoader.getForm();

        FilledForm filledForm = new FilledForm(form.getFormId());
        filledForm.putAll(formDataLoader.getValues());

        if (collectedData == null){
            formUtilities.loadForm(filledForm);
        }else{
            formUtilities.loadForm(filledForm, collectedData.getFormUri());
        }

    }

    private void openOdkForm(FormDataLoader formDataLoader, CollectedData collectedData) {

        this.lastLoadedForm = formDataLoader;

        Form form = formDataLoader.getForm();

        FilledForm filledForm = new FilledForm(form.getFormId());
        filledForm.putAll(formDataLoader.getValues());

        if (collectedData == null){
            formUtilities.loadForm(filledForm);
        }else{
            formUtilities.loadForm(filledForm, collectedData.getFormUri());
        }

    }

    private void buildFormSelectorDialog(List<FormDataLoader> loaders) {

        final FormLoaderAdapter adapter = new FormLoaderAdapter(this, loaders);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.member_details_forms_selector_lbl));
        builder.setCancelable(true);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FormDataLoader formDataLoader = adapter.getItem(which);
                openOdkForm(formDataLoader);
            }
        });

        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        formUtilities.onActivityResult(requestCode, resultCode, data, this);
    }

    @Override
    public void onFormFinalized(Uri contentUri, File xmlFile, String metaInstanceName, String lastUpdatedDate) {
        Log.d("form finalized"," "+contentUri+", "+xmlFile);

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
            collectedData.setFormInstanceName(metaInstanceName);
            collectedData.setFormLastUpdatedDate(lastUpdatedDate);

            collectedData.setFormModule(lastLoadedForm.getForm().getModules());
            collectedData.setCollectedBy(loggedUser.getUsername());
            collectedData.setUpdatedBy("");
            collectedData.setSupervisedBy("");

            collectedData.setRecordId(member.getId());
            collectedData.setTableName(member.getTableName());

            db.insert(collectedData);
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

            db.update(CollectedData.class, collectedData.getContentValues(), whereClause, whereArgs);
            Log.d("updating", "new collected data");
        }

        db.close();

        showCollectedData();
    }

    @Override
    public void onFormUnFinalized(Uri contentUri, File xmlFile, String metaInstanceName, String lastUpdatedDate) {
        Log.d("form unfinalized"," "+contentUri);

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
            collectedData.setFormXmlPath("");
            collectedData.setFormInstanceName(metaInstanceName);
            collectedData.setFormLastUpdatedDate(lastUpdatedDate);

            collectedData.setFormModule(lastLoadedForm.getForm().getModules());
            collectedData.setCollectedBy(loggedUser.getUsername());
            collectedData.setUpdatedBy("");
            collectedData.setSupervisedBy("");

            collectedData.setRecordId(member.getId());
            collectedData.setTableName(member.getTableName());

            db.insert(collectedData);
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

            db.update(CollectedData.class, collectedData.getContentValues(), whereClause, whereArgs);
            Log.d("updating", "new collected data");
        }

        db.close();

        showCollectedData();
    }

    @Override
    public void onDeleteForm(Uri contentUri) {
        Log.d("delete uri", "needs to be implemented");
        Database db = new Database(this);
        db.open();
        db.delete(CollectedData.class, DatabaseHelper.CollectedData.COLUMN_FORM_URI+"=?", new String[]{ contentUri.toString() } );
        db.close();

        showCollectedData();
    }
}
