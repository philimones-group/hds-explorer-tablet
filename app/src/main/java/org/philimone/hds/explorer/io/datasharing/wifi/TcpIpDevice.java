package org.philimone.hds.explorer.io.datasharing.wifi;

import java.util.Objects;

public class TcpIpDevice {

    public static final String DEVICE_NAME = "name";
    public static final String DEVICE_HOSTNAME = "hostname";
    public static final String DEVICE_PORT = "port";

    private String id;
    private String name;
    private String hostname;
    private int port;

    public TcpIpDevice(String name, String hostname, int port) {
        this.name = name;
        this.hostname = hostname;
        this.port = port;

        setId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setId();
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
        setId();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
        setId();
    }

    public String getId() {
        return id;
    }

    private String setId() {
        return this.id = hostname + ":" + port + "." + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TcpIpDevice device = (TcpIpDevice) o;
        return Objects.equals(id, device.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
