package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.NoVisitReason;

import io.objectbox.converter.PropertyConverter;

public class NoVisitReasonConverter implements PropertyConverter<NoVisitReason, String> {

    @Override
    public NoVisitReason convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        NoVisitReason entity = NoVisitReason.getFrom(databaseValue);

        return entity==null ? NoVisitReason.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(NoVisitReason entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
