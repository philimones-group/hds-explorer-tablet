package org.philimone.hds.explorer.model.converters;

import java.util.LinkedHashMap;
import java.util.Map;

import io.objectbox.converter.PropertyConverter;

public class LabelMappingConverter implements PropertyConverter<Map<String, String>, String> {

    private String labelsText = "";

    @Override
    public Map<String, String> convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        return convertLabelMapTextToMap(databaseValue);
    }

    @Override
    public String convertToDatabaseValue(Map<String, String> entityProperty) {

        entityProperty.forEach( (key, value) -> {
                if (labelsText.isEmpty()) {
                    labelsText = key+":"+value;
                } else {
                    labelsText += ";" + key+":"+value;
                }
            }
        );

        return labelsText;
    }

    private Map<String, String> convertLabelMapTextToMap(String labelsMapText) {
        Map<String, String> mapLabels = new LinkedHashMap<>();

        if (labelsMapText != null && !labelsMapText.isEmpty()){
            //this.labels.clear();

            String[] entries = labelsMapText.split(";");
            for (String entry : entries){
                String[] keyValue = entry.split(":");
                if (keyValue.length == 2){
                    mapLabels.put(keyValue[0], keyValue[1]);
                }
            }
        }

        return mapLabels;
    }
}
