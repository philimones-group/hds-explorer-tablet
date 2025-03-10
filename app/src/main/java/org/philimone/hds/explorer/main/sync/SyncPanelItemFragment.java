package org.philimone.hds.explorer.main.sync;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.io.SyncEntitiesListener;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.enums.SyncEntity;
import org.philimone.hds.explorer.io.SyncEntityReport;
import org.philimone.hds.explorer.io.SyncEntityResult;
import org.philimone.hds.explorer.model.enums.SyncState;
import org.philimone.hds.explorer.model.enums.SyncStatus;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.SyncResultDialog;
import io.objectbox.Box;

import java.util.List;

import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SyncPanelItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SyncPanelItemFragment extends Fragment implements View.OnClickListener, SyncEntitiesListener {

    private TextView syncTitleText;
    private Button syncButton;
    private Button syncStopButton;
    private Button syncDetails;
    private LinearProgressIndicator syncProgressBar;
    private TextView syncProgressText;
    private TextView syncSyncedDate;
    private TextView syncProgressMessage;
    private ImageView syncErrorIcon;

    private String titleText = "";

    private SyncPanelItemListener listener;

    private SyncResultDialog syncResultDialog;
    private SyncEntityResult syncResult;

    private boolean connectedToServer;
    private boolean hasDataToUpload;

    private Box<ApplicationParam> boxParams;

    public SyncPanelItemFragment() {
        this.boxParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.     *
     */
    public static SyncPanelItemFragment newInstance(String titleText, boolean connectedToServer, boolean hasDataToUpload) {
        SyncPanelItemFragment fragment = new SyncPanelItemFragment();
        fragment.connectedToServer = connectedToServer;
        fragment.hasDataToUpload = hasDataToUpload;

        fragment.titleText = titleText;

        return fragment;
    }

    public void setHasDataToUpload(boolean hasDataToUpload) {
        this.hasDataToUpload = hasDataToUpload;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.sync_download_panel_item, container, false);

        initialize(view);

        return view;
    }

    private void initialize(View view) {

        if (getActivity() instanceof SyncPanelItemListener){
            this.listener = (SyncPanelItemListener) getActivity();
        }

        //Log.d("set listener", ""+this.listener);

        this.syncTitleText = (TextView) view.findViewById(R.id.syncTitleText);
        this.syncButton = (Button) view.findViewById(R.id.syncButton);
        this.syncStopButton = (Button) view.findViewById(R.id.syncButton);
        this.syncDetails = (Button) view.findViewById(R.id.syncDetails);
        this.syncProgressBar = view.findViewById(R.id.syncProgressBar);
        this.syncProgressText = (TextView) view.findViewById(R.id.syncProgressText);
        this.syncSyncedDate = (TextView) view.findViewById(R.id.syncEntityMsg);
        this.syncProgressMessage = (TextView) view.findViewById(R.id.syncProgressMessage);
        this.syncErrorIcon = (ImageView) view.findViewById(R.id.syncErrorIcon);

        this.syncButton.setOnClickListener(this);
        this.syncStopButton.setOnClickListener(this);
        this.syncDetails.setOnClickListener(this);

        this.syncResultDialog = new SyncResultDialog(this.getActivity());
        this.syncResultDialog.create();

        cleanProgress();

        refreshSyncButton();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.syncButton: onSyncButtonClicked(); break;
            case R.id.syncStopButton: onSyncStopButtonClicked(); break;
            case R.id.syncDetails: onSyncDetailsButtonClicked(); break;
            default: break;
        }
    }

    public void cleanProgress() {
        this.syncTitleText.setText(titleText);
        this.syncProgressText.setText("0%");
        this.syncSyncedDate.setText("");
        this.syncProgressMessage.setText("");
        this.syncProgressBar.setProgress(0);

        this.syncProgressText.setVisibility(View.VISIBLE);
        this.syncErrorIcon.setVisibility(View.GONE);
        this.syncStopButton.setVisibility(View.GONE);
        this.syncButton.setVisibility(View.VISIBLE);

        this.syncResult = null;
    }

    public void resetSyncButton(){
        this.syncButton.setVisibility(View.VISIBLE);
        this.syncStopButton.setVisibility(View.GONE);
    }

    public void refreshSyncButton(){
        syncButton.setEnabled(boxParams.count() > 0);
    }

    public void setTitleText(String title){
        this.titleText = title;

        if (this.syncTitleText != null){
            this.syncTitleText.setText(titleText);
        }
    }

    public void setSyncedDate(String statusMessage, SyncStatus status) {
        if (this.syncSyncedDate != null){
            this.syncSyncedDate.setText(statusMessage);

            if (status == SyncStatus.STATUS_SYNC_ERROR){
                Log.d("syncbutton", "error appearing");
                this.syncErrorIcon.setVisibility(View.VISIBLE);
                this.syncProgressText.setVisibility(View.GONE);
                this.syncProgressMessage.setText("");
            } else {
                this.syncErrorIcon.setVisibility(View.GONE);
                this.syncProgressText.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setListener(SyncPanelItemListener listener) {
        this.listener = listener;
    }

    public SyncEntityResult getSyncResult() {
        return syncResult;
    }

    public void setSyncResult(SyncEntityResult syncResult) {
        this.syncResult = syncResult;
    }

    private void onSyncButtonClicked() {
        //evaluate the sync
        if (!connectedToServer) { //logged in locally only
            DialogFactory.createMessageInfo(this.getContext(), R.string.server_sync_offline_mode_title_lbl, R.string.server_sync_offline_mode_msg_lbl).show();
            return;
        }

        //check if there is data to upload? - ask: do you want to proceed
        if (hasDataToUpload){
            DialogFactory.createMessageInfo(this.getContext(), R.string.server_sync_warning_upload_required_lbl, R.string.server_sync_warning_upload_required_msg_lbl, new DialogFactory.OnClickListener() {
                @Override
                public void onClicked(DialogFactory.Buttons clickedButton) {

                }
            }).show();
        } else {
            executeSyncButtonEvent();
        }
    }

    private void executeSyncButtonEvent() {
        Log.d("cleaning", "cleaning and execute");
        cleanProgress();
        this.syncButton.setVisibility(View.GONE);
        this.syncStopButton.setVisibility(View.VISIBLE);
        listener.onSyncStartButtonClicked(this);
    }

    private void onSyncStopButtonClicked() {
        listener.onSyncStopButtonClicked(this);
    }

    private void onSyncDetailsButtonClicked() {

        if (syncResult != null){
            //create a dialog reporting results or errors

            syncResultDialog.clean();
            syncResultDialog.setSyncResult(syncResult);
            syncResultDialog.doLayout();
            syncResultDialog.show();
        }
    }

    @Override
    public void onSyncCreated() {
        cleanProgress();
    }

    @Override
    public void onSyncStarted(SyncEntity syncEntity, SyncState syncState, long size) {
        if (syncEntity == null) return;

        int s = (int)size;

        if (size == 0) {
            getActivity().runOnUiThread(() -> this.syncProgressBar.setIndeterminate(true));
        } else {
            getActivity().runOnUiThread(() -> this.syncProgressBar.setIndeterminate(false));
        }

        Log.d("sync-started", syncEntity.name()+", size="+size+", s="+s);

        getActivity().runOnUiThread(() ->  cleanProgress());

        this.syncProgressBar.setMax(s);
        //setSyncedDate("");
    }

    @Override
    public void onSyncProgressUpdate(Integer progress, String progressText) {

        int max = this.syncProgressBar.getMax();

        //calculate percentage
        double p = max==0 ? 0 : (Double.valueOf(progress) / Double.valueOf(max));
        long result = Math.round(p * 100);

        //Log.d("double", "d.v.p="+Double.valueOf(progress)+", d.v.max="+Double.valueOf(max));
        //Log.d("progress", "p="+progress+", max="+this.syncProgressBar.getMax()+", result="+result);

        this.syncProgressBar.setProgress(progress);
        this.syncProgressText.setText(result+"%");
        this.syncProgressMessage.setText(progressText);

    }

    @Override
    public void onSyncFinished(String result, List<SyncEntityReport> downloadReports, List<SyncEntityReport> persistedReports, Boolean hasError, String errorMessage) {

        Log.d("errors", "has="+hasError+", result="+result+", err_msg="+errorMessage);


        this.syncResult = new SyncEntityResult(result, downloadReports, persistedReports, hasError, errorMessage);

        this.syncProgressMessage.setText(result);
        //Find a way to show the reports

        if (!hasError) { //set 100%
            syncProgressBar.setMax(100);
            onSyncProgressUpdate(syncProgressBar.getMax(), result);
        }

        getActivity().runOnUiThread(() -> this.syncProgressBar.setIndeterminate(false));

        //Make sync button visible
        //this.syncStopButton.setVisibility(View.GONE);
        this.syncButton.setVisibility(View.VISIBLE);

        this.listener.onSyncFinished(this, this.syncResult);
    }

    interface SyncPanelItemListener {

        void onSyncStartButtonClicked(SyncPanelItemFragment syncPanelItem);

        void onSyncStopButtonClicked(SyncPanelItemFragment syncPanelItem);

        void onSyncFinished(SyncPanelItemFragment syncPanelItem, SyncEntityResult syncEntityResult);

    }

}