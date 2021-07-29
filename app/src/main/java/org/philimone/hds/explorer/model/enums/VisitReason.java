package org.philimone.hds.explorer.model.enums;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum VisitReason {
    /*
    12. Onde é que a visita esta sendo realizada?
    */

    UPDATE_ROUNDS ("UPDATE", R.string.visitReason_update_round),
    BASELINE      ("BASELINE",R.string.visitReason_baseline),
    NEW_HOUSEHOLD ("NEW_HOUSE",R.string.visitReason_newhouse),
    DATA_CLEANING ("CLEAN",R.string.visitReason_dataclean),
    OTHER         ("OTHER", R.string.visitReason_other),
    INVALID_ENUM ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    VisitReason(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, VisitReason> MAP = new HashMap<>();

    static {
        for (VisitReason e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static VisitReason getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}