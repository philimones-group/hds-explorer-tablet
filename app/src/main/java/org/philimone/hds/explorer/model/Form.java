package org.philimone.hds.explorer.model;

import android.content.ContentValues;

import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;
import org.philimone.hds.explorer.model.converters.FormMappingConverter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

/**
 * Created by paul on 5/20/16.
 */
@Entity
public class Form implements Serializable {

    @Id
    public long id;
    @Unique
    public String formId;
    public String formName;
    public String formDescription;
    public String formDependencies;
    public String regionLevel;
    public String gender; /*M, F, ALL*/
    public int minAge; //0
    public int maxAge; //Default - 120
    public String modules; //if null - is accessed by all
    public boolean isRegionForm;
    public boolean isHouseholdForm;
    public boolean isHouseholdHeadForm;
    public boolean isMemberForm;
    public boolean isFollowUpOnly;
    public boolean multiCollPerSession;

    @Convert(converter = FormMappingConverter.class, dbType = String.class)
    public Map<String, String> formMap;

    public String redcapApi;
    public String redcapMapText;

    public Form(){
        formMap = new LinkedHashMap<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getFormDescription() {
        return formDescription;
    }

    public void setFormDescription(String formDescription) {
        this.formDescription = formDescription;
    }

    public String getFormDependencies() {
        return formDependencies;
    }

    public void setFormDependencies(String formDependencies) {
        this.formDependencies = formDependencies;
    }

    public String getRegionLevel() {
        return regionLevel;
    }

    public void setRegionLevel(String regionLevel) {
        this.regionLevel = regionLevel;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public String getModules() {
        return modules;
    }

    public void setModules(String modules) {
        this.modules = modules;
    }

    public boolean isRegionForm() {
        return isRegionForm;
    }

    public void setRegionForm(boolean regionForm) {
        isRegionForm = regionForm;
    }

    public boolean isHouseholdForm() {
        return isHouseholdForm;
    }

    public void setHouseholdForm(boolean householdForm) {
        isHouseholdForm = householdForm;
    }

    public boolean isHouseholdHeadForm() {
        return isHouseholdHeadForm;
    }

    public void setHouseholdHeadForm(boolean householdHeadForm) {
        isHouseholdHeadForm = householdHeadForm;
    }

    public boolean isMemberForm() {
        return isMemberForm;
    }

    public void setMemberForm(boolean memberForm) {
        isMemberForm = memberForm;
    }

    public boolean isFollowUpOnly() {
        return isFollowUpOnly;
    }

    public void setFollowUpOnly(boolean followUpOnly) {
        isFollowUpOnly = followUpOnly;
    }

    public boolean isMultiCollPerSession() {
        return multiCollPerSession;
    }

    public void setMultiCollPerSession(boolean multiCollPerSession) {
        this.multiCollPerSession = multiCollPerSession;
    }

    public void setFormMap(Map<String, String> bindMap){
        this.formMap = bindMap;
    }

    public Map<String, String> getFormMap(){
        return this.formMap;
    }

    public void setRedcapApi(String redcapApi){
        this.redcapApi = redcapApi;
    }

    public void setRedcapMap(String redcapMapText){
        this.redcapMapText = redcapMapText;
    }

}
