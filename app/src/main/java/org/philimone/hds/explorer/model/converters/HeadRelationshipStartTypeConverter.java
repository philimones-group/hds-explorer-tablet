package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipStartType;

import io.objectbox.converter.PropertyConverter;

public class HeadRelationshipStartTypeConverter implements PropertyConverter<HeadRelationshipStartType, String> {

    @Override
    public HeadRelationshipStartType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        HeadRelationshipStartType entity = HeadRelationshipStartType.getFrom(databaseValue);

        return entity==null ? HeadRelationshipStartType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(HeadRelationshipStartType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
