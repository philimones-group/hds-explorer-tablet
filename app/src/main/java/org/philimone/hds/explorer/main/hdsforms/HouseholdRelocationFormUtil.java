package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.HouseholdFilterDialog;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.HeadRelationship_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.HouseholdRelocation;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Residency;
import org.philimone.hds.explorer.model.Residency_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.converters.StringCollectionConverter;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.HeadRelationshipType;
import org.philimone.hds.explorer.model.enums.HouseholdRelocationReason;
import org.philimone.hds.explorer.model.enums.HouseholdType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipEndType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipStartType;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

public class HouseholdRelocationFormUtil extends FormUtil<HouseholdRelocation> {

    private Box<Member> boxMembers;
    private Box<Residency> boxResidencies;
    private Box<HeadRelationship> boxHeadRelationships;
    private Box<HouseholdRelocation> boxHouseholdRelocations;
    //private Household household;
    private Visit visit;
    private Household originHousehold;
    private Household destinationHousehold;
    private Member headMember;
    private Map<String, String> mapSavedStates = new HashMap<>();
    public static final String SAVED_ENTITY_OBJECT_KEY = "householdRelocationFormUtilState";
    private List<Long> oldMembersResidenciesList = new ArrayList<>();
    private List<Long> oldMembersRelationshipsList = new ArrayList<>();
    private List<Long> newMembersResidenciesList = new ArrayList<>();
    private List<Long> newMembersRelationshipsList = new ArrayList<>();
    private boolean originHouseholdChanged = false;

    private DateUtil dateUtil = Bootstrap.getDateUtil();

    public HouseholdRelocationFormUtil(Fragment fragment, Context context, Visit visit, Household household, FormUtilities odkFormUtilities, FormUtilListener<HouseholdRelocation> listener){
        super(fragment, context, FormUtil.getHouseholdRelocationForm(context), odkFormUtilities, listener);

        //Log.d("enu-household", ""+household);

        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    public HouseholdRelocationFormUtil(Fragment fragment, Context context, Visit visit, Household household, HouseholdRelocation householdRelocationToEdit, FormUtilities odkFormUtilities, FormUtilListener<HouseholdRelocation> listener){
        super(fragment, context, FormUtil.getHouseholdRelocationForm(context), householdRelocationToEdit, odkFormUtilities, listener);

        this.household = household;
        this.visit = visit;

        initBoxes();

        readSavedEntityState();

        initialize();
    }

    public static HouseholdRelocationFormUtil newInstance(Mode openMode, Fragment fragment, Context context, Visit visit, Household household, HouseholdRelocation householdRelocationToEdit, FormUtilities odkFormUtilities, FormUtilListener<HouseholdRelocation> listener){
        if (openMode == Mode.CREATE) {
            return new HouseholdRelocationFormUtil(fragment, context, visit, household, odkFormUtilities, listener);
        } else if (openMode == Mode.EDIT) {
            return new HouseholdRelocationFormUtil(fragment, context, visit, household, householdRelocationToEdit, odkFormUtilities, listener);
        }

        return null;
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxHeadRelationships = ObjectBoxDatabase.get().boxFor(HeadRelationship.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxResidencies = ObjectBoxDatabase.get().boxFor(Residency.class);
        this.boxHouseholdRelocations = ObjectBoxDatabase.get().boxFor(HouseholdRelocation.class);
    }

    @Override
    protected void initialize(){
        super.initialize();

        this.destinationHousehold = household;

        if (currentMode == Mode.CREATE) {


        } else if (currentMode == Mode.EDIT) {

            this.destinationHousehold = boxHouseholds.query(Household_.code.equal(this.entity.destinationCode)).build().findFirst();
            this.originHousehold = boxHouseholds.query(Household_.code.equal(this.entity.originCode)).build().findFirst();
            this.headMember = boxMembers.query(Member_.code.equal(this.entity.headCode)).build().findFirst();
        }
    }

    private void readSavedEntityState() {
        SavedEntityState savedState = this.boxSavedEntityStates.query(SavedEntityState_.formEntity.equal(CoreFormEntity.HOUSEHOLD_RELOCATION.code)
                .and(SavedEntityState_.collectedId.equal(this.entity.id))
                .and(SavedEntityState_.objectKey.equal(SAVED_ENTITY_OBJECT_KEY))).build().findFirst();

        if (savedState != null) {
            HashMap map = new Gson().fromJson(savedState.objectGsonValue, HashMap.class);
            for (Object key : map.keySet()) {
                mapSavedStates.put(key.toString(), map.get(key).toString());
            }
        }

        String oldResidencyList = mapSavedStates.get("oldMembersResidenciesList");
        String oldHeadRelatList = mapSavedStates.get("oldMembersRelationshipsList");
        String newResidencyList = mapSavedStates.get("newMembersResidenciesList");
        String newHeadRelatList = mapSavedStates.get("newMembersRelationshipsList");

        if (oldResidencyList != null) {
            for (String strId : oldResidencyList.split(",")) {
                if (!StringUtil.isBlank(strId))
                    oldMembersResidenciesList.add(Long.parseLong(strId));
            }
        }
        if (oldHeadRelatList != null) {
            for (String strId : oldHeadRelatList.split(",")) {
                if (!StringUtil.isBlank(strId))
                    oldMembersRelationshipsList.add(Long.parseLong(strId));
            }
        }
        if (newResidencyList != null) {
            for (String strId : newResidencyList.split(",")) {
                if (!StringUtil.isBlank(strId))
                    newMembersResidenciesList.add(Long.parseLong(strId));
            }
        }
        if (newHeadRelatList != null) {
            for (String strId : newHeadRelatList.split(",")) {
                if (!StringUtil.isBlank(strId))
                    newMembersRelationshipsList.add(Long.parseLong(strId));
            }
        }

    }

    private List<Member> getHouseholdResidents(Household mHousehold) {
        List<Residency> residencies = this.boxResidencies.query(
                Residency_.householdCode.equal(mHousehold.code).and(Residency_.endType.equal(ResidencyEndType.NOT_APPLICABLE.code)))
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

    private List<Residency> getHouseholdResidencies(Household mHousehold) {
        List<Residency> residencies = this.boxResidencies.query(
                        Residency_.householdCode.equal(mHousehold.code).and(Residency_.endType.equal(ResidencyEndType.NOT_APPLICABLE.code)))
                .order(Residency_.memberCode)
                .build().find();

        return residencies;
    }

    private List<HeadRelationship> getHouseholdHeadRelationships(Household mHousehold) {
        List<HeadRelationship> headRelationships = this.boxHeadRelationships.query(
                        HeadRelationship_.householdCode.equal(mHousehold.code).and(HeadRelationship_.endType.equal(HeadRelationshipEndType.NOT_APPLICABLE.code)))
                .order(HeadRelationship_.memberCode)
                .build().find();

        return headRelationships;
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

        preloadedMap.put("visitCode", this.visit.code);
        preloadedMap.put("originCode", this.originHousehold.code);
        preloadedMap.put("destinationCode", this.destinationHousehold.code);
        preloadedMap.put("headCode", this.headMember.code);
        preloadedMap.put("headName", this.headMember.name);

        preloadedMap.put("modules", this.user.getSelectedModulesCodes());
    }

    @Override
    protected void preloadUpdatedValues() {
        if (originHouseholdChanged) {
            preloadedMap.put("originCode", this.originHousehold.code);
            preloadedMap.put("headCode", this.headMember.code);
            preloadedMap.put("headName", this.headMember.name);
        }
    }

    @Override
    public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colOriginCode = collectedValues.get("originCode");
        ColumnValue colDestinationCode = collectedValues.get("destinationCode");
        ColumnValue colHeadCode = collectedValues.get("headCode");
        ColumnValue colHeadName = collectedValues.get("headName");
        ColumnValue colEventDate = collectedValues.get("eventDate"); //not null cannot be in the future nor before dob
        ColumnValue colReason = collectedValues.get("reason");
        ColumnValue colReasonOther = collectedValues.get("reasonOther");

        //ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        //ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        //ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String originCode = colOriginCode.getValue();
        String destinationCode = colDestinationCode.getValue();
        String headCode = colHeadCode.getValue();
        String headName = colHeadName.getValue();
        Date eventDate = colEventDate.getDateValue();
        String reason = colReason.getValue();
        String reasonOther = colReasonOther.getValue();

        //validations

        if (StringUtil.isBlank(visitCode)){
            String message = this.context.getString(R.string.household_filter_dialog_visit_code_empty_lbl);
            return new ValidationResult(colVisitCode, message);
        }

        if (StringUtil.isBlank(originCode)){
            String message = this.context.getString(R.string.household_filter_dialog_origin_code_empty_lbl);
            return new ValidationResult(colOriginCode, message);
        }

        if (StringUtil.isBlank(destinationCode)){
            String message = this.context.getString(R.string.household_filter_dialog_dest_code_empty_lbl);
            return new ValidationResult(colDestinationCode, message);
        }

        if (StringUtil.isBlank(headCode)){
            String message = this.context.getString(R.string.household_filter_dialog_head_code_empty_lbl);
            return new ValidationResult(colHeadCode, message);
        }

        if (eventDate == null) {
            String message = this.context.getString(R.string.household_filter_dialog_event_date_empty_lbl);
            return new ValidationResult(colEventDate, message);
        }

        if (eventDate.after(new Date())){ //is before dob
            String message = this.context.getString(R.string.household_filter_dialog_event_date_not_great_today_lbl);
            return new ValidationResult(colEventDate, message);
        }

        if (StringUtil.isBlank(reason)) {
            String message = this.context.getString(R.string.household_filter_dialog_reason_empty_lbl);
            return new ValidationResult(colReason, message);
        }

        //eventDate cannot be before dob
        List<Member> originMembers = getHouseholdResidents(originHousehold);
        for (Member member : originMembers) {
            if (eventDate.before(member.dob)) { //is before dob
                String message = this.context.getString(R.string.household_filter_dialog_event_date_not_before_dob_lbl);
                return new ValidationResult(colEventDate, message);
            }
        }

        //check the eventDate against residency dates
        List<Residency> originMembersResidencies = getHouseholdResidencies(originHousehold);
        for (Residency residency : originMembersResidencies) {
            if (residency != null && residency.startDate != null && (eventDate.before(residency.startDate) || eventDate.equals(residency.startDate))){
                String message = this.context.getString(R.string.household_filter_dialog_event_date_not_before_member_res_startdate_lbl, dateUtil.formatYMD(eventDate), residency.memberCode, residency.startType.code, dateUtil.formatYMD(residency.startDate));
                return new ValidationResult(colEventDate, message);
            }
        }

        //check the eventDate against residency and headRelationship dates
        List<HeadRelationship> originMembersRelationships = getHouseholdHeadRelationships(originHousehold);
        for (HeadRelationship relationship : originMembersRelationships) {
            if (relationship != null && relationship.startDate != null && (eventDate.before(relationship.startDate) || eventDate.equals(relationship.startDate))){
                String message = this.context.getString(R.string.household_filter_dialog_event_date_not_before_member_hr_startdate_lbl, dateUtil.formatYMD(eventDate), relationship.memberCode, relationship.startType.code, dateUtil.formatYMD(relationship.startDate));
                return new ValidationResult(colEventDate, message);
            }
        }

        return ValidationResult.noErrors();
    }

    private HeadRelationship getLastHeadRelationship(String householdCode, String memberCode) {
        if (StringUtil.isBlank(memberCode) || StringUtil.isBlank(householdCode)) return null;

        HeadRelationship lastHeadRelationship = this.boxHeadRelationships.query(HeadRelationship_.memberCode.equal(memberCode).and(HeadRelationship_.householdCode.equal(householdCode)))
                .orderDesc(HeadRelationship_.startDate)
                .build().findFirst();

        return lastHeadRelationship;
    }

    private Residency getLastResidency(String householdCode, String memberCode) {
        if (StringUtil.isBlank(memberCode) || StringUtil.isBlank(householdCode)) return null;

        Residency lastResidency = this.boxResidencies.query(Residency_.memberCode.equal(memberCode).and(Residency_.householdCode.equal(householdCode)))
                .orderDesc(Residency_.startDate)
                .build().findFirst();

        return lastResidency;
    }

    private HeadRelationship getCurrentHeadRelationship(String householdCode, String memberCode) {
        HeadRelationship lastHeadRelationship = getLastHeadRelationship(householdCode, memberCode);

        if (lastHeadRelationship != null && lastHeadRelationship.endType == HeadRelationshipEndType.NOT_APPLICABLE) return lastHeadRelationship;

        return null;
    }

    private Residency getCurrentResidency(String householdCode, String memberCode) {
        Residency lastResidency = getLastResidency(householdCode, memberCode);

        if (lastResidency != null && lastResidency.endType == ResidencyEndType.NOT_APPLICABLE) return lastResidency;

        return null;
    }


    @Override
    public void onBeforeFormFinished(HForm form, CollectedDataMap collectedValues) {
        //using it to update collectedHouseholdId, collectedMemberId
        ColumnValue colHouseholdId = collectedValues.get("collectedHouseholdId");
        //ColumnValue colMemberId = collectedValues.get("collectedMemberId");

        colHouseholdId.setValue(this.household.collectedId);
        //colMemberId.setValue(this.newHeadMember.collectedId);
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

    private void updateGpsCoords(Member member, Household household) {
        member.gpsNull = household.gpsNull;
        member.gpsLatitude = household.gpsLatitude;
        member.gpsLongitude = household.gpsLongitude;
        member.gpsAltitude = household.gpsAltitude;
        member.gpsAccuracy = household.gpsAccuracy;
        member.sinLatitude = household.sinLatitude;
        member.cosLatitude = household.cosLatitude;
        member.sinLongitude = household.sinLongitude;
        member.cosLongitude = household.cosLongitude;
    }
    private void onModeCreate(CollectedDataMap collectedValues, XmlFormResult result) {
        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colOriginCode = collectedValues.get("originCode");
        ColumnValue colDestinationCode = collectedValues.get("destinationCode");
        ColumnValue colHeadCode = collectedValues.get("headCode");
        ColumnValue colHeadName = collectedValues.get("headName");
        ColumnValue colEventDate = collectedValues.get("eventDate"); //not null cannot be in the future nor before dob
        ColumnValue colReason = collectedValues.get("reason");
        ColumnValue colReasonOther = collectedValues.get("reasonOther");

        //ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        //ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        //ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String originCode = colOriginCode.getValue();
        String destinationCode = colDestinationCode.getValue();
        String headCode = colHeadCode.getValue();
        String headName = colHeadName.getValue();
        Date eventDate = colEventDate.getDateValue();
        String reason = colReason.getValue();
        String reasonOther = colReasonOther.getValue();
        Date closeEventDate = GeneralUtil.getDateAdd(eventDate, -1);
        
        //Create HouseholdRelocation
        //Get Current Residencies and Relationships and Close with CHG (Outmigrations) - saveEntityState
        //Create new Residencies and Relationships with eventDate on new Household     -  saveEntityState
        
        String affectedMembers = null;
        String savedOldResidencies = "";
        String savedOldHeadRelationships = "";
        String savedNewResidencies = "";
        String savedNewHeadRelationships = "";
        
        List<Residency> originResidencies = getHouseholdResidencies(originHousehold);
        List<HeadRelationship> originHeadRelationships = getHouseholdHeadRelationships(originHousehold);
        
        //close residencies
        for (Residency residency : originResidencies) {
            residency.endType = ResidencyEndType.INTERNAL_OUTMIGRATION;
            residency.endDate = closeEventDate;
            boxResidencies.put(residency);

            savedOldResidencies += (savedOldResidencies.isEmpty() ? "" : ",") + residency.id;

            affectedMembers = addAffectedMembers(affectedMembers, residency.memberCode);
        }
                
        //close head relationships
        for (HeadRelationship headRelationship : originHeadRelationships) {
            headRelationship.endType = HeadRelationshipEndType.INTERNAL_OUTMIGRATION;
            headRelationship.endDate = closeEventDate;
            boxHeadRelationships.put(headRelationship);

            savedOldHeadRelationships += (savedOldHeadRelationships.isEmpty() ? "" : ",") + headRelationship.id;
        }
        
        //create new residencies
        for (Residency oldResidency : originResidencies) {
            Residency residency = new Residency();
            residency.householdCode = destinationHousehold.code;
            residency.memberCode = oldResidency.memberCode;
            residency.startType = ResidencyStartType.INTERNAL_INMIGRATION;
            residency.startDate = eventDate;
            residency.endType = ResidencyEndType.NOT_APPLICABLE;
            residency.endDate = null;
            
            long oid = boxResidencies.put(residency);
            savedNewResidencies += (savedNewResidencies.isEmpty() ? "" : ",") + oid;

            //update member
            Member member = Queries.getMemberByCode(boxMembers, residency.memberCode);
            member.householdCode = residency.householdCode;
            member.householdName = destinationHousehold.name;
            member.startType = residency.startType;
            member.startDate = residency.startDate;
            member.endType = residency.endType;
            member.endDate = residency.endDate;
            updateGpsCoords(member, destinationHousehold);

            boxMembers.put(member);
        }
        
        //create new head relationships
        if (destinationHousehold.type != HouseholdType.INSTITUTIONAL) {
            for (HeadRelationship oldHeadRelationship : originHeadRelationships) {
                HeadRelationship hrelationship = new HeadRelationship();
                hrelationship.householdCode = destinationHousehold.code;
                hrelationship.headCode = headMember.code;
                hrelationship.memberCode = oldHeadRelationship.memberCode;
                hrelationship.relationshipType = oldHeadRelationship.relationshipType;
                hrelationship.startType = HeadRelationshipStartType.INTERNAL_INMIGRATION;
                hrelationship.startDate = eventDate;
                hrelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
                hrelationship.endDate = null;

                long oid = boxHeadRelationships.put(hrelationship);
                savedNewHeadRelationships += (savedNewHeadRelationships.isEmpty() ? "" : ",") + oid;

                //update member
                Member member = Queries.getMemberByCode(boxMembers, hrelationship.memberCode);
                member.headRelationshipType = hrelationship.relationshipType;
                boxMembers.put(member);
            }
        }

        //create household relocation
        HouseholdRelocation householdRelocation = new HouseholdRelocation();
        householdRelocation.visitCode = visitCode;
        householdRelocation.originCode = originHousehold.code;
        householdRelocation.destinationCode = destinationHousehold.code;
        householdRelocation.headCode = headMember.code;
        householdRelocation.headName = headMember.name;
        householdRelocation.eventDate = eventDate;
        householdRelocation.reason = HouseholdRelocationReason.getFrom(reason);
        householdRelocation.reasonOther = reasonOther;
        householdRelocation.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        householdRelocation.recentlyCreated = true;
        householdRelocation.recentlyCreatedUri = result.getFilename();

        boxHouseholdRelocations.put(householdRelocation);

        //save state map
        HashMap<String,String> saveStateMap = new HashMap<>();
        saveStateMap.put("oldMembersResidenciesList", savedOldResidencies);
        saveStateMap.put("oldMembersRelationshipsList", savedOldHeadRelationships);
        saveStateMap.put("newMembersResidenciesList", savedNewResidencies);
        saveStateMap.put("newMembersRelationshipsList", savedNewHeadRelationships);
        //save the list of ids of new head relationships
        SavedEntityState entityState = new SavedEntityState(CoreFormEntity.HOUSEHOLD_RELOCATION, householdRelocation.id, SAVED_ENTITY_OBJECT_KEY, new Gson().toJson(saveStateMap));
        this.boxSavedEntityStates.put(entityState);

        //save core collected data
        collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.HOUSEHOLD_RELOCATION;
        collectedData.formEntityId = householdRelocation.id;
        collectedData.formEntityCode = householdRelocation.destinationCode;
        collectedData.formEntityCodes = affectedMembers; //new head code, spouse, head related individuals
        collectedData.formEntityName = householdRelocation.originCode +" -> "+ householdRelocation.destinationCode;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        collectedData.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        collectedData.extension.setTarget(this.getFormExtension(collectedData.formEntity));
        this.boxCoreCollectedData.put(collectedData);

        this.entity = householdRelocation;
        this.collectExtensionForm(collectedValues);
    }

    private void onModeEdit(CollectedDataMap collectedValues, XmlFormResult result) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colOriginCode = collectedValues.get("originCode");
        ColumnValue colDestinationCode = collectedValues.get("destinationCode");
        ColumnValue colHeadCode = collectedValues.get("headCode");
        ColumnValue colHeadName = collectedValues.get("headName");
        ColumnValue colEventDate = collectedValues.get("eventDate"); //not null cannot be in the future nor before dob
        ColumnValue colReason = collectedValues.get("reason");
        ColumnValue colReasonOther = collectedValues.get("reasonOther");

        //ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        //ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        //ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String originCode = colOriginCode.getValue();
        String destinationCode = colDestinationCode.getValue();
        String headCode = colHeadCode.getValue();
        String headName = colHeadName.getValue();
        Date eventDate = colEventDate.getDateValue();
        String reason = colReason.getValue();
        String reasonOther = colReasonOther.getValue();
        Date closeEventDate = GeneralUtil.getDateAdd(eventDate, -1);

        String affectedMembers = null;
        String savedOldResidencies = "";
        String savedOldHeadRelationships = "";
        String savedNewResidencies = "";
        String savedNewHeadRelationships = "";

        //if origin not changed
        //update eventDate, reason, reasonOther
        //update old and new residencies and relationships dates
        //if origin changed
        //update all householdRelocation
        //delete new residencies and relationships
        //restore old residencies and relationships
        //create new residencies and relationships

        //update relocation
        HouseholdRelocation householdRelocation = boxHouseholdRelocations.get(this.entity.id);

        householdRelocation.visitCode = visitCode;
        householdRelocation.originCode = originHousehold.code;
        householdRelocation.destinationCode = destinationHousehold.code;
        householdRelocation.headCode = headMember.code;
        householdRelocation.headName = headMember.name;
        householdRelocation.eventDate = eventDate;
        householdRelocation.reason = HouseholdRelocationReason.getFrom(reason);
        householdRelocation.reasonOther = reasonOther;
        boxHouseholdRelocations.put(householdRelocation);

        if (originHouseholdChanged) {
            //delete new residencies and relationships
            boxResidencies.removeByIds(newMembersResidenciesList);
            boxHeadRelationships.removeByIds(newMembersRelationshipsList);

            //restore old residencies and relationships
            for (Long id : oldMembersResidenciesList) {
                Residency obj = boxResidencies.get(id);
                obj.endType = ResidencyEndType.NOT_APPLICABLE;
                obj.endDate = null;
                boxResidencies.put(obj);

                //update member
                Member member = Queries.getMemberByCode(boxMembers, obj.memberCode);
                member.householdCode = obj.householdCode;
                member.householdName = Queries.getHouseholdByCode(boxHouseholds, obj.householdCode).name;
                member.startType = obj.startType;
                member.startDate = obj.startDate;
                member.endType = ResidencyEndType.NOT_APPLICABLE;
                member.endDate = null;
                updateGpsCoords(member, destinationHousehold);
                boxMembers.put(member);
            }
            for (Long id : oldMembersRelationshipsList) {
                HeadRelationship obj = boxHeadRelationships.get(id);
                obj.endType = HeadRelationshipEndType.NOT_APPLICABLE;
                obj.endDate = null;
                boxHeadRelationships.put(obj);

                Member member = Queries.getMemberByCode(boxMembers, obj.memberCode);
                member.headRelationshipType = obj.relationshipType;
                boxMembers.put(member);
            }
        }

        //update current residencies - work for both (update of origin or just a regular update of previous records)
        List<Residency> originResidencies = getHouseholdResidencies(originHousehold);
        List<HeadRelationship> originHeadRelationships = getHouseholdHeadRelationships(originHousehold);
        //close current residencies
        for (Residency residency : originResidencies) {
            residency.endType = ResidencyEndType.INTERNAL_OUTMIGRATION;
            residency.endDate = closeEventDate;
            boxResidencies.put(residency);

            savedOldResidencies += (savedOldResidencies.isEmpty() ? "" : ",") + residency.id;
            affectedMembers = addAffectedMembers(affectedMembers, residency.memberCode);
        }
        //close current head relationships
        for (HeadRelationship headRelationship : originHeadRelationships) {
            headRelationship.endType = HeadRelationshipEndType.INTERNAL_OUTMIGRATION;
            headRelationship.endDate = closeEventDate;
            boxHeadRelationships.put(headRelationship);

            savedOldHeadRelationships += (savedOldHeadRelationships.isEmpty() ? "" : ",") + headRelationship.id;
        }

        //Create New Or Update residencies and relationships
        for (Residency originResidency : originResidencies) {
            Residency recentResidency = !originHouseholdChanged ? getCurrentResidency(destinationHousehold.code, originResidency.memberCode) : null;

            Residency residency = (originHouseholdChanged || recentResidency == null) ? new Residency() : recentResidency; //create new if origin was changed or n
            residency.householdCode = destinationHousehold.code;
            residency.memberCode = originResidency.memberCode;
            residency.startType = ResidencyStartType.INTERNAL_INMIGRATION;
            residency.startDate = eventDate;
            residency.endType = ResidencyEndType.NOT_APPLICABLE;
            residency.endDate = null;

            long oid = boxResidencies.put(residency);
            savedNewResidencies += (savedNewResidencies.isEmpty() ? "" : ",") + oid;

            //update member
            Member member = Queries.getMemberByCode(boxMembers, residency.memberCode);
            member.householdCode = residency.householdCode;
            member.householdName = destinationHousehold.name;
            member.startType = residency.startType;
            member.startDate = residency.startDate;
            member.endType = residency.endType;
            member.endDate = residency.endDate;
            updateGpsCoords(member, destinationHousehold);

            boxMembers.put(member);
        }

        //create new head relationships
        if (destinationHousehold.type != HouseholdType.INSTITUTIONAL) {
            for (HeadRelationship originHeadRelationship : originHeadRelationships) {
                HeadRelationship recentHeadRelationship = !originHouseholdChanged ? getCurrentHeadRelationship(destinationHousehold.code, originHeadRelationship.memberCode) : null;

                HeadRelationship hrelationship = (originHouseholdChanged || recentHeadRelationship == null) ? new HeadRelationship() : recentHeadRelationship;
                hrelationship.householdCode = destinationHousehold.code;
                hrelationship.headCode = headMember.code;
                hrelationship.memberCode = originHeadRelationship.memberCode;
                hrelationship.relationshipType = originHeadRelationship.relationshipType;
                hrelationship.startType = HeadRelationshipStartType.INTERNAL_INMIGRATION;
                hrelationship.startDate = eventDate;
                hrelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
                hrelationship.endDate = null;

                long oid = boxHeadRelationships.put(hrelationship);
                savedNewHeadRelationships += (savedNewHeadRelationships.isEmpty() ? "" : ",") + oid;

                //update member
                Member member = Queries.getMemberByCode(boxMembers, hrelationship.memberCode);
                member.headRelationshipType = hrelationship.relationshipType;
                boxMembers.put(member);
            }
        }

        //save state map
        HashMap<String,String> saveStateMap = new HashMap<>();
        saveStateMap.put("oldMembersResidenciesList", savedOldResidencies);
        saveStateMap.put("oldMembersRelationshipsList", savedOldHeadRelationships);
        saveStateMap.put("newMembersResidenciesList", savedNewResidencies);
        saveStateMap.put("newMembersRelationshipsList", savedNewHeadRelationships);
        //save the list of ids of new head relationships
        SavedEntityState entityState = new SavedEntityState(CoreFormEntity.HOUSEHOLD_RELOCATION, householdRelocation.id, SAVED_ENTITY_OBJECT_KEY, new Gson().toJson(saveStateMap));
        this.boxSavedEntityStates.put(entityState);

        //save core collected data
        collectedData.visitId = visit.id;
        collectedData.formEntityCodes = affectedMembers;
        collectedData.formEntityName = originCode + " -> " + destinationCode;
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
            openHouseholdFilterDialog(false);
        } else if (this.currentMode == Mode.EDIT) {
            checkChangeOriginHouseholdDialog();
        }
    }

    private void checkChangeOriginHouseholdDialog(){

        DialogFactory.createMessageYN(this.context, R.string.household_filter_dialog_change_origin_title_lbl, R.string.household_filter_dialog_origin_change_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                openHouseholdFilterDialog(true);
            }

            @Override
            public void onNoClicked() {
                //head remains unchanged
                executeCollectForm();
            }
        }).show();

    }

    private void openHouseholdFilterDialog(boolean updateOriginHousehold){

        HouseholdFilterDialog dialog = HouseholdFilterDialog.newInstance(this.fragmentManager, context.getString(R.string.household_filter_dialog_select_origin_title_lbl), true, new HouseholdFilterDialog.Listener() {
            @Override
            public void onSelectedHousehold(Household household) {
                Log.d("selected-house", ""+household.getCode());

                if (currentMode == Mode.EDIT && updateOriginHousehold) {
                    HouseholdRelocationFormUtil.this.originHouseholdChanged = !entity.originCode.equalsIgnoreCase(household.code);
                }

                HouseholdRelocationFormUtil.this.originHousehold = household;
                HouseholdRelocationFormUtil.this.headMember = getHeadOfHousehold(household);
                executeCollectForm();
            }

            @Override
            public void onCanceled() {

            }
        });

        dialog.setStartSearchOnShow(true);
        dialog.show();
    }

    private Member getHeadOfHousehold(Household mHousehold) {
        if (mHousehold != null) {

            HeadRelationship headRelationship = boxHeadRelationships.query(
                    HeadRelationship_.householdCode.equal(mHousehold.code)
                            .and(HeadRelationship_.relationshipType.equal(HeadRelationshipType.HEAD_OF_HOUSEHOLD.code))
                            .and(HeadRelationship_.endType.equal(HeadRelationshipEndType.NOT_APPLICABLE.code))
            ).orderDesc(HeadRelationship_.startDate).build().findFirst();

            if (headRelationship != null) {
                return Queries.getMemberByCode(boxMembers, headRelationship.memberCode);
            }
        }

        return null;
    }
}
