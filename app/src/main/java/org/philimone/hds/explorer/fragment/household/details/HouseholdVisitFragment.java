package org.philimone.hds.explorer.fragment.household.details;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import io.objectbox.Box;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.CoreCollectedExpandableAdapter;
import org.philimone.hds.explorer.adapter.MemberArrayAdapter;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.Dataset;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.Gender;
import org.philimone.hds.explorer.widget.LoadingDialog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HouseholdVisitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HouseholdVisitFragment extends Fragment {

    private ListView lvHouseholdMembers;
    private ExpandableListView elvVisitCollected;
    private Button btnVisitMemberEnu;
    private Button btnVisitBirthReg;
    private Button btnVisitPregnancyReg;
    private Button btnVisitExtInmigration;
    private Button btnVisitIntInmigration;
    private Button btnVisitOutmigration;
    private Button btnVisitDeath;
    private Button btnVisitMaritalRelationship;
    private Button btnVisitExtraForm;

    private Household household;
    private Visit visit;
    private Member selectedMember;
    private User loggedUser;

    private Box<Member> boxMembers;
    private Box<Form> boxForms;
    private Box<CollectedData> boxCollectedData;
    private Box<CoreCollectedData> boxCoreCollectedData;
    
    private VisitEventsMode currentEventMode = VisitEventsMode.HOUSEHOLD_EVENTS;
    
    private enum VisitEventsMode { HOUSEHOLD_EVENTS, MEMBER_EVENTS}

    public HouseholdVisitFragment() {
        initBoxes();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HouseholdVisitFragment.
     */
    public static HouseholdVisitFragment newInstance(Household household, Visit visit, User user) {
        HouseholdVisitFragment fragment = new HouseholdVisitFragment();
        fragment.household = household;
        fragment.visit = visit;
        fragment.loggedUser = user;
        return fragment;
    }

    public void load(Household household, Visit visit, User user){
        this.household = household;
        this.visit = visit;
        this.loggedUser = user;

        loadContentToViews();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.household_visit, container, false);

        initialize(view);

        return view;
    }

    private void initBoxes() {
        //this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
        this.boxForms = ObjectBoxDatabase.get().boxFor(Form.class);
        //this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
    }
    
    private void initialize(View view) {
        this.lvHouseholdMembers = view.findViewById(R.id.lvHouseholdMembers);
        this.elvVisitCollected = view.findViewById(R.id.elvVisitCollected);
        this.btnVisitMemberEnu = view.findViewById(R.id.btnVisitMemberEnu);
        this.btnVisitBirthReg = view.findViewById(R.id.btnVisitBirthReg);
        this.btnVisitPregnancyReg = view.findViewById(R.id.btnVisitPregnancyReg);
        this.btnVisitExtInmigration = view.findViewById(R.id.btnVisitExtInmigration);
        this.btnVisitIntInmigration = view.findViewById(R.id.btnVisitIntInmigration);
        this.btnVisitOutmigration = view.findViewById(R.id.btnVisitOutmigration);
        this.btnVisitDeath = view.findViewById(R.id.btnVisitDeath);
        this.btnVisitMaritalRelationship = view.findViewById(R.id.btnVisitMaritalRelationship);
        this.btnVisitExtraForm = view.findViewById(R.id.btnVisitExtraForm);

        this.lvHouseholdMembers.setOnItemClickListener((parent, view1, position, id) -> onMembersClick(position));

        this.lvHouseholdMembers.setOnItemLongClickListener((parent, view12, position, id) -> {
            onMembersLongClick(position);
            return true;
        });
    }

    private void loadContentToViews() {
        loadMembersToList();
        loadCollectedEventsToList();
        setHouseholdMode();
    }

    private void onMembersClick(int position) {
        //select one member and highlight

        MemberArrayAdapter adapter = getMembersAdapter();
        adapter.setSelectedIndex(position);

        this.selectedMember = adapter.getItem(position);
        setMemberMode();
    }

    private void onMembersLongClick(int position) {
        //select one and show MemberDetails without Highlight
        MemberArrayAdapter adapter = getMembersAdapter();
        Member member = adapter.getItem(position);

        //TODO DO IT LATER
    }

    private MemberArrayAdapter getMembersAdapter(){
        if (this.lvHouseholdMembers.getAdapter()==null) return null;

        return (MemberArrayAdapter) this.lvHouseholdMembers.getAdapter();
    }

    private void clearMemberSelection(){
        MemberArrayAdapter adapter = getMembersAdapter();

        if (adapter != null){
            adapter.setSelectedIndex(-1);
        }
    }

    private void setHouseholdMode() {
        this.currentEventMode = VisitEventsMode.HOUSEHOLD_EVENTS;
        
        clearMemberSelection();

        this.btnVisitMemberEnu.setEnabled(true);
        this.btnVisitBirthReg.setEnabled(false);
        this.btnVisitPregnancyReg.setEnabled(false);
        this.btnVisitExtInmigration.setEnabled(true);
        this.btnVisitIntInmigration.setEnabled(false);
        this.btnVisitOutmigration.setEnabled(false);
        this.btnVisitDeath.setEnabled(false);
        this.btnVisitMaritalRelationship.setEnabled(false);
        this.btnVisitExtraForm.setEnabled(false); //we need to analyse better this
    }
    
    private void setMemberMode() {
        this.currentEventMode = VisitEventsMode.MEMBER_EVENTS;

        this.btnVisitMemberEnu.setEnabled(false);
        this.btnVisitBirthReg.setEnabled(this.selectedMember!=null && this.selectedMember.gender== Gender.FEMALE);
        this.btnVisitPregnancyReg.setEnabled(this.selectedMember!=null && this.selectedMember.gender== Gender.FEMALE);
        this.btnVisitExtInmigration.setEnabled(false);
        this.btnVisitIntInmigration.setEnabled(true);
        this.btnVisitOutmigration.setEnabled(true);
        this.btnVisitDeath.setEnabled(true);
        this.btnVisitMaritalRelationship.setEnabled(true);
        this.btnVisitExtraForm.setEnabled(true); //we need to analyse better this
    }

    private void loadMembersToList() {
        if (household == null) return;

        List<Member> members = this.boxMembers.query().equal(Member_.householdCode, household.getCode()).build().find();

        MemberArrayAdapter adapter = new MemberArrayAdapter(this.getContext(), R.layout.household_visit_member_item, members);
        adapter.setShowHouseholdHead(false);
        this.lvHouseholdMembers.setAdapter(adapter);
    }

    private void loadCollectedEventsToList() {

        if (visit == null) return;

        //Type: Member Enumeration (3) -> CoreCollectedData as subitem
        //layouts: core_form_collected_item,

        List<CoreCollectedData> coreList = this.boxCoreCollectedData.query().equal(CoreCollectedData_.visitId, this.visit.id)
                                                                            .order(CoreCollectedData_.createdDate).build().find();
        LinkedHashMap<CoreFormEntity, List<CoreCollectedData>> mapData = new LinkedHashMap<>();
        
        mapData.put(CoreFormEntity.HOUSEHOLD, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.MEMBER_ENU, new ArrayList<CoreCollectedData>());
        //mapData.put(CoreFormEntity.HEAD_RELATIONSHIP, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.MARITAL_RELATIONSHIP, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.INMIGRATION, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.EXTERNAL_INMIGRATION, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.OUTMIGRATION, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.PREGNANCY_REGISTRATION, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.PREGNANCY_OUTCOME, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.DEATH, new ArrayList<CoreCollectedData>());
        mapData.put(CoreFormEntity.VISIT, new ArrayList<CoreCollectedData>());

        //group the coreList in Map
        coreList.forEach(collectedData -> {
            mapData.get(collectedData.formEntity).add(collectedData);
        });

        CoreCollectedExpandableAdapter adapter = new CoreCollectedExpandableAdapter(this.getContext(), mapData);
        this.elvVisitCollected.setAdapter(adapter);
    }
    
}