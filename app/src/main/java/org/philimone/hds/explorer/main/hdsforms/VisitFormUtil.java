package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;
import android.view.View;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.fragment.MemberFilterDialog;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Residency;
import org.philimone.hds.explorer.model.Residency_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.Visit_;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.HouseholdStatus;
import org.philimone.hds.explorer.model.enums.NoVisitReason;
import org.philimone.hds.explorer.model.enums.VisitLocationItem;
import org.philimone.hds.explorer.model.enums.VisitReason;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.utilities.StringUtil;

public class VisitFormUtil extends FormUtil<Visit> {

    //private Box<Household> boxHouseholds;
    private Box<Visit> boxVisits;
    private Box<Member> boxMembers;
    private Box<Residency> boxResidencies;

    //private Household household;
    private Member respondentMember;
    private boolean newHouseholdCreated;
    private boolean respondentNotRegistered = false;
    private boolean respondentExists;
    private boolean respondentResident = true;
    private int minimumRespondentAge;

    private static List<NoVisitReason> householdStatuses = Arrays.asList(NoVisitReason.HOUSE_OCCUPIED, NoVisitReason.HOUSE_VACANT, NoVisitReason.HOUSE_ABANDONED, NoVisitReason.HOUSE_DESTROYED, NoVisitReason.HOUSE_NOT_FOUND);

    public VisitFormUtil(Fragment fragment, Context context, Household household, boolean newHouseholdCreated, FormUtilities odkFormUtilities, FormUtilListener<Visit> listener){
        super(fragment, context, FormUtil.getVisitForm(context), odkFormUtilities, listener);

        this.household = household;
        this.newHouseholdCreated = newHouseholdCreated;

        initBoxes();
        initialize();
    }

    public VisitFormUtil(Fragment fragment, Context context, Household household, Visit visit, FormUtilities odkFormUtilities, FormUtilListener<Visit> listener){
        super(fragment, context, FormUtil.getVisitForm(context), visit, odkFormUtilities, listener);

        this.household = household;

        initBoxes();
        initialize();
    }

    public VisitFormUtil(AppCompatActivity activity, Context context, Household household, boolean newHouseholdCreated, FormUtilities odkFormUtilities, FormUtilListener<Visit> listener){
        super(activity, context, FormUtil.getVisitForm(context), odkFormUtilities, listener);

        this.household = household;
        this.newHouseholdCreated = newHouseholdCreated;

        initBoxes();
        initialize();
    }

    public VisitFormUtil(AppCompatActivity activity, Context context, Household household, Visit visit, FormUtilities odkFormUtilities, FormUtilListener<Visit> listener){
        super(activity, context, FormUtil.getVisitForm(context), visit, odkFormUtilities, listener);

        this.household = household;

        initBoxes();
        initialize();
    }

    public static VisitFormUtil newInstance(Mode openMode, AppCompatActivity activity, Context context, Household household, boolean newHouseholdCreated, Visit visit, FormUtilities odkFormUtilities, FormUtilListener<Visit> listener){
        if (openMode == Mode.CREATE) {
            return new VisitFormUtil(activity, context, household, newHouseholdCreated, odkFormUtilities, listener);
        } else if (openMode == Mode.EDIT) {
            return new VisitFormUtil(activity, context, household, visit, odkFormUtilities, listener);
        }

        return null;
    }

    public static VisitFormUtil newInstance(Mode openMode, Fragment fragment, Context context, Household household, boolean newHouseholdCreated, Visit visit, FormUtilities odkFormUtilities, FormUtilListener<Visit> listener){
        if (openMode == Mode.CREATE) {
            return new VisitFormUtil(fragment, context, household, newHouseholdCreated, odkFormUtilities, listener);
        } else if (openMode == Mode.EDIT) {
            return new VisitFormUtil(fragment, context, household, visit, odkFormUtilities, listener);
        }

        return null;
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        //this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxVisits = ObjectBoxDatabase.get().boxFor(Visit.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxResidencies = ObjectBoxDatabase.get().boxFor(Residency.class);

    }

    @Override
    protected void initialize() {
        super.initialize();

        this.minimumRespondentAge = retrieveMinimumRespondentAge();
    }

    private int retrieveMinimumRespondentAge() {
        ApplicationParam param = this.boxAppParams.query().equal(ApplicationParam_.name, ApplicationParam.PARAMS_MIN_AGE_OF_RESPONDENT, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (param != null) {
            try {
                return Integer.parseInt(param.value);
            } catch (Exception ex) {

            }
        }

        return 12;
    }

    @Override
    protected void preloadValues() {

        preloadedMap.put("code", codeGenerator.generateVisitCode(household));
        preloadedMap.put("householdCode", household.code);
        preloadedMap.put("visitDate", StringUtil.format(new Date(), "yyyy-MM-dd"));
        preloadedMap.put("roundNumber", currentRound.roundNumber+""); //get round number
        preloadedMap.put("respondentExists", respondentNotRegistered ? "" : respondentExists+"");
        preloadedMap.put("respondentResident", respondentResident+"");
        preloadedMap.put("respondentCode", respondentMember==null ? "" : respondentMember.code);
        preloadedMap.put("respondentName", respondentMember==null ? "" : respondentMember.name);

        if (newHouseholdCreated) {
            preloadedMap.put("respondentExists", "false");
            preloadedMap.put("visitPossible", "true");
            preloadedMap.put("visitReason", "NEW_HOUSE");
            preloadedMap.put("visitLocation", "HOME");
            //No need for GPS in this cases - and also whenever  its HOME doesnt need GPS, if its not will be inserted
            //preloadedMap.put("gpsLat", household.gpsLatitude.toString());
            //preloadedMap.put("gpsLon", household.gpsLongitude.toString());
            //preloadedMap.put("gpsAlt", household.gpsAltitude.toString());
            //preloadedMap.put("gpsAcc", household.gpsAccuracy.toString());
        }
        
    }

    @Override
    protected void preloadUpdatedValues() {
        //nothing to preload, but think about changing the respondent
    }

    @Override
    public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {
        ColumnValue colCode = collectedValues.get("code");
        ColumnValue colHouseholdCode = collectedValues.get("householdCode");
        ColumnValue colVisitDate = collectedValues.get("visitDate"); //check date in future / in past
        ColumnValue colRoundNumber = collectedValues.get("roundNumber"); //not blank
        ColumnValue colVisitReason = collectedValues.get("visitReason"); //not blank
        ColumnValue colVisitPossible = collectedValues.get("visitPossible");
        ColumnValue colvisitNotPossibleReason = collectedValues.get("visitNotPossibleReason");
        ColumnValue colRespondentExists = collectedValues.get("respondentExists");
        ColumnValue colRespondentResident = collectedValues.get("respondentResident");
        ColumnValue colRespondentRelationship = collectedValues.get("respondentRelationship");
        ColumnValue colRespondentCode = collectedValues.get("respondentCode"); //optional blank (NOR NEW_HOUSE)
        ColumnValue colRespondentName = collectedValues.get("respondentName");
        ColumnValue colHasInterpreter = collectedValues.get("hasInterpreter"); //not blank if (NOT NEW_HOUSE)
        ColumnValue colInterpreterName = collectedValues.get("interpreterName"); //not blank if hasInterpreter
        ColumnValue colVisitLocation = collectedValues.get("visitLocation"); //not blank
        ColumnValue colVisitLocationOther = collectedValues.get("visitLocationOther"); //not blank if has visitLocation

        String visit_code = colCode.getValue();
        String household_code = colHouseholdCode.getValue();
        VisitReason visit_reason = VisitReason.getFrom(colVisitReason.getValue());
        Boolean visitPossible = StringUtil.getBooleanValue(colVisitPossible.getValue());
        NoVisitReason visitNotPossibleReason = NoVisitReason.getFrom(colvisitNotPossibleReason.getValue());
        Boolean _respondentExists = StringUtil.getBooleanValue(colRespondentExists.getValue());
        Boolean respondentResident = StringUtil.getBooleanValue(colRespondentResident.getValue());
        String respondentRelationship = colRespondentRelationship.getValue();
        String respondentName = colRespondentName.getValue();
        boolean hasInterpreter = colHasInterpreter.getValue()!=null && colHasInterpreter.getValue().equalsIgnoreCase("true");
        VisitLocationItem visit_location = VisitLocationItem.getFrom(colVisitLocation.getValue());
        Date visitDate = colVisitDate.getDateValue();

        _respondentExists = _respondentExists != null && _respondentExists;

        if (!codeGenerator.isVisitCodeValid(visit_code)){
            String message = this.context.getString(R.string.new_visit_code_err_lbl);
            return new ValidationResult(colCode, message);
        }

        if (!visit_code.startsWith(household.code)){
            String message = this.context.getString(R.string.new_visit_code_household_err_lbl);
            return new ValidationResult(colCode, message);
        }

        //check if visit with code exists
        boolean visitExists = boxVisits.query().equal(Visit_.code, visit_code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst() != null;
        if (visitExists && currentMode==Mode.CREATE){
            String message = this.context.getString(R.string.new_visit_code_exists_lbl);
            return new ValidationResult(colCode, message);
        }

        if (visitDate.after(new Date())){ //is in the future
            String message = this.context.getString(R.string.new_visit_date_not_great_today_lbl);
            return new ValidationResult(colVisitDate, message);
        }

        if (StringUtil.isBlank(colRoundNumber.getValue())){
            String message = this.context.getString(R.string.new_visit_round_number_empty_lbl);
            return new ValidationResult(colRoundNumber, message);
        }

        if (StringUtil.isBlank(colVisitReason.getValue())){
            String message = this.context.getString(R.string.new_visit_reason_empty_lbl);
            return new ValidationResult(colVisitReason, message);
        }

        if (visit_reason != VisitReason.NEW_HOUSEHOLD && _respondentExists && StringUtil.isBlank(colRespondentCode.getValue())){
            String message = this.context.getString(R.string.new_visit_respondent_empty_lbl);
            return new ValidationResult(colRespondentCode, message);
        }

        //if not answered is false
        //if (visit_reason != VisitReason.NEW_HOUSEHOLD && StringUtil.isBlank(colHasInterpreter.getValue())){
        //    String message = this.context.getString(R.string.new_visit_hasinterpreter_empty_lbl);
        //    return new ValidationResult(colHasInterpreter, message);
        //}

        if (hasInterpreter && StringUtil.isBlank(colInterpreterName.getValue())){
            String message = this.context.getString(R.string.new_visit_interpreter_empty_lbl);
            return new ValidationResult(colInterpreterName, message);
        }

        if (visitPossible && StringUtil.isBlank(colVisitLocation.getValue())){
            String message = this.context.getString(R.string.new_visit_location_empty_lbl);
            return new ValidationResult(colVisitLocation, message);
        }

        if (visitPossible && visit_location == VisitLocationItem.OTHER_PLACE && StringUtil.isBlank(colVisitLocationOther.getValue())){
            String message = this.context.getString(R.string.new_visit_location_other_empty_lbl);
            return new ValidationResult(colVisitLocationOther, message);
        }

        return ValidationResult.noErrors();
    }

    @Override
    public void onBeforeFormFinished(HForm form, CollectedDataMap collectedValues) {
        //using it to update collectedHouseholdId, collectedMemberId
        ColumnValue colHouseholdId = collectedValues.get("collectedHouseholdId");
        //ColumnValue colMemberId = collectedValues.get("collectedMemberId");

        colHouseholdId.setValue(this.household.collectedId);
        //colMemberId.setValue(this.member.collectedId);
    }

    @Override
    public void onFormFinished(HForm form, CollectedDataMap collectedValues, XmlFormResult result) {

        Log.d("resultxml", result.getXmlResult());

        //saveNewHousehold();
        ColumnValue colCode = collectedValues.get("code");
        ColumnValue colHouseholdCode = collectedValues.get("householdCode");
        ColumnValue colVisitDate = collectedValues.get("visitDate");
        ColumnValue colRoundNumber = collectedValues.get("roundNumber");
        ColumnValue colVisitReason = collectedValues.get("visitReason");
        ColumnValue colVisitPossible = collectedValues.get("visitPossible");
        ColumnValue colvisitNotPossibleReason = collectedValues.get("visitNotPossibleReason");
        ColumnValue colRespondentResident = collectedValues.get("respondentResident");
        ColumnValue colRespondentRelationship = collectedValues.get("respondentRelationship");
        ColumnValue colRespondentCode = collectedValues.get("respondentCode");
        ColumnValue colRespondentName = collectedValues.get("respondentName");
        ColumnValue colHasInterpreter = collectedValues.get("hasInterpreter");
        ColumnValue colInterpreterName = collectedValues.get("interpreterName");
        ColumnValue colVisitLocation = collectedValues.get("visitLocation");
        ColumnValue colVisitLocationOther = collectedValues.get("visitLocationOther");
        ColumnValue colCollBy = collectedValues.get("collectedBy");
        ColumnValue colCollDate = collectedValues.get("collectedDate");
        ColumnValue colGps = collectedValues.get("gps");
        Map<String, Double> gpsValues = colGps.getGpsValues();
        Double gpsLat = gpsValues.get("gpsLat");
        Double gpsLon = gpsValues.get("gpsLon");
        Double gpsAlt = gpsValues.get("gpsAlt");
        Double gpsAcc = gpsValues.get("gpsAcc");


        Log.d("dates", ""+colVisitDate.getValue()+", "+colVisitDate.getDateValue());

        Visit visit = (currentMode==Mode.EDIT) ? entity : new Visit(); //EDIT VS CREATE

        visit.code = colCode.getValue();
        visit.householdCode = colHouseholdCode.getValue();
        visit.visitDate = colVisitDate.getDateValue();
        visit.roundNumber = colRoundNumber.getIntegerValue();
        visit.visitReason = VisitReason.getFrom(colVisitReason.getValue());
        visit.visitPossible = StringUtil.getBooleanValue(colVisitPossible.getValue());;
        visit.visitNotPossibleReason = NoVisitReason.getFrom(colvisitNotPossibleReason.getValue());
        visit.respondentResident = StringUtil.getBooleanValue(colRespondentResident.getValue());
        visit.respondentRelationship = colRespondentRelationship.getValue();
        visit.respondentCode = colRespondentCode.getValue();
        visit.respondentName = colRespondentName.getValue();
        visit.hasInterpreter = Boolean.getBoolean(colHasInterpreter.getValue());
        visit.interpreterName = colInterpreterName.getValue();
        visit.visitLocation = VisitLocationItem.getFrom(colVisitLocation.getValue());
        visit.visitLocationOther = colVisitLocationOther.getValue();

        visit.gpsLatitude = gpsLat;
        visit.gpsLongitude = gpsLon;
        visit.gpsAltitude = gpsAlt;
        visit.gpsAccuracy = gpsAcc;
        visit.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        visit.recentlyCreated = true;
        visit.recentlyCreatedUri = result.getFilename();

        boxVisits.put(visit);

        if (visit.visitNotPossibleReason!=null && householdStatuses.contains(visit.visitNotPossibleReason)){
            this.household.status = HouseholdStatus.getFrom(visit.visitNotPossibleReason.code);
            this.boxHouseholds.put(this.household);
        }

        if (currentMode == Mode.CREATE) {
            collectedData = new CoreCollectedData();
            collectedData.visitId = visit.id;
            collectedData.formEntity = CoreFormEntity.VISIT;
            collectedData.formEntityId = visit.id;
            collectedData.formEntityCode = visit.code;
            collectedData.formEntityName = visit.code;
            collectedData.formUuid = result.getFormUuid();
            collectedData.formFilename = result.getFilename();
            collectedData.createdDate = new Date();
            collectedData.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
            collectedData.extension.setTarget(this.getFormExtension(collectedData.formEntity));

            boxCoreCollectedData.put(collectedData);

            updateNewHouseholdCoreCollectedData(visit);
        } else {
            collectedData.updatedDate = new Date();
            boxCoreCollectedData.put(collectedData);
        }

        this.entity = visit;

        if (currentMode == Mode.CREATE) {
            if (visit.visitPossible) {
                this.collectExtensionForm(collectedValues);
            } else {
                onFinishedExtensionCollection();
            }
        } else {
            onFinishedExtensionCollection();
        }

    }

    @Override
    protected void onFinishedExtensionCollection() {
        Visit visit = this.entity;
        if (listener != null) {
            Map<String,Object> data = new HashMap<>();

            data.put("respondentNotRegistered", new Boolean(respondentNotRegistered));

            if (currentMode == Mode.CREATE) {
                listener.onNewEntityCreated(visit, data);
            } else if (currentMode == Mode.EDIT) {
                listener.onEntityEdited(visit, data);
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

        if (newHouseholdCreated || currentMode==Mode.EDIT) {
            if (this.entity != null && this.entity.respondentCode != null) {
                this.respondentMember = this.boxMembers.query(Member_.code.equal(this.entity.respondentCode)).build().findFirst();
            }

            executeCollectForm();
        } else if (currentMode == Mode.CREATE) {
            selectRespondent();
        }
    }

    private void selectRespondent(){
        MemberFilterDialog filterDialog = MemberFilterDialog.newInstance(this.fragmentManager, this.context.getString(R.string.new_visit_select_respondent_lbl), new MemberFilterDialog.Listener() {
            @Override
            public void onSelectedMember(Member member) {
                respondentMember = member;
                evaluateRespondentResident();
                executeCollectForm();
            }

            @Override
            public void onCanceled() {

            }
        });

        filterDialog.enableButton(MemberFilterDialog.Buttons.BUTTON_1, R.string.new_visit_respondent_not_reg_lbl, v -> {
            //respondent not yet registered - set as unknown
            onRespondentNotYetRegistered();
        });

        filterDialog.enableButton(MemberFilterDialog.Buttons.BUTTON_2, R.string.new_visit_respondent_not_resident_lbl, v -> {
            onRespondentNotResident();
        });

        filterDialog.setCancelable(true);
        filterDialog.setFastFilterHousehold(this.household); //to make faster search in this household
        filterDialog.setFilterHouseCode(this.household.code);
        filterDialog.setFilterStatus(MemberFilterDialog.StatusFilter.RESIDENT, true);
        filterDialog.setFilterMinAge(this.minimumRespondentAge, false);
        filterDialog.setStartSearchOnShow(true);
        filterDialog.show();

    }

    private void evaluateRespondentResident(){
        //this.household
        String memberCode = this.respondentMember.code;
        this.respondentResident = this.boxResidencies.query(Residency_.householdCode.equal(this.household.code)
                                                           .and(Residency_.memberCode.equal(memberCode))
                                                           .and(Residency_.endType.equal(ResidencyEndType.NOT_APPLICABLE.code))).build().count()>0;
        this.respondentExists = true;

    }

    private void onRespondentNotYetRegistered() {
        this.respondentMember = Member.getUnknownIndividual();
        this.respondentNotRegistered = true;
        this.respondentExists = true;

        DialogFactory.createMessageInfo(this.context, R.string.new_visit_respondent_not_reg_lbl, R.string.new_visit_respondent_not_reg_continue_lbl, clickedButton -> {
            executeCollectForm();
        }).show();
    }

    private void onRespondentNotResident() {
        this.respondentMember = null;
        this.respondentResident = false;
        this.respondentExists = false;

        executeCollectForm();
    }

    private void updateNewHouseholdCoreCollectedData(Visit visit) {
        if (newHouseholdCreated) {
            CoreCollectedData coreData = boxCoreCollectedData.query().equal(CoreCollectedData_.formEntityCode, household.code, QueryBuilder.StringOrder.CASE_SENSITIVE).and()
                    .equal(CoreCollectedData_.formEntity, CoreFormEntity.HOUSEHOLD.code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

            Log.d("found household", "core: "+coreData);

            //update household with visit
            if (coreData != null) {
                coreData.visitId = visit.id;
                boxCoreCollectedData.put(coreData);
            }
        }
    }

    public static void updateEndTimestamp(Context context, String xmlSavedFormPath) {
        HForm form = getVisitForm(context);
        FormFragment.updateEndTimestamp(form, xmlSavedFormPath);
    }

    public static void updateRespondent(Context context, String xmlSavedFormPath, Member respondentMember) {
        HForm form = getVisitForm(context);
        FormFragment.updateColumnOnXML(form, xmlSavedFormPath, "respondentCode", respondentMember.code);
    }

    public static void markAllAsNonVisited(Context context, String xmlSavedFormPath, String nonVisitedList) {
        HForm form = getVisitForm(context);
        FormFragment.updateColumnOnXML(form, xmlSavedFormPath, "nonVisitedMembers", nonVisitedList);
    }



}
