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
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.showcollected.adapter.model.CoreCollectedDataItem;
import org.philimone.hds.explorer.fragment.showcollected.adapter.ShowVisitCollectedDataAdapter;
import org.philimone.hds.explorer.main.HouseholdDetailsActivity;
import org.philimone.hds.explorer.main.MemberDetailsActivity;
import org.philimone.hds.explorer.main.RegionDetailsActivity;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.FormSubject;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Module;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.widget.LoadingDialog;
import org.philimone.hds.explorer.widget.RecyclerListView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.objectbox.Box;
import mz.betainteractive.odk.model.FilledForm;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShowVisitCollectedDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowVisitCollectedDataFragment extends Fragment {

    private enum SubjectMode { REGION, HOUSEHOLD, MEMBER };

    private RecyclerListView lvCollectedForms;
    private EditText txtCollectedDataFilter;
    private LoadingDialog loadingDialog;

    private FormSubject subject;
    private User loggedUser;

    private ActionListener actionListener;
    private List<FormDataLoader> formDataLoaders = new ArrayList<>();
    private boolean isRegionHeadSupported;

    private Box<CoreCollectedData> boxCoreCollectedData;
    private Box<ApplicationParam> boxAppParams;
    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<Visit> boxVisits;
    private Box<Form> boxForms;
    private Box<Module> boxModules;

    private List<String> selectedModules = new ArrayList<>();

    private ActivityResultLauncher<Intent> onVisitEditLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        //after calling details activity to edit a collected odk form
        fireOnVisitEdited();
    });

    public ShowVisitCollectedDataFragment() {
        // Required empty public constructor
        initBoxes();
        loggedUser = Bootstrap.getCurrentUser();
    }

    public ShowVisitCollectedDataFragment(ActionListener listener) {
        this();
        this.actionListener = listener;
    }

    public static ShowVisitCollectedDataFragment newInstance(ActionListener listener){
        ShowVisitCollectedDataFragment fragment = new ShowVisitCollectedDataFragment(listener);
        fragment.loggedUser = Bootstrap.getCurrentUser();

        return fragment;
    }

    private void fireOnVisitEdited(){
        if (this.actionListener != null) {
            this.actionListener.onVisitEdited();
        }
    }

    private void fireOnVisitsLoaded() {
        if (this.actionListener != null) {
            this.actionListener.onVisitsLoaded();
        }
    }

    private void fireOnViewsCreated() {
        if (this.actionListener != null) {
            this.actionListener.onVisitCollectedViewsCreated(this);
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.show_visit_collected_list, container, false);

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
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxVisits = ObjectBoxDatabase.get().boxFor(Visit.class);
        this.boxModules = ObjectBoxDatabase.get().boxFor(Module.class);
    }

    private void initialize(View view) {

        txtCollectedDataFilter = view.findViewById(R.id.txtCollectedDataFilter);
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

        //this.showCollectedData();
        this.isRegionHeadSupported = Queries.isRegionHeadSupported(boxAppParams);
    }

    private void filterCollectedData(String text){
        if (text != null){

            ShowVisitCollectedDataAdapter adapter = (ShowVisitCollectedDataAdapter) this.lvCollectedForms.getAdapter();
            if (adapter != null) {
                adapter.filterSubjects(text);
            }
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

        new ShowCollectedDataTask().execute();
    }

    public void reloadCollectedData(){
        showCollectedData();
    }

    private void onCollectedDataItemClicked(int position) {
        ShowVisitCollectedDataAdapter adapter = (ShowVisitCollectedDataAdapter) this.lvCollectedForms.getAdapter();
        CoreCollectedDataItem dataItem = adapter.getItem(position);

        if (dataItem.household != null) {
            ShowHouseholdTask task = new ShowHouseholdTask(dataItem.household);
            task.execute();
            showLoadingDialog(getString(R.string.loading_dialog_household_details_lbl), true);
        }
    }

    private List<CoreCollectedDataItem> getAllCollectedData() {
        List<CoreCollectedDataItem> list = new ArrayList<>();
        List<CoreCollectedData> listc = this.boxCoreCollectedData.query(CoreCollectedData_.formEntity.equal(CoreFormEntity.VISIT.code))
                                                                .orderDesc(CoreCollectedData_.createdDate).order(CoreCollectedData_.formEntityCode).build().find();

        for (CoreCollectedData cdata : listc) {


            //with retaining not uploaded data in the system after an upload - this would generate a bug
            //so we will not put CoreCollectedData here without associated data
            Visit visit = this.boxVisits.get(cdata.visitId);

            if (visit != null) {
                Household household = this.boxHouseholds.query(Household_.code.equal(visit.householdCode)).build().findFirst();

                if (household != null) {
                    List<CoreFormEntity> collectedForms = getCollectedFormEntityList(visit);
                    list.add(new CoreCollectedDataItem(cdata, household, collectedForms));
                }
            }
        }

        return list;
    }

    private List<CoreFormEntity> getCollectedFormEntityList(Visit visit) {
        List<CoreFormEntity> formEntities = new ArrayList<>();

        //Ignore Change Region Head if not supported
        if (isRegionHeadSupported) {
            formEntities = this.boxCoreCollectedData.query(CoreCollectedData_.visitId.equal(visit.id)).build().find().stream().map(CoreCollectedData::getFormEntity).collect(Collectors.toList());
        } else {
            formEntities = this.boxCoreCollectedData.query(CoreCollectedData_.visitId.equal(visit.id).and(CoreCollectedData_.formEntity.notEqual(CoreFormEntity.CHANGE_REGION_HEAD.code)))
                    .build().find().stream().map(CoreCollectedData::getFormEntity).collect(Collectors.toList());
        }


        return formEntities;
    }

    private void showLoadingDialog(String msg, boolean show){
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.dismiss();
        }
    }

    class ShowCollectedDataTask extends AsyncTask<Void, Void, ShowVisitCollectedDataAdapter> {

        @Override
        protected ShowVisitCollectedDataAdapter doInBackground(Void... voids) {
            if (lvCollectedForms == null) return null;

            List<CoreCollectedDataItem> list = getAllCollectedData();

            ShowVisitCollectedDataAdapter adapter = new ShowVisitCollectedDataAdapter(getContext(), list, new ShowVisitCollectedDataAdapter.OnItemActionListener() {
                @Override
                public void onInfoButtonClicked(CoreCollectedDataItem collectedData) {
                    Log.d("collected", ""+collectedData);
                }

                @Override
                public void onCheckedStatusChanged(int position, boolean checkedStatus, boolean allChecked, boolean anyChecked) {

                }
            });

            return adapter;
        }

        @Override
        protected void onPostExecute(ShowVisitCollectedDataAdapter adapter) {
            super.onPostExecute(adapter);

            if (adapter != null) {
                lvCollectedForms.setAdapter(adapter);
                fireOnVisitsLoaded();
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

            Intent intent = new Intent(ShowVisitCollectedDataFragment.this.getContext(), HouseholdDetailsActivity.class);
            intent.putExtra("household", household.id);

            showLoadingDialog(null, false);

            onVisitEditLauncher.launch(intent);
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
        void onVisitEdited();

        void onVisitsLoaded();

        void onVisitCollectedViewsCreated(ShowVisitCollectedDataFragment fragment);
    }
}
