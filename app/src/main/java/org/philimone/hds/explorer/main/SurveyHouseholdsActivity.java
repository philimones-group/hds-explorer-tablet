package org.philimone.hds.explorer.main;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.mapswithme.maps.api.MWMPoint;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.MemberArrayAdapter;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.Database;
import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.HouseholdFilterFragment;
import org.philimone.hds.explorer.fragment.MemberListFragment;
import org.philimone.hds.explorer.io.xml.FormXmlReader;
import org.philimone.hds.explorer.listeners.MemberActionListener;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.DataSet;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.LoadingDialog;
import org.philimone.hds.explorer.widget.member_details.Distance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.objectbox.Box;
import mz.betainteractive.odk.task.OdkGeneratedFormLoadTask;
import mz.betainteractive.odk.xml.FormUpdater;
import mz.betainteractive.utilities.StringUtil;

import static org.philimone.hds.explorer.fragment.MemberListFragment.Buttons.ADD_NEW_MEMBER;
import static org.philimone.hds.explorer.fragment.MemberListFragment.Buttons.CLOSEST_HOUSES;
import static org.philimone.hds.explorer.fragment.MemberListFragment.Buttons.CLOSEST_MEMBERS;
import static org.philimone.hds.explorer.fragment.MemberListFragment.Buttons.EDIT_MEMBER;
import static org.philimone.hds.explorer.fragment.MemberListFragment.Buttons.MEMBERS_MAP;
import static org.philimone.hds.explorer.fragment.MemberListFragment.Buttons.NEW_MEMBER_COLLECT;

public class SurveyHouseholdsActivity extends Activity implements HouseholdFilterFragment.Listener, MemberActionListener, BarcodeScannerActivity.InvokerClickListener {

    private HouseholdFilterFragment householdFilterFragment;
    private MemberListFragment memberListFragment;

    private Map<String, BarcodeScannerActivity.ResultListener> barcodeResultListeners = new HashMap<>();

    private User loggedUser;
    private boolean censusMode = true;

    private LoadingDialog loadingDialog;

    private Box<CollectedData> boxCollectedData;
    private Box<Form> boxForms;

    private final int MEMBER_DETAILS_REQUEST_CODE = 31;

    public enum FormFilter {
        REGION, HOUSEHOLD, HOUSEHOLD_HEAD, MEMBER, FOLLOW_UP
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_households);

        this.loggedUser = (User) getIntent().getExtras().get("user");
        //this.censusMode = getIntent().getExtras().getBoolean("censusMode");

        this.householdFilterFragment = (HouseholdFilterFragment) (getFragmentManager().findFragmentById(R.id.householdFilterFragment));
        this.memberListFragment = (MemberListFragment) getFragmentManager().findFragmentById(R.id.memberListFragment);

        initBoxes();
        initialize();
    }

    private void initBoxes() {
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
    }

    private void initialize() {
        this.memberListFragment.setButtonVisibilityGone(CLOSEST_MEMBERS);
        this.memberListFragment.setButtonEnabled(hasMemberBoundForms(), NEW_MEMBER_COLLECT);

        this.memberListFragment.setHouseholdHeaderVisibility(true);
        this.memberListFragment.setCensusMode(censusMode);
        this.householdFilterFragment.setCensusMode(censusMode);

        if (censusMode){
            this.memberListFragment.setButtonVisibilityGone(MEMBERS_MAP, CLOSEST_MEMBERS, NEW_MEMBER_COLLECT);
            this.memberListFragment.setButtonEnabled(false, ADD_NEW_MEMBER, EDIT_MEMBER, CLOSEST_HOUSES);

        } else{
            this.memberListFragment.setButtonVisibilityGone(ADD_NEW_MEMBER, EDIT_MEMBER, MEMBERS_MAP, CLOSEST_MEMBERS);
        }

        this.householdFilterFragment.setLoggedUser(loggedUser);

        this.householdFilterFragment.setBarcodeScannerListener(this);


        this.loadingDialog = new LoadingDialog(this);
    }

    @Override
    public void onAddNewHousehold(Region region) {
        FormDataLoader[] dataLoaders = getFormLoaders();

        Intent intent = new Intent(this, HouseholdDetailsActivity.class);
        intent.putExtra("user", loggedUser);
        intent.putExtra("region", region);
        intent.putExtra("dataloaders", dataLoaders);
        intent.putExtra("request_code", HouseholdDetailsActivity.REQUEST_CODE_NEW_HOUSEHOLD);

        startActivityForResult(intent, HouseholdDetailsActivity.REQUEST_CODE_NEW_HOUSEHOLD);
    }

    @Override
    public void onEditHousehold(Household household) {
        FormDataLoader[] dataLoaders = getFormLoaders();

        Intent intent = new Intent(this, HouseholdDetailsActivity.class);
        intent.putExtra("user", loggedUser);
        intent.putExtra("dataloaders", dataLoaders);
        intent.putExtra("household", household);
        intent.putExtra("request_code", HouseholdDetailsActivity.REQUEST_CODE_EDIT_HOUSEHOLD);

        startActivityForResult(intent, HouseholdDetailsActivity.REQUEST_CODE_EDIT_HOUSEHOLD);
    }

    @Override
    public void onAddNewMember(Household household) {
        FormDataLoader[] dataLoaders = getFormLoaders();

        Intent intent = new Intent(this, MemberDetailsActivity.class);
        intent.putExtra("user", loggedUser);
        intent.putExtra("household", household);
        intent.putExtra("dataloaders", dataLoaders);
        intent.putExtra("request_code", MemberDetailsActivity.REQUEST_CODE_ADD_NEW_MEMBER);

        startActivityForResult(intent, MemberDetailsActivity.REQUEST_CODE_ADD_NEW_MEMBER);
    }

    @Override
    public void onEditMember(Household household, Member member) {
        FormDataLoader[] dataLoaders = getFormLoaders();

        Intent intent = new Intent(this, MemberDetailsActivity.class);
        intent.putExtra("user", loggedUser);
        intent.putExtra("household", household);
        intent.putExtra("member", member);
        intent.putExtra("dataloaders", dataLoaders);
        intent.putExtra("request_code", MemberDetailsActivity.REQUEST_CODE_EDIT_NEW_MEMBER);

        startActivityForResult(intent, MemberDetailsActivity.REQUEST_CODE_EDIT_NEW_MEMBER);
    }

    @Override
    public void onHouseholdClick(Household household) {
        MemberSearchTask task = new MemberSearchTask(household, null, null, null, household.getCode());
        task.execute();
    }

    @Override
    public void onSelectedRegion(Region region) {
        FormDataLoader[] dataLoaders = getFormLoaders(FormFilter.REGION);
        this.householdFilterFragment.checkSupportForRegionForms(dataLoaders);
    }

    @Override
    public void onShowRegionDetailsClicked(Region region) {
        ShowRegionTask task = new ShowRegionTask(region);
        task.execute();

        showLoadingDialog(getString(R.string.loading_dialog_region_details_lbl), true);
    }

    @Override
    public void onMemberSelected(Household household, Member member, Region region) {
        OnMemberSelectedTask task = new OnMemberSelectedTask(household, member, region);
        task.execute();

        showLoadingDialog(getString(R.string.loading_dialog_member_details_lbl), true);
    }

    @Override
    public void onShowHouseholdClicked(Household household, Member member, Region region) {
        ShowHouseholdTask task = new ShowHouseholdTask(household, member, region);
        task.execute();

        showLoadingDialog(getString(R.string.loading_dialog_household_details_lbl), true);
    }

    @Override
    public void onClosestMembersResult(Member member, Distance distance, MWMPoint[] points, MWMPoint[] originalPoints, ArrayList<Member> members) {

    }

    @Override
    public void onClosestHouseholdsResult(Household household, Distance distance, MWMPoint[] points, ArrayList<Household> households) {
        FormDataLoader[] dataLoaders = getFormLoaders(FormFilter.HOUSEHOLD);
        loadFormValues(dataLoaders, household, null, null);

        Intent intent = new Intent(this, GpsSearchedListActivity.class);

        intent.putExtra("main_household", household);
        intent.putExtra("distance", distance);
        intent.putExtra("households", households);
        intent.putExtra("points", points);

        intent.putExtra("dataloaders", dataLoaders);

        startActivity(intent);
    }

    @Override
    public void onBarcodeScannerClicked(int txtResId, String labelText, BarcodeScannerActivity.ResultListener resultListener) {
        Intent intent = new Intent(this, BarcodeScannerActivity.class);

        String resultHashCode = resultListener.hashCode()+"";

        intent.putExtra("text_box_res_id", txtResId);
        intent.putExtra("text_box_label", labelText);
        intent.putExtra("result_listener_code", resultHashCode);

        Log.d("res listener", ""+resultListener);

        barcodeResultListeners.put(resultHashCode, resultListener);

        Log.d("res listener size", ""+barcodeResultListeners.size());

        startActivityForResult(intent, BarcodeScannerActivity.SCAN_BARCODE_REQUEST_CODE);
    }

    public FormDataLoader[] getFormLoaders(FormFilter... filters){

        List<FormFilter> listFilters = Arrays.asList(filters);

        String[] userModules = loggedUser.getModules().split(",");

        List<Form> forms = this.boxForms.getAll(); //get all forms
        List<FormDataLoader> list = new ArrayList<>();

        int i=0;
        for (Form form : forms){
            String[] formModules = form.getModules().split(",");
            Log.d("dl", "fm="+form.getModules()+", um"+loggedUser.getModules());
            if (StringUtil.containsAny(userModules, formModules)){ //if the user has access to module specified on Form
                FormDataLoader loader = new FormDataLoader(form);

                if (form.isFollowUpOnly() && listFilters.contains(FormFilter.FOLLOW_UP)){
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

    private List<SurveyMembersActivity.FormFilter> toList(SurveyMembersActivity.FormFilter... filters){
        List<SurveyMembersActivity.FormFilter> list = new ArrayList<>();
        for (SurveyMembersActivity.FormFilter f : filters){
            list.add(f);
        }
        return list;
    }

    private boolean hasMemberBoundForms(){
        for (FormDataLoader fdls : getFormLoaders(FormFilter.HOUSEHOLD_HEAD, FormFilter.MEMBER)){
            if (fdls.getForm().isMemberForm()){
                return true;
            }
        }
        return false;
    }

    private boolean hasHouseholdBoundForms(){
        for (FormDataLoader fdls : getFormLoaders(FormFilter.HOUSEHOLD)){
            if (fdls.getForm().isHouseholdForm()){
                return true;
            }
        }
        return false;
    }

    private boolean hasRegionBoundForms(){
        for (FormDataLoader fdls : getFormLoaders(FormFilter.REGION)){
            if (fdls.getForm().isRegionForm()){
                return true;
            }
        }
        return false;
    }

    private void loadFormValues(FormDataLoader loader, Household household, Member member, Region region){
        if (household != null){
            loader.loadHouseholdValues(household);
        }
        if (member != null){
            loader.loadMemberValues(member);
        }
        if (loggedUser != null){
            loader.loadUserValues(loggedUser);
        }
        if (region != null){
            loader.loadRegionValues(region);
        }

        loader.loadConstantValues();
        loader.loadSpecialConstantValues(household, member, loggedUser, region, null);

        //Load variables on datasets
        for (DataSet dataSet : getDataSets()){
            //Log.d("has-mapped-datasets", dataSet.getName()+", "+loader.hasMappedDatasetVariable(dataSet));
            if (loader.hasMappedDatasetVariable(dataSet)){
                //Log.d("hasMappedVariables", ""+dataSet.getName());
                loader.loadDataSetValues(dataSet, household, member, loggedUser, region);
            }
        }
    }

    private Set<DataSet> getLoadableDatasets(FormDataLoader[] loaders){
        Set<DataSet> list = new HashSet<>();
        List<DataSet> dataSets = getDataSets();

        for (FormDataLoader loader : loaders)
        for (DataSet dataSet : dataSets){
            if (loader.hasMappedDatasetVariable(dataSet)){
                list.add(dataSet);
            }
        }
        return list;
    }

    private void loadFormValues(FormDataLoader[] loaders, Household household, Member member, Region region){

        //get loadable datasets - to prevent reading unnecessary data
        //Set<DataSet> dataSets = getLoadableDatasets(loaders);

        for (FormDataLoader loader : loaders){
            loadFormValues(loader, household, member, region);
        }
    }

    private List<DataSet> getDataSets(){
        List<DataSet> list = null;

        Database db = new Database(this);
        db.open();
        list = Queries.getAllDataSetBy(db, null, null);
        db.close();

        return list;
    }

    private Household getHousehold(String code){
        if (code == null) return null;

        Database database = new Database(this);
        database.open();
        Household household = Queries.getHouseholdBy(database, DatabaseHelper.Household.COLUMN_CODE+"=?", new String[]{ code });
        database.close();

        return household;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MEMBER_DETAILS_REQUEST_CODE){ //if it is a return from MemberDetails - check if it was a new individual and thn update the list members

            if (data != null && data.getExtras() != null){
                String houseCode = data.getExtras().getString("household_code");
                Boolean isNewTempMember = data.getExtras().getBoolean("is_new_temp_member");

                if (isNewTempMember != null && isNewTempMember==true){
                    Household household = getHousehold(houseCode);
                    MemberSearchTask task = new MemberSearchTask(household, null, null, null, houseCode);
                    task.execute();
                }
                //Log.d("request code data1", ""+data.getExtras().getString("household_code"));
                //Log.d("request code data2", ""+data.getExtras().getBoolean("is_new_member"));
            }

            return;
        }

        if ((requestCode == HouseholdDetailsActivity.REQUEST_CODE_NEW_HOUSEHOLD || requestCode == HouseholdDetailsActivity.REQUEST_CODE_EDIT_HOUSEHOLD) && resultCode==RESULT_OK){ //If result is CANCELED just do nothing
            //Household added

            Household household = (Household) data.getExtras().get("household");

            if (household != null){
                //update Household table and class from XML
                updateHouseholdFromXML(household);

                householdFilterFragment.searchHouses(household.getCode());
                onHouseholdClick(household);
            }
        }

        if (requestCode == MemberDetailsActivity.REQUEST_CODE_ADD_NEW_MEMBER && resultCode==RESULT_OK){
            Household household = (Household) data.getExtras().get("household");
            Member member = (Member) data.getExtras().get("member");

            updateMemberFromXML(household, member);

            if (household != null){

                //update head of household
                if (household.isRecentlyCreated() && (household.getHeadCode() == null || household.getHeadCode().isEmpty())){
                    updateHouseholdHead(household, member);
                }

                Log.d("household-new-mem", ""+household);
                householdFilterFragment.searchHouses(household.getCode());
                onHouseholdClick(household);
            }
        }

        if (requestCode == MemberDetailsActivity.REQUEST_CODE_EDIT_NEW_MEMBER && resultCode == RESULT_OK){
            Household household = (Household) data.getExtras().get("household");
            Member member = (Member) data.getExtras().get("member");

            //update name, gender and dob on Member domain
            //update head name if the member is household head

            updateMemberFromXML(household, member);

            householdFilterFragment.searchHouses(household.getCode());
            onHouseholdClick(household);
        }

        if (requestCode == BarcodeScannerActivity.SCAN_BARCODE_REQUEST_CODE && resultCode == RESULT_OK){
            //send result back to the invoker listener

            int txtResId = data.getExtras().getInt("text_box_res_id");
            String txtLabel = data.getExtras().getString("text_box_label");
            String barcode = data.getExtras().getString("scanned_barcode");
            String resultListenerCode = data.getExtras().getString("result_listener_code");

            Log.d("returning with barcode", ""+barcode+", listener="+resultListenerCode);
            Log.d("contains listener", ""+barcodeResultListeners.containsKey(resultListenerCode));
            Log.d("listeners", ""+barcodeResultListeners);

            if (barcodeResultListeners.containsKey(resultListenerCode)){
                barcodeResultListeners.get(resultListenerCode).onBarcodeScanned(txtResId, txtLabel, barcode);
            }

        }
    }

    private void updateHouseholdFromXML(Household household){
        CollectedData cdata = getCollectedData("census_household", household.getId(), household.getTableName());
        String xmlFilePath = cdata.getFormXmlPath();

        //get content values from xml
        FormXmlReader xmlReader = new FormXmlReader();
        Map<String, String> mapValues = xmlReader.getXmlContentValues(xmlFilePath);

        //update Household Name and Household Head to the domains
        String houseName = mapValues.get("household_name");
        String headCode = mapValues.get("head_code");
        String headName = mapValues.get("head_name");

        //Log.d("saving-xml", "hhname:"+houseName+", hdcode:"+headCode+", hdname:"+headName);

        //put update content on cv
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.Household.COLUMN_NAME, houseName);
        cv.put(DatabaseHelper.Household.COLUMN_HEAD_CODE, headCode);
        cv.put(DatabaseHelper.Household.COLUMN_HEAD_NAME, headName);

        ContentValues cvh = new ContentValues();
        cvh.put(DatabaseHelper.Member.COLUMN_HOUSEHOLD_NAME, houseName);

        //Execute db update
        Database db = new Database(this);
        db.open();
        db.update(Household.class, cv, DatabaseHelper.Household._ID+"=?", new String[]{ household.getId()+"" } );
        //Update all members of the household
        db.update(Member.class, cvh, DatabaseHelper.Member.COLUMN_HOUSEHOLD_CODE +"=?", new String[]{ household.getCode()+"" } );
        db.close();

        Toast.makeText(this, getString(R.string.new_member_dialog_household_head_updated_lbl), Toast.LENGTH_SHORT);
    }

    private void updateMemberFromXML(Household household, Member member){

        CollectedData cdata = getCollectedData("census_member", member.getId(), member.getTableName());
        String xmlFilePath = cdata.getFormXmlPath();

        //get content values from xml
        FormXmlReader xmlReader = new FormXmlReader();
        Map<String, String> mapValues = xmlReader.getXmlContentValues(xmlFilePath);

        //Get xml values
        String memberName = mapValues.get("name");
        String memberGend = mapValues.get("gender");
        String memberDob = mapValues.get("dob");

        Log.d("member-name", memberName);
        Log.d("member-dob", memberDob);

        //update name, gender and dob on Member domain
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.Member.COLUMN_NAME, memberName);
        cv.put(DatabaseHelper.Member.COLUMN_GENDER, memberGend);
        cv.put(DatabaseHelper.Member.COLUMN_DOB, memberDob);

        //update head name if the member is household head
        ContentValues cvh = new ContentValues();
        cvh.put(DatabaseHelper.Household.COLUMN_HEAD_NAME, memberName);

        member.setName(memberName);
        member.setGender(memberName);
        member.setDob(memberDob);
        household.setHeadName(memberName);

        //Execute db update
        Database db = new Database(this);
        db.open();
        db.update(Member.class, cv, DatabaseHelper.Member._ID+"=?", new String[]{ member.getId()+"" } );

        Log.d("heading", "head-code="+household.getHeadCode()+", member.code="+member.getCode());

        if (household.getHeadCode().equals(member.getCode())){ //is household head
            Log.d("heading-2", "head-code="+household.getHeadCode()+", member.code="+member.getCode());
            db.update(Household.class, cvh, DatabaseHelper.Household._ID+"=?", new String[]{ household.getId()+"" } );
        }

        db.close();
    }

    private void updateHouseholdHead(Household household, Member headMember) {
        //get household collected data

        ContentValues cv = new ContentValues();
        cv.put("head_code", headMember.getCode());
        cv.put("head_name", headMember.getName());

        ContentValues cvDomain = new ContentValues();
        cvDomain.put(DatabaseHelper.Household.COLUMN_HEAD_CODE, headMember.getCode());
        cvDomain.put(DatabaseHelper.Household.COLUMN_HEAD_NAME, headMember.getName());

        Database database = new Database(this);
        database.open();

        //get collected data
        CollectedData collectedData = this.boxCollectedData.query().equal(CollectedData_.formId, "census_household")
                                                           .and().equal(CollectedData_.recordId, household.getId())
                                                           .and().equal(CollectedData_.tableName, household.getTableName()).build().findFirst();

        //update the household domain table
        household.setHeadCode(headMember.getCode());
        household.setHeadName(headMember.getName());
        database.update(Household.class, cvDomain, DatabaseHelper.Household._ID+"=?", new String[]{ household.getId()+"" } );

        database.close();

        if (collectedData != null){ //update the xml form
            String uriString = collectedData.getFormUri();
            Uri contentUri = Uri.parse(uriString);

            OdkGeneratedFormLoadTask formLoadTask = new OdkGeneratedFormLoadTask(this, contentUri, null);
            String xmlFilePath = formLoadTask.getXmlFilePath();

            Log.d("xmlPath", ""+xmlFilePath);

            FormUpdater formUpdater = new FormUpdater(xmlFilePath, cv);
            formUpdater.update();

            DialogFactory.createMessageInfo(this, R.string.info_lbl, R.string.new_member_dialog_household_head_updated_lbl).show();
        }


    }

    private CollectedData getCollectedData(String formId, long recordId, String tableName){
        //get collected data
        CollectedData collectedData = this.boxCollectedData.query().equal(CollectedData_.formId, formId)
                                                           .and().equal(CollectedData_.recordId, recordId)
                                                           .and().equal(CollectedData_.tableName, tableName).build().findFirst();

        return collectedData;
    }

    private void showLoadingDialog(String msg, boolean show){
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.hide();
        }
    }

    class MemberSearchTask extends AsyncTask<Void, Void, MemberArrayAdapter> {
        private String name;
        private String code;
        private String gender;
        private String houseCode;
        private Household household;

        public MemberSearchTask(Household household, String name, String code, String gender, String houseCode) {
            this.name = name;
            this.code = code;
            this.gender = gender;
            this.houseCode = houseCode;
            this.household = household;
        }

        @Override
        protected MemberArrayAdapter doInBackground(Void... params) {
            return memberListFragment.loadMembersByFilters(household, name, code, houseCode, gender, null, null, null, null, null);
        }

        @Override
        protected void onPostExecute(MemberArrayAdapter adapter) {
            memberListFragment.setCurrentHouseld(household);
            memberListFragment.setMemberAdapter(adapter);
            memberListFragment.showProgress(false);
            memberListFragment.setButtonEnabled(true, MemberListFragment.Buttons.SHOW_HOUSEHOLD);
        }
    }

    class OnMemberSelectedTask  extends AsyncTask<Void, Void, Void> {
        private Household household;
        private Member member;
        private Region region;
        private FormDataLoader[] dataLoaders;

        public OnMemberSelectedTask(Household household, Member member, Region region) {
            this.household = household;
            this.member = member;
            this.region = region;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            dataLoaders = getFormLoaders(FormFilter.HOUSEHOLD_HEAD, FormFilter.MEMBER);
            loadFormValues(dataLoaders, household, member, region);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = new Intent(SurveyHouseholdsActivity.this, MemberDetailsActivity.class);
            intent.putExtra("user", loggedUser);
            intent.putExtra("member", this.member);
            intent.putExtra("dataloaders", dataLoaders);

            showLoadingDialog(null, false);

            startActivityForResult(intent, MEMBER_DETAILS_REQUEST_CODE);
        }
    }

    class ShowHouseholdTask extends AsyncTask<Void, Void, Void> {
        private Household household;
        private Member member;
        private Region region;
        private FormDataLoader[] dataLoaders;

        public ShowHouseholdTask(Household household, Member member, Region region) {
            this.household = household;
            this.member = member;
            this.region = region;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            this.dataLoaders = getFormLoaders(FormFilter.HOUSEHOLD);
            loadFormValues(dataLoaders, household, member, region);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = new Intent(SurveyHouseholdsActivity.this, HouseholdDetailsActivity.class);
            intent.putExtra("user", loggedUser);
            intent.putExtra("household", household);
            intent.putExtra("dataloaders", dataLoaders);

            showLoadingDialog(null, false);

            startActivity(intent);
        }
    }

    class ShowRegionTask extends AsyncTask<Void, Void, Void> {
        private Region region;
        private FormDataLoader[] dataLoaders;

        public ShowRegionTask(Region region) {
            this.region = region;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            this.dataLoaders = getFormLoaders(FormFilter.REGION);
            loadFormValues(dataLoaders, null, null, region);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            showLoadingDialog(null, false);

            Intent intent = new Intent(SurveyHouseholdsActivity.this, RegionDetailsActivity.class);
            intent.putExtra("user", loggedUser);
            intent.putExtra("region", region);
            intent.putExtra("dataloaders", dataLoaders);

            startActivity(intent);
        }
    }

}
