package org.philimone.hds.explorer.adapter.model;

import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.FormGroupInstanceChild;
import org.philimone.hds.explorer.model.FormGroupMapping;

public class FormGroupChildItem {
    private FormDataLoader formDataLoader;
    private FormGroupMapping formGroupMapping;
    private FormGroupInstanceChild childInstance;
    private CollectedData collectedData;

    private FormGroupChildItem previousItem;

    public FormGroupChildItem(FormDataLoader formDataLoader, FormGroupMapping formGroupMapping, FormGroupInstanceChild childInstance, CollectedData collectedData) {
        this.formDataLoader = formDataLoader;
        this.formGroupMapping = formGroupMapping;
        this.childInstance = childInstance;
        this.collectedData = collectedData;
    }

    public Form getForm() {
        return this.formDataLoader.getForm();
    }

    public FormDataLoader getFormDataLoader() {
        return formDataLoader;
    }

    public void setFormDataLoader(FormDataLoader formDataLoader) {
        this.formDataLoader = formDataLoader;
    }

    public FormGroupMapping getFormGroupMapping() {
        return formGroupMapping;
    }

    public void setFormGroupMapping(FormGroupMapping formGroupMapping) {
        this.formGroupMapping = formGroupMapping;
    }

    public FormGroupInstanceChild getChildInstance() {
        return childInstance;
    }

    public void setChildInstance(FormGroupInstanceChild childInstance) {
        this.childInstance = childInstance;
    }

    public CollectedData getCollectedData() {
        return collectedData;
    }

    public void setCollectedData(CollectedData collectedData) {
        this.collectedData = collectedData;
    }

    public FormGroupChildItem getPreviousItem() {
        return previousItem;
    }

    public void setPreviousItem(FormGroupChildItem previousItem) {
        this.previousItem = previousItem;
    }
}
