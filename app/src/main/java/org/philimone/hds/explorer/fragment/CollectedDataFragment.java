package org.philimone.hds.explorer.fragment;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.CollectedDataAdapter;
import org.philimone.hds.explorer.adapter.model.CollectedDataItem;
import org.philimone.hds.explorer.adapter.model.FormGroupChildItem;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.data.FormFilter;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.Dataset;
import org.philimone.hds.explorer.model.Dataset_;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.FormGroupInstance;
import org.philimone.hds.explorer.model.FormGroupInstanceChild;
import org.philimone.hds.explorer.model.FormGroupInstanceChild_;
import org.philimone.hds.explorer.model.FormGroupInstance_;
import org.philimone.hds.explorer.model.FormGroupMapping;
import org.philimone.hds.explorer.model.FormSubject;
import org.philimone.hds.explorer.model.Form_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Module;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.enums.FormType;
import org.philimone.hds.explorer.utilities.FormGroupUtilities;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.FormGroupPanelDialog;
import org.philimone.hds.explorer.widget.FormSelectorDialog;
import org.philimone.hds.explorer.widget.LoadingDialog;
import org.philimone.hds.explorer.widget.RecyclerListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.listener.OdkFormResultListener;
import mz.betainteractive.odk.model.FilledForm;
import mz.betainteractive.odk.model.OdkFormLoadData;
import mz.betainteractive.utilities.StringUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CollectedDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CollectedDataFragment extends Fragment implements OdkFormResultListener {

    private FormGroupPanelDialog formGroupPanelDialog;

    private enum SubjectMode { REGION, HOUSEHOLD, MEMBER };

    private RecyclerListView lvCollectedForms;
    private LoadingDialog loadingDialog;

    private FormSubject subject;
    private User loggedUser;

    private List<FormDataLoader> formDataLoaders = new ArrayList<>();
    private FormDataLoader lastLoadedForm;
    private FormUtilities formUtilities;
    private FormGroupUtilities formGroupUtilities;

    private Box<CollectedData> boxCollectedData;
    private Box<Household> boxHouseholds;
    private Box<Region> boxRegions;
    private Box<Form> boxForms;
    private Box<FormGroupInstance> boxFormGroupInstances;
    private Box<FormGroupInstanceChild> boxFormGroupInstanceChilds;
    private Box<Module> boxModules;
    private Box<Dataset> boxDatasets;

    private SubjectMode subjectMode;

    private CollectedData autoHighlightCollectedData;

    private List<String> selectedModules = new ArrayList<>();

    public CollectedDataFragment() {
        // Required empty public constructor
        initBoxes();
    }

    public static CollectedDataFragment newInstance(FormSubject subject, User user, List<FormDataLoader> dataLoaders){
        CollectedDataFragment fragment = new CollectedDataFragment();
        fragment.subject = subject;
        fragment.loggedUser = user;

        if (dataLoaders == null) {
            fragment.initializeDataloaders();
        } else {
            fragment.formDataLoaders.addAll(dataLoaders);
        }

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        formUtilities = new FormUtilities(this, this);

        formGroupUtilities  = new FormGroupUtilities(getContext());

        removeUnusedFormGroupInstances();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.collected_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialize(view);
    }

    private void initBoxes() {
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        this.boxFormGroupInstances = ObjectBoxDatabase.get().boxFor(FormGroupInstance.class);
        this.boxFormGroupInstanceChilds = ObjectBoxDatabase.get().boxFor(FormGroupInstanceChild.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxModules = ObjectBoxDatabase.get().boxFor(Module.class);
        this.boxDatasets = ObjectBoxDatabase.get().boxFor(Dataset.class);
    }

    private void initializeDataloaders() {
        //get all visible forms for this subject
        Region region = null;
        Household household = null;
        Member member = null;
        User user = loggedUser;

        if (subject instanceof Region){
            region = (Region) subject;

            this.formDataLoaders = getFormLoaders(FormFilter.REGION);
            //loadFormValues(this.formDataLoaders, null, null, region);
        }

        if (subject instanceof Household){
            household = (Household) subject;

            region = this.boxRegions.query(Region_.code.equal(household.region)).build().findFirst();
            this.formDataLoaders = getFormLoaders(FormFilter.HOUSEHOLD);
            //loadFormValues(this.formDataLoaders, household, null, region);
        }

        if (subject instanceof Member){
            member = (Member) subject;
            household = this.boxHouseholds.query(Household_.code.equal(member.householdCode)).build().findFirst();;
            region = this.boxRegions.query(Region_.code.equal(household.region)).build().findFirst();

            this.formDataLoaders = getFormLoaders(FormFilter.HOUSEHOLD_HEAD, FormFilter.MEMBER);
            //loadFormValues(this.formDataLoaders, household, null, region);
        }

    }

    private void loadMappingDataValues(FormDataLoader formDataLoader, FormGroupInstance formGroupInstance) {
        //get all visible forms for this subject
        Region region = null;
        Household household = null;
        Member member = null;
        User user = loggedUser;

        if (subject instanceof Region){
            region = (Region) subject;
            loadFormValues(formDataLoader, null, null, region, formGroupInstance);
        }

        if (subject instanceof Household){
            household = (Household) subject;
            region = this.boxRegions.query(Region_.code.equal(household.region)).build().findFirst();

            loadFormValues(formDataLoader, household, null, region, formGroupInstance);
        }

        if (subject instanceof Member){
            member = (Member) subject;
            household = this.boxHouseholds.query(Household_.code.equal(member.householdCode)).build().findFirst();;
            region = this.boxRegions.query(Region_.code.equal(household.region)).build().findFirst();

            loadFormValues(formDataLoader, household, member, region, formGroupInstance);
        }

    }

    public List<FormDataLoader> getFormDataLoaders() {
        return formDataLoaders;
    }

    private List<FormDataLoader> getFormLoaders(FormFilter... filters) {
        return FormDataLoader.getFormLoadersList(boxForms, loggedUser, filters);
    }

    private void loadFormValues(FormDataLoader loader, Household household, Member member, Region region, FormGroupInstance formGroupInstance){
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
        if (formGroupInstance != null) {
            loader.loadFormGroupValues(formGroupInstance);
        }

        loader.loadTrackingListValues();

        loader.loadConstantValues();
        loader.loadSpecialConstantValues(household, member, loggedUser, region, null);

        //Load variables on datasets

        String[] mappedDatasets = loader.getPossibleDatasetNames().toArray(new String[0]);
        Log.d("dload-finished", "start - "+loader.getForm().formId+", mapped datasets = "+mappedDatasets.length);

        if (mappedDatasets.length > 0) {
            List<Dataset> datasets = this.boxDatasets.query(Dataset_.name.oneOf(mappedDatasets)).build().find();

            if (datasets.size()>0) {
                for (Dataset dataset : datasets){
                    Log.d("hasMappedVariables", ""+dataset.getName());
                    loader.loadDataSetValues(dataset, household, member, loggedUser, region);
                }
            }

        }

        Log.d("dload-finished", "true");

    }

    private void initialize(View view) {

        this.loadingDialog = new LoadingDialog(this.getContext());

        selectedModules.addAll(loggedUser.getSelectedModules());

        lvCollectedForms = view.findViewById(R.id.lvCollectedForms);

        lvCollectedForms.addOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long id) {
                onCollectedDataItemClicked(position);
            }

            @Override
            public void onItemLongClick(View view, int position, long id) {

            }
        });

        this.showCollectedData();
    }

    public void setAutoHighlightCollectedData(CollectedData autoHighlightCollectedData) {
        this.autoHighlightCollectedData = autoHighlightCollectedData;
    }

    /*
     * Show the data collected for the selected individual - but only shows data that belongs to Forms that the user can view (FormDataLoader)
     * With this if we selected a follow_up list household we will view only the forms of that individual
     */
    private void showCollectedData() {
        //this.showProgress(true);

        List<CollectedData> list = getAllCollectedData();
        List<Form> forms = this.boxForms.getAll();
        List<CollectedDataItem> cdl = new ArrayList<>();

        for (CollectedData cd : list){
            if (hasFormDataLoadersContains(cd.getFormId())){
                Form form = getFormById(forms, cd.getFormId());
                cdl.add(new CollectedDataItem(subject, form, cd));
            }
        }

        CollectedDataAdapter adapter = new CollectedDataAdapter(this.getContext(), cdl);
        this.lvCollectedForms.setAdapter(adapter);

        if (autoHighlightCollectedData != null) {
            setHighlight(autoHighlightCollectedData, adapter);
        }

        //Update FormGroupPanel if is visible
        Log.d("group panel "+(this.formGroupPanelDialog==null), "visitble="+ (this.formGroupPanelDialog != null && this.formGroupPanelDialog.isVisible()) );
        if (this.formGroupPanelDialog != null && this.formGroupPanelDialog.isVisible()) {
            this.formGroupPanelDialog.reloadPanelData();
        }
    }

    public void reloadCollectedData(){
        showCollectedData();
    }

    private void onCollectedDataItemClicked(int position) {
        CollectedDataAdapter adapter = (CollectedDataAdapter) this.lvCollectedForms.getAdapter();
        CollectedDataItem dataItem = adapter.getItem(position);

        CollectedData collectedData = dataItem.getCollectedData();
        FormDataLoader formDataLoader = getFormDataLoader(collectedData);

        if (collectedData.formGroupCollected) {
            //Handle Form Group edition correctly
            handleOnFormGroupCollectedData(collectedData);
        } else {
            reOpenOdkForm(formDataLoader, collectedData, false, null);
        }
    }

    private Form getFormById(List<Form> forms, String formId){
        for (Form f : forms){
            if (f.getFormId().equals(formId)) return f;
        }

        return null;
    }

    private boolean hasFormDataLoadersContains(String formId){
        for (FormDataLoader fdl : getFormDataLoaders()){
            if (fdl.getForm().getFormId().equals(formId)){
                return true;
            }
        }
        return false;
    }

    private FormDataLoader getFormDataLoader(CollectedData collectedData){

        for (FormDataLoader dl : getFormDataLoaders()){
            if (dl.getForm().getFormId().equals(collectedData.getFormId())){
                return dl;
            }
        }

        return null;
    }

    private List<CollectedData> getAllCollectedData() {
        List<CollectedData> list = this.boxCollectedData.query().equal(CollectedData_.recordId, subject.getId())
                                                                .and()
                                                                .equal(CollectedData_.recordEntity, subject.getTableName().code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                                .filter((c) -> StringUtil.containsAny(c.formModules, selectedModules))  //filter by module
                                                                .build().find();
        return list;
    }

    private CollectedData getCollectedData(FormDataLoader formDataLoader){

        List<CollectedData> collectedData = this.boxCollectedData.query().equal(CollectedData_.formId, formDataLoader.getForm().getFormId(), QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                                   .and().equal(CollectedData_.recordId, subject.getId())
                                                                   .and().equal(CollectedData_.recordEntity, subject.getTableName().code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                                   .filter((c) -> StringUtil.containsAny(c.formModules, selectedModules)) //filter by module
                                                                   .build().find();

        if (collectedData != null && collectedData.size()>0){
            return collectedData.get(0);
        }

        return null;
    }

    public void onCollectData(){

        List<FormDataLoader> loaderList = getFormDataLoaders();

        if (loaderList != null && loaderList.size() > 0){

            if (loaderList.size()==1){
                //open directly the form
                FormDataLoader formDataLoader = loaderList.get(0);
                Form form = formDataLoader.getForm();

                if (form.formType == FormType.FORM_GROUP) {
                    handleOnCollectFormGroupSelected(formDataLoader);
                } else {
                    openOdkForm(formDataLoader, false, null);
                }
            }else {
                //load list dialog and choice the form
                buildFormSelectorDialog(loaderList);
            }
        }
    }

    private void buildFormSelectorDialog(List<FormDataLoader> loaders) {

        //remove all formGroupExclusive Forms
        loaders = filterFormGroupExclusive(loaders);

        FormSelectorDialog.createDialog(this.getParentFragmentManager(), loaders, new FormSelectorDialog.OnFormSelectedListener() {
            @Override
            public void onFormSelected(FormDataLoader formDataLoader) {
                openOdkForm(formDataLoader, false, null);
            }

            @Override
            public void onFormGroupSelected(FormDataLoader formDataLoader) {
                handleOnCollectFormGroupSelected(formDataLoader);
            }

            @Override
            public void onCancelClicked() {

            }
        }).show();
    }

    private List<FormDataLoader> filterFormGroupExclusive(List<FormDataLoader> loaders) {
        List<FormDataLoader> exclusiveList = new ArrayList<>();

        for (FormDataLoader fdl : loaders) {
            if (fdl.getForm().isFormGroupExclusive) {
                exclusiveList.add(fdl);
            }
        }

        loaders.removeAll(exclusiveList);

        return loaders;
    }

    private List<FormDataLoader> getFormGroupDataLoaders(Form formGroup) {
        List<FormDataLoader> dataLoaders = new ArrayList<>();

        for (FormGroupMapping mapping : formGroup.groupMappings) {
            Form form = boxForms.query(Form_.formId.equal(mapping.formId)).build().findFirst();
            dataLoaders.add(new FormDataLoader(form));
        }

        return dataLoaders;
    }

    private void handleOnFormGroupCollectedData(CollectedData collectedData) {

        Form formGroup = boxForms.query(Form_.formId.equal(collectedData.formGroupId)).build().findFirst();
        List<FormDataLoader> dataLoaders = getFormGroupDataLoaders(formGroup);
        FormGroupInstance groupInstance = boxFormGroupInstances.query(FormGroupInstance_.instanceUuid.equal(collectedData.formGroupInstanceUuid)).build().findFirst();

        this.formGroupPanelDialog = FormGroupPanelDialog.createDialog(this.getContext(), this.getParentFragmentManager(), this.formGroupUtilities, this.subject, formGroup, dataLoaders, groupInstance, new FormGroupPanelDialog.OnFormSelectedListener() {
            @Override
            public void onFormSelected(FormGroupChildItem childItem) {
                if (childItem.getCollectedData() != null) {
                    reOpenOdkForm(childItem.getFormDataLoader(), collectedData, true, groupInstance);
                } else {
                    openOdkForm(childItem.getFormDataLoader(), true, groupInstance);
                }
            }

            @Override
            public void onCancelClicked() {
                removeUnusedFormGroupInstances();
                formGroupPanelDialog = null;
            }
        });

        this.formGroupPanelDialog.show();
    }

    private void handleOnCollectFormGroupSelected(FormDataLoader formDataLoader) {

        Form formGroup = formDataLoader.getForm();
        List<FormDataLoader> dataLoaders = getFormGroupDataLoaders(formGroup);
        FormGroupInstance groupInstance = null;

        //find existent or create new
        if (formGroup.multiCollPerSession == false) {
            groupInstance = this.formGroupUtilities.getLastFormGroupInstanceCreated(formGroup, this.subject);
        }

        if (groupInstance == null) {
            groupInstance = this.formGroupUtilities.createNewInstance(formGroup, this.subject);
            boxFormGroupInstances.put(groupInstance);
        }

        FormGroupInstance finalGroupInstance = groupInstance;
        this.formGroupPanelDialog = FormGroupPanelDialog.createDialog(this.getContext(), this.getParentFragmentManager(), this.formGroupUtilities, this.subject, formGroup, dataLoaders, groupInstance, new FormGroupPanelDialog.OnFormSelectedListener() {
            @Override
            public void onFormSelected(FormGroupChildItem childItem) {
                if (childItem.getCollectedData() != null){
                    reOpenOdkForm(childItem.getFormDataLoader(), childItem.getCollectedData(), true, finalGroupInstance);
                } else {
                    openOdkForm(childItem.getFormDataLoader(), true, finalGroupInstance);
                }
            }

            @Override
            public void onCancelClicked() {
                removeUnusedFormGroupInstances();
                formGroupPanelDialog = null;
            }
        });

        this.formGroupPanelDialog.show();

    }

    private void removeUnusedFormGroupInstances() {

        List<FormGroupInstance> listDelete = new ArrayList<>();

        for (FormGroupInstance formGroupInstance : this.boxFormGroupInstances.getAll()) {
            if (formGroupInstance.instanceChilds.size()==0) {
                Log.d("childs", "empty");

                listDelete.add(formGroupInstance);
            }
        }

        boxFormGroupInstances.remove(listDelete);

        Log.d("removed-fgi", listDelete.size()+"");
    }

    public void setHighlight(CollectedData collectedData) {
        CollectedDataAdapter adapter = (CollectedDataAdapter) this.lvCollectedForms.getAdapter();

        int position = adapter.getPositionOf(collectedData);
        if (position >= 0) {
            adapter.setHighlightedIndex(position);
            this.lvCollectedForms.scrollToPosition(position);
        }
    }

    public void setHighlight(CollectedData collectedData, CollectedDataAdapter adapter) {
        int position = adapter.getPositionOf(collectedData);
        if (position >= 0) {
            adapter.setHighlightedIndex(position);
            this.lvCollectedForms.scrollToPosition(position);
        }
    }

    //<editor-fold desc="ODK Form Utility methods">
    private void openOdkForm(FormDataLoader formDataLoader, boolean isFormGroup, FormGroupInstance formGroupInstance) {
        OpenODKFormTask task = new OpenODKFormTask(formDataLoader, isFormGroup, formGroupInstance);
        task.execute();
        showLoadingDialog(getString(R.string.loading_dialog_odk_load_lbl), true);
    }

    private void reOpenOdkForm(FormDataLoader formDataLoader, CollectedData collectedData, boolean isFormGroup, FormGroupInstance formGroupInstance) {
        ReOpenODKFormTask task = new ReOpenODKFormTask(formDataLoader, collectedData, isFormGroup, formGroupInstance);
        task.execute();
        showLoadingDialog(getString(R.string.loading_dialog_odk_reload_lbl), true);
    }

    @Override
    public void onFormFinalized(OdkFormLoadData formLoadData, Uri contentUri, String formId, String instanceFileUri, String metaInstanceName, Date lastUpdatedDate) {
        Log.d("form finalized"," "+contentUri+", file-uri = "+instanceFileUri);

        //search existing record
        CollectedData collectedData = this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString(), QueryBuilder.StringOrder.CASE_SENSITIVE)
                .and().equal(CollectedData_.recordId, subject.getId())
                .and().equal(CollectedData_.recordEntity, subject.getTableName().code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (collectedData == null){ //insert
            collectedData = new CollectedData();
            collectedData.setFormId(formId);
            collectedData.setFormUri(contentUri.toString());
            collectedData.setFormXmlPath(instanceFileUri);
            collectedData.setFormInstanceName(metaInstanceName);
            collectedData.setFormLastUpdatedDate(lastUpdatedDate);

            collectedData.setFormModules(lastLoadedForm.getForm().modules);

            collectedData.setCollectedBy(loggedUser.getUsername());
            collectedData.setUpdatedBy("");
            collectedData.setSupervisedBy("");

            collectedData.setRecordId(subject.getId());
            collectedData.setRecordEntity(subject.getTableName());

            collectedData.formGroupCollected = formLoadData.isFormGroupLoad;
            collectedData.formGroupId = formLoadData.formGroupId;
            collectedData.formGroupName = formLoadData.formGroupName;
            collectedData.formGroupInstanceUuid = formLoadData.formGroupInstanceUuid;

            this.boxCollectedData.put(collectedData);
            Log.d("inserting", "new collected data");
        }else{ //update
            collectedData.setFormId(formId);
            collectedData.setFormUri(contentUri.toString());
            //collectedData.setFormXmlPath(instanceFileUri);
            collectedData.setFormInstanceName(metaInstanceName);
            collectedData.setFormLastUpdatedDate(lastUpdatedDate);

            //collectedData.setFormModule(lastLoadedForm.getForm().getModules());
            //collectedData.setCollectedBy(loggedUser.getUsername());
            collectedData.setUpdatedBy(loggedUser.getUsername());
            //collectedData.setSupervisedBy("");

            collectedData.setRecordId(subject.getId());
            collectedData.setRecordEntity(subject.getTableName());

            collectedData.formGroupCollected = formLoadData.isFormGroupLoad;
            collectedData.formGroupId = formLoadData.formGroupId;
            collectedData.formGroupName = formLoadData.formGroupName;
            collectedData.formGroupInstanceUuid = formLoadData.formGroupInstanceUuid;

            this.boxCollectedData.put(collectedData);
            Log.d("updating", "new collected data");
        }

        //save formGroupInstance child
        if (formLoadData.isFormGroupLoad && formLoadData.formGroupInstanceUuid != null) {
            FormGroupInstance formGroupInstance = boxFormGroupInstances.query(FormGroupInstance_.instanceUuid.equal(formLoadData.formGroupInstanceUuid)).build().findFirst();
            FormGroupInstanceChild instanceChild = formGroupUtilities.findInstanceChildBy(formGroupInstance, collectedData.formXmlPath);

            if (instanceChild == null) {
                instanceChild = formGroupUtilities.createNewInstanceChild(formGroupInstance, collectedData);
                formGroupInstance.instanceChilds.add(instanceChild);
            }

            this.boxFormGroupInstances.put(formGroupInstance);
        }

        showCollectedData();

    }

    @Override
    public void onFormUnFinalized(OdkFormLoadData formLoadData, Uri contentUri, String formId, String instanceFileUri, String metaInstanceName, Date lastUpdatedDate) {
        Log.d("form unfinalized"," "+contentUri);

        //search existing record
        CollectedData collectedData = this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString(), QueryBuilder.StringOrder.CASE_SENSITIVE)
                .and().equal(CollectedData_.recordId, subject.getId())
                .and().equal(CollectedData_.recordEntity, subject.getTableName().code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (collectedData == null){ //insert
            collectedData = new CollectedData();
            collectedData.setFormId(formId);
            collectedData.setFormUri(contentUri.toString());
            collectedData.setFormXmlPath(instanceFileUri);
            collectedData.setFormInstanceName(metaInstanceName);
            collectedData.setFormLastUpdatedDate(lastUpdatedDate);

            collectedData.setFormModules(lastLoadedForm.getForm().modules);

            collectedData.setCollectedBy(loggedUser.getUsername());
            collectedData.setUpdatedBy("");
            collectedData.setSupervisedBy("");

            collectedData.setRecordId(subject.getId());
            collectedData.setRecordEntity(subject.getTableName());

            collectedData.formGroupCollected = formLoadData.isFormGroupLoad;
            collectedData.formGroupId = formLoadData.formGroupId;
            collectedData.formGroupName = formLoadData.formGroupName;
            collectedData.formGroupInstanceUuid = formLoadData.formGroupInstanceUuid;

            this.boxCollectedData.put(collectedData);
            Log.d("inserting", "new collected data");
        }else{ //update
            collectedData.setFormId(formId);
            collectedData.setFormUri(contentUri.toString());
            //collectedData.setFormXmlPath(instanceFileUri);
            collectedData.setFormInstanceName(metaInstanceName);
            collectedData.setFormLastUpdatedDate(lastUpdatedDate);

            //collectedData.setFormModule(lastLoadedForm.getForm().getModules());
            //collectedData.setCollectedBy(loggedUser.getUsername());
            collectedData.setUpdatedBy(loggedUser.getUsername());
            //collectedData.setSupervisedBy("");

            collectedData.setRecordId(subject.getId());
            collectedData.setRecordEntity(subject.getTableName());

            collectedData.formGroupCollected = formLoadData.isFormGroupLoad;
            collectedData.formGroupId = formLoadData.formGroupId;
            collectedData.formGroupName = formLoadData.formGroupName;
            collectedData.formGroupInstanceUuid = formLoadData.formGroupInstanceUuid;

            this.boxCollectedData.put(collectedData);
            Log.d("updating", "new collected data");
        }

        //save formGroupInstance child
        if (formLoadData.isFormGroupLoad && formLoadData.formGroupInstanceUuid != null) {
            FormGroupInstance formGroupInstance = boxFormGroupInstances.query(FormGroupInstance_.instanceUuid.equal(formLoadData.formGroupInstanceUuid)).build().findFirst();
            FormGroupInstanceChild instanceChild = formGroupUtilities.findInstanceChildBy(formGroupInstance, collectedData.formXmlPath);

            if (instanceChild == null) {
                instanceChild = formGroupUtilities.createNewInstanceChild(formGroupInstance, collectedData);
                formGroupInstance.instanceChilds.add(instanceChild);
            }

            this.boxFormGroupInstances.put(formGroupInstance);
        }

        showCollectedData();
    }

    @Override
    public void onDeleteForm(OdkFormLoadData formLoadData, Uri contentUri, String instanceFileUri) {

        this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString(), QueryBuilder.StringOrder.CASE_SENSITIVE).build().remove(); //delete where formUri=contentUri

        //delete the instanceFileUri - already deleted by removing instance
        if (instanceFileUri != null) {
            //formUtilities.deleteInstanceFile(instanceFileUri);
        }

        //delete also the formGroupInstanceChild
        this.boxFormGroupInstanceChilds.query(FormGroupInstanceChild_.formInstanceUri.equal(instanceFileUri)).build().remove();

        showCollectedData();
    }

    @Override
    public void onFormInstanceNotFound(OdkFormLoadData formLoadData, final Uri contenUri) {
        buildDeleteFormInstanceNotFoundDialog(formLoadData, contenUri);
    }

    private void buildDeleteFormInstanceNotFoundDialog(OdkFormLoadData formLoadData, final Uri contenUri){

        DialogFactory.createMessageYN(this.getContext(), R.string.household_details_dialog_del_saved_form_title_lbl, R.string.household_details_dialog_del_saved_form_msg_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                onDeleteForm(formLoadData, contenUri, null);
            }

            @Override
            public void onNoClicked() {

            }
        }).show();
    }
    //</editor-fold>

    private void showLoadingDialog(String msg, boolean show){
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.dismiss();
        }
    }

    class OpenODKFormTask extends AsyncTask<Void, Void, XResult> {
        private FormDataLoader formDataLoader;
        private boolean isFormGroup;
        private FormGroupInstance formGroupInstance;

        public OpenODKFormTask(FormDataLoader formDataLoader, boolean isFormGroup, FormGroupInstance formGroupInstance) {
            this.formDataLoader = formDataLoader;
            this.isFormGroup = isFormGroup;
            this.formGroupInstance = formGroupInstance;
        }

        @Override
        protected XResult doInBackground(Void... voids) {

            Form form = formDataLoader.getForm();

            //get the collected data only if is one form per session
            CollectedData collectedData = form.multiCollPerSession ? null : getCollectedData(formDataLoader);

            loadMappingDataValues(formDataLoader, formGroupInstance);
            //reload timestamp constants
            formDataLoader.reloadTimestampConstants();

            FilledForm filledForm = new FilledForm(form.getFormId());
            filledForm.putAll(formDataLoader.getValues());
            filledForm.updateUnknownMember(getContext());
            //filledForm.setHouseholdMembers(getMemberOnListAdapter());

            return new XResult(form, new OdkFormLoadData(form, filledForm, isFormGroup, formGroupInstance), collectedData);
        }

        @Override
        protected void onPostExecute(XResult result) {

            showLoadingDialog(null, false);

            lastLoadedForm = formDataLoader;

            if (result.collectedData == null || result.form.isMultiCollPerSession()){
                formUtilities.loadForm(result.odkFormLoadData);
            }else{
                formUtilities.loadForm(result.odkFormLoadData, result.collectedData.getFormUri(), result.collectedData.formXmlPath, CollectedDataFragment.this);
            }
        }


    }

    class ReOpenODKFormTask extends AsyncTask<Void, Void, XResult> {
        private FormDataLoader formDataLoader;
        private CollectedData collectedData;
        private boolean isFormGroup;
        private FormGroupInstance formGroupInstance;

        public ReOpenODKFormTask(FormDataLoader formDataLoader, CollectedData collectedData, boolean loadingFormGroup, FormGroupInstance formGroupInstance) {
            this.formDataLoader = formDataLoader;
            this.collectedData = collectedData;
            this.isFormGroup = loadingFormGroup;
            this.formGroupInstance = formGroupInstance;
        }

        @Override
        protected XResult doInBackground(Void... voids) {
            //reload timestamp constants
            this.formDataLoader.reloadTimestampConstants();

            Form form = this.formDataLoader.getForm();
            FilledForm filledForm = new FilledForm(form.getFormId());
            filledForm.putAll(this.formDataLoader.getValues());
            filledForm.updateUnknownMember(getContext());

            return new XResult(form, new OdkFormLoadData(form, filledForm, isFormGroup, formGroupInstance), this.collectedData);
        }

        @Override
        protected void onPostExecute(XResult result) {

            showLoadingDialog(null, false);

            lastLoadedForm = formDataLoader;

            //we are reopening a saved form
            formUtilities.loadForm(result.odkFormLoadData, result.collectedData.getFormUri(), result.collectedData.formXmlPath, CollectedDataFragment.this);
        }
    }

    class XResult {
        Form form;
        CollectedData collectedData;
        OdkFormLoadData odkFormLoadData;
        FilledForm filledForm;

        public XResult(Form form, OdkFormLoadData loadData, CollectedData collectedData) {
            this.form = form;
            this.collectedData = collectedData;
            this.odkFormLoadData = loadData;
            //this.filledForm = filledForm;
        }
    }
}
