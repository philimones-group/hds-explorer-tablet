package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.MemberStatus;

import io.objectbox.converter.PropertyConverter;

public class MemberStatusConverter implements PropertyConverter<MemberStatus, String> {

    @Override
    public MemberStatus convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        MemberStatus entity = MemberStatus.getFrom(databaseValue);

        return entity==null ? MemberStatus.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(MemberStatus entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
