package org.philimone.hds.explorer.utilities;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.io.xml.FormXmlReader;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.FormGroupInstance;
import org.philimone.hds.explorer.model.FormGroupInstanceChild;
import org.philimone.hds.explorer.model.FormGroupInstance_;
import org.philimone.hds.explorer.model.FormGroupMapping;
import org.philimone.hds.explorer.model.FormSubject;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.forms.widget.ColumnView;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.objectbox.Box;
import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

public class FormGroupUtilities {
    private Context mContext;
    private Box<FormGroupInstance> boxFormGroupInstances;
    private Box<FormGroupInstanceChild> boxFormGroupInstanceChilds;
    private Box<CollectedData> boxCollectedData;
    private User currentUser;

    private JexlEngine scriptEngine;
    private DateUtil dateUtil = Bootstrap.getDateUtil();

    public FormGroupUtilities(Context context) {
        this.mContext = context;
        initBoxes();
        initScriptEngine();
        this.currentUser = Bootstrap.getCurrentUser();
    }

    private void initBoxes() {
        this.boxFormGroupInstances = ObjectBoxDatabase.get().boxFor(FormGroupInstance.class);
        this.boxFormGroupInstanceChilds = ObjectBoxDatabase.get().boxFor(FormGroupInstanceChild.class);
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
    }

    private void initScriptEngine() {
        this.scriptEngine = new JexlBuilder().create();
    }

    public FormGroupInstance getLastFormGroupInstanceCreated(Form formGroup, FormSubject subject){

        FormGroupInstance instance = this.boxFormGroupInstances.query(FormGroupInstance_.groupFormId.equal(formGroup.formId)
                                                                      .and(FormGroupInstance_.subjectEntity.equal(subject.getTableName().code).and(FormGroupInstance_.subjectCode.equal(subject.getCode()))))
                                                                      .orderDesc(FormGroupInstance_.createdDate)
                                                                      .build().findFirst();

        return instance;
    }

    public FormGroupInstance createNewInstance(Form formGroup, FormSubject subject) {
        FormGroupInstance instance = new FormGroupInstance();
        instance.groupFormId = formGroup.formId;
        instance.groupFormName = formGroup.formName;
        instance.subjectEntity = subject.getTableName();
        instance.subjectCode = subject.getCode();
        instance.instanceUuid = GeneralUtil.generateUUID();
        instance.instanceCode = generateCode(formGroup, subject);
        instance.createdDate = new Date();

        for (FormGroupMapping formGroupMapping : formGroup.groupMappings) {
            instance.formsChilds.add(formGroupMapping.formId);
        }

        //instance.instanceChilds
        return instance;
    }

    public FormGroupInstanceChild createNewInstanceChild(FormGroupInstance formGroupInstance, CollectedData collectedData) {
        FormGroupInstanceChild instanceChild = new FormGroupInstanceChild();
        //instanceChild.groupInstance.setTarget(formGroupInstance);
        instanceChild.formId = collectedData.formId;
        instanceChild.formInstanceUri = collectedData.formXmlPath;
        instanceChild.collected = true;
        instanceChild.collectedDate = collectedData.formLastUpdatedDate;
        instanceChild.uploaded = false;
        instanceChild.uploadedDate = null;

        return instanceChild;
    }

    public FormGroupInstance updateTimestamp(FormGroupInstance formGroupInstance) {
        formGroupInstance.createdDate = new Date();
        formGroupInstance.instanceCode = generateCode(formGroupInstance.groupFormId, formGroupInstance.subjectCode);
        return formGroupInstance;
    }

    private String generateCode(Form formGroup, FormSubject subject){
        return generateCode(formGroup.formId, subject.getCode());
    }

    private String generateCode(String formId, FormSubject subject){
        return generateCode(formId, subject.getCode());
    }

    private String generateCode(String formId, String subjectCode){
        //20220802-122459.600
        String timestamp = StringUtil.format(new Date(), "yyyyMMdd-HHmmss.SSS");

        return formId + "_" +subjectCode + "_" + currentUser.getCode() + "_" + timestamp;
    }

    public FormGroupInstanceChild findInstanceChildBy(FormGroupInstance formGroupInstance, String instanceUri) {

        for (FormGroupInstanceChild instanceChild : formGroupInstance.instanceChilds) {
            if (instanceChild.formInstanceUri.equals(instanceUri)) {
                return instanceChild;
            }
        }

        return null;
    }

    public Object evaluateExpression(String expressionText) {
        JexlContext jexlContext = new MapContext();
        JexlExpression jxelExpression = this.scriptEngine.createExpression(expressionText);

        Log.d("evaluate", ""+expressionText);

        return jxelExpression.evaluate(jexlContext);
    }

    /**
     * substitute variables with values and operators
     * @param expression
     * @return
     */
    public ExTranslationResult translateExpression(String expression, FormGroupInstance formGroupInstance) {

        //${form_group_sample_main.question1}=true
        //replace formInstance variables with values, ${}
        //1. extract variables ${}
        //2. variables must come from form instances now, so it must have "."
        //3. extract form and variable

        //Log.d("")

        Map<String, String> mapVariableValues = new LinkedHashMap<>();
        Map<String, Map<String, String>> mapFormContent = new HashMap<>();
        Map<String, FormGroupInstanceChild> mapInstanceChilds = new HashMap<>();

        //Log.d("childs", ""+formGroupInstance.instanceChilds.size());

        for (FormGroupInstanceChild instanceChild : formGroupInstance.instanceChilds) {
            Log.d("instace "+instanceChild.formId, ""+instanceChild.formInstanceUri);
            mapInstanceChilds.put(instanceChild.formId, instanceChild);
        }

        //1. extract vars
        Pattern patternVarName = Pattern.compile("\\$\\{([a-zA-Z0-9_\\.]+)\\w*\\}");
        Matcher matcherVars = patternVarName.matcher(expression);
        while (matcherVars.find()) {
            String variableName = matcherVars.group(1);

            //1. supported variables from other collected forms
            if (variableName.contains(".")) {
                String[] spt = variableName.split("\\.");
                String formId = spt[0];
                String columnName = spt[1];
                FormGroupInstanceChild instanceChild = mapInstanceChilds.get(formId);
                String formInstanceUri = instanceChild != null ? instanceChild.formInstanceUri : "";

                //check if form is finalized
                CollectedData collectedData = boxCollectedData.query(CollectedData_.formXmlPath.equal(formInstanceUri)).build().findFirst();

                if (collectedData == null) {
                    return new ExTranslationResult(TranslationStatus.ERROR_DEPENDENCY_NOT_FOUND, "", null, instanceChild);
                } else {
                    if (!collectedData.isFormFinalized()) {
                        return new ExTranslationResult(TranslationStatus.ERROR_DEPENDENCY_FORM_NOT_FINALIZED, "", null, instanceChild);
                    }
                }

                //get form variables
                Map<String,String> formData = mapFormContent.get(formId);

                Log.d("readxml "+formId, ""+formInstanceUri+", formdata="+formData);

                //read xml data if not readed yet
                if (formData == null) {
                    InputStream xmlInputStream = openInstanceInputStream(formInstanceUri);
                    formData = FormXmlReader.getXmlData(xmlInputStream);
                    mapFormContent.put(formId, formData);
                }

                Log.d("readxml 2 "+formId, ""+formInstanceUri+", formdata="+formData);

                if (formData != null && columnName != null) {
                    //we have data
                    String variableValue = formData.get(columnName);

                    mapVariableValues.put(variableName, variableValue);
                }
            }
        }

        //Replace all variables with values
        for (Map.Entry<String,String> entry : mapVariableValues.entrySet()) {
            Log.d("parent-ev", ""+entry.getKey()+" : "+entry.getValue());
            expression = expression.replace("${"+entry.getKey()+"}", "'" + entry.getValue() + "'");
        }

        expression = expression.replace("and", "&&");
        expression = expression.replace("or", "||");
        expression = expression.replace("!=", "<>"); //to avoid !==
        expression = expression.replace("=", "==");
        expression = expression.replace("<>", "!="); //return to normal after =

        String[] words = {"true", "false", "yes", "no"};

        expression = StringUtil.toLowerCase(expression, words);

        return new ExTranslationResult(TranslationStatus.SUCCESS, expression);
    }

    private InputStream openInstanceInputStream(String instanceFileUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //SAF
            try {
                Uri uri = Uri.parse(instanceFileUri);
                return mContext.getContentResolver().openInputStream(uri);
            }catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            //File API
            try {
                return new FileInputStream(instanceFileUri);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    public class ExTranslationResult {

        public String translatedExpression;
        public TranslationStatus status;
        public String errorMessage;
        public FormGroupInstanceChild affectedChild;

        public ExTranslationResult(TranslationStatus status, String translatedExpression, String errorMessage, FormGroupInstanceChild instanceChild) {
            this.translatedExpression = translatedExpression;
            this.status = status;
            this.errorMessage = errorMessage;
            this.affectedChild = instanceChild;
        }

        public ExTranslationResult(TranslationStatus status, String translatedExpression) {
            this.translatedExpression = translatedExpression;
            this.status = status;
        }
    }

    public enum TranslationStatus { SUCCESS, ERROR, ERROR_DEPENDENCY_FORM_NOT_FINALIZED, ERROR_DEPENDENCY_NOT_FOUND};
}
