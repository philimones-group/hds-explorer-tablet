package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.HeadRelationshipType;

import io.objectbox.converter.PropertyConverter;

public class HeadRelationshipTypeConverter implements PropertyConverter<HeadRelationshipType, String> {

    @Override
    public HeadRelationshipType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        HeadRelationshipType entity = HeadRelationshipType.getFrom(databaseValue);

        return entity==null ? HeadRelationshipType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(HeadRelationshipType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
