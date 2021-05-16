package org.philimone.hds.explorer.model.enums.temporal;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum HeadRelationshipEndType {

    NOT_APPLICABLE        ("NA", R.string.eventType_not_applicable), //Currently Living Here
    INTERNAL_OUTMIGRATION ("CHG", R.string.eventType_internal_outmigration),
    EXTERNAL_OUTMIGRATION ("EXT", R.string.eventType_external_outmigration),
    DEATH                 ("DTH", R.string.eventType_death),
    DEATH_OF_HEAD_OF_HOUSEHOLD ("DHH", R.string.eventType_death_of_hoh), //Event Related to the Head of Household
    CHANGE_OF_HEAD_OF_HOUSEHOLD("CHH", R.string.eventType_change_of_hoh), //Event Related to the Head of Household
    INVALID_ENUM          ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    HeadRelationshipEndType(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, HeadRelationshipEndType> MAP = new HashMap<>();

    static {
        for (HeadRelationshipEndType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static HeadRelationshipEndType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}