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
import org.philimone.hds.explorer.main.hdsforms.PregnancyOutcomeFormUtil;
import org.philimone.hds.explorer.main.hdsforms.PregnancyVisitFormUtil;
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
import org.philimone.hds.explorer.model.enums.MaritalStatus;
import org.philimone.hds.explorer.model.enums.PregnancyStatus;
import org.philimone.hds.explorer.model.enums.temporal.ExternalInMigrationType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipEndType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipStartType;
import org.philimone.hds.explorer.model.enums.temporal.InMigrationType;
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
            Member member = boxMembers.query(Member_.code.equal(death.memberCode)).build().findFirst();

            Map<String, String> mapSavedStates = getSavedStateMap(CoreFormEntity.DEATH, death.id, "deathFormUtilState");

            String isHouseholdHeadVar = mapSavedStates.get("isHouseholdHead");
            mapSavedStates.get("isLastMemberOfHousehold");
            mapSavedStates.get("onlyMinorsLeftInHousehold");
            String memberClosedResidencyValue = mapSavedStates.get("memberResidency"); //restore residency but must update member.endtype/date
            String memberClosedHeadRelationshipValue = mapSavedStates.get("memberHeadRelationship"); //restore as head of household if is head's death
            String memberClosedMaritalRelationshipValues = mapSavedStates.get("memberMaritalRelationshipIdList"); //restore also previous marital status of both
            String memberClosedHeadRelationshipValues = mapSavedStates.get("headMemberRelationshipIdList"); //restore also member.headRelationship
            String memberNewHeadRelationships = mapSavedStates.get("newHeadRelationshipsList"); //delete the records

            Boolean isHouseholdHead = !StringUtil.isBlank(isHouseholdHeadVar) ? Boolean.valueOf(isHouseholdHeadVar) : false;
            Residency closedResidency = StringUtil.isLong(memberClosedResidencyValue) ? boxResidencies.get(Long.parseLong(memberClosedResidencyValue)) : null;
            HeadRelationship closedHeadRelationship = StringUtil.isLong(memberClosedHeadRelationshipValue) ? boxHeadRelationships.get(Long.parseLong(memberClosedHeadRelationshipValue)) : null;
            List<MaritalRelationship> closedMaritalRelationships = new ArrayList<>();
            List<HeadRelationship> closedHeadRelationships = new ArrayList<>();
            List<HeadRelationship> newHeadRelationships = new ArrayList<>();

            if (!StringUtil.isBlank(memberClosedMaritalRelationshipValues)) {
                for (String strId : memberClosedMaritalRelationshipValues.split(",")) {
                    MaritalRelationship mr = StringUtil.isLong(strId) ? boxMaritalRelationships.get(Long.parseLong(strId)) : null;
                    if (mr != null) closedMaritalRelationships.add(mr);
                }
            }
            if (!StringUtil.isBlank(memberClosedHeadRelationshipValues)) {
                for (String strId : memberClosedHeadRelationshipValues.split(",")) {
                    HeadRelationship hr = StringUtil.isLong(strId) ? boxHeadRelationships.get(Long.parseLong(strId)) : null;
                    if (hr != null) closedHeadRelationships.add(hr);
                }
            }
            if (!StringUtil.isBlank(memberNewHeadRelationships)) {
                for (String strId : memberNewHeadRelationships.split(",")) {
                    HeadRelationship hr = StringUtil.isLong(strId) ? boxHeadRelationships.get(Long.parseLong(strId)) : null;
                    if (hr != null) newHeadRelationships.add(hr);
                }
            }

            if (closedResidency != null) {
                closedResidency.endType = ResidencyEndType.NOT_APPLICABLE;
                closedResidency.endDate = null;
                this.boxResidencies.put(closedResidency);

                member.endType = ResidencyEndType.NOT_APPLICABLE;
                member.endDate = null;
                boxMembers.put(member);
            }

            if (closedHeadRelationship != null) {
                closedHeadRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
                closedHeadRelationship.endDate = null;
                this.boxHeadRelationships.put(closedHeadRelationship);

                if (isHouseholdHead) {
                    //restore as head
                    Household household = boxHouseholds.query(Household_.code.equal(closedHeadRelationship.householdCode)).build().findFirst();
                    if (household != null) {
                        household.headCode = member.code;
                        household.headName = member.name;
                        boxHouseholds.put(household);
                    }
                }
            }

            for (MaritalRelationship maritalRelationship : closedMaritalRelationships) {
                maritalRelationship.endStatus = MaritalEndStatus.NOT_APPLICABLE;
                maritalRelationship.endDate = null;
                this.boxMaritalRelationships.put(maritalRelationship);

                //restore spouses maritalSatuses
                Member spouseA = boxMembers.query(Member_.code.equal(maritalRelationship.memberA_code)).build().findFirst();
                Member spouseB = boxMembers.query(Member_.code.equal(maritalRelationship.memberB_code)).build().findFirst();

                if (spouseA != null) {
                    spouseA.maritalStatus = MaritalStatus.getFrom(maritalRelationship.startStatus.code);
                    boxMembers.put(spouseA);
                }
                if (spouseB != null) {
                    spouseB.maritalStatus = MaritalStatus.getFrom(maritalRelationship.startStatus.code);
                    boxMembers.put(spouseB);
                }
            }

            for (HeadRelationship headRelationship : closedHeadRelationships) {
                headRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
                headRelationship.endDate = null;
                this.boxHeadRelationships.put(headRelationship);

                Member relatedMember = Queries.getMemberByCode(boxMembers, headRelationship.memberCode);
                if (relatedMember != null) {
                    relatedMember.headRelationshipType = headRelationship.relationshipType;
                    boxMembers.put(relatedMember);
                }
            }

            for (HeadRelationship headRelationship : newHeadRelationships) {
                boxHeadRelationships.remove(headRelationship);
            }

            this.boxDeaths.remove(death);

            deleteSavedStateMap(CoreFormEntity.DEATH, death.id, "deathFormUtilState");
        }

        deleteCoreCollectedData(cdata);
    }

    private void deleteOutmigration(CoreCollectedData cdata) {
        //removes Residency, HeadRelationship endStatus, and delete Outmigration

        Outmigration outmigration = boxOutmigrations.get(cdata.formEntityId);

        if (outmigration != null) {

            Map<String, String> mapSavedStates = getSavedStateMap(CoreFormEntity.OUTMIGRATION, outmigration.id, "outimgFormUtilState");
            String currentResidencyId = mapSavedStates.get("currentResidencyId");
            String currentHeadRelationshipId = mapSavedStates.get("currentHeadRelationshipId");

            Residency residency = StringUtil.isLong(currentResidencyId) ? boxResidencies.get(Long.parseLong(currentResidencyId)) : null;
            HeadRelationship headRelationship = StringUtil.isLong(currentHeadRelationshipId) ? boxHeadRelationships.get(Long.parseLong(currentHeadRelationshipId)) : null;
            Member member = Queries.getMemberByCode(boxMembers, outmigration.memberCode);

            if (residency != null) {
                residency.endDate = null;
                residency.endType = ResidencyEndType.NOT_APPLICABLE;
                this.boxResidencies.put(residency);

                if (member != null) {
                    member.endType = ResidencyEndType.NOT_APPLICABLE;
                    member.endDate = null;
                    boxMembers.put(member);
                }
            }

            if (headRelationship != null) {
                headRelationship.endDate = null;
                headRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
                this.boxHeadRelationships.put(headRelationship);
            }

            deleteSavedStateMap(CoreFormEntity.OUTMIGRATION, outmigration.id, "outimgFormUtilState");

            boxOutmigrations.remove(outmigration);
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
            Map<String, String> mapSavedStates = getSavedStateMap(inmigration.type==InMigrationType.INTERNAL ? CoreFormEntity.INMIGRATION : CoreFormEntity.EXTERNAL_INMIGRATION,
                                                                  inmigration.id,
                                                                  inmigration.type==InMigrationType.INTERNAL ? "intimgFormUtilState" : "extimgFormUtilState");

            if (inmigration.extMigType == ExternalInMigrationType.ENTRY) { //EXTERNAL IN

                //first entry or new individual created
                Member member = this.boxMembers.query(Member_.code.equal(inmigration.memberCode)).build().findFirst();
                removeMember(member); //this will remove the inmigration too

            } else { //INTERNAL IN or REENTRY
                //the member already existed, just remove the most recent data created by this inmigration
                ///Residency and HeadRelationship

                String strResidencyId = mapSavedStates.get("createdResidencyId"); //delete
                String strHeadRelationshipId = mapSavedStates.get("createdHeadRelationshipId"); //delete
                String strOutmigrationId = mapSavedStates.get("createdOutmigrationId"); //delete
                String strPreviousResidencyId = mapSavedStates.get("previousResidencyId"); //restore state
                String strPreviousHeadRelationshipId = mapSavedStates.get("previousHeadRelationshipId"); //restore state

                Residency createdResidency = StringUtil.isLong(strResidencyId) ? boxResidencies.get(Long.parseLong(strResidencyId)) : null;
                HeadRelationship createdHeadRelationship = StringUtil.isLong(strHeadRelationshipId) ? boxHeadRelationships.get(Long.parseLong(strHeadRelationshipId)) : null;
                Outmigration createdOutmigration = StringUtil.isLong(strOutmigrationId) ? boxOutmigrations.get(Long.parseLong(strOutmigrationId)) : null;
                Residency previousResidency = StringUtil.isLong(strPreviousResidencyId) ? boxResidencies.get(Long.parseLong(strPreviousResidencyId)) : null;
                HeadRelationship previousHeadRelationship = StringUtil.isLong(strPreviousHeadRelationshipId) ? boxHeadRelationships.get(Long.parseLong(strPreviousHeadRelationshipId)) : null;

                Member member = Queries.getMemberByCode(boxMembers, inmigration.memberCode);

                if (createdResidency != null) boxResidencies.remove(createdResidency);
                if (createdHeadRelationship != null) boxHeadRelationships.remove(createdHeadRelationship);
                if (createdOutmigration != null) boxOutmigrations.remove(createdOutmigration);


                //the residency and head_relationship were closed with CHG when we created this event
                if (inmigration.type == InMigrationType.INTERNAL) {
                    if (previousResidency != null) {
                        previousResidency.endType = ResidencyEndType.NOT_APPLICABLE;
                        previousResidency.endDate = null;
                        boxResidencies.put(previousResidency);
                    }

                    if (previousHeadRelationship != null) {
                        previousHeadRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
                        previousHeadRelationship.endDate = null;
                        boxHeadRelationships.put(previousHeadRelationship);
                    }
                } else {
                    //external inmig, residency was already closed - we dont touch the previous residency
                }

                if (member != null) {
                    Household household = boxHouseholds.query(Household_.headCode.equal(member.code).and(Household_.code.equal(inmigration.destinationCode))).build().findFirst();
                    if (household != null) {
                        household.headCode = null;
                        household.headName = null;
                        boxHouseholds.put(household);
                    }

                    //restore member previous data
                    String previousMemberDataJson = mapSavedStates.get("previousMemberDataObj");
                    if (!StringUtil.isBlank(previousMemberDataJson)) {
                        try {
                            Member previousMemberData = new Gson().fromJson(previousMemberDataJson, Member.class);

                            Log.d("previous member", previousMemberData+" *---> "+previousMemberDataJson);

                            if (previousMemberData != null) {
                                member.householdCode = previousMemberData.householdCode;
                                member.householdName = previousMemberData.householdName;
                                member.startType = previousMemberData.startType;
                                member.startDate = previousMemberData.startDate;
                                member.endType = previousMemberData.endType;
                                member.endDate = previousMemberData.endDate;
                                member.education = previousMemberData.education;
                                member.religion = previousMemberData.religion;
                                member.phonePrimary = previousMemberData.phonePrimary;
                                member.phoneAlternative = previousMemberData.phoneAlternative;
                                member.headRelationshipType = previousMemberData.headRelationshipType;
                                member.gpsNull = previousMemberData.gpsNull;
                                member.gpsAccuracy = previousMemberData.gpsAccuracy;
                                member.gpsAltitude = previousMemberData.gpsAltitude;
                                member.gpsLatitude = previousMemberData.gpsLatitude;
                                member.gpsLongitude = previousMemberData.gpsLongitude;
                                member.cosLatitude = previousMemberData.cosLatitude;
                                member.sinLatitude = previousMemberData.sinLatitude;
                                member.cosLongitude = previousMemberData.cosLongitude;
                                member.sinLongitude = previousMemberData.sinLongitude;

                                boxMembers.put(member);

                                previousMemberData = null;
                            }


                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }

            boxInmigrations.remove(inmigration);

            deleteSavedStateMap(CoreFormEntity.INMIGRATION, inmigration.id, "intimgFormUtilState");
        }

        deleteCoreCollectedData(cdata);
    }

    private void deletePregnancyOutcome(CoreCollectedData cdata) {
        //need to remove childs
        PregnancyOutcome outcome = this.boxPregnancyOutcomes.get(cdata.formEntityId);

        if (outcome != null) {

            Map<String, String> mapSavedStates = getSavedStateMap(CoreFormEntity.PREGNANCY_OUTCOME, outcome.id, PregnancyOutcomeFormUtil.SAVED_ENTITY_OBJECT_KEY);
            String pregnancyRegistrationId = mapSavedStates.get("pregnancyRegistrationId");
            String pregnancyRegistrationObj = mapSavedStates.get("pregnancyRegistrationObj");

            PregnancyRegistration pregReg = StringUtil.isLong(pregnancyRegistrationId) ? boxPregnancyRegistrations.get(Long.parseLong(pregnancyRegistrationId)) : null;
            PregnancyRegistration previousPregRegData = StringUtil.isBlank(pregnancyRegistrationObj) ? null : new Gson().fromJson(pregnancyRegistrationObj, PregnancyRegistration.class);

            //restore previous data of pregnancy registration
            Log.d("restore preg on pout", "pr="+pregReg+", prd="+previousPregRegData);
            if (pregReg != null && previousPregRegData != null) {
                pregReg.status = previousPregRegData.status;
                pregReg.summary_antepartum_count = previousPregRegData.summary_antepartum_count;
                pregReg.summary_postpartum_count = previousPregRegData.summary_postpartum_count;
                //pregReg.summary_last_visit_status = previousPregRegData.summary_last_visit_status;
                //pregReg.summary_last_visit_type = previousPregRegData.summary_last_visit_type;
                //pregReg.summary_last_visit_date = previousPregRegData.summary_last_visit_date;
                //pregReg.summary_first_visit_date = previousPregRegData.summary_first_visit_date;
                pregReg.summary_has_pregnancy_outcome = previousPregRegData.summary_has_pregnancy_outcome;
                pregReg.summary_nr_outcomes = previousPregRegData.summary_nr_outcomes;
                pregReg.summary_followup_completed = previousPregRegData.summary_followup_completed;

                boxPregnancyRegistrations.put(pregReg);
            }

            //remove created childs
            for (PregnancyChild child : outcome.childs) {

                //get member and remove member and its dependencies
                Member member = this.boxMembers.query(Member_.code.equal(child.childCode)).build().findFirst();

                if (member != null) {
                    removeMember(member); //this also removes a PregnancyCHild record
                }

            }

            deleteSavedStateMap(CoreFormEntity.PREGNANCY_OUTCOME, outcome.id, PregnancyOutcomeFormUtil.SAVED_ENTITY_OBJECT_KEY);

            this.boxPregnancyOutcomes.remove(outcome);
        }

        deleteCoreCollectedData(cdata);
    }

    private void deletePregnancyVisit(CoreCollectedData cdata) {
        //need to remove childs
        PregnancyVisit visit = this.boxPregnancyVisits.get(cdata.formEntityId);

        if (visit != null) {

            Map<String, String> mapSavedStates = getSavedStateMap(CoreFormEntity.PREGNANCY_VISIT, visit.id, PregnancyVisitFormUtil.SAVED_ENTITY_OBJECT_KEY);
            String pregnancyRegistrationId = mapSavedStates.get("pregnancyRegistrationId");            
            String pregnancyRegistrationObj = mapSavedStates.get("pregnancyRegistrationObj");
            
            PregnancyRegistration pregReg = StringUtil.isLong(pregnancyRegistrationId) ? boxPregnancyRegistrations.get(Long.parseLong(pregnancyRegistrationId)) : null;
            PregnancyRegistration previousPregRegData = StringUtil.isBlank(pregnancyRegistrationObj) ? null : new Gson().fromJson(pregnancyRegistrationObj, PregnancyRegistration.class);
            
            //restore previous data of pregnancy registration
            Log.d("restore preg", "pr="+pregReg+", prd="+previousPregRegData);
            if (pregReg != null && previousPregRegData != null) {
                pregReg.summary_antepartum_count = previousPregRegData.summary_antepartum_count;
                pregReg.summary_postpartum_count = previousPregRegData.summary_postpartum_count;
                pregReg.summary_last_visit_status = previousPregRegData.summary_last_visit_status;
                pregReg.summary_last_visit_type = previousPregRegData.summary_last_visit_type;
                pregReg.summary_last_visit_date = previousPregRegData.summary_last_visit_date;
                pregReg.summary_first_visit_date = previousPregRegData.summary_first_visit_date;
                pregReg.summary_has_pregnancy_outcome = previousPregRegData.summary_has_pregnancy_outcome;
                pregReg.summary_nr_outcomes = previousPregRegData.summary_nr_outcomes;
                pregReg.summary_followup_completed = previousPregRegData.summary_followup_completed;

                boxPregnancyRegistrations.put(pregReg);
            }

            //remove childs if exists
            for (PregnancyVisitChild child : visit.childs) {
                boxPregnancyVisitChilds.remove(child);
            }

            this.boxPregnancyVisits.remove(visit);
            
            deleteSavedStateMap(CoreFormEntity.PREGNANCY_VISIT, visit.id, PregnancyVisitFormUtil.SAVED_ENTITY_OBJECT_KEY);
        }

        deleteCoreCollectedData(cdata);
    }

    private void deletePregnancyReg(CoreCollectedData cdata) {
        this.boxPregnancyRegistrations.remove(cdata.formEntityId);
        deleteCoreCollectedData(cdata);
    }

    private void deleteMaritalRelationship(CoreCollectedData cdata) {

        MaritalRelationship createdMaritalRelationship = this.boxMaritalRelationships.get(cdata.formEntityId);

        if (createdMaritalRelationship != null) {

            Map<String, String> mapSavedStates = getSavedStateMap(CoreFormEntity.MARITAL_RELATIONSHIP, createdMaritalRelationship.id, "maritalFormUtilState");
            String createdMaritalRelationshipId = mapSavedStates.get("createdMaritalRelationshipId");
            String previousMaritalRelationshipId = mapSavedStates.get("previousMaritalRelationshipId");
            String previousMaritalRelationshipDataValue = mapSavedStates.get("previousMaritalRelationshipData");
            String previousSpouseAId = mapSavedStates.get("previous_spouseA_id");
            String previousSpouseAData = mapSavedStates.get("previous_spouseA_data");
            String previousSpouseBId = mapSavedStates.get("previous_spouseB_id");
            String previousSpouseBData = mapSavedStates.get("previous_spouseB_data");
            
            MaritalRelationship previousMaritalRelationship = StringUtil.isLong(previousMaritalRelationshipId) ? boxMaritalRelationships.get(Long.parseLong(previousMaritalRelationshipId)) : null;
            MaritalRelationship previousMaritalRelationshipData = StringUtil.isBlank(previousMaritalRelationshipDataValue) ? null : new Gson().fromJson(previousMaritalRelationshipDataValue, MaritalRelationship.class);
            Member spouseA = StringUtil.isLong(previousSpouseAId) ? boxMembers.get(Long.parseLong(previousSpouseAId)) : null;
            Member spouseB = StringUtil.isLong(previousSpouseBId) ? boxMembers.get(Long.parseLong(previousSpouseBId)) : null;
            Member previousSpouseA = StringUtil.isBlank(previousSpouseAData) ? null : new Gson().fromJson(previousSpouseAData, Member.class);
            Member previousSpouseB = StringUtil.isBlank(previousSpouseBData) ? null : new Gson().fromJson(previousSpouseBData, Member.class);

            //restore back the previousMaritalRelationship/current before the createdHeadRelationship
            if (previousMaritalRelationship != null && previousMaritalRelationshipData != null) {
                previousMaritalRelationship.endStatus = previousMaritalRelationshipData.endStatus;
                previousMaritalRelationship.endDate = previousMaritalRelationshipData.endDate;
                boxMaritalRelationships.put(previousMaritalRelationship);
            }
            //restore back the spouseA status before the maritalRelationship
            if (spouseA != null && previousSpouseA != null) {
                Log.d("restore back", "spA: "+spouseA.code+",spA.ms:"+spouseA.maritalStatus+", Previous " +"spA: "+previousSpouseA.code+",spA.ms:"+previousSpouseA.maritalStatus);
                spouseA.maritalStatus = previousSpouseA.maritalStatus;
                spouseA.spouseCode = previousSpouseA.spouseCode;
                spouseA.spouseName = previousSpouseA.spouseName;
                boxMembers.put(spouseA);
            }
            //restore back the spouseB status before the maritalRelationship
            if (spouseB != null && previousSpouseA != null) {
                Log.d("restore back", "spB: "+spouseB.code+",spB.ms:"+spouseB.maritalStatus+", Previous " +"spB: "+previousSpouseB.code+",spB.ms:"+previousSpouseB.maritalStatus);
                spouseB.maritalStatus = previousSpouseA.maritalStatus;
                spouseB.spouseCode = previousSpouseA.spouseCode;
                spouseB.spouseName = previousSpouseA.spouseName;
                boxMembers.put(spouseB);
            }

            //delete the createdMaritalRelationship
            this.boxMaritalRelationships.remove(createdMaritalRelationship); //remove marital relationship

            deleteSavedStateMap(CoreFormEntity.MARITAL_RELATIONSHIP, createdMaritalRelationship.id, "maritalFormUtilState");
        }

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

    private void deleteSavedStateMap(CoreFormEntity formEntity, Long recordId, String mapName) {

        this.boxSavedEntityStates.query(SavedEntityState_.formEntity.equal(formEntity.code)
                .and(SavedEntityState_.collectedId.equal(recordId))
                .and(SavedEntityState_.objectKey.equal(mapName))).build().remove();
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
