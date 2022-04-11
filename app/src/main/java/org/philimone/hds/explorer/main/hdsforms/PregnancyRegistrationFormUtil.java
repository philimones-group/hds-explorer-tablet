package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.FragmentManager;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.PregnancyRegistration;
import org.philimone.hds.explorer.model.PregnancyRegistration_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.EstimatedDateOfDeliveryType;
import org.philimone.hds.explorer.model.enums.PregnancyStatus;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import java.util.Date;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

public class PregnancyRegistrationFormUtil extends FormUtil<PregnancyRegistration> {

    private Box<Member> boxMembers;
    private Box<PregnancyRegistration> boxPregnancyRegistrations;
    private Box<CoreCollectedData> boxCoreCollectedData;

    private Household household;
    private Visit visit;
    private Member mother;
    private boolean nonPregnantRegistration = false;
    private PregnancyStatus loadedPregnancyStatus;

    public PregnancyRegistrationFormUtil(FragmentManager fragmentManager, Context context, Visit visit, Household household, Member member, FormUtilListener<PregnancyRegistration> listener){
        super(fragmentManager, context, FormUtil.getPregnancyRegistrationForm(context), listener);

        //Log.d("enu-household", ""+household);

        this.household = household;
        this.mother = member;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    public PregnancyRegistrationFormUtil(FragmentManager fragmentManager, Context context, Visit visit, Household household, Member member, PregnancyStatus pregnancyStatus, FormUtilListener<PregnancyRegistration> listener){
        super(fragmentManager, context, FormUtil.getPregnancyRegistrationForm(context), listener);

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

    public PregnancyRegistrationFormUtil(FragmentManager fragmentManager, Context context, Visit visit, Household household, PregnancyRegistration pregToEdit, FormUtilListener<PregnancyRegistration> listener){
        super(fragmentManager, context, FormUtil.getPregnancyRegistrationForm(context), pregToEdit, listener);

        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxPregnancyRegistrations = ObjectBoxDatabase.get().boxFor(PregnancyRegistration.class);
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
    }

    @Override
    protected void initialize(){
        super.initialize();
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
        if (boxPregnancyRegistrations.query().equal(PregnancyRegistration_.code, code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().count() > 0){
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

        PregnancyRegistration pregReg = getLastPregnancyRegistration(this.mother);
        if (pregReg != null && pregReg.status == PregnancyStatus.PREGNANT){
            String message = this.context.getString(R.string.pregnancy_registration_previous_pending_lbl);
            return new ValidationResult(colCode, message);
        }


        return ValidationResult.noErrors();
    }

    @Override
    public void onFormFinished(HForm form, CollectedDataMap collectedValues, XmlFormResult result) {

        Log.d("resultxml", result.getXmlResult());

        if (currentMode == Mode.EDIT) {
            System.out.println("Editing PregnancyRegistration Not implemented yet");
            assert 1==0;
        }


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
        pregnancy.recentlyCreated = true;
        pregnancy.recentlyCreatedUri = result.getFilename();
        this.boxPregnancyRegistrations.put(pregnancy);

        //save core collected data
        CoreCollectedData collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.PREGNANCY_REGISTRATION;
        collectedData.formEntityId = mother.id;
        collectedData.formEntityCode = mother.code;
        collectedData.formEntityName = mother.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        this.boxCoreCollectedData.put(collectedData);

        if (listener != null) {
            listener.onNewEntityCreated(pregnancy);
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
        executeCollectForm();
    }

    private PregnancyRegistration getLastPregnancyRegistration(Member motherMember){
        //def pregnancies = PregnancyRegistration.executeQuery("select p from PregnancyRegistration p where p.mother.code=? order by p.recordedDate desc", [motherCode], [offset:0, max:1])
        PregnancyRegistration pregnancyRegistration = this.boxPregnancyRegistrations.query(PregnancyRegistration_.motherCode.equal(motherMember.code))
                                                                                    .orderDesc(PregnancyRegistration_.recordedDate).build().findFirst();

        return pregnancyRegistration;
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
