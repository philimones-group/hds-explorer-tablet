package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.VisitReason;

import io.objectbox.converter.PropertyConverter;

public class VisitReasonConverter implements PropertyConverter<VisitReason, String> {

    @Override
    public VisitReason convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        VisitReason entity = VisitReason.getFrom(databaseValue);

        return entity==null ? VisitReason.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(VisitReason entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
