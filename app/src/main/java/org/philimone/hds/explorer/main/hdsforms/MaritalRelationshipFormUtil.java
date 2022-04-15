package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.MemberFilterDialog;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Death;
import org.philimone.hds.explorer.model.MaritalRelationship;
import org.philimone.hds.explorer.model.MaritalRelationship_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.Gender;
import org.philimone.hds.explorer.model.enums.MaritalEndStatus;
import org.philimone.hds.explorer.model.enums.MaritalStartStatus;
import org.philimone.hds.explorer.model.enums.MaritalStatus;
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

public class MaritalRelationshipFormUtil extends FormUtil<MaritalRelationship> {

    private Box<Member> boxMembers;
    private Box<Death> boxDeaths;
    private Box<MaritalRelationship> boxMaritalRelationships;
    private Box<CoreCollectedData> boxCoreCollectedData;

    private Visit visit;
    private Member spouseA;
    private Member spouseB;
    private MaritalRelationship currentMaritalRelationship; //* if it is a relationship to close *//
    private int minimunSpouseAge;
    private boolean genderChecking;

    public MaritalRelationshipFormUtil(FragmentManager fragmentManager, Context context, Visit visit, Member spouseA, FormUtilListener<MaritalRelationship> listener){
        super(fragmentManager, context, FormUtil.getMaritalRelationshipForm(context), listener);

        this.visit = visit;
        this.spouseA = spouseA;

        initBoxes();
        initialize();
    }

    public MaritalRelationshipFormUtil(FragmentManager fragmentManager, Context context, Visit visit, MaritalRelationship relationshipToEdit, FormUtilListener<MaritalRelationship> listener){
        super(fragmentManager, context, FormUtil.getMaritalRelationshipForm(context), relationshipToEdit, listener);

        this.visit = visit;

        initBoxes();
        initialize();

        this.spouseA = boxMembers.query().equal(Member_.code, relationshipToEdit.memberA_code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
        this.spouseB = boxMembers.query().equal(Member_.code, relationshipToEdit.memberB_code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxDeaths = ObjectBoxDatabase.get().boxFor(Death.class);
        this.boxMaritalRelationships = ObjectBoxDatabase.get().boxFor(MaritalRelationship.class);
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
    }

    @Override
    protected void initialize(){
        super.initialize();

        this.minimunSpouseAge = retrieveMinimumSpouseAge();
        this.genderChecking = retrieveGenderChecking();
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
    }

    @Override
    protected void preloadUpdatedValues() {
        //only father and mother can be updated using dialogs
        preloadedMap.put("memberA", spouseA.code);
        preloadedMap.put("memberB", spouseB.code);
        preloadedMap.put("memberA_name", spouseA.name);
        preloadedMap.put("memberB_name", spouseB.name);
        preloadedMap.put("memberA_status", spouseA.maritalStatus.code);
        preloadedMap.put("memberB_status", spouseB.maritalStatus.code);
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
            if (previousRelA != null && previousRelA.endStatus==MaritalEndStatus.NOT_APPLICABLE){
                String message = this.context.getString(R.string.maritalrelationship_p1_previous_not_closed_lbl, "A");
                return new ValidationResult(colMemberA, message);
            }
            if (previousRelB != null && previousRelB.endStatus==MaritalEndStatus.NOT_APPLICABLE){
                String message = this.context.getString(R.string.maritalrelationship_p1_previous_not_closed_lbl, "B");
                return new ValidationResult(colMemberB, message);
            }

            //P2. Check If endDate of P.Relationship is not before new startDate
            if (previousRelA != null && eventDate.compareTo(previousRelA.endDate)<=0){
                String message = this.context.getString(R.string.maritalrelationship_p2_previous_enddate_overlap_lbl, "A");
                return new ValidationResult(colMemberA, message);
            }
            if (previousRelB != null && eventDate.compareTo(previousRelB.endDate)<=0){
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


        return ValidationResult.noErrors();
    }

    @Override
    public void onFormFinished(HForm form, CollectedDataMap collectedValues, XmlFormResult result) {

        Log.d("resultxml", result.getXmlResult());

        if (currentMode == Mode.EDIT) {
            System.out.println("Editing Member Enumeration Not implemented yet");
            assert 1==0;
        }


        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colMemberA = collectedValues.get("memberA");
        //ColumnValue colMemberA_name = collectedValues.get("memberA_name");
        ColumnValue colMemberB = collectedValues.get("memberB");
        //ColumnValue colMemberB_name = collectedValues.get("memberB_name");
        //ColumnValue colMemberA_status = collectedValues.get("memberA_status");
        //ColumnValue colMemberB_status = collectedValues.get("memberB_status");
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
        MaritalStartStatus startRelationType = MaritalStartStatus.getFrom(colRelationshipType.getValue());
        MaritalEndStatus endRelationType = MaritalEndStatus.getFrom(colRelationshipType.getValue());
        Date eventDate = colEventDate.getDateValue();



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

            maritalRelationship.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
            maritalRelationship.recentlyCreated = true;
            maritalRelationship.recentlyCreatedUri = result.getFilename();

            spouseA.maritalStatus = MaritalStatus.getFrom(startRelationType.code);
            spouseB.maritalStatus = MaritalStatus.getFrom(startRelationType.code);

        } else if (endRelationType != null) {
            //updates a existing relationship
            maritalRelationship = currentMaritalRelationship;

            maritalRelationship.endStatus = endRelationType;
            maritalRelationship.endDate = eventDate;

            spouseA.maritalStatus = MaritalStatus.getFrom(endRelationType.code);
            spouseB.maritalStatus = MaritalStatus.getFrom(endRelationType.code);
        }

        //save data
        boxMaritalRelationships.put(maritalRelationship);
        boxMembers.put(spouseA, spouseB);


        //save core collected data
        CoreCollectedData collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.MARITAL_RELATIONSHIP;
        collectedData.formEntityId = spouseA.id;
        collectedData.formEntityCode = spouseA.code;
        collectedData.formEntityName = spouseA.name + " -> " + spouseB.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();

        boxCoreCollectedData.put(collectedData);


        if (listener != null) {
            listener.onNewEntityCreated(maritalRelationship);
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

        //Check if MemberA has a registered marital relationship?
        //1. get last marital if still opened
        //2. select a spouse


        MaritalRelationship maritalRelationship = boxMaritalRelationships.query().equal(MaritalRelationship_.memberA_code, spouseA.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                                                 .or()
                                                                                 .equal(MaritalRelationship_.memberB_code, spouseA.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                                                 .orderDesc(MaritalRelationship_.startDate)
                                                                                 .build().findFirst();

        boolean existsRelationshipToClose = maritalRelationship != null && maritalRelationship.endStatus == MaritalEndStatus.NOT_APPLICABLE;

        if (existsRelationshipToClose) {
            this.currentMaritalRelationship = maritalRelationship;
        }

        if (currentMode == Mode.CREATE) {
            //1. create a try to close relationship
            //2. create a new relationship

            if (existsRelationshipToClose) {
                //if its about to close a relationship pull in the same order it was registered
                this.spouseA = boxMembers.query().equal(Member_.code, maritalRelationship.memberA_code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
                this.spouseB = boxMembers.query().equal(Member_.code, maritalRelationship.memberB_code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

                executeCollectForm();
            } else {
                //a new relationship will be created - then - select spouse B
                openSpouseFilterDialog();
            }

        } else if (currentMode == Mode.EDIT) {
            //edit a already created relationship
            //if is a new - try to update/change the spouse b

            if (existsRelationshipToClose) {
                executeCollectForm();
            } else {
                checkChangeSpouseDialog();
            }
        }

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

    private void openSpouseFilterDialog(){

        MemberFilterDialog dialog = MemberFilterDialog.newInstance(this.fragmentManager, context.getString(R.string.maritalrelationship_spouse_select_lbl), true, new MemberFilterDialog.Listener() {
            @Override
            public void onSelectedMember(Member member) {
                Log.d("selected-spouse", ""+member.getCode());

                spouseB = member;
                executeCollectForm();
            }

            @Override
            public void onCanceled() {

            }
        });

        if (genderChecking) {
            if (spouseA.gender == Gender.MALE){
                dialog.setGenderFemaleOnly();
            } else {
                dialog.setGenderMaleOnly();
            }
        }

        dialog.setFilterMinAge(this.minimunSpouseAge, true);
        dialog.setFilterHouseCode(visit.householdCode);
        dialog.setStartSearchOnShow(true);
        dialog.show();
    }

    private void checkChangeSpouseDialog(){

        DialogFactory.createMessageYN(this.context, R.string.maritalrelationship_spouse_select_lbl, R.string.maritalrelationship_spouse_change_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                openSpouseFilterDialog();
            }

            @Override
            public void onNoClicked() {
                //spouse remains unchanged
            }
        }).show();

    }

}
