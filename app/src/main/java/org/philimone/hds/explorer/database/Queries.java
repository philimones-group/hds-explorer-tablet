package org.philimone.hds.explorer.database;

import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.Death;
import org.philimone.hds.explorer.model.Death_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Module;
import org.philimone.hds.explorer.model.Module_;
import org.philimone.hds.explorer.model.SyncReport;
import org.philimone.hds.explorer.model.SyncReport_;
import org.philimone.hds.explorer.model.enums.SyncEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.StringUtil;

/**
 * Created by paul on 5/27/16.
 */
public class Queries {

    public static ApplicationParam getApplicationParamBy(Box<ApplicationParam> box, String name){
        ApplicationParam param = box.query().equal(ApplicationParam_.name, name, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
        return param;
    }

    public static String getApplicationParamValue(Box<ApplicationParam> box, String name){
        ApplicationParam param = box.query().equal(ApplicationParam_.name, name, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (param != null){
            return param.getValue();
        }

        return null;
    }

    public static SyncReport getSyncReportBy(Box<SyncReport> box, SyncEntity entity){
        SyncReport report = box.query().equal(SyncReport_.reportId, entity.getId()).build().findFirst();
        return report;
    }

    public static Household getHouseholdByCode(Box<Household> box, String code){
        Household household = box.query().equal(Household_.code, code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        return household;
    }

    public static Member getMemberByCode(Box<Member> box, String code){

        Member member = box.query().equal(Member_.code, code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
        return member;
    }

    public static Member getMemberById(Box<Member> box, long id){

        Member member = box.get(id);
        return member;
    }

    public static Death getDeathByCode(Box<Death> box, String code){

        Death death = box.query().equal(Death_.memberCode, code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
        return death;
    }

    public static List<CollectedData> getCollectedDataBy(Box<CollectedData> box, Collection<? extends String> modules) {
        List<CollectedData> list = new ArrayList<>();

        modules.forEach( moduleCode -> {
            List<CollectedData> clist = box.query().filter((collectedData) -> collectedData.formModules.contains(moduleCode)).build().find();
            list.addAll(clist);
        });

        return list;
    }

    public static List<Module> getModulesBy(Box<Module> box, Collection<? extends String> codes) {

        String[] codesArray = codes.toArray(new String[codes.size()]);

        List<Module> list = box.query().in(Module_.code, codesArray, QueryBuilder.StringOrder.CASE_SENSITIVE).build().find();

        return list;
    }
}
