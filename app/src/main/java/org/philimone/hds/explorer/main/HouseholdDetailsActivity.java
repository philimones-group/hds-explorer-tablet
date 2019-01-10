package org.philimone.hds.explorer.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import org.philimone.hds.explorer.adapter.FormLoaderAdapter;
import org.philimone.hds.explorer.adapter.MemberArrayAdapter;
import org.philimone.hds.explorer.adapter.model.CollectedDataItem;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.Converter;
import org.philimone.hds.explorer.database.Database;
import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.listener.OdkFormResultListener;
import mz.betainteractive.odk.model.FilledForm;

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
    private List<FormDataLoader> formDataLoaders = new ArrayList<>();
    private FormDataLoader lastLoadedForm;

    private User loggedUser;

    private FormUtilities formUtilities;

    private int activityRequestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.household_details);

        this.loggedUser = (User) getIntent().getExtras().get("user");
        this.household = (Household) getIntent().getExtras().get("household");
        this.activityRequestCode = getIntent().getExtras().getInt("request_code");

        readFormDataLoader();

        formUtilities = new FormUtilities(this);

        initialize();
    }

    private void readFormDataLoader(){

        Object[] objs = (Object[]) getIntent().getExtras().get("dataloaders");

        for (int i=0; i < objs.length; i++){
            FormDataLoader formDataLoader = (FormDataLoader) objs[i];
            Log.d("tag", ""+formDataLoader.getForm().getFormId());
            if (formDataLoader.getForm().isHouseholdForm()){
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

        setHouseholdData();

        enableButtonsByFormLoaders();
        enableButtonsByIntentData();
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

        Database db = new Database(this);
        db.open();

        List<CollectedData> list = Queries.getAllCollectedDataBy(db, DatabaseHelper.CollectedData.COLUMN_RECORD_ID + "=? AND "+DatabaseHelper.CollectedData.COLUMN_TABLE_NAME + "=?", new String[]{ household.getId()+"", household.getTableName() } );
        List<Form> forms = Queries.getAllFormBy(db, null, null);
        List<CollectedDataItem> cdl = new ArrayList<>();

        for (CollectedData cd : list){
            if (hasFormDataLoadersContains(cd.getFormId())){
                Form form = getFormById(forms, cd.getFormId());
                cdl.add(new CollectedDataItem(household, form, cd));
            }
        }

        db.close();

        CollectedDataArrayAdapter adapter = new CollectedDataArrayAdapter(this, cdl);
        this.lvCollectedForms.setAdapter(adapter);
    }

    private void showHouseholdMembers(){
        Database db = new Database(this);
        db.open();

        List<Member> members = Queries.getAllMemberBy(db, DatabaseHelper.Member.COLUMN_HOUSE_CODE+"=?", new String[]{ household.getCode() } );

        MemberArrayAdapter adapter = new MemberArrayAdapter(this, members);
        this.lvHouseholdMembers.setAdapter(adapter);
    }

    private Region getRegion(String code){
        Database db = new Database(this);
        db.open();

        //Region region = Queries.getRegionBy(db, DatabaseHelper.Region.COLUMN_CODE + "=?", new String[]{ code } );
        Cursor cursor = db.query(Region.class, null, null, null, null, null);

        cursor.moveToFirst();
        Region region = Converter.cursorToRegion(cursor);

        db.close();

        return region;
    }

    private String getHierarchyName(Region region){

        if (region == null) return "";

        Database db = new Database(this);
        db.open();

        ApplicationParam param = Queries.getApplicationParamBy(db, DatabaseHelper.ApplicationParam.COLUMN_NAME + "=?", new String[]{ region.getLevel() } );

        db.close();

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
        Database db = new Database(this);
        db.open();

        String whereClause = DatabaseHelper.CollectedData.COLUMN_FORM_ID + "=? AND " + DatabaseHelper.CollectedData.COLUMN_RECORD_ID + "=? AND "+DatabaseHelper.CollectedData.COLUMN_TABLE_NAME + "=?";
        String[] whereArgs = new String[]{ formDataLoader.getForm().getFormId(),  ""+household.getId(), household.getTableName() };

        CollectedData collectedData = Queries.getCollectedDataBy(db, whereClause, whereArgs);

        db.close();

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

        if (collectedData == null){
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

        final FormLoaderAdapter adapter = new FormLoaderAdapter(this, loaders);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.household_details_forms_selector_lbl));
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

        //Save household and Update the object household
        if (household.getId()==0){
            int id = (int) db.insert(household);
            household.setId(id);
        }


        //search existing record
        String whereClause = DatabaseHelper.CollectedData.COLUMN_RECORD_ID + "=? AND "+DatabaseHelper.CollectedData.COLUMN_FORM_URI + "=? AND "+DatabaseHelper.CollectedData.COLUMN_TABLE_NAME + "=?";
        String[] whereArgs = new String[]{ ""+household.getId(), contentUri.toString(), household.getTableName() };

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

            collectedData.setRecordId(household.getId());
            collectedData.setTableName(household.getTableName());

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

            collectedData.setRecordId(household.getId());
            collectedData.setTableName(household.getTableName());

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

        //Save household and Update the object household
        if (household.getId()==0){
            int id = (int) db.insert(household);
            household.setId(id);
        }

        //search existing record
        String whereClause = DatabaseHelper.CollectedData.COLUMN_RECORD_ID + "=? AND "+DatabaseHelper.CollectedData.COLUMN_FORM_URI + "=? AND "+DatabaseHelper.CollectedData.COLUMN_TABLE_NAME + "=?";
        String[] whereArgs = new String[]{ ""+household.getId(), contentUri.toString(), household.getTableName() };

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

            collectedData.setRecordId(household.getId());
            collectedData.setTableName(household.getTableName());

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

            collectedData.setRecordId(household.getId());
            collectedData.setTableName(household.getTableName());

            db.update(CollectedData.class, collectedData.getContentValues(), whereClause, whereArgs);
            Log.d("updating", "new collected data");
        }

        db.close();

        showCollectedData();
    }

    @Override
    public void onDeleteForm(Uri contentUri) {
        Database db = new Database(this);
        db.open();
        db.delete(CollectedData.class, DatabaseHelper.CollectedData.COLUMN_FORM_URI+"=?", new String[]{ contentUri.toString() } );
        db.close();

        showCollectedData();
    }

    AlertDialog dialogNewhousehold;

    @Override
    public void onFormNotFound(final Uri contenUri) {
        buildDeleteSavedFormDialog(contenUri);
    }

    private void buildDeleteSavedFormDialog(final Uri contenUri){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.household_details_dialog_del_saved_form_title_lbl));
        builder.setMessage(getString(R.string.household_details_dialog_del_saved_form_msg_lbl));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.bt_yes_lbl, null);
        builder.setNegativeButton(R.string.bt_no_lbl, null);
        dialogNewhousehold = builder.create();

        dialogNewhousehold.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button b = dialogNewhousehold.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onDeleteForm(contenUri);
                        dialogNewhousehold.dismiss();
                    }
                });
            }
        });

        dialogNewhousehold.show();
    }
}
