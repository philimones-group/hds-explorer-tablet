package org.philimone.hds.explorer.model.enums;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum HeadRelationshipType {

    HEAD_OF_HOUSEHOLD ("HOH", R.string.headRelationshipType_head_of_household),
    SPOUSE            ("SPO", R.string.headRelationshipType_spouse),
    SON_DAUGHTER      ("SON", R.string.headRelationshipType_son_daughter),
    BROTHER_SISTER    ("BRO", R.string.headRelationshipType_brother_sister),
    PARENT            ("PAR", R.string.headRelationshipType_parent),
    GRANDCHILD        ("GCH", R.string.headRelationshipType_grandchild),
    NOT_RELATED       ("NOR", R.string.headRelationshipType_not_related),
    OTHER_RELATIVE    ("OTH", R.string.headRelationshipType_other),
    DONT_KNOW         ("DNK", R.string.headRelationshipType_dont_know),
    INVALID_ENUM      ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    HeadRelationshipType(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, HeadRelationshipType> MAP = new HashMap<>();

    static {
        for (HeadRelationshipType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static HeadRelationshipType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }

}