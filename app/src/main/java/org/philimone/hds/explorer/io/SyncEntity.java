package org.philimone.hds.explorer.io;

import org.philimone.hds.explorer.R;

public enum SyncEntity {
    //SETTINGS           (0, R.string.sync_se),
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
}
