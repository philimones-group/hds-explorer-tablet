package org.philimone.hds.explorer.main.sync;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import io.objectbox.Box;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.CoreCollectedDataArrayAdapter;
import org.philimone.hds.explorer.adapter.HouseholdArrayAdapter;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.io.SyncUploadEntitiesTask;
import org.philimone.hds.explorer.io.UploadResponse;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Visit;

import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SyncUploadPanelFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SyncUploadPanelFragment extends Fragment implements SyncUploadEntitiesTask.Listener{

    private String username;
    private String password;
    private String serverUrl;

    private Button btUploadAllData;
    private ListView collectedDataListView;

    private Box<CoreCollectedData> boxCoreCollectedData;
    //private Box<Region> boxRegions;
    //private Box<Household> boxHouseholds;
    //private Box<Visit> boxVisits;

    public SyncUploadPanelFragment() {
        // Required empty public constructor
        initBoxes();
    }

    public static SyncUploadPanelFragment newInstance(String username, String password, String serverUrl) {
        SyncUploadPanelFragment fragment = new SyncUploadPanelFragment();

        fragment.username = username;
        fragment.password = password;
        fragment.serverUrl = serverUrl;

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

        this.btUploadAllData.setOnClickListener(v -> {
            onUploadAllButtonClicked();
        });

        this.collectedDataListView.setOnItemClickListener((parent, view1, position, id) -> {
            CoreCollectedDataArrayAdapter adapter = (CoreCollectedDataArrayAdapter) collectedDataListView.getAdapter();

            //check or uncheck
            adapter.setCheckedOrUnchecked(position);
        });

        loadCollectedData();
    }

    private void loadCollectedData() {

        List<CoreCollectedData> dataList = this.boxCoreCollectedData.getAll();
        CoreCollectedDataArrayAdapter adapter = new CoreCollectedDataArrayAdapter(this.getContext(), dataList);

        this.collectedDataListView.setAdapter(adapter);
    }

    private void onUploadAllButtonClicked() {
        CoreCollectedDataArrayAdapter adapter = (CoreCollectedDataArrayAdapter) collectedDataListView.getAdapter();

        Log.d("selected", ""+adapter.getSelectedCollectedData().size());

        for (CoreCollectedData collectedData : adapter.getSelectedCollectedData()) {
            uploadCollectedData(collectedData);
        }

        collectedDataListView.invalidateViews();
    }

    private void uploadCollectedData(CoreCollectedData coreCollectedData){

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
    }
}