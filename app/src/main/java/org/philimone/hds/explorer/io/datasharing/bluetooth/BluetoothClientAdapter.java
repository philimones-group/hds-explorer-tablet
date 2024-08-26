package org.philimone.hds.explorer.io.datasharing.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.io.datasharing.ClientAdapter;
import org.philimone.hds.explorer.io.datasharing.ClientAdapterListener;
import org.philimone.hds.explorer.io.datasharing.SharingDevice;
import org.philimone.hds.explorer.widget.DialogFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class BluetoothClientAdapter extends ClientAdapter {

    private AppCompatActivity mContext;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private boolean deviceAvailable;
    private final String serverDeviceUuid = "988c30d2-96f4-426f-adff-2208206f8f92";
    private String deviceName;

    private ActivityResultLauncher<Intent> enableBluetoothLauncher;
    private ActivityResultLauncher<Intent> devicesListLauncher;
    private ActivityResultLauncher<String[]> requestPermissions;
    private ClientConnectThread serverScanning;

    private ClientAdapterListener listener;

    public BluetoothClientAdapter(AppCompatActivity context, String name, ClientAdapterListener listener) {
        super();

        this.mContext = context;
        this.deviceName = name;
        this.listener = listener;

        initLaunchers();
        initBluetooth();
    }

    @Override
    protected void init() {

    }

    private void initPermissions() {
        this.requestPermissions = this.mContext.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionResults -> {
            boolean granted = !permissionResults.values().contains(false);

            if (granted) {
                initScanning();
            } else {
                DialogFactory.createMessageInfo(this.mContext, R.string.data_sharing_permissions_title_lbl, R.string.data_sharing_permissions_bluetooth_error).show();
            }
        });
    }

    private void ensurePermissionsGranted(final String... permissions) {
        boolean denied = Arrays.stream(permissions).anyMatch(permission -> ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_DENIED);

        if (denied) { //without access
            requestPermissions.launch(permissions);
        } else {
            initScanning();
        }
    }

    private void initBluetooth() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            bluetoothManager = mContext.getSystemService(BluetoothManager.class);
            bluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            this.deviceAvailable = false;
        } else {
            this.deviceAvailable = true;
        }
    }

    private void initLaunchers() {
        initPermissions();

        this.enableBluetoothLauncher = this.mContext.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> onEnableBluetoothResult(result));

        this.devicesListLauncher = this.mContext.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> onDeviceListResult(result));
    }

    private void onEnableBluetoothResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            //fire connectionListener.deviceEnabled
            listener.onDeviceEnabled(true);
        } else {
            //fire connectionListener.deviceNotEnabled
            listener.onDeviceEnabled(false);
        }
    }

    private void onDeviceListResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {

            Intent data = result.getData();

            String address = data.getExtras().getString(BluetoothDeviceListActivity.EXTRA_DEVICE_ADDRESS);

            BluetoothDevice remoteDevice = this.bluetoothAdapter.getRemoteDevice(address);

            onBluetoothSelected(remoteDevice);

        } else {
            listener.onScanningDevicesFinished();
        }
    }

    @Override
    public String getUUID() {
        return serverDeviceUuid;
    }

    @Override
    public String getName() {
        return this.deviceName;
    }

    @Override
    public boolean isAvailable() {
        return this.deviceAvailable;
    }

    @Override
    public boolean isEnabled() {
        return this.bluetoothAdapter != null && this.bluetoothAdapter.isEnabled();
    }

    @Override
    public void enable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        this.enableBluetoothLauncher.launch(enableBtIntent);
    }

    @Override
    public void startScanning() {
        ensurePermissionsGranted(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN);
    }

    @Override
    public void stopScanning() {

    }

    private void initScanning() {
        this.devicesListLauncher.launch(new Intent(mContext, BluetoothDeviceListActivity.class));
        listener.onScanningDevices();
    }

    private void onBluetoothSelected(BluetoothDevice bluetoothDevice) {
        Log.d("bluetooth", "device was selected");

        //try to connect to server device

        new ClientConnectThread(bluetoothDevice).execute();
    }

    private void onBluetoothSocketConnecting(BluetoothDevice device) {
        listener.onDeviceConnecting();
    }

    private void onBluetoothSocketConnected(BluetoothSocket clientSocket) {
        Log.d("client bluetooth", "client connected");

        listener.onDeviceConnected(new BluetoothSharingDevice(clientSocket, true));
    }

    private void onBluetoothSocketConnectionFailure(BluetoothSocket clientSocket) {
        listener.onDeviceConnectionFailure();
    }

    private class ClientConnectThread extends AsyncTask<Void, Integer, ClientConnectThread.ConnectResult> {
        private BluetoothDevice bluetoothDevice;
        private BluetoothSocket clientSocket;
        private boolean cancel;

        private final int PROGRESS_CONNECTING = 1;

        private ClientConnectThread(BluetoothDevice bluetoothDevice) {
            this.bluetoothDevice = bluetoothDevice;
            BluetoothSocket bSocket = null;

            try {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    DialogFactory.createMessageInfo(mContext, R.string.data_sharing_permissions_title_lbl, R.string.data_sharing_permissions_bluetooth_error).show();
                    return;
                }

                publishProgress(PROGRESS_CONNECTING);

                bSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(serverDeviceUuid));
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.clientSocket = bSocket;
        }

        @Override
        protected ConnectResult doInBackground(Void... voids) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            bluetoothAdapter.cancelDiscovery();

            Log.d("client adapter", "connecting to socket");
            try {
                clientSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();

                // Unable to connect; close the socket and return.
                try {
                    clientSocket.close();
                } catch (IOException closeException) {
                    Log.e("client-adapter", "Could not close the client socket", closeException);
                }


                return new ConnectResult(clientSocket, false);
            }

            if (clientSocket != null && clientSocket.isConnected()) {
                //connection accepted
                 return new ConnectResult(clientSocket, true);
            } else {
                return new ConnectResult(clientSocket, false);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            onBluetoothSocketConnecting(bluetoothDevice);
        }

        @Override
        protected void onPostExecute(ConnectResult result) {

            if (result != null && result.connected) {
                onBluetoothSocketConnected(clientSocket);
            } else {
                onBluetoothSocketConnectionFailure(clientSocket);
            }

        }

        class ConnectResult {
            public BluetoothSocket bluetoothSocket;
            public boolean connected;

            public ConnectResult(BluetoothSocket bluetoothSocket, boolean connected) {
                this.bluetoothSocket = bluetoothSocket;
                this.connected = connected;
            }
        }

    }

}
