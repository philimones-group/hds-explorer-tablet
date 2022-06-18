package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Death;
import org.philimone.hds.explorer.model.IncompleteVisit;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.IncompleteVisitReason;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import java.util.Date;
import java.util.HashMap;

import io.objectbox.Box;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.utilities.StringUtil;

public class IncompleteVisitFormUtil extends FormUtil<IncompleteVisit> {

    private Box<Member> boxMembers;
    private Box<Death> boxDeaths;
    private Box<IncompleteVisit> boxIncompleteVisits;
    
    private Visit visit;
    private Member member;
    private IncompleteVisit currentIncompleteVisit;

    public IncompleteVisitFormUtil(Fragment fragment, Context context, Visit visit, Member member, FormUtilities odkFormUtilities, FormUtilListener<IncompleteVisit> listener){
        super(fragment, context, FormUtil.getIncompleteVisitForm(context), odkFormUtilities, listener);

        this.visit = visit;
        this.member = member;

        initBoxes();
        initialize();
    }

    public IncompleteVisitFormUtil(Fragment fragment, Context context, Visit visit, IncompleteVisit incompleteVisitToEdit, FormUtilities odkFormUtilities, FormUtilListener<IncompleteVisit> listener){
        super(fragment, context, FormUtil.getIncompleteVisitForm(context), incompleteVisitToEdit, odkFormUtilities, listener);

        this.visit = visit;

        initBoxes();
        initialize();

        this.currentIncompleteVisit = incompleteVisitToEdit;
        this.member = incompleteVisitToEdit.member.getTarget();
    }

    public static IncompleteVisitFormUtil newInstance(Mode openMode, Fragment fragment, Context context, Visit visit, Member member, IncompleteVisit incompleteVisitToEdit, FormUtilities odkFormUtilities, FormUtilListener<IncompleteVisit> listener) {
        if (openMode == Mode.CREATE) {
            return new IncompleteVisitFormUtil(fragment, context, visit, member, odkFormUtilities, listener);
        } else if (openMode == Mode.EDIT) {
            return new IncompleteVisitFormUtil(fragment, context, visit, incompleteVisitToEdit, odkFormUtilities, listener);
        }

        return null;
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxDeaths = ObjectBoxDatabase.get().boxFor(Death.class);
        this.boxIncompleteVisits = ObjectBoxDatabase.get().boxFor(IncompleteVisit.class);

    }

    @Override
    protected void initialize(){
        super.initialize();
    }

    @Override
    protected void preloadValues() {
        preloadedMap.put("visitCode", this.visit.code);
        preloadedMap.put("householdCode", this.visit.householdCode);
        preloadedMap.put("memberCode", this.member.code);
        preloadedMap.put("memberName", this.member.name);
        //preloadedMap.put("reason", "");
        //preloadedMap.put("reasonOther", "");
    }

    @Override
    protected void preloadUpdatedValues() {
        preloadedMap.put("visitCode", this.visit.code);
        preloadedMap.put("householdCode", this.visit.householdCode);
        preloadedMap.put("memberCode", this.member.code);
        preloadedMap.put("memberName", this.member.name);
        preloadedMap.put("reason", this.currentIncompleteVisit!=null ? this.currentIncompleteVisit.reason.code : "");
        preloadedMap.put("reasonOther", this.currentIncompleteVisit!=null ? this.currentIncompleteVisit.reasonOther : "");
    }

    @Override
    public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {

        ColumnValue colVisitCode = collectedValues.get("visitCode");
        ColumnValue colHouseholdCode = collectedValues.get("householdCode");
        ColumnValue colMemberCode = collectedValues.get("memberCode");
        ColumnValue colMemberName = collectedValues.get("memberName");
        ColumnValue colReason = collectedValues.get("reason");
        ColumnValue colReasonOther = collectedValues.get("reasonOther");
        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");


        String visitCode = colVisitCode.getValue();
        String householdCode = colHouseholdCode.getValue();
        String memberCode = colMemberCode.getValue();
        String memberName = colMemberName.getValue();
        IncompleteVisitReason reason = IncompleteVisitReason.getFrom(colReason.getValue());
        String reasonOther = colReasonOther.getValue();

        //C1. Check Field not Blank
        if (StringUtil.isBlank(visitCode)){
            String message = this.context.getString(R.string.incomplete_visit_visit_code_empty);
            return new ValidationResult(colVisitCode, message);
        }

        if (StringUtil.isBlank(memberCode)){
            String message = this.context.getString(R.string.incomplete_visit_member_code_empty);
            return new ValidationResult(colMemberCode, message);
        }

        if (reason == null){
            String message = this.context.getString(R.string.incomplete_visit_reason_empty);
            return new ValidationResult(colReason, message);
        }

        if (reasonOther == null && reason == IncompleteVisitReason.OTHER){
            String message = this.context.getString(R.string.incomplete_visit_reason_other_empty);
            return new ValidationResult(colReasonOther, message);
        }

        //C4. Check Code reference existence
        if (Queries.getMemberByCode(boxMembers, memberCode) == null){
            String message = this.context.getString(R.string.incomplete_visit_member_not_found_lbl);
            return new ValidationResult(colMemberCode, message);
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
        ColumnValue colHouseholdCode = collectedValues.get("householdCode");
        ColumnValue colMemberCode = collectedValues.get("memberCode");
        ColumnValue colMemberName = collectedValues.get("memberName");
        ColumnValue colReason = collectedValues.get("reason");
        ColumnValue colReasonOther = collectedValues.get("reasonOther");
        ColumnValue colCollectedBy = collectedValues.get("collectedBy");
        ColumnValue colCollectedDate = collectedValues.get("collectedDate");


        String visitCode = colVisitCode.getValue();
        String householdCode = colHouseholdCode.getValue();
        String memberCode = colMemberCode.getValue();
        String memberName = colMemberName.getValue();
        IncompleteVisitReason reason = IncompleteVisitReason.getFrom(colReason.getValue());
        String reasonOther = colReasonOther.getValue();


        IncompleteVisit incompleteVisit = new IncompleteVisit();

        incompleteVisit.visit.setTarget(this.visit);
        incompleteVisit.member.setTarget(this.member);
        incompleteVisit.reason = reason;
        incompleteVisit.reasonOther = reason==IncompleteVisitReason.OTHER ? reasonOther : null;
        incompleteVisit.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        incompleteVisit.recentlyCreated = true;
        incompleteVisit.recentlyCreatedUri = result.getFilename();

        //save data
        boxIncompleteVisits.put(incompleteVisit);

        //save core collected data
        collectedData = new CoreCollectedData();
        collectedData.visitId = visit.id;
        collectedData.formEntity = CoreFormEntity.INCOMPLETE_VISIT;
        collectedData.formEntityId = member.id;
        collectedData.formEntityCode = member.code;
        collectedData.formEntityName = member.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        collectedData.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        collectedData.extension.setTarget(this.getFormExtension(collectedData.formEntity));

        boxCoreCollectedData.put(collectedData);

        this.entity = incompleteVisit;
        this.collectExtensionForm(collectedValues);
    }

    @Override
    protected void onFinishedExtensionCollection() {
        if (listener != null) {
            listener.onNewEntityCreated(this.entity, new HashMap<>());
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
        executeCollectForm();
    }

}
