package org.philimone.hds.explorer.fragment.showcollected.utilities;

import android.util.Log;

import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.fragment.showcollected.adapter.model.CoreCollectedDataItem;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreCollectedData_;
import org.philimone.hds.explorer.model.Death;
import org.philimone.hds.explorer.model.Death_;
import org.philimone.hds.explorer.model.HeadRelationship;
import org.philimone.hds.explorer.model.HeadRelationship_;
import org.philimone.hds.explorer.model.Household;
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
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.Residency;
import org.philimone.hds.explorer.model.Residency_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.Visit_;
import org.philimone.hds.explorer.model.enums.MaritalEndStatus;
import org.philimone.hds.explorer.model.enums.temporal.ExternalInMigrationType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipEndType;
import org.philimone.hds.explorer.model.enums.temporal.HeadRelationshipStartType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyStartType;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import io.objectbox.Box;
import mz.betainteractive.utilities.GeneralUtil;

public class CoreCollectedDataDeletionUtil {

    private Box<CoreCollectedData> boxCoreCollectedData;
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
    private Box<Death> boxDeaths;
    private Box<HeadRelationship> boxHeadRelationships;
    private Box<Residency> boxResidencies;
    private Box<IncompleteVisit> boxIncompleteVisits;

    public CoreCollectedDataDeletionUtil() {
        this.initBoxes();
    }

    private void initBoxes() {
        this.boxCoreCollectedData = ObjectBoxDatabase.get().boxFor(CoreCollectedData.class);
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
        this.boxDeaths = ObjectBoxDatabase.get().boxFor(Death.class);
        this.boxHeadRelationships = ObjectBoxDatabase.get().boxFor(HeadRelationship.class);
        this.boxResidencies = ObjectBoxDatabase.get().boxFor(Residency.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxIncompleteVisits = ObjectBoxDatabase.get().boxFor(IncompleteVisit.class);
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

    private void deleteCoreCollectedDataList(List<CoreCollectedData> coreCollectedDataList){
        for (CoreCollectedData cdata : coreCollectedDataList) {

            switch (cdata.formEntity) {
                case REGION: deleteRegion(cdata); break;
                case VISIT: deleteVisit(cdata); break;
                case HOUSEHOLD: deleteHousehold(cdata); break;
                case MARITAL_RELATIONSHIP: deleteMaritalRelationship(cdata); break;
                case PREGNANCY_REGISTRATION: deletePregnancyReg(cdata); break;
                case PREGNANCY_OUTCOME: deletePregnancyOutcome(cdata); break;
                case EXTERNAL_INMIGRATION: deleteInmigration(cdata); break;
                case OUTMIGRATION: deleteOutmigration(cdata); break;
                case INMIGRATION: deleteInmigration(cdata); break;
                case DEATH: deleteDeath(cdata); break;
                case MEMBER_ENU: deleteMemberEnumeration(cdata); break;
                case CHANGE_HOUSEHOLD_HEAD: deleteChangeHouseholdHead(cdata); break;
                case INCOMPLETE_VISIT: deleteIncompleteVisit(cdata); break;
                case PRE_HOUSEHOLD: deletePreHousehold(cdata); break;
                case EDITED_REGION: deleteCoreCollectedData(cdata); break;
                case EDITED_HOUSEHOLD: deleteCoreCollectedData(cdata); break;
                case EDITED_MEMBER: deleteCoreCollectedData(cdata); break;
            }
        }
    }

    private void deleteRegion(CoreCollectedData cdata) {
        Region region = boxRegions.query(Region_.collectedId.equal(cdata.collectedId)).build().findFirst();

        if (region != null) {
            removeRegion(region);
        }

        deleteCoreCollectedData(cdata);
    }

    private void deleteVisit(CoreCollectedData cdata) {
        Visit visit = boxVisits.query(Visit_.collectedId.equal(cdata.collectedId)).build().findFirst();

        //delete this first to avoid loops when call removeVisit - that will remove all CoreCollectedData of this visit
        deleteCoreCollectedData(cdata);

        if (visit != null) {
            removeVisit(visit);
        }
    }

    private void deleteHousehold(CoreCollectedData cdata) {

        Household household = boxHouseholds.query(Household_.collectedId.equal(cdata.collectedId)).build().findFirst();

        deleteCoreCollectedData(cdata);

        if (household != null) {
            removeHousehold(household);
        }
    }

    private void deletePreHousehold(CoreCollectedData cdata) {
        boxHouseholds.query(Household_.collectedId.equal(cdata.collectedId)).build().remove();
        deleteCoreCollectedData(cdata);
    }

    private void deleteIncompleteVisit(CoreCollectedData cdata) {
        boxIncompleteVisits.query(IncompleteVisit_.collectedId.equal(cdata.collectedId)).build().remove();
        deleteCoreCollectedData(cdata);
    }

    private void deleteChangeHouseholdHead(CoreCollectedData cdata) {

        HeadRelationship currentHeadRel = boxHeadRelationships.query(HeadRelationship_.collectedId.equal(cdata.collectedId)).build().findFirst();

        if (currentHeadRel != null) {
            Household household = boxHouseholds.query(Household_.code.equal(currentHeadRel.householdCode)).build().findFirst();
            Member currentHead = boxMembers.query(Member_.code.equal(currentHeadRel.memberCode)).build().findFirst();
            Date eventDate = GeneralUtil.getDateAdd(currentHeadRel.startDate, -1); //the event date that will be used on endDate to get the previous head of household


            if (household != null) {
                List<HeadRelationship> previousHeadRelationships = boxHeadRelationships.query(HeadRelationship_.householdCode.equal(household.code)
                                .and(HeadRelationship_.endType.equal(HeadRelationshipEndType.CHANGE_OF_HEAD_OF_HOUSEHOLD.code))
                                .and(HeadRelationship_.endDate.equal(eventDate)))
                        .build().find();

                //restore previous head relationships end status
                if (previousHeadRelationships != null) {
                    for (HeadRelationship headrel : previousHeadRelationships) {
                        headrel.endType = HeadRelationshipEndType.NOT_APPLICABLE;
                        headrel.endDate = null;
                        this.boxHeadRelationships.put(headrel);
                    }
                }

                //delete all the current head relationships with the currentHead
                boxHeadRelationships.query(HeadRelationship_.householdCode.equal(household.code).and(HeadRelationship_.headCode.equal(currentHead.code)).and(HeadRelationship_.startDate.equal(currentHead.startDate))).build().remove();

            }

            //delete current head relationship
            boxHeadRelationships.remove(currentHeadRel);
        }

        deleteCoreCollectedData(cdata);

        //its always better to perform synchronization after deleting records to ensure data integrity
    }

    private void deleteMemberEnumeration(CoreCollectedData cdata) {
        Member member = boxMembers.query(Member_.id.equal(cdata.formEntityId)).build().findFirst();
        removeMember(member);
        deleteCoreCollectedData(cdata);
    }

    private void deleteDeath(CoreCollectedData cdata) {
        //removes Residency, HeadRelationship, MaritalRelationship endStatus, and delete Death

        Death death = boxDeaths.query(Death_.id.equal(cdata.formEntityId)).build().findFirst();

        if (death != null) {
            Residency residency = boxResidencies.query(Residency_.memberCode.equal(death.memberCode).and(Residency_.endDate.equal(death.deathDate)).and(Residency_.endType.equal(ResidencyEndType.DEATH.code))).build().findFirst();
            HeadRelationship headRelationship = boxHeadRelationships.query(HeadRelationship_.memberCode.equal(death.memberCode).and(HeadRelationship_.endDate.equal(death.deathDate)).and(HeadRelationship_.endType.equal(HeadRelationshipEndType.DEATH.code))).build().findFirst();

            MaritalRelationship maritalRelationship = boxMaritalRelationships.query(MaritalRelationship_.endStatus.equal(MaritalEndStatus.WIDOWED.code).and(MaritalRelationship_.endDate.equal(death.deathDate))
                    .and(MaritalRelationship_.memberA_code.equal(death.memberCode).or(MaritalRelationship_.memberB_code.equal(death.memberCode)))).build().findFirst();

            residency.endType = ResidencyEndType.NOT_APPLICABLE;
            residency.endDate = null;
            headRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;
            headRelationship.endDate = null;
            maritalRelationship.endStatus = MaritalEndStatus.NOT_APPLICABLE;
            maritalRelationship.endDate = null;

            this.boxResidencies.put(residency);
            this.boxHeadRelationships.put(headRelationship);
            this.boxMaritalRelationships.put(maritalRelationship);
        }

        deleteCoreCollectedData(cdata);
    }

    private void deleteOutmigration(CoreCollectedData cdata) {
        //removes Residency, HeadRelationship endStatus, and delete Outmigration

        Outmigration outmigration = boxOutmigrations.query(Outmigration_.id.equal(cdata.formEntityId)).build().findFirst();

        if (outmigration != null) {
            Residency residency = boxResidencies.query(Residency_.memberCode.equal(outmigration.memberCode).and(Residency_.endDate.equal(outmigration.migrationDate)).and(Residency_.endType.equal(outmigration.migrationType.code))).build().findFirst();
            HeadRelationship headRelationship = boxHeadRelationships.query(HeadRelationship_.memberCode.equal(outmigration.memberCode).and(HeadRelationship_.endDate.equal(outmigration.migrationDate)).and(HeadRelationship_.endType.equal(outmigration.migrationType.code))).build().findFirst();

            residency.endDate = null;
            residency.endType = ResidencyEndType.NOT_APPLICABLE;

            headRelationship.endDate = null;
            headRelationship.endType = HeadRelationshipEndType.NOT_APPLICABLE;

            this.boxResidencies.put(residency);
            this.boxHeadRelationships.put(headRelationship);
        }

        deleteCoreCollectedData(cdata);
    }

    private void deleteInmigration(CoreCollectedData cdata) {
        //can remove a Member
        //will have to definetely remove the recent Residency and HeadRelationship
        //will update the Member residency status
        //can update the Household

        Inmigration inmigration = boxInmigrations.query(Inmigration_.id.equal(cdata.formEntityId)).build().findFirst();

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

            List<IncompleteVisit> listIncs = boxIncompleteVisits.query(IncompleteVisit_.visitId.equal(visit.id)).build().find();
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

            List<IncompleteVisit> listivs = boxIncompleteVisits.query(IncompleteVisit_.memberId.equal(member.id)).build().find();
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

    private void deleteCoreCollectedData(CoreCollectedData cdata) {
        if (cdata != null) {
            deleteXmlFile(cdata);
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

}
