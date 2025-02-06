package org.philimone.hds.explorer.model;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;


/**
 * Represents an application parameter or setting to be saved on the database
 */
@Entity
public class ApplicationParam {

    public static final String APP_URL = "app-url";
    public static final String ODK_URL = "odk-url";
    public static final String REDCAP_URL = "redcap-url";
    public static final String HFORM_POST_EXECUTION = "hds-execute-on-upload";
    public static final String LOGGED_USER = "logged-user";
    public static final String PARAMS_TRACKLIST_MAX_DATA_COLUMNS = "hds.explorer.trackinglists.max_data_columns";
    public static final String PARAMS_GENDER_CHECKING = "hds.explorer.constraints.gender.checking";
    public static final String PARAMS_MIN_AGE_OF_FATHER = "hds.explorer.constraints.father.age.min";
    public static final String PARAMS_MIN_AGE_OF_MOTHER = "hds.explorer.constraints.mother.age.min";
    public static final String PARAMS_MIN_AGE_OF_HEAD   = "hds.explorer.constraints.head.age.min";
    public static final String PARAMS_MIN_AGE_OF_SPOUSE   = "hds.explorer.constraints.spouse.age.min";
    public static final String PARAMS_MIN_AGE_OF_RESPONDENT   = "hds.explorer.constraints.respondent.age.min";
    public static final String PARAMS_SYSTEM_LANGUAGE = "hds.explorer.system.language";
    public static final String PARAMS_SYSTEM_CODE_GENERATOR = "hds.explorer.system.codegenerator";
    public static final String PARAMS_SYSTEM_CODE_GENERATOR_INCREMENTAL_RULE = "hds.explorer.system.codegenerator_rules.incremental";
    public static final String PARAMS_SYSTEM_REGION_HEAD_SUPPORT = "hds.explorer.system.region.head.support";
    public static final String PARAMS_SYSTEM_VISIT_GPS_REQUIRED = "hds.explorer.system.visit.gps.required";

    @Id
    private long id;
    @Unique
    public String name;
    public String type;
    public String value;

    public ApplicationParam(){

    }

    public ApplicationParam(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
