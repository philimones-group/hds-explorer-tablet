package org.philimone.hds.explorer.main;

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
import org.philimone.hds.explorer.fragment.MemberFilterFragment;
import org.philimone.hds.explorer.fragment.MemberListFragment;
import org.philimone.hds.explorer.listeners.MemberActionListener;
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

public class SurveyMembersActivity extends AppCompatActivity implements MemberFilterFragment.Listener, MemberActionListener, BarcodeScannerActivity.InvokerClickListener {

    private MemberFilterFragment memberFilterFragment;
    private MemberListFragment memberListFragment;

    private User loggedUser;

    private LoadingDialog loadingDialog;

    private Map<String, BarcodeScannerActivity.ResultListener> barcodeResultListeners = new HashMap<>();

    private Box<Form> boxForms;
    private Box<Dataset> boxDatasets;
    private Box<Household> boxHouseholds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_members);

        this.loggedUser = Bootstrap.getCurrentUser();

        this.memberFilterFragment = (MemberFilterFragment) (getSupportFragmentManager().findFragmentById(R.id.memberFilterFragment));
        this.memberListFragment = (MemberListFragment) getSupportFragmentManager().findFragmentById(R.id.memberListFragment);

        initBoxes();
        initialize();
    }

    private void initBoxes() {
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        this.boxDatasets = ObjectBoxDatabase.get().boxFor(Dataset.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
    }

    private void initialize() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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

    private Household getHousehold(Member member){
        if (member == null || member.getHouseholdCode()==null) return null;

        Household household = Queries.getHouseholdByCode(this.boxHouseholds, member.getHouseholdCode());

        return household;
    }

    private void showLoadingDialog(String msg, boolean show){
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.hide();
        }
    }

    class MemberSearchTask extends AsyncTask<Void, Void, MemberAdapter> {
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
        protected MemberAdapter doInBackground(Void... params) {
            return memberListFragment.loadMembersByFilters(null, name, code, houseNr, gender, minAge, maxAge, dead, outmigrated, resident);
        }

        @Override
        protected void onPostExecute(MemberAdapter adapter) {
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

            Intent intent = new Intent(SurveyMembersActivity.this, MemberDetailsActivity.class);
            intent.putExtra("household", this.household);
            intent.putExtra("member", this.member);

            showLoadingDialog(null, false);

            startActivity(intent);
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

            Intent intent = new Intent(SurveyMembersActivity.this, HouseholdDetailsActivity.class);
            intent.putExtra("household", household);

            showLoadingDialog(null, false);

            startActivity(intent);
        }
    }
}
