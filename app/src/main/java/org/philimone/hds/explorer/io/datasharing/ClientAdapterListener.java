package org.philimone.hds.explorer.io.datasharing;

public interface ClientAdapterListener {

    void onDeviceEnabled(boolean enabled);

    void onDeviceConnecting();

    void onDeviceConnected(SharingDevice device);

    void onDeviceConnectionFailure();

    void onScanningDevices();

    void onScanningDevicesFinished();

}
