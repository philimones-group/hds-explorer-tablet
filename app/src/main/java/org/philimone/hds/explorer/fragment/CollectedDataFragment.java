package org.philimone.hds.explorer.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.CollectedDataArrayAdapter;
import org.philimone.hds.explorer.adapter.model.CollectedDataItem;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.FormSubject;
import org.philimone.hds.explorer.model.Module;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.FormSelectorDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.objectbox.Box;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.listener.OdkFormResultListener;
import mz.betainteractive.odk.model.FilledForm;
import mz.betainteractive.utilities.StringUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CollectedDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CollectedDataFragment extends Fragment implements OdkFormResultListener {

    private enum SubjectMode { REGION, HOUSEHOLD, MEMBER };

    private ListView lvCollectedForms;

    private FormSubject subject;
    private User loggedUser;

    private List<FormDataLoader> formDataLoaders = new ArrayList<>();
    private FormDataLoader lastLoadedForm;
    private FormUtilities formUtilities;

    private Box<CollectedData> boxCollectedData;
    private Box<Form> boxForms;
    private Box<Module> boxModules;

    private SubjectMode subjectMode;

    private List<String> selectedModules = new ArrayList<>();

    public CollectedDataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HouseholdFormsFragment.
     */
    public static CollectedDataFragment newInstance(FormSubject subject, User user, List<FormDataLoader> formDataLoaders) {
        CollectedDataFragment fragment = new CollectedDataFragment();
        fragment.subject = subject;
        fragment.loggedUser = user;
        fragment.formDataLoaders.addAll(formDataLoaders);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        formUtilities = new FormUtilities(this, this);

        initBoxes();
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
        this.boxModules = ObjectBoxDatabase.get().boxFor(Module.class);
    }

    private void initialize(View view) {

        selectedModules.addAll(loggedUser.getSelectedModules());

        lvCollectedForms = view.findViewById(R.id.lvCollectedForms);

        lvCollectedForms.setOnItemClickListener((parent, view1, position, id) -> onCollectedDataItemClicked(position));

        this.showCollectedData();
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

        CollectedDataArrayAdapter adapter = new CollectedDataArrayAdapter(this.getContext(), cdl);
        this.lvCollectedForms.setAdapter(adapter);
    }

    private void onCollectedDataItemClicked(int position) {
        CollectedDataArrayAdapter adapter = (CollectedDataArrayAdapter) this.lvCollectedForms.getAdapter();
        CollectedDataItem dataItem = adapter.getItem(position);

        CollectedData collectedData = dataItem.getCollectedData();
        FormDataLoader formDataLoader = getFormDataLoader(collectedData);

        openOdkForm(formDataLoader, collectedData);
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

    private FormDataLoader getFormDataLoader(CollectedData collectedData){

        for (FormDataLoader dl : this.formDataLoaders){
            if (dl.getForm().getFormId().equals(collectedData.getFormId())){
                return dl;
            }
        }

        return null;
    }

    private List<CollectedData> getAllCollectedData() {
        List<CollectedData> list = this.boxCollectedData.query().equal(CollectedData_.recordId, subject.getId())
                                                                .and()
                                                                .equal(CollectedData_.tableName, subject.getTableName())
                                                                .filter((c) -> StringUtil.containsAny(c.formModules, selectedModules))  //filter by module
                                                                .build().find();
        return list;
    }

    private CollectedData getCollectedData(FormDataLoader formDataLoader){

        CollectedData collectedData = this.boxCollectedData.query().equal(CollectedData_.formId, formDataLoader.getForm().getFormId())
                                                                   .and().equal(CollectedData_.recordId, subject.getId())
                                                                   .and().equal(CollectedData_.tableName, subject.getTableName())
                                                                   .filter((c) -> StringUtil.containsAny(c.formModules, selectedModules)) //filter by module
                                                                   .build().findFirst();

        return collectedData;
    }

    public void onCollectData(){

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

    private void buildFormSelectorDialog(List<FormDataLoader> loaders) {

        FormSelectorDialog.createDialog(this.getParentFragmentManager(), loaders, new FormSelectorDialog.OnFormSelectedListener() {
            @Override
            public void onFormSelected(FormDataLoader formDataLoader) {
                openOdkForm(formDataLoader);
            }

            @Override
            public void onCancelClicked() {

            }
        }).show();
    }


    //<editor-fold desc="ODK Form Utility methods">
    private void openOdkForm(FormDataLoader formDataLoader) {

        CollectedData collectedData = getCollectedData(formDataLoader);

        this.lastLoadedForm = formDataLoader;

        Form form = formDataLoader.getForm();

        //reload timestamp constants
        formDataLoader.reloadTimestampConstants();

        FilledForm filledForm = new FilledForm(form.getFormId());
        filledForm.putAll(formDataLoader.getValues());
        //filledForm.setHouseholdMembers(getMemberOnListAdapter());

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

    @Override
    public void onFormFinalized(Uri contentUri, File xmlFile, String metaInstanceName, Date lastUpdatedDate) {
        Log.d("form finalized"," "+contentUri+", "+xmlFile);

        //search existing record
        CollectedData collectedData = this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString())
                .and().equal(CollectedData_.recordId, subject.getId())
                .and().equal(CollectedData_.tableName, subject.getTableName()).build().findFirst();

        if (collectedData == null){ //insert
            collectedData = new CollectedData();
            collectedData.setFormId(lastLoadedForm.getForm().getFormId());
            collectedData.setFormUri(contentUri.toString());
            collectedData.setFormXmlPath(xmlFile.toString());
            collectedData.setFormInstanceName(metaInstanceName);
            collectedData.setFormLastUpdatedDate(lastUpdatedDate);

            collectedData.setFormModules(lastLoadedForm.getForm().modules);

            collectedData.setCollectedBy(loggedUser.getUsername());
            collectedData.setUpdatedBy("");
            collectedData.setSupervisedBy("");

            collectedData.setRecordId(subject.getId());
            collectedData.setTableName(subject.getTableName());

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

            collectedData.setRecordId(subject.getId());
            collectedData.setTableName(subject.getTableName());

            this.boxCollectedData.put(collectedData);
            Log.d("updating", "new collected data");
        }

        showCollectedData();

    }

    @Override
    public void onFormUnFinalized(Uri contentUri, File xmlFile, String metaInstanceName, Date lastUpdatedDate) {
        Log.d("form unfinalized"," "+contentUri);

        //search existing record
        CollectedData collectedData = this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString())
                .and().equal(CollectedData_.recordId, subject.getId())
                .and().equal(CollectedData_.tableName, subject.getTableName()).build().findFirst();

        if (collectedData == null){ //insert
            collectedData = new CollectedData();
            collectedData.setFormId(lastLoadedForm.getForm().getFormId());
            collectedData.setFormUri(contentUri.toString());
            collectedData.setFormXmlPath("");
            collectedData.setFormInstanceName(metaInstanceName);
            collectedData.setFormLastUpdatedDate(lastUpdatedDate);

            collectedData.setFormModules(lastLoadedForm.getForm().modules);

            collectedData.setCollectedBy(loggedUser.getUsername());
            collectedData.setUpdatedBy("");
            collectedData.setSupervisedBy("");

            collectedData.setRecordId(subject.getId());
            collectedData.setTableName(subject.getTableName());

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

            collectedData.setRecordId(subject.getId());
            collectedData.setTableName(subject.getTableName());

            this.boxCollectedData.put(collectedData);
            Log.d("updating", "new collected data");
        }

        showCollectedData();
    }

    @Override
    public void onDeleteForm(Uri contentUri) {

        this.boxCollectedData.query().equal(CollectedData_.formUri, contentUri.toString()).build().remove(); //delete where formUri=contentUri

        showCollectedData();
    }

    @Override
    public void onFormNotFound(final Uri contenUri) {
        buildDeleteSavedFormDialog(contenUri);
    }

    private void buildDeleteSavedFormDialog(final Uri contenUri){

        DialogFactory.createMessageYN(this.getContext(), R.string.household_details_dialog_del_saved_form_title_lbl, R.string.household_details_dialog_del_saved_form_msg_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                onDeleteForm(contenUri);
            }

            @Override
            public void onNoClicked() {

            }
        }).show();
    }
    //</editor-fold>

}
