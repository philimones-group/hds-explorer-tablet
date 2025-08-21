package org.philimone.hds.explorer.model.enums;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum HouseholdType {
    REGULAR       ("REGULAR", "householdType.regular"),
    INSTITUTIONAL ("INSTITUTIONAL", "householdType.institutional"),
    INVALID_ENUM  ( "-1", "R.string.invalid_enum_value");

    public String code;
    public String name;

    HouseholdType(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    @Override
    public String toString() {
        return name;
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
