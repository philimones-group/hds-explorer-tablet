package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.MemberFilterDialog;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.HeadRelationship_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Residency;
import org.philimone.hds.explorer.model.Residency_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.HeadRelationshipType;
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
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

public class ChangeHeadFormUtil extends FormUtil<HeadRelationship> {

    private Box<Member> boxMembers;
    private Box<Residency> boxResidencies;
    //private Box<Household> boxHouseholds;
    private Box<HeadRelationship> boxHeadRelationships;
    //private Household household;
    private Visit visit;
    private Member oldHeadMember;
    private Member newHeadMember;
    private Member newHeadMemberAboutToChange;
    private HeadRelationshipType newHeadMemberPreviousRelatType;
    private List<HeadRelationship> oldHeadMemberRelationships;
    private HeadRelationship oldHeadMemberRelationship;
    private Map<String, String> mapSavedStates = new HashMap<>();

    private List<Member> householdResidents;
    private int minimunHeadAge;

    public ChangeHeadFormUtil(Fragment fragment, Context context, Visit visit, Household household, FormUtilities odkFormUtilities, FormUtilListener<HeadRelationship> listener){
        super(fragment, context, FormUtil.getChangeHeadForm(context), odkFormUtilities, listener);

        //Log.d("enu-household", ""+household);

        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    public ChangeHeadFormUtil(Fragment fragment, Context context, Visit visit, Household household, HeadRelationship headRelationshipToEdit, FormUtilities odkFormUtilities, FormUtilListener<HeadRelationship> listener){
        super(fragment, context, FormUtil.getChangeHeadForm(context), headRelationshipToEdit, odkFormUtilities, listener);

        this.household = household;
        this.visit = visit;

        initBoxes();

        this.newHeadMember = this.boxMembers.query(Member_.code.equal(headRelationshipToEdit.headCode)).build().findFirst();
        this.newHeadMemberAboutToChange = this.newHeadMember; //to save the new head - that can be changed when call collect()

        readSavedEntityState();

        initialize();
    }

    public static ChangeHeadFormUtil newInstance(Mode openMode, Fragment fragment, Context context, Visit visit, Household household, HeadRelationship headRelationshipToEdit, FormUtilities odkFormUtilities, FormUtilListener<HeadRelationship> listener){
        if (openMode == Mode.CREATE) {
            return new ChangeHeadFormUtil(fragment, context, visit, household, odkFormUtilities, listener);
        } else if (openMode == Mode.EDIT) {
            return new ChangeHeadFormUtil(fragment, context, visit, household, headRelationshipToEdit, odkFormUtilities, listener);
        }

        return null;
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxHeadRelationships = ObjectBoxDatabase.get().boxFor(HeadRelationship.class);
        //this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxResidencies = ObjectBoxDatabase.get().boxFor(Residency.class);
    }

    @Override
    protected void initialize(){
        super.initialize();

        if (currentMode == Mode.CREATE) {
            //get current memberHeadRelationship
            /*this.oldHeadMemberRelationship = this.boxHeadRelationships.query(
                                         HeadRelationship_.householdCode.equal(this.household.code)
                                    .and(HeadRelationship_.relationshipType.equal(HeadRelationshipType.HEAD_OF_HOUSEHOLD.code))
                                    .and(HeadRelationship_.endType.equal(HeadRelationshipEndType.NOT_APPLICABLE.code)))
                    .orderDesc(HeadRelationship_.startDate)
                    .build().findFirst();

            if (this.oldHeadMemberRelationship != null) {
                this.oldHeadMember = Queries.getMemberByCode(boxMembers, this.oldHeadMemberRelationship.memberCode);
            }*/

            this.oldHeadMemberRelationships = this.boxHeadRelationships.query(
                                         HeadRelationship_.householdCode.equal(this.household.code)
                                    .and(HeadRelationship_.endType.equal(HeadRelationshipEndType.NOT_APPLICABLE.code)))
                    .orderDesc(HeadRelationship_.startDate)
                    .build().find();

        } else if (currentMode == Mode.EDIT) {

            //oldHeadMember - is the original head member that will be changed     - read from readSavedEntityState

            //oldHeadMemberRelationship - is the original head member relationship - read from readSavedEntityState

            //oldHeadMemberRelationships - the relationships of the original head that are closed now
        }

        this.minimunHeadAge = retrieveMinimumHeadAge();
        this.householdResidents = getHouseholdResidents();

    }

    private void readSavedEntityState() {
        SavedEntityState savedState = this.boxSavedEntityStates.query(SavedEntityState_.formEntity.equal(CoreFormEntity.CHANGE_HOUSEHOLD_HEAD.code)
                .and(SavedEntityState_.collectedId.equal(this.entity.id))
                .and(SavedEntityState_.objectKey.equal("changeHeadFormUtilState"))).build().findFirst();

        if (savedState != null) {
            HashMap map = new Gson().fromJson(savedState.objectGsonValue, HashMap.class);
            for (Object key : map.keySet()) {
                mapSavedStates.put(key.toString(), map.get(key).toString());
            }
        }

        String oldHeadCode = mapSavedStates.get("oldHeadCode");
        String oldHeadRelatId = mapSavedStates.get("oldHeadMemberRelationshipId");
        String oldHeadRelatIdList = mapSavedStates.get("oldHeadMemberRelationshipIdList");
        String newHeadPreviousRelType = mapSavedStates.get("newHeadPreviousRelationshipType");

        if (oldHeadCode != null) {
            this.oldHeadMember = this.boxMembers.query(Member_.code.equal(oldHeadCode)).build().findFirst();
        }
        if (oldHeadRelatId != null) {
            this.oldHeadMemberRelationship = this.boxHeadRelationships.get(Long.parseLong(oldHeadRelatId));
        }
        if (newHeadPreviousRelType != null) {
            this.newHeadMemberPreviousRelatType = HeadRelationshipType.getFrom(newHeadPreviousRelType);
        }
        if (oldHeadRelatIdList != null) {
            this.oldHeadMemberRelationships = new ArrayList<>();
            for (String strId : oldHeadRelatIdList.split(",")) {
                HeadRelationship headRelationship = this.boxHeadRelationships.get(Long.parseLong(strId));
                this.oldHeadMemberRelationships.add(headRelationship);
            }
        }

    }

    private boolean newHeadChanged(){
        return (this.newHeadMemberAboutToChange != null && this.newHeadMember != null) && (newHeadMemberAboutToChange.id != newHeadMember.id);
    }

    private List<Member> getHouseholdResidents() {
        List<Residency> residencies = this.boxResidencies.query(
                Residency_.householdCode.equal(this.household.code).and(Residency_.endType.equal(ResidencyEndType.NOT_APPLICABLE.code)))
                .order(Residency_.memberCode)
                .build().find();

        //exclude old household head code
        List<String> residentMembers = new ArrayList<>();
        for (Residency residency : residencies) {
            residentMembers.add(residency.memberCode);
        }

        List<Member> members = this.boxMembers.query(Member_.code.oneOf(residentMembers.toArray(new String[0]))).order(Member_.code).build().find();

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

        reorderWithHeadAsFirst(this.householdResidents, newHeadMember);

        preloadedMap.put("visitCode", this.visit.code);
        preloadedMap.put("householdCode", this.household.code);

        if (this.oldHeadMember != null) {
            preloadedMap.put("oldHeadCode", this.oldHeadMember.code);
            preloadedMap.put("oldHeadName", this.oldHeadMember.name);
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
        //Log.d("relationships", newRelationshipsRepObj.getList().size()+"");
        preloadedMap.put("relationships", newRelationshipsRepObj);
        preloadedMap.put("modules", this.user.getSelectedModulesCodes());
    }

    @Override
    protected void preloadUpdatedValues() {
        if (newHeadChanged()) {

            if (this.householdResidents != null) {
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
            preloadedMap.put("relationships", newRelationshipsRepObj);
        }
    }

    @Override
    public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colHouseholdCode = collectedValues.get("householdCode");
        ColumnValue colOldHeadCode = collectedValues.get("oldHeadCode");
        ColumnValue colOldHeadName = collectedValues.get("oldHeadName");
        ColumnValue colNewHeadCode = collectedValues.get("newHeadCode");
        ColumnValue colNewHeadName = collectedValues.get("newHeadName");
        ColumnValue colEventDate = collectedValues.get("eventDate"); //not null cannot be in the future nor before dob
        ColumnValue colReason = collectedValues.get("reason");
        ColumnValue colReasonOther = collectedValues.get("reasonOther");

        RepeatColumnValue repNewRelationships = collectedValues.getRepeatColumn("relationships");
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
        String oldHeadCode = colOldHeadCode.getValue();
        String oldHeadName = colOldHeadName.getValue();
        String newHeadCode = colNewHeadCode.getValue();
        String newHeadName = colNewHeadName.getValue();
        Date eventDate = colEventDate.getDateValue();
        String reason = colReason.getValue();
        String reasonOther = colReasonOther.getValue();

        List<String> newMemberCodes = colNewMemberCodes.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> newMemberNames = colNewMemberNames.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> newRelationships = colNewRelationships.stream().map(ColumnValue::getValue).collect(Collectors.toList());

        //validations

        if (StringUtil.isBlank(visitCode)){
            String message = this.context.getString(R.string.changehead_visit_code_empty_lbl);
            return new ValidationResult(colVisitCode, message);
        }

        /*if (StringUtil.isBlank(oldHeadCode)){
            String message = this.context.getString(R.string.changehead_oldhead_code_empty_lbl);
            return new ValidationResult(colOldHeadCode, message);
        }*/

        if (StringUtil.isBlank(newHeadCode)){
            String message = this.context.getString(R.string.changehead_newhead_code_empty_lbl);
            return new ValidationResult(colNewHeadCode, message);
        }

        if (eventDate == null) {
            String message = this.context.getString(R.string.changehead_eventdate_empty_lbl);
            return new ValidationResult(colEventDate, message);
        }

        //eventDate cannot be before dob
        if (eventDate != null && eventDate.before(this.newHeadMember.dob)){ //is before dob
            String message = this.context.getString(R.string.changehead_eventdate_not_before_dob_lbl);
            return new ValidationResult(colEventDate, message);
        }

        if (eventDate != null && eventDate.after(new Date())){ //is before dob
            String message = this.context.getString(R.string.changehead_eventdate_not_great_today_lbl);
            return new ValidationResult(colEventDate, message);
        }

        //other validations
        //C6. Check Age of the new head of Household - done by filtering

        //check if new head member is a head of household of another household
        if (isHeadOfHouseholdSomewhere(newHeadCode) && (currentMode==Mode.CREATE || currentMode==Mode.EDIT && newHeadChanged())) {
            String message = this.context.getString(R.string.changehead_new_head_is_head_of_household_lbl);
            return new ValidationResult(colNewHeadCode, message);
        }

        //check dates of relationships of new head, old head and members of household - they are the current relationships - endType=NA
        HeadRelationship newHeadLastRelationship = getLastHeadRelationship(newHeadCode);
        List<HeadRelationship> lastMembersRelationships = oldHeadMemberRelationships;

        //eventDate vs newHeadLastRelationship.startDate
        if (newHeadLastRelationship != null && newHeadLastRelationship.startDate != null && eventDate.before(newHeadLastRelationship.startDate)){
            //The event date cannot be before the [start date] of the [new Head of Household] last Head Relationship record.
            String message = this.context.getString(R.string.changehead_eventdate_not_before_new_head_hrelationship_startdate_lbl, StringUtil.formatYMD(eventDate), newHeadLastRelationship.startType.code, StringUtil.formatYMD(newHeadLastRelationship.startDate));
            return new ValidationResult(colEventDate, message);
        }

        for (HeadRelationship relationship : lastMembersRelationships) {
            if (relationship != null && relationship.startDate != null && eventDate.before(relationship.startDate)){
                //The event date cannot be before the [start date] of the Member[??????] last Head Relationship record.
                String message = this.context.getString(R.string.changehead_eventdate_not_before_member_hrelationship_startdate_lbl, StringUtil.formatYMD(eventDate), relationship.memberCode, relationship.startType.code, StringUtil.formatYMD(relationship.startDate));
                return new ValidationResult(colEventDate, message);
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

    private Residency getLastResidency(String memberCode) {
        if (StringUtil.isBlank(memberCode)) return null;

        Residency lastResidency = this.boxResidencies.query(Residency_.memberCode.equal(memberCode))
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

    @Override
    public void onBeforeFormFinished(HForm form, CollectedDataMap collectedValues) {
        //using it to update collectedHouseholdId, collectedMemberId
        ColumnValue colHouseholdId = collectedValues.get("collectedHouseholdId");
        ColumnValue colMemberId = collectedValues.get("collectedMemberId");

        colHouseholdId.setValue(this.household.collectedId);
        colMemberId.setValue(this.newHeadMember.collectedId);
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
        ColumnValue colHouseholdCode = collectedValues.get("householdCode");
        ColumnValue colOldHeadCode = collectedValues.get("oldHeadCode");
        ColumnValue colOldHeadName = collectedValues.get("oldHeadName");
        ColumnValue colNewHeadCode = collectedValues.get("newHeadCode");
        ColumnValue colNewHeadName = collectedValues.get("newHeadName");
        ColumnValue colEventDate = collectedValues.get("eventDate"); //not null cannot be in the future nor before dob
        ColumnValue colReason = collectedValues.get("reason");
        ColumnValue colReasonOther = collectedValues.get("reasonOther");

        RepeatColumnValue repNewRelationships = collectedValues.getRepeatColumn("relationships");
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
        String oldHeadCode = colOldHeadCode.getValue();
        String oldHeadName = colOldHeadName.getValue();
        String newHeadCode = colNewHeadCode.getValue();
        String newHeadName = colNewHeadName.getValue();
        Date eventDate = colEventDate.getDateValue();
        String reason = colReason.getValue();
        String reasonOther = colReasonOther.getValue();

        List<String> newMemberCodes = colNewMemberCodes.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> newMemberNames = colNewMemberNames.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> newRelationships = colNewRelationships.stream().map(ColumnValue::getValue).collect(Collectors.toList());

        String affectedMembers = null;

        //new head head relationship
        HeadRelationship newHeadRelationship = new HeadRelationship();
        newHeadRelationship.householdCode = household.code;
        newHeadRelationship.memberCode = newHeadMember.code;
        newHeadRelationship.headCode = newHeadMember.code;
        newHeadRelationship.relationshipType = HeadRelationshipType.HEAD_OF_HOUSEHOLD;
        newHeadRelationship.startType = HeadRelationshipStartType.NEW_HEAD_OF_HOUSEHOLD;
        newHeadRelationship.startDate = GeneralUtil.getDateAdd(eventDate, 1);
        newHeadRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
        newHeadRelationship.endDate = null;
        newHeadRelationship.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        newHeadRelationship.recentlyCreated = true;
        newHeadRelationship.recentlyCreatedUri = result.getFilename();
        this.boxHeadRelationships.put(newHeadRelationship);

        this.household.headCode = newHeadMember.code;
        this.household.headName = newHeadMember.name;
        this.boxHouseholds.put(this.household);

        //close memberHeadRelationship
        if (this.oldHeadMemberRelationship != null){
            this.oldHeadMemberRelationship.endType = HeadRelationshipEndType.CHANGE_OF_HEAD_OF_HOUSEHOLD;
            this.oldHeadMemberRelationship.endDate = eventDate;
            this.boxHeadRelationships.put(this.oldHeadMemberRelationship);
        }
        //close old head relationships
        String savedOldHeadRelationships = "";
        if (oldHeadMemberRelationships != null && oldHeadMemberRelationships.size()>0){
            for (HeadRelationship headRelationship : oldHeadMemberRelationships) {
                headRelationship = boxHeadRelationships.get(headRelationship.id);
                headRelationship.endType = HeadRelationshipEndType.CHANGE_OF_HEAD_OF_HOUSEHOLD;
                headRelationship.endDate = eventDate;
                this.boxHeadRelationships.put(headRelationship);

                savedOldHeadRelationships += (savedOldHeadRelationships.isEmpty() ? "" : ",") + headRelationship.id;
            }
        }

        //save the new head member previous headRelationshipType
        HashMap<String,String> saveStateMap = new HashMap<>();
        saveStateMap.put("oldHeadCode", oldHeadMember != null ? oldHeadMember.code : ""); //the head that is being chang
        saveStateMap.put("oldHeadMemberRelationshipIdList", savedOldHeadRelationships);
        saveStateMap.put("oldHeadMemberRelationshipId", oldHeadMemberRelationship != null ? oldHeadMemberRelationship.id+"" : "");

        saveStateMap.put("newHeadPreviousRelationshipType", newHeadMember.headRelationshipType.code);

        newHeadMember.headRelationshipType = HeadRelationshipType.HEAD_OF_HOUSEHOLD;
        this.boxMembers.put(newHeadMember);

        //create new head relationships
        for (int i = 0; i < colNewMemberCodes.size(); i++) {
            HeadRelationship headRelationship = new HeadRelationship();
            headRelationship.householdCode = household.code;
            headRelationship.memberCode = newMemberCodes.get(i);
            headRelationship.headCode = newHeadMember.code;
            headRelationship.relationshipType = HeadRelationshipType.getFrom(newRelationships.get(i));
            headRelationship.startType = HeadRelationshipStartType.NEW_HEAD_OF_HOUSEHOLD;
            headRelationship.startDate = GeneralUtil.getDateAdd(eventDate, 1);
            headRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
            headRelationship.endDate = null;
            this.boxHeadRelationships.put(headRelationship);

            Member member = boxMembers.query(Member_.code.equal(headRelationship.memberCode)).build().findFirst();
            if (member != null) {
                //update member new relationship
                member.headRelationshipType = headRelationship.relationshipType;
                this.boxMembers.put(member);
            }

            String newHeadIds = !saveStateMap.containsKey("newHeadRelationshipsList") ? headRelationship.id+"" : saveStateMap.get("newHeadRelationshipsList") + "," + headRelationship.id;
            saveStateMap.put("newHeadRelationshipsList", newHeadIds);
        }

        //save the list of ids of new head relationships
        SavedEntityState entityState = new SavedEntityState(CoreFormEntity.CHANGE_HOUSEHOLD_HEAD, newHeadRelationship.id, "changeHeadFormUtilState", new Gson().toJson(saveStateMap));
        this.boxSavedEntityStates.put(entityState);

        affectedMembers = addAffectedMembers(affectedMembers, this.newHeadMember.code);

        //save core collected data
        collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.CHANGE_HOUSEHOLD_HEAD;
        collectedData.formEntityId = newHeadRelationship.id;
        collectedData.formEntityCode = newHeadMember.code;
        collectedData.formEntityCodes = affectedMembers; //new head code, spouse, head related individuals
        collectedData.formEntityName = (oldHeadMember != null ? oldHeadMember.name : "#") +" -> "+newHeadMember.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        collectedData.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        collectedData.extension.setTarget(this.getFormExtension(collectedData.formEntity));
        this.boxCoreCollectedData.put(collectedData);

        this.entity = newHeadRelationship;
        this.collectExtensionForm(collectedValues);
    }

    private void onModeEdit(CollectedDataMap collectedValues, XmlFormResult result) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colHouseholdCode = collectedValues.get("householdCode");
        ColumnValue colOldHeadCode = collectedValues.get("oldHeadCode");
        ColumnValue colOldHeadName = collectedValues.get("oldHeadName");
        ColumnValue colNewHeadCode = collectedValues.get("newHeadCode");
        ColumnValue colNewHeadName = collectedValues.get("newHeadName");
        ColumnValue colEventDate = collectedValues.get("eventDate"); //not null cannot be in the future nor before dob
        ColumnValue colReason = collectedValues.get("reason");
        ColumnValue colReasonOther = collectedValues.get("reasonOther");

        RepeatColumnValue repNewRelationships = collectedValues.getRepeatColumn("relationships");
        List<ColumnValue> colNewMemberCodes = new ArrayList<>();
        List<ColumnValue> colNewMemberNames = new ArrayList<>();
        List<ColumnValue> colNewRelationships = new ArrayList<>();
        Map<String,Integer> mapCodeIndexes = new HashMap<>();

        if (repNewRelationships != null) {
            for (int i = 0; i < repNewRelationships.getCount(); i++) {
                ColumnValue colNewMemberCode = repNewRelationships.get("newMemberCode", i);
                ColumnValue colNewMemberName = repNewRelationships.get("newMemberName", i);
                ColumnValue colNewRelationship = repNewRelationships.get("newRelationshipType", i);

                colNewMemberCodes.add(colNewMemberCode);
                colNewMemberNames.add(colNewMemberName);
                colNewRelationships.add(colNewRelationship);

                mapCodeIndexes.put(colNewMemberCode.getValue(), i);
            }
        }

        //ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        //ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        //ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String oldHeadCode = colOldHeadCode.getValue();
        String oldHeadName = colOldHeadName.getValue();
        String newHeadCode = colNewHeadCode.getValue();
        String newHeadName = colNewHeadName.getValue();
        Date eventDate = colEventDate.getDateValue();
        String reason = colReason.getValue();
        String reasonOther = colReasonOther.getValue();

        List<String> newMemberCodes = colNewMemberCodes.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> newMemberNames = colNewMemberNames.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> newRelationships = colNewRelationships.stream().map(ColumnValue::getValue).collect(Collectors.toList());

        String affectedMembers = null;

        //new head head relationship
        HeadRelationship newHeadRelationship = boxHeadRelationships.get(this.entity.id);
        newHeadRelationship.householdCode = household.code;
        newHeadRelationship.memberCode = newHeadMember.code;
        newHeadRelationship.headCode = newHeadMember.code;
        newHeadRelationship.relationshipType = HeadRelationshipType.HEAD_OF_HOUSEHOLD;
        newHeadRelationship.startType = HeadRelationshipStartType.NEW_HEAD_OF_HOUSEHOLD;
        newHeadRelationship.startDate = GeneralUtil.getDateAdd(eventDate, 1);
        newHeadRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
        newHeadRelationship.endDate = null;
        //newHeadRelationship.recentlyCreated = true;
        //newHeadRelationship.recentlyCreatedUri = result.getFilename();
        //this.boxHeadRelationships.put(newHeadRelationship);

        this.household.headCode = newHeadMember.code;
        this.household.headName = newHeadMember.name;
        this.boxHouseholds.put(this.household);

        //close memberHeadRelationship again
        if (this.oldHeadMemberRelationship != null){
            this.oldHeadMemberRelationship = boxHeadRelationships.get(oldHeadMemberRelationship.id);
            this.oldHeadMemberRelationship.endType = HeadRelationshipEndType.CHANGE_OF_HEAD_OF_HOUSEHOLD;
            this.oldHeadMemberRelationship.endDate = eventDate;
            this.boxHeadRelationships.put(this.oldHeadMemberRelationship);
        }
        //close old head relationships again
        String savedOldHeadRelationships = "";
        if (oldHeadMemberRelationships != null && oldHeadMemberRelationships.size()>0){
            for (HeadRelationship headRelationship : oldHeadMemberRelationships) {
                headRelationship = boxHeadRelationships.get(headRelationship.id);
                headRelationship.endType = HeadRelationshipEndType.CHANGE_OF_HEAD_OF_HOUSEHOLD;
                headRelationship.endDate = eventDate;
                this.boxHeadRelationships.put(headRelationship);

                savedOldHeadRelationships += (savedOldHeadRelationships.isEmpty() ? "" : ",") + headRelationship.id;
            }
        }

        //if the head was changed
        if (newHeadChanged()){

            //change the previous new head relationshipType
            String sidsList = mapSavedStates.get("newHeadRelationshipsList");
            newHeadMemberAboutToChange = boxMembers.get(newHeadMemberAboutToChange.id);
            newHeadMemberAboutToChange.headRelationshipType = newHeadMemberPreviousRelatType;
            //deleting the previous new head relationships and update member
            for (String sid : sidsList.split(",")) {
                this.boxHeadRelationships.remove(Long.parseLong(sid));
            }
            this.boxMembers.put(newHeadMemberAboutToChange);


            //save the new head member previous headRelationshipType
            HashMap<String,String> saveStateMap = new HashMap<>();
            saveStateMap.put("oldHeadCode", oldHeadMember != null ? oldHeadMember.code : ""); //the head that is being changed
            saveStateMap.put("oldHeadMemberRelationshipId", oldHeadMemberRelationship != null ? oldHeadMemberRelationship.id+"" : "");
            saveStateMap.put("oldHeadMemberRelationshipIdList", savedOldHeadRelationships);
            saveStateMap.put("newHeadPreviousRelationshipType", newHeadMember.headRelationshipType.code);

            newHeadMember.headRelationshipType = HeadRelationshipType.HEAD_OF_HOUSEHOLD;
            this.boxMembers.put(newHeadMember);

            //create new head relationships
            for (int i = 0; i < colNewMemberCodes.size(); i++) {
                HeadRelationship headRelationship = new HeadRelationship();
                headRelationship.householdCode = household.code;
                headRelationship.memberCode = newMemberCodes.get(i);
                headRelationship.headCode = newHeadMember.code;
                headRelationship.relationshipType = HeadRelationshipType.getFrom(newRelationships.get(i));
                headRelationship.startType = HeadRelationshipStartType.NEW_HEAD_OF_HOUSEHOLD;
                headRelationship.startDate = GeneralUtil.getDateAdd(eventDate, 1);
                headRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
                headRelationship.endDate = null;
                this.boxHeadRelationships.put(headRelationship);

                Member member = boxMembers.query(Member_.code.equal(headRelationship.memberCode)).build().findFirst();
                if (member != null) {
                    //update member new relationship
                    member.headRelationshipType = headRelationship.relationshipType;
                    this.boxMembers.put(member);
                }

                String newHeadIds = !saveStateMap.containsKey("newHeadRelationshipsList") ? headRelationship.id+"" : saveStateMap.get("newHeadRelationshipsList") + "," + headRelationship.id;
                saveStateMap.put("newHeadRelationshipsList", newHeadIds);
            }

            //update the saved entity state
            SavedEntityState entityState = this.boxSavedEntityStates.query(SavedEntityState_.formEntity.equal(CoreFormEntity.CHANGE_HOUSEHOLD_HEAD.code).and(SavedEntityState_.collectedId.equal(this.entity.id)).and(SavedEntityState_.objectKey.equal("changeHeadFormUtilState"))).build().findFirst();
            entityState.objectGsonValue = new Gson().toJson(saveStateMap);
            this.boxSavedEntityStates.put(entityState);

            affectedMembers = addAffectedMembers(affectedMembers, this.newHeadMember.code);
        } else {

            //the new head remains - get the new head relationships with the members of the household (saved as state)
            //Update the Head Relationships
            String sidsList = mapSavedStates.get("newHeadRelationshipsList");
            for (String strId: sidsList.split(",")) {
                HeadRelationship headRelationship = this.boxHeadRelationships.get(Long.parseLong(strId));

                Integer rindex = mapCodeIndexes.get(headRelationship.memberCode);

                headRelationship.householdCode = household.code;
                //headRelationship.memberCode = newMemberCodes.get(i);
                headRelationship.headCode = newHeadMember.code;
                headRelationship.relationshipType = HeadRelationshipType.getFrom(newRelationships.get(rindex));
                headRelationship.startType = HeadRelationshipStartType.NEW_HEAD_OF_HOUSEHOLD;
                headRelationship.startDate = GeneralUtil.getDateAdd(eventDate, 1);
                headRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
                headRelationship.endDate = null;

                this.boxHeadRelationships.put(headRelationship);

                if (headRelationship.memberCode.equals(oldHeadCode)) {
                    //update old head relationship
                    if (oldHeadMember != null) {
                        oldHeadMember.headRelationshipType = headRelationship.relationshipType;
                        this.boxMembers.put(oldHeadMember);
                    }
                }
            }
        }

        //save core collected data
        collectedData.visitId = visit.id;
        collectedData.formEntityCodes = affectedMembers; //new head code, spouse, head related individuals
        collectedData.formEntityName = (oldHeadMember != null ? oldHeadMember.name : "#") +" -> "+newHeadMember.name;
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

        if (this.currentMode == Mode.CREATE) {
            checkHeadOfHouseholdDialog();
        } else if (this.currentMode == Mode.EDIT) {
            //retrieveHeadOfHousehold();
            checkChangeNewHouseholdHeadDialog();
        }
    }

    private void retrieveHeadOfHousehold() {
        this.oldHeadMemberRelationship = getHeadOfHouseholdRelationship();
        this.oldHeadMember = getHeadOfHousehold(this.oldHeadMemberRelationship);
    }

    private HeadRelationship getHeadOfHouseholdRelationship() {

        HeadRelationship headRelationship = boxHeadRelationships.query(
                HeadRelationship_.householdCode.equal(household.code)
                        .and(HeadRelationship_.relationshipType.equal(HeadRelationshipType.HEAD_OF_HOUSEHOLD.code))
                        .and(HeadRelationship_.endType.equal(HeadRelationshipEndType.NOT_APPLICABLE.code))
        ).orderDesc(HeadRelationship_.startDate).build().findFirst();

        return headRelationship;
    }

    private Member getHeadOfHousehold(HeadRelationship headRelationship) {
        if (headRelationship != null) {
            return Queries.getMemberByCode(boxMembers, headRelationship.memberCode);
        }
        return null;
    }

    private void checkHeadOfHouseholdDialog() {
        retrieveHeadOfHousehold();

        if (this.oldHeadMember == null) {
            DialogFactory.createMessageInfo(this.context, R.string.eventType_change_of_hoh, R.string.changehead_head_dont_exists_lbl, new DialogFactory.OnClickListener() {
                @Override
                public void onClicked(DialogFactory.Buttons clickedButton) {
                    openNewHouseholdHeadFilterDialog();
                }
            }).show();
        } else {
            openNewHouseholdHeadFilterDialog();
        }
    }

    private void checkChangeNewHouseholdHeadDialog(){

        DialogFactory.createMessageYN(this.context, R.string.changehead_dialog_change_title_lbl, R.string.changehead_dialog_head_change_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                openNewHouseholdHeadFilterDialog();
            }

            @Override
            public void onNoClicked() {
                //head remains unchanged
                executeCollectForm();
            }
        }).show();

    }

    private void openNewHouseholdHeadFilterDialog(){

        MemberFilterDialog dialog = MemberFilterDialog.newInstance(this.fragmentManager, context.getString(R.string.changehead_new_head_select_lbl), true, new MemberFilterDialog.Listener() {
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
        if (this.oldHeadMember != null) {
            dialog.addFilterExcludeMember(this.oldHeadMember);
        }
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
