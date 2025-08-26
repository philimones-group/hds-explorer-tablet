package org.philimone.hds.explorer.fragment.showcollected.utilities;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.showcollected.adapter.model.CoreCollectedDataItem;
import org.philimone.hds.explorer.fragment.showcollected.adapter.model.OdkCollectedDataItem;
import org.philimone.hds.explorer.main.hdsforms.HouseholdRelocationFormUtil;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.Death;
import org.philimone.hds.explorer.model.Death_;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.HeadRelationship_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.HouseholdProxyHead;
import org.philimone.hds.explorer.model.HouseholdRelocation;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.IncompleteVisit;
import org.philimone.hds.explorer.model.IncompleteVisit_;
import org.philimone.hds.explorer.model.Inmigration;
import org.philimone.hds.explorer.model.Inmigration_;
import org.philimone.hds.explorer.model.MaritalRelationship;
import org.philimone.hds.explorer.model.MaritalRelationship_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.Outmigration;
import org.philimone.hds.explorer.model.Outmigration_;
import org.philimone.hds.explorer.model.PregnancyChild;
import org.philimone.hds.explorer.model.PregnancyChild_;
import org.philimone.hds.explorer.model.PregnancyOutcome;
import org.philimone.hds.explorer.model.PregnancyOutcome_;
import org.philimone.hds.explorer.model.PregnancyRegistration;
import org.philimone.hds.explorer.model.PregnancyRegistration_;
import org.philimone.hds.explorer.model.PregnancyVisit;
import org.philimone.hds.explorer.model.PregnancyVisitChild;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.RegionHeadRelationship;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.Residency;
import org.philimone.hds.explorer.model.Residency_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.Visit_;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.model.enums.HeadRelationshipType;
import org.philimone.hds.explorer.model.enums.MaritalEndStatus;
import org.philimone.hds.explorer.model.enums.temporal.ExternalInMigrationType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipEndType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipStartType;
import org.philimone.hds.explorer.model.enums.temporal.RegionHeadEndType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyStartType;
import org.philimone.hds.explorer.model.oldstate.SavedEntityState;
import org.philimone.hds.explorer.model.oldstate.SavedEntityState_;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.objectbox.Box;
import mz.betainteractive.odk.InstanceProviderAPI;
import mz.betainteractive.odk.model.FilledForm;
import mz.betainteractive.odk.task.OdkFormLoadResult;
import mz.betainteractive.utilities.GeneralUtil;
import mz.betainteractive.utilities.StringUtil;

public class CoreCollectedDataDeletionUtil {

    private final Context mContext;
    private Box<CoreCollectedData> boxCoreCollectedData;
    private Box<CollectedData> boxCollectedData;
    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<Visit> boxVisits;
    private Box<MaritalRelationship> boxMaritalRelationships;
    private Box<Inmigration> boxInmigrations;
    private Box<Outmigration> boxOutmigrations;
    private Box<PregnancyRegistration> boxPregnancyRegistrations;
    private Box<PregnancyOutcome> boxPregnancyOutcomes;
    private Box<PregnancyChild> boxPregnancyChilds;
    private Box<PregnancyVisit> boxPregnancyVisits;
    private Box<PregnancyVisitChild> boxPregnancyVisitChilds;
    private Box<Death> boxDeaths;
    private Box<HeadRelationship> boxHeadRelationships;
    private Box<Residency> boxResidencies;
    private Box<IncompleteVisit> boxIncompleteVisits;
    private Box<RegionHeadRelationship> boxRegionHeadRelationships;
    private Box<HouseholdRelocation> boxHouseholdRelocations;
    private Box<HouseholdProxyHead> boxHouseholdProxyHeads;
    private Box<SavedEntityState> boxSavedEntityStates;

    public CoreCollectedDataDeletionUtil(Context context) {
        this.mContext = context;
        this.initBoxes();
    }

    private void initBoxes() {
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
        this.boxCollectedData = ObjectBoxDatabase.get().boxFor(CollectedData.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxVisits = ObjectBoxDatabase.get().boxFor(Visit.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxMaritalRelationships = ObjectBoxDatabase.get().boxFor(MaritalRelationship.class);
        this.boxInmigrations = ObjectBoxDatabase.get().boxFor(Inmigration.class);
        this.boxInmigrations = ObjectBoxDatabase.get().boxFor(Inmigration.class);
        this.boxOutmigrations = ObjectBoxDatabase.get().boxFor(Outmigration.class);
        this.boxPregnancyRegistrations = ObjectBoxDatabase.get().boxFor(PregnancyRegistration.class);
        this.boxPregnancyOutcomes = ObjectBoxDatabase.get().boxFor(PregnancyOutcome.class);
        this.boxPregnancyChilds  = ObjectBoxDatabase.get().boxFor(PregnancyChild.class);
        this.boxPregnancyVisits = ObjectBoxDatabase.get().boxFor(PregnancyVisit.class);
        this.boxPregnancyVisitChilds  = ObjectBoxDatabase.get().boxFor(PregnancyVisitChild.class);
        this.boxDeaths = ObjectBoxDatabase.get().boxFor(Death.class);
        this.boxHeadRelationships = ObjectBoxDatabase.get().boxFor(HeadRelationship.class);
        this.boxResidencies = ObjectBoxDatabase.get().boxFor(Residency.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxIncompleteVisits = ObjectBoxDatabase.get().boxFor(IncompleteVisit.class);
        this.boxRegionHeadRelationships = ObjectBoxDatabase.get().boxFor(RegionHeadRelationship.class);
        this.boxHouseholdRelocations = ObjectBoxDatabase.get().boxFor(HouseholdRelocation.class);
        this.boxHouseholdProxyHeads = ObjectBoxDatabase.get().boxFor(HouseholdProxyHead.class);
        this.boxSavedEntityStates = ObjectBoxDatabase.get().boxFor(SavedEntityState.class);
    }

    public void deleteRecords(List<CoreCollectedDataItem> selectedList) {
        //CoreCollectedData
        // -> Household - delete all the visits, members, events related to it, delete Household
        // -> Member    - delete all the events related to it, delete Member, remove Member as Head of Household
        // -> Visit     - delete all the events, related to it, delete Visit
        // -> Region    - delete all Households and related data to it, delete Region
        //AFTER DELETION, ITS IMPORTANT TO SYNCHRONIZE BACK THE DEVICE TO AVOID DATA CONFLICTS

        List<CoreCollectedData> list = selectedList.stream().map(CoreCollectedDataItem::getCollectedData).collect( Collectors.toList());
        deleteCoreCollectedDataList(list);
    }

    public void deleteRecord(CoreCollectedData coreCollectedData) {
        deleteOneCoreCollectedData(coreCollectedData);
    }

    public void deleteOdkRecords(List<OdkCollectedDataItem> selectedList) {
        for (OdkCollectedDataItem dataItem : selectedList) {
            deleteCollectedData(dataItem.getCollectedData());
        }
    }

    private void deleteCoreCollectedDataList(List<CoreCollectedData> coreCollectedDataList){
        for (CoreCollectedData cdata : coreCollectedDataList) {
            deleteOneCoreCollectedData(cdata);
        }
    }

    private void deleteOneCoreCollectedData(CoreCollectedData cdata) {
        if (cdata != null) {
            if (cdata.collectedId == null) return;

            switch (cdata.formEntity) {
                case REGION: deleteRegion(cdata); break;
                case VISIT: deleteVisit(cdata); break;
                case HOUSEHOLD: deleteHousehold(cdata); break;
                case MARITAL_RELATIONSHIP: deleteMaritalRelationship(cdata); break;
                case PREGNANCY_REGISTRATION: deletePregnancyReg(cdata); break;
                case PREGNANCY_OUTCOME: deletePregnancyOutcome(cdata); break;
                case PREGNANCY_VISIT: deletePregnancyVisit(cdata); break;
                case EXTERNAL_INMIGRATION: deleteInmigration(cdata); break;
                case OUTMIGRATION: deleteOutmigration(cdata); break;
                case INMIGRATION: deleteInmigration(cdata); break;
                case HOUSEHOLD_RELOCATION: deleteHouseholdRelocation(cdata); break;
                case DEATH: deleteDeath(cdata); break;
                case MEMBER_ENU: deleteMemberEnumeration(cdata); break;
                case CHANGE_HOUSEHOLD_HEAD: deleteChangeHouseholdHead(cdata); break;
                case INCOMPLETE_VISIT: deleteIncompleteVisit(cdata); break;
                case CHANGE_REGION_HEAD: deleteChangeRegionHead(cdata); break;
                case CHANGE_PROXY_HEAD: deleteChangeProxyHead(cdata); break;
                case PRE_HOUSEHOLD: deletePreHousehold(cdata); break;
                case EDITED_REGION: deleteCoreCollectedData(cdata); break;
                case EDITED_HOUSEHOLD: deleteCoreCollectedData(cdata); break;
                case EDITED_MEMBER: deleteCoreCollectedData(cdata); break;
                default: deleteCoreCollectedData(cdata); break;
            }
        }
    }

    private void deleteRegion(CoreCollectedData cdata) {
        Region region = boxRegions.get(cdata.formEntityId); //query(Region_.collectedId.equal(cdata.collectedId)).build().findFirst();

        if (region != null) {
            removeRegion(region);
        }

        deleteCoreCollectedData(cdata);
    }

    private void deleteVisit(CoreCollectedData cdata) {
        Visit visit = boxVisits.get(cdata.formEntityId); //.query(Visit_.collectedId.equal(cdata.collectedId)).build().findFirst();

        //delete this first to avoid loops when call removeVisit - that will remove all CoreCollectedData of this visit
        deleteCoreCollectedData(cdata);

        if (visit != null) {
            removeVisit(visit);
        }
    }

    private void deleteHousehold(CoreCollectedData cdata) {

        Household household = boxHouseholds.get(cdata.formEntityId); //.query(Household_.collectedId.equal(cdata.collectedId)).build().findFirst();

        deleteCoreCollectedData(cdata);

        if (household != null) {
            removeHousehold(household);
        }
    }

    private void deletePreHousehold(CoreCollectedData cdata) {
        boxHouseholds.remove(cdata.formEntityId); //.query(Household_.collectedId.equal(cdata.collectedId)).build().remove();
        deleteCoreCollectedData(cdata);
    }

    private void deleteIncompleteVisit(CoreCollectedData cdata) {
        boxIncompleteVisits.remove(cdata.formEntityId); //.query(IncompleteVisit_.collectedId.equal(cdata.collectedId)).build().remove();
        deleteCoreCollectedData(cdata);
    }

    private void deleteChangeRegionHead(CoreCollectedData cdata) {
        RegionHeadRelationship regionHeadRelationship = boxRegionHeadRelationships.get(cdata.formEntityId);

        if (regionHeadRelationship != null) {
            Region region = boxRegions.query(Region_.code.equal(regionHeadRelationship.regionCode)).build().findFirst();


            RegionHeadRelationship oldHeadRelationship = regionHeadRelationship.oldHeadRelationshipId > 0 ? boxRegionHeadRelationships.get(regionHeadRelationship.oldHeadRelationshipId) : null;
            Member oldHead = regionHeadRelationship.oldHeadId > 0 ? boxMembers.get(regionHeadRelationship.oldHeadId) : null;

            if (region != null) {
                region.headCode = oldHead==null ? null : oldHead.code;
                region.headName = oldHead==null ? null : oldHead.name;
                boxRegions.put(region);
            }

            if (oldHeadRelationship != null) {
                oldHeadRelationship.endType = RegionHeadEndType.NOT_APPLICABLE;
                oldHeadRelationship.endDate = null;
                boxRegionHeadRelationships.put(oldHeadRelationship);
            }

            boxRegionHeadRelationships.remove(regionHeadRelationship);
        }

        deleteCoreCollectedData(cdata);
    }

    private void deleteChangeProxyHead(CoreCollectedData cdata) {
        HouseholdProxyHead householdProxyHead = boxHouseholdProxyHeads.get(cdata.formEntityId);

        if (householdProxyHead != null) {
            Household household = boxHouseholds.query(Household_.code.equal(householdProxyHead.householdCode)).build().findFirst();

            Map<String, String> mapSavedStates = getSavedStateMap(CoreFormEntity.CHANGE_PROXY_HEAD, householdProxyHead.id, "changeProxyHeadFormUtilState");
            String oldProxyHeadId = mapSavedStates.get("oldProxyHeadId");

            //restore last household proxy data
            if (!StringUtil.isBlank(oldProxyHeadId)) {
                HouseholdProxyHead oldProxyHead = boxHouseholdProxyHeads.get(Long.parseLong(oldProxyHeadId));

                if (oldProxyHead != null) {
                    oldProxyHead.endDate = null;
                    boxHouseholdProxyHeads.put(oldProxyHead);
                }

                if (household != null) {
                    household.proxyHeadType = oldProxyHead==null ? null : oldProxyHead.proxyHeadType;
                    household.proxyHeadCode = oldProxyHead==null ? null : oldProxyHead.proxyHeadCode;
                    household.proxyHeadName = oldProxyHead==null ? null : oldProxyHead.proxyHeadName;
                    household.proxyHeadRole = oldProxyHead==null ? null : oldProxyHead.proxyHeadRole;
                    boxHouseholds.put(household);
                }
            } else {
                //didnt have a proxy head so we must remove it
                if (household != null) {
                    household.proxyHeadType = null;
                    household.proxyHeadCode = null;
                    household.proxyHeadName = null;
                    household.proxyHeadRole = null;
                    boxHouseholds.put(household);
                }
            }

            boxHouseholdProxyHeads.remove(householdProxyHead);
        }

        deleteCoreCollectedData(cdata);
    }
    private void deleteChangeHouseholdHead(CoreCollectedData cdata) {

        HeadRelationship currentHeadRel = boxHeadRelationships.get(cdata.formEntityId); //.query(HeadRelationship_.collectedId.equal(cdata.collectedId)).build().findFirst();

        if (currentHeadRel != null) {

            Map<String, String> mapSavedStates = new HashMap<>();

            SavedEntityState savedState = this.boxSavedEntityStates.query(SavedEntityState_.formEntity.equal(CoreFormEntity.CHANGE_HOUSEHOLD_HEAD.code)
                    .and(SavedEntityState_.collectedId.equal(currentHeadRel.id))
                    .and(SavedEntityState_.objectKey.equal("changeHeadFormUtilState"))).build().findFirst();

            if (savedState != null) {
                HashMap map = new Gson().fromJson(savedState.objectGsonValue, HashMap.class);
                for (Object key : map.keySet()) {
                    mapSavedStates.put(key.toString(), map.get(key).toString());
                }
            }

            String oldHeadCode = mapSavedStates.get("oldHeadCode");
            String oldHeadRelatId = mapSavedStates.get("oldHeadMemberRelationshipId");
            String oldHeadRelatIdList = mapSavedStates.get("oldHeadMemberRelationshipIdList");
            String newHeadRelatIdList = mapSavedStates.get("newHeadRelationshipsList");
            String newHeadPreviousRelType = mapSavedStates.get("newHeadPreviousRelationshipType");

            Household household = boxHouseholds.query(Household_.code.equal(currentHeadRel.householdCode)).build().findFirst();
            Member currentHead = boxMembers.query(Member_.code.equal(currentHeadRel.memberCode)).build().findFirst();
            Member oldHeadMember = null;
            HeadRelationship oldHeadMemberRelationship = null;
            List<HeadRelationship> oldHeadMemberRelationships = new ArrayList<>();
            List<HeadRelationship> newHeadMemberRelationships = new ArrayList<>();
            HeadRelationshipType newHeadMemberPreviousRelatType = HeadRelationshipType.OTHER_RELATIVE;

            if (!StringUtil.isBlank(oldHeadCode)) {
                oldHeadMember = this.boxMembers.query(Member_.code.equal(oldHeadCode)).build().findFirst();
            }
            if (!StringUtil.isBlank(oldHeadRelatId)) {
                oldHeadMemberRelationship = this.boxHeadRelationships.get(Long.parseLong(oldHeadRelatId));
            }
            if (!StringUtil.isBlank(newHeadPreviousRelType)) {
                newHeadMemberPreviousRelatType = HeadRelationshipType.getFrom(newHeadPreviousRelType);
            }
            if (!StringUtil.isBlank(oldHeadRelatIdList)) {
                oldHeadMemberRelationships = new ArrayList<>();
                for (String strId : oldHeadRelatIdList.split(",")) {
                    if (StringUtil.isBlank(strId)) continue;
                    HeadRelationship headRelationship = this.boxHeadRelationships.get(Long.parseLong(strId));
                    oldHeadMemberRelationships.add(headRelationship);
                }
            }
            if (!StringUtil.isBlank(newHeadRelatIdList)) {
                newHeadMemberRelationships = new ArrayList<>();
                for (String strId : newHeadRelatIdList.split(",")) {
                    if (StringUtil.isBlank(strId)) continue;
                    HeadRelationship headRelationship = this.boxHeadRelationships.get(Long.parseLong(strId));
                    newHeadMemberRelationships.add(headRelationship);
                }
            }

            if (household != null) {
                //restore previous head relationships end status
                if (oldHeadMemberRelationships != null) {
                    for (HeadRelationship obj : oldHeadMemberRelationships) {
                        obj.endType = HeadRelationshipEndType.NOT_APPLICABLE;
                        obj.endDate = null;
                        this.boxHeadRelationships.put(obj);

                        Member member = Queries.getMemberByCode(boxMembers, obj.memberCode);
                        member.headRelationshipType = obj.relationshipType;
                        boxMembers.put(member);
                    }
                }

                if (currentHead != null) {
                    currentHead.headRelationshipType = newHeadMemberPreviousRelatType;
                    boxMembers.put(currentHead);
                }

                if (oldHeadMemberRelationship != null) {
                    oldHeadMemberRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
                    oldHeadMemberRelationship.endDate = null;
                    boxHeadRelationships.put(oldHeadMemberRelationship);
                }

                if (oldHeadMember != null) {
                    household.headCode = oldHeadMember.code;
                    household.headName = oldHeadMember.name;
                    boxHouseholds.put(household);
                }

                //delete all the current head relationships with the currentHead
                for (HeadRelationship obj : newHeadMemberRelationships) {
                    if (obj != null)
                        boxHeadRelationships.remove(obj);
                }

            }

            //delete current head relationship
            boxHeadRelationships.remove(currentHeadRel);
        }

        deleteCoreCollectedData(cdata);

        //its always better to perform synchronization after deleting records to ensure data integrity
    }

    private void deleteHouseholdRelocation(CoreCollectedData cdata) {

        HouseholdRelocation householdRelocation = boxHouseholdRelocations.get(cdata.formEntityId);

        if (householdRelocation != null) {

            //read saved entity and delete them
            Map<String, String> mapSavedStates = new HashMap<>();
            List<Long> oldMembersResidenciesList = new ArrayList<>();
            List<Long> oldMembersRelationshipsList = new ArrayList<>();
            List<Long> newMembersResidenciesList = new ArrayList<>();
            List<Long> newMembersRelationshipsList = new ArrayList<>();

            SavedEntityState savedState = this.boxSavedEntityStates.query(SavedEntityState_.formEntity.equal(CoreFormEntity.HOUSEHOLD_RELOCATION.code)
                    .and(SavedEntityState_.collectedId.equal(householdRelocation.id))
                    .and(SavedEntityState_.objectKey.equal(HouseholdRelocationFormUtil.SAVED_ENTITY_OBJECT_KEY))).build().findFirst();

            if (savedState != null) {
                HashMap map = new Gson().fromJson(savedState.objectGsonValue, HashMap.class);
                for (Object key : map.keySet()) {
                    mapSavedStates.put(key.toString(), map.get(key).toString());
                }
            }

            String oldResidencyList = mapSavedStates.get("oldMembersResidenciesList");
            String oldHeadRelatList = mapSavedStates.get("oldMembersRelationshipsList");
            String newResidencyList = mapSavedStates.get("newMembersResidenciesList");
            String newHeadRelatList = mapSavedStates.get("newMembersRelationshipsList");

            if (oldResidencyList != null) {
                for (String strId : oldResidencyList.split(",")) {
                    if (!StringUtil.isBlank(strId))
                        oldMembersResidenciesList.add(Long.parseLong(strId));
                }
            }
            if (oldHeadRelatList != null) {
                for (String strId : oldHeadRelatList.split(",")) {
                    if (!StringUtil.isBlank(strId))
                        oldMembersRelationshipsList.add(Long.parseLong(strId));
                }
            }
            if (newResidencyList != null) {
                for (String strId : newResidencyList.split(",")) {
                    if (!StringUtil.isBlank(strId))
                        newMembersResidenciesList.add(Long.parseLong(strId));
                }
            }
            if (newHeadRelatList != null) {
                for (String strId : newHeadRelatList.split(",")) {
                    if (!StringUtil.isBlank(strId))
                        newMembersRelationshipsList.add(Long.parseLong(strId));
                }
            }

            //DELETE AND UPDATE RECORDS AFFECTED BY THIS HOUSEHOLD RELOCATION
            //delete new residencies and relationships
            boxResidencies.removeByIds(newMembersResidenciesList);
            boxHeadRelationships.removeByIds(newMembersRelationshipsList);

            //restore old residencies and relationships
            for (Long id : oldMembersResidenciesList) {
                Residency obj = boxResidencies.get(id);
                obj.endType = ResidencyEndType.NOT_APPLICABLE;
                obj.endDate = null;
                boxResidencies.put(obj);

                //update member
                Household household = boxHouseholds.query(Household_.code.equal(obj.householdCode)).build().findFirst();
                Member member = Queries.getMemberByCode(boxMembers, obj.memberCode);
                member.householdCode = obj.householdCode;
                member.householdName = Queries.getHouseholdByCode(boxHouseholds, obj.householdCode).name;
                member.startType = obj.startType;
                member.startDate = obj.startDate;
                member.endType = ResidencyEndType.NOT_APPLICABLE;
                member.endDate = null;
                member.gpsNull = household.gpsNull;
                member.gpsLatitude = household.gpsLatitude;
                member.gpsLongitude = household.gpsLongitude;
                member.gpsAltitude = household.gpsAltitude;
                member.gpsAccuracy = household.gpsAccuracy;
                member.sinLatitude = household.sinLatitude;
                member.cosLatitude = household.cosLatitude;
                member.sinLongitude = household.sinLongitude;
                member.cosLongitude = household.cosLongitude;
                boxMembers.put(member);
            }
            for (Long id : oldMembersRelationshipsList) {
                HeadRelationship obj = boxHeadRelationships.get(id);
                obj.endType = HeadRelationshipEndType.NOT_APPLICABLE;
                obj.endDate = null;
                boxHeadRelationships.put(obj);

                Member member = Queries.getMemberByCode(boxMembers, obj.memberCode);
                member.headRelationshipType = obj.relationshipType;
                boxMembers.put(member);
            }

            //delete HouseholdRelocation
            boxHouseholdRelocations.remove(householdRelocation);
        }

        deleteCoreCollectedData(cdata);

        //its always better to perform synchronization after deleting records to ensure data integrity
    }

    private void deleteMemberEnumeration(CoreCollectedData cdata) {
        Member member = boxMembers.get(cdata.formEntityId);
        removeMember(member);
        deleteCoreCollectedData(cdata);
    }

    private void deleteDeath(CoreCollectedData cdata) {
        //removes Residency, HeadRelationship, MaritalRelationship endStatus, and delete Death

        Death death = boxDeaths.get(cdata.formEntityId);

        if (death != null) {
            Residency residency = boxResidencies.query(Residency_.memberCode.equal(death.memberCode).and(Residency_.endDate.equal(death.deathDate)).and(Residency_.endType.equal(ResidencyEndType.DEATH.code))).build().findFirst();
            HeadRelationship headRelationship = boxHeadRelationships.query(HeadRelationship_.memberCode.equal(death.memberCode).and(HeadRelationship_.endDate.equal(death.deathDate)).and(HeadRelationship_.endType.equal(HeadRelationshipEndType.DEATH.code))).build().findFirst();

            MaritalRelationship maritalRelationship = boxMaritalRelationships.query(MaritalRelationship_.endStatus.equal(MaritalEndStatus.WIDOWED.code).and(MaritalRelationship_.endDate.equal(death.deathDate))
                    .and(MaritalRelationship_.memberA_code.equal(death.memberCode).or(MaritalRelationship_.memberB_code.equal(death.memberCode)))).build().findFirst();

            if (residency != null) {
                residency.endType = ResidencyEndType.NOT_APPLICABLE;
                residency.endDate = null;
                this.boxResidencies.put(residency);
            }

            if (headRelationship != null) {
                headRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
                headRelationship.endDate = null;
                this.boxHeadRelationships.put(headRelationship);
            }

            if (maritalRelationship != null) {
                maritalRelationship.endStatus = MaritalEndStatus.NOT_APPLICABLE;
                maritalRelationship.endDate = null;
                this.boxMaritalRelationships.put(maritalRelationship);
            }
        }

        deleteCoreCollectedData(cdata);
    }

    private void deleteOutmigration(CoreCollectedData cdata) {
        //removes Residency, HeadRelationship endStatus, and delete Outmigration

        Outmigration outmigration = boxOutmigrations.get(cdata.formEntityId);

        if (outmigration != null) {
            Residency residency = boxResidencies.query(Residency_.memberCode.equal(outmigration.memberCode).and(Residency_.endDate.equal(outmigration.migrationDate)).and(Residency_.endType.equal(outmigration.migrationType.code))).build().findFirst();
            HeadRelationship headRelationship = boxHeadRelationships.query(HeadRelationship_.memberCode.equal(outmigration.memberCode).and(HeadRelationship_.endDate.equal(outmigration.migrationDate)).and(HeadRelationship_.endType.equal(outmigration.migrationType.code))).build().findFirst();

            if (residency != null) {
                residency.endDate = null;
                residency.endType = ResidencyEndType.NOT_APPLICABLE;
                this.boxResidencies.put(residency);
            }

            if (headRelationship != null) {
                headRelationship.endDate = null;
                headRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
                this.boxHeadRelationships.put(headRelationship);
            }
        }

        deleteCoreCollectedData(cdata);
    }

    private void deleteInmigration(CoreCollectedData cdata) {
        //can remove a Member
        //will have to definetely remove the recent Residency and HeadRelationship
        //will update the Member residency status
        //can update the Household

        Inmigration inmigration = boxInmigrations.get(cdata.formEntityId);

        if (inmigration != null) {

            if (inmigration.extMigType != null) { //EXTERNAL IN

                if (inmigration.extMigType == ExternalInMigrationType.ENTRY) { //first entry or new individual created
                    Member member = this.boxMembers.query(Member_.code.equal(inmigration.memberCode)).build().findFirst();
                    removeMember(member); //this will remove the inmigration too
                } else {
                    //the member already existed, just remove the most recent data created by this inmigration
                    //Residency and HeadRelationship
                    this.boxResidencies.query(Residency_.memberCode.equal(inmigration.memberCode).and(Residency_.startDate.equal(inmigration.migrationDate)).and(Residency_.startType.equal(ResidencyStartType.EXTERNAL_INMIGRATION.code))).build().remove();
                    this.boxHeadRelationships.query(HeadRelationship_.memberCode.equal(inmigration.memberCode).and(HeadRelationship_.startDate.equal(inmigration.migrationDate)).and(HeadRelationship_.startType.equal(HeadRelationshipStartType.EXTERNAL_INMIGRATION.code))).build().remove();
                    this.boxInmigrations.remove(inmigration);
                }
            } else { //INTERNAL IN
                this.boxResidencies.query(Residency_.memberCode.equal(inmigration.memberCode).and(Residency_.startDate.equal(inmigration.migrationDate)).and(Residency_.startType.equal(ResidencyStartType.EXTERNAL_INMIGRATION.code))).build().remove();
                this.boxHeadRelationships.query(HeadRelationship_.memberCode.equal(inmigration.memberCode).and(HeadRelationship_.startDate.equal(inmigration.migrationDate)).and(HeadRelationship_.startType.equal(HeadRelationshipStartType.EXTERNAL_INMIGRATION.code))).build().remove();
                this.boxInmigrations.remove(inmigration);
            }
        }

        deleteCoreCollectedData(cdata);
    }

    private void deletePregnancyOutcome(CoreCollectedData cdata) {
        //need to remove childs
        PregnancyOutcome outcome = this.boxPregnancyOutcomes.get(cdata.formEntityId);

        if (outcome != null) {

            for (PregnancyChild child : outcome.childs) {

                //get member and remove member and its dependencies
                Member member = this.boxMembers.query(Member_.code.equal(child.childCode)).build().findFirst();

                if (member != null) {
                    removeMember(member); //this also removes a PregnancyCHild record
                }

            }

            this.boxPregnancyOutcomes.remove(outcome);
        }

        deleteCoreCollectedData(cdata);
    }

    private void deletePregnancyVisit(CoreCollectedData cdata) {
        //need to remove childs
        PregnancyVisit visit = this.boxPregnancyVisits.get(cdata.formEntityId);

        if (visit != null) {

            for (PregnancyVisitChild child : visit.childs) {
                boxPregnancyVisitChilds.remove(child);
            }

            this.boxPregnancyVisits.remove(visit);
        }

        deleteCoreCollectedData(cdata);
    }

    private void deletePregnancyReg(CoreCollectedData cdata) {
        this.boxPregnancyRegistrations.remove(cdata.formEntityId);
        deleteCoreCollectedData(cdata);
    }

    private void deleteMaritalRelationship(CoreCollectedData cdata) {
        this.boxMaritalRelationships.remove(cdata.formEntityId); //remove marital relationship
        deleteCoreCollectedData(cdata);
    }

    private void removeRegion(Region region) {
        if (region != null) {
            List<Household> households = boxHouseholds.query(Household_.region.equal(region.code)).build().find();

            for (Household household : households) {
                removeHousehold(household);
            }

            boxRegions.remove(region);
        }
    }

    private void removeVisit(Visit visit) {
        //delete any record directly relacted to this visit except Households

        if (visit != null) {
            List<Death> listDths = boxDeaths.query(Death_.visitCode.equal(visit.code)).build().find();
            removeDeaths(listDths);

            List<IncompleteVisit> listIncs = boxIncompleteVisits.query(IncompleteVisit_.visitCode.equal(visit.code)).build().find();
            removeIncompleteVisits(listIncs);

            List<Inmigration> listImgs = boxInmigrations.query(Inmigration_.visitCode.equal(visit.code)).build().find();
            removeInmigrations(listImgs);

            List<Outmigration> listOmgs = boxOutmigrations.query(Outmigration_.visitCode.equal(visit.code)).build().find();
            removeOutmigrations(listOmgs);

            List<MaritalRelationship> listMrs = boxMaritalRelationships.query(MaritalRelationship_.visitCode.equal(visit.code)).build().find();
            removeMaritalRelationships(listMrs);

            List<PregnancyRegistration> listPrs = boxPregnancyRegistrations.query(PregnancyRegistration_.visitCode.equal(visit.code)).build().find();
            removePregnancyRegs(listPrs);

            List<PregnancyOutcome> listPos = boxPregnancyOutcomes.query(PregnancyOutcome_.visitCode.equal(visit.code)).build().find();
            removePregnancyOutcomes(listPos);

            //Get CoreCollectedData - and delete all, this visit corecollecteddata is already deleted
            List<CoreCollectedData> collectedDataList = boxCoreCollectedData.query(CoreCollectedData_.visitId.equal(visit.id)).build().find();
            boxVisits.remove(visit);
            deleteCoreCollectedDataList(collectedDataList);
        }

    }

    private void removeHousehold(Household household) {

        if (household != null) {

            //get member of this household to delete them
            List<Residency> residencies = boxResidencies.query(Residency_.householdCode.equal(household.code)).build().find();
            List<Visit> visits = boxVisits.query(Visit_.householdCode.equal(household.code)).build().find();

            for (Residency residency : residencies) {
                Member member = boxMembers.query(Member_.code.equal(residency.memberCode)).build().findFirst();
                if (member != null) {
                    removeMember(member);
                }
            }

            for (Visit visit : visits) {
                removeVisit(visit);
            }

            boxHouseholds.remove(household);
        }
    }

    private void removeMember(Member member) {
        //check dependencies and remove - check if they have CoreCollectedData-
        //Residency, HeadRelationship, MaritalRelationship,

        if (member != null) {
            //remove deaths
            boxHeadRelationships.query(HeadRelationship_.memberCode.equal(member.code)).build().remove();
            boxResidencies.query(Residency_.memberCode.equal(member.code)).build().remove();

            Death death = boxDeaths.query(Death_.memberCode.equal(member.code)).build().findFirst();
            removeDeath(death);

            List<MaritalRelationship> listmrs = boxMaritalRelationships.query(MaritalRelationship_.memberA_code.equal(member.code).or(MaritalRelationship_.memberB_code.equal(member.code))).build().find();
            removeMaritalRelationships(listmrs);

            List<IncompleteVisit> listivs = boxIncompleteVisits.query(IncompleteVisit_.memberCode.equal(member.code)).build().find();
            removeIncompleteVisits(listivs);

            List<Inmigration> listimgs = boxInmigrations.query(Inmigration_.memberCode.equal(member.code)).build().find();
            removeInmigrations(listimgs);

            List<Outmigration> listomgs = boxOutmigrations.query(Outmigration_.memberCode.equal(member.code)).build().find();
            removeOutmigrations(listomgs);

            List<PregnancyRegistration> listprs = boxPregnancyRegistrations.query(PregnancyRegistration_.motherCode.equal(member.code)).build().find();
            removePregnancyRegs(listprs);

            boxPregnancyChilds.query(PregnancyChild_.childCode.equal(member.code)).build().remove();

            List<PregnancyOutcome> listpos = boxPregnancyOutcomes.query(PregnancyOutcome_.motherCode.equal(member.code)).build().find();
            removePregnancyOutcomes(listpos);

            List<Visit> listvts = boxVisits.query(Visit_.respondentCode.equal(member.code)).build().find();
            removeVisits(listvts);

            //Household - remove the head of household if it is this member
            Household household = boxHouseholds.query(Household_.headCode.equal(member.code)).build().findFirst();
            if (household != null) {
                household.headCode = null;
                household.headName = null;
                boxHouseholds.put(household);
            }

            this.boxMembers.remove(member);
        }
    }

    private void removeDeath(Death death) {
        if (death != null) {
            if (death.collectedId != null) {
                boxCoreCollectedData.query(CoreCollectedData_.collectedId.equal(death.collectedId)).build().remove();
            }
            boxDeaths.remove(death);
        }
    }

    private void removeDeaths(List<Death> deaths) {
        for (Death death : deaths) {
            removeDeath(death);
        }
    }

    private void removeMaritalRelationships(List<MaritalRelationship> list) {
        for (MaritalRelationship maritalRelationship : list) {
            if (maritalRelationship.collectedId != null) {
                boxCoreCollectedData.query(CoreCollectedData_.collectedId.equal(maritalRelationship.collectedId)).build().remove();
            }
            boxMaritalRelationships.remove(maritalRelationship);
        }
    }

    private void removeIncompleteVisits(List<IncompleteVisit> list) {
        for (IncompleteVisit item : list) {
            if (item.collectedId != null) {
                boxCoreCollectedData.query(CoreCollectedData_.collectedId.equal(item.collectedId)).build().remove();
            }
            boxIncompleteVisits.remove(item);
        }
    }

    private void removeInmigrations(List<Inmigration> list) {
        for (Inmigration item : list) {
            if (item.collectedId != null) {
                boxCoreCollectedData.query(CoreCollectedData_.collectedId.equal(item.collectedId)).build().remove();
            }
            boxInmigrations.remove(item);
        }
    }

    private void removeOutmigrations(List<Outmigration> list) {
        for (Outmigration item : list) {
            if (item.collectedId != null) {
                boxCoreCollectedData.query(CoreCollectedData_.collectedId.equal(item.collectedId)).build().remove();
            }
            boxOutmigrations.remove(item);
        }
    }

    private void removePregnancyRegs(List<PregnancyRegistration> list) {
        for (PregnancyRegistration item : list) {
            if (item.collectedId != null) {
                boxCoreCollectedData.query(CoreCollectedData_.collectedId.equal(item.collectedId)).build().remove();
            }
            boxPregnancyRegistrations.remove(item);
        }
    }

    private void removePregnancyOutcomes(List<PregnancyOutcome> list) {
        for (PregnancyOutcome item : list) {
            if (item.code != null) {
                this.boxPregnancyChilds.query(PregnancyChild_.outcomeCode.equal(item.code)).build().remove();
            }
            if (item.collectedId != null) {
                boxCoreCollectedData.query(CoreCollectedData_.collectedId.equal(item.collectedId)).build().remove();
            }
            boxPregnancyOutcomes.remove(item);
        }
    }

    private void removeVisits(List<Visit> list) {
        for (Visit item : list) {
            if (item.collectedId != null) {
                boxCoreCollectedData.query(CoreCollectedData_.collectedId.equal(item.collectedId)).build().remove();
            }
            boxVisits.remove(item);
        }
    }

    private Map<String, String> getSavedStateMap(CoreFormEntity formEntity, Long recordId, String mapName) {
        Map<String, String> mapSavedStates = new HashMap<>();

        SavedEntityState savedState = this.boxSavedEntityStates.query(SavedEntityState_.formEntity.equal(formEntity.code)
                .and(SavedEntityState_.collectedId.equal(recordId))
                .and(SavedEntityState_.objectKey.equal(mapName))).build().findFirst();

        if (savedState != null) {
            HashMap map = new Gson().fromJson(savedState.objectGsonValue, HashMap.class);
            for (Object key : map.keySet()) {
                mapSavedStates.put(key.toString(), map.get(key).toString());
            }
        }

        return mapSavedStates;
    }

    private void deleteCoreCollectedData(CoreCollectedData cdata) {
        if (cdata != null) {
            deleteXmlFile(cdata);
            deleteExtensionXml(cdata);
            this.boxCoreCollectedData.remove(cdata);
        }
    }
    private void deleteXmlFile(CoreCollectedData cdata) {
        if (cdata != null) {
            try {
                new File(cdata.formFilename).delete();
            } catch (Exception ex) {
                Log.d("deletion-failed", ""+ex.getLocalizedMessage());
            }
        }
    }

    public void deleteCollectedData(CollectedData odkCollectedData) {
        if (odkCollectedData != null) {
            try {
                Uri odkContentUri = Uri.parse(odkCollectedData.formUri);
                //delete odk xml
                deleteOdkInstance(odkContentUri);

                this.boxCollectedData.remove(odkCollectedData);
            } catch (Exception ex) {
                Log.d("something", "wrong: "+ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void deleteExtensionXml(CoreCollectedData cdata) {

        CollectedData odkCollectedData = this.boxCollectedData.query(CollectedData_.collectedId.equal(cdata.collectedId)).build().findFirst();

        if (odkCollectedData != null) {
            Uri odkContentUri = Uri.parse(odkCollectedData.formUri);
            //delete odk xml
            deleteOdkInstance(odkContentUri);

            this.boxCollectedData.remove(odkCollectedData);
        }
    }

    private void deleteOdkInstance(Uri odkContentUri) {

        if (true) {
            //IT WILL NOT DELETE THE ODK RECORD BECAUSE THIS CREATES APP CRASHES WHEN THROWS REMOTE EXCEPTIONS - AND I CANT CATCH IT
            Log.d("dataDeletionUtil", "disabled-delete-odk-instance = "+odkContentUri);
            return;
        }


        if (odkContentUri == null) {
            return;
        }

        int errorCount = 0;
        String messageLike = "AppDependencyComponent.inject(org.odk.collect.android.external.InstanceProvider)' on a null object reference";
        String errorMessage = null;

        do {
            errorMessage = null;

            try {
                mContext.getContentResolver().delete(odkContentUri, null, null);
            } catch (Exception ex) {
                errorCount++;
                errorMessage = ex.getMessage();

                Log.d("dataDeletionUtil", "instanceprovider, error_count="+errorCount+", ex: "+errorMessage);
                ex.printStackTrace();
            }
        } while (errorMessage != null && errorMessage.contains(messageLike) && errorCount < 4);
    }

}
