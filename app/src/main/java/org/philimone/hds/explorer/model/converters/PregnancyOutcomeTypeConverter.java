package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.PregnancyOutcomeType;

import io.objectbox.converter.PropertyConverter;

public class PregnancyOutcomeTypeConverter implements PropertyConverter<PregnancyOutcomeType, String> {

    @Override
    public PregnancyOutcomeType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        PregnancyOutcomeType entity = PregnancyOutcomeType.getFrom(databaseValue);

        return entity==null ? PregnancyOutcomeType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(PregnancyOutcomeType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
