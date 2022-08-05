package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.FormCollectType;
import io.objectbox.converter.PropertyConverter;

public class FormCollectTypeConverter implements PropertyConverter<FormCollectType, String> {

    @Override
    public FormCollectType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        FormCollectType entity = FormCollectType.getFrom(databaseValue);

        return entity==null ? FormCollectType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(FormCollectType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
