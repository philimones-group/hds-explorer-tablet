package org.philimone.hds.explorer.io.datasharing;

public abstract class ServerAdapter {

    public ServerAdapter() {
        init();
    }

    protected abstract void init();

    public abstract String getName();

    public abstract String getUUID();

    public abstract boolean isAvailable();

    public abstract boolean isEnabled();

    public abstract void enable();

    public abstract void startListening();

    public abstract void stopListening();

}
