package org.philimone.hds.explorer.io.datasharing;

public interface ServerAdapterListener {

    void onDeviceEnabled(boolean enabled);

    void onDeviceConnected(SharingDevice remoteDevice);

    void onServerListening();

    void onServerStoppedListening();

}
