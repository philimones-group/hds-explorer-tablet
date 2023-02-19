package org.philimone.hds.explorer.io.datasharing;

public interface ClientAdapterListener {

    void onDeviceEnabled(boolean enabled);

    void onDeviceConnected(SharingDevice device);

    void onDeviceConnectionFailure();

    void onScanningDevices();

    void onScanningDevicesFinished();

}
