package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.MaritalStatus;

import io.objectbox.converter.PropertyConverter;

public class MaritalStatusConverter implements PropertyConverter<MaritalStatus, String> {

    @Override
    public MaritalStatus convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        MaritalStatus entity = MaritalStatus.getFrom(databaseValue);

        return entity==null ? MaritalStatus.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(MaritalStatus entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
