package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.FormType;

import io.objectbox.converter.PropertyConverter;

public class FormTypeConverter implements PropertyConverter<FormType, String> {

    @Override
    public FormType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        FormType entity = FormType.getFrom(databaseValue);

        return entity==null ? FormType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(FormType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
