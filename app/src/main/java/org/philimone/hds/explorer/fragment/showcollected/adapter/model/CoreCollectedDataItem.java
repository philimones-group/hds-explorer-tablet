package org.philimone.hds.explorer.fragment.showcollected.adapter.model;

import android.util.Log;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.FormSubject;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;

import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.utilities.StringUtil;

public class CoreCollectedDataItem {
    public CoreCollectedData collectedData;
    public Region region;
    public Household household;
    public Member member;
    public boolean selected;
    public List<CoreFormEntity> collectedForms = new ArrayList<>();

    public CoreCollectedDataItem(CoreCollectedData collectedData, Household household, List<CoreFormEntity> collectedForms) {
        this.collectedData = collectedData;
        this.household = household;
        this.collectedForms = collectedForms;
    }

    public CoreCollectedDataItem(CoreCollectedData collectedData, FormSubject subject) {
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

    public boolean contentMatches(String text, String formEntityNameText) {
        CoreCollectedData cd = collectedData;
        String createdDate = cd.createdDate==null ? "" : StringUtil.format(cd.createdDate, "yyyy-MM-dd HH:mm:ss");
        String updatedDate = cd.updatedDate==null ? createdDate : StringUtil.format(cd.updatedDate, "yyyy-MM-dd HH:mm:ss");

        String txtItem1 = "";
        String txtItem2 = "";
        String txtItem3 = updatedDate;

        txtItem1 = getFormNameText() + " - " + this.collectedData.formEntityName;
        txtItem2 = formEntityNameText + " (" + cd.formEntityCode + ")";


        Log.d("matching", txtItem1+" ?== "+text);

        return txtItem1.toLowerCase().matches(text) || txtItem2.toLowerCase().matches(text) || txtItem3.toLowerCase().matches(text);
    }

    private String getFormNameText() {

        if (this.region != null) {
            return this.region.code + " -> " + this.region.name;
        }
        if (this.household != null) {
            return this.household.code + " -> " + this.household.name;
        }
        if (this.member != null) {
            return this.member.code + " -> " + this.member.name;
        }

        return "";
    }

    public CoreCollectedData getCollectedData() {
        return collectedData;
    }
}
