package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.Gender;

import io.objectbox.converter.PropertyConverter;

public class GenderConverter implements PropertyConverter<Gender, String> {

    @Override
    public Gender convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        Gender entity = Gender.getFrom(databaseValue);

        return entity==null ? Gender.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(Gender entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
