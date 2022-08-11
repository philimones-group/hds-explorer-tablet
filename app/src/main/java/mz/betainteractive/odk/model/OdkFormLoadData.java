package mz.betainteractive.odk.model;

import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.FormGroupInstance;

public class OdkFormLoadData {
    public Form form;
    public String formId;
    public String formInstanceUri;
    public FilledForm preloadedData;
    public boolean isFormGroupLoad;
    public String formGroupId;
    public String formGroupName;
    public String formGroupInstanceUuid;
    public boolean skipUnfinalizedCheck;

    public OdkFormLoadData(Form form, FilledForm preloadedData, boolean isFormGroupLoad) {
        this.form = form;
        this.formId = (form != null) ? form.formId : null;
        this.preloadedData = preloadedData;
        this.isFormGroupLoad = isFormGroupLoad;
    }

    public OdkFormLoadData(String formId, FilledForm preloadedData, boolean isFormGroupLoad) {
        this.form = null;
        this.formId = formId;
        this.preloadedData = preloadedData;
        this.isFormGroupLoad = isFormGroupLoad;
    }

    public OdkFormLoadData(String formId, FilledForm preloadedData, boolean isFormGroupLoad, boolean skipUnfinalizedFormCheck) {
        this.form = null;
        this.formId = formId;
        this.preloadedData = preloadedData;
        this.isFormGroupLoad = isFormGroupLoad;
        this.skipUnfinalizedCheck = skipUnfinalizedFormCheck;
    }

    public OdkFormLoadData(Form form, FilledForm preloadedData, boolean isFormGroupLoad, FormGroupInstance formGroupInstance) {
        this.form = form;
        this.formId = (form != null) ? form.formId : null;
        this.preloadedData = preloadedData;
        this.isFormGroupLoad = isFormGroupLoad;
        this.formGroupId = formGroupInstance==null ? null : formGroupInstance.groupFormId;
        this.formGroupName = formGroupInstance==null ? null : formGroupInstance.groupFormName;
        this.formGroupInstanceUuid = formGroupInstance==null ? null : formGroupInstance.instanceUuid;
    }

    public OdkFormLoadData(String formId, FilledForm preloadedData, boolean isFormGroupLoad, FormGroupInstance formGroupInstance) {
        this.form = null;
        this.formId = formId;
        this.preloadedData = preloadedData;
        this.isFormGroupLoad = isFormGroupLoad;
        this.formGroupId = formGroupInstance.groupFormId;
        this.formGroupName = formGroupInstance.groupFormName;
        this.formGroupInstanceUuid = formGroupInstance==null ? null : formGroupInstance.instanceUuid;
    }
}
