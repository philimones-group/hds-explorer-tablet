package org.philimone.hds.explorer.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.utilities.StringTools;

import java.util.Calendar;
import java.util.Date;

import mz.betainteractive.utilities.GeneralUtil;

public class DateTimeSelector extends AppCompatDialog {

    private Context mContext;
    private TextView txtDialogTitle;
    private TextView txtDialogMessage;
    private Button btDialogOk;
    private Button btDialogCancel;
    private DatePicker dtpColumnDateValue;
    private TimePicker dtpColumnTimeValue;

    private String dialogTitle;
    private String dialogMessage;
    private boolean dateWithTime;

    private Date defaultDate;

    public enum Buttons { OK, CANCEL };

    private OnSelectedListener listener;

    public DateTimeSelector(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public static DateTimeSelector createDateWidget(Context context, OnSelectedListener listener){
        DateTimeSelector dialog = new DateTimeSelector(context);
        dialog.listener = listener;
        dialog.dateWithTime = false;

        return dialog;
    }

    public static DateTimeSelector createDateTimeWidget(Context context, OnSelectedListener listener){
        DateTimeSelector dialog = new DateTimeSelector(context);
        dialog.listener = listener;
        dialog.dateWithTime = true;

        return dialog;
    }

    public static DateTimeSelector createDateWidget(Context context, Date defaultDate, OnSelectedListener listener){
        DateTimeSelector dialog = new DateTimeSelector(context);
        dialog.listener = listener;
        dialog.dateWithTime = false;
        dialog.defaultDate = defaultDate;

        return dialog;
    }

    public static DateTimeSelector createDateTimeWidget(Context context, Date defaultDate, OnSelectedListener listener){
        DateTimeSelector dialog = new DateTimeSelector(context);
        dialog.listener = listener;
        dialog.dateWithTime = true;
        dialog.defaultDate = defaultDate;

        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.datetime_selector);

        initialize();
    }

    private void initialize(){
        this.txtDialogTitle = (TextView) findViewById(R.id.txtDialogTitle);
        this.txtDialogMessage = (TextView) findViewById(R.id.txtDialogMessage);
        this.btDialogOk = (Button) findViewById(R.id.btDialogOk);
        this.btDialogCancel = (Button) findViewById(R.id.btDialogCancel);
        this.dtpColumnDateValue = findViewById(R.id.dtpColumnDateValue);
        this.dtpColumnTimeValue = findViewById(R.id.dtpColumnTimeValue);

        if (this.btDialogOk != null)
            this.btDialogOk.setOnClickListener(v -> onOkClicked());

        if (this.btDialogCancel != null)
            this.btDialogCancel.setOnClickListener(v -> onCancelCicked());

        doLayout();
    }

    public void doLayout() {

        setCancelable(false);

        setTexts();
        setDefaultDate(defaultDate);

        this.btDialogOk.setVisibility(View.VISIBLE);
        this.btDialogCancel.setVisibility(View.VISIBLE);

        this.dtpColumnTimeValue.setVisibility(dateWithTime ? View.VISIBLE : View.GONE);
    }

    private void onCancelCicked(){
        dismiss();
    }

    private void onDateSelected(){

        int y = this.dtpColumnDateValue.getYear();
        int m = this.dtpColumnDateValue.getMonth()+1;
        int d = this.dtpColumnDateValue.getDayOfMonth();
        int hh = dateWithTime ? this.dtpColumnTimeValue.getCurrentHour() : 0;
        int mm = dateWithTime ? this.dtpColumnTimeValue.getCurrentMinute() : 0;

        String strdate = y + "-" + String.format("%02d", m) + "-" + String.format("%02d", d);
        String format = "yyyy-MM-dd";

        if (dateWithTime) {
            strdate += " " + String.format("%02d", hh) + ":" + String.format("%02d", mm) + ":00";
            format += " HH:mm:ss";
        }

        Date date = StringTools.toDate(strdate, format);

        if (listener != null) {
            listener.onDateSelected(date, strdate);
        }
    }

    private void onOkClicked() {
        dismiss();
        onDateSelected();
    }

    private void setDefaultDate(Date date) {
        Calendar cal = GeneralUtil.getCalendar(date);
        dtpColumnDateValue.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        if (dateWithTime) {
            dtpColumnTimeValue.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
            dtpColumnTimeValue.setCurrentMinute(cal.get(Calendar.MINUTE));
        }
    }

    public void setTexts(){

        //if (this.txtDialogTitle != null){
        //    this.txtDialogTitle.setText(this.dialogTitle);
        //    this.txtDialogMessage.setText(this.dialogMessage);
        //}
    }

    public void setDialogTitle(String title){
        this.dialogTitle = title;
        setTexts();
    }

    public void setDialogMessage(String message){
        this.dialogMessage = message;
        setTexts();
    }

    public interface OnSelectedListener {
        void onDateSelected(Date selectedDate, String selectedDateText);
    }

}
