package org.philimone.hds.explorer.server.settings.generator;

import org.philimone.hds.explorer.database.ObjectBoxDatabase;
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

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

public class CodeGeneratorService {

    private CodeGenerator codeGenerator;
    private Box<User> boxUsers;
    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<Visit> boxVisits;
    private Box<Round> boxRounds;
    private Box<PregnancyRegistration> boxPregnancies;

    public CodeGeneratorService() {
        this.codeGenerator =  CodeGeneratorFactory.newInstance();

        this.boxUsers = ObjectBoxDatabase.get().boxFor(User.class);
        this.boxRegions = ObjectBoxDatabase.get().boxFor(Region.class);
        this.boxHouseholds = ObjectBoxDatabase.get().boxFor(Household.class);
        this.boxMembers = ObjectBoxDatabase.get().boxFor(Member.class);
        this.boxVisits = ObjectBoxDatabase.get().boxFor(Visit.class);
        this.boxRounds = ObjectBoxDatabase.get().boxFor(Round.class);
        this.boxPregnancies = ObjectBoxDatabase.get().boxFor(PregnancyRegistration.class);
    }

    public boolean isRegionCodeValid(String code) {
        return codeGenerator.isRegionCodeValid(code);
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

    public String generateRegionCode(String regionName) {

        String[] codesArray = boxRegions.query().build().property(Region_.code).findStrings();
        List<String> codes = Arrays.asList(codesArray);
        return codeGenerator.generateRegionCode(regionName, codes);
    }

    public String generateHouseholdCode(Region region, User user) {
        String cbase = region.code + user.code;
        String[] codesArray = boxHouseholds.query().startsWith(Household_.code, cbase, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                   .order(Household_.code).build()
                                                   .property(Household_.code).findStrings();

        List<String> codes = Arrays.asList(codesArray); //Household.findAllByCodeLike("${cbase}%", [sort:'code', order: 'asc']).collect{ t -> t.code};

        return codeGenerator.generateHouseholdCode(cbase, codes);
    }

    public String generateMemberCode(Household household) {

        String cbase = household.code;
        String[] codesArray = boxMembers.query().startsWith(Member_.code, cbase, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                .order(Member_.code).build()
                                                .property(Member_.code).findStrings();
        List<String> codes = Arrays.asList(codesArray); //Member.findAllByCodeLike("${cbase}%", [sort:'code', order: 'asc']).collect{ t -> t.code};

        return codeGenerator.generateMemberCode(cbase, codes);
    }

    public String generateMemberCode(Household household, List<String> extraCodes) {

        String cbase = household.code;
        String[] codesArray = boxMembers.query().startsWith(Member_.code, cbase, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .order(Member_.code).build()
                .property(Member_.code).findStrings();
        List<String> codes = new ArrayList<>(); //Member.findAllByCodeLike("${cbase}%", [sort:'code', order: 'asc']).collect{ t -> t.code};

        for (int i = 0; i < codesArray.length; i++) {
            codes.add(codesArray[i]);
        }

        for (String code : extraCodes) {
            codes.add(code);
        }
        //codes.addAll(extraCodes);

        return codeGenerator.generateMemberCode(cbase, codes);
    }

    public String generateVisitCode(Household household) {

        long round = boxRounds.query().build().property(Round_.roundNumber).max();
        String cbase = household.code + "-" + String.format("%03d", round);
        String[] codesArray = boxVisits.query().startsWith(Visit_.code, cbase, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                               .order(Visit_.code).build()
                                               .property(Visit_.code).findStrings();

        List<String> codes = Arrays.asList(codesArray); //Visit.findAllByCodeLike("${cbase}%", [sort:'code', order: 'asc']).collect{ t -> t.code};

        return codeGenerator.generateVisitCode(cbase, codes);
    }

    public String generateUserCode(User user) {

        String[] codesArray = boxUsers.query().build().property(User_.code).findStrings();
        List<String> codes = Arrays.asList(codesArray); //User.list().collect{ t -> t.code};

        return codeGenerator.generateUserCode(user, codes);
    }

    public String generatePregnancyCode(Member mother) {

        String cbase = mother.code;
        String[] codesArray = boxPregnancies.query().startsWith(PregnancyRegistration_.code, cbase, QueryBuilder.StringOrder.CASE_SENSITIVE)
                                                    .order(PregnancyRegistration_.code).build()
                                                    .property(PregnancyRegistration_.code).findStrings();

        List<String> codes = Arrays.asList(codesArray); //PregnancyRegistration.findAllByCodeLike("${cbase}%", [sort:'code', order: 'asc']).collect{ t -> t.code};

        return codeGenerator.generatePregnancyCode(cbase, codes);
    }
}
