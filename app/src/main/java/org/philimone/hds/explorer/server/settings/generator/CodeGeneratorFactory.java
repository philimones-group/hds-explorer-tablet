package org.philimone.hds.explorer.server.settings.generator;

import android.util.Log;

import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

public class CodeGeneratorFactory {

    public static CodeGeneratorIncrementalRule INCREMENTAL_RULE = CodeGeneratorIncrementalRule.FILL_GAPS;

    public static CodeGenerator newInstance(){
        Box<ApplicationParam> box = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        ApplicationParam param = box.query().equal(ApplicationParam_.name, ApplicationParam.PARAMS_SYSTEM_CODE_GENERATOR, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
        ApplicationParam param2 = box.query().equal(ApplicationParam_.name, ApplicationParam.PARAMS_SYSTEM_CODE_GENERATOR_INCREMENTAL_RULE, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
        String codeGenClassName = param.getValue();
        String codeGenIncRule = param2.getValue();

        Class codeGenClass = null;
        try {
            codeGenClass = Class.forName(codeGenClassName);
            CodeGenerator result = (CodeGenerator) codeGenClass.newInstance();
            INCREMENTAL_RULE = CodeGeneratorIncrementalRule.getFrom(codeGenIncRule);

            Log.d("generator:", codeGenClassName+ " result: "+result+", rule: "+INCREMENTAL_RULE.code);

            return result;
        } catch (Exception e) {
            e.printStackTrace();

            assert 1==0;
        }

        return null;
    }

}
