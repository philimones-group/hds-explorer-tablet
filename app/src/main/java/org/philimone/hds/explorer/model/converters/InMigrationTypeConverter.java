package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.temporal.InMigrationType;

import io.objectbox.converter.PropertyConverter;

public class InMigrationTypeConverter implements PropertyConverter<InMigrationType, String> {

    @Override
    public InMigrationType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        InMigrationType entity = InMigrationType.getFrom(databaseValue);

        return entity==null ? InMigrationType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(InMigrationType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
