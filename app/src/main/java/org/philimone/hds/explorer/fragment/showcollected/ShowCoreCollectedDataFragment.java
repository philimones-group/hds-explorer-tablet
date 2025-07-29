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
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.showcollected.adapter.ShowCoreCollectedDataAdapter;
import org.philimone.hds.explorer.fragment.showcollected.adapter.model.CoreCollectedDataItem;
import org.philimone.hds.explorer.fragment.showcollected.utilities.CoreCollectedDataDeletionUtil;
import org.philimone.hds.explorer.main.HouseholdDetailsActivity;
import org.philimone.hds.explorer.main.MemberDetailsActivity;
import org.philimone.hds.explorer.main.RegionDetailsActivity;
import org.philimone.hds.explorer.main.ShowCollectedDataActivity;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.Death;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.FormSubject;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.HeadRelationship_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.IncompleteVisit;
import org.philimone.hds.explorer.model.Inmigration;
import org.philimone.hds.explorer.model.MaritalRelationship;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Outmigration;
import org.philimone.hds.explorer.model.PregnancyOutcome;
import org.philimone.hds.explorer.model.PregnancyRegistration;
import org.philimone.hds.explorer.model.PregnancyVisit;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.RegionHeadRelationship;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.Visit_;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.LoadingDialog;
import org.philimone.hds.explorer.widget.RecyclerListView;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import mz.betainteractive.odk.model.FilledForm;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShowCoreCollectedDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowCoreCollectedDataFragment extends Fragment {

    private ActionListener actionListener;
    private boolean isRegionHeadSupported;

    private enum SubjectMode { REGION, HOUSEHOLD, MEMBER };

    private RecyclerListView lvCollectedForms;
    private EditText txtCollectedDataFilter;
    private Button btShowCollectedDelete;
    private LoadingDialog loadingDialog;

    private FormSubject subject;
    private User loggedUser;

    private Box<CoreCollectedData> boxCoreCollectedData;
    private Box<ApplicationParam> boxAppParams;
    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Visit> boxVisits;
    private Box<Member> boxMembers;
    private Box<MaritalRelationship> boxMaritalRelationships;
    private Box<Inmigration> boxInmigrations;
    private Box<Outmigration> boxOutmigrations;
    private Box<PregnancyRegistration> boxPregnancyRegistrations;
    private Box<PregnancyOutcome> boxPregnancyOutcomes;
    private Box<PregnancyVisit> boxPregnancyVisits;
    private Box<Death> boxDeaths;
    private Box<HeadRelationship> boxHeadRelationships;
    private Box<IncompleteVisit> boxIncompleteVisits;
    private Box<RegionHeadRelationship> boxRegionHeadRelationships;

    private List<String> selectedModules = new ArrayList<>();

    private ActivityResultLauncher<Intent> onFormEditLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        //after calling details activity to edit a collected odk form
        fireOnFormEdited();
    });

    public ShowCoreCollectedDataFragment() {
        // Required empty public constructor
        initBoxes();
        loggedUser = Bootstrap.getCurrentUser();
    }

    public ShowCoreCollectedDataFragment(ActionListener listener) {
        this();
        this.actionListener = listener;
    }

    public static ShowCoreCollectedDataFragment newInstance(ActionListener listener){
        ShowCoreCollectedDataFragment fragment = new ShowCoreCollectedDataFragment(listener);
        return fragment;
    }

    private void fireOnFormEdited() {
        if (this.actionListener != null) {
            this.actionListener.onCoreFormEdited();
        }
    }

    private void fireOnDeletedForms() {
        if (this.actionListener != null) {
            this.actionListener.onDeletedCoreForms();
        }
    }

    private void fireOnCoreFormsLoaded() {
        if (this.actionListener != null) {
            this.actionListener.onCoreFormsLoaded();
        }
    }

    private void fireOnViewsCreated() {
        if (this.actionListener != null) {
            this.actionListener.onCoreCollectedViewsCreated(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.show_core_collected_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        fireOnViewsCreated();
    }

    private void initBoxes() {
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxVisits = ObjectBoxDatabase.get().boxFor(Visit.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxMaritalRelationships = ObjectBoxDatabase.get().boxFor(MaritalRelationship.class);
        this.boxInmigrations = ObjectBoxDatabase.get().boxFor(Inmigration.class);
        this.boxInmigrations = ObjectBoxDatabase.get().boxFor(Inmigration.class);
        this.boxOutmigrations = ObjectBoxDatabase.get().boxFor(Outmigration.class);
        this.boxPregnancyRegistrations = ObjectBoxDatabase.get().boxFor(PregnancyRegistration.class);
        this.boxPregnancyOutcomes = ObjectBoxDatabase.get().boxFor(PregnancyOutcome.class);
        this.boxPregnancyVisits = ObjectBoxDatabase.get().boxFor(PregnancyVisit.class);
        this.boxDeaths = ObjectBoxDatabase.get().boxFor(Death.class);
        this.boxHeadRelationships = ObjectBoxDatabase.get().boxFor(HeadRelationship.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxIncompleteVisits = ObjectBoxDatabase.get().boxFor(IncompleteVisit.class);
        this.boxRegionHeadRelationships = ObjectBoxDatabase.get().boxFor(RegionHeadRelationship.class);
    }

    private void initialize(View view) {

        this.txtCollectedDataFilter = view.findViewById(R.id.txtCollectedDataFilter);
        this.btShowCollectedDelete = view.findViewById(R.id.btShowCollectedDelete);
        this.loadingDialog = new LoadingDialog(this.getContext());

        selectedModules.addAll(loggedUser.getSelectedModules());

        lvCollectedForms = view.findViewById(R.id.lvCollectedForms);

        lvCollectedForms.addOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long id) {
                selectItem(position);
            }

            @Override
            public void onItemLongClick(View view, int position, long id) {
                onCollectedDataItemClicked(position);
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

        this.btShowCollectedDelete.setOnClickListener(v -> {
            onDeleteSelectedRecords();
        });

        //this.showCollectedData();
        this.isRegionHeadSupported = Queries.isRegionHeadSupported(boxAppParams);
    }

    private void filterCollectedData(String text){
        if (text != null){

            ShowCoreCollectedDataAdapter adapter = (ShowCoreCollectedDataAdapter) this.lvCollectedForms.getAdapter();
            if (adapter != null) {
                adapter.filterSubjects(text);

                //adapter.notifyDataSetChanged();
                //this.elvTrackingLists.invalidateViews();
            }

        }
    }

    /*
     * Show the data collected for the selected individual - but only shows data that belongs to Forms that the user can view (FormDataLoader)
     * With this if we selected a follow_up list household we will view only the forms of that individual
     */
    private void showCollectedData() {
        //this.showProgress(true);
        new ShowCollectedDataTask().execute();
    }

    public void reloadCollectedData(){
        showCollectedData();
    }

    private void selectItem(int position) {
        ShowCoreCollectedDataAdapter adapter = (ShowCoreCollectedDataAdapter) this.lvCollectedForms.getAdapter();
        adapter.setCheckedOrUnchecked(position);
    }

    private void onCollectedDataItemClicked(int position) {
        ShowCoreCollectedDataAdapter adapter = (ShowCoreCollectedDataAdapter) this.lvCollectedForms.getAdapter();
        CoreCollectedDataItem dataItem = adapter.getItem(position);

        if (dataItem.household != null) {
            ShowHouseholdTask task = new ShowHouseholdTask(dataItem.household);
            task.execute();
            showLoadingDialog(getString(R.string.loading_dialog_household_details_lbl), true);

        } else if (dataItem.member != null) {
            ShowMemberTask task = new ShowMemberTask(dataItem.member);
            task.execute();
            showLoadingDialog(getString(R.string.loading_dialog_member_details_lbl), true);

        } else if (dataItem.region != null) {
            ShowRegionTask task = new ShowRegionTask(dataItem.region);
            task.execute();
            showLoadingDialog(getString(R.string.loading_dialog_region_details_lbl), true);

        }
    }

    private void onDeleteSelectedRecords() {
        ShowCoreCollectedDataAdapter adapter = (ShowCoreCollectedDataAdapter) this.lvCollectedForms.getAdapter();
        List<CoreCollectedDataItem> selectedList = adapter.getSelectedCollectedData();

        if (selectedList.size() > 0) {
            //display two warnings
            //You are about to delete 3 records that were recently collected.
            //You must be cautious while doing this

            DialogFactory yesNoDialog = DialogFactory.createMessageYN(this.getContext(), R.string.show_collected_data_deletion_warning_title_lbl, R.string.show_collected_data_deletion_warning_msg2_lbl, new DialogFactory.OnYesNoClickListener() {
                @Override
                public void onYesClicked() {
                    deleteSelectedRecords(selectedList);
                }

                @Override
                public void onNoClicked() {

                }
            });

            DialogFactory.createMessageInfo(this.getContext(), R.string.show_collected_data_deletion_warning_title_lbl, R.string.show_collected_data_deletion_warning_msg1_lbl, clickedButton -> {
                yesNoDialog.show();
            }).show();
        }
    }

    private void deleteSelectedRecords(List<CoreCollectedDataItem> selectedList) {
        showLoadingDialog(getString(R.string.show_collected_data_deletion_loading_lbl), true);
        new DeletionTask(selectedList).execute();
    }

    private List<CoreCollectedDataItem> getAllCollectedData() {
        List<CoreCollectedDataItem> list = new ArrayList<>();
        List<CoreCollectedData> listc = this.boxCoreCollectedData.query().orderDesc(CoreCollectedData_.createdDate).order(CoreCollectedData_.formEntityCode).build().find();

        for (CoreCollectedData cdata : listc) {

            //Ignore Change Region Head if not supported
            if (!isRegionHeadSupported && cdata.formEntity == CoreFormEntity.CHANGE_REGION_HEAD) {
                continue;
            }

            //with retaining not uploaded data in the system after an upload - this would generate a bug
            //so we will not put CoreCollectedData here without associated data
            FormSubject subject = getFormSubject(cdata);
            list.add(new CoreCollectedDataItem(cdata, subject));
        }

        return list;
    }

    private FormSubject getFormSubject(CoreCollectedData coreCollectedData) {
        //All Core Forms except Region must go to Household
        switch (coreCollectedData.formEntity) {
            case EDITED_REGION:
            case REGION: return boxRegions.get(coreCollectedData.formEntityId); //query(Region_.id.equal(coreCollectedData.formEntityId)).build().findFirst();
            case PRE_HOUSEHOLD:
            case EDITED_HOUSEHOLD:
            case HOUSEHOLD_RELOCATION:
            case HOUSEHOLD: return boxHouseholds.get(coreCollectedData.formEntityId); //query(Household_.id.equal(coreCollectedData.formEntityId)).build().findFirst();
            case EDITED_MEMBER:
            case MEMBER_ENU: {
                Member member = boxMembers.get(coreCollectedData.formEntityId); //query(Member_.id.equal(coreCollectedData.formEntityId)).build().findFirst();
                if (member != null) {
                    return boxHouseholds.query(Household_.code.equal(member.householdCode)).build().findFirst();
                }
                return null;
            }
            case HEAD_RELATIONSHIP:
            case CHANGE_HOUSEHOLD_HEAD: {
                HeadRelationship entity = boxHeadRelationships.get(coreCollectedData.formEntityId); //query(HeadRelationship_.id.equal(coreCollectedData.formEntityId)).build().findFirst();
                if (entity != null) {
                    return boxHouseholds.query(Household_.code.equal(entity.householdCode)).build().findFirst();
                }
            }
            case MARITAL_RELATIONSHIP:
            case OUTMIGRATION:
            case PREGNANCY_REGISTRATION:
            case PREGNANCY_OUTCOME:
            case PREGNANCY_VISIT:
            case DEATH:
            case INCOMPLETE_VISIT:
            case INMIGRATION:
            case EXTERNAL_INMIGRATION:
            case VISIT: {
                Visit visit = boxVisits.get(coreCollectedData.visitId); //query(Visit_.id.equal(coreCollectedData.visitId)).build().findFirst();
                if (visit != null) {
                    return boxHouseholds.query(Household_.code.equal(visit.householdCode)).build().findFirst();
                }
            }
            case CHANGE_REGION_HEAD: {
                RegionHeadRelationship entity = boxRegionHeadRelationships.get(coreCollectedData.formEntityId);
                if (entity != null) {
                    if (coreCollectedData.visitId > 0) {
                        Visit visit = boxVisits.get(coreCollectedData.visitId);
                        if (visit != null) {
                            return boxHouseholds.query(Household_.code.equal(visit.householdCode)).build().findFirst();
                        } else {
                            return boxRegions.query(Region_.code.equal(entity.regionCode)).build().findFirst();
                        }
                    } else {
                        return boxRegions.query(Region_.code.equal(entity.regionCode)).build().findFirst();
                    }
                }
                //put visits here - to get household
            }
            case EXTRA_FORM: break;
            case INVALID_ENUM: break;

            default:
                throw new IllegalStateException("Unexpected value: " + coreCollectedData.formEntity);
        }

        return null;
    }

    private void showLoadingDialog(String msg, boolean show){
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.dismiss();
        }
    }

    class ShowCollectedDataTask extends AsyncTask<Void, Void, ShowCoreCollectedDataAdapter> {

        @Override
        protected ShowCoreCollectedDataAdapter doInBackground(Void... voids) {
            if (lvCollectedForms == null) return null;

            List<CoreCollectedDataItem> list = getAllCollectedData();

            ShowCoreCollectedDataAdapter adapter = new ShowCoreCollectedDataAdapter(getContext(), list, new ShowCoreCollectedDataAdapter.OnItemActionListener() {
                @Override
                public void onInfoButtonClicked(CoreCollectedDataItem collectedData) {
                    Log.d("collected", ""+collectedData);
                }

                @Override
                public void onCheckedStatusChanged(int position, boolean checkedStatus, boolean allChecked, boolean anyChecked) {
                    btShowCollectedDelete.setEnabled(anyChecked);
                }
            });

            return adapter;
        }

        @Override
        protected void onPostExecute(ShowCoreCollectedDataAdapter adapter) {
            super.onPostExecute(adapter);

            if (adapter != null) {
                lvCollectedForms.setAdapter(adapter);
                fireOnCoreFormsLoaded();
            }
        }
    }

    class ShowHouseholdTask extends AsyncTask<Void, Void, Void> {
        private Household household;

        public ShowHouseholdTask(Household household) {
            this.household = household;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = new Intent(ShowCoreCollectedDataFragment.this.getContext(), HouseholdDetailsActivity.class);
            intent.putExtra("household", household.id);

            showLoadingDialog(null, false);

            onFormEditLauncher.launch(intent);
        }
    }

    class ShowMemberTask extends AsyncTask<Void, Void, Void> {
        private Member member;

        public ShowMemberTask(Member member) {
            this.member = member;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = new Intent(ShowCoreCollectedDataFragment.this.getContext(), MemberDetailsActivity.class);
            intent.putExtra("member", member.id);

            showLoadingDialog(null, false);

            onFormEditLauncher.launch(intent);
        }
    }

    class ShowRegionTask extends AsyncTask<Void, Void, Void> {
        private Region region;

        public ShowRegionTask(Region region) {
            this.region = region;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = new Intent(ShowCoreCollectedDataFragment.this.getContext(), RegionDetailsActivity.class);
            intent.putExtra("region", region.id);

            showLoadingDialog(null, false);

            onFormEditLauncher.launch(intent);
        }
    }

    class DeletionTask extends AsyncTask<Void, Void, Void> {

        private List<CoreCollectedDataItem> list;

        public DeletionTask(List<CoreCollectedDataItem> list) {
            this.list = list;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            new CoreCollectedDataDeletionUtil(getContext()).deleteRecords(list);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            showLoadingDialog(null, false);
            fireOnDeletedForms();
        }
    }

    class XResult {
        Form form;
        CollectedData collectedData;
        FilledForm filledForm;

        public XResult(Form form, FilledForm filledForm, CollectedData collectedData) {
            this.form = form;
            this.collectedData = collectedData;
            this.filledForm = filledForm;
        }
    }

    public interface ActionListener {
        void onDeletedCoreForms();

        void onCoreFormEdited();

        void onCoreFormsLoaded();

        void onCoreCollectedViewsCreated(ShowCoreCollectedDataFragment fragment);
    }
}
