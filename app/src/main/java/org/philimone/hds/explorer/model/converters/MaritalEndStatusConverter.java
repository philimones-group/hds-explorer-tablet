package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.MaritalEndStatus;

import io.objectbox.converter.PropertyConverter;

public class MaritalEndStatusConverter implements PropertyConverter<MaritalEndStatus, String> {

    @Override
    public MaritalEndStatus convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        MaritalEndStatus entity = MaritalEndStatus.getFrom(databaseValue);

        return entity==null ? MaritalEndStatus.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(MaritalEndStatus entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
