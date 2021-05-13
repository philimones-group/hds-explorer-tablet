package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.EstimatedDateOfDeliveryType;

import io.objectbox.converter.PropertyConverter;

public class EstimatedDateOfDeliveryTypeConverter implements PropertyConverter<EstimatedDateOfDeliveryType, String> {

    @Override
    public EstimatedDateOfDeliveryType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        EstimatedDateOfDeliveryType entity = EstimatedDateOfDeliveryType.getFrom(databaseValue);

        return entity==null ? EstimatedDateOfDeliveryType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(EstimatedDateOfDeliveryType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
