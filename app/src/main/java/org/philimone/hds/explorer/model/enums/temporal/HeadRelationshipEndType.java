package org.philimone.hds.explorer.model.enums.temporal;

import java.util.HashMap;
import java.util.Map;

public enum HeadRelationshipEndType {

    NOT_APPLICABLE        ("NA", "eventType.not_applicable"), //Currently Living Here
    INTERNAL_OUTMIGRATION ("CHG", "eventType.internal_outmigration"),
    EXTERNAL_OUTMIGRATION ("EXT", "eventType.external_outmigration"),
    DEATH                 ("DTH", "eventType.death"),
    DEATH_OF_HEAD_OF_HOUSEHOLD ("DHH", "eventType.death_of_hoh"), //Event Related to the Head of Household
    CHANGE_OF_HEAD_OF_HOUSEHOLD("CHH", "eventType.change_of_hoh"), //Event Related to the Head of Household
    INVALID_ENUM          ( "-1", "-1");

    final String code;
    final String name;

    HeadRelationshipEndType(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    /* Finding Enum by code */
    private static final Map<String, HeadRelationshipEndType> MAP = new HashMap<>();

    static {
        for (HeadRelationshipEndType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static HeadRelationshipEndType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}