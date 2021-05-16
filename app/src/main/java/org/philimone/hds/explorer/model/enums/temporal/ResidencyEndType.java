package org.philimone.hds.explorer.model.enums.temporal;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum ResidencyEndType {

    NOT_APPLICABLE        ("NA", R.string.eventType_not_applicable), //Currently Living Here
    INTERNAL_OUTMIGRATION ("CHG", R.string.eventType_internal_outmigration),
    EXTERNAL_OUTMIGRATION ("EXT", R.string.eventType_external_outmigration),
    DEATH                 ("DTH", R.string.eventType_death),
    INVALID_ENUM          ( "-1", R.string.invalid_enum_value);

    final public String code;
    final public @StringRes int name;

    ResidencyEndType(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, ResidencyEndType> MAP = new HashMap<>();


    static {
        for (ResidencyEndType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static ResidencyEndType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}