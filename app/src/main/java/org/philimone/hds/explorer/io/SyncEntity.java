package org.philimone.hds.explorer.io;

import org.philimone.hds.explorer.R;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum SyncEntity implements Serializable {
    SETTINGS           (0, R.string.server_sync_bt_settings_lbl),
    PARAMETERS         (1, R.string.sync_params_lbl),
    MODULES            (2, R.string.sync_modules_lbl),
    FORMS              (3, R.string.sync_forms_lbl),
    DATASETS           (4, R.string.sync_datasets_lbl),
    DATASETS_CSV_FILES (5, R.string.sync_datasets_csv_lbl),
    TRACKING_LISTS     (6, R.string.sync_tracking_lists_lbl),
    USERS              (7, R.string.sync_users_lbl),
    REGIONS            (8, R.string.sync_regions_lbl),
    HOUSEHOLDS         (9, R.string.sync_households_lbl),
    MEMBERS            (10, R.string.sync_members_lbl);

    private int code;
    private int nameId;

    SyncEntity(int code, int id){
        this.code = code;
        this.nameId = id;
    }

    public int getCode() {
        return code;
    }

    public int getNameId() {
        return nameId;
    }

    /* Finding entity by code */
    private static final Map<Integer, SyncEntity> ENTITIES = new HashMap<>();

    static {
        for (SyncEntity e: values()) {
            ENTITIES.put(e.code, e);
        }
    }

    public static SyncEntity valueOf(int code) {
        return ENTITIES.get(code);
    }
}
