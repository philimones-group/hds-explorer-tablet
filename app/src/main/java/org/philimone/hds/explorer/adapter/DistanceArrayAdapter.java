package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.widget.member_details.Distance;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 8/10/16.
 */
public class DistanceArrayAdapter extends ArrayAdapter {
    private List<Distance> distanceList;
    private Context mContext;

    public DistanceArrayAdapter(Context context, List<Distance> objects){
        super(context, R.layout.gps_near_by_distance_item, objects);

        this.distanceList = new ArrayList<>();
        this.distanceList.addAll(objects);
        this.mContext = context;
    }

    public DistanceArrayAdapter(Context context, Distance[] objects){
        super(context, R.layout.gps_near_by_distance_item, objects);

        this.distanceList = new ArrayList<>();
        for (Distance distance : objects) this.distanceList.add(distance);
        this.mContext = context;
    }

    @Override
    public Distance getItem(int position) {
        return distanceList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.gps_near_by_distance_item, parent, false);

        TextView txtDistanceName = (TextView) rowView.findViewById(R.id.txtDistanceName);


        Distance distance = distanceList.get(position);
        txtDistanceName.setText(distance.getLabel());

        return rowView;
    }
}
