package org.philimone.hds.explorer.io.datasharing;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class SharingDevice {
    private String uuid;
    private String name;
    private String username;
    private String deviceId;
    private String appVersion;
    private boolean server;

    private DataSharingTask sharingTask;

    public abstract boolean isConnected();

    public abstract void connect();

    public abstract InputStream getInputStream();

    public abstract OutputStream getOutputStream();

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public boolean isServer() {
        return server;
    }

    public boolean isClient() {
        return !server;
    }

    public void setServer(boolean server) {
        this.server = server;
    }

    public DataSharingTask getSharingTask() {
        return sharingTask;
    }

    public void setSharingTask(DataSharingTask sharingTask) {
        this.sharingTask = sharingTask;
    }
}


