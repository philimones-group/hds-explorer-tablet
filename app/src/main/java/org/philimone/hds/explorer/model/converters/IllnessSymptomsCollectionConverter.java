package org.philimone.hds.explorer.model.converters;

import org.philimone.hds.explorer.model.enums.IllnessSymptoms;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import io.objectbox.converter.PropertyConverter;
import mz.betainteractive.utilities.StringUtil;

public class IllnessSymptomsCollectionConverter implements PropertyConverter<Set<IllnessSymptoms>, String> {

    @Override
    public Set<IllnessSymptoms> convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        return getCollectionFrom(databaseValue);
    }

    @Override
    public String convertToDatabaseValue(Set<IllnessSymptoms> entityProperty) {
        return getStringFrom(entityProperty);
    }

    public static Set<IllnessSymptoms> getCollectionFrom(String databaseValue) {
        Set<IllnessSymptoms> list = new LinkedHashSet<>();

        if (!StringUtil.isBlank(databaseValue)) {
            for (String opt : databaseValue.split(",")) {
                IllnessSymptoms symptoms = IllnessSymptoms.getFrom(opt);
                if (symptoms != null)
                    list.add(symptoms);
            }
        }

        return list;
    }

    public static String getStringFrom(Collection<? extends IllnessSymptoms> entityProperty) {
        StringBuilder str = new StringBuilder();

        entityProperty.forEach( value -> {
            str.append((str.length()==0 ? "" : ",") + value.getId());
        });

        return str.toString();
    }

}
