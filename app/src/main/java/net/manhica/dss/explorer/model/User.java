package net.manhica.dss.explorer.model;

import android.content.ContentValues;

import net.manhica.dss.explorer.database.DatabaseHelper;
import net.manhica.dss.explorer.database.Table;

import java.io.Serializable;

import mz.betainteractive.utilities.ReflectionUtils;

/**
 * Created by paul on 5/20/16.
 */
public class User implements Serializable, Table {

    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String modules;
    private String extras;

    public User(){

    }

    public User(String firstName, String lastName, String username, String password, String modules, String extras) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.modules = modules;
        this.extras = extras;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getModules() {
        return modules;
    }

    public void setModules(String modules) {
        this.modules = modules;
    }

    public String getExtras() {
        return extras;
    }

    public void setExtras(String extras) {
        this.extras = extras;
    }

    public String getFullname(){
        return firstName.trim() + " " + lastName.trim();
    }

    public String toString(){
        return ""+this.firstName+" "+this.lastName+", user: "+this.username;
    }

    public String getValueByName(String variableName){
        return ReflectionUtils.getValueByName(this, variableName);
    }

    @Override
    public String getTableName() {
        return DatabaseHelper.User.TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.User.COLUMN_FIRSTNAME, firstName);
        cv.put(DatabaseHelper.User.COLUMN_LASTNAME, lastName);
        cv.put(DatabaseHelper.User.COLUMN_USERNAME, username);
        cv.put(DatabaseHelper.User.COLUMN_PASSWORD, password);
        cv.put(DatabaseHelper.User.COLUMN_MODULES, modules);
        cv.put(DatabaseHelper.User.COLUMN_EXTRAS, extras);
        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.User.ALL_COLUMNS;
    }
}
