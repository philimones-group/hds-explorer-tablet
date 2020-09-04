package org.philimone.hds.explorer.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.philimone.hds.explorer.R;

/**
 * TODO: document your custom view class.
 */
public class CirclePercentageBar extends LinearLayout {

    private int value;
    private int percentageValue;
    private int maxValue;
    private TextView txtPercentageValue;
    private ProgressBar progressBar;
    private DisplayType displayType = DisplayType.PERCENTAGE;
    private int displayTextColor = 0;
    private int displayTextSize = 0;
    private int displayCircleColor = 0;
    private int displayPercentageValue = 0;

    public enum DisplayType {
        FRACTION, PERCENTAGE
    }

    public CirclePercentageBar(Context context) {
        super(context);
        initLayout(null);
    }

    public CirclePercentageBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout(attrs);
    }

    public CirclePercentageBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initLayout(attrs);
    }

    private void loadAttributes(AttributeSet attrs){
        String displayTextType = "";

        TypedArray ta = this.getContext().obtainStyledAttributes(attrs, R.styleable.CirclePercentageBar, 0, 0);

        try {
            displayTextType = ta.getString(R.styleable.CirclePercentageBar_displayTextType);
            displayTextSize = ta.getInt(R.styleable.CirclePercentageBar_displayTextSize, 10);
            displayTextColor = ta.getColor(R.styleable.CirclePercentageBar_displayTextColor, getResources().getColor(R.color.nui_color_circle_color_one, null));
            displayCircleColor = ta.getColor(R.styleable.CirclePercentageBar_displayCircleColor, getResources().getColor(R.color.nui_color_circle_color_two, null));
            displayPercentageValue = ta.getInt(R.styleable.CirclePercentageBar_displayPercentageValue, 0);

            if (displayTextType!=null && displayTextType.equals("fraction")){
                displayType = DisplayType.FRACTION;
            }else{
                displayType = DisplayType.PERCENTAGE;
            }

        } finally {
            ta.recycle();
        }
    }

    private void initLayout(AttributeSet attrs) {
        View view = inflate(getContext(), R.layout.circle_percentage_bar, null);
        addView(view);

        if (attrs != null)
            loadAttributes(attrs);

        this.txtPercentageValue = (TextView) view.findViewById(R.id.txtCircleBarValue);
        this.progressBar = (ProgressBar) view.findViewById(R.id.pBarCircleProgressBar);

        setAttributes();
        updateViews();

    }

    private void setAttributes() {
        if (displayTextSize != 0){
            this.txtPercentageValue.setTextSize(TypedValue.COMPLEX_UNIT_SP,  displayTextSize*1.0F);
        }

        if (displayTextColor != 0){
            this.txtPercentageValue.setTextColor(displayTextColor);
        }

        if (displayCircleColor != 0){
            this.progressBar.getProgressDrawable().setColorFilter(displayCircleColor, PorterDuff.Mode.SRC_IN);
        }

        this.progressBar.setProgress(displayPercentageValue);

        if (displayType==DisplayType.PERCENTAGE){
            this.txtPercentageValue.setText(displayPercentageValue+"%");
        }else{
            this.txtPercentageValue.setText(displayPercentageValue+"/"+maxValue);
        }

        Log.d("text-size", ""+displayTextSize);
        Log.d("text-color", ""+displayTextColor);
        Log.d("circ-color", ""+displayCircleColor);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        calcPercentageByValues();
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        calcPercentageByValues();
    }

    public void setPercentageValue(int value){
        this.percentageValue = value;
        this.progressBar.setProgress(value);
        updateViews();
    }

    private void calcPercentageByValues() {
        this.percentageValue = value/maxValue;
        this.progressBar.setProgress(this.percentageValue);
        updateViews();
    }

    private void updateViews(){
        updatePercentageValue();
    }

    private void updatePercentageValue(){
        if (displayType==DisplayType.PERCENTAGE){
            this.txtPercentageValue.setText(percentageValue+"%");
        }else{
            this.txtPercentageValue.setText(value+"/"+maxValue);
        }
    }


}
