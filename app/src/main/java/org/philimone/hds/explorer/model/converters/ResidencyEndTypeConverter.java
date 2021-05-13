package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;

import io.objectbox.converter.PropertyConverter;

public class ResidencyEndTypeConverter implements PropertyConverter<ResidencyEndType, String> {

    @Override
    public ResidencyEndType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        ResidencyEndType entity = ResidencyEndType.getFrom(databaseValue);

        return entity==null ? ResidencyEndType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(ResidencyEndType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
