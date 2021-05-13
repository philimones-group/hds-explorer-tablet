package org.philimone.hds.explorer.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum VisitLocationItem {
    /*
    12. Onde é que a visita esta sendo realizada?
    */

    HOME         ("HOME", "visitLocation.home"),
    WORK_PLACE   ("WORKPLACE","visitLocation.workplace"),
    HEALTH_UNIT  ("HEALTHUNIT","visitLocation.healthunit"),
    OTHER_PLACE  ("OTHERPLACE","visitLocation.other"),
    INVALID_ENUM ( "-1", "-1");

    String code;
    String name;

    VisitLocationItem(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, VisitLocationItem> MAP = new HashMap<>();

    static {
        for (VisitLocationItem e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static VisitLocationItem getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}