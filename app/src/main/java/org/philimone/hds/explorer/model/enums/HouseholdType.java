package org.philimone.hds.explorer.model.enums;

import androidx.annotation.StringRes;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum HouseholdType {
    REGULAR       ("REGULAR", R.string.household_type_regular_lbl),
    INSTITUTIONAL ("INSTITUTIONAL", R.string.household_type_institutional_lbl),
    INVALID_ENUM  ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int  name;

    HouseholdType(String code, @StringRes int  name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, HouseholdType> MAP = new HashMap<>();

    static {
        for (HouseholdType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static HouseholdType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}
