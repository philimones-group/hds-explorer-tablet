package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.ProxyHeadChangeReason;

import io.objectbox.converter.PropertyConverter;

public class ProxyHeadChangeReasonConverter implements PropertyConverter<ProxyHeadChangeReason, String> {

    @Override
    public ProxyHeadChangeReason convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        ProxyHeadChangeReason entity = ProxyHeadChangeReason.getFrom(databaseValue);

        return entity==null ? ProxyHeadChangeReason.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(ProxyHeadChangeReason entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
