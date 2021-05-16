package org.philimone.hds.explorer.model.enums;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum MaritalStatus {

    SINGLE          ("SIN", R.string.maritalStatus_single),
    MARRIED         ("MAR", R.string.maritalStatus_married),
    DIVORCED        ("DIV", R.string.maritalStatus_divorced),
    SEPARATED       ("SEP", R.string.maritalStatus_separated),
    WIDOWED         ("WID", R.string.maritalStatus_widowed),
    LIVING_TOGHETER ("LIV", R.string.maritalStatus_living_togheter),
    INVALID_ENUM    ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    MaritalStatus(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, MaritalStatus> MAP = new HashMap<>();

    static {
        for (MaritalStatus e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static MaritalStatus getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }

}
