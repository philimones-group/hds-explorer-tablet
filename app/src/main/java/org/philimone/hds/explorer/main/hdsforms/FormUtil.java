package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.CoreEntity;
import org.philimone.hds.explorer.model.CoreFormColumnOptions;
import org.philimone.hds.explorer.model.CoreFormColumnOptions_;
import org.philimone.hds.explorer.model.CoreFormExtension;
import org.philimone.hds.explorer.model.CoreFormExtension_;
import org.philimone.hds.explorer.model.*;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.SubjectEntity;
import org.philimone.hds.explorer.model.oldstate.SavedEntityState;
import org.philimone.hds.explorer.server.settings.generator.CodeGeneratorService;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.LoadingDialog;
import org.philimone.hds.forms.listeners.FormCollectionListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.PreloadMap;
import org.philimone.hds.forms.model.RepeatColumnValue;
import org.philimone.hds.forms.parsers.ExcelFormParser;
import org.philimone.hds.forms.parsers.form.model.FormOptions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.listener.OdkFormResultListener;
import mz.betainteractive.odk.model.FilledForm;
import mz.betainteractive.odk.model.OdkFormLoadData;
import mz.betainteractive.odk.task.OdkFormLoadResult;
import mz.betainteractive.utilities.ReflectionUtils;
import mz.betainteractive.utilities.StringUtil;

public abstract class FormUtil<T extends CoreEntity> implements FormCollectionListener, OdkFormResultListener {

    public enum Mode { CREATE, EDIT }

    protected AppCompatActivity activity;
    protected Fragment fragment;
    protected FragmentManager fragmentManager;
    protected Context context;
    protected HForm form;
    protected Household household;
    protected T entity;
    protected CoreCollectedData collectedData;
    protected CodeGeneratorService codeGenerator;
    protected PreloadMap preloadedMap;
    protected String formTitle;

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
    protected Box<Household> boxHouseholds;
    protected Box<Region> mBoxRegions;
    protected Box<Member> mBoxMembers;
    protected Box<SavedEntityState> boxSavedEntityStates;

    protected FormUtilities odkFormUtilities;
    private FilledForm lastLoadedForm;

    protected Mode currentMode;
    
    protected FormUtilListener<T> listener;

    protected LoadingDialog loadingDialog;

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

        this.loadingDialog = new LoadingDialog(context);
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

        initBoxes();
        readCollectedDataForEdit();

        this.loadingDialog = new LoadingDialog(context);
    }

    protected FormUtil(Fragment fragment, Context context, HForm hform, T existentEntity, CoreCollectedData coreCollectedData, FormUtilities odkFormUtilities, FormUtilListener<T> listener){
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

        initBoxes();
        //readCollectedDataForEdit();
        this.collectedData = coreCollectedData;
        if (this.collectedData.uploaded && !collectedData.uploadedWithError) {
            //make the form readonly
            this.form.setReadonly(true);
        }

        this.loadingDialog = new LoadingDialog(context);
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

        this.loadingDialog = new LoadingDialog(context);
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

        this.loadingDialog = new LoadingDialog(context);

        initBoxes();
        readCollectedDataForEdit();
    }

    private void readCollectedDataForEdit() {
        //this.collectedData = this.boxCoreCollectedData.query(CoreCollectedData_.formEntityId.equal(this.entity.getId()).and(CoreCollectedData_.collectedId.equal(this.entity.getCollectedId()))).build().findFirst();
        this.collectedData = this.boxCoreCollectedData.query(CoreCollectedData_.formEntityId.equal(this.entity.getId())).build().findFirst();
        Log.d("found-collected", ""+this.collectedData);

        if (this.collectedData.uploaded && !collectedData.uploadedWithError) {
            //make the form readonly
            this.form.setReadonly(true);
        }
    }

    protected void initBoxes(){
        if (this.boxAppParams == null) {
            this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        }

        if (this.boxRounds == null) {
            this.boxRounds = ObjectBoxDatabase.get().boxFor(Round.class);
        }

        if (this.boxCoreCollectedData == null) {
            this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
        }

        if (this.boxCoreFormExtension == null) {
            this.boxCoreFormExtension = ObjectBoxDatabase.get().boxFor(CoreFormExtension.class);
        }

        if (this.boxHouseholds == null) {
            this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        }

        if (this.boxCollectedData == null) {
            this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        }

        if (this.boxSavedEntityStates == null) {
            this.boxSavedEntityStates = ObjectBoxDatabase.get().boxFor(SavedEntityState.class);
        }

        this.mBoxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.mBoxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
    }

    protected void initialize() {
        this.currentRound = this.boxRounds.query().order(Round_.roundNumber, QueryBuilder.DESCENDING).build().findFirst();
        postExecution = Queries.getApplicationParamValue(this.boxAppParams, ApplicationParam.HFORM_POST_EXECUTION).equals("true");
    }

    public Context getContext(){
        return context;
    }

    private void showLoadingDialog(String msg, boolean show){
        Log.d("showloading dialog", show+" - "+msg);
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.dismiss();
        }
    }

    private void showLoadingDialog(@StringRes int msgResId, boolean show){
        showLoadingDialog(this.context.getString(msgResId), show);
    }

    @Override
    public void onFormLoaded(Object[] data) {
        showLoadingDialog("", false);
    }

    protected abstract void preloadValues();

    protected abstract void preloadUpdatedValues();

    public abstract void collect();

    protected void executeCollectForm() {
        Log.d("xloading", "creating form");
        showLoadingDialog(R.string.loading_dialog_form_load_lbl, true);

        new LoadFormFragmentTask().execute();

        /* //WE MOVED THIS TO A THREAD - TO KEEP THE LOADING DIALOG DISPLAYED
        if (currentMode == Mode.CREATE) {
            Log.d("xloading", "creating form");
            showLoadingDialog(R.string.loading_dialog_form_load_lbl, true);
            Log.d("xloading", "preloading");
            preloadValues();
            this.form.setCustomTitle(this.formTitle);
            Log.d("xloading", "creating form fragment");
            FormFragment form = FormFragment.newInstance(this.fragmentManager, this.form, Bootstrap.getInstancesPath(context), user.username, preloadedMap, postExecution, backgroundMode, resumeMode, this);
            Log.d("xloading", "starting form fragment");
            form.startCollecting();
        }

        if (currentMode == Mode.EDIT) {
            showLoadingDialog(R.string.loading_dialog_form_load_lbl, true);
            preloadUpdatedValues();
            this.form.setCustomTitle(this.formTitle);
            String savedXmlFilename = this.entity.getRecentlyCreatedUri()==null ? this.collectedData.formFilename : this.entity.getRecentlyCreatedUri();
            FormFragment form = FormFragment.newInstance(this.fragmentManager, this.form, Bootstrap.getInstancesPath(context), user.username, savedXmlFilename, preloadedMap, postExecution, backgroundMode, true, this);
            form.startCollecting();
        }
        */
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
                        openOdkForm(new OdkFormLoadData(formExtension.extFormId, odkFilledForm, false, true));
                    }).show();
                } else {
                    //optional message
                    String message = this.context.getString(R.string.form_util_extension_collect_optional_lbl,getFormName());
                    DialogFactory.createMessageYN(this.context, title, message, new DialogFactory.OnYesNoClickListener() {
                        @Override
                        public void onYesClicked() {
                            //COLLECT EXTENSION FORM
                            openOdkForm(new OdkFormLoadData(formExtension.extFormId, odkFilledForm, false, true));
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

    public void editExtensionForm(CollectedData odkCollectedData) {

        CoreFormExtension formExtension = getFormExtension(collectedData.formEntity);

        if (odkCollectedData != null) {
            FilledForm filledForm = new FilledForm(formExtension.extFormId);
            openOdkForm(new OdkFormLoadData(formExtension.extFormId, filledForm, false, true), odkCollectedData);
        }
    }

    private FilledForm createFilledForm(CoreFormExtension formExtension, CollectedDataMap collectedValues){
        FilledForm filledForm = new FilledForm(formExtension.extFormId);
        Map<String, List<Map<String, String>>> repeatGroups = new LinkedHashMap<>();
        final String householdPrefix = "Household.";

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

                            if (repeatColumnValue != null) {
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

                } else if (value.startsWith(householdPrefix)) {
                    //Load data from Household object
                    String variableName = value.replaceFirst(householdPrefix, "");
                    if (!StringUtil.isBlank(variableName) && this.household != null) {
                        String variableValue = ReflectionUtils.getValueByName(this.household, variableName);
                        filledForm.put(key, variableValue);
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

    private void openOdkForm(OdkFormLoadData loadData) {

        this.lastLoadedForm = loadData.preloadedData;

        CollectedData collectedData = getCollectedData(loadData.preloadedData);

        if (collectedData == null){
            odkFormUtilities.loadForm(loadData);
        }else{
            odkFormUtilities.loadForm(loadData, collectedData.getFormUri(), collectedData.formXmlPath, this); //load existent form
        }

    }

    private void openOdkForm(OdkFormLoadData loadData, CollectedData collectedData) {

        this.lastLoadedForm = loadData.preloadedData;

        if (collectedData == null){
            odkFormUtilities.loadForm(loadData);
        }else{
            odkFormUtilities.loadForm(loadData, collectedData.getFormUri(), collectedData.formXmlPath, this);
        }

    }

    @Override
    public void onFormFinalized(OdkFormLoadData formLoadData, Uri contentUri, String formId, String instanceFileUri, String metaInstanceName, Date lastUpdatedDate) {
        Log.d("ext form finalized"," "+contentUri+", file-uri = "+instanceFileUri);

        //search existing record
        CollectedData odkCollectedData = this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString(), QueryBuilder.StringOrder.CASE_SENSITIVE)
                .and().equal(CollectedData_.collectedId, this.collectedData.collectedId, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (odkCollectedData == null){ //insert
            odkCollectedData = new CollectedData();
            odkCollectedData.setFormId(formId);
            odkCollectedData.setFormUri(contentUri.toString());
            odkCollectedData.setFormXmlPath(instanceFileUri);
            odkCollectedData.setFormInstanceName(metaInstanceName);
            odkCollectedData.setFormLastUpdatedDate(lastUpdatedDate);
            odkCollectedData.formFinalized = true;

            odkCollectedData.setFormModules(user.getSelectedModules());

            odkCollectedData.setCollectedBy(user.getUsername());
            odkCollectedData.setUpdatedBy("");
            odkCollectedData.setSupervisedBy("");

            odkCollectedData.setRecordId(getCurrentRecordId());
            odkCollectedData.setRecordEntity(getCurrentTablename());
            odkCollectedData.collectedId = this.collectedData.collectedId;

            this.boxCollectedData.put(odkCollectedData);

            //Log.d("inserting", "new collected data");
        }else{ //update
            odkCollectedData.setFormId(formId);
            odkCollectedData.setFormUri(contentUri.toString());
            //odkCollectedData.setFormXmlPath(instanceFileUri);
            odkCollectedData.setFormInstanceName(metaInstanceName);
            odkCollectedData.setFormLastUpdatedDate(lastUpdatedDate);
            odkCollectedData.formFinalized = true;

            //collectedData.setFormModule(lastLoadedForm.getForm().getModules());
            //collectedData.setCollectedBy(loggedUser.getUsername());
            odkCollectedData.setUpdatedBy(user.getUsername());
            //collectedData.setSupervisedBy("");

            odkCollectedData.setRecordId(getCurrentRecordId());
            odkCollectedData.setRecordEntity(getCurrentTablename());
            odkCollectedData.collectedId = this.collectedData.collectedId;

            this.boxCollectedData.put(odkCollectedData);
            Log.d("updating", "new extension collected data");
        }

        //save corecollecteddata
        this.collectedData.extensionCollected = true;
        this.collectedData.extensionCollectedUri = odkCollectedData.formUri;
        this.collectedData.extensionCollectedFilepath = odkCollectedData.formXmlPath;
        this.boxCoreCollectedData.put(collectedData);

        onFinishedExtensionCollection();
    }

    @Override
    public void onFormUnFinalized(OdkFormLoadData formLoadData, Uri contentUri, String formId, String instanceFileUri, String metaInstanceName, Date lastUpdatedDate) {
        Log.d("ext form unfinalized"," "+contentUri);

        buildExtensionFormNotFinalizedDialog(new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                odkFormUtilities.editLastOpenedForm();
            }

            @Override
            public void onNoClicked() {
                //saving
                saveUnfinalizedForm(contentUri, formId, instanceFileUri, metaInstanceName, lastUpdatedDate);
                onFinishedExtensionCollection();
            }
        });


    }

    @Override
    public void onDeleteForm(OdkFormLoadData formLoadData, Uri contentUri, String instanceFileUri) {

        this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString(), QueryBuilder.StringOrder.CASE_SENSITIVE).build().remove(); //delete where formUri=contentUri

        //delete instanceFileUri - already deleted by removing instance
        if (instanceFileUri != null) {
            //odkFormUtilities.deleteInstanceFile(instanceFileUri);
        }

        //save corecollecteddata
        this.collectedData.extensionCollected = false;
        this.collectedData.extensionCollectedUri = null;
        this.boxCoreCollectedData.put(collectedData);

        onFinishedExtensionCollection();
    }

    @Override
    public void onFormLoadError(OdkFormLoadData formLoadData, OdkFormLoadResult result) {
        onFinishedExtensionCollection();
    }

    @Override
    public void onFormInstanceNotFound(OdkFormLoadData formLoadData, final Uri contenUri) {
        buildDeleteFormInstanceNotFoundDialog(formLoadData, contenUri);
        onFinishedExtensionCollection();
    }

    private void saveUnfinalizedForm(Uri contentUri, String formId, String instanceFileUri, String metaInstanceName, Date lastUpdatedDate) {
        //search existing record
        CollectedData odkCollectedData = this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString(), QueryBuilder.StringOrder.CASE_SENSITIVE)
                .and().equal(CollectedData_.collectedId, this.collectedData.collectedId, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (odkCollectedData == null){ //insert
            odkCollectedData = new CollectedData();
            odkCollectedData.setFormId(formId);
            odkCollectedData.setFormUri(contentUri.toString());
            odkCollectedData.setFormXmlPath(instanceFileUri);
            odkCollectedData.setFormInstanceName(metaInstanceName);
            odkCollectedData.setFormLastUpdatedDate(lastUpdatedDate);
            odkCollectedData.formFinalized = false;

            odkCollectedData.setFormModules(user.getSelectedModules());

            odkCollectedData.setCollectedBy(user.getUsername());
            odkCollectedData.setUpdatedBy("");
            odkCollectedData.setSupervisedBy("");

            odkCollectedData.setRecordId(getCurrentRecordId());
            odkCollectedData.setRecordEntity(getCurrentTablename());
            odkCollectedData.collectedId = this.collectedData.collectedId;

            this.boxCollectedData.put(odkCollectedData);
            //Log.d("inserting", "new ext collected data");
        }else{ //update
            odkCollectedData.setFormId(formId);
            odkCollectedData.setFormUri(contentUri.toString());
            //odkCollectedData.setFormXmlPath(instanceFileUri);
            odkCollectedData.setFormInstanceName(metaInstanceName);
            odkCollectedData.setFormLastUpdatedDate(lastUpdatedDate);
            odkCollectedData.formFinalized = false;

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
        this.collectedData.extensionCollectedFilepath = odkCollectedData.formXmlPath;
        this.boxCoreCollectedData.put(collectedData);
    }

    private void buildDeleteFormInstanceNotFoundDialog(OdkFormLoadData formLoadData, final Uri contenUri){

        DialogFactory.createMessageYN(this.getContext(), R.string.household_details_dialog_del_saved_form_title_lbl, R.string.household_details_dialog_del_saved_form_msg_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                onDeleteForm(formLoadData, contenUri, null);
            }

            @Override
            public void onNoClicked() {
                onFinishedExtensionCollection();
            }
        }).show();
    }

    private void buildExtensionFormNotFinalizedDialog(DialogFactory.OnYesNoClickListener yesNoClickListener){

        CoreFormExtension extension = this.collectedData.extension.getTarget();

        String title = getContext().getString(R.string.warning_lbl);
        String form = getContext().getString(extension.formEntity.name);
        String message = getContext().getString(R.string.odk_unfinished_extension_msg, form);
        String yes = getContext().getString(R.string.odk_unfinished_extension_button_edit);
        String no = getContext().getString(R.string.odk_unfinished_extension_button_save);

        DialogFactory.createMessageYN(this.getContext(), title, message, yes, no, yesNoClickListener).show();
    }

    //</editor-fold>

    private SubjectEntity getCurrentTablename() {

        if (this.entity instanceof Death) return SubjectEntity.MEMBER;
        if (this.entity instanceof HeadRelationship) return SubjectEntity.HOUSEHOLD;
        if (this.entity instanceof Household) return SubjectEntity.HOUSEHOLD;
        if (this.entity instanceof IncompleteVisit) return SubjectEntity.MEMBER;
        if (this.entity instanceof Inmigration) return SubjectEntity.MEMBER;
        if (this.entity instanceof MaritalRelationship) return SubjectEntity.MEMBER;
        if (this.entity instanceof Member) return SubjectEntity.MEMBER;
        if (this.entity instanceof Outmigration) return SubjectEntity.MEMBER;
        if (this.entity instanceof PregnancyChild) return SubjectEntity.MEMBER;
        if (this.entity instanceof PregnancyOutcome) return SubjectEntity.MEMBER;
        if (this.entity instanceof PregnancyRegistration) return SubjectEntity.MEMBER;
        if (this.entity instanceof Region) return SubjectEntity.REGION;
        if (this.entity instanceof Visit) return SubjectEntity.HOUSEHOLD;
        if (this.entity instanceof RegionHeadRelationship) return SubjectEntity.REGION;

        return SubjectEntity.INVALID_ENUM;
    }

    private long getCurrentRecordId() {
        String code = "";
        SubjectEntity subject = null;

        if (this.entity instanceof Death) {
            code = ((Death) this.entity).memberCode;
            subject = SubjectEntity.MEMBER;
        } else if (this.entity instanceof HeadRelationship) {
            code = ((HeadRelationship) this.entity).householdCode;
            subject = SubjectEntity.HOUSEHOLD;
        } else if (this.entity instanceof Household) {
            return this.entity.getId();
        } else if (this.entity instanceof IncompleteVisit) {
            code = ((IncompleteVisit) this.entity).memberCode;
            subject = SubjectEntity.MEMBER;
        } else if (this.entity instanceof Inmigration) {
            code = ((Inmigration) this.entity).memberCode;
            subject = SubjectEntity.MEMBER;
        } else if (this.entity instanceof MaritalRelationship) {
            code = ((MaritalRelationship) this.entity).memberA_code;
            subject = SubjectEntity.MEMBER;
        } else if (this.entity instanceof Member) {
            return this.entity.getId();
        } else if (this.entity instanceof Outmigration) {
            code = ((Outmigration) this.entity).memberCode;
            subject = SubjectEntity.MEMBER;
        } else if (this.entity instanceof PregnancyChild) {
            PregnancyOutcome outcome = ((PregnancyChild) this.entity).outcome.getTarget();
            code = outcome.motherCode;
            subject = SubjectEntity.MEMBER;
        } else if (this.entity instanceof PregnancyOutcome) {
            code = ((PregnancyOutcome) this.entity).motherCode;
            subject = SubjectEntity.MEMBER;
        } else if (this.entity instanceof PregnancyRegistration) {
            code = ((PregnancyRegistration) this.entity).motherCode;
            subject = SubjectEntity.MEMBER;
        } else if (this.entity instanceof Region) {
            return this.entity.getId();
        } else if (this.entity instanceof Visit) {
            code = ((Visit) this.entity).householdCode;
            subject = SubjectEntity.HOUSEHOLD;
        } else if (this.entity instanceof RegionHeadRelationship){
            code = ((RegionHeadRelationship) this.entity).regionCode;
            subject = SubjectEntity.REGION;
        }

          switch (subject) {
              case REGION:
                  Region region = this.mBoxRegions.query(Region_.code.equal(code)).build().findFirst();
                  return Objects.requireNonNull(region).getId();
              case HOUSEHOLD:
                  Household hh = this.boxHouseholds.query(Household_.code.equal(code)).build().findFirst();
                  return Objects.requireNonNull(hh).getId();
              case MEMBER:
                  Member member = this.mBoxMembers.query(Member_.code.equal(code)).build().findFirst();
                  return Objects.requireNonNull(member).getId();
          }

        return -1;
    }

    private String getFormName(){

        if (this.collectedData.formEntity == CoreFormEntity.REGION) return this.context.getString(R.string.core_entity_region_lbl);
        if (this.collectedData.formEntity == CoreFormEntity.CHANGE_HOUSEHOLD_HEAD) return this.context.getString(R.string.core_entity_changehoh_lbl);
        if (this.collectedData.formEntity == CoreFormEntity.DEATH) return this.context.getString(R.string.core_entity_death_lbl);
        if (this.collectedData.formEntity == CoreFormEntity.EXTERNAL_INMIGRATION) return this.context.getString(R.string.core_entity_external_inmigration_lbl);
        if (this.collectedData.formEntity == CoreFormEntity.HOUSEHOLD) return this.context.getString(R.string.core_entity_household_lbl);
        if (this.collectedData.formEntity == CoreFormEntity.INCOMPLETE_VISIT) return this.context.getString(R.string.core_entity_member_not_visited_lbl);
        if (this.collectedData.formEntity == CoreFormEntity.INMIGRATION) return this.context.getString(R.string.core_entity_inmigration_lbl);
        if (this.collectedData.formEntity == CoreFormEntity.MEMBER_ENU) return this.context.getString(R.string.core_entity_member_enu_lbl);
        if (this.collectedData.formEntity == CoreFormEntity.OUTMIGRATION) return this.context.getString(R.string.core_entity_outmigration_lbl);
        if (this.collectedData.formEntity == CoreFormEntity.MARITAL_RELATIONSHIP) return this.context.getString(R.string.core_entity_marital_relationship_lbl);
        if (this.collectedData.formEntity == CoreFormEntity.PREGNANCY_REGISTRATION) return this.context.getString(R.string.core_entity_pregnancy_reg_lbl);
        if (this.collectedData.formEntity == CoreFormEntity.PREGNANCY_OUTCOME) return this.context.getString(R.string.core_entity_pregnancy_out_lbl);
        if (this.collectedData.formEntity == CoreFormEntity.VISIT) return this.context.getString(R.string.core_entity_visit_lbl);
        if (this.collectedData.formEntity == CoreFormEntity.CHANGE_REGION_HEAD) return this.context.getString(R.string.core_entity_changehor_lbl);

        return null;
    }

    protected String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /* statics */
    protected static HForm getVisitForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.visit_form);
        HForm form = retrieveForm(inputStream);
        return form;
    }

    protected static HForm getMemberEnuForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.member_enu_form);
        HForm form = retrieveForm(inputStream);
        return form;
    }

    protected static HForm getRegionForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.region_form);
        HForm form = retrieveForm(inputStream);
        return form;
    }

    protected static HForm getHouseholdForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.household_form);
        HForm form = retrieveForm(inputStream);
        return form;
    }

    protected static HForm getPreHouseholdForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.pre_registration_household_form);
        HForm form = retrieveForm(inputStream);
        return form;
    }

    protected static HForm getMaritalRelationshipForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.marital_relationship_form);
        HForm form = retrieveForm(inputStream);
        return form;
    }

    protected static HForm getExternalInMigrationForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.ext_inmigration_form);
        HForm form = retrieveForm(inputStream);
        return form;
    }

    protected static HForm getInMigrationForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.inmigration_form);
        HForm form = retrieveForm(inputStream);
        return form;
    }

    protected static HForm getOutmigrationForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.outmigration_form);
        HForm form = retrieveForm(inputStream);
        return form;
    }

    protected static HForm getPregnancyRegistrationForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.pregnancy_registration_form);
        HForm form = retrieveForm(inputStream);
        return form;
    }

    protected static HForm getPregnancyOutcomeForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.pregnancy_outcome_form);
        HForm form = retrieveForm(inputStream);
        return form;
    }

    protected static HForm getDeathForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.death_form);
        HForm form = retrieveForm(inputStream);
        return form;
    }

    protected static HForm getChangeHeadForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.change_head_form);
        HForm form = retrieveForm(inputStream);
        return form;
    }

    protected static HForm getIncompleteVisitForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.incomplete_form);
        HForm form = retrieveForm(inputStream);
        return form;
    }

    protected static HForm getChangeRegionHeadForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.change_region_head_form);
        HForm form = retrieveForm(inputStream);
        return form;
    }

    protected static HForm retrieveForm(InputStream inputStream) {
        HForm form = new ExcelFormParser(inputStream).getForm();

        importOptions(form);

        return form;
    }

    private static void importOptions(HForm form) {
        Box<CoreFormColumnOptions> boxCoreFormOptions = ObjectBoxDatabase.get().boxFor(CoreFormColumnOptions.class);
        boolean formHasCustomOptions = boxCoreFormOptions.query(CoreFormColumnOptions_.formName.equal(form.getFormId())).build().count() > 0;

        if (formHasCustomOptions) {
            //map options <column, option, label, labelcode>
            List<CoreFormColumnOptions> list = boxCoreFormOptions.query(CoreFormColumnOptions_.formName.equal(form.getFormId())).order(CoreFormColumnOptions_.id).build().find();

            //clear column options
            Set<String> columnNames = new HashSet<>();
            for (CoreFormColumnOptions opt : list) {
                columnNames.add(opt.columnName);
            }

            for (String columnName : columnNames) {
                Column column = form.getColumn(columnName);
                column.clearTypeOptions();
            }

            for (CoreFormColumnOptions opt : list) {
                Column column = form.getColumn(opt.columnName);

                if (column != null) {
                    //column.getTypeOptions().put(opt.optionValue, new FormOptions.OptionValue(opt.optionLabel, false, ""));
                    column.addTypeOptions(opt.optionValue, opt.optionLabel, false, "");
                }
            }
        }
    }

    public static HForm getHFormBy(Context context, CoreFormEntity formEntity){
        switch (formEntity) {
            case REGION: return getRegionForm(context);
            case PRE_HOUSEHOLD:
            case HOUSEHOLD: return getHouseholdForm(context);
            case MEMBER_ENU: return getMemberEnuForm(context);
            case HEAD_RELATIONSHIP: break;
            case MARITAL_RELATIONSHIP: return getMaritalRelationshipForm(context);
            case INMIGRATION: return getInMigrationForm(context);
            case EXTERNAL_INMIGRATION: return getExternalInMigrationForm(context);
            case OUTMIGRATION: return getOutmigrationForm(context);
            case PREGNANCY_REGISTRATION: return getPregnancyRegistrationForm(context);
            case PREGNANCY_OUTCOME: return getPregnancyOutcomeForm(context);
            case DEATH: return getDeathForm(context);
            case CHANGE_HOUSEHOLD_HEAD: return getChangeHeadForm(context);
            case INCOMPLETE_VISIT: return getIncompleteVisitForm(context);
            case VISIT: return getVisitForm(context);
            case CHANGE_REGION_HEAD: return getChangeRegionHeadForm(context);
            case EXTRA_FORM: break;
            case EDITED_REGION: break;
            case EDITED_HOUSEHOLD: break;
            case EDITED_MEMBER: break;
            case INVALID_ENUM: break;
        }

        return null;
    }

    class LoadFormFragmentTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("xloading", "preloading");

            if (currentMode == Mode.CREATE) {
                preloadValues();
                form.setCustomTitle(formTitle);
            } else if (currentMode == Mode.EDIT) {
                preloadUpdatedValues();
                form.setCustomTitle(formTitle);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            Log.d("xloading", "creating form fragment");

            if (currentMode == Mode.CREATE) {
                FormFragment formFragment = FormFragment.newInstance(fragmentManager, form, Bootstrap.getInstancesPath(context), user.username, preloadedMap, postExecution, backgroundMode, resumeMode, FormUtil.this);
                Log.d("xloading", "starting form fragment");
                formFragment.startCollecting();

            } else if (currentMode == Mode.EDIT) {
                String savedXmlFilename = entity.getRecentlyCreatedUri()==null ? collectedData.formFilename : entity.getRecentlyCreatedUri();
                FormFragment formFragment = FormFragment.newInstance(fragmentManager, form, Bootstrap.getInstancesPath(context), user.username, savedXmlFilename, preloadedMap, postExecution, backgroundMode, true, FormUtil.this);
                Log.d("xloading", "starting form fragment");
                formFragment.startCollecting();
            }

        }
    }

}
