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
import org.philimone.hds.explorer.model.oldstate.SavedEntityState_;
import org.philimone.hds.explorer.server.settings.generator.CodeGeneratorService;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.LoadingDialog;
import org.philimone.hds.forms.listeners.FormCollectionListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnGroup;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.PreloadMap;
import org.philimone.hds.forms.model.RepeatColumnValue;
import org.philimone.hds.forms.model.RepeatObject;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;
import org.philimone.hds.forms.model.parsers.ExcelFormParser;
import org.philimone.hds.forms.model.parsers.form.model.FormOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.ReflectionUtils;
import mz.betainteractive.utilities.StringUtil;

public abstract class FormUtil<T extends CoreEntity> implements FormCollectionListener {

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

    protected Mode currentMode;
    
    protected FormUtilListener<T> listener;

    protected LoadingDialog loadingDialog;

    /* Load a Creator */

    /* Load a Editor */

    protected FormUtil(Fragment fragment, Context context, HForm hform, FormUtilListener<T> listener){
        this.fragment = fragment;
        this.fragmentManager = fragment.getActivity().getSupportFragmentManager();
        this.context = context;
        this.form = hform;
        this.user = Bootstrap.getCurrentUser();
        this.codeGenerator = new CodeGeneratorService();
        this.preloadedMap = new PreloadMap();

        this.currentMode = Mode.CREATE;

        this.listener = listener;

        this.loadingDialog = new LoadingDialog(context);
    }

    protected FormUtil(Fragment fragment, Context context, HForm hform, T existentEntity, FormUtilListener<T> listener){
        this.fragment = fragment;
        this.fragmentManager = fragment.getActivity().getSupportFragmentManager();
        this.context = context;
        this.form = hform;
        this.user = Bootstrap.getCurrentUser();
        this.codeGenerator = new CodeGeneratorService();
        this.preloadedMap = new PreloadMap();

        this.currentMode = Mode.EDIT;
        this.entity = existentEntity;

        this.listener = listener;

        initBoxes();
        readCollectedDataForEdit();

        this.loadingDialog = new LoadingDialog(context);
    }

    protected FormUtil(Fragment fragment, Context context, HForm hform, T existentEntity, CoreCollectedData coreCollectedData, FormUtilListener<T> listener){
        this.fragment = fragment;
        this.fragmentManager = fragment.getActivity().getSupportFragmentManager();
        this.context = context;
        this.form = hform;
        this.user = Bootstrap.getCurrentUser();
        this.codeGenerator = new CodeGeneratorService();
        this.preloadedMap = new PreloadMap();

        this.currentMode = Mode.EDIT;
        this.entity = existentEntity;

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

    protected FormUtil(AppCompatActivity activity, Context context, HForm hform, FormUtilListener<T> listener){
        this.activity = activity;
        this.fragmentManager = activity.getSupportFragmentManager();
        this.context = context;
        this.form = hform;
        this.user = Bootstrap.getCurrentUser();
        this.codeGenerator = new CodeGeneratorService();
        this.preloadedMap = new PreloadMap();

        this.currentMode = Mode.CREATE;

        this.listener = listener;

        this.loadingDialog = new LoadingDialog(context);
    }

    protected FormUtil(AppCompatActivity activity, Context context, HForm hform, T existentEntity, FormUtilListener<T> listener){
        this.activity = activity;
        this.fragmentManager = activity.getSupportFragmentManager();
        this.context = context;
        this.form = hform;
        this.user = Bootstrap.getCurrentUser();
        this.codeGenerator = new CodeGeneratorService();
        this.preloadedMap = new PreloadMap();

        this.currentMode = Mode.EDIT;
        this.entity = existentEntity;

        this.listener = listener;

        this.loadingDialog = new LoadingDialog(context);

        initBoxes();
        readCollectedDataForEdit();
    }

    private void readCollectedDataForEdit() {
        this.collectedData = this.boxCoreCollectedData.query(CoreCollectedData_.formEntityId.equal(this.entity.getId()).and(CoreCollectedData_.collectedId.equal(this.entity.getCollectedId()))).build().findFirst();
        //this.collectedData = this.boxCoreCollectedData.query(CoreCollectedData_.formEntityId.equal(this.entity.getId())).build().findFirst();
        Log.d("found-collected", "uploaded="+this.collectedData.uploaded+", id="+this.collectedData.id+", feid="+this.collectedData.formEntityId);

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
        new LoadFormFragmentTask().execute();
    }

    protected CoreFormExtension getFormExtension(CoreFormEntity formEntity){
        CoreFormEntity cformEntity = formEntity==CoreFormEntity.EXTERNAL_INMIGRATION ? CoreFormEntity.INMIGRATION : formEntity;
        return this.boxCoreFormExtension.query(CoreFormExtension_.formEntity.equal(cformEntity.code)).build().findFirst();
    }

    protected void afterFormCollected(CollectedDataMap collectedValues) {
        collectMediaFiles(collectedValues, false);
        this.boxCoreCollectedData.put(this.collectedData); //saves the media files

        if (currentMode == Mode.CREATE) {
            collectExtensionForm(collectedValues);
        } else {
            onFinishedExtensionCollection();
        }
    }

    protected void collectExtensionForm(CollectedDataMap collectedValues){
        //Log.d("collecting", "extension"+collectedValues.get(HForm.COLUMN_ID));
        if (this.collectedData != null) {
            CoreFormExtension formExtension = getFormExtension(collectedData.formEntity);

            if (formExtension != null && formExtension.enabled) {
                //is required or optional
                //read the mapping columns and preload them with collected values

                PreloadMap extPreloads = createExtensionPreloadMap(formExtension, collectedValues);
                String title = this.context.getString(R.string.form_util_extension_collect_lbl);

                if (formExtension.required) {

                    String message = this.context.getString(R.string.form_util_extension_collect_required_msg_lbl, getFormName());
                    DialogFactory.createMessageInfo(this.context, title, message, clickedButton -> {
                        //COLLECT EXTENSION FORM
                        new LoadExtFormFragmentTask(formExtension, extPreloads).execute();
                    }).show();
                } else {
                    //optional message
                    String message = this.context.getString(R.string.form_util_extension_collect_optional_lbl,getFormName());
                    DialogFactory.createMessageYN(this.context, title, message, new DialogFactory.OnYesNoClickListener() {
                        @Override
                        public void onYesClicked() {
                            //COLLECT EXTENSION FORM
                            new LoadExtFormFragmentTask(formExtension, extPreloads).execute();
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

    public void editExtensionForm(CoreCollectedData collectedData) {
        CoreFormExtension formExtension = getFormExtension(collectedData.formEntity);

        if (formExtension != null && !StringUtil.isBlank(collectedData.extensionCollectedFilepath)) {
            new LoadExtFormFragmentTask(formExtension, new PreloadMap(), collectedData.extensionCollectedFilepath).execute();
        }
    }

    protected InputStream getZipInputStream(String zipPath) throws Exception {
        ZipInputStream zin = new ZipInputStream(new FileInputStream(zipPath));
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null) {
            if (entry.getName().endsWith(".xlsx") || entry.getName().endsWith(".xls")) {
                return zin;
            }
        }
        return null;
    }

    private PreloadMap createExtensionPreloadMap(CoreFormExtension formExtension, CollectedDataMap collectedValues){
        PreloadMap preloads = new PreloadMap();
        final String householdPrefix = "Household.";

        for (String key : formExtension.columnsMapping.keySet()){
            String value = formExtension.columnsMapping.get(key);

            if (key.contains(".")){ //is repeat group variable

                if (value.startsWith("$")){ //load collected repeat values
                    try {
                        value = value.replace("$", "");
                        int iKeyPoint = key.indexOf(".");
                        int iValPoint = value.indexOf(".");
                        String extRepeatGroup = key.substring(0, iKeyPoint);
                        String extRepeatInnerColumn = key.substring(iKeyPoint + 1);
                        String hdsRepeatGroup = value.substring(0, iValPoint);
                        String hdsRepeatInnerColumn = value.substring(iValPoint + 1);

                        RepeatColumnValue hdsRepeatColumnValue = collectedValues.getRepeatColumn(hdsRepeatGroup);

                        if (hdsRepeatColumnValue != null) {
                            RepeatObject extRepeatObject = preloads.getRepeatObject(extRepeatGroup);
                            if (extRepeatObject == null) {
                                extRepeatObject = new RepeatObject();
                                preloads.put(extRepeatGroup, extRepeatObject);
                            }

                            for (int repeatIndex = 0; repeatIndex < hdsRepeatColumnValue.getCount(); repeatIndex++) {
                                Map<String, String> mapRepeatItem;
                                if (repeatIndex < extRepeatObject.count()) {
                                    mapRepeatItem = extRepeatObject.getList().get(repeatIndex);
                                } else {
                                    mapRepeatItem = extRepeatObject.createNewObject();
                                }

                                String hdsValue = hdsRepeatColumnValue.get(hdsRepeatInnerColumn, repeatIndex).getValue();
                                mapRepeatItem.put(extRepeatInnerColumn, hdsValue);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            } else { //root variable

                if (value.startsWith("#")){ //variable - from collected values
                    value = value.replace("#", "");
                    ColumnValue columnValue = collectedValues.get(value);
                    if (columnValue != null) {
                        preloads.put(key, columnValue.getValue());
                    }
                } else if (value.startsWith(householdPrefix)) {
                    String variableName = value.replaceFirst(householdPrefix, "");
                    if (!StringUtil.isBlank(variableName) && this.household != null) {
                        String variableValue = ReflectionUtils.getValueByName(this.household, variableName);
                        preloads.put(key, variableValue);
                    }
                } else { //constant
                    preloads.put(key, value);
                }
            }
        }

        return preloads;
    }

    /*
     * This method updates the mediaFiles list of the current CoreCollectedData
     */
    protected void collectMediaFiles(CollectedDataMap collectedValues, boolean isExtension) {
        if (this.collectedData.coreMediaFiles == null) {
            this.collectedData.coreMediaFiles = new ArrayList<>();
        }
        if (this.collectedData.extensionMediaFiles == null) {
            this.collectedData.extensionMediaFiles = new ArrayList<>();
        }

        List<String> targetList = isExtension ? this.collectedData.extensionMediaFiles : this.collectedData.coreMediaFiles;
        targetList.clear();

        // Scan all values in the map
        for (ColumnValue cv : collectedValues.values()) {
            scanMediaFiles(cv, targetList);
        }
    }

    private void scanMediaFiles(ColumnValue cv, List<String> targetList) {
        if (cv instanceof RepeatColumnValue) {
            RepeatColumnValue rcv = (RepeatColumnValue) cv;
            for (int i = 0; i < rcv.getCount(); i++) {
                for (ColumnValue rcvInner : rcv.getColumnValues(i)) {
                    scanMediaFiles(rcvInner, targetList);
                }
            }
        } else if (cv.getColumn() != null && cv.getColumn().isMediaColumn() && !StringUtil.isBlank(cv.getValue())) {
            String filename = cv.getValue();
            if (!targetList.contains(filename)) {
                targetList.add(filename);
            }
        }
    }

    protected abstract void onFinishedExtensionCollection();

    protected SubjectEntity getCurrentTablename() {

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
        if (this.entity instanceof PregnancyVisit) return SubjectEntity.MEMBER;
        if (this.entity instanceof Region) return SubjectEntity.REGION;
        if (this.entity instanceof Visit) return SubjectEntity.HOUSEHOLD;
        if (this.entity instanceof RegionHeadRelationship) return SubjectEntity.REGION;
        if (this.entity instanceof HouseholdRelocation) return SubjectEntity.HOUSEHOLD;
        if (this.entity instanceof HouseholdProxyHead) return SubjectEntity.HOUSEHOLD;

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
        } else if (this.entity instanceof PregnancyVisit) {
            code = ((PregnancyVisit) this.entity).motherCode;
            subject = SubjectEntity.MEMBER;
        } else if (this.entity instanceof Region) {
            return this.entity.getId();
        } else if (this.entity instanceof Visit) {
            code = ((Visit) this.entity).householdCode;
            subject = SubjectEntity.HOUSEHOLD;
        } else if (this.entity instanceof RegionHeadRelationship){
            code = ((RegionHeadRelationship) this.entity).regionCode;
            subject = SubjectEntity.REGION;
        } else  if (this.entity instanceof HouseholdRelocation) {
            code = ((HouseholdRelocation) this.entity).destinationCode;
            subject = SubjectEntity.HOUSEHOLD;
        } else  if (this.entity instanceof HouseholdProxyHead) {
            code = ((HouseholdProxyHead) this.entity).householdCode;
            subject = SubjectEntity.HOUSEHOLD;
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
        if (this.collectedData.formEntity == CoreFormEntity.PREGNANCY_VISIT) return this.context.getString(R.string.core_entity_pregnancy_vis_lbl);
        if (this.collectedData.formEntity == CoreFormEntity.VISIT) return this.context.getString(R.string.core_entity_visit_lbl);
        if (this.collectedData.formEntity == CoreFormEntity.CHANGE_REGION_HEAD) return this.context.getString(R.string.core_entity_changehor_lbl);
        if (this.collectedData.formEntity == CoreFormEntity.HOUSEHOLD_RELOCATION) return this.context.getString(R.string.core_entity_household_reloc_lbl);
        if (this.collectedData.formEntity == CoreFormEntity.CHANGE_PROXY_HEAD) return this.context.getString(R.string.core_entity_changephd_lbl);

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

    protected static HForm getPregnancyVisitForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.pregnancy_visit_form);
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

    protected static HForm getHouseholdRelocationForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.household_relocation_form);
        HForm form = retrieveForm(inputStream);
        return form;
    }

    protected static HForm getChangeProxyHeadForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.change_proxy_head_form);
        HForm form = retrieveForm(inputStream);
        return form;
    }

    protected static HForm retrieveForm(InputStream inputStream) {
        Log.d("RETRIEVING FORM", "FORM "+new Date());
        HForm form = new ExcelFormParser(inputStream).getForm();

        importOptions(form);

        Log.d("RETRIEVING FORM ENDS", "FORM "+new Date());
        Log.d("Loading Form - Ends", new Date()+"");
        return form;
    }

    private static void importOptions(HForm form) {
        Box<CoreFormColumnOptions> boxCoreFormOptions = ObjectBoxDatabase.get().boxFor(CoreFormColumnOptions.class);
        List<CoreFormColumnOptions> list = boxCoreFormOptions.query(CoreFormColumnOptions_.formName.equal(form.getFormId())).order(CoreFormColumnOptions_.id).build().find();

        if (!list.isEmpty()) {
            // Map all columns once for fast lookup
            Map<String, Column> allColumns = new HashMap<>();
            for (ColumnGroup group : form.getColumns()) {
                for (Column column : group.getColumns()) {
                    allColumns.put(column.getName(), column);
                }
            }

            //clear column options
            Set<String> columnNames = new HashSet<>();
            for (CoreFormColumnOptions opt : list) {
                columnNames.add(opt.columnName);
            }

            //save default column options and clear for updating with options from the server
            Map<Column, Map<String, FormOptions.OptionValue>> optionsBak = new HashMap<>();
            for (String columnName : columnNames) {
                Column column = allColumns.get(columnName);

                //backup options and clear options
                if (column != null && !column.getTypeOptions().isEmpty()) {
                    optionsBak.put(column, new HashMap<>(column.getTypeOptions()));
                    column.clearTypeOptions();
                }
            }

            for (CoreFormColumnOptions opt : list) {
                Column column = allColumns.get(opt.columnName);

                if (column != null) {
                    //column.getTypeOptions().put(opt.optionValue, new FormOptions.OptionValue(opt.optionLabel, false, ""));
                    String optionLabel = opt.optionLabel;
                    String optionReadonlyCondition = "";
                    String optionDisplayCondition = "";

                    //For options that are default from the app get the label from the XLS Hform
                    Map<String, FormOptions.OptionValue> options = optionsBak.get(column);
                    if (options != null && options.containsKey(opt.optionValue)) {
                        FormOptions.OptionValue optionValue = options.get(opt.optionValue);
                        optionLabel = optionValue.label;
                        optionReadonlyCondition = optionValue.readonlyCondition;
                        optionDisplayCondition = optionValue.displayCondition;
                    }

                    column.addTypeOptions(opt.optionValue, optionLabel, optionReadonlyCondition, optionDisplayCondition);
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
            case PREGNANCY_VISIT: return getPregnancyVisitForm(context);
            case DEATH: return getDeathForm(context);
            case CHANGE_HOUSEHOLD_HEAD: return getChangeHeadForm(context);
            case INCOMPLETE_VISIT: return getIncompleteVisitForm(context);
            case VISIT: return getVisitForm(context);
            case CHANGE_REGION_HEAD: return getChangeRegionHeadForm(context);
            case HOUSEHOLD_RELOCATION: return getHouseholdRelocationForm(context);
            case CHANGE_PROXY_HEAD: return getChangeProxyHeadForm(context);
            case EXTRA_FORM: break;
            case EDITED_REGION: break;
            case EDITED_HOUSEHOLD: break;
            case EDITED_MEMBER: break;
            case INVALID_ENUM: break;
        }

        return null;
    }

    public SavedEntityState getEntityState(CoreFormEntity formEntity, Long recordId, String entityObjectKey) {
        return this.boxSavedEntityStates.query(SavedEntityState_.formEntity.equal(formEntity.code).and(SavedEntityState_.collectedId.equal(recordId)).and(SavedEntityState_.objectKey.equal(entityObjectKey))).build().findFirst();
    }

    protected Map<String, String> getSavedStateMap(SavedEntityState savedState) {
        Map<String, String> mapSavedStates = new HashMap<>();

        if (savedState != null) {
            HashMap map = new Gson().fromJson(savedState.objectGsonValue, HashMap.class);
            for (Object key : map.keySet()) {
                mapSavedStates.put(key.toString(), map.get(key).toString());
            }
        }

        return mapSavedStates;
    }

    /*
     * Used to call a method or function outside of HDS-Explorer
     */
    @Override
    public abstract String onFormCallMethod(String methodExpression, String[] args);

    class LoadFormFragmentTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            Log.d("xloading", "creating form");
            showLoadingDialog(R.string.loading_dialog_form_load_lbl, true);
        }

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

            DateUtil.SupportedCalendar supportedCalendar = Bootstrap.getSupportedCalendar();

            Log.d("xloading", "creating form fragment");

            if (currentMode == Mode.CREATE) {
                FormFragment formFragment = FormFragment.newInstance(fragmentManager, form, supportedCalendar, Bootstrap.getInstancesPath(context), user.username, preloadedMap, postExecution, backgroundMode, resumeMode, FormUtil.this);
                Log.d("xloading", "starting form fragment");
                formFragment.startCollecting();

            } else if (currentMode == Mode.EDIT) {
                String savedXmlFilename = entity.getRecentlyCreatedUri()==null ? collectedData.formFilename : entity.getRecentlyCreatedUri();
                FormFragment formFragment = FormFragment.newInstance(fragmentManager, form, supportedCalendar, Bootstrap.getInstancesPath(context), user.username, savedXmlFilename, preloadedMap, postExecution, backgroundMode, true, FormUtil.this);
                Log.d("xloading", "starting form fragment");
                formFragment.startCollecting();
            }

        }
    }

    class LoadExtFormFragmentTask extends AsyncTask<Void, Void, HForm> {
        private CoreFormExtension extension;
        private PreloadMap preloads;
        private String savedXmlPath;

        public LoadExtFormFragmentTask(CoreFormExtension extension, PreloadMap preloads) {
            this.extension = extension;
            this.preloads = preloads;
        }

        public LoadExtFormFragmentTask(CoreFormExtension extension, PreloadMap preloads, String savedXmlPath) {
            this.extension = extension;
            this.preloads = preloads;
            this.savedXmlPath = savedXmlPath;
        }

        @Override
        protected void onPreExecute() {
            showLoadingDialog(R.string.loading_dialog_form_load_lbl, true);
        }

        @Override
        protected HForm doInBackground(Void... voids) {
            Log.d("Loading Form", new Date()+"");
            try {
                InputStream is = getZipInputStream(extension.extFormFilePath);
                if (is != null) {
                    return retrieveForm(is);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(HForm extForm) {
            if (extForm == null) {
                showLoadingDialog("Error loading extension form", false);
                onFinishedExtensionCollection();
                return;
            }

            DateUtil.SupportedCalendar supportedCalendar = Bootstrap.getSupportedCalendar();

            Log.d("CREATING", "FormFragment");
            FormFragment formFragment;
            if (savedXmlPath == null) {
                formFragment = FormFragment.newInstance(fragmentManager, extForm, supportedCalendar, Bootstrap.getInstancesPath(context), user.username, preloads, postExecution, backgroundMode, resumeMode, new ExtensionFormListener());
            } else {
                formFragment = FormFragment.newInstance(fragmentManager, extForm, supportedCalendar, Bootstrap.getInstancesPath(context), user.username, savedXmlPath, preloads, postExecution, backgroundMode, true, new ExtensionFormListener());
            }
            Log.d("CREATING ENDS", "FormFragment");

            showLoadingDialog("", false);
            formFragment.startCollecting();
            Log.d("STARTED COLLECTING", "DATA");
        }
    }

    class ExtensionFormListener implements FormCollectionListener {
        @Override
        public void onFormLoaded(Object[] data) {}

        @Override
        public void onFormCancelled() {
            onFinishedExtensionCollection();
        }

        @Override
        public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {
            return ValidationResult.noErrors();
        }

        @Override
        public void onBeforeFormFinished(HForm form, CollectedDataMap collectedValues) {}

        @Override
        public void onFormFinished(HForm form, CollectedDataMap collectedValues, XmlFormResult result) {
            collectMediaFiles(collectedValues, true);

            collectedData.extensionCollected = true;
            collectedData.extensionCollectedFilepath = result.getFilename();
            collectedData.extensionCollectedUri = result.getFilename() != null ? Uri.fromFile(new File(result.getFilename())).toString() : null;
            collectedData.extensionCollectedDate = new Date();
            boxCoreCollectedData.put(collectedData); //saves also the media files

            onFinishedExtensionCollection();
        }

        @Override
        public String onFormCallMethod(String methodExpression, String[] args) {
            return FormUtil.this.onFormCallMethod(methodExpression, args);
        }
    }

}
