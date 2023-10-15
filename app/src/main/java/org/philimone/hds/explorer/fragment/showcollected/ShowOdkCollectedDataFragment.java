package org.philimone.hds.explorer.fragment.showcollected;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.data.FormFilter;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.fragment.showcollected.adapter.ShowCollectedDataAdapter;
import org.philimone.hds.explorer.fragment.showcollected.adapter.model.OdkCollectedDataItem;
import org.philimone.hds.explorer.main.HouseholdDetailsActivity;
import org.philimone.hds.explorer.main.MemberDetailsActivity;
import org.philimone.hds.explorer.main.RegionDetailsActivity;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.CoreFormExtension;
import org.philimone.hds.explorer.model.Dataset;
import org.philimone.hds.explorer.model.Dataset_;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.FormSubject;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Module;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.widget.LoadingDialog;
import org.philimone.hds.explorer.widget.RecyclerListView;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.utilities.StringUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShowOdkCollectedDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowOdkCollectedDataFragment extends Fragment {

    private enum SubjectMode { REGION, HOUSEHOLD, MEMBER };

    private RecyclerListView lvCollectedForms;
    private EditText txtCollectedDataFilter;
    private LoadingDialog loadingDialog;

    private User loggedUser;

    private Box<CollectedData> boxCollectedData;
    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<Visit> boxVisits;
    private Box<User> boxUsers;
    private Box<Form> boxForms;
    private Box<CoreFormExtension> boxCoreForms;
    private Box<Module> boxModules;
    private Box<Dataset> boxDatasets;
    private List<String> selectedModules = new ArrayList<>();

    private ActivityResultLauncher<Intent> onFormEditLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
       //after calling details activity to edit a collected odk form
       reloadCollectedData();
    });

    public ShowOdkCollectedDataFragment() {
        // Required empty public constructor
        initBoxes();
    }

    public static ShowOdkCollectedDataFragment newInstance(){
        ShowOdkCollectedDataFragment fragment = new ShowOdkCollectedDataFragment();

        fragment.loggedUser = Bootstrap.getCurrentUser();
        //fragment.initializeDataloaders();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.show_collected_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loggedUser = Bootstrap.getCurrentUser();

        initialize(view);
    }

    private void initBoxes() {
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        this.boxCoreForms = ObjectBoxDatabase.get().boxFor(CoreFormExtension.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxVisits = ObjectBoxDatabase.get().boxFor(Visit.class);
        this.boxUsers = ObjectBoxDatabase.get().boxFor(User.class);
        this.boxModules = ObjectBoxDatabase.get().boxFor(Module.class);
        this.boxDatasets = ObjectBoxDatabase.get().boxFor(Dataset.class);
    }

    private void initialize(View view) {

        this.loadingDialog = new LoadingDialog(this.getContext());

        selectedModules.addAll(loggedUser.getSelectedModules());

        lvCollectedForms = view.findViewById(R.id.lvCollectedForms);
        txtCollectedDataFilter = view.findViewById(R.id.txtCollectedDataFilter);

        lvCollectedForms.addOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long id) {
                onCollectedDataItemClicked(position);
            }

            @Override
            public void onItemLongClick(View view, int position, long id) {

            }
        });

        this.txtCollectedDataFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterCollectedData(s.toString());
            }
        });

        this.showCollectedData();
    }

    private void filterCollectedData(String text){
        if (text != null){

            ShowCollectedDataAdapter adapter = (ShowCollectedDataAdapter) this.lvCollectedForms.getAdapter();
            adapter.filterSubjects(text);
            //adapter.notifyDataSetChanged();
            //this.elvTrackingLists.invalidateViews();
        }
    }

    /*
     * Show the data collected for the selected individual - but only shows data that belongs to Forms that the user can view (FormDataLoader)
     * With this if we selected a follow_up list household we will view only the forms of that individual
     */
    private void showCollectedData() {
        //this.showProgress(true);

        if (this.lvCollectedForms == null) return;

        List<CollectedData> list = getAllCollectedData();
        List<Form> forms = this.boxForms.getAll();
        List<CoreFormExtension> coreforms = this.boxCoreForms.getAll();
        List<OdkCollectedDataItem> cdl = new ArrayList<>();

        for (CollectedData cd : list){
            Form form = getFormById(forms, cd.getFormId());
            CoreFormExtension coreform = form==null ? getFormExtensionById(coreforms, cd.getFormId()) : null;
            cdl.add(new OdkCollectedDataItem(cd.getFormId(), getSubject(cd), form, coreform, cd));
        }

        ShowCollectedDataAdapter adapter = new ShowCollectedDataAdapter(this.getContext(), cdl);
        this.lvCollectedForms.setAdapter(adapter);
    }

    private FormSubject getSubject(CollectedData collectedData) {
        FormSubject subject = null;

        if (collectedData.recordEntity == null) return null;

        switch (collectedData.recordEntity) {
            case REGION: subject = this.boxRegions.get(collectedData.recordId); break;
            case HOUSEHOLD: subject = this.boxHouseholds.get(collectedData.recordId); break;
            case MEMBER: subject = this.boxMembers.get(collectedData.recordId); break;
            case VISIT: break;
            case USER: subject = this.boxUsers.get(collectedData.recordId); break;
        }

        return subject;
    }

    public void reloadCollectedData(){
        showCollectedData();
    }

    private void onCollectedDataItemClicked(int position) {
        ShowCollectedDataAdapter adapter = (ShowCollectedDataAdapter) this.lvCollectedForms.getAdapter();
        OdkCollectedDataItem dataItem = adapter.getItem(position);

        CollectedData collectedData = dataItem.getCollectedData();

        if (dataItem.isRegionItem()) {
            ShowRegionTask task = new ShowRegionTask(dataItem.getRegion(), collectedData);
            task.execute();

            showLoadingDialog(getString(R.string.loading_dialog_region_details_lbl), true);
        }

        if (dataItem.isHouseholdItem()) {
            ShowHouseholdTask task = new ShowHouseholdTask(dataItem.getHousehold(), collectedData);
            task.execute();

            showLoadingDialog(getString(R.string.loading_dialog_household_details_lbl), true);
        }

        if (dataItem.isMemberItem()) {
            ShowMemberTask task = new ShowMemberTask(dataItem.getMember(), collectedData);
            task.execute();

            showLoadingDialog(getString(R.string.loading_dialog_member_details_lbl), true);
        }
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

    private List<CollectedData> getAllCollectedData() {
        List<CollectedData> list = this.boxCollectedData.query().filter((c) -> StringUtil.containsAny(c.formModules, selectedModules))
                                                                .orderDesc(CollectedData_.formLastUpdatedDate).order(CollectedData_.recordEntity).order(CollectedData_.recordId).order(CollectedData_.formId)
                                                                .build().find();
        return list;
    }

    private void showLoadingDialog(String msg, boolean show){
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.dismiss();
        }
    }
    
    class ShowRegionTask extends AsyncTask<Void, Void, Void> {
        private Region region;
        private CollectedData collectedData;

        public ShowRegionTask(Region region, CollectedData collectedData) {
            this.region = region;
            this.collectedData = collectedData;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = new Intent(ShowOdkCollectedDataFragment.this.getContext(), RegionDetailsActivity.class);
            intent.putExtra("region", region.id);
            intent.putExtra("odk-form-edit", collectedData.id);

            showLoadingDialog(null, false);

            onFormEditLauncher.launch(intent);
        }
    }

    class ShowHouseholdTask extends AsyncTask<Void, Void, Void> {
        private Household household;
        private CollectedData collectedData;

        public ShowHouseholdTask(Household household, CollectedData collectedData) {
            this.household = household;
            this.collectedData = collectedData;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = new Intent(ShowOdkCollectedDataFragment.this.getContext(), HouseholdDetailsActivity.class);
            intent.putExtra("household", household.id);
            intent.putExtra("odk-form-edit", collectedData.id);

            showLoadingDialog(null, false);

            onFormEditLauncher.launch(intent);
        }
    }

    class ShowMemberTask extends AsyncTask<Void, Void, Void> {
        private Member member;
        private CollectedData collectedData;

        public ShowMemberTask(Member member, CollectedData collectedData) {
            this.member = member;
            this.collectedData = collectedData;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = new Intent(ShowOdkCollectedDataFragment.this.getContext(), MemberDetailsActivity.class);
            intent.putExtra("member", member.id);
            intent.putExtra("odk-form-edit", collectedData.id);

            showLoadingDialog(null, false);

            onFormEditLauncher.launch(intent);
        }
    }

}
