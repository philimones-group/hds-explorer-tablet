package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.CoreCollectedData;
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
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.utilities.StringUtil;

public class RegionFormUtil extends FormUtil<Region> {

    private Box<Region> boxRegions;
    private Region parentRegion;
    private Region region;

    public RegionFormUtil(Fragment fragment, Context context, String parentRegionCode, FormUtilities odkFormUtilities, FormUtilListener<Region> listener){
        super(fragment, context, FormUtil.getRegionForm(context), odkFormUtilities, listener);

        initBoxes();

        this.parentRegion = this.boxRegions.query(Region_.code.equal(parentRegionCode)).build().findFirst();

        initialize();
    }

    public RegionFormUtil(Fragment fragment, Context context, Region regionToEdit, FormUtilities odkFormUtilities, FormUtilListener<Region> listener){
        super(fragment, context, FormUtil.getRegionForm(context), regionToEdit, odkFormUtilities, listener);

        initBoxes();

        this.region = regionToEdit;
        this.parentRegion = this.boxRegions.query(Region_.code.equal(regionToEdit.parent)).build().findFirst();

        initialize();
    }

    public RegionFormUtil(AppCompatActivity activity, Context context, String parentRegionCode, FormUtilities odkFormUtilities, FormUtilListener<Region> listener){
        super(activity, context, FormUtil.getRegionForm(context), odkFormUtilities, listener);

        initBoxes();

        this.parentRegion = this.boxRegions.query(Region_.code.equal(parentRegionCode)).build().findFirst();

        initialize();
    }

    public RegionFormUtil(AppCompatActivity activity, Context context, Region regionToEdit, FormUtilities odkFormUtilities, FormUtilListener<Region> listener){
        super(activity, context, FormUtil.getRegionForm(context), regionToEdit, odkFormUtilities, listener);

        initBoxes();

        this.region = regionToEdit;
        this.parentRegion = this.boxRegions.query(Region_.code.equal(regionToEdit.parent)).build().findFirst();

        initialize();
    }

    public static RegionFormUtil newInstance(Mode openMode, Fragment fragment, Context context, String parentRegionCode, Region regionToEdit, FormUtilities odkFormUtilities, FormUtilListener<Region> listener){
        if (openMode == Mode.CREATE) {
            return new RegionFormUtil(fragment, context, parentRegionCode, odkFormUtilities, listener);
        } else if (openMode == Mode.EDIT) {
            return new RegionFormUtil(fragment, context, regionToEdit, odkFormUtilities, listener);
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
        preloadedMap.put("parentCode", parentRegion.code);
        preloadedMap.put("parentName", parentRegion.name);
        preloadedMap.put("regionName", "");
        preloadedMap.put("regionCode", "");

        preloadedMap.put("modules", this.user.getSelectedModulesCodes());
    }

    @Override
    protected void preloadUpdatedValues() {
        //nothing to update yet
    }

    @Override
    public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {
        ColumnValue columnParentCode = collectedValues.get("parentCode");
        ColumnValue columnParentName = collectedValues.get("parentName");
        ColumnValue columnRegionCode = collectedValues.get("regionCode");
        ColumnValue columnRegionName = collectedValues.get("regionName");

        String parent_code = columnParentCode.getValue();
        String parent_name = columnParentName.getValue();
        String region_code = columnRegionCode.getValue();
        String region_name = columnRegionName.getValue();
        //String region_gps = collectedValues.get("gps").getValue();

        if (StringUtil.isBlank(region_name)){
            String message = this.context.getString(R.string.new_region_name_empty_lbl);
            //DialogFactory.createMessageInfo(RegionDetailsActivity.this, R.string.info_lbl, R.string.new_region_code_empty_lbl).show();
            return new ValidationResult(columnRegionName, message);
        }

        if (StringUtil.isBlank(region_code)){
            String message = this.context.getString(R.string.new_region_code_empty_lbl);
            return new ValidationResult(columnRegionCode, message);
        }

        if (isChildAtLowestLevel(this.parentRegion)) {
            if (!codeGenerator.isLowestRegionCodeValid(region_code)){
                String message = this.context.getString(R.string.new_region_code_err_lbl);
                //DialogFactory.createMessageInfo(RegionDetailsActivity.this, R.string.info_lbl, R.string.new_region_code_err_lbl).show();
                return new ValidationResult(columnRegionCode, message);
            }
        } else {
            if (!codeGenerator.isRegionCodeValid(region_code)){
                String message = this.context.getString(R.string.new_region_code_err_lbl);
                //DialogFactory.createMessageInfo(RegionDetailsActivity.this, R.string.info_lbl, R.string.new_region_code_err_lbl).show();
                return new ValidationResult(columnRegionCode, message);
            }
        }

        //check if region with code exists
        if (currentMode==Mode.CREATE && boxRegions.query().equal(Region_.code, region_code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst() != null){
            String message = this.context.getString(R.string.new_region_code_exists_lbl);
            //DialogFactory.createMessageInfo(RegionDetailsActivity.this, R.string.info_lbl, R.string.new_region_code_exists_lbl).show();
            return new ValidationResult(columnRegionCode, message);
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

        ColumnValue columnParentCode = collectedValues.get("parentCode");
        ColumnValue columnParentName = collectedValues.get("parentName");
        ColumnValue columnRegionCode = collectedValues.get("regionCode");
        ColumnValue columnRegionName = collectedValues.get("regionName");
        ColumnValue columnModules = collectedValues.get("modules");

        String parent_code = columnParentCode.getValue();
        String parent_name = columnParentName.getValue();
        String region_code = columnRegionCode.getValue();
        String region_name = columnRegionName.getValue();

        Region region = new Region();

        region.code = region_code;
        region.name = region_name;
        region.level = getNextLevel(this.parentRegion);
        region.parent = parent_code;

        region.shareable = true;
        region.preRegistration = false;

        region.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        region.recentlyCreated = true;
        region.recentlyCreatedUri = result.getFilename();
        region.modules.addAll(StringCollectionConverter.getCollectionFrom(columnModules.getValue()));

        boxRegions.put(region);

        collectedData = new CoreCollectedData();
        collectedData.visitId = 0; //not a household visit based event
        collectedData.formEntity = CoreFormEntity.REGION;
        collectedData.formEntityId = region.id;
        collectedData.formEntityCode = region.code;
        collectedData.formEntityName = region.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();
        collectedData.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        collectedData.extension.setTarget(this.getFormExtension(collectedData.formEntity));

        boxCoreCollectedData.put(collectedData);

        this.entity = region;
        this.region = region;
        collectExtensionForm(collectedValues);
    }

    private void onModeEdit(CollectedDataMap collectedValues, XmlFormResult result) {
        //saveNewRegion();
        ColumnValue columnParentCode = collectedValues.get("parentCode");
        ColumnValue columnParentName = collectedValues.get("parentName");
        ColumnValue columnRegionCode = collectedValues.get("regionCode");
        ColumnValue columnRegionName = collectedValues.get("regionName");
        ColumnValue columnModules = collectedValues.get("modules");

        String parent_code = columnParentCode.getValue();
        String parent_name = columnParentName.getValue();
        String region_code = columnRegionCode.getValue();
        String region_name = columnRegionName.getValue();

        Region region = this.entity;

        region.code = region_code;
        region.name = region_name;
        region.level = getNextLevel(parentRegion);
        region.parent = parent_code;

        //region.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        //region.recentlyCreated = true;
        //region.recentlyCreatedUri = result.getFilename();
        //region.modules.addAll(StringCollectionConverter.getCollectionFrom(colModules.getValue()));

        boxRegions.put(region);

        //collectedData was read when creating an Edit FormUtil
        collectedData.formEntityName = region.name;
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
        Log.d("methodcall", ""+methodExpression);

        if (methodExpression.startsWith("generateRegionCode")){
            String parentCode = args[0];
            String regionName = args[1];

            Region parentRegion = boxRegions.query(Region_.code.equal(parentCode)).build().findFirst();
            String result = generateRegionCode(parentRegion, regionName);

            //always is necessary to specify the type of the return value (in this case is returning a string)
            return result==null ? null : "'" + result  + "'";
        }
        return null;
    }

    @Override
    public void collect() {
        executeCollectForm();
    }

    String getLowestRegionLevel() {
        List<ApplicationParam> params = boxAppParams.query().startsWith(ApplicationParam_.name, "hierarchy", QueryBuilder.StringOrder.CASE_SENSITIVE).build().find(); //COLUMN_NAME+" like 'hierarchy%'"
        String lowestLevel = null;
        int lowestLevelNumber = 0;

        for (ApplicationParam param : params) {

            if (!param.getValue().isEmpty()) {
                String value = param.getName();
                String strNum = value.replace("hierarchy", "");
                int num = Integer.parseInt(strNum);

                if (num > lowestLevelNumber) {
                    lowestLevelNumber = num;
                    lowestLevel = value;
                }
            }
        }

        return lowestLevel;
    }

    private String getNextLevel(Region region) {
        int levelIndex = Region.ALL_HIERARCHIES.indexOf(region.level);

        if (levelIndex == 9) return null; //out of boundaries of region level

        return Region.ALL_HIERARCHIES.get(levelIndex+1);
    }

    private String generateRegionCode(Region parentRegion, String regionName) {
        String lowest = getLowestRegionLevel();
        String householdLevel = getNextLevel(parentRegion);

        if (householdLevel == null) return null;

        if (lowest == householdLevel) {
            return codeGenerator.generateLowestRegionCode(parentRegion, regionName);
        }

        return codeGenerator.generateRegionCode(parentRegion, regionName);
    }

    private boolean isChildAtLowestLevel(Region parentRegion) {
        String lowest = getLowestRegionLevel();
        String householdLevel = getNextLevel(parentRegion);

        if (householdLevel != null && lowest == householdLevel) {
            return true;
        }

        return false;
    }

}
