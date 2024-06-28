package org.philimone.hds.explorer.fragment.showcollected.adapter.model;

import android.content.Context;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CoreFormExtension;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.FormSubject;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;

import java.io.Serializable;

import mz.betainteractive.utilities.StringUtil;

/**
 * Created by paul on 8/10/16.
 */
public class OdkCollectedDataItem implements Serializable {

    private int id;
    private String formId;
    private Household household;
    private Member member;
    private Form form;
    private CoreFormExtension formExtension;
    private Region region;
    private CollectedData collectedData;
    public boolean selected = false;

    public OdkCollectedDataItem(String formId, FormSubject subject, Form form, CoreFormExtension coreFormExtension, CollectedData collectedData) {
        this.formId = formId;
        this.form = form;
        this.formExtension = coreFormExtension;
        this.collectedData = collectedData;

        if (subject instanceof Region){
            this.region = (Region) subject;
        }
        if (subject instanceof Household){
            this.household = (Household) subject;
        }
        if (subject instanceof Member){
            this.member = (Member) subject;
        }


    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Household getHousehold() {
        return household;
    }

    public void setHousehold(Household household) {
        this.household = household;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Region getRegion() {
        return region;
    }

    public Form getForm() {
        return form;
    }

    public String getFormName(Context context) {
        String extension = " " + context.getString(R.string.core_entity_extension_lbl);
        return form != null ? form.getFormName() : formExtension != null ? context.getString(formExtension.formEntity.name) + extension : formId;
    }

    public boolean isFormExtension() {
        return formExtension != null;
    }

    public boolean isFormNull(){
        return form==null;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public CollectedData getCollectedData() {
        return collectedData;
    }

    public void setCollectedData(CollectedData collectedData) {
        this.collectedData = collectedData;
    }

    public boolean isHouseholdItem(){
        return household!=null;
    }

    public boolean isMemberItem(){
        return member != null;
    }

    public boolean isRegionItem(){
        return region != null;
    }

    public boolean contentMatches(String text) {
        CollectedData cd = collectedData;
        String instanceName = cd.getFormInstanceName();
        String formName = (isFormNull() ? cd.getFormId() : getForm().getFormName());
        String subjectText = getFormText(this);
        String updatedDate = StringUtil.format(cd.formLastUpdatedDate, "yyyy-MM-dd HH:mm:ss");

        return instanceName.toLowerCase().matches(text) || formName.toLowerCase().matches(text) || subjectText.toLowerCase().matches(text) || updatedDate.matches(text);
    }

    private String getFormText(OdkCollectedDataItem cdi) {

        if (cdi.isRegionItem()) {
            return cdi.getRegion().code + " -> " + cdi.getRegion().name;
        }
        if (cdi.isHouseholdItem()) {
            return cdi.getHousehold().code + " -> " + cdi.getHousehold().name;
        }
        if (cdi.isMemberItem()) {
            return cdi.getMember().code + " -> " + cdi.getMember().name;
        }

        return "";
    }
}
