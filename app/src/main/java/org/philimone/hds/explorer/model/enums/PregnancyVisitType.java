package org.philimone.hds.explorer.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum PregnancyVisitType {
    ANTEPARTUM ("ANTEPARTUM", "pregnancyVisitType.antepartum"),
    POSTPARTUM ("POSTPARTUM", "pregnancyVisitType.postpartum"),
    INVALID_ENUM           ( "-1", "R.string.invalid_enum_value");

    public String code;
    public String name;

    PregnancyVisitType(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    @Override
    public String toString() {
        return name;
    }

    /* Finding Enum by code */
    private static final Map<String, PregnancyVisitType> MAP = new HashMap<>();

    static {
        for (PregnancyVisitType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static PregnancyVisitType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}
