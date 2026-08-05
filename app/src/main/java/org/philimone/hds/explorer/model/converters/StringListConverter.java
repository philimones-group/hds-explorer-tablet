package org.philimone.hds.explorer.model.converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.objectbox.converter.PropertyConverter;
import mz.betainteractive.utilities.StringUtil;

public class StringListConverter implements PropertyConverter<List<String>, String> {

    @Override
    public List<String> convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return new ArrayList<>();
        }

        List<String> list = new ArrayList<>();

        if (!StringUtil.isBlank(databaseValue)) {
            list.addAll(Arrays.asList(databaseValue.split(",")));
        }

        return list;
    }

    @Override
    public String convertToDatabaseValue(List<String> entityProperty) {
        if (entityProperty == null || entityProperty.isEmpty()) return null;
        
        StringBuilder str = new StringBuilder();

        for (String value : entityProperty) {
            str.append((str.length() == 0 ? "" : ",") + value);
        }

        return str.toString();
    }

}
