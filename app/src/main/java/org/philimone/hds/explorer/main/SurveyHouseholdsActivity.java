package org.philimone.hds.explorer.main;

import static org.philimone.hds.explorer.fragment.MemberListFragment.Buttons.MEMBERS_MAP;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.MemberAdapter;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.HouseholdFilterFragment;
import org.philimone.hds.explorer.fragment.MemberListFragment;
import org.philimone.hds.explorer.listeners.MemberActionListener;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.Dataset;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.settings.RequestCodes;
import org.philimone.hds.explorer.widget.LoadingDialog;

import java.util.HashMap;
import java.util.Map;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

public class SurveyHouseholdsActivity extends AppCompatActivity implements HouseholdFilterFragment.Listener, MemberActionListener, BarcodeScannerActivity.InvokerClickListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_households);

        this.loggedUser = Bootstrap.getCurrentUser();
        //this.censusMode = getIntent().getExtras().getBoolean("censusMode");

        this.householdFilterFragment = (HouseholdFilterFragment) (getSupportFragmentManager().findFragmentById(R.id.householdFilterFragment));
        this.memberListFragment = (MemberListFragment) getSupportFragmentManager().findFragmentById(R.id.memberListFragment);

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

        this.memberListFragment.setHouseholdHeaderVisibility(true);
        this.memberListFragment.setCensusMode(censusMode);
        this.householdFilterFragment.setCensusMode(censusMode);

        if (censusMode){

        } else{
            this.memberListFragment.setButtonVisibilityGone(MEMBERS_MAP);
        }

        this.householdFilterFragment.setLoggedUser(loggedUser);

        this.householdFilterFragment.setBarcodeScannerListener(this);


        this.loadingDialog = new LoadingDialog(this);
    }

    @Override
    public void onHouseholdClick(Household household) {
        memberListFragment.showProgress(true);
        MemberSearchTask task = new MemberSearchTask(household);
        task.execute();

    }

    @Override
    public void onSelectedRegion(Region region) {

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

        startActivityForResult(intent, RequestCodes.SCAN_BARCODE);
    }

    private Household getHousehold(String code){
        if (code == null) return null;

        Household household = Queries.getHouseholdByCode(this.boxHouseholds, code);

        return household;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.MEMBER_DETAILS_FROM_SURVEY_HOUSEHOLDS){ //if it is a return from MemberDetails - check if it was a new individual and thn update the list members

            if (data != null && data.getExtras() != null){
                String houseCode = data.getExtras().getString("household_code");
                Boolean isNewTempMember = data.getExtras().getBoolean("is_new_temp_member");

                if (isNewTempMember != null && isNewTempMember==true){
                    Household household = getHousehold(houseCode);
                    MemberSearchTask task = new MemberSearchTask(household);
                    task.execute();
                }
                //Log.d("request code data1", ""+data.getExtras().getString("household_code"));
                //Log.d("request code data2", ""+data.getExtras().getBoolean("is_new_member"));
            }

            return;
        }

        if (requestCode == RequestCodes.SCAN_BARCODE && resultCode == RESULT_OK){
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
        CollectedData collectedData = this.boxCollectedData.query().equal(CollectedData_.formId, formId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                           .and().equal(CollectedData_.recordId, recordId)
                                                           .and().equal(CollectedData_.recordEntity, tableName, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        return collectedData;
    }

    private void showLoadingDialog(String msg, boolean show){
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.dismiss();
        }
    }

    class MemberSearchTask extends AsyncTask<Void, Void, MemberAdapter> {
        private String name;
        private String code;
        private String gender;
        private String houseCode;
        private Household household;

        public MemberSearchTask(Household household) {
            this.name = name;
            this.code = code;
            this.gender = gender;
            this.houseCode = houseCode;
            this.household = household;
        }

        @Override
        protected MemberAdapter doInBackground(Void... params) {
            return memberListFragment.loadResidentsByHousehold(household);
        }

        @Override
        protected void onPostExecute(MemberAdapter adapter) {

            adapter.setShowExtraDetails(true);
            adapter.setShowHouseholdAndCode(false);
            adapter.setShowMemberDetails(true);

            memberListFragment.setCurrentHouseld(household);
            memberListFragment.setMemberAdapter(adapter);
            memberListFragment.showProgress(false);
            memberListFragment.setButtonEnabled(true, MemberListFragment.Buttons.SHOW_HOUSEHOLD);

            //showLoadingDialog("", false);
        }
    }

    class OnMemberSelectedTask  extends AsyncTask<Void, Void, Void> {
        private Household household;
        private Member member;
        private Region region;

        public OnMemberSelectedTask(Household household, Member member, Region region) {
            this.household = household;
            this.member = member;
            this.region = region;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = new Intent(SurveyHouseholdsActivity.this, MemberDetailsActivity.class);
            intent.putExtra("household", this.household.id);
            intent.putExtra("member", this.member.id);

            showLoadingDialog(null, false);

            startActivityForResult(intent, RequestCodes.MEMBER_DETAILS_FROM_SURVEY_HOUSEHOLDS);
        }
    }

    class ShowHouseholdTask extends AsyncTask<Void, Void, Void> {
        private Household household;
        private Member member;
        private Region region;

        public ShowHouseholdTask(Household household, Member member, Region region) {
            this.household = household;
            this.member = member;
            this.region = region;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Intent intent = new Intent(SurveyHouseholdsActivity.this, HouseholdDetailsActivity.class);
            intent.putExtra("household", household.id);

            showLoadingDialog(null, false);

            startActivity(intent);
        }
    }

    class ShowRegionTask extends AsyncTask<Void, Void, Void> {
        private Region region;

        public ShowRegionTask(Region region) {
            this.region = region;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            showLoadingDialog(null, false);

            Intent intent = new Intent(SurveyHouseholdsActivity.this, RegionDetailsActivity.class);
            intent.putExtra("region", region.id);

            startActivity(intent);
        }
    }

}
