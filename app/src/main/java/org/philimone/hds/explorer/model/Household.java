package org.philimone.hds.explorer.model;

import android.content.ContentValues;

import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;

import java.io.Serializable;

import mz.betainteractive.utilities.ReflectionUtils;

/**
 * Created by paul on 5/20/16.
 */
public class Household implements Serializable, Table {

    private int id;
    private String code;
    private String region;
    private String name;
    private String headCode;
    private String headName;
    private String secHeadCode;

    private String hierarchy1;
    private String hierarchy2;
    private String hierarchy3;
    private String hierarchy4;
    private String hierarchy5;
    private String hierarchy6;
    private String hierarchy7;
    private String hierarchy8;


    private boolean gpsNull;
    private Double gpsAccuracy;
    private Double gpsAltitude;
    private Double gpsLatitude;
    private Double gpsLongitude;

    private Double cosLatitude;
    private Double sinLatitude;
    private Double cosLongitude;
    private Double sinLongitude;

    private Boolean recentlyCreated = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getSecHeadCode() {
        return secHeadCode;
    }

    public void setSecHeadCode(String secHeadCode) {
        this.secHeadCode = secHeadCode;
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

    public Boolean isRecentlyCreated() {
        return recentlyCreated;
    }

    public void setRecentlyCreated(Boolean recentlyCreated) {
        this.recentlyCreated = recentlyCreated;
    }

    public String getValueByName(String variableName){
        return ReflectionUtils.getValueByName(this, variableName);
    }

    @Override
    public String getTableName() {
        return DatabaseHelper.Household.TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.Household.COLUMN_CODE, code);
        cv.put(DatabaseHelper.Household.COLUMN_NAME, name);
        cv.put(DatabaseHelper.Household.COLUMN_HEAD_CODE, headCode);
        cv.put(DatabaseHelper.Household.COLUMN_HEAD_NAME, headName);
        cv.put(DatabaseHelper.Household.COLUMN_SECHEAD_CODE, secHeadCode);
        cv.put(DatabaseHelper.Household.COLUMN_REGION, region);
        cv.put(DatabaseHelper.Household.COLUMN_HIERARCHY_1, hierarchy1);
        cv.put(DatabaseHelper.Household.COLUMN_HIERARCHY_2, hierarchy2);
        cv.put(DatabaseHelper.Household.COLUMN_HIERARCHY_3, hierarchy3);
        cv.put(DatabaseHelper.Household.COLUMN_HIERARCHY_4, hierarchy4);
        cv.put(DatabaseHelper.Household.COLUMN_HIERARCHY_5, hierarchy5);
        cv.put(DatabaseHelper.Household.COLUMN_HIERARCHY_6, hierarchy6);
        cv.put(DatabaseHelper.Household.COLUMN_HIERARCHY_7, hierarchy7);
        cv.put(DatabaseHelper.Household.COLUMN_HIERARCHY_8, hierarchy8);
        cv.put(DatabaseHelper.Household.COLUMN_GPS_NULL, gpsNull ? 1 : 0);
        cv.put(DatabaseHelper.Household.COLUMN_GPS_ACCURACY, gpsAccuracy);
        cv.put(DatabaseHelper.Household.COLUMN_GPS_ALTITUDE, gpsAltitude);
        cv.put(DatabaseHelper.Household.COLUMN_GPS_LATITUDE, gpsLatitude);
        cv.put(DatabaseHelper.Household.COLUMN_GPS_LONGITUDE, gpsLongitude);
        cv.put(DatabaseHelper.Household.COLUMN_COS_LATITUDE, cosLatitude);
        cv.put(DatabaseHelper.Household.COLUMN_SIN_LATITUDE, sinLatitude);
        cv.put(DatabaseHelper.Household.COLUMN_COS_LONGITUDE, cosLongitude);
        cv.put(DatabaseHelper.Household.COLUMN_SIN_LONGITUDE, sinLongitude);
        cv.put(DatabaseHelper.Household.COLUMN_RECENTLY_CREATED, recentlyCreated ? 1 : 0);

        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.Household.ALL_COLUMNS;
    }
}
