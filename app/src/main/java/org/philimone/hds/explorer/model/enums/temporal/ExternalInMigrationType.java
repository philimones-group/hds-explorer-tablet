package org.philimone.hds.explorer.model.enums.temporal;

import androidx.annotation.StringRes;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum ExternalInMigrationType {

    ENTRY        ("ENTRY", R.string.external_inmigration_entry_lbl),
    REENTRY      ("REENTRY", R.string.external_inmigration_reentry_lbl),
    INVALID_ENUM ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    ExternalInMigrationType(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, ExternalInMigrationType> MAP = new HashMap<>();

    static {
        for (ExternalInMigrationType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static ExternalInMigrationType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}