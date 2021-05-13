package org.philimone.hds.explorer.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum BirthPlace {

    HOME ("HOME", "birthPlace.home"),
    HOSPITAL ("HOSPITAL", "birthPlace.hospital"),
    TRADITIONAL_MIDWIFE ("TRADITIONAL_MIDWIFE", "birthPlace.traditional_midwife"),
    HEALTH_CENTER_CLINIC ("HEALTH_CENTER_CLINIC", "birthPlace.health_center_clinic"),
    OTHER ("OTHER", "birthPlace.other"),
    INVALID_ENUM    ( "-1", "-1");

    String code;
    String name;

    BirthPlace(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, BirthPlace> MAP = new HashMap<>();

    static {
        for (BirthPlace e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static BirthPlace getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }

}