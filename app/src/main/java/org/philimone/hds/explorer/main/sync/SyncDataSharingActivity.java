package org.philimone.hds.explorer.main.sync;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.ConnectionArrayAdapter;
import org.philimone.hds.explorer.adapter.SharingDeviceArrayAdapter;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.io.datasharing.ClientAdapter;
import org.philimone.hds.explorer.io.datasharing.ClientAdapterListener;
import org.philimone.hds.explorer.io.datasharing.ConnectionType;
import org.philimone.hds.explorer.io.datasharing.DataSharingTask;
import org.philimone.hds.explorer.io.datasharing.ServerAdapter;
import org.philimone.hds.explorer.io.datasharing.ServerAdapterListener;
import org.philimone.hds.explorer.io.datasharing.SharedData;
import org.philimone.hds.explorer.io.datasharing.SharingDevice;
import org.philimone.hds.explorer.io.datasharing.bluetooth.BluetoothClientAdapter;
import org.philimone.hds.explorer.io.datasharing.bluetooth.BluetoothServerAdapter;
import org.philimone.hds.explorer.io.datasharing.wifi.TcpIpClientAdapter;
import org.philimone.hds.explorer.io.datasharing.wifi.TcpIpServerAdapter;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;

import java.io.IOException;
import java.util.Map;

import io.objectbox.Box;

public class SyncDataSharingActivity extends AppCompatActivity implements DataSharingTask.Listener{

    private Spinner spnConnections;
    private Button btServerStart;
    private Button btClientStart;
    private TextView txtConnectionAction;
    private ListView lvConnectedDevs;
    private Button btShareData;

    private ProgressBar pBarShareProgress;
    private TextView txtSharePercentageMsg;
    private TextView txtShareProgressMsg;
    private TextView txtRecordsToShare;
    private TextView txtSharedRecords;

    private ServerAdapter serverAdapter = null;
    private ClientAdapter clientAdapter = null;
    private BluetoothServerAdapter bluetoothServerAdapter = null;
    private BluetoothClientAdapter bluetoothClientAdapter = null;
    private TcpIpServerAdapter tcpIpServerAdapter = null;
    private TcpIpClientAdapter tcpIpClientAdapter = null;

    private SharingDeviceArrayAdapter connectedDevicesArrayAdapter;

    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;

    private String deviceId = null; /* need to be retrieved */
    private boolean serverStarted;
    private boolean clientStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sync_data_sharing);

        preventStrictModeBlock();

        initBoxes();
        initialize();
    }

    private void preventStrictModeBlock() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private void initBoxes() {
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        stopDeviceConnections();
    }

    private void initialize() {

        initDeviceAdapters();

        this.spnConnections = findViewById(R.id.spnConnections);
        this.btServerStart = findViewById(R.id.btServerStart);
        this.btClientStart = findViewById(R.id.btClientStart);
        this.txtConnectionAction = findViewById(R.id.txtConnectionAction);
        this.lvConnectedDevs = findViewById(R.id.lvConnectedDevs);
        this.btShareData = findViewById(R.id.btShareData);
        this.pBarShareProgress = findViewById(R.id.pBarShareProgress);
        this.txtSharePercentageMsg = findViewById(R.id.txtSharePercentageMsg);
        this.txtShareProgressMsg = findViewById(R.id.txtShareProgressMsg);
        this.txtRecordsToShare = findViewById(R.id.txtRecordsToShare);
        this.txtSharedRecords = findViewById(R.id.txtSharedRecords);

        this.updateConnectionAction(R.string.data_sharing_msg_conn_not_init_lbl, false);
        this.btShareData.setEnabled(false);

        this.btServerStart.setOnClickListener(v -> {
            startServerConnection();
        });

        this.btClientStart.setOnClickListener(v -> {
            startClientConnection();
        });

        this.btShareData.setOnClickListener(v -> {
            onSharedDataClicked();
        });

        this.spnConnections.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onSelectedConnectionType();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        loadConnectionTypes();

        //set adapter to lvConnectedDevs
        this.connectedDevicesArrayAdapter = new SharingDeviceArrayAdapter(this);
        this.lvConnectedDevs.setAdapter(this.connectedDevicesArrayAdapter);

        enableButtons();

        this.pBarShareProgress.setProgress(0);
        this.txtSharePercentageMsg.setText("0%");
        this.txtShareProgressMsg.setText("");
        this.txtSharedRecords.setText("0");
        this.txtRecordsToShare.setText(""+getDataToShareCount());
    }

    private void initDeviceAdapters() {
        this.bluetoothServerAdapter = new BluetoothServerAdapter(this, getUsername(), this.serverAdapterListener);
        this.bluetoothClientAdapter = new BluetoothClientAdapter(this, getUsername(), this.clientAdapterListener);
        this.tcpIpServerAdapter = new TcpIpServerAdapter(this, getUsername(), this.serverAdapterListener);
        this.tcpIpClientAdapter = new TcpIpClientAdapter(this, getUsername(), this.clientAdapterListener);
    }

    private void stopDeviceConnections() {
        if (serverAdapter != null) {
            serverAdapter.stopListening();
        }
        if (clientAdapter != null) {
            clientAdapter.stopScanning();
        }
    }

    private void loadConnectionTypes() {
        this.spnConnections.setAdapter(null);

        ConnectionArrayAdapter adapter = new ConnectionArrayAdapter(this);
        this.spnConnections.setAdapter(adapter);
    }

    private void onSelectedConnectionType() {
        enableButtons();
    }


    private void enableButtons() {
        ConnectionType connectionType = getSelectedConnectionType();

        if (connectionType == null) {
            btServerStart.setEnabled(false);
            btClientStart.setEnabled(false);
        } else if (connectionType == ConnectionType.BLUETOOTH) {
            btServerStart.setEnabled(this.bluetoothServerAdapter.isAvailable());
            btClientStart.setEnabled(this.bluetoothClientAdapter.isAvailable());
        } else if (connectionType == ConnectionType.TCP_IP) {
            btServerStart.setEnabled(this.tcpIpServerAdapter.isAvailable());
            btClientStart.setEnabled(this.tcpIpClientAdapter.isAvailable());
        }
    }

    private int getDataToShareCount() {
        int c = 0;

        c += this.boxRegions.query(Region_.shareable.equal(true)).build().count();
        c += this.boxHouseholds.query(Household_.shareable.equal(true)).build().count();

        return c;
    }

    private ConnectionType getSelectedConnectionType() {
        try {
            ConnectionArrayAdapter adapter = (ConnectionArrayAdapter) this.spnConnections.getAdapter();
            ConnectionType connectionType = adapter.getItem(this.spnConnections.getSelectedItemPosition());
            return connectionType;
        }catch (Exception ex) {
            return null;
        }
    }

    private String getUsername() {
        User user = Bootstrap.getCurrentUser();

        return user.getFullName();
    }

    private void startServerConnection() {
        ConnectionType connectionType = getSelectedConnectionType();

        if (connectionType == ConnectionType.BLUETOOTH) {
            this.serverAdapter = this.bluetoothServerAdapter;

            if (serverAdapter.isAvailable()) {
                if (!serverAdapter.isEnabled()) {
                    serverAdapter.enable();
                } else {
                    startServerListening();
                }
            } else {
                updateConnectionAction(R.string.data_sharing_device_btt_not_found_lbl, true);
                btShareData.setEnabled(false);
            }
        } else if (connectionType == ConnectionType.TCP_IP) {
            this.serverAdapter = this.tcpIpServerAdapter;

            if (serverAdapter.isAvailable()) {
                if (!serverAdapter.isEnabled()) {
                    serverAdapter.enable();
                } else {
                    startServerListening();
                }
            } else {
                updateConnectionAction(R.string.data_sharing_device_tcp_not_found_lbl, true);
                btShareData.setEnabled(false);
            }
        }
    }

    private void startClientConnection() {
        ConnectionType connectionType = getSelectedConnectionType();

        if (connectionType == ConnectionType.BLUETOOTH) {
            this.clientAdapter = this.bluetoothClientAdapter;

            if (this.clientAdapter.isAvailable()) {
                if (!this.clientAdapter.isEnabled()) {
                    this.clientAdapter.enable();
                } else {
                    Log.d("bluetooth", "enabled");
                    startClientScanning();
                }
            } else {
                updateConnectionAction(R.string.data_sharing_device_btt_not_found_lbl, true);
                btShareData.setEnabled(false);
            }
        } else if (connectionType == ConnectionType.TCP_IP) {
            this.clientAdapter = this.tcpIpClientAdapter;

            if (this.clientAdapter.isAvailable()) {
                if (!this.clientAdapter.isEnabled()) {
                    this.clientAdapter.enable();
                } else {
                    Log.d("tcp/ip", "enabled");
                    startClientScanning();
                }
            } else {
                updateConnectionAction(R.string.data_sharing_device_tcp_not_found_lbl, true);
                btShareData.setEnabled(false);
            }
        }
    }

    private void startServerListening() {
        serverAdapter.startListening();
        this.serverStarted = true;
        this.clientStarted = false;

        this.btServerStart.setEnabled(false);
        this.btClientStart.setEnabled(false);
    }

    private void startClientScanning() {
        this.clientAdapter.startScanning();
        this.clientStarted = true;
        this.serverStarted = false;

        this.btServerStart.setEnabled(false);
        this.btClientStart.setEnabled(false);
    }

    private void onSharedDataClicked() {
        if (this.connectedDevicesArrayAdapter.getCount() > 0) {

            this.btShareData.setEnabled(false);

            this.pBarShareProgress.setIndeterminate(true);
            this.txtSharePercentageMsg.setText("0%");
            this.txtShareProgressMsg.setText(R.string.data_sharing_transferring_data_lbl);

            if (this.serverStarted) {
                try {
                    for (SharingDevice device : this.connectedDevicesArrayAdapter.getDevicesList()) {
                        device.getSharingTask().sendCommand(DataSharingTask.Command.POST_SHARED_DATA);
                    }

                    onFinishedDataSharing();

                } catch (IOException ex) {
                    ex.printStackTrace();

                    onFailureDataSharing();
                }

            } else if (this.clientStarted) {
                try {
                    for (SharingDevice device : this.connectedDevicesArrayAdapter.getDevicesList()) {
                        device.getSharingTask().sendCommand(DataSharingTask.Command.GET_SHARED_DATA);
                    }

                    onFinishedDataSharing();
                } catch (IOException ex) {
                    ex.printStackTrace();

                    onFailureDataSharing();
                }
            }


        }
    }

    private void onDeviceConnecting() {
        updateConnectionAction(R.string.data_sharing_device_connecting_lbl, false);
    }

    private void onDeviceConnected(SharingDevice remoteDevice) {
        //create a DataSharingTask

        DataSharingTask task = new DataSharingTask(this, remoteDevice, this);
        //task.execute();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        Log.d("device", "connected "+remoteDevice+ ", server = "+remoteDevice.isServer());

        this.connectedDevicesArrayAdapter.addDevice(remoteDevice);
        this.btShareData.setEnabled(serverStarted); //only server can start sharing data
    }

    @Override
    public void onDataSharingTaskStarted() {
        //the task is initiated and started listening to inputstream
        Log.d("task", "started");
        for (SharingDevice device : this.connectedDevicesArrayAdapter.getDevicesList()) {
            DataSharingTask task = device.getSharingTask();

            try {
                task.sendCommand(DataSharingTask.Command.GET_DATA_INFO);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPostDataInfo(SharingDevice device, Map<String, String> contentInfo) {
        //update device info
        Log.d("post data info", contentInfo.entrySet().toString());
        this.connectedDevicesArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPostSharedData(SharingDevice device, SharedData data) {

        onFinishedDataSharing();
        this.txtSharedRecords.setText(""+data.countSharedData());
    }

    private void onFinishedDataSharing() {
        this.pBarShareProgress.setIndeterminate(false);
        this.pBarShareProgress.setProgress(100);
        this.txtSharePercentageMsg.setText("100%");
        this.txtShareProgressMsg.setText(R.string.data_sharing_data_shared_lbl);

        this.btShareData.setEnabled(serverStarted);
    }

    private void onFailureDataSharing() {
        this.pBarShareProgress.setIndeterminate(false);
        this.pBarShareProgress.setProgress(0);
        this.txtSharePercentageMsg.setText("0%");
        this.txtShareProgressMsg.setText(R.string.data_sharing_transferring_failure_lbl);

        this.btShareData.setEnabled(serverStarted);
    }
    
    private void updateConnectionAction(@StringRes int text, boolean enableButtons) {
        this.txtConnectionAction.setText(text);

        this.btServerStart.setEnabled(enableButtons);
        this.btClientStart.setEnabled(enableButtons);
    }

    private ServerAdapterListener serverAdapterListener = new ServerAdapterListener() {
        @Override
        public void onDeviceEnabled(boolean enabled) {
            if (enabled) {
                startServerListening();
            } else {
                ConnectionType connectionType = getSelectedConnectionType();
                if (connectionType == ConnectionType.BLUETOOTH) {
                    updateConnectionAction(R.string.data_sharing_device_btt_disabled_lbl, false);
                } else if (connectionType == ConnectionType.TCP_IP) {
                    updateConnectionAction(R.string.data_sharing_device_tcp_disabled_lbl, false);
                }
            }
        }

        @Override
        public void onDeviceConnected(SharingDevice remoteDevice) {
            SyncDataSharingActivity.this.onDeviceConnected(remoteDevice);
        }

        @Override
        public void onServerListening() {
            updateConnectionAction(R.string.data_sharing_server_listening_lbl, false);
        }

        @Override
        public void onServerStoppedListening() {
            updateConnectionAction(R.string.data_sharing_server_stopped_listening_lbl, true);
            btServerStart.setEnabled(true);
            btClientStart.setEnabled(true);
        }
    };

    private ClientAdapterListener clientAdapterListener = new ClientAdapterListener() {
        @Override
        public void onDeviceEnabled(boolean enabled) {
            if (enabled) {
                startClientScanning();
            } else {
                ConnectionType connectionType = getSelectedConnectionType();

                if (connectionType == ConnectionType.BLUETOOTH) {
                    updateConnectionAction(R.string.data_sharing_device_btt_disabled_lbl, false);
                } else if (connectionType == ConnectionType.TCP_IP) {
                    updateConnectionAction(R.string.data_sharing_device_tcp_disabled_lbl, false);
                }
            }
        }

        @Override
        public void onDeviceConnecting() {
            SyncDataSharingActivity.this.onDeviceConnecting();
        }

        @Override
        public void onDeviceConnected(SharingDevice device) {
            updateConnectionAction(R.string.data_sharing_device_connected_lbl, false);
            SyncDataSharingActivity.this.onDeviceConnected(device);
        }

        @Override
        public void onDeviceConnectionFailure() {
            updateConnectionAction(R.string.data_sharing_device_connection_failure_lbl, true);
        }

        @Override
        public void onScanningDevices() {
            Log.d("device", "scanning started");
            updateConnectionAction(R.string.data_sharing_scan_for_servers_lbl, false);
        }

        @Override
        public void onScanningDevicesFinished() {
            updateConnectionAction(R.string.data_sharing_scanning_stopped_lbl, true);
        }
    };

}