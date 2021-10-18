package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.GenderConverter;
import org.philimone.hds.explorer.model.converters.HeadRelationshipTypeConverter;
import org.philimone.hds.explorer.model.converters.MaritalStatusConverter;
import org.philimone.hds.explorer.model.converters.ResidencyEndTypeConverter;
import org.philimone.hds.explorer.model.converters.ResidencyStartTypeConverter;
import org.philimone.hds.explorer.model.converters.StringCollectionConverter;
import org.philimone.hds.explorer.model.enums.Gender;
import org.philimone.hds.explorer.model.enums.HeadRelationshipType;
import org.philimone.hds.explorer.model.enums.MaritalStatus;
import org.philimone.hds.explorer.model.enums.SubjectEntity;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyStartType;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
public class Member implements CoreEntity, FormSubject, Serializable {

    @Id
    public long id;
    @Unique
    public String code;
    public String name;

    @Index
    @Convert(converter = GenderConverter.class, dbType = String.class)
    public Gender gender;

    public Date dob;
    public int age;
    public int ageAtDeath;

    @Index
    public String motherCode;
    public String motherName;
    @Index
    public String fatherCode;
    public String fatherName;

    @Convert(converter = MaritalStatusConverter.class, dbType = String.class)
    public MaritalStatus maritalStatus;

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

    @Index
    @Convert(converter = ResidencyStartTypeConverter.class, dbType = String.class)
    public ResidencyStartType startType;
    public Date startDate;

    @Index
    @Convert(converter = ResidencyEndTypeConverter.class, dbType = String.class)
    public ResidencyEndType endType;
    public Date endDate;

    @Index
    public String entryHousehold;
    @Convert(converter = ResidencyStartTypeConverter.class, dbType = String.class)
    public ResidencyStartType entryType;
    public Date entryDate;

    @Index
    @Convert(converter = HeadRelationshipTypeConverter.class, dbType = String.class)
    public HeadRelationshipType headRelationshipType;

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

    public boolean recentlyCreated = false;

    public String recentlyCreatedUri;

    @Index
    @Convert(converter = StringCollectionConverter.class, dbType = String.class)
    public Set<String> modules;

    @Transient
    private boolean isHouseholdHead; /*not on database*/
    @Transient
    private boolean isSecHouseholdHead; /*not on database*/

    public Member() {
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
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

    public ResidencyStartType getStartType() {
        return startType;
    }

    public void setStartType(ResidencyStartType startType) {
        this.startType = startType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public ResidencyEndType getEndType() {
        return endType;
    }

    public void setEndType(ResidencyEndType endType) {
        this.endType = endType;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getEntryHousehold() {
        return entryHousehold;
    }

    public void setEntryHousehold(String entryHousehold) {
        this.entryHousehold = entryHousehold;
    }

    public ResidencyStartType getEntryType() {
        return entryType;
    }

    public void setEntryType(ResidencyStartType entryType) {
        this.entryType = entryType;
    }

    public Date getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(Date entryDate) {
        this.entryDate = entryDate;
    }

    public HeadRelationshipType getHeadRelationshipType() {
        return headRelationshipType;
    }

    public void setHeadRelationshipType(HeadRelationshipType headRelationshipType) {
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

    public void setRecentlyCreated(Boolean recentlyCreated) {
        this.recentlyCreated = recentlyCreated;
    }

    public void setModules(Collection<? extends String> modules) {
        this.modules.addAll(modules);
    }

    public String getValueByName(String variableName){
        return ReflectionUtils.getValueByName(this, variableName);
    }

    public static Member getEmptyMember(){
        Member member = new Member();
        member.code = "";
        member.name = "";
        member.gender = Gender.MALE;
        member.dob = null;
        member.age = 0;
        member.ageAtDeath = 0;

        member.spouseCode = "";
        member.spouseName = "";
        member.maritalStatus = MaritalStatus.SINGLE;

        member.motherCode = "";
        member.motherName = "";
        member.fatherCode = "";
        member.fatherName = "";

        member.householdCode = "";
        member.householdName = "";
        member.startType = null;
        member.startDate = null;
        member.endType = null;
        member.endDate = null;

        member.entryHousehold = "";
        member.entryType = null;
        member.entryDate = null;

        member.headRelationshipType = null;

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

    public boolean isUnknownIndividual(){
        return code.equals("UNK");
    }

    public static Member getUnknownIndividual(){
        Member m = Member.getEmptyMember();

        m.code = "UNK";
        m.name = "Unknown Individual";

        return m;
    }

    public SubjectEntity getTableName() {
        return SubjectEntity.MEMBER;
    }

    @Override
    public long getId() {
        return this.id;
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
