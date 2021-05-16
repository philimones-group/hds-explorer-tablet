package org.philimone.hds.explorer.model.enums;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum MaritalStartStatus {

    MARRIED         ("MAR", R.string.maritalStatus_married),
    LIVING_TOGHETER ("LIV", R.string.maritalStatus_living_togheter),
    INVALID_ENUM    ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    MaritalStartStatus(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, MaritalStartStatus> MAP = new HashMap<>();

    static {
        for (MaritalStartStatus e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static MaritalStartStatus getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }

}
