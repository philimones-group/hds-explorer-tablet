package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.StringCollectionConverter;
import org.philimone.hds.explorer.model.enums.SubjectEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Transient;
import io.objectbox.annotation.Unique;
import mz.betainteractive.utilities.ReflectionUtils;

@Entity
public class Region implements CoreEntity, FormSubject, Serializable {

    public static String HIERARCHY_1 = "hierarchy1";
    public static String HIERARCHY_2 = "hierarchy2";
    public static String HIERARCHY_3 = "hierarchy3";
    public static String HIERARCHY_4 = "hierarchy4";
    public static String HIERARCHY_5 = "hierarchy5";
    public static String HIERARCHY_6 = "hierarchy6";
    public static String HIERARCHY_7 = "hierarchy7";
    public static String HIERARCHY_8 = "hierarchy8";
    public static String HIERARCHY_9 = "hierarchy9";
    public static String HIERARCHY_10 = "hierarchy10";
    public static List<String> ALL_HIERARCHIES = Arrays.asList(HIERARCHY_1, HIERARCHY_2, HIERARCHY_3, HIERARCHY_4, HIERARCHY_5,
                                                               HIERARCHY_6, HIERARCHY_7, HIERARCHY_8, HIERARCHY_9, HIERARCHY_10);

    @Id
    public long id;
    @Unique
    public String code;
    public String name;
    public String level;
    public String parent;

    @Unique
    public String collectedId;
    public boolean recentlyCreated = false;
    public String recentlyCreatedUri;

    /* shared and pre-register settings */
    public boolean shareable;
    public boolean preRegistration;

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

    @Override
    public SubjectEntity getTableName() {
        return SubjectEntity.REGION;
    }

    @Override
    public String getCollectedId() {
        return this.collectedId;
    }

    @Override
    public boolean isRecentlyCreated() {
        return this.recentlyCreated;
    }

    @Override
    public String getRecentlyCreatedUri() {
        return this.recentlyCreatedUri;
    }

}
