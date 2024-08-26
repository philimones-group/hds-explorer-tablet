package org.philimone.hds.explorer.io.datasharing.wifi;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import org.philimone.hds.explorer.io.datasharing.SharingDevice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TcpIpSharingDevice extends SharingDevice {

    private TcpIpDevice tcpDevice;
    private Socket socket;

    public TcpIpSharingDevice(TcpIpDevice device, Socket socket, boolean isServerDevice) {
        this.tcpDevice = device;
        this.socket = socket;

        this.setName(device.getName());
        this.setUuid(socket.getRemoteSocketAddress().toString());
        this.setServer(isServerDevice);

        if (getUuid().startsWith("/")) {
            String uid = getUuid().replace("/", "");
            setUuid(uid);
        }
    }

    public TcpIpDevice getTcpDevice() {
        return tcpDevice;
    }

    @Override
    public boolean isConnected() {
        return this.socket != null && this.socket.isConnected();
    }

    @Override
    public void connect() {
        if (this.socket != null) {
            /*
            try {
                socket.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
        }
    }

    @Override
    public InputStream getInputStream() {
        if (this.socket != null) {
            try {
                return this.socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public OutputStream getOutputStream() {
        if (this.socket != null) {
            try {
                return this.socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
