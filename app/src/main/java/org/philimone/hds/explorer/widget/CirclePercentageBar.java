package org.philimone.hds.explorer.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
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
            if (displayTextType.equals("fraction")){
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

        updateTextView();
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
        updateTextView();
    }

    private void calcPercentageByValues() {
        this.percentageValue = value/maxValue;
        this.progressBar.setProgress(this.percentageValue);
        updateTextView();
    }

    private void updateTextView(){
        if (displayType==DisplayType.PERCENTAGE){
            this.txtPercentageValue.setText(percentageValue+"%");
        }else{
            this.txtPercentageValue.setText(value+"/"+maxValue);
        }
    }



}
