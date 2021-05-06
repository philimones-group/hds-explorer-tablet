package org.philimone.hds.explorer.model.followup;

import java.io.Serializable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;

@Entity
public class TrackingSubjectList implements Serializable {

    public static String TYPE_REGION = "Region";
    public static String TYPE_HOUSEHOLD = "Household";
    public static String TYPE_MEMBER = "Member";
    public static String TYPE_USER = "User";

    @Id
    public long id;
    public int listId;     /* Id of the MemberList */
    @Index
    public long trackingId; /* Id of the TrackingList/Follow-up List */
    public String title;   /* Title of the List */
    @Index
    public String forms;   /* List of the Forms that all Members will have to collect */

    @Index
    public String subjectCode;
    public String subjectType;
    @Index
    public String subjectForms;
    public int subjectVisit;
    public Double completionRate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public long getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(long trackingId) {
        this.trackingId = trackingId;
    }

    public String getForms() {
        return forms;
    }

    public void setForms(String forms) {
        this.forms = forms;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
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

    public int getSubjectVisit() {
        return subjectVisit;
    }

    public void setSubjectVisit(int subjectVisit) {
        this.subjectVisit = subjectVisit;
    }

    public String getSubjectForms() {
        return subjectForms;
    }

    public void setSubjectForms(String subjectForms) {
        this.subjectForms = subjectForms;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public String getTableName() {
        return "tracking_subject_list";
    }

}