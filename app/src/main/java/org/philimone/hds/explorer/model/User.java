package org.philimone.hds.explorer.model;

import android.content.ContentValues;

import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Table;

import java.io.Serializable;

import mz.betainteractive.utilities.ReflectionUtils;

/**
 * Created by paul on 5/20/16.
 */
public class User implements Serializable, Table {

    private int id;
    private String code;
    private String firstName;
    private String lastName;
    private String fullName;
    private String username;
    private String password;
    private String email;
    private String modules;


    public User(){

    }

    public User(String firstName, String lastName, String fullName, String username, String password, String modules, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.modules = modules;
        this.email = email;
    }

    @Override
    public int getId() {
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
        cv.put(DatabaseHelper.User.COLUMN_CODE, code);
        cv.put(DatabaseHelper.User.COLUMN_FIRSTNAME, firstName);
        cv.put(DatabaseHelper.User.COLUMN_LASTNAME, lastName);
        cv.put(DatabaseHelper.User.COLUMN_USERNAME, username);
        cv.put(DatabaseHelper.User.COLUMN_FULLNAME, fullName);
        cv.put(DatabaseHelper.User.COLUMN_PASSWORD, password);
        cv.put(DatabaseHelper.User.COLUMN_EMAIL, email);
        cv.put(DatabaseHelper.User.COLUMN_MODULES, modules);
        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.User.ALL_COLUMNS;
    }
}
