package org.philimone.hds.explorer.model.followup;

import android.content.ContentValues;

import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;

import java.io.Serializable;

/*
 * Represents a FollowUp List Description, is the parent object for a List of Members that will be followed-up
 */
public class TrackingList implements Serializable, Table {

    private int id;
    private String code;    /** The Follow-up List identification code **/
    private String name;    /** The name of the Follow-up List (eg. HIV Case or Index Case) - will be displyed as the top left header label**/
    private String title;   /** The title of the Follow-up List  **/
    private String details; /** The details of the Follow-up List **/
    private String module;  /** The module(s) that the Follow-up List belongs to **/
    private Double completionRate; /** Rate of completion in % **/

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    @Override
    public String getTableName() {
        return DatabaseHelper.TrackingList.TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();

        cv.put(DatabaseHelper.TrackingList.COLUMN_CODE, code);
        cv.put(DatabaseHelper.TrackingList.COLUMN_NAME, name);
        cv.put(DatabaseHelper.TrackingList.COLUMN_DETAILS, details);
        cv.put(DatabaseHelper.TrackingList.COLUMN_TITLE, title);
        cv.put(DatabaseHelper.TrackingList.COLUMN_MODULE, module);
        cv.put(DatabaseHelper.TrackingList.COLUMN_COMPLETION_RATE, completionRate);

        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.TrackingList.ALL_COLUMNS;
    }
}