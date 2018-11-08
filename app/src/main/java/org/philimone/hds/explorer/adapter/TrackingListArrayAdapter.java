package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.philimone.hds.explorer.R;

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
    private Context mContext;

    public TrackingListArrayAdapter(Context context, List<TrackingList> objects){
        super(context, R.layout.tracking_list_item, objects);

        this.trackingLists = new ArrayList<>();
        this.trackingLists.addAll(objects);
        this.mContext = context;
    }

    public TrackingListArrayAdapter(Context context, TrackingList[] objects){
        super(context, R.layout.tracking_list_item, objects);

        this.trackingLists = new ArrayList<>();
        for (TrackingList tl : objects) this.trackingLists.add(tl);
        this.mContext = context;
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

        TrackingList trackingList = trackingLists.get(position);

        int completion = (int) (trackingList.getCompletionRate()*100);  // df.format(trackingList.getCompletionRate()*100D) + "%";

        txtTitle.setText(trackingList.getName() + ":  " + trackingList.getTitle());
        txtModule.setText(trackingList.getDetails());
        txtDetails.setText(mContext.getString(R.string.trackinglist_module_lbl) + ":" + trackingList.getModule());
        pBar.setPercentageValue(completion);


        return rowView;
    }
}
