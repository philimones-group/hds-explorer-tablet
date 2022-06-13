package org.philimone.hds.explorer.adapter.model;

import org.philimone.hds.explorer.model.followup.TrackingList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by paul on 11/4/17.
 */
public class TrackingSubListItem implements Serializable {
    private int id;
    private TrackingList trackingList;
    private String title;
    private boolean collapsed = true;

    public TrackingSubListItem(){
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public boolean isExpanded() {
        return !collapsed;
    }

    public void collapse(){
        this.collapsed = true;
    }

    public void expand(){
        this.collapsed = false;
    }

    public void toggleCollapseExpand(){
        if (collapsed) {
            expand();
        } else {
            collapse();
        }
    }

    public TrackingList getTrackingList() {
        return trackingList;
    }

    public void setTrackingList(TrackingList trackingList) {
        this.trackingList = trackingList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TrackingSubListItem){
            return ((TrackingSubListItem)o).id == this.id;
        }else
            return false;
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
