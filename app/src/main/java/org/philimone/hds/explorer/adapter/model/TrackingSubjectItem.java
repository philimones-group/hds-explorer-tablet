package org.philimone.hds.explorer.adapter.model;

import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.philimone.hds.explorer.model.followup.TrackingSubjectList.TYPE_HOUSEHOLD;
import static org.philimone.hds.explorer.model.followup.TrackingSubjectList.TYPE_MEMBER;
import static org.philimone.hds.explorer.model.followup.TrackingSubjectList.TYPE_REGION;

public class TrackingSubjectItem implements Serializable {
    private Region region;
    private Household  household;
    private Member member;
    private TrackingSubListItem listItem;
    private String subjectType;
    private int visitNumber;
    private List<String> forms;
    private List<CollectedData> collectedForms;

    public TrackingSubjectItem() {
        forms = new ArrayList<>();
        collectedForms = new ArrayList<>();
    }

    public void addForm(String form){
        this.forms.add(form);
    }

    public void addCollectedData(CollectedData collectedData){
        this.collectedForms.add(collectedData);
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Household getHousehold() {
        return household;
    }

    public void setHousehold(Household household) {
        this.household = household;
    }

    public boolean isRegionSubject(){
        return subjectType.equalsIgnoreCase(TYPE_REGION);
    }

    public boolean isHouseholdSubject(){
        return subjectType.equalsIgnoreCase(TYPE_HOUSEHOLD);
    }

    public boolean isMemberSubject(){
        return subjectType.equalsIgnoreCase(TYPE_MEMBER);
    }

    public TrackingSubListItem getListItem() {
        return listItem;
    }

    public void setListItem(TrackingSubListItem listItem) {
        this.listItem = listItem;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public int getVisitNumber() {
        return visitNumber;
    }

    public void setVisitNumber(int visitNumber) {
        this.visitNumber = visitNumber;
    }

    public List<String> getForms() {
        return forms;
    }

    public void addForms(List<String> forms) {
        this.forms.addAll(forms);
    }

    public void addForms(String[] forms) {
        this.forms.addAll(Arrays.asList(forms));
    }

    public List<CollectedData> getCollectedForms() {
        return collectedForms;
    }

    public void addCollectedData(List<CollectedData> collectedForms) {
        this.collectedForms.addAll(collectedForms);
    }

}