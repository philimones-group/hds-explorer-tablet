package org.philimone.hds.explorer.fragment.showcollected.adapter.model;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.model.CollectedData;
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
    private Household household;
    private Member member;
    private Form form;
    private Region region;
    private CollectedData collectedData;

    public OdkCollectedDataItem(int id, Member member, Form form, CollectedData collectedData) {
        this.id = id;
        this.member = member;
        this.form = form;
        this.collectedData = collectedData;
    }

    public OdkCollectedDataItem(FormSubject subject, Form form, CollectedData collectedData) {
        this.form = form;
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

    public OdkCollectedDataItem(Member member, Form form, CollectedData collectedData) {
        this.member = member;
        this.form = form;
        this.collectedData = collectedData;
    }

    public OdkCollectedDataItem(int id, Household household, Form form, CollectedData collectedData) {
        this.id = id;
        this.household = household;
        this.form = form;
        this.collectedData = collectedData;
    }

    public OdkCollectedDataItem(Household household, Form form, CollectedData collectedData) {
        this.household = household;
        this.form = form;
        this.collectedData = collectedData;
    }

    public OdkCollectedDataItem(int id, Region region, Form form, CollectedData collectedData) {
        this.id = id;
        this.region = region;
        this.form = form;
        this.collectedData = collectedData;
    }

    public OdkCollectedDataItem(Region region, Form form, CollectedData collectedData) {
        this.region = region;
        this.form = form;
        this.collectedData = collectedData;
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
