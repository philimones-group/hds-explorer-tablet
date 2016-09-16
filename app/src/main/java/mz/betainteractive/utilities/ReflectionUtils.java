package mz.betainteractive.utilities;

import java.lang.reflect.Field;

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

            return obj.toString();

        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

}
