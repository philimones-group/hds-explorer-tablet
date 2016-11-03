package net.manhica.dss.explorer.adapter;

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

import net.manhica.clip.explorer.R;
import net.manhica.dss.explorer.model.Household;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 6/6/16.
 */
public class HouseholdArrayAdapter extends ArrayAdapter<Household> {
    private List<Household> households;
    private List<Boolean> checkableHouseholds;
    private List<Boolean> supervisedHouseholds;
    private Context mContext;
    private int layoutResId;
    private int selectedIndex = -1;

    public HouseholdArrayAdapter(Context context, List<Household> objects){
        super(context, R.layout.household_item, objects);

        this.households = new ArrayList<>();
        this.households.addAll(objects);
        this.mContext = context;
        this.layoutResId = R.layout.household_item;
    }

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

    public HouseholdArrayAdapter(Context context, List<Household> objects, List<Boolean> checks, List<Boolean> supervisionList){
        this(context, objects, checks);

        if (supervisionList != null){
            this.supervisedHouseholds = new ArrayList<>();
            this.supervisedHouseholds.addAll(supervisionList);
        }
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
        TextView txtName = (TextView) rowView.findViewById(R.id.txtHousenumber);
        TextView txtExtId = (TextView) rowView.findViewById(R.id.txtHouseExtID);
        CheckBox chkVBprocessed = (CheckBox) rowView.findViewById(R.id.chkProcessed);

        Household hh = households.get(position);

        txtName.setText(hh.getHouseNumber());
        txtExtId.setText(hh.getExtId());

        if (chkVBprocessed != null && checkableHouseholds != null){
            chkVBprocessed.setChecked(checkableHouseholds.get(position));
        }

        if (supervisedHouseholds != null && position < supervisedHouseholds.size()){
            if (supervisedHouseholds.get(position)==true){
                txtName.setTypeface(null, Typeface.BOLD);
                iconView.setImageResource(R.mipmap.household_chk);
            }
        }

        if (selectedIndex == position){
            int colorB = Color.parseColor("#0073C6");

            rowView.setBackgroundColor(colorB);
            txtName.setTextColor(Color.WHITE);
            txtExtId.setTextColor(Color.WHITE);
        }

        return rowView;
    }
}
