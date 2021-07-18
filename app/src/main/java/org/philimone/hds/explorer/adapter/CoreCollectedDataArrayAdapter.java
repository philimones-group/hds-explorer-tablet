package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.model.CoreCollectedData;

import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.utilities.StringUtil;

/**
 * Created by paul on 8/10/16.
 */
public class CoreCollectedDataArrayAdapter extends ArrayAdapter<CoreCollectedData> {
    private List<CoreCollectedData> collectedDataList;
    private boolean[] selectedList;
    private boolean multiSelectable = true;
    private Context mContext;

    public CoreCollectedDataArrayAdapter(Context context, List<CoreCollectedData> objects){
        super(context, R.layout.core_form_collected_item, objects);

        this.collectedDataList = new ArrayList<>();
        this.collectedDataList.addAll(objects);
        this.selectedList = new boolean[objects.size()];
        this.mContext = context;
    }

    public CoreCollectedDataArrayAdapter(Context context, CoreCollectedData[] objects){
        super(context, R.layout.form_collected_item, objects);

        this.collectedDataList = new ArrayList<>();
        for (CoreCollectedData cd : objects) this.collectedDataList.add(cd);
        this.selectedList = new boolean[objects.length];
        this.mContext = context;
    }

    public List<CoreCollectedData> getCollectedDataList(){
        return this.collectedDataList;
    }

    @Override
    public CoreCollectedData getItem(int position) {
        return collectedDataList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.core_form_collected_item, parent, false);

        TextView txtItem1 = (TextView) rowView.findViewById(R.id.txtItem1);
        TextView txtItem2 = (TextView) rowView.findViewById(R.id.txtItem2);
        TextView txtItem3 = (TextView) rowView.findViewById(R.id.txtItem3);
        CheckBox chkProcessed = (CheckBox) rowView.findViewById(R.id.chkProcessed);

        CoreCollectedData cd = collectedDataList.get(position);


        String createdDate = cd.createdDate==null ? "" : StringUtil.format(cd.createdDate, "yyyy-MM-dd HH:mm:ss");
        String updatedDate = cd.updatedDate==null ? "" : StringUtil.format(cd.updatedDate, "yyyy-MM-dd HH:mm:ss");
        String uploadedDate = cd.uploadedDate==null ? "" : StringUtil.format(cd.uploadedDate, "yyyy-MM-dd HH:mm:ss");

        boolean checked = selectedList[position];

        txtItem1.setText(cd.formEntityName);
        txtItem2.setText(this.mContext.getString(cd.formEntity.name));
        txtItem3.setText("Collected: "+createdDate+", Updated Date: "+updatedDate);
        chkProcessed.setChecked(checked);

        return rowView;
    }

    public void setCheckedOrUnchecked(int position) {
        selectedList[position] = !selectedList[position];
        notifyDataSetChanged();
    }

    public void setChecked(int position, boolean checked) {
        selectedList[position] = checked;
        notifyDataSetChanged();
    }

    public List<CoreCollectedData> getSelectedCollectedData() {
        List<CoreCollectedData> list = new ArrayList<>();

        for (int i = 0; i < selectedList.length; i++) {
            if (selectedList[i] == true) {
                list.add(collectedDataList.get(i));
            }
        }

        return list;
    }
}
