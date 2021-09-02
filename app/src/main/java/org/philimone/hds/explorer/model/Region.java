package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.StringCollectionConverter;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Transient;
import io.objectbox.annotation.Unique;
import mz.betainteractive.utilities.ReflectionUtils;

@Entity
public class Region implements FormSubject, Serializable {

    public static String HIERARCHY_1 = "hierarchy1";
    public static String HIERARCHY_2 = "hierarchy2";
    public static String HIERARCHY_3 = "hierarchy3";
    public static String HIERARCHY_4 = "hierarchy4";
    public static String HIERARCHY_5 = "hierarchy5";
    public static String HIERARCHY_6 = "hierarchy6";
    public static String HIERARCHY_7 = "hierarchy7";
    public static String HIERARCHY_8 = "hierarchy8";

    @Id
    public long id;
    @Unique
    public String code;
    public String name;
    public String level;
    public String parent;

    @Index
    @Convert(converter = StringCollectionConverter.class, dbType = String.class)
    public Set<String> modules;

    @Transient
    private boolean selected; /*USED ON EXPANDED SELECTION LIST*/

    public Region() {
        this.modules = new HashSet<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }        

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setModules(Collection<? extends String> modules) {
        this.modules.addAll(modules);
    }

    public String getValueByName(String variableName){
        return ReflectionUtils.getValueByName(this, variableName);
    }

    public String getTableName() {
        return "Region";
    }

}
