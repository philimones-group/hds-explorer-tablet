package org.philimone.hds.explorer.io.datasharing.wifi;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.philimone.hds.explorer.io.datasharing.ClientAdapter;
import org.philimone.hds.explorer.io.datasharing.ClientAdapterListener;
import org.philimone.hds.explorer.io.datasharing.bluetooth.BluetoothDeviceListActivity;

import java.io.IOException;
import java.net.Socket;

public class TcpIpClientAdapter extends ClientAdapter {

    private AppCompatActivity mContext;
    private boolean deviceAvailable;
    private final String serverDeviceUuid = "988c30d2-96f4-426f-adff-2208206f8f92";
    private String deviceName;
    private ActivityResultLauncher<Intent> devicesListLauncher;
    private ClientConnectThread serverScanning;

    private ClientAdapterListener listener;

    public TcpIpClientAdapter(AppCompatActivity context, String name, ClientAdapterListener listener) {
        super();

        this.mContext = context;
        this.deviceName = name;
        this.listener = listener;

        initLaunchers();
        initDevice();
    }

    @Override
    protected void init() {

    }

    private void initDevice() {
        this.deviceAvailable = isNetworkAvailable();
    }

    // Check if Wi-Fi is enabled
    public boolean isWifiEnabled() {
        WifiManager wifiManager = (WifiManager) this.mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null && wifiManager.isWifiEnabled();
    }

    // Check if TCP/IP connectivity is available
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                return networkCapabilities != null &&
                        (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            } else {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }

    private void initLaunchers() {
        this.devicesListLauncher = this.mContext.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> onDeviceListResult(result));
    }

    private void onDeviceListResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {

            Intent data = result.getData();

            String name = data.getStringExtra(TcpIpDevice.DEVICE_NAME);
            String host = data.getStringExtra(TcpIpDevice.DEVICE_HOSTNAME);
            Integer port = data.getIntExtra(TcpIpDevice.DEVICE_PORT, TcpIpServerAdapter.TCP_IP_PORT);

            TcpIpDevice device = new TcpIpDevice(name, host, port);

            onServerSelected(device);

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
        return deviceAvailable;
    }

    @Override
    public void enable() {

    }

    @Override
    public void startScanning() {
        //ensurePermissionsGranted(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN);
        initScanning();
    }

    @Override
    public void stopScanning() {

    }

    private void initScanning() {
        this.devicesListLauncher.launch(new Intent(mContext, TcpIpDeviceListActivity.class));
        listener.onScanningDevices();
    }

    private void onServerSelected(TcpIpDevice device) {
        Log.d("tcp/ip", "device was selected");

        //try to connect to server device
        new ClientConnectThread(device).execute();
    }

    private void onDeviceSocketConnecting(TcpIpDevice device) {
        listener.onDeviceConnecting();
    }

    private void onDeviceSocketConnected(TcpIpDevice device, Socket clientSocket) {
        Log.d("client tcp/ip", "client connected");

        listener.onDeviceConnected(new TcpIpSharingDevice(device, clientSocket, true));
    }

    private void onDeviceSocketConnectionFailure(TcpIpDevice device, Socket clientSocket) {
        listener.onDeviceConnectionFailure();
    }

    private class ClientConnectThread extends AsyncTask<Void, Integer, ClientConnectThread.ConnectResult> {
        private TcpIpDevice tcpDevice;
        private Socket clientSocket;
        private boolean cancel;

        private final int PROGRESS_CONNECTING = 1;

        private ClientConnectThread(TcpIpDevice device) {
            this.tcpDevice = device;
        }

        @Override
        protected ConnectResult doInBackground(Void... voids) {
            Socket tSocket = null;

            Log.d("client adapter", "connecting to socket host=["+ tcpDevice.getHostname() +"], port=[" + tcpDevice.getPort() + "]");

            publishProgress(PROGRESS_CONNECTING);

            try {
                tSocket = new Socket(tcpDevice.getHostname(), tcpDevice.getPort());
            } catch (IOException e) {
                e.printStackTrace(); //throw new RuntimeException(e);
                return new ConnectResult(tcpDevice, null, false);
            }

            this.clientSocket = tSocket;

            if (clientSocket != null && clientSocket.isConnected()) {
                //connection accepted
                 return new ConnectResult(tcpDevice, clientSocket, true);
            } else {
                return new ConnectResult(tcpDevice, clientSocket, false);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            onDeviceSocketConnecting(tcpDevice);
        }

        @Override
        protected void onPostExecute(ConnectResult result) {

            if (result != null && result.connected) {
                onDeviceSocketConnected(tcpDevice, clientSocket);
            } else {
                onDeviceSocketConnectionFailure(tcpDevice, clientSocket);
            }

        }

        class ConnectResult {
            public TcpIpDevice tcpDevice;
            public Socket socket;
            public boolean connected;

            public ConnectResult(TcpIpDevice device, Socket socket, boolean connected) {
                this.tcpDevice = device;
                this.socket = socket;
                this.connected = connected;
            }
        }

    }

}
