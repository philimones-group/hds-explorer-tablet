package mz.betainteractive.odk.listener;

import android.net.Uri;

import java.io.File;
import java.util.Date;

import mz.betainteractive.odk.model.OdkFormLoadData;
import mz.betainteractive.odk.task.OdkFormLoadResult;

/**
 * Created by paul on 8/11/16.
 */
public interface OdkFormResultListener {

    public void onFormFinalized(OdkFormLoadData formLoadData, Uri formUri, Uri instanceUri, String formId, String instanceXmlPath, String metaInstanceName, Date lastUpdatedDate);

    public void onFormUnFinalized(OdkFormLoadData formLoadData, Uri formUri, Uri instanceUri, String formId, String instanceXmlPath, String metaInstanceName, Date lastUpdatedDate);

    public void onDeleteForm(OdkFormLoadData formLoadData, Uri formUri, Uri instanceUri, String instanceXmlPath);

    public void onFormLoadError(OdkFormLoadData formLoadData, OdkFormLoadResult result);

    /* For cases where the uri of form was not found */
    public void onFormInstanceNotFound(OdkFormLoadData formLoadData, Uri formUri, Uri instanceUri);

}
