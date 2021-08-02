package org.philimone.hds.explorer.model.enums;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringRes;

public enum SyncEntity {

    SETTINGS           (0, R.string.server_sync_bt_settings_lbl),
    PARAMETERS         (1, R.string.sync_params_lbl),
    MODULES            (2, R.string.sync_modules_lbl),
    FORMS              (3, R.string.sync_forms_lbl),
    USERS              (4, R.string.sync_users_lbl),
    DATASETS           (5, R.string.sync_datasets_lbl),
    DATASETS_CSV_FILES (6, R.string.sync_datasets_csv_lbl),
    TRACKING_LISTS     (7, R.string.sync_tracking_lists_lbl),
    HOUSEHOLDS_DATASETS(8, R.string.sync_households_datasets_lbl),
    ROUNDS             (9, R.string.sync_rounds_lbl),
    REGIONS            (10, R.string.sync_regions_lbl),
    HOUSEHOLDS         (11, R.string.sync_households_lbl),
    MEMBERS            (12, R.string.sync_members_lbl),
    RESIDENCIES        (13, R.string.sync_residencies_lbl),
    DEMOGRAPHICS_EVENTS(14, R.string.sync_demographics_events_lbl),
    VISITS             (15, R.string.sync_visits_lbl),
    HEAD_RELATIONSHIPS (16, R.string.sync_head_relationships_lbl),
    MARITAL_RELATIONSHIPS (17, R.string.sync_marital_relationships_lbl),
    PREGNANCY_REGISTRATIONS (18, R.string.sync_pregnancies_lbl),
    INVALID_ENUM       (-1, -1);

    private int code;
    private @StringRes int nameId;

    SyncEntity(int code, @StringRes int id){
        this.code = code;
        this.nameId = id;
    }

    public int getId() {
        return code;
    }

    public @StringRes int getNameId() {
        return nameId;
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
