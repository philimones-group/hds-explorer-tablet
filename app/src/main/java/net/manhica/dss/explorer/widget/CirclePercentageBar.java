package net.manhica.dss.explorer.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.manhica.dss.explorer.R;

/**
 * TODO: document your custom view class.
 */
public class CirclePercentageBar extends LinearLayout {

    private TextView txtPercentageValue;
    private ProgressBar progressBar;

    public CirclePercentageBar(Context context) {
        super(context);
        initLayout();
    }

    public CirclePercentageBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    public CirclePercentageBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initLayout();
    }

    private void initLayout() {
        View view = inflate(getContext(), R.layout.circle_percentage_bar, null);
        addView(view);

        this.txtPercentageValue = (TextView) view.findViewById(R.id.txtCircleBarValue);
        this.progressBar = (ProgressBar) view.findViewById(R.id.pBarCircleProgressBar);
    }

    public void setPercentageValue(int value){
        this.progressBar.setProgress(value);
        this.txtPercentageValue.setText(value+"%");
    }

}
