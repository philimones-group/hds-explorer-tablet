package org.philimone.hds.explorer.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum HealthcareProviderType {
    HOSPITAL             ("HOSPITAL", "healthcareProvider.hospital"),
    HEALTH_CENTER_CLINIC ("HEALTH_CENTER_CLINIC", "healthcareProvider.health_center_clinic"),
    TRADITIONAL_MIDWIFE  ("TRADITIONAL_MIDWIFE", "healthcareProvider.traditional_midwife"),
    TRADITIONAL_HEALER   ("TRADITIONAL_HEALER", "healthcareProvider.on_the_way"),
    OTHER                ("OTHER", "healthcareProvider.other"),
    INVALID_ENUM           ( "-1", "R.string.invalid_enum_value");

    String code;
    String name;

    HealthcareProviderType(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getId(){
        return code;
    }

    @Override
    public String toString() {
        return name;
    }

    /* Finding Enum by code */
    private static final Map<String, HealthcareProviderType> MAP = new HashMap<>();

    static {
        for (HealthcareProviderType e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static HealthcareProviderType getFrom(String code) {
        return code==null ? null : MAP.get(code);
    }
}
