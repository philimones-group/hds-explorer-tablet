package org.philimone.hds.explorer.model.enums;

import androidx.annotation.StringRes;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum IncompleteVisitReason {
    /*
    12. Onde é que a visita esta sendo realizada?
    */

    UNAVAILABLE_TODAY ("UNAVAILABLE_TODAY", R.string.incompleteVisitReason_unavail_today),30+++++

    UNAVAILABLE_ROUND ("UNAVAILABLE_ROUND",R.string.incompleteVisitReason_unavail_round),
    UNWILLING         ("UNWILLING",R.string.incompleteVisitReason_unwilling),
    HOSPITALIZED      ("HOSPITALIZED",R.string.incompleteVisitReason_hospitalized),
    RELOCATED         ("RELOCATED",R.string.incompleteVisitReason_relocated),
    WITHDREW          ("WITHDREW",R.string.incompleteVisitReason_withdrew),
    DEAD              ("DEAD",R.string.incompleteVisitReason_dead),
    OTHER             ("OTHER", R.string.incompleteVisitReason_other),
    INVALID_ENUM      ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    IncompleteVisitReason(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, IncompleteVisitReason> MAP = new HashMap<>();

    static {
        for (IncompleteVisitReason e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static IncompleteVisitReason getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}