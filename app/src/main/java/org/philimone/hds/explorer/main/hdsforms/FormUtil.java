package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreEntity;
import org.philimone.hds.explorer.model.CoreFormExtension;
import org.philimone.hds.explorer.model.CoreFormExtension_;
import org.philimone.hds.explorer.model.Round;
import org.philimone.hds.explorer.model.Round_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.settings.generator.CodeGeneratorService;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.forms.listeners.FormCollectionListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.PreloadMap;
import org.philimone.hds.forms.model.RepeatColumnValue;
import org.philimone.hds.forms.parsers.ExcelFormParser;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.listener.OdkFormResultListener;
import mz.betainteractive.odk.model.FilledForm;
import mz.betainteractive.utilities.StringUtil;

public abstract class FormUtil<T extends CoreEntity> implements FormCollectionListener, OdkFormResultListener {

    public enum Mode { CREATE, EDIT }

    protected AppCompatActivity activity;
    protected Fragment fragment;
    protected FragmentManager fragmentManager;
    protected Context context;
    protected HForm form;
    protected T entity;
    protected CoreCollectedData collectedData;
    protected CodeGeneratorService codeGenerator;
    protected PreloadMap preloadedMap;

    protected boolean backgroundMode;
    protected boolean resumeMode;
    protected boolean postExecution;
    protected Round currentRound;
    protected User user;

    protected Box<ApplicationParam> boxAppParams;
    protected Box<Round> boxRounds;
    protected Box<CoreCollectedData> boxCoreCollectedData;
    protected Box<CoreFormExtension> boxCoreFormExtension;
    protected Box<CollectedData> boxCollectedData;

    protected FormUtilities odkFormUtilities;
    private FilledForm lastLoadedForm;

    protected Mode currentMode;
    
    protected FormUtilListener<T> listener;

    /* Load a Creator */

    /* Load a Editor */

    protected FormUtil(Fragment fragment, Context context, HForm hform, FormUtilities odkFormUtilities, FormUtilListener<T> listener){
        this.fragment = fragment;
        this.fragmentManager = fragment.getActivity().getSupportFragmentManager();
        this.context = context;
        this.form = hform;
        this.user = Bootstrap.getCurrentUser();
        this.codeGenerator = new CodeGeneratorService();
        this.preloadedMap = new PreloadMap();

        this.currentMode = Mode.CREATE;

        //ODK Form Utilities
        this.odkFormUtilities = odkFormUtilities;
        this.odkFormUtilities.setOdkFormResultListener(this);

        this.listener = listener;
    }

    protected FormUtil(Fragment fragment, Context context, HForm hform, T existentEntity, FormUtilities odkFormUtilities, FormUtilListener<T> listener){
        this.fragment = fragment;
        this.fragmentManager = fragment.getActivity().getSupportFragmentManager();
        this.context = context;
        this.form = hform;
        this.user = Bootstrap.getCurrentUser();
        this.codeGenerator = new CodeGeneratorService();
        this.preloadedMap = new PreloadMap();

        this.currentMode = Mode.EDIT;
        this.entity = existentEntity;

        //ODK Form Utilities
        this.odkFormUtilities = odkFormUtilities;
        this.odkFormUtilities.setOdkFormResultListener(this);

        this.listener = listener;
    }

    protected FormUtil(AppCompatActivity activity, Context context, HForm hform, FormUtilities odkFormUtilities, FormUtilListener<T> listener){
        this.activity = activity;
        this.fragmentManager = activity.getSupportFragmentManager();
        this.context = context;
        this.form = hform;
        this.user = Bootstrap.getCurrentUser();
        this.codeGenerator = new CodeGeneratorService();
        this.preloadedMap = new PreloadMap();

        this.currentMode = Mode.CREATE;

        //ODK Form Utilities
        this.odkFormUtilities = odkFormUtilities;
        this.odkFormUtilities.setOdkFormResultListener(this);

        this.listener = listener;
    }

    protected FormUtil(AppCompatActivity activity, Context context, HForm hform, T existentEntity, FormUtilities odkFormUtilities, FormUtilListener<T> listener){
        this.activity = activity;
        this.fragmentManager = activity.getSupportFragmentManager();
        this.context = context;
        this.form = hform;
        this.user = Bootstrap.getCurrentUser();
        this.codeGenerator = new CodeGeneratorService();
        this.preloadedMap = new PreloadMap();

        this.currentMode = Mode.EDIT;
        this.entity = existentEntity;

        //ODK Form Utilities
        this.odkFormUtilities = odkFormUtilities;
        this.odkFormUtilities.setOdkFormResultListener(this);

        this.listener = listener;
    }

    protected void initBoxes(){
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        this.boxRounds = ObjectBoxDatabase.get().boxFor(Round.class);
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
        this.boxCoreFormExtension = ObjectBoxDatabase.get().boxFor(CoreFormExtension.class);
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
    }

    protected void initialize() {
        this.currentRound = this.boxRounds.query().order(Round_.roundNumber, QueryBuilder.DESCENDING).build().findFirst();
        postExecution = Queries.getApplicationParamValue(this.boxAppParams, ApplicationParam.HFORM_POST_EXECUTION).equals("true");
    }

    public Context getContext(){
        return context;
    }

    protected abstract void preloadValues();

    protected abstract void preloadUpdatedValues();

    public abstract void collect();

    protected void executeCollectForm() {
        if (currentMode == Mode.CREATE) {
            preloadValues();
            FormFragment form = FormFragment.newInstance(this.fragmentManager, this.form, Bootstrap.getInstancesPath(), user.username, preloadedMap, postExecution, backgroundMode, resumeMode, this);
            form.startCollecting();
        }

        if (currentMode == Mode.EDIT) {
            preloadUpdatedValues();
            FormFragment form = FormFragment.newInstance(this.fragmentManager, this.form, Bootstrap.getInstancesPath(), user.username, this.entity.getRecentlyCreatedUri(), preloadedMap, postExecution, false, true, this);
            form.startCollecting();
        }
    }

    protected CoreFormExtension getFormExtension(CoreFormEntity formEntity){
        CoreFormEntity cformEntity = formEntity==CoreFormEntity.EXTERNAL_INMIGRATION ? CoreFormEntity.INMIGRATION : formEntity;
        return this.boxCoreFormExtension.query(CoreFormExtension_.formEntity.equal(cformEntity.code)).build().findFirst();
    }

    protected void collectExtensionForm(CollectedDataMap collectedValues){
        //Log.d("collecting", "extension"+collectedValues.get(HForm.COLUMN_ID));
        if (this.collectedData != null) {
            CoreFormExtension formExtension = getFormExtension(collectedData.formEntity);

            if (formExtension != null && formExtension.enabled) {
                //is required or optional
                //read the mapping columns and preload them with collected values
                // - understand how to preload repeat on ODK
                //get the odk form

                FilledForm odkFilledForm = createFilledForm(formExtension, collectedValues);
                String title = this.context.getString(R.string.form_util_extension_collect_lbl);

                if (formExtension.required) {

                    String message = this.context.getString(R.string.form_util_extension_collect_required_msg_lbl, getFormName());
                    DialogFactory.createMessageInfo(this.context, title, message, clickedButton -> {
                        //COLLECT EXTENSION FORM
                        openOdkForm(odkFilledForm);
                    }).show();
                } else {
                    //optional message
                    String message = this.context.getString(R.string.form_util_extension_collect_optional_lbl,getFormName());
                    DialogFactory.createMessageYN(this.context, title, message, new DialogFactory.OnYesNoClickListener() {
                        @Override
                        public void onYesClicked() {
                            //COLLECT EXTENSION FORM
                            openOdkForm(odkFilledForm);
                        }

                        @Override
                        public void onNoClicked() {
                            onFinishedExtensionCollection();
                        }
                    }).show();
                }
            } else {
                onFinishedExtensionCollection();
            }
        } else {
            onFinishedExtensionCollection();
        }
    }

    private FilledForm createFilledForm(CoreFormExtension formExtension, CollectedDataMap collectedValues){
        FilledForm filledForm = new FilledForm(formExtension.extFormId);
        Map<String, List<Map<String, String>>> repeatGroups = new LinkedHashMap<>();

        for (String key : formExtension.columnsMapping.keySet()){
            String value = formExtension.columnsMapping.get(key);
            Log.d("mapkv", key+":"+value);
            if (key.contains(".")){ //is repeat group variable - maps to load-repeat or constant

                if (value.startsWith("#")){ //from collected values

                    //LOAD TO A REPEAT GROUP FROM VARIABLES OR CONSTANTS IS NOT SUPPORTED YET
                    assert 1==0;

                } else if (value.startsWith("$")){ //load collected repeat values

                    //repeatGroupName.variableName -> $childs.outcomeType
                    try {
                        value = value.replace("$", "");
                        int iKeyPoint = key.indexOf(".");
                        int iValPoint = value.indexOf(".");
                        String odkRepeatGroup = key.substring(0, iKeyPoint);
                        String odkRepeatInnerColumn = key.substring(iKeyPoint + 1);
                        String hdsRepeatGroup = value.substring(0, iValPoint);
                        String hdsRepeatInnerColumn = value.substring(iValPoint + 1);
                        //Log.d("test", hdsRepeatGroup+":"+hdsRepeatInnerColumn);
                        if (odkRepeatGroup != null) {
                            List<Map<String, String>> repeatGroupLists = repeatGroups.get(odkRepeatGroup);
                            if (repeatGroupLists == null) {
                                repeatGroupLists = new ArrayList<>();
                                repeatGroups.put(odkRepeatGroup, repeatGroupLists);
                            }

                            //Read Values
                            RepeatColumnValue repeatColumnValue = collectedValues.getRepeatColumn(hdsRepeatGroup);
                            for (int repeatIndex = 0; repeatIndex < repeatColumnValue.getCount(); repeatIndex++) {
                                Map<String, String> mapRepeatItem = null;

                                if (repeatIndex >= 0 && repeatIndex < repeatGroupLists.size()) {
                                    mapRepeatItem = repeatGroupLists.get(repeatIndex);
                                } else {
                                    mapRepeatItem = new LinkedHashMap<>();
                                    repeatGroupLists.add(mapRepeatItem);
                                }

                                //map the inner method and its value
                                String hdsRepeatInnerValue = repeatColumnValue.get(hdsRepeatInnerColumn, repeatIndex).getValue();
                                mapRepeatItem.put(odkRepeatInnerColumn, hdsRepeatInnerValue);

                                //Log.d("mapkv-"+repeatIndex+"-"+odkRepeatInnerColumn, hdsRepeatInnerValue);
                            }

                            //save on filled form to be load to odk
                            filledForm.putRepeatObjects(odkRepeatGroup, repeatGroupLists);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            } else { //root variable -> maps to constant or variable

                if (value.startsWith("#")){ //variable - from collected values
                    value = value.replace("#", "");

                    ColumnValue columnValue = collectedValues.get(value);
                    Log.d("columnvalue", "get "+value+" is "+(columnValue==null ? "null" : columnValue.getValue()));
                    if (columnValue != null) {
                        filledForm.put(key, columnValue.getValue());
                    }

                } else { //constant
                    filledForm.put(key, value);
                }
            }
        }

        //load the repeats to filledforms
        //repeatGroupVar -> List<Objects with many attributes>

        return filledForm;
    }

    protected abstract void onFinishedExtensionCollection();

    //<editor-fold desc="ODK Form Utility methods">

    private CollectedData getCollectedData(FilledForm filledForm){

        List<CollectedData> cData = this.boxCollectedData.query(
                CollectedData_.formId.equal(filledForm.getFormName()).and(CollectedData_.collectedId.equal(this.collectedData.collectedId))
        ).filter((c) -> StringUtil.containsAny(c.formModules, this.user.getSelectedModules())).build().find();

        if (cData != null && cData.size()>0) return cData.get(0);

        return null;
    }

    private void openOdkForm(FilledForm filledForm) {

        this.lastLoadedForm = filledForm;

        CollectedData collectedData = getCollectedData(filledForm);

        if (collectedData == null){
            odkFormUtilities.loadForm(filledForm);
        }else{
            odkFormUtilities.loadForm(filledForm, collectedData.getFormUri(), this); //load existent form
        }

    }

    private void openOdkForm(FilledForm filledForm, CollectedData collectedData) {

        this.lastLoadedForm = filledForm;

        if (collectedData == null){
            odkFormUtilities.loadForm(filledForm);
        }else{
            odkFormUtilities.loadForm(filledForm, collectedData.getFormUri(), this);
        }

    }

    @Override
    public void onFormFinalized(Uri contentUri, String formId, File xmlFile, String metaInstanceName, Date lastUpdatedDate) {
        Log.d("ext form finalized"," "+contentUri+", "+xmlFile);

        //search existing record
        CollectedData odkCollectedData = this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString(), QueryBuilder.StringOrder.CASE_SENSITIVE)
                .and().equal(CollectedData_.collectedId, this.collectedData.collectedId, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (odkCollectedData == null){ //insert
            odkCollectedData = new CollectedData();
            odkCollectedData.setFormId(formId);
            odkCollectedData.setFormUri(contentUri.toString());
            odkCollectedData.setFormXmlPath(xmlFile.toString());
            odkCollectedData.setFormInstanceName(metaInstanceName);
            odkCollectedData.setFormLastUpdatedDate(lastUpdatedDate);

            odkCollectedData.setFormModules(user.getSelectedModules());

            odkCollectedData.setCollectedBy(user.getUsername());
            odkCollectedData.setUpdatedBy("");
            odkCollectedData.setSupervisedBy("");

            //collectedData.setRecordId(subject.getId());
            //collectedData.setRecordEntity(subject.getTableName());
            odkCollectedData.collectedId = this.collectedData.collectedId;

            this.boxCollectedData.put(odkCollectedData);

            //Log.d("inserting", "new collected data");
        }else{ //update
            odkCollectedData.setFormId(formId);
            odkCollectedData.setFormUri(contentUri.toString());
            odkCollectedData.setFormXmlPath(xmlFile.toString());
            odkCollectedData.setFormInstanceName(metaInstanceName);
            odkCollectedData.setFormLastUpdatedDate(lastUpdatedDate);

            //collectedData.setFormModule(lastLoadedForm.getForm().getModules());
            //collectedData.setCollectedBy(loggedUser.getUsername());
            odkCollectedData.setUpdatedBy(user.getUsername());
            //collectedData.setSupervisedBy("");

            //collectedData.setRecordId(subject.getId());
            //collectedData.setRecordEntity(subject.getTableName());
            odkCollectedData.collectedId = this.collectedData.collectedId;

            this.boxCollectedData.put(odkCollectedData);
            Log.d("updating", "new extension collected data");
        }

        //save corecollecteddata
        this.collectedData.extensionCollected = true;
        this.collectedData.extensionCollectedUri = odkCollectedData.formUri;
        this.boxCoreCollectedData.put(collectedData);

        onFinishedExtensionCollection();
    }

    @Override
    public void onFormUnFinalized(Uri contentUri, String formId, File xmlFile, String metaInstanceName, Date lastUpdatedDate) {
        Log.d("ext form unfinalized"," "+contentUri);

        //search existing record
        CollectedData odkCollectedData = this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString(), QueryBuilder.StringOrder.CASE_SENSITIVE)
                .and().equal(CollectedData_.collectedId, this.collectedData.collectedId, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (odkCollectedData == null){ //insert
            odkCollectedData = new CollectedData();
            odkCollectedData.setFormId(formId);
            odkCollectedData.setFormUri(contentUri.toString());
            odkCollectedData.setFormXmlPath("");
            odkCollectedData.setFormInstanceName(metaInstanceName);
            odkCollectedData.setFormLastUpdatedDate(lastUpdatedDate);

            odkCollectedData.setFormModules(user.getSelectedModules());

            odkCollectedData.setCollectedBy(user.getUsername());
            odkCollectedData.setUpdatedBy("");
            odkCollectedData.setSupervisedBy("");

            //collectedData.setRecordId(subject.getId());
            //collectedData.setRecordEntity(subject.getTableName());
            odkCollectedData.collectedId = this.collectedData.collectedId;

            this.boxCollectedData.put(odkCollectedData);
            //Log.d("inserting", "new ext collected data");
        }else{ //update
            odkCollectedData.setFormId(formId);
            odkCollectedData.setFormUri(contentUri.toString());
            odkCollectedData.setFormXmlPath("");
            odkCollectedData.setFormInstanceName(metaInstanceName);
            odkCollectedData.setFormLastUpdatedDate(lastUpdatedDate);

            //collectedData.setFormModule(lastLoadedForm.getForm().getModules());
            //collectedData.setCollectedBy(loggedUser.getUsername());
            odkCollectedData.setUpdatedBy(user.getUsername());
            //collectedData.setSupervisedBy("");

            odkCollectedData.collectedId = this.collectedData.collectedId;

            this.boxCollectedData.put(odkCollectedData);
            //Log.d("updating", "new ext collected data");
        }

        //save corecollecteddata
        this.collectedData.extensionCollected = true;
        this.collectedData.extensionCollectedUri = odkCollectedData.formUri;
        this.boxCoreCollectedData.put(collectedData);

        onFinishedExtensionCollection();
    }

    @Override
    public void onDeleteForm(Uri contentUri) {

        this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString(), QueryBuilder.StringOrder.CASE_SENSITIVE).build().remove(); //delete where formUri=contentUri

        //save corecollecteddata
        this.collectedData.extensionCollected = false;
        this.collectedData.extensionCollectedUri = null;
        this.boxCoreCollectedData.put(collectedData);

        onFinishedExtensionCollection();
    }

    @Override
    public void onFormNotFound(final Uri contenUri) {
        buildDeleteSavedFormDialog(contenUri);
        onFinishedExtensionCollection();
    }

    private void buildDeleteSavedFormDialog(final Uri contenUri){

        DialogFactory.createMessageYN(this.getContext(), R.string.household_details_dialog_del_saved_form_title_lbl, R.string.household_details_dialog_del_saved_form_msg_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                onDeleteForm(contenUri);
            }

            @Override
            public void onNoClicked() {
                onFinishedExtensionCollection();
            }
        }).show();
    }
    //</editor-fold>

    private String getFormName(){

        if (this instanceof ChangeHeadFormUtil) return this.context.getString(R.string.core_entity_changehoh_lbl);
        if (this instanceof DeathFormUtil) return this.context.getString(R.string.core_entity_death_lbl);
        if (this instanceof ExternalInMigrationFormUtil) return this.context.getString(R.string.core_entity_external_inmigration_lbl);
        if (this instanceof HouseholdFormUtil) return this.context.getString(R.string.core_entity_household_lbl);
        if (this instanceof IncompleteVisitFormUtil) return this.context.getString(R.string.core_entity_member_not_visited_lbl);
        if (this instanceof InternalInMigrationFormUtil) return this.context.getString(R.string.core_entity_inmigration_lbl);
        if (this instanceof MemberEnumerationFormUtil) return this.context.getString(R.string.core_entity_member_enu_lbl);
        if (this instanceof OutmigrationFormUtil) return this.context.getString(R.string.core_entity_outmigration_lbl);
        if (this instanceof MaritalRelationshipFormUtil) return this.context.getString(R.string.core_entity_marital_relationship_lbl);
        if (this instanceof PregnancyRegistrationFormUtil) return this.context.getString(R.string.core_entity_pregnancy_reg_lbl);
        if (this instanceof PregnancyOutcomeFormUtil) return this.context.getString(R.string.core_entity_pregnancy_out_lbl);
        if (this instanceof VisitFormUtil) return this.context.getString(R.string.core_entity_visit_lbl);

        return null;
    }

    /* statics */
    protected static HForm getVisitForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.visit_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getMemberEnuForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.member_enu_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getHouseholdForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.household_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getMaritalRelationshipForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.marital_relationship_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getExternalInMigrationForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.ext_inmigration_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getInMigrationForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.inmigration_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getOutmigrationForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.outmigration_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getPregnancyRegistrationForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.pregnancy_registration_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getPregnancyOutcomeForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.pregnancy_outcome_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getDeathForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.death_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getChangeHeadForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.change_head_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getIncompleteVisitForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.incomplete_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }



}
