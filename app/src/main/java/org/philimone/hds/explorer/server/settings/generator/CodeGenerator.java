package org.philimone.hds.explorer.server.settings.generator;

import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Round;
import org.philimone.hds.explorer.model.User;

import java.util.List;

public interface CodeGenerator {

    String getName();

    boolean isRegionCodeValid(String code);

    boolean isLowestRegionCodeValid(String code);

    boolean isHouseholdCodeValid(String code);

    boolean isMemberCodeValid(String code);

    boolean isVisitCodeValid(String code);

    boolean isUserCodeValid(String code);

    boolean isPregnancyCodeValid(String code);

    String generateRegionCode(Region parentRegion, String regionName, List<String> existentCodes);

    String generateLowestRegionCode(Region parentRegion, String regionName, List<String> existentCodes);

    String generateHouseholdCode(String baseCode, List<String> existentCodes);

    String generateMemberCode(String baseCode, List<String> existentCodes);

    String generateVisitCode(String baseCode, List<String> existentCodes);

    String generateUserCode(User user, List<String> existentCodes);

    String generatePregnancyCode(String baseCode, List<String> existentCodes);

    String getHouseholdBaseCode(Region region, User user);

    String getVisitBaseCode(Household household, Round round);

    String getModuleSampleCode();

    String getRegionSampleCode();

    String getLowestRegionSampleCode();

    String getHouseholdSampleCode();

    String getMemberSampleCode();

    String getVisitSampleCode();

    String getUserSampleCode();

    String getPregnancySampleCode();

}
