package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.temporal.RegionHeadEndType;

import io.objectbox.converter.PropertyConverter;

public class RegionHeadEndTypeConverter implements PropertyConverter<RegionHeadEndType, String> {

    @Override
    public RegionHeadEndType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        RegionHeadEndType entity = RegionHeadEndType.getFrom(databaseValue);

        return entity==null ? RegionHeadEndType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(RegionHeadEndType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
