package org.philimone.hds.explorer.model.enums.temporal;

import java.util.HashMap;
import java.util.Map;

public enum OutMigrationType {

    INTERNAL     ("CHG", "eventType.internal_outmigration"),
    EXTERNAL     ("EXT", "eventType.external_outmigration"),
    INVALID_ENUM ( "-1", "-1");

    final String code;
    final String name;

    OutMigrationType(String code, String name){
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