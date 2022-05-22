package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.model.HierarchyItem;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 6/6/16.
 */
public class RegionArrayAdapter extends ArrayAdapter<Region> {
    private List<Region> regions;
    private List<Boolean> checkableRegions;
    private List<Boolean> supervisedRegions;
    private List<String> extras;
    private Context mContext;
    private int layoutResId;
    private int selectedIndex = -1;

    /**
     * Adapter of a List View Item for households (name and code are displayed)
     * @param context
     * @param objects
     */
    public RegionArrayAdapter(Context context, List<Region> objects){
        super(context, R.layout.region_details_child_item, objects);

        this.regions = new ArrayList<>();
        this.regions.addAll(objects);
        this.mContext = context;
        this.layoutResId = R.layout.region_details_child_item;
    }

    /**
     * Adapter of a List View Item for households (name, code, and a check box are displayed)
     * @param context
     * @param objects
     * @param checks
     */
    public RegionArrayAdapter(Context context, List<Region> objects, List<Boolean> checks){
        super(context, R.layout.region_details_child_item, objects);

        this.regions = new ArrayList<>();
        this.regions.addAll(objects);

        if (checks != null){
            this.checkableRegions = new ArrayList<>();
            this.checkableRegions.addAll(checks);
        }

        this.mContext = context;
        this.layoutResId = R.layout.region_details_child_item;
    }

    /**
     * Adapter of a List View Item for households (name, code, checkbox and different icon for supervised household are displayed)
     * @param context
     * @param objects
     * @param checks
     * @param supervisionList
     */
    public RegionArrayAdapter(Context context, List<Region> objects, List<Boolean> checks, List<Boolean> supervisionList){
        this(context, objects, checks);

        if (supervisionList != null){
            this.supervisedRegions = new ArrayList<>();
            this.supervisedRegions.addAll(supervisionList);
        }
    }

    /**
     * Adapter of a List View Item for households (name, code, extra text view and a large sized icon are displayed)
     * @param context
     * @param objects
     * @param extras
     */
    public RegionArrayAdapter(Context context, List<Region> objects, ArrayList<String> extras){
        super(context, R.layout.region_details_child_item, objects);

        this.regions = new ArrayList<>();
        this.regions.addAll(objects);

        if (extras != null){
            this.extras = new ArrayList<>();
            this.extras.addAll(extras);
        }

        this.mContext = context;
        this.layoutResId = R.layout.region_details_child_item;
    }

    public void setSelectedIndex(int index){
        this.selectedIndex = index;
        notifyDataSetChanged();
    }

    public List<Region> getRegions(){
        return this.regions;
    }

    @Override
    public Region getItem(int position) {
        return regions.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(layoutResId, parent, false);

        TextView txtChildRegionName = (TextView) rowView.findViewById(R.id.txtChildRegionName);
        TextView txtChildRegionCode = (TextView) rowView.findViewById(R.id.txtChildRegionCode);
        RadioButton chkRegionSelected = (RadioButton) rowView.findViewById(R.id.chkRegionSelected);

        Region region = this.regions.get(position);


        txtChildRegionName.setText(region.getName());
        txtChildRegionCode.setText(region.getCode() );
        chkRegionSelected.setChecked(region.isSelected());

        return rowView;
    }
}
