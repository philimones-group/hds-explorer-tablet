package org.philimone.hds.explorer.model.enums;

import androidx.annotation.StringRes;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum MaritalEventType {

    START ("START"), END ("END");

    public String code;

    MaritalEventType(String code){
        this.code = code;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, MaritalEventType> MAP = new HashMap<>();

    static {
        for (MaritalEventType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static MaritalEventType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }

}
