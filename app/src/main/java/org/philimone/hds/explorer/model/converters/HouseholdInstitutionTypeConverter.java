package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.HouseholdInstitutionType;
import io.objectbox.converter.PropertyConverter;

public class HouseholdInstitutionTypeConverter implements PropertyConverter<HouseholdInstitutionType, String> {

    @Override
    public HouseholdInstitutionType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        HouseholdInstitutionType entity = HouseholdInstitutionType.getFrom(databaseValue);

        return entity==null ? HouseholdInstitutionType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(HouseholdInstitutionType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
