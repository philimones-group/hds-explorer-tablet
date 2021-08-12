package org.philimone.hds.explorer.main.sync;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.StringUtil;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.CoreCollectedDataArrayAdapter;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.io.SyncUploadEntitiesTask;
import org.philimone.hds.explorer.io.UploadEntityReport;
import org.philimone.hds.explorer.io.UploadEntityResult;
import org.philimone.hds.explorer.io.UploadResponse;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.UploadResultDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SyncUploadPanelFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SyncUploadPanelFragment extends Fragment implements SyncUploadEntitiesTask.Listener, CoreCollectedDataArrayAdapter.OnItemActionListener {

    private String username;
    private String password;
    private String serverUrl;
    private boolean connectedToServer;

    private Button btUploadAllData;
    private ListView collectedDataListView;
    private ProgressBar syncProgressBar;
    private TextView syncProgressText;
    private TextView syncProgressMessage;
    private TextView lastSyncedDate;
    private TextView txtUploadedSuccess;
    private TextView txtUploadedError;
    private TextView txtNotUploaded;
    private Switch schNotUploaded;
    private Switch schUploaded;
    private Switch schUploadedErrors;
    private CheckBox chkSelectAll;

    private UploadResultDialog uploadResultDialog;

    private Box<CoreCollectedData> boxCoreCollectedData;
    //private Box<Region> boxRegions;
    //private Box<Household> boxHouseholds;
    //private Box<Visit> boxVisits;

    public SyncUploadPanelFragment() {
        // Required empty public constructor
        initBoxes();
    }

    public static SyncUploadPanelFragment newInstance(String username, String password, String serverUrl, boolean connectedToServer) {
        SyncUploadPanelFragment fragment = new SyncUploadPanelFragment();

        fragment.username = username;
        fragment.password = password;
        fragment.serverUrl = serverUrl;
        fragment.connectedToServer = connectedToServer;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.sync_upload_panel, container, false);

        initialize(view);

        return view;
    }

    private void initBoxes() {
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
        //this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        //this.boxVisits = ObjectBoxDatabase.get().boxFor(Visit.class);
        //this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
    }

    private void initialize(View view) {
        this.btUploadAllData = view.findViewById(R.id.btUploadAllData);
        this.collectedDataListView = view.findViewById(R.id.collectedDataListView);
        this.syncProgressBar = (ProgressBar) view.findViewById(R.id.syncProgressBar);
        this.syncProgressText = (TextView) view.findViewById(R.id.syncProgressText);
        this.syncProgressMessage = (TextView) view.findViewById(R.id.syncProgressMessage);
        this.lastSyncedDate = view.findViewById(R.id.lastSyncedDate);
        this.txtUploadedSuccess = view.findViewById(R.id.txtUploadedSuccess);
        this.txtUploadedError = view.findViewById(R.id.txtUploadedError);
        this.txtNotUploaded = view.findViewById(R.id.txtNotUploaded);
        this.schNotUploaded = view.findViewById(R.id.schNotUploaded);
        this.schUploaded = view.findViewById(R.id.schUploaded);
        this.schUploadedErrors = view.findViewById(R.id.schUploadedErrors);
        this.chkSelectAll = view.findViewById(R.id.chkSelectAll);

        this.uploadResultDialog = new UploadResultDialog(this.getActivity());
        this.uploadResultDialog.create();

        this.btUploadAllData.setOnClickListener(v -> {
            onUploadAllButtonClicked();
        });

        this.collectedDataListView.setOnItemClickListener((parent, view1, position, id) -> {
            CoreCollectedDataArrayAdapter adapter = (CoreCollectedDataArrayAdapter) collectedDataListView.getAdapter();
            //check or uncheck
            adapter.setCheckedOrUnchecked(position);
        });

        this.chkSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selectAll(isChecked);
        });

        this.schNotUploaded.setOnCheckedChangeListener((buttonView, isChecked) -> {
            loadCollectedData();
            this.chkSelectAll.setChecked(false);
        });

        this.schUploaded.setOnCheckedChangeListener((buttonView, isChecked) -> {
            loadCollectedData();
            this.chkSelectAll.setChecked(false);
        });

        this.schUploadedErrors.setOnCheckedChangeListener((buttonView, isChecked) -> {
            loadCollectedData();
            this.chkSelectAll.setChecked(false);
        });

        clearAll();
        loadCollectedData();
        loadResume();
    }

    private void clearAll() {
        this.syncProgressBar.setProgress(0);
        this.syncProgressText.setText("0 %");
        this.syncProgressMessage.setText("");

        this.lastSyncedDate.setText("");
        this.txtUploadedSuccess.setText(getString(R.string.server_sync_upload_total_lbl, 0));
        this.txtNotUploaded.setText(getString(R.string.server_sync_upload_total_lbl, 0));
        this.txtUploadedError.setText(getString(R.string.server_sync_upload_total_lbl, 0));

        selectAll(false);

        this.btUploadAllData.setEnabled(false);
    }

    private void selectAll(boolean isChecked){
        CoreCollectedDataArrayAdapter adapter = (CoreCollectedDataArrayAdapter) collectedDataListView.getAdapter();
        if (adapter != null) {
            adapter.setAllChecked(isChecked);
        }
    }

    public void loadCollectedData() {

        QueryBuilder<CoreCollectedData> builder = this.boxCoreCollectedData.query();

        boolean uploaded = schUploaded.isChecked();
        boolean notuploaded = schNotUploaded.isChecked();
        boolean uploadedErrors = schUploadedErrors.isChecked();

        //up=false,nup=false (show all - notuploaded & uploaded) -> cant exist
        //up=true,nup=false  (show uploaded)
        //up=false,nup=true  (show notuploaded)
        //up=true,nup=true   (show all)

        if (uploaded || notuploaded || uploadedErrors)
        if (uploaded == notuploaded && uploaded == true) {
            builder.equal(CoreCollectedData_.uploadedWithError, uploadedErrors).or().equal(CoreCollectedData_.uploaded, true).or().equal(CoreCollectedData_.uploaded, false);
        } else if (uploaded == notuploaded && uploaded == false) {
            if (uploadedErrors) {
                builder.equal(CoreCollectedData_.uploadedWithError, true);
            } else {
                builder.equal(CoreCollectedData_.uploadedWithError, false).equal(CoreCollectedData_.uploaded, true); //dont show anything - never true
            }
        } else {
            if (uploaded) {
                if (uploadedErrors) {
                    builder.equal(CoreCollectedData_.uploadedWithError, true).or().equal(CoreCollectedData_.uploaded, true);
                } else {
                    builder.equal(CoreCollectedData_.uploadedWithError, false).equal(CoreCollectedData_.uploaded, true);
                }
            } else  {
                if (uploadedErrors) {
                    builder.equal(CoreCollectedData_.uploaded, false).or().equal(CoreCollectedData_.uploadedWithError, true);
                } else {
                    builder.equal(CoreCollectedData_.uploaded, false);
                }
            }
        }



        List<CoreCollectedData> dataList = builder.order(CoreCollectedData_.createdDate).build().find();
        CoreCollectedDataArrayAdapter adapter = new CoreCollectedDataArrayAdapter(this.getContext(), dataList, this);

        this.collectedDataListView.setAdapter(adapter);
    }

    private void loadResume() {

        boolean noUploadsYet = this.boxCoreCollectedData.query().equal(CoreCollectedData_.uploaded, true).build().count()==0;
        CoreCollectedData lastCCD = noUploadsYet ? null : this.boxCoreCollectedData.query().orderDesc(CoreCollectedData_.uploadedDate).build().findFirst();

        String date = (lastCCD == null) ? getString(R.string.server_sync_status_notsynced_lbl) : StringUtil.format(lastCCD.uploadedDate, "yyyy-MM-dd HH:mm:ss");
        long uploaded = this.boxCoreCollectedData.query().equal(CoreCollectedData_.uploaded, true).build().count();;
        long notuploaded = this.boxCoreCollectedData.query().equal(CoreCollectedData_.uploaded, false).build().count();;
        long uploadederror = this.boxCoreCollectedData.query().equal(CoreCollectedData_.uploadedWithError, true).build().count();;

        this.lastSyncedDate.setText(date);
        this.txtUploadedSuccess.setText(getString(R.string.server_sync_upload_total_lbl, uploaded));
        this.txtNotUploaded.setText(getString(R.string.server_sync_upload_total_lbl, notuploaded));
        this.txtUploadedError.setText(getString(R.string.server_sync_upload_total_lbl, uploadederror));
    }

    private void onUploadAllButtonClicked() {

        if (!connectedToServer) { //logged in locally only
            DialogFactory.createMessageInfo(this.getContext(), R.string.server_sync_offline_mode_title_lbl, R.string.server_sync_offline_mode_msg_lbl).show();
            return;
        }

        CoreCollectedDataArrayAdapter adapter = (CoreCollectedDataArrayAdapter) collectedDataListView.getAdapter();
        int total = adapter.getSelectedCollectedData().size();

        this.syncProgressBar.setMin(0);
        this.syncProgressBar.setMax(total);

        Log.d("selected", ""+adapter.getSelectedCollectedData().size());

        for (CoreCollectedData collectedData : adapter.getSelectedCollectedData()) {
            uploadCollectedData(collectedData);
        }

        collectedDataListView.invalidateViews();
    }

    private void uploadCollectedData(CoreCollectedData coreCollectedData){

        //update progress
        String entity_lbl = getString(coreCollectedData.formEntity.name);
        this.syncProgressMessage.setText(getString(R.string.server_sync_uploading_form_lbl, entity_lbl));

        SyncUploadEntitiesTask uploadTask = new SyncUploadEntitiesTask(this.getContext(), serverUrl, username, password, coreCollectedData, this);
        uploadTask.execute();
    }

    @Override
    public void onUploadFinished(UploadResponse response, CoreCollectedData collectedData) {
        collectedData.uploaded = response.hasUploaded();
        collectedData.uploadedWithError = response.hasErrors();
        collectedData.uploadedDate = new Date();
        collectedData.uploadedError = response.getErrors();
        this.boxCoreCollectedData.put(collectedData);

        this.syncProgressMessage.setText(getString(R.string.server_sync_uploading_finished_lbl));
        this.syncProgressBar.incrementProgressBy(1);

        loadCollectedData();
        loadResume();
    }

    @Override
    public void onInfoButtonClicked(CoreCollectedData collectedData) {
        openInfoDialog(collectedData);
    }

    @Override
    public void onCheckedStatusChanged(int position, boolean checkedStatus, boolean allChecked, boolean anyChecked) {
        this.btUploadAllData.setEnabled(anyChecked);
    }

    private void openInfoDialog(CoreCollectedData collectedData){

        Log.d("clicked", ""+collectedData);

        if (collectedData.uploadedWithError) {
            List<UploadEntityReport> reports = new ArrayList<UploadEntityReport>();

            Arrays.stream(collectedData.uploadedError.split("\n")).forEach( error -> {
                reports.add(new UploadEntityReport(collectedData.formEntity, error, false));
            });

            UploadEntityResult result = new UploadEntityResult(getString(R.string.server_sync_upload_errors_result_lbl), collectedData, reports, true);

            uploadResultDialog.clean();
            uploadResultDialog.setUploadResult(result);
            uploadResultDialog.doLayout();
            uploadResultDialog.show();
        }

    }
}