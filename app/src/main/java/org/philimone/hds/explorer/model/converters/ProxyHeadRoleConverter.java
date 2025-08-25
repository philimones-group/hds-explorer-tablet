package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.ProxyHeadRole;

import io.objectbox.converter.PropertyConverter;

public class ProxyHeadRoleConverter implements PropertyConverter<ProxyHeadRole, String> {

    @Override
    public ProxyHeadRole convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        ProxyHeadRole entity = ProxyHeadRole.getFrom(databaseValue);

        return entity==null ? ProxyHeadRole.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(ProxyHeadRole entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
