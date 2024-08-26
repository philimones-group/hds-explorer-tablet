package org.philimone.hds.explorer.io.datasharing.wifi;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.philimone.hds.explorer.io.datasharing.ServerAdapter;
import org.philimone.hds.explorer.io.datasharing.ServerAdapterListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpIpServerAdapter extends ServerAdapter {

    private AppCompatActivity mContext;
    private boolean deviceAvailable;
    private final String deviceUuid = "988c30d2-96f4-426f-adff-2208206f8f92";
    private String deviceName;

    //private ActivityResultLauncher<Intent> enableTcpIpLauncher;
    //private ActivityResultLauncher<String[]> requestPermissions;
    //private ActivityResultLauncher<String[]> requestEnableBtPermissions;
    private ServerListeningTask serverListening;

    private ServerAdapterListener listener;

    private NsdManager mNsdManager;
    private NsdManager.RegistrationListener mRegistrationListener;

    public enum ListeningState {
        CONNECTED, STOPPED, STARTED;
    }

    public static final int TCP_IP_PORT = 1047; //47661;
    public static final String SERVICE_TYPE = "_http._tcp.";

    public static final String SERVICE_NAME = "HDS_SHARE.";

    public TcpIpServerAdapter(AppCompatActivity context, String name, ServerAdapterListener listener) {
        super();

        this.mContext = context;
        this.deviceName = name;
        this.listener = listener;

        initTcp();
    }

    @Override
    protected void init() {

    }

    private void initTcp() {
        this.mNsdManager = (NsdManager) this.mContext.getSystemService(Context.NSD_SERVICE);

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

    private void initRegistrationListener() {
        unregisterService();

        mRegistrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                String serviceName = NsdServiceInfo.getServiceName();
                Log.d("NsdHelper", "Service registered: " + serviceName);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e("NsdHelper", "Service registration failed: " + errorCode);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                Log.d("NsdHelper", "Service unregistered");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e("NsdHelper", "Service unregistration failed: " + errorCode);
            }
        };
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME + this.getName());
        serviceInfo.setServiceType(SERVICE_TYPE);

        if (serverListening != null && serverListening.getServerSocket() != null) {
            ServerSocket socket = serverListening.getServerSocket();
            serviceInfo.setHost(socket.getInetAddress());
            Log.d("setting serviceinfo", socket.getInetAddress()+" - " + port);

            serviceInfo.setPort(port);
            mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        }
    }

    public void unregisterService() {
        if (mNsdManager != null && mRegistrationListener != null) {

            try {
                mNsdManager.unregisterService(mRegistrationListener);
            } catch (Exception ex) {
                Log.d("unregister", ex.getMessage());
            }

            mRegistrationListener = null;
            System.gc();
        }
    }

    private void onEnableTcpIpResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            //fire connectionListener.deviceEnabled
            listener.onDeviceEnabled(isNetworkAvailable());
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
        return this.deviceAvailable;
    }

    @Override
    public void enable() {
        initTcp();
        listener.onDeviceEnabled(deviceAvailable);
    }

    @Override
    public void startListening() {
        //Initiate NSD Registration listener
        initRegistrationListener();

        //Initiate Server Socket
        initServerListening();

        //Register NSD Service in HDS SHARING PORT
        registerService(TCP_IP_PORT);
    }

    @Override
    public void stopListening() {
        unregisterService();

        if (this.serverListening != null) {
            this.serverListening.cancelThread();
        }
    }

    private void initServerListening() {
        this.serverListening = new ServerListeningTask();
        this.serverListening.execute();
    }

    private void onTcpServerStartedListening() {
        Log.d("tcp/ip server", "started listening");
        listener.onServerListening();
    }

    private void onTcpServerStoppedListening() {
        Log.d("tcp/ip server", "stopped listening");
        listener.onServerStoppedListening();
    }

    private void onTcpServerSocketConnected(ServerListeningTask.ConnectResult result) {
        Log.d("tcp/ip server", "client connected");
        listener.onDeviceConnected(new TcpIpSharingDevice(result.tcpDevice, result.clientSocket, false));
    }

    private class ServerListeningTask extends AsyncTask<Void, ServerListeningTask.ConnectResult, ServerListeningTask.ConnectResult> {
        private ServerSocket serverSocket = null;
        private boolean cancel;

        private ServerListeningTask() {
            ServerSocket tmpSocket = null;

            try {
                tmpSocket = new ServerSocket(TCP_IP_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.serverSocket = tmpSocket;
        }

        public ServerSocket getServerSocket() {
            return serverSocket;
        }

        @Override
        protected ConnectResult doInBackground(Void... voids) {

            Socket clientSocket = null;

            //listen continuously
            Log.d("server adapter", "start listening");
            publishProgress(new ConnectResult(null, null, ListeningState.STARTED));

            while (!this.serverSocket.isClosed()) {

                if (this.serverSocket == null) {
                    Log.d("server adapter", "socket null");
                    return new ConnectResult(null, null, ListeningState.STOPPED);
                }

                try {
                    Log.d("server adapter", "listen to socket");
                    clientSocket = this.serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();

                    if (cancel) {
                        Log.d("cancel", "thread was cancelled");
                        break;
                    }

                    if (serverSocket.isClosed()) {
                        cancelThread();
                        return new ConnectResult(null, null, ListeningState.STOPPED);
                    }
                }

                if (clientSocket != null) {
                    //connection accepted
                    Log.d("server adapter", "publishing connected");

                    TcpIpDevice device = new TcpIpDevice(null, clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());

                    publishProgress(new ConnectResult(device, clientSocket, ListeningState.CONNECTED));
                }

                Log.d("server adapter", "still in while condition");
            }

            return new ConnectResult(null, null, ListeningState.STOPPED);
        }

        @Override
        protected void onProgressUpdate(ConnectResult... values) {
            if (values != null && values.length > 0) {
                ConnectResult result = values[0];

                switch (result.state) {
                    case STARTED: onTcpServerStartedListening(); break;
                    case STOPPED: onTcpServerStoppedListening(); break;
                    case CONNECTED:  onTcpServerSocketConnected(result); break;
                }
            }
        }

        @Override
        protected void onPostExecute(ConnectResult result) {
            switch (result.state) {
                case STARTED: onTcpServerStartedListening(); break;
                case STOPPED: onTcpServerStoppedListening(); break;
                case CONNECTED:  onTcpServerSocketConnected(result); break;
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
            public TcpIpDevice tcpDevice;
            public Socket clientSocket;
            public ListeningState state;

            public ConnectResult(TcpIpDevice tcpIpDevice, Socket socket, ListeningState state) {
                this.tcpDevice = tcpIpDevice;
                this.clientSocket = socket;
                this.state = state;
            }
        }
    }

}
