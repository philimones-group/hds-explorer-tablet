package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.FragmentManager;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.HeadRelationship_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Outmigration;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Residency;
import org.philimone.hds.explorer.model.Residency_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipEndType;
import org.philimone.hds.explorer.model.enums.temporal.OutMigrationType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import java.util.Date;
import java.util.Map;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.StringUtil;

public class OutmigrationFormUtil extends FormUtil<Outmigration> {

    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<Residency> boxResidencies;
    private Box<HeadRelationship> boxHeadRelationships;
    private Box<Outmigration> boxOutmigrations;
    private Box<CoreCollectedData> boxCoreCollectedData;

    private Household household;
    private Visit visit;
    private Member member;
    private Residency memberResidency;
    private HeadRelationship memberHeadRelationship;

    public OutmigrationFormUtil(FragmentManager fragmentManager, Context context, Visit visit, Household household, Member member, FormUtilListener<Outmigration> listener){
        super(fragmentManager, context, FormUtil.getOutmigrationForm(context), listener);

        //Log.d("enu-household", ""+household);

        this.household = household;
        this.member = member;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    public OutmigrationFormUtil(FragmentManager fragmentManager, Context context, Visit visit, Household household, Outmigration outmigToEdit, FormUtilListener<Outmigration> listener){
        super(fragmentManager, context, FormUtil.getOutmigrationForm(context), outmigToEdit, listener);

        this.household = household;
        this.visit = visit;

        initBoxes();
        initialize();
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxResidencies = ObjectBoxDatabase.get().boxFor(Residency.class);
        this.boxHeadRelationships = ObjectBoxDatabase.get().boxFor(HeadRelationship.class);
        this.boxOutmigrations = ObjectBoxDatabase.get().boxFor(Outmigration.class);
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
    }

    @Override
    protected void initialize(){
        super.initialize();

        Log.d("init-household", ""+household);
    }

    @Override
    protected void preloadValues() {
        //member_details_unknown_lbl

        preloadedMap.put("visitCode", this.visit.code);
        preloadedMap.put("memberCode", this.member.code);
        preloadedMap.put("memberName", this.member.name);
        preloadedMap.put("migrationType", OutMigrationType.EXTERNAL.code);
        preloadedMap.put("originCode", this.household.code);
        preloadedMap.put("modules", this.user.getSelectedModulesCodes());
    }

    @Override
    protected void preloadUpdatedValues() {

    }

    @Override
    public ValidationResult onFormValidate(HForm form, Map<String, ColumnValue> collectedValues) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colMemberCode = collectedValues.get("memberCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colMemberName = collectedValues.get("memberName"); //not blank
        ColumnValue colMigrationType = collectedValues.get("migrationType");
        ColumnValue colOriginCode = collectedValues.get("originCode");
        //ColumnValue colDestinationCode = collectedValues.get("destinationCode");
        ColumnValue colDestinationOther = collectedValues.get("destinationOther");
        ColumnValue colMigrationDate = collectedValues.get("migrationDate"); //not null cannot be in the future nor before dob
        ColumnValue colMigrationReason = collectedValues.get("migrationReason");
        ColumnValue colMigrationReasonOther = collectedValues.get("migrationReasonOther");

        //ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        //ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        //ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String memberCode = colMemberCode.getValue();
        String memberName = colMemberName.getValue();
        OutMigrationType migrationType = OutMigrationType.getFrom(colMigrationType.getValue());
        String originCode = colOriginCode.getValue();
        String destinationOther = colDestinationOther.getValue();
        Date migrationDate = colMigrationDate.getDateValue();
        String migrationReason = colMigrationReason.getValue();
        String migrationReasonOther = colMigrationReasonOther.getValue();

        //validations
        //memberCode blank/valid
        if (!codeGenerator.isMemberCodeValid(memberCode)){
            String message = this.context.getString(R.string.new_member_code_err_lbl);
            return new ValidationResult(colMemberCode, message);
        }

        if (StringUtil.isBlank(memberName)){
            String message = this.context.getString(R.string.new_member_name_empty_lbl);
            return new ValidationResult(colMemberName, message);
        }

        //validate migrationType
        if (migrationType == null){
            String message = this.context.getString(R.string.external_inmigration_migration_type_empty_lbl);
            return new ValidationResult(colMigrationType, message);
        }

        if (migrationDate == null){
            String message = this.context.getString(R.string.external_inmigration_migrationdate_empty_lbl);
            return new ValidationResult(colMigrationDate, message);
        }

        //migration date cannot be in the future
        if (migrationDate.after(new Date())){ //is in the future
            String message = this.context.getString(R.string.external_inmigration_migrationdate_not_great_today_lbl);
            return new ValidationResult(colMigrationDate, message);
        }

        //migration date cannot be before dob
        if (migrationDate.before(this.member.dob)){ //is before dob
            String message = this.context.getString(R.string.external_inmigration_migrationdate_not_before_dob_lbl);
            return new ValidationResult(colMigrationDate, message);
        }

        return ValidationResult.noErrors();
    }

    @Override
    public void onFormFinished(HForm form, Map<String, ColumnValue> collectedValues, XmlFormResult result) {

        Log.d("resultxml", result.getXmlResult());

        if (currentMode == Mode.EDIT) {
            System.out.println("Editing Outmigration Not implemented yet");
            assert 1==0;
        }


        //saveNewHousehold();
        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colMemberCode = collectedValues.get("memberCode"); //check if code is valid + check duplicate + member belongs to household
        ColumnValue colMemberName = collectedValues.get("memberName"); //not blank
        ColumnValue colMigrationType = collectedValues.get("migrationType");
        ColumnValue colOriginCode = collectedValues.get("originCode");
        //ColumnValue colDestinationCode = collectedValues.get("destinationCode");
        ColumnValue colDestinationOther = collectedValues.get("destinationOther");
        ColumnValue colMigrationDate = collectedValues.get("migrationDate"); //not null cannot be in the future nor before dob
        ColumnValue colMigrationReason = collectedValues.get("migrationReason");
        ColumnValue colMigrationReasonOther = collectedValues.get("migrationReasonOther");

        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");
        ColumnValue colModules = collectedValues.get("modules");

        String visitCode = colVisitCode.getValue();
        String memberCode = colMemberCode.getValue();
        String memberName = colMemberName.getValue();
        OutMigrationType migrationType = OutMigrationType.getFrom(colMigrationType.getValue());
        String originCode = colOriginCode.getValue();
        String destinationOther = colDestinationOther.getValue();
        Date migrationDate = colMigrationDate.getDateValue();
        String migrationReason = colMigrationReason.getValue();
        String migrationReasonOther = colMigrationReasonOther.getValue();

        //update member, close residency, close headrelationship, create Outmigration


        //Outmigration
        Outmigration outmigration = new Outmigration();
        outmigration.visitCode = visitCode;
        outmigration.migrationType = OutMigrationType.EXTERNAL;
        outmigration.originCode = this.household.code;
        //outmigration.destinationCode = household.code;
        outmigration.destinationOther = destinationOther;
        outmigration.migrationDate = migrationDate;
        outmigration.migrationReason = migrationReasonOther != null ?  migrationReasonOther : migrationReason;
        outmigration.recentlyCreated = true;
        outmigration.recentlyCreatedUri = result.getFilename();
        this.boxOutmigrations.put(outmigration);

        //Residency - close
        memberResidency.endDate = migrationDate;
        memberResidency.endType = ResidencyEndType.EXTERNAL_OUTMIGRATION;
        this.boxResidencies.put(memberResidency);

        //HeadRelationship - close
        memberHeadRelationship.endDate = migrationDate;
        memberHeadRelationship.endType = HeadRelationshipEndType.EXTERNAL_OUTMIGRATION;
        this.boxHeadRelationships.put(memberHeadRelationship);

        //update member
        member.endType = ResidencyEndType.EXTERNAL_OUTMIGRATION;
        member.endDate = migrationDate;
        this.boxMembers.put(member);

        //save core collected data
        CoreCollectedData collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.OUTMIGRATION;
        collectedData.formEntityId = member.id;
        collectedData.formEntityCode = member.code;
        collectedData.formEntityName = member.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        this.boxCoreCollectedData.put(collectedData);

        if (listener != null) {
            listener.onNewEntityCreated(outmigration);
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

        memberResidency = boxResidencies.query().equal(Residency_.memberCode, this.member.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .and().equal(Residency_.endType, ResidencyEndType.NOT_APPLICABLE.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .orderDesc(Residency_.startDate).build().findFirst();

        memberHeadRelationship = boxHeadRelationships.query().equal(HeadRelationship_.memberCode, this.member.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .and().equal(HeadRelationship_.endType, HeadRelationshipEndType.NOT_APPLICABLE.code, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .orderDesc(HeadRelationship_.startDate).build().findFirst();

        if (memberResidency == null){
            DialogFactory.createMessageInfo(this.context, R.string.error_lbl, R.string.internal_inmigration_select_inmig_no_residency_lbl).show();
            return;
        }

        executeCollectForm();
    }


}
