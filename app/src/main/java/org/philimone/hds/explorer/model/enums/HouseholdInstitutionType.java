package org.philimone.hds.explorer.model.enums;

import androidx.annotation.StringRes;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum HouseholdInstitutionType {
    ORPHANAGE              ("ORP", R.string.household_institution_type_orphanage_lbl, R.string.household_institution_type_orphanage_desc),
    MENTAL_HEALTH_FACILITY ("MHF", R.string.household_institution_type_mental_health_facility_lbl, R.string.household_institution_type_mental_health_facility_desc),
    ELDERLY_CARE_HOME("ECH", R.string.household_institution_type_elderly_care_home_lbl, R.string.household_institution_type_elderly_care_home_desc),
    BOARDING_SCHOOL("BSC", R.string.household_institution_type_boarding_school_lbl, R.string.household_institution_type_boarding_school_desc),
    UNIVERSITY_DORM("UDM", R.string.household_institution_type_university_dorm_lbl, R.string.household_institution_type_university_dorm_desc),
    HOSTEL("HST", R.string.household_institution_type_hostel_lbl, R.string.household_institution_type_hostel_desc),
    RELIGIOUS_INSTITUTION("REL", R.string.household_institution_type_religious_institution_lbl, R.string.household_institution_type_religious_institution_desc),
    REFUGEE_CAMP("RFC", R.string.household_institution_type_refugee_camp_lbl, R.string.household_institution_type_refugee_camp_desc),
    IDP_CAMP("IDC", R.string.household_institution_type_idp_camp_lbl, R.string.household_institution_type_idp_camp_desc),
    PRISON("PRI", R.string.household_institution_type_prison_lbl, R.string.household_institution_type_prison_desc),
    JUVENILE_DETENTION("JDT", R.string.household_institution_type_juvenile_detention_lbl, R.string.household_institution_type_juvenile_detention_desc),
    MILITARY_BARRACKS("MBR", R.string.household_institution_type_military_barracks_lbl, R.string.household_institution_type_military_barracks_desc),
    REHABILITATION_CENTER("RHC", R.string.household_institution_type_rehabilitation_center_lbl, R.string.household_institution_type_rehabilitation_center_desc),
    LONG_STAY_HOSPITAL("LSH", R.string.household_institution_type_long_stay_hospital_lbl, R.string.household_institution_type_long_stay_hospital_desc),
    HOMELESS_SHELTER("HMS", R.string.household_institution_type_homeless_shelter_lbl, R.string.household_institution_type_homeless_shelter_desc),
    WORK_CAMP("WKP", R.string.household_institution_type_work_camp_lbl, R.string.household_institution_type_work_camp_desc),
    OTHER("OTH", R.string.household_institution_type_other_lbl, R.string.household_institution_type_other_desc),
    INVALID_ENUM ( "-1", R.string.invalid_enum_value, R.string.invalid_enum_value);

    public String code;
    public @StringRes int  name;   // i18n key
    public @StringRes int  description;

    HouseholdInstitutionType(String code, @StringRes int  name, @StringRes int  description){
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getId(){ return code; }

    private static final Map<String, HouseholdInstitutionType> MAP = new HashMap<>();
    static {
        for (HouseholdInstitutionType e : values()) {
            MAP.put(e.code, e);
        }
    }

    public static HouseholdInstitutionType getFrom(String code) {
        return code == null ? null : MAP.get(code);
    }
}
