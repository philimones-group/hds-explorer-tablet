package org.philimone.hds.explorer.database;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import org.philimone.hds.explorer.io.SyncEntity;
import org.philimone.hds.explorer.io.SyncStatus;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.SyncReport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.objectbox.Box;

import static org.philimone.hds.explorer.model.SyncReport.*;

/**
 * Created by paul on 5/26/16.
 * Will be used to initialize any data on database tables
 */
public class Bootstrap {
    private static final String APP_PATH = "org.philimone.hds.explorer";
    private Database database;

    private Box<ApplicationParam> boxAppParams;

    public Bootstrap(Context context){
        this.database = new Database(context);
        initBoxes();
    }

    private void initBoxes() {
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
    }

    public void init(){
        database.open();

        insertSyncReports();
        insertParams();

        database.close();

        initializePaths();
    }

    private void insertParams(){
        if (boxAppParams.count()==0){
            boxAppParams.put(new ApplicationParam(ApplicationParam.APP_URL, "string", "https://icemr2-aws.medcol.mw:8443/hds-explorer-server")); // Server URL
            boxAppParams.put(new ApplicationParam(ApplicationParam.ODK_URL, "string", "https://icemr2-aws.medcol.mw:8443/ODKAggregate"));        // ODK Aggregate Server URL
            boxAppParams.put(new ApplicationParam(ApplicationParam.REDCAP_URL, "string", "https://apps.betainteractive.net/redcap"));            // REDCap Server URL
        }
    }

    private void insertSyncReports(){
        List<SyncReport> reports = Queries.getAllSyncReportBy(database, null, null);
        List<SyncReport> newReports = new ArrayList<>();
        //Initialize SyncReport

        newReports.add(new SyncReport(SyncEntity.PARAMETERS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. App Parameters"));
        newReports.add(new SyncReport(SyncEntity.MODULES, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Modules"));
        newReports.add(new SyncReport(SyncEntity.FORMS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Forms"));
        newReports.add(new SyncReport(SyncEntity.DATASETS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Datasets"));
        newReports.add(new SyncReport(SyncEntity.DATASETS_CSV_FILES, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Datasets"));
        newReports.add(new SyncReport(SyncEntity.USERS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Users"));
        newReports.add(new SyncReport(SyncEntity.TRACKING_LISTS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Tracking Lists"));
        newReports.add(new SyncReport(SyncEntity.REGIONS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Regions"));
        newReports.add(new SyncReport(SyncEntity.HOUSEHOLDS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Households"));
        newReports.add(new SyncReport(SyncEntity.MEMBERS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Members"));

        List<SyncEntity> reportIds = reports.stream().map(SyncReport::getReportId).collect(Collectors.toList());

        newReports.forEach( report -> {
            if (!reportIds.contains(report.getReportId())){
                database.insert(report);
            }
        });

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
