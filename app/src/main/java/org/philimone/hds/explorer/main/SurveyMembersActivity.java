package org.philimone.hds.explorer.main;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.mapswithme.maps.api.MWMPoint;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.MemberArrayAdapter;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.MemberFilterFragment;
import org.philimone.hds.explorer.fragment.MemberListFragment;
import org.philimone.hds.explorer.listeners.MemberActionListener;
import org.philimone.hds.explorer.model.Dataset;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.widget.LoadingDialog;
import org.philimone.hds.explorer.widget.member_details.Distance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import mz.betainteractive.utilities.StringUtil;

import static org.philimone.hds.explorer.fragment.MemberListFragment.Buttons.ADD_NEW_MEMBER;
import static org.philimone.hds.explorer.fragment.MemberListFragment.Buttons.EDIT_MEMBER;

public class SurveyMembersActivity extends Activity implements MemberFilterFragment.Listener, MemberActionListener, BarcodeScannerActivity.InvokerClickListener {

    private MemberFilterFragment memberFilterFragment;
    private MemberListFragment memberListFragment;

    private User loggedUser;

    private LoadingDialog loadingDialog;

    private Map<String, BarcodeScannerActivity.ResultListener> barcodeResultListeners = new HashMap<>();

    private Box<Form> boxForms;
    private Box<Dataset> boxDatasets;
    private Box<Household> boxHouseholds;

    public enum FormFilter {
        REGION, HOUSEHOLD, HOUSEHOLD_HEAD, MEMBER, FOLLOW_UP
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_members);

        this.loggedUser = (User) getIntent().getExtras().get("user");

        this.memberFilterFragment = (MemberFilterFragment) (getFragmentManager().findFragmentById(R.id.memberFilterFragment));
        this.memberListFragment = (MemberListFragment) getFragmentManager().findFragmentById(R.id.memberListFragment);

        initBoxes();
        initialize();
    }

    private void initBoxes() {
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        this.boxDatasets = ObjectBoxDatabase.get().boxFor(Dataset.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
    }

    private void initialize() {
        this.memberListFragment.setButtonVisibilityGone(MemberListFragment.Buttons.CLOSEST_HOUSES, ADD_NEW_MEMBER, EDIT_MEMBER);
        this.memberListFragment.setButtonEnabled(hasMemberBoundForms(), MemberListFragment.Buttons.NEW_MEMBER_COLLECT);

        this.memberFilterFragment.setBarcodeScannerListener(this);

        this.loadingDialog = new LoadingDialog(this);
    }

    @Override
    public void onSearch(String name, String code, String houseNumber, String gender, Integer minAge, Integer maxAge, Boolean isDead, Boolean hasOutmigrated, Boolean liveResident) {
        this.memberListFragment.showProgress(true);

        MemberSearchTask task = new MemberSearchTask(name, code, houseNumber, gender, minAge, maxAge, isDead, hasOutmigrated, liveResident);
        task.execute();
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
        FormDataLoader[] dataLoaders = getFormLoaders(FormFilter.HOUSEHOLD_HEAD, FormFilter.MEMBER); //only members
        Household household = getHousehold(member);
        loadFormValues(dataLoaders, household, member, null);

        Intent intent = new Intent(this, GpsSearchedListActivity.class);
        intent.putExtra("main_member", member);
        intent.putExtra("main_household", household);
        intent.putExtra("distance", distance);
        intent.putExtra("members", members);
        intent.putExtra("points", points);
        intent.putExtra("points_original", originalPoints);

        intent.putExtra("dataloaders", dataLoaders);

        startActivity(intent);
    }

    @Override
    public void onClosestHouseholdsResult(Household household, Distance distance, MWMPoint[] points, ArrayList<Household> households) {

    }

    @Override
    public void onAddNewMember(Household household) {

    }

    @Override
    public void onEditMember(Household household, Member member) {

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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

    private Household getHousehold(Member member){
        if (member == null || member.getHouseholdCode()==null) return null;

        Household household = Queries.getHouseholdByCode(this.boxHouseholds, member.getHouseholdCode());

        return household;
    }

    public FormDataLoader[] getFormLoaders(FormFilter... filters){

        List<FormFilter> listFilters = Arrays.asList(filters);

        String[] userModules = loggedUser.getModules().split(",");

        List<Form> forms = this.boxForms.getAll(); //get all forms
        List<FormDataLoader> list = new ArrayList<>();

        int i=0;
        for (Form form : forms){
            String[] formModules = form.getModules().split(",");
            Log.d("forms", ""+loggedUser.getModules() +" - " + form.getModules() );
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

    private List<FormFilter> toList(FormFilter... filters){
        List<FormFilter> list = new ArrayList<>();
        for (FormFilter f : filters){
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
            if (loader.hasMappedDatasetVariable(dataSet)){
                //Log.d("hasMappedVariables", ""+dataSet.getName());
                loader.loadDataSetValues(dataSet, household, member, loggedUser, region);
            }
        }
    }

    private void loadFormValues(FormDataLoader[] loaders, Household household, Member member, Region region){
        for (FormDataLoader loader : loaders){
            loadFormValues(loader, household, member, region);
        }
    }

    private List<Dataset> getDataSets(){
        List<Dataset> list = this.boxDatasets.getAll();
        return list;
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
        private String houseNr;
        private Integer minAge;
        private Integer maxAge;
        private Boolean dead;
        private Boolean outmigrated;
        private Boolean resident;

        public MemberSearchTask(String name, String code, String houseNumber, String gender, Integer minAge, Integer maxAge, Boolean isDead, Boolean hasOutmigrated, Boolean liveResident) {
            this.name = name;
            this.code = code;
            this.houseNr = houseNumber;
            this.gender = gender;
            this.minAge = minAge;
            this.maxAge = maxAge;
            this.dead = isDead;
            this.outmigrated = hasOutmigrated;
            this.resident = liveResident;
        }

        @Override
        protected MemberArrayAdapter doInBackground(Void... params) {
            return memberListFragment.loadMembersByFilters(null, name, code, houseNr, gender, minAge, maxAge, dead, outmigrated, resident);
        }

        @Override
        protected void onPostExecute(MemberArrayAdapter adapter) {
            memberListFragment.setMemberAdapter(adapter);
            memberListFragment.showProgress(false);

            if (adapter.isEmpty()){
                memberListFragment.showMemberNotFoundMessage();
            }
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

            Intent intent = new Intent(SurveyMembersActivity.this, MemberDetailsActivity.class);
            intent.putExtra("user", loggedUser);
            intent.putExtra("member", this.member);
            intent.putExtra("dataloaders", dataLoaders);

            showLoadingDialog(null, false);

            startActivity(intent);
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

            Intent intent = new Intent(SurveyMembersActivity.this, HouseholdDetailsActivity.class);
            intent.putExtra("user", loggedUser);
            intent.putExtra("household", household);
            intent.putExtra("dataloaders", dataLoaders);

            showLoadingDialog(null, false);

            startActivity(intent);
        }
    }
}
