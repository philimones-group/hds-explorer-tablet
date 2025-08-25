package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.ProxyHeadType;

import io.objectbox.converter.PropertyConverter;

public class ProxyHeadTypeConverter implements PropertyConverter<ProxyHeadType, String> {

    @Override
    public ProxyHeadType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        ProxyHeadType entity = ProxyHeadType.getFrom(databaseValue);

        return entity==null ? ProxyHeadType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(ProxyHeadType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
