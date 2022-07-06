package org.philimone.hds.explorer.fragment.showcollected.adapter.model;

import android.util.Log;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;

import java.util.List;

import mz.betainteractive.utilities.StringUtil;

public class CoreCollectedDataItem {
    public CoreCollectedData collectedData;
    public Household household;
    public List<CoreFormEntity> collectedForms;

    public CoreCollectedDataItem(CoreCollectedData collectedData, Household household, List<CoreFormEntity> collectedForms) {
        this.collectedData = collectedData;
        this.household = household;
        this.collectedForms = collectedForms;
    }

    public boolean contentMatches(String text, String formEntityNameText) {
        CoreCollectedData cd = collectedData;
        String createdDate = cd.createdDate==null ? "" : StringUtil.format(cd.createdDate, "yyyy-MM-dd HH:mm:ss");
        String updatedDate = cd.updatedDate==null ? createdDate : StringUtil.format(cd.updatedDate, "yyyy-MM-dd HH:mm:ss");

        String txtItem1 = household.name;
        String txtItem2 = household.code + " - " + formEntityNameText + " (" + cd.formEntityCode + ")";
        String txtItem3 = updatedDate;

        Log.d("matching", txtItem1+" ?== "+text);

        return txtItem1.toLowerCase().matches(text) || txtItem2.toLowerCase().matches(text) || txtItem3.toLowerCase().matches(text);
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
