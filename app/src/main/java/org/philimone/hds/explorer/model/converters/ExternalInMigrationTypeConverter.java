package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.temporal.ExternalInMigrationType;

import io.objectbox.converter.PropertyConverter;

public class ExternalInMigrationTypeConverter implements PropertyConverter<ExternalInMigrationType, String> {

    @Override
    public ExternalInMigrationType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        ExternalInMigrationType entity = ExternalInMigrationType.getFrom(databaseValue);

        return entity==null ? ExternalInMigrationType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(ExternalInMigrationType entityProperty) {
        return entityProperty == null ? null : entityProperty.name();
    }
}
