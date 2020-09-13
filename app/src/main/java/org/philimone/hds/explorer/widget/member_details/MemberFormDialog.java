package org.philimone.hds.explorer.widget.member_details;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import org.philimone.hds.explorer.database.Database;
import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.widget.DialogFactory;

import java.util.Date;

import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

/**
 *
 */
public class MemberFormDialog extends DialogFragment {

    private FragmentManager fragmentManager;

    private TextView txtNewMemHouseCode;
    private TextView txtNewMemHouseName;
    private TextView txtNewMemCode;
    private EditText txtNewMemName;
    private RadioButton chkNewMemGMale;
    private RadioButton chkNewMemGFemale;
    private DatePicker dtpNewMemDob;
    private Button btNewMemCancel;
    private Button btNewMemCollect;

    private Database database;

    private Listener listener;

    private Household household;

    public MemberFormDialog(){
        super();
    }


    public static MemberFormDialog newInstance(FragmentManager fm, Household household, Listener listener){
        MemberFormDialog dialog = new MemberFormDialog();

        dialog.fragmentManager = fm;
        dialog.household = household;
        dialog.listener = listener;
        dialog.setCancelable(false);

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

        initialize(view);
    }

    private void initialize(View view) {
        this.database = new Database(getActivity());

        this.txtNewMemHouseCode = (TextView) view.findViewById(R.id.txtNewMemHouseCode);
        this.txtNewMemHouseName = (TextView) view.findViewById(R.id.txtNewMemHouseName);
        this.txtNewMemCode = (TextView) view.findViewById(R.id.txtNewMemCode);
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


        if (!member.getCode().matches("[A-Z0-9]{6}[0-9]{6}")){
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
        Database database = new Database(this.getActivity());
        database.open();
        String baseId = household.getCode();
        String[] columns = new String[] {DatabaseHelper.Member.COLUMN_CODE};
        String where = DatabaseHelper.Member.COLUMN_CODE + " LIKE ?";
        String[] whereArgs = new String[] { baseId + "%" };
        String orderBy = DatabaseHelper.Member.COLUMN_CODE + " DESC";
        String generatedId = null;

        Cursor cursor = database.query(Member.class, columns, where, whereArgs, null, null, orderBy);

        if (cursor.moveToFirst()) {
            String lastGeneratedId = cursor.getString(0);

            try { //ACA 000001 001
                int increment = Integer.parseInt(lastGeneratedId.substring(9));
                generatedId = baseId + String.format("%03d", increment+1);
            } catch (NumberFormatException e) {
                return baseId + "ERROR_01";
            }

        } else { //no Code based on "baseId"
            generatedId = baseId + "001"; //set the first id of individual household
        }

        cursor.close();
        database.close();

        return generatedId;
    }

    private boolean checkIfCodeExists(String code){
        Database database = new Database(this.getActivity());
        database.open();
        Member member = Queries.getMemberBy(database, DatabaseHelper.Member.COLUMN_CODE+"=?", new String[] { code });
        database.close();

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
