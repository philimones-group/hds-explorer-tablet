package org.philimone.hds.explorer.fragment.region.details;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegionEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegionEditFragment extends Fragment {

    private EditText txtEditName;
    private Button btEditUpdateDetails;
    private LoadingDialog loadingDialog;

    private Region region;
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

    public RegionEditFragment() {
        // Required empty public constructor

        initBoxes();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HouseholdMembersFragment.
     */
    public static RegionEditFragment newInstance(Region region, User user) {
        RegionEditFragment fragment = new RegionEditFragment();
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
        View view = inflater.inflate(R.layout.region_details_edit, container, false);

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
        this.btEditUpdateDetails = view.findViewById(R.id.btEditUpdateDetails);

        this.loadingDialog = new LoadingDialog(this.getContext());

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

        this.txtEditName.setText(region.name);

        btEditUpdateDetails.setEnabled(false);
        //clearGpsResultTexts();

        //get the last CoreCollectedData record of this household - only one can exists per subject
        this.collectedData = getCollectedData();
    }

    private void onFormContentChanges() {
        boolean changed1 = !(this.region.name.equals(this.txtEditName.getText().toString()));

        this.btEditUpdateDetails.setEnabled(changed1);
    }

    private CoreCollectedData getCollectedData(){
        //only one edit collected data is allowed per subject
        CoreCollectedData coreCollectedData = this.boxCoreCollectedData.query(
                        CoreCollectedData_.formEntity.equal(CoreFormEntity.EDITED_REGION.code).and(CoreCollectedData_.formEntityId.equal(region.id)))
                .orderDesc(CoreCollectedData_.createdDate)
                .build().findFirst();

        return coreCollectedData;
    }

    private void onUpdateDetailsClicked() {

        if (this.region.isRecentlyCreated()) {
            DialogFactory.createMessageInfo(this.getContext(), R.string.region_details_tab_edit_lbl, R.string.region_details_edit_cant_update_new_record).show();
            return;
        }

        //persist the changes into the database
        this.region.name = this.txtEditName.getText().toString();
        this.boxRegions.put(this.region);

        updateAffectedRecords(region);

        //create xml mapped columns
        Map<String,String> mapXml = new LinkedHashMap<>();
        mapXml.put("regionCode", this.region.code);
        mapXml.put("regionName", this.txtEditName.getText().toString());
        mapXml.put("collectedBy", this.loggedUser.username);
        mapXml.put("collectedDate", StringUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));


        //generate xml and create/overwrite a file
        String xml = XmlCreator.generateXml(CoreFormEntity.EDITED_REGION.code, mapXml);
        String filename = this.collectedData!=null ? this.collectedData.formFilename : generateXmlFilename(CoreFormEntity.EDITED_REGION, region.code);
        createXmlFile(filename, xml);

        //get or create CoreCollectedData
        collectedData = collectedData==null ? new CoreCollectedData() : collectedData;
        collectedData.visitId = 0; //no visit associated
        collectedData.formEntity = CoreFormEntity.EDITED_REGION;
        collectedData.recordType = CoreFormRecordType.UPDATE_RECORD;
        collectedData.formEntityId = region.id;
        collectedData.formEntityCode = region.code;
        collectedData.formEntityCodes = region.code; //new head code, spouse, head related individuals
        collectedData.formEntityName = region.name;
        collectedData.formUuid = collectedData.formUuid==null ? GeneralUtil.generateUUID() : collectedData.formUuid;
        collectedData.formFilename = filename;
        collectedData.createdDate = new Date();
        collectedData.collectedId = "";
        collectedData.uploaded = false;

        this.boxCoreCollectedData.put(collectedData);

        DialogFactory.createMessageInfo(this.getContext(), R.string.region_details_tab_edit_lbl, R.string.updated_successfully_lbl, new DialogFactory.OnClickListener() {
            @Override
            public void onClicked(DialogFactory.Buttons clickedButton) {
                if (editListener != null) editListener.onUpdate();
            }
        }).show();
    }

    private void updateAffectedRecords(Region region) {

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

    private void showLoadingDialog(@StringRes int msgId, boolean show) {
        if (show) {
            this.loadingDialog.setMessage(msgId);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.dismiss();
        }
    }

    public interface EditListener {
        void onUpdate();
    }
}