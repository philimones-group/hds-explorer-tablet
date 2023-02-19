package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.converters.StringCollectionConverter;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import java.util.Date;
import java.util.HashMap;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.utilities.StringUtil;

public class PreHouseholdFormUtil extends FormUtil<Household> {

    private Box<Region> boxRegions;
    private Region region;
    private Household household;

    public PreHouseholdFormUtil(Fragment fragment, Context context, Region region, FormUtilities odkFormUtilities, FormUtilListener<Household> listener){
        super(fragment, context, FormUtil.getPreHouseholdForm(context), odkFormUtilities, listener);

        initBoxes();

        this.region = region;

        initialize();
    }

    public PreHouseholdFormUtil(Fragment fragment, Context context, Household householdToEdit, FormUtilities odkFormUtilities, FormUtilListener<Household> listener){
        super(fragment, context, FormUtil.getPreHouseholdForm(context), householdToEdit, odkFormUtilities, listener);

        initBoxes();

        this.household = householdToEdit;
        this.region = this.boxRegions.query(Region_.code.equal(householdToEdit.region)).build().findFirst();

        initialize();
    }

    public PreHouseholdFormUtil(AppCompatActivity activity, Context context, Region region, FormUtilities odkFormUtilities, FormUtilListener<Household> listener){
        super(activity, context, FormUtil.getPreHouseholdForm(context), odkFormUtilities, listener);

        initBoxes();

        this.region = region;

        initialize();
    }

    public PreHouseholdFormUtil(AppCompatActivity activity, Context context, Household householdToEdit, FormUtilities odkFormUtilities, FormUtilListener<Household> listener){
        super(activity, context, FormUtil.getRegionForm(context), householdToEdit, odkFormUtilities, listener);

        initBoxes();

        this.household = householdToEdit;
        this.region = this.boxRegions.query(Region_.code.equal(householdToEdit.region)).build().findFirst();

        initialize();
    }

    public static PreHouseholdFormUtil newInstance(Mode openMode, Fragment fragment, Context context, Region region, Household householdToEdit, FormUtilities odkFormUtilities, FormUtilListener<Household> listener){
        if (openMode == Mode.CREATE) {
            return new PreHouseholdFormUtil(fragment, context, region, odkFormUtilities, listener);
        } else if (openMode == Mode.EDIT) {
            return new PreHouseholdFormUtil(fragment, context, householdToEdit, odkFormUtilities, listener);
        }

        return null;
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
    }

    @Override
    protected void preloadValues() {
        preloadedMap.put("regionCode", region.code);
        preloadedMap.put("regionName", region.name);
        preloadedMap.put("householdCode", codeGenerator.generateHouseholdCode(region, user));
        preloadedMap.put("householdName", "");
        preloadedMap.put("modules", this.user.getSelectedModulesCodes());
    }

    @Override
    protected void preloadUpdatedValues() {
        //nothing to update yet
    }

    @Override
    public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {
        ColumnValue columnRegionCode = collectedValues.get("regionCode");
        ColumnValue columnRegionName = collectedValues.get("regionName");
        ColumnValue columnHouseholdCode = collectedValues.get("householdCode");
        ColumnValue columnHouseholdName = collectedValues.get("householdName");

        String region_code = columnRegionCode.getValue();
        String region_name = columnRegionName.getValue();
        String household_code = columnHouseholdCode.getValue();
        String household_name = columnHouseholdName.getValue();
        //String region_gps = collectedValues.get("gps").getValue();

        if (StringUtil.isBlank(region_code)){
            String message = this.context.getString(R.string.new_region_code_empty_lbl);
            return new ValidationResult(columnRegionCode, message);
        }

        if (StringUtil.isBlank(region_name)){
            String message = this.context.getString(R.string.new_region_name_empty_lbl);
            //DialogFactory.createMessageInfo(RegionDetailsActivity.this, R.string.info_lbl, R.string.new_region_code_empty_lbl).show();
            return new ValidationResult(columnRegionName, message);
        }

        if (StringUtil.isBlank(household_code)){
            String message = this.context.getString(R.string.new_household_code_empty_lbl);
            return new ValidationResult(columnHouseholdCode, message);
        }

        if (!codeGenerator.isHouseholdCodeValid(household_code)){
            String message = this.context.getString(R.string.new_household_code_err_lbl);
            return new ValidationResult(columnHouseholdCode, message);
        }

        /* ITS OPTIONAL
        if (StringUtil.isBlank(household_name)){
            String message = this.context.getString(R.string.new_household_name_empty_lbl);
            //DialogFactory.createMessageInfo(RegionDetailsActivity.this, R.string.info_lbl, R.string.new_region_code_empty_lbl).show();
            return new ValidationResult(columnRegionName, message);
        }*/

        //check if household with code exists
        if (currentMode==Mode.CREATE && boxHouseholds.query().equal(Household_.code, household_code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst() != null){
            String message = this.context.getString(R.string.new_household_code_exists_lbl);
            return new ValidationResult(columnHouseholdCode, message);
        }

        return ValidationResult.noErrors();
    }

    @Override
    public void onBeforeFormFinished(HForm form, CollectedDataMap collectedValues) {

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

        ColumnValue columnRegionCode = collectedValues.get("regionCode");
        ColumnValue columnRegionName = collectedValues.get("regionName");
        ColumnValue columnHouseholdCode = collectedValues.get("householdCode");
        ColumnValue columnHouseholdName = collectedValues.get("householdName");
        ColumnValue columnModules = collectedValues.get("modules");

        String region_code = columnRegionCode.getValue();
        String region_name = columnRegionName.getValue();
        String household_code = columnHouseholdCode.getValue();
        String household_name = columnHouseholdName.getValue();

        Household household = new Household();

        household.code = household_code;
        household.name = household_name;
        household.region = region_code;

        household.shareable = true;
        household.preRegistered = true;

        household.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        household.recentlyCreated = true;
        household.recentlyCreatedUri = result.getFilename();
        household.modules.addAll(StringCollectionConverter.getCollectionFrom(columnModules.getValue()));

        boxHouseholds.put(household);

        collectedData = new CoreCollectedData();
        collectedData.visitId = 0; //not a household visit based event
        collectedData.formEntity = CoreFormEntity.PRE_HOUSEHOLD;
        collectedData.formEntityId = household.id;
        collectedData.formEntityCode = household.code;
        collectedData.formEntityName = household.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        collectedData.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        collectedData.extension.setTarget(this.getFormExtension(collectedData.formEntity));

        boxCoreCollectedData.put(collectedData);

        this.entity = household;
        this.household = household;
        collectExtensionForm(collectedValues);
    }

    private void onModeEdit(CollectedDataMap collectedValues, XmlFormResult result) {

        ColumnValue columnRegionCode = collectedValues.get("regionCode");
        ColumnValue columnRegionName = collectedValues.get("regionName");
        ColumnValue columnHouseholdCode = collectedValues.get("householdCode");
        ColumnValue columnHouseholdName = collectedValues.get("householdName");
        ColumnValue columnModules = collectedValues.get("modules");

        String region_code = columnRegionCode.getValue();
        String region_name = columnRegionName.getValue();
        String household_code = columnHouseholdCode.getValue();
        String household_name = columnHouseholdName.getValue();

        Household phousehold = this.entity;

        phousehold.code = household_code;
        phousehold.name = household_name;
        phousehold.region = region_code;

        //phousehold.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        //phousehold.recentlyCreated = true;
        //phousehold.recentlyCreatedUri = result.getFilename();
        //phousehold.modules.addAll(StringCollectionConverter.getCollectionFrom(colModules.getValue()));

        boxHouseholds.put(phousehold);

        //collectedData was read when creating an Edit FormUtil
        collectedData.formEntityCode = phousehold.code;
        collectedData.formEntityName = phousehold.name;
        collectedData.updatedDate = new Date();
        boxCoreCollectedData.put(collectedData);

        //collectExtensionForm(collectedValues);

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
