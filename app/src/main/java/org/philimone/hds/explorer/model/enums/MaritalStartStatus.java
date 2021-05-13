package org.philimone.hds.explorer.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum MaritalStartStatus {

    MARRIED         ("MAR", "maritalStatus.married"),
    LIVING_TOGHETER ("LIV","maritalStatus.living_togheter"),
    INVALID_ENUM    ( "-1", "-1");

    String code;
    String name;

    MaritalStartStatus(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, MaritalStartStatus> MAP = new HashMap<>();

    static {
        for (MaritalStartStatus e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static MaritalStartStatus getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }

}
