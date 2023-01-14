package org.philimone.hds.explorer.model.enums;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum CoreFormEntity {

    REGION                 ("rawRegion",                R.string.core_entity_region_lbl),
    HOUSEHOLD              ("rawHousehold",             R.string.core_entity_household_lbl),
    MEMBER_ENU             ("rawMemberEnu",             R.string.core_entity_member_enu_lbl),
    HEAD_RELATIONSHIP      ("rawHeadRelationship",      R.string.core_entity_head_relationship_lbl),
    MARITAL_RELATIONSHIP   ("rawMaritalRelationship",   R.string.core_entity_marital_relationship_lbl),
    INMIGRATION            ("rawInMigration",           R.string.core_entity_inmigration_lbl),
    EXTERNAL_INMIGRATION   ("rawExternalInMigration",   R.string.core_entity_external_inmigration_lbl),
    OUTMIGRATION           ("rawOutMigration",          R.string.core_entity_outmigration_lbl),
    PREGNANCY_REGISTRATION ("rawPregnancyRegistration", R.string.core_entity_pregnancy_reg_lbl),
    PREGNANCY_OUTCOME      ("rawPregnancyOutcome",      R.string.core_entity_pregnancy_out_lbl),
    DEATH                  ("rawDeath",                 R.string.core_entity_death_lbl),
    CHANGE_HOUSEHOLD_HEAD  ("rawChangeHead",            R.string.core_entity_changehoh_lbl),
    INCOMPLETE_VISIT       ("rawMemberNotVisited",      R.string.core_entity_member_not_visited_lbl),
    VISIT                  ("rawVisit",                 R.string.core_entity_visit_lbl),
    EDITED_REGION          ("rawEditRegion",            R.string.core_entity_edit_region_lbl),
    EDITED_HOUSEHOLD       ("rawEditHousehold",         R.string.core_entity_edit_household_lbl),
    EDITED_MEMBER          ("rawEditMember",            R.string.core_entity_edit_member_lbl),

    INVALID_ENUM    ( "-1", R.string.invalid_enum_value);

    public String code;
    public @StringRes int name;

    CoreFormEntity(String code, @StringRes int name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, CoreFormEntity> MAP = new HashMap<>();

    static {
        for (CoreFormEntity e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static CoreFormEntity getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}