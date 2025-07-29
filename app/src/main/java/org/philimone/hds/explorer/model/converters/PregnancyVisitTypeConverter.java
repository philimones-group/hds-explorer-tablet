package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.PregnancyVisitType;

import io.objectbox.converter.PropertyConverter;

public class PregnancyVisitTypeConverter implements PropertyConverter<PregnancyVisitType, String> {

    @Override
    public PregnancyVisitType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        PregnancyVisitType entity = PregnancyVisitType.getFrom(databaseValue);

        return entity==null ? PregnancyVisitType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(PregnancyVisitType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
