package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.MemberFilterDialog;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.RegionHeadRelationship;
import org.philimone.hds.explorer.model.RegionHeadRelationship_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.temporal.RegionHeadEndType;
import org.philimone.hds.explorer.model.enums.temporal.RegionHeadStartType;
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

public class ChangeRegionHeadFormUtil extends FormUtil<RegionHeadRelationship> {

    private Box<Member> boxMembers;
    private Box<Region> boxRegions;
    private Box<RegionHeadRelationship> boxRegionHeadRelationships;
    //private Box<Household> boxHouseholds;
    //private Household household;
    private Region region;
    private Visit visit;
    private Member oldHeadMember;
    private Member newHeadMember;
    private Member newHeadAboutToChange;
    private RegionHeadRelationship oldRegionHeadRelationship;
    private Map<String, String> mapSavedStates = new HashMap<>();

    private int minimunHeadAge;
    private String regionHierarchyName;

    public ChangeRegionHeadFormUtil(Fragment fragment, Context context, Region region, Visit visit, Household household, FormUtilities odkFormUtilities, FormUtilListener<RegionHeadRelationship> listener){
        super(fragment, context, FormUtil.getChangeRegionHeadForm(context), odkFormUtilities, listener);

        //Log.d("enu-household", ""+household);e
        this.region = region;
        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    public ChangeRegionHeadFormUtil(Fragment fragment, Context context, Region region, Visit visit, Household household, RegionHeadRelationship headRelationshipToEdit, FormUtilities odkFormUtilities, FormUtilListener<RegionHeadRelationship> listener){
        super(fragment, context, FormUtil.getChangeRegionHeadForm(context), headRelationshipToEdit, odkFormUtilities, listener);

        this.region = region;
        this.household = household;
        this.visit = visit;

        initBoxes();

        this.newHeadMember = this.boxMembers.query(Member_.code.equal(headRelationshipToEdit.headCode)).build().findFirst();
        this.newHeadAboutToChange = this.newHeadMember; //to save the new head - that can be changed when call collect()

        readSavedEntityState();

        initialize();
    }

    public ChangeRegionHeadFormUtil(AppCompatActivity activity, Context context, Region region, Visit visit, Household household, FormUtilities odkFormUtilities, FormUtilListener<RegionHeadRelationship> listener){
        super(activity, context, FormUtil.getChangeRegionHeadForm(context), odkFormUtilities, listener);

        //Log.d("enu-household", ""+household);e
        this.region = region;
        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    public ChangeRegionHeadFormUtil(AppCompatActivity activity, Context context, Region region, Visit visit, Household household, RegionHeadRelationship headRelationshipToEdit, FormUtilities odkFormUtilities, FormUtilListener<RegionHeadRelationship> listener){
        super(activity, context, FormUtil.getChangeRegionHeadForm(context), headRelationshipToEdit, odkFormUtilities, listener);

        this.region = region;
        this.household = household;
        this.visit = visit;

        initBoxes();

        this.newHeadMember = this.boxMembers.query(Member_.code.equal(headRelationshipToEdit.headCode)).build().findFirst();
        this.newHeadAboutToChange = this.newHeadMember; //to save the new head - that can be changed when call collect()

        readSavedEntityState();

        initialize();
    }

    public static ChangeRegionHeadFormUtil newInstance(Mode openMode, Fragment fragment, Context context, Region region, Visit visit, Household household, RegionHeadRelationship headRelationshipToEdit, FormUtilities odkFormUtilities, FormUtilListener<RegionHeadRelationship> listener){
        if (openMode == Mode.CREATE) {
            return new ChangeRegionHeadFormUtil(fragment, context, region, visit, household, odkFormUtilities, listener);
        } else if (openMode == Mode.EDIT) {
            return new ChangeRegionHeadFormUtil(fragment, context, region, visit, household, headRelationshipToEdit, odkFormUtilities, listener);
        }

        return null;
    }

    public static ChangeRegionHeadFormUtil newInstance(Mode openMode, AppCompatActivity activity, Context context, Region region, Visit visit, Household household, RegionHeadRelationship headRelationshipToEdit, FormUtilities odkFormUtilities, FormUtilListener<RegionHeadRelationship> listener){
        if (openMode == Mode.CREATE) {
            return new ChangeRegionHeadFormUtil(activity, context, region, visit, household, odkFormUtilities, listener);
        } else if (openMode == Mode.EDIT) {
            return new ChangeRegionHeadFormUtil(activity, context, region, visit, household, headRelationshipToEdit, odkFormUtilities, listener);
        }

        return null;
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        //this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxRegionHeadRelationships = ObjectBoxDatabase.get().boxFor(RegionHeadRelationship.class);
    }

    @Override
    protected void initialize(){
        super.initialize();

        if (currentMode == Mode.CREATE) {
            //get current memberHeadRelationship
            this.oldRegionHeadRelationship = this.boxRegionHeadRelationships.query(
                                         RegionHeadRelationship_.regionCode.equal(this.region.code)
                                    .and(RegionHeadRelationship_.endType.equal(RegionHeadEndType.NOT_APPLICABLE.code)))
                                    .orderDesc(RegionHeadRelationship_.startDate).build().findFirst();

            if (this.oldRegionHeadRelationship != null) {
                this.oldHeadMember = Queries.getMemberByCode(boxMembers, this.oldRegionHeadRelationship.headCode);
            }

        } else if (currentMode == Mode.EDIT) {

            //oldHeadMember - is the original head member that will be changed     - read from readSavedEntityState
            //oldHeadMemberRelationship - is the original head member relationship - read from readSavedEntityState
        }

        this.minimunHeadAge = retrieveMinimumHeadAge();
        this.regionHierarchyName = getHierarchyName(region);
    }

    private String getHierarchyName(Region region){
        if (region == null) return "";

        ApplicationParam param = Queries.getApplicationParamBy(boxAppParams, region.getLevel() );

        if (param != null){
            return param.getValue();
        }

        return "";
    }

    private void readSavedEntityState() {
        SavedEntityState savedState = this.boxSavedEntityStates.query(SavedEntityState_.formEntity.equal(CoreFormEntity.CHANGE_REGION_HEAD.code)
                .and(SavedEntityState_.collectedId.equal(this.entity.id))
                .and(SavedEntityState_.objectKey.equal("changeRegionHeadFormUtilState"))).build().findFirst();

        if (savedState != null) {
            HashMap map = new Gson().fromJson(savedState.objectGsonValue, HashMap.class);
            for (Object key : map.keySet()) {
                mapSavedStates.put(key.toString(), map.get(key).toString());
            }
        }

        String oldHeadCode = mapSavedStates.get("oldHeadCode");
        String oldHeadRelatId = mapSavedStates.get("oldHeadMemberId");

        if (oldHeadCode != null) {
            this.oldHeadMember = this.boxMembers.query(Member_.code.equal(oldHeadCode)).build().findFirst();
        }
        if (oldHeadRelatId != null) {
            try {
                this.oldRegionHeadRelationship = this.boxRegionHeadRelationships.get(Long.parseLong(oldHeadRelatId));
            } catch ( Exception ex) {
                Log.e("reading-oldregionhead", "error: "+ex.getMessage());
            }
        }

    }

    private boolean newHeadChanged(){
        return (this.newHeadAboutToChange != null && this.newHeadMember != null) && (newHeadAboutToChange.id != newHeadMember.id);
    }

    @Override
    protected void preloadValues() {
        //member_details_unknown_lbl
        preloadedMap.put("visitCode", this.visit != null ? this.visit.code : "");
        preloadedMap.put("regionCode", this.region.code);

        if (this.oldHeadMember != null) {
            preloadedMap.put("oldHeadCode", this.oldHeadMember.code);
            preloadedMap.put("oldHeadName", this.oldHeadMember.name);
        }

        preloadedMap.put("newHeadCode", this.newHeadMember.code);
        preloadedMap.put("newHeadName", this.newHeadMember.name);
        preloadedMap.put("modules", this.user.getSelectedModulesCodes());
    }

    @Override
    protected void preloadUpdatedValues() {
        if (newHeadChanged()) {
            preloadedMap.put("newHeadCode", this.newHeadMember.code);
            preloadedMap.put("newHeadName", this.newHeadMember.name);
        }

        //update visit code
        if (this.visit != null) {
            preloadedMap.put("visitCode", this.visit.code);
        }
    }

    @Override
    public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colRegionCode = collectedValues.get("regionCode");
        ColumnValue colOldHeadCode = collectedValues.get("oldHeadCode");
        ColumnValue colOldHeadName = collectedValues.get("oldHeadName");
        ColumnValue colNewHeadCode = collectedValues.get("newHeadCode");
        ColumnValue colNewHeadName = collectedValues.get("newHeadName");
        ColumnValue colEventDate = collectedValues.get("eventDate"); //not null cannot be in the future nor before dob
        ColumnValue colReason = collectedValues.get("reason");
        ColumnValue colReasonOther = collectedValues.get("reasonOther");

        //ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        //ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        //ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String regionCode = colRegionCode.getValue();
        String oldHeadCode = colOldHeadCode.getValue();
        String oldHeadName = colOldHeadName.getValue();
        String newHeadCode = colNewHeadCode.getValue();
        String newHeadName = colNewHeadName.getValue();
        Date eventDate = colEventDate.getDateValue();
        String reason = colReason.getValue();
        String reasonOther = colReasonOther.getValue();

        //validations

        if (StringUtil.isBlank(regionCode)){
            String message = this.context.getString(R.string.changeregionhead_region_code_empty_lbl, regionHierarchyName);
            return new ValidationResult(colVisitCode, message);
        }

        /*if (StringUtil.isBlank(oldHeadCode)){
            String message = this.context.getString(R.string.changeregionhead_oldhead_code_empty_lbl);
            return new ValidationResult(colOldHeadCode, message);
        }*/

        if (StringUtil.isBlank(newHeadCode)){
            String message = this.context.getString(R.string.changeregionhead_newhead_code_empty_lbl, regionHierarchyName);
            return new ValidationResult(colNewHeadCode, message);
        }

        if (eventDate == null) {
            String message = this.context.getString(R.string.changeregionhead_eventdate_empty_lbl);
            return new ValidationResult(colEventDate, message);
        }

        //eventDate cannot be before dob
        if (eventDate != null && eventDate.before(this.newHeadMember.dob)){ //is before dob
            String message = this.context.getString(R.string.changeregionhead_eventdate_not_before_dob_lbl, regionHierarchyName);
            return new ValidationResult(colEventDate, message);
        }

        if (eventDate != null && eventDate.after(new Date())){ //is before dob
            String message = this.context.getString(R.string.changeregionhead_eventdate_not_great_today_lbl);
            return new ValidationResult(colEventDate, message);
        }

        //other validations
        //C6. Check Age of the new head of Household - done by filtering


        //check if new head member is a head of household of another household
        if (isHeadOfRegionSomewhere(newHeadCode) && (currentMode==Mode.CREATE || currentMode==Mode.EDIT && newHeadChanged())) {
            String message = this.context.getString(R.string.changeregionhead_new_head_is_head_of_household_lbl, regionHierarchyName);
            return new ValidationResult(colNewHeadCode, message);
        }

        //check dates of previous relationship of the new head against the eventDate (eventDate vs newHeadLastRelationship.startDate)
        RegionHeadRelationship newHeadLastRelationship = getLastRegionHeadRelationshipByHeadCode(newHeadCode);
        if (newHeadLastRelationship != null && newHeadLastRelationship.startDate != null && eventDate.before(newHeadLastRelationship.startDate)){
            //The event date cannot be before the [start date] of the [new Head of Region] last Head Relationship record.
            String message = this.context.getString(R.string.changeregionhead_eventdate_not_before_new_head_hrelationship_startdate_lbl, StringUtil.formatYMD(eventDate), newHeadLastRelationship.startType.code, StringUtil.formatYMD(newHeadLastRelationship.startDate), regionHierarchyName);
            return new ValidationResult(colEventDate, message);
        }

        //check dates of last region relationship head against the eventDate (eventDate vs regionHeadLastRelationship.startDate)
        RegionHeadRelationship regionHeadLastRelationship = getLastRegionHeadRelationshipByRegionCode(regionCode);
        if (regionHeadLastRelationship != null && regionHeadLastRelationship.startDate != null && eventDate.before(regionHeadLastRelationship.startDate)){
            //The event date cannot be before the [start date] of the [new Head of Region] last Head Relationship record.
            String message = this.context.getString(R.string.changeregionhead_eventdate_not_before_lastregion_head_hrelationship_startdate_lbl, StringUtil.formatYMD(eventDate), regionHeadLastRelationship.startType.code, StringUtil.formatYMD(regionHeadLastRelationship.startDate), regionHierarchyName);
            return new ValidationResult(colEventDate, message);
        }

        return ValidationResult.noErrors();
    }

    private RegionHeadRelationship getLastRegionHeadRelationshipByHeadCode(String memberCode) {
        if (StringUtil.isBlank(memberCode)) return null;

        RegionHeadRelationship lastHeadRelationship = null;

        if (currentMode == Mode.EDIT && this.entity != null) {
            //if its editing, search for the last excluding the most recently created
            lastHeadRelationship = this.boxRegionHeadRelationships.query(
                    RegionHeadRelationship_.headCode.equal(memberCode).and(RegionHeadRelationship_.id.notEqual(this.entity.id)))
                    .orderDesc(RegionHeadRelationship_.startDate).build().findFirst();

        } else if (currentMode == Mode.CREATE) {
            //Just get the last relationship
            lastHeadRelationship = this.boxRegionHeadRelationships.query(RegionHeadRelationship_.headCode.equal(memberCode))
                    .orderDesc(RegionHeadRelationship_.startDate)
                    .build().findFirst();
        }

        return lastHeadRelationship;
    }

    private RegionHeadRelationship getLastRegionHeadRelationshipByRegionCode(String regionCode) {
        if (StringUtil.isBlank(regionCode)) return null;

        RegionHeadRelationship lastHeadRelationship = null;

        if (currentMode == Mode.EDIT && this.entity != null) {
            lastHeadRelationship = this.boxRegionHeadRelationships.query(
                    RegionHeadRelationship_.regionCode.equal(regionCode).and(RegionHeadRelationship_.id.notEqual(this.entity.id)))
                    .orderDesc(RegionHeadRelationship_.startDate).build().findFirst();

        } else if (currentMode == Mode.CREATE) {
            lastHeadRelationship = this.boxRegionHeadRelationships.query(RegionHeadRelationship_.regionCode.equal(regionCode))
                    .orderDesc(RegionHeadRelationship_.startDate).build().findFirst();
        }

        return lastHeadRelationship;
    }

    private boolean isHeadOfRegionSomewhere(String memberCode) {
        long count = this.boxRegionHeadRelationships.query(
                RegionHeadRelationship_.headCode.equal(memberCode).and(RegionHeadRelationship_.endType.equal(RegionHeadEndType.NOT_APPLICABLE.code)))
                .build().count();

        return count > 0;
    }

    @Override
    public void onBeforeFormFinished(HForm form, CollectedDataMap collectedValues) {
        //using it to update collectedHouseholdId, collectedMemberId
        //ColumnValue colHouseholdId = collectedValues.get("collectedHouseholdId");
        ColumnValue colMemberId = collectedValues.get("collectedMemberId");

        //colHouseholdId.setValue("");
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
        ColumnValue colRegionCode = collectedValues.get("regionCode");
        ColumnValue colOldHeadCode = collectedValues.get("oldHeadCode");
        ColumnValue colOldHeadName = collectedValues.get("oldHeadName");
        ColumnValue colNewHeadCode = collectedValues.get("newHeadCode");
        ColumnValue colNewHeadName = collectedValues.get("newHeadName");
        ColumnValue colEventDate = collectedValues.get("eventDate"); //not null cannot be in the future nor before dob
        ColumnValue colReason = collectedValues.get("reason");
        ColumnValue colReasonOther = collectedValues.get("reasonOther");

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

        String affectedMembers = null;
        
        Date closeEventDate = GeneralUtil.getDateAdd(eventDate, -1);

        //new head head relationship
        RegionHeadRelationship newHeadRelationship = new RegionHeadRelationship();
        newHeadRelationship.regionCode = region.code;
        newHeadRelationship.headCode = newHeadMember.code;
        newHeadRelationship.oldHeadId = oldHeadMember != null ? oldHeadMember.id : 0;
        newHeadRelationship.oldHeadRelationshipId = oldRegionHeadRelationship != null ? oldRegionHeadRelationship.id : 0;
        newHeadRelationship.startType = RegionHeadStartType.NEW_HEAD_OF_REGION;
        newHeadRelationship.startDate = eventDate;
        newHeadRelationship.endType = RegionHeadEndType.NOT_APPLICABLE;
        newHeadRelationship.endDate = null;
        newHeadRelationship.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        newHeadRelationship.recentlyCreated = true;
        newHeadRelationship.recentlyCreatedUri = result.getFilename();
        this.boxRegionHeadRelationships.put(newHeadRelationship);

        this.region.headCode = newHeadMember.code;
        this.region.headName = newHeadMember.name;
        this.boxRegions.put(this.region);

        //close memberHeadRelationship
        if (this.oldRegionHeadRelationship != null){
            this.oldRegionHeadRelationship.endType = RegionHeadEndType.CHANGE_OF_HEAD_OF_REGION;
            this.oldRegionHeadRelationship.endDate = closeEventDate;
            this.boxRegionHeadRelationships.put(this.oldRegionHeadRelationship);
        }

        //save the new head member previous headRelationshipType
        HashMap<String,String> saveStateMap = new HashMap<>();
        saveStateMap.put("oldHeadCode", oldHeadMember != null ? oldHeadMember.code : ""); //the head that is being chang
        saveStateMap.put("oldHeadMemberId", oldRegionHeadRelationship != null ? oldRegionHeadRelationship.id+"" : "");

        //save the list of ids of new head relationships
        SavedEntityState entityState = new SavedEntityState(CoreFormEntity.CHANGE_REGION_HEAD, newHeadRelationship.id, "changeRegionHeadFormUtilState", new Gson().toJson(saveStateMap));
        this.boxSavedEntityStates.put(entityState);

        affectedMembers = addAffectedMembers(affectedMembers, this.newHeadMember.code);

        //save core collected data
        collectedData = new CoreCollectedData();
        collectedData.visitId = visit==null ? 0 : visit.id;
        collectedData.formEntity = CoreFormEntity.CHANGE_REGION_HEAD;
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
        ColumnValue colRegionCode = collectedValues.get("regionCode");
        ColumnValue colOldHeadCode = collectedValues.get("oldHeadCode");
        ColumnValue colOldHeadName = collectedValues.get("oldHeadName");
        ColumnValue colNewHeadCode = collectedValues.get("newHeadCode");
        ColumnValue colNewHeadName = collectedValues.get("newHeadName");
        ColumnValue colEventDate = collectedValues.get("eventDate"); //not null cannot be in the future nor before dob
        ColumnValue colReason = collectedValues.get("reason");
        ColumnValue colReasonOther = collectedValues.get("reasonOther");

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

        String affectedMembers = null;

        Date closeEventDate = GeneralUtil.getDateAdd(eventDate, -1);

        //new head head relationship
        RegionHeadRelationship newHeadRelationship = boxRegionHeadRelationships.get(this.entity.id);
        newHeadRelationship.regionCode = region.code;
        newHeadRelationship.headCode = newHeadMember.code;
        newHeadRelationship.startType = RegionHeadStartType.NEW_HEAD_OF_REGION;
        newHeadRelationship.startDate = eventDate;
        newHeadRelationship.endType = RegionHeadEndType.NOT_APPLICABLE;
        newHeadRelationship.endDate = null;
        //newHeadRelationship.recentlyCreated = true;
        //newHeadRelationship.recentlyCreatedUri = result.getFilename();
        this.boxRegionHeadRelationships.put(newHeadRelationship);

        this.region.headCode = newHeadMember.code;
        this.region.headName = newHeadMember.name;
        this.boxRegions.put(this.region);

        //close memberHeadRelationship again
        if (this.oldRegionHeadRelationship != null){
            this.oldRegionHeadRelationship = boxRegionHeadRelationships.get(oldRegionHeadRelationship.id);
            this.oldRegionHeadRelationship.endType = RegionHeadEndType.CHANGE_OF_HEAD_OF_REGION;
            this.oldRegionHeadRelationship.endDate = closeEventDate;
            this.boxRegionHeadRelationships.put(this.oldRegionHeadRelationship);
        }

        //if the head was changed
        if (newHeadChanged()){

            //change the previous new head relationshipType
            newHeadAboutToChange = boxMembers.get(newHeadAboutToChange.id);

            //save the new head member previous headRelationshipType
            HashMap<String,String> saveStateMap = new HashMap<>();
            saveStateMap.put("oldHeadCode", oldHeadMember != null ? oldHeadMember.code : ""); //the head that is being changed
            saveStateMap.put("oldHeadMemberId", oldRegionHeadRelationship != null ? oldRegionHeadRelationship.id+"" : "");

            //update the saved entity state
            SavedEntityState entityState = this.boxSavedEntityStates.query(SavedEntityState_.formEntity.equal(CoreFormEntity.CHANGE_REGION_HEAD.code).and(SavedEntityState_.collectedId.equal(this.entity.id)).and(SavedEntityState_.objectKey.equal("changeRegionHeadFormUtilState"))).build().findFirst();
            entityState.objectGsonValue = new Gson().toJson(saveStateMap);
            this.boxSavedEntityStates.put(entityState);

            affectedMembers = addAffectedMembers(affectedMembers, this.newHeadMember.code);
        } else {

            //the new head remains - get the new head relationships with the members of the household (saved as state)
        }

        //save core collected data
        collectedData.visitId = visit==null ? 0 : visit.id;
        collectedData.formEntityCode = newHeadMember.code;
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
        this.formTitle = regionHierarchyName;

        if (this.currentMode == Mode.CREATE) {
            checkHeadOfRegionDialog();
        } else if (this.currentMode == Mode.EDIT) {
            //retrieveHeadOfHousehold();
            checkChangeNewRegionHeadDialog();
        }
    }

    public void setNewHeadOfRegion(Member head) {
        this.newHeadMember = head;
    }

    private void retrieveHeadOfHousehold() {
        this.oldRegionHeadRelationship = getHeadOfRegionRelationship();
        this.oldHeadMember = getHeadOfRegion(this.oldRegionHeadRelationship);
        
        if (this.oldHeadMember == null && this.region.headCode != null) {
            this.oldHeadMember = Queries.getMemberByCode(boxMembers, region.headCode);
        }
    }

    private RegionHeadRelationship getHeadOfRegionRelationship() {

        RegionHeadRelationship headRelationship = boxRegionHeadRelationships.query(
                RegionHeadRelationship_.regionCode.equal(region.code).and(RegionHeadRelationship_.endType.equal(RegionHeadEndType.NOT_APPLICABLE.code))
        ).orderDesc(RegionHeadRelationship_.startDate).build().findFirst();

        return headRelationship;
    }

    private Member getHeadOfRegion(RegionHeadRelationship headRelationship) {
        if (headRelationship != null) {
            return Queries.getMemberByCode(boxMembers, headRelationship.headCode);
        }
        return null;
    }

    private void checkHeadOfRegionDialog() {
        retrieveHeadOfHousehold();

        if (this.oldHeadMember == null && this.newHeadMember==null) {
            String title = context.getString(R.string.eventType_change_of_hor);
            String message = context.getString(R.string.changeregionhead_head_dont_exists_lbl, regionHierarchyName);
            DialogFactory.createMessageInfo(this.context, title, message, clickedButton -> openNewRegionHeadFilterDialog()).show();
        } else {
            openNewRegionHeadFilterDialog();
        }
    }

    private void checkChangeNewRegionHeadDialog(){

        if (currentMode == Mode.EDIT && this.collectedData.uploaded) {
            executeCollectForm();
            return;
        }

        String title = context.getString(R.string.changeregionhead_dialog_change_title_lbl);
        String message = context.getString(R.string.changeregionhead_dialog_head_change_lbl, regionHierarchyName);
        DialogFactory.createMessageYN(this.context, title, message, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                openNewRegionHeadFilterDialog();
            }

            @Override
            public void onNoClicked() {
                //head remains unchanged
                executeCollectForm();
            }
        }).show();

    }

    private void openNewRegionHeadFilterDialog(){

        if (newHeadMember != null && currentMode == Mode.CREATE) {
            //The new Head is already choosen
            executeCollectForm();
            return;
        }

        String title = context.getString(R.string.changeregionhead_new_head_select_lbl, regionHierarchyName);
        MemberFilterDialog dialog = MemberFilterDialog.newInstance(this.fragmentManager, title, true, new MemberFilterDialog.Listener() {
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
        dialog.setFilterHouseCode(this.visit != null ? this.visit.householdCode : region.code, false);
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
                ex.printStackTrace();
            }
        }

        return 12;
    }

}