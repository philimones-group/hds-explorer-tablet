package org.philimone.hds.explorer.init;

import android.app.Application;

import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;

public class ApplicationInit extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initDatabases();
    }

    private void initDatabases() {
        ObjectBoxDatabase.init(this);

        Bootstrap bootstrap = new Bootstrap(this);
        bootstrap.init();
    }

}
