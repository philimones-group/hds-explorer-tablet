package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.IncompleteVisitReason;
import org.philimone.hds.explorer.model.enums.VisitReason;

import io.objectbox.converter.PropertyConverter;

public class IncompleteVisitReasonConverter implements PropertyConverter<IncompleteVisitReason, String> {

    @Override
    public IncompleteVisitReason convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        IncompleteVisitReason entity = IncompleteVisitReason.getFrom(databaseValue);

        return entity==null ? IncompleteVisitReason.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(IncompleteVisitReason entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
