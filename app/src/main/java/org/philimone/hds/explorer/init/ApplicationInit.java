package org.philimone.hds.explorer.init;

import android.app.Application;

import org.philimone.hds.explorer.database.ObjectBoxDatabase;

public class ApplicationInit extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ObjectBoxDatabase.init(this);
    }

}
