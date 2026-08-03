package org.philimone.hds.explorer.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.tabs.TabLayout;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.MemberAdapter;
import org.philimone.hds.explorer.adapter.PregnancyVisitExpandableAdapter;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.PregnancyChild;
import org.philimone.hds.explorer.model.PregnancyChild_;
import org.philimone.hds.explorer.model.PregnancyOutcome;
import org.philimone.hds.explorer.model.PregnancyOutcome_;
import org.philimone.hds.explorer.model.PregnancyRegistration;
import org.philimone.hds.explorer.model.PregnancyVisit;
import org.philimone.hds.explorer.model.PregnancyVisit_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.PregnancyOutcomeType;
import org.philimone.hds.explorer.model.enums.PregnancyStatus;
import org.philimone.hds.explorer.model.enums.PregnancyVisitType;
import org.philimone.hds.explorer.widget.RecyclerListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.GeneralUtil;

public class PregnancySurveillanceDialog extends DialogFragment implements PregnancyVisitExpandableAdapter.Listener {

    private FragmentManager fragmentManager;
    private Member mother;
    private Household household;
    private Visit currentVisit;
    private PregnancyRegistration pregnancyRegistration;

    private TextView txtDialogTitle, txtDialogSubtitle;
    private TextView txtMotherName, txtPregnancyCode, txtStatusPregnant, txtStatusDelivered, txtStatusLostTrack;
    private TextView txtLMPDate, txtGestWeeks, txtEDDDate, txtNextVisit;
    private TextView lblEDD, lblGestWeeks;
    private TextView txtComplicationWarning;
    private TabLayout tabLayout;
    private RelativeLayout layoutVisitsTab;
    private LinearLayout layoutOutcomesTab;
    private ExpandableListView elvPregnancyVisits;
    private TextView txtOutcomeDetails;
    private RecyclerListView lvPregnancyChildren;
    private Button btRegisterBirth, btDialogClose, btPanelBack;

    private Box<CoreCollectedData> boxCoreCollectedData;
    private Box<PregnancyRegistration> boxPregnancyRegistrations;
    private Box<PregnancyVisit> boxPregnancyVisits;
    private Box<PregnancyOutcome> boxPregnancyOutcomes;
    private Box<PregnancyChild> boxPregnancyChilds;
    private Box<Member> boxMembers;
    private Box<ApplicationParam> boxAppParams;

    private Listener listener;
    private DateUtil dateUtil;

    private int maxAntepartumVisits = 8;
    private int maxPostpartumVisits = 4;
    private List<Integer> antepartumSchedule = new ArrayList<>();
    private List<Integer> postpartumSchedule = new ArrayList<>();

    public PregnancySurveillanceDialog() {
        super();
        initBoxes();
        this.dateUtil = Bootstrap.getDateUtil();
        loadParameters();
    }

    public static PregnancySurveillanceDialog newInstance(FragmentManager fm, Member mother, Household household, Visit currentVisit, PregnancyRegistration pregnancyRegistration, Listener listener) {
        PregnancySurveillanceDialog dialog = new PregnancySurveillanceDialog();
        dialog.fragmentManager = fm;
        dialog.mother = mother;
        dialog.household = household;
        dialog.currentVisit = currentVisit;
        dialog.pregnancyRegistration = pregnancyRegistration;
        dialog.listener = listener;
        return dialog;
    }

    private void initBoxes() {
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
        this.boxPregnancyRegistrations = ObjectBoxDatabase.get().boxFor(PregnancyRegistration.class);
        this.boxPregnancyVisits = ObjectBoxDatabase.get().boxFor(PregnancyVisit.class);
        this.boxPregnancyOutcomes = ObjectBoxDatabase.get().boxFor(PregnancyOutcome.class);
        this.boxPregnancyChilds = ObjectBoxDatabase.get().boxFor(PregnancyChild.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
    }

    private void loadParameters() {
        ApplicationParam paramMaxAnt = boxAppParams.query(ApplicationParam_.name.equal(ApplicationParam.PARAMS_MAX_ANTEPARTUM_VISITS)).build().findFirst();
        ApplicationParam paramMaxPost = boxAppParams.query(ApplicationParam_.name.equal(ApplicationParam.PARAMS_MAX_POSTPARTUM_VISITS)).build().findFirst();
        ApplicationParam paramSchedAnt = boxAppParams.query(ApplicationParam_.name.equal(ApplicationParam.PARAMS_ANTEPARTUM_SCHEDULE)).build().findFirst();
        ApplicationParam paramSchedPost = boxAppParams.query(ApplicationParam_.name.equal(ApplicationParam.PARAMS_POSTPARTUM_SCHEDULE)).build().findFirst();

        if (paramMaxAnt != null) try { maxAntepartumVisits = Integer.parseInt(paramMaxAnt.value); } catch (Exception e) {}
        if (paramMaxPost != null) try { maxPostpartumVisits = Integer.parseInt(paramMaxPost.value); } catch (Exception e) {}

        String schedAntStr = paramSchedAnt != null ? paramSchedAnt.value : "12,20,26,30,34,36,38,40";
        String schedPostStr = paramSchedPost != null ? paramSchedPost.value : "3,7,21,42";

        antepartumSchedule = parseSchedule(schedAntStr);
        postpartumSchedule = parseSchedule(schedPostStr);
    }

    private List<Integer> parseSchedule(String schedStr) {
        List<Integer> list = new ArrayList<>();
        if (schedStr != null && !schedStr.isEmpty()) {
            schedStr = schedStr.replaceAll("\\s+","");
            String[] parts = schedStr.split(",");
            for (String part : parts) {
                try {
                    list.add(Integer.parseInt(part.trim()));
                } catch (Exception e) {}
            }
        }
        return list;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pregnancy_surveillance_panel, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    private void initialize(View view) {
        this.txtDialogTitle = view.findViewById(R.id.txtDialogTitle);
        this.txtDialogSubtitle = view.findViewById(R.id.txtDialogSubtitle);
        this.txtMotherName = view.findViewById(R.id.txtMotherName);
        this.txtPregnancyCode = view.findViewById(R.id.txtPregnancyCode);
        this.txtStatusPregnant = view.findViewById(R.id.txtStatusPregnant);
        this.txtStatusDelivered = view.findViewById(R.id.txtStatusDelivered);
        this.txtStatusLostTrack = view.findViewById(R.id.txtStatusLostTrack);
        this.txtLMPDate = view.findViewById(R.id.txtLMPDate);
        this.txtGestWeeks = view.findViewById(R.id.txtGestWeeks);
        this.lblGestWeeks = view.findViewById(R.id.lblGestWeeks);
        this.lblEDD = view.findViewById(R.id.lblEDD);
        this.txtEDDDate = view.findViewById(R.id.txtEDDDate);
        this.txtNextVisit = view.findViewById(R.id.txtNextVisit);
        this.txtComplicationWarning = view.findViewById(R.id.txtComplicationWarning);
        this.tabLayout = view.findViewById(R.id.tabLayout);
        this.layoutVisitsTab = view.findViewById(R.id.layoutVisitsTab);
        this.layoutOutcomesTab = view.findViewById(R.id.layoutOutcomesTab);
        this.elvPregnancyVisits = view.findViewById(R.id.elvPregnancyVisits);
        this.txtOutcomeDetails = view.findViewById(R.id.txtOutcomeDetails);
        this.lvPregnancyChildren = view.findViewById(R.id.lvPregnancyChildren);
        this.btRegisterBirth = view.findViewById(R.id.btRegisterBirth);
        this.btDialogClose = view.findViewById(R.id.btDialogClose);
        this.btPanelBack = view.findViewById(R.id.btPanelBack);

        this.btDialogClose.setOnClickListener(v -> dismiss());
        this.btPanelBack.setOnClickListener(v -> dismiss());
        this.btRegisterBirth.setOnClickListener(v -> onRegisterBirthClicked());

        this.elvPregnancyVisits.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            PregnancyVisitExpandableAdapter adapter = (PregnancyVisitExpandableAdapter) elvPregnancyVisits.getExpandableListAdapter();
            PregnancyVisit pregnancyVisit = (PregnancyVisit) adapter.getChild(groupPosition, childPosition);

            onChildPregnancyVisitClicked(pregnancyVisit);
            return false;
        });

        this.lvPregnancyChildren.addOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long id) {
                onChildMemberClicked(position);
            }

            @Override
            public void onItemLongClick(View view, int position, long id) { }
        });

        setupTabs();
        displayData();
    }

    private void setupTabs() {
        //tabLayout.addTab(tabLayout.newTab().setText(R.string.pregnancy_panel_tab_visits));
        //tabLayout.addTab(tabLayout.newTab().setText(R.string.pregnancy_panel_tab_outcomes));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    layoutVisitsTab.setVisibility(View.VISIBLE);
                    layoutOutcomesTab.setVisibility(View.GONE);
                } else {
                    layoutVisitsTab.setVisibility(View.GONE);
                    layoutOutcomesTab.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void displayMotherData() {
        if (mother != null) {
            //txtDialogTitle.setText(mother.name);
            txtDialogSubtitle.setText(getString(R.string.pregnancy_panel_mother_code_subtitle_lbl, mother.code));
            txtMotherName.setText(mother.name);
            txtPregnancyCode.setText(pregnancyRegistration.code);
            switch (pregnancyRegistration.status) {
                case PREGNANT:
                    txtStatusPregnant.setVisibility(View.VISIBLE);
                    txtStatusDelivered.setVisibility(View.GONE);
                    txtStatusLostTrack.setVisibility(View.GONE);
                    break;
                case DELIVERED:
                    txtStatusPregnant.setVisibility(View.GONE);
                    txtStatusDelivered.setVisibility(View.VISIBLE);
                    txtStatusLostTrack.setVisibility(View.GONE);
                    break;
                case LOST_TRACK:
                    txtStatusPregnant.setVisibility(View.GONE);
                    txtStatusDelivered.setVisibility(View.GONE);
                    txtStatusLostTrack.setVisibility(View.VISIBLE);
                    break;
                default:
                    txtStatusPregnant.setVisibility(View.GONE);
                    txtStatusDelivered.setVisibility(View.GONE);
                    txtStatusLostTrack.setVisibility(View.GONE);
            }
        }
    }

    public void reloadData() {
        this.pregnancyRegistration = this.boxPregnancyRegistrations.get(this.pregnancyRegistration.id);
        displayData();
    }

    private void displayData() {
        displayMotherData();

        if (pregnancyRegistration != null) {
            //txtDialogSubtitle.setText(getString(R.string.pregnancy_panel_title) + " - " + getString(pregnancyRegistration.status.name));
            txtLMPDate.setText(pregnancyRegistration.lmpDate != null ? dateUtil.formatYMD(pregnancyRegistration.lmpDate) : getString(R.string.not_applicable_lbl));

            if (pregnancyRegistration.status == PregnancyStatus.PREGNANT) {
                lblEDD.setText(R.string.pregnancy_panel_edd_lbl);
                lblGestWeeks.setText(R.string.pregnancy_panel_gest_weeks_lbl);
                txtEDDDate.setText(pregnancyRegistration.expectedDeliveryDate != null ? dateUtil.formatYMD(pregnancyRegistration.expectedDeliveryDate) : getString(R.string.not_applicable_lbl));

                if (pregnancyRegistration.lmpDate != null) {
                    long days = GeneralUtil.getAgeInDays(pregnancyRegistration.lmpDate, new java.util.Date());
                    txtGestWeeks.setText((days / 7) + "");
                } else if (pregnancyRegistration.pregMonths != null) {
                    long weeks = Math.round((pregnancyRegistration.pregMonths - 1) * 4.33);
                    txtGestWeeks.setText(weeks + "");
                }
            } else if (pregnancyRegistration.status == PregnancyStatus.DELIVERED) {
                lblEDD.setText(R.string.eventType_birth);
                lblGestWeeks.setText(R.string.pregnancy_panel_postpartum_days_lbl);
                PregnancyOutcome outcome = boxPregnancyOutcomes.query(PregnancyOutcome_.code.equal(pregnancyRegistration.code)).build().findFirst();
                txtEDDDate.setText(outcome != null ? dateUtil.formatYMD(outcome.outcomeDate) : getString(R.string.not_applicable_lbl));

                if (outcome != null) {
                    long days = GeneralUtil.getAgeInDays(outcome.outcomeDate, new java.util.Date());
                    txtGestWeeks.setText(days + "");
                }
            }

            loadVisits();
            loadOutcomes();
            updateNextVisitRecommended();

            btRegisterBirth.setVisibility(pregnancyRegistration.status == PregnancyStatus.PREGNANT ? View.VISIBLE : View.GONE);
        }
    }

    private void updateNextVisitRecommended() {
        txtNextVisit.setVisibility(View.GONE);
        txtNextVisit.setTextColor(getResources().getColor(R.color.nui_color_text_darkgray));

        if (pregnancyRegistration.status == PregnancyStatus.PREGNANT) {
            if (pregnancyRegistration.lmpDate != null) {
                int count = pregnancyRegistration.summary_antepartum_count != null ? pregnancyRegistration.summary_antepartum_count : 0;

                if (count < maxAntepartumVisits && count < antepartumSchedule.size()) {
                    int targetWeek = antepartumSchedule.get(count);
                    java.util.Date nextDate = GeneralUtil.getDateAdd(pregnancyRegistration.lmpDate, targetWeek * 7);
                    boolean overdue = new Date().after(nextDate);

                    txtNextVisit.setText(getString(R.string.pregnancy_panel_next_visit_lbl, dateUtil.formatYMD(nextDate)));
                    txtNextVisit.setVisibility(View.VISIBLE);

                    if (overdue) {
                        txtNextVisit.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        txtNextVisit.setText(getString(R.string.pregnancy_panel_next_visit_overdue_lbl, txtNextVisit.getText()));
                    }
                }
            }
        } else if (pregnancyRegistration.status == PregnancyStatus.DELIVERED) {
            PregnancyOutcome outcome = boxPregnancyOutcomes.query(PregnancyOutcome_.code.equal(pregnancyRegistration.code)).build().findFirst();
            if (outcome != null) {
                int count = pregnancyRegistration.summary_postpartum_count != null ? pregnancyRegistration.summary_postpartum_count : 0;

                if (count < maxPostpartumVisits && isPostpartumExpired(outcome)) {
                    txtNextVisit.setText(R.string.pregnancy_panel_followup_expired_lbl);
                    txtNextVisit.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    txtNextVisit.setVisibility(View.VISIBLE);
                } else if (count < maxPostpartumVisits && count < postpartumSchedule.size()) {
                    int targetDays = postpartumSchedule.get(count);
                    java.util.Date nextDate = GeneralUtil.getDateAdd(outcome.outcomeDate, targetDays);
                    boolean overdue = new java.util.Date().after(nextDate);

                    txtNextVisit.setText(getString(R.string.pregnancy_panel_next_visit_lbl, dateUtil.formatYMD(nextDate)));
                    txtNextVisit.setVisibility(View.VISIBLE);

                    if (overdue) {
                        txtNextVisit.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        txtNextVisit.setText(getString(R.string.pregnancy_panel_next_visit_overdue_lbl, txtNextVisit.getText()));
                    }
                }
            }
        }
    }

    private boolean isPostpartumExpired(PregnancyOutcome outcome) {
        if (outcome == null || outcome.outcomeDate == null) return false;
        int lastPostpartumDay = postpartumSchedule.isEmpty() ? 42 : postpartumSchedule.get(postpartumSchedule.size() - 1);
        // Adding a 14-day grace period after the last scheduled visit
        int grace_period = 14;
        Date expiryDate = GeneralUtil.getDateAdd(outcome.outcomeDate, lastPostpartumDay + grace_period);
        return new Date().after(expiryDate);
    }

    private void loadVisits() {
        List<PregnancyVisit> visits = boxPregnancyVisits.query(PregnancyVisit_.motherCode.equal(mother.code).and(PregnancyVisit_.code.equal(pregnancyRegistration.code)))
                .order(PregnancyVisit_.visitDate)
                .build().find();

        List<PregnancyVisitType> groupList = new ArrayList<>();
        groupList.add(PregnancyVisitType.ANTEPARTUM);
        groupList.add(PregnancyVisitType.POSTPARTUM);

        Map<PregnancyVisitType, List<PregnancyVisit>> childMap = new HashMap<>();
        List<PregnancyVisit> antepartum = new ArrayList<>();
        List<PregnancyVisit> postpartum = new ArrayList<>();

        boolean hasComplications = false;

        for (PregnancyVisit v : visits) {
            if (v.visitType == PregnancyVisitType.ANTEPARTUM) {
                antepartum.add(v);
                if (v.complicationsReported != null && v.complicationsReported) hasComplications = true;
            } else {
                postpartum.add(v);
                if (v.postpartumComplications != null && v.postpartumComplications) hasComplications = true;
            }
        }

        childMap.put(PregnancyVisitType.ANTEPARTUM, antepartum);
        childMap.put(PregnancyVisitType.POSTPARTUM, postpartum);

        PregnancyOutcome outcome = boxPregnancyOutcomes.query(PregnancyOutcome_.code.equal(pregnancyRegistration.code)).build().findFirst();
        boolean postpartumExpired = isPostpartumExpired(outcome);

        boolean antepartumCompleted = antepartum.size() >= maxAntepartumVisits || pregnancyRegistration.status != PregnancyStatus.PREGNANT;
        boolean postpartumCompleted = postpartum.size() >= maxPostpartumVisits || pregnancyRegistration.status != PregnancyStatus.DELIVERED || (pregnancyRegistration.summary_followup_completed != null && pregnancyRegistration.summary_followup_completed) || postpartumExpired;

        PregnancyVisitExpandableAdapter adapter = new PregnancyVisitExpandableAdapter(getContext(), groupList, childMap, !antepartumCompleted, !postpartumCompleted, this);
        elvPregnancyVisits.setAdapter(adapter);
        elvPregnancyVisits.expandGroup(0);
        elvPregnancyVisits.expandGroup(1);

        if (hasComplications) {
            txtComplicationWarning.setText(R.string.pregnancy_panel_complications_warning);
            txtComplicationWarning.setVisibility(View.VISIBLE);
        } else {
            txtComplicationWarning.setVisibility(View.GONE);
        }
    }

    private void loadOutcomes() {
        PregnancyOutcome outcome = boxPregnancyOutcomes.query(PregnancyOutcome_.motherCode.equal(mother.code).and(PregnancyOutcome_.code.equal(pregnancyRegistration.code))).build().findFirst();

        if (outcome != null) {
            List<PregnancyChild> children = boxPregnancyChilds.query(PregnancyChild_.outcomeCode.equal(outcome.code)).build().find();
            int liveCount = 0;
            int stillCount = 0;

            for (PregnancyChild pc : children) {
                if (pc.outcomeType == PregnancyOutcomeType.LIVEBIRTH) liveCount++;
                if (pc.outcomeType == PregnancyOutcomeType.STILLBIRTH) stillCount++;
            }

            String birthPlaceStr = outcome.birthPlace != null ? getString(outcome.birthPlace.name) : getString(R.string.not_applicable_lbl);
            String details = getString(R.string.pregnancy_panel_outcome_date_lbl, dateUtil.formatYMD(outcome.outcomeDate)) + "\n" +
                             getString(R.string.pregnancy_panel_outcome_place_lbl, birthPlaceStr) + "\n" +
                             getString(R.string.pregnancy_panel_outcome_livebirths_lbl, liveCount) +
                             (stillCount > 0 ? " | " + getString(R.string.pregnancy_panel_outcome_stillbirths_lbl, stillCount) : "");
            txtOutcomeDetails.setText(details);

            List<Member> memberChildren = new ArrayList<>();
            ArrayList<String> extraData = new ArrayList<>();

            for (PregnancyChild pc : children) {
                Member m = boxMembers.query(Member_.code.equal(pc.childCode)).build().findFirst();
                if (m != null) {
                    // Handle missing names for stillborns
                    if (pc.outcomeType == PregnancyOutcomeType.STILLBIRTH && (m.name == null || m.name.trim().isEmpty())) {
                        m.name = getString(R.string.pregnancy_panel_child_stillborn_name_lbl, getString(R.string.pregOutcomeType_stillbirth));
                    }

                    memberChildren.add(m);

                    // Create extra data string: "Outcome Type | Gender | Age | Marital Status"
                    String outcomeTypeStr = getString(pc.outcomeType.name);
                    String maritalText = getString(R.string.relationship_type_title_abbrv_lbl) + ": " + getString(m.maritalStatus.name);
                    String mDetails = getString(R.string.member_details_gender_lbl) + " " + m.gender.code + ", " +
                                     getString(R.string.member_details_age_lbl) + " " + m.age + ", " + maritalText;

                    extraData.add(getString(R.string.pregnancy_panel_child_item_details_lbl, outcomeTypeStr, mDetails));
                }
            }

            MemberAdapter adapter = new MemberAdapter(getContext(), memberChildren, extraData);
            adapter.setShowExtraDetails(true);
            adapter.setShowMemberDetails(false); // We combine details in extraData
            lvPregnancyChildren.setAdapter(adapter);

        } else {
            txtOutcomeDetails.setText(R.string.pregnancy_panel_no_outcome_lbl);
        }
    }

    @Override
    public void onAddVisitClicked(PregnancyVisitType type) {
        if (listener != null) {
            listener.onAddPregnancyVisit(pregnancyRegistration, type);
        }
    }

    private void onChildPregnancyVisitClicked(PregnancyVisit pregnancyVisit) {
        /*
        CoreCollectedData coreCollectedData = boxCoreCollectedData.query(
                             CoreCollectedData_.formEntity.equal(CoreFormEntity.PREGNANCY_VISIT.code)
                        .and(CoreCollectedData_.formEntityId.equal(pregnancyVisit.id))).build().findFirst();
         */

        if (pregnancyVisit != null && listener != null) {
            listener.onViewCollectedPregnancyVisit(pregnancyVisit);
        }
    }

    private void onChildMemberClicked(int position) {
        MemberAdapter adapter = (MemberAdapter) lvPregnancyChildren.getAdapter();
        if (adapter != null) {
            Member child = adapter.getItem(position);
            if (listener != null) {
                listener.onViewMemberDetails(child);
            }
        }
    }

    private void onRegisterBirthClicked() {
        if (listener != null) {
            listener.onAddBirthRegistration(pregnancyRegistration);
        }
    }

    public void show() {
        this.show(fragmentManager, "PregnancySurveillanceDialog");
    }

    public interface Listener {
        void onAddPregnancyVisit(PregnancyRegistration registration, PregnancyVisitType type);
        void onAddBirthRegistration(PregnancyRegistration registration);
        void onViewMemberDetails(Member member);
        void onViewCollectedPregnancyVisit(PregnancyVisit pregnancyVisit);
        void onCanceled();
    }

    public interface ExecutionListener {
        void afterCreatingPregnancyVisit();

        void afterCreatingBirthRegistration();
    }

    public void afterCreatingPregnancyVisit() {
        reloadData();
        // Ensure both groups are expanded to show the new visit
        if (elvPregnancyVisits != null) {
            elvPregnancyVisits.expandGroup(0);
            elvPregnancyVisits.expandGroup(1);
        }
    }

    public void afterCreatingBirthRegistration() {
        reloadData();
        // Switch to Outcomes Tab
        if (tabLayout != null && tabLayout.getTabCount() > 1) {
            TabLayout.Tab outcomeTab = tabLayout.getTabAt(1);
            if (outcomeTab != null) {
                outcomeTab.select();
            }
        }
    }
}
