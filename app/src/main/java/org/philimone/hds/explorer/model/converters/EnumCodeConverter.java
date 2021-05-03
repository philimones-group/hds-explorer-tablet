package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.EnumCode;

import io.objectbox.converter.PropertyConverter;

public class EnumCodeConverter<T extends Enum<T> & EnumCode> implements PropertyConverter<T, Integer> {

    @Override
    public T convertToEntityProperty(Integer databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        /*
        for (EnumCode anEnum : T.values()) {
            if (anEnum.getCode() == databaseValue) {
                return anEnum;
            }
        }*/


        return null;
    }

    @Override
    public Integer convertToDatabaseValue(T entityProperty) {
        return entityProperty == null ? null : entityProperty.getCode();
    }

}
