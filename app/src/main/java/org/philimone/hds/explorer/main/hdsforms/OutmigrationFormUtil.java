package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Death;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.HeadRelationship_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Outmigration;
import org.philimone.hds.explorer.model.Residency;
import org.philimone.hds.explorer.model.Residency_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.HeadRelationshipType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipEndType;
import org.philimone.hds.explorer.model.enums.temporal.OutMigrationType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.model.oldstate.SavedEntityState;
import org.philimone.hds.explorer.model.oldstate.SavedEntityState_;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.utilities.StringUtil;

public class OutmigrationFormUtil extends FormUtil<Outmigration> {

    //private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<Residency> boxResidencies;
    private Box<HeadRelationship> boxHeadRelationships;
    private Box<Outmigration> boxOutmigrations;
    protected Box<Death> boxDeaths;

    //private Household household;
    private Visit visit;
    private Member member;
    private Residency currentMemberResidency;
    private HeadRelationship currentMemberHeadRelationship;
    private Member currentHead;

    public OutmigrationFormUtil(Fragment fragment, Context context, Visit visit, Household household, Member member, FormUtilities odkFormUtilities, FormUtilListener<Outmigration> listener){
        super(fragment, context, FormUtil.getOutmigrationForm(context), odkFormUtilities, listener);

        //Log.d("enu-household", ""+household);

        this.household = household;
        this.member = member;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    public OutmigrationFormUtil(Fragment fragment, Context context, Visit visit, Household household, Outmigration outmigToEdit, FormUtilities odkFormUtilities, FormUtilListener<Outmigration> listener){
        super(fragment, context, FormUtil.getOutmigrationForm(context), outmigToEdit, odkFormUtilities, listener);

        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();

        this.member = this.boxMembers.query(Member_.code.equal(outmigToEdit.memberCode)).build().findFirst();

        readSavedEntityState();
    }

    public static OutmigrationFormUtil newInstance(Mode openMode, Fragment fragment, Context context, Visit visit, Household household, Member member, Outmigration outmigToEdit, FormUtilities odkFormUtilities, FormUtilListener<Outmigration> listener){
        if (openMode == Mode.CREATE) {
            return new OutmigrationFormUtil(fragment, context, visit, household, member, odkFormUtilities, listener);
        } else if (openMode == Mode.EDIT) {
            return new OutmigrationFormUtil(fragment, context, visit, household, outmigToEdit, odkFormUtilities, listener);
        }

        return null;
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        //this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxResidencies = ObjectBoxDatabase.get().boxFor(Residency.class);
        this.boxHeadRelationships = ObjectBoxDatabase.get().boxFor(HeadRelationship.class);
        this.boxOutmigrations = ObjectBoxDatabase.get().boxFor(Outmigration.class);

        this.boxDeaths = ObjectBoxDatabase.get().boxFor(Death.class);
    }

    @Override
    protected void initialize(){
        super.initialize();

        Log.d("init-household", ""+household);
    }

    private void readSavedEntityState() {
        Map<String, String> mapSavedStates = new HashMap<>();

        SavedEntityState savedState = this.boxSavedEntityStates.query(SavedEntityState_.formEntity.equal(CoreFormEntity.OUTMIGRATION.code)
                .and(SavedEntityState_.collectedId.equal(this.entity.id))
                .and(SavedEntityState_.objectKey.equal("outimgFormUtilState"))).build().findFirst();

        if (savedState != null) {
            HashMap map = new Gson().fromJson(savedState.objectGsonValue, HashMap.class);
            for (Object key : map.keySet()) {
                mapSavedStates.put(key.toString(), map.get(key).toString());
            }
        }

        String strResidencyId = mapSavedStates.get("currentResidencyId");
        String strHeadRelationshipId = mapSavedStates.get("currentHeadRelationshipId");

        if (strResidencyId != null) {
            this.currentMemberResidency = this.boxResidencies.get(Long.parseLong(strResidencyId));
        }
        if (strHeadRelationshipId != null) {
            this.currentMemberHeadRelationship = this.boxHeadRelationships.get(Long.parseLong(strHeadRelationshipId));
        }

    }

    @Override
    protected void preloadValues() {
        //member_details_unknown_lbl

        preloadedMap.put("visitCode", this.visit.code);
        preloadedMap.put("memberCode", this.member.code);
        preloadedMap.put("memberName", this.member.name);
        preloadedMap.put("migrationType", OutMigrationType.EXTERNAL.code);
        preloadedMap.put("originCode", this.household.code);
        preloadedMap.put("modules", this.user.getSelectedModulesCodes());
    }

    @Override
    protected void preloadUpdatedValues() {

    }

    @Override
    public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colMemberCode = collectedValues.get("memberCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colMemberName = collectedValues.get("memberName"); //not blank
        ColumnValue colMigrationType = collectedValues.get("migrationType");
        ColumnValue colOriginCode = collectedValues.get("originCode");
        //ColumnValue colDestinationCode = collectedValues.get("destinationCode");
        ColumnValue colDestinationOther = collectedValues.get("destinationOther");
        ColumnValue colMigrationDate = collectedValues.get("migrationDate"); //not null cannot be in the future nor before dob
        ColumnValue colMigrationReason = collectedValues.get("migrationReason");
        ColumnValue colMigrationReasonOther = collectedValues.get("migrationReasonOther");

        //ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        //ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        //ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String memberCode = colMemberCode.getValue();
        String memberName = colMemberName.getValue();
        OutMigrationType migrationType = OutMigrationType.getFrom(colMigrationType.getValue());
        String originCode = colOriginCode.getValue();
        String destinationOther = colDestinationOther.getValue();
        Date migrationDate = colMigrationDate.getDateValue();
        String migrationReason = colMigrationReason.getValue();
        String migrationReasonOther = colMigrationReasonOther.getValue();

        //validations
        //memberCode blank/valid
        if (!codeGenerator.isMemberCodeValid(memberCode)){
            String message = this.context.getString(R.string.new_member_code_err_lbl);
            return new ValidationResult(colMemberCode, message);
        }

        if (StringUtil.isBlank(memberName)){
            String message = this.context.getString(R.string.new_member_name_empty_lbl);
            return new ValidationResult(colMemberName, message);
        }

        //validate migrationType
        if (migrationType == null){
            String message = this.context.getString(R.string.external_inmigration_migration_type_empty_lbl);
            return new ValidationResult(colMigrationType, message);
        }

        if (migrationDate == null){
            String message = this.context.getString(R.string.external_inmigration_migrationdate_empty_lbl);
            return new ValidationResult(colMigrationDate, message);
        }

        //migration date cannot be in the future
        if (migrationDate.after(new Date())){ //is in the future
            String message = this.context.getString(R.string.external_inmigration_migrationdate_not_great_today_lbl);
            return new ValidationResult(colMigrationDate, message);
        }

        //migration date cannot be before dob
        if (migrationDate.before(this.member.dob)){ //is before dob
            String message = this.context.getString(R.string.external_inmigration_migrationdate_not_before_dob_lbl);
            return new ValidationResult(colMigrationDate, message);
        }

        //check destinationCode, originCode existence - both a selected automatically
        //check Member Death Status - if returning to DSS
        if (Queries.getDeathByCode(this.boxDeaths, memberCode)!=null){
            String message = this.context.getString(R.string.outmigration_death_exists_lbl);
            return new ValidationResult(colMemberCode, message);
        }

        //check if Member is a head of household of another household and Check if is the last individual in the household
        if (isHeadOfHousehold(memberCode, household.code) && !isTheOnlyHouseholdMember(memberCode, household.code)) { //yet still
            String message = this.context.getString(R.string.outmigration_is_head_of_household_lbl);
            return new ValidationResult(colMemberCode, message);
        }

        //check last residency and head relationship start dates because they are endType=NA
        Residency lastResidency = currentMemberResidency;
        HeadRelationship lastRelationship = currentMemberHeadRelationship;

        //migrationDate vs lastResidency.startDate --- the endDate is null right now
        if (lastResidency != null && lastResidency.startDate != null && migrationDate.before(lastResidency.startDate)){
            String message = this.context.getString(R.string.outmigration_migrationdate_not_before_residency_startdate_lbl, StringUtil.formatYMD(migrationDate), lastResidency.startType.code, StringUtil.formatYMD(lastResidency.startDate));
            return new ValidationResult(colMigrationDate, message);
        }
        //migrationDate vs lastRelationship.endDate
        if (lastRelationship != null && lastRelationship.startDate != null && migrationDate.before(lastRelationship.startDate)){
            String message = this.context.getString(R.string.outmigration_migrationdate_not_before_hrelationship_startdate_lbl, StringUtil.formatYMD(migrationDate), lastRelationship.startType.code, StringUtil.formatYMD(lastRelationship.startDate));
            return new ValidationResult(colMigrationDate, message);
        }

        return ValidationResult.noErrors();
    }

    private HeadRelationship getLastHeadRelationship(String memberCode) {
        HeadRelationship lastHeadRelationship = this.boxHeadRelationships.query(HeadRelationship_.memberCode.equal(memberCode))
                .orderDesc(HeadRelationship_.startDate)
                .build().findFirst();

        return lastHeadRelationship;
    }

    private Residency getLastResidency(String memberCode) {
        Residency lastResidency = this.boxResidencies.query(Residency_.memberCode.equal(memberCode))
                .orderDesc(Residency_.startDate)
                .build().findFirst();

        return lastResidency;
    }

    private boolean isHeadOfHousehold(String memberCode, String householdCode) {
        long count = this.boxHeadRelationships.query(
                HeadRelationship_.memberCode.equal(memberCode).and(HeadRelationship_.householdCode.equal(householdCode))
            .and(HeadRelationship_.relationshipType.equal(HeadRelationshipType.HEAD_OF_HOUSEHOLD.code))
            .and(HeadRelationship_.endType.equal(HeadRelationshipEndType.NOT_APPLICABLE.code)))
            .build().count();

        return count > 0;
    }

    private boolean isTheOnlyHouseholdMember(String memberCode, String householdCode){

        List<Residency> houseResidents = boxResidencies.query(Residency_.householdCode.equal(householdCode).and(Residency_.endType.equal(ResidencyEndType.NOT_APPLICABLE.code))).orderDesc(Residency_.startDate).build().find();

        if (houseResidents.size()==1 && !StringUtil.isBlank(memberCode) && memberCode.equals(houseResidents.get(0).memberCode)) {
            return true; //the only one living in the household
        }

        return false;
    }

    @Override
    public void onBeforeFormFinished(HForm form, CollectedDataMap collectedValues) {
        //using it to update collectedHouseholdId, collectedMemberId
        ColumnValue colHouseholdId = collectedValues.get("collectedHouseholdId");
        ColumnValue colMemberId = collectedValues.get("collectedMemberId");

        colHouseholdId.setValue(this.household.collectedId);
        colMemberId.setValue(this.member.collectedId);
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
        //saveNewHousehold();
        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colMemberCode = collectedValues.get("memberCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colMemberName = collectedValues.get("memberName"); //not blank
        ColumnValue colMigrationType = collectedValues.get("migrationType");
        ColumnValue colOriginCode = collectedValues.get("originCode");
        //ColumnValue colDestinationCode = collectedValues.get("destinationCode");
        ColumnValue colDestinationOther = collectedValues.get("destinationOther");
        ColumnValue colMigrationDate = collectedValues.get("migrationDate"); //not null cannot be in the future nor before dob
        ColumnValue colMigrationReason = collectedValues.get("migrationReason");
        ColumnValue colMigrationReasonOther = collectedValues.get("migrationReasonOther");

        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String memberCode = colMemberCode.getValue();
        String memberName = colMemberName.getValue();
        OutMigrationType migrationType = OutMigrationType.getFrom(colMigrationType.getValue());
        String originCode = colOriginCode.getValue();
        String destinationOther = colDestinationOther.getValue();
        Date migrationDate = colMigrationDate.getDateValue();
        String migrationReason = colMigrationReason.getValue();
        String migrationReasonOther = colMigrationReasonOther.getValue();

        //update member, close residency, close headrelationship, create Outmigration

        //Outmigration
        Outmigration outmigration = new Outmigration();
        outmigration.visitCode = visitCode;
        outmigration.memberCode = memberCode;
        outmigration.migrationType = OutMigrationType.EXTERNAL;
        outmigration.originCode = this.household.code;
        //outmigration.destinationCode = household.code;
        outmigration.destinationOther = destinationOther;
        outmigration.migrationDate = migrationDate;
        outmigration.migrationReason = migrationReasonOther != null ?  migrationReasonOther : migrationReason;
        outmigration.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        outmigration.recentlyCreated = true;
        outmigration.recentlyCreatedUri = result.getFilename();
        this.boxOutmigrations.put(outmigration);

        //Residency - close
        currentMemberResidency = boxResidencies.get(currentMemberResidency.id);
        currentMemberResidency.endDate = migrationDate;
        currentMemberResidency.endType = ResidencyEndType.EXTERNAL_OUTMIGRATION;
        this.boxResidencies.put(currentMemberResidency);

        //HeadRelationship - close
        currentMemberHeadRelationship.endDate = migrationDate;
        currentMemberHeadRelationship.endType = HeadRelationshipEndType.EXTERNAL_OUTMIGRATION;
        this.boxHeadRelationships.put(currentMemberHeadRelationship);

        //update member
        member = boxMembers.get(member.id);
        member.endType = ResidencyEndType.EXTERNAL_OUTMIGRATION;
        member.endDate = migrationDate;
        this.boxMembers.put(member);

        //save core collected data
        collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.OUTMIGRATION;
        collectedData.formEntityId = outmigration.id;
        collectedData.formEntityCode = member.code;
        collectedData.formEntityName = member.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        collectedData.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        collectedData.extension.setTarget(this.getFormExtension(collectedData.formEntity));
        this.boxCoreCollectedData.put(collectedData);

        //save state for editing
        HashMap<String,String> saveStateMap = new HashMap<>();
        saveStateMap.put("currentResidencyId", currentMemberResidency.id+"");
        saveStateMap.put("currentHeadRelationshipId", currentMemberHeadRelationship.id+"");
        SavedEntityState entityState = new SavedEntityState(CoreFormEntity.OUTMIGRATION, outmigration.id, "outimgFormUtilState", new Gson().toJson(saveStateMap));
        this.boxSavedEntityStates.put(entityState);

        this.entity = outmigration;
        this.collectExtensionForm(collectedValues);
    }

    private void onModeEdit(CollectedDataMap collectedValues, XmlFormResult result) {

        //saveNewHousehold();
        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colMemberCode = collectedValues.get("memberCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colMemberName = collectedValues.get("memberName"); //not blank
        ColumnValue colMigrationType = collectedValues.get("migrationType");
        ColumnValue colOriginCode = collectedValues.get("originCode");
        //ColumnValue colDestinationCode = collectedValues.get("destinationCode");
        ColumnValue colDestinationOther = collectedValues.get("destinationOther");
        ColumnValue colMigrationDate = collectedValues.get("migrationDate"); //not null cannot be in the future nor before dob
        ColumnValue colMigrationReason = collectedValues.get("migrationReason");
        ColumnValue colMigrationReasonOther = collectedValues.get("migrationReasonOther");

        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String memberCode = colMemberCode.getValue();
        String memberName = colMemberName.getValue();
        OutMigrationType migrationType = OutMigrationType.getFrom(colMigrationType.getValue());
        String originCode = colOriginCode.getValue();
        String destinationOther = colDestinationOther.getValue();
        Date migrationDate = colMigrationDate.getDateValue();
        String migrationReason = colMigrationReason.getValue();
        String migrationReasonOther = colMigrationReasonOther.getValue();

        //update member, close residency, close headrelationship, create Outmigration

        //Outmigration
        Outmigration outmigration = boxOutmigrations.get(this.entity.id);
        outmigration.visitCode = visitCode;
        outmigration.migrationType = OutMigrationType.EXTERNAL;
        outmigration.originCode = this.household.code;
        outmigration.destinationOther = destinationOther;
        outmigration.migrationDate = migrationDate;
        outmigration.migrationReason = migrationReasonOther != null ?  migrationReasonOther : migrationReason;
        this.boxOutmigrations.put(outmigration);

        //Residency - update close
        currentMemberResidency = boxResidencies.get(currentMemberResidency.id);
        currentMemberResidency.endDate = migrationDate;
        currentMemberResidency.endType = ResidencyEndType.EXTERNAL_OUTMIGRATION;
        this.boxResidencies.put(currentMemberResidency);

        //HeadRelationship - update close
        currentMemberHeadRelationship = boxHeadRelationships.get(currentMemberHeadRelationship.id);
        currentMemberHeadRelationship.endDate = migrationDate;
        currentMemberHeadRelationship.endType = HeadRelationshipEndType.EXTERNAL_OUTMIGRATION;
        this.boxHeadRelationships.put(currentMemberHeadRelationship);

        //update member
        member = boxMembers.get(member.id);
        member.endType = ResidencyEndType.EXTERNAL_OUTMIGRATION;
        member.endDate = migrationDate;
        this.boxMembers.put(member);

        //save core collected data
        collectedData = boxCoreCollectedData.get(collectedData.id);
        collectedData.formEntityCode = member.code;
        collectedData.formEntityName = member.name;
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

        if (currentMode == Mode.CREATE) {
            currentMemberResidency = boxResidencies.query().equal(Residency_.memberCode, this.member.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                    .and().equal(Residency_.endType, ResidencyEndType.NOT_APPLICABLE.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                    .orderDesc(Residency_.startDate).build().findFirst();

            currentMemberHeadRelationship = boxHeadRelationships.query().equal(HeadRelationship_.memberCode, this.member.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                    .and().equal(HeadRelationship_.endType, HeadRelationshipEndType.NOT_APPLICABLE.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                    .orderDesc(HeadRelationship_.startDate).build().findFirst();

            if (currentMemberResidency == null) {
                DialogFactory.createMessageInfo(this.context, R.string.error_lbl, R.string.internal_inmigration_select_inmig_no_residency_lbl).show();
                return;
            }

            //check if Member is a head of household of another household and Check if is the last individual in the household
            if (isHeadOfHousehold(member.code, household.code) && !isTheOnlyHouseholdMember(member.code, household.code)) { //yet still
                DialogFactory.createMessageInfo(this.context, R.string.error_lbl, this.context.getString(R.string.outmigration_is_head_of_household_lbl)).show();
                return;
            }

            executeCollectForm();
        } else if (currentMode == Mode.EDIT) {
            executeCollectForm();
        }


    }


}
