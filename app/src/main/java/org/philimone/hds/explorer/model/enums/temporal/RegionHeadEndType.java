package org.philimone.hds.explorer.model.enums.temporal;

import androidx.annotation.StringRes;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum RegionHeadEndType {

    NOT_APPLICABLE        ("NA", R.string.eventType_not_applicable), //Currently Living Here
    EXTERNAL_OUTMIGRATION ("EXT", R.string.eventType_external_outmigration),
    DEATH_OF_HEAD         ("DHR", R.string.eventType_death_of_hor), //Event Related to the Head of Household
    CHANGE_OF_HEAD_OF_REGION ("CHR", R.string.eventType_change_of_hor), //Event Related to the Head of Household
    INVALID_ENUM          ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    RegionHeadEndType(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, RegionHeadEndType> MAP = new HashMap<>();

    static {
        for (RegionHeadEndType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static RegionHeadEndType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}