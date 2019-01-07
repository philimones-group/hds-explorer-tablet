package org.philimone.hds.explorer.database;

import android.content.Context;
import android.database.Cursor;

import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.DataSet;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.SyncReport;
import org.philimone.hds.explorer.model.followup.TrackingList;
import org.philimone.hds.explorer.model.followup.TrackingMemberList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 5/27/16.
 */
public class Queries {

    public static ApplicationParam getApplicationParamBy(Database database, String whereClause, String[] clauseArgs){
        ApplicationParam param = null;

        Cursor cursor = database.query(ApplicationParam.class, whereClause, clauseArgs, null, null, null);

        if (cursor.moveToFirst()){
            param = Converter.cursorToApplicationParam(cursor);
        }

        return param;
    }

    public static List<ApplicationParam> getAllApplicationParamBy(Database database, String whereClause, String[] clauseArgs){
        List<ApplicationParam> list = new ArrayList<>();

        Cursor cursor = database.query(ApplicationParam.class, whereClause, clauseArgs, null, null, null);

        while (cursor.moveToNext()){
            ApplicationParam param = Converter.cursorToApplicationParam(cursor);
            list.add(param);
        }

        return list;
    }

    public static String getApplicationParamValue(String name, Context context){
        String value = null;

        Database database = new Database(context);
        database.open();

        ApplicationParam param = getApplicationParamBy(database, DatabaseHelper.ApplicationParam.COLUMN_NAME+"=?", new String[]{ name });

        if (param != null){
            value = param.getValue();
        }

        database.close();

        return value;
    }

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

    public static Region getRegionBy(Database database, String whereClause, String[] clauseArgs){
        Region region = null;

        Cursor cursor = database.query(Region.class, whereClause, clauseArgs, null, null, null);

        if (cursor.moveToNext()){
            region = Converter.cursorToRegion(cursor);
        }

        return region;
    }

    public static List<Region> getAllRegionBy(Database database, String whereClause, String[] clauseArgs){
        List<Region> list = new ArrayList<>();

        Cursor cursor = database.query(Region.class, whereClause, clauseArgs, null, null, null);

        while (cursor.moveToNext()){
            Region region = Converter.cursorToRegion(cursor);
            list.add(region);
        }

        return list;
    }

    public static Household getHouseholdBy(Database database, String whereClause, String[] clauseArgs){
        Household household = null;

        Cursor cursor = database.query(Household.class, whereClause, clauseArgs, null, null, null);

        if (cursor.moveToFirst()){
            household = Converter.cursorToHousehold(cursor);
        }

        return household;
    }

    public static List<Household> getAllHouseholdBy(Database database, String whereClause, String[] clauseArgs){
        List<Household> list = new ArrayList<>();

        Cursor cursor = database.query(Household.class, whereClause, clauseArgs, null, null, null);

        while (cursor.moveToNext()){
            Household household = Converter.cursorToHousehold(cursor);
            list.add(household);
        }

        return list;
    }

    public static Member getMemberBy(Database database, String whereClause, String[] clauseArgs){
        Member member = null;

        Cursor cursor = database.query(Member.class, whereClause, clauseArgs, null, null, null);

        if (cursor.moveToFirst()){
            member = Converter.cursorToMember(cursor);
        }

        return member;
    }

    public static List<Member> getAllMemberBy(Database database, String whereClause, String[] clauseArgs){
        List<Member> list = new ArrayList<>();

        Cursor cursor = database.query(Member.class, whereClause, clauseArgs, null, null, DatabaseHelper.Member.COLUMN_CODE);

        while (cursor.moveToNext()){
            Member member = Converter.cursorToMember(cursor);
            list.add(member);
        }

        return list;
    }

    public static TrackingList getTrackingListBy(Database database, String whereClause, String[] clauseArgs){
        TrackingList trackingList = null;

        Cursor cursor = database.query(TrackingList.class, whereClause, clauseArgs, null, null, null);

        if (cursor.moveToFirst()){
            trackingList = Converter.cursorToTrackingList(cursor);
        }

        return trackingList;
    }

    public static List<TrackingList> getAllTrackingListBy(Database database, String whereClause, String[] clauseArgs){
        List<TrackingList> list = new ArrayList<>();

        Cursor cursor = database.query(TrackingList.class, whereClause, clauseArgs, null, null, null);

        while (cursor.moveToNext()){
            TrackingList trackingList = Converter.cursorToTrackingList(cursor);
            list.add(trackingList);
        }

        return list;
    }

    public static TrackingMemberList getTrackingMemberListBy(Database database, String whereClause, String[] clauseArgs){
        TrackingMemberList trackingList = null;

        Cursor cursor = database.query(TrackingMemberList.class, whereClause, clauseArgs, null, null, null);

        if (cursor.moveToFirst()){
            trackingList = Converter.cursorToTrackingMembersList(cursor);
        }

        return trackingList;
    }

    public static List<TrackingMemberList> getAllTrackingMemberListBy(Database database, String whereClause, String[] clauseArgs){
        List<TrackingMemberList> list = new ArrayList<>();

        Cursor cursor = database.query(TrackingMemberList.class, whereClause, clauseArgs, null, null, null);

        while (cursor.moveToNext()){
            TrackingMemberList trackingList = Converter.cursorToTrackingMembersList(cursor);
            list.add(trackingList);
        }

        return list;
    }

    public static DataSet getDataSetBy(Database database, String whereClause, String[] clauseArgs){
        DataSet dataSet = null;

        Cursor cursor = database.query(DataSet.class, whereClause, clauseArgs, null, null, null);

        if (cursor.moveToFirst()){
            dataSet = Converter.cursorToDataSet(cursor);
        }

        return dataSet;
    }

    public static List<DataSet> getAllDataSetBy(Database database, String whereClause, String[] clauseArgs){
        List<DataSet> list = new ArrayList<>();

        Cursor cursor = database.query(DataSet.class, whereClause, clauseArgs, null, null, null);

        while (cursor.moveToNext()){
            DataSet dataSet = Converter.cursorToDataSet(cursor);
            list.add(dataSet);
        }

        return list;
    }
}
