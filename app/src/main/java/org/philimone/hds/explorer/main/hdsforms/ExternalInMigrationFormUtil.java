package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;
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
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Residency;
import org.philimone.hds.explorer.model.Residency_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.converters.StringCollectionConverter;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.Gender;
import org.philimone.hds.explorer.model.enums.HeadRelationshipType;
import org.philimone.hds.explorer.model.enums.MaritalStatus;
import org.philimone.hds.explorer.model.enums.temporal.ExternalInMigrationType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipEndType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipStartType;
import org.philimone.hds.explorer.model.enums.temporal.InMigrationType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyStartType;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import java.util.Date;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

public class ExternalInMigrationFormUtil extends FormUtil<Member> {

    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<Residency> boxResidencies;
    private Box<HeadRelationship> boxHeadRelationships;
    private Box<CoreCollectedData> boxCoreCollectedData;
    private Box<Death> boxDeaths;

    private Household household;
    private Visit visit;
    private Member father;
    private Member mother;
    private Member returningMember;
    private ExternalInMigrationType externalInMigrationType = ExternalInMigrationType.ENTRY; //default is first entry
    private boolean isFirstHouseholdMember;
    private int minimunHeadAge;
    private int minimunFatherAge;
    private int minimunMotherAge;

    public ExternalInMigrationFormUtil(Fragment fragment, Context context, Visit visit, Household household, FormUtilities odkFormUtilities, FormUtilListener<Member> listener){
        super(fragment, context, FormUtil.getExternalInMigrationForm(context), odkFormUtilities, listener);

        //Log.d("enu-household", ""+household);

        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    public ExternalInMigrationFormUtil(Fragment fragment, Context context, Visit visit, Household household, Member memberToEdit, FormUtilities odkFormUtilities, FormUtilListener<Member> listener){
        super(fragment, context, FormUtil.getExternalInMigrationForm(context), memberToEdit, odkFormUtilities, listener);

        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();

        this.father = boxMembers.query().equal(Member_.code, memberToEdit.fatherCode, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
        this.mother = boxMembers.query().equal(Member_.code, memberToEdit.motherCode, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxResidencies = ObjectBoxDatabase.get().boxFor(Residency.class);
        this.boxHeadRelationships = ObjectBoxDatabase.get().boxFor(HeadRelationship.class);

        this.boxDeaths = ObjectBoxDatabase.get().boxFor(Death.class);
    }

    @Override
    protected void initialize(){
        super.initialize();

        Log.d("init-household", ""+household);

        this.isFirstHouseholdMember = boxResidencies.query().equal(Residency_.householdCode, household.code, QueryBuilder.StringOrder.CASE_SENSITIVE).and().equal(Residency_.endType, ResidencyEndType.NOT_APPLICABLE.code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().count()==0; //find any resident
        this.minimunHeadAge = retrieveMinimumHeadAge();
        this.minimunFatherAge = retrieveMinimumFatherAge();
        this.minimunMotherAge = retrieveMinimumMotherAge();
    }

    @Override
    protected void preloadValues() {
        //member_details_unknown_lbl

        String memberCode = codeGenerator.generateMemberCode(household);

        preloadedMap.put("visitCode", this.visit.code);
        preloadedMap.put("memberCode", memberCode);
        preloadedMap.put("motherCode", mother.code);
        preloadedMap.put("motherName", mother.isUnknownIndividual() ? context.getString(R.string.member_details_unknown_lbl) : mother.name);
        preloadedMap.put("fatherCode", father.code);
        preloadedMap.put("fatherName", father.isUnknownIndividual() ? context.getString(R.string.member_details_unknown_lbl) : father.name);
        preloadedMap.put("migrationType", InMigrationType.EXTERNAL.code);
        preloadedMap.put("extMigrationType", externalInMigrationType.code); //Must be selected

        if (externalInMigrationType==ExternalInMigrationType.REENTRY){
            preloadedMap.put("memberCode", returningMember.code);
            preloadedMap.put("memberName", returningMember.name);
            preloadedMap.put("memberGender", returningMember.gender.code);
            preloadedMap.put("memberDob", StringUtil.formatYMD(returningMember.dob));
            preloadedMap.put("originCode", returningMember.householdCode); //but I dont think we need this since the member is coming from outside the dss
        }

        preloadedMap.put("destinationCode", household.code);
        preloadedMap.put("modules", this.user.getSelectedModulesCodes());

        if (isFirstHouseholdMember) {
            preloadedMap.put("headRelationshipType", "HOH");
        }

    }

    @Override
    protected void preloadUpdatedValues() {
        //only father and mother can be updated using dialogs
        preloadedMap.put("motherCode", mother.code);
        preloadedMap.put("motherName", mother.isUnknownIndividual() ? context.getString(R.string.member_details_unknown_lbl) : mother.name);
        preloadedMap.put("fatherCode", father.code);
        preloadedMap.put("fatherName", father.isUnknownIndividual() ? context.getString(R.string.member_details_unknown_lbl) : father.name);
    }

    @Override
    public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colMemberCode = collectedValues.get("memberCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colMotherCode = collectedValues.get("motherCode"); //not blank
        ColumnValue colMotherName = collectedValues.get("motherName");
        ColumnValue colFatherCode = collectedValues.get("fatherCode"); //not blank
        ColumnValue colFatherName = collectedValues.get("fatherName");
        ColumnValue colMemberName = collectedValues.get("memberName"); //not blank
        ColumnValue colMemberGender = collectedValues.get("memberGender"); //not blank
        ColumnValue colMemberDob = collectedValues.get("memberDob"); //date cannot be in future + head min age
        ColumnValue colHeadRelationshipType = collectedValues.get("headRelationshipType"); //not blank
        ColumnValue colMigrationType = collectedValues.get("migrationType");
        ColumnValue colExtMigrationType = collectedValues.get("extMigrationType");
        ColumnValue colOriginCode = collectedValues.get("originCode");
        ColumnValue colOriginOther = collectedValues.get("originOther");
        ColumnValue colDestinationCode = collectedValues.get("destinationCode");
        ColumnValue colMigrationDate = collectedValues.get("migrationDate"); //not null cannot be in the future nor before dob
        ColumnValue colMigrationReason = collectedValues.get("migrationReason");
        ColumnValue colMigrationReasonOther = collectedValues.get("migrationReasonOther");

        //ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        //ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        //ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String memberCode = colMemberCode.getValue();
        String motherCode = colMotherCode.getValue();
        String motherName = colMotherName.getValue();
        String fatherCode = colFatherCode.getValue();
        String fatherName = colFatherName.getValue();
        String memberName = colMemberName.getValue();
        Gender memberGender = Gender.getFrom(colMemberGender.getValue());
        Date memberDob = colMemberDob.getDateValue();
        HeadRelationshipType headRelationshipType = HeadRelationshipType.getFrom(colHeadRelationshipType.getValue());
        InMigrationType migrationType = InMigrationType.getFrom(colMigrationType.getValue());
        ExternalInMigrationType extMigrationType = ExternalInMigrationType.getFrom(colExtMigrationType.getValue());
        String originCode = colOriginCode.getValue();
        String originOther = colOriginOther.getValue();
        String destinationCode = colDestinationCode.getValue();
        Date migrationDate = colMigrationDate.getDateValue();

        //validations
        //memberCode blank/valid
        if (extMigrationType==ExternalInMigrationType.ENTRY && !codeGenerator.isMemberCodeValid(memberCode)){
            String message = this.context.getString(R.string.new_member_code_err_lbl);
            return new ValidationResult(colMemberCode, message);
        }
        //memberCode - valid household on first entry
        if (extMigrationType==ExternalInMigrationType.ENTRY && !memberCode.startsWith(household.code)){
            String message = this.context.getString(R.string.new_member_code_household_err_lbl);
            return new ValidationResult(colMemberCode, message);
        }

        //check if visit with code exists
        if (extMigrationType==ExternalInMigrationType.ENTRY && Queries.getMemberByCode(boxMembers, memberCode) != null){
            String message = this.context.getString(R.string.new_member_code_exists_lbl);
            return new ValidationResult(colMemberCode, message);
        }

        if (StringUtil.isBlank(motherCode)){
            String message = this.context.getString(R.string.new_member_mother_empty_lbl);
            return new ValidationResult(colMotherCode, message);
        }

        if (StringUtil.isBlank(fatherCode)){
            String message = this.context.getString(R.string.new_member_father_empty_lbl);
            return new ValidationResult(colFatherCode, message);
        }

        if (StringUtil.isBlank(memberName)){
            String message = this.context.getString(R.string.new_member_name_empty_lbl);
            return new ValidationResult(colMemberName, message);
        }

        if (memberGender == null){
            String message = this.context.getString(R.string.new_member_gender_empty_lbl);
            return new ValidationResult(colMemberGender, message);
        }

        if (memberDob == null){
            String message = this.context.getString(R.string.new_member_dob_empty_lbl);
            return new ValidationResult(colMemberDob, message);
        }

        //dob cannot be in future
        if (memberDob.after(new Date())){ //is in the future
            String message = this.context.getString(R.string.new_member_dob_not_great_today_lbl);
            return new ValidationResult(colMemberDob, message);
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
            int age = GeneralUtil.getAge(memberDob);
            if (age < minimunHeadAge){
                String message = this.context.getString(R.string.new_member_dob_minimum_age_head_lbl, minimunHeadAge+"");
                return new ValidationResult(colMemberDob, message);
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
        if (migrationDate.before(memberDob)){ //is before dob
            String message = this.context.getString(R.string.external_inmigration_migrationdate_not_before_dob_lbl);
            return new ValidationResult(colMigrationDate, message);
        }

        //check destinationCode, originCode existence
        //check Member Death Status - if returning to DSS
        if (externalInMigrationType == ExternalInMigrationType.REENTRY) {

            if (Queries.getDeathByCode(this.boxDeaths, memberCode)!=null){
                String message = this.context.getString(R.string.external_inmigration_death_exists_lbl);
                return new ValidationResult(colMemberCode, message);
            }

            //check Member last closed headtype status - if returning to DSS
            HeadRelationship lastHeadRelationship = getLastOpenedHeadRelationship(memberCode);
            if (lastHeadRelationship != null) {
                String message = this.context.getString(R.string.external_inmigration_head_relationship_opened_lbl);
                return new ValidationResult(colMemberCode, message);
            }
        }

        return ValidationResult.noErrors();
    }

    private HeadRelationship getLastOpenedHeadRelationship(String memberCode) {
        HeadRelationship lastHeadRelationship = this.boxHeadRelationships.query(
                HeadRelationship_.memberCode.equal(memberCode).and(HeadRelationship_.endType.equal(HeadRelationshipEndType.NOT_APPLICABLE.code)))
                .orderDesc(HeadRelationship_.startDate)
                .build().findFirst();

        return lastHeadRelationship;
    }

    @Override
    public void onFormFinished(HForm form, CollectedDataMap collectedValues, XmlFormResult result) {

        Log.d("resultxml", result.getXmlResult());

        if (currentMode == Mode.EDIT) {
            System.out.println("Editing External InMigration Not implemented yet");
            assert 1==0;
        }


        //saveNewHousehold();
        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colMemberCode = collectedValues.get("memberCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colMotherCode = collectedValues.get("motherCode"); //not blank
        ColumnValue colMotherName = collectedValues.get("motherName");
        ColumnValue colFatherCode = collectedValues.get("fatherCode"); //not blank
        ColumnValue colFatherName = collectedValues.get("fatherName");
        ColumnValue colMemberName = collectedValues.get("memberName"); //not blank
        ColumnValue colMemberGender = collectedValues.get("memberGender"); //not blank
        ColumnValue colMemberDob = collectedValues.get("memberDob"); //date cannot be in future + head min age
        ColumnValue colHeadRelationshipType = collectedValues.get("headRelationshipType"); //not blank
        ColumnValue colMigrationType = collectedValues.get("migrationType");
        ColumnValue colExtMigrationType = collectedValues.get("extMigrationType");
        ColumnValue colOriginCode = collectedValues.get("originCode");
        ColumnValue colOriginOther = collectedValues.get("originOther");
        ColumnValue colDestinationCode = collectedValues.get("destinationCode");
        ColumnValue colMigrationDate = collectedValues.get("migrationDate"); //not null cannot be in the future nor before dob
        ColumnValue colMigrationReason = collectedValues.get("migrationReason");
        ColumnValue colMigrationReasonOther = collectedValues.get("migrationReasonOther");

        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String memberCode = colMemberCode.getValue();
        String motherCode = colMotherCode.getValue();
        String motherName = colMotherName.getValue();
        String fatherCode = colFatherCode.getValue();
        String fatherName = colFatherName.getValue();
        String memberName = colMemberName.getValue();
        Gender memberGender = Gender.getFrom(colMemberGender.getValue());
        Date memberDob = colMemberDob.getDateValue();
        HeadRelationshipType headRelationshipType = HeadRelationshipType.getFrom(colHeadRelationshipType.getValue());
        InMigrationType migrationType = InMigrationType.getFrom(colMigrationType.getValue());
        ExternalInMigrationType extMigrationType = ExternalInMigrationType.getFrom(colExtMigrationType.getValue());
        String originCode = colOriginCode.getValue();
        String originOther = colOriginOther.getValue();
        String destinationCode = colDestinationCode.getValue();
        Date migrationDate = colMigrationDate.getDateValue();

        //create member, residency, headrelationship, inmigration

        Member member = null;

        if (extMigrationType == ExternalInMigrationType.ENTRY) {
            member = new Member();

            member.code = memberCode;
            member.name = memberName;
            member.gender = memberGender;
            member.dob = memberDob;
            member.age = GeneralUtil.getAge(memberDob);
            member.ageAtDeath = 0;
            member.motherCode = motherCode;
            member.motherName = motherName;
            member.fatherCode = fatherCode;
            member.fatherName = fatherName;
            member.maritalStatus = MaritalStatus.SINGLE; //always start as single
            member.householdCode = household.code;
            member.householdName = household.name;
            member.entryHousehold = household.code;
            member.entryType = ResidencyStartType.EXTERNAL_INMIGRATION;
            member.entryDate = migrationDate;

        } else {
            member = returningMember;
        }

        //residency current status
        member.startType = ResidencyStartType.EXTERNAL_INMIGRATION;
        member.startDate = migrationDate;
        member.endType = ResidencyEndType.NOT_APPLICABLE;
        member.endDate = null;

        member.headRelationshipType = headRelationshipType;
        member.gpsNull = household.gpsNull;
        member.gpsAccuracy = household.gpsAccuracy;
        member.gpsAltitude = household.gpsAltitude;
        member.gpsLatitude = household.gpsLatitude;
        member.gpsLongitude = household.gpsLongitude;
        member.cosLatitude = household.cosLatitude;
        member.sinLatitude = household.sinLatitude;
        member.cosLongitude = household.cosLongitude;
        member.sinLongitude = household.sinLongitude;
        member.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        member.recentlyCreated = true;
        member.recentlyCreatedUri = result.getFilename();
        member.modules.addAll(StringCollectionConverter.getCollectionFrom(colModules.getValue()));


        //Residency
        Residency residency = new Residency();
        residency.householdCode = household.code;
        residency.memberCode = member.code;
        residency.startType = ResidencyStartType.EXTERNAL_INMIGRATION;
        residency.startDate = migrationDate;
        residency.endType = ResidencyEndType.NOT_APPLICABLE;
        residency.endDate = null;

        //HeadRelationship
        HeadRelationship headRelationship = new HeadRelationship();
        headRelationship.householdCode = household.code;
        headRelationship.memberCode = member.code;
        headRelationship.relationshipType = headRelationshipType;
        headRelationship.startType = HeadRelationshipStartType.ENUMERATION;
        headRelationship.startDate = migrationDate;
        headRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
        headRelationship.endDate = null;

        //save data
        boxMembers.put(member);
        boxResidencies.put(residency);
        boxHeadRelationships.put(headRelationship);

        //save core collected data
        collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.EXTERNAL_INMIGRATION;
        collectedData.formEntityId = member.id;
        collectedData.formEntityCode = member.code;
        collectedData.formEntityName = member.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        collectedData.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        collectedData.extension.setTarget(this.getFormExtension(collectedData.formEntity));

        boxCoreCollectedData.put(collectedData);

        if (isFirstHouseholdMember) { //head of household
            household.headCode = member.code;
            household.headName = member.name;
            boxHouseholds.put(household);
        }

        this.entity = member;
        this.collectExtensionForm(collectedValues);

    }

    @Override
    protected void onFinishedExtensionCollection() {
        if (listener != null) {
            listener.onNewEntityCreated(entity);
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
            selectExtInMigTypeDialog();
        } else if (currentMode == Mode.EDIT) {
            checkChangeFatherDialog();
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

    private int retrieveMinimumFatherAge() {
        ApplicationParam param = this.boxAppParams.query().equal(ApplicationParam_.name, ApplicationParam.PARAMS_MIN_AGE_OF_FATHER, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (param != null) {
            try {
                return Integer.parseInt(param.value);
            } catch (Exception ex) {

            }
        }

        return 12;
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

    private void selectExtInMigTypeDialog(){
        //Title: External InMigration
        //Select the Type of Migration to be executed
        //Is The member you are about to InMigrate already registered in the DSS
        //New Member Registration (First Entry)
        //A Registered Member returning to the HDSS

        DialogFactory.createMessageYN(this.context, R.string.eventType_external_inmigration, R.string.external_inmigration_asktype_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                //A member its returning to the DSS
                externalInMigrationType = ExternalInMigrationType.REENTRY;
                openReturningMemberFilterDialog();
            }

            @Override
            public void onNoClicked() {
                //its a first entry - go filter father and mother
                externalInMigrationType = ExternalInMigrationType.ENTRY;
                checkFatherDialog();
            }
        }).show();
    }

    private void openReturningMemberFilterDialog(){

        MemberFilterDialog dialog = MemberFilterDialog.newInstance(this.fragmentManager, context.getString(R.string.external_inmigration_select_return_member_lbl), true, new MemberFilterDialog.Listener() {
            @Override
            public void onSelectedMember(Member member) {
                Log.d("selected-member", ""+member.getCode());

                returningMember = member;
                father = Queries.getMemberByCode(boxMembers, member.fatherCode);
                mother = Queries.getMemberByCode(boxMembers, member.motherCode);

                executeCollectForm();
            }

            @Override
            public void onCanceled() {

            }
        });

        dialog.setFilterHouseCode(household.getCode());
        dialog.setFilterStatus(MemberFilterDialog.StatusFilter.OUTMIGRATED, true);
        dialog.setStartSearchOnShow(true);
        dialog.show();
    }

    private void checkFatherDialog(){

        DialogFactory.createMessageYN(this.context, R.string.new_member_dialog_father_select_lbl, R.string.new_member_dialog_father_exists_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                openFatherFilterDialog();
            }

            @Override
            public void onNoClicked() {
                //set unknown and jump to father dialog
                father = Member.getUnknownIndividual();
                checkMotherDialog();
            }
        }).show();

    }

    private void checkMotherDialog(){

        DialogFactory.createMessageYN(this.context, R.string.new_member_dialog_mother_select_lbl, R.string.new_member_dialog_mother_exists_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                openMotherFilterDialog();
            }

            @Override
            public void onNoClicked() {
                mother = Member.getUnknownIndividual();
                executeCollectForm();
            }

        }).show();

    }

    private void checkChangeFatherDialog(){

        DialogFactory.createMessageYN(this.context, R.string.new_member_dialog_change_title_lbl, R.string.new_member_dialog_father_change_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                openFatherFilterDialog();
            }

            @Override
            public void onNoClicked() {
                //father remains unchanged
                checkChangeMotherDialog();
            }
        }).show();

    }

    private void checkChangeMotherDialog(){

        DialogFactory.createMessageYN(this.context, R.string.new_member_dialog_change_title_lbl, R.string.new_member_dialog_father_change_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                openMotherFilterDialog();
            }

            @Override
            public void onNoClicked() {
                //mother remains unchanged
                executeCollectForm();
            }
        }).show();

    }

    private void openFatherFilterDialog(){

        MemberFilterDialog dialog = MemberFilterDialog.newInstance(this.fragmentManager, context.getString(R.string.new_member_dialog_father_select_lbl), true, new MemberFilterDialog.Listener() {
            @Override
            public void onSelectedMember(Member member) {
                Log.d("selected-father", ""+member.getCode());

                father = member;
                checkMotherDialog();
            }

            @Override
            public void onCanceled() {

            }
        });

        dialog.setGenderMaleOnly();
        dialog.setFilterMinAge(this.minimunFatherAge, true);
        dialog.setFilterHouseCode(household.getCode());
        dialog.setStartSearchOnShow(true);
        dialog.show();
    }

    private void openMotherFilterDialog(){

        MemberFilterDialog dialog = MemberFilterDialog.newInstance(this.fragmentManager, context.getString(R.string.new_member_dialog_mother_select_lbl), true, new MemberFilterDialog.Listener() {
            @Override
            public void onSelectedMember(Member member) {
                Log.d("selected-mother", ""+member.getCode());

                mother = member;
                executeCollectForm();
            }

            @Override
            public void onCanceled() {

            }
        });

        dialog.setGenderFemaleOnly();
        dialog.setFilterMinAge(this.minimunMotherAge, true);
        dialog.setFilterHouseCode(household.getCode());
        dialog.setStartSearchOnShow(true);
        dialog.show();
    }

}
