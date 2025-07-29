package org.philimone.hds.explorer.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum IllnessSymptoms {
    FEVER              ("FEVER", "childIllnessSymptom.fever"),
    DIARRHEA           ("DIARRHEA", "childIllnessSymptom.diarrhea"),
    COUGH              ("COUGH", "childIllnessSymptom.cough"),
    BREATHING_PROBLEMS ("BREATHING_PROBLEMS", "childIllnessSymptom.breathing_problems"),
    RASH               ("RASH", "childIllnessSymptom.rash"),
    VOMITING           ("VOMITING", "childIllnessSymptom.vomiting"),
    EYE_DISCHARGE      ("EYE_DISCHARGE", "childIllnessSymptom.eye_discharge"),
    OTHER              ("OTHER", "childIllnessSymptom.other"),
    INVALID_ENUM           ( "-1", "R.string.invalid_enum_value");

    String code;
    String name;

    IllnessSymptoms(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getId() {
        return code;
    }

    private static final Map<String, IllnessSymptoms> MAP = new HashMap<>();

    static {
        for (IllnessSymptoms e: values()) {
            MAP.put(e.code, e);
        }
    }

    public static IllnessSymptoms getFrom(String code) {
        return code == null ? null : MAP.get(code);
    }

    @Override
    public String toString() {
        return name;
    }
}
