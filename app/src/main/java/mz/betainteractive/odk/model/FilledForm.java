package mz.betainteractive.odk.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;

/**
 * A filled form represents an ODK form that has been prefilled with values from
 * the OpenHDS application.
 */
public class FilledForm {
    private String formName;
    private Map<String,Object> values;
    
    public FilledForm(String formName) {
        this.formName = formName;
        this.values = new HashMap<String, Object>();
    }
    
    public void put(String variable, Object value){
    	values.put(variable, value);
    }

	public void putAll(Map<String, Object> mapValues){
		values.putAll(mapValues);
	}
    
    public Object get(String variable){
    	return values.get(variable);
    }

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public List<String> getVariables(){
		return new ArrayList<String>(this.values.keySet()); 
	}

	public void setValues(ContentValues contentValues) {
		for (String key : contentValues.keySet()){
			String value = contentValues.getAsString(key);
			values.put(key, value);
		}
	}
    
	
}
