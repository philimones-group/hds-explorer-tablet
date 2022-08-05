package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.FormSubjectType;

import io.objectbox.converter.PropertyConverter;

public class FormSubjectTypeConverter implements PropertyConverter<FormSubjectType, String> {

    @Override
    public FormSubjectType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        FormSubjectType entity = FormSubjectType.getFrom(databaseValue);

        return entity==null ? FormSubjectType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(FormSubjectType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
