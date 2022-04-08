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
import org.philimone.hds.explorer.model.Death_;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.HeadRelationship_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.MaritalRelationship;
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
import org.philimone.hds.forms.model.CollectedDataMap;
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

public class ChangeHeadFormUtil extends FormUtil<Member> {

    private Box<Member> boxMembers;
    private Box<Residency> boxResidencies;
    private Box<Household> boxHouseholds;
    private Box<HeadRelationship> boxHeadRelationships;
    private Box<CoreCollectedData> boxCoreCollectedData;

    private Household household;
    private Visit visit;
    private Member oldHeadMember;
    private Member newHeadMember;
    private List<HeadRelationship> oldHeadMemberRelationships;
    private HeadRelationship oldHeadMemberRelationship;

    private List<Member> householdResidents;
    private int minimunHeadAge;

    public ChangeHeadFormUtil(FragmentManager fragmentManager, Context context, Visit visit, Household household, FormUtilListener<Member> listener){
        super(fragmentManager, context, FormUtil.getChangeHeadForm(context), listener);

        //Log.d("enu-household", ""+household);

        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    public ChangeHeadFormUtil(FragmentManager fragmentManager, Context context, Visit visit, Household household, Member newHeadMember, FormUtilListener<Member> listener){
        super(fragmentManager, context, FormUtil.getChangeHeadForm(context), newHeadMember, listener);

        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxHeadRelationships = ObjectBoxDatabase.get().boxFor(HeadRelationship.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxResidencies = ObjectBoxDatabase.get().boxFor(Residency.class);
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
    }

    @Override
    protected void initialize(){
        super.initialize();

        this.oldHeadMember = Queries.getMemberByCode(boxMembers, this.household.headCode);

        this.oldHeadMemberRelationships = this.boxHeadRelationships.query(
                                        HeadRelationship_.householdCode.equal(this.household.code)
                                       .and(HeadRelationship_.headCode.equal(this.oldHeadMember.code))
                                       .and(HeadRelationship_.endType.equal(HeadRelationshipEndType.NOT_APPLICABLE.code)))
                                       .orderDesc(HeadRelationship_.startDate)
                                       .build().find();

        //get current memberHeadRelationship
        this.oldHeadMemberRelationship = this.boxHeadRelationships.query(
                     HeadRelationship_.householdCode.equal(this.household.code)
                .and(HeadRelationship_.memberCode.equal(this.oldHeadMember.code))
                .and(HeadRelationship_.endType.equal(HeadRelationshipEndType.NOT_APPLICABLE.code)))
                .orderDesc(HeadRelationship_.startDate)
                .build().findFirst();

        this.minimunHeadAge = retrieveMinimumHeadAge();
        this.householdResidents = getHouseholdResidentsExcOldHead();

    }

    private List<Member> getHouseholdResidentsExcOldHead() {
        List<Residency> residencies = this.boxResidencies.query(
                Residency_.householdCode.equal(this.household.code).and(Residency_.endType.equal(ResidencyEndType.NOT_APPLICABLE.code)))
                .order(Residency_.memberCode)
                .build().find();

        //exclude old household head code
        List<String> residentMembersExOldHead = new ArrayList<>();
        for (Residency residency : residencies) {
            if (!oldHeadMember.code.equals(residency.memberCode)) { //its not the old member
                residentMembersExOldHead.add(residency.memberCode);
                Log.d("member", ""+residency.memberCode);
            }
        }

        List<Member> members = this.boxMembers.query(Member_.code.oneOf(residentMembersExOldHead.toArray(new String[0]))).build().find();

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
        preloadedMap.put("oldHeadCode", this.oldHeadMember.code);
        preloadedMap.put("oldHeadName", this.oldHeadMember.name);
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
            String message = this.context.getString(R.string.death_visit_code_empty_lbl);
            return new ValidationResult(colVisitCode, message);
        }

        if (StringUtil.isBlank(oldHeadCode)){
            String message = this.context.getString(R.string.death_member_code_empty_lbl);
            return new ValidationResult(colOldHeadCode, message);
        }

        if (StringUtil.isBlank(newHeadCode)){
            String message = this.context.getString(R.string.death_member_code_empty_lbl);
            return new ValidationResult(colNewHeadCode, message);
        }

        if (eventDate == null) {
            String message = this.context.getString(R.string.death_deathdate_empty_lbl);
            return new ValidationResult(colEventDate, message);
        }

        //eddDate cannot be before dob
        if (eventDate != null && eventDate.before(this.oldHeadMember.dob)){ //is before dob
            String message = this.context.getString(R.string.death_deathdate_not_before_dob_lbl);
            return new ValidationResult(colEventDate, message);
        }

        if (eventDate != null && eventDate.after(new Date())){ //is before dob
            String message = this.context.getString(R.string.death_deathdate_not_before_dob_lbl);
            return new ValidationResult(colEventDate, message);
        }

        //other validations

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
        if (oldHeadMemberRelationships != null && oldHeadMemberRelationships.size()>0){
            for (HeadRelationship headRelationship : oldHeadMemberRelationships) {
                if (!this.oldHeadMemberRelationship.equals(headRelationship)){
                    headRelationship.endType = HeadRelationshipEndType.CHANGE_OF_HEAD_OF_HOUSEHOLD;
                    headRelationship.endDate = eventDate;
                    this.boxHeadRelationships.put(headRelationship);
                }
            }
        }
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
            headRelationship.recentlyCreated = true;
            this.boxHeadRelationships.put(headRelationship);
        }
        affectedMembers = addAffectedMembers(affectedMembers, this.newHeadMember.code);

        //save core collected data
        CoreCollectedData collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.DEATH;
        collectedData.formEntityId = oldHeadMember.id;
        collectedData.formEntityCode = oldHeadMember.code;
        collectedData.formEntityCodes = affectedMembers; //new head code, spouse, head related individuals
        collectedData.formEntityName = oldHeadMember.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        this.boxCoreCollectedData.put(collectedData);

        if (listener != null) {
            listener.onNewEntityCreated(newHeadMember);
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


        //search for new head of household
        openNewHouseholdHeadFilterDialog();
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
        dialog.setFilterHouseCode(visit.householdCode);
        dialog.setFilterStatus(MemberFilterDialog.StatusFilter.RESIDENT, true);
        dialog.setFilterExcludeMember(this.oldHeadMember.code);
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
