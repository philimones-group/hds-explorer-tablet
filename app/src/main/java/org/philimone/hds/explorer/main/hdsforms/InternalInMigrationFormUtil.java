package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.FragmentManager;

import org.philimone.hds.explorer.R;
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
import org.philimone.hds.explorer.model.Outmigration;
import org.philimone.hds.explorer.model.Residency;
import org.philimone.hds.explorer.model.Residency_;
import org.philimone.hds.explorer.model.Round;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.HeadRelationshipType;
import org.philimone.hds.explorer.model.enums.temporal.ExternalInMigrationType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipEndType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipStartType;
import org.philimone.hds.explorer.model.enums.temporal.InMigrationType;
import org.philimone.hds.explorer.model.enums.temporal.OutMigrationType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyStartType;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import java.util.Date;
import java.util.Map;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

public class InternalInMigrationFormUtil extends FormUtil<Inmigration> {

    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<Residency> boxResidencies;
    private Box<HeadRelationship> boxHeadRelationships;
    private Box<Inmigration> boxInmigrations;
    private Box<Outmigration> boxOutmigrations;
    private Box<CoreCollectedData> boxCoreCollectedData;
    protected Box<Death> boxDeaths;

    private Household household;
    private Visit visit;
    private Member selectedMember;
    private Residency selectedMemberResidency;
    private HeadRelationship selectedMemberHeadRelationship;
    private boolean isFirstHouseholdMember;
    private int minimunHeadAge;

    public InternalInMigrationFormUtil(FragmentManager fragmentManager, Context context, Visit visit, Household household, FormUtilListener<Inmigration> listener){
        super(fragmentManager, context, FormUtil.getInMigrationForm(context), listener);

        //Log.d("enu-household", ""+household);

        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    public InternalInMigrationFormUtil(FragmentManager fragmentManager, Context context, Visit visit, Household household, Inmigration inmigToEdit, FormUtilListener<Inmigration> listener){
        super(fragmentManager, context, FormUtil.getInMigrationForm(context), inmigToEdit, listener);

        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
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

    @Override
    protected void preloadValues() {
        //member_details_unknown_lbl

        if (this.selectedMember != null){
            preloadedMap.put("visitCode", this.visit.code);
            preloadedMap.put("memberCode", this.selectedMember.code);
            preloadedMap.put("memberName", this.selectedMember.name);
            //preloadedMap.put("headRelationshipType", "");
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

        if (headRelationshipType == null){
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
        if (isHeadOfHouseholdSomewhere(memberCode)) {
            String message = this.context.getString(R.string.internal_inmigration_is_head_of_household_lbl);
            return new ValidationResult(colMemberCode, message);
        }


        return ValidationResult.noErrors();
    }

    private boolean isHeadOfHouseholdSomewhere(String memberCode) {
        long count = this.boxHeadRelationships.query(
                HeadRelationship_.memberCode.equal(memberCode).and(HeadRelationship_.relationshipType.equal(HeadRelationshipType.HEAD_OF_HOUSEHOLD.code))
                .and(HeadRelationship_.endType.equal(HeadRelationshipEndType.NOT_APPLICABLE.code)))
                .build().count();

        return count > 0;
    }

    @Override
    public void onFormFinished(HForm form, CollectedDataMap collectedValues, XmlFormResult result) {

        Log.d("resultxml", result.getXmlResult());

        if (currentMode == Mode.EDIT) {
            System.out.println("Editing InMigration Not implemented yet");
            assert 1==0;
        }


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
        selectedMemberHeadRelationship.endDate = GeneralUtil.getDateAdd(migrationDate, -1);
        selectedMemberHeadRelationship.endType = HeadRelationshipEndType.INTERNAL_OUTMIGRATION;
        this.boxHeadRelationships.put(selectedMemberHeadRelationship);

        //HeadRelationship - new
        HeadRelationship headRelationship = new HeadRelationship();
        headRelationship.householdCode = household.code;
        headRelationship.memberCode = memberCode;
        headRelationship.relationshipType = headRelationshipType;
        headRelationship.startType = HeadRelationshipStartType.INTERNAL_INMIGRATION;
        headRelationship.startDate = migrationDate;
        headRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
        headRelationship.endDate = null;
        this.boxHeadRelationships.put(headRelationship);

        //update member
        selectedMember.householdCode = household.code;
        selectedMember.householdName = household.name;
        selectedMember.startType = ResidencyStartType.INTERNAL_INMIGRATION;
        selectedMember.startDate = migrationDate;
        selectedMember.endType = ResidencyEndType.NOT_APPLICABLE;
        selectedMember.endDate = null;
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
        collectedData.formEntityId = selectedMember.id;
        collectedData.formEntityCode = selectedMember.code;
        collectedData.formEntityName = selectedMember.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        collectedData.extension.setTarget(this.getFormExtension(collectedData.formEntity));
        this.boxCoreCollectedData.put(collectedData);

        if (isFirstHouseholdMember) { //head of household
            household.headCode = selectedMember.code;
            household.headName = selectedMember.name;
            boxHouseholds.put(household);
        }

        if (listener != null) {
            listener.onNewEntityCreated(inmigration);
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
        //filterFather, Mother
        //Is the Father/Mother of this Member known and exists on DSS?

        if (currentMode == Mode.CREATE) {
            openInMigratingMemberFilterDialog();
        } else if (currentMode == Mode.EDIT) {
            executeCollectForm();
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

}
