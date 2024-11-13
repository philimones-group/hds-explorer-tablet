package org.philimone.hds.explorer.fragment.region.details;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.HouseholdAdapter;
import org.philimone.hds.explorer.adapter.RegionAdapter;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.main.HouseholdDetailsActivity;
import org.philimone.hds.explorer.main.RegionDetailsActivity;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.Dataset;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.widget.LoadingDialog;
import org.philimone.hds.explorer.widget.RecyclerListView;

import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.StringUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegionChildsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegionChildsFragment extends Fragment {

    private RecyclerListView lvRegionChilds;
    private LoadingDialog loadingDialog;

    private Region region;
    private User loggedUser;

    private String lastRegionLevelName;
    private boolean isLatestRegionLevel;

    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Form> boxForms;
    private Box<ApplicationParam> boxAppParams;
    private Box<Dataset> boxDatasets;

    public RegionChildsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HouseholdMembersFragment.
     */
    public static RegionChildsFragment newInstance(Region region, User user) {
        RegionChildsFragment fragment = new RegionChildsFragment();
        fragment.region = region;
        fragment.loggedUser = user;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.region_details_childs, container, false);

        initBoxes();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialize(view);
    }

    private void initialize(View view) {
        lvRegionChilds = view.findViewById(R.id.lvRegionChilds);

        lvRegionChilds.addOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long id) {
                onChildClicked(position);
            }

            @Override
            public void onItemLongClick(View view, int position, long id) {

            }
        });

        this.loadingDialog = new LoadingDialog(this.getContext());

        this.lastRegionLevelName = getLastRegionLevel();

        this.isLatestRegionLevel = this.region.level.equalsIgnoreCase(this.lastRegionLevelName);

        this.showRegionChilds();
    }

    private void initBoxes() {
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxDatasets = ObjectBoxDatabase.get().boxFor(Dataset.class);
    }

    private void onChildClicked(int position) {

        if (this.lvRegionChilds.getAdapter() instanceof HouseholdAdapter) {
            HouseholdAdapter adapter = (HouseholdAdapter) this.lvRegionChilds.getAdapter();
            Household household = adapter.getItem(position);

            openHouseholdDetails(household);
        } else if (this.lvRegionChilds.getAdapter() instanceof RegionAdapter) {
            RegionAdapter adapter = (RegionAdapter) this.lvRegionChilds.getAdapter();
            Region region = adapter.getItem(position);

            openRegionDetails(region);
        }
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    private Region getRegion(String code){

        Region region = this.boxRegions.query().equal(Region_.code, code, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        return region;
    }

    private int getHierarchyNumber(String hierarchyName) {
        if (StringUtil.isBlank(hierarchyName)) return 0;

        try {
            return Integer.parseInt(hierarchyName.replace("hierarchy", ""));
        }catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    private String getLastRegionLevel() {
        List<ApplicationParam> params = boxAppParams.query().startsWith(ApplicationParam_.name, "hierarchy", QueryBuilder.StringOrder.CASE_SENSITIVE).build().find(); //COLUMN_NAME+" like 'hierarchy%'"
        String lastRegionLevel = "";

        for (ApplicationParam param : params){
            if (!param.getValue().isEmpty() && !param.getName().endsWith(".head")) {
                if (getHierarchyNumber(param.getName()) > getHierarchyNumber(lastRegionLevel)) {
                    lastRegionLevel = param.getName();
                }
            }
        }

        return lastRegionLevel;
    }

    private void showRegionChilds(){

        if (isLatestRegionLevel) {
            //get existing households and create an adapter
            List<Household> households = this.boxHouseholds.query(Household_.region.equal(region.getCode())).build().find();
            HouseholdAdapter adapter = new HouseholdAdapter(this.getContext(), households);
            this.lvRegionChilds.setAdapter(adapter);
        } else {
            //get exisiting child regions and create adapter
            List<Region> regions = this.boxRegions.query(Region_.parent.equal(region.getCode())).build().find();
            RegionAdapter adapter = new RegionAdapter(this.getContext(), regions);
            this.lvRegionChilds.setAdapter(adapter);
        }

    }

    private void showLoadingDialog(String msg, boolean show){
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.dismiss();
        }
    }

    public void reloadRegions() {
        this.showRegionChilds();
    }

    private void openHouseholdDetails(Household household) {
        ShowHouseholdTask task = new ShowHouseholdTask(household, null, null);
        task.execute();

        showLoadingDialog(getString(R.string.loading_dialog_household_details_lbl), true);
    }

    private void openRegionDetails(Region region) {
        ShowRegionTask task = new ShowRegionTask(region);
        task.execute();

        showLoadingDialog(getString(R.string.loading_dialog_household_details_lbl), true);
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

            Intent intent = new Intent(RegionChildsFragment.this.getContext(), HouseholdDetailsActivity.class);
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

            Intent intent = new Intent(RegionChildsFragment.this.getContext(), RegionDetailsActivity.class);
            intent.putExtra("region", region.id);

            startActivity(intent);
        }
    }
}