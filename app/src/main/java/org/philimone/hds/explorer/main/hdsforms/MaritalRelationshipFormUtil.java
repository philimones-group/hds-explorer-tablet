package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.MaritalRelationshipDialog;
import org.philimone.hds.explorer.fragment.MemberFilterDialog;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Death;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.MaritalRelationship;
import org.philimone.hds.explorer.model.MaritalRelationship_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.Gender;
import org.philimone.hds.explorer.model.enums.MaritalEndStatus;
import org.philimone.hds.explorer.model.enums.MaritalEventType;
import org.philimone.hds.explorer.model.enums.MaritalStartStatus;
import org.philimone.hds.explorer.model.enums.MaritalStatus;
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
import java.util.Map;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

public class MaritalRelationshipFormUtil extends FormUtil<MaritalRelationship> {

    private Box<Member> boxMembers;
    private Box<Death> boxDeaths;
    private Box<MaritalRelationship> boxMaritalRelationships;
    
    private Visit visit;
    private Member spouseA;
    private Member spouseB;
    private Member savedSpouseB;
    private MaritalStatus savedSpouseBstatus;
    private MaritalRelationship currentMaritalRelationship; //* if it is a relationship to close *//
    private int minimunSpouseAge;
    private boolean genderChecking;
    private boolean spouseWillBeUpdated;

    private MaritalEventType eventType = MaritalEventType.START; /* default is starting event */
    private boolean isPolygamicRelationship = false;
    private String polygamicId; /* the first MaritalRelationship uuid */
    private boolean mainIsPolygamicRelationship;

    private MaritalRelationshipDialog maritalRelationshipDialog;

    public MaritalRelationshipFormUtil(Fragment fragment, Context context, Visit visit, Member spouseA, FormUtilities odkFormUtilities, FormUtilListener<MaritalRelationship> listener){
        super(fragment, context, FormUtil.getMaritalRelationshipForm(context), odkFormUtilities, listener);

        this.visit = visit;
        this.spouseA = spouseA;

        initBoxes();
        initialize();
    }

    public MaritalRelationshipFormUtil(Fragment fragment, Context context, Visit visit, MaritalRelationship relationshipToEdit, FormUtilities odkFormUtilities, FormUtilListener<MaritalRelationship> listener){
        super(fragment, context, FormUtil.getMaritalRelationshipForm(context), relationshipToEdit, odkFormUtilities, listener);


        this.visit = visit;
        initBoxes();
        initialize();

        this.spouseA = boxMembers.query().equal(Member_.code, relationshipToEdit.memberA_code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
        this.spouseB = boxMembers.query().equal(Member_.code, relationshipToEdit.memberB_code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        this.mainIsPolygamicRelationship = boxMaritalRelationships.query(MaritalRelationship_.polygamicId.equal(relationshipToEdit.collectedId)).build().count()>0;

        readSavedEntityState();
    }

    public static MaritalRelationshipFormUtil newInstance(Mode openMode, Fragment fragment, Context context, Visit visit, Member spouseA, MaritalRelationship relationshipToEdit, FormUtilities odkFormUtilities, FormUtilListener<MaritalRelationship> listener){
        if (openMode == Mode.CREATE) {
            return new MaritalRelationshipFormUtil(fragment, context, visit, spouseA, odkFormUtilities, listener);
        } else if (openMode == Mode.EDIT) {
            return new MaritalRelationshipFormUtil(fragment, context, visit, relationshipToEdit, odkFormUtilities, listener);
        }

        return null;
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxDeaths = ObjectBoxDatabase.get().boxFor(Death.class);
        this.boxMaritalRelationships = ObjectBoxDatabase.get().boxFor(MaritalRelationship.class);

    }

    @Override
    protected void initialize(){
        super.initialize();

        this.household = boxHouseholds.query(Household_.code.equal(visit.householdCode)).build().findFirst();
        this.minimunSpouseAge = retrieveMinimumSpouseAge();
        this.genderChecking = retrieveGenderChecking();
    }

    private void readSavedEntityState() {
        Map<String, String> mapSavedStates = new HashMap<>();

        SavedEntityState savedState = this.boxSavedEntityStates.query(SavedEntityState_.formEntity.equal(CoreFormEntity.MARITAL_RELATIONSHIP.code)
                .and(SavedEntityState_.collectedId.equal(this.entity.id))
                .and(SavedEntityState_.objectKey.equal("maritalFormUtilState"))).build().findFirst();

        if (savedState != null) {
            HashMap map = new Gson().fromJson(savedState.objectGsonValue, HashMap.class);
            for (Object key : map.keySet()) {
                mapSavedStates.put(key.toString(), map.get(key).toString());
            }
        }

        String strSpouseBId = mapSavedStates.get("spouseB_id");
        String strSpouseBstatus = mapSavedStates.get("spouseB_marital_status");

        if (strSpouseBId != null) {
            this.savedSpouseB = this.boxMembers.get(Long.parseLong(strSpouseBId));
        }
        if (strSpouseBstatus != null) {
            this.savedSpouseBstatus = MaritalStatus.getFrom(strSpouseBstatus);
        }

    }

    private boolean spouseBChanged() {
        return (savedSpouseB != null && savedSpouseB.id != spouseB.id);
    }

    @Override
    protected void preloadValues() {
        preloadedMap.put("visitCode", this.visit.code);
        preloadedMap.put("memberA", spouseA.code);
        preloadedMap.put("memberB", spouseB.code);
        preloadedMap.put("memberA_name", spouseA.name);
        preloadedMap.put("memberB_name", spouseB.name);
        preloadedMap.put("memberA_status", spouseA.maritalStatus.code);
        preloadedMap.put("memberB_status", spouseB.maritalStatus.code);
        preloadedMap.put("eventType", eventType.code);
        preloadedMap.put("isPolygamic", isPolygamicRelationship +"");
        preloadedMap.put("polygamicId", polygamicId==null ? "" : polygamicId);
    }

    @Override
    protected void preloadUpdatedValues() {
        if (spouseWillBeUpdated) {
            //spouse A dont need to be updated only spouseB
            //preloadedMap.put("memberA", spouseA.code);
            preloadedMap.put("memberB", spouseB.code);
            //preloadedMap.put("memberA_name", spouseA.name);
            preloadedMap.put("memberB_name", spouseB.name);
            //preloadedMap.put("memberA_status", spouseA.maritalStatus.code);
            preloadedMap.put("memberB_status", spouseB.maritalStatus.code);
        }
    }

    @Override
    public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colMemberA = collectedValues.get("memberA");
        ColumnValue colMemberA_name = collectedValues.get("memberA_name");
        ColumnValue colMemberB = collectedValues.get("memberB");
        ColumnValue colMemberB_name = collectedValues.get("memberB_name");
        ColumnValue colMemberA_status = collectedValues.get("memberA_status");
        ColumnValue colMemberB_status = collectedValues.get("memberB_status");
        ColumnValue colIsPolygamic = collectedValues.get("isPolygamic");
        ColumnValue colPolygamicId = collectedValues.get("polygamicId");
        ColumnValue colRelationshipType = collectedValues.get("relationshipType");
        ColumnValue colEventDate = collectedValues.get("eventDate");
        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");


        String visitCode = colVisitCode.getValue();
        String memberA = colMemberA.getValue();
        String memberA_name = colMemberA_name.getValue();
        String memberB = colMemberB.getValue();
        String memberB_name = colMemberB_name.getValue();
        MaritalStatus memberA_status = MaritalStatus.getFrom(colMemberA_status.getValue());
        MaritalStatus memberB_status = MaritalStatus.getFrom(colMemberB_status.getValue());
        Boolean isPolygamic = Boolean.parseBoolean(colIsPolygamic.getValue());
        String polygamicId = colPolygamicId.getValue();
        MaritalStartStatus startRelationType = MaritalStartStatus.getFrom(colRelationshipType.getValue());
        MaritalEndStatus endRelationType = MaritalEndStatus.getFrom(colRelationshipType.getValue());
        Date eventDate = colEventDate.getDateValue();

        //C1. Check Field not Blank
        if (StringUtil.isBlank(visitCode)){
            String message = this.context.getString(R.string.maritalrelationship_visit_code_empty_lbl);
            return new ValidationResult(colVisitCode, message);
        }

        if (StringUtil.isBlank(memberA)){
            String message = this.context.getString(R.string.maritalrelationship_member_a_code_empty_lbl);
            return new ValidationResult(colMemberA, message);
        }

        if (StringUtil.isBlank(memberB)){
            String message = this.context.getString(R.string.maritalrelationship_member_b_code_empty_lbl);
            return new ValidationResult(colMemberB, message);
        }

        if (memberA_status == null){
            String message = this.context.getString(R.string.maritalrelationship_member_a_status_empty_lbl);
            return new ValidationResult(colMemberA_status, message);
        }

        if (memberB_status == null){
            String message = this.context.getString(R.string.maritalrelationship_member_b_status_empty_lbl);
            return new ValidationResult(colMemberB_status, message);
        }

        if (eventDate == null){
            String message = this.context.getString(R.string.maritalrelationship_relationship_date_empty_lbl);
            return new ValidationResult(colEventDate, message);
        }

        //C2. Check If is a Valid Marital Status Event
        if (startRelationType == null && endRelationType == null){
            String message = this.context.getString(R.string.maritalrelationship_relationship_type_empty_lbl);
            return new ValidationResult(colRelationshipType, message);
        }

        //CX. Check Members equality
        if (memberA.equalsIgnoreCase(memberB)){
            String message = this.context.getString(R.string.maritalrelationship_same_member_lbl);
            return new ValidationResult(colMemberB, message);
        }

        //C4. Check Code reference existence
        if (Queries.getMemberByCode(boxMembers, memberA) == null){
            String message = this.context.getString(R.string.maritalrelationship_spouse_a_not_found_lbl);
                return new ValidationResult(colMemberA, message);
        }

        if (Queries.getMemberByCode(boxMembers, memberB) == null){
            String message = this.context.getString(R.string.maritalrelationship_spouse_b_not_found_lbl);
            return new ValidationResult(colMemberB, message);
        }

        //C5. Check Date is greater than today
        if (eventDate.after(new Date())){ //is in the future
            String message = this.context.getString(R.string.maritalrelationship_eventdate_not_great_today_lbl);
            return new ValidationResult(colEventDate, message);
        }

        //C6. Check Dates against DOB
        if (eventDate.before(spouseA.dob) || eventDate.before(spouseB.dob)){
            String message = this.context.getString(R.string.maritalrelationship_eventdate_not_before_member_dob_lbl);
            return new ValidationResult(colEventDate, message);
        }

        //C7. Check Spouse Age (must be greater or equals to 16)
        int ageA_to_eventdate = GeneralUtil.getAge(spouseA.dob, eventDate);
        int ageB_to_eventdate = GeneralUtil.getAge(spouseB.dob, eventDate);

        if (ageA_to_eventdate < minimunSpouseAge){
            String message = this.context.getString(R.string.maritalrelationship_dob_minimum_spouse_age_lbl, minimunSpouseAge +"");
            return new ValidationResult(colEventDate, message);
        }
        if (ageB_to_eventdate < minimunSpouseAge){
            String message = this.context.getString(R.string.maritalrelationship_dob_minimum_spouse_age_lbl, minimunSpouseAge +"");
            return new ValidationResult(colEventDate, message);
        }

        //C8. Check Spouse Gender (must be the opposite) [optional]
        if (genderChecking && spouseA.gender==spouseB.gender){
            String message = this.context.getString(R.string.maritalrelationship_gender_must_be_opposite_lbl);
            return new ValidationResult(colMemberA, message);
        }

        //C9. Check If memberA or memberB are alive
        if (Queries.getDeathByCode(boxDeaths, memberA) != null || Queries.getDeathByCode(boxDeaths, memberB) != null){
            String message = this.context.getString(R.string.maritalrelationship_dead_members_lbl);
            return new ValidationResult(colMemberA==null ? colMemberB : colMemberA, message);
        }

        //Start: MAR, LIV
        //End: DIV, SEP, WID
        if (currentMode == Mode.CREATE) {
            MaritalRelationship previousRelA = boxMaritalRelationships.query().equal(MaritalRelationship_.memberA_code, spouseA.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                    .or()
                    .equal(MaritalRelationship_.memberB_code, spouseA.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                    .orderDesc(MaritalRelationship_.startDate)
                    .build().findFirst();

            MaritalRelationship previousRelB = boxMaritalRelationships.query().equal(MaritalRelationship_.memberA_code, spouseB.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                    .or()
                    .equal(MaritalRelationship_.memberB_code, spouseB.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                    .orderDesc(MaritalRelationship_.startDate)
                    .build().findFirst();

            //New Relationship
            if (startRelationType != null) {
                //P1. Check If P.Relationship of A and B are closed
                //If is a polygamic relationship and A/B is a MALE dont need to check if is closed
                boolean isMalePolygamyA = spouseA.gender==Gender.MALE && isPolygamic;
                boolean isMalePolygamyB = spouseB.gender==Gender.MALE && isPolygamic;

                if (previousRelA != null && previousRelA.endStatus==MaritalEndStatus.NOT_APPLICABLE && !isMalePolygamyA){
                    String message = this.context.getString(R.string.maritalrelationship_p1_previous_not_closed_lbl, "A");
                    return new ValidationResult(colMemberA, message);
                }
                if (previousRelB != null && previousRelB.endStatus==MaritalEndStatus.NOT_APPLICABLE && !isMalePolygamyB){
                    String message = this.context.getString(R.string.maritalrelationship_p1_previous_not_closed_lbl, "B");
                    return new ValidationResult(colMemberB, message);
                }

                //P2. Check If endDate of P.Relationship is not before new startDate
                if (previousRelA != null && previousRelA.endDate != null && eventDate.compareTo(previousRelA.endDate)<=0 && !isMalePolygamyA){
                    String message = this.context.getString(R.string.maritalrelationship_p2_previous_enddate_overlap_lbl, "A");
                    return new ValidationResult(colMemberA, message);
                }
                if (previousRelB != null && previousRelB.endDate != null && eventDate.compareTo(previousRelB.endDate)<=0 && !isMalePolygamyB){
                    String message = this.context.getString(R.string.maritalrelationship_p2_previous_enddate_overlap_lbl, "B");
                    return new ValidationResult(colMemberB, message);
                }
            }

            //Closing a Relationship
            if (endRelationType != null) {
                //CP1. Check If Current Relationship Exists
                if (currentMaritalRelationship == null){
                    String message = this.context.getString(R.string.maritalrelationship_cp1_current_dont_exists_lbl);
                    return new ValidationResult(colMemberB, message);
                }
                //CP2. Check If EndType is not empty or NA
                if (currentMaritalRelationship.endStatus != MaritalEndStatus.NOT_APPLICABLE){
                    String message = this.context.getString(R.string.maritalrelationship_cp2_already_closed_lbl, currentMaritalRelationship.endStatus.code);
                    return new ValidationResult(colMemberB, message);
                }
                //CP3. Check If endDate is before or equal to startDate
                if (eventDate.compareTo(currentMaritalRelationship.startDate) <= 0){
                    String message = this.context.getString(R.string.maritalrelationship_cp3_dates_overlap_lbl);
                    return new ValidationResult(colMemberB, message);
                }
            }
        } else if (currentMode == Mode.EDIT) {
            //Ignore the current relationship
            MaritalRelationship previousRelA = boxMaritalRelationships.query(MaritalRelationship_.id.notEqual(currentMaritalRelationship.id).and(MaritalRelationship_.memberA_code.equal(spouseA.code).or(MaritalRelationship_.memberB_code.equal(spouseA.code))))
                    .orderDesc(MaritalRelationship_.startDate)
                    .build().findFirst();

            MaritalRelationship previousRelB = boxMaritalRelationships.query(MaritalRelationship_.id.notEqual(currentMaritalRelationship.id).and(MaritalRelationship_.memberA_code.equal(spouseB.code).or(MaritalRelationship_.memberB_code.equal(spouseB.code))))
                    .orderDesc(MaritalRelationship_.startDate)
                    .build().findFirst();

            //New Relationship
            if (startRelationType != null) {
                //P1. Check If P.Relationship of A and B are closed
                boolean isMalePolygamyA = spouseA.gender==Gender.MALE && (isPolygamic || mainIsPolygamicRelationship);
                boolean isMalePolygamyB = spouseB.gender==Gender.MALE && (isPolygamic || mainIsPolygamicRelationship);

                if (previousRelA != null && previousRelA.endStatus==MaritalEndStatus.NOT_APPLICABLE && !isMalePolygamyA){
                    String message = this.context.getString(R.string.maritalrelationship_p1_previous_not_closed_lbl, "A");
                    return new ValidationResult(colMemberA, message);
                }
                if (previousRelB != null && previousRelB.endStatus==MaritalEndStatus.NOT_APPLICABLE && !isMalePolygamyA){
                    String message = this.context.getString(R.string.maritalrelationship_p1_previous_not_closed_lbl, "B");
                    return new ValidationResult(colMemberB, message);
                }

                //P2. Check If endDate of P.Relationship is not before new startDate
                if (previousRelA != null && previousRelA.endDate != null && eventDate.compareTo(previousRelA.endDate)<=0 && !isMalePolygamyA){
                    String message = this.context.getString(R.string.maritalrelationship_p2_previous_enddate_overlap_lbl, "A");
                    return new ValidationResult(colMemberA, message);
                }
                if (previousRelB != null && previousRelB.endDate != null && eventDate.compareTo(previousRelB.endDate)<=0 && !isMalePolygamyA){
                    String message = this.context.getString(R.string.maritalrelationship_p2_previous_enddate_overlap_lbl, "B");
                    return new ValidationResult(colMemberB, message);
                }
            }

            //Closing a Relationship
            if (endRelationType != null) {
                //CP1. Check If Current Relationship Exists
                if (currentMaritalRelationship == null){
                    String message = this.context.getString(R.string.maritalrelationship_cp1_current_dont_exists_lbl);
                    return new ValidationResult(colMemberB, message);
                }
                //CP2. Check If EndType is not empty or NA - we are editing this is not applicable
                //if (currentMaritalRelationship.endStatus != MaritalEndStatus.NOT_APPLICABLE){
                //    String message = this.context.getString(R.string.maritalrelationship_cp2_already_closed_lbl, currentMaritalRelationship.endStatus.code);
                //    return new ValidationResult(colMemberB, message);
                //}
                //CP3. Check If endDate is before or equal to startDate
                if (eventDate.compareTo(currentMaritalRelationship.startDate) <= 0){
                    String message = this.context.getString(R.string.maritalrelationship_cp3_dates_overlap_lbl);
                    return new ValidationResult(colMemberB, message);
                }
            }
        }

        return ValidationResult.noErrors();
    }

    @Override
    public void onBeforeFormFinished(HForm form, CollectedDataMap collectedValues) {
        //using it to update collectedHouseholdId, collectedMemberId
        ColumnValue colHouseholdId = collectedValues.get("collectedHouseholdId");
        ColumnValue colMemberId = collectedValues.get("collectedMemberId");

        colHouseholdId.setValue(this.household.collectedId);
        colMemberId.setValue(this.spouseA.collectedId);
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
        ColumnValue colMemberA = collectedValues.get("memberA");
        //ColumnValue colMemberA_name = collectedValues.get("memberA_name");
        ColumnValue colMemberB = collectedValues.get("memberB");
        //ColumnValue colMemberB_name = collectedValues.get("memberB_name");
        //ColumnValue colMemberA_status = collectedValues.get("memberA_status");
        //ColumnValue colMemberB_status = collectedValues.get("memberB_status");
        ColumnValue colIsPolygamic = collectedValues.get("isPolygamic");
        ColumnValue colPolygamicId = collectedValues.get("polygamicId");
        ColumnValue colRelationshipType = collectedValues.get("relationshipType");
        ColumnValue colEventDate = collectedValues.get("eventDate");
        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");


        String visitCode = colVisitCode.getValue();
        String memberA = colMemberA.getValue();
        //String memberA_name = colMemberA_name.getValue();
        String memberB = colMemberB.getValue();
        //String memberB_name = colMemberB_name.getValue();
        //MaritalStatus memberA_status = MaritalStatus.getFrom(colMemberA_status.getValue());
        //MaritalStatus memberB_status = MaritalStatus.getFrom(colMemberB_status.getValue());
        Boolean isPolygamic = Boolean.parseBoolean(colIsPolygamic.getValue());
        String polygamicId = colPolygamicId.getValue();
        MaritalStartStatus startRelationType = MaritalStartStatus.getFrom(colRelationshipType.getValue());
        MaritalEndStatus endRelationType = MaritalEndStatus.getFrom(colRelationshipType.getValue());
        Date eventDate = colEventDate.getDateValue();


        //save spouseB maritalStatus for restoring in case edit changes the spouseB
        HashMap<String,String> saveStateMap = new HashMap<>();
        saveStateMap.put("spouseB_id", spouseB.id+"");
        saveStateMap.put("spouseB_marital_status", spouseB.maritalStatus.code);


        MaritalRelationship maritalRelationship = null;

        if (startRelationType != null){
            //creates a new relationship
            maritalRelationship = new MaritalRelationship();

            maritalRelationship.memberA_code = spouseA.code;
            maritalRelationship.memberB_code = spouseB.code;
            maritalRelationship.startStatus = startRelationType;
            maritalRelationship.startDate = eventDate;
            maritalRelationship.endStatus = MaritalEndStatus.NOT_APPLICABLE;
            maritalRelationship.endDate = null;
            maritalRelationship.isPolygamic = isPolygamic;
            maritalRelationship.polygamicId = polygamicId;

            maritalRelationship.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
            maritalRelationship.recentlyCreated = true;
            maritalRelationship.recentlyCreatedUri = result.getFilename();

            spouseA.maritalStatus = MaritalStatus.getFrom(startRelationType.code);
            spouseB.maritalStatus = MaritalStatus.getFrom(startRelationType.code);

            spouseA.spouseCode = spouseB.code;
            spouseA.spouseName = spouseB.name;
            spouseB.spouseCode = spouseA.code;
            spouseB.spouseName = spouseA.name;

        } else if (endRelationType != null) {
            //updates a existing relationship
            maritalRelationship = currentMaritalRelationship;

            maritalRelationship.endStatus = endRelationType;
            maritalRelationship.endDate = eventDate;

            spouseA.maritalStatus = MaritalStatus.getFrom(endRelationType.code);
            spouseB.maritalStatus = MaritalStatus.getFrom(endRelationType.code);

            spouseA.spouseCode = spouseB.code;
            spouseA.spouseName = spouseB.name;
            spouseB.spouseCode = spouseA.code;
            spouseB.spouseName = spouseA.name;
        }

        maritalRelationship.visitCode = visitCode;

        //save data
        boxMaritalRelationships.put(maritalRelationship);
        boxMembers.put(spouseA, spouseB);


        //save core collected data
        collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.MARITAL_RELATIONSHIP;
        collectedData.formEntityId = maritalRelationship.id;
        collectedData.formEntityCode = spouseA.code;
        collectedData.formEntityName = spouseA.name + " -> " + spouseB.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        collectedData.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        collectedData.extension.setTarget(this.getFormExtension(collectedData.formEntity));

        boxCoreCollectedData.put(collectedData);

        //save state for editing
        SavedEntityState entityState = new SavedEntityState(CoreFormEntity.MARITAL_RELATIONSHIP, maritalRelationship.id, "maritalFormUtilState", new Gson().toJson(saveStateMap));
        this.boxSavedEntityStates.put(entityState);

        this.entity = maritalRelationship;
        this.collectExtensionForm(collectedValues);
    }

    private void onModeEdit(CollectedDataMap collectedValues, XmlFormResult result) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colMemberA = collectedValues.get("memberA");
        //ColumnValue colMemberA_name = collectedValues.get("memberA_name");
        ColumnValue colMemberB = collectedValues.get("memberB");
        //ColumnValue colMemberB_name = collectedValues.get("memberB_name");
        //ColumnValue colMemberA_status = collectedValues.get("memberA_status");
        //ColumnValue colMemberB_status = collectedValues.get("memberB_status");
        ColumnValue colIsPolygamic = collectedValues.get("isPolygamic");
        ColumnValue colPolygamicId = collectedValues.get("polygamicId");
        ColumnValue colRelationshipType = collectedValues.get("relationshipType");
        ColumnValue colEventDate = collectedValues.get("eventDate");
        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");


        String visitCode = colVisitCode.getValue();
        String memberA = colMemberA.getValue();
        //String memberA_name = colMemberA_name.getValue();
        String memberB = colMemberB.getValue();
        //String memberB_name = colMemberB_name.getValue();
        //MaritalStatus memberA_status = MaritalStatus.getFrom(colMemberA_status.getValue());
        //MaritalStatus memberB_status = MaritalStatus.getFrom(colMemberB_status.getValue());
        Boolean isPolygamic = Boolean.parseBoolean(colIsPolygamic.getValue());
        String polygamicId = colPolygamicId.getValue();
        MaritalStartStatus startRelationType = MaritalStartStatus.getFrom(colRelationshipType.getValue());
        MaritalEndStatus endRelationType = MaritalEndStatus.getFrom(colRelationshipType.getValue());
        Date eventDate = colEventDate.getDateValue();


        //save spouseB maritalStatus before updating with new status
        HashMap<String,String> saveStateMap = new HashMap<>();
        saveStateMap.put("spouseB_id", spouseB.id+"");
        saveStateMap.put("spouseB_marital_status", spouseB.maritalStatus.code);

        if (spouseBChanged()) {
            //restore back the previous status of the old spouseB (the one that was substitued)
            savedSpouseB = boxMembers.get(savedSpouseB.id);
            savedSpouseB.maritalStatus = savedSpouseBstatus;
            this.boxMembers.put(savedSpouseB);
        }

        //refresh
        spouseA = boxMembers.get(spouseA.id);
        spouseB = boxMembers.get(spouseB.id);

        MaritalRelationship maritalRelationship = boxMaritalRelationships.get(this.entity.id);

        if (startRelationType != null){
            //creates a new relationship
            maritalRelationship.memberA_code = spouseA.code;
            maritalRelationship.memberB_code = spouseB.code;
            maritalRelationship.startStatus = startRelationType;
            maritalRelationship.startDate = eventDate;
            maritalRelationship.endStatus = MaritalEndStatus.NOT_APPLICABLE;
            maritalRelationship.endDate = null;

            spouseA.maritalStatus = MaritalStatus.getFrom(startRelationType.code);
            spouseB.maritalStatus = MaritalStatus.getFrom(startRelationType.code);

            spouseA.spouseCode = spouseB.code;
            spouseA.spouseName = spouseB.name;
            spouseB.spouseCode = spouseA.code;
            spouseB.spouseName = spouseA.name;

        } else if (endRelationType != null) {
            //updates a existing relationship

            maritalRelationship.endStatus = endRelationType;
            maritalRelationship.endDate = eventDate;

            spouseA.maritalStatus = MaritalStatus.getFrom(endRelationType.code);
            spouseB.maritalStatus = MaritalStatus.getFrom(endRelationType.code);

            spouseA.spouseCode = spouseB.code;
            spouseA.spouseName = spouseB.name;
            spouseB.spouseCode = spouseA.code;
            spouseB.spouseName = spouseA.name;
        }

        maritalRelationship.visitCode = visitCode;

        //save data
        boxMaritalRelationships.put(maritalRelationship);
        boxMembers.put(spouseA, spouseB);

        //save core collected data
        collectedData.formEntityCode = spouseA.code;
        collectedData.formEntityName = spouseA.name + " -> " + spouseB.name;
        collectedData.updatedDate = new Date();
        boxCoreCollectedData.put(collectedData);

        //save state for editing
        SavedEntityState entityState = this.boxSavedEntityStates.query(SavedEntityState_.formEntity.equal(CoreFormEntity.MARITAL_RELATIONSHIP.code).and(SavedEntityState_.collectedId.equal(this.entity.id)).and(SavedEntityState_.objectKey.equal("maritalFormUtilState"))).build().findFirst();
        this.boxSavedEntityStates.put(entityState);

        onFinishedExtensionCollection();
    }

    @Override
    protected void onFinishedExtensionCollection() {
        if (listener != null) {
            if (currentMode == Mode.CREATE) {
                updateMaritalRelationshipDialog();

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

    private MaritalRelationship getCurrentMaritalRelationship(Member mSpouseA) {
        if (mSpouseA == null) return null;

        MaritalRelationship maritalRelationship = boxMaritalRelationships.query().equal(MaritalRelationship_.memberA_code, mSpouseA.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .or()
                .equal(MaritalRelationship_.memberB_code, mSpouseA.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .orderDesc(MaritalRelationship_.startDate)
                .build().findFirst();

        return maritalRelationship;
    }

    private boolean isCurrentlyMarried(Member member) {
        MaritalRelationship maritalRelationship = getCurrentMaritalRelationship(member);
        return maritalRelationship != null && maritalRelationship.endStatus == MaritalEndStatus.NOT_APPLICABLE;
    }

    @Override
    public void collect() {

        if (currentMode == Mode.CREATE) {
            //1. selected member, spouseA can be male or female, spouseA should be husband and spouseB the female counterpart
            //2. create a new relationship

            Member selectedSpouse = spouseA;
            //Check if MemberA has a registered marital relationship? - //1. get last marital if still opened  or  //2. select a spouse
            MaritalRelationship maritalRelationship = getCurrentMaritalRelationship(selectedSpouse);
            boolean isCurrentlyMarried = maritalRelationship != null && maritalRelationship.endStatus == MaritalEndStatus.NOT_APPLICABLE;

            //if maritalRelationship is recentlyCreated -> go to edit mode
            /*if (maritalRelationship != null && maritalRelationship.isRecentlyCreated() ) {
                //There is a recently created Marital Relationship for this member, To Edit the Relationship use the collected list in the right panel and select the form
                DialogFactory.createMessageInfo(this.context, R.string.relationship_type_title_lbl, R.string.maritalrelationship_recently_created_error_lbl).show();
                return;
            }*/

            if (isCurrentlyMarried) {
                //this.currentMaritalRelationship = maritalRelationship;
                //if its about to close a relationship pull in the same order it was registered
                this.spouseA = boxMembers.query().equal(Member_.code, maritalRelationship.memberA_code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
                this.spouseB = boxMembers.query().equal(Member_.code, maritalRelationship.memberB_code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
                //reOrderSpouse();

                //show MaritalRelationship dialog
                //wife (selected member)
                openMaritalRelationshipDialog(selectedSpouse, isCurrentlyMarried, spouseA, spouseB);

            } else {
                //a new relationship will be created - then - select spouse B
                //Not married
                openSpouseFilterDialog(new SpouseFilterListener() {
                    @Override
                    public void onSelectedSpouse(Member member) {
                        Log.d("selected-spouse", ""+member.getCode()+", "+member.gender);
                        spouseB = member;
                        boolean memberMarried = isCurrentlyMarried(member);
                        //reOrderSpouse();

                        if (memberMarried) {
                            //show MaritalRelationship dialog
                            openMaritalRelationshipDialog(selectedSpouse, isCurrentlyMarried, spouseA, spouseB);
                        } else {
                            executeNewRegularRelationship();
                        }
                    }

                    @Override
                    public void onCanceled() {
                        spouseWillBeUpdated = false;
                    }
                });
            }

        } else if (currentMode == Mode.EDIT) {
            //edit a already created relationship
            //if is a new - try to update/change the spouse b

            this.currentMaritalRelationship = this.entity;

            if (this.currentMaritalRelationship.endStatus != MaritalEndStatus.NOT_APPLICABLE) {
                //is closing a relationship - cannot update the spouses while editing
                executeCollectForm();
            } else {
                checkChangeSpouseDialog();
            }
        }

    }

    private void openMaritalRelationshipDialog(Member selectedSpouse, boolean isCurrentlyMarried, Member mSpouseA, Member mSpouseB) {
        this.spouseA = mSpouseA;
        this.spouseB = mSpouseB;

        this.maritalRelationshipDialog = MaritalRelationshipDialog.newInstance(this.fragmentManager, selectedSpouse, isCurrentlyMarried, this.spouseA, this.spouseB, new MaritalRelationshipDialog.MrDialogListener() {
            @Override
            public void onEndingRelationship(MaritalRelationship maritalRelationship) {
                executeEndingRelationship(maritalRelationship);
            }

            @Override
            public void onAddNewPolygamicRelationship(MaritalRelationship mainMaritalRelationship, Member mSpouseA, Member mSpouseB) {
                executeNewPolygamicRelationship(mainMaritalRelationship, mSpouseA, mSpouseB);
            }

            @Override
            public void onCanceled() {

            }
        });
        maritalRelationshipDialog.setGenderChecking(genderChecking);
        maritalRelationshipDialog.setFastFilterHousehold(household);
        maritalRelationshipDialog.setFilterHouseholdCode(visit.householdCode);
        maritalRelationshipDialog.setFilterMinimunSpouseAge(minimunSpouseAge);
        maritalRelationshipDialog.show();
    }

    private void executeNewRegularRelationship() {
        eventType = MaritalEventType.START;
        isPolygamicRelationship = false;
        polygamicId = null;

        executeCollectForm();
    }

    private void executeNewPolygamicRelationship(MaritalRelationship mainMaritalRelationship, Member mSpouseA, Member mSpouseB){
        eventType = MaritalEventType.START;
        this.spouseA = mSpouseA;
        this.spouseB = mSpouseB;
        isPolygamicRelationship = true;
        polygamicId = mainMaritalRelationship.collectedId;

        executeCollectForm();
    }

    private void executeEndingRelationship(MaritalRelationship maritalRelationship) {
        eventType = MaritalEventType.END;

        currentMaritalRelationship = maritalRelationship;

        this.spouseA = boxMembers.query().equal(Member_.code, maritalRelationship.memberA_code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
        this.spouseB = boxMembers.query().equal(Member_.code, maritalRelationship.memberB_code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
        reOrderSpouse();

        executeCollectForm();
    }

    private int retrieveMinimumSpouseAge() {
        ApplicationParam param = this.boxAppParams.query().equal(ApplicationParam_.name, ApplicationParam.PARAMS_MIN_AGE_OF_SPOUSE, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (param != null) {
            try {
                return Integer.parseInt(param.value);
            } catch (Exception ex) {

            }
        }

        return 12;
    }

    private boolean retrieveGenderChecking() {
        ApplicationParam param = this.boxAppParams.query().equal(ApplicationParam_.name, ApplicationParam.PARAMS_GENDER_CHECKING, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (param != null) {
            try {
                return Boolean.parseBoolean(param.value);
            } catch (Exception ex) {

            }
        }

        return true;
    }

    private void openSpouseFilterDialog(SpouseFilterListener listener){

        MemberFilterDialog dialog = MemberFilterDialog.newInstance(this.fragmentManager, context.getString(R.string.maritalrelationship_spouse_select_lbl), true, new MemberFilterDialog.Listener() {
            @Override
            public void onSelectedMember(Member member) {
                listener.onSelectedSpouse(member);
            }

            @Override
            public void onCanceled() {
                listener.onCanceled();
            }
        });

        if (genderChecking) {
            if (spouseA.gender == Gender.MALE){
                dialog.setGenderFemaleOnly();
                dialog.addFilterExcludeMarried();
            } else {
                dialog.setGenderMaleOnly();
            }
        }

        dialog.setFilterMinAge(this.minimunSpouseAge, true);
        dialog.setFilterHouseCode(visit.householdCode);
        dialog.setFastFilterHousehold(household);
        dialog.addFilterExcludeMember(this.spouseA);
        dialog.addFilterExcludeMember(this.spouseB);
        dialog.setStartSearchOnShow(true);
        dialog.show();
    }

    private void checkChangeSpouseDialog(){

        DialogFactory.createMessageYN(this.context, R.string.maritalrelationship_spouse_select_update_lbl, R.string.maritalrelationship_spouse_change_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                spouseWillBeUpdated = true;
                openSpouseFilterDialog(new SpouseFilterListener() {
                    @Override
                    public void onSelectedSpouse(Member member) {
                        Log.d("selected-spouse", ""+member.getCode());

                        spouseB = member;
                        reOrderSpouse();
                        executeCollectForm();
                    }

                    @Override
                    public void onCanceled() {
                        spouseWillBeUpdated = false;
                    }
                });
            }

            @Override
            public void onNoClicked() {
                //spouse remains unchanged
                executeCollectForm();
            }
        }).show();

    }

    private void reOrderSpouse(){
        //male as A, B as female
        if (spouseA != null && spouseB != null) {
            if (spouseA.gender == Gender.FEMALE) {
                Member temp = this.spouseA;
                this.spouseA = this.spouseB;
                this.spouseB = temp;
            }
        }
    }

    private void updateMaritalRelationshipDialog(){
        if (this.maritalRelationshipDialog != null) {
            this.maritalRelationshipDialog.loadHusbandRelationships();
        }
    }

    private interface SpouseFilterListener {
        void onSelectedSpouse(Member member);

        void onCanceled();
    }
}
