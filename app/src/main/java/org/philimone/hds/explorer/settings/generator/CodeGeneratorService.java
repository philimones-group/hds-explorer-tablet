package org.philimone.hds.explorer.settings.generator;

import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Member_;
import org.philimone.hds.explorer.model.PregnancyRegistration;
import org.philimone.hds.explorer.model.PregnancyRegistration_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.User_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.explorer.model.Visit_;

import java.util.Arrays;
import java.util.List;

import io.objectbox.Box;

public class CodeGeneratorService {

    private CodeGenerator codeGenerator;
    private Box<User> boxUsers;
    private Box<Region> boxRegions;
    private Box<Household> boxHouseholds;
    private Box<Member> boxMembers;
    private Box<Visit> boxVisits;
    private Box<PregnancyRegistration> boxPregnancies;
    

    public CodeGeneratorService() {
        this.codeGenerator =  CodeGeneratorFactory.newInstance();
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
        String[] codesArray = boxHouseholds.query().startsWith(Household_.code, cbase)
                                                   .order(Household_.code).build()
                                                   .property(Household_.code).findStrings();

        List<String> codes = Arrays.asList(codesArray); //Household.findAllByCodeLike("${cbase}%", [sort:'code', order: 'asc']).collect{ t -> t.code};

        return codeGenerator.generateHouseholdCode(cbase, codes);
    }

    public String generateMemberCode(Household household) {

        String cbase = household.code;
        String[] codesArray = boxMembers.query().startsWith(Member_.code, cbase)
                                                .order(Member_.code).build()
                                                .property(Member_.code).findStrings();
        List<String> codes = Arrays.asList(codesArray); //Member.findAllByCodeLike("${cbase}%", [sort:'code', order: 'asc']).collect{ t -> t.code};

        return codeGenerator.generateMemberCode(cbase, codes);
    }

    public String generateVisitCode(Household household) {

        String cbase = household.code;
        String[] codesArray = boxVisits.query().startsWith(Visit_.code, cbase)
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
        String[] codesArray = boxPregnancies.query().startsWith(PregnancyRegistration_.code, cbase)
                                                    .order(PregnancyRegistration_.code).build()
                                                    .property(PregnancyRegistration_.code).findStrings();

        List<String> codes = Arrays.asList(codesArray); //PregnancyRegistration.findAllByCodeLike("${cbase}%", [sort:'code', order: 'asc']).collect{ t -> t.code};

        return codeGenerator.generatePregnancyCode(cbase, codes);
    }
}
