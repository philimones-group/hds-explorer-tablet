package org.philimone.hds.explorer.database;

import android.content.Context;
import android.util.Log;

import org.philimone.hds.explorer.BuildConfig;
import org.philimone.hds.explorer.model.MyObjectBox;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;

/*
 * Initialize ObjectBox Database
 */

public class ObjectBoxDatabase {
    private static BoxStore boxStore;

    public static void init(Context context) {
        boxStore = MyObjectBox.builder().androidContext(context.getApplicationContext()).build();

        if (BuildConfig.DEBUG) {
            boolean started = new AndroidObjectBrowser(boxStore).start(context);
            Log.i("ObjectBrowser", "Started: " + started);
        }
    }

    public static BoxStore get() {
        return boxStore;
    }
}
