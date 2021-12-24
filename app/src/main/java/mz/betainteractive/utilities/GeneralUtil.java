package mz.betainteractive.utilities;

import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

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

    public  static int getAge(Date dobDate, Date dateInTime){
        Calendar now = Calendar.getInstance();
        Calendar dob = Calendar.getInstance();

        dob.setTime(dobDate);
        now.setTime(dateInTime);

        int age = now.get(Calendar.YEAR)-dob.get(Calendar.YEAR) + (now.get(Calendar.DAY_OF_YEAR)<dob.get(Calendar.DAY_OF_YEAR) ? -1 : 0);

        return age;
    }

    public  static Calendar getCalendar(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
         return cal;
    }
}
