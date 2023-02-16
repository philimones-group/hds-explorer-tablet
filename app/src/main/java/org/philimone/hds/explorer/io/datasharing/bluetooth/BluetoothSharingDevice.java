package org.philimone.hds.explorer.io.datasharing.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import org.philimone.hds.explorer.io.datasharing.SharingDevice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothSharingDevice extends SharingDevice {

    private BluetoothSocket bluetoothSocket;

    public BluetoothSharingDevice(BluetoothSocket bluetoothSocket, boolean isServerDevice) {
        this.bluetoothSocket = bluetoothSocket;

        BluetoothDevice device = bluetoothSocket.getRemoteDevice();

        this.setName(device.getName());
        this.setUuid(device.getAddress());
        this.setServer(isServerDevice);
    }
    @Override
    public boolean isConnected() {
        return this.bluetoothSocket != null && this.bluetoothSocket.isConnected();
    }

    @Override
    public void connect() {
        if (this.bluetoothSocket != null) {
            try {
                bluetoothSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public InputStream getInputStream() {
        if (this.bluetoothSocket != null) {
            try {
                return this.bluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public OutputStream getOutputStream() {
        if (this.bluetoothSocket != null) {
            try {
                return this.bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
