package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.FormSubjectTypeConverter;
import org.philimone.hds.explorer.model.converters.FormTypeConverter;
import org.philimone.hds.explorer.model.converters.MapStringConverter;
import org.philimone.hds.explorer.model.converters.StringCollectionConverter;
import org.philimone.hds.explorer.model.enums.FormSubjectType;
import org.philimone.hds.explorer.model.enums.FormType;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import io.objectbox.annotation.Backlink;
import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToMany;

/**
 * Created by paul on 5/20/16.
 */
@Entity
public class Form implements Serializable {

    @Id
    public long id;
    @Convert(converter = FormTypeConverter.class, dbType = String.class)
    public FormType formType;

    @Unique
    public String formId;
    public String formName;
    public String formDescription;

    @Convert(converter = FormSubjectTypeConverter.class, dbType = String.class)
    public FormSubjectType formSubjectType;
    public String formDependencies;
    public String regionLevel;
    public String gender; /*M, F, ALL*/
    public int minAge; //0
    public int maxAge; //Default - 120

    @Index
    @Convert(converter = StringCollectionConverter.class, dbType = String.class)
    public Set<String> modules; //if empty - is accessed by all

    public boolean isRegionForm;
    public boolean isHouseholdForm;
    public boolean isHouseholdHeadForm;
    public boolean isMemberForm;
    public boolean isFollowUpForm;
    public boolean isFormGroupExclusive;
    public boolean multiCollPerSession;

    @Convert(converter = MapStringConverter.class, dbType = String.class)
    public Map<String, String> formMap;

    public String redcapApi;
    public String redcapMapText;

    @Backlink(to = "groupForm")
    public ToMany<FormGroupMapping> groupMappings;

    public Form(){
        this.formMap = new LinkedHashMap<>();
        this.modules = new HashSet<>();
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

    public Set<String> getModules() {
        return modules;
    }

    public void setModules(Collection<? extends String> modules) {
        this.modules.addAll(modules);
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

    public boolean isFollowUpForm() {
        return isFollowUpForm;
    }

    public void setFollowUpForm(boolean followUpForm) {
        isFollowUpForm = followUpForm;
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
