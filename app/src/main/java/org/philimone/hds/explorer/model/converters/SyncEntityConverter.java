package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.SyncEntity;

import io.objectbox.converter.PropertyConverter;

public class SyncEntityConverter implements PropertyConverter<SyncEntity, Integer> {

        @Override
        public SyncEntity convertToEntityProperty(Integer databaseValue) {
            if (databaseValue == null) {
                return null;
            }

            SyncEntity entity = SyncEntity.fromCode(databaseValue);

            return entity==null ? SyncEntity.NOT_APPLICABLE : entity;
        }

    @Override
    public Integer convertToDatabaseValue(SyncEntity entityProperty) {
        return entityProperty == null ? null : entityProperty.getCode();
    }
}
