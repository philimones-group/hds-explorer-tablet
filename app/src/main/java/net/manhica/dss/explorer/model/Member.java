package net.manhica.dss.explorer.model;

import android.content.ContentValues;

import net.manhica.dss.explorer.database.DatabaseHelper;
import net.manhica.dss.explorer.database.Table;

import java.io.Serializable;

import mz.betainteractive.utilities.ReflectionUtils;

/**
 * Created by paul on 5/20/16.
 */
public class Member implements Serializable, Table {

    private int id;
    private String extId;
    private String permId;
    private String name;
    private String gender;
    private String dob;
    private int age;

    private String spouseExtId;
    private String spouseName;
    private String spousePermId;

    private String motherExtId;
    private String motherName;
    private String motherPermId;
    private String fatherExtId;
    private String fatherName;
    private String fatherPermId;
    /**
     * Current HouseHold Status
     */
    private String houseExtId;
    private String houseNumber;
    private String startType;
    private String startDate;
    private String endType;
    private String endDate;
    /** GPS Status */
    private Double gpsAccuracy;
    private Double gpsAltitude;
    private Double gpsLatitude;
    private Double gpsLongitude;
    /* Extras Variables*/ //separated by semicolon :
    String extrasColumns;
    String extrasValues;


    private boolean isHouseholdHead; /*not on database*/
    private boolean isSubsHouseholdHead; /*not on database*/

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

    public String getPermId() {
        return permId;
    }

    public void setPermId(String permId) {
        this.permId = permId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getMotherExtId() {
        return motherExtId;
    }

    public void setMotherExtId(String motherExtId) {
        this.motherExtId = motherExtId;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getMotherPermId() {
        return motherPermId;
    }

    public void setMotherPermId(String motherPermId) {
        this.motherPermId = motherPermId;
    }

    public String getFatherExtId() {
        return fatherExtId;
    }

    public void setFatherExtId(String fatherExtId) {
        this.fatherExtId = fatherExtId;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getFatherPermId() {
        return fatherPermId;
    }

    public void setFatherPermId(String fatherPermId) {
        this.fatherPermId = fatherPermId;
    }

    public String getHouseExtId() {
        return houseExtId;
    }

    public void setHouseExtId(String houseExtId) {
        this.houseExtId = houseExtId;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getStartType() {
        return startType;
    }

    public void setStartType(String startType) {
        this.startType = startType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndType() {
        return endType;
    }

    public void setEndType(String endType) {
        this.endType = endType;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate ==null ? "" : endDate;
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

    public String getSpouseExtId() {
        return spouseExtId;
    }

    public void setSpouseExtId(String spouseExtId) {
        this.spouseExtId = spouseExtId;
    }

    public String getSpouseName() {
        return spouseName;
    }

    public void setSpouseName(String spouseName) {
        this.spouseName = spouseName;
    }

    public String getSpousePermId() {
        return spousePermId;
    }

    public void setSpousePermId(String spousePermId) {
        this.spousePermId = spousePermId;
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

    public boolean isHouseholdHead() {
        return isHouseholdHead;
    }

    public void setHouseholdHead(boolean householdHead) {
        isHouseholdHead = householdHead;
    }

    public boolean isSubsHouseholdHead() {
        return isSubsHouseholdHead;
    }

    public void setSubsHouseholdHead(boolean subsHouseholdHead) {
        isSubsHouseholdHead = subsHouseholdHead;
    }

    public boolean hasNullCoordinates(){
        return gpsLatitude==null || gpsLongitude==null;
    }

    public String getValueByName(String variableName){
        return ReflectionUtils.getValueByName(this, variableName);
    }

    @Override
    public String getTableName() {
        return DatabaseHelper.Member.TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.Member.COLUMN_EXT_ID, extId);
        cv.put(DatabaseHelper.Member.COLUMN_PERM_ID, permId);
        cv.put(DatabaseHelper.Member.COLUMN_NAME, name);
        cv.put(DatabaseHelper.Member.COLUMN_GENDER, gender);
        cv.put(DatabaseHelper.Member.COLUMN_DOB, dob);
        cv.put(DatabaseHelper.Member.COLUMN_AGE, age);
        cv.put(DatabaseHelper.Member.COLUMN_SPOUSE_EXT_ID, spouseExtId);
        cv.put(DatabaseHelper.Member.COLUMN_SPOUSE_NAME, spouseName);
        cv.put(DatabaseHelper.Member.COLUMN_SPOUSE_PERM_ID, spousePermId);
        cv.put(DatabaseHelper.Member.COLUMN_MOTHER_EXT_ID, motherExtId);
        cv.put(DatabaseHelper.Member.COLUMN_MOTHER_NAME, motherName);
        cv.put(DatabaseHelper.Member.COLUMN_MOTHER_PERM_ID, motherPermId);
        cv.put(DatabaseHelper.Member.COLUMN_FATHER_EXT_ID, fatherExtId);
        cv.put(DatabaseHelper.Member.COLUMN_FATHER_NAME, fatherName);
        cv.put(DatabaseHelper.Member.COLUMN_FATHER_PERM_ID, fatherPermId);
        cv.put(DatabaseHelper.Member.COLUMN_HOUSE_EXT_ID, houseExtId);
        cv.put(DatabaseHelper.Member.COLUMN_HOUSE_NUMBER, houseNumber);
        cv.put(DatabaseHelper.Member.COLUMN_START_TYPE, startType);
        cv.put(DatabaseHelper.Member.COLUMN_START_DATE, startDate);
        cv.put(DatabaseHelper.Member.COLUMN_END_TYPE, endType);
        cv.put(DatabaseHelper.Member.COLUMN_END_DATE, endDate);
        cv.put(DatabaseHelper.Member.COLUMN_GPS_ACCURACY, gpsAccuracy);
        cv.put(DatabaseHelper.Member.COLUMN_GPS_ALTITUDE, gpsAltitude);
        cv.put(DatabaseHelper.Member.COLUMN_GPS_LATITUDE, gpsLatitude);
        cv.put(DatabaseHelper.Member.COLUMN_GPS_LONGITUDE, gpsLongitude);
        cv.put(DatabaseHelper.Member.COLUMN_EXTRAS_COLUMNS, extrasColumns);
        cv.put(DatabaseHelper.Member.COLUMN_EXTRAS_VALUES, extrasValues);
        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.Member.ALL_COLUMNS;
    }
}
