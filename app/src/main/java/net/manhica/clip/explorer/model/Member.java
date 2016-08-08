package net.manhica.clip.explorer.model;

import android.content.ContentValues;

import net.manhica.clip.explorer.database.DatabaseHelper;
import net.manhica.clip.explorer.database.Table;

import java.io.Serializable;

/**
 * Created by paul on 5/20/16.
 */
public class Member implements Serializable, Table {

    private String extId;
    private String permId;
    private String name;
    private String gender;
    private String dob;
    private int age;
    private String motherExtId;
    private String motherName;
    private String motherPermId;
    private String fatherExtId;
    private String fatherName;
    private String fatherPermId;
    /**
     * Current HouseHold Status
     */
    private String hhExtId;
    private String hhNumber;
    private String hhStartType;
    private String hhStartDate;
    private String hhEndType;
    private String hhEndDate;
    /** GPS Status */
    private String gpsAccuracy;
    private String gpsAltitude;
    private String gpsLatitude;
    private String gpsLongitude;

    private int nrPregnancies;

    /* Current Pregnancy*/
    private boolean hasDelivered;  //0 - false, 1 - false
    private boolean isPregnant;

    private String clip_id_1;
    private String clip_id_2;
    private String clip_id_3;
    private String clip_id_4;
    private String clip_id_5;
    private String clip_id_6;
    private String clip_id_7;
    private String clip_id_8;
    private String clip_id_9;
    /**
     *  Used on Study Section Status - Pom, Facility and Form D e E
     */
    private boolean onPom;
    private boolean onFacility;
    private boolean onSurveillance;

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

    public String getHhExtId() {
        return hhExtId;
    }

    public void setHhExtId(String hhExtId) {
        this.hhExtId = hhExtId;
    }

    public String getHhNumber() {
        return hhNumber;
    }

    public void setHhNumber(String hhNumber) {
        this.hhNumber = hhNumber;
    }

    public String getHhStartType() {
        return hhStartType;
    }

    public void setHhStartType(String hhStartType) {
        this.hhStartType = hhStartType;
    }

    public String getHhStartDate() {
        return hhStartDate;
    }

    public void setHhStartDate(String hhStartDate) {
        this.hhStartDate = hhStartDate;
    }

    public String getHhEndType() {
        return hhEndType;
    }

    public void setHhEndType(String hhEndType) {
        this.hhEndType = hhEndType;
    }

    public String getHhEndDate() {
        return hhEndDate;
    }

    public void setHhEndDate(String hhEndDate) {
        this.hhEndDate = hhEndDate==null ? "" : hhEndDate;
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

    public int getNrPregnancies() {
        return nrPregnancies;
    }

    public void setNrPregnancies(int nrPregnancies) {
        this.nrPregnancies = nrPregnancies;
    }

    public boolean isHasDelivered() {
        return hasDelivered;
    }

    public void setHasDelivered(boolean hasDelivered) {
        this.hasDelivered = hasDelivered;
    }

    public boolean isPregnant() {
        return isPregnant;
    }

    public void setPregnant(boolean pregnant) {
        isPregnant = pregnant;
    }

    public String getClip_id_1() {
        return clip_id_1;
    }

    public void setClip_id_1(String clip_id_1) {
        this.clip_id_1 = clip_id_1==null ? "" : clip_id_1 ;
    }

    public String getClip_id_2() {
        return clip_id_2;
    }

    public void setClip_id_2(String clip_id_2) {
        this.clip_id_2 = clip_id_2==null ? "" : clip_id_2 ;
    }

    public String getClip_id_3() {
        return clip_id_3;
    }

    public void setClip_id_3(String clip_id_3) {
        this.clip_id_3 = clip_id_3==null ? "" : clip_id_3 ;
    }

    public String getClip_id_4() {
        return clip_id_4;
    }

    public void setClip_id_4(String clip_id_4) {
        this.clip_id_4 = clip_id_4==null ? "" : clip_id_4 ;
    }

    public String getClip_id_5() {
        return clip_id_5;
    }

    public void setClip_id_5(String clip_id_5) {
        this.clip_id_5 = clip_id_5==null ? "" : clip_id_5 ;
    }

    public String getClip_id_6() {
        return clip_id_6;
    }

    public void setClip_id_6(String clip_id_6) {
        this.clip_id_6 = clip_id_6==null ? "" : clip_id_6 ;
    }

    public String getClip_id_7() {
        return clip_id_7;
    }

    public void setClip_id_7(String clip_id_7) {
        this.clip_id_7 = clip_id_7==null ? "" : clip_id_7 ;
    }

    public String getClip_id_8() {
        return clip_id_8;
    }

    public void setClip_id_8(String clip_id_8) {
        this.clip_id_8 = clip_id_8==null ? "" : clip_id_8 ;
    }

    public String getClip_id_9() {
        return clip_id_9;
    }

    public void setClip_id_9(String clip_id_9) {
        this.clip_id_9 = clip_id_9==null ? "" : clip_id_9 ;
    }

    public boolean isOnPom() {
        return onPom;
    }

    public void setOnPom(boolean onPom) {
        this.onPom = onPom;
    }

    public boolean isOnFacility() {
        return onFacility;
    }

    public void setOnFacility(boolean onFacility) {
        this.onFacility = onFacility;
    }

    public boolean isOnSurveillance() {
        return onSurveillance;
    }

    public void setOnSurveillance(boolean onSurveillance) {
        this.onSurveillance = onSurveillance;
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
        cv.put(DatabaseHelper.Member.COLUMN_MOTHER_EXT_ID, motherExtId);
        cv.put(DatabaseHelper.Member.COLUMN_MOTHER_NAME, motherName);
        cv.put(DatabaseHelper.Member.COLUMN_MOTHER_PERM_ID, motherPermId);
        cv.put(DatabaseHelper.Member.COLUMN_FATHER_EXT_ID, fatherExtId);
        cv.put(DatabaseHelper.Member.COLUMN_FATHER_NAME, fatherName);
        cv.put(DatabaseHelper.Member.COLUMN_FATHER_PERM_ID, fatherPermId);
        cv.put(DatabaseHelper.Member.COLUMN_HH_EXT_ID, hhExtId);
        cv.put(DatabaseHelper.Member.COLUMN_HH_NUMBER, hhNumber);
        cv.put(DatabaseHelper.Member.COLUMN_HH_START_TYPE, hhStartType);
        cv.put(DatabaseHelper.Member.COLUMN_HH_START_DATE, hhStartDate);
        cv.put(DatabaseHelper.Member.COLUMN_HH_END_TYPE, hhEndType);
        cv.put(DatabaseHelper.Member.COLUMN_HH_END_DATE, hhEndDate);
        cv.put(DatabaseHelper.Member.COLUMN_GPS_ACCURACY, gpsAccuracy);
        cv.put(DatabaseHelper.Member.COLUMN_GPS_ALTITUDE, gpsAltitude);
        cv.put(DatabaseHelper.Member.COLUMN_GPS_LATITUDE, gpsLatitude);
        cv.put(DatabaseHelper.Member.COLUMN_GPS_LONGITUDE, gpsLongitude);
        cv.put(DatabaseHelper.Member.COLUMN_NR_PREGNANCIES, nrPregnancies);
        cv.put(DatabaseHelper.Member.COLUMN_HAS_DELIVERED, hasDelivered);
        cv.put(DatabaseHelper.Member.COLUMN_IS_PREGNANT, isPregnant);
        cv.put(DatabaseHelper.Member.COLUMN_CLIP_ID_1, clip_id_1);
        cv.put(DatabaseHelper.Member.COLUMN_CLIP_ID_2, clip_id_2);
        cv.put(DatabaseHelper.Member.COLUMN_CLIP_ID_3, clip_id_3);
        cv.put(DatabaseHelper.Member.COLUMN_CLIP_ID_4, clip_id_4);
        cv.put(DatabaseHelper.Member.COLUMN_CLIP_ID_5, clip_id_5);
        cv.put(DatabaseHelper.Member.COLUMN_CLIP_ID_6, clip_id_6);
        cv.put(DatabaseHelper.Member.COLUMN_CLIP_ID_7, clip_id_7);
        cv.put(DatabaseHelper.Member.COLUMN_CLIP_ID_8, clip_id_8);
        cv.put(DatabaseHelper.Member.COLUMN_CLIP_ID_9, clip_id_9);
        cv.put(DatabaseHelper.Member.COLUMN_ON_POM, onPom ? 1 : 0);
        cv.put(DatabaseHelper.Member.COLUMN_ON_FACILITY, onFacility ? 1 : 0);
        cv.put(DatabaseHelper.Member.COLUMN_ON_SURVEILLANCE, onSurveillance ? 1 : 0);
        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.Member.ALL_COLUMNS;
    }
}
