package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.model.Household;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 6/6/16.
 */
public class HouseholdArrayAdapter extends ArrayAdapter<Household> {
    private List<Household> households;
    private List<Boolean> checkableHouseholds;
    private List<Boolean> supervisedHouseholds;
    private List<String> extras;
    private Context mContext;
    private int layoutResId;
    private int selectedIndex = -1;

    /**
     * Adapter of a List View Item for households (name and code are displayed)
     * @param context
     * @param objects
     */
    public HouseholdArrayAdapter(Context context, List<Household> objects){
        super(context, R.layout.household_item, objects);

        this.households = new ArrayList<>();
        this.households.addAll(objects);
        this.mContext = context;
        this.layoutResId = R.layout.household_item;
    }

    /**
     * Adapter of a List View Item for households (name, code, and a check box are displayed)
     * @param context
     * @param objects
     * @param checks
     */
    public HouseholdArrayAdapter(Context context, List<Household> objects, List<Boolean> checks){
        super(context, R.layout.household_item_chk, objects);

        this.households = new ArrayList<>();
        this.households.addAll(objects);

        if (checks != null){
            this.checkableHouseholds = new ArrayList<>();
            this.checkableHouseholds.addAll(checks);
        }

        this.mContext = context;
        this.layoutResId = R.layout.household_item_chk;
    }

    /**
     * Adapter of a List View Item for households (name, code, checkbox and different icon for supervised household are displayed)
     * @param context
     * @param objects
     * @param checks
     * @param supervisionList
     */
    public HouseholdArrayAdapter(Context context, List<Household> objects, List<Boolean> checks, List<Boolean> supervisionList){
        this(context, objects, checks);

        if (supervisionList != null){
            this.supervisedHouseholds = new ArrayList<>();
            this.supervisedHouseholds.addAll(supervisionList);
        }
    }

    /**
     * Adapter of a List View Item for households (name, code, extra text view and a large sized icon are displayed)
     * @param context
     * @param objects
     * @param extras
     */
    public HouseholdArrayAdapter(Context context, List<Household> objects, ArrayList<String> extras){
        super(context, R.layout.household_item_xtra, objects);

        this.households = new ArrayList<>();
        this.households.addAll(objects);

        if (extras != null){
            this.extras = new ArrayList<>();
            this.extras.addAll(extras);
        }

        this.mContext = context;
        this.layoutResId = R.layout.household_item_xtra;
    }

    public void setSelectedIndex(int index){
        this.selectedIndex = index;
        notifyDataSetChanged();
    }

    public List<Household> getHouseholds(){
        return this.households;
    }

    @Override
    public Household getItem(int position) {
        return households.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(layoutResId, parent, false);

        ImageView iconView = (ImageView) rowView.findViewById(R.id.iconView);
        TextView txtName = (TextView) rowView.findViewById(R.id.txtHousename);
        TextView txtHeadLabel = (TextView) rowView.findViewById(R.id.txtHeadLabel);
        TextView txtCode = (TextView) rowView.findViewById(R.id.txtHouseCode);
        TextView txtHead = (TextView) rowView.findViewById(R.id.txtHead);
        TextView txtExtra = (TextView) rowView.findViewById(R.id.txtExtras);
        CheckBox chkVBprocessed = (CheckBox) rowView.findViewById(R.id.chkProcessed);

        Household hh = households.get(position);

        txtName.setText(hh.getName());
        txtCode.setText(hh.getCode());

        if (txtHead != null){
            txtHead.setText(hh.getHeadName());
        }

        if (chkVBprocessed != null && checkableHouseholds != null){
            chkVBprocessed.setChecked(checkableHouseholds.get(position));
        }

        if (supervisedHouseholds != null && position < supervisedHouseholds.size()){
            if (supervisedHouseholds.get(position)==true){
                txtName.setTypeface(null, Typeface.BOLD);
                iconView.setImageResource(R.mipmap.household_chk);
            }
        }

        if (hh.isRecentlyCreated()){
            iconView.setImageResource(R.mipmap.household_new);
        }

        if (this.extras != null && position < this.extras.size()){
            txtExtra.setText(extras.get(position));
        }

        if (selectedIndex == position){
            int colorA = mContext.getColor(R.color.nui_lists_selected_item_textcolor);
            int colorB = mContext.getColor(R.color.nui_lists_selected_item_color_2);

            rowView.setBackgroundColor(colorB);
            txtName.setTextColor(colorA);
            txtCode.setTextColor(colorA);
            if (txtHeadLabel!=null) txtHeadLabel.setTextColor(colorA);
            if (txtHead!=null) txtHead.setTextColor(colorA);
            if (txtExtra!=null) txtExtra.setTextColor(colorA);
        }

        return rowView;
    }
}
