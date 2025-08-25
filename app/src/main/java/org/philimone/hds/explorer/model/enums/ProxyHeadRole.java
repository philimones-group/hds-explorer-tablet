package org.philimone.hds.explorer.model.enums;

import androidx.annotation.StringRes;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum ProxyHeadRole {
    ADMINISTRATOR           ("ADM", R.string.proxy_head_role_administrator_lbl),
    WARDEN                  ("WRD", R.string.proxy_head_role_warden_lbl),
    SOCIAL_WORKER           ("SOC", R.string.proxy_head_role_social_worker_lbl),
    CARETAKER               ("CTK", R.string.proxy_head_role_caretaker_lbl),
    LEGAL_GUARDIAN          ("LGU", R.string.proxy_head_role_legal_guardian_lbl),
    FAMILY_REPRESENTATIVE   ("FAM", R.string.proxy_head_role_family_representative_lbl),
    COMMUNITY_REPRESENTATIVE("COM", R.string.proxy_head_role_community_representative_lbl),
    RELIGIOUS_LEADER        ("REL", R.string.proxy_head_role_religious_leader_lbl),
    MILITARY_COMMANDER      ("MIL", R.string.proxy_head_role_military_commander_lbl),
    PRINCIPAL               ("PRI", R.string.proxy_head_role_principal_lbl),
    DIRECTOR                ("DIR", R.string.proxy_head_role_director_lbl),
    OTHER                   ("OTH", R.string.proxy_head_role_other_lbl),
    INVALID_ENUM            ("-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int  name;

    ProxyHeadRole(String code, @StringRes int  name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    private static final Map<String, ProxyHeadRole> MAP = new HashMap<>();

    static {
        for (ProxyHeadRole e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static ProxyHeadRole getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}
