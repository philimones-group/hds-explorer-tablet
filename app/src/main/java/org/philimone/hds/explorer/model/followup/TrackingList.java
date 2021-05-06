package org.philimone.hds.explorer.model.followup;

import java.io.Serializable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Unique;

/*
 * Represents a FollowUp List Description, is the parent object for a List of Members that will be followed-up
 */
@Entity
public class TrackingList implements Serializable {

    @Id
    public long id;
    @Unique
    public String code;    /** The Follow-up List identification code **/
    public String name;    /** The name of the Follow-up List (eg. HIV Case or Index Case) - will be displyed as the top left header label**/
    public String title;   /** The title of the Follow-up List  **/
    public String details; /** The details of the Follow-up List **/
    @Index
    public String module;  /** The module(s) that the Follow-up List belongs to **/
    public Double completionRate; /** Rate of completion in % **/

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public String getTableName() {
        return "tracking_list";
    }

}