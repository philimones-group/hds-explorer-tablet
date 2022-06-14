package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.CoreFormRecordType;
import io.objectbox.converter.PropertyConverter;

public class CoreFormRecordTypeConverter implements PropertyConverter<CoreFormRecordType, String> {

    @Override
    public CoreFormRecordType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        CoreFormRecordType entity = CoreFormRecordType.getFrom(databaseValue);

        return entity==null ? CoreFormRecordType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(CoreFormRecordType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
