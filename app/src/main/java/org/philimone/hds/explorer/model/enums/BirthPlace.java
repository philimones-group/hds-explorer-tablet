package org.philimone.hds.explorer.model.enums;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum BirthPlace {

    HOME ("HOME", R.string.birthPlace_home),
    HOSPITAL ("HOSPITAL", R.string.birthPlace_hospital),
    TRADITIONAL_MIDWIFE ("TRADITIONAL_MIDWIFE", R.string.birthPlace_traditional_midwife),
    HEALTH_CENTER_CLINIC ("HEALTH_CENTER_CLINIC", R.string.birthPlace_health_center_clinic),
    OTHER ("OTHER", R.string.birthPlace_other),
    INVALID_ENUM    ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    BirthPlace(String code, @StringRes int name){
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