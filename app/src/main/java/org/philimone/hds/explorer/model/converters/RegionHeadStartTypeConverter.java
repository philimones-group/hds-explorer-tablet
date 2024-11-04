package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.temporal.RegionHeadStartType;

import io.objectbox.converter.PropertyConverter;

public class RegionHeadStartTypeConverter implements PropertyConverter<RegionHeadStartType, String> {

    @Override
    public RegionHeadStartType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        RegionHeadStartType entity = RegionHeadStartType.getFrom(databaseValue);

        return entity==null ? RegionHeadStartType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(RegionHeadStartType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
