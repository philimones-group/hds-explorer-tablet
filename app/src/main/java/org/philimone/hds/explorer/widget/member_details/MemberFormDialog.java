package org.philimone.hds.explorer.widget.member_details;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.widget.DialogFactory;

import java.util.Date;

import androidx.annotation.Nullable;
import io.objectbox.Box;
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

/**
 *
 */
public class MemberFormDialog extends DialogFragment {

    private FragmentManager fragmentManager;

    private TextView dialogTitle;
    private TextView txtNewMemHouseCode;
    private TextView txtNewMemHouseName;
    private EditText txtNewMemCode;
    private EditText txtNewMemName;
    private RadioButton chkNewMemGMale;
    private RadioButton chkNewMemGFemale;
    private DatePicker dtpNewMemDob;
    private Button btNewMemCancel;
    private Button btNewMemCollect;

    private Box<Member> boxMembers;

    private Listener listener;

    private Household household;
    private boolean isTempMember = false;

    public MemberFormDialog(){
        super();
    }


    public static MemberFormDialog createMemberDialog(FragmentManager fm, Household household, Listener listener){
        MemberFormDialog dialog = new MemberFormDialog();

        dialog.fragmentManager = fm;
        dialog.household = household;
        dialog.listener = listener;
        dialog.setCancelable(false);

        return dialog;
    }

    public static MemberFormDialog createTemporaryMemberDialog(FragmentManager fm, Household household, Listener listener){
        MemberFormDialog dialog = new MemberFormDialog();

        dialog.fragmentManager = fm;
        dialog.household = household;
        dialog.listener = listener;
        dialog.setCancelable(false);
        dialog.isTempMember = true;

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.new_member, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initBoxes();
        initialize(view);
    }

    private void initBoxes() {
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
    }

    private void initialize(View view) {

        this.dialogTitle = (TextView) view.findViewById(R.id.dialogTitle);
        this.txtNewMemHouseCode = (TextView) view.findViewById(R.id.txtNewMemHouseCode);
        this.txtNewMemHouseName = (TextView) view.findViewById(R.id.txtNewMemHouseName);
        this.txtNewMemCode = (EditText) view.findViewById(R.id.txtNewMemCode);
        this.txtNewMemName = (EditText) view.findViewById(R.id.txtNewMemName);
        this.chkNewMemGMale = (RadioButton) view.findViewById(R.id.chkNewMemGMale);
        this.chkNewMemGFemale = (RadioButton) view.findViewById(R.id.chkNewMemGFemale);
        this.dtpNewMemDob = (DatePicker) view.findViewById(R.id.dtpNewMemDob);
        this.btNewMemCancel = (Button) view.findViewById(R.id.btNewMemCancel);
        this.btNewMemCollect = (Button) view.findViewById(R.id.btNewMemCollect);

        this.btNewMemCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelClicked();
            }
        });

        this.btNewMemCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCollectClicked();
            }
        });

        //Set values to textviews
        String code = generateMemberCode(household);

        txtNewMemHouseCode.setText(household.getCode());
        txtNewMemHouseName.setText(household.getName());
        txtNewMemCode.setText(code);

        setTempMemberStuff();
    }

    private void setTempMemberStuff() {

        setEditable(txtNewMemCode, isTempMember);

        if (isTempMember) {
            this.dialogTitle.setText(R.string.member_list_new_temp_mem_title_lbl);
            this.txtNewMemCode.setTextColor(this.getContext().getColor(R.color.nui_color_text_darkgray));
        } else {
            this.txtNewMemCode.setTextColor(this.getContext().getColor(R.color.nui_color_text_adgray));
        }

    }

    private void setEditable(EditText editText, boolean editable){

        editText.setInputType(editable ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL);
        editText.setCursorVisible(editable);
        editText.setClickable(editable);
        editText.setFocusable(editable);
    }

    private void onCancelClicked(){
        dismiss();

        if (this.listener != null){
            this.listener.onCancelClicked();
        }
    }

    private void fireOnNewMemberCreated(Member member){
        dismiss();

        if (this.listener != null){
            this.listener.onNewMemberCreated(member);
        }
    }

    private void onCollectClicked(){

        //try to create new Member
        Member member = validateAndCreateMember();

        if (member != null){
            fireOnNewMemberCreated(member);
        }
    }

    private Member validateAndCreateMember() {
                
        Member member = Member.getEmptyMember();
        
        member.setRecentlyCreated(true);
        member.setHouseholdCode(txtNewMemHouseCode.getText().toString());
        member.setHouseholdName(txtNewMemHouseName.getText().toString());
        member.setCode(txtNewMemCode.getText().toString());
        member.setName(txtNewMemName.getText().toString());
        member.setGender(chkNewMemGMale.isChecked() ? "M" : "F");
        member.setDob(StringUtil.format(GeneralUtil.getDate(dtpNewMemDob), "yyyy-MM-dd" ));
        member.setAge(GeneralUtil.getAge(GeneralUtil.getDate(dtpNewMemDob)));

        member.setStartType("ENU");


        if (!isTempMember && !member.getCode().matches("[A-Z0-9]{6}[0-9]{6}")){
            DialogFactory.createMessageInfo(getActivity(), R.string.info_lbl, R.string.member_list_newmem_code_err_lbl).show();
            txtNewMemCode.requestFocus();            
            return null;
        }
        if (member.getName().trim().isEmpty()){
            DialogFactory.createMessageInfo(getActivity(), R.string.info_lbl, R.string.member_list_newmem_name_err_lbl).show();
            txtNewMemName.requestFocus();
            return null;
        }
        if (member.getDobDate().after(new Date())){
            DialogFactory.createMessageInfo(getActivity(), R.string.info_lbl, R.string.member_list_newmem_dob_err_lbl).show();
            return null;
        }

        if (chkNewMemGFemale.isChecked() && chkNewMemGMale.isChecked()){
            DialogFactory.createMessageInfo(getActivity(), R.string.info_lbl, R.string.member_list_newmem_gender_err1_lbl).show();
            return null;
        }

        if (!chkNewMemGFemale.isChecked() && !chkNewMemGMale.isChecked()){
            DialogFactory.createMessageInfo(getActivity(), R.string.info_lbl, R.string.member_list_newmem_gender_err2_lbl).show();
            return null;
        }

        //check if permid exists
        if (checkIfCodeExists(member.getCode())){
            DialogFactory.createMessageInfo(getActivity(), R.string.info_lbl, R.string.member_list_newmem_code_exists_lbl).show();
            txtNewMemCode.requestFocus();
            return null;
        }
        
        return member;
    }


    /* Database Usefull Methods */
    private String generateMemberCode(Household household){

        if (isTempMember){
            return household.getCode()+"XXX"; //CREATE A TEMPORARY CODE, THE EDITEXT IS EDITABLE
        }

        String baseId = household.getCode();
        String generatedId = null;

        /*
        String[] columns = new String[] {DatabaseHelper.Member.COLUMN_CODE};
        String where = DatabaseHelper.Member.COLUMN_CODE + " LIKE ?";
        String[] whereArgs = new String[] { baseId + "%" };
        String orderBy = DatabaseHelper.Member.COLUMN_CODE + " DESC";
        */

        Member member = this.boxMembers.query().startsWith(Member_.code, baseId).orderDesc(Member_.code).build().findFirst();

        if (member != null) {
            String lastGeneratedId = member.getCode();

            try { //ACA 000001 001
                int increment = Integer.parseInt(lastGeneratedId.substring(9));
                generatedId = baseId + String.format("%03d", increment+1);
            } catch (NumberFormatException e) {
                return baseId + "ERROR_01";
            }

        } else { //no Code based on "baseId"
            generatedId = baseId + "001"; //set the first id of individual household
        }

        return generatedId;
    }

    private boolean checkIfCodeExists(String code){
        Member member = Queries.getMemberByCode(this.boxMembers, code);

        return member != null;
    }

    public void show(){
        this.show(fragmentManager, "memform");
    }

    public interface Listener {

        void onNewMemberCreated(Member member);

        void onCancelClicked();

    }

}
