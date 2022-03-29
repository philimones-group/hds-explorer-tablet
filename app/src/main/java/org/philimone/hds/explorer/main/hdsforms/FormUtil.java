package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.CoreEntity;
import org.philimone.hds.explorer.model.Round;
import org.philimone.hds.explorer.model.Round_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.settings.generator.CodeGeneratorService;
import org.philimone.hds.forms.listeners.FormCollectionListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.parsers.ExcelFormParser;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import androidx.fragment.app.FragmentManager;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

public abstract class FormUtil<T extends CoreEntity> implements FormCollectionListener {

    protected enum Mode { CREATE, EDIT }

    protected FragmentManager fragmentManager;
    protected Context context;
    protected HForm form;
    protected T entity;
    protected CodeGeneratorService codeGenerator;
    protected Map<String, String> preloadedMap;

    protected boolean backgroundMode;
    protected boolean postExecution;
    protected Round currentRound;
    protected User user;

    protected Box<ApplicationParam> boxAppParams;
    protected Box<Round> boxRounds;

    protected Mode currentMode;
    
    protected FormUtilListener<T> listener;

    /* Load a Creator */

    /* Load a Editor */

    protected FormUtil(FragmentManager fragmentManager, Context context, HForm hform, FormUtilListener<T> listener){
        this.fragmentManager = fragmentManager;
        this.context = context;
        this.form = hform;
        this.user = Bootstrap.getCurrentUser();
        this.codeGenerator = new CodeGeneratorService();
        this.preloadedMap = new LinkedHashMap<>();

        this.currentMode = Mode.CREATE;
        
        this.listener = listener;
    }

    protected FormUtil(FragmentManager fragmentManager, Context context, HForm hform, T existentEntity, FormUtilListener<T> listener){
        this.fragmentManager = fragmentManager;
        this.context = context;
        this.form = hform;
        this.user = Bootstrap.getCurrentUser();
        this.codeGenerator = new CodeGeneratorService();
        this.preloadedMap = new LinkedHashMap<>();

        this.currentMode = Mode.EDIT;
        this.entity = existentEntity;

        this.listener = listener;
    }

    protected void initBoxes(){
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        this.boxRounds = ObjectBoxDatabase.get().boxFor(Round.class);
    }

    protected void initialize() {
        this.currentRound = this.boxRounds.query().order(Round_.roundNumber, QueryBuilder.DESCENDING).build().findFirst();
        postExecution = Queries.getApplicationParamValue(this.boxAppParams, ApplicationParam.HFORM_POST_EXECUTION).equals("true");
    }

    protected abstract void preloadValues();

    protected abstract void preloadUpdatedValues();

    public abstract void collect();

    protected void executeCollectForm() {
        if (currentMode == Mode.CREATE) {
            preloadValues();
            FormFragment form = FormFragment.newInstance(this.fragmentManager, this.form, Bootstrap.getInstancesPath(), user.username, preloadedMap, postExecution, backgroundMode, this);
            form.startCollecting();
        }

        if (currentMode == Mode.EDIT) {
            preloadUpdatedValues();
            FormFragment form = FormFragment.newInstance(this.fragmentManager, this.form, Bootstrap.getInstancesPath(), user.username, this.entity.getRecentlyCreatedUri(), preloadedMap, postExecution, false, true, this);
            form.startCollecting();
        }
    }

    /* statics */
    protected static HForm getVisitForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.visit_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getMemberEnuForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.member_enu_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getHouseholdForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.household_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getMaritalRelationshipForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.marital_relationship_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getExternalInMigrationForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.ext_inmigration_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getInMigrationForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.inmigration_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getOutmigrationForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.outmigration_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getPregnancyRegistrationForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.pregnancy_registration_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getPregnancyOutcomeForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.pregnancy_outcome_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getDeathForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.death_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getChangeHeadForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.change_head_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }

    protected static HForm getIncompleteVisitForm(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.incomplete_form);
        HForm form = new ExcelFormParser(inputStream).getForm();
        return form;
    }



}
