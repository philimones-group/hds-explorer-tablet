package org.philimone.hds.explorer.model.enums;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum VisitLocationItem {
    /*
    12. Onde é que a visita esta sendo realizada?
    */

    HOME         ("HOME", R.string.visitLocation_home),
    WORK_PLACE   ("WORKPLACE", R.string.visitLocation_workplace),
    HEALTH_UNIT  ("HEALTHUNIT", R.string.visitLocation_healthunit),
    OTHER_PLACE  ("OTHERPLACE", R.string.visitLocation_other),
    INVALID_ENUM ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    VisitLocationItem(String code, @StringRes int name){
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