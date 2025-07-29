package org.philimone.hds.explorer.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum ImmunizationStatus {
    UP_TO_DATE       ("UP_TO_DATE", "childImmunizationStatus.up_to_date"),
    PARTIALLY_DONE   ("PARTIALLY_DONE", "childImmunizationStatus.partially_done"),
    NOT_DONE         ("NOT_DONE", "childImmunizationStatus.not_done"),
    UNKNOWN          ("UNKNOWN", "childImmunizationStatus.unknown"),
    INVALID_ENUM           ( "-1", "R.string.invalid_enum_value");

    String code;
    String name;

    ImmunizationStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getId() {
        return code;
    }

    static final Map<String, ImmunizationStatus> MAP = new HashMap<>();

    static {
        for (ImmunizationStatus e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static ImmunizationStatus getFrom(String code) {
        return code == null ? null : MAP.get(code);
    }

    @Override
    public String toString() {
        return name;
    }
}
