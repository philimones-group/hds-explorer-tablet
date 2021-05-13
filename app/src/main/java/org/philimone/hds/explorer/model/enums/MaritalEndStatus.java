package org.philimone.hds.explorer.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum MaritalEndStatus {

    NOT_APPLICABLE ("NA", "maritalStatus.not_applicable"), //Currently Living Here
    DIVORCED       ("DIV", "maritalStatus.divorced"),
    SEPARATED      ("SEP","maritalStatus.separated"),
    WIDOWED        ("WID","maritalStatus.widowed"),
    INVALID_ENUM   ( "-1", "-1");

    String code;
    String name;

    MaritalEndStatus(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, MaritalEndStatus> MAP = new HashMap<>();

    static {
        for (MaritalEndStatus e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static MaritalEndStatus getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }

}
