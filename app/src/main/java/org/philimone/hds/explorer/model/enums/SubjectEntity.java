package org.philimone.hds.explorer.model.enums;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum SubjectEntity {

    USER        ("User"),
    REGION      ("Region"),
    HOUSEHOLD   ("Household"),
    MEMBER      ("Member"),
    VISIT       ("Visit"),

    INVALID_ENUM    ( "-1");

    public String code;
    public @StringRes int name;

    SubjectEntity(String code){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, SubjectEntity> MAP = new HashMap<>();

    static {
        for (SubjectEntity e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static SubjectEntity getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}