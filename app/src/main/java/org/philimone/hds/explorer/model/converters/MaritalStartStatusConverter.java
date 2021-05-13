package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.MaritalStartStatus;

import io.objectbox.converter.PropertyConverter;

public class MaritalStartStatusConverter implements PropertyConverter<MaritalStartStatus, String> {

    @Override
    public MaritalStartStatus convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        MaritalStartStatus entity = MaritalStartStatus.getFrom(databaseValue);

        return entity==null ? MaritalStartStatus.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(MaritalStartStatus entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
