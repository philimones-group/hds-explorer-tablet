package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.philimone.hds.explorer.R;

import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.followup.TrackingList;
import org.philimone.hds.explorer.widget.CirclePercentageBar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 8/10/16.
 */
public class TrackingListArrayAdapter extends ArrayAdapter {
    private List<TrackingList> trackingLists;
    private List<TrackingList> originalLists;
    private Context mContext;
    private User currentUser;

    public TrackingListArrayAdapter(Context context, List<TrackingList> objects){
        super(context, R.layout.tracking_list_item, objects);

        this.trackingLists = new ArrayList<>();
        this.originalLists = new ArrayList<>();
        this.trackingLists.addAll(objects);
        this.originalLists.addAll(objects);
        this.mContext = context;
        this.currentUser = Bootstrap.getCurrentUser();
    }

    public TrackingListArrayAdapter(Context context, TrackingList[] objects){
        super(context, R.layout.tracking_list_item, objects);

        this.trackingLists = new ArrayList<>();
        this.originalLists = new ArrayList<>();
        for (TrackingList tl : objects) {
            this.trackingLists.add(tl);
            this.originalLists.add(tl);
        }
        this.mContext = context;
        this.currentUser = Bootstrap.getCurrentUser();
    }

    @Override
    public int getCount() {
        return this.trackingLists.size();
    }

    public List<TrackingList> getTrackingLists(){
        return this.trackingLists;
    }

    @Override
    public TrackingList getItem(int position) {
        return trackingLists.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.tracking_list_item, parent, false);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.txtTrackListItemTitle);
        TextView txtModule = (TextView) rowView.findViewById(R.id.txtTrackListItemModule);
        TextView txtDetails = (TextView) rowView.findViewById(R.id.txtTrackListItemDetails);
        CirclePercentageBar pBar = (CirclePercentageBar) rowView.findViewById(R.id.pbarTrackListItem);

        DecimalFormat df = new DecimalFormat("#0.0");
        Log.d("test", ""+trackingLists.size());

        if (trackingLists.size()==0) return rowView;

        TrackingList trackingList = trackingLists.get(position);

        int completion = (int) (trackingList.getCompletionRate()*100);  // df.format(trackingList.getCompletionRate()*100D) + "%";

        txtTitle.setText(trackingList.getName() + ":  " + trackingList.getTitle());
        txtModule.setText(trackingList.getDetails());
        txtDetails.setText(mContext.getString(R.string.trackinglist_module_lbl) + ": " + currentUser.getModulesNamesAsText(trackingList.modules));
        pBar.setPercentageValue(completion);


        return rowView;
    }

    public void filterSubjects(String code){
        Log.d("filtering", ""+code);

        List<TrackingList> toRemove = new ArrayList<>();
        this.trackingLists.clear();
        this.trackingLists.addAll(originalLists);


        for (TrackingList trackingList : this.originalLists){

            //filter or remove from subjectItems
            if (!codeMatches(trackingList, code)){
                toRemove.add(trackingList);
            }
        }

        this.trackingLists.removeAll(toRemove);
    }

    public boolean codeMatches(TrackingList trackingList, String code){

        String codeRegex = ".*" + code.toLowerCase() + ".*";

        String title = trackingList.getTitle().toLowerCase();
        String name = trackingList.getName().toLowerCase();
        String details = trackingList.getDetails().toLowerCase();

        return title.matches(codeRegex) || name.matches(codeRegex) || details.matches(codeRegex);

    }
}
