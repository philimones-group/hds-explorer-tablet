package org.philimone.hds.explorer.fragment.member.details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.CoreFormColumnOptions;
import org.philimone.hds.explorer.model.CoreFormColumnOptions_;
import org.philimone.hds.explorer.model.Dataset;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.widget.LoadingDialog;

import java.util.Date;

import io.objectbox.Box;
import mz.betainteractive.utilities.StringUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MemberDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberDetailsFragment extends Fragment {

    private LoadingDialog loadingDialog;

    private TextView mbDetailsHouseNo;
    private TextView mbDetailsEndType;
    private TextView mbDetailsEndDate;
    private TextView mbDetailsFather;
    private TextView mbDetailsMother;
    private TextView mbDetailsEducation;
    private TextView mbDetailsReligion;

    private TextView mbDetailsSpouse;

    private Member member;
    private User loggedUser;

    private Box<Region> boxRegions;
    private Box<Member> boxMembers;
    private Box<Form> boxForms;
    private Box<Dataset> boxDatasets;
    private Box<CoreFormColumnOptions> boxCoreFormColumnOptions;

    public MemberDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HouseholdMembersFragment.
     */
    public static MemberDetailsFragment newInstance(Member member, User user) {
        MemberDetailsFragment fragment = new MemberDetailsFragment();
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
        View view = inflater.inflate(R.layout.member_details_dpanel, container, false);

        initBoxes();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialize(view);
    }

    private void initialize(View view) {

        mbDetailsHouseNo = (TextView) view.findViewById(R.id.mbDetailsHouseName);
        mbDetailsEndType = (TextView) view.findViewById(R.id.mbDetailsEndType);
        mbDetailsEndDate = (TextView) view.findViewById(R.id.mbDetailsEndDate);
        mbDetailsFather = (TextView) view.findViewById(R.id.mbDetailsFather);
        mbDetailsMother = (TextView) view.findViewById(R.id.mbDetailsMother);
        mbDetailsEducation = (TextView) view.findViewById(R.id.mbDetailsEducation);
        mbDetailsReligion = (TextView) view.findViewById(R.id.mbDetailsReligion);
        mbDetailsSpouse = (TextView) view.findViewById(R.id.mbDetailsSpouse);

        this.loadingDialog = new LoadingDialog(this.getContext());

        this.clearMemberData();
        this.setMemberData();
    }

    private void initBoxes() {
//        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
//        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        this.boxDatasets = ObjectBoxDatabase.get().boxFor(Dataset.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxCoreFormColumnOptions = ObjectBoxDatabase.get().boxFor(CoreFormColumnOptions.class);
//        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
    }

    public void refreshMemberData(){
        clearMemberData();
        setMemberData();
    }

    private void clearMemberData(){
        mbDetailsHouseNo.setText("");
        mbDetailsEndType.setText("");
        mbDetailsEndDate.setText("");
        mbDetailsFather.setText("");
        mbDetailsMother.setText("");
        mbDetailsEducation.setText("");
        mbDetailsReligion.setText("");
        mbDetailsSpouse.setText("");
    }

    private void setMemberData(){
        mbDetailsHouseNo.setText(member.getHouseholdName());
        mbDetailsEndType.setText(getEndTypeMsg(member));
        mbDetailsEndDate.setText(getEndDateMsg(member));
        mbDetailsFather.setText(getParentName(member.getFatherName()));
        mbDetailsMother.setText(getParentName(member.getMotherName()));
        mbDetailsEducation.setText(getEducationLabel(member.education));
        mbDetailsReligion.setText(getReligionLabel(member.religion));
        mbDetailsSpouse.setText(getSpouseName(member.getSpouseName()));
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    private String getEndTypeMsg(Member member){
        if (member.getEndType() == ResidencyEndType.NOT_APPLICABLE) return getString(R.string.member_details_endtype_na_lbl);
        if (member.getEndType() == ResidencyEndType.EXTERNAL_OUTMIGRATION) return getString(R.string.member_details_endtype_ext_lbl);
        if (member.getEndType() == ResidencyEndType.DEATH) return getString(R.string.member_details_endtype_dth_lbl);

        return member.getEndType().getId();
    }

    private String getEndDateMsg(Member member){
        Date date = member.getEndDate();
        if (member.getEndType() == ResidencyEndType.NOT_APPLICABLE) {
            date = member.getStartDate();
        }

        return StringUtil.formatYMD(date);
    }

    private String getParentName(String name){
        if (name.equals("Unknown") || name.equals("member.unknown.label")){
            return getString(R.string.member_details_unknown_lbl);
        }else {
            return name;
        }
    }

    private String getSpouseName(String name){
        if (name == null || name.isEmpty()){
            return "";
        }
        if (name.equals("Unknown") || name.equals("member.unknown.label")){
            return getString(R.string.member_details_unknown_lbl);
        }else {
            return name;
        }
    }

    private String getEducationLabel(String educationCode) {
        if (!StringUtil.isBlank(educationCode)) {
            CoreFormColumnOptions option = boxCoreFormColumnOptions.query(CoreFormColumnOptions_.columnName.equal("education").and(CoreFormColumnOptions_.optionValue.equal(educationCode))).build().findFirst();
            if (option != null) {
                return option.optionLabel;
            }
        }
        return "";
    }

    private String getReligionLabel(String religionCode) {
        if (!StringUtil.isBlank(religionCode)) {
            CoreFormColumnOptions option = boxCoreFormColumnOptions.query(CoreFormColumnOptions_.columnName.equal("religion").and(CoreFormColumnOptions_.optionValue.equal(religionCode))).build().findFirst();
            if (option != null) {
                return option.optionLabel;
            }
        }
        return "";
    }
}