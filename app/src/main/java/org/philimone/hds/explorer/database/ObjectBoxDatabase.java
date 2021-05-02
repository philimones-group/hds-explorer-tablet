package org.philimone.hds.explorer.database;

import android.content.Context;

import org.philimone.hds.explorer.model.MyObjectBox;
import io.objectbox.BoxStore;

/*
 * Initialize ObjectBox Database
 */

public class ObjectBoxDatabase {
    private static BoxStore boxStore;

    public static void init(Context context) {
        boxStore = MyObjectBox.builder().androidContext(context.getApplicationContext()).build();
    }

    public static BoxStore get() {
        return boxStore;
    }
}
