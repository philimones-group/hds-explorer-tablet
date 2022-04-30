package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.converters.StringCollectionConverter;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import java.util.Date;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.utilities.StringUtil;

public class HouseholdFormUtil extends FormUtil<Household> {

    private Box<Household> boxHouseholds;
    private Region region;

    public HouseholdFormUtil(Fragment fragment, Context context, Region region, FormUtilities odkFormUtilities, FormUtilListener<Household> listener){
        super(fragment, context, FormUtil.getHouseholdForm(context), odkFormUtilities, listener);

        this.region = region;

        initBoxes();
        initialize();
    }

    public HouseholdFormUtil(AppCompatActivity activity, Context context, Region region, FormUtilities odkFormUtilities, FormUtilListener<Household> listener){
        super(activity, context, FormUtil.getHouseholdForm(context), odkFormUtilities, listener);

        this.region = region;

        initBoxes();
        initialize();
    }

    @Override
    protected void initBoxes() {
        super.initBoxes();

        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);

    }

    @Override
    protected void preloadValues() {
        preloadedMap.put("regionCode", region.code);
        preloadedMap.put("regionName", region.name);
        preloadedMap.put("householdCode", codeGenerator.generateHouseholdCode(region, this.user));
        preloadedMap.put("modules", this.user.getSelectedModulesCodes());
    }

    @Override
    protected void preloadUpdatedValues() {
        //nothing to update yet
    }

    @Override
    public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {
        ColumnValue columnHouseholdCode = collectedValues.get("householdCode");
        ColumnValue columnHouseholdName = collectedValues.get("householdName");

        String household_code = columnHouseholdCode.getValue();
        String household_name = columnHouseholdName.getValue();
        //String household_gps = collectedValues.get("gps").getValue();

        if (StringUtil.isBlank(household_code)){
            String message = this.context.getString(R.string.new_household_code_empty_lbl);
            return new ValidationResult(columnHouseholdCode, message);
        }

        if (!codeGenerator.isHouseholdCodeValid(household_code)){
            String message = this.context.getString(R.string.new_household_code_err_lbl);
            //DialogFactory.createMessageInfo(HouseholdDetailsActivity.this, R.string.info_lbl, R.string.new_household_code_err_lbl).show();
            return new ValidationResult(columnHouseholdCode, message);
        }

        if (!household_code.startsWith(region.code)){
            String message = this.context.getString(R.string.new_household_code_region_err_lbl);
            //DialogFactory.createMessageInfo(HouseholdDetailsActivity.this, R.string.info_lbl, R.string.new_household_code_region_err_lbl).show();
            return new ValidationResult(columnHouseholdCode, message);
        }

        //check if household with code exists
        if (boxHouseholds.query().equal(Household_.code, household_code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst() != null){
            String message = this.context.getString(R.string.new_household_code_exists_lbl);
            //DialogFactory.createMessageInfo(HouseholdDetailsActivity.this, R.string.info_lbl, R.string.new_household_code_exists_lbl).show();
            return new ValidationResult(columnHouseholdCode, message);
        }

        if (StringUtil.isBlank(household_name)){
            String message = this.context.getString(R.string.new_household_name_empty_lbl);
            //DialogFactory.createMessageInfo(HouseholdDetailsActivity.this, R.string.info_lbl, R.string.new_household_code_empty_lbl).show();
            return new ValidationResult(columnHouseholdName, message);
        }

        return ValidationResult.noErrors();
    }

    @Override
    public void onFormFinished(HForm form, CollectedDataMap collectedValues, XmlFormResult result) {

        Log.d("resultxml", result.getXmlResult());

        if (currentMode == Mode.EDIT) {
            System.out.println("Editing Household Not implemented yet");
            assert 1==0;
        }

        //saveNewHousehold();
        ColumnValue colRegionCode = collectedValues.get("regionCode");
        ColumnValue colRegionName = collectedValues.get("regionName");
        ColumnValue colHouseholdCode = collectedValues.get("householdCode");
        ColumnValue colHouseholdName = collectedValues.get("householdName");
        ColumnValue colHeadCode = collectedValues.get("headCode");
        ColumnValue colHeadName = collectedValues.get("headName");
        ColumnValue colCollBy = collectedValues.get("collectedBy");
        ColumnValue colCollDate = collectedValues.get("collectedDate");
        ColumnValue colModules = collectedValues.get("modules");

        ColumnValue colGps = collectedValues.get("gps");
        Map<String, Double> gpsValues = colGps.getGpsValues();
        Double gpsLat = gpsValues.get("gpsLat");
        Double gpsLon = gpsValues.get("gpsLon");
        Double gpsAlt = gpsValues.get("gpsAlt");
        Double gpsAcc = gpsValues.get("gpsAcc");


        Household household = new Household();
        household.region = colRegionCode.getValue();
        household.code = colHouseholdCode.getValue();
        household.name = colHouseholdName.getValue();
        household.headCode = colHeadCode.getValue();
        household.headName = colHeadName.getValue();
        household.gpsLatitude = gpsLat;
        household.gpsLongitude = gpsLon;
        household.gpsAltitude = gpsAlt;
        household.gpsAccuracy = gpsAcc;
        household.updateGpsCalculations();
        household.collectedId = collectedValues.get(HForm.COLUMN_ID).getValue();
        household.recentlyCreated = true;
        household.recentlyCreatedUri = result.getFilename();
        household.modules.addAll(StringCollectionConverter.getCollectionFrom(colModules.getValue()));

        boxHouseholds.put(household);

        collectedData = new CoreCollectedData();
        //collectedData.visitId = visit.id; //will be updated when visit is created
        collectedData.formEntity = CoreFormEntity.HOUSEHOLD;
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
        collectExtensionForm(collectedValues);

    }

    @Override
    protected void onFinishedExtensionCollection() {
        if (listener != null) {
            listener.onNewEntityCreated(this.entity);
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
