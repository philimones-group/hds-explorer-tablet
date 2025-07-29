package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.ImmunizationStatus;

import io.objectbox.converter.PropertyConverter;

public class ImmunizationStatusConverter implements PropertyConverter<ImmunizationStatus, String> {

    @Override
    public ImmunizationStatus convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        ImmunizationStatus entity = ImmunizationStatus.getFrom(databaseValue);

        return entity==null ? ImmunizationStatus.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(ImmunizationStatus entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
