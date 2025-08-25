package org.philimone.hds.explorer.model.enums;

import androidx.annotation.StringRes;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum ProxyHeadChangeReason {
    ADMIN_APPOINTMENT    ("AAP", R.string.proxy_head_change_reason_admin_appointment_lbl),
    INSTITUTION_REQUIRED ("INS", R.string.proxy_head_change_reason_institution_required_lbl),
    TEMP_ABSENCE_OF_HEAD ("TAH", R.string.proxy_head_change_reason_temp_absence_of_head_lbl),
    LEGAL_GUARDIANSHIP   ("LGU", R.string.proxy_head_change_reason_legal_guardianship_lbl),
    REPLACEMENT          ("REP", R.string.proxy_head_change_reason_replacement_lbl),
    RESIGNATION          ("RES", R.string.proxy_head_change_reason_resignation_lbl),
    DECEASED             ("DEC", R.string.proxy_head_change_reason_deceased_lbl),
    OTHER                ("OTH", R.string.proxy_head_change_reason_other_lbl),
    INVALID_ENUM         ("-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int  name;

    ProxyHeadChangeReason(String code, @StringRes int  name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, ProxyHeadChangeReason> MAP = new HashMap<>();

    static {
        for (ProxyHeadChangeReason e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static ProxyHeadChangeReason getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}
