package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.BreastFeedingStatus;

import io.objectbox.converter.PropertyConverter;

public class BreastFeedingStatusConverter implements PropertyConverter<BreastFeedingStatus, String> {

    @Override
    public BreastFeedingStatus convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        BreastFeedingStatus entity = BreastFeedingStatus.getFrom(databaseValue);

        return entity==null ? BreastFeedingStatus.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(BreastFeedingStatus entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
