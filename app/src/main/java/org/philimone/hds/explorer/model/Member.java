package org.philimone.hds.explorer.model;

import android.content.ContentValues;

import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;

import java.io.Serializable;
import java.util.Date;

import mz.betainteractive.utilities.ReflectionUtils;
import mz.betainteractive.utilities.StringUtil;

/**
 * Created by paul on 5/20/16.
 */
public class Member implements Serializable, Table {

    private int id;
    private String code;
    private String name;
    private String gender;
    private String dob;
    private int age;
    private int ageAtDeath;

    private String motherCode;
    private String motherName;
    private String fatherCode;
    private String fatherName;

    private String spouseCode;
    private String spouseName;
    private String spouseType;

    /**
     * Current HouseHold Status
     */
    private String householdCode;
    private String householdName;
    private String startType;
    private String startDate;
    private String endType;
    private String endDate;

    private String entryHousehold;
    private String entryType;
    private String entryDate;

    private boolean isHouseholdHead; /*not on database*/
    private boolean isSecHouseholdHead; /*not on database*/

    /** GPS Status */
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public Date getDobDate(){
        return StringUtil.toDate(dob, "yyyy-MM-dd");
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

    public int getAgeAtDeath() {
        return ageAtDeath;
    }

    public void setAgeAtDeath(int ageAtDeath) {
        this.ageAtDeath = ageAtDeath;
    }

    public String getMotherCode() {
        return motherCode;
    }

    public void setMotherCode(String motherCode) {
        this.motherCode = motherCode;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getFatherCode() {
        return fatherCode;
    }

    public void setFatherCode(String fatherCode) {
        this.fatherCode = fatherCode;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getHouseholdCode() {
        return householdCode;
    }

    public void setHouseholdCode(String householdCode) {
        this.householdCode = householdCode;
    }

    public String getHouseholdName() {
        return householdName;
    }

    public void setHouseholdName(String householdName) {
        this.householdName = householdName;
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

    public Double getCosLongitude() {
        return cosLongitude;
    }

    public void setCosLongitude(Double cosLongitude) {
        this.cosLongitude = cosLongitude;
    }

    public Double getSinLongitude() {
        return sinLongitude;
    }

    public void setSinLongitude(Double sinLongitude) {
        this.sinLongitude = sinLongitude;
    }

    public String getSpouseCode() {
        return spouseCode;
    }

    public void setSpouseCode(String spouseCode) {
        this.spouseCode = spouseCode;
    }

    public String getSpouseName() {
        return spouseName;
    }

    public void setSpouseName(String spouseName) {
        this.spouseName = spouseName;
    }

    public String getSpouseType() {
        return spouseType;
    }

    public void setSpouseType(String spouseType) {
        this.spouseType = spouseType;
    }

    public String getEntryHousehold() {
        return entryHousehold;
    }

    public void setEntryHousehold(String entryHousehold) {
        this.entryHousehold = entryHousehold;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }

    public boolean isHouseholdHead() {
        return isHouseholdHead;
    }

    public void setHouseholdHead(boolean householdHead) {
        isHouseholdHead = householdHead;
    }

    public boolean isSecHouseholdHead() {
        return isSecHouseholdHead;
    }

    public void setSecHouseholdHead(boolean secHouseholdHead) {
        isSecHouseholdHead = secHouseholdHead;
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

    public static Member getEmptyMember(){
        Member member = new Member();
        member.code = "";
        member.name = "";
        member.gender = "";
        member.dob = "";
        member.age = 0;
        member.ageAtDeath = 0;

        member.spouseCode = "";
        member.spouseName = "";
        member.spouseType = "";

        member.motherCode = "";
        member.motherName = "";
        member.fatherCode = "";
        member.fatherName = "";

        member.householdCode = "";
        member.householdName = "";
        member.startType = "";
        member.startDate = "";
        member.endType = "";
        member.endDate = "";

        member.entryHousehold = "";
        member.entryType = "";
        member.entryDate = null;

        member.gpsNull = true;
        member.gpsAccuracy = 0.0;
        member.gpsAltitude = 0.0;
        member.gpsLatitude = 0.0;
        member.gpsLongitude = 0.0;

        member.cosLatitude = 0.0;
        member.sinLatitude = 0.0;
        member.cosLongitude = 0.0;
        member.sinLongitude = 0.0;


        return member;
    }

    @Override
    public String getTableName() {
        return DatabaseHelper.Member.TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.Member.COLUMN_CODE, code);
        cv.put(DatabaseHelper.Member.COLUMN_NAME, name);
        cv.put(DatabaseHelper.Member.COLUMN_GENDER, gender);
        cv.put(DatabaseHelper.Member.COLUMN_DOB, dob);
        cv.put(DatabaseHelper.Member.COLUMN_AGE, age);
        cv.put(DatabaseHelper.Member.COLUMN_AGE_AT_DEATH, ageAtDeath);
        cv.put(DatabaseHelper.Member.COLUMN_SPOUSE_CODE, spouseCode);
        cv.put(DatabaseHelper.Member.COLUMN_SPOUSE_NAME, spouseName);
        cv.put(DatabaseHelper.Member.COLUMN_SPOUSE_TYPE, spouseType);
        cv.put(DatabaseHelper.Member.COLUMN_MOTHER_CODE, motherCode);
        cv.put(DatabaseHelper.Member.COLUMN_MOTHER_NAME, motherName);
        cv.put(DatabaseHelper.Member.COLUMN_FATHER_CODE, fatherCode);
        cv.put(DatabaseHelper.Member.COLUMN_FATHER_NAME, fatherName);
        cv.put(DatabaseHelper.Member.COLUMN_HOUSE_CODE, householdCode);
        cv.put(DatabaseHelper.Member.COLUMN_HOUSE_NAME, householdName);
        cv.put(DatabaseHelper.Member.COLUMN_START_TYPE, startType);
        cv.put(DatabaseHelper.Member.COLUMN_START_DATE, startDate);
        cv.put(DatabaseHelper.Member.COLUMN_END_TYPE, endType);
        cv.put(DatabaseHelper.Member.COLUMN_END_DATE, endDate);
        cv.put(DatabaseHelper.Member.COLUMN_ENTRY_HOUSEHOLD, entryHousehold);
        cv.put(DatabaseHelper.Member.COLUMN_ENTRY_TYPE, entryType);
        cv.put(DatabaseHelper.Member.COLUMN_ENTRY_DATE, entryDate);
        cv.put(DatabaseHelper.Member.COLUMN_IS_HOUSEHOLD_HEAD, isHouseholdHead ? 1 : 0);
        cv.put(DatabaseHelper.Member.COLUMN_IS_SEC_HOUSEHOLD_HEAD, isSecHouseholdHead ? 1 : 0);
        cv.put(DatabaseHelper.Member.COLUMN_GPS_NULL, gpsNull ? 1 : 0);
        cv.put(DatabaseHelper.Member.COLUMN_GPS_ACCURACY, gpsAccuracy);
        cv.put(DatabaseHelper.Member.COLUMN_GPS_ALTITUDE, gpsAltitude);
        cv.put(DatabaseHelper.Member.COLUMN_GPS_LATITUDE, gpsLatitude);
        cv.put(DatabaseHelper.Member.COLUMN_GPS_LONGITUDE, gpsLongitude);
        cv.put(DatabaseHelper.Member.COLUMN_COS_LATITUDE, cosLatitude);
        cv.put(DatabaseHelper.Member.COLUMN_SIN_LATITUDE, sinLatitude);
        cv.put(DatabaseHelper.Member.COLUMN_COS_LONGITUDE, cosLongitude);
        cv.put(DatabaseHelper.Member.COLUMN_SIN_LONGITUDE, sinLongitude);
        cv.put(DatabaseHelper.Member.COLUMN_RECENTLY_CREATED, recentlyCreated ? 1 : 0);
        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.Member.ALL_COLUMNS;
    }
}
