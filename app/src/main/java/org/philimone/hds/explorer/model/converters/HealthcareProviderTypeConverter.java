package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.HealthcareProviderType;
import io.objectbox.converter.PropertyConverter;

public class HealthcareProviderTypeConverter implements PropertyConverter<HealthcareProviderType, String> {

    @Override
    public HealthcareProviderType convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        HealthcareProviderType entity = HealthcareProviderType.getFrom(databaseValue);

        return entity==null ? HealthcareProviderType.INVALID_ENUM : entity;
    }

    @Override
    public String convertToDatabaseValue(HealthcareProviderType entityProperty) {
        return entityProperty == null ? null : entityProperty.getId();
    }
}
