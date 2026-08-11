package org.philimone.hds.explorer.model.enums;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum MemberPregnancyStatus {

    NONE ("NONE", R.string.not_applicable_lbl),
    PREGNANT ("PREGNANT", R.string.pregnacyStatus_pregnant),
    POSTPARTUM ("POSTPARTUM", R.string.pregnacyStatus_delivered);

    public String code;
    public @StringRes int name;

    MemberPregnancyStatus(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, MemberPregnancyStatus> MAP = new HashMap<>();

    static {
        for (MemberPregnancyStatus e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static MemberPregnancyStatus getFrom(String code) {
        return code==null ? NONE : MAP.get(code);
    }
}