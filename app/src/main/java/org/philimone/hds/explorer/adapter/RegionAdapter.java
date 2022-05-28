package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.model.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 6/6/16.
 */
public class RegionAdapter extends RecyclerView.Adapter<RegionAdapter.RegionViewHolder> {
    private List<Region> regions;
    private List<Boolean> checkableRegions;
    private List<Boolean> supervisedRegions;
    private List<String> extras;
    private Context mContext;
    private @LayoutRes
    int layoutResId;
    private int selectedIndex = -1;

    /**
     * Adapter of a List View Item for households (name and code are displayed)
     * @param context
     * @param objects
     */
    public RegionAdapter(Context context, List<Region> objects){
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
    public RegionAdapter(Context context, List<Region> objects, List<Boolean> checks){
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
    public RegionAdapter(Context context, List<Region> objects, List<Boolean> checks, List<Boolean> supervisionList){
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
    public RegionAdapter(Context context, List<Region> objects, ArrayList<String> extras){
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

    public Region getItem(int position) {
        return regions.get(position);
    }

    @NonNull
    @Override
    public RegionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(layoutResId, parent, false);
        return new RegionViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(@NonNull RegionViewHolder holder, int position) {
        Region region = getItem(position);
        holder.setValues(region);
    }

    @Override
    public int getItemCount() {
        return this.regions.size();
    }

    class RegionViewHolder extends RecyclerView.ViewHolder {
        private ViewGroup rowView;

        public RegionViewHolder(@NonNull View itemView) {
            super(itemView);

            this.rowView = (ViewGroup) itemView;
        }

        public void setValues(Region region) {
            TextView txtChildRegionName = rowView.findViewById(R.id.txtChildRegionName);
            TextView txtChildRegionCode = rowView.findViewById(R.id.txtChildRegionCode);
            RadioButton chkRegionSelected = rowView.findViewById(R.id.chkRegionSelected);

            txtChildRegionName.setText(region.getName());
            txtChildRegionCode.setText(region.getCode() );
            chkRegionSelected.setChecked(region.isSelected());
        }
    }
}
