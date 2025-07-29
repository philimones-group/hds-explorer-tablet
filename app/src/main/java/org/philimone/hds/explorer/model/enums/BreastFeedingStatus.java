package org.philimone.hds.explorer.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum BreastFeedingStatus {
    EXCLUSIVE         ("EXCLUSIVE", "breastFeedingStatus.exclusive"),
    PARTIAL           ("PARTIAL", "breastFeedingStatus.partial"),
    NOT_BREASTFEEDING ("NOT_BREASTFEEDING", "breastFeedingStatus.not_breatfeeding"),
    INVALID_ENUM           ( "-1", "R.string.invalid_enum_value");

    String code;
    String name;

    BreastFeedingStatus(String code, String name){
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
    private static final Map<String, BreastFeedingStatus> MAP = new HashMap<>();

    static {
        for (BreastFeedingStatus e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static BreastFeedingStatus getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}
