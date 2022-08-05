package mz.betainteractive.odk.listener;

import android.net.Uri;

import java.io.File;
import java.util.Date;

import mz.betainteractive.odk.model.OdkFormLoadData;

/**
 * Created by paul on 8/11/16.
 */
public interface OdkFormResultListener {

    public void onFormFinalized(OdkFormLoadData formLoadData, Uri contentUri, String formId, String instanceFileUri, String metaInstanceName, Date lastUpdatedDate);

    public void onFormUnFinalized(OdkFormLoadData formLoadData, Uri contentUri, String formId, String instanceFileUri, String metaInstanceName, Date lastUpdatedDate);

    public void onDeleteForm(OdkFormLoadData formLoadData, Uri contentUri, String instanceFileUri);

    /* For cases where the uri of form was not found */
    public void onFormInstanceNotFound(OdkFormLoadData formLoadData, Uri contenUri);

}
