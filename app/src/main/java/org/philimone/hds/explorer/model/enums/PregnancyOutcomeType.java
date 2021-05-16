package org.philimone.hds.explorer.model.enums;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum PregnancyOutcomeType {

    LIVEBIRTH    ("LBR", R.string.pregOutcomeType_livebirth),
    STILLBIRTH   ("SBR", R.string.pregOutcomeType_stillbirth),
    MISCARRIAGE  ("MIS", R.string.pregOutcomeType_miscarriage),
    ABORTION     ("ABT", R.string.pregOutcomeType_abortion),
    INVALID_ENUM ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    PregnancyOutcomeType(String code, @StringRes int name){
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