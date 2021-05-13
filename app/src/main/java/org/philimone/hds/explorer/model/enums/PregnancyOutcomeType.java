package org.philimone.hds.explorer.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum PregnancyOutcomeType {

    LIVEBIRTH    ("LBR", "pregOutcomeType.livebirth"),
    STILLBIRTH   ("SBR",	"pregOutcomeType.stillbirth"),
    MISCARRIAGE  ("MIS",	"pregOutcomeType.miscarriage"),
    ABORTION     ("ABT", "pregOutcomeType.abortion"),
    INVALID_ENUM ( "-1", "-1");

    String code;
    String name;

    PregnancyOutcomeType(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, PregnancyOutcomeType> MAP = new HashMap<>();

    static {
        for (PregnancyOutcomeType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static PregnancyOutcomeType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }

}