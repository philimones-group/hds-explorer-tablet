package org.philimone.hds.explorer.model.enums;

import androidx.annotation.StringRes;

import java.util.HashMap;
import java.util.Map;

public enum CoreFormRecordType {

    NEW_RECORD    ("NEW"),
    UPDATE_RECORD ("UPDATE"),

    INVALID_ENUM    ( "-1");

    public String code;
    public @StringRes int name;

    CoreFormRecordType(String code){
        this.code = code;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, CoreFormRecordType> MAP = new HashMap<>();

    static {
        for (CoreFormRecordType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static CoreFormRecordType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}