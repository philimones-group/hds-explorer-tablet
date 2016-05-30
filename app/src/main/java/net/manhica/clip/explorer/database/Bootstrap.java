package net.manhica.clip.explorer.database;

import android.content.Context;
import android.database.Cursor;

import net.manhica.clip.explorer.model.SyncReport;

import java.util.List;

/**
 * Created by paul on 5/26/16.
 * Will be used to initialize any data on database tables
 */
public class Bootstrap {

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

            database.insert(sr1);
            database.insert(sr2);
            database.insert(sr3);
            database.insert(sr4);
            database.insert(sr5);
        }

        database.close();
    }

    public void dropTables(){
        database.open();
        database.dropAllTables();
        database.close();
    }
}
