package mz.betainteractive.odk.listener;

import android.net.Uri;

import java.io.File;

/**
 * Created by paul on 8/11/16.
 */
public interface OdkFormResultListener {

    public void onFormFinalized(Uri contentUri, File xmlFile);

    public void onFormUnFinalized(Uri contentUri, File xmlFile);

    public void onDeleteForm(Uri contentUri);

}
