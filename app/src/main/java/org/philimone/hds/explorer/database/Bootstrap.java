package org.philimone.hds.explorer.database;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.SyncReport;

import java.io.File;

/**
 * Created by paul on 5/26/16.
 * Will be used to initialize any data on database tables
 */
public class Bootstrap {
    private static final String APP_PATH = "org.philimone.hds.explorer";
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
            SyncReport sr7 = new SyncReport(SyncReport.REPORT_PARAMETERS, null, SyncReport.STATUS_NOT_SYNCED, "Sync. App Parameters");
            SyncReport sr8 = new SyncReport(SyncReport.REPORT_REGIONS, null, SyncReport.STATUS_NOT_SYNCED, "Sync. Regions");

            database.insert(sr1);
            database.insert(sr2);
            database.insert(sr3);
            database.insert(sr4);
            database.insert(sr5);
            database.insert(sr6);
            database.insert(sr7);
            database.insert(sr8);
        }

        Cursor cursorParams = database.query(ApplicationParam.class, null, null, null, null, null);

        if (cursorParams.getCount()==0){
            ApplicationParam param1 = new ApplicationParam(ApplicationParam.APP_URL, "string", "https://icemr2-aws.medcol.mw:8443/hds-explorer-server"); // Server URL
            ApplicationParam param2 = new ApplicationParam(ApplicationParam.ODK_URL, "string", "https://icemr2-aws.medcol.mw:8443/ODKAggregate");      // ODK Aggregate Server URL
            ApplicationParam param3 = new ApplicationParam(ApplicationParam.REDCAP_URL, "string", "https://apps.betainteractive.net/redcap");           // REDCap Server URL

            database.insert(param1);
            database.insert(param2);
            database.insert(param3);
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
