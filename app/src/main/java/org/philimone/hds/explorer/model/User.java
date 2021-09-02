package org.philimone.hds.explorer.model;

import org.philimone.hds.explorer.model.converters.StringCollectionConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.Transient;
import io.objectbox.annotation.Unique;
import mz.betainteractive.utilities.ReflectionUtils;

/**
 * Created by paul on 5/20/16.
 */
@Entity
public class User implements FormSubject, Serializable {

    @Id
    public long id;
    @Unique
    public String code;
    public String firstName;
    public String lastName;
    public String fullName;
    @Unique
    public String username;
    public String password;
    public String email;

    @Index
    @Convert(converter = StringCollectionConverter.class, dbType = String.class)
    public Set<String> modules;

    @Transient
    private List<Module> selectedModules;
    @Transient
    private String selectedModulesText;
    @Transient
    private String selectedModulesCodesText;

    public User(){
        this.modules = new HashSet<>();
        this.selectedModules = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public Set<String> getModules() {
        return modules;
    }

    public void setModules(Collection<? extends String> modules) {
        this.modules.addAll(modules);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Module> getSelectedModules() {
        return selectedModules;
    }

    public void setSelectedModules(List<Module> selectedModules) {
        StringBuilder str = new StringBuilder();

        for (Module module : selectedModules) {
            str.append((str.length()==0 ? "" : ",") + module.name);
            this.selectedModules.add(module);
        }
    }

    public String getModulesNamesAsText(Set<String> modules) {
        StringBuilder str = new StringBuilder();

        for (Module module : this.selectedModules) {
            if (modules.contains(module.code)) {
                str.append((str.length()==0 ? "" : ",") + module.name);
            }
        }

        return str.toString();
    }

    public String toString(){
        return ""+this.firstName+" "+this.lastName+", user: "+this.username;
    }

    public String getValueByName(String variableName){
        return ReflectionUtils.getValueByName(this, variableName);
    }

    public String getTableName() {
        return "User";
    }

}
