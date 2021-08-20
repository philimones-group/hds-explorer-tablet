package mz.betainteractive.utilities;

import java.sql.Timestamp;
import java.text.Normalizer;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by paul on 6/7/15.
 */
public class StringUtil {
    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

    public static Time getTime(String timeStr){

        if (timeStr == null || timeStr.trim().isEmpty()) return null; //dont know

        if (timeStr.equals("99")){
            return new Time(true);
        }

        String[] tm = timeStr.split(":");

        Time time = new Time();

        try{
            if (!timeStr.contains(":")){
                time.setHours(Integer.parseInt(timeStr));
            }else{
                time.setHours(Integer.parseInt(tm[0]));
                time.setMinutes(Integer.parseInt(tm[1]));
            }

        }catch(ArrayIndexOutOfBoundsException ex){
            System.out.println("tm content: "+timeStr+", err-msg: "+ex.getMessage());
            //ex.printStackTrace();
            return null;
        }

        return time;
    }

    public static String removeAcentuation(String text) {

        String converted = Normalizer.normalize(text, Normalizer.Form.NFD);
        converted = converted.replaceAll("\\p{M}", "");

        return converted;
    }

    public static String removeQuotes(String label) {
        label = label.replaceAll("^\"|\"$", "");
        return label;
    }

    public static boolean isUppercase(String text){
        return text.equals(text.toUpperCase());
    }

    //Search if a certain string is inside a text returning percentage

    //levenstein algorhytm

    /*
        This method searchs all portions of the word on a text, and returns a report about the search
        Without using symbol sensivite
    */
    static SearchReport search(String word, String onText) {
        return search(word, onText, false);
    }

    /*
        This method searchs all portions of the word on a text, and returns a report about the search
    */
    static SearchReport search(String word, String onText, boolean isSymbolSensivite) {

        if (!isSymbolSensivite) {
            onText = removeAcentuation(onText.toLowerCase());
            word = removeAcentuation(word.toLowerCase());
        }

        String[] words = word.split("\\s++");
        boolean[] check = new boolean[words.length];
        int[] indexes = new int[words.length];

        for (int i = 0; i < check.length; i++) {

            if (onText.contains(words[i])) {
                check[i] = true;
                indexes[i] = onText.indexOf(words[i]);
            } else {
                check[i] = false;
            }
        }

        return new SearchReport(indexes, words, check);
    }

    public static double isSimilar(String str1, String str2) {
        return isSimilar(str1, str2, false);
    }

    public static double isSimilar(String str1, String str2, boolean isSymbolSensivite) {
        if (str1 == null || str2 == null) {
            return 0;
        }

        double maxlength = Math.max(str1.length(), str2.length());
        double distance = levenshteinDistance(str1, str2, isSymbolSensivite);
        return 1 - (distance / maxlength);
    }

    private static int levenshteinDistance(String s0, String s1, boolean isSymbolSensivite) {
        int len0 = s0.length() + 1;
        int len1 = s1.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        //search lower strings without acentuation
        if (!isSymbolSensivite) {
            s0 = removeAcentuation(s0.toLowerCase());
            s1 = removeAcentuation(s1.toLowerCase());
        }

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) {
            cost[i] = i;
        }

        // dynamically computing the array of distances
        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for (int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (s0.charAt(i - 1) == s1.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }

    public static class SearchReport {

        private int[] indexesOfSearchedWords;
        private String[] searchedWords;
        private boolean found;
        private double percentage;

        public SearchReport(int[] indexes, String[] words, boolean[] checked) {
            this.indexesOfSearchedWords = indexes;
            this.searchedWords = words;
            percentage = calculatePercentage(checked);
            this.found = percentage > 0.9;
        }

        private double calculatePercentage(boolean[] checked) {
            double checkedOnes = 0;
            double total = checked.length;

            for (boolean b : checked) {
                if (b == true) {
                    checkedOnes++;
                }
            }

            return total == 0 ? 0.0 : (checkedOnes / total);
        }

        public boolean hasFound() {
            return found;
        }

        public double getPercentage() {
            return percentage;
        }

        public int[] getIndexesOfSearchedWords() {
            return indexesOfSearchedWords;
        }

        public String[] getSearchedWords() {
            return searchedWords;
        }
    }

    public static class Time {
        boolean unknown = false;
        int hours;
        int minutes;
        int seconds;

        public Time(){

        }

        public Time(boolean dontKnown){
            this.unknown = dontKnown;
        }

        public Time(int hours, int minutes, int seconds) {
            this.hours = hours;
            this.minutes = minutes;
            this.seconds = seconds;
        }

        public boolean isUnknown(){
            return unknown;
        }

        public int getTotalHoursAprx(){
            return minutes >= 30 ? hours+1 : hours;
        }

        public double getFloatHours(){
            return (hours*1.0) + (minutes*0.01);
        }

        public double getHoursWithPercentageMinutes(){
            double minf = (minutes*1.0) / 60.0;
            return (hours*1.0) + minf;
        }

        public int getMinutes() {
            return minutes;
        }

        public int getHours() {
            return hours;
        }

        public int getSeconds() {
            return seconds;
        }

        public void setHours(int hours) {
            this.hours = hours;
        }

        public void setMinutes(int minutes) {
            this.minutes = minutes;
        }

        public void setSeconds(int seconds) {
            this.seconds = seconds;
        }

        public String getFormattedTime(){
            if (isUnknown()){
                return "99";
            }
            return String.format("%02d", hours) + ":" + String.format("%02d", minutes);
        }
    }

    public static String format(Date date, String format){
        java.text.DateFormat formatter = new java.text.SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static String formatYMD(Date date){
        java.text.DateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }

    public static String formatYMDHMS(Date date){
        java.text.DateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    public static Date toDate(String date, String format){
        java.text.DateFormat formatter = new java.text.SimpleDateFormat(format);
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static boolean isBlank(String value){
        return value==null || value.trim().isEmpty();
    }

    public static boolean isBlankDate(LocalDate value){
        return value==null;
    }

    public static boolean isBlankBoolean(Boolean value){
        return value==null;
    }

    public static boolean isBlankInteger(Integer value){
        return value==null;
    }

    public static boolean isBlankDouble(Double value){
        return value==null;
    }

    public static boolean containsAny(String[] list, String[] items){
        List<String> aList = Arrays.asList(list);
        return containsAny(aList, items);
    }

    public static boolean containsAny(List<String> list, String[] items){
        for (String item : items){
            if (list.contains(item)){
                return true;
            }
        }

        return false;
    }

    public static boolean containsAny(List<String> list, List<String> items){
        for (String item : items){
            if (list.contains(item)){
                return true;
            }
        }

        return false;
    }

    public static String toInClause(String[] items){
        //"'name1', 'name2"
        String str = "";

        for (String s : items){
            str += "'" + s + "',";
        }

        if (str.length()>0){
            str = str.substring(0,str.length()-1);
        }

        return str;
    }

    public static String toInClause(List<String> items){
        //"'name1', 'name2"
        String str = "";

        for (String s : items){
            str += "'" + s + "',";
        }

        if (str.length()>0){
            str = str.substring(0,str.length()-1);
        }

        return str;
    }

    /**Numbers**/
    public static boolean isDouble(String str){
        final String Digits     = "(\\p{Digit}+)";
        final String HexDigits  = "(\\p{XDigit}+)";
        // an exponent is 'e' or 'E' followed by an optionally
        // signed decimal integer.
        final String Exp        = "[eE][+-]?"+Digits;
        final String fpRegex    =
                ("[\\x00-\\x20]*"+  // Optional leading "whitespace"
                        "[+-]?(" + // Optional sign character
                        "NaN|" +           // "NaN" string
                        "Infinity|" +      // "Infinity" string

                        // A decimal floating-point string representing a finite positive
                        // number without a leading sign has at most five basic pieces:
                        // Digits . Digits ExponentPart FloatTypeSuffix
                        //
                        // Since this method allows integer-only strings as input
                        // in addition to strings of floating-point literals, the
                        // two sub-patterns below are simplifications of the grammar
                        // productions from section 3.10.2 of
                        // The Java Language Specification.

                        // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                        "((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+

                        // . Digits ExponentPart_opt FloatTypeSuffix_opt
                        "(\\.("+Digits+")("+Exp+")?)|"+

                        // Hexadecimal strings
                        "((" +
                        // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                        "(0[xX]" + HexDigits + "(\\.)?)|" +

                        // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                        "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

                        ")[pP][+-]?" + Digits + "))" +
                        "[fFdD]?))" +
                        "[\\x00-\\x20]*");// Optional trailing "whitespace"

        /*
        if (Pattern.matches(fpRegex, str))
            Double.valueOf(str); // Will not throw NumberFormatException
        else {
            // Perform suitable alternative action
        }*/

        return Pattern.matches(fpRegex, str);
    }

    public static Double toDouble(String value){
        try{
            return Double.parseDouble(value);
        } catch (Exception ex){
            return null;
        }
    }

    public static Integer toInteger(String value){
        if (value == null) return null;

        try{
            return Integer.parseInt(value);
        } catch (Exception ex){
            return null;
        }
    }

    public static Boolean toBoolean(String value){
        if (value == null) return null;

        try{
            return Boolean.parseBoolean(value);
        } catch (Exception ex){
            return null;
        }
    }
}
