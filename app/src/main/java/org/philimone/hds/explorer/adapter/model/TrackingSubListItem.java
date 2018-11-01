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
    private List<String> forms;

    public TrackingSubListItem(){
        this.forms = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public List<String> getForms() {
        return forms;
    }

    public void setForms(List<String> forms) {
        this.forms.addAll(forms);
    }

    public void setForms(String[] forms) {
        this.forms.addAll(Arrays.asList(forms));
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