package org.philimone.hds.explorer.server.settings.generator;

import android.util.Log;

import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.PregnancyRegistration;
import org.philimone.hds.explorer.model.PregnancyRegistration_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.Round;
import org.philimone.hds.explorer.model.Round_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.User_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.Visit_;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import mz.betainteractive.utilities.GeneralUtil;

public class CodeGeneratorService {

    private CodeGenerator codeGenerator;
    private Box<User> boxUsers;
    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<Visit> boxVisits;
    private Box<Round> boxRounds;
    private Box<PregnancyRegistration> boxPregnancies;
    private Box<ApplicationParam> boxAppParams;

    public CodeGeneratorService() {
        initBoxes();

        this.codeGenerator =  CodeGeneratorFactory.newInstance();
    }

    private void initBoxes() {
        this.boxUsers = ObjectBoxDatabase.get().boxFor(User.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxVisits = ObjectBoxDatabase.get().boxFor(Visit.class);
        this.boxRounds = ObjectBoxDatabase.get().boxFor(Round.class);
        this.boxPregnancies = ObjectBoxDatabase.get().boxFor(PregnancyRegistration.class);
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
    }

    public boolean isRegionCodeValid(String code) {
        return codeGenerator.isRegionCodeValid(code);
    }

    public boolean isLowestRegionCodeValid(String code) {
        return codeGenerator.isLowestRegionCodeValid(code);
    }

    public boolean isHouseholdCodeValid(String code) {
        return codeGenerator.isHouseholdCodeValid(code);
    }

    public boolean isMemberCodeValid(String code) {
        return codeGenerator.isMemberCodeValid(code);
    }

    public boolean isVisitCodeValid(String code) {
        return codeGenerator.isVisitCodeValid(code);
    }

    public boolean isUserCodeValid(String code) {
        return codeGenerator.isUserCodeValid(code);
    }

    public boolean isPregnancyCodeValid(String code){
        return codeGenerator.isPregnancyCodeValid(code);
    }

    public String generateRegionCode(Region parentRegion, String regionName) {
        List<String> codes = boxRegions.query().order(Region_.code).build().find().stream().map(Region::getCode).collect(Collectors.toList());
        return codeGenerator.generateRegionCode(parentRegion, regionName, codes);
    }

    public String generateLowestRegionCode(Region parentRegion, String regionName) {
        List<String> codes = boxRegions.query().order(Region_.code).build().find().stream().map(Region::getCode).collect(Collectors.toList());
        return codeGenerator.generateLowestRegionCode(parentRegion, regionName, codes);
    }

    public String generateHouseholdCode(Region region, User user) {
        String cbase = codeGenerator.getHouseholdBaseCode(region, user);
        List<String> codes = boxHouseholds.query().startsWith(Household_.code, cbase, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                   .order(Household_.code).build().find()
                                                   .stream().map(Household::getCode).collect(Collectors.toList());

        return codeGenerator.generateHouseholdCode(cbase, codes);
    }

    public String generateMemberCode(Household household) {

        String cbase = household.code;
        List<String> codes = boxMembers.query().startsWith(Member_.code, cbase, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                .order(Member_.code).build().find()
                                                .stream().map(Member::getCode).collect(Collectors.toList());

        return codeGenerator.generateMemberCode(cbase, codes);
    }

    public String generateMemberCode(Household household, List<String> extraCodes) {

        String cbase = household.code;
        List<String> codes = boxMembers.query().startsWith(Member_.code, cbase, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .order(Member_.code).build().find()
                .stream().map(Member::getCode).collect(Collectors.toList());

        for (String code : extraCodes) {
            codes.add(code);
        }
        //codes.addAll(extraCodes);

        return codeGenerator.generateMemberCode(cbase, codes);
    }

    public String generateVisitCode(Household household) {

        long roundNumber = boxRounds.query().build().property(Round_.roundNumber).max();
        Round round = boxRounds.query(Round_.roundNumber.equal(roundNumber)).build().findFirst();
        String cbase = codeGenerator.getVisitBaseCode(household, round);

        List<String> codes = boxVisits.query().startsWith(Visit_.code, cbase, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                               .order(Visit_.code).build().find()
                                               .stream().map(Visit::getCode).collect(Collectors.toList());

        return codeGenerator.generateVisitCode(cbase, codes);
    }

    public String generateUserCode(User user) {
        List<String> codes = boxUsers.query().order(User_.code).build().find().stream().map(User::getCode).collect(Collectors.toList());
        return codeGenerator.generateUserCode(user, codes);
    }

    public String generatePregnancyCode(Member mother) {

        String cbase = mother.code;
        List<String> codes = boxPregnancies.query().startsWith(PregnancyRegistration_.code, cbase, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                    .order(PregnancyRegistration_.code).build().find()
                                                    .stream().map(PregnancyRegistration::getCode).collect(Collectors.toList());

        return codeGenerator.generatePregnancyCode(cbase, codes);
    }

    public String getModuleSampleCode() {
        return codeGenerator.getModuleSampleCode();
    }

    public String getRegionSampleCode() {
        return codeGenerator.getRegionSampleCode();
    }

    public String getLowestRegionSampleCode() {
        return codeGenerator.getLowestRegionSampleCode();
    }

    public String getHouseholdSampleCode() {
        return codeGenerator.getHouseholdSampleCode();
    }

    public String getMemberSampleCode() {
        return codeGenerator.getMemberSampleCode();
    }

    public String getVisitSampleCode() {
        return codeGenerator.getVisitSampleCode();
    }

    public String getUserSampleCode() {
        return codeGenerator.getUserSampleCode();
    }

    public String getPregnancySampleCode() {
        return codeGenerator.getPregnancySampleCode();
    }
}
