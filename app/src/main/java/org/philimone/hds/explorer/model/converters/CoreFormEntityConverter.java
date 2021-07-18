package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.PregnancyStatus;

import io.objectbox.converter.PropertyConverter;

public class CoreFormEntityConverter implements PropertyConverter<CoreFormEntity, String> {

    @Override
    public CoreFormEntity convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        CoreFormEntity entity = CoreFormEntity.getFrom(databaseValue);

        return entity==null ? CoreFormEntity.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(CoreFormEntity entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
