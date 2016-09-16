package net.manhica.clip.explorer.data;

import android.util.Log;

import net.manhica.clip.explorer.model.Form;
import net.manhica.clip.explorer.model.Household;
import net.manhica.clip.explorer.model.Member;
import net.manhica.clip.explorer.model.User;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 8/9/16.
 */
public class FormDataLoader implements Serializable {
    private Form form;
    private Map<String, Object> values;

    public FormDataLoader(){
        this.values = new HashMap<>();
    }

    public FormDataLoader(Form form){
        this();
        this.form = form;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values.putAll(values);
    }

    public void putExtra(String key, String value){
        this.values.put(key, value);
    }

    public void loadHouseholdValues(Household household){
        Map<String, String> map = form.getBindMap();
        for (String key : map.keySet()){
            String internalVariableName = key.replace("Household.","");
            String odkVariable = map.get(key);
            String value = household.getValueByName(internalVariableName);

            if (value==null) value="";

            this.values.put(odkVariable, value);
            Log.d("h-odk auto-loadable", odkVariable+", "+value);
        }
    }

    public void loadMemberValues(Member member){
        Map<String, String> map = form.getBindMap();
        for (String key : map.keySet()){
            String internalVariableName = key.replace("Member.","");
            String odkVariable = map.get(key);
            String value = member.getValueByName(internalVariableName);

            if (value==null) value="";

            this.values.put(odkVariable, value);
            Log.d("m-odk auto-loadable", odkVariable+", "+value);
        }
    }

    public void loadUserValues(User user){
        Map<String, String> map = form.getBindMap();
        for (String key : map.keySet()){
            String internalVariableName = key.replace("User.","");
            String odkVariable = map.get(key);
            String value = user.getValueByName(internalVariableName);

            if (value==null) value="";

            this.values.put(odkVariable, value);
            Log.d("u-odk auto-loadable", odkVariable+", "+value);
        }
    }
}
