package org.philimone.hds.explorer.database;

import android.database.Cursor;

import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
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

}
