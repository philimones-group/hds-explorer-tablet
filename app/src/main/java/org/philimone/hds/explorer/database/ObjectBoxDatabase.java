package org.philimone.hds.explorer.database;

import android.content.Context;
import android.util.Log;

import org.philimone.hds.explorer.BuildConfig;
import org.philimone.hds.explorer.model.MyObjectBox;
import io.objectbox.BoxStore;
import io.objectbox.BoxStoreBuilder;
import io.objectbox.android.Admin;
import io.objectbox.android.AndroidObjectBrowser;

/*
 * Initialize ObjectBox Database
 */

public class ObjectBoxDatabase {
    private static BoxStore boxStore;
    private static long maxDatabaseSize =  3 * 1024 * 1024; //3GB

    public static void init(Context context) {
        BoxStoreBuilder boxStoreBuilder = MyObjectBox.builder().androidContext(context.getApplicationContext());
        boxStoreBuilder = boxStoreBuilder.maxSizeInKByte(maxDatabaseSize);
        boxStore = boxStoreBuilder.build();


        if (BuildConfig.DEBUG) {
            boolean started = new Admin(boxStore).start(context);
            Log.i("ObjectBrowser", "Started: " + started);
        }
    }

    public static BoxStore get() {
        return boxStore;
    }
}
