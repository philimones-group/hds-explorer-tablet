package org.philimone.hds.explorer.model.enums;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum MaritalEndStatus {

    NOT_APPLICABLE ("NA", R.string.maritalStatus_not_applicable), //Currently Living Here
    DIVORCED       ("DIV", R.string.maritalStatus_divorced),
    SEPARATED      ("SEP", R.string.maritalStatus_separated),
    WIDOWED        ("WID", R.string.maritalStatus_widowed),
    INVALID_ENUM   ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    MaritalEndStatus(String code, @StringRes int name){
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
