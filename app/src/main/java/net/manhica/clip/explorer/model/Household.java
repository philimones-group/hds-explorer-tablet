package net.manhica.clip.explorer.model;

import android.content.ContentValues;

import net.manhica.clip.explorer.database.DatabaseHelper;
import net.manhica.clip.explorer.database.Table;

import java.io.Serializable;

/**
 * Created by paul on 5/20/16.
 */
public class Household implements Serializable, Table {

    private int id;
    private String extId;
    private String headExtId;
    private String houseNumber;
    private String neighborhood;
    private String locality;
    private String adminPost;
    private String district;
    private String province;
    private String head;
    private String accuracy;
    private String altitude;
    private String latitude;
    private String longitude;

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

    public String getHeadExtId() {
        return headExtId;
    }

    public void setHeadExtId(String headExtId) {
        this.headExtId = headExtId;
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

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @Override
    public String getTableName() {
        return DatabaseHelper.Household.TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.Household.COLUMN_EXT_ID, extId);
        cv.put(DatabaseHelper.Household.COLUMN_HEAD_EXT_ID, headExtId);
        cv.put(DatabaseHelper.Household.COLUMN_HOUSE_NUMBER, houseNumber);
        cv.put(DatabaseHelper.Household.COLUMN_NEIGHBORHOOD, neighborhood);
        cv.put(DatabaseHelper.Household.COLUMN_LOCALITY, locality);
        cv.put(DatabaseHelper.Household.COLUMN_ADMIN_POST, adminPost);
        cv.put(DatabaseHelper.Household.COLUMN_DISTRICT, district);
        cv.put(DatabaseHelper.Household.COLUMN_PROVINCE, province);
        cv.put(DatabaseHelper.Household.COLUMN_HEAD, head);
        cv.put(DatabaseHelper.Household.COLUMN_ACCURACY, accuracy);
        cv.put(DatabaseHelper.Household.COLUMN_ALTITUDE, altitude);
        cv.put(DatabaseHelper.Household.COLUMN_LATITUDE, latitude);
        cv.put(DatabaseHelper.Household.COLUMN_LONGITUDE, longitude);
        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.Household.ALL_COLUMNS;
    }
}
