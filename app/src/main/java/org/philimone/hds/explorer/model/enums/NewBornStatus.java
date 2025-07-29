package org.philimone.hds.explorer.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum NewBornStatus {
    ALIVE      ("ALIVE", "newBornStatus.alive"),
    STILLBORN  ("SBR", "newBornStatus.stillborn"),
    MISCARRIAGE("MIS", "newBornStatus.miscarriage"),
    ABORTION   ("ABT", "newBornStatus.abortion"),
    DIED_AFTER_BIRTH ("DIED_AFTER_BIRTH", "newBornStatus.died_after_birth"),
    INVALID_ENUM           ( "-1", "R.string.invalid_enum_value");

    String code;
    String name;

    NewBornStatus(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    @Override
    public String toString() {
        return name;
    }

    /* Finding Enum by code */
    private static final Map<String, NewBornStatus> MAP = new HashMap<>();

    static {
        for (NewBornStatus e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static NewBornStatus getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}
