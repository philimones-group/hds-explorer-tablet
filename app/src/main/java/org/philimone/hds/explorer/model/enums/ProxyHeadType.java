package org.philimone.hds.explorer.model.enums;

import androidx.annotation.StringRes;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum ProxyHeadType {
    RESIDENT       ("RESIDENT", R.string.proxy_head_type_resident_lbl),
    NON_RESIDENT   ("NON_RESIDENT", R.string.proxy_head_type_non_resident_lbl),
    NON_DSS_MEMBER ("NON_DSS_MEMBER", R.string.proxy_head_type_non_dss_member_lbl),
    INVALID_ENUM   ("-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    ProxyHeadType(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, ProxyHeadType> MAP = new HashMap<>();

    static {
        for (ProxyHeadType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static ProxyHeadType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}
