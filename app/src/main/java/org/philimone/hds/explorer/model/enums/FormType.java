package org.philimone.hds.explorer.model.enums;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum FormType {

    REGULAR    ("REGULAR", "formType.REGULAR"), //Currently Living Here
    FORM_GROUP ("FORM_GROUP", "formType.FORM_GROUP"),
    INVALID_ENUM           ( "-1", "R.string.invalid_enum_value");


    public String code;
    public String name;

    FormType(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, FormType> MAP = new HashMap<>();

    static {
        for (FormType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static FormType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}