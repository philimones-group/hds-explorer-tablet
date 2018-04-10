package mz.betainteractive.odk.listener;

import android.net.Uri;

import java.io.File;

/**
 * Created by paul on 8/11/16.
 */
public interface OdkFormResultListener {

    public void onFormFinalized(Uri contentUri, File xmlFile, String metaInstanceName, String lastUpdatedDate);

    public void onFormUnFinalized(Uri contentUri, File xmlFile, String metaInstanceName, String lastUpdatedDate);

    public void onDeleteForm(Uri contentUri);

    /* For cases where the uri of form was not found */
    public void onFormNotFound(Uri contenUri);

}
