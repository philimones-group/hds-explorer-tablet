package org.philimone.hds.explorer.io.datasharing.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
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
import org.philimone.hds.explorer.io.datasharing.ServerAdapter;
import org.philimone.hds.explorer.io.datasharing.ServerAdapterListener;
import org.philimone.hds.explorer.widget.DialogFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class BluetoothServerAdapter extends ServerAdapter {

    private AppCompatActivity mContext;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private boolean deviceAvailable;
    private final String deviceUuid = "988c30d2-96f4-426f-adff-2208206f8f92";
    private String deviceName;

    private ActivityResultLauncher<Intent> enableBluetoothLauncher;
    private ActivityResultLauncher<String[]> requestPermissions;
    private ActivityResultLauncher<String[]> requestEnableBtPermissions;
    private ServerListeningTask serverListening;

    private ServerAdapterListener listener;

    public enum ListeningState {
        CONNECTED, STOPPED, STARTED;
    }

    public BluetoothServerAdapter(AppCompatActivity context, String name, ServerAdapterListener listener) {
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
                initServerListening();
            } else {
                DialogFactory.createMessageInfo(this.mContext, R.string.data_sharing_permissions_title_lbl, R.string.data_sharing_permissions_bluetooth_error).show();
            }
        });

        this.requestEnableBtPermissions = this.mContext.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionResults -> {
            boolean granted = !permissionResults.values().contains(false);

            if (granted) {
                initEnableBt();
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
            initServerListening();
        }
    }

    private void ensureEnableBtPermissionsGranted(final String... permissions) {
        boolean denied = Arrays.stream(permissions).anyMatch(permission -> ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_DENIED);

        if (denied) { //without access
            requestEnableBtPermissions.launch(permissions);
        } else {
            initEnableBt();
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
        this.enableBluetoothLauncher = this.mContext.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> onEnableBluetoothResult(result));

        initPermissions();
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

    @Override
    public String getUUID() {
        return deviceUuid;
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
        ensureEnableBtPermissionsGranted(Manifest.permission.BLUETOOTH_CONNECT);
    }

    private void initEnableBt() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        this.enableBluetoothLauncher.launch(enableBtIntent);
    }

    @Override
    public void startListening() {
        ensurePermissionsGranted(Manifest.permission.BLUETOOTH_CONNECT);
    }

    private void initServerListening() {
        this.serverListening = new ServerListeningTask();
        this.serverListening.execute();
    }

    private void onBluetoothStartedListening() {
        Log.d("bluetooth server", "started listening");
        listener.onServerListening();
    }

    private void onBluetoothStoppedListening() {
        Log.d("bluetooth server", "stopped listening");
        listener.onServerStoppedListening();
    }

    private void onBluetoothSocketConnected(BluetoothSocket clientSocket) {
        Log.d("bluetooth server", "client connected");
        listener.onDeviceConnected(new BluetoothSharingDevice(clientSocket, false));
    }

    private class ServerListeningTask extends AsyncTask<Void, ServerListeningTask.ConnectResult, ServerListeningTask.ConnectResult> {
        private BluetoothServerSocket serverSocket = null;
        private boolean cancel;

        private ServerListeningTask() {
            BluetoothServerSocket tmpSocket = null;

            try {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    DialogFactory.createMessageInfo(mContext, R.string.data_sharing_permissions_title_lbl, R.string.data_sharing_permissions_bluetooth_error).show();
                    return;
                }

                tmpSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(BluetoothServerAdapter.this.getName(), UUID.fromString(getUUID()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.serverSocket = tmpSocket;
        }

        @Override
        protected ConnectResult doInBackground(Void... voids) {

            BluetoothSocket clientSocket = null;

            //listen continuously
            Log.d("server adapter", "start listening");
            publishProgress(new ConnectResult(null, ListeningState.STARTED));
            while (true) {

                if (this.serverSocket == null) {
                    Log.d("server adapter", "socket null");
                    return new ConnectResult(null, ListeningState.STOPPED);
                }

                try {
                    Log.d("server adapter", "listen to sockect");
                    clientSocket = this.serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();

                    if (cancel) {
                        Log.d("cancel", "thread was cancelled");
                        break;
                    }

                    if (!bluetoothAdapter.isEnabled()) {
                        cancelThread();
                        return new ConnectResult(null, ListeningState.STOPPED);
                    }
                }

                if (clientSocket != null) {
                    //connection accepted
                    Log.d("server adapter", "publishing connected");
                    publishProgress(new ConnectResult(clientSocket, ListeningState.CONNECTED));
                }

                Log.d("server adapter", "still in while condition");
            }

            return new ConnectResult(null, ListeningState.STOPPED);
        }

        @Override
        protected void onProgressUpdate(ConnectResult... values) {
            if (values != null && values.length > 0) {
                ConnectResult result = values[0];

                switch (result.state) {
                    case STARTED: onBluetoothStartedListening(); break;
                    case STOPPED: onBluetoothStoppedListening(); break;
                    case CONNECTED:  onBluetoothSocketConnected(result.bluetoothSocket); break;
                }
            }
        }

        @Override
        protected void onPostExecute(ConnectResult result) {
            switch (result.state) {
                case STARTED: onBluetoothStartedListening(); break;
                case STOPPED: onBluetoothStoppedListening(); break;
                case CONNECTED:  onBluetoothSocketConnected(result.bluetoothSocket); break;
            }
        }

        public void cancelThread() {
            try {
                if (this.serverSocket != null) {
                    this.cancel = true;
                    this.serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        class ConnectResult {
            public BluetoothSocket bluetoothSocket;
            public ListeningState state;

            public ConnectResult(BluetoothSocket bluetoothSocket, ListeningState state) {
                this.bluetoothSocket = bluetoothSocket;
                this.state = state;
            }
        }
    }

}
