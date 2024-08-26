
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.philimone.hds.explorer.io.datasharing.wifi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.philimone.hds.explorer.R;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;

import mz.betainteractive.utilities.StringUtil;

/**
 * This Activity appears as a dialog. It lists any HDS-Explorer server and
 * devices detected in the network it is connected to.
 * Activity in the result Intent.
 *
 * This Activicty was copied from BluetoothDeviceListActivity and Updated for TCP/IP Devices by Paulo Filimone
 */
public class TcpIpDeviceListActivity extends AppCompatActivity {

    private ListView lvFoundDevices;
    private TextView txtFoundDevicesMsg;
    private ProgressBar pbarFoundDevices;
    private Button btScanDevices;

    private TcpIpDeviceArrayAdapter devicesAdapter;

    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tcp_device_list);
        setResult(Activity.RESULT_CANCELED); // Set result CANCELED in case the user backs out

        initialize();
    }

    private void initialize() {
        // Initialize components
        this.btScanDevices = findViewById(R.id.btScanDevices);
        this.lvFoundDevices = findViewById(R.id.lvFoundDevices);
        this.txtFoundDevicesMsg = findViewById(R.id.txtFoundDevicesMsg);
        this.pbarFoundDevices = findViewById(R.id.pbarFoundDevices);

        btScanDevices.setOnClickListener(v -> {
            discoverNetworkServices();
            btScanDevices.setEnabled(false);
        });

        lvFoundDevices.setOnItemClickListener((parent, view, position, id) -> {
            if (parent.getAdapter() instanceof TcpIpDeviceArrayAdapter) {
                onDeviceSelected(((TcpIpDeviceArrayAdapter) parent.getAdapter()), position);
            }
        });

        initDiscoveryManager();

        this.devicesAdapter = new TcpIpDeviceArrayAdapter(this);
        this.lvFoundDevices.setAdapter(this.devicesAdapter);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        discoverNetworkServices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopNetworkDiscovery();
    }

    /* Discovery methods */

    private void initDiscoveryManager() {
        this.mNsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);
    }

    public void initializeDiscoveryListener() {
        this.mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d("NsdHelper", "Service discovery started");
                setProgressBarIndeterminateVisibility(true);
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d("NsdHelper", "Service discovery success: " + service);

                if (service.getServiceName().contains(TcpIpServerAdapter.SERVICE_NAME)) {
                    final String hostUsername = service.getServiceName().replace(TcpIpServerAdapter.SERVICE_NAME, "");

                    mNsdManager.resolveService(service, new NsdManager.ResolveListener() {
                        @Override
                        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                            Log.e("NsdHelper", "Resolve failed: " + errorCode);
                        }

                        @Override
                        public void onServiceResolved(NsdServiceInfo serviceInfo) {
                            Log.d("NsdHelper", "Resolve Succeeded. [" + (StringUtil.formatPrecise(new Date())) + "] - " + serviceInfo);
                            InetAddress host = null;
                            int port = serviceInfo.getPort();

                            //if (port == TcpIpServerAdapter.TCP_IP_PORT) { //its explorer

                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                    List<InetAddress> hostAddresses = serviceInfo.getHostAddresses();
                                    host = hostAddresses.get(0);
                                } else {
                                    host = serviceInfo.getHost();
                                }

                                // Connect to the server using host and port [host, port, hostUsername]
                                if (devicesAdapter != null) {
                                    Log.d("NdsHelper", "adding a device unam="+hostUsername+", host="+host.getHostAddress()+", port="+port);
                                    TcpIpDevice device = new TcpIpDevice(hostUsername, host.getHostAddress(), port);
                                    onDevicesFound(device);
                                }
                            //}
                        }
                    });
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e("NsdHelper", "service lost: " + service);
                onDevicesNotFound();
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i("NsdHelper", "Discovery stopped: " + serviceType);
                onDevicesNotFound();
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e("NsdHelper", "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e("NsdHelper", "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    private void onDevicesFound(TcpIpDevice device) {
        //capture thread
        runOnUiThread(() -> {
            devicesAdapter.addDevice(device);
            pbarFoundDevices.setVisibility(View.GONE);
            txtFoundDevicesMsg.setVisibility(View.GONE);
        });
    }

    private void onDevicesNotFound() {
        if (devicesAdapter.isEmpty()) {
            txtFoundDevicesMsg.setVisibility(View.VISIBLE);
            pbarFoundDevices.setVisibility(View.GONE);
        }
    }

    public void discoverNetworkServices() {
        //stop current discovery
        stopNetworkDiscovery();

        devicesAdapter.clear();
        //lvFoundDevices.setAdapter(null);
        btScanDevices.setEnabled(false);

        initializeDiscoveryListener();
        mNsdManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

        pbarFoundDevices.setVisibility(View.VISIBLE);
        txtFoundDevicesMsg.setVisibility(View.GONE);
    }

    public void stopNetworkDiscovery() {
        pbarFoundDevices.setVisibility(View.GONE);
        txtFoundDevicesMsg.setVisibility(View.GONE);

        if (mDiscoveryListener != null) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }

        mDiscoveryListener = null;
        System.gc();
    }

    /**
     * The on-click listener for all devices in the ListViews
     */
    private void onDeviceSelected(TcpIpDeviceArrayAdapter adapter, int position) {
        stopNetworkDiscovery();

        TcpIpDevice tcpIpDevice = adapter.getItem(position);

        Intent intent = new Intent();
        intent.putExtra(TcpIpDevice.DEVICE_NAME, tcpIpDevice.getName());
        intent.putExtra(TcpIpDevice.DEVICE_HOSTNAME, tcpIpDevice.getHostname());
        intent.putExtra(TcpIpDevice.DEVICE_PORT, tcpIpDevice.getPort());

        // Set result and finish this Activity
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

}