package org.philimone.hds.explorer.model;

import android.content.ContentValues;

import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;

import java.io.Serializable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

/**
 * Created by paul on 5/20/16.
 */
@Entity
public class Module implements Serializable {

    public final static String DSS_SURVEY_MODULE = "DSS-SURVEY";
    public final static String DSS_SUPERVISOR = "DSS-SUPERVISOR";
    public final static String DSS_OTHERS = "DSS-ANY";

    @Id
    public long id;
    @Unique
    public String code;
    public String name;
    public String description;

    public long getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
