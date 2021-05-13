package org.philimone.hds.explorer.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum PregnancyStatus {

    PREGNANT ("PREGNANT", "pregnacyStatus.pregnant"),
    DELIVERED ("DELIVERED", "pregnacyStatus.delivered"),
    LOST_TRACK ("LOST_TRACK", "pregnacyStatus.lost_track"),
    INVALID_ENUM    ( "-1", "-1");

    String code;
    String name;

    PregnancyStatus(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, PregnancyStatus> MAP = new HashMap<>();

    static {
        for (PregnancyStatus e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static PregnancyStatus getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}