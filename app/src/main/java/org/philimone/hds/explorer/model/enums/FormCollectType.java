package org.philimone.hds.explorer.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum FormCollectType {

    NORMAL_COLLECT("NORMAL_COLLECT", "formCollectType.NORMAL_COLLECT"), //Currently Living Here
    PREVIOUS_FORM_COLLECTED    ("PREVIOUS_FORM_COLLECTED", "formCollectType.PREVIOUS_FORM_COLLECTED"),
    CALCULATE_EXPRESSION       ("CALCULATE_EXPRESSION","formCollectType.CALCULATE_EXPRESSION"),
    INVALID_ENUM           ( "-1", "R.string.invalid_enum_value");

    public String code;
    public String name;

    FormCollectType(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, FormCollectType> MAP = new HashMap<>();

    static {
        for (FormCollectType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static FormCollectType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}