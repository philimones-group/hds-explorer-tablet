package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.fragment.MemberFilterDialog;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.HeadRelationship;
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
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipEndType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipStartType;
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

import androidx.fragment.app.FragmentManager;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

public class MemberEnumerationFormUtil extends FormUtil<Member> {

    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<Residency> boxResidencies;
    private Box<HeadRelationship> boxHeadRelationships;
    private Box<CoreCollectedData> boxCoreCollectedData;

    private Household household;
    private Visit visit;
    private Member father;
    private Member mother;
    private Member head;
    private boolean isFirstHouseholdMember;
    private int minimunHeadAge;
    private int minimunFatherAge;
    private int minimunMotherAge;

    public MemberEnumerationFormUtil(FragmentManager fragmentManager, Context context, Visit visit, Household household, FormUtilListener<Member> listener){
        super(fragmentManager, context, FormUtil.getMemberEnuForm(context), listener);

        //Log.d("enu-household", ""+household);

        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    public MemberEnumerationFormUtil(FragmentManager fragmentManager, Context context, Visit visit, Household household, Member memberToEdit, FormUtilListener<Member> listener){
        super(fragmentManager, context, FormUtil.getMemberEnuForm(context), memberToEdit, listener);

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
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
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
        preloadedMap.put("visitCode", this.visit.code);
        preloadedMap.put("code", codeGenerator.generateMemberCode(household));
        preloadedMap.put("householdCode", household.code);
        preloadedMap.put("householdName", household.name);
        preloadedMap.put("motherCode", mother.code);
        preloadedMap.put("motherName", mother.isUnknownIndividual() ? context.getString(R.string.member_details_unknown_lbl) : mother.name);
        preloadedMap.put("fatherCode", father.code);
        preloadedMap.put("fatherName", father.isUnknownIndividual() ? context.getString(R.string.member_details_unknown_lbl) : father.name);
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
        ColumnValue colCode = collectedValues.get("code"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colHouseholdCode = collectedValues.get("householdCode");
        ColumnValue colHouseholdName = collectedValues.get("householdName");
        ColumnValue colMotherCode = collectedValues.get("motherCode"); //not blank
        ColumnValue colMotherName = collectedValues.get("motherName");
        ColumnValue colFatherCode = collectedValues.get("fatherCode"); //not blank
        ColumnValue colFatherName = collectedValues.get("fatherName");
        ColumnValue colName = collectedValues.get("name"); //not blank
        ColumnValue colGender = collectedValues.get("gender"); //not blank
        ColumnValue colDob = collectedValues.get("dob"); //date cannot be in future + head min age
        ColumnValue colHeadRelationshipType = collectedValues.get("headRelationshipType"); //not blank
        ColumnValue colResidencyStartDate = collectedValues.get("residencyStartDate"); //not null cannot be in the future nor before dob
        //ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        //ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        //ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String code = colCode.getValue();
        String householdCode = colHouseholdCode.getValue();
        String householdName = colHouseholdName.getValue();
        String motherCode = colMotherCode.getValue();
        String motherName = colMotherName.getValue();
        String fatherCode = colFatherCode.getValue();
        String fatherName = colFatherName.getValue();
        String name = colName.getValue();
        Gender gender = Gender.getFrom(colGender.getValue());
        Date dob = colDob.getDateValue();
        HeadRelationshipType headRelationshipType = HeadRelationshipType.getFrom(colHeadRelationshipType.getValue());
        Date residencyStartDate = colResidencyStartDate.getDateValue();

        if (!codeGenerator.isMemberCodeValid(code)){
            String message = this.context.getString(R.string.new_member_code_err_lbl);
            return new ValidationResult(colCode, message);
        }

        if (!code.startsWith(household.code)){
            String message = this.context.getString(R.string.new_member_code_household_err_lbl);
            return new ValidationResult(colCode, message);
        }

        //check if visit with code exists
        if (boxMembers.query().equal(Member_.code, code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst() != null){
            String message = this.context.getString(R.string.new_member_code_exists_lbl);
            return new ValidationResult(colCode, message);
        }

        if (StringUtil.isBlank(motherCode)){
            String message = this.context.getString(R.string.new_member_mother_empty_lbl);
            return new ValidationResult(colMotherCode, message);
        }

        if (StringUtil.isBlank(fatherCode)){
            String message = this.context.getString(R.string.new_member_father_empty_lbl);
            return new ValidationResult(colFatherCode, message);
        }

        if (StringUtil.isBlank(name)){
            String message = this.context.getString(R.string.new_member_name_empty_lbl);
            return new ValidationResult(colName, message);
        }

        if (gender == null){
            String message = this.context.getString(R.string.new_member_gender_empty_lbl);
            return new ValidationResult(colGender, message);
        }

        if (dob == null){
            String message = this.context.getString(R.string.new_member_dob_empty_lbl);
            return new ValidationResult(colDob, message);
        }

        //dob cannot be in future
        if (dob.after(new Date())){ //is in the future
            String message = this.context.getString(R.string.new_member_dob_not_great_today_lbl);
            return new ValidationResult(colDob, message);
        }

        if (headRelationshipType == null){
            String message = this.context.getString(R.string.new_member_head_relattype_empty_lbl);
            return new ValidationResult(colHeadRelationshipType, message);
        }

        //dob age must be greater or equals to head min age
        if (isFirstHouseholdMember) { //head
            int age = GeneralUtil.getAge(dob);
            if (age < minimunHeadAge){
                String message = this.context.getString(R.string.new_member_dob_minimum_age_head_lbl, minimunHeadAge+"");
                return new ValidationResult(colDob, message);
            }
        }

        if (residencyStartDate == null){
            String message = this.context.getString(R.string.new_member_residency_startdate_empty_lbl);
            return new ValidationResult(colResidencyStartDate, message);
        }

        //startdate cannot be in the future
        if (residencyStartDate.after(new Date())){ //is in the future
            String message = this.context.getString(R.string.new_member_residency_startdate_not_great_today_lbl);
            return new ValidationResult(colResidencyStartDate, message);
        }

        //residency start date cannot be before dob
        if (residencyStartDate.before(dob)){ //is before dob
            String message = this.context.getString(R.string.new_member_residency_startdate_not_before_dob_lbl);
            return new ValidationResult(colResidencyStartDate, message);
        }

        return ValidationResult.noErrors();
    }

    @Override
    public void onFormFinished(HForm form, CollectedDataMap collectedValues, XmlFormResult result) {

        Log.d("resultxml", result.getXmlResult());

        if (currentMode == Mode.EDIT) {
            System.out.println("Editing Member Enumeration Not implemented yet");
            assert 1==0;
        }


        //saveNewHousehold();
        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colCode = collectedValues.get("code"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colHouseholdCode = collectedValues.get("householdCode");
        ColumnValue colHouseholdName = collectedValues.get("householdName");
        ColumnValue colMotherCode = collectedValues.get("motherCode"); //not blank
        ColumnValue colMotherName = collectedValues.get("motherName");
        ColumnValue colFatherCode = collectedValues.get("fatherCode"); //not blank
        ColumnValue colFatherName = collectedValues.get("fatherName");
        ColumnValue colName = collectedValues.get("name"); //not blank
        ColumnValue colGender = collectedValues.get("gender"); //not blank
        ColumnValue colDob = collectedValues.get("dob"); //date cannot be in future + head min age
        ColumnValue colHeadRelationshipType = collectedValues.get("headRelationshipType"); //not blank
        ColumnValue colResidencyStartDate = collectedValues.get("residencyStartDate"); //not null cannot be in the future nor before dob
        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String code = colCode.getValue();
        String householdCode = colHouseholdCode.getValue();
        String householdName = colHouseholdName.getValue();
        String motherCode = colMotherCode.getValue();
        String motherName = colMotherName.getValue();
        String fatherCode = colFatherCode.getValue();
        String fatherName = colFatherName.getValue();
        String name = colName.getValue();
        Gender gender = Gender.getFrom(colGender.getValue());
        Date dob = colDob.getDateValue();
        HeadRelationshipType headRelationshipType = HeadRelationshipType.getFrom(colHeadRelationshipType.getValue());
        Date residencyStartDate = colResidencyStartDate.getDateValue();

        Member member = new Member();
        member.code = code;
        member.name = name;
        member.gender = gender;
        member.dob = dob;
        member.age = GeneralUtil.getAge(dob);
        member.ageAtDeath = 0;
        member.motherCode = motherCode;
        member.motherName = motherName;
        member.fatherCode = fatherCode;
        member.fatherName = fatherName;
        member.maritalStatus = MaritalStatus.SINGLE; //always single
        //member.spouseCode = null;
        //member.spouseName = null;
        member.householdCode = householdCode;
        member.householdName = householdName;
        member.startType = ResidencyStartType.ENUMERATION;
        member.startDate = residencyStartDate;
        member.endType = ResidencyEndType.NOT_APPLICABLE;
        member.endDate = null;
        member.entryHousehold = householdCode;
        member.entryType = ResidencyStartType.ENUMERATION;
        member.entryDate = residencyStartDate;
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
        member.recentlyCreated = true;
        member.recentlyCreatedUri = result.getFilename();
        member.modules.addAll(StringCollectionConverter.getCollectionFrom(colModules.getValue()));


        //Residency
        Residency residency = new Residency();
        residency.householdCode = householdCode;
        residency.memberCode = member.code;
        residency.startType = ResidencyStartType.ENUMERATION;
        residency.startDate = residencyStartDate;
        residency.endType = ResidencyEndType.NOT_APPLICABLE;
        residency.endDate = null;

        //HeadRelationship
        HeadRelationship headRelationship = new HeadRelationship();
        headRelationship.householdCode = householdCode;
        headRelationship.memberCode = member.code;
        headRelationship.relationshipType = headRelationshipType;
        headRelationship.startType = HeadRelationshipStartType.ENUMERATION;
        headRelationship.startDate = residencyStartDate;
        headRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
        headRelationship.endDate = null;

        //save data
        boxMembers.put(member);
        boxResidencies.put(residency);
        boxHeadRelationships.put(headRelationship);

        //save core collected data
        CoreCollectedData collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.MEMBER_ENU;
        collectedData.formEntityId = member.id;
        collectedData.formEntityCode = member.code;
        collectedData.formEntityName = member.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();

        boxCoreCollectedData.put(collectedData);

        if (isFirstHouseholdMember) { //head of household
            household.headCode = member.code;
            household.headName = member.name;
            boxHouseholds.put(household);
        }


        if (listener != null) {
            listener.onNewEntityCreated(member);
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
            checkFatherDialog();
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
