package org.philimone.hds.explorer.adapter.trackinglist;

import org.philimone.hds.explorer.adapter.model.TrackingSubListItem;
import org.philimone.hds.explorer.adapter.model.TrackingSubjectItem;

import java.util.List;

public class TrackingGroupItem {
    private int viewType;
    private int position;
    private TrackingSubListItem groupItem;
    private TrackingSubjectItem childItem;
    private List<TrackingSubjectItem> childs;

    public TrackingGroupItem(int viewType, int position, TrackingSubListItem groupItem, TrackingSubjectItem childItem, List<TrackingSubjectItem> childsList) {
        this.viewType = viewType;
        this.position = position;
        this.groupItem = groupItem;
        this.childItem = childItem;
        this.childs = childsList;
    }

    public int getViewType() {
        return viewType;
    }

    public int getPosition() {
        return position;
    }

    public TrackingSubListItem getGroupItem() {
        return groupItem;
    }

    public TrackingSubjectItem getChildItem() {
        return childItem;
    }

    public List<TrackingSubjectItem> getChilds() {
        return childs;
    }

    public boolean isGroupItem() {
        return viewType == TrackingListExpandableAdapter.VIEW_TYPE_GROUP;
    }

    public boolean isChildItem() {
        return viewType == TrackingListExpandableAdapter.VIEW_TYPE_CHILD_REGION || viewType == TrackingListExpandableAdapter.VIEW_TYPE_CHILD_HOUSEHOLD || viewType == TrackingListExpandableAdapter.VIEW_TYPE_CHILD_MEMBER;
    }

    public boolean isChildRegionItem() {
        return viewType == TrackingListExpandableAdapter.VIEW_TYPE_CHILD_REGION;
    }

    public boolean isChildHouseholdItem() {
        return viewType == TrackingListExpandableAdapter.VIEW_TYPE_CHILD_HOUSEHOLD;
    }

    public boolean isChildMemberItem() {
        return viewType == TrackingListExpandableAdapter.VIEW_TYPE_CHILD_MEMBER;
    }
}
