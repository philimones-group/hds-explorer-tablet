package org.philimone.hds.explorer.model.enums;

import androidx.annotation.StringRes;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum NoVisitReason {
    /*
    2.1.1. Why is not possible to perform the visit to this Household?
    */
    HOUSE_OCCUPIED ("HOUSE_OCCUPIED", R.string.noVisitReason_hh_occupied),
    HOUSE_NOT_FOUND ("HOUSE_NOT_FOUND", R.string.noVisitReason_hh_notfound),
    HOUSE_DESTROYED ("HOUSE_DESTROYED", R.string.noVisitReason_hh_destroyed),
    HOUSE_ABANDONED ("HOUSE_ABANDONED", R.string.noVisitReason_hh_abandoned),
    HOUSE_VACANT ("HOUSE_VACANT", R.string.noVisitReason_hh_vacant),
    NO_RESPONDENT ("NO_RESPONDENT", R.string.noVisitReason_no_respondent),
    REFUSE ("REFUSE", R.string.noVisitReason_refuse),
    OTHER         ("OTHER", R.string.noVisitReason_other),
    INVALID_ENUM ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    NoVisitReason(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, NoVisitReason> MAP = new HashMap<>();

    static {
        for (NoVisitReason e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static NoVisitReason getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}