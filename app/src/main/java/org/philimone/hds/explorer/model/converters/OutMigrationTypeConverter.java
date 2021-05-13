package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.temporal.OutMigrationType;

import io.objectbox.converter.PropertyConverter;

public class OutMigrationTypeConverter implements PropertyConverter<OutMigrationType, String> {

    @Override
    public OutMigrationType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        OutMigrationType entity = OutMigrationType.getFrom(databaseValue);

        return entity==null ? OutMigrationType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(OutMigrationType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
