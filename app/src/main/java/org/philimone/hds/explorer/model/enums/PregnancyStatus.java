package org.philimone.hds.explorer.model.enums;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum PregnancyStatus {

    PREGNANT ("PREGNANT", R.string.pregnacyStatus_pregnant),
    DELIVERED ("DELIVERED", R.string.pregnacyStatus_delivered),
    LOST_TRACK ("LOST_TRACK", R.string.pregnacyStatus_lost_track),
    INVALID_ENUM    ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    PregnancyStatus(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, PregnancyStatus> MAP = new HashMap<>();

    static {
        for (PregnancyStatus e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static PregnancyStatus getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}