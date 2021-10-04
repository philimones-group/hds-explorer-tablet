package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.converters.StringCollectionConverter;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.settings.generator.CodeGeneratorService;
import org.philimone.hds.forms.listeners.FormCollectionListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;
import org.philimone.hds.forms.parsers.ExcelFormParser;

import java.io.InputStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import androidx.fragment.app.FragmentManager;
import io.objectbox.Box;

public class HouseholdFormUtil implements FormCollectionListener {
    private FragmentManager fragmentManager;
    private Context context;

    private HForm form;
    private CodeGeneratorService codeGenerator;
    private Map<String, String> preloadedMap;

    private Box<ApplicationParam> boxAppParams;
    private Box<Household> boxHouseholds;
    private Box<CoreCollectedData> boxCoreCollectedData;

    private User loggedUser;
    private Region region;
    private boolean postExecution;
    private Listener listener;

    public HouseholdFormUtil(FragmentManager fragmentManager, Context context, Region region, Listener listener){
        this.fragmentManager = fragmentManager;
        this.context = context;

        this.region = region;

        this.preloadedMap = new LinkedHashMap<>();
        this.codeGenerator = new CodeGeneratorService();
        this.listener = listener;

        this.loggedUser = Bootstrap.getCurrentUser();

        initBoxes();
        initialize();
    }

    private void initBoxes() {
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
    }

    private void initialize(){
        InputStream inputStream = this.context.getResources().openRawResource(R.raw.household_form);
        this.form = new ExcelFormParser(inputStream).getForm();

        postExecution = Queries.getApplicationParamValue(this.boxAppParams, ApplicationParam.HFORM_POST_EXECUTION).equals("true");
    }

    private void preloadValues() {
        preloadedMap.put("regionCode", region.code);
        preloadedMap.put("regionName", region.name);
        preloadedMap.put("householdCode", codeGenerator.generateHouseholdCode(region, loggedUser));
        preloadedMap.put("modules", loggedUser.getSelectedModulesCodes());
    }

    @Override
    public ValidationResult onFormValidate(HForm form, Map<String, ColumnValue> collectedValues) {
        ColumnValue columnHouseholdCode = collectedValues.get("householdCode");
        ColumnValue columnHouseholdName = collectedValues.get("householdName");

        String household_code = columnHouseholdCode.getValue();
        String household_name = columnHouseholdName.getValue();
        //String household_gps = collectedValues.get("gps").getValue();

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
        if (boxHouseholds.query().equal(Household_.code, household_code).build().findFirst() != null){
            String message = this.context.getString(R.string.new_household_code_exists_lbl);
            //DialogFactory.createMessageInfo(HouseholdDetailsActivity.this, R.string.info_lbl, R.string.new_household_code_exists_lbl).show();
            return new ValidationResult(columnHouseholdCode, message);
        }

        if (household_name.isEmpty()){
            String message = this.context.getString(R.string.new_household_code_empty_lbl);
            //DialogFactory.createMessageInfo(HouseholdDetailsActivity.this, R.string.info_lbl, R.string.new_household_code_empty_lbl).show();
            return new ValidationResult(columnHouseholdName, message);
        }

        return ValidationResult.noErrors();
    }

    @Override
    public void onFormFinished(HForm form, Map<String, ColumnValue> collectedValues, XmlFormResult result) {

        Log.d("resultxml", result.getXmlResult());

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
        household.recentlyCreated = true;
        household.recentlyCreatedUri = result.getFilename();
        household.modules.addAll(StringCollectionConverter.getCollectionFrom(colModules.getValue()));

        long entityId = boxHouseholds.put(household);

        CoreCollectedData collectedData = new CoreCollectedData();
        //collectedData.visitId = visit.id; //will be updated when visit is created
        collectedData.formEntity = CoreFormEntity.HOUSEHOLD;
        collectedData.formEntityId = entityId;
        collectedData.formEntityCode = household.code;
        collectedData.formEntityName = household.name;
        collectedData.formUuid = result.getFormUuid();
        collectedData.formFilename = result.getFilename();
        collectedData.createdDate = new Date();

        boxCoreCollectedData.put(collectedData);


        if (listener != null) {
            listener.onNewHouseholdCreated(household);
        }

    }

    @Override
    public void onFormCancelled(){
        if (listener != null) {
            listener.onFormCancelled();
        }
    }

    public void collect() {

        preloadValues();

        FormFragment form = FormFragment.newInstance(this.fragmentManager, this.form, Bootstrap.getInstancesPath(), loggedUser.username, preloadedMap, postExecution , this);
        form.startCollecting();
    }

    public interface Listener {
        void onNewHouseholdCreated(Household household);

        void onHouseholdEdited(Household household);

        void onFormCancelled();
    }

}
