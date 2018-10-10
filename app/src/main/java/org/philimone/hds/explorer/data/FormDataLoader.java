package org.philimone.hds.explorer.data;

import android.util.Log;

import org.philimone.hds.explorer.adapter.model.TrackingMemberItem;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.followup.TrackingList;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import mz.betainteractive.utilities.StringUtil;

/**
 * Created by paul on 8/9/16.
 */
public class FormDataLoader implements Serializable {
    private final String householdPrefix = "Household.";
    private final String memberPrefix = "Member.";
    private final String userPrefix = "User.";
    private final String constPrefix = "#.";
    private final String specialConstPrefix = "$.";

    private final String boolFormatPrefix = "Boolean[";
    private final String dateFormatPrefix = "Date[";
    private final String choiceFormatPrefix = "Choices[";

    private Form form;
    private Map<String, Object> values;

    public FormDataLoader(){
        this.values = new HashMap<>();
    }

    public FormDataLoader(Form form){
        this();
        this.form = form;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values.putAll(values);
    }

    public void putExtra(String key, String value){
        this.values.put(key, value);
    }

    public void loadHouseholdValues(Household household){
        Map<String, String> map = form.getBindMap();
        for (String key : map.keySet()){
            if (key.startsWith(householdPrefix)) {
                String internalVariableName = key.replace(householdPrefix, "");
                String odkVariable = map.get(key);
                String value = null;
                ExtrasVariable extrasVariable = tryParseExtrasVariable(internalVariableName);

                if (extrasVariable == null){
                    value = household.getValueByName(internalVariableName);
                }else {
                    internalVariableName = extrasVariable.columnName;
                    String commaValue = household.getValueByName(internalVariableName);
                    String[] values = commaValue.split(",");
                    value = (extrasVariable.arrayIndex >= values.length) ? null : values[extrasVariable.arrayIndex];
                }

                if (value == null) value = "";

                //get variable format from odkVariable eg. variableName->format => patientName->yes,no
                Log.d("odkvar", ""+odkVariable);
                String[] splt = odkVariable.split("->");
                odkVariable = splt[0]; //variableName
                String format = splt[1];      //format of the value
                if (!format.equalsIgnoreCase("None")){
                    if (format.startsWith(boolFormatPrefix)){
                        value = getBooleanFormattedValue(format, value);
                    }
                    if (format.startsWith(choiceFormatPrefix)){
                        value = getChoicesFormattedValue(format, value);
                    }
                    if (format.startsWith(dateFormatPrefix)){
                        value = getDateFormattedValue(format, value);
                    }
                }

                this.values.put(odkVariable, value);
                Log.d("h-odk auto-loadable", odkVariable + ", " + value);
            }
        }
    }

    public void loadMemberValues(Member member){
        Map<String, String> map = form.getBindMap();
        for (String key : map.keySet()){
            if (key.startsWith(memberPrefix)) {
                String internalVariableName = key.replace(memberPrefix, "");
                String odkVariable = map.get(key);
                String value = null;
                ExtrasVariable extrasVariable = tryParseExtrasVariable(internalVariableName);

                if (extrasVariable == null){
                    value = member.getValueByName(internalVariableName);
                }else {
                    internalVariableName = extrasVariable.columnName;
                    String commaValue = member.getValueByName(internalVariableName);
                    String[] values = commaValue.split(",");
                    value = (extrasVariable.arrayIndex >= values.length) ? null : values[extrasVariable.arrayIndex];
                }

                if (value == null) value = "";

                //get variable format from odkVariable eg. variableName->format => patientName->yes,no
                String[] splt = odkVariable.split("->");
                odkVariable = splt[0]; //variableName
                String format = splt[1];      //format of the value
                if (!format.equalsIgnoreCase("None")){
                    if (format.startsWith(boolFormatPrefix)){
                        value = getBooleanFormattedValue(format, value);
                    }
                    if (format.startsWith(choiceFormatPrefix)){
                        value = getChoicesFormattedValue(format, value);
                    }
                    if (format.startsWith(dateFormatPrefix)){
                        value = getDateFormattedValue(format, value);
                    }
                }

                this.values.put(odkVariable, value);
                Log.d("m-odk auto-loadable", odkVariable + ", " + value);
            }
        }
    }

    public void loadUserValues(User user){
        Map<String, String> map = form.getBindMap();
        for (String key : map.keySet()){
            if (key.startsWith(userPrefix)) {
                String internalVariableName = key.replace(userPrefix, "");
                String odkVariable = map.get(key);
                String value = null;
                ExtrasVariable extrasVariable = tryParseExtrasVariable(internalVariableName);

                if (extrasVariable == null){
                    value = user.getValueByName(internalVariableName);
                }else {
                    internalVariableName = extrasVariable.columnName;
                    String commaValue = user.getValueByName(internalVariableName);
                    String[] values = commaValue.split(",");
                    value = (extrasVariable.arrayIndex >= values.length) ? null : values[extrasVariable.arrayIndex];
                }

                if (value == null) value = "";

                //get variable format from odkVariable eg. variableName->format => patientName->yes,no
                String[] splt = odkVariable.split("->");
                odkVariable = splt[0]; //variableName
                String format = splt[1];      //format of the value
                if (!format.equalsIgnoreCase("None")){
                    if (format.startsWith(boolFormatPrefix)){
                        value = getBooleanFormattedValue(format, value);
                    }
                    if (format.startsWith(choiceFormatPrefix)){
                        value = getChoicesFormattedValue(format, value);
                    }
                    if (format.startsWith(dateFormatPrefix)){
                        value = getDateFormattedValue(format, value);
                    }
                }

                this.values.put(odkVariable, value);
                Log.d("u-odk auto-loadable", odkVariable + ", " + value);
            }
        }
    }

    /*When were loading constant values: the key of map will be "#.value" where "value" is the exact value to be used = internalVariableName*/
    public void loadConstantValues(){
        Map<String, String> map = form.getBindMap();
        for (String key : map.keySet()){
            if (key.startsWith(constPrefix)) {
                String internalVariableName = key.replace(constPrefix, "");
                String odkVariable = map.get(key);
                String value = null;
                ExtrasVariable extrasVariable = tryParseExtrasVariable(internalVariableName);

                if (extrasVariable == null){
                    value = internalVariableName; //user.getValueByName(internalVariableName);
                }else {
                    internalVariableName = extrasVariable.columnName;
                    String commaValue = internalVariableName; //user.getValueByName(internalVariableName);
                    value = commaValue.split(",")[extrasVariable.arrayIndex];
                }

                if (value == null) value = "";

                //get variable format from odkVariable eg. variableName->format => patientName->yes,no
                String[] splt = odkVariable.split("->");
                odkVariable = splt[0]; //variableName
                String format = splt[1];      //format of the value
                if (!format.equalsIgnoreCase("None")){
                    if (format.startsWith(boolFormatPrefix)){
                        value = getBooleanFormattedValue(format, value);
                    }
                    if (format.startsWith(choiceFormatPrefix)){
                        value = getChoicesFormattedValue(format, value);
                    }
                    if (format.startsWith(dateFormatPrefix)){
                        value = getDateFormattedValue(format, value);
                    }
                }

                this.values.put(odkVariable, value);
                Log.d("u-odk auto-loadable", odkVariable + ", " + value);
            }
        }
    }

    /* loading special constant values */
    public void loadSpecialConstantValues(Household household, Member member, User user, TrackingMemberItem memberItem){
        Map<String, String> map = form.getBindMap();
        for (String key : map.keySet()){
            //Log.d("special constant", ""+key );
            if (key.startsWith(specialConstPrefix)) {
                final String internalVariableName = key.replace(specialConstPrefix, "");
                String odkVariable = map.get(key);
                String value = null;
                ExtrasVariable extrasVariable = tryParseExtrasVariable(internalVariableName);

                if (extrasVariable == null){
                    value = internalVariableName; //user.getValueByName(internalVariableName);
                }

                if (value == null) value = "";

                //check on constants
                if (internalVariableName.equals("MemberExists")){
                    value = (member!=null && member.getId()>0) ? "true" : "false";
                }
                //check for studyCode that is used on Tracking/Follow-up Lists of studies modules
                if (internalVariableName.equals("studyCode") && memberItem!=null){
                    value = memberItem.getStudyCode();
                }
                //check for the of the tracking list
                if (internalVariableName.equals("trackListCode") && memberItem!=null){
                    if (memberItem.getListItem()!=null){
                        if (memberItem.getListItem().getTrackingList()!=null){
                            TrackingList trackingList = memberItem.getListItem().getTrackingList();
                            value = trackingList.getCode();
                        }
                    }
                }
                //check for trackingListMember visitNumber/visit
                if (internalVariableName.equals("visitNumber") && memberItem!=null){
                    value = memberItem.getVisitNumber()+"";
                }

                //get variable format from odkVariable eg. variableName->format => patientName->yes,no
                String[] splt = odkVariable.split("->");
                odkVariable = splt[0]; //variableName
                String format = splt[1];      //format of the value
                if (!format.equalsIgnoreCase("None")){
                    if (format.startsWith(boolFormatPrefix)){
                        value = getBooleanFormattedValue(format, value);
                    }
                    if (format.startsWith(choiceFormatPrefix)){
                        value = getChoicesFormattedValue(format, value);
                    }
                    if (format.startsWith(dateFormatPrefix)){
                        value = getDateFormattedValue(format, value);
                    }
                }

                this.values.put(odkVariable, value);
                Log.d("u-odk spc auto-loadable", odkVariable + ", " + value);
            }
        }
    }

    private String getBooleanFormattedValue(String format, String value) {
        format = format.replace(boolFormatPrefix, "");
        format = format.replace("]","");
        String[] split = format.split(",");

        if (value.equalsIgnoreCase("true")) return split[0];
        if (value.equalsIgnoreCase("false")) return split[1];

        return value;
    }

    private String getChoicesFormattedValue(String format, String value) {
        format = format.replace(choiceFormatPrefix, "");
        format = format.replace("]","");
        String[] split = format.split(",");

        for (String choiceMap : split){
            String[] sp = choiceMap.split("=");
            if (sp[0].equals(value)){
                return sp[1];
            }
        }

        return value;
    }

    private String getDateFormattedValue(String format, String value) {
        format = format.replace(boolFormatPrefix, "");
        format = format.replace("]","");

        try{
            Date date = StringUtil.toDate(value, "yyyy-MM-dd"); // the default date format
            String formattedDate = StringUtil.format(date, format); //format using the desired format
            value = formattedDate;
        }catch (Exception e){
            e.printStackTrace();
        }

        return value;
    }


    /**
     * A variable composed by text with commas, each word/text between the commas are values
     * @param str
     * @return
     */
    private ExtrasVariable tryParseExtrasVariable(String str){
        if (str.matches(".+\\[[0-9]+\\]")){
            ExtrasVariable extras = new ExtrasVariable();
            int indexPar = str.indexOf("[");
            extras.columnName = str.substring(0, indexPar);
            extras.arrayIndex = Integer.parseInt(str.substring(indexPar+1,str.length()-1));

            return extras;
        }

        return null;
    }

    private class ExtrasVariable {
        public String columnName;
        public int arrayIndex;
    }
}
