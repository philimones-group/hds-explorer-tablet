package org.philimone.hds.explorer.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum SyncStatus {

    STATUS_NOT_SYNCED (0),
    STATUS_SYNCED     (1),
    STATUS_SYNC_ERROR (2),
    NOT_APPLICABLE    (-1);

    private int code;

    SyncStatus(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /* Finding entity by code */
    private static final Map<Integer, SyncStatus> STATUSES = new HashMap<>();

    static {
        for (SyncStatus e: values()) {
            STATUSES.put(e.code, e);
        }
    }

    public static SyncStatus fromCode(int code) {
        return STATUSES.get(code);
    }

}
