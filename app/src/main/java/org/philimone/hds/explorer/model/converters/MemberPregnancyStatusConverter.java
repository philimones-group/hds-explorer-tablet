package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.MemberPregnancyStatus;

import io.objectbox.converter.PropertyConverter;

public class MemberPregnancyStatusConverter implements PropertyConverter<MemberPregnancyStatus, String> {
    @Override
    public MemberPregnancyStatus convertToEntityProperty(String databaseValue) {
        return MemberPregnancyStatus.getFrom(databaseValue);
    }

    @Override
    public String convertToDatabaseValue(MemberPregnancyStatus entityProperty) {
        return entityProperty == null ? null : entityProperty.code;
    }
}