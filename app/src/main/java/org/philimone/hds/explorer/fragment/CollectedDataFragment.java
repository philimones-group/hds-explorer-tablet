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
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.main.hdsforms.EditCoreExtensionFormUtil;
import org.philimone.hds.explorer.main.hdsforms.FormUtil;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.CoreEntity;
import org.philimone.hds.explorer.model.CoreFormExtension;
import org.philimone.hds.explorer.model.Dataset;
import org.philimone.hds.explorer.model.Dataset_;
import org.philimone.hds.explorer.model.Death;
import org.philimone.hds.explorer.model.Death_;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.FormGroupInstance;
import org.philimone.hds.explorer.model.FormGroupInstanceChild;
import org.philimone.hds.explorer.model.FormGroupInstanceChild_;
import org.philimone.hds.explorer.model.FormGroupInstance_;
import org.philimone.hds.explorer.model.FormGroupMapping;
import org.philimone.hds.explorer.model.FormSubject;
import org.philimone.hds.explorer.model.Form_;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.HeadRelationship_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.IncompleteVisit;
import org.philimone.hds.explorer.model.IncompleteVisit_;
import org.philimone.hds.explorer.model.Inmigration;
import org.philimone.hds.explorer.model.Inmigration_;
import org.philimone.hds.explorer.model.MaritalRelationship;
import org.philimone.hds.explorer.model.MaritalRelationship_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Module;
import org.philimone.hds.explorer.model.Outmigration;
import org.philimone.hds.explorer.model.Outmigration_;
import org.philimone.hds.explorer.model.PregnancyOutcome;
import org.philimone.hds.explorer.model.PregnancyOutcome_;
import org.philimone.hds.explorer.model.PregnancyRegistration;
import org.philimone.hds.explorer.model.PregnancyRegistration_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.Visit_;
import org.philimone.hds.explorer.model.followup.TrackingSubjectList;
import org.philimone.hds.explorer.utilities.FormGroupUtilities;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.FormGroupPanelDialog;
import org.philimone.hds.explorer.widget.FormSelectorDialog;
import org.philimone.hds.explorer.widget.LoadingDialog;
import org.philimone.hds.explorer.widget.RecyclerListView;
import org.philimone.hds.forms.model.HForm;

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
import mz.betainteractive.odk.task.OdkFormLoadResult;
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

    private Visit visit;

    private List<FormDataLoader> formDataLoaders = new ArrayList<>();
    private FormDataLoader lastLoadedForm;
    private FormUtilities formUtilities;
    private FormGroupUtilities formGroupUtilities;

    private Box<CollectedData> boxCollectedData;
    private Box<CoreCollectedData> boxCoreCollectedData;
    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<Visit> boxVisits;
    private Box<Region> boxRegions;
    private Box<MaritalRelationship> boxMaritalRelationships;
    private Box<Inmigration> boxInmigrations;
    private Box<Outmigration> boxOutmigrations;
    private Box<PregnancyRegistration> boxPregnancyRegistrations;
    private Box<PregnancyOutcome> boxPregnancyOutcomes;
    private Box<Death> boxDeaths;
    private Box<HeadRelationship> boxHeadRelationships;
    private Box<IncompleteVisit> boxIncompleteVisits;
    private Box<Form> boxForms;
    private Box<CoreFormExtension> boxCoreFormExtensions;
    private Box<FormGroupInstance> boxFormGroupInstances;
    private Box<FormGroupInstanceChild> boxFormGroupInstanceChilds;
    private Box<Module> boxModules;
    private Box<Dataset> boxDatasets;

    private TrackingSubjectList trackingSubject;
    private SubjectMode subjectMode;

    private CollectedData collectedDataToEdit;
    private boolean externalCallOnCollectData;
    private boolean externalCallCollectedDataToEdit;

    private List<String> selectedModules = new ArrayList<>();

    private CollectedDataFragmentListener collectedDataFragmentListener;

    public CollectedDataFragment() {
        // Required empty public constructor
        initBoxes();
        this.loggedUser = Bootstrap.getCurrentUser();
    }

    public static CollectedDataFragment newInstance(FormSubject subject, User user, TrackingSubjectList trackingSubject){
        CollectedDataFragment fragment = new CollectedDataFragment();
        fragment.trackingSubject = trackingSubject;
        fragment.subject = subject;
        //fragment.loggedUser = user;

        fragment.initializeDataloaders();

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
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        this.boxCoreFormExtensions = ObjectBoxDatabase.get().boxFor(CoreFormExtension.class);
        this.boxFormGroupInstances = ObjectBoxDatabase.get().boxFor(FormGroupInstance.class);
        this.boxFormGroupInstanceChilds = ObjectBoxDatabase.get().boxFor(FormGroupInstanceChild.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxModules = ObjectBoxDatabase.get().boxFor(Module.class);
        this.boxDatasets = ObjectBoxDatabase.get().boxFor(Dataset.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxVisits = ObjectBoxDatabase.get().boxFor(Visit.class);
        this.boxMaritalRelationships = ObjectBoxDatabase.get().boxFor(MaritalRelationship.class);
        this.boxInmigrations = ObjectBoxDatabase.get().boxFor(Inmigration.class);
        this.boxInmigrations = ObjectBoxDatabase.get().boxFor(Inmigration.class);
        this.boxOutmigrations = ObjectBoxDatabase.get().boxFor(Outmigration.class);
        this.boxPregnancyRegistrations = ObjectBoxDatabase.get().boxFor(PregnancyRegistration.class);
        this.boxPregnancyOutcomes = ObjectBoxDatabase.get().boxFor(PregnancyOutcome.class);
        this.boxDeaths = ObjectBoxDatabase.get().boxFor(Death.class);
        this.boxHeadRelationships = ObjectBoxDatabase.get().boxFor(HeadRelationship.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxIncompleteVisits = ObjectBoxDatabase.get().boxFor(IncompleteVisit.class);
    }

    public void setCollectedDataFragmentListener(CollectedDataFragmentListener collectedDataFragmentListener) {
        this.collectedDataFragmentListener = collectedDataFragmentListener;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    private void initializeDataloaders() {
        //get all visible forms for this subject
        Region region = null;
        Household household = null;
        Member member = null;
        User user = loggedUser;

        if (this.trackingSubject != null) {

            String[] formIds = this.trackingSubject.getSubjectForms().split(",");

            if (formIds.length > 0){
                List<Form> forms = this.boxForms.query(Form_.formId.oneOf(formIds)).build().find();

                for (Form form : forms) {
                    FormDataLoader loader = new FormDataLoader(form);
                    this.formDataLoaders.add(loader);
                }
            }

            return;
        }

        if (subject instanceof Region){
            region = (Region) subject;

            this.formDataLoaders = getFormLoaders(FormFilter.REGION);
            //loadFormValues(this.formDataLoaders, null, null, region);
        }

        if (subject instanceof Household){
            household = (Household) subject;

            if (household != null) {
                region = this.boxRegions.query(Region_.code.equal(household.region)).build().findFirst();
            }
            this.formDataLoaders = getFormLoaders(FormFilter.HOUSEHOLD);
            //loadFormValues(this.formDataLoaders, household, null, region);
        }

        if (subject instanceof Member){
            member = (Member) subject;
            household = this.boxHouseholds.query(Household_.code.equal(member.householdCode)).build().findFirst();

            if (household != null) {
                region = this.boxRegions.query(Region_.code.equal(household.region)).build().findFirst();
            }
            this.formDataLoaders = getFormLoaders(FormFilter.HOUSEHOLD_HEAD, FormFilter.MEMBER);
            //loadFormValues(this.formDataLoaders, household, null, region);
            filterForms(member);
        }

    }

    private void filterForms(Member member){
        List<FormDataLoader> toRemove = new ArrayList<>();
        for (FormDataLoader loader : this.formDataLoaders){
            Form form = loader.getForm();
            Log.d("form", "fminage="+form.minAge+", fmaxge="+form.maxAge+", fg="+form.gender);
            //age
            if (!(member.age >= form.minAge && member.age <= form.maxAge)) {
                toRemove.add(loader);
                continue;
            }
            if (!form.gender.equals("ALL") && !form.gender.equals(member.gender.code)) {
                toRemove.add(loader);
                continue;
            }
            if (form.isHouseholdHeadForm && !member.isHouseholdHead()) {
                toRemove.add(loader);
            }
        }

        Log.d("removed forms", formDataLoaders.size()+"/"+toRemove.size()+", g="+member.gender+", age="+ member.age);

        this.formDataLoaders.removeAll(toRemove);
    }

    private void loadMappingDataValues(FormDataLoader formDataLoader, Visit visit, TrackingSubjectList trackingSubject, FormGroupInstance formGroupInstance) {
        //get all visible forms for this subject
        Region region = null;
        Household household = null;
        Member member = null;
        User user = loggedUser;

        if (subject instanceof Region){
            region = (Region) subject;
            loadFormValues(formDataLoader, null, null, region, null, trackingSubject, formGroupInstance);
        }

        if (subject instanceof Household){
            household = (Household) subject;
            region = this.boxRegions.query(Region_.code.equal(household.region)).build().findFirst();

            loadFormValues(formDataLoader, household, null, region, visit, trackingSubject, formGroupInstance);
        }

        if (subject instanceof Member){
            member = (Member) subject;
            household = this.boxHouseholds.query(Household_.code.equal(member.householdCode)).build().findFirst();;
            region = this.boxRegions.query(Region_.code.equal(household.region)).build().findFirst();

            loadFormValues(formDataLoader, household, member, region, visit, trackingSubject, formGroupInstance);
        }

    }

    public List<FormDataLoader> getFormDataLoaders() {
        return formDataLoaders;
    }

    private List<FormDataLoader> getFormLoaders(FormFilter... filters) {
        return FormDataLoader.getFormLoadersList(boxForms, loggedUser, filters);
    }

    private void loadFormValues(FormDataLoader loader, Household household, Member member, Region region, Visit visit, TrackingSubjectList trackingSubjectItem, FormGroupInstance formGroupInstance){
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
        if (visit != null) {
            loader.loadVisitValues(visit);
        }
        if (formGroupInstance != null) {
            loader.loadFormGroupValues(formGroupInstance);
        }

        if (trackingSubjectItem != null) {
            loader.loadTrackingListValues(trackingSubjectItem);
        }

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

        Log.d("externalOnCollect", externalCallOnCollectData+"");
        Log.d("collectedDataEdit", collectedDataToEdit+"");
        if (externalCallOnCollectData) {
            this.onCollectData();
        } else if (collectedDataToEdit != null) {
            //reopen / edit
            CollectedDataAdapter adapter = (CollectedDataAdapter) this.lvCollectedForms.getAdapter();
            int position = adapter.getPositionOf(collectedDataToEdit);
            if (position >= 0) {
                onCollectedDataItemClicked(position);
            }
        }
    }

    public void setInternalCollectedDataToEdit(CollectedData collectedData) {
        this.collectedDataToEdit = collectedData;
    }
    public void setExternalCollectedDataToEdit(CollectedData collectedData) {
        this.collectedDataToEdit = collectedData;
        this.externalCallCollectedDataToEdit = collectedData != null;
    }

    public void setExternalCallOnCollectData(boolean externalCallOnCollectData) {
        this.externalCallOnCollectData = externalCallOnCollectData;
    }

    /*
     * Show the data collected for the selected individual - but only shows data that belongs to Forms that the user can view (FormDataLoader)
     * With this if we selected a follow_up list household we will view only the forms of that individual
     */
    private void showCollectedData() {
        //this.showProgress(true);

        List<CollectedData> list = getAllCollectedData();
        List<Form> forms = this.boxForms.getAll();
        List<CoreFormExtension> coreforms = this.boxCoreFormExtensions.getAll();
        List<CollectedDataItem> cdl = new ArrayList<>();

        for (CollectedData cd : list){
            //Log.d("form", cd+", formid="+cd.getFormId()+", hassdl="+hasFormDataLoadersContains(cd.getFormId()));
            if (hasFormDataLoadersContains(cd.getFormId()) || getFormExtensionById(coreforms, cd.getFormId()) != null){
                Form form = getFormById(forms, cd.getFormId());
                CoreFormExtension formExtension = form == null ? getFormExtensionById(coreforms, cd.getFormId()) : null;
                cdl.add(new CollectedDataItem(subject, cd.getFormId(), form, formExtension, cd));
            }
        }

        CollectedDataAdapter adapter = new CollectedDataAdapter(this.getContext(), cdl);
        this.lvCollectedForms.setAdapter(adapter);

        if (collectedDataToEdit != null) { //highlight collected data to edit
            highlightCollectedData(collectedDataToEdit, adapter);
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


        if (dataItem.isFormExtension()) {
            CoreCollectedData coreCollectedData = boxCoreCollectedData.query(CoreCollectedData_.collectedId.equal(collectedData.collectedId)).build().findFirst();
            if (coreCollectedData != null) {
                HForm hform = FormUtil.getHFormBy(getContext(), coreCollectedData.formEntity);
                CoreEntity existingEntity = getRecordEntity(coreCollectedData);
                EditCoreExtensionFormUtil formUtil = new EditCoreExtensionFormUtil(this, this.getContext(), hform, coreCollectedData, existingEntity, this.formUtilities, new EditCoreExtensionFormUtil.Listener() {
                    @Override
                    public void onFinishedCollecting() {
                        showCollectedData();
                    }
                });

                formUtil.editExtensionForm(collectedData);
            } else {
                //remove because the CoreCollectedData was deleted it doesnt exists
                boxCollectedData.remove(collectedData);
            }

        } else {
            FormDataLoader formDataLoader = getFormDataLoader(collectedData);

            if (collectedData.formGroupCollected) {
                //Handle Form Group edition correctly
                handleOnFormGroupCollectedData(collectedData);
            } else {
                reOpenOdkForm(formDataLoader, collectedData, false, null);
            }
        }


    }

    private CoreEntity getRecordEntity(CoreCollectedData coreCollectedData) {
        CoreEntity entity = null;
        switch (coreCollectedData.formEntity) {
            case EDITED_REGION:
            case REGION: entity = boxRegions.get(coreCollectedData.formEntityId); break;
            case PRE_HOUSEHOLD:
            case EDITED_HOUSEHOLD:
            case HOUSEHOLD: entity = boxHouseholds.get(coreCollectedData.formEntityId); break;
            case EDITED_MEMBER:
            case MEMBER_ENU: entity = boxMembers.get(coreCollectedData.formEntityId); break;
            case HEAD_RELATIONSHIP:
            case CHANGE_HOUSEHOLD_HEAD: entity = boxHeadRelationships.get(coreCollectedData.formEntityId); break;
            case MARITAL_RELATIONSHIP: entity = boxMaritalRelationships.get(coreCollectedData.formEntityId); break;
            case INMIGRATION:
            case EXTERNAL_INMIGRATION: entity = boxInmigrations.get(coreCollectedData.formEntityId); break;
            case OUTMIGRATION: entity = boxOutmigrations.get(coreCollectedData.formEntityId); break;
            case PREGNANCY_REGISTRATION: entity = boxPregnancyRegistrations.get(coreCollectedData.formEntityId); break;
            case PREGNANCY_OUTCOME: entity = boxPregnancyOutcomes.get(coreCollectedData.formEntityId); break;
            case DEATH: entity = boxDeaths.get(coreCollectedData.formEntityId); break;
            case INCOMPLETE_VISIT: entity = boxIncompleteVisits.get(coreCollectedData.formEntityId); break;
            case VISIT: entity = boxVisits.get(coreCollectedData.formEntityId); break;
            case EXTRA_FORM: break;
            case INVALID_ENUM: break;
        }

        return entity;
    }

    private Form getFormById(List<Form> forms, String formId){
        for (Form f : forms){
            if (f.getFormId().equals(formId)) return f;
        }

        return null;
    }

    private CoreFormExtension getFormExtensionById(List<CoreFormExtension> forms, String formId){
        for (CoreFormExtension f : forms){
            if (f.extFormId.equals(formId)) return f;
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

        if (subject == null) return new ArrayList<CollectedData>();

        List<CollectedData> list = this.boxCollectedData.query().equal(CollectedData_.recordId, subject.getId())
                                                                .and()
                                                                .equal(CollectedData_.recordEntity, subject.getTableName().code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                                .filter((c) -> StringUtil.containsAny(c.formModules, selectedModules))  //filter by module
                                                                .build().find();

        //Log.d("listed forms: ", "sub-id="+subject+list.size());

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
            buildFormSelectorDialog(loaderList);
        }
    }

    public void onEditCollectedData(CollectedData collectedData) {

       if (collectedDataToEdit != null) {
            //reopen / edit
            CollectedDataAdapter adapter = (CollectedDataAdapter) this.lvCollectedForms.getAdapter();
            int position = adapter.getPositionOf(collectedDataToEdit);
            if (position >= 0) {
                onCollectedDataItemClicked(position);
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
                onFormSelectorCancelClicked();
            }
        }).show();
    }

    private void onFormSelectorCancelClicked() {

        if (externalCallOnCollectData) {
            //if we opened onCollectData from a External Activity not the same that has this Fragment
            if (collectedDataFragmentListener != null) {
                this.externalCallOnCollectData = false;
                collectedDataFragmentListener.afterExternalCallOnCollectDataFinished();
            }
        } else if (externalCallCollectedDataToEdit) {
            //if we opened a form to edit from a External Activity not the same that has this Fragment
            if (collectedDataFragmentListener != null) {
                this.externalCallCollectedDataToEdit = false;
                collectedDataFragmentListener.afterExternalCallCollectedDataToEditFinished();
            }
        }
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

    public void highlightCollectedData(CollectedData collectedData, CollectedDataAdapter adapter) {
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
            collectedData.formFinalized = true;

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

            collectedData.visitId = visit != null ? visit.id : 0;

            this.boxCollectedData.put(collectedData);
            Log.d("inserting", "new collected data - visit.id = "+collectedData.visitId);
        }else{ //update
            collectedData.setFormId(formId);
            collectedData.setFormUri(contentUri.toString());
            //collectedData.setFormXmlPath(instanceFileUri);
            collectedData.setFormInstanceName(metaInstanceName);
            collectedData.setFormLastUpdatedDate(lastUpdatedDate);
            collectedData.formFinalized = true;

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

        onFinishedOdkDataCollection(formLoadData);

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
            collectedData.formFinalized = false;

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

            collectedData.visitId = visit != null ? visit.id : 0;

            this.boxCollectedData.put(collectedData);
            Log.d("inserting", "new collected data");
        }else{ //update
            collectedData.setFormId(formId);
            collectedData.setFormUri(contentUri.toString());
            //collectedData.setFormXmlPath(instanceFileUri);
            collectedData.setFormInstanceName(metaInstanceName);
            collectedData.setFormLastUpdatedDate(lastUpdatedDate);
            collectedData.formFinalized = false;

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

        onFinishedOdkDataCollection(formLoadData);
    }

    @Override
    public void onDeleteForm(OdkFormLoadData formLoadData, Uri contentUri, String instanceFileUri) {

        getActivity().runOnUiThread(()->{
            this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString(), QueryBuilder.StringOrder.CASE_SENSITIVE).build().remove();
            // delete also the formGroupInstanceChild
            this.boxFormGroupInstanceChilds.query(FormGroupInstanceChild_.formInstanceUri.equal(instanceFileUri)).build().remove();
        });

        getActivity().runOnUiThread(() -> {
            onFinishedOdkDataCollection(formLoadData);
        });
    }

    @Override
    public void onFormLoadError(OdkFormLoadData formLoadData, OdkFormLoadResult result) {
        onFinishedOdkDataCollection(formLoadData);
    }

    @Override
    public void onFormInstanceNotFound(OdkFormLoadData formLoadData, final Uri contenUri) {
        buildDeleteFormInstanceNotFoundDialog(formLoadData, contenUri);
    }

    private void onFinishedOdkDataCollection(OdkFormLoadData formLoadData){
        showCollectedData();

        if (!formLoadData.isFormGroupLoad) { //normal odk collection - form group after collecting odk they remain showing dialog

            if (externalCallOnCollectData) {
                ////if we opened onCollectData from a External Activity not the same that has this Fragment
                if (collectedDataFragmentListener != null) {
                    this.externalCallOnCollectData = false;
                    collectedDataFragmentListener.afterExternalCallOnCollectDataFinished();
                }
            } else if (externalCallCollectedDataToEdit) {
                //if we opened a form to edit from a External Activity not the same that has this Fragment
                if (collectedDataFragmentListener != null) {
                    this.externalCallCollectedDataToEdit = false;
                    Log.d("executed-until", "cdf-afterExtCallEdit");
                    collectedDataFragmentListener.afterExternalCallCollectedDataToEditFinished();
                }
            } else {
                //its a normal data collection (called from EntityDetails button onCollectData or by selecting a collected data in the list) from an Activity that has this Fragment
                if (collectedDataFragmentListener != null) {
                    this.externalCallOnCollectData = false;
                    this.externalCallCollectedDataToEdit = false;
                    
                    collectedDataFragmentListener.afterInternalCollectDataFinished();
                }
            }

        }
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

            loadMappingDataValues(formDataLoader, CollectedDataFragment.this.visit, trackingSubject, formGroupInstance);
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

    public interface CollectedDataFragmentListener {
        void afterExternalCallOnCollectDataFinished();

        void afterExternalCallCollectedDataToEditFinished();

        void afterInternalCollectDataFinished();
    }
}
