package org.philimone.hds.explorer.fragment.household.details;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.io.xml.XmlCreator;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.CoreFormRecordType;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.LoadingDialog;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.XmlFormResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import mz.betainteractive.odk.model.FilledForm;
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HouseholdEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HouseholdEditFragment extends Fragment implements LocationListener {

    private EditText txtEditName;
    private Button btEditGetGps;
    private TextView txtGpsLatitude;
    private TextView txtGpsLongitude;
    private TextView txtGpsAltitude;
    private TextView txtGpsAccuracy;
    private Button btEditUpdateDetails;
    private LoadingDialog loadingDialog;

    private Household household;
    private User loggedUser;
    private CoreCollectedData collectedData;

    private Location gpsLocationResult;
    private LocationManager locationManager;

    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<CoreCollectedData> boxCoreCollectedData;

    private ActivityResultLauncher<String[]> requestPermissions;

    private EditListener editListener;

    public HouseholdEditFragment() {
        // Required empty public constructor

        initBoxes();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HouseholdMembersFragment.
     */
    public static HouseholdEditFragment newInstance(Household household, User user) {
        HouseholdEditFragment fragment = new HouseholdEditFragment();
        fragment.household = household;
        fragment.loggedUser = user;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPermissions();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.household_details_edit, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialize(view);
    }

    public void setEditListener(EditListener editListener) {
        this.editListener = editListener;
    }

    private void initBoxes() {
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
    }

    private void initialize(View view) {
        
        this.txtEditName = view.findViewById(R.id.txtEditName);
        this.btEditGetGps = view.findViewById(R.id.btEditGetGps);
        this.txtGpsLatitude = view.findViewById(R.id.txtGpsLatitude);
        this.txtGpsLongitude = view.findViewById(R.id.txtGpsLongitude);
        this.txtGpsAltitude = view.findViewById(R.id.txtGpsAltitude);
        this.txtGpsAccuracy = view.findViewById(R.id.txtGpsAccuracy);
        this.btEditUpdateDetails = view.findViewById(R.id.btEditUpdateDetails);

        this.loadingDialog = new LoadingDialog(this.getContext());

        this.btEditGetGps.setOnClickListener(v -> {
            onGetGpsClicked();
        });

        this.btEditUpdateDetails.setOnClickListener(v -> {
            onUpdateDetailsClicked();
        });

        this.txtEditName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onFormContentChanges();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        this.txtEditName.setText(household.name);
        showGpsData(this.household);

        btEditUpdateDetails.setEnabled(false);
        //clearGpsResultTexts();

        //get the last CoreCollectedData record of this household - only one can exists per subject
        this.collectedData = getCollectedData();
    }

    private void onFormContentChanges() {

        boolean changed1 = !(this.household.name.equals(this.txtEditName.getText().toString()));

        if (this.gpsLocationResult != null) {
            //but I believe this should every time be different
            try {
                float acc = (float) (this.household.gpsAccuracy * 1f); //convert double to
                changed1 = changed1 ||
                        this.gpsLocationResult.getLatitude() != this.household.gpsLatitude ||
                        this.gpsLocationResult.getLongitude() != this.household.gpsLongitude ||
                        this.gpsLocationResult.getAltitude() != this.household.gpsAltitude ||
                        this.gpsLocationResult.getAccuracy() != acc;
            } catch(Exception e){
                changed1 = true;
            }
        }

        this.btEditUpdateDetails.setEnabled(changed1);
    }

    private CoreCollectedData getCollectedData(){
        //only one edit collected data is allowed per subject
        CoreCollectedData coreCollectedData = this.boxCoreCollectedData.query(
                        CoreCollectedData_.formEntity.equal(CoreFormEntity.EDITED_HOUSEHOLD.code).and(CoreCollectedData_.formEntityId.equal(household.id)))
                .orderDesc(CoreCollectedData_.createdDate)
                .build().findFirst();

        return coreCollectedData;
    }

    private void onGetGpsClicked() {
        ensurePermissionsGranted(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private void onUpdateDetailsClicked() {

        if (this.household.isRecentlyCreated()) {
            DialogFactory.createMessageInfo(this.getContext(), R.string.household_details_tab_edit_lbl, R.string.household_details_edit_cant_update_new_record).show();
            return;
        }

        //persist the changes into the database
        this.household.name = this.txtEditName.getText().toString();
        if (this.gpsLocationResult != null){
            this.household.gpsLatitude = this.gpsLocationResult.getLatitude();
            this.household.gpsLongitude = this.gpsLocationResult.getLongitude();
            this.household.gpsAltitude = this.gpsLocationResult.getAltitude();
            this.household.gpsAccuracy = Double.valueOf(this.gpsLocationResult.getAccuracy());
        }
        this.boxHouseholds.put(this.household);

        updateAffectedRecords(household);

        //create xml mapped columns
        Map<String,String> mapXml = new LinkedHashMap<>();
        mapXml.put("householdCode", this.household.code);
        mapXml.put("householdName", this.txtEditName.getText().toString());
        if (gpsLocationResult != null) {
            mapXml.put("gpsLat", this.household.gpsLatitude+"");
            mapXml.put("gpsLon", this.household.gpsLongitude+"");
            mapXml.put("gpsAlt", this.household.gpsAltitude+"");
            mapXml.put("gpsAcc", this.household.gpsAccuracy+"");
        }
        mapXml.put("collectedBy", this.loggedUser.username);
        mapXml.put("collectedDate", StringUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));


        //generate xml and create/overwrite a file
        String xml = XmlCreator.generateXml(CoreFormEntity.EDITED_HOUSEHOLD.code, mapXml);
        String filename = this.collectedData!=null ? this.collectedData.formFilename : generateXmlFilename(CoreFormEntity.EDITED_HOUSEHOLD, household.code);
        createXmlFile(filename, xml);

        //get or create CoreCollectedData
        collectedData = collectedData==null ? new CoreCollectedData() : collectedData;
        collectedData.visitId = 0; //no visit associated
        collectedData.formEntity = CoreFormEntity.EDITED_HOUSEHOLD;
        collectedData.recordType = CoreFormRecordType.UPDATE_RECORD;
        collectedData.formEntityId = household.id;
        collectedData.formEntityCode = household.code;
        collectedData.formEntityCodes = household.code; //new head code, spouse, head related individuals
        collectedData.formEntityName = household.name;
        collectedData.formUuid = collectedData.formUuid==null ? GeneralUtil.generateUUID() : collectedData.formUuid;
        collectedData.formFilename = filename;
        collectedData.createdDate = new Date();
        collectedData.collectedId = household.collectedId;
        collectedData.uploaded = false;

        this.boxCoreCollectedData.put(collectedData);

        DialogFactory.createMessageInfo(this.getContext(), R.string.household_details_tab_edit_lbl, R.string.updated_successfully_lbl, new DialogFactory.OnClickListener() {
            @Override
            public void onClicked(DialogFactory.Buttons clickedButton) {
                if (editListener != null) editListener.onUpdate();
            }
        }).show();
    }

    private void updateAffectedRecords(Household household) {
        //update household name on Member
        List<Member> members = this.boxMembers.query(Member_.householdCode.equal(household.code)).build().find();
        for (Member m : members){
            m.householdName = household.name;
        }
        this.boxMembers.put(members);
    }

    private String generateXmlFilename(CoreFormEntity entity, String code) {
        String dateTmsp = StringUtil.format(new Date(), "yyyy-MM-dd_HH_mm_ss");
        return Bootstrap.getInstancesPath(this.getContext()) + entity.code + "_" + code + "_" + dateTmsp + ".xml";
    }

    private boolean createXmlFile(String filename, String xmlContent) {
        try {

            PrintStream output = new PrintStream(filename);
            output.print(xmlContent);
            output.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void initPermissions() {
        this.requestPermissions = this.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionResults -> {
            boolean granted = !permissionResults.values().contains(false);

            if (granted) {
                detectGpsLocation();
            } else {
                DialogFactory.createMessageInfo(this.getContext(), R.string.gps_title_lbl, R.string.gps_permissions_error).show();
            }
        });
    }

    private void ensurePermissionsGranted(final String... permissions) {
        boolean denied = Arrays.stream(permissions).anyMatch(permission -> ContextCompat.checkSelfPermission(this.getContext(), permission) == PackageManager.PERMISSION_DENIED);

        if (denied) { //without access
            requestPermissions.launch(permissions);
        } else {
            detectGpsLocation();
        }
    }

    private void detectGpsLocation() {

        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            DialogFactory.createMessageInfo(this.getContext(), R.string.gps_title_lbl, R.string.gps_permissions_error).show();
            return;
        }

        this.locationManager = this.locationManager == null ? (LocationManager)this.getContext().getSystemService(Context.LOCATION_SERVICE) : locationManager;

        boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        String provider = gps_enabled ? LocationManager.GPS_PROVIDER : network_enabled ? LocationManager.NETWORK_PROVIDER : "";

        if (provider.isEmpty()) {
            //No provider available
            DialogFactory.createMessageInfo(this.getContext(), R.string.gps_title_lbl, R.string.gps_no_provider_available_error).show();
            return;
        }

        this.gpsLocationResult = null;

        showLoadingDialog(R.string.gps_loading_lbl, true);
        locationManager.requestLocationUpdates(provider, 5, 0, this);

    }

    private void clearGpsResultTexts() {
        this.txtGpsLatitude.setText("");
        this.txtGpsLongitude.setText("");
        this.txtGpsAltitude.setText("");
        this.txtGpsAccuracy.setText("");
    }

    private void showLoadingDialog(@StringRes int msgId, boolean show) {
        if (show) {
            this.loadingDialog.setMessage(msgId);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.dismiss();
        }
    }

    private void showGpsResults() {
        if (this.gpsLocationResult != null) {
            this.txtGpsLatitude.setText(Location.convert(this.gpsLocationResult.getLatitude(), Location.FORMAT_DEGREES));
            this.txtGpsLongitude.setText(Location.convert(this.gpsLocationResult.getLongitude(), Location.FORMAT_DEGREES));
            this.txtGpsAltitude.setText("" + this.gpsLocationResult.getAltitude());
            this.txtGpsAccuracy.setText("" + this.gpsLocationResult.getAccuracy());
        }
    }

    private void showGpsData(Household household) {
        if (this.household.gpsLatitude != null) {
            this.txtGpsLatitude.setText(Location.convert(this.household.gpsLatitude, Location.FORMAT_DEGREES));
            this.txtGpsLongitude.setText(Location.convert(this.household.gpsLongitude, Location.FORMAT_DEGREES));
            this.txtGpsAltitude.setText(this.household.gpsAltitude==null ? "" : this.household.gpsAltitude+"");
            this.txtGpsAccuracy.setText(this.household.gpsAccuracy==null ? "" : this.household.gpsAccuracy+"");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        showLoadingDialog(0, false);

        this.gpsLocationResult = location;

        showGpsResults();

        onFormContentChanges();

        showLoadingDialog(0, false);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("megps", ""+provider+", status="+status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        showLoadingDialog(0, false);
    }

    @Override
    public void onProviderDisabled(String provider) {
        showLoadingDialog(0, false);
    }

    public interface EditListener {
        void onUpdate();
    }
}