package org.philimone.hds.explorer.model.enums;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum EstimatedDateOfDeliveryType {

    ULTRASOUND             ("1", R.string.eddtype_ultrasound),
    LAST_MENSTRUAL_PERIOD  ("2", R.string.eddtype_last_menstrual_period),
    SYMPHISIS_FUNDAL_EIGHT ("3", R.string.eddtype_symphisis_fundal_eight),
    DONT_KNOW              ("99", R.string.eddtype_dont_know),
    INVALID_ENUM           ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    EstimatedDateOfDeliveryType(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, EstimatedDateOfDeliveryType> MAP = new HashMap<>();

    static {
        for (EstimatedDateOfDeliveryType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static EstimatedDateOfDeliveryType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}