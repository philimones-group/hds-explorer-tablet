package mz.betainteractive.utilities;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * Created by paul on 9/15/16.
 */
public class ReflectionUtils {

    /*
     * The default Boolean values on the app are "zero" and "one"
     */
    public static String getValueByName(Object object, String variableName){

        try{

            Field field = object.getClass().getDeclaredField(variableName);
            field.setAccessible(true);
            Object obj = field.get(object);

            if (obj instanceof Boolean){
                return ((boolean)obj) ? "1" : "0";
            }

            if (obj instanceof Date) {
                Date dateValue = (Date) obj;

                return StringUtil.formatPrecise(dateValue);
            }

            return obj.toString();

        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public static boolean isFieldType(Object object, String variableName, Class<?> fieldType) {
        try{

            Field field = object.getClass().getDeclaredField(variableName);
            field.setAccessible(true);
            return field.getType().equals(fieldType);

        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean isDateFieldType(Object object, String variableName) {
        Class<?> fieldType = Date.class;

        return isFieldType(object, variableName, fieldType);
    }

}
