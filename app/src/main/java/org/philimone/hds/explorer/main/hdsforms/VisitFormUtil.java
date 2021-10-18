package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.MemberFilterDialog;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Round;
import org.philimone.hds.explorer.model.Round_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.Visit_;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.VisitLocationItem;
import org.philimone.hds.explorer.model.enums.VisitReason;
import org.philimone.hds.explorer.settings.generator.CodeGeneratorService;
import org.philimone.hds.forms.listeners.FormCollectionListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;
import org.philimone.hds.forms.parsers.ExcelFormParser;

import java.io.InputStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import androidx.fragment.app.FragmentManager;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.StringUtil;

public class VisitFormUtil extends FormUtil<Visit> {

    private Box<Household> boxHouseholds;
    private Box<Visit> boxVisits;
    private Box<CoreCollectedData> boxCoreCollectedData;

    private Household household;
    private Member respondentMember;
    private boolean newHouseholdCreated;

    public VisitFormUtil(FragmentManager fragmentManager, Context context, Household household, boolean newHouseholdCreated, FormUtilListener<Visit> listener){
        super(fragmentManager, context, FormUtil.getVisitForm(context), listener);

        this.household = household;
        this.newHouseholdCreated = newHouseholdCreated;

        initBoxes();
        initialize();
    }

    public VisitFormUtil(FragmentManager fragmentManager, Context context, Household household, Visit visit, FormUtilListener<Visit> listener){
        super(fragmentManager, context, FormUtil.getVisitForm(context), visit, listener);

        this.household = household;

        initBoxes();
        initialize();
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxVisits = ObjectBoxDatabase.get().boxFor(Visit.class);
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
    }

    @Override
    protected void preloadValues() {

        preloadedMap.put("code", codeGenerator.generateVisitCode(household));
        preloadedMap.put("householdCode", household.code);
        preloadedMap.put("visitDate", StringUtil.format(new Date(), "yyyy-MM-dd"));
        preloadedMap.put("roundNumber", currentRound.roundNumber+""); //get round number
        preloadedMap.put("respondentCode", respondentMember==null ? "" : respondentMember.code);

        if (newHouseholdCreated) {
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
    public ValidationResult onFormValidate(HForm form, Map<String, ColumnValue> collectedValues) {
        ColumnValue colCode = collectedValues.get("code");
        ColumnValue colHouseholdCode = collectedValues.get("householdCode");
        ColumnValue colVisitDate = collectedValues.get("visitDate"); //check date in future / in past
        ColumnValue colRoundNumber = collectedValues.get("roundNumber"); //not blank
        ColumnValue colVisitReason = collectedValues.get("visitReason"); //not blank
        ColumnValue colRespondentCode = collectedValues.get("respondentCode"); //optional blank (NOR NEW_HOUSE)
        ColumnValue colHasInterpreter = collectedValues.get("hasInterpreter"); //not blank if (NOT NEW_HOUSE)
        ColumnValue colInterpreterName = collectedValues.get("interpreterName"); //not blank if hasInterpreter
        ColumnValue colVisitLocation = collectedValues.get("visitLocation"); //not blank
        ColumnValue colVisitLocationOther = collectedValues.get("visitLocationOther"); //not blank if has visitLocation

        String visit_code = colCode.getValue();
        String household_code = colHouseholdCode.getValue();
        VisitReason visit_reason = VisitReason.getFrom(colVisitReason.getValue());
        boolean hasInterpreter = colHasInterpreter.getValue()!=null && colHasInterpreter.getValue().equalsIgnoreCase("true");
        VisitLocationItem visit_location = VisitLocationItem.getFrom(colVisitLocation.getValue());
        Date visitDate = colVisitDate.getDateValue();

        if (!codeGenerator.isVisitCodeValid(visit_code)){
            String message = this.context.getString(R.string.new_household_code_err_lbl);
            return new ValidationResult(colCode, message);
        }

        if (!visit_code.startsWith(household.code)){
            String message = this.context.getString(R.string.new_visit_code_household_err_lbl);
            return new ValidationResult(colCode, message);
        }

        //check if visit with code exists
        boolean visitExists = boxVisits.query().equal(Visit_.code, visit_code).build().findFirst() != null;
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

        if (visit_reason != VisitReason.NEW_HOUSEHOLD && StringUtil.isBlank(colRespondentCode.getValue())){
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

        if (StringUtil.isBlank(colVisitLocation.getValue())){
            String message = this.context.getString(R.string.new_visit_location_empty_lbl);
            return new ValidationResult(colVisitLocation, message);
        }

        if (visit_location == VisitLocationItem.OTHER_PLACE && StringUtil.isBlank(colVisitLocationOther.getValue())){
            String message = this.context.getString(R.string.new_visit_location_other_empty_lbl);
            return new ValidationResult(colVisitLocationOther, message);
        }

        return ValidationResult.noErrors();
    }

    @Override
    public void onFormFinished(HForm form, Map<String, ColumnValue> collectedValues, XmlFormResult result) {

        Log.d("resultxml", result.getXmlResult());

        //saveNewHousehold();
        ColumnValue colCode = collectedValues.get("code");
        ColumnValue colHouseholdCode = collectedValues.get("householdCode");
        ColumnValue colVisitDate = collectedValues.get("visitDate");
        ColumnValue colRoundNumber = collectedValues.get("roundNumber");
        ColumnValue colVisitReason = collectedValues.get("visitReason");
        ColumnValue colRespondentCode = collectedValues.get("respondentCode");
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
        visit.respondentCode = colRespondentCode.getValue();
        visit.hasInterpreter = Boolean.getBoolean(colHasInterpreter.getValue());
        visit.interpreterName = colInterpreterName.getValue();
        visit.visitLocation = VisitLocationItem.getFrom(colVisitLocation.getValue());
        visit.visitLocationOther = colVisitLocationOther.getValue();

        visit.gpsLatitude = gpsLat;
        visit.gpsLongitude = gpsLon;
        visit.gpsAltitude = gpsAlt;
        visit.gpsAccuracy = gpsAcc;
        visit.recentlyCreated = true;
        visit.recentlyCreatedUri = result.getFilename();

        boxVisits.put(visit);

        if (currentMode == Mode.CREATE) {
            CoreCollectedData collectedData = new CoreCollectedData();
            collectedData.visitId = visit.id;
            collectedData.formEntity = CoreFormEntity.VISIT;
            collectedData.formEntityId = visit.id;
            collectedData.formEntityCode = visit.code;
            collectedData.formEntityName = visit.code;
            collectedData.formUuid = result.getFormUuid();
            collectedData.formFilename = result.getFilename();
            collectedData.createdDate = new Date();

            boxCoreCollectedData.put(collectedData);

            updateNewHouseholdCoreCollectedData(visit);
        }

        if (listener != null) {
            if (currentMode == Mode.CREATE) {
                listener.onNewEntityCreated(visit);
            } else if (currentMode == Mode.EDIT) {
                listener.onEntityEdited(visit);
            }
        }

    }

    @Override
    public void onFormCancelled(){
        if (listener != null) {
            listener.onFormCancelled();
        }
    }

    public void collect() {

        if (newHouseholdCreated || currentMode==Mode.EDIT) {
            executeCollectForm();
        } else {
            selectRespondent();
        }
    }

    private void selectRespondent(){
        MemberFilterDialog filterDialog = MemberFilterDialog.newInstance(this.fragmentManager, this.context.getString(R.string.new_visit_select_respondent_lbl), selectedMember -> {
            respondentMember = selectedMember;
            executeCollectForm();
        });

        filterDialog.show();
    }

    private void updateNewHouseholdCoreCollectedData(Visit visit) {
        if (newHouseholdCreated) {
            CoreCollectedData coreData = boxCoreCollectedData.query().equal(CoreCollectedData_.formEntityCode, household.code).and()
                    .equal(CoreCollectedData_.formEntity, CoreFormEntity.HOUSEHOLD.code).build().findFirst();

            Log.d("found household", "core: "+coreData);

            //update household with visit
            if (coreData != null) {
                coreData.visitId = visit.id;
                boxCoreCollectedData.put(coreData);
            }
        }
    }

}
