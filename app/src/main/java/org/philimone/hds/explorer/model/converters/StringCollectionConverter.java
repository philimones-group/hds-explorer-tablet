package org.philimone.hds.explorer.model.converters;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.objectbox.converter.PropertyConverter;
import mz.betainteractive.utilities.StringUtil;

public class StringCollectionConverter implements PropertyConverter<Set<String>, String> {

    @Override
    public Set<String> convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        return getCollectionFrom(databaseValue);
    }

    @Override
    public String convertToDatabaseValue(Set<String> entityProperty) {
        return getStringFrom(entityProperty);
    }

    public Set<String> getCollectionFrom(String databaseValue) {
        Set<String> list = new HashSet<>();

        if (!StringUtil.isBlank(databaseValue)) {
            list.addAll(Arrays.asList(databaseValue.split(",")));
        }

        return list;
    }

    public String getStringFrom(Collection<? extends String> entityProperty) {
        StringBuilder str = new StringBuilder();

        entityProperty.forEach( value -> {
            str.append((str.length()==0 ? "" : ",") + value);
        });

        return str.toString();
    }

}
