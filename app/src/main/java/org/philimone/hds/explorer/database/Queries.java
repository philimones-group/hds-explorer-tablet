package org.philimone.hds.explorer.database;

import android.content.Context;
import android.database.Cursor;

import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.DataSet;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.SyncReport;
import org.philimone.hds.explorer.model.SyncReport_;
import org.philimone.hds.explorer.model.enums.SyncEntity;
import org.philimone.hds.explorer.model.followup.TrackingList;
import org.philimone.hds.explorer.model.followup.TrackingSubjectList;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

/**
 * Created by paul on 5/27/16.
 */
public class Queries {

    public static ApplicationParam getApplicationParamBy(Box<ApplicationParam> box, String name){
        ApplicationParam param = box.query().equal(ApplicationParam_.name, name).build().findFirst();
        return param;
    }

    public static String getApplicationParamValue(Box<ApplicationParam> box, String name){
        ApplicationParam param = box.query().equal(ApplicationParam_.name, name).build().findFirst();

        if (param != null){
            return param.getValue();
        }

        return null;
    }

    public static SyncReport getSyncReportBy(Box<SyncReport> box, SyncEntity entity){
        SyncReport report = box.query().equal(SyncReport_.reportId, entity.getCode()).build().findFirst();
        return report;
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

    public static TrackingSubjectList getTrackingSubjectListBy(Database database, String whereClause, String[] clauseArgs){
        TrackingSubjectList trackingList = null;

        Cursor cursor = database.query(TrackingSubjectList.class, whereClause, clauseArgs, null, null, null);

        if (cursor.moveToFirst()){
            trackingList = Converter.cursorToTrackingSubjectList(cursor);
        }

        return trackingList;
    }

    public static List<TrackingSubjectList> getAllTrackingSubjectListBy(Database database, String whereClause, String[] clauseArgs){
        List<TrackingSubjectList> list = new ArrayList<>();

        Cursor cursor = database.query(TrackingSubjectList.class, whereClause, clauseArgs, null, null, null);

        while (cursor.moveToNext()){
            TrackingSubjectList trackingList = Converter.cursorToTrackingSubjectList(cursor);
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
