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
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.HeadRelationship_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.HouseholdProxyHead;
import org.philimone.hds.explorer.model.HouseholdProxyHead_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Residency;
import org.philimone.hds.explorer.model.Residency_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.HeadRelationshipType;
import org.philimone.hds.explorer.model.enums.ProxyHeadChangeReason;
import org.philimone.hds.explorer.model.enums.ProxyHeadRole;
import org.philimone.hds.explorer.model.enums.ProxyHeadType;
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

public class ChangeProxyHeadFormUtil extends FormUtil<HouseholdProxyHead> {

    private Box<Member> boxMembers;
    private Box<Residency> boxResidencies;
    private Box<Household> boxHouseholds;
    private Box<HouseholdProxyHead> boxHouseholdProxyHeads;
    private Box<HeadRelationship> boxHeadRelationships;
    private Visit visit;    
    private Map<String, String> mapSavedStates = new HashMap<>();
    private int minimunHeadAge;
    private boolean onlyMinorsLeftToBeHead;

    private DateUtil dateUtil = Bootstrap.getDateUtil();
    private Member currentHead;
    private Member selectedProxyHead;
    private ProxyHeadType selectedProxyHeadType;
    private HouseholdProxyHead oldProxyHead;
    private Member oldProxyHeadMember;
    private Member editedProxyHead;

    public ChangeProxyHeadFormUtil(Fragment fragment, Context context, Visit visit, Household household, FormUtilities odkFormUtilities, FormUtilListener<HouseholdProxyHead> listener){
        super(fragment, context, FormUtil.getChangeProxyHeadForm(context), odkFormUtilities, listener);

        //Log.d("enu-household", ""+household);

        this.household = household;
        this.visit = visit;

        initBoxes();

        retrieveHeads();
        initialize();
    }

    public ChangeProxyHeadFormUtil(Fragment fragment, Context context, Visit visit, Household household, HouseholdProxyHead householdProxyHeadToEdit, FormUtilities odkFormUtilities, FormUtilListener<HouseholdProxyHead> listener){
        super(fragment, context, FormUtil.getChangeProxyHeadForm(context), householdProxyHeadToEdit, odkFormUtilities, listener);

        this.household = household;
        this.visit = visit;

        initBoxes();

        if (householdProxyHeadToEdit.proxyHeadCode != null) {
            this.editedProxyHead = this.boxMembers.query(Member_.code.equal(householdProxyHeadToEdit.proxyHeadCode)).build().findFirst();
        }

        retrieveHeads();
        readSavedEntityState();

        initialize();
    }

    public static ChangeProxyHeadFormUtil newInstance(Mode openMode, Fragment fragment, Context context, Visit visit, Household household, HouseholdProxyHead householdProxyHeadToEdit, FormUtilities odkFormUtilities, FormUtilListener<HouseholdProxyHead> listener){
        if (openMode == Mode.CREATE) {
            return new ChangeProxyHeadFormUtil(fragment, context, visit, household, odkFormUtilities, listener);
        } else if (openMode == Mode.EDIT) {
            return new ChangeProxyHeadFormUtil(fragment, context, visit, household, householdProxyHeadToEdit, odkFormUtilities, listener);
        }

        return null;
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxHouseholdProxyHeads = ObjectBoxDatabase.get().boxFor(HouseholdProxyHead.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxResidencies = ObjectBoxDatabase.get().boxFor(Residency.class);
        this.boxHeadRelationships = ObjectBoxDatabase.get().boxFor(HeadRelationship.class);
    }

    @Override
    protected void initialize(){
        super.initialize();

        this.minimunHeadAge = retrieveMinimumHeadAge();

        if (currentMode == Mode.CREATE) {

        } else if (currentMode == Mode.EDIT) {

        }

    }

    private void readSavedEntityState() {
        SavedEntityState savedState = this.boxSavedEntityStates.query(SavedEntityState_.formEntity.equal(CoreFormEntity.CHANGE_PROXY_HEAD.code)
                .and(SavedEntityState_.collectedId.equal(this.entity.id))
                .and(SavedEntityState_.objectKey.equal("changeProxyHeadFormUtilState"))).build().findFirst();

        if (savedState != null) {
            HashMap map = new Gson().fromJson(savedState.objectGsonValue, HashMap.class);
            for (Object key : map.keySet()) {
                mapSavedStates.put(key.toString(), map.get(key).toString());
            }
        }

        String oldProxyHeadId = mapSavedStates.get("oldProxyHeadId");

        if (!StringUtil.isBlank(oldProxyHeadId)) {
            this.oldProxyHead = this.boxHouseholdProxyHeads.get(Long.parseLong(oldProxyHeadId));
        }
    }

    private boolean newProxyHeadChanged(){
        return (this.editedProxyHead != null && this.selectedProxyHead != null && editedProxyHead.id != selectedProxyHead.id) ||
                (this.currentMode==Mode.EDIT && this.editedProxyHead == null && this.selectedProxyHead != null);
    }

    @Override
    protected void preloadValues() {
        //member_details_unknown_lbl

        preloadedMap.put("visitCode", this.visit.code);
        preloadedMap.put("householdCode", this.household.code);

        preloadedMap.put("proxyHeadType", this.selectedProxyHeadType.code);
        if (this.selectedProxyHead != null) {
            preloadedMap.put("proxyHeadCode", this.selectedProxyHead.code);
            preloadedMap.put("proxyHeadName", this.selectedProxyHead.name);
        }
        //preloadedMap.put("proxyHeadRole", "");

        preloadedMap.put("modules", this.user.getSelectedModulesCodes());
    }

    @Override
    protected void preloadUpdatedValues() {
        if (newProxyHeadChanged()) {
            preloadedMap.put("proxyHeadType", this.selectedProxyHeadType.code);
            if (this.selectedProxyHead != null) {
                preloadedMap.put("proxyHeadCode", this.selectedProxyHead.code);
                preloadedMap.put("proxyHeadName", this.selectedProxyHead.name);
            }
        }
    }

    @Override
    public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colHouseholdCode = collectedValues.get("householdCode");
        ColumnValue colProxyHeadType = collectedValues.get("proxyHeadType");
        ColumnValue colProxyHeadCode = collectedValues.get("proxyHeadCode");
        ColumnValue colProxyHeadName = collectedValues.get("proxyHeadName");
        ColumnValue colProxyHeadRole = collectedValues.get("proxyHeadRole");
        ColumnValue colEventDate = collectedValues.get("eventDate"); //not null cannot be in the future nor before dob
        ColumnValue colReason = collectedValues.get("reason");
        ColumnValue colReasonOther = collectedValues.get("reasonOther");

        //ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        //ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        //ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String householdCode = colHouseholdCode.getValue();
        String proxyHeadTypeValue = colProxyHeadType.getValue();
        String proxyHeadCodeValue = colProxyHeadCode.getValue();
        String proxyHeadNameValue = colProxyHeadName.getValue();
        String proxyHeadRoleValue = colProxyHeadRole.getValue();
        Date eventDate = colEventDate.getDateValue();
        String reason = colReason.getValue();
        String reasonOther = colReasonOther.getValue();

        //validations

        if (StringUtil.isBlank(visitCode)){
            String message = this.context.getString(R.string.changehead_visit_code_empty_lbl);
            return new ValidationResult(colVisitCode, message);
        }

        /*if (StringUtil.isBlank(householdCode)){
            String message = this.context.getString(R.string.changeproxyhead_household_code_empty_lbl);
            return new ValidationResult(colHouseholdCode, message);
        }*/

        if (selectedProxyHeadType != ProxyHeadType.NON_DSS_MEMBER && StringUtil.isBlank(proxyHeadNameValue)){
            String message = this.context.getString(R.string.changeproxyhead_proxy_head_type_empty_lbl);
            return new ValidationResult(colProxyHeadName, message);
        }

        if (eventDate == null) {
            String message = this.context.getString(R.string.changeproxyhead_eventdate_empty_lbl);
            return new ValidationResult(colEventDate, message);
        }

        //eventDate cannot be before dob
        if (this.selectedProxyHead != null && eventDate.before(this.selectedProxyHead.dob)){ //is before dob
            String message = this.context.getString(R.string.changeproxyhead_eventdate_not_before_dob_lbl);
            return new ValidationResult(colEventDate, message);
        }

        if (eventDate.after(new Date())){ //is before dob
            String message = this.context.getString(R.string.changeproxyhead_eventdate_not_great_today_lbl);
            return new ValidationResult(colEventDate, message);
        }

        //other validations

        HouseholdProxyHead lastHouseholdProxyHead = getLastHouseholdProxyHead();
        lastHouseholdProxyHead = currentMode==Mode.EDIT ? getLastHouseholdProxyHead(lastHouseholdProxyHead) : lastHouseholdProxyHead; //when editing the newHeadLastRelationship is the last created by this event

        //eventDate vs newHeadLastRelationship.startDate
        if (lastHouseholdProxyHead != null && lastHouseholdProxyHead.startDate != null && eventDate.before(lastHouseholdProxyHead.startDate)){
            //The event date cannot be before the [start date] of the [new Head of Household] last Head Relationship record.
            String message = this.context.getString(R.string.changeproxyhead_eventdate_not_before_new_proxy_head_startdate_lbl, dateUtil.formatYMD(eventDate), dateUtil.formatYMD(lastHouseholdProxyHead.startDate));
            return new ValidationResult(colEventDate, message);
        }

        return ValidationResult.noErrors();
    }

    private HouseholdProxyHead getLastHouseholdProxyHead() {
        return boxHouseholdProxyHeads.query(HouseholdProxyHead_.householdCode.equal(household.code)).orderDesc(HouseholdProxyHead_.startDate).build().findFirst();
    }

    private HouseholdProxyHead getLastHouseholdProxyHead(HouseholdProxyHead toExcludeHouseholdProxyHead) {
        if (toExcludeHouseholdProxyHead == null) return null;

        return boxHouseholdProxyHeads.query(HouseholdProxyHead_.householdCode.equal(household.code).and(HouseholdProxyHead_.id.notEqual(toExcludeHouseholdProxyHead.id))).orderDesc(HouseholdProxyHead_.startDate).build().findFirst();
    }

    @Override
    public void onBeforeFormFinished(HForm form, CollectedDataMap collectedValues) {
        //using it to update collectedHouseholdId, collectedMemberId
        ColumnValue colHouseholdId = collectedValues.get("collectedHouseholdId");
        ColumnValue colMemberId = collectedValues.get("collectedMemberId");

        colHouseholdId.setValue(this.household.collectedId);
        if (selectedProxyHead != null) {
            colMemberId.setValue(this.selectedProxyHead.collectedId);
        }
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
        ColumnValue colProxyHeadType = collectedValues.get("proxyHeadType");
        ColumnValue colProxyHeadCode = collectedValues.get("proxyHeadCode");
        ColumnValue colProxyHeadName = collectedValues.get("proxyHeadName");
        ColumnValue colProxyHeadRole = collectedValues.get("proxyHeadRole");
        ColumnValue colEventDate = collectedValues.get("eventDate"); //not null cannot be in the future nor before dob
        ColumnValue colReason = collectedValues.get("reason");
        ColumnValue colReasonOther = collectedValues.get("reasonOther");

        //ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        //ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        //ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String householdCode = colHouseholdCode.getValue();
        String proxyHeadTypeValue = colProxyHeadType.getValue();
        String proxyHeadCodeValue = colProxyHeadCode.getValue();
        String proxyHeadNameValue = colProxyHeadName.getValue();
        String proxyHeadRoleValue = colProxyHeadRole.getValue();
        Date eventDate = colEventDate.getDateValue();
        String reason = colReason.getValue();
        String reasonOther = colReasonOther.getValue();

        String affectedMembers = null;

        Date closeEventDate = GeneralUtil.getDateAdd(eventDate, -1);
        
        //new proxy head
        HouseholdProxyHead newHouseholdProxyHead = new HouseholdProxyHead();
        newHouseholdProxyHead.householdCode = household.code;
        newHouseholdProxyHead.visitCode = visitCode;
        newHouseholdProxyHead.proxyHeadType = ProxyHeadType.getFrom(proxyHeadTypeValue);
        newHouseholdProxyHead.proxyHeadCode = StringUtil.isBlank(proxyHeadCodeValue) ? null : proxyHeadCodeValue;
        newHouseholdProxyHead.proxyHeadName = StringUtil.isBlank(proxyHeadNameValue) ? null : proxyHeadNameValue;
        newHouseholdProxyHead.proxyHeadRole = ProxyHeadRole.getFrom(proxyHeadRoleValue);
        newHouseholdProxyHead.startDate = eventDate;
        newHouseholdProxyHead.endDate = null;
        newHouseholdProxyHead.reason = ProxyHeadChangeReason.getFrom(reason);
        newHouseholdProxyHead.reasonOther = reasonOther;
        newHouseholdProxyHead.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        newHouseholdProxyHead.recentlyCreated = true;
        newHouseholdProxyHead.recentlyCreatedUri = result.getFilename();
        this.boxHouseholdProxyHeads.put(newHouseholdProxyHead);

        this.household.proxyHeadType = newHouseholdProxyHead.proxyHeadType;
        this.household.proxyHeadCode = newHouseholdProxyHead.proxyHeadCode;
        this.household.proxyHeadName = newHouseholdProxyHead.proxyHeadName;
        this.household.proxyHeadRole = newHouseholdProxyHead.proxyHeadRole;
        this.boxHouseholds.put(this.household);

        //close old proxy head
        if (this.oldProxyHead != null){
            this.oldProxyHead.endDate = closeEventDate;
            this.boxHouseholdProxyHeads.put(this.oldProxyHead);
        }

        //save the new head member previous headRelationshipType
        HashMap<String,String> saveStateMap = new HashMap<>();
        saveStateMap.put("oldProxyHeadId", this.oldProxyHead != null ? this.oldProxyHead.id+"" : "");

        //save the list of ids of new head relationships
        SavedEntityState entityState = new SavedEntityState(CoreFormEntity.CHANGE_PROXY_HEAD, newHouseholdProxyHead.id, "changeProxyHeadFormUtilState", new Gson().toJson(saveStateMap));
        this.boxSavedEntityStates.put(entityState);

        //save core collected data
        collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.CHANGE_PROXY_HEAD;
        collectedData.formEntityId = newHouseholdProxyHead.id;
        collectedData.formEntityCode = newHouseholdProxyHead.householdCode;
        collectedData.formEntityCodes = affectedMembers; //new head code, spouse, head related individuals
        collectedData.formEntityName = (oldProxyHeadMember != null ? oldProxyHeadMember.name : "#") +" -> "+proxyHeadNameValue;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        collectedData.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        collectedData.extension.setTarget(this.getFormExtension(collectedData.formEntity));
        this.boxCoreCollectedData.put(collectedData);

        this.entity = newHouseholdProxyHead;
        this.collectExtensionForm(collectedValues);
    }

    private void onModeEdit(CollectedDataMap collectedValues, XmlFormResult result) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colHouseholdCode = collectedValues.get("householdCode");
        ColumnValue colProxyHeadType = collectedValues.get("proxyHeadType");
        ColumnValue colProxyHeadCode = collectedValues.get("proxyHeadCode");
        ColumnValue colProxyHeadName = collectedValues.get("proxyHeadName");
        ColumnValue colProxyHeadRole = collectedValues.get("proxyHeadRole");
        ColumnValue colEventDate = collectedValues.get("eventDate"); //not null cannot be in the future nor before dob
        ColumnValue colReason = collectedValues.get("reason");
        ColumnValue colReasonOther = collectedValues.get("reasonOther");

        //ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        //ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        //ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String householdCode = colHouseholdCode.getValue();
        String proxyHeadTypeValue = colProxyHeadType.getValue();
        String proxyHeadCodeValue = colProxyHeadCode.getValue();
        String proxyHeadNameValue = colProxyHeadName.getValue();
        String proxyHeadRoleValue = colProxyHeadRole.getValue();
        Date eventDate = colEventDate.getDateValue();
        String reason = colReason.getValue();
        String reasonOther = colReasonOther.getValue();

        String affectedMembers = null;

        Date closeEventDate = GeneralUtil.getDateAdd(eventDate, -1);
        
        //new head head relationship
        HouseholdProxyHead newHouseholdProxyHead = boxHouseholdProxyHeads.get(this.entity.id);
        newHouseholdProxyHead.householdCode = household.code;
        newHouseholdProxyHead.visitCode = visitCode;
        newHouseholdProxyHead.proxyHeadType = ProxyHeadType.getFrom(proxyHeadTypeValue);
        newHouseholdProxyHead.proxyHeadCode = StringUtil.isBlank(proxyHeadCodeValue) ? null : proxyHeadCodeValue;
        newHouseholdProxyHead.proxyHeadName = StringUtil.isBlank(proxyHeadNameValue) ? null : proxyHeadNameValue;
        newHouseholdProxyHead.proxyHeadRole = ProxyHeadRole.getFrom(proxyHeadRoleValue);
        newHouseholdProxyHead.startDate = eventDate;
        newHouseholdProxyHead.endDate = null;
        newHouseholdProxyHead.reason = ProxyHeadChangeReason.getFrom(reason);
        newHouseholdProxyHead.reasonOther = reasonOther;
        //newHeadRelationship.recentlyCreated = true;
        //newHeadRelationship.recentlyCreatedUri = result.getFilename();
        this.boxHouseholdProxyHeads.put(newHouseholdProxyHead);

        this.household.proxyHeadType = newHouseholdProxyHead.proxyHeadType;
        this.household.proxyHeadCode = newHouseholdProxyHead.proxyHeadCode;
        this.household.proxyHeadName = newHouseholdProxyHead.proxyHeadName;
        this.household.proxyHeadRole = newHouseholdProxyHead.proxyHeadRole;
        this.boxHouseholds.put(this.household);

        //close proxy Head again
        if (this.oldProxyHead != null){
            this.oldProxyHead.endDate = closeEventDate;
            this.boxHouseholdProxyHeads.put(this.oldProxyHead);
        }

        //save core collected data
        collectedData.visitId = visit.id;
        collectedData.formEntityCodes = affectedMembers; //new head code, spouse, head related individuals
        collectedData.formEntityName = (oldProxyHeadMember != null ? oldProxyHeadMember.name : "#") +" -> "+proxyHeadNameValue;
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
            checkProxyHeadRegistrationDialog();
        } else if (this.currentMode == Mode.EDIT) {
            checkChangeNewProxyHeadDialog();
        }
    }

    private void retrieveHeads() {
        HeadRelationship headRelationship = boxHeadRelationships.query(
                HeadRelationship_.householdCode.equal(household.code)
                        .and(HeadRelationship_.relationshipType.equal(HeadRelationshipType.HEAD_OF_HOUSEHOLD.code))
                        .and(HeadRelationship_.endType.equal(HeadRelationshipEndType.NOT_APPLICABLE.code))
        ).orderDesc(HeadRelationship_.startDate).build().findFirst();

        if (headRelationship != null) {
            this.currentHead =  Queries.getMemberByCode(boxMembers, headRelationship.memberCode);
        }

        //get proxy head
        HouseholdProxyHead lastHouseholdProxyHead = boxHouseholdProxyHeads.query(HouseholdProxyHead_.householdCode.equal(household.code)).orderDesc(HouseholdProxyHead_.startDate).build().findFirst();
        if (lastHouseholdProxyHead != null && lastHouseholdProxyHead.endDate == null) { //is the recent head
            this.oldProxyHead = lastHouseholdProxyHead;

            if (!StringUtil.isBlank(this.oldProxyHead.proxyHeadCode)){
                this.oldProxyHeadMember = Queries.getMemberByCode(boxMembers, this.oldProxyHead.proxyHeadCode);
            }
        }
    }

    private ProxyHeadType getProxyHeadType(Member member) {
        if (member == null) return ProxyHeadType.NON_DSS_MEMBER;

        boolean isResident = boxResidencies.query(Residency_.householdCode.equal(this.household.code).and(Residency_.memberCode.equal(member.code)).and(Residency_.endType.equal(ResidencyEndType.NOT_APPLICABLE.code))).build().count()>0;

        return isResident ? ProxyHeadType.RESIDENT : ProxyHeadType.NON_RESIDENT;
    }

    private void checkProxyHeadRegistrationDialog() {

        DialogFactory.createMessageYN(this.context, R.string.eventType_change_proxy, R.string.changeproxyhead_head_exists_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                openNewProxyHeadFilterDialog();
            }

            @Override
            public void onNoClicked() {
                selectedProxyHead = null;
                selectedProxyHeadType = ProxyHeadType.NON_DSS_MEMBER;
                executeCollectForm();
            }
        }).show();

    }

    private void checkChangeNewProxyHeadDialog(){

        DialogFactory.createMessageYN(this.context, R.string.changeproxyhead_dialog_change_title_lbl, R.string.changeproxyhead_dialog_head_change_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                checkProxyHeadRegistrationDialog();
            }

            @Override
            public void onNoClicked() {
                //head remains unchanged
                executeCollectForm();
            }
        }).show();

    }

    private void openNewProxyHeadFilterDialog(){

        MemberFilterDialog dialog = MemberFilterDialog.newInstance(this.fragmentManager, context.getString(R.string.changeproxyhead_new_head_select_lbl), true, new MemberFilterDialog.Listener() {
            @Override
            public void onSelectedMember(Member member) {
                Log.d("selected-proxyhead", ""+member.getCode());

                selectedProxyHead = member;
                selectedProxyHeadType = getProxyHeadType(member);

                executeCollectForm();
            }

            @Override
            public void onCanceled() {

            }
        });

        dialog.setFilterMinAge(onlyMinorsLeftToBeHead ? 0 : this.minimunHeadAge, true);
        dialog.setFilterHouseCode(visit.householdCode, false);
        dialog.setFilterStatus(MemberFilterDialog.StatusFilter.RESIDENT, false);
        if (this.currentHead != null) {
            dialog.addFilterExcludeMember(this.currentHead);
        }
        if (this.oldProxyHeadMember != null) {
            dialog.addFilterExcludeMember(this.oldProxyHeadMember);
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
