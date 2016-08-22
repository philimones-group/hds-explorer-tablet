package net.manhica.clip.explorer.database;

import android.database.Cursor;

import net.manhica.clip.explorer.model.CollectedData;
import net.manhica.clip.explorer.model.Form;
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

    public static Form getFormBy(Database database, String whereClause, String[] clauseArgs){
        Form form = null;

        Cursor cursor = database.query(Form.class, whereClause, clauseArgs, null, null, null);

        if (cursor.moveToFirst()){
            form = Converter.cursorToForm(cursor);
        }

        return form;
    }

    public static List<Form> getAllFormBy(Database database, String whereClause, String[] clauseArgs){
        List<Form> list = new ArrayList<>();

        Cursor cursor = database.query(Form.class, whereClause, clauseArgs, null, null, null);

        while (cursor.moveToNext()){
            Form form = Converter.cursorToForm(cursor);
            list.add(form);
        }

        return list;
    }

    public static CollectedData getCollectedDataBy(Database database, String whereClause, String[] clauseArgs){
        CollectedData cd = null;

        Cursor cursor = database.query(CollectedData.class, DatabaseHelper.CollectedData.ALL_COLUMNS,  whereClause, clauseArgs, null, null, null);

        if (cursor.moveToFirst()){
            cd = Converter.cursorToCollectedData(cursor);
        }

        return cd;
    }

    public static List<CollectedData> getAllCollectedDataBy(Database database, String whereClause, String[] clauseArgs){
        List<CollectedData> list = new ArrayList<>();

        Cursor cursor = database.query(CollectedData.class, DatabaseHelper.CollectedData.ALL_COLUMNS, whereClause, clauseArgs, null, null, null);

        while (cursor.moveToNext()){
            CollectedData cd = Converter.cursorToCollectedData(cursor);
            list.add(cd);
        }

        return list;
    }

}
