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

    private String locality;
    private String adminPost;
    private String district;
    private String province;

    private String neighborhood;
    private String name;
    private String headCode;
    private String secHeadCode;

    private boolean gpsNull;
    private Double gpsAccuracy;
    private Double gpsAltitude;
    private Double gpsLatitude;
    private Double gpsLongitude;

    private Double cosLatitude;
    private Double sinLatitude;
    private Double cosLongitude;
    private Double sinLongitude;

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

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getAdminPost() {
        return adminPost;
    }

    public void setAdminPost(String adminPost) {
        this.adminPost = adminPost;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
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
        cv.put(DatabaseHelper.Household.COLUMN_SECHEAD_CODE, secHeadCode);
        cv.put(DatabaseHelper.Household.COLUMN_NEIGHBORHOOD, neighborhood);
        cv.put(DatabaseHelper.Household.COLUMN_LOCALITY, locality);
        cv.put(DatabaseHelper.Household.COLUMN_ADMIN_POST, adminPost);
        cv.put(DatabaseHelper.Household.COLUMN_DISTRICT, district);
        cv.put(DatabaseHelper.Household.COLUMN_PROVINCE, province);
        cv.put(DatabaseHelper.Household.COLUMN_GPS_NULL, gpsNull ? 1 : 0);
        cv.put(DatabaseHelper.Household.COLUMN_GPS_ACCURACY, gpsAccuracy);
        cv.put(DatabaseHelper.Household.COLUMN_GPS_ALTITUDE, gpsAltitude);
        cv.put(DatabaseHelper.Household.COLUMN_GPS_LATITUDE, gpsLatitude);
        cv.put(DatabaseHelper.Household.COLUMN_GPS_LONGITUDE, gpsLongitude);
        cv.put(DatabaseHelper.Household.COLUMN_COS_LATITUDE, cosLatitude);
        cv.put(DatabaseHelper.Household.COLUMN_SIN_LATITUDE, sinLatitude);
        cv.put(DatabaseHelper.Household.COLUMN_COS_LONGITUDE, cosLongitude);
        cv.put(DatabaseHelper.Household.COLUMN_SIN_LONGITUDE, sinLongitude);

        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.Household.ALL_COLUMNS;
    }
}
