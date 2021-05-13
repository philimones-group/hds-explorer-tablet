package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.VisitLocationItem;

import io.objectbox.converter.PropertyConverter;

public class VisitLocationItemConverter implements PropertyConverter<VisitLocationItem, String> {

    @Override
    public VisitLocationItem convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        VisitLocationItem entity = VisitLocationItem.getFrom(databaseValue);

        return entity==null ? VisitLocationItem.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(VisitLocationItem entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
