package mz.betainteractive.odk.storage.access;

import androidx.annotation.StringRes;

import org.philimone.hds.explorer.R;

import java.util.HashMap;
import java.util.Map;

public enum OdkStorageType {

    NO_ODK_INSTALLED,
    ODK_SHARED_FOLDER,
    ODK_SCOPED_FOLDER_NO_PROJECTS,
    ODK_SCOPED_FOLDER_PROJECTS;

}