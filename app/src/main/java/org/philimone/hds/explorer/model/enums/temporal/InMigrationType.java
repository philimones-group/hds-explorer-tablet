package org.philimone.hds.explorer.model.enums.temporal;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum InMigrationType {

    INTERNAL     ("ENT", R.string.eventType_internal_inmigration),
    EXTERNAL     ("XEN", R.string.eventType_external_inmigration),
    INVALID_ENUM ( "-1", R.string.invalid_enum_value);
    //RETURNING ( "XEN", R.string.eventType.returning_inmigration) /* I dont think I will need this, entry_date and start_type suits enough to know that its a return to dss */

    public String code;
    public @StringRes int name;

    InMigrationType(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, InMigrationType> MAP = new HashMap<>();

    static {
        for (InMigrationType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static InMigrationType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}