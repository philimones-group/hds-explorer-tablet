
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

package org.philimone.hds.explorer.io.datasharing.bluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.philimone.hds.explorer.R;

import java.util.Set;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 *
 * This Activicty was copied from DeviceListActivity from BluetoothChat project and Updated by Paulo Filimone
 */
public class BluetoothDeviceListActivity extends AppCompatActivity {

    private static final String TAG = "Bluetooth-DeviceList";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private BluetoothAdapter bluetoothAdapter;

    private TextView txtPairedDevicesMsg;
    private Button btScanDevices;
    private ListView lvPairedDevices;
    private TextView txtNewDevices;
    private ListView lvNewDevices;
    private TextView txtPairedDevices;
    private TextView txtNewDevicesMsg;

    private BluetoothDeviceArrayAdapter pairedDevicesAdapter;
    private BluetoothDeviceArrayAdapter newDevicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_device_list);
        setResult(Activity.RESULT_CANCELED); // Set result CANCELED in case the user backs out

        initialize();
    }

    private void initialize() {
        // Initialize components
        this.btScanDevices = findViewById(R.id.btScanDevices);
        this.lvPairedDevices = findViewById(R.id.lvPairedDevices);
        this.lvNewDevices = findViewById(R.id.lvNewDevices);
        this.txtPairedDevices = findViewById(R.id.txtPairedDevices);
        this.txtNewDevices = findViewById(R.id.txtNewDevices);
        this.txtPairedDevicesMsg = findViewById(R.id.txtPairedDevicesMsg);
        this.txtNewDevicesMsg = findViewById(R.id.txtNewDevicesMsg);

        btScanDevices.setOnClickListener(v -> {
            doDiscovery();
            v.setEnabled(false);
        });

        lvPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (parent.getAdapter() instanceof BluetoothDeviceArrayAdapter) {
                    onDeviceSelected(((BluetoothDeviceArrayAdapter) parent.getAdapter()), position);
                }
            }
        });

        lvNewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (parent.getAdapter() instanceof BluetoothDeviceArrayAdapter) {
                    onDeviceSelected(((BluetoothDeviceArrayAdapter) parent.getAdapter()), position);
                }
            }
        });

        // Find and set up the ListView for paired devices
        this.pairedDevicesAdapter = new BluetoothDeviceArrayAdapter(this);
        this.newDevicesAdapter = new BluetoothDeviceArrayAdapter(this);
        lvPairedDevices.setAdapter(pairedDevicesAdapter);
        lvNewDevices.setAdapter(newDevicesAdapter);

        initBluetoothDevice();
        loadPairedDevicesToList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (bluetoothAdapter != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                //msg
                return;
            }
            bluetoothAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(bluetoothBroadcastReceiver);
    }

    private void initBluetoothDevice() {

        // Get the local Bluetooth adapter
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            BluetoothManager bluetoothManager = this.getSystemService(BluetoothManager.class);
            this.bluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        // Register for broadcasts when a device is discovered and when discovery has finished
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        this.registerReceiver(bluetoothBroadcastReceiver, filter1);
        this.registerReceiver(bluetoothBroadcastReceiver, filter2);
    }

    private void loadPairedDevicesToList() {

        if (bluetoothAdapter == null) return;

        // Get a set of currently paired devices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            //PERMISSION MSG
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            txtPairedDevices.setVisibility(View.VISIBLE);
            txtPairedDevicesMsg.setVisibility(View.GONE);

            pairedDevicesAdapter.addDevices(pairedDevices);

            /*for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAdapter.add(device);
            }*/
        } else {
            //String noDevices = getResources().getText(R.string.bluetooth_devlist_none_paired).toString();
            //pairedDevicesArrayAdapter.add(noDevices);
            txtPairedDevicesMsg.setVisibility(View.VISIBLE);

        }
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        //setTitle(R.string.bluetooth_devlist_scanning_lbl);

        pairedDevicesAdapter.clear();
        newDevicesAdapter.clear();

        // Turn on sub-title for new devices
        txtNewDevices.setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        bluetoothAdapter.startDiscovery();
    }

    /**
     * The on-click listener for all devices in the ListViews
     */
    private void onDeviceSelected(BluetoothDeviceArrayAdapter adapter, int position) {
        BluetoothDevice bluetoothDevice = adapter.getItem(position);

        Intent intent = new Intent();
        intent.putExtra(EXTRA_DEVICE_ADDRESS, bluetoothDevice.getAddress());
        // Set result and finish this Activity
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device != null && device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    newDevicesAdapter.addDevice(device);

                    lvNewDevices.setVisibility(View.VISIBLE);
                    txtNewDevicesMsg.setVisibility(View.GONE);
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.data_sharing_devlist_select_device_lbl);

                if (newDevicesAdapter.getCount() == 0) {
                    //String noDevices = getResources().getText(R.string.bluetooth_devlist_none_found).toString();
                    //mNewDevicesArrayAdapter.add(noDevices);
                    txtNewDevicesMsg.setVisibility(View.VISIBLE);
                } else {
                    txtNewDevicesMsg.setVisibility(View.GONE);
                }

                btScanDevices.setEnabled(true);
            }
        }
    };
}