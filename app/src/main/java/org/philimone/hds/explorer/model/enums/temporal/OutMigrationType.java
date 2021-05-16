package org.philimone.hds.explorer.model.enums.temporal;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum OutMigrationType {

    INTERNAL     ("CHG", R.string.eventType_internal_outmigration),
    EXTERNAL     ("EXT", R.string.eventType_external_outmigration),
    INVALID_ENUM ( "-1", R.string.invalid_enum_value);

    final public String code;
    final public @StringRes int name;

    OutMigrationType(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, OutMigrationType> MAP = new HashMap<>();

    static {
        for (OutMigrationType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static OutMigrationType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}