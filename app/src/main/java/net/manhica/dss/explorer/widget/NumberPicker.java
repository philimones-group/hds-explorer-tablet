package net.manhica.dss.explorer.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by paul on 11/25/16.
 */
public class NumberPicker extends android.widget.NumberPicker{
    public NumberPicker(Context context) {
        super(context);
    }

    public NumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        addExtrasAttributeSet(attrs);
    }

    public NumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addExtrasAttributeSet(attrs);
    }

    private void addExtrasAttributeSet(AttributeSet attrs) {
        //This method reads the parameters given in the xml file and sets the properties according to it
        this.setMinValue(attrs.getAttributeIntValue(null, "min_value", 0));
        this.setMaxValue(attrs.getAttributeIntValue(null, "max_value", 0));
        this.setValue(attrs.getAttributeIntValue(null, "default_value", 0));
    }
}
