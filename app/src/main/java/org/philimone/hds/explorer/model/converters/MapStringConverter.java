package org.philimone.hds.explorer.model.converters;

import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;

import io.objectbox.converter.PropertyConverter;

public class MapStringConverter implements PropertyConverter<Map<String, String>, String> {

    private String mapAsText = "";

    @Override
    public Map<String, String> convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        return convertFormMapTextToMap(databaseValue);
    }

    @Override
    public String convertToDatabaseValue(Map<String, String> entityProperty) {

        entityProperty.forEach( (key, value) -> {
            final String map = key + "<#>" + value;
            if (mapAsText.isEmpty()){
                mapAsText = map;
            } else {
                mapAsText += ";" + map;
            }
        });

        return mapAsText;
    }

    private Map<String, String> convertFormMapTextToMap(String formMapText) {
        Map<String, String> map = new LinkedHashMap<>();

        if (formMapText != null && !formMapText.isEmpty()){
            //map.clear();

            String[] entries = formMapText.split(";");
            for (String entry : entries){
                String[] keyValue = entry.split("<#>");
                if (keyValue.length == 2){
                    map.put(keyValue[0], keyValue[1]); //mapping unique items (odk variables) as Key, the values a the domain column names (TableName.columnName)
                }
            }
        }

        return map;
    }
}
