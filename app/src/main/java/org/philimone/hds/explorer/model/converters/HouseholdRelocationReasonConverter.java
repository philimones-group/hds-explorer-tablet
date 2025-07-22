package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.HouseholdRelocationReason;
import org.philimone.hds.explorer.model.enums.NoVisitReason;

import io.objectbox.converter.PropertyConverter;

public class HouseholdRelocationReasonConverter implements PropertyConverter<HouseholdRelocationReason, String> {

    @Override
    public HouseholdRelocationReason convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        HouseholdRelocationReason entity = HouseholdRelocationReason.getFrom(databaseValue);

        return entity==null ? HouseholdRelocationReason.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(HouseholdRelocationReason entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
