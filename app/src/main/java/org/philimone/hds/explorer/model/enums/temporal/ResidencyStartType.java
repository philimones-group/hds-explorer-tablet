package org.philimone.hds.explorer.model.enums.temporal;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum ResidencyStartType {

    ENUMERATION           ("ENU", R.string.eventType_enumeration),
    BIRTH                 ("BIR", R.string.eventType_birth),
    INTERNAL_INMIGRATION  ("ENT", R.string.eventType_internal_inmigration),
    EXTERNAL_INMIGRATION  ("XEN", R.string.eventType_external_inmigration),
    INVALID_ENUM          ( "-1", R.string.invalid_enum_value);

    final public String code;
    final public @StringRes int name;

    ResidencyStartType(String code, @StringRes int name){
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