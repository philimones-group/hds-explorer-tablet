package org.philimone.hds.explorer.model.enums;

import androidx.annotation.StringRes;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum HouseholdStatus {
    /*
    Status of a Household
    */
    HOUSE_OCCUPIED ("HOUSE_OCCUPIED", R.string.householdStatus_occupied),
    HOUSE_NOT_FOUND ("HOUSE_NOT_FOUND", R.string.householdStatus_notfound),
    HOUSE_DESTROYED ("HOUSE_DESTROYED", R.string.householdStatus_destroyed),
    HOUSE_ABANDONED ("HOUSE_ABANDONED", R.string.householdStatus_abandoned),
    HOUSE_VACANT ("HOUSE_VACANT", R.string.householdStatus_vacant),
    OTHER         ("OTHER", R.string.householdStatus_other),
    INVALID_ENUM ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    HouseholdStatus(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, HouseholdStatus> MAP = new HashMap<>();

    static {
        for (HouseholdStatus e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static HouseholdStatus getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}