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

    private String gpsAccuracy;
    private String gpsAltitude;
    private String gpsLatitude;
    private String gpsLongitude;
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

    public String getGpsAccuracy() {
        return gpsAccuracy;
    }

    public void setGpsAccuracy(String gpsAccuracy) {
        this.gpsAccuracy = gpsAccuracy;
    }

    public String getGpsAltitude() {
        return gpsAltitude;
    }

    public void setGpsAltitude(String gpsAltitude) {
        this.gpsAltitude = gpsAltitude;
    }

    public String getGpsLatitude() {
        return gpsLatitude;
    }

    public void setGpsLatitude(String gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
    }

    public String getGpsLongitude() {
        return gpsLongitude;
    }

    public void setGpsLongitude(String gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
    }

    public boolean hasNullCoordinates(){
        return gpsLatitude==null || gpsLatitude.isEmpty() || gpsLongitude==null || gpsLongitude.isEmpty();
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
        cv.put(DatabaseHelper.Household.COLUMN_GPS_ACCURACY, gpsAccuracy);
        cv.put(DatabaseHelper.Household.COLUMN_GPS_ALTITUDE, gpsAltitude);
        cv.put(DatabaseHelper.Household.COLUMN_GPS_LATITUDE, gpsLatitude);
        cv.put(DatabaseHelper.Household.COLUMN_GPS_LONGITUDE, gpsLongitude);
        cv.put(DatabaseHelper.Household.COLUMN_EXTRAS_COLUMNS, extrasColumns);
        cv.put(DatabaseHelper.Household.COLUMN_EXTRAS_VALUES, extrasValues);

        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.Household.ALL_COLUMNS;
    }
}
