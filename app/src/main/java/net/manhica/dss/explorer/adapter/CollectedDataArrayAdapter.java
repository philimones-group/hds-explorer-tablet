package net.manhica.dss.explorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.manhica.dss.explorer.R;
import net.manhica.dss.explorer.model.CollectedData;
import net.manhica.dss.explorer.model.Member;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 8/10/16.
 */
public class CollectedDataArrayAdapter extends ArrayAdapter {
    private List<CollectedDataItem> collectedDataList;
    private Context mContext;

    public CollectedDataArrayAdapter(Context context, List<CollectedDataItem> objects){
        super(context, R.layout.form_collected_item, objects);

        this.collectedDataList = new ArrayList<>();
        this.collectedDataList.addAll(objects);
        this.mContext = context;
    }

    public CollectedDataArrayAdapter(Context context, CollectedDataItem[] objects){
        super(context, R.layout.form_collected_item, objects);

        this.collectedDataList = new ArrayList<>();
        for (CollectedDataItem cd : objects) this.collectedDataList.add(cd);
        this.mContext = context;
    }

    public List<CollectedDataItem> getCollectedDataList(){
        return this.collectedDataList;
    }

    @Override
    public CollectedDataItem getItem(int position) {
        return collectedDataList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.form_collected_item, parent, false);

        TextView txtName = (TextView) rowView.findViewById(R.id.txtItem1);
        TextView txtForm = (TextView) rowView.findViewById(R.id.txtItem2);
        TextView txtExtra = (TextView) rowView.findViewById(R.id.txtItem3);

        CollectedDataItem cdi = collectedDataList.get(position);
        Member mb = cdi.getMember();
        CollectedData cd = cdi.getCollectedData();

        String processed = "0";

        txtName.setText(cd.getFormInstanceName());
        txtForm.setText(cd.getFormModule() + " -> " + cd.getFormId());
        txtExtra.setText(cd.getFormLastUpdatedDate());

        return rowView;
    }
}
