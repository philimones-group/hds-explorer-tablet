package org.philimone.hds.explorer.model;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;


/**
 * Represents an application parameter or setting to be saved on the database
 */
@Entity
public class ApplicationParam {

    public static final String APP_URL = "app-url";
    public static final String ODK_URL = "odk-url";
    public static final String REDCAP_URL = "redcap-url";

    @Id
    private long id;
    @Unique
    public String name;
    public String type;
    public String value;

    public ApplicationParam(){

    }

    public ApplicationParam(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
