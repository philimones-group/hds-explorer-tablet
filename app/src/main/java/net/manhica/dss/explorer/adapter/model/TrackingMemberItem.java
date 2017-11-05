package net.manhica.dss.explorer.adapter.model;

import net.manhica.dss.explorer.model.CollectedData;
import net.manhica.dss.explorer.model.Member;
import net.manhica.dss.explorer.model.followup.TrackingMemberList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrackingMemberItem implements Serializable {
    private Member member;
    private TrackingSubListItem listItem;
    private String studyCode;
    private List<String> forms;
    private List<CollectedData> collectedForms;

    public TrackingMemberItem() {
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

    public TrackingSubListItem getListItem() {
        return listItem;
    }

    public void setListItem(TrackingSubListItem listItem) {
        this.listItem = listItem;
    }

    public String getStudyCode() {
        return studyCode;
    }

    public void setStudyCode(String studyCode) {
        this.studyCode = studyCode;
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