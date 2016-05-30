package net.manhica.clip.explorer.model;

import android.content.ContentValues;

import net.manhica.clip.explorer.database.DatabaseHelper;
import net.manhica.clip.explorer.database.Table;

import java.io.Serializable;

/**
 * Created by paul on 5/20/16.
 */
public class User implements Serializable, Table {

    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String modules;

    public User(){

    }

    public User(String firstName, String lastName, String username, String password, String modules) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.modules = modules;
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

    public String getFullname(){
        return firstName + " " +lastName;
    }

    public String toString(){
        return ""+this.firstName+" "+this.lastName+", user: "+this.username;
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
        return cv;
    }

    @Override
    public String[] getColumnNames() {
        return DatabaseHelper.User.ALL_COLUMNS;
    }
}
