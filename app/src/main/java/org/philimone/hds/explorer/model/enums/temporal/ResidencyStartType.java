package org.philimone.hds.explorer.model.enums.temporal;

import java.util.HashMap;
import java.util.Map;

public enum ResidencyStartType {

    ENUMERATION           ("ENU", "eventType.enumeration"),
    BIRTH                 ("BIR", "eventType.birth"),
    INTERNAL_INMIGRATION  ("ENT", "eventType.internal_inmigration"),
    EXTERNAL_INMIGRATION  ("XEN", "eventType.external_inmigration"),
    INVALID_ENUM          ( "-1", "-1");

    final String code;
    final String name;

    ResidencyStartType(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, ResidencyStartType> MAP = new HashMap<>();

    static {
        for (ResidencyStartType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static ResidencyStartType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}