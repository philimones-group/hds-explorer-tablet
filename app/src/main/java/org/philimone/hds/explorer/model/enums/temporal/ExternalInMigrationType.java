package org.philimone.hds.explorer.model.enums.temporal;

import java.util.HashMap;
import java.util.Map;

public enum ExternalInMigrationType {

    ENTRY,
    REENTRY,
    INVALID_ENUM;

    /* Finding Enum by code */
    private static final Map<String, ExternalInMigrationType> MAP = new HashMap<>();

    static {
        for (ExternalInMigrationType e: values()) {
            MAP.put(e.name(), e);
        }
    }

    public static ExternalInMigrationType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}