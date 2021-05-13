package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.SyncStatus;

import io.objectbox.converter.PropertyConverter;

public class SyncStatusConverter implements PropertyConverter<SyncStatus, Integer> {

        @Override
        public SyncStatus convertToEntityProperty(Integer databaseValue) {
            if (databaseValue == null) {
                return null;
            }

            SyncStatus entity = SyncStatus.getFrom(databaseValue);

            return entity==null ? SyncStatus.INVALID_ENUM : entity;
        }

    @Override
    public Integer convertToDatabaseValue(SyncStatus entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
