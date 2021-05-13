package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipEndType;

import io.objectbox.converter.PropertyConverter;

public class HeadRelationshipEndTypeConverter implements PropertyConverter<HeadRelationshipEndType, String> {

    @Override
    public HeadRelationshipEndType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        HeadRelationshipEndType entity = HeadRelationshipEndType.getFrom(databaseValue);

        return entity==null ? HeadRelationshipEndType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(HeadRelationshipEndType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
