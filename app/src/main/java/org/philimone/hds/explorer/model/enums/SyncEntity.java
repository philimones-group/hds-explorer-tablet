package org.philimone.hds.explorer.model.enums;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum SyncEntity {

    SETTINGS           (0, R.string.server_sync_bt_settings_lbl, "", ""),
    PARAMETERS         (1, R.string.sync_params_lbl, "params.zip", "params/zip"),
    MODULES            (2, R.string.sync_modules_lbl, "modules.zip", "modules/zip"),
    FORMS              (3, R.string.sync_forms_lbl, "forms.zip", "forms/zip"),
    CORE_FORMS_EXT     (4, R.string.sync_coreforms_exts_lbl, "coreforms.zip", "coreforms/zip"),
    CORE_FORMS_OPTIONS (5, R.string.sync_coreformsoptions_lbl, "coreformsoptions.zip", "coreformsoptions/zip"),
    USERS              (6, R.string.sync_users_lbl, "users.zip", "users/zip"),
    DATASETS           (7, R.string.sync_datasets_lbl, "datasets.zip", "datasets/zip"),
    DATASETS_CSV_FILES (8, R.string.sync_datasets_csv_lbl, "", ""),
    TRACKING_LISTS     (9, R.string.sync_tracking_lists_lbl, "trackinglists.zip", "trackinglists/zip"),
    HOUSEHOLDS_DATASETS(10, R.string.sync_households_datasets_lbl, "", ""),
    ROUNDS             (11, R.string.sync_rounds_lbl, "rounds.zip", "rounds/zip"),
    REGIONS            (12, R.string.sync_regions_lbl, "regions.zip", "regions/zip"),
    HOUSEHOLDS         (13, R.string.sync_households_lbl, "households.zip", "households/zip"),
    MEMBERS            (14, R.string.sync_members_lbl, "members.zip", "members/zip"),
    RESIDENCIES        (15, R.string.sync_residencies_lbl, "residencies.zip", "residencies/zip"),
    DEMOGRAPHICS_EVENTS(16, R.string.sync_demographics_events_lbl, "", ""),
    VISITS             (17, R.string.sync_visits_lbl, "visits.zip", "visits/zip"),
    HEAD_RELATIONSHIPS (18, R.string.sync_head_relationships_lbl, "headrelationships.zip", "hrelationships/zip"),
    MARITAL_RELATIONSHIPS (19, R.string.sync_marital_relationships_lbl, "maritalrelationships.zip", "mrelationships/zip"),
    INMIGRATIONS       (20, R.string.sync_inmigrations_lbl, "inmigrations.zip", "inmigrations/zip"),
    OUTMIGRATIONS      (21, R.string.sync_outmigrations_lbl, "outmigrations.zip", "outmigrations/zip"),
    PREGNANCY_REGISTRATIONS (22, R.string.sync_pregnancy_registrations_lbl, "pregnancyregistrations.zip", "pregnancies/zip"),
    PREGNANCY_OUTCOMES (23, R.string.sync_pregnancy_outcomes_lbl, "pregnancyoutcomes.zip", "pregnancyoutcomes/zip"),
    DEATHS             (24, R.string.sync_deaths_lbl, "deaths.zip", "deaths/zip"),
    INCOMPLETE_VISITS  (25, R.string.sync_incomplete_visits_lbl, "incompletevisits.zip", "incompletevisits/zip"),
    INVALID_ENUM       (-1, -1, "", "");

    private int code;
    private @StringRes int nameId;
    private String zipFile;
    private String urlPath;

    SyncEntity(int code, @StringRes int id, String zipFile, String urlPath){
        this.code = code;
        this.nameId = id;
        this.zipFile = zipFile;
        this.urlPath = urlPath;
    }

    public int getId() {
        return code;
    }

    public @StringRes int getNameId() {
        return nameId;
    }

    public String getZipFile() {
        return zipFile;
    }

    public String getUrlPath() {
        return urlPath;
    }

    /* Finding entity by code */
    private static final Map<Integer, SyncEntity> ENTITIES = new HashMap<>();

    static {
        for (SyncEntity e: values()) {
            ENTITIES.put(e.code, e);
        }
    }

    public static SyncEntity getFrom(int code) {
        return ENTITIES.get(code);
    }
}
