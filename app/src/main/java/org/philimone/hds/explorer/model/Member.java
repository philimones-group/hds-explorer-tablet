package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.GenderConverter;
import org.philimone.hds.explorer.model.enums.Gender;

import java.io.Serializable;
import java.util.Date;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Transient;
import io.objectbox.annotation.Unique;
import mz.betainteractive.utilities.ReflectionUtils;
import mz.betainteractive.utilities.StringUtil;

/**
 * Created by paul on 5/20/16.
 */
@Entity
public class Member implements Serializable {

    @Id
    public long id;
    @Unique
    public String code;
    public String name;

    @Convert(converter = GenderConverter.class, dbType = String.class)
    public Gender gender;

    public String dob;
    public int age;
    public int ageAtDeath;

    @Index
    public String motherCode;
    public String motherName;
    @Index
    public String fatherCode;
    public String fatherName;

    public String maritalStatus;

    @Index
    public String spouseCode;
    public String spouseName;

    /**
     * Current HouseHold Status
     */
    @Index
    public String householdCode;
    @Index
    public String householdName;
    public String startType;
    public String startDate;
    public String endType;
    public String endDate;

    @Index
    public String entryHousehold;
    public String entryType;
    public String entryDate;

    @Index
    public String headRelationshipType;

    /** GPS Status */
    public boolean gpsNull;
    public Double gpsAccuracy;
    public Double gpsAltitude;
    public Double gpsLatitude;
    public Double gpsLongitude;

    public Double cosLatitude;
    public Double sinLatitude;
    public Double cosLongitude;
    public Double sinLongitude;

    public Boolean recentlyCreated = false;

    @Transient
    private boolean isHouseholdHead; /*not on database*/
    @Transient
    private boolean isSecHouseholdHead; /*not on database*/

    public Member() {

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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
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

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
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

    public String getHeadRelationshipType() {
        return headRelationshipType;
    }

    public void setHeadRelationshipType(String headRelationshipType) {
        this.headRelationshipType = headRelationshipType;
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
        member.gender = Gender.MALE;
        member.dob = "";
        member.age = 0;
        member.ageAtDeath = 0;

        member.spouseCode = "";
        member.spouseName = "";
        member.maritalStatus = "";

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

        member.headRelationshipType = "";

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

    public static Member getUnknownIndividual(){
        Member m = Member.getEmptyMember();

        m.code = "UNK";
        m.name = "Unknown Individual";

        return m;
    }

    public static String getTableName() {
        return "member";
    }

}
