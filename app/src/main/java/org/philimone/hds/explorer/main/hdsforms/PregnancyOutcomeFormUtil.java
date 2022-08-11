package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.fragment.MemberFilterDialog;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Death;
import org.philimone.hds.explorer.model.Death_;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.HeadRelationship_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.PregnancyChild;
import org.philimone.hds.explorer.model.PregnancyChild_;
import org.philimone.hds.explorer.model.PregnancyOutcome;
import org.philimone.hds.explorer.model.PregnancyOutcome_;
import org.philimone.hds.explorer.model.PregnancyRegistration;
import org.philimone.hds.explorer.model.PregnancyRegistration_;
import org.philimone.hds.explorer.model.Residency;
import org.philimone.hds.explorer.model.Residency_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.converters.StringCollectionConverter;
import org.philimone.hds.explorer.model.enums.BirthPlace;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.Gender;
import org.philimone.hds.explorer.model.enums.HeadRelationshipType;
import org.philimone.hds.explorer.model.enums.MaritalStatus;
import org.philimone.hds.explorer.model.enums.PregnancyOutcomeType;
import org.philimone.hds.explorer.model.enums.PregnancyStatus;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipEndType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipStartType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyStartType;
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

public class PregnancyOutcomeFormUtil extends FormUtil<PregnancyOutcome> {

    private Box<Member> boxMembers;
    private Box<PregnancyRegistration> boxPregnancyRegistrations;
    private Box<PregnancyOutcome> boxPregnancyOutcomes;
    private Box<PregnancyChild> boxPregnancyChilds;
    private Box<HeadRelationship> boxHeadRelationships;
    private Box<Residency> boxResidencies;
    private Box<Death> boxDeaths;
    
    //private Household household;
    private Visit visit;
    private Member mother;
    private Member father;
    private PregnancyRegistration pregnancyRegistration;
    private boolean pregnancyRegistrationCreated;
    private int numberOfOutcomes = 1;

    private int minimunFatherAge;
    private int minimunMotherAge;

    public PregnancyOutcomeFormUtil(Fragment fragment, Context context, Visit visit, Household household, Member mother, PregnancyRegistration pregnancyRegistration, boolean recentlyCreatedForOutcome, FormUtilities odkFormUtilities, FormUtilListener<PregnancyOutcome> listener){
        super(fragment, context, FormUtil.getPregnancyOutcomeForm(context), odkFormUtilities, listener);

        //Log.d("enu-household", ""+household);

        this.household = household;
        this.mother = mother;
        this.visit = visit;
        this.pregnancyRegistration = pregnancyRegistration;
        this.pregnancyRegistrationCreated = recentlyCreatedForOutcome;

        initBoxes();
        initialize();
    }

    public PregnancyOutcomeFormUtil(Fragment fragment, Context context, Visit visit, Household household, PregnancyOutcome pregToEdit, FormUtilities odkFormUtilities, FormUtilListener<PregnancyOutcome> listener){
        super(fragment, context, FormUtil.getPregnancyOutcomeForm(context), pregToEdit, odkFormUtilities, listener);

        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();

        this.father = this.boxMembers.query(Member_.code.equal(pregToEdit.fatherCode)).build().findFirst();
        this.mother = this.boxMembers.query(Member_.code.equal(pregToEdit.motherCode)).build().findFirst();
        this.numberOfOutcomes = pregToEdit.numberOfOutcomes;

        this.pregnancyRegistration = this.boxPregnancyRegistrations.query(PregnancyRegistration_.code.equal(pregToEdit.code)).build().findFirst();
    }

    public static PregnancyOutcomeFormUtil newInstance(Mode openMode, Fragment fragment, Context context, Visit visit, Household household, Member mother, PregnancyRegistration pregnancyRegistration, boolean recentlyCreatedForOutcome, PregnancyOutcome pregToEdit, FormUtilities odkFormUtilities, FormUtilListener<PregnancyOutcome> listener){
        if (openMode == Mode.CREATE) {
            return new PregnancyOutcomeFormUtil(fragment, context, visit, household, mother, pregnancyRegistration, recentlyCreatedForOutcome, odkFormUtilities, listener);
        } else if (openMode == Mode.EDIT) {
            return new PregnancyOutcomeFormUtil(fragment, context, visit, household, pregToEdit, odkFormUtilities, listener);
        }

        return null;
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxPregnancyRegistrations = ObjectBoxDatabase.get().boxFor(PregnancyRegistration.class);
        this.boxPregnancyOutcomes = ObjectBoxDatabase.get().boxFor(PregnancyOutcome.class);
        this.boxPregnancyChilds = ObjectBoxDatabase.get().boxFor(PregnancyChild.class);
        this.boxHeadRelationships = ObjectBoxDatabase.get().boxFor(HeadRelationship.class);
        this.boxResidencies = ObjectBoxDatabase.get().boxFor(Residency.class);
        this.boxDeaths = ObjectBoxDatabase.get().boxFor(Death.class);

    }

    @Override
    protected void initialize(){
        super.initialize();

        this.minimunFatherAge = retrieveMinimumFatherAge();
        this.minimunMotherAge = retrieveMinimumMotherAge();
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

    @Override
    protected void preloadValues() {
        //member_details_unknown_lbl
        boolean parentsAreHouseholdHead = areFatherOrMotherHead();

        preloadedMap.put("visitCode", this.visit.code);
        preloadedMap.put("code", this.pregnancyRegistration.code);
        preloadedMap.put("motherCode", this.mother.code);
        preloadedMap.put("motherName", this.mother.name);
        preloadedMap.put("fatherCode", this.father.code);
        preloadedMap.put("fatherName", this.father.name);
        preloadedMap.put("numberOfOutcomes", this.numberOfOutcomes+"");

        RepeatObject childsRepObj = new RepeatObject();
        List<String> generatedCodes = new ArrayList<>();

        for (int i=0; i < this.numberOfOutcomes; i++) {
            Log.d("outcome", ""+i);
            Map<String, String> obj = childsRepObj.createNewObject();

            String code = codeGenerator.generateMemberCode(this.household, generatedCodes);
            generatedCodes.add(code);

            obj.put("childCode", code);
            obj.put("headRelationshipType", parentsAreHouseholdHead ? "SON" : ""); //Set the head if is one of them
            obj.put("childOrdinalPosition", ""+(i+1));
        }
        Log.d("childs", childsRepObj.getList().size()+"");
        preloadedMap.put("childs", childsRepObj);

        preloadedMap.put("modules", this.user.getSelectedModulesCodes());
    }

    @Override
    protected void preloadUpdatedValues() {
        preloadedMap.put("fatherCode", this.father.code);
        preloadedMap.put("fatherName", this.father.name);
    }

    private boolean areFatherOrMotherHead(){
        boolean oneOfIsHead = this.boxHeadRelationships.query(
                             HeadRelationship_.householdCode.equal(this.household.code)
                        .and(HeadRelationship_.memberCode.equal(this.mother.code).or(HeadRelationship_.memberCode.equal(this.father.code)))
                        .and(HeadRelationship_.relationshipType.equal(HeadRelationshipType.HEAD_OF_HOUSEHOLD.code))
                        .and(HeadRelationship_.endType.equal(HeadRelationshipEndType.NOT_APPLICABLE.code)))
                .orderDesc(HeadRelationship_.startDate)
                .build().count()>0;

        return oneOfIsHead;
    }

    @Override
    public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colCode = collectedValues.get("code");
        ColumnValue colMotherCode = collectedValues.get("motherCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colMotherName = collectedValues.get("motherName"); //not blank
        ColumnValue colFatherCode = collectedValues.get("fatherCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colFatherName = collectedValues.get("fatherName"); //not blank
        ColumnValue colNrOfOutcomes = collectedValues.get("numberOfOutcomes");
        ColumnValue colOutcomeDate = collectedValues.get("outcomeDate"); //not null cannot be in the future nor before dob
        ColumnValue colBirthPlace = collectedValues.get("birthPlace");
        ColumnValue colBirthPlaceOther = collectedValues.get("birthPlaceOther");

        RepeatColumnValue repChilds = collectedValues.getRepeatColumn("childs");
        List<ColumnValue> colOutcomeTypes = new ArrayList<>();
        List<ColumnValue> colChildCodes = new ArrayList<>();
        List<ColumnValue> colChildNames = new ArrayList<>();
        List<ColumnValue> colChildGenders = new ArrayList<>();
        List<ColumnValue> colChildOrdPositions = new ArrayList<>();
        List<ColumnValue> colChildRelationships = new ArrayList<>();

        if (repChilds != null) {
            for (int i = 0; i < repChilds.getCount(); i++) {
                ColumnValue colOutcomeType = repChilds.get("outcomeType", i);
                ColumnValue colChildCode = repChilds.get("childCode", i);
                ColumnValue colChildName = repChilds.get("childName", i);
                ColumnValue colChildGender = repChilds.get("childGender", i);
                ColumnValue colChildOrdPosition = repChilds.get("childOrdinalPosition", i);
                ColumnValue colChildRelationship = repChilds.get("headRelationshipType", i);

                colOutcomeTypes.add(colOutcomeType);
                colChildCodes.add(colChildCode);
                colChildNames.add(colChildName);
                colChildGenders.add(colChildGender);
                colChildOrdPositions.add(colChildOrdPosition);
                colChildRelationships.add(colChildRelationship);
            }
        }

        //ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        //ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        //ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String code = colCode.getValue();
        String motherCode = colMotherCode.getValue();
        String motherName = colMotherName.getValue();
        String fatherCode = colMotherCode.getValue();
        String fatherName = colMotherName.getValue();
        Integer nrOfOutcomes = colNrOfOutcomes.getIntegerValue();
        Date outcomeDate = colOutcomeDate.getDateValue();
        BirthPlace birthPlace = BirthPlace.getFrom(colBirthPlace.getValue());
        String birthPlaceOther = colBirthPlaceOther.getValue();

        List<String> childOutcomeTypes = colOutcomeTypes.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> childCodes = colChildCodes.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> childNames = colChildNames.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> childGenders = colChildGenders.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> childOrdPositions = colChildOrdPositions.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> childRelationships = colChildRelationships.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        
        
        //validations
        //memberCode blank/valid
        if (!codeGenerator.isPregnancyCodeValid(code)){
            String message = this.context.getString(R.string.pregnancy_outcome_code_err_lbl);
            return new ValidationResult(colMotherCode, message);
        }

        //code is duplicate
        if (currentMode == Mode.CREATE && boxPregnancyOutcomes.query().equal(PregnancyOutcome_.code, code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().count() > 0){
            String message = this.context.getString(R.string.pregnancy_outcome_code_exists_lbl);
            return new ValidationResult(colCode, message);
        }

        if (StringUtil.isBlank(motherCode)){
            String message = this.context.getString(R.string.pregnancy_outcome_mothercode_empty_lbl);
            return new ValidationResult(colMotherName, message);
        }

        //validate preg status
        if (nrOfOutcomes == null || nrOfOutcomes <= 0){
            String message = this.context.getString(R.string.pregnancy_outcome_nroutcomes_empty_lbl);
            return new ValidationResult(colNrOfOutcomes, message);
        }

        if (outcomeDate == null){
            String message = this.context.getString(R.string.pregnancy_outcome_outcomedate_empty_lbl);
            return new ValidationResult(colOutcomeDate, message);
        }

        //eddDate cannot be before dob
        if (outcomeDate != null && outcomeDate.before(this.mother.dob)){ //is before dob
            String message = this.context.getString(R.string.pregnancy_outcome_outcdate_not_before_dob_lbl);
            return new ValidationResult(colOutcomeDate, message);
        }
        
        if (pregnancyRegistration != null && pregnancyRegistration.status == PregnancyStatus.PREGNANT){
            String message = this.context.getString(R.string.pregnancy_outcome_previous_pending_lbl);
            return new ValidationResult(colCode, message);
        }

        //validate childs
        for (int i = 0; i < repChilds.getCount(); i++) {
            ColumnValue colOutcomeType = repChilds.get("outcomeType", i);
            ColumnValue colChildCode = repChilds.get("childCode", i);
            ColumnValue colChildName = repChilds.get("childName", i);
            ColumnValue colChildGender = repChilds.get("childGender", i);
            ColumnValue colChildOrdPosition = repChilds.get("childOrdinalPosition", i);
            ColumnValue colChildRelationship = repChilds.get("headRelationshipType", i);

            PregnancyOutcomeType outcomeType = PregnancyOutcomeType.getFrom(colOutcomeType.getValue());
            String childCode = colChildCode.getValue();
            String childName = colChildName.getValue();
            Gender childGender = Gender.getFrom(colChildGender.getValue());
            Integer ordinalPosition = colChildOrdPosition.getIntegerValue();
            HeadRelationshipType headRelationshipType = HeadRelationshipType.getFrom(colChildRelationship.getValue());

            if (outcomeType == null) {
                //Outcome Type cannot be null
                String message = "";
                return new ValidationResult(colOutcomeType, message);
            }


            //check code
            if (!codeGenerator.isMemberCodeValid(childCode)){
                String message = this.context.getString(R.string.pregnancy_outcome_member_code_err_lbl);
                return new ValidationResult(colChildCode, message);
            }

            if (!childCode.startsWith(household.code)){
                String message = this.context.getString(R.string.pregnancy_outcome_member_code_household_err_lbl);
                return new ValidationResult(colChildCode, message);
            }

            if (currentMode == Mode.CREATE && boxMembers.query().equal(Member_.code, childCode, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst() != null){
                String message = this.context.getString(R.string.pregnancy_outcome_member_code_exists_lbl);
                return new ValidationResult(colChildCode, message);
            }

            if (StringUtil.isBlank(childName)){
                String message = this.context.getString(R.string.pregnancy_outcome_member_name_empty_lbl);
                return new ValidationResult(colChildName, message);
            }

            if (childGender == null){
                String message = this.context.getString(R.string.pregnancy_outcome_member_gender_empty_lbl);
                return new ValidationResult(colChildGender, message);
            }

            if (childGender == Gender.UNKNOWN && outcomeType == PregnancyOutcomeType.LIVEBIRTH) {
                String message = this.context.getString(R.string.pregnancy_outcome_member_gender_invalid_lbr_lbl);
                return new ValidationResult(colChildGender, message);
            }

            if (headRelationshipType == null){
                String message = this.context.getString(R.string.pregnancy_outcome_member_head_relattype_empty_lbl);
                return new ValidationResult(colChildRelationship, message);
            }

        }

        return ValidationResult.noErrors();
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
        ColumnValue colCode = collectedValues.get("code");
        ColumnValue colMotherCode = collectedValues.get("motherCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colMotherName = collectedValues.get("motherName"); //not blank
        ColumnValue colFatherCode = collectedValues.get("fatherCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colFatherName = collectedValues.get("fatherName"); //not blank
        ColumnValue colNrOfOutcomes = collectedValues.get("numberOfOutcomes");
        ColumnValue colOutcomeDate = collectedValues.get("outcomeDate"); //not null cannot be in the future nor before dob
        ColumnValue colBirthPlace = collectedValues.get("birthPlace");
        ColumnValue colBirthPlaceOther = collectedValues.get("birthPlaceOther");

        RepeatColumnValue repChilds = collectedValues.getRepeatColumn("childs");
        List<ColumnValue> colOutcomeTypes = new ArrayList<>();
        List<ColumnValue> colChildCodes = new ArrayList<>();
        List<ColumnValue> colChildNames = new ArrayList<>();
        List<ColumnValue> colChildGenders = new ArrayList<>();
        List<ColumnValue> colChildOrdPositions = new ArrayList<>();
        List<ColumnValue> colChildRelationships = new ArrayList<>();

        if (repChilds != null) {
            for (int i = 0; i < repChilds.getCount(); i++) {
                ColumnValue colOutcomeType = repChilds.get("outcomeType", i);
                ColumnValue colChildCode = repChilds.get("childCode", i);
                ColumnValue colChildName = repChilds.get("childName", i);
                ColumnValue colChildGender = repChilds.get("childGender", i);
                ColumnValue colChildOrdPosition = repChilds.get("childOrdinalPosition", i);
                ColumnValue colChildRelationship = repChilds.get("headRelationshipType", i);

                colOutcomeTypes.add(colOutcomeType);
                colChildCodes.add(colChildCode);
                colChildNames.add(colChildName);
                colChildGenders.add(colChildGender);
                colChildOrdPositions.add(colChildOrdPosition);
                colChildRelationships.add(colChildRelationship);
            }
        }

        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String code = colCode.getValue();
        String motherCode = colMotherCode.getValue();
        String motherName = colMotherName.getValue();
        String fatherCode = colMotherCode.getValue();
        String fatherName = colMotherName.getValue();
        Integer nrOfOutcomes = colNrOfOutcomes.getIntegerValue();
        Date outcomeDate = colOutcomeDate.getDateValue();
        BirthPlace birthPlace = BirthPlace.getFrom(colBirthPlace.getValue());
        String birthPlaceOther = colBirthPlaceOther.getValue();

        List<String> childOutcomeTypes = colOutcomeTypes.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> childCodes = colChildCodes.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> childNames = colChildNames.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> childGenders = colChildGenders.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<Integer> childOrdPositions = colChildOrdPositions.stream().map(ColumnValue::getIntegerValue).collect(Collectors.toList());
        List<String> childRelationships = colChildRelationships.stream().map(ColumnValue::getValue).collect(Collectors.toList());

        String affectedMembers = null;

        //close pregnancy registration with delivered
        //create pregnancy outcome
        //create pregnancy outcome child
        //create child members
        //create child members resisdencies
        //create child members head relationships

        pregnancyRegistration.status = PregnancyStatus.DELIVERED;
        this.boxPregnancyRegistrations.put(pregnancyRegistration);

        PregnancyOutcome pregnancyOutcome = new PregnancyOutcome();
        pregnancyOutcome.code = pregnancyRegistration.code;
        pregnancyOutcome.visitCode = visit.code;
        pregnancyOutcome.visit.setTarget(visit);
        pregnancyOutcome.motherCode = mother.code;
        pregnancyOutcome.fatherCode = father.code;
        //pregnancyOutcome.mother.setTarget(mother);
        //pregnancyOutcome.father.setTarget(father);
        pregnancyOutcome.numberOfOutcomes = numberOfOutcomes;
        //pregnancyOutcome.numberOfLivebirths = 0;
        pregnancyOutcome.outcomeDate = outcomeDate;
        pregnancyOutcome.birthPlace = birthPlace;
        pregnancyOutcome.birthPlaceOther = birthPlaceOther;
        pregnancyOutcome.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        pregnancyOutcome.recentlyCreated = true;
        pregnancyOutcome.recentlyCreatedUri = result.getFilename();

        //livebirths and childs
        int livebirths = 0;
        for (int i = 0; i < nrOfOutcomes; i++) {
            PregnancyOutcomeType type = PregnancyOutcomeType.getFrom(childOutcomeTypes.get(i));
            if (type == PregnancyOutcomeType.LIVEBIRTH) {
                livebirths++;
            }

            String childCode = childCodes.get(i);
            String childName = childNames.get(i);
            Gender childGender = Gender.getFrom(childGenders.get(i));
            Integer childOrdPos = childOrdPositions.get(i);
            HeadRelationshipType headRelationshipType = HeadRelationshipType.getFrom(childRelationships.get(i));

            //Member
            Member childMember = createNewMember(pregnancyOutcome, childCode, childName, childGender, headRelationshipType, colModules.getValue());
            this.boxMembers.put(childMember);

            affectedMembers = addAffectedMembers(affectedMembers, childMember.code);
            Log.d("affctd", ""+affectedMembers);

            //Residency
            Residency childResidency = new Residency();
            childResidency.householdCode = household.code;
            childResidency.memberCode = childCode;
            childResidency.startType = ResidencyStartType.BIRTH;
            childResidency.startDate = outcomeDate;
            childResidency.endType = ResidencyEndType.NOT_APPLICABLE;
            childResidency.endDate = null;
            this.boxResidencies.put(childResidency);

            //HeadRelationship
            HeadRelationship childHeadRelationship = new HeadRelationship();
            childHeadRelationship.householdCode = household.code;
            childHeadRelationship.memberCode = childCode;
            childHeadRelationship.relationshipType = headRelationshipType;
            childHeadRelationship.startType = HeadRelationshipStartType.BIRTH;
            childHeadRelationship.startDate = outcomeDate;
            childHeadRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
            childHeadRelationship.endDate = null;
            this.boxHeadRelationships.put(childHeadRelationship);

            //Child
            PregnancyChild pregnancyChild = new PregnancyChild();
            pregnancyChild.outcomeCode = pregnancyOutcome.code;
            pregnancyChild.outcome.setTarget(pregnancyOutcome);
            pregnancyChild.outcomeType = type;
            pregnancyChild.childCode = childCode;
            //pregnancyChild.child.setTarget(childMember);
            pregnancyChild.childOrdinalPosition = childOrdPos;
            pregnancyChild.childHeadRelationshipType = headRelationshipType;
            pregnancyChild.childHeadRelationship.setTarget(childHeadRelationship);
            pregnancyChild.recentlyCreated = true;

            pregnancyOutcome.childs.add(pregnancyChild);

            if (type != PregnancyOutcomeType.LIVEBIRTH) {
                Death death = new Death();
                death.visitCode = visitCode;
                death.memberCode = childCode;
                death.deathDate = outcomeDate;
                death.deathCause = type.code;
                death.deathPlace = null;
                death.ageAtDeath = 0;
                this.boxDeaths.put(death);

                childMember.endType = ResidencyEndType.DEATH;
                childMember.endDate = outcomeDate;
                this.boxMembers.put(childMember);

                childResidency.endType = ResidencyEndType.DEATH;
                childResidency.endDate = outcomeDate;
                this.boxResidencies.put(childResidency);

                childHeadRelationship.endType = HeadRelationshipEndType.DEATH;
                childHeadRelationship.endDate = outcomeDate;
                this.boxHeadRelationships.put(childHeadRelationship);
            }
        }

        pregnancyOutcome.numberOfLivebirths = livebirths;

        this.boxPregnancyOutcomes.put(pregnancyOutcome);


        //save core collected data
        collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.PREGNANCY_OUTCOME;
        collectedData.formEntityId = pregnancyOutcome.id;
        collectedData.formEntityCode = mother.code;
        collectedData.formEntityCodes = affectedMembers;
        collectedData.formEntityName = mother.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        collectedData.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        collectedData.extension.setTarget(this.getFormExtension(collectedData.formEntity));
        this.boxCoreCollectedData.put(collectedData);

        this.entity = pregnancyOutcome;
        this.collectExtensionForm(collectedValues);
    }

    private void onModeEdit(CollectedDataMap collectedValues, XmlFormResult result) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colCode = collectedValues.get("code");
        ColumnValue colMotherCode = collectedValues.get("motherCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colMotherName = collectedValues.get("motherName"); //not blank
        ColumnValue colFatherCode = collectedValues.get("fatherCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colFatherName = collectedValues.get("fatherName"); //not blank
        ColumnValue colNrOfOutcomes = collectedValues.get("numberOfOutcomes");
        ColumnValue colOutcomeDate = collectedValues.get("outcomeDate"); //not null cannot be in the future nor before dob
        ColumnValue colBirthPlace = collectedValues.get("birthPlace");
        ColumnValue colBirthPlaceOther = collectedValues.get("birthPlaceOther");

        RepeatColumnValue repChilds = collectedValues.getRepeatColumn("childs");
        List<ColumnValue> colOutcomeTypes = new ArrayList<>();
        List<ColumnValue> colChildCodes = new ArrayList<>();
        List<ColumnValue> colChildNames = new ArrayList<>();
        List<ColumnValue> colChildGenders = new ArrayList<>();
        List<ColumnValue> colChildOrdPositions = new ArrayList<>();
        List<ColumnValue> colChildRelationships = new ArrayList<>();

        if (repChilds != null) {
            for (int i = 0; i < repChilds.getCount(); i++) {
                ColumnValue colOutcomeType = repChilds.get("outcomeType", i);
                ColumnValue colChildCode = repChilds.get("childCode", i);
                ColumnValue colChildName = repChilds.get("childName", i);
                ColumnValue colChildGender = repChilds.get("childGender", i);
                ColumnValue colChildOrdPosition = repChilds.get("childOrdinalPosition", i);
                ColumnValue colChildRelationship = repChilds.get("headRelationshipType", i);

                colOutcomeTypes.add(colOutcomeType);
                colChildCodes.add(colChildCode);
                colChildNames.add(colChildName);
                colChildGenders.add(colChildGender);
                colChildOrdPositions.add(colChildOrdPosition);
                colChildRelationships.add(colChildRelationship);
            }
        }

        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String code = colCode.getValue();
        String motherCode = colMotherCode.getValue();
        String motherName = colMotherName.getValue();
        String fatherCode = colMotherCode.getValue();
        String fatherName = colMotherName.getValue();
        Integer nrOfOutcomes = colNrOfOutcomes.getIntegerValue();
        Date outcomeDate = colOutcomeDate.getDateValue();
        BirthPlace birthPlace = BirthPlace.getFrom(colBirthPlace.getValue());
        String birthPlaceOther = colBirthPlaceOther.getValue();

        List<String> childOutcomeTypes = colOutcomeTypes.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> childCodes = colChildCodes.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> childNames = colChildNames.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<String> childGenders = colChildGenders.stream().map(ColumnValue::getValue).collect(Collectors.toList());
        List<Integer> childOrdPositions = colChildOrdPositions.stream().map(ColumnValue::getIntegerValue).collect(Collectors.toList());
        List<String> childRelationships = colChildRelationships.stream().map(ColumnValue::getValue).collect(Collectors.toList());

        String affectedMembers = null;

        //close pregnancy registration with delivered
        //create pregnancy outcome
        //create pregnancy outcome child
        //create child members
        //create child members resisdencies
        //create child members head relationships

        //update again, but not needed
        pregnancyRegistration.status = PregnancyStatus.DELIVERED;
        this.boxPregnancyRegistrations.put(pregnancyRegistration);

        PregnancyOutcome pregnancyOutcome = this.entity;
        pregnancyOutcome.motherCode = mother.code;
        pregnancyOutcome.fatherCode = father.code;
        pregnancyOutcome.outcomeDate = outcomeDate;
        pregnancyOutcome.birthPlace = birthPlace;
        pregnancyOutcome.birthPlaceOther = birthPlaceOther;

        //livebirths and childs
        int livebirths = 0;
        for (int i = 0; i < nrOfOutcomes; i++) {
            PregnancyOutcomeType type = PregnancyOutcomeType.getFrom(childOutcomeTypes.get(i));
            if (type == PregnancyOutcomeType.LIVEBIRTH){
                livebirths++;
            }
            String childCode = childCodes.get(i);
            String childName = childNames.get(i);
            Gender childGender = Gender.getFrom(childGenders.get(i));
            Integer childOrdPos = childOrdPositions.get(i);
            HeadRelationshipType headRelationshipType = HeadRelationshipType.getFrom(childRelationships.get(i));

            //Member
            Member childMember = this.boxMembers.query(Member_.code.equal(childCode)).build().findFirst();
            childMember.name = childName;
            childMember.gender = childGender;
            childMember.dob = pregnancyOutcome.outcomeDate;
            childMember.motherCode = mother.code;
            childMember.motherName = mother.name;
            childMember.fatherCode = father.code;
            childMember.fatherName = father.name;
            childMember.startType = ResidencyStartType.BIRTH;
            childMember.startDate = pregnancyOutcome.outcomeDate;
            childMember.endType = ResidencyEndType.NOT_APPLICABLE;
            childMember.endDate = null;
            childMember.entryHousehold = household.code;
            childMember.entryType = ResidencyStartType.BIRTH;
            childMember.entryDate = pregnancyOutcome.outcomeDate;
            childMember.headRelationshipType = headRelationshipType;
            this.boxMembers.put(childMember);

            affectedMembers = addAffectedMembers(affectedMembers, childMember.code);
            Log.d("affctd", ""+affectedMembers);

            //Residency - only one exists
            Residency childResidency =  this.boxResidencies.query(Residency_.memberCode.equal(childCode)).build().findFirst();
            childResidency.householdCode = household.code;
            childResidency.memberCode = childCode;
            childResidency.startType = ResidencyStartType.BIRTH;
            childResidency.startDate = outcomeDate;
            childResidency.endType = ResidencyEndType.NOT_APPLICABLE;
            childResidency.endDate = null;
            this.boxResidencies.put(childResidency);

            //HeadRelationship
            HeadRelationship childHeadRelationship = this.boxHeadRelationships.query(HeadRelationship_.memberCode.equal(childCode)).build().findFirst();
            childHeadRelationship.householdCode = household.code;
            childHeadRelationship.memberCode = childCode;
            childHeadRelationship.relationshipType = headRelationshipType;
            childHeadRelationship.startType = HeadRelationshipStartType.BIRTH;
            childHeadRelationship.startDate = outcomeDate;
            childHeadRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
            childHeadRelationship.endDate = null;
            this.boxHeadRelationships.put(childHeadRelationship);

            //Child
            PregnancyChild pregnancyChild = this.boxPregnancyChilds.query(PregnancyChild_.childCode.equal(childCode)).build().findFirst();
            boolean wasDead = pregnancyChild.outcomeType != PregnancyOutcomeType.LIVEBIRTH;

            pregnancyChild.outcomeType = type;
            pregnancyChild.childCode = childCode;
            pregnancyChild.childOrdinalPosition = childOrdPos;
            pregnancyChild.childHeadRelationshipType = headRelationshipType;
            pregnancyChild.childHeadRelationship.setTarget(childHeadRelationship);
            pregnancyOutcome.childs.add(pregnancyChild);

            //generate deaths
            if (type != PregnancyOutcomeType.LIVEBIRTH) {
                Death death = this.boxDeaths.query(Death_.memberCode.equal(childCode)).build().findFirst();

                if (death == null) {
                    death = new Death();
                }

                death.visitCode = visitCode;
                death.memberCode = childCode;
                death.deathDate = outcomeDate;
                death.deathCause = type.code;
                death.deathPlace = null;
                death.ageAtDeath = 0;
                this.boxDeaths.put(death);


            } else if (wasDead) {
                //delete the death
                Death death = this.boxDeaths.query(Death_.memberCode.equal(childCode)).build().findFirst();
                this.boxDeaths.remove(death);
            }
        }

        pregnancyOutcome.numberOfLivebirths = livebirths;

        this.boxPregnancyOutcomes.put(pregnancyOutcome);

        //save core collected data
        collectedData.visitId = visit.id;
        collectedData.formEntityCode = mother.code;
        collectedData.formEntityCodes = affectedMembers;
        collectedData.formEntityName = mother.name;
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

    private Member createNewMember(PregnancyOutcome pregnancyOutcome, String code, String name, Gender gender, HeadRelationshipType headRelationshipType, String modules) {
        Member member = new Member();
        member.code = code;
        member.name = name;
        member.gender = gender;
        member.dob = pregnancyOutcome.outcomeDate;
        member.age = 0;
        member.ageAtDeath = 0;
        member.motherCode = mother.code;
        member.motherName = mother.name;
        member.fatherCode = father.code;
        member.fatherName = father.name;
        member.maritalStatus = MaritalStatus.SINGLE; //always single
        //member.spouseCode = null;
        //member.spouseName = null;
        member.householdCode = household.code;
        member.householdName = household.name;
        member.startType = ResidencyStartType.BIRTH;
        member.startDate = pregnancyOutcome.outcomeDate;
        member.endType = ResidencyEndType.NOT_APPLICABLE;
        member.endDate = null;
        member.entryHousehold = household.code;
        member.entryType = ResidencyStartType.BIRTH;
        member.entryDate = pregnancyOutcome.outcomeDate;
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
        member.modules.addAll(StringCollectionConverter.getCollectionFrom(modules));

        return member;
    }

    @Override
    public void onFormCancelled(){
        if (listener != null) {
            listener.onFormCancelled();
        }

        deleteCreatedPregnancy();
    }

    @Override
    public String onFormCallMethod(String methodExpression, String[] args) {
        return null;
    }

    @Override
    public void collect() {
        Log.d("calling", "collect - pregout");
        //Limit by Age
        if (GeneralUtil.getAge(this.mother.dob, new Date()) < this.minimunMotherAge) {
            DialogFactory.createMessageInfo(this.context, R.string.core_entity_pregnancy_out_lbl, R.string.pregnancy_registration_mother_age_err_lbl).show();
            return;
        }

        if (currentMode == Mode.CREATE) {
            //1. get the father
            checkFatherDialog();
        } else if (currentMode == Mode.EDIT) {
            checkChangeFatherDialog();
        }

    }

    private void checkFatherDialog(){
        Log.d("started ", "pregnancy outcome - check father");
        DialogFactory.createMessageYN(this.context, R.string.pregnancy_outcome_child_father_select_lbl, R.string.pregnancy_outcome_child_father_exists_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                openFatherFilterDialog();
            }

            @Override
            public void onNoClicked() {
                //set unknown and jump to father dialog
                father = Member.getUnknownIndividual();
                openNumberOfOutcomesDialog();
            }
        }).show();

    }

    private void checkChangeFatherDialog(){

        DialogFactory.createMessageYN(this.context, R.string.pregnancy_outcome_child_father_change_title_lbl, R.string.pregnancy_outcome_child_father_change_lbl, new DialogFactory.OnYesNoClickListener() {
            @Override
            public void onYesClicked() {
                openFatherFilterDialog();
            }

            @Override
            public void onNoClicked() {
                //father remains unchanged
                executeCollectForm();
            }
        }).show();

    }

    private void openFatherFilterDialog(){

        MemberFilterDialog dialog = MemberFilterDialog.newInstance(this.fragmentManager, context.getString(R.string.pregnancy_outcome_child_father_select_lbl), true, new MemberFilterDialog.Listener() {
            @Override
            public void onSelectedMember(Member member) {
                Log.d("selected-father", ""+member.getCode());

                father = member;

                if (currentMode == Mode.CREATE) {
                    openNumberOfOutcomesDialog();
                } else if (currentMode == Mode.EDIT) {
                    executeCollectForm();
                }
            }

            @Override
            public void onCanceled() {
                deleteCreatedPregnancy();
            }
        });

        dialog.setGenderMaleOnly();
        dialog.setFilterMinAge(this.minimunFatherAge, true);
        dialog.setFilterHouseCode(household.getCode());
        dialog.setStartSearchOnShow(true);
        dialog.show();
    }

    private void openNumberOfOutcomesDialog(){

        DialogFactory.createNumberInput(this.context, R.string.pregnancy_outcome_nroutcomes_title_lbl, R.string.pregnancy_outcome_nroutcomes_lbl, new DialogFactory.OnInputTextListener() {
            @Override
            public void onNumberTyped(Integer value) {
                numberOfOutcomes = value;
                executeCollectForm();
            }

            @Override
            public void onTextTyped(String value) {

            }

            @Override
            public void onCancel() {
                deleteCreatedPregnancy();
            }
        }).show();
    }

    private void deleteCreatedPregnancy() {
        if (pregnancyRegistrationCreated && pregnancyRegistration != null) {
            this.boxPregnancyRegistrations.remove(pregnancyRegistration);
        }
    }

}
