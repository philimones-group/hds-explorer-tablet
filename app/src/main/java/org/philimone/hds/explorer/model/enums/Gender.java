package org.philimone.hds.explorer.model.enums;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum Gender {

    MALE   ("M", R.string.default_gender_male),
    FEMALE ("F", R.string.default_gender_female),
    NOT_KNOWN ("NA", R.string.default_gender_female),
    INVALID_ENUM    ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    Gender(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, Gender> MAP = new HashMap<>();

    static {
        for (Gender e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static Gender getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}