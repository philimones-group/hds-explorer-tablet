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
import org.philimone.hds.explorer.model.Death_;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.HeadRelationship_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.MaritalRelationship;
import org.philimone.hds.explorer.model.MaritalRelationship_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Death;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Residency;
import org.philimone.hds.explorer.model.Residency_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.HeadRelationshipType;
import org.philimone.hds.explorer.model.enums.MaritalEndStatus;
import org.philimone.hds.explorer.model.enums.MaritalStatus;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipEndType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipStartType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.RepeatColumnValue;
import org.philimone.hds.forms.model.RepeatObject;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

public class DeathFormUtil extends FormUtil<Death> {

    private Box<Member> boxMembers;
    private Box<Death> boxDeaths;
    private Box<HeadRelationship> boxHeadRelationships;
    private Box<Residency> boxResidencies;
    private Box<MaritalRelationship> boxMaritalRelationships;
    private Box<CoreCollectedData> boxCoreCollectedData;

    private Household household;
    private Visit visit;
    private Member member;
    private Boolean isHouseholdHead = false;
    private Member newHeadMember;
    private List<HeadRelationship> headMemberHeadRelationships;
    private Residency memberResidency;
    private HeadRelationship memberHeadRelationship;
    private MaritalRelationship memberMaritalRelationship;
    private Member spouseMember;

    private List<Member> householdResidents;
    private int minimunHeadAge;

    public DeathFormUtil(FragmentManager fragmentManager, Context context, Visit visit, Household household, Member member, FormUtilListener<Death> listener){
        super(fragmentManager, context, FormUtil.getDeathForm(context), listener);

        //Log.d("enu-household", ""+household);

        this.household = household;
        this.member = member;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    public DeathFormUtil(FragmentManager fragmentManager, Context context, Visit visit, Household household, Death deathToEdit, FormUtilListener<Death> listener){
        super(fragmentManager, context, FormUtil.getDeathForm(context), deathToEdit, listener);

        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxDeaths = ObjectBoxDatabase.get().boxFor(Death.class);
        this.boxHeadRelationships = ObjectBoxDatabase.get().boxFor(HeadRelationship.class);
        this.boxResidencies = ObjectBoxDatabase.get().boxFor(Residency.class);
        this.boxMaritalRelationships = ObjectBoxDatabase.get().boxFor(MaritalRelationship.class);

    }

    @Override
    protected void initialize(){
        super.initialize();

        this.isHouseholdHead = this.boxHeadRelationships.query(
                HeadRelationship_.householdCode.equal(this.household.code)
                .and(HeadRelationship_.memberCode.equal(this.member.code))
                .and(HeadRelationship_.relationshipType.equal(HeadRelationshipType.HEAD_OF_HOUSEHOLD.code))
                .and(HeadRelationship_.endType.equal(HeadRelationshipEndType.NOT_APPLICABLE.code)))
                .orderDesc(HeadRelationship_.startDate)
                .build().count()>0;

        this.headMemberHeadRelationships = this.boxHeadRelationships.query(
                                        HeadRelationship_.householdCode.equal(this.household.code)
                                       .and(HeadRelationship_.headCode.equal(this.member.code))
                                       .and(HeadRelationship_.endType.equal(HeadRelationshipEndType.NOT_APPLICABLE.code)))
                                       .orderDesc(HeadRelationship_.startDate)
                                       .build().find();

        //get current memberResidency
        this.memberResidency = this.boxResidencies.query(
                Residency_.householdCode.equal(this.household.code)
                .and(Residency_.memberCode.equal(this.member.code))
                .and(Residency_.endType.equal(ResidencyEndType.NOT_APPLICABLE.code)))
                .orderDesc(Residency_.startDate)
                .build().findFirst();
        //get current memberHeadRelationship
        this.memberHeadRelationship = this.boxHeadRelationships.query(
                     HeadRelationship_.householdCode.equal(this.household.code)
                .and(HeadRelationship_.memberCode.equal(this.member.code))
                .and(HeadRelationship_.endType.equal(HeadRelationshipEndType.NOT_APPLICABLE.code)))
                .orderDesc(HeadRelationship_.startDate)
                .build().findFirst();
        //get current MaritalRelationship
        this.memberMaritalRelationship = this.boxMaritalRelationships.query(
                     MaritalRelationship_.endStatus.equal(MaritalEndStatus.NOT_APPLICABLE.code)
                .and(MaritalRelationship_.memberA_code.equal(this.member.code).or(MaritalRelationship_.memberB_code.equal(this.member.code))))
                .orderDesc(MaritalRelationship_.startDate)
                .build().findFirst();
        //get spouse
        if (this.memberMaritalRelationship != null) {
            String spouseCode = this.memberMaritalRelationship.memberA_code==member.code ? this.memberMaritalRelationship.memberB_code : this.memberMaritalRelationship.memberA_code;
            this.spouseMember = Queries.getMemberByCode(this.boxMembers, spouseCode);
        }

        this.minimunHeadAge = retrieveMinimumHeadAge();

        if (isHouseholdHead) { //is death of head of household
            this.householdResidents = getHouseholdResidentsExcDeadHead();
        }

    }

    private List<Member> getHouseholdResidentsExcDeadHead() {

        //order list with new head of household as the first id

        List<Residency> residencies = this.boxResidencies.query(
                Residency_.householdCode.equal(this.household.code).and(Residency_.endType.equal(ResidencyEndType.NOT_APPLICABLE.code)))
                .order(Residency_.memberCode)
                .build().find();

        //exclude old household code
        List<String> residentMembersExDeadHead = new ArrayList<>();
        for (Residency residency : residencies) {
            if (!member.code.equals(residency.memberCode)) { //its not the dead member
                residentMembersExDeadHead.add(residency.memberCode);
                //Log.d("member", ""+residency.memberCode);
            }
        }

        List<Member> members = this.boxMembers.query(Member_.code.oneOf(residentMembersExDeadHead.toArray(new String[0]))).build().find();

        return members;
    }

    private List<Member> reorderWithHeadAsFirst(List<Member> members, Member headMember){
        Member extractedMember = null;
        for (Member mb : members){
            if (mb.code.equals(headMember.code)){
                extractedMember = mb;
            }
        }

        if (extractedMember != null){
            members.remove(extractedMember);
            members.add(0, extractedMember);
        }

        return members;
    }

    @Override
    protected void preloadValues() {
        //member_details_unknown_lbl

        if (this.householdResidents != null) {
            reorderWithHeadAsFirst(this.householdResidents, newHeadMember);
        }

        preloadedMap.put("visitCode", this.visit.code);
        preloadedMap.put("memberCode", this.member.code);
        preloadedMap.put("memberName", this.member.name);
        preloadedMap.put("isHouseholdHead", this.isHouseholdHead.toString().toUpperCase());

        if (isHouseholdHead){
            preloadedMap.put("newHeadCode", this.newHeadMember.code);
            preloadedMap.put("newHeadName", this.newHeadMember.name);

            //Load Repeat Objects
            RepeatObject newRelationshipsRepObj = new RepeatObject();
            for (Member mb : householdResidents) {
                Log.d("resident", ""+mb.code);
                Map<String, String> obj = newRelationshipsRepObj.createNewObject();
                obj.put("newMemberCode", mb.code);
                obj.put("newMemberName", mb.name);
                obj.put("newRelationshipType", this.newHeadMember.code.equals(mb.code) ? "HOH" : ""); //Set the head if is one of them
            }
            Log.d("relationships", newRelationshipsRepObj.getList().size()+"");
            preloadedMap.put("newRelationships", newRelationshipsRepObj);
        }

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
        ColumnValue colDeathDate = collectedValues.get("deathDate"); //not null cannot be in the future nor before dob
        ColumnValue colDeathCause = collectedValues.get("deathCause");
        ColumnValue colDeathCauseOther = collectedValues.get("deathCauseOther");
        ColumnValue colDeathPlace = collectedValues.get("deathPlace");
        ColumnValue colDeathPlaceOther = collectedValues.get("deathPlaceOther"); //not null, cannot be before dob+12
        ColumnValue colIsHouseholdHead = collectedValues.get("isHouseholdHead");
        ColumnValue colNewHeadCode = collectedValues.get("newHeadCode");
        ColumnValue colNewHeadName = collectedValues.get("newHeadName");

        RepeatColumnValue repNewRelationships = collectedValues.getRepeatColumn("newRelationships");
        List<ColumnValue> colNewMemberCodes = new ArrayList<>();
        List<ColumnValue> colNewMemberNames = new ArrayList<>();
        List<ColumnValue> colNewRelationships = new ArrayList<>();

        if (repNewRelationships != null) {
            for (int i = 0; i < repNewRelationships.getCount(); i++) {
                ColumnValue colNewMemberCode = repNewRelationships.get("newMemberCode", i);
                ColumnValue colNewMemberName = repNewRelationships.get("newMemberName", i);
                ColumnValue colNewRelationship = repNewRelationships.get("newRelationshipType", i);

                colNewMemberCodes.add(colNewMemberCode);
                colNewMemberNames.add(colNewMemberName);
                colNewRelationships.add(colNewRelationship);
            }
        }

        //ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        //ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        //ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String memberCode = colMemberCode.getValue();
        String memberName = colMemberName.getValue();
        Date deathDate = colDeathDate.getDateValue();
        String deathCause = colDeathCause.getValue();
        String deathCauseOther = colDeathCauseOther.getValue();
        String deathPlace = colDeathPlace.getValue();
        String deathPlaceOther = colDeathPlaceOther.getValue();
        Boolean isHouseholdHead = StringUtil.toBoolean(colIsHouseholdHead.getValue());
        String newHeadCode = colNewHeadCode.getValue();
        String newHeadName = colNewHeadName.getValue();

        List<String> newMemberCodes = colNewMemberCodes.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> newMemberNames = colNewMemberNames.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> newRelationships = colNewRelationships.stream().map(ColumnValue::getValue).collect(Collectors.toList());

        //validations

        if (StringUtil.isBlank(visitCode)){
            String message = this.context.getString(R.string.death_visit_code_empty_lbl);
            return new ValidationResult(colVisitCode, message);
        }

        if (StringUtil.isBlank(memberCode)){
            String message = this.context.getString(R.string.death_member_code_empty_lbl);
            return new ValidationResult(colMemberCode, message);
        }

        if (boxDeaths.query(Death_.memberCode.equal(memberCode)).build().count()>0){
            String message = this.context.getString(R.string.death_member_code_exists_lbl);
            return new ValidationResult(colMemberCode, message);
        }

        if (deathDate == null) {
            String message = this.context.getString(R.string.death_deathdate_empty_lbl);
            return new ValidationResult(colDeathDate, message);
        }

        //eddDate cannot be before dob
        if (deathDate != null && deathDate.before(this.member.dob)){ //is before dob
            String message = this.context.getString(R.string.death_deathdate_not_before_dob_lbl);
            return new ValidationResult(colDeathDate, message);
        }

        if (deathDate != null && deathDate.after(new Date())){ //is before dob
            String message = this.context.getString(R.string.death_deathdate_not_before_dob_lbl);
            return new ValidationResult(colDeathDate, message);
        }

        if (isHouseholdHead && StringUtil.isBlank(newHeadCode)){
            String message = this.context.getString(R.string.death_new_head_code_empty_lbl);
            return new ValidationResult(colNewHeadCode, message);
        }

        //other validations
        /*
        if (isHouseholdHead){
            Residency residency = this.boxResidencies.query(
                    Residency_.memberCode.equal(newHeadCode)
                    .and(Residency_.endType.equal(ResidencyEndType.NOT_APPLICABLE.code))
                    .and(Residency_.householdCode.notEqual(household.code)))
                    .orderDesc(Residency_.startDate).build().findFirst();

            if (residency != null){
                String message = "";
            }
        }
        */

        return ValidationResult.noErrors();
    }

    @Override
    public void onFormFinished(HForm form, CollectedDataMap collectedValues, XmlFormResult result) {

        Log.d("resultxml", result.getXmlResult());

        if (currentMode == Mode.EDIT) {
            System.out.println("Editing Death Not implemented yet");
            assert 1==0;
        }

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colMemberCode = collectedValues.get("memberCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colMemberName = collectedValues.get("memberName"); //not blank
        ColumnValue colDeathDate = collectedValues.get("deathDate"); //not null cannot be in the future nor before dob
        ColumnValue colDeathCause = collectedValues.get("deathCause");
        ColumnValue colDeathCauseOther = collectedValues.get("deathCauseOther");
        ColumnValue colDeathPlace = collectedValues.get("deathPlace");
        ColumnValue colDeathPlaceOther = collectedValues.get("deathPlaceOther"); //not null, cannot be before dob+12
        ColumnValue colIsHouseholdHead = collectedValues.get("isHouseholdHead");
        ColumnValue colNewHeadCode = collectedValues.get("newHeadCode");
        ColumnValue colNewHeadName = collectedValues.get("newHeadName");

        RepeatColumnValue repNewRelationships = collectedValues.getRepeatColumn("newRelationships");
        List<ColumnValue> colNewMemberCodes = new ArrayList<>();
        List<ColumnValue> colNewMemberNames = new ArrayList<>();
        List<ColumnValue> colNewRelationships = new ArrayList<>();

        if (repNewRelationships != null) {
            for (int i = 0; i < repNewRelationships.getCount(); i++) {
                ColumnValue colNewMemberCode = repNewRelationships.get("newMemberCode", i);
                ColumnValue colNewMemberName = repNewRelationships.get("newMemberName", i);
                ColumnValue colNewRelationship = repNewRelationships.get("newRelationshipType", i);

                colNewMemberCodes.add(colNewMemberCode);
                colNewMemberNames.add(colNewMemberName);
                colNewRelationships.add(colNewRelationship);
            }
        }

        //ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        //ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        //ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String memberCode = colMemberCode.getValue();
        String memberName = colMemberName.getValue();
        Date deathDate = colDeathDate.getDateValue();
        String deathCause = colDeathCause.getValue();
        String deathCauseOther = colDeathCauseOther.getValue();
        String deathPlace = colDeathPlace.getValue();
        String deathPlaceOther = colDeathPlaceOther.getValue();
        Boolean isHouseholdHead = StringUtil.toBoolean(colIsHouseholdHead.getValue());
        String newHeadCode = colNewHeadCode.getValue();
        String newHeadName = colNewHeadName.getValue();

        List<String> newMemberCodes = colNewMemberCodes.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> newMemberNames = colNewMemberNames.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> newRelationships = colNewRelationships.stream().map(ColumnValue::getValue).collect(Collectors.toList());

        String affectedMembers = null;

        //Death
        Death death = new Death();
        death.visitCode = visitCode;
        death.memberCode = member.code;
        death.deathDate = deathDate;
        death.deathCause = deathCauseOther==null ? deathCause : deathCauseOther;
        death.deathPlace = deathPlaceOther==null ? deathPlace : deathPlaceOther;
        death.ageAtDeath = GeneralUtil.getAge(member.dob, deathDate);
        death.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        death.recentlyCreated = true;
        death.recentlyCreatedUri = result.getFilename();
        this.boxDeaths.put(death);

        this.member.endType = ResidencyEndType.DEATH;
        this.member.endDate = deathDate;
        this.boxMembers.put(this.member);

        //close memberResidency
        if (this.memberResidency != null){
            this.memberResidency.endType = ResidencyEndType.DEATH;
            this.memberResidency.endDate = deathDate;
            this.boxResidencies.put(this.memberResidency);
        }
        //close memberHeadRelationship
        if (this.memberHeadRelationship != null){
            this.memberHeadRelationship.endType = HeadRelationshipEndType.DEATH;
            this.memberHeadRelationship.endDate = deathDate;
            this.boxHeadRelationships.put(this.memberHeadRelationship);
        }
        //close memberMaritalRelationship
        if (this.memberMaritalRelationship != null){
            this.memberMaritalRelationship.endStatus = MaritalEndStatus.WIDOWED;
            this.memberMaritalRelationship.endDate = deathDate;
            this.boxMaritalRelationships.put(this.memberMaritalRelationship);

            this.member.maritalStatus = MaritalStatus.WIDOWED;
            this.spouseMember.maritalStatus = MaritalStatus.WIDOWED;
            this.boxMembers.put(this.member, this.spouseMember);

            affectedMembers = addAffectedMembers(affectedMembers, this.spouseMember.code);
        }
        //close previous head relationships
        if (headMemberHeadRelationships != null && headMemberHeadRelationships.size()>0){
            for (HeadRelationship headRelationship : headMemberHeadRelationships) {
                if (!this.memberHeadRelationship.equals(headRelationship)){
                    headRelationship.endType = HeadRelationshipEndType.DEATH_OF_HEAD_OF_HOUSEHOLD;
                    headRelationship.endDate = deathDate;
                    this.boxHeadRelationships.put(headRelationship);
                }
            }
        }
        //create new head relationships
        if (isHouseholdHead){
            for (int i = 0; i < colNewMemberCodes.size(); i++) {
                HeadRelationship headRelationship = new HeadRelationship();
                headRelationship.householdCode = household.code;
                headRelationship.memberCode = newMemberCodes.get(i);
                headRelationship.headCode = newHeadMember.code;
                headRelationship.relationshipType = HeadRelationshipType.getFrom(newRelationships.get(i));
                headRelationship.startType = HeadRelationshipStartType.NEW_HEAD_OF_HOUSEHOLD;
                headRelationship.startDate = GeneralUtil.getDateAdd(deathDate, 1);
                headRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
                headRelationship.endDate = null;
                this.boxHeadRelationships.put(headRelationship);
            }
            affectedMembers = addAffectedMembers(affectedMembers, this.newHeadMember.code);
        }


        //save core collected data
        collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.DEATH;
        collectedData.formEntityId = member.id;
        collectedData.formEntityCode = member.code;
        collectedData.formEntityCodes = affectedMembers; //new head code, spouse, head related individuals
        collectedData.formEntityName = member.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        collectedData.extension.setTarget(this.getFormExtension(collectedData.formEntity));
        this.boxCoreCollectedData.put(collectedData);

        if (listener != null) {
            listener.onNewEntityCreated(death);
        }

    }

    private String addAffectedMembers(String members, String memberCode) {
        if (StringUtil.isBlank(members)) {
            members = memberCode;
        } else {
            members += ";" + memberCode;
        }
        return members;
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

        if (isHouseholdHead) {
            //death of head of household - search for new head of household
            openNewHouseholdHeadFilterDialog();
        } else {
            executeCollectForm();
        }
    }

    private void openNewHouseholdHeadFilterDialog(){

        MemberFilterDialog dialog = MemberFilterDialog.newInstance(this.fragmentManager, context.getString(R.string.death_new_head_select_lbl), true, new MemberFilterDialog.Listener() {
            @Override
            public void onSelectedMember(Member member) {
                Log.d("selected-head", ""+member.getCode());

                newHeadMember = member;
                executeCollectForm();
            }

            @Override
            public void onCanceled() {

            }
        });

        dialog.setFilterMinAge(this.minimunHeadAge, true);
        dialog.setFilterHouseCode(visit.householdCode, true);
        dialog.setFilterStatus(MemberFilterDialog.StatusFilter.RESIDENT, true);
        dialog.setFilterExcludeMember(this.member.code);
        dialog.setStartSearchOnShow(true);
        dialog.show();
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

}
