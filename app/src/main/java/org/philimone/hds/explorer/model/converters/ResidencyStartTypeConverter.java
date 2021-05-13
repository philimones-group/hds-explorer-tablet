package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.temporal.ResidencyStartType;

import io.objectbox.converter.PropertyConverter;

public class ResidencyStartTypeConverter implements PropertyConverter<ResidencyStartType, String> {

    @Override
    public ResidencyStartType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        ResidencyStartType entity = ResidencyStartType.getFrom(databaseValue);

        return entity==null ? ResidencyStartType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(ResidencyStartType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
