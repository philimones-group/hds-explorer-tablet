package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.BirthPlace;
import org.philimone.hds.explorer.model.enums.VisitLocationItem;

import io.objectbox.converter.PropertyConverter;

public class BirthPlaceConverter implements PropertyConverter<BirthPlace, String> {

    @Override
    public BirthPlace convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        BirthPlace entity = BirthPlace.getFrom(databaseValue);

        return entity==null ? BirthPlace.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(BirthPlace entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
