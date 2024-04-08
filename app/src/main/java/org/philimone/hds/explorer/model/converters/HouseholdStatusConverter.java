package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.HouseholdStatus;

import io.objectbox.converter.PropertyConverter;

public class HouseholdStatusConverter implements PropertyConverter<HouseholdStatus, String> {

    @Override
    public HouseholdStatus convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        HouseholdStatus entity = HouseholdStatus.getFrom(databaseValue);

        return entity==null ? HouseholdStatus.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(HouseholdStatus entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
