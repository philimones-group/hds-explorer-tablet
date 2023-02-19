package org.philimone.hds.explorer.io.datasharing;

import androidx.annotation.StringRes;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum ConnectionType {

    BLUETOOTH ("BLUETOOTH", R.string.data_sharing_connection_type_bluetooth_lbl),
    TCP_IP    ("WIFI", R.string.data_sharing_connection_type_wifi_lbl);

    public String code;
    public @StringRes int name;

    ConnectionType(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, ConnectionType> MAP = new HashMap<>();

    static {
        for (ConnectionType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static ConnectionType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}
