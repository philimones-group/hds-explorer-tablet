package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.SubjectEntity;

import io.objectbox.converter.PropertyConverter;

public class SubjectEntityConverter implements PropertyConverter<SubjectEntity, String> {

    @Override
    public SubjectEntity convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        SubjectEntity entity = SubjectEntity.getFrom(databaseValue);

        return entity==null ? SubjectEntity.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(SubjectEntity entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
