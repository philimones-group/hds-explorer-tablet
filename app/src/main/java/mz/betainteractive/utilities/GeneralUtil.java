package mz.betainteractive.utilities;

import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by paul on 4/13/17.
 */
public class GeneralUtil {
    public static Date getDate(DatePicker datePicker){
        return new Date(datePicker.getCalendarView().getDate());
    }
    public  static int getAge(Date dobDate){
        Calendar now = Calendar.getInstance();
        Calendar dob = Calendar.getInstance();
        dob.setTime(dobDate);

        int age = now.get(Calendar.YEAR)-dob.get(Calendar.YEAR) + (now.get(Calendar.DAY_OF_YEAR)<dob.get(Calendar.DAY_OF_YEAR) ? -1 : 0);

        return age;
    }

    public static int getAge(Date dobDate, Date endDate){
        Calendar end = Calendar.getInstance();
        Calendar dob = Calendar.getInstance();
        end.setTime(endDate);
        dob.setTime(dobDate);

        if (dob != null) {

        }

        int age = end.get(Calendar.YEAR)-dob.get(Calendar.YEAR) + (end.get(Calendar.DAY_OF_YEAR)<dob.get(Calendar.DAY_OF_YEAR) ? -1 : 0);

        return age;
    }

    public static int getAgeInDays(Date dobDate, Date endDate){
        return (int)TimeUnit.DAYS.convert(endDate.getTime() - dobDate.getTime(), TimeUnit.MILLISECONDS);
    }

    public static int getYearsDiff(Date firstDate, Date secondDate){
        Calendar second = Calendar.getInstance();
        Calendar first = Calendar.getInstance();
        first.setTime(firstDate);
        second.setTime(secondDate);

        int diff = second.get(Calendar.YEAR)-first.get(Calendar.YEAR) + (second.get(Calendar.DAY_OF_YEAR)<first.get(Calendar.DAY_OF_YEAR) ? -1 : 0);

        return diff;
    }

    public static int getDaysDiff(Date startDate, Date endDate){
        return getAgeInDays(startDate, endDate);
    }

    public static Calendar getCalendar(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static Date getDate(int y, int m, int d){
        Calendar cal = Calendar.getInstance();
        cal.set(y, m, d);
        return cal.getTime();
    }

    public static Date getDate(int y, int m, int d, int hour, int min, int sec){
        Calendar cal = Calendar.getInstance();
        cal.set(y, m, d, hour, min, sec);
        return cal.getTime();
    }

    public static Date getDateAdd(Date date, int plusDays){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, plusDays);
        return cal.getTime();
    }

    public static Date getDateStart(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        return cal.getTime();
    }

    public static Date getDateEnd(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);

        return cal.getTime();
    }

    public static Date getDateStart(Date date, int plusDays){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        cal.add(Calendar.DAY_OF_YEAR, plusDays);

        return cal.getTime();
    }

    public static Date getDateEnd(Date date, int plusDays){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);

        cal.add(Calendar.DAY_OF_YEAR, plusDays);

        return cal.getTime();
    }

    public static boolean dateEquals(Date date1, Date date2) {

        if (date1 == null || date2 == null) return false;

        Calendar c1 = getCalendar(date1);
        Calendar c2 = getCalendar(date2);

        return c1.get(Calendar.YEAR)==c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH)==c2.get(Calendar.MONTH) && c1.get(Calendar.DAY_OF_MONTH)==c2.get(Calendar.DAY_OF_MONTH);
    }
}
