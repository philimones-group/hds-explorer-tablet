package org.philimone.hds.explorer.main.sync;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.io.SyncEntitiesListener;
import org.philimone.hds.explorer.io.SyncEntity;
import org.philimone.hds.explorer.io.SyncEntityReport;
import org.philimone.hds.explorer.io.SyncEntityResult;
import org.philimone.hds.explorer.io.SyncState;
import org.philimone.hds.explorer.io.SyncStatus;
import org.philimone.hds.explorer.widget.SyncResultDialog;

import java.util.List;

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
    private ProgressBar syncProgressBar;
    private TextView syncProgressText;
    private TextView syncSyncedDate;
    private TextView syncProgressMessage;
    private ImageView syncErrorIcon;

    private String titleText = "";

    private SyncPanelItemListener listener;

    private SyncResultDialog syncResultDialog;
    private SyncEntityResult syncResult;

    public SyncPanelItemFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.     *
     */
    public static SyncPanelItemFragment newInstance(String titleText) {
        SyncPanelItemFragment fragment = new SyncPanelItemFragment();

        fragment.titleText = titleText;

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.synchronization_panel_item, container, false);

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
        this.syncProgressBar = (ProgressBar) view.findViewById(R.id.syncProgressBar);
        this.syncProgressText = (TextView) view.findViewById(R.id.syncProgressText);
        this.syncSyncedDate = (TextView) view.findViewById(R.id.syncSyncedDate);
        this.syncProgressMessage = (TextView) view.findViewById(R.id.syncProgressMessage);
        this.syncErrorIcon = (ImageView) view.findViewById(R.id.syncErrorIcon);

        this.syncButton.setOnClickListener(this);
        this.syncStopButton.setOnClickListener(this);
        this.syncDetails.setOnClickListener(this);

        this.syncResultDialog = new SyncResultDialog(this.getActivity());
        this.syncResultDialog.create();

        cleanProgress();
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

    private void onSyncButtonClicked() {
        //gone to sync and visible stop
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
        int s = (int)size;

        Log.d("sync-started", syncEntity.name()+", size="+size+", s="+s);
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

        Log.d("errors", "has="+hasError+", err_msg="+errorMessage);

        this.syncResult = new SyncEntityResult(result, downloadReports, persistedReports, hasError, errorMessage);

        this.syncProgressMessage.setText(result);
        //Find a way to show the reports

        //Make sync button visible
        //gone to sync and visible stop
        //this.syncStopButton.setVisibility(View.GONE);
        this.syncButton.setVisibility(View.VISIBLE);

        this.listener.onSyncFinished(this);
    }

    interface SyncPanelItemListener {

        void onSyncStartButtonClicked(SyncPanelItemFragment syncPanelItem);

        void onSyncStopButtonClicked(SyncPanelItemFragment syncPanelItem);

        void onSyncFinished(SyncPanelItemFragment syncPanelItem);

    }

}