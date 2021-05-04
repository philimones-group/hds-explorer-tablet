package org.philimone.hds.explorer.fragment;

import android.app.DialogFragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.MemberArrayAdapter;
import org.philimone.hds.explorer.database.Converter;
import org.philimone.hds.explorer.database.Database;
import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.widget.NumberPicker;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MemberFilterDialog extends DialogFragment {

    private TextView txtDialogTitle;
    private EditText txtMemFilterName;
    private EditText txtMemFilterCode;
    private EditText txtMemFilterHouseCode;
    private CheckBox chkMemFilterGFemale;
    private CheckBox chkMemFilterGMale;
    private NumberPicker nbpMemFilterMinAge;
    private NumberPicker nbpMemFilterMaxAge;
    private Spinner spnMemFilterStatus;
    private ListView lvMembersList;
    private Button btMemFilterClear;
    private Button btMemFilterSearch;

    private boolean genderMaleOnly;
    private boolean genderFemaleOnly;

    private String filterName;
    private String filterCode;
    private String filterHouseCode;
    private Integer filterMinAge;
    private boolean filterMinAgeExclusive;
    private Integer filterMaxAge;
    private boolean filterMaxAgeExclusive;
    private Integer filterStatus;

    public enum StatusFilter {
        EMPTY, IS_DEAD, OUTMIGRATED, RESIDENT
    }

    private View mProgressView;

    private Database database;

    private Listener listener;

    private String title;

    public MemberFilterDialog(){
        super();
    }

    public static MemberFilterDialog newInstance(Listener memberFilterListener, String title){
        MemberFilterDialog filterDialog = new MemberFilterDialog();

        filterDialog.listener = memberFilterListener;
        filterDialog.title = title;

        return filterDialog;
    }

    public static MemberFilterDialog newInstance(Listener memberFilterListener, String title, boolean cancelable){
        MemberFilterDialog filterDialog = new MemberFilterDialog();

        filterDialog.listener = memberFilterListener;
        filterDialog.title = title;
        filterDialog.setCancelable(cancelable);

        return filterDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.member_filter_dialog, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().setTitle(title==null ? "" : title);

        initialize(view);
    }

    private void initialize(View view) {
        this.database = new Database(getActivity());

        this.txtDialogTitle = (TextView) view.findViewById(R.id.txtDialogTitle);
        this.txtMemFilterName = (EditText) view.findViewById(R.id.txtMemFilterName);
        this.txtMemFilterCode = (EditText) view.findViewById(R.id.txtMemFilterCode);
        this.txtMemFilterHouseCode = (EditText) view.findViewById(R.id.txtMemFilterCurrHousecode);
        this.chkMemFilterGFemale = (CheckBox) view.findViewById(R.id.chkMemFilterGFemale);
        this.chkMemFilterGMale = (CheckBox) view.findViewById(R.id.chkMemFilterGMale);
        this.spnMemFilterStatus = (Spinner) view.findViewById(R.id.spnMemFilterStatus);
        this.nbpMemFilterMinAge = (NumberPicker) view.findViewById(R.id.nbpMemFilterMinAge);
        this.nbpMemFilterMaxAge = (NumberPicker) view.findViewById(R.id.nbpMemFilterMaxAge);
        this.spnMemFilterStatus = (Spinner) view.findViewById(R.id.spnMemFilterStatus);
        this.mProgressView = view.findViewById(R.id.viewListProgressBar);
        this.lvMembersList = (ListView) view.findViewById(R.id.lvMembersList);
        this.btMemFilterClear = (Button) view.findViewById(R.id.btMemFilterClear);
        this.btMemFilterSearch = (Button) view.findViewById(R.id.btMemFilterSearch);

        this.btMemFilterClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
            }
        });

        this.btMemFilterSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearch();
            }
        });

        if (txtMemFilterCode.getText().length()>0){
            onSearch();
        }

        this.lvMembersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MemberArrayAdapter adapter = (MemberArrayAdapter) lvMembersList.getAdapter();
                Member member = adapter.getItem(position);

                onMemberClicked(member);
            }
        });

        if (genderMaleOnly){
            updateGenderMaleOnly();
        }

        if (genderFemaleOnly){
            updateGenderFemaleOnly();
        }

        if (filterName != null){
            txtMemFilterName.setText(filterName);
        }
        if (filterCode != null){
            txtMemFilterCode.setText(filterCode);
        }
        if (filterHouseCode != null){
            txtMemFilterHouseCode.setText(filterHouseCode);
        }
        if (filterMinAge != null){
            nbpMemFilterMinAge.setValue(filterMinAge);
            nbpMemFilterMinAge.setEnabled(!filterMinAgeExclusive);
        }
        if (filterMaxAge != null){
            nbpMemFilterMinAge.setValue(filterMaxAge);
            nbpMemFilterMaxAge.setEnabled(!filterMaxAgeExclusive);
        }
        if (filterStatus != null){
            spnMemFilterStatus.setSelection(filterStatus);
        }

        if (title != null){
            this.txtDialogTitle.setText(title);
        }

        initializeSpinners();
    }

    private void initializeSpinners(){
        String[] statuses = getContext().getResources().getStringArray(R.array.member_current_status);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), R.layout.spinner_list_item, statuses);
        spnMemFilterStatus.setAdapter(adapter);
    }

    private void onSearch() {
        String name = txtMemFilterName.getText().toString();
        String permId = txtMemFilterCode.getText().toString();
        String houseNumber = txtMemFilterHouseCode.getText().toString();
        String gender = (chkMemFilterGMale.isChecked() && chkMemFilterGFemale.isChecked()) ? "" : chkMemFilterGMale.isChecked() ? "M" : chkMemFilterGFemale.isChecked() ? "F" : "";

        int minAge = this.nbpMemFilterMinAge.getValue();
        int maxAge = this.nbpMemFilterMaxAge.getValue();
        int status = this.spnMemFilterStatus.getSelectedItemPosition();
        boolean isDead = status == 1;
        boolean hasOutmigrated = status == 2;
        boolean liveResident = status == 3;

        showProgress(true);
        MemberSearchTask task = new MemberSearchTask(name, permId, houseNumber, gender, minAge, maxAge, isDead, hasOutmigrated, liveResident);
        task.execute();
    }

    private void clear(){
        this.txtMemFilterName.setText("");
        this.txtMemFilterCode.setText("");
        this.txtMemFilterHouseCode.setText("");
        this.chkMemFilterGMale.setChecked(false);
        this.chkMemFilterGFemale.setChecked(false);
        this.nbpMemFilterMinAge.setValue(0);
        this.nbpMemFilterMaxAge.setValue(120);
        //this.spnMemFilterStatus.
    }

    private void updateGenderMaleOnly(){
        this.chkMemFilterGMale.setChecked(true);
        this.chkMemFilterGFemale.setChecked(false);

        this.chkMemFilterGMale.setEnabled(false);
        this.chkMemFilterGFemale.setEnabled(false);
    }

    private void updateGenderFemaleOnly(){
        this.chkMemFilterGMale.setChecked(false);
        this.chkMemFilterGFemale.setChecked(true);

        this.chkMemFilterGMale.setEnabled(false);
        this.chkMemFilterGFemale.setEnabled(false);
    }

    public void setGenderMaleOnly(){
        this.genderMaleOnly = true;
    }

    public void setGenderFemaleOnly(){
        this.genderFemaleOnly = true;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public void setFilterCode(String filterCode) {
        this.filterCode = filterCode;
    }

    public void setFilterHouseCode(String filterHouseCode) {
        this.filterHouseCode = filterHouseCode;
    }

    public void setFilterMinAge(int filterMinAge, boolean exclusive) {
        this.filterMinAge = filterMinAge;
        this.filterMinAgeExclusive = exclusive;
    }

    public void setFilterMaxAge(int filterMaxAge, boolean exclusive) {
        this.filterMaxAge = filterMaxAge;
        this.filterMaxAgeExclusive = exclusive;
    }

    public void setFilterStatus(StatusFilter filter) {
        switch (filter){
            case EMPTY: this.filterStatus = 0; break;
            case IS_DEAD: this.filterStatus = 1; break;
            case OUTMIGRATED: this.filterStatus = 2; break;
            case RESIDENT: this.filterStatus = 3; break;
        }
    }

    private void onMemberClicked(Member member){
        dismiss();

        if (this.listener != null){
            this.listener.onSelectedMember(member);
        }
    }

    public MemberArrayAdapter loadMembersByFilters(String name, String code, String houseCode, String gender, Integer minAge, Integer maxAge, Boolean isDead, Boolean hasOutmigrated, Boolean liveResident) {

        String endType = "";

        if (name == null) name = "";
        if (code == null) code = "";
        if (houseCode == null) houseCode = "";
        if (gender == null) gender = "";
        if (isDead != null && isDead) endType = "DTH";
        if (hasOutmigrated != null && hasOutmigrated) endType = "EXT";
        if (liveResident != null && liveResident) endType = "NA";


        //search on database
        List<Member> members = new ArrayList<>();
        List<String> whereValues = new ArrayList<>();
        String whereClause = "";

        if (!name.isEmpty()) {
            whereClause = DatabaseHelper.Member.COLUMN_NAME + " like ?";
            whereValues.add(name+"%");
        }
        if (!code.isEmpty()){
            whereClause += (whereClause.isEmpty()? "" : " AND ");
            whereClause += DatabaseHelper.Member.COLUMN_CODE + " like ?";
            whereValues.add(code+"%");
        }
        if (!houseCode.isEmpty()){
            whereClause += (whereClause.isEmpty()? "" : " AND ");
            whereClause += DatabaseHelper.Member.COLUMN_HOUSEHOLD_CODE + " like ?";
            whereValues.add(houseCode+"%");
        }
        if (!gender.isEmpty()){
            whereClause += (whereClause.isEmpty()? "" : " AND ");
            whereClause += DatabaseHelper.Member.COLUMN_GENDER + " = ?";
            whereValues.add(gender);
        }

        if (endType != null && endType=="DTH"){
            if (minAge != null){
                whereClause += (whereClause.isEmpty()? "" : " AND ");
                whereClause += DatabaseHelper.Member.COLUMN_AGE_AT_DEATH + " >= ?";
                whereValues.add(minAge.toString());
            }
            if (maxAge != null){
                whereClause += (whereClause.isEmpty()? "" : " AND ");
                whereClause += DatabaseHelper.Member.COLUMN_AGE_AT_DEATH + " <= ?";
                whereValues.add(maxAge.toString());
            }
        }else {
            if (minAge != null){
                whereClause += (whereClause.isEmpty()? "" : " AND ");
                whereClause += DatabaseHelper.Member.COLUMN_AGE + " >= ?";
                whereValues.add(minAge.toString());
            }
            if (maxAge != null){
                whereClause += (whereClause.isEmpty()? "" : " AND ");
                whereClause += DatabaseHelper.Member.COLUMN_AGE + " <= ?";
                whereValues.add(maxAge.toString());
            }
        }

        if (!endType.isEmpty()){
            whereClause += (whereClause.isEmpty()? "" : " AND ");
            whereClause += DatabaseHelper.Member.COLUMN_END_TYPE + " = ?";
            whereValues.add(endType);
        }


        database.open();

        String[] ar = new String[whereValues.size()];
        Cursor cursor = database.query(Member.class, DatabaseHelper.Member.ALL_COLUMNS, whereClause, whereValues.toArray(ar), null, null, DatabaseHelper.Member.COLUMN_CODE);

        while (cursor.moveToNext()){
            Member member = Converter.cursorToMember(cursor);
            members.add(member);
            //Log.d("household", ""+household);
            //Log.d("head", ""+(household!=null ? household.getHeadPermId():"null"));
        }

        database.close();

        MemberArrayAdapter currentAdapter = new MemberArrayAdapter(this.getActivity(), members);

        return currentAdapter;

    }

    public void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        lvMembersList.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public void setMemberAdapter(MemberArrayAdapter memberAdapter) {
        this.lvMembersList.setAdapter(memberAdapter);
    }

    class MemberSearchTask extends AsyncTask<Void, Void, MemberArrayAdapter> {
        private String name;
        private String code;
        private String houseNumber;
        private String gender;
        private Integer minAge;
        private Integer maxAge;
        private Boolean isDead;
        private Boolean hasOutmigrated;
        private Boolean liveResident;

        public MemberSearchTask(String name, String code, String houseNumber, String gender, Integer minAge, Integer maxAge, Boolean isDead, Boolean hasOutmigrated, Boolean liveResident) {
            this.name = name;
            this.code = code;
            this.houseNumber = houseNumber;
            this.gender = gender;
            this.minAge = minAge;
            this.maxAge = maxAge;
            this.isDead = isDead;
            this.hasOutmigrated = hasOutmigrated;
            this.liveResident = liveResident;
        }

        @Override
        protected MemberArrayAdapter doInBackground(Void... params) {
            return loadMembersByFilters(name, code, houseNumber, gender, minAge, maxAge, isDead, hasOutmigrated, liveResident);
        }

        @Override
        protected void onPostExecute(MemberArrayAdapter adapter) {
            lvMembersList.setAdapter(adapter);
            showProgress(false);
        }
    }

    public interface Listener {
        void onSelectedMember(Member member);
    }

}
