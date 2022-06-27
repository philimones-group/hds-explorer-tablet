package org.philimone.hds.explorer.data;

import android.util.Log;

import org.philimone.hds.explorer.adapter.model.TrackingSubjectItem;
import org.philimone.hds.explorer.model.Dataset;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Form_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.User;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.objectbox.Box;
import mz.betainteractive.io.readers.CSVReader;
import mz.betainteractive.utilities.StringUtil;

/**
 * Created by paul on 8/9/16.
 */
public class FormDataLoader implements Serializable {
    private final String householdPrefix = "Household.";
    private final String memberPrefix = "Member.";
    private final String userPrefix = "User.";
    private final String regionPrefix = "Region.";
    private final String trackingListPrefix = "FollowUp-List.";
    private final String constPrefix = "#.";
    private final String specialConstPrefix = "$.";

    private final String boolFormatPrefix = "Boolean[";
    private final String dateFormatPrefix = "Date[";
    private final String choiceFormatPrefix = "Choices[";

    private Form form;
    private TrackingSubjectItem trackingSubjectItem;
    private Map<String, Object> values;

    private Map<String, CSVReader.CSVRow> generalCSVRows;

    public FormDataLoader(){
        this.values = new LinkedHashMap<>();

        if (generalCSVRows == null){
            //Log.d("initiating gnv-rows", "init status");
            generalCSVRows = new LinkedHashMap<>();
        }
    }

    public FormDataLoader(Form form){
        this();
        this.form = form;
    }

    public FormDataLoader(Form form, TrackingSubjectItem subjectItem){
        this(form);
        this.trackingSubjectItem = subjectItem;
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

    public void putData(String key, String value){
        this.values.put(key, value);
    }

    public boolean hasMappedDatasetVariable(Dataset dataSet){

        for (String mapValue : form.getFormMap().values()){
            //Log.d("mapValue", ""+mapValue);
            if (mapValue.startsWith(dataSet.getName()+".")){ //add point at the end to match correctly
                return true;
            }
        }

        return false;
    }

    public Set<String> getPossibleDatasetNames() {
        Set<String> list = new HashSet<>();

        for (String mapValue : form.getFormMap().values()){
            if (mapValue.contains(".") && !mapValue.startsWith(regionPrefix) && !mapValue.startsWith(householdPrefix) && !mapValue.startsWith(memberPrefix) && !mapValue.startsWith(userPrefix) && !mapValue.startsWith(constPrefix) && !mapValue.startsWith(specialConstPrefix)) {
                //possible dataset
                String dt = mapValue.substring(0, mapValue.indexOf("."));
                //Log.d("dt",""+dt);
                list.add(dt);
            }
        }

        return list;
    }

    public void loadHouseholdValues(Household household){
        Map<String, String> map = form.getFormMap();
        for (String key : map.keySet()){
            //key   - odkVariable
            //value - domain column name
            String mapValue = map.get(key); //Domain ColumnName that we will get its content
            if (mapValue.startsWith(householdPrefix)) {
                String internalVariableName = mapValue.replace(householdPrefix, "");
                String odkVariable = key;
                String value = household.getValueByName(internalVariableName);

                if (value == null) value = "";

                //get variable format from odkVariable eg. variableName->format => patientName->yes,no
                //Log.d("odkvar", ""+odkVariable);
                if (odkVariable.contains("->")) {
                    String[] splt = odkVariable.split("->");
                    odkVariable = splt[0]; //variableName
                    String format = splt[1];      //format of the value
                    if (!format.equalsIgnoreCase("None")) {
                        if (format.startsWith(boolFormatPrefix)) {
                            value = getBooleanFormattedValue(format, value);
                        }
                        if (format.startsWith(choiceFormatPrefix)) {
                            value = getChoicesFormattedValue(format, value);
                        }
                        if (format.startsWith(dateFormatPrefix)) {
                            value = getDateFormattedValue(format, value);
                        }
                    }
                }

                this.values.put(odkVariable, value);
                Log.d("h-odk auto-loadable", odkVariable + ", " + value);
            }
        }
    }

    public void loadMemberValues(Member member){
        Map<String, String> map = form.getFormMap();
        for (String key : map.keySet()){
            //key   - odkVariable
            //value - domain column name
            String mapValue = map.get(key); //Domain ColumnName that we will get its content
            if (mapValue.startsWith(memberPrefix)) {
                String internalVariableName = mapValue.replace(memberPrefix, "");
                String odkVariable = key;
                String value = member.getValueByName(internalVariableName);

                if (value == null) value = "";

                //get variable format from odkVariable eg. variableName->format => patientName->yes,no
                if (odkVariable.contains("->")) {
                    String[] splt = odkVariable.split("->");
                    odkVariable = splt[0]; //variableName
                    String format = splt[1];      //format of the value
                    if (!format.equalsIgnoreCase("None")) {
                        if (format.startsWith(boolFormatPrefix)) {
                            value = getBooleanFormattedValue(format, value);
                        }
                        if (format.startsWith(choiceFormatPrefix)) {
                            value = getChoicesFormattedValue(format, value);
                        }
                        if (format.startsWith(dateFormatPrefix)) {
                            value = getDateFormattedValue(format, value);
                        }
                    }
                }

                this.values.put(odkVariable, value);
                //Log.d("m-odk auto-loadable", odkVariable + ", " + value);
            }
        }
    }

    public void loadUserValues(User user){
        Map<String, String> map = form.getFormMap();
        for (String key : map.keySet()){
            //key   - odkVariable
            //value - domain column name
            String mapValue = map.get(key); //Domain ColumnName that we will get its content
            if (mapValue.startsWith(userPrefix)) {
                String internalVariableName = mapValue.replace(userPrefix, "");
                String odkVariable = key;
                String value = user.getValueByName(internalVariableName);

                if (value == null) value = "";

                //get variable format from odkVariable eg. variableName->format => patientName->yes,no
                if (odkVariable.contains("->")) {
                    String[] splt = odkVariable.split("->");
                    odkVariable = splt[0]; //variableName
                    String format = splt[1];      //format of the value
                    if (!format.equalsIgnoreCase("None")) {
                        if (format.startsWith(boolFormatPrefix)) {
                            value = getBooleanFormattedValue(format, value);
                        }
                        if (format.startsWith(choiceFormatPrefix)) {
                            value = getChoicesFormattedValue(format, value);
                        }
                        if (format.startsWith(dateFormatPrefix)) {
                            value = getDateFormattedValue(format, value);
                        }
                    }
                }

                this.values.put(odkVariable, value);
                //Log.d("u-odk auto-loadable", odkVariable + ", " + value);
            }
        }
    }

    public void loadRegionValues(Region region){
        Map<String, String> map = form.getFormMap();
        for (String key : map.keySet()){
            //key   - odkVariable
            //value - domain column name
            String mapValue = map.get(key); //Domain ColumnName that we will get its content
            if (mapValue.startsWith(regionPrefix)) {
                String internalVariableName = mapValue.replace(regionPrefix, "");
                String odkVariable = key;
                String value = region.getValueByName(internalVariableName);

                if (value == null) value = "";

                //get variable format from odkVariable eg. variableName->format => patientName->yes,no
                if (odkVariable.contains("->")) {
                    String[] splt = odkVariable.split("->");
                    odkVariable = splt[0]; //variableName
                    String format = splt[1];      //format of the value
                    if (!format.equalsIgnoreCase("None")) {
                        if (format.startsWith(boolFormatPrefix)) {
                            value = getBooleanFormattedValue(format, value);
                        }
                        if (format.startsWith(choiceFormatPrefix)) {
                            value = getChoicesFormattedValue(format, value);
                        }
                        if (format.startsWith(dateFormatPrefix)) {
                            value = getDateFormattedValue(format, value);
                        }
                    }
                }

                this.values.put(odkVariable, value);
                //Log.d("r-odk auto-loadable", odkVariable + ", " + value);
            }
        }
    }

    public void loadTrackingListValues(){
        if (this.trackingSubjectItem != null) {
            Map<String, String> map = form.getFormMap();
            for (String key : map.keySet()){
                //key   - odkVariable
                //value - domain column name
                String mapValue = map.get(key); //Domain ColumnName that we will get its content
                if (mapValue.startsWith(trackingListPrefix)) {
                    String internalVariableName = mapValue.replace(trackingListPrefix, "");
                    String odkVariable = key;
                    String value = ""; //member.getValueByName(internalVariableName);

                    if (key.equals("subject_visit_code")) value = this.trackingSubjectItem.getVisitCode();

                    if (key.equals("subject_visit_uuid")) value = this.trackingSubjectItem.getVisitUuid();

                    //get variable format from odkVariable eg. variableName->format => patientName->yes,no
                    if (odkVariable.contains("->")) {
                        String[] splt = odkVariable.split("->");
                        odkVariable = splt[0]; //variableName
                        String format = splt[1];      //format of the value
                        if (!format.equalsIgnoreCase("None")) {
                            if (format.startsWith(boolFormatPrefix)) {
                                value = getBooleanFormattedValue(format, value);
                            }
                            if (format.startsWith(choiceFormatPrefix)) {
                                value = getChoicesFormattedValue(format, value);
                            }
                            if (format.startsWith(dateFormatPrefix)) {
                                value = getDateFormattedValue(format, value);
                            }
                        }
                    }

                    this.values.put(odkVariable, value);
                    Log.d("trl-odk auto-loadable", odkVariable + ", " + value);
                }
            }
        }
    }

    /*When were loading constant values: the key of map will be "#.value" where "value" is the exact value to be used = internalVariableName*/
    public void loadConstantValues(){
        Map<String, String> map = form.getFormMap();
        for (String key : map.keySet()){
            //key   - odkVariable
            //value - domain column name
            String mapValue = map.get(key); //Domain ColumnName that we will get its content
            if (mapValue.startsWith(constPrefix)) {
                String internalVariableName = mapValue.replace(constPrefix, "");
                String odkVariable = key;
                String value = internalVariableName; //user.getValueByName(internalVariableName);

                if (value == null) value = "";

                //get variable format from odkVariable eg. variableName->format => patientName->yes,no
                if (odkVariable.contains("->")) {
                    String[] splt = odkVariable.split("->");
                    odkVariable = splt[0]; //variableName
                    String format = splt[1];      //format of the value
                    if (!format.equalsIgnoreCase("None")) {
                        if (format.startsWith(boolFormatPrefix)) {
                            value = getBooleanFormattedValue(format, value);
                        }
                        if (format.startsWith(choiceFormatPrefix)) {
                            value = getChoicesFormattedValue(format, value);
                        }
                        if (format.startsWith(dateFormatPrefix)) {
                            value = getDateFormattedValue(format, value);
                        }
                    }
                }

                this.values.put(odkVariable, value);
                //Log.d("u-odk auto-loadable", odkVariable + ", " + value);
            }
        }
    }

    /* loading special constant values */
    public void loadSpecialConstantValues(Household household, Member member, User user, Region region, TrackingSubjectItem memberItem){
        Map<String, String> map = form.getFormMap();
        for (String key : map.keySet()){
            //Log.d("special constant", ""+key );
            //key   - odkVariable
            //value - domain column name
            String mapValue = map.get(key); //Domain ColumnName that we will get its content
            if (mapValue.startsWith(specialConstPrefix)) {
                final String internalVariableName = mapValue.replace(specialConstPrefix, "");
                String odkVariable = key;
                String value = internalVariableName; //user.getValueByName(internalVariableName);

                if (value == null) value = "";

                //check on constants
                if (internalVariableName.equals("MemberExists")){ //Member Exists on DSS Database
                    value = (member!=null && member.getId()>0) ? "true" : "false";
                }

                if (internalVariableName.equals("Timestamp")){ //Member Exists on DSS Database
                    value = StringUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
                }

                //get variable format from odkVariable eg. variableName->format => patientName->yes,no
                if (odkVariable.contains("->")) {
                    String[] splt = odkVariable.split("->");
                    odkVariable = splt[0]; //variableName
                    String format = splt[1];      //format of the value
                    if (!format.equalsIgnoreCase("None")) {
                        if (format.startsWith(boolFormatPrefix)) {
                            value = getBooleanFormattedValue(format, value);
                        }
                        if (format.startsWith(choiceFormatPrefix)) {
                            value = getChoicesFormattedValue(format, value);
                        }
                        if (format.startsWith(dateFormatPrefix)) {

                            if (internalVariableName.equals("Timestamp")) {
                                value = getDateFormattedValue("yyyy-MM-dd HH:mm:ss", format, value);
                            } else {
                                value = getDateFormattedValue(format, value);
                            }

                        }
                    }
                }

                this.values.put(odkVariable, value);
                //Log.d("u-odk spc auto-loadable", odkVariable + ", " + value);
            }
        }
    }

    /**
     * Intended to be executed before starting to collect the form
     */
    public void reloadTimestampConstants(){
        Map<String, String> map = form.getFormMap();
        for (String key : map.keySet()){
            //Log.d("special constant", ""+key );
            //key   - odkVariable
            //value - domain column name
            String mapValue = map.get(key); //Domain ColumnName that we will get its content
            if (mapValue.startsWith(specialConstPrefix)) {
                final String internalVariableName = mapValue.replace(specialConstPrefix, "");
                String odkVariable = key;
                String value = "";
                boolean foundTimestampVariable = false;
                //check on constants

                if (internalVariableName.equals("Timestamp")){ //Member Exists on DSS Database
                    value = StringUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
                    foundTimestampVariable = true;
                }

                /* Only Execute if is a Timestamp Variable */
                if (foundTimestampVariable){
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

                            if (internalVariableName.equals("Timestamp")) {
                                value = getDateFormattedValue("yyyy-MM-dd HH:mm:ss", format, value);
                            } else {
                                value = getDateFormattedValue(format, value);
                            }

                        }
                    }

                    //This will override the previous loaded value
                    this.values.put(odkVariable, value);
                    //Log.d("u-odk spc auto-loadable", odkVariable + ", " + value);
                }

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

    private String getDateFormattedValue(String originalFormat, String format, String value) {
        format = format.replace(boolFormatPrefix, "");
        format = format.replace("]","");

        try{
            Date date = StringUtil.toDate(value, originalFormat); // the original date format must be given
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
        /* // WE ARE NOT USING EXTRAS VARIABLES ON HDS-EXPLORER
        if (str.matches(".+\\[[0-9]+\\]")){
            ExtrasVariable extras = new ExtrasVariable();
            int indexPar = str.indexOf("[");
            extras.columnName = str.substring(0, indexPar);
            extras.arrayIndex = Integer.parseInt(str.substring(indexPar+1,str.length()-1));

            return extras;
        }
        */
        return null;

    }

    private class ExtrasVariable {
        public String columnName;
        public int arrayIndex;
    }

    /* Load Datasets Values */
    public void loadDataSetValues(Dataset dataSet, Household household, Member member, User user, Region region) {
        String tableName = dataSet.getTableName()+".";
        String tableColumnName = dataSet.getTableColumn();
        String linkValue = "";
        CSVReader.CSVRow valueRow = null;
        String dataSetPrefix = dataSet.getName()+".";

        if (tableName.startsWith(householdPrefix)){
            if (household != null){
                linkValue = household.getValueByName(tableColumnName);
            }
        }
        if (tableName.startsWith(memberPrefix)){
            if (member != null){
                linkValue = member.getValueByName(tableColumnName);
            }
        }
        if (tableName.startsWith(userPrefix)){
            if (user != null){
                linkValue = user.getValueByName(tableColumnName);
            }
        }
        if (tableName.startsWith(regionPrefix)){
            if (region != null){
                linkValue = region.getValueByName(tableColumnName);
            }
        }



        //process zip file - get row with ${dtKeyColumn}=linkValue  -

        String csvRowKey = dataSet.getName()+"->"+tableName+tableColumnName+"="+linkValue;

        boolean vrExists = this.generalCSVRows.containsKey(csvRowKey);
        valueRow = this.generalCSVRows.get(csvRowKey);

        if (!vrExists){

            valueRow = getRowFromCSVFile(dataSet, linkValue);

            //Log.d("Key1: " +csvRowKey, "reading dataset: "+dataSet.getName()+ ", "+valueRow);


            this.generalCSVRows.put(csvRowKey, valueRow);

        } else {
            if (valueRow == null){
                //Log.d("Key2: "+csvRowKey, "not reading data because it was not found before");
            } else {
                //Log.d("Key2: "+csvRowKey, "get data from saved row");
            }

        }

        //Log.d("found row", ""+valueRow+", linkValue: "+linkValue);

        if (valueRow == null){
            return;
        }

        Map<String, String> map = form.getFormMap();
        for (String odkVariable : map.keySet()){
            String mapVariable = map.get(odkVariable);

            if (mapVariable.startsWith(dataSetPrefix)){ // Get DatasetName.csvColumn
                String internalVariableName = mapVariable.replace(dataSetPrefix, "");

                String value = valueRow.getField(internalVariableName);

                if (value == null) value = "";

                //get variable format from odkVariable eg. variableName->format => patientName->yes,no
                //Log.d("xxx-odkvar", ""+odkVariable);
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
                //Log.d("xxx-odk auto-loadable", odkVariable + ", " + value);
            }


        }

    }

    private CSVReader.CSVRow getRowFromCSVFile(Dataset dataSet, String linkValue) {

        //Log.d("zip", "processing zip file, linkValue="+linkValue);

        if (linkValue == null || linkValue == "") return null;  //dont need to read the csv without need

        try {
            InputStream inputStream = new FileInputStream(dataSet.getFilename());

            ZipInputStream zin = new ZipInputStream(inputStream);
            ZipEntry entry = zin.getNextEntry();

            if (entry != null){ //has a file inside (supposed to be a csv file)
                //processXMLDocument(zin);

                CSVReader csvReader = new CSVReader(zin, true, ",");
                //Log.d("fields", csvReader.getFieldNames()+", "+csvReader.getMapFields()+", "+dataSet.getKeyColumn());
                for (CSVReader.CSVRow row : csvReader.getRows()){
                    String csvKeyCol = row.getField(dataSet.getKeyColumn());
                    //Log.d("keyColValue", ""+csvKeyCol+" == "+linkValue);
                    if (csvKeyCol!=null && csvKeyCol.equals(linkValue)){
                        return row; //break the loop
                    }
                }


                zin.closeEntry();
            }

            zin.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    /* Static dataloaders */
    public static FormDataLoader[] getFormLoaders(Box<Form> boxForms, User user, FormFilter... filters){

        List<FormFilter> listFilters = Arrays.asList(filters);

        List<Form> forms = boxForms.query().order(Form_.formName).build().find(); //get all forms
        List<FormDataLoader> list = new ArrayList<>();

        int i=0;
        for (Form form : forms){

            //Log.d("forms", ""+user.getModules() +" - " + form.getModules() );
            //Log.d("forms-map", "" + form.formMap );
            if (StringUtil.containsAny(user.modules, form.modules)){ //if the user has access to module specified on Form

                FormDataLoader loader = new FormDataLoader(form);

                if (form.isFollowUpForm() && listFilters.contains(FormFilter.FOLLOW_UP)){
                    list.add(loader);
                    continue;
                }
                if (form.isRegionForm() && listFilters.contains(FormFilter.REGION)){
                    list.add(loader);
                    continue;
                }
                if (form.isHouseholdForm() && listFilters.contains(FormFilter.HOUSEHOLD)){
                    list.add(loader);
                    continue;
                }
                if (form.isHouseholdHeadForm() && listFilters.contains(FormFilter.HOUSEHOLD_HEAD)){
                    list.add(loader);
                    continue;
                }
                if (form.isMemberForm() && listFilters.contains(FormFilter.MEMBER)){
                    list.add(loader);
                    continue;
                }
            }
        }

        FormDataLoader[] aList = new FormDataLoader[list.size()];

        return list.toArray(aList);
    }

    public static List<FormDataLoader> getFormLoadersList(Box<Form> boxForms, User user, FormFilter... filters){

        List<FormFilter> listFilters = Arrays.asList(filters);

        List<Form> forms = boxForms.query().order(Form_.formName).build().find(); //get all forms
        List<FormDataLoader> list = new ArrayList<>();

        int i=0;
        for (Form form : forms){

            //Log.d("forms", ""+user.getModules() +" - " + form.getModules() );
            //Log.d("forms-map", "" + form.formMap );
            if (StringUtil.containsAny(user.modules, form.modules)){ //if the user has access to module specified on Form

                FormDataLoader loader = new FormDataLoader(form);

                if (form.isFollowUpForm() && listFilters.contains(FormFilter.FOLLOW_UP)){
                    list.add(loader);
                    continue;
                }
                if (form.isRegionForm() && listFilters.contains(FormFilter.REGION)){
                    list.add(loader);
                    continue;
                }
                if (form.isHouseholdForm() && listFilters.contains(FormFilter.HOUSEHOLD)){
                    list.add(loader);
                    continue;
                }
                if (form.isHouseholdHeadForm() && listFilters.contains(FormFilter.HOUSEHOLD_HEAD)){
                    list.add(loader);
                    continue;
                }
                if (form.isMemberForm() && listFilters.contains(FormFilter.MEMBER)){
                    list.add(loader);
                    continue;
                }
            }
        }

        return list;
    }
}
