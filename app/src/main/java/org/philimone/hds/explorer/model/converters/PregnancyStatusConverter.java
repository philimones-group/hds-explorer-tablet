package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.PregnancyStatus;

import io.objectbox.converter.PropertyConverter;

public class PregnancyStatusConverter implements PropertyConverter<PregnancyStatus, String> {

    @Override
    public PregnancyStatus convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        PregnancyStatus entity = PregnancyStatus.getFrom(databaseValue);

        return entity==null ? PregnancyStatus.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(PregnancyStatus entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
