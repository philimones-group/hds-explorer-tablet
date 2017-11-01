package net.manhica.dss.explorer.database;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import net.manhica.dss.explorer.model.SyncReport;

import java.io.File;

/**
 * Created by paul on 5/26/16.
 * Will be used to initialize any data on database tables
 */
public class Bootstrap {
    private static final String APP_PATH = "net.manhica.dss.explorer";
    private Database database;

    public Bootstrap(Context context){
        this.database = new Database(context);
    }

    public void init(){
        database.open();

        Cursor cursor = database.query(SyncReport.class, null, null, null, null, null);

        //Initialize SyncReport
        if (cursor.getCount()==0) {
            SyncReport sr1 = new SyncReport(SyncReport.REPORT_MODULES, null, SyncReport.STATUS_NOT_SYNCED, "Sync. Modules");
            SyncReport sr2 = new SyncReport(SyncReport.REPORT_FORMS, null, SyncReport.STATUS_NOT_SYNCED, "Sync. Forms");
            SyncReport sr3 = new SyncReport(SyncReport.REPORT_USERS, null, SyncReport.STATUS_NOT_SYNCED, "Sync. Users");
            SyncReport sr4 = new SyncReport(SyncReport.REPORT_HOUSEHOLDS, null, SyncReport.STATUS_NOT_SYNCED, "Sync. Households");
            SyncReport sr5 = new SyncReport(SyncReport.REPORT_MEMBERS, null, SyncReport.STATUS_NOT_SYNCED, "Sync. Members");
            SyncReport sr6 = new SyncReport(SyncReport.REPORT_TRACKING_LISTS, null, SyncReport.STATUS_NOT_SYNCED, "Sync. Tracking Lists");

            database.insert(sr1);
            database.insert(sr2);
            database.insert(sr3);
            database.insert(sr4);
            database.insert(sr5);
            database.insert(sr6);
        }

        database.close();

        initializePaths();
    }

    public void initializePaths(){
        File root = Environment.getExternalStorageDirectory();
        String destinationPath = root.getAbsolutePath() + File.separator + "Android" + File.separator + "data"
                + File.separator + APP_PATH + File.separator + "files"+ File.separator;

        File baseDir = new File(destinationPath);

        if (!baseDir.exists()) {
            boolean created = baseDir.mkdirs();
            if (!created) {
                Log.d("app-dirs", "not created");
            }else{
                Log.d("app-dirs", "created");
            }
        }
    }

    public static String getAppPath(){
        File root = Environment.getExternalStorageDirectory();
        String destinationPath = root.getAbsolutePath() + File.separator + "Android" + File.separator + "data"
                + File.separator + APP_PATH + File.separator + "files"+ File.separator;

        return destinationPath;
    }

    public void dropTables(){
        database.open();
        database.dropAllTables();
        database.close();
    }
}
