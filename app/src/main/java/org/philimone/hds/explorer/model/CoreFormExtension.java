package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.MapStringConverter;

import java.util.LinkedHashMap;
import java.util.Map;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class CoreFormExtension {

    @Id
    public long id;

    public String formName;

    @Unique
    public String formId;

    public String extFormId;

    public boolean required = false;

    public boolean enabled = false;

    @Convert(converter = MapStringConverter.class, dbType = String.class)
    public Map<String, String> columnsMapping;

    public CoreFormExtension(){
        columnsMapping = new LinkedHashMap<>();
    }

}
