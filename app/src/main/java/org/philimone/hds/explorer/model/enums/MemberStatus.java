package org.philimone.hds.explorer.model.enums;

import androidx.annotation.StringRes;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum MemberStatus {
    /*
    Status of a Member
    */
    ACTIVE ("ACTIVE", R.string.memberStatus_active),
    INACTIVE ("INACTIVE", R.string.memberStatus_inactive),
    MEMBER_NOT_FOUND ("MEMBER_NOT_FOUND", R.string.memberStatus_not_found),

    INVALID_ENUM ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    MemberStatus(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, MemberStatus> MAP = new HashMap<>();

    static {
        for (MemberStatus e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static MemberStatus getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}