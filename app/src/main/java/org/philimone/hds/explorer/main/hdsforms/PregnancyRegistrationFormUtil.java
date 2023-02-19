package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.PregnancyRegistration;
import org.philimone.hds.explorer.model.PregnancyRegistration_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.EstimatedDateOfDeliveryType;
import org.philimone.hds.explorer.model.enums.PregnancyStatus;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import java.util.Date;
import java.util.HashMap;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

public class PregnancyRegistrationFormUtil extends FormUtil<PregnancyRegistration> {

    private Box<Member> boxMembers;
    private Box<PregnancyRegistration> boxPregnancyRegistrations;
    
    //private Household household;
    private Visit visit;
    private Member mother;
    private boolean nonPregnantRegistration = false;
    private PregnancyStatus loadedPregnancyStatus;
    private int minimunMotherAge;

    public PregnancyRegistrationFormUtil(Fragment fragment, Context context, Visit visit, Household household, Member member, FormUtilities odkFormUtilities, FormUtilListener<PregnancyRegistration> listener){
        super(fragment, context, FormUtil.getPregnancyRegistrationForm(context), odkFormUtilities, listener);

        //Log.d("enu-household", ""+household);

        this.household = household;
        this.mother = member;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    public PregnancyRegistrationFormUtil(Fragment fragment, Context context, Visit visit, Household household, Member member, PregnancyStatus pregnancyStatus, FormUtilities odkFormUtilities, FormUtilListener<PregnancyRegistration> listener){
        super(fragment, context, FormUtil.getPregnancyRegistrationForm(context), odkFormUtilities, listener);

        //Log.d("enu-household", ""+household);

        this.household = household;
        this.mother = member;
        this.visit = visit;
        this.loadedPregnancyStatus = pregnancyStatus;

        initBoxes();
        initialize();

        if (pregnancyStatus != null && pregnancyStatus != PregnancyStatus.PREGNANT){
            this.nonPregnantRegistration = true;
            this.resumeMode = true;
        }
    }

    public PregnancyRegistrationFormUtil(Fragment fragment, Context context, Visit visit, Household household, PregnancyRegistration pregToEdit, FormUtilities odkFormUtilities, FormUtilListener<PregnancyRegistration> listener){
        super(fragment, context, FormUtil.getPregnancyRegistrationForm(context), pregToEdit, odkFormUtilities, listener);

        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();

        this.mother = this.boxMembers.query(Member_.code.equal(pregToEdit.motherCode)).build().findFirst();
    }

    public static PregnancyRegistrationFormUtil newInstance(Mode openMode, Fragment fragment, Context context, Visit visit, Household household, Member member, PregnancyRegistration pregToEdit, FormUtilities odkFormUtilities, FormUtilListener<PregnancyRegistration> listener){
        if (openMode == Mode.CREATE) {
            return new PregnancyRegistrationFormUtil(fragment, context, visit, household, member, odkFormUtilities, listener);
        } else if (openMode == Mode.EDIT) {
            return new PregnancyRegistrationFormUtil(fragment, context, visit, household, pregToEdit, odkFormUtilities, listener);
        }

        return null;
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxPregnancyRegistrations = ObjectBoxDatabase.get().boxFor(PregnancyRegistration.class);

    }

    @Override
    protected void initialize(){
        super.initialize();

        this.minimunMotherAge = retrieveMinimumMotherAge();
    }

    @Override
    protected void preloadValues() {
        //member_details_unknown_lbl

        preloadedMap.put("visitCode", this.visit.code);
        preloadedMap.put("code", codeGenerator.generatePregnancyCode(this.mother));
        preloadedMap.put("motherCode", this.mother.code);
        preloadedMap.put("motherName", this.mother.name);

        if (nonPregnantRegistration) {
            preloadedMap.put("status", loadedPregnancyStatus.code);
        }

        preloadedMap.put("modules", this.user.getSelectedModulesCodes());
    }

    @Override
    protected void preloadUpdatedValues() {

    }

    @Override
    public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colCode = collectedValues.get("code");
        ColumnValue colMotherCode = collectedValues.get("motherCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colMotherName = collectedValues.get("motherName"); //not blank
        ColumnValue colRecordedDate = collectedValues.get("recordedDate"); //not null cannot be in the future nor before dob
        ColumnValue colStatus = collectedValues.get("status");
        ColumnValue colEddKnown = collectedValues.get("eddKnown");
        ColumnValue colHasPrenatalRecord = collectedValues.get("hasPrenatalRecord");
        ColumnValue colEddDate = collectedValues.get("eddDate"); //not null, cannot be before dob+12
        ColumnValue colEddType = collectedValues.get("eddType");
        ColumnValue colPregMonths = collectedValues.get("pregMonths");
        ColumnValue colLmpKnown = collectedValues.get("lmpKnown");
        ColumnValue colLmpDate = collectedValues.get("lmpDate");
        ColumnValue colExpectedDelDate = collectedValues.get("expectedDeliveryDate");
        //ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        //ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        //ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String code = colCode.getValue();
        String motherCode = colMotherCode.getValue();
        String motherName = colMotherName.getValue();
        Date recordedDate = colRecordedDate.getDateValue();
        PregnancyStatus status = PregnancyStatus.getFrom(colStatus.getValue());
        Boolean eddKnown = StringUtil.toBoolean(colEddKnown.getValue());
        Boolean hasPrenatalRecord = StringUtil.toBoolean(colHasPrenatalRecord.getValue());
        Date eddDate = colEddDate.getDateValue();
        EstimatedDateOfDeliveryType eddType = EstimatedDateOfDeliveryType.getFrom(colEddType.getValue());
        Integer pregMonths = colPregMonths.getIntegerValue();
        Boolean lmpKnown = StringUtil.toBoolean(colEddKnown.getValue());
        Date lmpDate = colLmpDate.getDateValue();
        Date expectedDelDate = colExpectedDelDate.getDateValue();

        //validations
        //memberCode blank/valid
        if (!codeGenerator.isPregnancyCodeValid(code)){
            String message = this.context.getString(R.string.pregnancy_registration_code_err_lbl);
            return new ValidationResult(colMotherCode, message);
        }

        //code is duplicate
        if (currentMode == Mode.CREATE && boxPregnancyRegistrations.query().equal(PregnancyRegistration_.code, code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().count() > 0){
            String message = this.context.getString(R.string.pregnancy_registration_code_exists_lbl);
            return new ValidationResult(colCode, message);
        }

        if (StringUtil.isBlank(motherCode)){
            String message = this.context.getString(R.string.pregnancy_registration_mothercode_empty_lbl);
            return new ValidationResult(colMotherCode, message);
        }

        if (recordedDate == null) {
            String message = "";
            return new ValidationResult(colRecordedDate, message);
        }

        //validate preg status
        if (status == null){
            String message = this.context.getString(R.string.pregnancy_registration_status_empty_lbl);
            return new ValidationResult(colStatus, message);
        }

        if ((eddKnown != null && eddKnown==true) && status==PregnancyStatus.PREGNANT && eddDate == null){
            String message = this.context.getString(R.string.pregnancy_registration_eddate_empty_lbl);
            return new ValidationResult(colEddDate, message);
        }

        //eddDate cannot be before dob
        if (eddDate != null && eddDate.before(this.mother.dob)){ //is before dob
            String message = this.context.getString(R.string.pregnancy_registration_eddate_not_before_dob_lbl);
            return new ValidationResult(colEddDate, message);
        }

        if (currentMode == Mode.CREATE) {
            PregnancyRegistration pregReg = getLastPregnancyRegistration(this.mother);
            if (pregReg != null && pregReg.status == PregnancyStatus.PREGNANT) {
                String message = this.context.getString(R.string.pregnancy_registration_previous_pending_lbl);
                return new ValidationResult(colCode, message);
            }
        }


        return ValidationResult.noErrors();
    }

    @Override
    public void onBeforeFormFinished(HForm form, CollectedDataMap collectedValues) {
        //using it to update collectedHouseholdId, collectedMemberId
        ColumnValue colHouseholdId = collectedValues.get("collectedHouseholdId");
        ColumnValue colMemberId = collectedValues.get("collectedMemberId");

        colHouseholdId.setValue(this.household.collectedId);
        colMemberId.setValue(this.mother.collectedId);
    }

    @Override
    public void onFormFinished(HForm form, CollectedDataMap collectedValues, XmlFormResult result) {

        Log.d("resultxml", result.getXmlResult());

        if (currentMode == Mode.EDIT) {
            onModeEdit(collectedValues, result);
        } else if (currentMode == Mode.CREATE) {
            onModeCreate(collectedValues, result);
        }

    }

    private void onModeCreate(CollectedDataMap collectedValues, XmlFormResult result) {
        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colCode = collectedValues.get("code");
        ColumnValue colMotherCode = collectedValues.get("motherCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colMotherName = collectedValues.get("motherName"); //not blank
        ColumnValue colRecordedDate = collectedValues.get("recordedDate"); //not null cannot be in the future nor before dob
        ColumnValue colStatus = collectedValues.get("status");
        ColumnValue colEddKnown = collectedValues.get("eddKnown");
        ColumnValue colHasPrenatalRecord = collectedValues.get("hasPrenatalRecord");
        ColumnValue colEddDate = collectedValues.get("eddDate"); //not null, cannot be before dob+12
        ColumnValue colEddType = collectedValues.get("eddType");
        ColumnValue colPregMonths = collectedValues.get("pregMonths");
        ColumnValue colLmpKnown = collectedValues.get("lmpKnown");
        ColumnValue colLmpDate = collectedValues.get("lmpDate");
        ColumnValue colExpectedDelDate = collectedValues.get("expectedDeliveryDate");
        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String code = colCode.getValue();
        String motherCode = colMotherCode.getValue();
        String motherName = colMotherName.getValue();
        Date recordedDate = colRecordedDate.getDateValue();
        PregnancyStatus status = PregnancyStatus.getFrom(colStatus.getValue());
        Boolean hasPrenatalRecord = StringUtil.toBoolean(colHasPrenatalRecord.getValue());
        Boolean eddKnown = StringUtil.toBoolean(colEddKnown.getValue());
        Date eddDate = colEddDate.getDateValue();
        EstimatedDateOfDeliveryType eddType = EstimatedDateOfDeliveryType.getFrom(colEddType.getValue());
        Integer pregMonths = colPregMonths.getIntegerValue();
        Boolean lmpKnown = StringUtil.toBoolean(colEddKnown.getValue());
        Date lmpDate = colLmpDate.getDateValue();
        Date expectedDeliveryDate = colExpectedDelDate.getDateValue();

        //create pregnancy registration


        //PregnancyRegistration
        PregnancyRegistration pregnancy = new PregnancyRegistration();
        pregnancy.visitCode = visitCode;
        pregnancy.code = code;
        pregnancy.motherCode = mother.code;
        pregnancy.recordedDate = recordedDate;
        pregnancy.pregMonths = pregMonths;
        pregnancy.eddKnown = eddKnown;
        pregnancy.hasPrenatalRecord = hasPrenatalRecord;
        pregnancy.eddDate = eddDate;
        pregnancy.eddType = eddType;
        pregnancy.lmpKnown = lmpKnown;
        pregnancy.lmpDate = lmpDate;
        pregnancy.expectedDeliveryDate = expectedDeliveryDate;
        pregnancy.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        pregnancy.recentlyCreated = true;
        pregnancy.recentlyCreatedUri = result.getFilename();
        this.boxPregnancyRegistrations.put(pregnancy);

        //save core collected data
        collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.PREGNANCY_REGISTRATION;
        collectedData.formEntityId = pregnancy.id;
        collectedData.formEntityCode = mother.code;
        collectedData.formEntityName = mother.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        collectedData.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        collectedData.extension.setTarget(this.getFormExtension(collectedData.formEntity));
        this.boxCoreCollectedData.put(collectedData);

        this.entity = pregnancy;
        this.collectExtensionForm(collectedValues);
    }

    private void onModeEdit(CollectedDataMap collectedValues, XmlFormResult result) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colCode = collectedValues.get("code");
        ColumnValue colMotherCode = collectedValues.get("motherCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colMotherName = collectedValues.get("motherName"); //not blank
        ColumnValue colRecordedDate = collectedValues.get("recordedDate"); //not null cannot be in the future nor before dob
        ColumnValue colStatus = collectedValues.get("status");
        ColumnValue colEddKnown = collectedValues.get("eddKnown");
        ColumnValue colHasPrenatalRecord = collectedValues.get("hasPrenatalRecord");
        ColumnValue colEddDate = collectedValues.get("eddDate"); //not null, cannot be before dob+12
        ColumnValue colEddType = collectedValues.get("eddType");
        ColumnValue colPregMonths = collectedValues.get("pregMonths");
        ColumnValue colLmpKnown = collectedValues.get("lmpKnown");
        ColumnValue colLmpDate = collectedValues.get("lmpDate");
        ColumnValue colExpectedDelDate = collectedValues.get("expectedDeliveryDate");
        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String code = colCode.getValue();
        String motherCode = colMotherCode.getValue();
        String motherName = colMotherName.getValue();
        Date recordedDate = colRecordedDate.getDateValue();
        PregnancyStatus status = PregnancyStatus.getFrom(colStatus.getValue());
        Boolean hasPrenatalRecord = StringUtil.toBoolean(colHasPrenatalRecord.getValue());
        Boolean eddKnown = StringUtil.toBoolean(colEddKnown.getValue());
        Date eddDate = colEddDate.getDateValue();
        EstimatedDateOfDeliveryType eddType = EstimatedDateOfDeliveryType.getFrom(colEddType.getValue());
        Integer pregMonths = colPregMonths.getIntegerValue();
        Boolean lmpKnown = StringUtil.toBoolean(colEddKnown.getValue());
        Date lmpDate = colLmpDate.getDateValue();
        Date expectedDeliveryDate = colExpectedDelDate.getDateValue();

        //create pregnancy registration
        //PregnancyRegistration
        PregnancyRegistration pregnancy = this.entity;
        pregnancy.visitCode = visitCode;
        pregnancy.code = code;
        pregnancy.motherCode = mother.code;
        pregnancy.recordedDate = recordedDate;
        pregnancy.pregMonths = pregMonths;
        pregnancy.eddKnown = eddKnown;
        pregnancy.hasPrenatalRecord = hasPrenatalRecord;
        pregnancy.eddDate = eddDate;
        pregnancy.eddType = eddType;
        pregnancy.lmpKnown = lmpKnown;
        pregnancy.lmpDate = lmpDate;
        pregnancy.expectedDeliveryDate = expectedDeliveryDate;
        //pregnancy.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        //pregnancy.recentlyCreated = true;
        //pregnancy.recentlyCreatedUri = result.getFilename();
        this.boxPregnancyRegistrations.put(pregnancy);

        //save core collected data
        collectedData.visitId = visit.id;
        collectedData.formEntityCode = mother.code;
        collectedData.formEntityName = mother.name;
        collectedData.updatedDate = new Date();
        this.boxCoreCollectedData.put(collectedData);

        onFinishedExtensionCollection();
    }

    @Override
    protected void onFinishedExtensionCollection() {
        if (listener != null) {
            if (currentMode == Mode.CREATE) {
                listener.onNewEntityCreated(this.entity, new HashMap<>());
            } else if (currentMode == Mode.EDIT) {
                listener.onEntityEdited(this.entity, new HashMap<>());
            }
        }
    }

    @Override
    public void onFormCancelled(){
        if (listener != null) {
            listener.onFormCancelled();
        }
    }

    @Override
    public String onFormCallMethod(String methodExpression, String[] args) {
        return handleMethodExecution(methodExpression, args);
    }

    @Override
    public void collect() {

        //Limit by Age
        if (GeneralUtil.getAge(this.mother.dob, new Date()) < this.minimunMotherAge) {
            DialogFactory.createMessageInfo(this.context, R.string.core_entity_pregnancy_reg_lbl, R.string.pregnancy_registration_mother_age_err_lbl).show();
            return;
        }

        executeCollectForm();
    }

    private PregnancyRegistration getLastPregnancyRegistration(Member motherMember){
        //def pregnancies = PregnancyRegistration.executeQuery("select p from PregnancyRegistration p where p.mother.code=? order by p.recordedDate desc", [motherCode], [offset:0, max:1])
        PregnancyRegistration pregnancyRegistration = this.boxPregnancyRegistrations.query(PregnancyRegistration_.motherCode.equal(motherMember.code))
                                                                                    .orderDesc(PregnancyRegistration_.recordedDate).build().findFirst();

        return pregnancyRegistration;
    }

    private int retrieveMinimumMotherAge() {
        ApplicationParam param = this.boxAppParams.query().equal(ApplicationParam_.name, ApplicationParam.PARAMS_MIN_AGE_OF_MOTHER, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (param != null) {
            try {
                return Integer.parseInt(param.value);
            } catch (Exception ex) {

            }
        }

        return 12;
    }

    String handleMethodExecution(String methodExpression, String[] args) {
        Log.d("methodcall", ""+methodExpression);

        if (methodExpression.startsWith("calculateEdd")){
            //calculateEdd('2022-09-28', '2021-11-29','2')
            //calculateEdd('2022-09-28',\\s+'2021-11-29',\\s+'2')

            Date recordedDate = StringUtil.toDateYMD(args[0]);
            Date eddDate = StringUtil.toDateYMD(args[1]);
            Date lmpDate = StringUtil.toDateYMD(args[2]);
            Integer pregMonths = StringUtil.toInteger(args[3]);

            Log.d("args", args[0]+","+args[1]+","+args[2]+","+args[3]);

            String result = "'CALC ERROR!!!'";

            if (eddDate != null) {
                result = StringUtil.formatYMD(eddDate);
            } else {
                if (lmpDate != null) {
                    //lmpDate+280days
                    eddDate = GeneralUtil.getDateAdd(lmpDate, 280);
                    result = StringUtil.formatYMD(eddDate);
                } else {
                    int pdays = pregMonths*4*7;
                    Date pdate = GeneralUtil.getDateAdd(recordedDate, -1*pdays);
                    eddDate = GeneralUtil.getDateAdd(pdate, 280);
                    result = StringUtil.formatYMD(eddDate);
                }
            }

            return "'"+result+"'";

        }

        return "'CALC ERROR!!!'";
    }


}
