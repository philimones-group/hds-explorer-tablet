package net.manhica.clip.explorer.database;

import android.database.Cursor;

import net.manhica.clip.explorer.model.SyncReport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 5/27/16.
 */
public class Queries {
    public static SyncReport getSyncReportBy(Database database, String whereClause, String[] clauseArgs){
        SyncReport report = null;

        Cursor cursor = database.query(SyncReport.class, whereClause, clauseArgs, null, null, null);

        if (cursor.moveToFirst()){
            report = Converter.cursorToSyncReport(cursor);
        }

        return report;
    }

    public static List<SyncReport> getAllSyncReportBy(Database database, String whereClause, String[] clauseArgs){
        List<SyncReport> list = new ArrayList<>();

        Cursor cursor = database.query(SyncReport.class, whereClause, clauseArgs, null, null, null);

        while (cursor.moveToNext()){
            SyncReport report = Converter.cursorToSyncReport(cursor);
            list.add(report);
        }

        return list;
    }
}
