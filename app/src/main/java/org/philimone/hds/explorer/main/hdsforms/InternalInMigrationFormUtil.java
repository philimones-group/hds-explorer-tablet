package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.MemberFilterDialog;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Death;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.HeadRelationship_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Inmigration;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Outmigration;
import org.philimone.hds.explorer.model.Residency;
import org.philimone.hds.explorer.model.Residency_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.HeadRelationshipType;
import org.philimone.hds.explorer.model.enums.HouseholdType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipEndType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipStartType;
import org.philimone.hds.explorer.model.enums.temporal.InMigrationType;
import org.philimone.hds.explorer.model.enums.temporal.OutMigrationType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyStartType;
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
import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

public class InternalInMigrationFormUtil extends FormUtil<Inmigration> {

    //private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<Residency> boxResidencies;
    private Box<HeadRelationship> boxHeadRelationships;
    private Box<Inmigration> boxInmigrations;
    private Box<Outmigration> boxOutmigrations;
    protected Box<Death> boxDeaths;

    //private Household household;
    private Visit visit;
    private Member selectedMember;
    private Residency selectedMemberResidency;
    private HeadRelationship selectedMemberHeadRelationship;
    private Member currentHead;
    private boolean isFirstHouseholdMember;
    private int minimunHeadAge;

    private Map<String, String> mapSavedStates = new HashMap<>();
    private Residency savedResidency;
    private HeadRelationship savedHeadRelationship;
    private Outmigration savedOutmigration;

    private DateUtil dateUtil = Bootstrap.getDateUtil();

    private final String PHONE_NUMBER_REGEX = "^(\\+?\\d{1,3})?[-.\\s]?\\(?\\d{2,4}\\)?[-.\\s]?\\d{3,5}[-.\\s]?\\d{4,6}$";

    public InternalInMigrationFormUtil(Fragment fragment, Context context, Visit visit, Household household, FormUtilities odkFormUtilities, FormUtilListener<Inmigration> listener){
        super(fragment, context, FormUtil.getInMigrationForm(context), odkFormUtilities, listener);

        //Log.d("enu-household", ""+household);

        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    public InternalInMigrationFormUtil(Fragment fragment, Context context, Visit visit, Household household, Inmigration inmigToEdit, FormUtilities odkFormUtilities, FormUtilListener<Inmigration> listener){
        super(fragment, context, FormUtil.getInMigrationForm(context), inmigToEdit, odkFormUtilities, listener);

        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();

        readSavedEntityState();
    }

    public static InternalInMigrationFormUtil newInstance(Mode openMode, Fragment fragment, Context context, Visit visit, Household household, Inmigration inmigToEdit, FormUtilities odkFormUtilities, FormUtilListener<Inmigration> listener){
        if (openMode == Mode.CREATE) {
            return new InternalInMigrationFormUtil(fragment, context, visit, household, odkFormUtilities, listener);
        } else if (openMode == Mode.EDIT) {
            return new InternalInMigrationFormUtil(fragment, context, visit, household, inmigToEdit, odkFormUtilities, listener);
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
        this.boxInmigrations = ObjectBoxDatabase.get().boxFor(Inmigration.class);
        this.boxOutmigrations = ObjectBoxDatabase.get().boxFor(Outmigration.class);

        this.boxDeaths = ObjectBoxDatabase.get().boxFor(Death.class);
    }

    @Override
    protected void initialize(){
        super.initialize();

        Log.d("init-household", ""+household);

        this.isFirstHouseholdMember = boxResidencies.query().equal(Residency_.householdCode, household.code, QueryBuilder.StringOrder.CASE_SENSITIVE).and().equal(Residency_.endType, ResidencyEndType.NOT_APPLICABLE.code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().count()==0; //find any resident
        this.minimunHeadAge = retrieveMinimumHeadAge();
    }

    private void readSavedEntityState() {
        SavedEntityState savedState = this.boxSavedEntityStates.query(SavedEntityState_.formEntity.equal(CoreFormEntity.INMIGRATION.code)
                .and(SavedEntityState_.collectedId.equal(this.entity.id))
                .and(SavedEntityState_.objectKey.equal("intimgFormUtilState"))).build().findFirst();

        if (savedState != null) {
            HashMap map = new Gson().fromJson(savedState.objectGsonValue, HashMap.class);
            for (Object key : map.keySet()) {
                mapSavedStates.put(key.toString(), map.get(key).toString());
            }
        }

        String strResidencyId = mapSavedStates.get("residencyId");
        String strHeadRelationshipId = mapSavedStates.get("headRelationshipId");
        String strOutmigrationId = mapSavedStates.get("outmigrationId");

        if (!StringUtil.isBlank(strResidencyId)) {
            this.savedResidency = this.boxResidencies.get(Long.parseLong(strResidencyId));
        }
        if (!StringUtil.isBlank(strHeadRelationshipId)) {
            this.savedHeadRelationship = this.boxHeadRelationships.get(Long.parseLong(strHeadRelationshipId));
        }
        if (!StringUtil.isBlank(strOutmigrationId)) {
            this.savedOutmigration = this.boxOutmigrations.get(Long.parseLong(strOutmigrationId));
        }

    }

    @Override
    protected void preloadValues() {
        //member_details_unknown_lbl

        if (this.selectedMember != null){
            preloadedMap.put("visitCode", this.visit.code);
            preloadedMap.put("memberCode", this.selectedMember.code);
            preloadedMap.put("memberName", this.selectedMember.name);
            //preloadedMap.put("headRelationshipType", "");
            preloadedMap.put("education", this.selectedMember.education);
            preloadedMap.put("religion", this.selectedMember.religion);
            preloadedMap.put("hasPhoneNumbers", this.selectedMember.hasPhoneNumbers()+"");
            if (this.selectedMember.hasPhoneNumbers()) {
                preloadedMap.put("phonePrimary", this.selectedMember.phonePrimary);
                preloadedMap.put("phoneAlternative", this.selectedMember.phoneAlternative);
            }
            preloadedMap.put("migrationType", InMigrationType.INTERNAL.code);
            preloadedMap.put("originCode", this.selectedMemberResidency.householdCode);
            preloadedMap.put("destinationCode", household.code);
        }

        preloadedMap.put("modules", this.user.getSelectedModulesCodes());

        if (isFirstHouseholdMember) {
            preloadedMap.put("headRelationshipType", "HOH");
        }

    }

    @Override
    protected void preloadUpdatedValues() {

    }

    @Override
    public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colMemberCode = collectedValues.get("memberCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colMemberName = collectedValues.get("memberName"); //not blank
        ColumnValue colHeadRelationshipType = collectedValues.get("headRelationshipType"); //not blank
        ColumnValue colMigrationType = collectedValues.get("migrationType");
        ColumnValue colOriginCode = collectedValues.get("originCode");
        ColumnValue colDestinationCode = collectedValues.get("destinationCode");
        ColumnValue colMigrationDate = collectedValues.get("migrationDate"); //not null cannot be in the future nor before dob
        ColumnValue colMigrationReason = collectedValues.get("migrationReason");
        ColumnValue colMigrationReasonOther = collectedValues.get("migrationReasonOther");
        ColumnValue colHasPhoneNumbers = collectedValues.get("hasPhoneNumbers");
        ColumnValue colPhonePrimary = collectedValues.get("phonePrimary"); //not blank
        ColumnValue colPhoneAlternative = collectedValues.get("phoneAlternative"); //not blank

        //ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        //ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        //ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String memberCode = colMemberCode.getValue();
        String memberName = colMemberName.getValue();
        HeadRelationshipType headRelationshipType = HeadRelationshipType.getFrom(colHeadRelationshipType.getValue());
        InMigrationType migrationType = InMigrationType.getFrom(colMigrationType.getValue());
        String originCode = colOriginCode.getValue();
        String destinationCode = colDestinationCode.getValue();
        Date migrationDate = colMigrationDate.getDateValue();
        Boolean hasPhoneNumbers = StringUtil.getBooleanValue(colHasPhoneNumbers.getValue());
        String phonePrimary = colPhonePrimary.getValue();
        String phoneAlternative = colPhoneAlternative.getValue();

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

        if (headRelationshipType == null && !isInstitutionalHousehold()){
            String message = this.context.getString(R.string.new_member_head_relattype_empty_lbl);
            return new ValidationResult(colHeadRelationshipType, message);
        }

        if (migrationType == null){
            String message = this.context.getString(R.string.external_inmigration_migration_type_empty_lbl);
            return new ValidationResult(colMigrationType, message);
        }

        //dob age must be greater or equals to head min age
        if (headRelationshipType == HeadRelationshipType.HEAD_OF_HOUSEHOLD) { //head
            int age = GeneralUtil.getAge(this.selectedMember.dob);
            if (age < minimunHeadAge){
                String message = this.context.getString(R.string.new_member_dob_minimum_age_head_lbl, minimunHeadAge+"");
                return new ValidationResult(colHeadRelationshipType, message);
            }
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
        if (migrationDate.before(this.selectedMember.dob)){ //is before dob
            String message = this.context.getString(R.string.external_inmigration_migrationdate_not_before_dob_lbl);
            return new ValidationResult(colMigrationDate, message);
        }

        //check destinationCode, originCode existence - both a selected automatically
        //check Member Death Status - if returning to DSS
        if (Queries.getDeathByCode(this.boxDeaths, memberCode)!=null){
            String message = this.context.getString(R.string.external_inmigration_death_exists_lbl);
            return new ValidationResult(colMemberCode, message);
        }

        //check if Member is a head of household of another household
        if (isHeadOfHouseholdSomewhere(memberCode) && !isTheOnlyHouseholdMember(memberCode, originCode)) {
            String message = this.context.getString(R.string.internal_inmigration_is_head_of_household_lbl);
            return new ValidationResult(colMemberCode, message);
        }

        //check last residency and head relationship
        Residency lastResidency = getLastResidency(memberCode);
        HeadRelationship lastRelationship = getLastHeadRelationship(memberCode);

        //Ensure that you are testing the last Residency and Head Relationship
        lastResidency = currentMode==Mode.EDIT ? getLastResidency(memberCode, lastResidency) : lastResidency; //when editing the lastResidency is the last created by this event
        lastRelationship = currentMode==Mode.EDIT ? getLastHeadRelationship(memberCode, lastRelationship) : lastRelationship; //when editing the lastRelationship is the last created by this event

        //check dates - for new residency and new head_relationship
        //migrationDate vs lastResidency.startDate --- the endDate is null right now
        if (lastResidency != null && lastResidency.startDate != null && migrationDate.before(lastResidency.startDate)){ //is before lastres.startdate
            String message = this.context.getString(R.string.internal_inmigration_migrationdate_not_before_residency_startdate_lbl, dateUtil.formatYMD(migrationDate), lastResidency.startType.code, dateUtil.formatYMD(lastResidency.startDate));
            return new ValidationResult(colMigrationDate, message);
        }
        //migrationDate vs lastRelationship.endDate
        if (lastRelationship != null && lastRelationship.startDate != null && migrationDate.before(lastRelationship.startDate)){ //is before lasthead.startdate
            String message = this.context.getString(R.string.internal_inmigration_migrationdate_not_before_hrelationship_startdate_lbl, dateUtil.formatYMD(migrationDate), lastRelationship.startType.code, dateUtil.formatYMD(lastRelationship.startDate));
            return new ValidationResult(colMigrationDate, message);
        }

        //validate phone numbers
        if (hasPhoneNumbers) {

            if (!isValidPhoneNumber(phonePrimary)) {
                String message = this.context.getString(R.string.new_member_phone_primary_invalid_lbl);
                return new ValidationResult(colPhonePrimary, message);
            }

            if (!StringUtil.isBlank(phoneAlternative) && !isValidPhoneNumber(phoneAlternative)) {
                String message = this.context.getString(R.string.new_member_phone_alternative_invalid_lbl);
                return new ValidationResult(colPhoneAlternative, message);
            }
        }

        return ValidationResult.noErrors();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return !StringUtil.isBlank(phoneNumber) && phoneNumber.matches(PHONE_NUMBER_REGEX);
    }

    private HeadRelationship getLastHeadRelationship(String memberCode) {
        if (StringUtil.isBlank(memberCode)) return null;

        HeadRelationship lastHeadRelationship = this.boxHeadRelationships.query(HeadRelationship_.memberCode.equal(memberCode))
                .orderDesc(HeadRelationship_.startDate)
                .build().findFirst();

        return lastHeadRelationship;
    }

    private HeadRelationship getLastHeadRelationship(String memberCode, HeadRelationship excludeHeadRelationship) {
        if (StringUtil.isBlank(memberCode)) return null;

        HeadRelationship lastHeadRelationship = this.boxHeadRelationships.query(HeadRelationship_.memberCode.equal(memberCode).and(HeadRelationship_.id.notEqual(excludeHeadRelationship.id)))
                .orderDesc(HeadRelationship_.startDate)
                .build().findFirst();

        return lastHeadRelationship;
    }

    private Residency getLastResidency(String memberCode) {
        if (StringUtil.isBlank(memberCode)) return null;

        Residency lastResidency = this.boxResidencies.query(Residency_.memberCode.equal(memberCode))
                .orderDesc(Residency_.startDate)
                .build().findFirst();

        return lastResidency;
    }

    private Residency getLastResidency(String memberCode, Residency excludeResidency) {
        if (StringUtil.isBlank(memberCode)) return null;

        Residency lastResidency = this.boxResidencies.query(Residency_.memberCode.equal(memberCode).and(Residency_.id.notEqual(excludeResidency.id)))
                .orderDesc(Residency_.startDate)
                .build().findFirst();

        return lastResidency;
    }

    private boolean isHeadOfHouseholdSomewhere(String memberCode) {
        long count = this.boxHeadRelationships.query(
                HeadRelationship_.memberCode.equal(memberCode).and(HeadRelationship_.relationshipType.equal(HeadRelationshipType.HEAD_OF_HOUSEHOLD.code))
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

    private boolean isInstitutionalHousehold() {
        return this.household.type == HouseholdType.INSTITUTIONAL;
    }

    @Override
    public void onBeforeFormFinished(HForm form, CollectedDataMap collectedValues) {
        //using it to update collectedHouseholdId, collectedMemberId
        ColumnValue colHouseholdId = collectedValues.get("collectedHouseholdId");
        ColumnValue colMemberId = collectedValues.get("collectedMemberId");

        colHouseholdId.setValue(this.household.collectedId);
        colMemberId.setValue(this.selectedMember.collectedId);
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
        ColumnValue colHeadRelationshipType = collectedValues.get("headRelationshipType"); //not blank
        ColumnValue colMigrationType = collectedValues.get("migrationType");
        ColumnValue colOriginCode = collectedValues.get("originCode");
        ColumnValue colDestinationCode = collectedValues.get("destinationCode");
        ColumnValue colMigrationDate = collectedValues.get("migrationDate"); //not null cannot be in the future nor before dob
        ColumnValue colMigrationReason = collectedValues.get("migrationReason");
        ColumnValue colMigrationReasonOther = collectedValues.get("migrationReasonOther");
        ColumnValue colEducation = collectedValues.get("education");
        ColumnValue colReligion = collectedValues.get("religion");
        ColumnValue colHasPhoneNumbers = collectedValues.get("hasPhoneNumbers");
        ColumnValue colPhonePrimary = collectedValues.get("phonePrimary"); //not blank
        ColumnValue colPhoneAlternative = collectedValues.get("phoneAlternative"); //not blank
        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String memberCode = colMemberCode.getValue();
        String memberName = colMemberName.getValue();
        HeadRelationshipType headRelationshipType = HeadRelationshipType.getFrom(colHeadRelationshipType.getValue());
        InMigrationType migrationType = InMigrationType.getFrom(colMigrationType.getValue());
        String originCode = colOriginCode.getValue();
        String destinationCode = colDestinationCode.getValue();
        Date migrationDate = colMigrationDate.getDateValue();
        String migrationReason = colMigrationReason.getValue();
        String migrationReasonOther = colMigrationReasonOther.getValue();
        String education = colEducation.getValue();
        String religion = colReligion.getValue();
        Boolean hasPhoneNumbers = StringUtil.getBooleanValue(colHasPhoneNumbers.getValue());
        String phonePrimary = colPhonePrimary.getValue();
        String phoneAlternative = colPhoneAlternative.getValue();

        //update member, create/update residency, headrelationship, create inmigration

        Inmigration inmigration = new Inmigration();
        inmigration.visitCode = visitCode;
        inmigration.memberCode = memberCode;
        inmigration.type = migrationType;
        inmigration.extMigType = null;
        inmigration.originCode = this.selectedMemberResidency.householdCode;
        inmigration.destinationCode = this.household.code;
        inmigration.migrationDate = migrationDate;
        inmigration.migrationReason = migrationReasonOther != null ?  migrationReasonOther : migrationReason;
        inmigration.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        inmigration.recentlyCreated = true;
        inmigration.recentlyCreatedUri = result.getFilename();
        boxInmigrations.put(inmigration);

        //Outmigration
        Outmigration outmigration = new Outmigration();
        outmigration.visitCode = visitCode;
        outmigration.migrationType = OutMigrationType.INTERNAL;
        outmigration.originCode = this.selectedMemberResidency.householdCode;
        outmigration.destinationCode = household.code;
        outmigration.migrationDate = GeneralUtil.getDateAdd(migrationDate, -1);
        //outmigration.recentlyCreated = true;
        //outmigration.recentlyCreatedUri = null;
        this.boxOutmigrations.put(outmigration);

        //Residency - close
        selectedMemberResidency = boxResidencies.get(selectedMemberResidency.id);
        selectedMemberResidency.endDate = GeneralUtil.getDateAdd(migrationDate, -1);
        selectedMemberResidency.endType = ResidencyEndType.INTERNAL_OUTMIGRATION;
        this.boxResidencies.put(selectedMemberResidency);

        //Residency - new
        Residency residency = new Residency();
        residency.householdCode = household.code;
        residency.memberCode = memberCode;
        residency.startType = ResidencyStartType.INTERNAL_INMIGRATION;
        residency.startDate = migrationDate;
        residency.endType = ResidencyEndType.NOT_APPLICABLE;
        residency.endDate = null;
        this.boxResidencies.put(residency);

        //HeadRelationship - close
        if (selectedMemberHeadRelationship != null) {
            selectedMemberHeadRelationship = boxHeadRelationships.get(selectedMemberHeadRelationship.id);
            selectedMemberHeadRelationship.endDate = GeneralUtil.getDateAdd(migrationDate, -1);
            selectedMemberHeadRelationship.endType = HeadRelationshipEndType.INTERNAL_OUTMIGRATION;
            this.boxHeadRelationships.put(selectedMemberHeadRelationship);
        }

        //HeadRelationship - new
        HeadRelationship headRelationship = null;
        if (!isInstitutionalHousehold()) {
            headRelationship = new HeadRelationship();
            headRelationship.householdCode = household.code;
            headRelationship.memberCode = memberCode;
            headRelationship.headCode = (headRelationshipType == HeadRelationshipType.HEAD_OF_HOUSEHOLD) ? memberCode : currentHead.code;
            headRelationship.relationshipType = headRelationshipType;
            headRelationship.startType = HeadRelationshipStartType.INTERNAL_INMIGRATION;
            headRelationship.startDate = migrationDate;
            headRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
            headRelationship.endDate = null;
            this.boxHeadRelationships.put(headRelationship);
        }

        //update member
        //selectedMember = boxMembers.get(selectedMember.id);
        selectedMember.householdCode = household.code;
        selectedMember.householdName = household.name;
        selectedMember.startType = ResidencyStartType.INTERNAL_INMIGRATION;
        selectedMember.startDate = migrationDate;
        selectedMember.endType = ResidencyEndType.NOT_APPLICABLE;
        selectedMember.endDate = null;
        selectedMember.education = education;
        selectedMember.religion = religion;
        selectedMember.phonePrimary = phonePrimary;
        selectedMember.phoneAlternative = phoneAlternative;
        selectedMember.headRelationshipType = headRelationshipType;
        selectedMember.gpsNull = household.gpsNull;
        selectedMember.gpsAccuracy = household.gpsAccuracy;
        selectedMember.gpsAltitude = household.gpsAltitude;
        selectedMember.gpsLatitude = household.gpsLatitude;
        selectedMember.gpsLongitude = household.gpsLongitude;
        selectedMember.cosLatitude = household.cosLatitude;
        selectedMember.sinLatitude = household.sinLatitude;
        selectedMember.cosLongitude = household.cosLongitude;
        selectedMember.sinLongitude = household.sinLongitude;
        this.boxMembers.put(selectedMember);

        //save core collected data
        collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.INMIGRATION;
        collectedData.formEntityId = inmigration.id;
        collectedData.formEntityCode = selectedMember.code;
        collectedData.formEntityName = selectedMember.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        collectedData.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        collectedData.extension.setTarget(this.getFormExtension(collectedData.formEntity));
        this.boxCoreCollectedData.put(collectedData);

        if (selectedMember.isHouseholdHead()) { //head of household
            household = boxHouseholds.get(household.id);
            household.headCode = selectedMember.code;
            household.headName = selectedMember.name;
            boxHouseholds.put(household);
        }

        //save state for editing
        HashMap<String,String> saveStateMap = new HashMap<>();
        saveStateMap.put("residencyId", residency.id+"");
        if (headRelationship != null) {
            saveStateMap.put("headRelationshipId", headRelationship.id + "");
        }
        saveStateMap.put("outmigrationId", outmigration.id+"");
        SavedEntityState entityState = new SavedEntityState(CoreFormEntity.INMIGRATION, inmigration.id, "intimgFormUtilState", new Gson().toJson(saveStateMap));
        this.boxSavedEntityStates.put(entityState);

        this.entity = inmigration;
        this.collectExtensionForm(collectedValues);
    }

    private void onModeEdit(CollectedDataMap collectedValues, XmlFormResult result) {

        //saveNewHousehold();
        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colMemberCode = collectedValues.get("memberCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colMemberName = collectedValues.get("memberName"); //not blank
        ColumnValue colHeadRelationshipType = collectedValues.get("headRelationshipType"); //not blank
        ColumnValue colMigrationType = collectedValues.get("migrationType");
        ColumnValue colOriginCode = collectedValues.get("originCode");
        ColumnValue colDestinationCode = collectedValues.get("destinationCode");
        ColumnValue colMigrationDate = collectedValues.get("migrationDate"); //not null cannot be in the future nor before dob
        ColumnValue colMigrationReason = collectedValues.get("migrationReason");
        ColumnValue colMigrationReasonOther = collectedValues.get("migrationReasonOther");
        ColumnValue colEducation = collectedValues.get("education");
        ColumnValue colReligion = collectedValues.get("religion");
        ColumnValue colHasPhoneNumbers = collectedValues.get("hasPhoneNumbers");
        ColumnValue colPhonePrimary = collectedValues.get("phonePrimary"); //not blank
        ColumnValue colPhoneAlternative = collectedValues.get("phoneAlternative"); //not blank
        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String memberCode = colMemberCode.getValue();
        String memberName = colMemberName.getValue();
        HeadRelationshipType headRelationshipType = HeadRelationshipType.getFrom(colHeadRelationshipType.getValue());
        InMigrationType migrationType = InMigrationType.getFrom(colMigrationType.getValue());
        String originCode = colOriginCode.getValue();
        String destinationCode = colDestinationCode.getValue();
        Date migrationDate = colMigrationDate.getDateValue();
        String migrationReason = colMigrationReason.getValue();
        String migrationReasonOther = colMigrationReasonOther.getValue();
        String education = colEducation.getValue();
        String religion = colReligion.getValue();
        Boolean hasPhoneNumbers = StringUtil.getBooleanValue(colHasPhoneNumbers.getValue());
        String phonePrimary = colPhonePrimary.getValue();
        String phoneAlternative = colPhoneAlternative.getValue();

        //update member, create/update residency, headrelationship, create inmigration

        Inmigration inmigration = this.entity;
        inmigration.visitCode = visitCode;
        inmigration.memberCode = memberCode;
        inmigration.type = migrationType;
        inmigration.extMigType = null;
        inmigration.originCode = this.selectedMemberResidency.householdCode;
        inmigration.destinationCode = this.household.code;
        inmigration.migrationDate = migrationDate;
        inmigration.migrationReason = migrationReasonOther != null ?  migrationReasonOther : migrationReason;
        boxInmigrations.put(inmigration);

        //Outmigration - update
        Outmigration outmigration = boxOutmigrations.get(savedOutmigration.id);
        outmigration.migrationDate = GeneralUtil.getDateAdd(migrationDate, -1);
        this.boxOutmigrations.put(outmigration);

        //Residency - update close
        selectedMemberResidency = boxResidencies.get(selectedMemberResidency.id);
        selectedMemberResidency.endDate = GeneralUtil.getDateAdd(migrationDate, -1);
        selectedMemberResidency.endType = ResidencyEndType.INTERNAL_OUTMIGRATION;
        this.boxResidencies.put(selectedMemberResidency);

        //Residency - update
        Residency residency = savedResidency;
        residency.householdCode = household.code;
        residency.memberCode = memberCode;
        residency.startType = ResidencyStartType.INTERNAL_INMIGRATION;
        residency.startDate = migrationDate;
        residency.endType = ResidencyEndType.NOT_APPLICABLE;
        residency.endDate = null;
        this.boxResidencies.put(residency);

        //HeadRelationship - update close
        if (selectedMemberHeadRelationship != null) {
            selectedMemberHeadRelationship = boxHeadRelationships.get(selectedMemberHeadRelationship.id);
            selectedMemberHeadRelationship.endDate = GeneralUtil.getDateAdd(migrationDate, -1);
            selectedMemberHeadRelationship.endType = HeadRelationshipEndType.INTERNAL_OUTMIGRATION;
            this.boxHeadRelationships.put(selectedMemberHeadRelationship);
        }

        //HeadRelationship - update
        HeadRelationship headRelationship = null;
        if (!isInstitutionalHousehold()) {
            headRelationship = savedHeadRelationship;
            headRelationship.householdCode = household.code;
            headRelationship.memberCode = memberCode;
            headRelationship.headCode = (headRelationshipType == HeadRelationshipType.HEAD_OF_HOUSEHOLD) ? memberCode : currentHead.code;
            headRelationship.relationshipType = headRelationshipType;
            headRelationship.startType = HeadRelationshipStartType.INTERNAL_INMIGRATION;
            headRelationship.startDate = migrationDate;
            headRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
            headRelationship.endDate = null;
            this.boxHeadRelationships.put(headRelationship);
        }

        //update member
        selectedMember.householdCode = household.code;
        selectedMember.householdName = household.name;
        selectedMember.startType = ResidencyStartType.INTERNAL_INMIGRATION;
        selectedMember.startDate = migrationDate;
        selectedMember.endType = ResidencyEndType.NOT_APPLICABLE;
        selectedMember.endDate = null;
        selectedMember.headRelationshipType = headRelationshipType;
        selectedMember.education = education;
        selectedMember.religion = religion;
        selectedMember.phonePrimary = phonePrimary;
        selectedMember.phoneAlternative = phoneAlternative;
        this.boxMembers.put(selectedMember);

        //save core collected data
        collectedData.formEntityCode = selectedMember.code;
        collectedData.formEntityName = selectedMember.name;
        collectedData.updatedDate = new Date();
        this.boxCoreCollectedData.put(collectedData);

        if (selectedMember.isHouseholdHead()) { //head of household
            household.headCode = selectedMember.code;
            household.headName = selectedMember.name;
            boxHouseholds.put(household);
        }

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
        //filterFather, Mother
        //Is the Father/Mother of this Member known and exists on DSS?

        if (currentMode == Mode.CREATE) {
            checkHeadOfHouseholdDialog();
        } else if (currentMode == Mode.EDIT) {
            selectedMember = this.boxMembers.query(Member_.code.equal(this.entity.memberCode)).build().findFirst();
            retrieveHeadOfHousehold();
            openInmigrationForm();
        }

    }

    private int retrieveMinimumHeadAge() {
        ApplicationParam param = this.boxAppParams.query().equal(ApplicationParam_.name, ApplicationParam.PARAMS_MIN_AGE_OF_HEAD, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (param != null) {
            try {
                return Integer.parseInt(param.value);
            } catch (Exception ex) {

            }
        }

        return 12;
    }

    private void retrieveHeadOfHousehold() {
        this.currentHead = getHeadOfHousehold();
    }

    private Member getHeadOfHousehold() {

        HeadRelationship headRelationship = boxHeadRelationships.query(
                HeadRelationship_.householdCode.equal(household.code)
                        .and(HeadRelationship_.relationshipType.equal(HeadRelationshipType.HEAD_OF_HOUSEHOLD.code))
                        .and(HeadRelationship_.endType.equal(HeadRelationshipEndType.NOT_APPLICABLE.code))
        ).orderDesc(HeadRelationship_.startDate).build().findFirst();

        if (headRelationship != null) {
            return Queries.getMemberByCode(boxMembers, headRelationship.memberCode);
        }

        return null;
    }

    private void checkHeadOfHouseholdDialog() {
        retrieveHeadOfHousehold();

        if (this.currentHead != null || isFirstHouseholdMember || isInstitutionalHousehold()) {
            openInMigratingMemberFilterDialog();
        } else {
            //display dialog
            DialogFactory.createMessageInfo(this.context, R.string.eventType_internal_inmigration, R.string.household_head_dont_exists_lbl).show();
        }
    }

    private void openInMigratingMemberFilterDialog(){

        MemberFilterDialog dialog = MemberFilterDialog.newInstance(this.fragmentManager, context.getString(R.string.internal_inmigration_select_inmig_member_lbl), true, new MemberFilterDialog.Listener() {
            @Override
            public void onSelectedMember(Member member) {
                Log.d("selected-member", ""+member.getCode());
                selectedMember = member;
                openInmigrationForm();
            }

            @Override
            public void onCanceled() {

            }
        });

        dialog.setFilterHouseCode(household.getCode());
        dialog.setFastFilterHousehold(household);
        dialog.setFilterExcludeHousehold(household.code);
        dialog.setFilterStatus(MemberFilterDialog.StatusFilter.RESIDENT, true);
        dialog.setStartSearchOnShow(true);
        dialog.show();
    }

    private void openInmigrationForm(){
        selectedMemberResidency = boxResidencies.query().equal(Residency_.memberCode, this.selectedMember.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .and().equal(Residency_.endType, ResidencyEndType.NOT_APPLICABLE.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .orderDesc(Residency_.startDate).build().findFirst();

        selectedMemberHeadRelationship = boxHeadRelationships.query().equal(HeadRelationship_.memberCode, this.selectedMember.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                                     .and().equal(HeadRelationship_.endType, HeadRelationshipEndType.NOT_APPLICABLE.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                                     .orderDesc(HeadRelationship_.startDate).build().findFirst();

        if (selectedMemberResidency == null){
            DialogFactory.createMessageInfo(this.context, R.string.error_lbl, R.string.internal_inmigration_select_inmig_no_residency_lbl).show();
            return;
        }

        executeCollectForm();
    }

    String handleMethodExecution(String methodExpression, String[] args) {
        Log.d("methodcall", ""+methodExpression);

        if (methodExpression.startsWith("isInstitutionalHousehold")){
            return "'" + (this.household.type == HouseholdType.INSTITUTIONAL) + "'";
        }

        if (methodExpression.startsWith("getMemberAge")){
            //get current member age
            int age = -1;
            if (this.selectedMember != null) {
                age = GeneralUtil.getAge(this.selectedMember.dob);
            }
            Log.d("noargs", "age="+age);

            return "'"+age+"'";

        }

        return "'CALC ERROR!!!'";
    }

}
