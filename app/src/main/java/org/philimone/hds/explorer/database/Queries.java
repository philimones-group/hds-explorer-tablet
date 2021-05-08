package org.philimone.hds.explorer.database;

import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.SyncReport;
import org.philimone.hds.explorer.model.SyncReport_;
import org.philimone.hds.explorer.model.enums.SyncEntity;

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

    public static Household getHouseholdByCode(Box<Household> box, String code){
        Household household = box.query().equal(Household_.code, code).build().findFirst();

        return household;
    }

    public static Member getMemberByCode(Box<Member> box, String code){

        Member member = box.query().equal(Member_.code, code).build().findFirst();
        return member;
    }

    public static Member getMemberById(Box<Member> box, long id){

        Member member = box.query().equal(Member_.id, id).build().findFirst();
        return member;
    }

}
