package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.model.HierarchyItem;
import org.philimone.hds.explorer.model.Region;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RegionExpandableListAdapter extends BaseExpandableListAdapter implements Serializable {

    private Context mContext;
    private ArrayList<HierarchyItem> groupItems;
    private HashMap<HierarchyItem, ArrayList<Region>> originalRegionCollection; /* will hold all dataset */
    private HashMap<HierarchyItem, ArrayList<Region>> regionCollection;
    private Listener listener;


    public RegionExpandableListAdapter(Context mContext, ArrayList<HierarchyItem> groupItems, HashMap<HierarchyItem, ArrayList<Region>> regionCollection) {
        this.mContext = mContext;
        this.groupItems = new ArrayList<>();
        this.regionCollection = new HashMap<>();
        this.originalRegionCollection = new HashMap<>();

        this.groupItems.addAll(groupItems);
        this.regionCollection.putAll(regionCollection);


        for (HierarchyItem item : groupItems){
            ArrayList<Region> list = new ArrayList<>();

            for (Region region : regionCollection.get(item)){
                list.add(region);
            }

            this.originalRegionCollection.put(item, list);
        }
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    @Override
    public int getGroupCount() {
        return groupItems.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        HierarchyItem group = groupItems.get(groupPosition);
        return regionCollection.get(group).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.groupItems.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        HierarchyItem group = groupItems.get(groupPosition);
        return regionCollection.get(group).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition*1000+childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView==null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.region_group_item, parent, false);
        }

        TextView txtGroupRegion = (TextView) convertView.findViewById(R.id.txtGroupRegion);
        TextView txtGroupRegionExtra = (TextView) convertView.findViewById(R.id.txtGroupRegionExtra);

        HierarchyItem item = groupItems.get(groupPosition);

        txtGroupRegion.setText(item.getName());
        txtGroupRegionExtra.setText(item.getLevel());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView==null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.region_child_item, parent, false);
        }

        TextView txtChildRegionName = (TextView) convertView.findViewById(R.id.txtChildRegionName);
        TextView txtChildRegionCode = (TextView) convertView.findViewById(R.id.txtChildRegionCode);
        RadioButton chkRegionSelected = (RadioButton) convertView.findViewById(R.id.chkRegionSelected);
        ImageView iconView = convertView.findViewById(R.id.iconView);
        ImageView iconNewView = convertView.findViewById(R.id.iconNewView);

        HierarchyItem itemGroup = groupItems.get(groupPosition);
        Region region = this.regionCollection.get(itemGroup).get(childPosition);

        iconView.setVisibility(region.isRecentlyCreated() ? View.GONE : View.VISIBLE);
        iconNewView.setVisibility(region.isRecentlyCreated() ? View.VISIBLE : View.GONE);

        txtChildRegionName.setText(region.getName());
        txtChildRegionCode.setText(region.getCode() );
        chkRegionSelected.setChecked(region.isSelected());


        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void selectChild(int groupPosition, int childPosition){
        HierarchyItem item = this.groupItems.get(groupPosition);
        Region region = this.regionCollection.get(item).get(childPosition);

        for (Region r : this.regionCollection.get(item)){
            r.setSelected(false);
        }

        region.setSelected(true);

        //get all parents of this region and set them selected=true (retro cascade select)
        String parentCode = region.parent;
        for (int i = groupPosition-1; i >= 0; i--) {
            HierarchyItem parentItem = this.groupItems.get(i);

            //find parent with code = currentParent
            List<Region> regionList = this.regionCollection.get(parentItem);
            if (regionList != null) {
                final String pcode = parentCode;
                Region parentRegion = regionList.stream().filter(r -> r.code.equals(pcode)).findFirst().orElse(null);

                if (parentRegion != null) {
                    parentRegion.setSelected(true);
                    parentCode = parentRegion.parent;
                }
            }
        }

        //Filter Items
        int nextGroup = filterNextGroupBy(region, groupPosition);

        //colapse the current group
        //expand the next group
        if (listener != null){
            listener.onChildSelected(region, groupPosition, childPosition, nextGroup);
        }

        if (nextGroup != -1){
            ArrayList<Region> nextGroupList = this.regionCollection.get( this.groupItems.get(nextGroup) );

            if (nextGroupList.size()==1){
                //selectChild(nextGroup, 0); // select automatically the next group child
            }
        }

        notifyDataSetChanged();
    }

    private int filterNextGroupBy(Region parentRegion, int groupPosition) {
        groupPosition++;

        if (groupPosition<0 || groupPosition >= this.groupItems.size()){
            return -1;
        }

        HierarchyItem nextItem = this.groupItems.get(groupPosition);

        //get data from the original source
        ArrayList<Region> originalList = this.originalRegionCollection.get(nextItem);
        ArrayList<Region> currentList = this.regionCollection.get(nextItem);
        currentList.clear();

        for (Region r : originalList){ //fill new list with filtered content
            if (r.getParent().equals(parentRegion.getCode())){ //check if the parent is the clicked region
                currentList.add(r);
                r.setSelected(false);
            }
        }

        //clear the select of the groups after this one other items and the items
        int g = groupPosition+1;
        while (g>=0 && g < this.groupItems.size()){
            HierarchyItem it = this.groupItems.get(g);
            ArrayList<Region> list = this.regionCollection.get(it);

            for (Region r : list){
                r.setSelected(false);
            }

            list.clear();

            g++;
        }

        return groupPosition;
    }

    public Region selectRegion(Region region) {
        String level = region.getLevel();
        HierarchyItem item = groupItems.stream().filter(r -> r.getLevel().equals(level)).findFirst().orElse(null);
        List<Region> regionList = regionCollection.get(item);

        Region resultRegion = regionList.stream().filter(r -> r.code.equals(region.code)).findFirst().orElse(null);

        if (resultRegion != null) {
            int group = groupItems.indexOf(item);
            int child = regionList.indexOf(resultRegion);

            selectChild(group, child);

            return resultRegion;
        }

        return null;
    }

    public void addRegion(Region region) {
        String level = region.getLevel();
        HierarchyItem item = groupItems.stream().filter(r -> r.getLevel().equals(level)).findFirst().orElse(null);
        List<Region> currentList = regionCollection.get(item);
        List<Region> originalList = originalRegionCollection.get(item);

        currentList.add(region);
        originalList.add(region);

        notifyDataSetChanged();
    }

    public interface Listener {
        public void onChildSelected(Region selectedRegion, int groupPosition, int childPosition, int nextGroupPosition);
    }
}
