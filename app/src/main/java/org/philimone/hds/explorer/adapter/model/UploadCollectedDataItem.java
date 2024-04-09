package org.philimone.hds.explorer.adapter.model;

import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreFormExtension;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.Visit_;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;

import io.objectbox.Box;
import mz.betainteractive.odk.FormUtilities;

public class UploadCollectedDataItem {
    private CoreCollectedData coreCollectedData;
    private CollectedData odkCollectedData;

    public FormUtilities.FormStatus odkFormStatus;

    private Box<Visit> boxVisits;

    public UploadCollectedDataItem(Box<Visit> visitBox, CoreCollectedData collectedData, FormUtilities.FormStatus status) {
        this.boxVisits = visitBox;
        this.coreCollectedData = collectedData;
        this.odkFormStatus = status;
    }

    public CoreCollectedData getCoreCollectedData() {
        return coreCollectedData;
    }

    public CollectedData getOdkCollectedData() {
        return odkCollectedData;
    }

    public CoreFormExtension getExtension() {
        return this.coreCollectedData != null ? this.coreCollectedData.extension.getTarget() : null;
    }

    public boolean isExtensionCollected(){
        return this.coreCollectedData != null && this.coreCollectedData.extensionCollected;
    }

    /**
     * This method will be used has a condition to upload Core Forms data to the server
     * If a Form extension is not required and not collected           -> can upload
     * If a Form extension is collected and finalized                  -> can upload
     * If a Form extension is collected and not finalized or not found -> cannot upload
     * @return
     */
    public boolean isFormExtensionValid() {
        CoreFormExtension extension = this.coreCollectedData.extension.getTarget();

        if (extension == null) return true;

        if (!extension.enabled) {
            return true; //Not Enabled Extension means that FormExtension is Valid (no need for checks)
        }

        if (coreCollectedData.extensionCollected) {
            if (odkFormStatus == FormUtilities.FormStatus.FINALIZED) { //doesnt matter if it is required
                return true;  //can upload because its finalized
            } else {
                return false; //dont upload
            }
        }

        if (extension.required) {
            if (extension.formEntity== CoreFormEntity.VISIT) {
                boolean isVisitNotPossible = boxVisits.query(Visit_.collectedId.equal(coreCollectedData.collectedId).and(Visit_.visitPossible.equal(false))).build().count()>0;
                return isVisitNotPossible;
            }

            return false; //dont upload because extension is not collected.
        }

        return true;
    }

}

