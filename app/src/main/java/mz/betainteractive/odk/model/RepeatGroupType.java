package mz.betainteractive.odk.model;

import androidx.annotation.StringRes;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum RepeatGroupType {

    RESIDENT_MEMBERS    ("HouseholdResidentMembers"), //""),
    DEAD_MEMBERS        ("HouseholdDeadMembers"), //""),
    OUTMIGRATED_MEMBERS ("HouseholdExtMembers"), //""),
    ALL_MEMBERS         ("HouseholdAllMembers"), //, ""),
    MAPPED_VALUES       ("MappedRepeatValues"),
    INVALID_ENUM        ( "-1"); //, R.string.invalid_enum_value);

    public String code;
    //public @StringRes int name;

    RepeatGroupType(String code) {//, @StringRes int name){
        this.code = code;
        //this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, RepeatGroupType> MAP = new HashMap<>();

    static {
        for (RepeatGroupType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static RepeatGroupType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }

}