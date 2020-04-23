package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.model.CollectedDataItem;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.Member;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 8/10/16.
 */
public class CollectedDataArrayAdapter extends ArrayAdapter {
    private List<CollectedDataItem> collectedDataList;
    private boolean[] selectedList;
    private boolean multiSelectable;
    private Context mContext;

    public CollectedDataArrayAdapter(Context context, List<CollectedDataItem> objects){
        super(context, R.layout.form_collected_item, objects);

        this.collectedDataList = new ArrayList<>();
        this.collectedDataList.addAll(objects);
        this.selectedList = new boolean[objects.size()];
        this.mContext = context;
    }

    public CollectedDataArrayAdapter(Context context, CollectedDataItem[] objects){
        super(context, R.layout.form_collected_item, objects);

        this.collectedDataList = new ArrayList<>();
        for (CollectedDataItem cd : objects) this.collectedDataList.add(cd);
        this.selectedList = new boolean[objects.length];
        this.mContext = context;
    }

    /*
     * Will allow the adapter items to be selectable or not,
     * Enable or Disables selectability of the List
     */
    public void setMultiSelection(boolean value){
        this.multiSelectable = value;
        //update views by enabling first Checkbox


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
        CheckBox chkProcessed = (CheckBox) rowView.findViewById(R.id.chkProcessed);

        CollectedDataItem cdi = collectedDataList.get(position);
        //Member mb = cdi.getMember();
        CollectedData cd = cdi.getCollectedData();

        String processed = "0";

        txtName.setText(cd.getFormInstanceName());
        txtExtra.setText(cd.getFormLastUpdatedDate());
        chkProcessed.setChecked(cd.isFormFinalized());

        if (cdi.getForm()!=null){
            txtForm.setText(cd.getFormModule() + " -> " + cdi.getForm().getFormName());
        }else {
            txtForm.setText(cd.getFormModule() + " -> " + cd.getFormId());
        }


        return rowView;
    }
}
