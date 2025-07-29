package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.IllnessSymptoms;
import io.objectbox.converter.PropertyConverter;

public class IllnessSymptomsConverter implements PropertyConverter<IllnessSymptoms, String> {

    @Override
    public IllnessSymptoms convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        IllnessSymptoms entity = IllnessSymptoms.getFrom(databaseValue);

        return entity==null ? IllnessSymptoms.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(IllnessSymptoms entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
