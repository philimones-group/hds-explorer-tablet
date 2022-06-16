package org.philimone.hds.explorer.fragment.member.details;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.fragment.MemberFilterDialog;
import org.philimone.hds.explorer.io.xml.XmlCreator;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.HeadRelationship_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Residency;
import org.philimone.hds.explorer.model.Residency_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.CoreFormRecordType;
import org.philimone.hds.explorer.model.enums.Gender;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipStartType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyStartType;
import org.philimone.hds.explorer.widget.DateTimeSelector;
import org.philimone.hds.explorer.widget.DialogFactory;
import org.philimone.hds.explorer.widget.LoadingDialog;

import java.io.PrintStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MemberEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberEditFragment extends Fragment {

    private FragmentManager fragmentManager;

    private EditText txtEditName;
    private RadioButton chkEditGFemale;
    private RadioButton chkEditGMale;
    private TextView txtEditDob;
    private Button btEditDob;
    private TextView txtEditFatherCode;
    private TextView txtEditFatherName;
    private Button btEditFather;
    private TextView txtEditMotherCode;
    private TextView txtEditMotherName;
    private Button btEditMother;
    private Button btEditUpdateDetails;
    private LoadingDialog loadingDialog;
    private DateTimeSelector datePicker;

    private Household household;
    private Member member;
    private Member selectedFather;
    private Member selectedMother;
    private Date selectedDob;
    private User loggedUser;
    private CoreCollectedData collectedData;

    private int minimunFatherAge;
    private int minimunMotherAge;

    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<Residency> boxResidencies;
    private Box<HeadRelationship> boxHeadRelationships;
    private Box<CoreCollectedData> boxCoreCollectedData;
    private Box<ApplicationParam> boxAppParams;

    private EditListener editListener;

    public MemberEditFragment() {
        // Required empty public constructor

        initBoxes();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HouseholdMembersFragment.
     */
    public static MemberEditFragment newInstance(FragmentManager fragmentManager, Household household, Member member, User user) {
        MemberEditFragment fragment = new MemberEditFragment();
        fragment.fragmentManager = fragmentManager;
        fragment.household = household;
        fragment.member = member;
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
        View view = inflater.inflate(R.layout.member_details_edit, container, false);

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
        this.boxResidencies = ObjectBoxDatabase.get().boxFor(Residency.class);
        this.boxHeadRelationships = ObjectBoxDatabase.get().boxFor(HeadRelationship.class);
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
    }

    private void initialize(View view) {

        this.minimunFatherAge = retrieveMinimumFatherAge();
        this.minimunMotherAge = retrieveMinimumMotherAge();
        
        this.txtEditName = view.findViewById(R.id.txtEditName);
        this.chkEditGFemale = view.findViewById(R.id.chkEditGFemale);
        this.chkEditGMale = view.findViewById(R.id.chkEditGMale);
        this.txtEditDob = view.findViewById(R.id.txtEditDob);
        this.btEditDob = view.findViewById(R.id.btEditDob);
        this.txtEditFatherCode = view.findViewById(R.id.txtEditFatherCode);
        this.txtEditFatherName = view.findViewById(R.id.txtEditFatherName);
        this.btEditFather = view.findViewById(R.id.btEditFather);
        this.txtEditMotherCode = view.findViewById(R.id.txtEditMotherCode);
        this.txtEditMotherName = view.findViewById(R.id.txtEditMotherName);
        this.btEditMother = view.findViewById(R.id.btEditMother);
        this.btEditUpdateDetails = view.findViewById(R.id.btEditUpdateDetails);

        this.loadingDialog = new LoadingDialog(this.getContext());

        this.datePicker = DateTimeSelector.createDateWidget(this.getContext(), (pSelectedDate, selectedDateText) -> {
            txtEditDob.setText(selectedDateText);
            selectedDob = pSelectedDate;
            onFormContentChanges();
        });

        this.btEditDob.setOnClickListener(v -> {
            datePicker.show();
        });

        this.btEditFather.setOnClickListener(v -> {
            openFatherFilterDialog();
        });

        this.btEditMother.setOnClickListener(v -> {
            openMotherFilterDialog();
        });

        this.btEditUpdateDetails.setOnClickListener(v -> {
            onUpdateDetailsClicked();
        });

        //show data before calling change listeners
        setMemberData();

        this.txtEditName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { onFormContentChanges(); }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        this.chkEditGMale.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onFormContentChanges();
        });
        this.chkEditGFemale.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onFormContentChanges();
        });

        btEditUpdateDetails.setEnabled(false);

        //get the last CoreCollectedData record of this household - only one can exists per subject
        this.collectedData = getCollectedData();
    }

    private void setMemberData() {
        this.txtEditName.setText(member.name);
        this.chkEditGFemale.setChecked(member.gender == Gender.FEMALE);
        this.chkEditGMale.setChecked(member.gender== Gender.MALE);
        this.txtEditDob.setText(StringUtil.formatYMD(member.dob));
        this.txtEditFatherCode.setText(member.fatherCode);
        this.txtEditFatherName.setText(getParentName(member.fatherName));
        this.txtEditMotherCode.setText(member.motherCode);
        this.txtEditMotherName.setText(getParentName(member.motherName));
    }

    private String getParentName(String name){
        if (name.equals("Unknown") || name.equals("member.unknown.label")){
            return getString(R.string.member_details_unknown_lbl);
        }else {
            return name;
        }
    }

    private void onFormContentChanges() {

        Gender selectedGender = chkEditGMale.isChecked() ? Gender.MALE : Gender.FEMALE;

        //check warning of changing the sex - check if member is male - if is not a father somewhere
        if (selectedGender != member.gender) {

            if (selectedGender == Gender.FEMALE) {
                //check if it is a father -> cannot change the gender because this member is a father of other members - fix first the other members
                boolean isFather = this.boxMembers.query(Member_.fatherCode.equal(member.code)).build().count()>0;

                if (isFather) {
                    DialogFactory.createMessageInfo(this.getContext(), R.string.info_lbl, R.string.member_details_edit_father_sex_cant_change_lbl, clickedButton -> {
                        chkEditGMale.setChecked(true);
                    }).show();
                }

            } else if (selectedGender == Gender.MALE) {
                //check if it is a father -> cannot change the gender because this member is a father of other members - fix first the other members
                boolean isMother = this.boxMembers.query(Member_.motherCode.equal(member.code)).build().count()>0;

                if (isMother) {
                    DialogFactory.createMessageInfo(this.getContext(), R.string.info_lbl, R.string.member_details_edit_mother_sex_cant_change_lbl, clickedButton -> {
                        chkEditGFemale.setChecked(true);
                    }).show();
                }

            }

        }

        //check warning of changing the date of birth - search for residency with starttype=BIR and startdate==date
        if (selectedDob != null) {
            Residency residency = this.boxResidencies.query(Residency_.memberCode.equal(member.code).and(Residency_.startType.equal(ResidencyStartType.BIRTH.code))).build().findFirst();

            if (GeneralUtil.dateEquals(member.dob, residency.startDate) && !GeneralUtil.dateEquals(selectedDob, member.dob)) {
                //throw warning

                DialogFactory.createMessageYN(this.getContext(), R.string.member_details_edit_member_dob_lbl, R.string.member_details_edit_dob_bir_warning_lbl, new DialogFactory.OnYesNoClickListener() {
                    @Override
                    public void onYesClicked() {
                        //move on
                    }

                    @Override
                    public void onNoClicked() {
                        //go back to original state
                        selectedDob = null;
                        txtEditDob.setText(StringUtil.formatYMD(member.dob));
                    }
                }).show();
            }
        }


        boolean changed1 = !(this.member.name.equals(this.txtEditName.getText().toString())) || !(this.member.gender==selectedGender) ||
                           !(selectedFather==null) || !(selectedMother==null) || (selectedDob !=null && !GeneralUtil.dateEquals(selectedDob, member.dob));


        this.btEditUpdateDetails.setEnabled(changed1);
    }

    private CoreCollectedData getCollectedData(){
        //only one edit collected data is allowed per subject
        CoreCollectedData coreCollectedData = this.boxCoreCollectedData.query(
                        CoreCollectedData_.formEntity.equal(CoreFormEntity.EDITED_MEMBER.code).and(CoreCollectedData_.formEntityId.equal(member.id)))
                .orderDesc(CoreCollectedData_.createdDate)
                .build().findFirst();

        return coreCollectedData;
    }

    private void onUpdateDetailsClicked() {

        if (this.member.isRecentlyCreated()) {
            DialogFactory.createMessageInfo(this.getContext(), R.string.member_details_tab_edit_lbl, R.string.member_details_edit_cant_update_new_record).show();
            return;
        }

        //persist the changes into the database
        this.member.name = this.txtEditName.getText().toString();
        this.member.gender = chkEditGMale.isChecked() ? Gender.MALE : Gender.FEMALE;
        this.member.dob = StringUtil.toDateYMD(txtEditDob.getText().toString());
        this.member.fatherCode = txtEditFatherCode.getText().toString();
        this.member.fatherName = txtEditFatherName.getText().toString();
        this.member.motherCode = txtEditMotherCode.getText().toString();
        this.member.motherName = txtEditMotherName.getText().toString();
        this.boxMembers.put(this.member);

        updateAffectedRecords(member);

        //create xml mapped columns
        Map<String,String> mapXml = new LinkedHashMap<>();
        mapXml.put("code", this.member.code);
        mapXml.put("name", this.member.name);
        mapXml.put("gender", this.member.gender.code);
        mapXml.put("dob", this.txtEditDob.getText().toString());
        mapXml.put("motherCode", this.member.motherCode);
        mapXml.put("motherName", this.member.motherName);
        mapXml.put("fatherCode", this.member.fatherCode);
        mapXml.put("fatherName", this.member.fatherName);
        mapXml.put("collectedBy", this.loggedUser.username);
        mapXml.put("collectedDate", StringUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));

        //generate xml and create/overwrite a file
        String xml = XmlCreator.generateXml(CoreFormEntity.EDITED_MEMBER.code, mapXml);
        String filename = this.collectedData!=null ? this.collectedData.formFilename : generateXmlFilename(CoreFormEntity.EDITED_MEMBER, member.code);
        createXmlFile(filename, xml);

        //get or create CoreCollectedData
        collectedData = collectedData==null ? new CoreCollectedData() : collectedData;
        collectedData.visitId = 0; //no visit associated
        collectedData.formEntity = CoreFormEntity.EDITED_MEMBER;
        collectedData.recordType = CoreFormRecordType.UPDATE_RECORD;
        collectedData.formEntityId = member.id;
        collectedData.formEntityCode = member.code;
        collectedData.formEntityCodes = member.code; //new head code, spouse, head related individuals
        collectedData.formEntityName = member.name;
        collectedData.formUuid = collectedData.formUuid==null ? GeneralUtil.generateUUID() : collectedData.formUuid;
        collectedData.formFilename = filename;
        collectedData.createdDate = new Date();
        collectedData.collectedId = member.collectedId;
        collectedData.uploaded = false;

        this.boxCoreCollectedData.put(collectedData);

        DialogFactory.createMessageInfo(this.getContext(), R.string.member_details_tab_edit_lbl, R.string.updated_successfully_lbl, new DialogFactory.OnClickListener() {
            @Override
            public void onClicked(DialogFactory.Buttons clickedButton) {
                if (editListener != null) editListener.onUpdate();
            }
        }).show();
    }

    private void updateAffectedRecords(Member member) {
        //Update Household.(headName)
        //Update Member.(fatherName, motherName, spouseName)

        List<Household> households = this.boxHouseholds.query(Household_.headCode.equal(member.code)).build().find();
        List<Member> members = this.boxMembers.query(Member_.fatherCode.equal(member.code)
                                                 .or(Member_.motherCode.equal(member.code)
                                                 .or(Member_.spouseCode.equal(member.code)))).build().find();

        for (Household h : households) {
            h.headName = member.name;
        }

        for (Member m : members){
            if (m.fatherCode.equals(member.fatherCode)) m.fatherName = member.name;
            if (m.motherCode.equals(member.motherCode)) m.motherName = member.name;
            if (m.spouseCode.equals(member.spouseCode)) m.spouseName = member.name;
        }

        this.boxHouseholds.put(households);
        this.boxMembers.put(members);

        //Update residency startDate / membership start date if necessary
        Residency residency = this.boxResidencies.query(Residency_.memberCode.equal(member.code).and(Residency_.startType.equal(ResidencyStartType.BIRTH.code))).order(Residency_.startDate).build().findFirst();
        HeadRelationship headRelationship = this.boxHeadRelationships.query(HeadRelationship_.memberCode.equal(member.code).and(HeadRelationship_.startType.equal(HeadRelationshipStartType.BIRTH.code))).order(HeadRelationship_.startDate).build().findFirst();

        if (residency != null){
            residency.startDate = member.dob;
            this.boxResidencies.put(residency);
        }

        if (headRelationship != null) {
            headRelationship.startDate = member.dob;
            this.boxHeadRelationships.put(headRelationship);
        }
    }

    private void openFatherFilterDialog(){

        MemberFilterDialog dialog = MemberFilterDialog.newInstance(this.fragmentManager, this.getContext().getString(R.string.member_details_edit_father_select_lbl), true, new MemberFilterDialog.Listener() {
            @Override
            public void onSelectedMember(Member member) {
                Log.d("selected-father", ""+member.getCode());
                selectedFather = member;
                txtEditFatherCode.setText(member.getCode());
                txtEditFatherName.setText(member.getName());
                onFormContentChanges();
            }

            @Override
            public void onCanceled() {

            }
        });

        dialog.enableButton(MemberFilterDialog.Buttons.BUTTON_1, R.string.member_details_edit_father_unknown_lbl, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedFather = Member.getUnknownIndividual();
                txtEditFatherCode.setText(selectedFather.getCode());
                txtEditFatherName.setText(getParentName(selectedFather.name));
                onFormContentChanges();
            }
        });


        dialog.setGenderMaleOnly();
        dialog.setFilterMinAge(this.minimunFatherAge, true);
        dialog.setFilterHouseCode(household.getCode());
        dialog.setStartSearchOnShow(true);
        dialog.show();
    }

    private void openMotherFilterDialog(){

        MemberFilterDialog dialog = MemberFilterDialog.newInstance(this.fragmentManager, this.getContext().getString(R.string.member_details_edit_mother_select_lbl), true, new MemberFilterDialog.Listener() {
            @Override
            public void onSelectedMember(Member member) {
                Log.d("selected-mother", ""+member.getCode());
                selectedMother = member;
                txtEditMotherCode.setText(member.getCode());
                txtEditMotherName.setText(member.getName());
                onFormContentChanges();
            }

            @Override
            public void onCanceled() {

            }
        });

        dialog.enableButton(MemberFilterDialog.Buttons.BUTTON_1, R.string.member_details_edit_mother_unknown_lbl, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedMother = Member.getUnknownIndividual();
                txtEditMotherCode.setText(selectedMother.getCode());
                txtEditMotherName.setText(getParentName(selectedMother.name));
                onFormContentChanges();
            }
        });

        dialog.setGenderFemaleOnly();
        dialog.setFilterMinAge(this.minimunMotherAge, true);
        dialog.setFilterHouseCode(household.getCode());
        dialog.setStartSearchOnShow(true);
        dialog.show();
    }

    private int retrieveMinimumFatherAge() {
        ApplicationParam param = this.boxAppParams.query().equal(ApplicationParam_.name, ApplicationParam.PARAMS_MIN_AGE_OF_FATHER, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (param != null) {
            try {
                return Integer.parseInt(param.value);
            } catch (Exception ex) {

            }
        }

        return 12;
    }

    private int retrieveMinimumMotherAge() {
        ApplicationParam param = this.boxAppParams.query().equal(ApplicationParam_.name, ApplicationParam.PARAMS_MIN_AGE_OF_MOTHER, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        if (param != null) {
            try {
                return Integer.parseInt(param.value);
            } catch (Exception ex) {

            }
        }

        return 12;
    }

    private String generateXmlFilename(CoreFormEntity entity, String code) {
        String dateTmsp = StringUtil.format(new Date(), "yyyy-MM-dd_HH_mm_ss");
        return Bootstrap.getInstancesPath() + entity.code + "_" + code + "_" + dateTmsp + ".xml";
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
            this.loadingDialog.hide();
        }
    }

    public interface EditListener {
        void onUpdate();
    }
}