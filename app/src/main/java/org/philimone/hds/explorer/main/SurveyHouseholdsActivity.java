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
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.HouseholdFilterFragment;
import org.philimone.hds.explorer.fragment.MemberListFragment;
import org.philimone.hds.explorer.io.xml.FormXmlReader;
import org.philimone.hds.explorer.listeners.MemberActionListener;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.Dataset;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.enums.Gender;
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
    private Box<Dataset> boxDatasets;
    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;

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
        this.boxDatasets = ObjectBoxDatabase.get().boxFor(Dataset.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
    }

    private void initialize() {
        this.memberListFragment.setButtonEnabled(hasMemberBoundForms(), NEW_MEMBER_COLLECT);

        this.memberListFragment.setHouseholdHeaderVisibility(true);
        this.memberListFragment.setCensusMode(censusMode);
        this.householdFilterFragment.setCensusMode(censusMode);

        if (censusMode){
            this.memberListFragment.setButtonVisibilityGone(MEMBERS_MAP, NEW_MEMBER_COLLECT);

        } else{
            this.memberListFragment.setButtonVisibilityGone(MEMBERS_MAP);
        }

        this.householdFilterFragment.setLoggedUser(loggedUser);

        this.householdFilterFragment.setBarcodeScannerListener(this);


        this.loadingDialog = new LoadingDialog(this);
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
        for (Dataset dataSet : getDataSets()){
            //Log.d("has-mapped-datasets", dataSet.getName()+", "+loader.hasMappedDatasetVariable(dataSet));
            if (loader.hasMappedDatasetVariable(dataSet)){
                //Log.d("hasMappedVariables", ""+dataSet.getName());
                loader.loadDataSetValues(dataSet, household, member, loggedUser, region);
            }
        }
    }

    private Set<Dataset> getLoadableDatasets(FormDataLoader[] loaders){
        Set<Dataset> list = new HashSet<>();
        List<Dataset> datasets = getDataSets();

        for (FormDataLoader loader : loaders)
        for (Dataset dataSet : datasets){
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

    private List<Dataset> getDataSets(){
        List<Dataset> list = this.boxDatasets.getAll();
        return list;
    }

    private Household getHousehold(String code){
        if (code == null) return null;

        Household household = Queries.getHouseholdByCode(this.boxHouseholds, code);

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
