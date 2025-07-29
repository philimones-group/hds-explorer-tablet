package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.NewBornStatus;
import io.objectbox.converter.PropertyConverter;

public class NewBornStatusConverter implements PropertyConverter<NewBornStatus, String> {

    @Override
    public NewBornStatus convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        NewBornStatus entity = NewBornStatus.getFrom(databaseValue);

        return entity==null ? NewBornStatus.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(NewBornStatus entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
