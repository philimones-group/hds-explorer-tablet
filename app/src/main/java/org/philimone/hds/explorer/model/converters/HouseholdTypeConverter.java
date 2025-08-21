package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.HouseholdType;
import io.objectbox.converter.PropertyConverter;

public class HouseholdTypeConverter implements PropertyConverter<HouseholdType, String> {

    @Override
    public HouseholdType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        HouseholdType entity = HouseholdType.getFrom(databaseValue);

        return entity==null ? HouseholdType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(HouseholdType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
