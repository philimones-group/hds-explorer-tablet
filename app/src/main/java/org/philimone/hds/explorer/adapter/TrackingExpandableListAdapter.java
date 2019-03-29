package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.model.TrackingSubjectItem;
import org.philimone.hds.explorer.adapter.model.TrackingSubListItem;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.widget.CirclePercentageBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by paul on 11/4/17.
 */
public class TrackingExpandableListAdapter extends BaseExpandableListAdapter implements Serializable {

    private Context mContext;
    private ArrayList<TrackingSubListItem> groupItems;
    private HashMap<TrackingSubListItem, ArrayList<TrackingSubjectItem>> trackingCollection;
    private HashMap<TrackingSubListItem, ArrayList<TrackingSubjectItem>> originalCollection;

    public TrackingExpandableListAdapter(Context context, ArrayList<TrackingSubListItem> listItems, HashMap<TrackingSubListItem, ArrayList<TrackingSubjectItem>> collection){
        this.mContext = context;
        this.groupItems = new ArrayList<>();
        this.trackingCollection = new LinkedHashMap<>();
        this.originalCollection = new LinkedHashMap<>();
        this.groupItems.addAll(listItems);
        this.trackingCollection.putAll(collection);
        //backup collection
        backupCollection();

    }

    private void backupCollection(){
        for (TrackingSubListItem item : this.trackingCollection.keySet()){
            ArrayList<TrackingSubjectItem> listNew = new ArrayList<>();
            ArrayList<TrackingSubjectItem> list = this.trackingCollection.get(item);
            listNew.addAll(list);

            this.originalCollection.put(item, listNew);
        }
    }

    public ArrayList<TrackingSubListItem> getGroupItems() {
        return groupItems;
    }

    public HashMap<TrackingSubListItem, ArrayList<TrackingSubjectItem>> getTrackingCollection() {
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
        txtExtras.setText(n+" Subjects");
        pBar.setPercentageValue(getCompletionOfList(listItem));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        TrackingSubListItem listItem = groupItems.get(groupPosition);
        TrackingSubjectItem subjectItem = this.trackingCollection.get(listItem).get(childPosition);
        Region region = subjectItem.getRegion();
        Household household = subjectItem.getHousehold();
        Member member = subjectItem.getMember();
        boolean isRegion = subjectItem.isRegionSubject();
        boolean isHousehold = subjectItem.isHouseholdSubject();
        boolean isMember = subjectItem.isMemberSubject();


        if (convertView==null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (isRegion){
                convertView = inflater.inflate(R.layout.tracking_region_item, parent, false);
            }
            if (isHousehold){
                convertView = inflater.inflate(R.layout.tracking_household_item, parent, false);
            }
            if (isMember){
                convertView = inflater.inflate(R.layout.tracking_member_item, parent, false);
            }
        }

        TextView txtName = (TextView) convertView.findViewById(R.id.txtMemberItemName);
        TextView txtId = (TextView) convertView.findViewById(R.id.txtMemberItemCode);
        TextView txtDetails = (TextView) convertView.findViewById(R.id.txtMemberItemExtras);
        CirclePercentageBar pBar = (CirclePercentageBar) convertView.findViewById(R.id.pbarTrackListItem);

        if (isRegion) {
            txtName.setText(region.getName());
            txtId.setText(region.getCode() +" -> "+region.getLevel());
            txtDetails.setText(subjectItem.getSubjectType());
        }
        if (isHousehold) {
            txtName.setText(household.getName());
            txtId.setText(household.getCode());
            txtDetails.setText(household.getHeadName());
        }
        if (isMember) {
            txtName.setText(member.getName());
            txtId.setText(member.getHouseholdName() +" -> "+member.getCode());
            txtDetails.setText(subjectItem.getSubjectType());
        }

        pBar.setMaxValue(subjectItem.getForms().size());
        pBar.setValue(subjectItem.getCollectedForms().size());

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public int getCompletionOfList(TrackingSubListItem listItem){
        double to_collect = 0;
        double collected = 0;

        for (TrackingSubjectItem memberItem : this.trackingCollection.get(listItem)){
            to_collect += memberItem.getForms().size()*1D;
            collected += memberItem.getCollectedForms().size()*1D;
        }

        if (to_collect==0) return 100;

        return (int)((collected / to_collect) *100);
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

    public void filterSubjects(String code){
        Log.d("filtering", ""+code);
        for (TrackingSubListItem subList : this.originalCollection.keySet()){
            ArrayList<TrackingSubjectItem> origSubjectItems = this.originalCollection.get(subList);
            ArrayList<TrackingSubjectItem> subjectItems = this.trackingCollection.get(subList);

            subjectItems.clear();
            subjectItems.addAll(origSubjectItems);

            //filter or remove from subjectItems
            filterSubjects(subjectItems, code);

        }
    }

    private void filterSubjects(ArrayList<TrackingSubjectItem> subjectItems, String code){
        ArrayList<TrackingSubjectItem> toRemove = new ArrayList<>();

        if (code.length()==0) return;

        for (TrackingSubjectItem subject : subjectItems) {
            if (!codeMatches(subject, code)){
                toRemove.add(subject);
            }
        }

        Log.d("to remove", ""+toRemove.size());

        subjectItems.removeAll(toRemove);
    }

    public boolean codeMatches(TrackingSubjectItem subjectItem, String strCode){

        String codeRegex = strCode.toLowerCase() + ".*";
        String nameRegex = ".*" + strCode.toLowerCase() + ".*";

        String name = "";
        String code = "";
        String details = "";

        if (subjectItem.isRegionSubject()) {
            name = subjectItem.getRegion().getName().toLowerCase();
            code = subjectItem.getRegion().getCode().toLowerCase() + "  " + subjectItem.getRegion().getLevel().toLowerCase();

            return code.matches(nameRegex) || name.matches(nameRegex);
        }
        if (subjectItem.isHouseholdSubject()) {
            name = subjectItem.getHousehold().getName().toLowerCase();
            code = subjectItem.getHousehold().getCode().toLowerCase();
            details = subjectItem.getHousehold().getHeadName().toLowerCase();

            return code.matches(codeRegex) || name.matches(nameRegex) || details.matches(nameRegex);
        }
        if (subjectItem.isMemberSubject()) {
            name = subjectItem.getMember().getName().toLowerCase();
            code = subjectItem.getMember().getHouseholdName().toLowerCase() +"  "+subjectItem.getMember().getCode().toLowerCase();

            return code.matches(nameRegex) || name.matches(nameRegex);
        }

        return false;
    }
}
