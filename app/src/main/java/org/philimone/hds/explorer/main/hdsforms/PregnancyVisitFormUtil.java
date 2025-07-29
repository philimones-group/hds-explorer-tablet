package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.PregnancyChild;
import org.philimone.hds.explorer.model.PregnancyChild_;
import org.philimone.hds.explorer.model.PregnancyOutcome;
import org.philimone.hds.explorer.model.PregnancyOutcome_;
import org.philimone.hds.explorer.model.PregnancyRegistration;
import org.philimone.hds.explorer.model.PregnancyRegistration_;
import org.philimone.hds.explorer.model.PregnancyVisit;
import org.philimone.hds.explorer.model.PregnancyVisitChild;
import org.philimone.hds.explorer.model.PregnancyVisitChild_;
import org.philimone.hds.explorer.model.PregnancyVisit_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.converters.IllnessSymptomsCollectionConverter;
import org.philimone.hds.explorer.model.converters.IllnessSymptomsConverter;
import org.philimone.hds.explorer.model.enums.BirthPlace;
import org.philimone.hds.explorer.model.enums.BreastFeedingStatus;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.EstimatedDateOfDeliveryType;
import org.philimone.hds.explorer.model.enums.Gender;
import org.philimone.hds.explorer.model.enums.HeadRelationshipType;
import org.philimone.hds.explorer.model.enums.HealthcareProviderType;
import org.philimone.hds.explorer.model.enums.IllnessSymptoms;
import org.philimone.hds.explorer.model.enums.ImmunizationStatus;
import org.philimone.hds.explorer.model.enums.NewBornStatus;
import org.philimone.hds.explorer.model.enums.PregnancyOutcomeType;
import org.philimone.hds.explorer.model.enums.PregnancyStatus;
import org.philimone.hds.explorer.model.enums.PregnancyVisitType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.RepeatColumnValue;
import org.philimone.hds.forms.model.RepeatObject;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

public class PregnancyVisitFormUtil extends FormUtil<PregnancyVisit> {

    static int MAX_ANTEPARTUM_VISITS = 4; //default
    static int MAX_POSTPARTUM_VISITS = 4; //default

    private Box<Member> boxMembers;
    private Box<PregnancyRegistration> boxPregnancyRegistrations;
    private Box<PregnancyOutcome> boxPregnancyOutcomes;
    private Box<PregnancyChild> boxPregnancyChilds;
    private Box<PregnancyVisit> boxPregnancyVisits;
    private Box<PregnancyVisitChild> boxPregnancyVisitChilds;

    //private Household household;
    private Visit visit;
    private Member mother;
    private PregnancyRegistration pregnancyRegistration;
    private PregnancyOutcome pregnancyOutcome;
    private boolean isPossibleToCollectVisit;
    private PregnancyVisitType currentVisitType;
    private Integer currentVisitNumber;
    private PregnancyStatus currentPregnancyStatus;

    public PregnancyVisitFormUtil(Fragment fragment, Context context, Visit visit, Household household, Member member, FormUtilities odkFormUtilities, FormUtilListener<PregnancyVisit> listener){
        super(fragment, context, FormUtil.getPregnancyVisitForm(context), odkFormUtilities, listener);

        //Log.d("enu-household", ""+household);

        this.household = household;
        this.mother = member;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    public PregnancyVisitFormUtil(Fragment fragment, Context context, Visit visit, Household household, PregnancyVisit pregToEdit, FormUtilities odkFormUtilities, FormUtilListener<PregnancyVisit> listener){
        super(fragment, context, FormUtil.getPregnancyVisitForm(context), pregToEdit, odkFormUtilities, listener);

        this.household = household;
        this.visit = visit;
        this.mother = this.boxMembers.query(Member_.code.equal(pregToEdit.motherCode)).build().findFirst();

        initBoxes();
        initialize();
    }

    public static PregnancyVisitFormUtil newInstance(Mode openMode, Fragment fragment, Context context, Visit visit, Household household, Member member, PregnancyVisit pregToEdit, FormUtilities odkFormUtilities, FormUtilListener<PregnancyVisit> listener){
        if (openMode == Mode.CREATE) {
            return new PregnancyVisitFormUtil(fragment, context, visit, household, member, odkFormUtilities, listener);
        } else if (openMode == Mode.EDIT) {
            return new PregnancyVisitFormUtil(fragment, context, visit, household, pregToEdit, odkFormUtilities, listener);
        }

        return null;
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxPregnancyRegistrations = ObjectBoxDatabase.get().boxFor(PregnancyRegistration.class);
        this.boxPregnancyOutcomes = ObjectBoxDatabase.get().boxFor(PregnancyOutcome.class);
        this.boxPregnancyChilds = ObjectBoxDatabase.get().boxFor(PregnancyChild.class);
        this.boxPregnancyVisits = ObjectBoxDatabase.get().boxFor(PregnancyVisit.class);
        this.boxPregnancyVisitChilds = ObjectBoxDatabase.get().boxFor(PregnancyVisitChild.class);
    }

    @Override
    protected void initialize(){
        super.initialize();

        //get a pregnancy registration and also a
        this.pregnancyRegistration = this.boxPregnancyRegistrations.query(PregnancyRegistration_.motherCode.equal(mother.code)).orderDesc(PregnancyRegistration_.code).build().findFirst();

        if (this.pregnancyRegistration != null) {
            this.pregnancyOutcome = this.boxPregnancyOutcomes.query(PregnancyOutcome_.code.equal(this.pregnancyRegistration.code)).build().findFirst();
        }

        if (this.pregnancyOutcome != null) {

        }

        this.isPossibleToCollectVisit = canCollectPregnancyVisit();

        if (this.isPossibleToCollectVisit) {
            boolean pregnant = this.pregnancyRegistration.status == PregnancyStatus.PREGNANT;
            boolean noOutcome = this.pregnancyOutcome == null;
            this.currentVisitType = (pregnant && noOutcome) ? PregnancyVisitType.ANTEPARTUM : PregnancyVisitType.POSTPARTUM;
            this.currentVisitNumber = (int) (this.boxPregnancyVisits.query(PregnancyVisit_.code.equal(pregnancyRegistration.code)).build().count() + 1);
            this.currentPregnancyStatus = this.currentVisitType==PregnancyVisitType.ANTEPARTUM ? PregnancyStatus.PREGNANT : PregnancyStatus.DELIVERED;
        }

        retrieveParameters();
    }

    private int retrieveParameters() {
        ApplicationParam paramAntepartum = this.boxAppParams.query().equal(ApplicationParam_.name, ApplicationParam.PARAMS_MAX_ANTEPARTUM_VISITS, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
        ApplicationParam paramPostpartum = this.boxAppParams.query().equal(ApplicationParam_.name, ApplicationParam.PARAMS_MAX_POSTPARTUM_VISITS, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (paramAntepartum != null) {
            try {
                MAX_ANTEPARTUM_VISITS = Integer.parseInt(paramAntepartum.value);
            } catch (Exception ex) { }
        }

        if (paramPostpartum != null) {
            try {
                MAX_POSTPARTUM_VISITS = Integer.parseInt(paramPostpartum.value);
            } catch (Exception ex) { }
        }

        return 12;
    }

    @Override
    protected void preloadValues() {
        //member_details_unknown_lbl

        preloadedMap.put("visitCode", this.visit.code);
        preloadedMap.put("code", this.pregnancyRegistration.code);
        preloadedMap.put("motherCode", this.mother.code);
        preloadedMap.put("motherName", this.mother.name);
        preloadedMap.put("visitType", this.currentVisitType.code+"");
        preloadedMap.put("visitNumber", this.currentVisitNumber+"");
        preloadedMap.put("status", this.currentPregnancyStatus.code);

        if (this.pregnancyOutcome != null) {

            preloadedMap.put("numberOfChilds", this.pregnancyOutcome.childs.size()+"");

            RepeatObject childsRepObj = new RepeatObject();
            for (PregnancyChild child : this.pregnancyOutcome.childs) {
                Member memberChild = Queries.getMemberByCode(boxMembers, child.childCode);
                Log.d("outcome", "" + child.childCode);

                Map<String, String> obj = childsRepObj.createNewObject();
                obj.put("outcomeType", child.outcomeType.code);
                obj.put("childCode", child.childCode);
                obj.put("childName", memberChild.name);
                obj.put("childGender", memberChild.gender.code);

                if (child.outcomeType != PregnancyOutcomeType.LIVEBIRTH) {
                    obj.put("childStatus", child.outcomeType.code);
                }
            }

            Log.d("childs", childsRepObj.getList().size() + "");
            preloadedMap.put("childs", childsRepObj);
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
        ColumnValue colVisitType = collectedValues.get("visitType");
        ColumnValue colMotherCode = collectedValues.get("motherCode");
        ColumnValue colMotherName = collectedValues.get("motherName");
        ColumnValue colVisitNumber = collectedValues.get("visitNumber");
        ColumnValue colVisitDate = collectedValues.get("visitDate");
        ColumnValue colStatus = collectedValues.get("status");

        ColumnValue colWeeksGestation = collectedValues.get("weeksGestation");
        ColumnValue colPrenatalCareReceived = collectedValues.get("prenatalCareReceived");
        ColumnValue colPrenatalCareProvider = collectedValues.get("prenatalCareProvider");
        ColumnValue colComplicationsReported = collectedValues.get("complicationsReported");
        ColumnValue colComplicationDetails = collectedValues.get("complicationDetails");
        ColumnValue colHasBirthPlan = collectedValues.get("hasBirthPlan");
        ColumnValue colExpectedBirthPlace = collectedValues.get("expectedBirthPlace");
        ColumnValue colBirthPlaceOther = collectedValues.get("birthPlaceOther");
        ColumnValue colTransportationPlan = collectedValues.get("transportationPlan");
        ColumnValue colFinancialPreparedness = collectedValues.get("financialPreparedness");

        ColumnValue colPostpartumComplications = collectedValues.get("postpartumComplications");
        ColumnValue colPostpartumComplicationDetails = collectedValues.get("postpartumComplicationDetails");
        ColumnValue colBreastfeedingStatus = collectedValues.get("breastfeedingStatus");
        ColumnValue colResumedDailyActivities = collectedValues.get("resumedDailyActivities");
        ColumnValue colAttendedPostpartumCheckup = collectedValues.get("attendedPostpartumCheckup");

        ColumnValue colNumberOfChilds = collectedValues.get("numberOfChilds");

        RepeatColumnValue repChilds = collectedValues.getRepeatColumn("childs");
        List<ColumnValue> colOutcomeTypes = new ArrayList<>();
        List<ColumnValue> colChildCodes = new ArrayList<>();
        List<ColumnValue> colChildNames = new ArrayList<>();
        List<ColumnValue> colChildGenders = new ArrayList<>();
        List<ColumnValue> colChildStatuses = new ArrayList<>();
        List<ColumnValue> colChildWeights = new ArrayList<>();
        List<ColumnValue> colChildIllnessSymptoms = new ArrayList<>();
        List<ColumnValue> colChildBreastfeedingStatuses = new ArrayList<>();
        List<ColumnValue> colChildImmunizationStatuses = new ArrayList<>();
        List<ColumnValue> colNotes = new ArrayList<>();

        if (repChilds != null) {
            for (int i = 0; i < repChilds.getCount(); i++) {
                colOutcomeTypes.add(repChilds.get("outcomeType", i));
                colChildCodes.add(repChilds.get("childCode", i));
                colChildNames.add(repChilds.get("childName", i));
                colChildGenders.add(repChilds.get("childGender", i));
                colChildStatuses.add(repChilds.get("childStatus", i));
                colChildWeights.add(repChilds.get("childWeight", i));
                colChildIllnessSymptoms.add(repChilds.get("childIllnessSymptoms", i));
                colChildBreastfeedingStatuses.add(repChilds.get("childBreastfeedingStatus", i));
                colChildImmunizationStatuses.add(repChilds.get("childImmunizationStatus", i));
                colNotes.add(repChilds.get("notes", i));
            }
        }

        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        ColumnValue colModules = collectedValues.get("modules");

        // --- Basic Visit Info ---
        String visitCode = colVisitCode.getValue();
        String code = colCode.getValue();
        String motherCode = colMotherCode.getValue();
        String motherName = colMotherName.getValue();
        String visitTypeStr = colVisitType.getValue();
        PregnancyVisitType visitType = PregnancyVisitType.getFrom(visitTypeStr);
        Integer visitNumber = colVisitNumber.getIntegerValue();
        Date visitDate = colVisitDate.getDateValue();
        PregnancyStatus status = PregnancyStatus.getFrom(colStatus.getValue());
        // --- Antepartum Fields ---
        Integer weeksGestation = colWeeksGestation.getIntegerValue();
        Boolean prenatalCareReceived = StringUtil.toBoolean(colPrenatalCareReceived.getValue()) ;
        HealthcareProviderType prenatalCareProvider = HealthcareProviderType.getFrom(colPrenatalCareProvider.getValue());
        Boolean complicationsReported = StringUtil.toBoolean(colComplicationsReported.getValue());
        String complicationDetails = colComplicationDetails.getValue();
        Boolean hasBirthPlan = StringUtil.toBoolean(colHasBirthPlan.getValue());
        BirthPlace expectedBirthPlace = BirthPlace.getFrom(colExpectedBirthPlace.getValue());
        String birthPlaceOther = colBirthPlaceOther.getValue();
        Boolean transportationPlan = StringUtil.toBoolean(colTransportationPlan.getValue());
        Boolean financialPreparedness = StringUtil.toBoolean(colFinancialPreparedness.getValue());
        // --- Postpartum Fields ---
        Boolean postpartumComplications = StringUtil.toBoolean(colPostpartumComplications.getValue());
        String postpartumComplicationDetails = colPostpartumComplicationDetails.getValue();
        BreastFeedingStatus breastfeedingStatus = BreastFeedingStatus.getFrom(colBreastfeedingStatus.getValue());
        Boolean resumedDailyActivities = StringUtil.toBoolean(colResumedDailyActivities.getValue());
        Boolean attendedPostpartumCheckup = StringUtil.toBoolean(colAttendedPostpartumCheckup.getValue());
        // --- Children Info ---
        Integer numberOfChilds = colNumberOfChilds.getIntegerValue();

        //validations
        //memberCode blank/valid
        boolean pregnancyExists = boxPregnancyRegistrations.query().equal(PregnancyRegistration_.code, code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().count() > 0;
        if (!pregnancyExists){
            String message = this.context.getString(R.string.pregnancy_visit_no_pregnancy_registration_err_lbl);
            return new ValidationResult(colMotherCode, message);
        }

        //visit is duplicate
        boolean visitDuplicated = boxPregnancyVisits.query(PregnancyVisit_.code.equal(code).and(PregnancyVisit_.visitNumber.equal(visitNumber))).build().count() > 0;
        if (currentMode == Mode.CREATE && visitDuplicated){
            String message = this.context.getString(R.string.pregnancy_visit_duplicated_visit_number_err_lbl);
            return new ValidationResult(colCode, message);
        }

        if (StringUtil.isBlank(motherCode)){
            String message = this.context.getString(R.string.pregnancy_registration_mothercode_empty_lbl);
            return new ValidationResult(colMotherCode, message);
        }

        if (visitDate == null) {
            String message = "";
            return new ValidationResult(colVisitDate, message);
        }

        //recordedDate cannot be in the future
        if (visitDate.after(new Date())){ //is in the future
            String message = this.context.getString(R.string.pregnancy_registration_recordeddate_not_great_today_lbl);
            return new ValidationResult(colVisitDate, message);
        }

        //validate preg status
        if (status == null){
            String message = this.context.getString(R.string.pregnancy_registration_status_empty_lbl);
            return new ValidationResult(colStatus, message);
        }

        //visitDate cannot be before dob
        if (visitDate.before(this.mother.dob)){ //is before dob
            String message = this.context.getString(R.string.pregnancy_visit_visitdate_not_before_dob_lbl);
            return new ValidationResult(colVisitDate, message);
        }

        //visitDate cannot be before the outcomeDate
        if (status == PregnancyStatus.DELIVERED && pregnancyOutcome != null && visitDate.before(pregnancyOutcome.outcomeDate)) {
            String message = this.context.getString(R.string.pregnancy_visit_visitdate_not_before_outcome_date_lbl);
            return new ValidationResult(colVisitDate, message);
        }

        //visitDate cannot be before the last pregnancy visit date
        PregnancyVisit lastVisit = boxPregnancyVisits.query(PregnancyVisit_.code.equal(code)).orderDesc(PregnancyVisit_.visitDate).build().findFirst();
        lastVisit = this.entity == null ? lastVisit : boxPregnancyVisits.query(PregnancyVisit_.code.equal(code).and(PregnancyVisit_.id.notEqual(this.entity.id))).orderDesc(PregnancyVisit_.visitDate).build().findFirst();
        if (lastVisit != null && (visitDate.before(lastVisit.visitDate) || visitDate.equals(lastVisit.visitDate))) {
            String message = this.context.getString(R.string.pregnancy_visit_visitdate_not_before_last_visit_date_lbl);
            return new ValidationResult(colVisitDate, message);
        }

        if (status == PregnancyStatus.DELIVERED && pregnancyOutcome != null && pregnancyOutcome.childs.isEmpty()) {
            String message = this.context.getString(R.string.pregnancy_visit_status_postpartum_no_outcomes_err_lbl);
            return new ValidationResult(colStatus, message);
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
        ColumnValue colVisitType = collectedValues.get("visitType");
        ColumnValue colMotherCode = collectedValues.get("motherCode");
        ColumnValue colMotherName = collectedValues.get("motherName");
        ColumnValue colVisitNumber = collectedValues.get("visitNumber");
        ColumnValue colVisitDate = collectedValues.get("visitDate");
        ColumnValue colStatus = collectedValues.get("status");

        ColumnValue colWeeksGestation = collectedValues.get("weeksGestation");
        ColumnValue colPrenatalCareReceived = collectedValues.get("prenatalCareReceived");
        ColumnValue colPrenatalCareProvider = collectedValues.get("prenatalCareProvider");
        ColumnValue colComplicationsReported = collectedValues.get("complicationsReported");
        ColumnValue colComplicationDetails = collectedValues.get("complicationDetails");
        ColumnValue colHasBirthPlan = collectedValues.get("hasBirthPlan");
        ColumnValue colExpectedBirthPlace = collectedValues.get("expectedBirthPlace");
        ColumnValue colBirthPlaceOther = collectedValues.get("birthPlaceOther");
        ColumnValue colTransportationPlan = collectedValues.get("transportationPlan");
        ColumnValue colFinancialPreparedness = collectedValues.get("financialPreparedness");

        ColumnValue colPostpartumComplications = collectedValues.get("postpartumComplications");
        ColumnValue colPostpartumComplicationDetails = collectedValues.get("postpartumComplicationDetails");
        ColumnValue colBreastfeedingStatus = collectedValues.get("breastfeedingStatus");
        ColumnValue colResumedDailyActivities = collectedValues.get("resumedDailyActivities");
        ColumnValue colAttendedPostpartumCheckup = collectedValues.get("attendedPostpartumCheckup");

        ColumnValue colNumberOfChilds = collectedValues.get("numberOfChilds");

        RepeatColumnValue repChilds = collectedValues.getRepeatColumn("childs");
        List<ColumnValue> colOutcomeTypes = new ArrayList<>();
        List<ColumnValue> colChildCodes = new ArrayList<>();
        List<ColumnValue> colChildNames = new ArrayList<>();
        List<ColumnValue> colChildGenders = new ArrayList<>();
        List<ColumnValue> colChildStatuses = new ArrayList<>();
        List<ColumnValue> colChildWeights = new ArrayList<>();
        List<ColumnValue> colChildIllnessSymptoms = new ArrayList<>();
        List<ColumnValue> colChildBreastfeedingStatuses = new ArrayList<>();
        List<ColumnValue> colChildImmunizationStatuses = new ArrayList<>();
        List<ColumnValue> colNotes = new ArrayList<>();

        if (repChilds != null) {
            for (int i = 0; i < repChilds.getCount(); i++) {
                colOutcomeTypes.add(repChilds.get("outcomeType", i));
                colChildCodes.add(repChilds.get("childCode", i));
                colChildNames.add(repChilds.get("childName", i));
                colChildGenders.add(repChilds.get("childGender", i));
                colChildStatuses.add(repChilds.get("childStatus", i));
                colChildWeights.add(repChilds.get("childWeight", i));
                colChildIllnessSymptoms.add(repChilds.get("childIllnessSymptoms", i));
                colChildBreastfeedingStatuses.add(repChilds.get("childBreastfeedingStatus", i));
                colChildImmunizationStatuses.add(repChilds.get("childImmunizationStatus", i));
                colNotes.add(repChilds.get("notes", i));
            }
        }

        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        ColumnValue colModules = collectedValues.get("modules");

        // --- Basic Visit Info ---
        String visitCode = colVisitCode.getValue();
        String code = colCode.getValue();
        String motherCode = colMotherCode.getValue();
        String motherName = colMotherName.getValue();
        String visitTypeStr = colVisitType.getValue();
        PregnancyVisitType visitType = PregnancyVisitType.getFrom(visitTypeStr);
        Integer visitNumber = colVisitNumber.getIntegerValue();
        Date visitDate = colVisitDate.getDateValue();
        PregnancyStatus status = PregnancyStatus.getFrom(colStatus.getValue());
        // --- Antepartum Fields ---
        Integer weeksGestation = colWeeksGestation.getIntegerValue();
        Boolean prenatalCareReceived = StringUtil.toBoolean(colPrenatalCareReceived.getValue()) ;
        HealthcareProviderType prenatalCareProvider = HealthcareProviderType.getFrom(colPrenatalCareProvider.getValue());
        Boolean complicationsReported = StringUtil.toBoolean(colComplicationsReported.getValue());
        String complicationDetails = colComplicationDetails.getValue();
        Boolean hasBirthPlan = StringUtil.toBoolean(colHasBirthPlan.getValue());
        BirthPlace expectedBirthPlace = BirthPlace.getFrom(colExpectedBirthPlace.getValue());
        String birthPlaceOther = colBirthPlaceOther.getValue();
        Boolean transportationPlan = StringUtil.toBoolean(colTransportationPlan.getValue());
        Boolean financialPreparedness = StringUtil.toBoolean(colFinancialPreparedness.getValue());
        // --- Postpartum Fields ---
        Boolean postpartumComplications = StringUtil.toBoolean(colPostpartumComplications.getValue());
        String postpartumComplicationDetails = colPostpartumComplicationDetails.getValue();
        BreastFeedingStatus breastfeedingStatus = BreastFeedingStatus.getFrom(colBreastfeedingStatus.getValue());
        Boolean resumedDailyActivities = StringUtil.toBoolean(colResumedDailyActivities.getValue());
        Boolean attendedPostpartumCheckup = StringUtil.toBoolean(colAttendedPostpartumCheckup.getValue());
        // --- Children Info ---
        Integer numberOfChilds = colNumberOfChilds.getIntegerValue();

        String affectedMembers = null;


        PregnancyVisit pregnancyVisit = new PregnancyVisit();
        // Basic Identifiers
        pregnancyVisit.code = pregnancyRegistration.code;
        pregnancyVisit.visitCode = visitCode;
        pregnancyVisit.motherCode = mother.code;
        // Pregnancy Status and Visit Info
        pregnancyVisit.status = status;
        pregnancyVisit.visitType = visitType;
        pregnancyVisit.visitNumber = visitNumber;
        pregnancyVisit.visitDate = visitDate;
        // Antepartum Details
        pregnancyVisit.weeksGestation = weeksGestation;
        pregnancyVisit.prenatalCareReceived = prenatalCareReceived;
        pregnancyVisit.prenatalCareProvider = prenatalCareProvider;
        pregnancyVisit.complicationsReported = complicationsReported;
        pregnancyVisit.complicationDetails = complicationDetails;
        pregnancyVisit.hasBirthPlan = hasBirthPlan;
        pregnancyVisit.expectedBirthPlace = expectedBirthPlace;
        pregnancyVisit.birthPlaceOther = birthPlaceOther;
        pregnancyVisit.transportationPlan = transportationPlan;
        pregnancyVisit.financialPreparedness = financialPreparedness;
        // Postpartum Details
        pregnancyVisit.postpartumComplications = postpartumComplications;
        pregnancyVisit.postpartumComplicationDetails = postpartumComplicationDetails;
        pregnancyVisit.breastfeedingStatus = breastfeedingStatus;
        pregnancyVisit.resumedDailyActivities = resumedDailyActivities;
        pregnancyVisit.attendedPostpartumCheckup = attendedPostpartumCheckup;

        //childs
        if (repChilds != null) {
            for (int i = 0; i < repChilds.getCount(); i++) {
                PregnancyOutcomeType outcomeType = PregnancyOutcomeType.getFrom(colOutcomeTypes.get(i).getValue());
                BigDecimal weightValue = colChildWeights.get(i).getDecimalValue();
                String childCode =  colChildCodes.get(i).getValue();
                String childName = colChildNames.get(i).getValue();
                String childGender = colChildGenders.get(i).getValue(); // Usually "MALE" or "FEMALE"
                NewBornStatus childStatus = NewBornStatus.getFrom(colChildStatuses.get(i).getValue());
                Double childWeight = weightValue==null ? null : weightValue.doubleValue();
                Set<IllnessSymptoms> illnessSymptoms = IllnessSymptomsCollectionConverter.getCollectionFrom(colChildIllnessSymptoms.get(i).getValue()); // multi-select - handled accordingly
                BreastFeedingStatus childBreastfeeding = BreastFeedingStatus.getFrom(colChildBreastfeedingStatuses.get(i).getValue());
                ImmunizationStatus childImmunization = ImmunizationStatus.getFrom(colChildImmunizationStatuses.get(i).getValue());
                String notes = colNotes.get(i).getValue();

                // Build child object
                PregnancyVisitChild child = new PregnancyVisitChild();
                child.pregnancyCode = pregnancyVisit.code;
                child.outcomeType = outcomeType;
                child.childCode = childCode;
                child.childStatus = childStatus;
                child.childWeight = childWeight;
                child.childIllnessSymptoms.clear();
                child.childIllnessSymptoms.addAll(illnessSymptoms);
                child.childBreastfeedingStatus = childBreastfeeding;
                child.childImmunizationStatus = childImmunization;
                child.notes = notes;

                pregnancyVisit.childs.add(child);
            }
        }

        pregnancyVisit.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        pregnancyVisit.recentlyCreated = true;
        pregnancyVisit.recentlyCreatedUri = result.getFilename();

        long result_id = this.boxPregnancyVisits.put(pregnancyVisit);

        //Update Pregnancy Registrations
        boolean first_visit = this.boxPregnancyVisits.query(PregnancyVisit_.code.equal(pregnancyVisit.code)).build().count() == 1;
        int antepartum_count = (int) boxPregnancyVisits.query(PregnancyVisit_.code.equal(code).and(PregnancyVisit_.visitType.equal(PregnancyVisitType.ANTEPARTUM.code))).build().count();
        int postpartum_count = (int) boxPregnancyVisits.query(PregnancyVisit_.code.equal(code).and(PregnancyVisit_.visitType.equal(PregnancyVisitType.POSTPARTUM.code))).build().count();

        pregnancyRegistration = boxPregnancyRegistrations.get(pregnancyRegistration.id);
        pregnancyRegistration.summary_antepartum_count = antepartum_count;
        pregnancyRegistration.summary_postpartum_count = postpartum_count;
        pregnancyRegistration.summary_last_visit_status = pregnancyVisit.status;
        pregnancyRegistration.summary_last_visit_type = pregnancyVisit.visitType;
        pregnancyRegistration.summary_last_visit_date = pregnancyVisit.visitDate;
        pregnancyRegistration.summary_first_visit_date = first_visit ? pregnancyVisit.visitDate : null;
        pregnancyRegistration.summary_has_pregnancy_outcome = boxPregnancyOutcomes.query(PregnancyOutcome_.code.equal(pregnancyVisit.code)).build().count()>0;
        pregnancyRegistration.summary_nr_outcomes = (int) boxPregnancyChilds.query(PregnancyChild_.outcomeCode.equal(pregnancyVisit.code)).build().count();
        pregnancyRegistration.summary_followup_completed = (antepartum_count>=MAX_ANTEPARTUM_VISITS && postpartum_count >= MAX_POSTPARTUM_VISITS) || pregnancyVisit.status == PregnancyStatus.LOST_TRACK;

        this.boxPregnancyRegistrations.put(pregnancyRegistration);

        //save core collected data
        collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.PREGNANCY_VISIT;
        collectedData.formEntityId = result_id;
        collectedData.formEntityCode = mother.code;
        collectedData.formEntityName = mother.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        collectedData.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        collectedData.extension.setTarget(this.getFormExtension(collectedData.formEntity));
        this.boxCoreCollectedData.put(collectedData);

        this.entity = pregnancyVisit;
        this.collectExtensionForm(collectedValues);
    }

    private void onModeEdit(CollectedDataMap collectedValues, XmlFormResult result) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colCode = collectedValues.get("code");
        ColumnValue colVisitType = collectedValues.get("visitType");
        ColumnValue colMotherCode = collectedValues.get("motherCode");
        ColumnValue colMotherName = collectedValues.get("motherName");
        ColumnValue colVisitNumber = collectedValues.get("visitNumber");
        ColumnValue colVisitDate = collectedValues.get("visitDate");
        ColumnValue colStatus = collectedValues.get("status");

        ColumnValue colWeeksGestation = collectedValues.get("weeksGestation");
        ColumnValue colPrenatalCareReceived = collectedValues.get("prenatalCareReceived");
        ColumnValue colPrenatalCareProvider = collectedValues.get("prenatalCareProvider");
        ColumnValue colComplicationsReported = collectedValues.get("complicationsReported");
        ColumnValue colComplicationDetails = collectedValues.get("complicationDetails");
        ColumnValue colHasBirthPlan = collectedValues.get("hasBirthPlan");
        ColumnValue colExpectedBirthPlace = collectedValues.get("expectedBirthPlace");
        ColumnValue colBirthPlaceOther = collectedValues.get("birthPlaceOther");
        ColumnValue colTransportationPlan = collectedValues.get("transportationPlan");
        ColumnValue colFinancialPreparedness = collectedValues.get("financialPreparedness");

        ColumnValue colPostpartumComplications = collectedValues.get("postpartumComplications");
        ColumnValue colPostpartumComplicationDetails = collectedValues.get("postpartumComplicationDetails");
        ColumnValue colBreastfeedingStatus = collectedValues.get("breastfeedingStatus");
        ColumnValue colResumedDailyActivities = collectedValues.get("resumedDailyActivities");
        ColumnValue colAttendedPostpartumCheckup = collectedValues.get("attendedPostpartumCheckup");

        ColumnValue colNumberOfChilds = collectedValues.get("numberOfChilds");

        RepeatColumnValue repChilds = collectedValues.getRepeatColumn("childs");
        List<ColumnValue> colOutcomeTypes = new ArrayList<>();
        List<ColumnValue> colChildCodes = new ArrayList<>();
        List<ColumnValue> colChildNames = new ArrayList<>();
        List<ColumnValue> colChildGenders = new ArrayList<>();
        List<ColumnValue> colChildStatuses = new ArrayList<>();
        List<ColumnValue> colChildWeights = new ArrayList<>();
        List<ColumnValue> colChildIllnessSymptoms = new ArrayList<>();
        List<ColumnValue> colChildBreastfeedingStatuses = new ArrayList<>();
        List<ColumnValue> colChildImmunizationStatuses = new ArrayList<>();
        List<ColumnValue> colNotes = new ArrayList<>();

        if (repChilds != null) {
            for (int i = 0; i < repChilds.getCount(); i++) {
                colOutcomeTypes.add(repChilds.get("outcomeType", i));
                colChildCodes.add(repChilds.get("childCode", i));
                colChildNames.add(repChilds.get("childName", i));
                colChildGenders.add(repChilds.get("childGender", i));
                colChildStatuses.add(repChilds.get("childStatus", i));
                colChildWeights.add(repChilds.get("childWeight", i));
                colChildIllnessSymptoms.add(repChilds.get("childIllnessSymptoms", i));
                colChildBreastfeedingStatuses.add(repChilds.get("childBreastfeedingStatus", i));
                colChildImmunizationStatuses.add(repChilds.get("childImmunizationStatus", i));
                colNotes.add(repChilds.get("notes", i));
            }
        }

        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        ColumnValue colModules = collectedValues.get("modules");

        // --- Basic Visit Info ---
        String visitCode = colVisitCode.getValue();
        String code = colCode.getValue();
        String motherCode = colMotherCode.getValue();
        String motherName = colMotherName.getValue();
        String visitTypeStr = colVisitType.getValue();
        PregnancyVisitType visitType = PregnancyVisitType.getFrom(visitTypeStr);
        Integer visitNumber = colVisitNumber.getIntegerValue();
        Date visitDate = colVisitDate.getDateValue();
        PregnancyStatus status = PregnancyStatus.getFrom(colStatus.getValue());
        // --- Antepartum Fields ---
        Integer weeksGestation = colWeeksGestation.getIntegerValue();
        Boolean prenatalCareReceived = StringUtil.toBoolean(colPrenatalCareReceived.getValue()) ;
        HealthcareProviderType prenatalCareProvider = HealthcareProviderType.getFrom(colPrenatalCareProvider.getValue());
        Boolean complicationsReported = StringUtil.toBoolean(colComplicationsReported.getValue());
        String complicationDetails = colComplicationDetails.getValue();
        Boolean hasBirthPlan = StringUtil.toBoolean(colHasBirthPlan.getValue());
        BirthPlace expectedBirthPlace = BirthPlace.getFrom(colExpectedBirthPlace.getValue());
        String birthPlaceOther = colBirthPlaceOther.getValue();
        Boolean transportationPlan = StringUtil.toBoolean(colTransportationPlan.getValue());
        Boolean financialPreparedness = StringUtil.toBoolean(colFinancialPreparedness.getValue());
        // --- Postpartum Fields ---
        Boolean postpartumComplications = StringUtil.toBoolean(colPostpartumComplications.getValue());
        String postpartumComplicationDetails = colPostpartumComplicationDetails.getValue();
        BreastFeedingStatus breastfeedingStatus = BreastFeedingStatus.getFrom(colBreastfeedingStatus.getValue());
        Boolean resumedDailyActivities = StringUtil.toBoolean(colResumedDailyActivities.getValue());
        Boolean attendedPostpartumCheckup = StringUtil.toBoolean(colAttendedPostpartumCheckup.getValue());
        // --- Children Info ---
        Integer numberOfChilds = colNumberOfChilds.getIntegerValue();

        String affectedMembers = null;


        PregnancyVisit pregnancyVisit = this.entity;
        // Basic Identifiers
        pregnancyVisit.code = pregnancyRegistration.code;
        pregnancyVisit.visitCode = visitCode;
        pregnancyVisit.motherCode = mother.code;
        // Pregnancy Status and Visit Info
        pregnancyVisit.status = status;
        pregnancyVisit.visitType = visitType;
        pregnancyVisit.visitNumber = visitNumber;
        pregnancyVisit.visitDate = visitDate;
        // Antepartum Details
        pregnancyVisit.weeksGestation = weeksGestation;
        pregnancyVisit.prenatalCareReceived = prenatalCareReceived;
        pregnancyVisit.prenatalCareProvider = prenatalCareProvider;
        pregnancyVisit.complicationsReported = complicationsReported;
        pregnancyVisit.complicationDetails = complicationDetails;
        pregnancyVisit.hasBirthPlan = hasBirthPlan;
        pregnancyVisit.expectedBirthPlace = expectedBirthPlace;
        pregnancyVisit.birthPlaceOther = birthPlaceOther;
        pregnancyVisit.transportationPlan = transportationPlan;
        pregnancyVisit.financialPreparedness = financialPreparedness;
        // Postpartum Details
        pregnancyVisit.postpartumComplications = postpartumComplications;
        pregnancyVisit.postpartumComplicationDetails = postpartumComplicationDetails;
        pregnancyVisit.breastfeedingStatus = breastfeedingStatus;
        pregnancyVisit.resumedDailyActivities = resumedDailyActivities;
        pregnancyVisit.attendedPostpartumCheckup = attendedPostpartumCheckup;

        //childs
        if (repChilds != null) {
            for (int i = 0; i < repChilds.getCount(); i++) {
                PregnancyOutcomeType outcomeType = PregnancyOutcomeType.getFrom(colOutcomeTypes.get(i).getValue());
                BigDecimal weightValue = colChildWeights.get(i).getDecimalValue();
                String childCode =  colChildCodes.get(i).getValue();
                String childName = colChildNames.get(i).getValue();
                String childGender = colChildGenders.get(i).getValue(); // Usually "MALE" or "FEMALE"
                NewBornStatus childStatus = NewBornStatus.getFrom(colChildStatuses.get(i).getValue());
                Double childWeight = weightValue != null ? weightValue.doubleValue() : null;
                Set<IllnessSymptoms> illnessSymptoms = IllnessSymptomsCollectionConverter.getCollectionFrom(colChildIllnessSymptoms.get(i).getValue()); // multi-select - handled accordingly
                BreastFeedingStatus childBreastfeeding = BreastFeedingStatus.getFrom(colChildBreastfeedingStatuses.get(i).getValue());
                ImmunizationStatus childImmunization = ImmunizationStatus.getFrom(colChildImmunizationStatuses.get(i).getValue());
                String notes = colNotes.get(i).getValue();

                // Build child object
                PregnancyVisitChild child = this.boxPregnancyVisitChilds.query(PregnancyVisitChild_.childCode.equal(childCode)).build().findFirst();
                if (child != null) {
                    child.pregnancyCode = pregnancyVisit.code;
                    child.outcomeType = outcomeType;
                    child.childCode = childCode;
                    child.childStatus = childStatus;
                    child.childWeight = childWeight;
                    child.childIllnessSymptoms.clear();
                    child.childIllnessSymptoms.addAll(illnessSymptoms);
                    child.childBreastfeedingStatus = childBreastfeeding;
                    child.childImmunizationStatus = childImmunization;
                    child.notes = notes;

                    boxPregnancyVisitChilds.put(child);
                }
            }
        }

        //pregnancyVisit.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        //pregnancyVisit.recentlyCreated = true;
        //pregnancyVisit.recentlyCreatedUri = result.getFilename();

        this.boxPregnancyVisits.put(pregnancyVisit);

        //Update Pregnancy Registration
        boolean first_visit = this.boxPregnancyVisits.query(PregnancyVisit_.code.equal(pregnancyVisit.code)).build().count() == 1;
        int antepartum_count = (int) boxPregnancyVisits.query(PregnancyVisit_.code.equal(code).and(PregnancyVisit_.visitType.equal(PregnancyVisitType.ANTEPARTUM.code))).build().count();
        int postpartum_count = (int) boxPregnancyVisits.query(PregnancyVisit_.code.equal(code).and(PregnancyVisit_.visitType.equal(PregnancyVisitType.POSTPARTUM.code))).build().count();

        pregnancyRegistration = boxPregnancyRegistrations.get(pregnancyRegistration.id);
        pregnancyRegistration.summary_antepartum_count = antepartum_count;
        pregnancyRegistration.summary_postpartum_count = postpartum_count;
        pregnancyRegistration.summary_last_visit_status = pregnancyVisit.status;
        pregnancyRegistration.summary_last_visit_type = pregnancyVisit.visitType;
        pregnancyRegistration.summary_last_visit_date = pregnancyVisit.visitDate;
        pregnancyRegistration.summary_first_visit_date = first_visit ? pregnancyVisit.visitDate : null;
        pregnancyRegistration.summary_has_pregnancy_outcome = boxPregnancyOutcomes.query(PregnancyOutcome_.code.equal(pregnancyVisit.code)).build().count()>0;
        pregnancyRegistration.summary_nr_outcomes = (int) boxPregnancyChilds.query(PregnancyChild_.outcomeCode.equal(pregnancyVisit.code)).build().count();
        pregnancyRegistration.summary_followup_completed = (antepartum_count>=MAX_ANTEPARTUM_VISITS && postpartum_count >= MAX_POSTPARTUM_VISITS) || pregnancyVisit.status == PregnancyStatus.LOST_TRACK;
        boxPregnancyRegistrations.put(pregnancyRegistration);

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
        return null;
    }

    @Override
    public void collect() {

        if (!isPossibleToCollectVisit) {
            String message = getContext().getString(R.string.pregnancy_registration_warning_close_dates_w_previous_lbl);
            DialogFactory.createMessageInfo(this.context, getContext().getString(R.string.core_entity_pregnancy_out_lbl), message).show();
            return;
        }


        executeCollectForm();
    }

    private boolean canCollectPregnancyVisit() {
        boolean isPregnant = pregnancyRegistration != null && pregnancyRegistration.status == PregnancyStatus.PREGNANT;
        boolean hasDelivered = pregnancyRegistration != null && pregnancyRegistration.status == PregnancyStatus.DELIVERED;

        return isPregnant || (hasDelivered && (pregnancyRegistration.summary_followup_completed == null || !pregnancyRegistration.summary_followup_completed));
    }
    private void checkPreviousPregnancyOutcomes() {
        PregnancyOutcome lastPregnancyOutcome = boxPregnancyOutcomes.query(PregnancyOutcome_.motherCode.equal(mother.code)).orderDesc(PregnancyOutcome_.outcomeDate).build().findFirst();

        if (lastPregnancyOutcome != null) {
            Date currentDate = new Date();

            if (GeneralUtil.getAgeInDays(currentDate, lastPregnancyOutcome.outcomeDate) <= 184) { //less than 6 months
                //If another is being registered in less than six months
                //This woman had a previous pregnancy outcome [on DATE]. Are you sure this is a new, separate pregnancy event?
                String message = getContext().getString(R.string.pregnancy_registration_warning_close_dates_w_previous_lbl, StringUtil.formatYMD(lastPregnancyOutcome.outcomeDate));
                DialogFactory.createMessageInfo(this.context, getContext().getString(R.string.core_entity_pregnancy_out_lbl), message).show();
            }
        }
    }
}
