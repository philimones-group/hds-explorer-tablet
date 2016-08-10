package net.manhica.clip.explorer.data;

import net.manhica.clip.explorer.model.Form;

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
}
