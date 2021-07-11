package org.philimone.hds.explorer.settings.generator;

import org.philimone.hds.explorer.model.User;

import java.util.List;

import mz.betainteractive.utilities.StringUtil;

/*
 * The HDS-Explorer Default code generator (different sites can implement they own type of codes)
 */
public class DefaultCodeGenerator implements CodeGenerator {
    final String REGION_CODE_PATTERN = "^[A-Z0-9]{3}$";
    final String HOUSEHOLD_CODE_PATTERN = "^[A-Z0-9]{6}[0-9]{3}$";
    final String MEMBER_CODE_PATTERN = "^[A-Z0-9]{6}[0-9]{6}$";
    final String VISIT_CODE_PATTERN = "^[A-Z0-9]{6}[0-9]{6}$";
    final String USER_CODE_PATTERN = "^[A-Z0-9]{3}$";
    final String PREGNANCY_CODE_PATTERN = "^[A-Z0-9]{6}[0-9]{6}-[0-9]{2}$";

    final String CHARS_A_TO_Z = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    final String CHARS_1_TO_9 = "123456789";

    @Override
    public boolean isRegionCodeValid(String code) {
        return !StringUtil.isBlank(code) && code.matches(REGION_CODE_PATTERN);
    }

    @Override
    public boolean isHouseholdCodeValid(String code) {
        return !StringUtil.isBlank(code) && code.matches(HOUSEHOLD_CODE_PATTERN);
    }

    @Override
    public boolean isMemberCodeValid(String code) {
        return !StringUtil.isBlank(code) && code.matches(MEMBER_CODE_PATTERN);
    }

    @Override
    public boolean isVisitCodeValid(String code) {
        return !StringUtil.isBlank(code) && code.matches(VISIT_CODE_PATTERN);
    }

    @Override
    public boolean isUserCodeValid(String code) {
        return !StringUtil.isBlank(code) && code.matches(USER_CODE_PATTERN);
    }

    @Override
    public boolean isPregnancyCodeValid(String code){
        return !StringUtil.isBlank(code) && code.matches(PREGNANCY_CODE_PATTERN);
    }

    @Override
    public String generateRegionCode(String regionName, List<String> existentCodes) {

        if (StringUtil.isBlank(regionName)) return null;

        //first 3 characters
        String u = regionName.toUpperCase();

        String chars = CHARS_1_TO_9 + CHARS_A_TO_Z;
        //List<String> alist = [u.charAt(0)];
        char[] blist = (u.length()>1) ? u.substring(1).toCharArray() : chars.toCharArray();
        char[] clist = (u.length()>2) ? (u.substring(2) + chars).toCharArray() : chars.toCharArray();

        String a = u.charAt(0)+"";

        for (char b : blist){
            for (char c : clist){
                String test = a + "" + b + "" + c;

                if (!existentCodes.contains(test)){
                    return test;
                }
            }

        }

        return null;
    }

    @Override
    public String generateHouseholdCode(String baseCode, List<String> existentCodes) {
        //Generate codes and try to match the database until u cant

        if (StringUtil.isBlank(baseCode)) return null;

        if (existentCodes.size()==0){
            return baseCode+"001";
        } else {
            for (int i=1; i <= 999; i++){
                String code = baseCode+ String.format("%03d", i);
                if (!existentCodes.contains(code)){
                    return code;
                }
            }
        }

        return baseCode+"ERROR";
    }

    @Override
    public String generateMemberCode(String baseCode, List<String> existentCodes) {
        //Generate codes and try to match the database until u cant

        if (StringUtil.isBlank(baseCode)) return null;

        if (existentCodes.size()==0){
            return baseCode+"001";
        } else {
            for (int i=1; i <= 999; i++){
                String code = baseCode + String.format("%03d", i);
                if (!existentCodes.contains(code)){
                    return code;
                }
            }
        }

        return baseCode+"ERROR";
    }

    @Override
    public String generateVisitCode(String baseCode, List<String> existentCodes) {

        if (StringUtil.isBlank(baseCode)) return null;

        if (existentCodes.size()==0){
            return baseCode+"001";
        } else {

            String first = existentCodes.get(existentCodes.size()-1);
            String sorder = first.replaceAll(baseCode, "");
            Integer n = StringUtil.toInteger(sorder);

            if (n==null) n = 1;
            for (int i=n; i <= 999; i++){
                String code = baseCode + String.format("%03d", i);
                if (!existentCodes.contains(code)){
                    return code;
                }
            }
        }

        return null;
    }

    @Override
    public String generateUserCode(User user, List<String> existentCodes) {
        String regexFw = "^FW[A-Za-z]{3}$";
        String username = user.username;

        if (username.matches(regexFw)){ //ohds fieldworker
            return username.toUpperCase().replaceAll("FW", "");
        }else {
            //def codes = User.list().collect{ t -> t.code}

            String f = user.firstName.toUpperCase();
            String l = user.lastName.toUpperCase();
            char[] alist = f.toCharArray();
            char[] blist = l.toCharArray();
            char[] clist = (CHARS_1_TO_9 + (l.length()>1 ? l.substring(1) : "") + CHARS_A_TO_Z).toCharArray();

            for (char a : alist){
                for (char b : blist){
                    for (char c : clist){
                        String test = a+""+b+""+c;

                        if (!existentCodes.contains(test)){
                            return test;
                        }
                    }

                }
            }
        }

        return null;
    }

    @Override
    public String generatePregnancyCode(String baseCode, List<String> existentCodes) {
        if (StringUtil.isBlank(baseCode)) return null;

        if (existentCodes.size()==0){
            return baseCode+"-01";
        } else {

            String first = existentCodes.get(existentCodes.size()-1);
            String sorder = first.replaceAll(baseCode+"-", "");
            Integer n = StringUtil.toInteger(sorder);

            if (n==null) n = 1;
            for (int i=n; i <= 99; i++){
                String code = baseCode+"-"+ String.format("%02d", i);
                if (!existentCodes.contains(code)){
                    return code;
                }
            }
        }

        return baseCode+"-FULL";

    }
}
