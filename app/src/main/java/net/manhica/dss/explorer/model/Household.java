package net.manhica.dss.explorer.model;

import android.content.ContentValues;

import net.manhica.dss.explorer.database.DatabaseHelper;
import net.manhica.dss.explorer.database.Table;

import java.io.Serializable;

import mz.betainteractive.utilities.ReflectionUtils;

/**
 * Created by paul on 5/20/16.
 */
public class Household implements Serializable, Table {

    private int id;
    private String extId;

    private String locality;
    private String adminPost;
    private String district;
    private String province;

    private String neighborhood;
    private String houseNumber;
    private String headPermId;
    private String subsHeadPermId;

    private boolean gpsNull;
    private Double gpsAccuracy;
    private Double gpsAltitude;
    private Double gpsLatitude;
    private Double gpsLongitude;

    private Double cosLatitude;
    private Double sinLatitude;
    private Double cosLongitude;
    private Double sinLongitude;

    private Double populationDensity;
    private String densityType;

    /* Extras Variables*/ //separated by semicolon :
    String extrasColumns;
    String extrasValues;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
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

    public String getHeadPermId() {
        return headPermId;
    }

    public void setHeadPermId(String headPermId) {
        this.headPermId = headPermId;
    }

    public String getSubsHeadPermId() {
        return subsHeadPermId;
    }

    public void setSubsHeadPermId(String subsHeadPermId) {
        this.subsHeadPermId = subsHeadPermId;
    }

    public String getExtrasColumns() {
        return extrasColumns;
    }

    public void setExtrasColumns(String extrasColumns) {
        this.extrasColumns = extrasColumns;
    }

    public String getExtrasValues() {
        return extrasValues;
    }

    public void setExtrasValues(String extrasValues) {
        this.extrasValues = extrasValues;
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

    public Double getPopulationDensity() {
        return populationDensity;
    }

    public void setPopulationDensity(Double populationDensity) {
        this.populationDensity = populationDensity;
    }

    public String getDensityType() {
        return densityType;
    }

    public void setDensityType(String densityType) {
        this.densityType = densityType;
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
        cv.put(DatabaseHelper.Household.COLUMN_EXT_ID, extId);
        cv.put(DatabaseHelper.Household.COLUMN_HOUSE_NUMBER, houseNumber);
        cv.put(DatabaseHelper.Household.COLUMN_HEAD_PERM_ID, headPermId);
        cv.put(DatabaseHelper.Household.COLUMN_SUBSHEAD_PERM_ID, subsHeadPermId);
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
        cv.put(DatabaseHelper.Household.COLUMN_POPULATION_DENSITY, populationDensity);
        cv.put(DatabaseHelper.Household.COLUMN_DENSITY_TYPE, densityType);
        cv.put(DatabaseHelper.Household.COLUMN_EXTRAS_COLUMNS, extrasColumns);
        cv.put(DatabaseHelper.Household.COLUMN_EXTRAS_VALUES, extrasValues);

        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.Household.ALL_COLUMNS;
    }
}
