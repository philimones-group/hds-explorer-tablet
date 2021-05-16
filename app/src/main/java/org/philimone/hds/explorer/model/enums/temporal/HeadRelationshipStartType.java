package org.philimone.hds.explorer.model.enums.temporal;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum HeadRelationshipStartType {

    ENUMERATION          ("ENU", R.string.eventType_enumeration),
    BIRTH                ("BIR", R.string.eventType_birth),
    INTERNAL_INMIGRATION ("ENT", R.string.eventType_internal_inmigration),
    EXTERNAL_INMIGRATION ("XEN", R.string.eventType_external_inmigration),
    NEW_HEAD_OF_HOUSEHOLD ("NHH", R.string.eventType_new_hoh), //Event Related to the Head of Household
    INVALID_ENUM          ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    HeadRelationshipStartType(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, HeadRelationshipStartType> MAP = new HashMap<>();

    static {
        for (HeadRelationshipStartType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static HeadRelationshipStartType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}