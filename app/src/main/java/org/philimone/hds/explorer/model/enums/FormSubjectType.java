package org.philimone.hds.explorer.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum FormSubjectType {

    REGION         ("REGION", "formSubjectType.REGION"),
    HOUSEHOLD      ("HOUSEHOLD","formSubjectType.HOUSEHOLD"),
    MEMBER         ("MEMBER", "formSubjectType.MEMBER"),
    HOUSEHOLD_HEAD ("HOUSEHOLD_HEAD", "formSubjectType.HOUSEHOLD_HEAD"),
    INVALID_ENUM           ( "-1", "R.string.invalid_enum_value");

    public String code;
    public String name;

    FormSubjectType(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /* Finding Enum by code */
    private static final Map<String, FormSubjectType> MAP = new HashMap<>();

    static {
        for (FormSubjectType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static FormSubjectType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}