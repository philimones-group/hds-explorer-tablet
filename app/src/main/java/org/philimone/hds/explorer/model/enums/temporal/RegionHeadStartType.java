package org.philimone.hds.explorer.model.enums.temporal;

import androidx.annotation.StringRes;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum RegionHeadStartType {

    NEW_HEAD_OF_REGION ("NHR", R.string.eventType_new_hor), //Event Related to the Head of Household
    INVALID_ENUM          ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    RegionHeadStartType(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, RegionHeadStartType> MAP = new HashMap<>();

    static {
        for (RegionHeadStartType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static RegionHeadStartType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}