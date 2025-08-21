package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.enums.HouseholdType;

import java.util.ArrayList;
import java.util.List;

public class HouseholdAdapter extends RecyclerView.Adapter<HouseholdAdapter.HouseholdViewHolder> {

    private List<Household> households;
    private List<Boolean> checkableHouseholds;
    private List<Boolean> supervisedHouseholds;
    private List<String> extras;
    private Context mContext;
    private @LayoutRes int layoutResId;
    private int selectedIndex = -1;

    /**
     * Adapter of a List View Item for households (name and code are displayed)
     * @param context
     * @param objects
     */
    public HouseholdAdapter(Context context, List<Household> objects){
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
    public HouseholdAdapter(Context context, List<Household> objects, List<Boolean> checks){
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
    public HouseholdAdapter(Context context, List<Household> objects, List<Boolean> checks, List<Boolean> supervisionList){
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
    public HouseholdAdapter(Context context, List<Household> objects, ArrayList<String> extras){
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

    public Household getItem(int position) {
        return households.get(position);
    }
    
    @NonNull
    @Override
    public HouseholdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layoutResId, parent, false);
        return new HouseholdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HouseholdViewHolder holder, int position) {
        Household household = this.getItem(position);
        holder.setValues(household);
    }

    @Override
    public int getItemCount() {
        return this.households.size();
    }

    class HouseholdViewHolder extends RecyclerView.ViewHolder {

        private ViewGroup rowView;

        public HouseholdViewHolder(@NonNull View itemView) {
            super(itemView);
            this.rowView = (ViewGroup) itemView;
        }

        public void setValues(Household hh) {
            ImageView iconView = (ImageView) rowView.findViewById(R.id.iconView);
            TextView txtName = (TextView) rowView.findViewById(R.id.txtHousename);
            TextView txtHeadLabel = (TextView) rowView.findViewById(R.id.txtHeadLabel);
            TextView txtCode = (TextView) rowView.findViewById(R.id.txtHouseCode);
            TextView txtHead = (TextView) rowView.findViewById(R.id.txtHead);
            TextView txtExtra = (TextView) rowView.findViewById(R.id.txtExtras);
            CheckBox chkVBprocessed = (CheckBox) rowView.findViewById(R.id.chkProcessed);

            int position = households.indexOf(hh);

            txtName.setText(hh.getName());
            txtCode.setText(hh.getCode());

            if (txtHead != null){
                txtHead.setText(hh.getHeadName());
            }

            if (chkVBprocessed != null && checkableHouseholds != null){
                chkVBprocessed.setChecked(checkableHouseholds.get(position));
            }

            //set default icon
            iconView.setImageResource(hh.type == HouseholdType.INSTITUTIONAL ? R.mipmap.nui_household_inst_red_icon : R.mipmap.nui_household_red_icon);

            if (supervisedHouseholds != null && position < supervisedHouseholds.size()){
                if (supervisedHouseholds.get(position)==true){
                    txtName.setTypeface(null, Typeface.BOLD);
                    iconView.setImageResource(hh.type == HouseholdType.INSTITUTIONAL ? R.mipmap.nui_household_inst_red_chk_icon : R.mipmap.nui_household_red_chk_icon);
                }
            }

            if (hh.isRecentlyCreated()){
                iconView.setImageResource(hh.type == HouseholdType.INSTITUTIONAL ? R.mipmap.nui_household_inst_red_new_icon : R.mipmap.nui_household_red_new_icon);
            }

            if (hh.preRegistered) {
                iconView.setImageResource(R.mipmap.nui_household_red_notreg_icon);
            }

            if (extras != null && position < extras.size()){
                txtExtra.setText(extras.get(position));
            }

            if (selectedIndex == position){
                int colorA = ContextCompat.getColor(mContext, R.color.nui_lists_selected_item_textcolor);
                int colorB = ContextCompat.getColor(mContext, R.color.nui_lists_selected_item_color_2);

                rowView.setBackgroundColor(colorB);
                txtName.setTextColor(colorA);
                txtCode.setTextColor(colorA);
                if (txtHeadLabel!=null) txtHeadLabel.setTextColor(colorA);
                if (txtHead!=null) txtHead.setTextColor(colorA);
                if (txtExtra!=null) txtExtra.setTextColor(colorA);
            } else {
                TypedValue colorBvalue = new TypedValue();
                mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, colorBvalue, true);

                int colorA = mContext.getResources().getColor(R.color.nui_member_item_textcolor);
                int colorB = colorBvalue.resourceId;

                rowView.setBackgroundResource(colorB);
                txtName.setTextColor(colorA);
                txtCode.setTextColor(colorA);
                if (txtHeadLabel!=null) txtHeadLabel.setTextColor(colorA);
                if (txtHead!=null) txtHead.setTextColor(colorA);
                if (txtExtra!=null) txtExtra.setTextColor(colorA);
            }
        }
    }
}
