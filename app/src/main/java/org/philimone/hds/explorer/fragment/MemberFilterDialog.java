package org.philimone.hds.explorer.fragment;

import androidx.fragment.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.widget.NumberPicker;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.TextFilters;

/**
 *
 */
public class MemberFilterDialog extends DialogFragment {

    private FragmentManager fragmentManager;

    private TextView txtDialogTitle;
    private Button btDialogClose;
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
    private boolean filterStatusExclusive;
    private String filterExcludeHousehold;
    private String filterExcludeMember;

    private boolean startSearchOnShow;

    public enum StatusFilter {
        EMPTY, IS_DEAD, OUTMIGRATED, RESIDENT
    }

    private View progressBarLayout;
    private Box<Member> boxMembers;

    private Listener listener;

    private String title;

    public MemberFilterDialog(){
        super();
        initBoxes();
    }
    /*
    public static MemberFilterDialog newInstance(FragmentManager fm, @StringRes int titleResId, Listener memberFilterListener){
        return newInstance(fm, titleResId, false, memberFilterListener);
    }

    public static MemberFilterDialog newInstance(FragmentManager fm, @StringRes int titleResId, boolean cancelable, Listener memberFilterListener){
        MemberFilterDialog filterDialog = newInstance(fm, "", cancelable, memberFilterListener);
        filterDialog.title = filterDialog.getString(titleResId);
        return filterDialog;
    }*/

    public static MemberFilterDialog newInstance(FragmentManager fm, String title, Listener memberFilterListener){
        return newInstance(fm, title, false, memberFilterListener);
    }

    public static MemberFilterDialog newInstance(FragmentManager fm, String title, boolean cancelable, Listener memberFilterListener){
        MemberFilterDialog filterDialog = new MemberFilterDialog();

        filterDialog.fragmentManager = fm;
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

    private void initBoxes() {
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
    }

    private void initialize(View view) {

        this.txtDialogTitle = (TextView) view.findViewById(R.id.txtDialogTitle);
        this.btDialogClose = (Button) view.findViewById(R.id.btDialogClose);
        this.txtMemFilterName = (EditText) view.findViewById(R.id.txtMemFilterName);
        this.txtMemFilterCode = (EditText) view.findViewById(R.id.txtMemFilterCode);
        this.txtMemFilterHouseCode = (EditText) view.findViewById(R.id.txtMemFilterCurrHousecode);
        this.chkMemFilterGFemale = (CheckBox) view.findViewById(R.id.chkMemFilterGFemale);
        this.chkMemFilterGMale = (CheckBox) view.findViewById(R.id.chkMemFilterGMale);
        this.spnMemFilterStatus = (Spinner) view.findViewById(R.id.spnMemFilterStatus);
        this.nbpMemFilterMinAge = (NumberPicker) view.findViewById(R.id.nbpMemFilterMinAge);
        this.nbpMemFilterMaxAge = (NumberPicker) view.findViewById(R.id.nbpMemFilterMaxAge);
        this.spnMemFilterStatus = (Spinner) view.findViewById(R.id.spnMemFilterStatus);
        this.progressBarLayout = view.findViewById(R.id.progressBarLayout);
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

        this.btDialogClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCancelable()) {
                    closeDialog();
                }
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

        if (title != null){
            this.txtDialogTitle.setText(title);
        }

        initializeSpinners();

        updateFilterStatus();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (startSearchOnShow) {
            onSearch();
        }
    }

    private void initializeSpinners(){
        String[] statuses = getContext().getResources().getStringArray(R.array.member_current_status);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), R.layout.spinner_list_item, statuses);

        //adapter.setDropDownViewResource(R.layout.spinner_list_head);

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

    private void updateFilterStatus(){
        if (this.spnMemFilterStatus != null && filterStatus != null) {
            this.spnMemFilterStatus.setSelection(filterStatus);
            this.spnMemFilterStatus.setEnabled(!filterStatusExclusive);
        }
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

    public void setFilterExcludeHousehold(String householdCode) {
        this.filterExcludeHousehold = householdCode;
    }

    public void setFilterExcludeMember(String memberCode) {
        this.filterExcludeMember = memberCode;
    }

    public void setFilterStatus(StatusFilter filter, boolean exclusiveSelection) {
        switch (filter){
            case EMPTY: this.filterStatus = 0; break;
            case IS_DEAD: this.filterStatus = 1; break;
            case OUTMIGRATED: this.filterStatus = 2; break;
            case RESIDENT: this.filterStatus = 3; break;
        }

        this.filterStatusExclusive = exclusiveSelection;

        updateFilterStatus();
    }

    public void setStartSearchOnShow(boolean searchOnShow) {
        this.startSearchOnShow = searchOnShow;
    }

    private void onMemberClicked(Member member){
        dismiss();

        if (this.listener != null){
            this.listener.onSelectedMember(member);
        }
    }
                            //Household household, String name, String code, String householdCode, String gender, Integer minAge, Integer maxAge, Boolean isDead, Boolean hasOutmigrated, Boolean liveResident
    private MemberArrayAdapter loadMembersByFilters(String name, String code, String householdCode, String gender, Integer minAge, Integer maxAge, Boolean isDead, Boolean hasOutmigrated, Boolean liveResident) {

        ResidencyEndType endType = null;

        if (name == null) name = "";
        if (code == null) code = "";
        if (householdCode == null) householdCode = "";
        if (gender == null) gender = "";
        if (isDead != null && isDead) endType = ResidencyEndType.DEATH;
        if (hasOutmigrated != null && hasOutmigrated) endType = ResidencyEndType.EXTERNAL_OUTMIGRATION;
        if (liveResident != null && liveResident) endType = ResidencyEndType.NOT_APPLICABLE;

        //search on database
        QueryBuilder<Member> builder = this.boxMembers.query();

        if (!name.isEmpty()) {

            TextFilters filter = new TextFilters(name);
            String text = filter.getFilterText();
            switch (filter.getFilterType()) {
                case STARTSWITH:
                    builder.startsWith(Member_.name, text, QueryBuilder.StringOrder.CASE_SENSITIVE);
                    break;
                case ENDSWITH:
                    builder.endsWith(Member_.name, text, QueryBuilder.StringOrder.CASE_SENSITIVE);
                    break;
                case CONTAINS:
                    builder.contains(Member_.name, text, QueryBuilder.StringOrder.CASE_SENSITIVE);
                    break;
                case MULTIPLE_CONTAINS:
                    for (String t : filter.getFilterTexts()) {
                        builder.contains(Member_.name, t, QueryBuilder.StringOrder.CASE_SENSITIVE);
                    }                    
                    break;
                case NONE:
                    builder.equal(Member_.name, text, QueryBuilder.StringOrder.CASE_SENSITIVE);
                    break;
                case EMPTY:
                    break;
            }
            //whereClause = DatabaseHelper.Member.COLUMN_NAME + " like ?";
        }
        if (!code.isEmpty()){

            TextFilters filter = new TextFilters(code);
            String text = filter.getFilterText();
            switch (filter.getFilterType()) {
                case STARTSWITH:
                    builder.startsWith(Member_.code, text, QueryBuilder.StringOrder.CASE_SENSITIVE);
                    break;
                case ENDSWITH:
                    builder.endsWith(Member_.code, text, QueryBuilder.StringOrder.CASE_SENSITIVE);
                    Log.d("running-code", "endswith="+text);
                    break;
                case CONTAINS:
                    builder.contains(Member_.code, text, QueryBuilder.StringOrder.CASE_SENSITIVE);
                    break;
                case MULTIPLE_CONTAINS:
                    for (String t : filter.getFilterTexts()) {
                        builder.contains(Member_.code, t, QueryBuilder.StringOrder.CASE_SENSITIVE);
                    }
                    break;
                case NONE:
                    builder.equal(Member_.code, text, QueryBuilder.StringOrder.CASE_SENSITIVE);
                    break;
                case EMPTY:
                    break;
            }
            //whereClause += DatabaseHelper.Member.COLUMN_CODE + " like ?";            
        }
        if (!householdCode.isEmpty()){
            TextFilters filter = new TextFilters(householdCode);
            String text = filter.getFilterText();
            switch (filter.getFilterType()) {
                case STARTSWITH:
                    builder.startsWith(Member_.householdCode, text, QueryBuilder.StringOrder.CASE_SENSITIVE);
                    break;
                case ENDSWITH:
                    builder.endsWith(Member_.householdCode, text, QueryBuilder.StringOrder.CASE_SENSITIVE);
                    break;
                case CONTAINS:
                    builder.contains(Member_.householdCode, text, QueryBuilder.StringOrder.CASE_SENSITIVE);
                    break;
                case MULTIPLE_CONTAINS:
                    for (String t : filter.getFilterTexts()) {
                        builder.contains(Member_.householdCode, t, QueryBuilder.StringOrder.CASE_SENSITIVE);
                    }
                    break;
                case NONE:
                    builder.equal(Member_.householdCode, text, QueryBuilder.StringOrder.CASE_SENSITIVE);
                    break;
                case EMPTY:
                    break;
            }
            //whereClause += DatabaseHelper.Member.COLUMN_HOUSEHOLD_CODE + " like ?";
            
        }
        if (!gender.isEmpty()){
            builder.equal(Member_.gender, gender, QueryBuilder.StringOrder.CASE_SENSITIVE);
            //whereClause += DatabaseHelper.Member.COLUMN_GENDER + " = ?";
        }

        if (endType != null && endType==ResidencyEndType.DEATH){
            if (minAge != null){
                builder.greaterOrEqual(Member_.ageAtDeath, minAge);
                //whereClause += DatabaseHelper.Member.COLUMN_AGE_AT_DEATH + " >= ?";
            }
            if (maxAge != null){
                builder.lessOrEqual(Member_.ageAtDeath, maxAge);
                //whereClause += DatabaseHelper.Member.COLUMN_AGE_AT_DEATH + " <= ?";
            }
        }else {
            if (minAge != null){
                builder.greaterOrEqual(Member_.age, minAge);
                //whereClause += DatabaseHelper.Member.COLUMN_AGE + " >= ?";
            }
            if (maxAge != null){
                builder.lessOrEqual(Member_.age, maxAge);
                //whereClause += DatabaseHelper.Member.COLUMN_AGE + " <= ?";
            }
        }

        if (endType != null){
            builder.equal(Member_.endType, endType.code, QueryBuilder.StringOrder.CASE_SENSITIVE);
            //whereClause += DatabaseHelper.Member.COLUMN_END_TYPE + " = ?";
        }

        if (filterExcludeHousehold != null) {
            builder.notEqual(Member_.householdCode, filterExcludeHousehold, QueryBuilder.StringOrder.CASE_SENSITIVE);
        }

        if (filterExcludeMember != null) {
            builder.notEqual(Member_.code, filterExcludeMember, QueryBuilder.StringOrder.CASE_SENSITIVE);
        }

        Log.d("sql", builder.toString());

        List<Member> members = builder.build().find();

        MemberArrayAdapter currentAdapter = new MemberArrayAdapter(this.getActivity(), members);

        return currentAdapter;
    }

    private void showProgress(final boolean show) {
        //lvMembersList.setAdapter(null);
        progressBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void show(){
        this.show(fragmentManager, "relatype");
    }

    private void closeDialog(){
        dismiss();

        if (this.listener != null){
            this.listener.onCanceled();
        }
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

        void onCanceled();
    }

}
