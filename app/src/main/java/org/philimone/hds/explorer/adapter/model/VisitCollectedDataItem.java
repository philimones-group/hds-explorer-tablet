package org.philimone.hds.explorer.adapter.model;

import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreFormExtension;
import org.philimone.hds.explorer.model.Form;

import java.util.Date;

public class VisitCollectedDataItem {
    private CoreCollectedData coreCollectedData;
    private CollectedData odkCollectedData;

    public String formEntityCode;
    public String formEntityName;
    public Form odkForm;
    public Date createdDate;
    public Date updatedDate;
    public boolean uploaded;
    public Date uploadedDate;


    public VisitCollectedDataItem(CoreCollectedData collectedData) {
        this.coreCollectedData = collectedData;

        this.formEntityCode = collectedData.formEntityCode;
        this.formEntityName = collectedData.formEntityName;
        this.createdDate = collectedData.createdDate;
        this.updatedDate = collectedData.updatedDate;
        this.uploaded = collectedData.uploaded;
        this.updatedDate = collectedData.uploadedDate;
    }

    public VisitCollectedDataItem(CollectedData collectedData, Form form) {
        this.odkCollectedData = collectedData;
        this.odkForm = form;

        this.formEntityCode = collectedData.formUri;
        this.formEntityName = collectedData.formInstanceName;
        this.createdDate = collectedData.formLastUpdatedDate;
        //this.updatedDate = collectedData.updatedDate;
        //this.uploaded = collectedData.uploaded;
        //this.updatedDate = collectedData.uploadedDate;
    }

    public CoreCollectedData getCoreCollectedData() {
        return coreCollectedData;
    }

    public CollectedData getOdkCollectedData() {
        return odkCollectedData;
    }

    public boolean isCoreCollectedData(){
        return this.coreCollectedData != null;
    }

    public boolean isOdkCollectedData() {
        return this.odkCollectedData != null;
    }

    public CoreFormExtension getExtension() {
        return isCoreCollectedData() ? this.coreCollectedData.extension.getTarget() : null;
    }

    public boolean isExtensionCollected(){
        return isCoreCollectedData() ? this.coreCollectedData.extensionCollected : false;
    }

}

