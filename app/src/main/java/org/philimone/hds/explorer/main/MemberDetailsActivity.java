package org.philimone.hds.explorer.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.fragment.CollectedDataFragment;
import org.philimone.hds.explorer.fragment.ExternalDatasetsFragment;
import org.philimone.hds.explorer.fragment.member.details.MemberDetailsFragment;
import org.philimone.hds.explorer.fragment.member.details.MemberEditFragment;
import org.philimone.hds.explorer.fragment.member.details.adapter.MemberDetailsFragmentAdapter;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.followup.TrackingSubjectList;
import org.philimone.hds.explorer.settings.RequestCodes;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import io.objectbox.Box;
import mz.betainteractive.utilities.StringUtil;

public class MemberDetailsActivity extends AppCompatActivity {

    private TabLayout memberDetailsTabLayout;
    private ViewPager2 memberDetailsTabViewPager;
    private RelativeLayout mainPanelTabsLayout;

    private TextView mbDetailsName;
    private TextView mbDetailsCode;
    private TextView mbDetailsGender;
    private TextView mbDetailsAge;
    private TextView mbDetailsDob;
    private Button btMemDetailsCollectData;
    private Button btMemDetailsBack;
    private ImageView iconView;

    private Household household;
    private Member member;
    private boolean isNewTempMember;
    private List<FormDataLoader> formDataLoaders = new ArrayList<>();

    private Box<Member> boxMembers;
    private Box<Household> boxHousehold;
    private Box<CollectedData> boxCollectedData;
    private Box<TrackingSubjectList> boxTrackingSubjectList;

    private TrackingSubjectList trackingSubject;
    private MemberDetailsFragmentAdapter fragmentAdapter;

    private User loggedUser;

    private int requestCode;

    private CollectedData autoHighlightCollectedData;
    private boolean autoClickCollectData;

    //public static final int REQUEST_CODE_ADD_NEW_MEMBER = 10; /* Member Requests will be from 10 to 19 */
    //public static final int REQUEST_CODE_EDIT_NEW_MEMBER = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_details);

        this.loggedUser = Bootstrap.getCurrentUser();

        initBoxes();
        readIntentData();
        initialize();
        initFragments();
        enableButtonsByFormLoaders();
    }

    private void readIntentData() {
        long householdId = getIntent().getExtras().getLong("household");
        long memberId = getIntent().getExtras().getLong("member");
        this.requestCode = getIntent().getExtras().getInt("request_code");

        try {
            this.household = boxHousehold.get(householdId);
        }catch (Exception ex) {
            Log.d("intent-error", ex.getMessage());
        }

        try {
            this.member = boxMembers.get(memberId);
        } catch (Exception ex) {
            Log.d("intent-error", ex.getMessage());
        }

        if (getIntent().getExtras().containsKey("tracking_subject_id")) {
            this.trackingSubject = boxTrackingSubjectList.get(getIntent().getExtras().getLong("tracking_subject_id"));
        }

        if (getIntent().getExtras().containsKey("odk-form-select")) {
            long collectedDataId = getIntent().getExtras().getLong("odk-form-select");
            this.autoHighlightCollectedData = boxCollectedData.get(collectedDataId);
        }

        if (getIntent().getExtras().containsKey("odk-form-collect")) {
            this.autoClickCollectData = true;
        }
    }

    private boolean isMemberVisualizableForm(Form form) {
        if (requestCode != RequestCodes.MEMBER_DETAILS_FROM_TRACKING_LIST_DETAILS){ //MemberDetails was not opened via Tracking/FollowUp lists
            if (form.isFollowUpForm()){ //forms flagged with followUpOnly can only be opened using FollowUp Lists, to be able to open via normal surveys remove the flag on the server
                return false;
            }
        }

        if (form.isHouseholdForm()){ //Dont Show Household Form
            return false;
        }

        if (form.isHouseholdHeadForm() && (!member.isHouseholdHead() || !member.isSecHouseholdHead() )){ //Dont show HouseholdHead Form for non-head members
            return false;
        }

        return  (member.getAge() >= form.getMinAge() && member.getAge() <= form.getMaxAge()) && (member.getGender().equals(form.getGender()) || form.getGender().equals("ALL")) && (form.isMemberForm());
    }

    public void setMember(Member member){
        this.member = member;
    }

    private void initBoxes() {
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxHousehold = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxTrackingSubjectList = ObjectBoxDatabase.get().boxFor(TrackingSubjectList.class);
    }

    private void initialize() {
        mbDetailsName = (TextView) findViewById(R.id.mbDetailsName);
        mbDetailsCode = (TextView) findViewById(R.id.mbDetailsCode);
        mbDetailsGender = (TextView) findViewById(R.id.mbDetailsGender);
        mbDetailsAge = (TextView) findViewById(R.id.mbDetailsAge);
        mbDetailsDob = (TextView) findViewById(R.id.mbDetailsDob);
        btMemDetailsCollectData = (Button) findViewById(R.id.btMemDetailsCollectData);
        btMemDetailsBack = (Button) findViewById(R.id.btMemDetailsBack);
        iconView = (ImageView) findViewById(R.id.iconView);
        memberDetailsTabLayout = findViewById(R.id.memberDetailsTabLayout);
        memberDetailsTabViewPager = findViewById(R.id.memberDetailsTabViewPager);
        mainPanelTabsLayout = findViewById(R.id.mainPanelTabsLayout);

        btMemDetailsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemberDetailsActivity.this.onBackPressed();
            }
        });

        btMemDetailsCollectData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCollectDataClicked();
            }
        });

        isNewTempMember = member!=null && member.getId()==0;

        clearMemberData();
        setMemberData();
    }

    @Override
    public void onBackPressed() {
        onBackClicked();
        //super.onBackPressed();
    }

    private void onBackClicked() {
        Intent resultIntent = new Intent();

        resultIntent.putExtra("household_code", member.getHouseholdCode());
        resultIntent.putExtra("is_new_temp_member", isNewTempMember);
        setResult(AppCompatActivity.RESULT_OK, resultIntent);
        finish();
    }

    private void enableButtonsByFormLoaders() {
        boolean hasForms = this.fragmentAdapter.getFragmentCollected().getFormDataLoaders().size()>0;
        this.btMemDetailsCollectData.setEnabled(hasForms);
    }

    private void initFragments() {

        if (member != null && fragmentAdapter == null) {
            List<String> tabTitles = new ArrayList<>();
            tabTitles.add(getString(R.string.member_details_tab_details_list_lbl));
            tabTitles.add(getString(R.string.member_details_tab_datasets_lbl));
            tabTitles.add(getString(R.string.member_details_tab_collected_forms_lbl));
            tabTitles.add(getString(R.string.member_details_tab_edit_lbl));

            boolean isTracking = requestCode == RequestCodes.MEMBER_DETAILS_FROM_TRACKING_LIST_DETAILS;

            fragmentAdapter = new MemberDetailsFragmentAdapter(this.getSupportFragmentManager(), this.getLifecycle(), household, member, loggedUser, trackingSubject, tabTitles);
            fragmentAdapter.setAutoHighlightCollectedData(autoHighlightCollectedData);
            fragmentAdapter.setAutoClickCollectData(autoClickCollectData);
            memberDetailsTabViewPager.setAdapter(fragmentAdapter);
            fragmentAdapter.setFragmentEditListener(new MemberEditFragment.EditListener() {
                @Override
                public void onUpdate() {
                    setMemberData();
                    reloadFragmentsData();
                }
            });

            fragmentAdapter.setFragmentCollectListener(new CollectedDataFragment.CollectedDataFragmentListener() {
                @Override
                public void onCollectDataBackClicked() {
                    autoClickCollectData = false;
                    onBackPressed();
                }
            });

            //this will create all fragments
            memberDetailsTabViewPager.setOffscreenPageLimit(4);

            new TabLayoutMediator(memberDetailsTabLayout, memberDetailsTabViewPager, (tab, position) -> {
                tab.setText(fragmentAdapter.getTitle(position));
            }).attach();

            if (autoHighlightCollectedData != null) {
                this.memberDetailsTabLayout.getTabAt(2).select();
            }
        }


    }

    private void reloadFragmentsData(){
        if (fragmentAdapter != null) {
            MemberDetailsFragment membersFragment = this.fragmentAdapter.getFragmentMemberDetails();
            ExternalDatasetsFragment datasetsFragment = this.fragmentAdapter.getFragmentDatasets();
            CollectedDataFragment collectedDataFragment = this.fragmentAdapter.getFragmentCollected();

            if (membersFragment != null) {
                membersFragment.refreshMemberData();
            }

            if (datasetsFragment != null) {
                //datasetsFragment.
            }

            if (collectedDataFragment != null) {
                collectedDataFragment.reloadCollectedData();
            }
        }
    }

    private void clearMemberData(){
        mbDetailsName.setText("");
        mbDetailsCode.setText("");
        mbDetailsGender.setText("");
        mbDetailsAge.setText("");
        mbDetailsDob.setText("");
    }

    private void setMemberData(){

        this.member = this.boxMembers.get(member.id);

        mbDetailsName.setText(member.getName());
        mbDetailsCode.setText(member.getCode());
        mbDetailsGender.setText(member.getGender().getId());
        mbDetailsAge.setText(member.getAge()+"");
        mbDetailsDob.setText(StringUtil.formatYMD(member.dob));

        if (member.isHouseholdHead()){
            iconView.setImageResource(R.mipmap.nui_member_red_filled_icon);
        }

        if (member.isSecHouseholdHead()){
            iconView.setImageResource(R.mipmap.nui_member_red_filled_two_icon);
        }

        showCollectedData();

    }

    private void showCollectedData() {

    }

    private void onCollectDataClicked(){

        this.memberDetailsTabLayout.getTabAt(2).select();
        //this.householdDetailsTabViewPager.setCurrentItem(2, true);

        //Go to HouseholdFormsFragment and call this action
        CollectedDataFragment collectedDataFragment = this.fragmentAdapter.getFragmentCollected();
        collectedDataFragment.onCollectData();
    }
}
