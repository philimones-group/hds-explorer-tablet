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
    private final String householdPrefix = "Household.";
    private final String memberPrefix = "Member.";
    private final String userPrefix = "User.";

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
            if (key.startsWith(householdPrefix)) {
                String internalVariableName = key.replace(householdPrefix, "");
                String odkVariable = map.get(key);
                String value = null;
                ExtrasVariable extrasVariable = tryParseExtrasVariable(internalVariableName);

                if (extrasVariable == null){
                    value = household.getValueByName(internalVariableName);
                }else {
                    internalVariableName = extrasVariable.columnName;
                    String commaValue = household.getValueByName(internalVariableName);
                    value = commaValue.split(",")[extrasVariable.arrayIndex];
                }

                if (value == null) value = "";

                this.values.put(odkVariable, value);
                Log.d("h-odk auto-loadable", odkVariable + ", " + value);
            }
        }
    }

    public void loadMemberValues(Member member){
        Map<String, String> map = form.getBindMap();
        for (String key : map.keySet()){
            if (key.startsWith(memberPrefix)) {
                String internalVariableName = key.replace(memberPrefix, "");
                String odkVariable = map.get(key);
                String value = null;
                ExtrasVariable extrasVariable = tryParseExtrasVariable(internalVariableName);

                if (extrasVariable == null){
                    value = member.getValueByName(internalVariableName);
                }else {
                    internalVariableName = extrasVariable.columnName;
                    String commaValue = member.getValueByName(internalVariableName);
                    value = commaValue.split(",")[extrasVariable.arrayIndex];
                }

                if (value == null) value = "";

                this.values.put(odkVariable, value);
                Log.d("m-odk auto-loadable", odkVariable + ", " + value);
            }
        }
    }

    public void loadUserValues(User user){
        Map<String, String> map = form.getBindMap();
        for (String key : map.keySet()){
            if (key.startsWith(userPrefix)) {
                String internalVariableName = key.replace(userPrefix, "");
                String odkVariable = map.get(key);
                String value = null;
                ExtrasVariable extrasVariable = tryParseExtrasVariable(internalVariableName);

                if (extrasVariable == null){
                    value = user.getValueByName(internalVariableName);
                }else {
                    internalVariableName = extrasVariable.columnName;
                    String commaValue = user.getValueByName(internalVariableName);
                    value = commaValue.split(",")[extrasVariable.arrayIndex];
                }

                if (value == null) value = "";

                this.values.put(odkVariable, value);
                Log.d("u-odk auto-loadable", odkVariable + ", " + value);
            }
        }
    }

    private ExtrasVariable tryParseExtrasVariable(String str){
        if (str.matches(".+\\[[0-9]+\\]")){
            ExtrasVariable extras = new ExtrasVariable();
            int indexPar = str.indexOf("[");
            extras.columnName = str.substring(0, indexPar);
            extras.arrayIndex = Integer.parseInt(str.substring(indexPar+1,str.length()-1));

            return extras;
        }

        return null;
    }

    private class ExtrasVariable {
        public String columnName;
        public int arrayIndex;
    }
}
