package org.philimone.hds.explorer.model.enums;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

import org.philimone.hds.explorer.R;

public enum SubjectEntity {

    USER        ("User", R.string.form_subject_user_lbl),
    REGION      ("Region", R.string.form_subject_region_lbl),
    HOUSEHOLD   ("Household", R.string.form_subject_household_lbl),
    MEMBER      ("Member", R.string.form_subject_member_lbl),
    VISIT       ("Visit", R.string.form_subject_visit_lbl),

    INVALID_ENUM    ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    SubjectEntity(String code, @StringRes int nameRes){
        this.code = code;
        this.name = nameRes;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, SubjectEntity> MAP = new HashMap<>();

    static {
        for (SubjectEntity e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static SubjectEntity getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}