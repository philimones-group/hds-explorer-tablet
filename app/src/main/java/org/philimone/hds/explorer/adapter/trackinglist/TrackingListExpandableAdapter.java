package org.philimone.hds.explorer.adapter.trackinglist;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.model.TrackingSubListItem;
import org.philimone.hds.explorer.adapter.model.TrackingSubjectItem;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.widget.CirclePercentageBar;
import org.philimone.hds.explorer.widget.RecyclerListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mz.betainteractive.utilities.StringUtil;

public class TrackingListExpandableAdapter extends RecyclerView.Adapter<TrackingListExpandableViewHolder> implements RecyclerListView.OnItemClickListener {

    final static int VIEW_TYPE_GROUP = 1;
    final static int VIEW_TYPE_CHILD_REGION = 2;
    final static int VIEW_TYPE_CHILD_HOUSEHOLD = 3;
    final static int VIEW_TYPE_CHILD_MEMBER = 4;

    private RecyclerListView recyclerListView;

    private LinkedHashMap<TrackingSubListItem, List<TrackingSubjectItem>> groups;
    private String filterText;

    public TrackingListExpandableAdapter(RecyclerListView recyclerView, HashMap<TrackingSubListItem, List<TrackingSubjectItem>> collection) {
        this.recyclerListView = recyclerView;
        this.recyclerListView.addOnItemClickListener(this);

        this.groups = new LinkedHashMap<>();
        this.groups.putAll(collection);
    }

    @NonNull
    @Override
    public TrackingListExpandableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_GROUP) {
            view = inflater.inflate(R.layout.tracking_sublist_item, parent, false);
        } else if (viewType == VIEW_TYPE_CHILD_REGION){
            view = inflater.inflate(R.layout.tracking_region_item, parent, false);
        } else if (viewType == VIEW_TYPE_CHILD_HOUSEHOLD){
            view = inflater.inflate(R.layout.tracking_household_item, parent, false);
        } else if (viewType == VIEW_TYPE_CHILD_MEMBER){
            view = inflater.inflate(R.layout.tracking_member_item, parent, false);
        }

        Log.d("create-viewholder", ""+view+", viewtype="+viewType);
        return new TrackingListExpandableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackingListExpandableViewHolder holder, int position) {
        TrackingGroupItem item = getItem(position);
        holder.setValues(item);
    }

    @Override
    public int getItemCount() {
        int size = groups.size();

        for (Map.Entry<TrackingSubListItem, List<TrackingSubjectItem>> entry : groups.entrySet()){
            if (entry.getKey().isExpanded()){
                size += filterList(entry.getValue()).size();
            }
        }

        return size;
    }

    @Override
    public int getItemViewType(int position) {

        int pos_count = 0;
        for (Map.Entry<TrackingSubListItem, List<TrackingSubjectItem>> entry : groups.entrySet()){

            if (pos_count == position) { //is always group
                return VIEW_TYPE_GROUP;
            }

            if (entry.getKey().isExpanded()){
                //iterate childs

                List<TrackingSubjectItem> list = filterList(entry.getValue()); //list, if (pos-pos_c-1 >=0 && pos-pos_c-1 < list.size) then list.get(pos-pos_c-1)
                int inner_pos = position - pos_count - 1;
                if (inner_pos >= 0 && inner_pos < list.size()) {
                    //is a child group type - the element is a subjectitem
                    TrackingSubjectItem subjectItem = list.get(inner_pos);

                    if (subjectItem.isRegionSubject()) return VIEW_TYPE_CHILD_REGION;
                    if (subjectItem.isHouseholdSubject()) return VIEW_TYPE_CHILD_HOUSEHOLD;
                    if (subjectItem.isMemberSubject()) return VIEW_TYPE_CHILD_MEMBER;

                } else {
                    pos_count += list.size();
                }
            }

            pos_count++;
        }

        return -1;
    }

    public TrackingGroupItem getItem(int position) {

        int pos_count = 0;
        for (Map.Entry<TrackingSubListItem, List<TrackingSubjectItem>> entry : groups.entrySet()){

            if (pos_count == position) { //is always group
                return new TrackingGroupItem(VIEW_TYPE_GROUP, position, entry.getKey(), null, entry.getValue()) ;
            }

            if (entry.getKey().isExpanded()){
                //iterate childs

                List<TrackingSubjectItem> list = filterList(entry.getValue()); //list, if (pos-pos_c-1 >=0 && pos-pos_c-1 < list.size) then list.get(pos-pos_c-1)
                int inner_pos = position - pos_count - 1;
                if (inner_pos >= 0 && inner_pos < list.size()) {
                    //is a child group type - the element is a subjectitem
                    TrackingSubjectItem subjectItem = list.get(inner_pos);
                    int viewType = -1;
                    if (subjectItem.isRegionSubject()) viewType = VIEW_TYPE_CHILD_REGION;
                    if (subjectItem.isHouseholdSubject()) viewType = VIEW_TYPE_CHILD_HOUSEHOLD;
                    if (subjectItem.isMemberSubject()) viewType = VIEW_TYPE_CHILD_MEMBER;

                    return new TrackingGroupItem(viewType, position, entry.getKey(), subjectItem, entry.getValue());

                } else {
                    pos_count += list.size();
                }
            }
            pos_count++;
        }

        return null;
    }

    public int getCompletionOfTrackingList(){
        int n = groups.size();
        int c = 0;

        for (Map.Entry<TrackingSubListItem, List<TrackingSubjectItem>> entry : groups.entrySet()){
            c += getCompletionOfList(entry.getValue());
        }

        if (n==0) return 0;

        return (c / n);
    }

    public int getCompletionOfList(List<TrackingSubjectItem> childList){
        double to_collect = 0;
        double collected = 0;

        for (TrackingSubjectItem memberItem : childList){
            to_collect += memberItem.getForms().size()*1D;
            collected += memberItem.getCollectedForms().size()*1D;
        }

        if (to_collect==0) return 100;

        return (int)((collected / to_collect) *100);
    }

    @Override
    public void onItemClick(View view, int position, long id) {
        TrackingGroupItem listItem = getItem(position);
        if (listItem != null) {
            listItem.getGroupItem().toggleCollapseExpand();
            notifyDataSetChanged();
        }
    }

    @Override
    public void onItemLongClick(View view, int position, long id) {

    }

    public void expandAll() {
        for (TrackingSubListItem listItem : groups.keySet()){
            listItem.expand();
        }
        notifyDataSetChanged();
    }

    public void filterSubjects(String code){
        Log.d("filtering", ""+code);

        if (StringUtil.isBlank(code)){
            this.filterText = null;
        } else {
            this.filterText = code;
        }

        notifyDataSetChanged();
    }

    private boolean codeMatches(TrackingSubjectItem subjectItem, String strCode){

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

    private List<TrackingSubjectItem> filterList(List<TrackingSubjectItem> subjectItemList){
        if (filterText==null) return subjectItemList;

        List<TrackingSubjectItem> filtered = new ArrayList<>();

        for (TrackingSubjectItem item : subjectItemList) {
            if (codeMatches(item, filterText)){
                filtered.add(item);
            }
        }

        return filtered;
    }

}


