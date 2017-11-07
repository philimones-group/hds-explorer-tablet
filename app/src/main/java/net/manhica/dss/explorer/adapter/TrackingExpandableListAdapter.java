package net.manhica.dss.explorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import net.manhica.dss.explorer.R;
import net.manhica.dss.explorer.adapter.model.TrackingMemberItem;
import net.manhica.dss.explorer.adapter.model.TrackingSubListItem;
import net.manhica.dss.explorer.model.Member;
import net.manhica.dss.explorer.widget.CirclePercentageBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by paul on 11/4/17.
 */
public class TrackingExpandableListAdapter extends BaseExpandableListAdapter implements Serializable {

    private Context mContext;
    private ArrayList<TrackingSubListItem> groupItems;
    private HashMap<TrackingSubListItem, ArrayList<TrackingMemberItem>> trackingCollection;

    public TrackingExpandableListAdapter(Context context, ArrayList<TrackingSubListItem> listItems, HashMap<TrackingSubListItem, ArrayList<TrackingMemberItem>> collection){
        this.mContext = context;
        this.groupItems = new ArrayList<>();
        this.trackingCollection = new HashMap<>();
        this.groupItems.addAll(listItems);
        this.trackingCollection.putAll(collection);
    }

    public ArrayList<TrackingSubListItem> getGroupItems() {
        return groupItems;
    }

    public HashMap<TrackingSubListItem, ArrayList<TrackingMemberItem>> getTrackingCollection() {
        return trackingCollection;
    }

    @Override
    public int getGroupCount() {
        return this.groupItems.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        TrackingSubListItem listItem = groupItems.get(groupPosition);
        return this.trackingCollection.get(listItem).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.groupItems.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        TrackingSubListItem listItem = groupItems.get(groupPosition);
        return trackingCollection.get(listItem).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView==null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.tracking_sublist_item, parent, false);
        }

        TextView txtTitle = (TextView) convertView.findViewById(R.id.txtTrackSubListTitle);
        TextView txtExtras = (TextView) convertView.findViewById(R.id.txtTrackSubListExtras);
        CirclePercentageBar pBar = (CirclePercentageBar) convertView.findViewById(R.id.pbarTrackListItem);

        TrackingSubListItem listItem = groupItems.get(groupPosition);
        int n = trackingCollection.get(listItem).size();

        txtTitle.setText(listItem.getTitle());
        txtExtras.setText(n+" Members");
        pBar.setPercentageValue(getCompletionOfList(listItem));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView==null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.tracking_member_item, parent, false);
        }

        TextView txtName = (TextView) convertView.findViewById(R.id.txtMemberItemName);
        TextView txtId = (TextView) convertView.findViewById(R.id.txtMemberItemId);
        TextView txtDetails = (TextView) convertView.findViewById(R.id.txtMemberItemExtras);
        CirclePercentageBar pBar = (CirclePercentageBar) convertView.findViewById(R.id.pbarTrackListItem);

        TrackingSubListItem listItem = groupItems.get(groupPosition);
        TrackingMemberItem memberItem = this.trackingCollection.get(listItem).get(childPosition);
        Member member = memberItem.getMember();

        txtName.setText(member.getName());
        txtId.setText(member.getHouseNumber() +" -> "+member.getPermId());
        txtDetails.setText(memberItem.getStudyCode());

        pBar.setMaxValue(memberItem.getForms().size());
        pBar.setValue(memberItem.getCollectedForms().size());

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public int getCompletionOfList(TrackingSubListItem listItem){
        int to_collect = 0;
        int collected = 0;

        for (TrackingMemberItem memberItem : this.trackingCollection.get(listItem)){
            to_collect += memberItem.getForms().size();
            collected += memberItem.getCollectedForms().size();
        }

        if (to_collect==0) return 100;

        return (collected / to_collect)*100;
    }

    public int getCompletionOfTrackingList(){
        int n = groupItems.size();
        int c = 0;

        for (TrackingSubListItem listItem : groupItems){
            c += getCompletionOfList(listItem);
        }

        if (n==0) return 0;

        return (c / n);
    }
}
