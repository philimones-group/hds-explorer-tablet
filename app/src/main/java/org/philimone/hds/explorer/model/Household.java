package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.HouseholdInstitutionTypeConverter;
import org.philimone.hds.explorer.model.converters.HouseholdStatusConverter;
import org.philimone.hds.explorer.model.converters.HouseholdTypeConverter;
import org.philimone.hds.explorer.model.converters.ProxyHeadRoleConverter;
import org.philimone.hds.explorer.model.converters.StringCollectionConverter;
import org.philimone.hds.explorer.model.enums.HouseholdInstitutionType;
import org.philimone.hds.explorer.model.enums.HouseholdStatus;
import org.philimone.hds.explorer.model.enums.HouseholdType;
import org.philimone.hds.explorer.model.enums.ProxyHeadRole;
import org.philimone.hds.explorer.model.enums.SubjectEntity;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;
import mz.betainteractive.utilities.ReflectionUtils;

/**
 * Created by paul on 5/20/16.
 */

@Entity
public class Household implements CoreEntity, FormSubject, Serializable {

    @Id
    public long id;
    @Index
    public String prefixCode;
    @Unique
    public String code;
    @Index
    public String region;
    @Convert(converter = HouseholdTypeConverter.class, dbType = String.class)
    public HouseholdType type;
    @Convert(converter = HouseholdInstitutionTypeConverter.class, dbType = String.class)
    public HouseholdInstitutionType institutionType;
    public String institutionTypeOther;

    public String name;
    @Index
    public String headCode;
    public String headName;

    public String hierarchy1;
    public String hierarchy2;
    public String hierarchy3;
    public String hierarchy4;
    public String hierarchy5;
    public String hierarchy6;
    public String hierarchy7;
    public String hierarchy8;


    public boolean gpsNull;
    public Double gpsAccuracy;
    public Double gpsAltitude;
    public Double gpsLatitude;
    public Double gpsLongitude;

    public Double cosLatitude;
    public Double sinLatitude;
    public Double cosLongitude;
    public Double sinLongitude;

    @Convert(converter = HouseholdStatusConverter.class, dbType = String.class)
    public HouseholdStatus status;

    @Unique
    public String collectedId;
    public boolean recentlyCreated = false;
    public String recentlyCreatedUri;

    /* shared and pre-register settings */
    public boolean shareable;
    public boolean preRegistered;

    @Index
    @Convert(converter = StringCollectionConverter.class, dbType = String.class)
    public Set<String> modules;

    public Household() {
        this.modules = new HashSet<>();
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getHierarchy1() {
        return hierarchy1;
    }

    public void setHierarchy1(String hierarchy1) {
        this.hierarchy1 = hierarchy1;
    }

    public String getHierarchy2() {
        return hierarchy2;
    }

    public void setHierarchy2(String hierarchy2) {
        this.hierarchy2 = hierarchy2;
    }

    public String getHierarchy3() {
        return hierarchy3;
    }

    public void setHierarchy3(String hierarchy3) {
        this.hierarchy3 = hierarchy3;
    }

    public String getHierarchy4() {
        return hierarchy4;
    }

    public void setHierarchy4(String hierarchy4) {
        this.hierarchy4 = hierarchy4;
    }

    public String getHierarchy5() {
        return hierarchy5;
    }

    public void setHierarchy5(String hierarchy5) {
        this.hierarchy5 = hierarchy5;
    }

    public String getHierarchy6() {
        return hierarchy6;
    }

    public void setHierarchy6(String hierarchy6) {
        this.hierarchy6 = hierarchy6;
    }

    public String getHierarchy7() {
        return hierarchy7;
    }

    public void setHierarchy7(String hierarchy7) {
        this.hierarchy7 = hierarchy7;
    }

    public String getHierarchy8() {
        return hierarchy8;
    }

    public void setHierarchy8(String hierarchy8) {
        this.hierarchy8 = hierarchy8;
    }

    public String getHeadCode() {
        return headCode;
    }

    public void setHeadCode(String headCode) {
        this.headCode = headCode;
    }

    public String getHeadName() {
        return headName;
    }

    public void setHeadName(String headName) {
        this.headName = headName;
    }

    public Double getGpsAccuracy() {
        return gpsAccuracy;
    }

    public void setGpsAccuracy(Double gpsAccuracy) {
        this.gpsAccuracy = gpsAccuracy;
    }

    public Double getGpsAltitude() {
        return gpsAltitude;
    }

    public void setGpsAltitude(Double gpsAltitude) {
        this.gpsAltitude = gpsAltitude;
    }

    public Double getGpsLatitude() {
        return gpsLatitude;
    }

    public void setGpsLatitude(Double gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
    }

    public Double getGpsLongitude() {
        return gpsLongitude;
    }

    public void setGpsLongitude(Double gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
    }

    public Double getCosLatitude() {
        return cosLatitude;
    }

    public void setCosLatitude(Double cosLatitude) {
        this.cosLatitude = cosLatitude;
    }

    public Double getSinLatitude() {
        return sinLatitude;
    }

    public void setSinLatitude(Double sinLatitude) {
        this.sinLatitude = sinLatitude;
    }

    public Double getSinLongitude() {
        return sinLongitude;
    }

    public void setSinLongitude(Double sinLongitude) {
        this.sinLongitude = sinLongitude;
    }

    public Double getCosLongitude() {
        return cosLongitude;
    }

    public void setCosLongitude(Double cosLongitude) {
        this.cosLongitude = cosLongitude;
    }

    public boolean isGpsNull() {
        return gpsNull;
    }

    public void setGpsNull(boolean gpsNull) {
        this.gpsNull = gpsNull;
    }

    public void setRecentlyCreated(boolean recentlyCreated) {
        this.recentlyCreated = recentlyCreated;
    }

    public void setModules(Collection<? extends String> modules) {
        this.modules.addAll(modules);
    }

    public void updateGpsCalculations(){
        if (gpsLatitude != null) {
            sinLatitude = Math.sin(gpsLatitude);
            cosLatitude = Math.cos(gpsLatitude);
        }

        if (gpsLongitude != null) {
            sinLongitude = Math.sin(gpsLongitude);
            cosLongitude = Math.cos(gpsLongitude);
        }
    }

    public static Household getEmptyHousehold(String code){
        Household h = getEmptyHousehold();
        h.code = code;
        return h;
    }

    public static Household getEmptyHousehold(){
        Household household = new Household();
        household.code = "";
        household.name = "";
        household.region = "";
        household.headCode = "";
        household.headName = "";

        household.hierarchy1 = "";
        household.hierarchy2 = "";
        household.hierarchy3 = "";
        household.hierarchy4 = "";
        household.hierarchy5 = "";
        household.hierarchy6 = "";
        household.hierarchy7 = "";
        household.hierarchy8 = "";


        household.gpsNull = true;
        household.gpsAccuracy = 0.0;
        household.gpsAltitude = 0.0;
        household.gpsLatitude = 0.0;
        household.gpsLongitude = 0.0;

        household.cosLatitude = 0.0;
        household.sinLatitude = 0.0;
        household.cosLongitude = 0.0;
        household.sinLongitude = 0.0;


        return household;
    }

    public String getValueByName(String variableName){
        return ReflectionUtils.getValueByName(this, variableName);
    }

    @Override
    public SubjectEntity getTableName() {
        return SubjectEntity.HOUSEHOLD;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getCollectedId() {
        return collectedId;
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
