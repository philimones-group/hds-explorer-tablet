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
import org.philimone.hds.explorer.model.Death_;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.HeadRelationship_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.MaritalRelationship;
import org.philimone.hds.explorer.model.MaritalRelationship_;
import org.philimone.hds.explorer.model.Member;
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
import org.philimone.hds.explorer.model.oldstate.SavedEntityState;
import org.philimone.hds.explorer.model.oldstate.SavedEntityState_;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.RepeatColumnValue;
import org.philimone.hds.forms.model.RepeatObject;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

public class DeathFormUtil extends FormUtil<Death> {

    //private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<Death> boxDeaths;
    private Box<HeadRelationship> boxHeadRelationships;
    private Box<Residency> boxResidencies;
    private Box<MaritalRelationship> boxMaritalRelationships;
    
    //private Household household;
    private Visit visit;
    private Member member;
    private Boolean isHouseholdHead = false;
    private Boolean isLastMemberOfHousehold = false;
    private Member newHeadMember;
    private Member previousNewHeadMember;
    private HeadRelationshipType previousNewHeadRelationshipType;
    private List<HeadRelationship> headMemberHeadRelationships;
    private Residency memberResidency;
    private HeadRelationship memberHeadRelationship;
    //private MaritalRelationship memberMaritalRelationship;
    private List<MaritalRelationship> memberMaritalRelationships = new ArrayList<>();
    //private Member spouseMember;
    private List<Member> spouseMembers = new ArrayList<>();
    private Map<String, String> mapSavedStates = new HashMap<>();

    private List<Member> householdResidents;
    private int minimunHeadAge;

    private DateUtil dateUtil = Bootstrap.getDateUtil();
    private boolean onlyMinorsLeftInHousehold;

    public DeathFormUtil(Fragment fragment, Context context, Visit visit, Household household, Member member, FormUtilities odkFormUtilities, FormUtilListener<Death> listener){
        super(fragment, context, FormUtil.getDeathForm(context), odkFormUtilities, listener);

        //Log.d("enu-household", ""+household);

        this.household = household;
        this.member = member;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    public DeathFormUtil(Fragment fragment, Context context, Visit visit, Household household, Death deathToEdit, FormUtilities odkFormUtilities, FormUtilListener<Death> listener){
        super(fragment, context, FormUtil.getDeathForm(context), deathToEdit, odkFormUtilities, listener);

        this.household = household;
        this.visit = visit;

        initBoxes();

        this.member = this.boxMembers.query(Member_.code.equal(deathToEdit.memberCode)).build().findFirst();

        initialize();
    }

    public static DeathFormUtil newInstance(Mode openMode, Fragment fragment, Context context, Visit visit, Household household, Member member, Death deathToEdit, FormUtilities odkFormUtilities, FormUtilListener<Death> listener){
        if (openMode == Mode.CREATE) {
            return new DeathFormUtil(fragment, context, visit, household, member, odkFormUtilities, listener);
        } else if (openMode == Mode.EDIT) {
            return new DeathFormUtil(fragment, context, visit, household, deathToEdit, odkFormUtilities, listener);
        }

        return null;
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        //this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxDeaths = ObjectBoxDatabase.get().boxFor(Death.class);
        this.boxHeadRelationships = ObjectBoxDatabase.get().boxFor(HeadRelationship.class);
        this.boxResidencies = ObjectBoxDatabase.get().boxFor(Residency.class);
        this.boxMaritalRelationships = ObjectBoxDatabase.get().boxFor(MaritalRelationship.class);

    }

    @Override
    protected void initialize(){
        super.initialize();

        this.minimunHeadAge = retrieveMinimumHeadAge();

        if (currentMode == Mode.CREATE) {
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
            this.memberMaritalRelationships = this.boxMaritalRelationships.query(
                    MaritalRelationship_.endStatus.equal(MaritalEndStatus.NOT_APPLICABLE.code)
                    .and(MaritalRelationship_.memberA_code.equal(this.member.code).or(MaritalRelationship_.memberB_code.equal(this.member.code))))
                    .orderDesc(MaritalRelationship_.startDate)
                    .build().find();

            calculateHelperVariables();

        } else if (currentMode == Mode.EDIT) {

            readSavedEntityState();

            //reading isHouseholdHead, memberResidency, memberHeadRelationship, memberMaritalRelationship, headMemberHeadRelationships
        }

        //get spouse
        /*if (this.memberMaritalRelationship != null) {
            String spouseCode = this.memberMaritalRelationship.memberA_code==member.code ? this.memberMaritalRelationship.memberB_code : this.memberMaritalRelationship.memberA_code;
            this.spouseMember = Queries.getMemberByCode(this.boxMembers, spouseCode);
        }*/
        
        if (this.memberMaritalRelationships != null) {
            for (MaritalRelationship maritalRelationship : this.memberMaritalRelationships) {
                String spouseCode = maritalRelationship.memberA_code==member.code ? maritalRelationship.memberB_code : maritalRelationship.memberA_code;
                Member mb = Queries.getMemberByCode(this.boxMembers, spouseCode);

                if (mb != null) {
                    this.spouseMembers.add(mb);
                }
            }
        }

        if (isHouseholdHead) { //is death of head of household
            this.householdResidents = getHouseholdResidents();
        }
    }

    private void readSavedEntityState() {
        SavedEntityState savedState = this.boxSavedEntityStates.query(SavedEntityState_.formEntity.equal(CoreFormEntity.DEATH.code)
                                                                        .and(SavedEntityState_.collectedId.equal(this.entity.id))
                                                                        .and(SavedEntityState_.objectKey.equal("deathFormUtilState"))).build().findFirst();

        if (savedState != null) {
            HashMap map = new Gson().fromJson(savedState.objectGsonValue, HashMap.class);
            for (Object key : map.keySet()) {
                mapSavedStates.put(key.toString(), map.get(key).toString());
            }
        }


        String newHeadCode = mapSavedStates.get("previousNewHeadCode");
        String headRelationshipType = mapSavedStates.get("previousNewHeadRelationshipType");
        String isHouseholdHeadVar = mapSavedStates.get("isHouseholdHead");
        String onlyMinorsLeftInHouseholdVar = mapSavedStates.get("onlyMinorsLeftInHousehold");
        String memberResidencyId = mapSavedStates.get("memberResidency");
        String memberHeadRelationshipId = mapSavedStates.get("memberHeadRelationship");
        String memberMaritalRelationshipIdList = mapSavedStates.get("memberMaritalRelationshipIdList");
        String headMemberRelationshipIdList = mapSavedStates.get("headMemberRelationshipIdList");

        if (newHeadCode != null) {
            this.previousNewHeadMember = this.boxMembers.query(Member_.code.equal(newHeadCode)).build().findFirst();
        }
        if (headRelationshipType != null) {
            this.previousNewHeadRelationshipType = HeadRelationshipType.getFrom(headRelationshipType);
        }
        if (isHouseholdHeadVar != null) {
            this.isHouseholdHead = Boolean.parseBoolean(isHouseholdHeadVar);
        }
        if (onlyMinorsLeftInHouseholdVar != null) {
            this.onlyMinorsLeftInHousehold = Boolean.parseBoolean(onlyMinorsLeftInHouseholdVar);
        }
        if (memberResidencyId != null) {
            this.memberResidency = this.boxResidencies.get(Long.parseLong(memberResidencyId));
        }
        if (memberHeadRelationshipId != null) {
            this.memberHeadRelationship = this.boxHeadRelationships.get(Long.parseLong(memberHeadRelationshipId));
        }
        if (!StringUtil.isBlank(memberMaritalRelationshipIdList)) {
            this.memberMaritalRelationships = new ArrayList<>();
            for (String strId : memberMaritalRelationshipIdList.split(",")) {
                MaritalRelationship maritalRelationship = this.boxMaritalRelationships.get(Long.parseLong(strId));
                this.memberMaritalRelationships.add(maritalRelationship);
            }
        }
        if (!StringUtil.isBlank(headMemberRelationshipIdList)) {
            this.headMemberHeadRelationships = new ArrayList<>();
            for (String strId : headMemberRelationshipIdList.split(",")) {
                HeadRelationship headRelationship = this.boxHeadRelationships.get(Long.parseLong(strId));
                this.headMemberHeadRelationships.add(headRelationship);
            }
        }
    }

    private boolean newHeadChanged(){
        return (this.previousNewHeadMember != null && this.newHeadMember != null) && (previousNewHeadMember.id != newHeadMember.id);
    }

    private List<Member> getHouseholdResidents() {

        //order list with new head of household as the first id
        //exclude the recently dead member
        List<Residency> residencies = this.boxResidencies.query(
                Residency_.householdCode.equal(this.household.code).and(Residency_.endType.equal(ResidencyEndType.NOT_APPLICABLE.code))
                        .and(Residency_.memberCode.notEqual(this.member.code)))
                .order(Residency_.memberCode)
                .build().find();

        //exclude old household code
        List<String> residentMembers = new ArrayList<>();
        for (Residency residency : residencies) {
            residentMembers.add(residency.memberCode);
        }

        List<Member> members = this.boxMembers.query(Member_.code.oneOf(residentMembers.toArray(new String[0]))).build().find();

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

        if (this.householdResidents != null && newHeadMember != null) {
            reorderWithHeadAsFirst(this.householdResidents, newHeadMember);
        }

        preloadedMap.put("visitCode", this.visit.code);
        preloadedMap.put("memberCode", this.member.code);
        preloadedMap.put("memberName", this.member.name);
        preloadedMap.put("isHouseholdHead", this.isHouseholdHead+"");
        preloadedMap.put("isLastMember", this.isLastMemberOfHousehold+"");

        if (isHouseholdHead && newHeadMember != null){
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
        if (newHeadChanged()) {

            if (this.householdResidents != null && newHeadMember != null) {
                reorderWithHeadAsFirst(this.householdResidents, newHeadMember);
            }

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

        if (currentMode==Mode.CREATE && boxDeaths.query(Death_.memberCode.equal(memberCode)).build().count()>0){
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

        if (deathDate != null && deathDate.after(new Date())){ //is after today
            String message = this.context.getString(R.string.death_deathdate_not_before_dob_lbl);
            return new ValidationResult(colDeathDate, message);
        }

        if (isHouseholdHead && !isLastMemberOfHousehold && StringUtil.isBlank(newHeadCode)){
            String message = this.context.getString(R.string.death_new_head_code_empty_lbl);
            return new ValidationResult(colNewHeadCode, message);
        }

        //other validations
        if (isHouseholdHead){

            //check dates of relationships of new head, old head and members of household - they are the current relationships - endType=NA
            //HeadRelationship newHeadLastRelationship = getLastHeadRelationship(newHeadCode);
            //List<HeadRelationship> lastMembersRelationships = getLastHeadRelationships(newMemberCodes);

            //deathDate vs newHeadLastRelationship.startDate - NOT NEEDED BECAUSE THE NEW HEAD IS A MEMBER OF THIS HOUSEHOLD SO ITS CHECKED ON THE FOR LOOP
            /*
            if (newHeadLastRelationship != null && newHeadLastRelationship.startDate != null && deathDate.before(newHeadLastRelationship.startDate)) {
                //The death date cannot be before the [start date] of the [new Head of Household] last Head Relationship record.
                String message = this.context.getString(R.string.death_deathdate_not_before_new_head_hrelationship_startdate_lbl, StringUtil.formatYMD(deathDate), newHeadLastRelationship.startType.code, StringUtil.formatYMD(newHeadLastRelationship.startDate));
                return new ValidationResult(colDeathDate, message);
            }*/

            //We are doublechecking relationships start dates of the current members of this household before the death of head of household
            //headMemberHeadRelationships on both Mode.CREATE or Mode.EDIT are the relationships of the members of this household before the death of the head
            for (HeadRelationship relationship : headMemberHeadRelationships) {//lastMembersRelationships) {
                if (relationship != null && relationship.startDate != null && deathDate.before(relationship.startDate)) {
                    //The death date cannot be before the [start date] of the Member[??????] last Head Relationship record.
                    String message = this.context.getString(R.string.death_deathdate_not_before_member_hrelationship_startdate_lbl, dateUtil.formatYMD(deathDate), relationship.memberCode, relationship.startType.code, dateUtil.formatYMD(relationship.startDate));
                    return new ValidationResult(colDeathDate, message);
                }
            }
        }

        return ValidationResult.noErrors();
    }

    private HeadRelationship getLastHeadRelationship(String memberCode) {
        if (StringUtil.isBlank(memberCode)) return null;

        HeadRelationship lastHeadRelationship = this.boxHeadRelationships.query(HeadRelationship_.memberCode.equal(memberCode))
                .orderDesc(HeadRelationship_.startDate)
                .build().findFirst();

        return lastHeadRelationship;
    }

    private List<HeadRelationship> getLastHeadRelationships(List<String> memberCodes) {
        List<HeadRelationship> list = new ArrayList<>();

        for (String memberCode : memberCodes) {
            HeadRelationship hr = getLastHeadRelationship(memberCode);
            if (hr != null) list.add(hr);
        }

        return list;
    }

    private Residency getLastResidency(String memberCode) {
        if (StringUtil.isBlank(memberCode)) return null;

        Residency lastResidency = this.boxResidencies.query(Residency_.memberCode.equal(memberCode))
                .orderDesc(Residency_.startDate)
                .build().findFirst();

        return lastResidency;
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

    private void onModeEdit(CollectedDataMap collectedValues, XmlFormResult result) {

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
        Death death = entity;
        death.visitCode = visitCode;
        death.memberCode = member.code;
        death.deathDate = deathDate;
        death.deathCause = deathCauseOther==null ? deathCause : deathCauseOther;
        death.deathPlace = deathPlaceOther==null ? deathPlace : deathPlaceOther;
        death.ageAtDeath = GeneralUtil.getAge(member.dob, deathDate);
        //death.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        //death.recentlyCreated = true;
        //death.recentlyCreatedUri = result.getFilename();
        this.boxDeaths.put(death);

        //update member again
        this.member = boxMembers.get(member.id);
        this.member.ageAtDeath = death.ageAtDeath;
        this.member.endType = ResidencyEndType.DEATH;
        this.member.endDate = deathDate;
        this.boxMembers.put(this.member);

        //close memberResidency again
        if (this.memberResidency != null){
            this.memberResidency = boxResidencies.get(memberResidency.id);
            this.memberResidency.endType = ResidencyEndType.DEATH;
            this.memberResidency.endDate = deathDate;
            this.boxResidencies.put(this.memberResidency);
        }
        //close memberHeadRelationship again
        if (this.memberHeadRelationship != null){
            this.memberHeadRelationship = boxHeadRelationships.get(memberHeadRelationship.id);
            this.memberHeadRelationship.endType = HeadRelationshipEndType.DEATH;
            this.memberHeadRelationship.endDate = deathDate;
            this.boxHeadRelationships.put(this.memberHeadRelationship);
        }
        //close memberMaritalRelationship again
        if (this.memberMaritalRelationships != null) {
            for (MaritalRelationship maritalRelationship : this.memberMaritalRelationships) {
                if (maritalRelationship != null) {
                    maritalRelationship = boxMaritalRelationships.get(maritalRelationship.id);
                    maritalRelationship.endStatus = MaritalEndStatus.WIDOWED;
                    maritalRelationship.endDate = deathDate;
                    this.boxMaritalRelationships.put(maritalRelationship);

                    this.member = boxMembers.get(member.id);
                    this.member.maritalStatus = MaritalStatus.WIDOWED;
                    this.boxMembers.put(this.member);
                }
            }

            if (this.spouseMembers != null) {
                for (Member spouse : this.spouseMembers) {
                    spouse = boxMembers.get(spouse.id);
                    spouse.maritalStatus = MaritalStatus.WIDOWED;
                    this.boxMembers.put(spouse);

                    affectedMembers = addAffectedMembers(affectedMembers, spouse.code);
                }
            }
        }
        
        //close previous head relationships again
        if (isHouseholdHead && headMemberHeadRelationships != null && headMemberHeadRelationships.size()>0){
            for (HeadRelationship headRelationship : headMemberHeadRelationships) {
                headRelationship = boxHeadRelationships.get(headRelationship.id);
                headRelationship.endType = HeadRelationshipEndType.DEATH_OF_HEAD_OF_HOUSEHOLD;
                headRelationship.endDate = deathDate;
                this.boxHeadRelationships.put(headRelationship);
            }
        }

        //if the head was changed
        if (isHouseholdHead && newHeadChanged()){

            //delete previous data
            String sidsList = mapSavedStates.get("newHeadRelationshipsList");

            //deleting head relationships and update member
            for (String sid : sidsList.split(",")) {
                this.boxHeadRelationships.remove(Long.parseLong(sid));
            }

            previousNewHeadMember = boxMembers.get(previousNewHeadMember.id);
            previousNewHeadMember.headRelationshipType = previousNewHeadRelationshipType;
            this.boxMembers.put(previousNewHeadMember);


            //create new head relationships
            //save the new head member previous headRelationshipType
            HashMap<String,String> saveStateMap = new HashMap<>();
            saveStateMap.put("newHeadCode", newHeadMember.code);
            saveStateMap.put("headRelationshipType", newHeadMember.headRelationshipType.code);

            this.household.headCode = newHeadMember.code;
            this.household.headName = newHeadMember.name;
            newHeadMember.headRelationshipType = HeadRelationshipType.HEAD_OF_HOUSEHOLD;
            //--------------------
            this.boxHouseholds.put(this.household);
            this.boxMembers.put(newHeadMember);

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

                String newHeadIds = !saveStateMap.containsKey("newHeadRelationshipsList") ? headRelationship.id+"" : saveStateMap.get("newHeadRelationshipsList") + "," + headRelationship.id;
                saveStateMap.put("newHeadRelationshipsList", newHeadIds);
            }

            //update the saved entity state
            SavedEntityState entityState = this.boxSavedEntityStates.query(SavedEntityState_.formEntity.equal(CoreFormEntity.DEATH.code).and(SavedEntityState_.collectedId.equal(this.entity.id)).and(SavedEntityState_.objectKey.equal("deathFormUtilState"))).build().findFirst();
            entityState.objectGsonValue = new Gson().toJson(saveStateMap);
            this.boxSavedEntityStates.put(entityState);

            affectedMembers = addAffectedMembers(affectedMembers, this.newHeadMember.code);
        }

        //save core collected data
        collectedData.visitId = visit.id;
        collectedData.formEntityCodes = affectedMembers; //new head code, spouse, head related individuals
        collectedData.updatedDate = new Date();
        this.boxCoreCollectedData.put(collectedData);

        onFinishedExtensionCollection();
    }

    private void onModeCreate(CollectedDataMap collectedValues, XmlFormResult result) {
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

        this.member = boxMembers.get(member.id);
        this.member.ageAtDeath = death.ageAtDeath;
        this.member.endType = ResidencyEndType.DEATH;
        this.member.endDate = deathDate;
        this.boxMembers.put(this.member);

        //close memberResidency
        if (this.memberResidency != null){
            this.memberResidency = boxResidencies.get(memberResidency.id);
            this.memberResidency.endType = ResidencyEndType.DEATH;
            this.memberResidency.endDate = deathDate;
            this.boxResidencies.put(this.memberResidency);
        }
        //close memberHeadRelationship
        if (this.memberHeadRelationship != null){
            this.memberHeadRelationship = boxHeadRelationships.get(memberHeadRelationship.id);
            this.memberHeadRelationship.endType = HeadRelationshipEndType.DEATH;
            this.memberHeadRelationship.endDate = deathDate;
            this.boxHeadRelationships.put(this.memberHeadRelationship);
        }

        //close memberMaritalRelationship
        String savedMemberMaritalRelationshipIdList = "";
        if (this.memberMaritalRelationships != null) {
            for (MaritalRelationship maritalRelationship : this.memberMaritalRelationships) {
                if (maritalRelationship != null) {
                    maritalRelationship = boxMaritalRelationships.get(maritalRelationship.id);
                    maritalRelationship.endStatus = MaritalEndStatus.WIDOWED;
                    maritalRelationship.endDate = deathDate;
                    this.boxMaritalRelationships.put(maritalRelationship);

                    //only the spouse that is alive will be WIDOWED
                    //this.member.maritalStatus = MaritalStatus.WIDOWED;
                    //this.boxMembers.put(this.member);

                    savedMemberMaritalRelationshipIdList += (savedMemberMaritalRelationshipIdList.isEmpty() ? "" : ",") + maritalRelationship.id;
                }
            }

            if (this.spouseMembers != null) {
                for (Member spouse : this.spouseMembers) {
                    spouse = boxMembers.get(spouse.id);
                    spouse.maritalStatus = MaritalStatus.WIDOWED;
                    this.boxMembers.put(spouse);
                    affectedMembers = addAffectedMembers(affectedMembers, spouse.code);
                }
            }
        }
        //close previous head relationships
        String savedOldHeadRelationships = "";
        if (isHouseholdHead && headMemberHeadRelationships != null && headMemberHeadRelationships.size()>0){
            for (HeadRelationship headRelationship : headMemberHeadRelationships) {
                headRelationship = boxHeadRelationships.get(headRelationship.id);
                headRelationship.endType = HeadRelationshipEndType.DEATH_OF_HEAD_OF_HOUSEHOLD;
                headRelationship.endDate = deathDate;
                this.boxHeadRelationships.put(headRelationship);

                savedOldHeadRelationships += (savedOldHeadRelationships.isEmpty() ? "" : ",") + headRelationship.id;
            }
        }

        //save states
        HashMap<String,String> saveStateMap = new HashMap<>();
        saveStateMap.put("isHouseholdHead", isHouseholdHead+"");
        saveStateMap.put("onlyMinorsLeftInHousehold", onlyMinorsLeftInHousehold+"");
        saveStateMap.put("memberResidency", memberResidency == null ? "" : memberResidency.id+"");
        saveStateMap.put("memberHeadRelationship", memberHeadRelationship == null ? "" : memberHeadRelationship.id+"");
        //saveStateMap.put("memberMaritalRelationship", memberMaritalRelationship == null ? "" : memberMaritalRelationship.id+"");
        saveStateMap.put("memberMaritalRelationshipIdList", savedMemberMaritalRelationshipIdList);
        saveStateMap.put("headMemberRelationshipIdList", savedOldHeadRelationships);

        //create new head relationships
        if (isHouseholdHead && newHeadMember != null){
            //read again the head member because we might have updated the maritalStatus before (if was the spouse of the head)
            this.newHeadMember = this.boxMembers.get(this.newHeadMember.id);

            saveStateMap.put("previousNewHeadCode", newHeadMember.code);
            saveStateMap.put("previousNewHeadRelationshipType", newHeadMember.headRelationshipType.code);

            this.household = boxHouseholds.get(household.id);
            this.household.headCode = newHeadMember.code;
            this.household.headName = newHeadMember.name;
            newHeadMember.headRelationshipType = HeadRelationshipType.HEAD_OF_HOUSEHOLD;
            //--------------------
            this.boxHouseholds.put(this.household);
            this.boxMembers.put(newHeadMember);

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

                String newHeadIds = !saveStateMap.containsKey("newHeadRelationshipsList") ? headRelationship.id+"" : saveStateMap.get("newHeadRelationshipsList") + "," + headRelationship.id;
                saveStateMap.put("newHeadRelationshipsList", newHeadIds);
            }
            //save the list of ids of new head relationships
            SavedEntityState entityState = new SavedEntityState(CoreFormEntity.DEATH, death.id, "deathFormUtilState", new Gson().toJson(saveStateMap));
            this.boxSavedEntityStates.put(entityState);

            affectedMembers = addAffectedMembers(affectedMembers, this.newHeadMember.code);
        }

        //save core collected data
        collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.DEATH;
        collectedData.formEntityId = death.id;
        collectedData.formEntityCode = member.code;
        collectedData.formEntityCodes = affectedMembers; //new head code, spouse, head related individuals
        collectedData.formEntityName = member.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        collectedData.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        collectedData.extension.setTarget(this.getFormExtension(collectedData.formEntity));
        this.boxCoreCollectedData.put(collectedData);

        this.entity = death;
        this.collectExtensionForm(collectedValues);
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

    private String addAffectedMembers(String members, String memberCode) {
        if (StringUtil.isBlank(memberCode)) return members;
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
        if (currentMode == Mode.CREATE) {
            if (isHouseholdHead && !isLastMemberOfHousehold) {
                //death of head of household - search for new head of household
                openNewHouseholdHeadFilterDialog();
            } else {
                executeCollectForm();
            }
        } else if (currentMode == Mode.EDIT) {
            if (isHouseholdHead && !isLastMemberOfHousehold) {
                //ask if wants to change the head
                checkChangeNewHouseholdHeadDialog();
            } else {
               executeCollectForm();
            }
        }

    }

    private void calculateHelperVariables(){
        String memberCode = member.code;
        String householdCode = household.code;

        List<Residency> houseResidents = boxResidencies.query(Residency_.householdCode.equal(householdCode).and(Residency_.endType.equal(ResidencyEndType.NOT_APPLICABLE.code))).orderDesc(Residency_.startDate).build().find();

        this.isLastMemberOfHousehold = (houseResidents.size()==1 && !StringUtil.isBlank(memberCode) && memberCode.equals(houseResidents.get(0).memberCode));

        this.onlyMinorsLeftInHousehold = !isLastMemberOfHousehold;
        for (Residency residency : houseResidents) {
            Member m = boxMembers.query(Member_.code.equal(residency.memberCode)).build().findFirst();
            if (m != null && !m.code.equals(memberCode)) { //exclude the member who died
                this.onlyMinorsLeftInHousehold = this.onlyMinorsLeftInHousehold && m.age < minimunHeadAge;
                if (!this.onlyMinorsLeftInHousehold) return;
            }
        }
    }

    private void checkChangeNewHouseholdHeadDialog(){

        DialogFactory.createMessageYN(this.context, R.string.death_dialog_change_title_lbl, R.string.death_dialog_head_change_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                openNewHouseholdHeadFilterDialog();
            }

            @Override
            public void onNoClicked() {
                executeCollectForm();
            }
        }).show();

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

        dialog.setFilterMinAge(onlyMinorsLeftInHousehold ? 0 : minimunHeadAge, true);
        dialog.setFilterHouseCode(visit.householdCode, true);
        dialog.setFilterStatus(MemberFilterDialog.StatusFilter.RESIDENT, true);
        dialog.addFilterExcludeMember(this.member);
        dialog.setStartSearchOnShow(true);


        if (onlyMinorsLeftInHousehold) {
            DialogFactory.createMessageInfo(context, R.string.death_hoh_removal_info_title_lbl, R.string.death_hoh_minors_left_warning_lbl, clickedButton -> {
                dialog.show();
            }).show();
        } else {
            dialog.show();
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

}
