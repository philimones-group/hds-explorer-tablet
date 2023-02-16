package org.philimone.hds.explorer.io.datasharing;

public abstract class ClientAdapter {

    public ClientAdapter() {
        init();
    }

    protected abstract void init();

    public abstract String getName();

    public abstract String getUUID();

    public abstract boolean isAvailable();

    public abstract boolean isEnabled();

    public abstract void enable();

    public abstract void startScanning();

}
